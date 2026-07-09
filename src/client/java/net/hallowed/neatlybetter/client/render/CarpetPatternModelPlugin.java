package net.hallowed.neatlybetter.client.render;

import com.mojang.logging.LogUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Predicate;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBakedItemModel;
import net.fabricmc.fabric.api.client.renderer.v1.model.FabricBlockStateModel;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;

import net.hallowed.neatlybetter.data.CarpetPatternData;
import net.hallowed.neatlybetter.data.ChunkCarpetPatterns;
import net.hallowed.neatlybetter.init.ModData;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.sprite.MaterialBaker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.WoolCarpetBlock;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.resources.model.sprite.Material;

import net.minecraft.world.level.chunk.ChunkAccess;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class CarpetPatternModelPlugin implements ModelLoadingPlugin {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final String[] KNOWN_PATTERN_PATHS = {
            "base", "border", "curly_border",
            "stripe_bottom", "stripe_top", "stripe_left", "stripe_right",
            "stripe_center", "stripe_middle", "stripe_downright", "stripe_downleft",
            "small_stripes", "cross", "straight_cross",
            "diagonal_left", "diagonal_right", "diagonal_up_left", "diagonal_up_right",
            "half_vertical", "half_vertical_right", "half_horizontal", "half_horizontal_bottom",
            "square_bottom_left", "square_bottom_right", "square_top_left", "square_top_right",
            "triangle_bottom", "triangle_top", "triangles_bottom", "triangles_top",
            "circle", "rhombus", "bricks", "gradient", "gradient_up",
            "creeper", "skull", "flower", "mojang", "globe", "piglin", "flow", "guster"
    };

    private static final Set<String> WARNED_MISSING_PATTERNS =
            Collections.synchronizedSet(new HashSet<>());

    private static final Set<ChunkAccess> HOOKED_CHUNKS =
            Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    private static final float[][] UV_CORNERS = {
            {0f, 0f}, {0f, 1f}, {1f, 1f}, {1f, 0f}
    };

    @Override
    public void initialize(Context ctx) {
        ctx.modifyBlockModelAfterBake().register(
                ModelModifier.WRAP_PHASE,
                CarpetPatternModelPlugin::modify
        );
        ctx.modifyItemModelAfterBake().register(
                ModelModifier.WRAP_PHASE,
                CarpetPatternModelPlugin::modifyItem
        );
    }

    private static BlockStateModel modify(BlockStateModel model, ModelModifier.AfterBakeBlock.Context ctx) {
        if (ctx.state().getBlock() instanceof WoolCarpetBlock) {
            PatternMaterials materials = bakePatternMaterials(ctx.baker().materials());
            return new PatternedCarpetModel(model, materials);
        }
        return model;
    }

    private static ItemModel modifyItem(ItemModel model, ModelModifier.AfterBakeItem.Context ctx) {
        Item item = BuiltInRegistries.ITEM.getValue(ctx.itemId());
        if (item instanceof BlockItem blockItem && blockItem.getBlock() instanceof WoolCarpetBlock) {
            PatternMaterials materials = bakePatternMaterials(ctx.bakingContext().blockModelBaker().materials());
            return new PatternedCarpetItemModel(model, materials);
        }
        return model;
    }

    private static PatternMaterials bakePatternMaterials(MaterialBaker materials) {
        Map<String, Material.Baked> patternMaterials = new HashMap<>();
        for (String path : KNOWN_PATTERN_PATHS) {
            Material material = new Material(Identifier.fromNamespaceAndPath("neatly-better", "block/pattern/" + path));
            patternMaterials.put(path, materials.get(material, () -> "NeatlyBetter carpet pattern: " + path));
        }

        return new PatternMaterials(patternMaterials, null);
    }

    private record PatternMaterials(Map<String, Material.Baked> byPath, Material.Baked fallback) {
        Material.Baked resolve(BannerPatternLayers.Layer layer) {
            Optional<ResourceKey<BannerPattern>> key = layer.pattern().unwrapKey();
            if (key.isEmpty()) {
                return this.fallback;
            }

            String path = key.get().identifier().getPath();
            Material.Baked material = this.byPath.get(path);
            if (material == null) {
                if (WARNED_MISSING_PATTERNS.add(path)) {
                    LOGGER.info("[NeatlyBetter/CarpetPatterns] No carpet texture for pattern '{}', using fallback", path);
                }
                return this.fallback;
            }
            return material;
        }
    }

    private static void applyRotatedUv(QuadEmitter emitter, int rotationSteps) {
        int r = rotationSteps & 3;
        for (int i = 0; i < 4; i++) {
            float[] uv = UV_CORNERS[(i + r) & 3];
            emitter.uv(i, uv[0], uv[1]);
        }
    }

    private static void emitPatternLayer(QuadEmitter emitter, Material.Baked material,
                                         BannerPatternLayers.Layer layer, int layerIndex, int rotationSteps) {
        final float y = 0.0625f + (layerIndex * 0.000009999999999f);

        emitter.pos(0, 0.0f, y, 0.0f);
        emitter.pos(1, 0.0f, y, 1.0f);
        emitter.pos(2, 1.0f, y, 1.0f);
        emitter.pos(3, 1.0f, y, 0.0f);

        emitter.nominalFace(Direction.UP);
        emitter.cullFace(null);

        applyRotatedUv(emitter, rotationSteps);
        emitter.materialBake(material, MutableQuadView.BAKE_NORMALIZED);

        int tint = layer.color().getTextureDiffuseColor();
        emitter.color(tint, tint, tint, tint);

        emitter.emit();
    }

    private static @Nullable CarpetPatternData lookupPatternData(BlockPos pos) {
        ClientLevel clientLevel = Minecraft.getInstance().level;
        if (clientLevel == null) {
            return null;
        }

        ChunkAccess chunk = clientLevel.getChunk(pos);
        hookChunkIfNeeded(chunk);

        ChunkCarpetPatterns patterns = chunk.getAttached(ModData.CARPET_PATTERN_DATA);
        if (patterns == null) {
            return null;
        }

        CarpetPatternData data = patterns.get(pos);
        if (data == null || data.isEmpty()) {
            return null;
        }

        return data;
    }

    private static void hookChunkIfNeeded(ChunkAccess chunk) {
        if (HOOKED_CHUNKS.add(chunk)) {
            chunk.onAttachedSet(ModData.CARPET_PATTERN_DATA).register(
                    CarpetPatternModelPlugin::onCarpetPatternsChanged
            );
        }
    }

    private static void onCarpetPatternsChanged(@Nullable ChunkCarpetPatterns oldPatterns,
                                                @Nullable ChunkCarpetPatterns newPatterns) {
        ClientLevel clientLevel = Minecraft.getInstance().level;
        if (clientLevel == null) {
            return;
        }

        Set<BlockPos> changedPositions = new HashSet<>();
        if (oldPatterns != null) {
            changedPositions.addAll(oldPatterns.getAll().keySet());
        }
        if (newPatterns != null) {
            changedPositions.addAll(newPatterns.getAll().keySet());
        }

        for (BlockPos pos : changedPositions) {
            BlockState state = clientLevel.getBlockState(pos);
            clientLevel.sendBlockUpdated(pos, state, state, 0);
        }
    }

    private record PatternedCarpetModel(BlockStateModel wrapped, PatternMaterials materials)
            implements BlockStateModel, FabricBlockStateModel {

        @Override
        public void emitQuads(@NonNull QuadEmitter emitter, @NonNull BlockAndTintGetter level, @NonNull BlockPos pos,
                              @NonNull BlockState state, @NonNull RandomSource random,
                              @NonNull Predicate<@Nullable Direction> cullTest) {
            this.wrapped.emitQuads(emitter, level, pos, state, random, cullTest);

            CarpetPatternData data = lookupPatternData(pos);
            if (data == null || data.isEmpty()) {
                return;
            }

            List<BannerPatternLayers.Layer> layers = data.patterns().layers();
            int rotation = data.rotation();
            for (int i = 0; i < layers.size(); i++) {
                BannerPatternLayers.Layer layer = layers.get(i);
                emitPatternLayer(emitter, this.materials.resolve(layer), layer, i, rotation);
            }
        }

        @Override
        public @Nullable Object createGeometryKey(@NonNull BlockAndTintGetter level, @NonNull BlockPos pos,
                                                  @NonNull BlockState state, @NonNull RandomSource random) {
            Object wrappedKey = this.wrapped.createGeometryKey(level, pos, state, random);
            if (wrappedKey == null) {
                return null;
            }

            CarpetPatternData data = lookupPatternData(pos);
            if (data == null || data.isEmpty()) {
                return wrappedKey;
            }

            return new CarpetGeometryKey(wrappedKey, data);
        }

        private record CarpetGeometryKey(Object wrappedKey, CarpetPatternData data) { }

        @Override
        public Material.@NonNull Baked particleMaterial(@NonNull BlockAndTintGetter level, @NonNull BlockPos pos, @NonNull BlockState state) {
            return this.wrapped.particleMaterial(level, pos, state);
        }

        @Override
        public void collectParts(@NonNull RandomSource random, @NonNull List<BlockStateModelPart> parts) {
            this.wrapped.collectParts(random, parts);
        }

        @Override
        public Material.@NonNull Baked particleMaterial() {
            return this.wrapped.particleMaterial();
        }

        @Override
        public int materialFlags() {
            return this.wrapped.materialFlags();
        }
    }

    private static final class PatternedCarpetItemModel extends WrapperBakedItemModel {
        private final PatternMaterials materials;

        private PatternedCarpetItemModel(ItemModel wrapped, PatternMaterials materials) {
            super(wrapped);
            this.materials = materials;
        }

        @Override
        public void update(@NonNull ItemStackRenderState output, @NonNull ItemStack item, @NonNull ItemModelResolver resolver,
                           @NonNull ItemDisplayContext displayContext, @Nullable ClientLevel level,
                           @Nullable ItemOwner owner, int seed) {
            CarpetPatternData data = item.getOrDefault(ModData.CARPET_PATTERNS, CarpetPatternData.EMPTY);
            output.appendModelIdentityElement(data);

            super.update(output, item, resolver, displayContext, level, owner, seed);

            if (data.isEmpty()) {
                return;
            }

            List<BannerPatternLayers.Layer> layers = data.patterns().layers();
            if (layers.isEmpty()) {
                return;
            }

            QuadEmitter emitter = output.layers[0].emitter();
            int rotation = data.rotation();
            for (int i = 0; i < layers.size(); i++) {
                BannerPatternLayers.Layer layer = layers.get(i);
                emitPatternLayer(emitter, this.materials.resolve(layer), layer, i, rotation);
            }
        }
    }
}
package net.hallowed.neatlybetter.client.render;

import com.mojang.logging.LogUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBakedItemModel;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.model.FabricBlockStateModel;

import net.hallowed.neatlybetter.data.CarpetPatternData;
import net.hallowed.neatlybetter.data.ChunkCarpetPatterns;
import net.hallowed.neatlybetter.registry.ModData;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.sprite.Material;
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
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.WoolCarpetBlock;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

/**
 * Draws loom patterns on carpets, in the world and as items.
 *
 * <p>World: patterns live in a per-chunk attachment. Chunk meshes are built on worker threads, so the model never
 * touches the level or its chunks; it reads {@link #CLIENT_PATTERNS}, a thread-safe copy maintained on the main
 * thread from chunk load/unload and attachment-change events. Only carpets whose pattern actually changed are
 * re-meshed.
 *
 * <p>Item: the pattern quads are added to the carpet item's mesh. They sit visibly above the top face; at the old
 * 0.00001 offset they lost the depth test in the GUI (orthographic projection) and the inventory icon looked plain.
 */
public class CarpetPatternModelPlugin implements ModelLoadingPlugin {

    private static final Logger LOGGER = LogUtils.getLogger();

    /** Pattern ids with a texture at {@code neatly-better:block/pattern/<id>}. */
    private static final String[] KNOWN_PATTERN_PATHS = {
            "border", "curly_border",
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

    /** Carpet top face height. */
    private static final float TOP_Y = 1f / 16f;
    /** Gap between stacked pattern layers: world (perspective, fine depth) vs. items (GUI is orthographic). */
    private static final float BLOCK_LAYER_STEP = 0.0001f;
    private static final float ITEM_LAYER_STEP = 0.001f;

    private static final float[][] UV_CORNERS = {
            {0f, 0f}, {0f, 1f}, {1f, 1f}, {1f, 0f}
    };

    private static final Set<String> WARNED_MISSING_PATTERNS = Collections.synchronizedSet(new HashSet<>());

    /** Chunk key → (block pos long → pattern). Inner maps are immutable and replaced whole. */
    private static final Map<Long, Map<Long, CarpetPatternData>> CLIENT_PATTERNS = new ConcurrentHashMap<>();

    // ══════════════════════════════════════════════════════════════════════
    //  Registration
    // ══════════════════════════════════════════════════════════════════════

    @Override
    public void initialize(Context ctx) {
        ctx.modifyBlockModelAfterBake().register(ModelModifier.WRAP_PHASE, CarpetPatternModelPlugin::modify);
        ctx.modifyItemModelAfterBake().register(ModelModifier.WRAP_PHASE, CarpetPatternModelPlugin::modifyItem);
    }

    /** Client chunk lifecycle hooks. Call once from client init. */
    public static void registerClientEvents() {
        ClientChunkEvents.CHUNK_LOAD.register((level, chunk) -> {
            chunk.onAttachedSet(ModData.CARPET_PATTERN_DATA).register(
                    (oldValue, newValue) -> updateChunk(chunk, newValue));
            updateChunk(chunk, chunk.getAttached(ModData.CARPET_PATTERN_DATA));
        });
        ClientChunkEvents.CHUNK_UNLOAD.register((level, chunk) -> CLIENT_PATTERNS.remove(chunk.getPos().pack()));
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Client pattern cache (main thread writes, mesh workers read)
    // ══════════════════════════════════════════════════════════════════════

    private static void updateChunk(LevelChunk chunk, @Nullable ChunkCarpetPatterns patterns) {
        long chunkKey = chunk.getPos().pack();

        Map<Long, CarpetPatternData> next = new HashMap<>();
        if (patterns != null) {
            patterns.getAll().forEach((pos, data) -> {
                if (!data.isEmpty()) next.put(pos.asLong(), data);
            });
        }
        Map<Long, CarpetPatternData> previous = next.isEmpty()
                ? CLIENT_PATTERNS.remove(chunkKey)
                : CLIENT_PATTERNS.put(chunkKey, Map.copyOf(next));
        if (previous == null) previous = Map.of();

        // Re-mesh only carpets whose pattern changed
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;
        Set<Long> changed = new HashSet<>();
        for (Map.Entry<Long, CarpetPatternData> e : next.entrySet()) {
            if (!e.getValue().equals(previous.get(e.getKey()))) changed.add(e.getKey());
        }
        for (Long key : previous.keySet()) {
            if (!next.containsKey(key)) changed.add(key);
        }
        for (long key : changed) {
            BlockPos pos = BlockPos.of(key);
            BlockState state = level.getBlockState(pos);
            level.sendBlockUpdated(pos, state, state, 0);
        }
    }

    private static @Nullable CarpetPatternData lookupPatternData(BlockPos pos) {
        Map<Long, CarpetPatternData> chunk = CLIENT_PATTERNS.get(ChunkPos.pack(pos.getX() >> 4, pos.getZ() >> 4));
        return chunk == null ? null : chunk.get(pos.asLong());
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Model wrapping
    // ══════════════════════════════════════════════════════════════════════

    private static BlockStateModel modify(BlockStateModel model, ModelModifier.AfterBakeBlock.Context ctx) {
        if (ctx.state().getBlock() instanceof WoolCarpetBlock) {
            return new PatternedCarpetModel(model, bakePatternMaterials(ctx.baker().materials()));
        }
        return model;
    }

    private static ItemModel modifyItem(ItemModel model, ModelModifier.AfterBakeItem.Context ctx) {
        Item item = BuiltInRegistries.ITEM.getValue(ctx.itemId());
        if (item instanceof BlockItem blockItem && blockItem.getBlock() instanceof WoolCarpetBlock) {
            return new PatternedCarpetItemModel(model, bakePatternMaterials(ctx.bakingContext().blockModelBaker().materials()));
        }
        return model;
    }

    private static PatternMaterials bakePatternMaterials(MaterialBaker materials) {
        Map<String, Material.Baked> byPath = new HashMap<>();
        for (String path : KNOWN_PATTERN_PATHS) {
            Material material = new Material(Identifier.fromNamespaceAndPath("neatly-better", "block/pattern/" + path));
            byPath.put(path, materials.get(material, () -> "NeatlyBetter carpet pattern: " + path));
        }
        return new PatternMaterials(Map.copyOf(byPath));
    }

    private record PatternMaterials(Map<String, Material.Baked> byPath) {
        /** Null for patterns without a carpet texture (e.g. from other mods); those layers are skipped. */
        Material.@Nullable Baked resolve(BannerPatternLayers.Layer layer) {
            Optional<ResourceKey<BannerPattern>> key = layer.pattern().unwrapKey();
            if (key.isEmpty()) return null;

            String path = key.get().identifier().getPath();
            Material.Baked material = byPath.get(path);
            if (material == null && WARNED_MISSING_PATTERNS.add(path)) {
                LOGGER.info("[NeatlyBetter/CarpetPatterns] No carpet texture for pattern '{}', skipping it", path);
            }
            return material;
        }
    }

    private static void emitPatternLayers(QuadEmitter emitter, PatternMaterials materials,
                                          CarpetPatternData data, float layerStep) {
        List<BannerPatternLayers.Layer> layers = data.patterns().layers();
        int rotation = data.rotation() & 3;
        for (int i = 0; i < layers.size(); i++) {
            BannerPatternLayers.Layer layer = layers.get(i);
            Material.Baked material = materials.resolve(layer);
            if (material == null) continue;

            float y = TOP_Y + layerStep * (i + 1);
            emitter.pos(0, 0f, y, 0f);
            emitter.pos(1, 0f, y, 1f);
            emitter.pos(2, 1f, y, 1f);
            emitter.pos(3, 1f, y, 0f);
            emitter.nominalFace(Direction.UP);
            emitter.cullFace(null);

            for (int v = 0; v < 4; v++) {
                float[] uv = UV_CORNERS[(v + rotation) & 3];
                emitter.uv(v, uv[0], uv[1]);
            }
            emitter.materialBake(material, MutableQuadView.BAKE_NORMALIZED);

            int tint = layer.color().getTextureDiffuseColor();
            emitter.color(tint, tint, tint, tint);
            emitter.emit();
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
            if (data != null) emitPatternLayers(emitter, this.materials, data, BLOCK_LAYER_STEP);
        }

        @Override
        public @Nullable Object createGeometryKey(@NonNull BlockAndTintGetter level, @NonNull BlockPos pos,
                                                  @NonNull BlockState state, @NonNull RandomSource random) {
            Object wrappedKey = this.wrapped.createGeometryKey(level, pos, state, random);
            if (wrappedKey == null) return null;

            CarpetPatternData data = lookupPatternData(pos);
            return data == null ? wrappedKey : new CarpetGeometryKey(wrappedKey, data);
        }

        private record CarpetGeometryKey(Object wrappedKey, CarpetPatternData data) {}

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
            this.materials = Objects.requireNonNull(materials);
        }

        @Override
        public void update(@NonNull ItemStackRenderState output, @NonNull ItemStack item, @NonNull ItemModelResolver resolver,
                           @NonNull ItemDisplayContext displayContext, @Nullable ClientLevel level,
                           @Nullable ItemOwner owner, int seed) {
            super.update(output, item, resolver, displayContext, level, owner, seed);

            CarpetPatternData data = item.get(ModData.CARPET_PATTERNS);
            if (data == null || data.isEmpty()) return;

            // Distinguishes patterned stacks in the GUI item atlas cache.
            output.appendModelIdentityElement(data);
            emitPatternLayers(output.layers[0].emitter(), this.materials, data, ITEM_LAYER_STEP);
        }
    }
}

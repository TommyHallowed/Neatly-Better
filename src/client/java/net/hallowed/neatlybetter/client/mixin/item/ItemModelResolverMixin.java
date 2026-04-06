package net.hallowed.neatlybetter.client.mixin.item;

import net.hallowed.neatlybetter.init.ModDataComponents;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.WeakHashMap;

@Mixin(ItemModelResolver.class)
public class ItemModelResolverMixin {

    @Unique
    private static final WeakHashMap<BakedQuad, BakedQuad> neatlybetter$glowCache = new WeakHashMap<>();

    @Inject(method = "appendItemLayers", at = @At("RETURN"))
    private void neatlybetter$applyEmissiveTrims(
            ItemStackRenderState output, ItemStack item,
            ItemDisplayContext displayContext, Level level,
            ItemOwner owner, int seed, CallbackInfo ci
    ) {
        if (!item.getOrDefault(ModDataComponents.EMISSIVE_TRIM, false)) {
            return;
        }

        for (ItemStackRenderState.LayerRenderState layer : output.layers) {
            List<BakedQuad> quads = layer.prepareQuadList();

            for (int i = 0; i < quads.size(); i++) {
                BakedQuad quad = quads.get(i);

                // sprite is now accessed via materialInfo()
                if (quad.materialInfo().sprite().contents().name().getPath().contains("trims/items/")) {
                    BakedQuad glowing = neatlybetter$glowCache.computeIfAbsent(quad, q -> {
                        BakedQuad.MaterialInfo oldInfo = q.materialInfo();
                        // Rebuild MaterialInfo with lightEmission = 15
                        BakedQuad.MaterialInfo emissiveInfo = new BakedQuad.MaterialInfo(
                                oldInfo.sprite(),
                                oldInfo.layer(),
                                oldInfo.itemRenderType(),
                                oldInfo.tintIndex(),
                                oldInfo.shade(),
                                15
                        );
                        return new BakedQuad(
                                q.position0(), q.position1(), q.position2(), q.position3(),
                                q.packedUV0(), q.packedUV1(), q.packedUV2(), q.packedUV3(),
                                q.direction(), emissiveInfo
                        );
                    });
                    quads.set(i, glowing);
                }
            }
        }
    }
}
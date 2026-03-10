package net.hallowed.oldways.client.mixin.item;

import net.hallowed.oldways.init.ModDataComponents;
import net.minecraft.client.renderer.block.model.BakedQuad;
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
public class ItemModelManagerMixin {

    /**
     * Cache: original BakedQuad → glowing BakedQuad.
     * WeakHashMap ensures entries are cleaned up when the original is GC'd
     * (e.g., after a resource pack reload replaces quads).
     */
    @Unique
    private static final WeakHashMap<BakedQuad, BakedQuad> oldways$glowCache = new WeakHashMap<>();

    @Inject(method = "appendItemLayers", at = @At("RETURN"))
    private void ow$applyEmissiveTrims(
            ItemStackRenderState renderState, ItemStack stack,
            ItemDisplayContext displayContext, Level world,
            ItemOwner heldItemContext, int seed, CallbackInfo ci
    ) {
        if (!stack.getOrDefault(ModDataComponents.EMISSIVE_TRIM, false)) {
            return;
        }

        for (ItemStackRenderState.LayerRenderState layer : renderState.layers) {
            List<BakedQuad> quads = layer.prepareQuadList();

            for (int i = 0; i < quads.size(); i++) {
                BakedQuad quad = quads.get(i);

                if (quad.sprite().contents().name().getPath().contains("trims/items/")) {
                    // Cache lookup — only allocates once per unique quad
                    BakedQuad glowing = oldways$glowCache.computeIfAbsent(quad, q ->
                            new BakedQuad(
                                    q.position0(), q.position1(), q.position2(), q.position3(),
                                    q.packedUV0(), q.packedUV1(), q.packedUV2(), q.packedUV3(),
                                    q.tintIndex(), q.direction(), q.sprite(), q.shade(),
                                    15
                            )
                    );
                    quads.set(i, glowing);
                }
            }
        }
    }
}
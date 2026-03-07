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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ItemModelResolver.class)
public class ItemModelManagerMixin {

    @Inject(method = "appendItemLayers", at = @At("RETURN"))
    private void ow$applyEmissiveTrims(ItemStackRenderState renderState, ItemStack stack, ItemDisplayContext displayContext, Level world, ItemOwner heldItemContext, int seed, CallbackInfo ci) {

        // 1. Only proceed if the item has our custom component
        if (!stack.getOrDefault(ModDataComponents.EMISSIVE_TRIM, false)) {
            return;
        }

        // 2. Loop through the rendering layers using your Access Widener
        for (ItemStackRenderState.LayerRenderState layer : renderState.layers) {
            // 3. Get the actual mutable list of 3D pixels (Quads) that will be drawn
            List<BakedQuad> quads = layer.prepareQuadList();

            for (int i = 0; i < quads.size(); i++) {
                BakedQuad quad = quads.get(i);

                // 4. Check if this specific quad belongs to Trimmable Tools
                if (quad.sprite().contents().name().getPath().contains("trims/items/")) {

                    // 5. Recreate the exact same quad, but force 'lightEmission' to 15!
                    BakedQuad glowingQuad = new BakedQuad(
                            quad.position0(), quad.position1(), quad.position2(), quad.position3(),
                            quad.packedUV0(), quad.packedUV1(), quad.packedUV2(), quad.packedUV3(),
                            quad.tintIndex(), quad.direction(), quad.sprite(), quad.shade(),
                            15 // <-- THIS is natively supported by Vanilla, Sodium, and Iris!
                    );

                    // 6. Replace the dark quad with the glowing quad in the list
                    quads.set(i, glowingQuad);
                }
            }
        }
    }
}
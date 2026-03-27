package net.hallowed.neatlybetter.client.mixin.compat;

import net.hallowed.neatlybetter.init.ModBlocks;

import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BedBlock;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Compat mixin for Better Block Entities (BBE).
 * BBE bakes beds into the chunk mesh via BBEEmitter.emitBed(), bypassing
 * vanilla BedRenderer entirely. This redirect swaps the material for our
 * rainbow bed so it renders with the correct animated texture.

 * Only applied when BBE is loaded (controlled by NTCompatMixinPlugin).
 */
@Mixin(targets = "betterblockentities.client.chunk.pipeline.BBEEmitter")
public abstract class BBEBedMixin {

    @Unique
    private static final ThreadLocal<Boolean> NEATLY$IS_RAINBOW = ThreadLocal.withInitial(() -> false);

    @Unique
    private static Material neatlybetter$rainbowMaterial;

    /**
     * Intercept BedBlock.getColor() to detect if the bed being emitted is our rainbow bed.
     * Sets a thread-local flag so the next redirect can swap the material.
     */
    @Redirect(
            method = "emitBed",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/BedBlock;getColor()Lnet/minecraft/world/item/DyeColor;"
            )
    )
    private static DyeColor neatlybetter$checkRainbow(BedBlock bed) {
        if (bed == ModBlocks.RAINBOW_BED) {
            NEATLY$IS_RAINBOW.set(true);
        }
        return bed.getColor();
    }

    /**
     * Intercept Sheets.getBedMaterial() to swap to our rainbow material when the flag is set.
     * The rainbow texture is in the beds atlas with animation mcmeta, so it animates
     * automatically even in BBE's baked chunk mesh.
     */
    @Redirect(
            method = "emitBed",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/Sheets;getBedMaterial(Lnet/minecraft/world/item/DyeColor;)Lnet/minecraft/client/resources/model/Material;"
            )
    )
    private static Material neatlybetter$swapBBEBedMaterial(DyeColor color) {
        if (NEATLY$IS_RAINBOW.get()) {
            NEATLY$IS_RAINBOW.set(false);
            if (neatlybetter$rainbowMaterial == null) {
                neatlybetter$rainbowMaterial = new Material(
                        Sheets.BED_SHEET,
                        Identifier.fromNamespaceAndPath("neatly-better", "entity/bed/rainbow")
                );
            }
            return neatlybetter$rainbowMaterial;
        }
        return Sheets.getBedMaterial(color);
    }
}

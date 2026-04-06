package net.hallowed.neatlybetter.client.mixin.compat;

import net.hallowed.neatlybetter.init.ModBlocks;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BedBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "betterblockentities.client.chunk.pipeline.BBEEmitter")
public abstract class BBEBedMixin {

    @Unique
    private static final ThreadLocal<Boolean> NEATLYBETTER$IS_RAINBOW = ThreadLocal.withInitial(() -> false);

    @Unique
    private static SpriteId neatlybetter$rainbowSpriteId;

    @Redirect(
            method = "emitBed",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/BedBlock;getColor()Lnet/minecraft/world/item/DyeColor;"
            )
    )
    private static DyeColor neatlybetter$checkRainbow(BedBlock bed) {
        if (bed == ModBlocks.RAINBOW_BED) {
            NEATLYBETTER$IS_RAINBOW.set(true);
        }
        return bed.getColor();
    }

    @Redirect(
            method = "emitBed",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/Sheets;getBedSprite(Lnet/minecraft/world/item/DyeColor;)Lnet/minecraft/client/resources/model/sprite/SpriteId;"
            )
    )
    private static SpriteId neatlybetter$swapBBEBedSpriteId(DyeColor color) {
        if (NEATLYBETTER$IS_RAINBOW.get()) {
            NEATLYBETTER$IS_RAINBOW.set(false);
            if (neatlybetter$rainbowSpriteId == null) {
                neatlybetter$rainbowSpriteId = new SpriteId(
                        Sheets.BED_SHEET,
                        Identifier.fromNamespaceAndPath("neatly-better", "entity/bed/rainbow")
                );
            }
            return neatlybetter$rainbowSpriteId;
        }
        return Sheets.getBedSprite(color);
    }
}
package net.hallowed.oldways.mixin.block;

import net.minecraft.block.ComposterBlock;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ComposterBlock.class)
public abstract class ComposterBlockMixin {

    @Inject(method = "registerDefaultCompostableItems", at = @At("TAIL"))
    private static void oldways$addPoisonousPotato(CallbackInfo ci) {
        ComposterBlock.ITEM_TO_LEVEL_INCREASE_CHANCE.put(Items.POISONOUS_POTATO, 0.5f);
    }
}
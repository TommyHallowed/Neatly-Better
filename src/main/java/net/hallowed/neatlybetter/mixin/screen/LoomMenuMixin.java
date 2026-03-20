package net.hallowed.neatlybetter.mixin.screen;

import net.minecraft.world.inventory.LoomMenu;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;


@Mixin(LoomMenu.class)
public abstract class LoomMenuMixin {

    @ModifyConstant(
            method = "slotsChanged(Lnet/minecraft/world/Container;)V",
            constant = @Constant(intValue = 6)
    )
    private int neatlybetter$raisePatternCap(int original) {
        return 16;
    }
}
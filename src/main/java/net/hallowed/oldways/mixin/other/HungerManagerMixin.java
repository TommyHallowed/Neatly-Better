package net.hallowed.oldways.mixin.other;

import net.minecraft.entity.player.HungerManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(HungerManager.class)
public abstract class HungerManagerMixin {

    @ModifyConstant(
            method = "update(Lnet/minecraft/server/network/ServerPlayerEntity;)V",
            constant = @Constant(intValue = 18),
            require = 0
    )
    private int slowHealStart(int original) {
        return 10;
    }

    @ModifyConstant(
            method = "update(Lnet/minecraft/server/network/ServerPlayerEntity;)V",
            constant = @Constant(intValue = 10),
            require = 0
    )
    private int fastHealInterval(int original) {
        return 40;
    }

    @ModifyConstant(
            method = "update(Lnet/minecraft/server/network/ServerPlayerEntity;)V",
            constant = @Constant(floatValue = 6.0F),
            require = 0
    )
    private float exhaustionPerHp(float original) {
        return 3.0F;
    }
}

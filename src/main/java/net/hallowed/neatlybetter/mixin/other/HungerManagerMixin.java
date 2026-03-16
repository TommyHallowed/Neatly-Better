package net.hallowed.neatlybetter.mixin.other;

import net.minecraft.world.food.FoodData;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(FoodData.class)
public abstract class HungerManagerMixin {

    @ModifyConstant(
            method = "tick(Lnet/minecraft/server/level/ServerPlayer;)V",
            constant = @Constant(intValue = 18),
            require = 0
    )
    private int slowHealStart(int original) {
        return 10;
    }

    @ModifyConstant(
            method = "tick(Lnet/minecraft/server/level/ServerPlayer;)V",
            constant = @Constant(intValue = 10),
            require = 0
    )
    private int fastHealInterval(int original) {
        return 40;
    }

    @ModifyConstant(
            method = "tick(Lnet/minecraft/server/level/ServerPlayer;)V",
            constant = @Constant(floatValue = 6.0F),
            require = 0
    )
    private float exhaustionPerHp(float original) {
        return 3.0F;
    }
}

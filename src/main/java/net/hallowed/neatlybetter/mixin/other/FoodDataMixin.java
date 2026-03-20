package net.hallowed.neatlybetter.mixin.other;

import net.hallowed.neatlybetter.config.NTServerConfig;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(FoodData.class)
public abstract class FoodDataMixin {

    @ModifyConstant(
            method = "tick(Lnet/minecraft/server/level/ServerPlayer;)V",
            constant = @Constant(intValue = 18),
            require = 0
    )
    private int slowHealStart(int original) {
        if (!NTServerConfig.CONFIG.hungerMechanics.get()) return original;
        return 10;
    }

    @ModifyConstant(
            method = "tick(Lnet/minecraft/server/level/ServerPlayer;)V",
            constant = @Constant(intValue = 10),
            require = 0
    )
    private int neatlybetter$fastHealInterval(int original, ServerPlayer player) {
        if (!NTServerConfig.CONFIG.hungerMechanics.get()) return original;
        return switch (player.level().getDifficulty()) {
            case HARD -> 40;
            case NORMAL -> 25;
            default -> original;
        };
    }

    @ModifyConstant(
            method = "tick(Lnet/minecraft/server/level/ServerPlayer;)V",
            constant = @Constant(floatValue = 6.0F),
            require = 0
    )
    private float neatlybetter$exhaustionPerHp(float original) {
        if (!NTServerConfig.CONFIG.hungerMechanics.get()) return original;
        return 3.0F;
    }
}
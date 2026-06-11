package net.hallowed.neatlybetter.mixin.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.hallowed.neatlybetter.config.NTServerConfig;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SweetBerryBushBlock.class)
public class SweetBerryBushBlockMixin {

    @Inject(method = "randomTick", at = @At("RETURN"))
    private void neatlybetter$onSweetBerryRandomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {

        if (!NTServerConfig.CONFIG.rainIncreasesCropGrowth.get()) return;
        if (!level.isRainingAt(pos.above())) return;

        SweetBerryBushBlock self = (SweetBerryBushBlock)(Object)this;

        for (int i = 0; i < 2; i++) {
            if (random.nextInt(5) == 0) {
                try {
                    self.performBonemeal(level, random, pos, state);
                } catch (Throwable ignored) {}
            }
        }
    }

    @WrapOperation(
            method = "entityInside",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z")
    )
    private boolean neatlybetter$leggingsProtectFromBush(Entity entity, ServerLevel serverLevel, DamageSource damageSource, float amount, Operation<Boolean> original) {
        if (entity instanceof LivingEntity livingEntity) {
            if (!livingEntity.getItemBySlot(EquipmentSlot.LEGS).isEmpty()) {
                return false;
            }
        }
        return original.call(entity, serverLevel, damageSource, amount);
    }
}

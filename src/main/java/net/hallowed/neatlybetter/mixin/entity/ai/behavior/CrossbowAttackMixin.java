package net.hallowed.neatlybetter.mixin.entity.ai.behavior;

import net.hallowed.neatlybetter.config.NTServerConfig;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.CrossbowAttack;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CrossbowAttack.class)
public abstract class CrossbowAttackMixin {

    @Unique
    private int neatlybetter$shieldHoldoff = 0;

    @Inject(
            method = "crossbowAttack(Lnet/minecraft/world/entity/Mob;Lnet/minecraft/world/entity/LivingEntity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/monster/RangedAttackMob;performRangedAttack(Lnet/minecraft/world/entity/LivingEntity;F)V"
            ),
            cancellable = true
    )
    private void neatlybetter$dontShootShields(Mob body, LivingEntity target, CallbackInfo ci) {
        if (!NTServerConfig.CONFIG.rangedMobShieldHoldoff.get()) return;
        if (body.level().getDifficulty() != Difficulty.HARD) {
            this.neatlybetter$shieldHoldoff = 0;
            return;
        }

        if (target == null) return;

        if (target.isBlocking()) {
            ItemStack blockingWith = target.getItemBlockingWith();
            assert blockingWith != null;
            if (blockingWith.is(ItemTags.SWORDS)) return;

            this.neatlybetter$shieldHoldoff = 4;
            ci.cancel();
            return;
        }

        if (this.neatlybetter$shieldHoldoff > 0) {
            this.neatlybetter$shieldHoldoff--;
            ci.cancel();
        }
    }
}
package net.hallowed.neatlybetter.mixin.entity.generic;

import net.hallowed.neatlybetter.duck.FireTouchTracker;
import net.hallowed.neatlybetter.feature.combat.SoulFire;

import net.minecraft.world.entity.Entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class SoulFireEntityMixin implements FireTouchTracker {

    @Shadow private int remainingFireTicks;

    @Unique private long neatlybetter$fireTouchTime = Long.MIN_VALUE;
    @Unique private boolean neatlybetter$fireTouchSoul;
    @Unique private long neatlybetter$predictTime = Long.MIN_VALUE;
    @Unique private boolean neatlybetter$predictSoul;

    @Override
    public void neatlybetter$predictFireKind(boolean soul, long gameTime) {
        this.neatlybetter$predictTime = gameTime;
        this.neatlybetter$predictSoul = soul;
    }

    @Override
    public Boolean neatlybetter$predictedFireKind(long since) {
        return this.neatlybetter$predictTime > since ? this.neatlybetter$predictSoul : null;
    }

    @Override
    public void neatlybetter$markFireTouch(boolean soul, long gameTime) {
        this.neatlybetter$fireTouchTime = gameTime;
        this.neatlybetter$fireTouchSoul = soul;
    }

    @Override
    public boolean neatlybetter$touchedFireAt(long gameTime) {
        return this.neatlybetter$fireTouchTime == gameTime;
    }

    @Override
    public boolean neatlybetter$lastTouchWasSoul() {
        return this.neatlybetter$fireTouchSoul;
    }

    /** Burning damage: soul fire hurts like the soul fire block (2) instead of 1. */
    @ModifyArg(
            method = "baseTick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z"),
            index = 2
    )
    private float neatlybetter$soulBurnDamage(float damage) {
        return SoulFire.isSoulBurning((Entity) (Object) this) ? SoulFire.SOUL_BURN_DAMAGE : damage;
    }

    /** Every ignition goes through here; only one that actually raises the fire ticks decides the kind. */
    @Inject(method = "igniteForTicks", at = @At("HEAD"))
    private void neatlybetter$decideFireKind(int numberOfTicks, CallbackInfo ci) {
        if (this.remainingFireTicks < numberOfTicks) SoulFire.onIgnite((Entity) (Object) this);
    }
}

package net.hallowed.neatlybetter.mixin.entity.hostile;

import net.hallowed.neatlybetter.config.NTServerConfig;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.monster.illager.Evoker;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Vex.class)
public abstract class VexMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void neatlybetter$dieWhenEvokerOwnerDies(CallbackInfo ci) {
        if (!NTServerConfig.CONFIG.vexDiesAfterSummoner.get()) return;
        Vex self = (Vex)(Object)this;
        if (self.level().isClientSide()) return;

        Mob owner = self.getOwner();
        if (owner instanceof Evoker && !owner.isAlive()) {
            self.hurt(self.damageSources().magic(), Float.MAX_VALUE);
        }
    }
}

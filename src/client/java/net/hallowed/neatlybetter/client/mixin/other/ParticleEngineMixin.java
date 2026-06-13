package net.hallowed.neatlybetter.client.mixin.other;

import net.hallowed.neatlybetter.config.NTServerConfig;
import net.hallowed.neatlybetter.handler.LegacyCombatHandler;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {

    @Inject(method = "makeParticle", at = @At("HEAD"), cancellable = true)
    private <T extends ParticleOptions> void cancelSweepParticles(T options, double x, double y, double z,
                                                                  double xa, double ya, double za,
                                                                  CallbackInfoReturnable<Particle> callback) {
        if (NTServerConfig.CONFIG.legacyCombat.get() && LegacyCombatHandler.shouldCancelParticle(options)) {
            callback.setReturnValue(null);
        }
    }
}

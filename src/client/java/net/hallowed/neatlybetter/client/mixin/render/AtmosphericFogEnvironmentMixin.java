package net.hallowed.neatlybetter.client.mixin.render;

import net.hallowed.neatlybetter.client.config.NTClientConfig;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.AtmosphericFogEnvironment;
import net.minecraft.util.Mth;
import net.minecraft.world.attribute.EnvironmentAttributes;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AtmosphericFogEnvironment.class)
public abstract class AtmosphericFogEnvironmentMixin {

    @Shadow private float rainFogMultiplier;

    /** Smoothed rain * biome factor, without the sky light term vanilla's multiplier includes. */
    @Unique private float neatlybetter$fullRainFog;

    @Inject(
            method = "setupFog(Lnet/minecraft/client/renderer/fog/FogData;Lnet/minecraft/client/Camera;Lnet/minecraft/client/multiplayer/ClientLevel;FLnet/minecraft/client/DeltaTracker;)V",
            at = @At("TAIL")
    )
    private void neatlybetter$configureRainFog(FogData fog, Camera camera, ClientLevel level,
                                               float renderDistance, DeltaTracker deltaTracker,
                                               CallbackInfo ci) {
        float partialTicks = deltaTracker.getGameTimeDeltaPartialTick(false);
        float deltaTicks = deltaTracker.getGameTimeDeltaTicks();

        // Keep in sync even while the feature is off or at defaults, so changing it doesn't pop.
        boolean rainsInBiome = level.getBiome(camera.blockPosition()).value().hasPrecipitation();
        float target = level.getRainLevel(partialTicks) * (rainsInBiome ? 1.0F : 0.5F);
        this.neatlybetter$fullRainFog += (target - this.neatlybetter$fullRainFog) * deltaTicks * 0.2F;

        NTClientConfig cfg = NTClientConfig.CONFIG;
        if (!cfg.rainFogModification.get()) return;

        // Vanilla values: leave the fog exactly as vanilla (and any other mod) computed it.
        if (cfg.rainFogOutdoorStart.get() == 100 && cfg.rainFogOutdoorEnd.get() == 100
                && cfg.rainFogIndoorStart.get() == 0 && cfg.rainFogIndoorEnd.get() == 0) return;

        if (Minecraft.getInstance().gui.hud.getBossOverlay().shouldCreateWorldFog()) return;

        float outdoor = Mth.clamp(this.rainFogMultiplier, 0.0F, this.neatlybetter$fullRainFog);
        float indoor = this.neatlybetter$fullRainFog - outdoor;

        float startMul = outdoor * (cfg.rainFogOutdoorStart.get() / 100.0F)
                + indoor * (cfg.rainFogIndoorStart.get() / 100.0F);
        float endMul = outdoor * (cfg.rainFogOutdoorEnd.get() / 100.0F)
                + indoor * (cfg.rainFogIndoorEnd.get() / 100.0F);

        float baseStart = camera.attributeProbe().getValue(EnvironmentAttributes.FOG_START_DISTANCE, partialTicks);
        float baseEnd = camera.attributeProbe().getValue(EnvironmentAttributes.FOG_END_DISTANCE, partialTicks);

        fog.environmentalStart = baseStart - 160.0F * startMul;
        //noinspection MathClampMigration
        fog.environmentalEnd = Math.max(Math.min(96.0F, baseEnd), baseEnd - 256.0F * endMul);
    }
}
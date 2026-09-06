package net.hallowed.neatlybetter.client.content.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.LavaFogEnvironment;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FogType;

import org.jspecify.annotations.NonNull;

import static net.minecraft.client.renderer.fog.FogRenderer.FOG_ENVIRONMENTS;

@Environment(EnvType.CLIENT)
public final class FireResistanceLavaFog {

    private static final Renderer FOG_RENDERER = new Renderer();

    private FireResistanceLavaFog() {}

    public static void register() {
        var vanillaLava = FOG_ENVIRONMENTS.stream()
                .filter(e -> e instanceof LavaFogEnvironment)
                .toList();
        if (vanillaLava.isEmpty()) {
            FOG_ENVIRONMENTS.add(FOG_RENDERER);
        } else {
            FOG_ENVIRONMENTS.add(FOG_ENVIRONMENTS.indexOf(vanillaLava.getFirst()), FOG_RENDERER);
        }
    }

    public static final class Renderer extends LavaFogEnvironment {

        @Override
        public boolean isApplicable(FogType submersionType, @NonNull Entity cameraEntity) {
            return super.isApplicable(submersionType, cameraEntity)
                    && cameraEntity instanceof LivingEntity livingEntity
                    && livingEntity.hasEffect(MobEffects.FIRE_RESISTANCE);
        }

        @Override
        public void setupFog(@NonNull FogData data, @NonNull Camera camera, @NonNull ClientLevel world,
                             float viewDistance, @NonNull DeltaTracker tickCounter) {
            super.setupFog(data, camera, world, viewDistance, tickCounter);
            data.environmentalStart *= 4.0F;
            data.environmentalEnd *= 4.0F;
        }
    }
}
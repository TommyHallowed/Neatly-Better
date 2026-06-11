package net.hallowed.neatlybetter.client.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import net.hallowed.neatlybetter.client.config.NTClientConfig;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.AtmosphericFogEnvironment;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FogType;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static net.minecraft.client.renderer.fog.FogRenderer.FOG_ENVIRONMENTS;

@Environment(EnvType.CLIENT)
public final class VoidFog {

    private static final Renderer FOG_RENDERER = new Renderer();
    private static final ColorDarkener FOG_COLOR = new ColorDarkener();

    private VoidFog() {}

    private static NTClientConfig cfg() {
        return NTClientConfig.CONFIG;
    }

    public static void register() {
        var atmospheric = FOG_ENVIRONMENTS.stream()
                .filter(e -> e instanceof AtmosphericFogEnvironment)
                .toList();
        if (atmospheric.isEmpty()) {
            FOG_ENVIRONMENTS.add(FOG_RENDERER);
        } else {
            FOG_ENVIRONMENTS.add(FOG_ENVIRONMENTS.indexOf(atmospheric.getLast()), FOG_RENDERER);
        }
        FOG_ENVIRONMENTS.add(FOG_COLOR);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.isPaused() || client.level == null || client.getCameraEntity() == null) return;
            Entity entity = client.getCameraEntity();
            if (isDepthFogActive(entity, client.level)) {
                spawnParticles(client.level, entity);
            }
        });
    }

    static boolean isDepthFogActive(Entity entity, Level world) {
        if (entity.isSpectator()) return false;
        if (cfg().DisableInCreative.get() && entity instanceof Player p && p.isCreative()) return false;

        return world.isClientSide()
                && ((ClientLevel) world).getLevelData().getHorizonHeight(world) > world.getMinY()
                && world.dimensionType().hasSkyLight()
                && !world.dimensionType().hasCeiling();
    }

    private static boolean isNearBedrock(BlockPos pos, Level world) {
        return pos.getY() < world.getMinY() + 6;
    }

    static Entity getCorrectEntity(Entity entity) {
        while (true) {
            assert entity != null;
            if (!(entity.isPassenger() && !entity.getBlockStateOn().isAir())) break;
            entity = entity.getVehicle();
        }
        return entity;
    }

    static double getAltitude(Entity entity, Level world) {
        entity = getCorrectEntity(entity);
        return !isDepthFogActive(entity, world)
                ? cfg().MaxHeight.get() + 1
                : (entity.getY() - world.getMinY());
    }

    static float getDifficultyMultiplier(Level world) {
        return cfg().ScaleWithDifficulty.get() ? world.getDifficulty().getId() + 1 : 1;
    }

    static float getFogBlendingDelta(Entity entity) {
        float altitude = (float) getAltitude(entity, entity.level());
        float transitionDist = Math.max(0, cfg().TransitionDistance.get().floatValue());
        float maxFogAltitude = cfg().MaxHeight.get() - transitionDist;
        return Mth.clamp((altitude - maxFogAltitude) / transitionDist, 0, 1);
    }

    private static int getLuminance(Entity entity, boolean includeBlocks) {
        entity = getCorrectEntity(entity);
        BlockPos pos = BlockPos.containing(entity.getEyePosition());
        int lightCoords = LevelRenderer.getLightCoords(entity.level(), pos);
        if (includeBlocks) {
            return Math.max(LightCoordsUtil.block(lightCoords), LightCoordsUtil.sky(lightCoords));
        }
        return LightCoordsUtil.sky(lightCoords);
    }

    private static BlockPos randomPos(RandomSource rand) {
        return new BlockPos(rand.nextInt(16), rand.nextInt(16), rand.nextInt(16));
    }

    private static void spawnParticles(Level world, Entity entity) {
        int maxFogHeight = cfg().MaxHeight.get();
        int maxParticleWorldY = world.getMinY() + maxFogHeight;
        if (getAltitude(entity, world) > maxFogHeight) return;

        int particleCount = (int) (cfg().ParticleDensity.get() * (1 - getFogBlendingDelta(entity)));
        int difficultyMultiplier = (int) (8 * getDifficultyMultiplier(world));
        RandomSource rand = world.getRandom();

        for (int pass = 0; pass < particleCount; pass++) {
            BlockPos rawPos = randomPos(rand).subtract(randomPos(rand)).offset(entity.blockPosition());

            BlockPos pos = new BlockPos(rawPos.getX(),
                    Math.min(rawPos.getY(), maxParticleWorldY),
                    rawPos.getZ());

            BlockState state = world.getBlockState(pos);

            if (state.isAir()
                    && world.getFluidState(pos).isEmpty()
                    && pos.getY() <= maxParticleWorldY
                    && rand.nextInt(difficultyMultiplier) <= maxFogHeight) {

                boolean nearBedrock = isNearBedrock(pos, world);
                world.addParticle(
                        nearBedrock ? ParticleTypes.ASH : ParticleTypes.MYCELIUM,
                        pos.getX() + rand.nextFloat(),
                        pos.getY() + rand.nextFloat(),
                        pos.getZ() + rand.nextFloat(),
                        0,
                        nearBedrock ? rand.nextFloat() : 0,
                        0
                );
            }
        }
    }

    public static final class Renderer extends AtmosphericFogEnvironment {

        private float lastFogDistance = 1000;
        private float smoothedBlend   = 0f;
        private float dissipationDelayRemaining = 0f;

        @Override
        public boolean isApplicable(FogType submersionType, @NonNull Entity cameraEntity) {
            return super.isApplicable(submersionType, cameraEntity)
                    && !(cameraEntity instanceof LivingEntity l && l.hasEffect(MobEffects.BLINDNESS))
                    && isDepthFogActive(cameraEntity, cameraEntity.level());
        }

        @Override
        public void setupFog(@NonNull FogData data, @NonNull Camera camera, @NonNull ClientLevel world,
                             float viewDistance, @NonNull DeltaTracker tickCounter) {
            super.setupFog(data, camera, world, viewDistance, tickCounter);

            float targetBlend     = computeTargetBlend(camera.entity());
            float tickDelta       = tickCounter.getGameTimeDeltaTicks();
            float transitionTicks = cfg().TransitionTicks.get().floatValue();
            float dissipationDelay = cfg().DissipationDelayTicks.get().floatValue();

            if (targetBlend < 1f) {
                dissipationDelayRemaining = dissipationDelay;
                smoothedBlend = Mth.lerp(tickDelta / transitionTicks, smoothedBlend, targetBlend);
                float distance = computeFogDistance(world, camera.entity(), tickDelta, transitionTicks, false);
                data.environmentalStart = Mth.lerp(smoothedBlend, fogStart(distance), viewDistance - 1f);
                data.environmentalEnd   = Mth.lerp(smoothedBlend, fogEnd(distance),   viewDistance);
            } else {
                if (dissipationDelayRemaining > 0f) {
                    dissipationDelayRemaining = Math.max(0f, dissipationDelayRemaining - tickDelta);
                    float distance = lastFogDistance;
                    data.environmentalStart = Mth.lerp(smoothedBlend, fogStart(distance), viewDistance - 1f);
                    data.environmentalEnd   = Mth.lerp(smoothedBlend, fogEnd(distance),   viewDistance);
                } else {
                    smoothedBlend = Mth.lerp(tickDelta / transitionTicks, smoothedBlend, 1f);
                    float distance = computeFogDistance(world, camera.entity(), tickDelta, transitionTicks, true);
                    data.environmentalStart = Mth.lerp(smoothedBlend, fogStart(distance), viewDistance - 1f);
                    data.environmentalEnd   = Mth.lerp(smoothedBlend, fogEnd(distance),   viewDistance);
                }
            }
        }

        private static float computeTargetBlend(Entity entity) {
            float altitude = (float) getAltitude(entity, entity.level());
            float transitionDist = Math.max(0.001f, cfg().TransitionDistance.get().floatValue());
            float maxFogAltitude = cfg().MaxHeight.get() - transitionDist;
            return Mth.clamp((altitude - maxFogAltitude) / transitionDist, 0, 1);
        }

        private float computeFogDistance(ClientLevel world, Entity entity, float tickDelta,
                                         float transitionTicks, boolean aboveFogZone) {
            float viewDistance = Minecraft.getInstance().options.getEffectiveRenderDistance();
            int maxFogHeight = cfg().MaxHeight.get();

            double altitude = aboveFogZone
                    ? maxFogHeight
                    : Math.min(getAltitude(entity, world), maxFogHeight);

            double fogDistance =
                    getLuminance(entity, cfg().RespectTorches.get()) / 16D
                            + altitude / (maxFogHeight * getDifficultyMultiplier(world));

            float distance = fogDistance >= 1
                    ? viewDistance
                    : (float) Mth.clamp(100 * Math.pow(Math.max(fogDistance, 0), 2), 5, viewDistance);

            if (entity instanceof LivingEntity l && l.hasEffect(MobEffects.NIGHT_VISION)) {
                distance *= 4 * GameRenderer.getNightVisionScale(l, tickDelta);
            }

            distance = Mth.lerp(tickDelta / transitionTicks, lastFogDistance, distance);
            lastFogDistance = distance;
            return distance;
        }

        private static float fogStart(float distance) {
            float density = cfg().Density.get().floatValue();
            float factor = 0.55f * (1 - (distance - 5) / 127f);
            return distance * Math.max(0, factor) - (1 - density) * 9.9f;
        }

        private static float fogEnd(float distance) {
            float density = cfg().Density.get().floatValue();
            return distance + (1 - density) * 9.9f;
        }
    }

    public static final class ColorDarkener extends FogEnvironment {

        private float brightness = 0f;
        private float dissipationDelayRemaining = 0f;

        @Override
        public boolean modifiesDarkness() {
            return true;
        }

        @Override
        public void setupFog(@NonNull FogData fog, @NonNull Camera camera, @NonNull ClientLevel level,
                             float renderDistance, @NonNull DeltaTracker deltaTracker) {
        }

        @Override
        public boolean isApplicable(@Nullable FogType fogType, @NonNull Entity cameraEntity) {
            return fogType == FogType.ATMOSPHERIC
                    && isDepthFogActive(cameraEntity, cameraEntity.level());
        }

        @Override
        public float getModifiedDarkness(@NonNull LivingEntity cameraEntity, float darkness, float tickProgress) {
            float light = getLuminance(cameraEntity, false);

            float targetBrightness = light >= 1f ? 0f : (1f - light);

            float transitionTicks  = cfg().TransitionTicks.get().floatValue();
            float dissipationDelay = cfg().DissipationDelayTicks.get().floatValue();

            if (targetBrightness > brightness) {
                dissipationDelayRemaining = dissipationDelay;
                brightness = Mth.lerp(tickProgress / transitionTicks, brightness, targetBrightness);
            } else {
                if (dissipationDelayRemaining > 0f) {
                    dissipationDelayRemaining = Math.max(0f, dissipationDelayRemaining - tickProgress);
                } else {
                    brightness = Mth.lerp(tickProgress / transitionTicks, brightness, targetBrightness);
                }
            }

            return Math.max(darkness, brightness);
        }
    }
}
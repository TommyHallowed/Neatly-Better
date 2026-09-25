package net.hallowed.neatlybetter.client.content.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import net.hallowed.neatlybetter.client.config.NTClientConfig;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
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
        if (cfg().VoidFogEnabled.isFalse()) return false;
        if (cfg().DisableInCreative.isTrue() && entity instanceof Player p && p.isCreative()) return false;

        return world.isClientSide()
                && world.dimension() == Level.OVERWORLD
                && !((ClientLevel) world).getLevelData().isFlat
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

    private static float getTransitionDistance() {
        return Math.max(10f, cfg().TransitionDistance.get().floatValue());
    }

    private static float getFullFogAltitude() {
        return cfg().MaxHeight.get() - getTransitionDistance();
    }

    static float getFogBlendingDelta(Entity entity) {
        float altitude = (float) getAltitude(entity, entity.level());
        float transitionDist = getTransitionDistance();
        float maxFogAltitude = getFullFogAltitude();
        return Mth.clamp((altitude - maxFogAltitude) / transitionDist, 0, 1);
    }

    private static int getLuminance(Entity entity, boolean includeBlocks) {
        entity = getCorrectEntity(entity);
        BlockPos pos = BlockPos.containing(entity.getEyePosition());
        int lightCoords = LightCoordsUtil.getLightCoords(entity.level(), pos);
        if (includeBlocks) {
            return Math.max(LightCoordsUtil.block(lightCoords), LightCoordsUtil.sky(lightCoords));
        }
        return LightCoordsUtil.sky(lightCoords);
    }

    private static BlockPos randomPos(RandomSource rand) {
        return new BlockPos(rand.nextInt(16), rand.nextInt(16), rand.nextInt(16));
    }

    private static void spawnParticles(Level world, Entity entity) {
        if (getFogBlendingDelta(entity) > 0f) return;

        int fullFogHeight = Math.max(0, (int) getFullFogAltitude());
        if (getAltitude(entity, world) > fullFogHeight) return;

        int maxParticleWorldY = world.getMinY() + fullFogHeight;
        int particleCount = cfg().ParticleDensity.get();
        int difficultyMultiplier = (int) (8 * getDifficultyMultiplier(world));
        RandomSource rand = world.getRandom();

        int luminance = getLuminance(entity, cfg().RespectTorches.get());
        float luminanceClearance = (luminance / 15f) * 8f;
        float densityClearance   = cfg().Density.get().floatValue() * 3f;
        float clearanceRadius    = Math.max(luminanceClearance, densityClearance);
        double minClearanceDistanceSq = clearanceRadius * clearanceRadius;

        for (int pass = 0; pass < particleCount; pass++) {
            BlockPos offset = randomPos(rand).subtract(randomPos(rand));

            double distanceSq = (double) offset.getX() * offset.getX()
                    + (double) offset.getY() * offset.getY()
                    + (double) offset.getZ() * offset.getZ();
            if (distanceSq < minClearanceDistanceSq) continue;

            BlockPos rawPos = offset.offset(entity.blockPosition());
            BlockPos pos = new BlockPos(rawPos.getX(),
                    Math.min(rawPos.getY(), maxParticleWorldY),
                    rawPos.getZ());

            BlockState state = world.getBlockState(pos);

            if (state.isAir()
                    && world.getFluidState(pos).isEmpty()
                    && pos.getY() <= maxParticleWorldY
                    && rand.nextInt(difficultyMultiplier) <= fullFogHeight) {

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

        private float smoothedBlend = 0f;

        private float smoothedDistance = 0f;
        private boolean distanceInitialized = false;

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

            Entity entity = camera.entity();
            float tickDelta       = tickCounter.getGameTimeDeltaTicks();
            float transitionTicks = Math.max(1f, cfg().TransitionTicks.get().floatValue());
            float lerpFactor      = Mth.clamp(tickDelta / transitionTicks, 0f, 1f);

            float targetBlend = getFogBlendingDelta(entity);
            smoothedBlend = Mth.lerp(lerpFactor, smoothedBlend, targetBlend);

            float targetDistance = computeTargetFogDistance(world, entity, viewDistance, tickDelta);
            if (!distanceInitialized) {
                smoothedDistance = targetDistance;
                distanceInitialized = true;
            } else {
                smoothedDistance = Mth.lerp(lerpFactor, smoothedDistance, targetDistance);
            }

            data.environmentalStart = Mth.lerp(smoothedBlend, fogStart(smoothedDistance), data.environmentalStart);
            data.environmentalEnd   = Mth.lerp(smoothedBlend, fogEnd(smoothedDistance),   data.environmentalEnd);
        }

        private float computeTargetFogDistance(ClientLevel world, Entity entity, float viewDistance, float tickDelta) {
            int maxFogHeight = cfg().MaxHeight.get();
            double altitude = Math.min(getAltitude(entity, world), maxFogHeight);

            double luminanceTerm = Math.min(getLuminance(entity, cfg().RespectTorches.get()) / 16D, 0.2);
            double fogDistance =
                    luminanceTerm
                            + altitude / (maxFogHeight * getDifficultyMultiplier(world));

            float distance = fogDistance >= 1
                    ? viewDistance
                    : (float) Mth.clamp(100 * Math.pow(Math.max(fogDistance, 0), 2), 5, viewDistance);

            if (entity instanceof LivingEntity l && l.hasEffect(MobEffects.NIGHT_VISION)) {
                distance *= 4 * GameRenderer.nightVisionScale(l, tickDelta);
            }

            return distance;
        }

        private static float fogStart(float distance) {
            float density = cfg().Density.get().floatValue();
            float factor = 0.55f * (1 - (distance - 5) / 127f);
            float start = distance * Math.max(0, factor) - (1 - density) * 9.9f;
            return start * 1.2f;
        }

        private static float fogEnd(float distance) {
            float density = cfg().Density.get().floatValue();
            float end = distance + (1 - density) * 9.9f;
            return end * 1.2f;
        }
    }

    public static final class ColorDarkener extends FogEnvironment {

        private float brightness = 0f;

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

            float transitionTicks = Math.max(1f, cfg().TransitionTicks.get().floatValue());
            brightness = Mth.lerp(Mth.clamp(tickProgress / transitionTicks, 0f, 1f), brightness, targetBrightness);

            return Math.max(darkness, brightness);
        }
    }
}
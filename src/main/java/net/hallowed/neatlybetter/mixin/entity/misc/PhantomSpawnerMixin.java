package net.hallowed.neatlybetter.mixin.entity.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.material.FluidState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PhantomSpawner.class)
public class PhantomSpawnerMixin {

    @Shadow private int nextTick;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void neatlybetter$altitudePhantomSpawn(ServerLevel level, boolean spawnEnemies, CallbackInfo ci) {
        ci.cancel();
        if (!spawnEnemies) return;
        if (!level.getGameRules().get(GameRules.SPAWN_PHANTOMS)) return;

        RandomSource randomSource = level.getRandom();
        this.nextTick--;
        if (this.nextTick > 0) return;

        this.nextTick += (60 + randomSource.nextInt(60)) * 20;

        // Must be dark (vanilla check)
        if (level.getSkyDarken() < 5 && level.dimensionType().hasSkyLight()) return;

        for (ServerPlayer serverPlayer : level.players()) {
            if (serverPlayer.isSpectator() || serverPlayer.isCreative()) continue;

            BlockPos blockPos = serverPlayer.blockPosition();
            int playerY = blockPos.getY();

            if (level.dimensionType().hasSkyLight()) {
                // Altitude check replaces insomnia
                if (playerY < 160 || playerY > 319) continue;
                // Player must be under open sky
                if (!level.canSeeSky(blockPos)) continue;
            }

            // Difficulty check (vanilla)
            DifficultyInstance difficultyInstance = level.getCurrentDifficultyAt(blockPos);
            if (!difficultyInstance.isHarderThan(randomSource.nextFloat() * 3.0F)) continue;

            // Spawn position: 20-34 blocks above player, ±10 blocks horizontal (vanilla)
            BlockPos spawnPos = blockPos.above(20 + randomSource.nextInt(15))
                    .east(-10 + randomSource.nextInt(21))
                    .south(-10 + randomSource.nextInt(21));

            // Surface light check: highest solid block under phantom spawn must have light level 0
            int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, spawnPos.getX(), spawnPos.getZ());
            BlockPos surfacePos = new BlockPos(spawnPos.getX(), surfaceY, spawnPos.getZ());
            if (level.getMaxLocalRawBrightness(surfacePos) > 0) continue;

            // Valid empty block check (vanilla)
            BlockState blockState = level.getBlockState(spawnPos);
            FluidState fluidState = level.getFluidState(spawnPos);
            if (!NaturalSpawner.isValidEmptySpawnBlock(level, spawnPos, blockState, fluidState, EntityType.PHANTOM)) continue;

            // Spawn phantoms (vanilla)
            SpawnGroupData spawnGroupData = null;
            int count = 1 + randomSource.nextInt(difficultyInstance.getDifficulty().getId() + 1);

            for (int l = 0; l < count; l++) {
                Phantom phantom = EntityType.PHANTOM.create(level, EntitySpawnReason.NATURAL);
                if (phantom != null) {
                    phantom.snapTo(spawnPos, 0.0F, 0.0F);
                    spawnGroupData = phantom.finalizeSpawn(
                            level, difficultyInstance, EntitySpawnReason.NATURAL, spawnGroupData
                    );
                    level.addFreshEntityWithPassengers(phantom);
                }
            }
        }
    }
}

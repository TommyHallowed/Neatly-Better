package net.hallowed.neatlybetter.content.spawner;

import java.util.List;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.hallowed.neatlybetter.config.NTServerConfig;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.material.FluidState;

public final class EndPhantomSpawner {

    private int cooldown;

    private EndPhantomSpawner() {
    }

    public static void register() {
        EndPhantomSpawner spawning = new EndPhantomSpawner();
        ServerTickEvents.END_LEVEL_TICK.register(spawning::tick);
    }

    private void tick(ServerLevel level) {
        if (level.dimension() != Level.END) {
            return;
        }
        if (NTServerConfig.CONFIG.phantomSpawnerMode.get() != NTServerConfig.PhantomSpawner.END) {
            return;
        }
        if (!level.getGameRules().get(GameRules.SPAWN_PHANTOMS)) {
            return;
        }

        RandomSource random = level.getRandom();
        if (--this.cooldown > 0) {
            return;
        }
        this.cooldown = (60 + random.nextInt(60)) * 20;

        if (level.getEntities(EntityTypes.PHANTOM, LivingEntity::isAlive).size() >= 8) {
            return;
        }

        List<ServerPlayer> players = level.players();
        for (ServerPlayer player : players) {
            if (player.isSpectator()) {
                continue;
            }

            BlockPos playerPos = player.blockPosition();
            if (isWithinMainIsland(playerPos)) {
                continue;
            }
            if (!level.canSeeSky(playerPos)) {
                continue;
            }

            DifficultyInstance difficulty = level.getCurrentDifficultyAt(playerPos);
            float spawnChance = Mth.clamp(difficulty.getEffectiveDifficulty() / 6.75F, 0.0F, 1.0F) * 0.35F;
            if (random.nextFloat() >= spawnChance) {
                continue;
            }

            BlockPos spawnPos = playerPos
                    .above(20 + random.nextInt(15))
                    .east(-10 + random.nextInt(21))
                    .south(-10 + random.nextInt(21));

            if (!level.canSeeSky(spawnPos)) {
                continue;
            }

            BlockState blockState = level.getBlockState(spawnPos);
            FluidState fluidState = level.getFluidState(spawnPos);
            if (!NaturalSpawner.isValidEmptySpawnBlock(level, spawnPos, blockState, fluidState, EntityTypes.PHANTOM)) {
                continue;
            }

            spawnPhantomGroup(level, spawnPos, difficulty, random);
            return;
        }
    }

    private void spawnPhantomGroup(ServerLevel level, BlockPos pos, DifficultyInstance difficulty, RandomSource random) {
        SpawnGroupData groupData = null;
        int groupSize = 1 + random.nextInt(difficulty.getDifficulty().getId() + 1);

        for (int i = 0; i < groupSize; ++i) {
            Phantom phantom = EntityTypes.PHANTOM.create(level, EntitySpawnReason.NATURAL);
            if (phantom == null) {
                continue;
            }
            phantom.snapTo(pos, 0.0F, 0.0F);
            groupData = phantom.finalizeSpawn(level, difficulty, EntitySpawnReason.NATURAL, groupData);
            level.addFreshEntityWithPassengers(phantom);
        }
    }

    private static boolean isWithinMainIsland(BlockPos pos) {
        long dx = pos.getX();
        long dz = pos.getZ();
        return dx * dx + dz * dz < 1000000;
    }
}
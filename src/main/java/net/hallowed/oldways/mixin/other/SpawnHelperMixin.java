package net.hallowed.oldways.mixin.other;

import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.Heightmap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SpawnHelper.class)
public abstract class SpawnHelperMixin {

    @Unique private static final double SURFACE_NIGHT_MULT      = 1.25;
    @Unique private static final double UNDERGROUND_DAY_MULT    = 1.25;
    @Unique private static final double DEFAULT_MONSTER_MULT    = 1.0;

    @Redirect(
            method = "spawnEntitiesInChunk(Lnet/minecraft/entity/SpawnGroup;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/chunk/WorldChunk;Lnet/minecraft/world/SpawnHelper$Checker;Lnet/minecraft/world/SpawnHelper$Runner;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/SpawnHelper;spawnEntitiesInChunk(Lnet/minecraft/entity/SpawnGroup;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/chunk/Chunk;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/SpawnHelper$Checker;Lnet/minecraft/world/SpawnHelper$Runner;)V"
            )
    )
    private static void oldways$boostAttempts(
            SpawnGroup group,
            ServerWorld world,
            Chunk chunk,
            BlockPos pos,
            SpawnHelper.Checker checker,
            SpawnHelper.Runner runner
    ) {
        if (group != SpawnGroup.MONSTER || world.getRegistryKey() != World.OVERWORLD) {
            SpawnHelper.spawnEntitiesInChunk(group, world, chunk, pos, checker, runner);
            return;
        }
        int surfaceY = chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE, pos.getX(), pos.getZ());
        boolean underground = pos.getY() < surfaceY;

        boolean isNight = world.isNight();
        boolean isDay   = world.isDay();

        double mult = DEFAULT_MONSTER_MULT;
        if (!underground && isNight) {
            mult = SURFACE_NIGHT_MULT;
        } else if (underground && isDay) {
            mult = UNDERGROUND_DAY_MULT;
        }

        int repeats = Math.max(1, (int)Math.round(mult));
        for (int i = 0; i < repeats; i++) {
            SpawnHelper.spawnEntitiesInChunk(group, world, chunk, pos, checker, runner);
        }
    }
}

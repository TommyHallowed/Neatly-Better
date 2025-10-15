package net.hallowed.oldways.content.entity.ai.task;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.*;
import net.minecraft.entity.ai.brain.BlockPosLookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.event.GameEvent;


public class FarmerReplantTask extends MultiTickTask<VillagerEntity> {
    private static final int SCAN_RADIUS = 4;
    private static final float WALK_SPEED = 0.5F;

    private BlockPos target;

    public FarmerReplantTask() {
        super(ImmutableMap.of(
                MemoryModuleType.LOOK_TARGET, MemoryModuleState.VALUE_ABSENT,
                MemoryModuleType.WALK_TARGET, MemoryModuleState.VALUE_ABSENT
        ));
    }

    @Override
    protected boolean shouldRun(ServerWorld world, VillagerEntity villager) {
        if (!world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) return false;

        BlockPos origin = villager.getBlockPos();
        BlockPos.Mutable m = new BlockPos.Mutable();
        target = null;

        for (int dx = -SCAN_RADIUS; dx <= SCAN_RADIUS; dx++) {
            for (int dz = -SCAN_RADIUS; dz <= SCAN_RADIUS; dz++) {
                for (int dy = -1; dy <= 1; dy++) {
                    m.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
                    if (isTillingCandidate(world.getBlockState(m), world, m)) {
                        target = m.toImmutable();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isTillingCandidate(BlockState state, ServerWorld world, BlockPos pos) {
        if (!world.isAir(pos.up())) return false;

        Block block = state.getBlock();

        return block == Blocks.DIRT ||
                block == Blocks.GRASS_BLOCK ||
                block == Blocks.COARSE_DIRT ||
                block == Blocks.ROOTED_DIRT;
    }

    @Override
    protected void run(ServerWorld world, VillagerEntity villager, long time) {
        if (target != null) {
            villager.getBrain().remember(MemoryModuleType.LOOK_TARGET, new BlockPosLookTarget(target));
            villager.getBrain().remember(MemoryModuleType.WALK_TARGET, new WalkTarget(new BlockPosLookTarget(target), WALK_SPEED, 1));
        }
    }

    @Override
    protected boolean shouldKeepRunning(ServerWorld world, VillagerEntity villager, long time) {
        return target != null && target.isWithinDistance(villager.getEntityPos(), 16.0D);
    }

    @Override
    protected void keepRunning(ServerWorld world, VillagerEntity villager, long time) {
        if (target == null) return;

        if (target.isWithinDistance(villager.getEntityPos(), 1.5D)) {

            BlockState before = world.getBlockState(target);
            if (isTillingCandidate(before, world, target)) {
                BlockState after = Blocks.FARMLAND.getDefaultState();
                world.setBlockState(target, after, Block.NOTIFY_ALL);
                world.emitGameEvent(villager, GameEvent.BLOCK_CHANGE, target);
                world.playSound(null, target, SoundEvents.ITEM_HOE_TILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
            }

            // done with this spot
            villager.getBrain().forget(MemoryModuleType.LOOK_TARGET);
            villager.getBrain().forget(MemoryModuleType.WALK_TARGET);
            target = null;
        }
    }

    @Override
    protected void finishRunning(ServerWorld world, VillagerEntity villager, long time) {
        villager.getBrain().forget(MemoryModuleType.LOOK_TARGET);
        villager.getBrain().forget(MemoryModuleType.WALK_TARGET);
        target = null;
    }
}

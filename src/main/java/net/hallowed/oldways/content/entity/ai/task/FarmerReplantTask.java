package net.hallowed.oldways.content.entity.ai.task;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gamerules.GameRules;
import org.jetbrains.annotations.NotNull;

public class FarmerReplantTask extends Behavior<@NotNull Villager> {
    private static final int SCAN_RADIUS = 4;
    private static final float WALK_SPEED = 0.5F;

    private BlockPos target;

    public FarmerReplantTask() {
        super(ImmutableMap.of(
                MemoryModuleType.LOOK_TARGET, MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT
        ));
    }

    // FIX: shouldRun -> checkExtraStartConditions
    @Override
    protected boolean checkExtraStartConditions(ServerLevel world, Villager villager) {
        // FIX: GameRules mapping syntax
        if (!world.getGameRules().get(GameRules.MOB_GRIEFING)) return false;

        BlockPos origin = villager.blockPosition();
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        target = null;

        for (int dx = -SCAN_RADIUS; dx <= SCAN_RADIUS; dx++) {
            for (int dz = -SCAN_RADIUS; dz <= SCAN_RADIUS; dz++) {
                for (int dy = -1; dy <= 1; dy++) {
                    m.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
                    if (isTillingCandidate(world.getBlockState(m), world, m)) {
                        target = m.immutable();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isTillingCandidate(BlockState state, ServerLevel world, BlockPos pos) {
        if (!world.isEmptyBlock(pos.above())) return false;

        Block block = state.getBlock();

        return block == Blocks.DIRT ||
                block == Blocks.GRASS_BLOCK ||
                block == Blocks.COARSE_DIRT ||
                block == Blocks.ROOTED_DIRT;
    }

    // FIX: run -> start
    @Override
    protected void start(@NotNull ServerLevel world, Villager villager, long time) {
        if (target != null) {
            villager.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(target));
            villager.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new BlockPosTracker(target), WALK_SPEED, 1));
        }
    }

    // FIX: shouldKeepRunning -> canStillUse
    @Override
    protected boolean canStillUse(@NotNull ServerLevel world, Villager villager, long time) {
        return target != null && target.closerToCenterThan(villager.position(), 16.0D);
    }

    // FIX: keepRunning -> tick
    @Override
    protected void tick(@NotNull ServerLevel world, Villager villager, long time) {
        if (target == null) return;

        if (target.closerToCenterThan(villager.position(), 1.5D)) {

            BlockState before = world.getBlockState(target);
            if (isTillingCandidate(before, world, target)) {
                BlockState after = Blocks.FARMLAND.defaultBlockState();

                // FIX: Block.UPDATE_ALL -> 3 (Mojang uses the hardcoded int '3' for standard block updates)
                world.setBlock(target, after, 3);

                world.gameEvent(villager, GameEvent.BLOCK_CHANGE, target);
                world.playSound(null, target, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            }

            // done with this spot
            villager.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
            villager.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
            target = null;
        }
    }

    // FIX: finishRunning -> stop
    @Override
    protected void stop(@NotNull ServerLevel world, Villager villager, long time) {
        villager.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
        villager.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        target = null;
    }
}
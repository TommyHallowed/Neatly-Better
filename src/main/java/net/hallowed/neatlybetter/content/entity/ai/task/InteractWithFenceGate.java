package net.hallowed.neatlybetter.content.entity.ai.task;

import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.Path;

public class InteractWithFenceGate {
    private static final double OPEN_DISTANCE_SQ = 0.5 * 0.5;
    private static final double CLOSE_DISTANCE_SQ = 0.5 * 0.5;
    private static final int NODE_LOOK_AHEAD = 2;

    public static BehaviorControl<LivingEntity> create() {
        Set<BlockPos> openedGates = Sets.newHashSet();
        Set<BlockPos> pendingGates = Sets.newHashSet();

        return BehaviorBuilder.create(instance ->
                instance.group(
                        instance.registered(MemoryModuleType.PATH)
                ).apply(instance, pathAccessor -> (serverLevel, livingEntity, tick) -> {
                    closeDistantGates(serverLevel, livingEntity, openedGates);

                    Optional<Path> optPath = instance.tryGet(pathAccessor);
                    if (optPath.isEmpty()) {
                        pendingGates.clear();
                        return true;
                    }

                    Path path = optPath.get();
                    if (path.notStarted() || path.isDone()) {
                        pendingGates.clear();
                        return true;
                    }

                    pendingGates.clear();
                    int nextIdx = path.getNextNodeIndex();
                    int start = Math.max(0, nextIdx - 1);
                    int end = Math.min(nextIdx + NODE_LOOK_AHEAD, path.getNodeCount() - 1);

                    for (int i = start; i <= end; i++) {
                        BlockPos pos = path.getNode(i).asBlockPos();
                        BlockState state = serverLevel.getBlockState(pos);
                        if (state.getBlock() instanceof FenceGateBlock && !state.getValue(FenceGateBlock.OPEN)) {
                            if (!openedGates.contains(pos)) {
                                pendingGates.add(pos.immutable());
                            }
                        }
                    }

                    Iterator<BlockPos> it = pendingGates.iterator();
                    while (it.hasNext()) {
                        BlockPos pos = it.next();
                        double dx = pos.getX() + 0.5 - livingEntity.getX();
                        double dz = pos.getZ() + 0.5 - livingEntity.getZ();
                        if (dx * dx + dz * dz <= OPEN_DISTANCE_SQ) {
                            openGate(serverLevel, livingEntity, pos, openedGates);
                            it.remove();
                        }
                    }

                    return true;
                })
        );
    }

    private static void openGate(ServerLevel level, LivingEntity entity,
                                 BlockPos pos, Set<BlockPos> openedGates) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof FenceGateBlock && !state.getValue(FenceGateBlock.OPEN)) {
            level.setBlock(pos, state.setValue(FenceGateBlock.OPEN, true), 3);
            level.playSound(null, pos, SoundEvents.FENCE_GATE_OPEN, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(entity, GameEvent.BLOCK_OPEN, pos);
            openedGates.add(pos.immutable());
        }
    }

    private static void closeDistantGates(ServerLevel level, LivingEntity entity, Set<BlockPos> gates) {
        Iterator<BlockPos> it = gates.iterator();
        while (it.hasNext()) {
            BlockPos pos = it.next();
            double dx = pos.getX() + 0.5 - entity.getX();
            double dz = pos.getZ() + 0.5 - entity.getZ();
            if (dx * dx + dz * dz > CLOSE_DISTANCE_SQ) {
                BlockState state = level.getBlockState(pos);
                if (state.getBlock() instanceof FenceGateBlock && state.getValue(FenceGateBlock.OPEN)) {
                    level.setBlock(pos, state.setValue(FenceGateBlock.OPEN, false), 3);
                    level.playSound(null, pos, SoundEvents.FENCE_GATE_CLOSE, SoundSource.BLOCKS, 1.0F, 1.0F);
                    level.gameEvent(entity, GameEvent.BLOCK_CLOSE, pos);
                }
                it.remove();
            }
        }
    }
}

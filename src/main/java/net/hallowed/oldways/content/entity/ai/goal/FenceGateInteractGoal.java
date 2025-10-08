package net.hallowed.oldways.content.entity.ai.goal;

import net.minecraft.block.BlockState;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public abstract class FenceGateInteractGoal extends Goal {

    protected final MobEntity mob;
    protected BlockPos gatePos = BlockPos.ORIGIN;
    protected boolean hasGate;
    private boolean passed;
    private float doorOpenDirX;
    private float doorOpenDirZ;

    public FenceGateInteractGoal(MobEntity mob) {
        this.mob = mob;

        if (!(mob.getNavigation() instanceof MobNavigation)) {
            throw new IllegalArgumentException("Unsupported mob type for FenceGateInteractGoal");
        }
    }


    @Override
    public boolean canStart() {
        final EntityNavigation nav = mob.getNavigation();
        if (!(nav instanceof MobNavigation groundNav)) return false;

        Path path = groundNav.getCurrentPath();
        if (path == null) return false;

        int limit = Math.min(path.getCurrentNodeIndex() + 2, path.getLength());
        World w = mob.getWorld();

        for (int i = 0; i < limit; i++) {
            PathNode node = path.getNode(i);
            BlockPos probe = new BlockPos(
                    node.x + mob.getRandom().nextBetween(-2, 2),
                    node.y,
                    node.z + mob.getRandom().nextBetween(-2, 2)
            );

            if (mob.squaredDistanceTo(probe.getX() + 0.5, probe.getY(), probe.getZ() + 0.5) < 2.25D) {
                BlockState s = w.getBlockState(probe);
                if (s.getBlock() instanceof FenceGateBlock) {
                    this.gatePos = probe;
                    this.hasGate = true;
                    return true;
                }
            }
        }
        this.hasGate = false;
        return false;
    }

    @Override
    public boolean shouldContinue() {
        return !this.passed;
    }

    @Override
    public void start() {
        this.passed = false;
        this.doorOpenDirX = (float)((double)this.gatePos.getX() + 0.5D - this.mob.getX());
        this.doorOpenDirZ = (float)((double)this.gatePos.getZ() + 0.5D - this.mob.getZ());
        setOpen(true);
    }

    @Override
    public void stop() {
        setOpen(false);
    }

    @Override
    public boolean shouldRunEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        float dx = (float)((double)this.gatePos.getX() + 0.5D - this.mob.getX());
        float dz = (float)((double)this.gatePos.getZ() + 0.5D - this.mob.getZ());
        float dot = this.doorOpenDirX * dx + this.doorOpenDirZ * dz;
        if (dot < 0.0F) {
            this.passed = true;
        }
    }


    protected void setOpen(boolean open) {
        if (!this.hasGate) return;

        World w = mob.getWorld();
        BlockState s = w.getBlockState(this.gatePos);
        if (!(s.getBlock() instanceof FenceGateBlock)) return;

        if (s.contains(FenceGateBlock.OPEN) && s.get(FenceGateBlock.OPEN) != open) {
            w.setBlockState(this.gatePos, s.with(FenceGateBlock.OPEN, open), 10);

            w.playSound(null,
                    this.gatePos,
                    open ? SoundEvents.BLOCK_FENCE_GATE_OPEN : SoundEvents.BLOCK_FENCE_GATE_CLOSE,
                    SoundCategory.BLOCKS,
                    1.0F,
                    w.getRandom().nextFloat() * 0.1F + 0.9F
            );
            w.emitGameEvent(open ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, this.gatePos, GameEvent.Emitter.of(mob));
        }
    }
}

package net.hallowed.oldways.content.entity.ai.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;

public abstract class FenceGateInteractGoal extends Goal {

    protected final Mob mob;
    protected BlockPos gatePos = BlockPos.ZERO;
    protected boolean hasGate;
    private boolean passed;
    private float doorOpenDirX;
    private float doorOpenDirZ;

    public FenceGateInteractGoal(Mob mob) {
        this.mob = mob;

        if (!(mob.getNavigation() instanceof GroundPathNavigation)) {
            throw new IllegalArgumentException("Unsupported mob type for FenceGateInteractGoal");
        }
    }


    @Override
    public boolean canUse() {
        final PathNavigation nav = mob.getNavigation();
        if (!(nav instanceof GroundPathNavigation groundNav)) return false;

        Path path = groundNav.getPath();
        if (path == null) return false;

        int limit = Math.min(path.getNextNodeIndex() + 2, path.getNodeCount());
        Level w = mob.level();

        for (int i = 0; i < limit; i++) {
            Node node = path.getNode(i);
            BlockPos probe = new BlockPos(
                    node.x + mob.getRandom().nextIntBetweenInclusive(-2, 2),
                    node.y,
                    node.z + mob.getRandom().nextIntBetweenInclusive(-2, 2)
            );

            if (mob.distanceToSqr(probe.getX() + 0.5, probe.getY(), probe.getZ() + 0.5) < 2.25D) {
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
    public boolean canContinueToUse() {
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
    public boolean requiresUpdateEveryTick() {
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

        Level w = mob.level();
        BlockState s = w.getBlockState(this.gatePos);
        if (!(s.getBlock() instanceof FenceGateBlock)) return;

        if (s.hasProperty(FenceGateBlock.OPEN) && s.getValue(FenceGateBlock.OPEN) != open) {
            w.setBlock(this.gatePos, s.setValue(FenceGateBlock.OPEN, open), 10);

            w.playSound(null,
                    this.gatePos,
                    open ? SoundEvents.FENCE_GATE_OPEN : SoundEvents.FENCE_GATE_CLOSE,
                    SoundSource.BLOCKS,
                    1.0F,
                    w.getRandom().nextFloat() * 0.1F + 0.9F
            );
            w.gameEvent(open ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, this.gatePos, GameEvent.Context.of(mob));
        }
    }
}

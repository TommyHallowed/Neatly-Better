package net.hallowed.oldways.client.mixin.entity;

import net.hallowed.oldways.client.accessor.EntitySoulFireAccessor;
import net.hallowed.oldways.util.FireShapeUtils;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings({"rawtypes", "unchecked", "ConstantConditions"})
@Mixin(Entity.class)
public abstract class EntityCatchFireMixin {
    @SuppressWarnings("unchecked")
    @Inject(method = "setOnFireFor(F)V", at = @At("HEAD"))
    private void oldways$onSetOnFireForFloat(float seconds, CallbackInfo ci) {
        try {
            Entity e = (Entity)(Object)this;
            var bbox = e.getBoundingBox();
            int x0 = (int)Math.floor(bbox.minX - 0.001D);
            int x1 = (int)Math.floor(bbox.maxX + 0.001D);
            int y0 = (int)Math.floor(bbox.minY - 0.001D);
            int y1 = (int)Math.floor(bbox.maxY + 0.001D);
            int z0 = (int)Math.floor(bbox.minZ - 0.001D);
            int z1 = (int)Math.floor(bbox.maxZ + 0.001D);

            boolean atSoul = false;
            boolean atFire = false;
            boolean atLava = false;
            for (int xi = x0; xi <= x1 && !(atSoul && atFire && atLava); xi++) {
                for (int yi = y0; yi <= y1 && !(atSoul && atFire && atLava); yi++) {
                    for (int zi = z0; zi <= z1 && !(atSoul && atFire && atLava); zi++) {
                        BlockPos checkPos = new BlockPos(xi, yi, zi);
                        try {
                            var bs = e.getEntityWorld().getBlockState(checkPos);
                            if (bs.isOf(Blocks.SOUL_FIRE) && FireShapeUtils.outlineIntersectsEntity(bs, e.getEntityWorld(), checkPos, bbox)) {
                                atSoul = true;
                            } else if (bs.isOf(Blocks.FIRE) && FireShapeUtils.outlineIntersectsEntity(bs, e.getEntityWorld(), checkPos, bbox)) {
                                atFire = true;
                            } else if (bs.isOf(Blocks.LAVA) && FireShapeUtils.outlineIntersectsEntity(bs, e.getEntityWorld(), checkPos, bbox)) {
                                atLava = true;
                            }
                        } catch (Throwable ignored) {
                        }
                    }
                }
            }

            // If lava is present, clear client-side accessor flag so lava-caused fire doesn't look like soul-fire
            if (atLava) {
                if (e instanceof EntitySoulFireAccessor acc) acc.oldways$setSoulFire(false);
                return;
            }

            // Client-side now relies on server-synced DataTracker; don't write to cache here to avoid duplication.
            if (e instanceof EntitySoulFireAccessor acc) {
                if (atSoul) acc.oldways$setSoulFire(true);
                else if (atFire) acc.oldways$setSoulFire(false);
            }
        } catch (Throwable ignored) {
        }
    }

    @SuppressWarnings("unchecked")
    @Inject(method = "setOnFireForTicks(I)V", at = @At("HEAD"))
    private void oldways$onSetOnFireForTicks(int ticks, CallbackInfo ci) {
        try {
            Entity e = (Entity)(Object)this;
            var bbox = e.getBoundingBox();
            int x0 = (int)Math.floor(bbox.minX - 0.001D);
            int x1 = (int)Math.floor(bbox.maxX + 0.001D);
            int y0 = (int)Math.floor(bbox.minY - 0.001D);
            int y1 = (int)Math.floor(bbox.maxY + 0.001D);
            int z0 = (int)Math.floor(bbox.minZ - 0.001D);
            int z1 = (int)Math.floor(bbox.maxZ + 0.001D);

            boolean atSoul = false;
            boolean atFire = false;
            boolean atLava = false;
            for (int xi = x0; xi <= x1 && !(atSoul && atFire && atLava); xi++) {
                for (int yi = y0; yi <= y1 && !(atSoul && atFire && atLava); yi++) {
                    for (int zi = z0; zi <= z1 && !(atSoul && atFire && atLava); zi++) {
                        BlockPos checkPos = new BlockPos(xi, yi, zi);
                        try {
                            var bs = e.getEntityWorld().getBlockState(checkPos);
                            if (bs.isOf(Blocks.SOUL_FIRE) && FireShapeUtils.outlineIntersectsEntity(bs, e.getEntityWorld(), checkPos, bbox)) {
                                atSoul = true;
                            } else if (bs.isOf(Blocks.FIRE) && FireShapeUtils.outlineIntersectsEntity(bs, e.getEntityWorld(), checkPos, bbox)) {
                                atFire = true;
                            } else if (bs.isOf(Blocks.LAVA) && FireShapeUtils.outlineIntersectsEntity(bs, e.getEntityWorld(), checkPos, bbox)) {
                                atLava = true;
                            }
                        } catch (Throwable ignored) {
                        }
                    }
                }
            }

            if (atLava) {
                if (e instanceof EntitySoulFireAccessor acc) acc.oldways$setSoulFire(false);
                return;
            }

            if (e instanceof EntitySoulFireAccessor acc) {
                if (atSoul) acc.oldways$setSoulFire(true);
                else if (atFire) acc.oldways$setSoulFire(false);
            }
        } catch (Throwable ignored) {
        }
    }
}

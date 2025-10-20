package net.hallowed.oldways.client.mixin.entity;

import net.hallowed.oldways.client.accessor.EntitySoulFireAccessor;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityCatchFireMixin {
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
            for (int xi = x0; xi <= x1 && !(atSoul && atFire); xi++) {
                for (int yi = y0; yi <= y1 && !(atSoul && atFire); yi++) {
                    for (int zi = z0; zi <= z1 && !(atSoul && atFire); zi++) {
                        BlockPos checkPos = new BlockPos(xi, yi, zi);
                        try {
                            if (e.getEntityWorld().getBlockState(checkPos).isOf(Blocks.SOUL_FIRE)) {
                                atSoul = true;
                            } else if (e.getEntityWorld().getBlockState(checkPos).isOf(Blocks.FIRE)) {
                                atFire = true;
                            }
                        } catch (Throwable ignored) {
                        }
                    }
                }
            }

            // Client-side now relies on server-synced DataTracker; don't write to cache here to avoid duplication.
            if (e instanceof EntitySoulFireAccessor acc) {
                if (atSoul) acc.oldways$setSoulFire(true);
                else if (atFire) acc.oldways$setSoulFire(false);
            }
        } catch (Throwable ignored) {
        }
    }

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
            for (int xi = x0; xi <= x1 && !(atSoul && atFire); xi++) {
                for (int yi = y0; yi <= y1 && !(atSoul && atFire); yi++) {
                    for (int zi = z0; zi <= z1 && !(atSoul && atFire); zi++) {
                        BlockPos checkPos = new BlockPos(xi, yi, zi);
                        try {
                            if (e.getEntityWorld().getBlockState(checkPos).isOf(Blocks.SOUL_FIRE)) {
                                atSoul = true;
                            } else if (e.getEntityWorld().getBlockState(checkPos).isOf(Blocks.FIRE)) {
                                atFire = true;
                            }
                        } catch (Throwable ignored) {
                        }
                    }
                }
            }

            if (e instanceof EntitySoulFireAccessor acc) {
                if (atSoul) acc.oldways$setSoulFire(true);
                else if (atFire) acc.oldways$setSoulFire(false);
            }
        } catch (Throwable ignored) {
        }
    }
}

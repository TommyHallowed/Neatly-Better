package net.hallowed.oldways.mixin.entity.generic;

import net.hallowed.oldways.content.entity.vehicle.LavaBoatEntity;
import net.hallowed.oldways.init.OldWaysTrackedData;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.server.world.ServerWorld;

@Mixin(Entity.class)
public abstract class EntityMixin {

    /* ------------------------ lava-boat behavior ------------------------ */

    @Shadow public abstract World getEntityWorld();

    @Inject(method = "isInLava()Z", at = @At("HEAD"), cancellable = true)
    private void oldways$ignoreLavaWhileOnWarpedBoat(CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity)(Object)this;
        if (self.hasVehicle() && self.getVehicle() instanceof LavaBoatEntity) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "setOnFireFromLava", at = @At("HEAD"), cancellable = true)
    private void oldways$noLavaIgniteWhileOnWarpedBoat(CallbackInfo ci) {
        Entity self = (Entity)(Object)this;
        if (self.hasVehicle() && self.getVehicle() instanceof LavaBoatEntity) {
            ci.cancel();
        }
    }

    @Inject(method = "igniteByLava", at = @At("HEAD"), cancellable = true)
    private void oldways$dontIgniteWhenOnWarpedBoat(CallbackInfo ci) {
        Entity self = (Entity)(Object)this;
        if (self.getVehicle() instanceof LavaBoatEntity) {
            ci.cancel();
        }
    }

    @Inject(method = "setOnFireFor(F)V", at = @At("HEAD"))
    private void oldways$onSetOnFireForFloat(float seconds, CallbackInfo ci) {
        Entity e = (Entity)(Object)this;
        // only run on server side where data tracking is authoritative
        if (!(e instanceof LivingEntity) || !(e.getEntityWorld() instanceof ServerWorld)) return;

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

        LivingEntity le = (LivingEntity)e;
        DataTracker tracker = le.getDataTracker();
        if (atSoul) tracker.set(OldWaysTrackedData.OLDWAYS_SOUL_FIRE, (byte)1);
        else if (atFire) tracker.set(OldWaysTrackedData.OLDWAYS_SOUL_FIRE, (byte)0);
    }

    @Inject(method = "setOnFireForTicks(I)V", at = @At("HEAD"))
    private void oldways$onSetOnFireForTicks(int ticks, CallbackInfo ci) {
        Entity e = (Entity)(Object)this;
        // only run on server side where data tracking is authoritative
        if (!(e instanceof LivingEntity) || !(e.getEntityWorld() instanceof ServerWorld)) return;

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

        LivingEntity le = (LivingEntity)e;
        DataTracker tracker = le.getDataTracker();
        if (atSoul) tracker.set(OldWaysTrackedData.OLDWAYS_SOUL_FIRE, (byte)1);
        else if (atFire) tracker.set(OldWaysTrackedData.OLDWAYS_SOUL_FIRE, (byte)0);
    }
}

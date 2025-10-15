package net.hallowed.oldways.mixin.entity.generic;

import com.llamalad7.mixinextras.sugar.Local;
import net.hallowed.oldways.content.entity.vehicle.LavaBoatEntity;
import net.hallowed.oldways.util.EntityInsideFireHandler;
import net.hallowed.oldways.util.FireSourceHolder;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin implements FireSourceHolder {

    /* ------------------------ lava-boat behavior ------------------------ */

    @Shadow public abstract World getEntityWorld();
    @Shadow public abstract DataTracker getDataTracker();

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

    /* ------------------------ fire source tracking ------------------------ */

    @Unique private Block oldways$lastFireSource = Blocks.FIRE;

    @Unique
    private static final TrackedData<Boolean> OLDWAYS_SOULFIRE =
            DataTracker.registerData(Entity.class, TrackedDataHandlerRegistry.BOOLEAN);

    @Inject(
            method = "<init>(Lnet/minecraft/entity/EntityType;Lnet/minecraft/world/World;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;initDataTracker(Lnet/minecraft/entity/data/DataTracker$Builder;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void oldways$addSoulFireTrackedData(EntityType<?> type, World world, CallbackInfo ci,
                                                @Local DataTracker.Builder builder) {
        builder.add(OLDWAYS_SOULFIRE, false);
    }

    @Inject(method = "setOnFireForTicks", at = @At("HEAD"))
    private void oldways$captureSourceOnIgnite(int ticks, CallbackInfo ci) {
        if (ticks > 0) {
            EntityInsideFireHandler.setLastFireSourceFromBlocks((Entity)(Object)this);
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void oldways$refreshSoulFireFlag(CallbackInfo ci) {
        Entity self = (Entity)(Object)this;
        if (self.getEntityWorld().isClient()) return;
        if (self instanceof PlayerEntity) return;

        if (self.isOnFire()) {
            EntityInsideFireHandler.setLastFireSourceFromBlocks(self);
            boolean soul = (oldways$lastFireSource == Blocks.SOUL_FIRE);
            if (this.getDataTracker().get(OLDWAYS_SOULFIRE) != soul) {
                this.getDataTracker().set(OLDWAYS_SOULFIRE, soul);
            }
        }
    }

    /* ------------------------ FireSourceHolder impl ------------------------ */

    @Override
    public Block oldways$getLastFireSource() {
        Entity self = (Entity)(Object)this;
        World world = getEntityWorld();

        if (self instanceof PlayerEntity) {
            return oldways$lastFireSource;
        }

        if (world.isClient()) {
            return this.getDataTracker().get(OLDWAYS_SOULFIRE) ? Blocks.SOUL_FIRE : Blocks.FIRE;
        }
        return oldways$lastFireSource;
    }

    @Override
    public void oldways$setLastFireSource(Block block) {
        this.oldways$lastFireSource = FireSourceHolder.normalize(block);
        getEntityWorld();
    }
}

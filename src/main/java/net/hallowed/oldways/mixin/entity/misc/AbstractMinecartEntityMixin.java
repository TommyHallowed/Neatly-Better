package net.hallowed.oldways.mixin.entity.misc;

import net.hallowed.oldways.api.LinkableMinecart;
import net.hallowed.oldways.util.CartUtils;
import net.hallowed.oldways.util.CollisionUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(AbstractMinecart.class)
public abstract class AbstractMinecartEntityMixin implements LinkableMinecart {

    // --- RE-ADDED DATA TRACKER FOR CLIENT SYNC ---
    @Unique
    private static final EntityDataAccessor<@NotNull Integer> oldways$FOLLOWING_ID = SynchedEntityData.defineId(AbstractMinecart.class, EntityDataSerializers.INT);

    @Unique private AbstractMinecart oldways$following;
    @Unique private AbstractMinecart oldways$follower;
    @Unique private UUID oldways$followingUUID;
    @Unique private UUID oldways$followerUUID;
    @Unique private ItemStack oldways$itemStack = ItemStack.EMPTY;

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void oldways$initDataTracker(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(oldways$FOLLOWING_ID, -1);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void oldways$writeCustomData(ValueOutput view, CallbackInfo ci) {
        if (this.oldways$followingUUID != null) view.putString("LK-Following", this.oldways$followingUUID.toString());
        if (this.oldways$followerUUID != null) view.putString("LK-Follower", this.oldways$followerUUID.toString());
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void oldways$readCustomData(ValueInput view, CallbackInfo ci) {
        String following = view.getStringOr("LK-Following", "");
        if (!following.isEmpty()) { try { this.oldways$followingUUID = UUID.fromString(following); } catch (Exception ignored) {} }

        String follower = view.getStringOr("LK-Follower", "");
        if (!follower.isEmpty()) { try { this.oldways$followerUUID = UUID.fromString(follower); } catch (Exception ignored) {} }
    }

    @Inject(method = "push(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private void oldways$cancelTrainCollision(Entity entity, CallbackInfo ci) {
        if (!CollisionUtils.shouldCollide((Entity) (Object) this, entity)) {
            ci.cancel();
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void oldways$tickPhysics(CallbackInfo ci) {
        AbstractMinecart self = (AbstractMinecart) (Object) this;
        if (self.level().isClientSide()) return;

        if (this.oldways$following == null && this.oldways$followingUUID != null && self.level() instanceof ServerLevel sw) {
            Entity e = sw.getEntity(this.oldways$followingUUID);
            if (e instanceof AbstractMinecart cart) this.oldways$setFollowing(cart);
        }
        if (this.oldways$follower == null && this.oldways$followerUUID != null && self.level() instanceof ServerLevel sw) {
            Entity e = sw.getEntity(this.oldways$followerUUID);
            if (e instanceof AbstractMinecart cart) this.oldways$setFollower(cart);
        }

        if (this.oldways$following != null) {
            if (this.oldways$following.isRemoved()) {
                CartUtils.unlinkFromParent(self);
                return;
            }

            Vec3 myPos = self.position();
            Vec3 targetPos = this.oldways$following.position();

            double linkDistance = 1.0;
            double dist = Math.max(Math.abs(myPos.distanceTo(targetPos)) - linkDistance, 0.0);

            Vec3 vec3d = targetPos.subtract(myPos);

            Vec3 myVel = self.getDeltaMovement();
            Vec3 targetVel = this.oldways$following.getDeltaMovement();

            boolean differentDirection = myVel.length() > 0.15 && targetVel.length() > 0.005
                    && myVel.normalize().distanceTo(targetVel.normalize()) > 1.42
                    && myPos.distanceTo(targetPos) > 0.5;

            if (differentDirection) {
                dist += linkDistance;
                vec3d = myVel;
            }

            if (vec3d.lengthSqr() > 0) {
                vec3d = vec3d.normalize().scale(dist);
            }

            if (dist <= 1.0) {
                self.setDeltaMovement(vec3d.scale(0.8 + 0.2 * Math.abs(dist)));
            } else if (dist <= 8.0) {
                self.setDeltaMovement(vec3d);
            } else {
                CartUtils.unlinkFromParent(self);
            }
        }
    }

    @Override public AbstractMinecart oldways$getFollowing() { return this.oldways$following; }
    @Override public void oldways$setFollowing(AbstractMinecart entity) {
        AbstractMinecart self = (AbstractMinecart) (Object) this;
        this.oldways$following = entity;
        this.oldways$followingUUID = entity != null ? entity.getUUID() : null;

        // Pushes the connected ID to the client!
        self.getEntityData().set(oldways$FOLLOWING_ID, entity != null ? entity.getId() : -1);
    }

    @Override public AbstractMinecart oldways$getFollower() { return this.oldways$follower; }
    @Override public void oldways$setFollower(AbstractMinecart entity) {
        this.oldways$follower = entity;
        this.oldways$followerUUID = entity != null ? entity.getUUID() : null;
    }

    @Override public ItemStack oldways$getLinkItem() { return this.oldways$itemStack; }
    @Override public void oldways$setLinkItem(ItemStack linkItem) { this.oldways$itemStack = linkItem; }

    @Override public int oldways$getFollowingId() {
        AbstractMinecart self = (AbstractMinecart) (Object) this;
        return self.getEntityData().get(oldways$FOLLOWING_ID);
    }
}
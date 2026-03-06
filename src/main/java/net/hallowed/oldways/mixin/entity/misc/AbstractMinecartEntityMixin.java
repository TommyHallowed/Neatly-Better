package net.hallowed.oldways.mixin.entity.misc;

import net.hallowed.oldways.api.LinkableMinecart;
import net.hallowed.oldways.util.CartUtils;
import net.hallowed.oldways.util.CollisionUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(AbstractMinecartEntity.class)
public abstract class AbstractMinecartEntityMixin implements LinkableMinecart {

    // --- RE-ADDED DATA TRACKER FOR CLIENT SYNC ---
    @Unique
    private static final TrackedData<Integer> oldways$FOLLOWING_ID = DataTracker.registerData(AbstractMinecartEntity.class, TrackedDataHandlerRegistry.INTEGER);

    @Unique private AbstractMinecartEntity oldways$following;
    @Unique private AbstractMinecartEntity oldways$follower;
    @Unique private UUID oldways$followingUUID;
    @Unique private UUID oldways$followerUUID;
    @Unique private ItemStack oldways$itemStack = ItemStack.EMPTY;

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void oldways$initDataTracker(DataTracker.Builder builder, CallbackInfo ci) {
        builder.add(oldways$FOLLOWING_ID, -1);
    }

    @Inject(method = "writeCustomData", at = @At("TAIL"))
    private void oldways$writeCustomData(WriteView view, CallbackInfo ci) {
        if (this.oldways$followingUUID != null) view.putString("LK-Following", this.oldways$followingUUID.toString());
        if (this.oldways$followerUUID != null) view.putString("LK-Follower", this.oldways$followerUUID.toString());
    }

    @Inject(method = "readCustomData", at = @At("TAIL"))
    private void oldways$readCustomData(ReadView view, CallbackInfo ci) {
        String following = view.getString("LK-Following", "");
        if (!following.isEmpty()) { try { this.oldways$followingUUID = UUID.fromString(following); } catch (Exception ignored) {} }

        String follower = view.getString("LK-Follower", "");
        if (!follower.isEmpty()) { try { this.oldways$followerUUID = UUID.fromString(follower); } catch (Exception ignored) {} }
    }

    @Inject(method = "pushAwayFrom", at = @At("HEAD"), cancellable = true)
    private void oldways$cancelTrainCollision(Entity entity, CallbackInfo ci) {
        if (!CollisionUtils.shouldCollide((Entity) (Object) this, entity)) {
            ci.cancel();
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void oldways$tickPhysics(CallbackInfo ci) {
        AbstractMinecartEntity self = (AbstractMinecartEntity) (Object) this;
        if (self.getEntityWorld().isClient()) return;

        if (this.oldways$following == null && this.oldways$followingUUID != null && self.getEntityWorld() instanceof ServerWorld sw) {
            Entity e = sw.getEntity(this.oldways$followingUUID);
            if (e instanceof AbstractMinecartEntity cart) this.oldways$setFollowing(cart);
        }
        if (this.oldways$follower == null && this.oldways$followerUUID != null && self.getEntityWorld() instanceof ServerWorld sw) {
            Entity e = sw.getEntity(this.oldways$followerUUID);
            if (e instanceof AbstractMinecartEntity cart) this.oldways$setFollower(cart);
        }

        if (this.oldways$following != null) {
            if (this.oldways$following.isRemoved()) {
                CartUtils.unlinkFromParent(self);
                return;
            }

            Vec3d myPos = self.getEntityPos();
            Vec3d targetPos = this.oldways$following.getEntityPos();

            double linkDistance = 1.0;
            double dist = Math.max(Math.abs(myPos.distanceTo(targetPos)) - linkDistance, 0.0);

            Vec3d vec3d = targetPos.subtract(myPos);

            Vec3d myVel = self.getVelocity();
            Vec3d targetVel = this.oldways$following.getVelocity();

            boolean differentDirection = myVel.length() > 0.15 && targetVel.length() > 0.005
                    && myVel.normalize().distanceTo(targetVel.normalize()) > 1.42
                    && myPos.distanceTo(targetPos) > 0.5;

            if (differentDirection) {
                dist += linkDistance;
                vec3d = myVel;
            }

            if (vec3d.lengthSquared() > 0) {
                vec3d = vec3d.normalize().multiply(dist);
            }

            if (dist <= 1.0) {
                self.setVelocity(vec3d.multiply(0.8 + 0.2 * Math.abs(dist)));
            } else if (dist <= 8.0) {
                self.setVelocity(vec3d);
            } else {
                CartUtils.unlinkFromParent(self);
            }
        }
    }

    @Override public AbstractMinecartEntity oldways$getFollowing() { return this.oldways$following; }
    @Override public void oldways$setFollowing(AbstractMinecartEntity entity) {
        AbstractMinecartEntity self = (AbstractMinecartEntity) (Object) this;
        this.oldways$following = entity;
        this.oldways$followingUUID = entity != null ? entity.getUuid() : null;

        // Pushes the connected ID to the client!
        self.getDataTracker().set(oldways$FOLLOWING_ID, entity != null ? entity.getId() : -1);
    }

    @Override public AbstractMinecartEntity oldways$getFollower() { return this.oldways$follower; }
    @Override public void oldways$setFollower(AbstractMinecartEntity entity) {
        this.oldways$follower = entity;
        this.oldways$followerUUID = entity != null ? entity.getUuid() : null;
    }

    @Override public ItemStack oldways$getLinkItem() { return this.oldways$itemStack; }
    @Override public void oldways$setLinkItem(ItemStack linkItem) { this.oldways$itemStack = linkItem; }

    @Override public int oldways$getFollowingId() {
        AbstractMinecartEntity self = (AbstractMinecartEntity) (Object) this;
        return self.getDataTracker().get(oldways$FOLLOWING_ID);
    }
}
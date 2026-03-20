package net.hallowed.neatlybetter.mixin.entity.misc;

import net.hallowed.neatlybetter.api.LinkableMinecart;
import net.hallowed.neatlybetter.util.CartUtils;
import net.hallowed.neatlybetter.util.CollisionUtils;

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
public abstract class AbstractMinecartMixin implements LinkableMinecart {
    @Unique
    private static final EntityDataAccessor<@NotNull Integer> neatlybetter$FOLLOWING_ID = SynchedEntityData.defineId(AbstractMinecart.class, EntityDataSerializers.INT);

    @Unique private AbstractMinecart neatlybetter$following;
    @Unique private AbstractMinecart neatlybetter$follower;
    @Unique private UUID neatlybetter$followingUUID;
    @Unique private UUID neatlybetter$followerUUID;
    @Unique private ItemStack neatlybetter$itemStack = ItemStack.EMPTY;

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void neatlybetter$initDataTracker(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(neatlybetter$FOLLOWING_ID, -1);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void neatlybetter$writeCustomData(ValueOutput view, CallbackInfo ci) {
        if (this.neatlybetter$followingUUID != null) view.putString("LK-Following", this.neatlybetter$followingUUID.toString());
        if (this.neatlybetter$followerUUID != null) view.putString("LK-Follower", this.neatlybetter$followerUUID.toString());
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void neatlybetter$readCustomData(ValueInput view, CallbackInfo ci) {
        String following = view.getStringOr("LK-Following", "");
        if (!following.isEmpty()) { try { this.neatlybetter$followingUUID = UUID.fromString(following); } catch (Exception ignored) {} }

        String follower = view.getStringOr("LK-Follower", "");
        if (!follower.isEmpty()) { try { this.neatlybetter$followerUUID = UUID.fromString(follower); } catch (Exception ignored) {} }
    }

    @Inject(method = "push(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private void neatlybetter$cancelTrainCollision(Entity entity, CallbackInfo ci) {
        if (!CollisionUtils.shouldCollide((Entity) (Object) this, entity)) {
            ci.cancel();
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void neatlybetter$tickPhysics(CallbackInfo ci) {
        AbstractMinecart self = (AbstractMinecart) (Object) this;
        if (self.level().isClientSide()) return;

        if (this.neatlybetter$following == null && this.neatlybetter$followingUUID != null && self.level() instanceof ServerLevel sw) {
            Entity e = sw.getEntity(this.neatlybetter$followingUUID);
            if (e instanceof AbstractMinecart cart) this.neatlybetter$setFollowing(cart);
        }
        if (this.neatlybetter$follower == null && this.neatlybetter$followerUUID != null && self.level() instanceof ServerLevel sw) {
            Entity e = sw.getEntity(this.neatlybetter$followerUUID);
            if (e instanceof AbstractMinecart cart) this.neatlybetter$setFollower(cart);
        }

        if (this.neatlybetter$following != null) {
            if (this.neatlybetter$following.isRemoved()) {
                CartUtils.unlinkFromParent(self);
                return;
            }

            Vec3 myPos = self.position();
            Vec3 targetPos = this.neatlybetter$following.position();

            double linkDistance = 1.0;
            double dist = Math.max(Math.abs(myPos.distanceTo(targetPos)) - linkDistance, 0.0);

            Vec3 vec3d = targetPos.subtract(myPos);

            Vec3 myVel = self.getDeltaMovement();
            Vec3 targetVel = this.neatlybetter$following.getDeltaMovement();

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

    @Override public AbstractMinecart neatlybetter$getFollowing() { return this.neatlybetter$following; }
    @Override public void neatlybetter$setFollowing(AbstractMinecart entity) {
        AbstractMinecart self = (AbstractMinecart) (Object) this;
        this.neatlybetter$following = entity;
        this.neatlybetter$followingUUID = entity != null ? entity.getUUID() : null;

        self.getEntityData().set(neatlybetter$FOLLOWING_ID, entity != null ? entity.getId() : -1);
    }

    @Override public AbstractMinecart neatlybetter$getFollower() { return this.neatlybetter$follower; }
    @Override public void neatlybetter$setFollower(AbstractMinecart entity) {
        this.neatlybetter$follower = entity;
        this.neatlybetter$followerUUID = entity != null ? entity.getUUID() : null;
    }

    @Override public ItemStack neatlybetter$getLinkItem() { return this.neatlybetter$itemStack; }
    @Override public void neatlybetter$setLinkItem(ItemStack linkItem) { this.neatlybetter$itemStack = linkItem; }

    @Override public int neatlybetter$getFollowingId() {
        AbstractMinecart self = (AbstractMinecart) (Object) this;
        return self.getEntityData().get(neatlybetter$FOLLOWING_ID);
    }
}
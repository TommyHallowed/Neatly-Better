package net.hallowed.neatlybetter.mixin.entity.misc;

import net.hallowed.neatlybetter.duck.LinkableMinecart;
import net.hallowed.neatlybetter.feature.minecart.CollisionUtils;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import org.jetbrains.annotations.NotNull;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
    private void neatlybetter$initDataTracker(SynchedEntityData.Builder entityData, CallbackInfo ci) {
        entityData.define(neatlybetter$FOLLOWING_ID, -1);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void neatlybetter$writeCustomData(ValueOutput output, CallbackInfo ci) {
        if (this.neatlybetter$followingUUID != null) output.putString("Following", this.neatlybetter$followingUUID.toString());
        if (this.neatlybetter$followerUUID != null) output.putString("Follower", this.neatlybetter$followerUUID.toString());
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void neatlybetter$readCustomData(ValueInput input, CallbackInfo ci) {
        String following = input.getStringOr("Following", "");
        if (!following.isEmpty()) { try { this.neatlybetter$followingUUID = UUID.fromString(following); } catch (Exception ignored) {} }

        String follower = input.getStringOr("Follower", "");
        if (!follower.isEmpty()) { try { this.neatlybetter$followerUUID = UUID.fromString(follower); } catch (Exception ignored) {} }
    }

    @Inject(method = "push(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private void neatlybetter$cancelTrainCollision(Entity entity, CallbackInfo ci) {
        if (CollisionUtils.shouldCollide((Entity) (Object) this, entity)) {
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
    }

    @Inject(method = "useExperimentalMovement", at = @At("HEAD"), cancellable = true)
    private static void neatlybetter$forceExperimentalMovement(Level level, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
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
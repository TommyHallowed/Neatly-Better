package net.hallowed.oldways.mixin.entity.hostile;

import net.hallowed.oldways.init.ModGameRules;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ZombifiedPiglinEntity.class)
public abstract class ZombifiedPiglinEntityMixin {

    @Unique
    protected int playerHitTimer;
    @Unique
    @Nullable protected PlayerEntity attackingPlayer;

    @Unique private static final int REFRESH_PERIOD_TICKS = 20;
    @Unique private static final double MAX_DISTANCE = 96.0D;
    @Unique private static final double MAX_DISTANCE_SQ = MAX_DISTANCE * MAX_DISTANCE;

    @Unique private int oldways$lastRefreshTick = 0;

    @Inject(method = "mobTick(Lnet/minecraft/server/world/ServerWorld;)V", at = @At("RETURN"))
    private void oldways$keepAngryForever(ServerWorld world, CallbackInfo ci) {
        final ZombifiedPiglinEntity self = (ZombifiedPiglinEntity)(Object)this;

        if (!world.getGameRules().getBoolean(ModGameRules.OLD_GOLD_XP_FARM)) return;

        if (self.getAngerTime() <= 0) return;
        if (self.age - oldways$lastRefreshTick < REFRESH_PERIOD_TICKS) return;

        LivingEntity target = self.getTarget();
        if (target instanceof PlayerEntity player) {
            if (self.squaredDistanceTo(player) <= MAX_DISTANCE_SQ) {
                this.playerHitTimer = self.age;
                this.attackingPlayer = player;
                oldways$lastRefreshTick = self.age;
            }
        }
    }

    @Inject(method = "setTarget(Lnet/minecraft/entity/LivingEntity;)V", at = @At("RETURN"))
    private void oldways$onTargetSet(@Nullable LivingEntity target, CallbackInfo ci) {
        final ZombifiedPiglinEntity self = (ZombifiedPiglinEntity)(Object)this;

        if (!(self.getWorld() instanceof ServerWorld sw)) return;
        if (!sw.getGameRules().getBoolean(ModGameRules.OLD_GOLD_XP_FARM)) return;

        if (target instanceof PlayerEntity player) {
            this.playerHitTimer = self.age;
            this.attackingPlayer = player;
        }
    }

}

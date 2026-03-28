package net.hallowed.neatlybetter.mixin.entity.generic;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.hallowed.neatlybetter.config.NTServerConfig;
import net.hallowed.neatlybetter.content.feature.ItemCooldownHandler;
import net.hallowed.neatlybetter.util.ProtectionContext;

import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("ConstantValue")
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    protected LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow
    public abstract boolean onClimbable();

    @Unique
    private static final Identifier neatlybetter$STEP_UP_ID =
            Identifier.fromNamespaceAndPath("neatly-better", "enchantment.step_up/legs");

    /* ===================== 1) Totem cooldown ===================== */
    @Inject(
            method = "checkTotemDeathProtection(Lnet/minecraft/world/damagesource/DamageSource;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void neatlybetter$blockIfTotemCooling(DamageSource source,
                                                  CallbackInfoReturnable<Boolean> cir) {
        //noinspection ConstantValue
        if ((Object) this instanceof Player player
                && ItemCooldownHandler.isTotemOnCooldown(player)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(
            method = "checkTotemDeathProtection(Lnet/minecraft/world/damagesource/DamageSource;)Z",
            at = @At("RETURN")
    )
    private void neatlybetter$onTotemUsed(DamageSource source,
                                          CallbackInfoReturnable<Boolean> cir) {
        if (!Boolean.TRUE.equals(cir.getReturnValue())) return;

        if ((Object) this instanceof Player player) {
            ItemCooldownHandler.applyTotemCooldown(player);
        }
    }

    /* ===================== 2) Protection context ===================== */
    @Inject(method = "getDamageAfterMagicAbsorb", at = @At("HEAD"))
    private void neatlybetter$setProtContext(DamageSource source, float amount,
                                             CallbackInfoReturnable<Float> cir) {
        if (!NTServerConfig.CONFIG.protectionOverhaul.get()) return;
        ProtectionContext.set((LivingEntity) (Object) this, source);
    }

    @Inject(method = "getDamageAfterMagicAbsorb", at = @At("RETURN"))
    private void neatlybetter$clearProtContext(DamageSource source, float amount,
                                               CallbackInfoReturnable<Float> cir) {
        if (!NTServerConfig.CONFIG.protectionOverhaul.get()) return;
        ProtectionContext.clear();
    }

    /* ===================== 3) Resistance effect absorption nerf ===================== */
    @ModifyConstant(method = "getDamageAfterMagicAbsorb", constant = @Constant(intValue = 25))
    private int neatlybetter$resistanceDenominatorInt(int original) {
        if (!NTServerConfig.CONFIG.resistanceOverhaul.get()) return original;
        return 50;
    }

    @ModifyConstant(method = "getDamageAfterMagicAbsorb", constant = @Constant(floatValue = 25.0F))
    private float neatlybetter$resistanceDenominatorFloat(float original) {
        if (!NTServerConfig.CONFIG.resistanceOverhaul.get()) return original;
        return 50.0F;
    }

    /* ===================== 4) Explosions disable shields ===================== */
    @Inject(
            method = "applyItemBlocking(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)F",
            at = @At("RETURN")
    )
    private void neatlybetter$explosionDisablesShield(ServerLevel world,
                                                      DamageSource source,
                                                      float amount,
                                                      CallbackInfoReturnable<Float> cir) {

        if (!NTServerConfig.CONFIG.explosionsDisableShield.get()) return;
        if (cir.getReturnValue() <= 0.0F) return;
        if (!source.is(DamageTypeTags.IS_EXPLOSION)) return;

        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof Player player)) return;

        ItemStack blocking = self.getItemBlockingWith();
        if (blocking == null || blocking.isEmpty()) return;

        BlocksAttacks blocks = blocking.get(DataComponents.BLOCKS_ATTACKS);
        if (blocks == null) return;

        blocks.disable(world, player, 5.0F, blocking);
        self.releaseUsingItem();
    }

    /* ===================== 5) No shield raise delay ===================== */
    @WrapOperation(
            method = "getItemBlockingWith",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/component/BlocksAttacks;blockDelayTicks()I"
            )
    )
    private int neatlybetter$customBlockDelay(BlocksAttacks instance, Operation<Integer> original) {
        int configDelay = NTServerConfig.CONFIG.shieldRaiseDelay.get();
        if (configDelay == 5) return original.call(instance);
        return configDelay;
    }

    /* ===================== 6) Step Up disabled while sneaking ===================== */
    @ModifyReturnValue(method = "maxUpStep", at = @At("RETURN"))
    private float neatlybetter$suppressStepUpWhileShifting(float original) {

        if (!NTServerConfig.CONFIG.stepUpDisabledWhileShifting.get()) return original;

        LivingEntity self = (LivingEntity) (Object) this;

        boolean shifting = self.isShiftKeyDown();
        AttributeInstance attr = self.getAttribute(Attributes.STEP_HEIGHT);
        AttributeModifier mod = attr != null ? attr.getModifier(neatlybetter$STEP_UP_ID) : null;

        if (!shifting) return original;
        if (mod == null) return original;

        return original - (float) mod.amount();
    }

    /* ===================== 7) Faster climbing ===================== */

    @Unique private int neatlybetter$climbUpTicks = 0;
    @Unique private int neatlybetter$climbDownTicks = 0;
    @Unique private boolean neatlybetter$climbingUpThisTick = false;

    @WrapOperation(
            method = "handleOnClimbable",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/Mth;clamp(DDD)D"
            )
    )
    private double neatlybetter$removeHorizontalClamp(
            double speed, double min, double max, Operation<Double> original
    ) {
        if (!((Object) this instanceof Player) || !this.onGround()) {
            return original.call(speed, min, max);
        }
        return speed;
    }

    @WrapOperation(
            method = "handleOnClimbable",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/Math;max(DD)D"
            )
    )
    private double neatlybetter$modifyDownwardClimbSpeed(
            double currentY, double vanillaMin, Operation<Double> original
    ) {
        if (!((Object) this instanceof Player)) {
            return original.call(currentY, vanillaMin);
        }

        double maxDown = Mth.clampedMap(getXRot(), 20, 90, vanillaMin, -0.4);
        if (maxDown < vanillaMin) {
            maxDown = Mth.clampedMap(
                    neatlybetter$climbDownTicks, 0, 60,
                    maxDown, maxDown * 1.5
            );
        }
        return original.call(currentY, maxDown);
    }

    @ModifyArg(
            method = "handleRelativeFrictionAndCalculateMovement",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/phys/Vec3;<init>(DDD)V"
            ),
            index = 1
    )
    private double neatlybetter$speedUpClimbingUp(double vanillaClimbSpeed) {
        if (!((Object) this instanceof Player)) {
            return vanillaClimbSpeed;
        }

        if (this.getXRot() > -20) {
            return vanillaClimbSpeed;
        }

        neatlybetter$climbingUpThisTick = true;

        double boosted = vanillaClimbSpeed * 1.25;
        double ramped = Mth.clampedMap(
                neatlybetter$climbUpTicks, 0, 60,
                boosted, boosted * 2.5
        );
        return Math.max(this.getDeltaMovement().y, ramped);
    }

    @Inject(
            method = "handleRelativeFrictionAndCalculateMovement",
            at = @At("RETURN")
    )
    private void neatlybetter$updateClimbTimers(
            Vec3 vec3, float f, CallbackInfoReturnable<Vec3> cir
    ) {
        if (!((Object) this instanceof Player)) return;

        Vec3 movement = cir.getReturnValue();

        if (onClimbable() && movement.y < 0 && getXRot() > 20) {
            neatlybetter$climbDownTicks++;
        } else {
            neatlybetter$climbDownTicks = 0;
        }

        if (neatlybetter$climbingUpThisTick) {
            neatlybetter$climbUpTicks++;
            neatlybetter$climbingUpThisTick = false;
        } else {
            neatlybetter$climbUpTicks = 0;
        }
    }
}

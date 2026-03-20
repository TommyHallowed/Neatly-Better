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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlocksAttacks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

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
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self.level() instanceof ServerLevel)) return original.call(instance);

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
}

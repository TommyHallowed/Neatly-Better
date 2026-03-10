package net.hallowed.oldways.mixin.entity.generic;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.hallowed.oldways.init.ModGameRules;
import net.hallowed.oldways.util.ProtectionContext;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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

    // Cached — the cooldown API only reads the item's cooldown group, never mutates the stack
    @Unique
    private static ItemStack oldways$totemStack;

    @Unique
    private static ItemStack oldways$getTotemStack() {
        if (oldways$totemStack == null) {
            oldways$totemStack = new ItemStack(Items.TOTEM_OF_UNDYING);
        }
        return oldways$totemStack;
    }

    /* ===================== 1) Totem cooldown ===================== */

    @Inject(
            method = "checkTotemDeathProtection(Lnet/minecraft/world/damagesource/DamageSource;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void oldways$blockIfTotemCooling(DamageSource source,
                                             CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof Player player) {
            if (player.getCooldowns().isOnCooldown(oldways$getTotemStack())) {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(
            method = "checkTotemDeathProtection(Lnet/minecraft/world/damagesource/DamageSource;)Z",
            at = @At("RETURN")
    )
    private void oldways$applyTotemCooldown(DamageSource source,
                                            CallbackInfoReturnable<Boolean> cir) {
        // FIX: Only apply cooldown when the totem actually saved the player
        if (!Boolean.TRUE.equals(cir.getReturnValue())) return;

        if ((Object) this instanceof Player player) {
            player.getCooldowns().addCooldown(oldways$getTotemStack(), 1200);
        }
    }

    /* ===================== 2) Protection context ===================== */

    @Inject(method = "getDamageAfterMagicAbsorb", at = @At("HEAD"))
    private void oldways$setProtContext(DamageSource source, float amount,
                                        CallbackInfoReturnable<Float> cir) {
        ProtectionContext.set((LivingEntity) (Object) this, source);
    }

    @Inject(method = "getDamageAfterMagicAbsorb", at = @At("RETURN"))
    private void oldways$clearProtContext(DamageSource source, float amount,
                                          CallbackInfoReturnable<Float> cir) {
        ProtectionContext.clear();
    }

    /* ===================== 3) Resistance effect absorption nerf ===================== */

    @ModifyConstant(method = "getDamageAfterMagicAbsorb", constant = @Constant(intValue = 25))
    private int oldways$resistanceDenominatorInt(int original) {
        return 50;
    }

    @ModifyConstant(method = "getDamageAfterMagicAbsorb", constant = @Constant(floatValue = 25.0F))
    private float oldways$resistanceDenominatorFloat(float original) {
        return 50.0F;
    }

    /* ===================== 4) Explosions disable shields ===================== */

    @Inject(
            method = "applyItemBlocking(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)F",
            at = @At("RETURN")
    )
    private void oldways$explosionDisablesShield(ServerLevel world,
                                                 DamageSource source,
                                                 float amount,
                                                 CallbackInfoReturnable<Float> cir) {
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

    // FIX: @ModifyReturnValue chains with other mods (replaces @Inject HEAD which killed compat)
    // NOTE: getGameRules() only exists on ServerLevel in 1.21.11, so this stays server-side only.
    //       The client may briefly show the shield raised before the server agrees — this is a
    //       vanilla limitation. To fix the visual desync you'd need to sync the game rule value
    //       to the client via a custom packet.
    @ModifyReturnValue(
            method = "getItemBlockingWith()Lnet/minecraft/world/item/ItemStack;",
            at = @At("RETURN")
    )
    private ItemStack oldways$customShieldRaiseDelay(ItemStack original) {
        if (original == null || original.isEmpty() || !original.is(Items.SHIELD)) return original;

        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self.level() instanceof ServerLevel serverLevel)) return original;
        int delay = Math.max(0, serverLevel.getGameRules().get(ModGameRules.SHIELD_RAISE_DELAY_TICKS));
        if (delay <= 0) return original;

        return self.getTicksUsingItem() >= delay ? original : ItemStack.EMPTY;
    }
}
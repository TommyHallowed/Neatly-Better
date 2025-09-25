package net.hallowed.oldways.mixin.shield;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BlocksAttacksComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * If an explosion is blocked by a shield, disable the shield like an axe/vindicator hit.
 * Uses the same vanilla path as PlayerEntity#takeShieldHit: BlocksAttacksComponent#applyShieldCooldown.
 */
@Mixin(LivingEntity.class)
public abstract class ExplosionsDisableShields {

    /**
     * 1.21.8 signature:
     *   float LivingEntity#getDamageBlockedAmount(ServerWorld, DamageSource, float)

     * We run at RETURN: if any damage was blocked and the source is an explosion,
     * apply the standard shield cooldown to the blocking item and stop blocking.
     */
    @Inject(
            method = "getDamageBlockedAmount(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;F)F",
            at = @At("RETURN")
    )
    private void oldways$explosionDisablesShield(ServerWorld world,
                                                 DamageSource source,
                                                 float amount,
                                                 CallbackInfoReturnable<Float> cir) {
        // Only care if something was actually blocked and the source is an explosion
        if (cir.getReturnValue() <= 0.0F) return;
        if (!source.isIn(DamageTypeTags.IS_EXPLOSION)) return;

        LivingEntity self = (LivingEntity)(Object)this;
        if (!(self instanceof PlayerEntity player)) return;

        // Use the *blocking* item (can be null)
        ItemStack blocking = self.getBlockingItem();
        if (blocking == null || blocking.isEmpty()) return;

        BlocksAttacksComponent blocks = blocking.get(DataComponentTypes.BLOCKS_ATTACKS);
        if (blocks == null) return;

        // Match vanilla disable timing (same path as takeShieldHit)
        // 5 seconds is the vanilla default if the attacker provides that value;
        // here we hardcode 5.0F for explosions.
        blocks.applyShieldCooldown(world, player, 5.0F, blocking);

        // Immediately drop out of blocking
        self.stopUsingItem();
    }
}

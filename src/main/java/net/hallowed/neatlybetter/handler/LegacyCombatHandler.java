package net.hallowed.neatlybetter.handler;

import net.hallowed.neatlybetter.config.NTServerConfig;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public final class LegacyCombatHandler {

    private LegacyCombatHandler() {}

    private static final Set<SoundEvent> CANCELED_ATTACK_SOUNDS = Set.of(
            SoundEvents.PLAYER_ATTACK_CRIT,
            SoundEvents.PLAYER_ATTACK_KNOCKBACK,
            SoundEvents.PLAYER_ATTACK_NODAMAGE,
            SoundEvents.PLAYER_ATTACK_STRONG,
            SoundEvents.PLAYER_ATTACK_WEAK,
            SoundEvents.PLAYER_ATTACK_SWEEP
    );

    private static final Set<SimpleParticleType> CANCELED_PARTICLES = Set.of(
            ParticleTypes.SWEEP_ATTACK,
            ParticleTypes.DAMAGE_INDICATOR
    );

    public static boolean shouldCancelAttackSound(SoundEvent soundEvent) {
        if (!NTServerConfig.CONFIG.legacyCombat.get()) {
            return false;
        }
        return CANCELED_ATTACK_SOUNDS.contains(soundEvent);
    }

    public static boolean shouldCancelParticle(net.minecraft.core.particles.ParticleOptions particleOptions) {
        if (!NTServerConfig.CONFIG.legacyCombat.get()) {
            return false;
        }
        return CANCELED_PARTICLES.contains(particleOptions.getType());
    }

    public static void applyUpwardsKnockback(LivingEntity entity, double strength, double ratioX, double ratioZ) {
        if (!NTServerConfig.CONFIG.legacyCombat.get()) {
            return;
        }
        if (!entity.onGround() && !entity.isInWater()) {
            double kbResistance = entity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
            double adjustedStrength = strength * (1.0 - kbResistance);
            Vec3 deltaMovement = entity.getDeltaMovement();
            double newY = Math.min(0.4, deltaMovement.y / 2.0 + adjustedStrength);
            entity.setDeltaMovement(deltaMovement.x, newY, deltaMovement.z);
        }
    }

    public static boolean isSword(ItemStack stack) {
        if (stack.isEmpty()) return false;

        if (stack.is(Items.WOODEN_SWORD)
                || stack.is(Items.STONE_SWORD)
                || stack.is(Items.COPPER_SWORD)
                || stack.is(Items.IRON_SWORD)
                || stack.is(Items.GOLDEN_SWORD)
                || stack.is(Items.DIAMOND_SWORD)
                || stack.is(Items.NETHERITE_SWORD)) {
            return true;
        }

        Tool tool = stack.get(DataComponents.TOOL);
        if (tool == null) return false;

        Identifier swordTag = BlockTags.SWORD_EFFICIENT.location();
        return tool.rules().stream().anyMatch(rule ->
                rule.blocks().unwrapKey()
                        .map(key -> key.location().equals(swordTag))
                        .orElse(false)
        );
    }

    public static boolean isSwordBlocking(LivingEntity entity) {
        if (!NTServerConfig.CONFIG.legacyCombat.get()) {
            return false;
        }
        return entity.isUsingItem() && isSword(entity.getUseItem());
    }

    public static float applySwordBlockingReduction(LivingEntity entity, DamageSource source, float amount) {
        if (!NTServerConfig.CONFIG.legacyCombat.get() || amount <= 0.0F) {
            return amount;
        }
        if (isSwordBlocking(entity)) {
            if (!source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_SHIELD)
                    && !source.is(net.minecraft.tags.DamageTypeTags.IS_EXPLOSION)
                    && !source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE)) {
                return amount * 0.5F;
            }
        }
        return amount;
    }

    public static void damageSwordOnBlock(LivingEntity entity, float blockedDamage) {
        if (!NTServerConfig.CONFIG.legacyCombat.get()) {
            return;
        }
        if (!(entity instanceof Player player)) {
            return;
        }
        ItemStack blockingItem = player.getUseItem();
        if (isSword(blockingItem) && !player.level().isClientSide()) {
            blockingItem.hurtAndBreak(1, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
        }
    }
}

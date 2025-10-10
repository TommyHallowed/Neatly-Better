// src/main/java/net/hallowed/oldways/mixin/blockentity/BeaconBlockEntityMixin.java
package net.hallowed.oldways.mixin.blockentity;

import net.hallowed.oldways.init.ModGameRules;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BeaconBlockEntity.class)
abstract class BeaconBlockEntityMixin {

    @Redirect(
            method = "applyPlayerEffects",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Box;expand(D)Lnet/minecraft/util/math/Box;")
    )
    private static Box oldways$useScaledGamerule(
            Box box, double vanillaRadius,
            World world, BlockPos pos, int beaconLevel,
            RegistryEntry<StatusEffect> primary, RegistryEntry<StatusEffect> secondary
    ) {
        if (world instanceof ServerWorld sw) {
            int rule = sw.getGameRules().getInt(ModGameRules.MAX_BEACON_RANGE);
            if (rule > 0) {
                final int MAX_LEVEL = 4;
                final double VANILLA_MAX_RADIUS = MAX_LEVEL * 10.0 + 10.0;

                double scaledRadius = (beaconLevel * 10.0 + 10.0) * (rule / VANILLA_MAX_RADIUS);
                return box.expand(scaledRadius);
            }
        }
        return box.expand(vanillaRadius);
    }

    @Redirect(
            method = "applyPlayerEffects",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;)Z")
    )
    private static boolean oldways$soakOrVanilla(
            PlayerEntity player, StatusEffectInstance instance,
            World world, BlockPos pos, int beaconLevel,
            RegistryEntry<StatusEffect> primary, RegistryEntry<StatusEffect> secondary
    ) {
        boolean soak = false;
        if (world instanceof ServerWorld sw) {
            soak = sw.getGameRules().getBoolean(ModGameRules.BEACON_SOAK_EFFECTS);
        }
        if (!soak) return player.addStatusEffect(instance);

        int cap = Math.max(1, beaconLevel) * 60 * 20;
        int current = 0;
        StatusEffectInstance existing = player.getStatusEffect(instance.getEffectType());
        if (existing != null) current = existing.getDuration();

        int total = Math.min(cap, current + instance.getDuration());
        StatusEffectInstance applied =
                new StatusEffectInstance(instance.getEffectType(), total, instance.getAmplifier(), true, true);
        return player.addStatusEffect(applied);
    }
}

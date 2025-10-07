package net.hallowed.oldways.mixin.blockentity;

import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
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
    private static Box oldways$scaleByLevel(Box box, double value, World world, BlockPos pos, int beaconLevel,
                                            RegistryEntry<StatusEffect> primary, RegistryEntry<StatusEffect> secondary) {
        double scale = beaconLevel >= 3 ? 1.5D : (beaconLevel == 2 ? 1.25D : 1.0D);
        return box.expand(value * scale);
    }

    @Redirect(
            method = "applyPlayerEffects",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;)Z")
    )
    private static boolean oldways$stackAndSpeed(PlayerEntity player, StatusEffectInstance instance,
                                                 World world, BlockPos pos, int beaconLevel,
                                                 RegistryEntry<StatusEffect> primary, RegistryEntry<StatusEffect> secondary) {
        int cap = Math.max(1, beaconLevel) * 60 * 20;
        double speed = beaconLevel >= 3 ? 1.5D : (beaconLevel == 2 ? 1.25D : 1.0D);
        int current = 0;
        StatusEffectInstance existing = player.getStatusEffect(instance.getEffectType());
        if (existing != null) current = existing.getDuration();
        int added = (int)Math.round(instance.getDuration() * speed);
        int total = current + added;
        if (total > cap) total = cap;
        StatusEffectInstance applied = new StatusEffectInstance(instance.getEffectType(), total, instance.getAmplifier(), true, true);
        return player.addStatusEffect(applied);
    }
}

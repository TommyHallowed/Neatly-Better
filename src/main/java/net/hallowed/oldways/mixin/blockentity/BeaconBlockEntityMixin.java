package net.hallowed.oldways.mixin.blockentity;

import net.hallowed.oldways.init.ModGameRules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BeaconBlockEntity.class)
abstract class BeaconBlockEntityMixin {

    @Redirect(
            method = "applyEffects",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/AABB;inflate(D)Lnet/minecraft/world/phys/AABB;")
    )
    private static AABB oldways$useScaledGamerule(
            AABB box, double vanillaRadius,
            Level world, BlockPos pos, int beaconLevel,
            Holder<@NotNull MobEffect> primary, Holder<@NotNull MobEffect> secondary
    ) {
        if (world instanceof ServerLevel sw) {
            int rule = sw.getGameRules().get(ModGameRules.MAX_BEACON_RANGE);
            if (rule > 0) {
                final int MAX_LEVEL = 4;
                final double VANILLA_MAX_RADIUS = MAX_LEVEL * 10.0 + 10.0;

                double scaledRadius = (beaconLevel * 10.0 + 10.0) * (rule / VANILLA_MAX_RADIUS);
                return box.inflate(scaledRadius);
            }
        }
        return box.inflate(vanillaRadius);
    }

    @Redirect(
            method = "applyEffects",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z")
    )
    private static boolean oldways$soakOrVanilla(
            Player player, MobEffectInstance instance,
            Level world, BlockPos pos, int beaconLevel,
            Holder<@NotNull MobEffect> primary, Holder<@NotNull MobEffect> secondary
    ) {
        boolean soak = false;
        if (world instanceof ServerLevel sw) {
            soak = sw.getGameRules().get(ModGameRules.BEACON_SOAK_EFFECTS);
        }
        if (!soak) return player.addEffect(instance);

        int cap = Math.max(1, beaconLevel) * 60 * 20;
        int current = 0;
        MobEffectInstance existing = player.getEffect(instance.getEffect());
        if (existing != null) current = existing.getDuration();

        int total = Math.min(cap, current + instance.getDuration());
        MobEffectInstance applied =
                new MobEffectInstance(instance.getEffect(), total, instance.getAmplifier(), true, true);
        return player.addEffect(applied);
    }
}

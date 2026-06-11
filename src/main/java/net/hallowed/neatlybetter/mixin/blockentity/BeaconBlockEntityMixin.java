package net.hallowed.neatlybetter.mixin.blockentity;

import net.hallowed.neatlybetter.api.PremiumBaseAccessor;
import net.hallowed.neatlybetter.config.NTServerConfig;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import org.jetbrains.annotations.NotNull;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BeaconBlockEntity.class, priority = 900)
public class BeaconBlockEntityMixin implements PremiumBaseAccessor {

    // =====================================================================
    //  Premium-base detection
    // =====================================================================

    @Shadow
    private int levels;

    @Unique
    private int neatlybetter$premiumBase = 0;

    @Inject(method = "tick", at = @At("TAIL"))
    private static void neatlybetter$updatePremiumBase(
            Level level, BlockPos pos, BlockState selfState,
            BeaconBlockEntity entity, CallbackInfo ci) {

        if (!NTServerConfig.CONFIG.beaconSaturationEffect.get()) return;
        if (level.isClientSide()) return;

        BeaconBlockEntityMixin self = (BeaconBlockEntityMixin)(Object) entity;

        assert self != null;
        if (self.levels < 4) {
            self.neatlybetter$premiumBase = 0;
            return;
        }

        self.neatlybetter$premiumBase = neatlybetter$computePremiumBase(level, pos) ? 1 : 0;
    }

    @Unique
    private static boolean neatlybetter$computePremiumBase(Level level, BlockPos pos) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        for (int step = 1; step <= 4; step++) {
            int ly = y - step;
            if (ly < level.getMinY()) return false;

            for (int lx = x - step; lx <= x + step; lx++) {
                for (int lz = z - step; lz <= z + step; lz++) {
                    BlockState state = level.getBlockState(new BlockPos(lx, ly, lz));

                    if (!state.is(BlockTags.BEACON_BASE_BLOCKS)) return false;
                    if (!state.is(Blocks.DIAMOND_BLOCK) && !state.is(Blocks.NETHERITE_BLOCK)) return false;
                }
            }
        }
        return true;
    }

    @Override
    @Unique
    public int neatlybetter$getPremiumBase() {
        return neatlybetter$premiumBase;
    }

    // =====================================================================
    //  Configurable beacon range
    // =====================================================================

    @Redirect(
            method = "applyEffects",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/phys/AABB;inflate(D)Lnet/minecraft/world/phys/AABB;"
            )
    )
    private static AABB neatlybetter$useScaledConfigValue(
            AABB box, double vanillaRadius,
            Level world, BlockPos pos, int beaconLevel,
            Holder<@NotNull MobEffect> primary, Holder<@NotNull MobEffect> secondary) {

        if (world instanceof ServerLevel) {
            int rule = NTServerConfig.CONFIG.maxBeaconRange.get();
            if (rule > 0) {
                final int MAX_LEVEL = 4;
                final double VANILLA_MAX_RADIUS = MAX_LEVEL * 10.0 + 10.0;
                double scaledRadius = (beaconLevel * 10.0 + 10.0) * (rule / VANILLA_MAX_RADIUS);
                return box.inflate(scaledRadius);
            }
        }
        return box.inflate(vanillaRadius);
    }

    // =====================================================================
    //  Soak-effects mode (stacking duration)
    // =====================================================================

    @Redirect(
            method = "applyEffects",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z"
            )
    )
    private static boolean neatlybetter$soakOrVanilla(
            Player player, MobEffectInstance instance,
            Level world, BlockPos pos, int beaconLevel,
            Holder<@NotNull MobEffect> primary, Holder<@NotNull MobEffect> secondary) {

        boolean soak = false;
        if (world instanceof ServerLevel) {
            soak = NTServerConfig.CONFIG.beaconSoakEffects.get();
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
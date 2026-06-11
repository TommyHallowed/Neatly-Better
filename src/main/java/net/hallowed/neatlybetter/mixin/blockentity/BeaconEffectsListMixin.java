package net.hallowed.neatlybetter.mixin.blockentity;

import com.google.common.collect.ImmutableList;

import net.hallowed.neatlybetter.config.NTServerConfig;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mixin(value = BeaconBlockEntity.class, priority = 900)
public class BeaconEffectsListMixin {

    @Final
    @Mutable
    @Shadow
    public static List<List<Holder<MobEffect>>> BEACON_EFFECTS;

    @Final
    @Mutable
    @Shadow
    private static Set<Holder<MobEffect>> VALID_EFFECTS;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void neatlybetter$addSaturationEffect(CallbackInfo ci) {
        if (!NTServerConfig.CONFIG.beaconSaturationEffect.get()) return;

        List<List<Holder<MobEffect>>> outer = new ArrayList<>(BEACON_EFFECTS);
        List<Holder<MobEffect>> tier3 = new ArrayList<>(outer.get(3));
        tier3.add(MobEffects.SATURATION);
        outer.set(3, ImmutableList.copyOf(tier3));
        BEACON_EFFECTS = ImmutableList.copyOf(outer);

        Set<Holder<MobEffect>> mutableValid = new HashSet<>(VALID_EFFECTS);
        mutableValid.add(MobEffects.SATURATION);
        VALID_EFFECTS = Collections.unmodifiableSet(mutableValid);
    }
}

package net.hallowed.neatlybetter.client.mixin.other;

import net.hallowed.neatlybetter.handler.LegacyCombatHandler;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;

import org.jetbrains.annotations.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {

    @Inject(
            method = "playSeededSound(Lnet/minecraft/world/entity/Entity;DDDLnet/minecraft/core/Holder;Lnet/minecraft/sounds/SoundSource;FFJ)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void cancelCombatSoundsClient(@Nullable Entity except,
                                          double x, double y, double z,
                                          Holder<SoundEvent> sound,
                                          SoundSource source,
                                          float volume, float pitch, long seed,
                                          CallbackInfo ci) {
        if (LegacyCombatHandler.shouldCancelAttackSound(sound.value())) {
            ci.cancel();
        }
    }
}
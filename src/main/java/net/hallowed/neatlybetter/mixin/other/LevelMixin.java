package net.hallowed.neatlybetter.mixin.other;

import net.hallowed.neatlybetter.handler.LegacyCombatHandler;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Level.class)
public class LevelMixin {

    @Inject(
            method = "playSound(Lnet/minecraft/world/entity/Entity;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void cancelCombatSounds(Entity except, double x, double y, double z,
                                    SoundEvent sound, SoundSource source,
                                    float volume, float pitch, CallbackInfo ci) {
        if (LegacyCombatHandler.shouldCancelAttackSound(sound)) {
            ci.cancel();
        }
    }
}

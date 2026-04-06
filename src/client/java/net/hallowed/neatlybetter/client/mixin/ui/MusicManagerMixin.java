package net.hallowed.neatlybetter.client.mixin.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.sounds.Music;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MusicManager.class)
public class MusicManagerMixin {

    @Inject(method = "canReplace", at = @At("HEAD"), cancellable = true)
    private static void NeatlyBetter$SeamlessMusicTransitions(Music music, SoundInstance currentMusic, CallbackInfoReturnable<Boolean> cir) {
        if (Minecraft.getInstance().level == null) {
            cir.setReturnValue(false);
        }
    }
}
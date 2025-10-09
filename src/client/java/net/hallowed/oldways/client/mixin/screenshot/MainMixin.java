package net.hallowed.oldways.client.mixin.screenshot;

import net.hallowed.oldways.client.util.SettingsPrefs;
import net.minecraft.client.main.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Locale;

@Mixin(Main.class)
public class MainMixin {

    @Unique
    private static final SettingsPrefs OW$prefs = SettingsPrefs.get();

    @Inject(method = "main", at = @At("HEAD"), remap = false)
    private static void oldways$enableAwt(CallbackInfo ci) {
        if (!OW$prefs.copyScreenshots) return;
        if (!System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("mac")) {
            System.setProperty("java.awt.headless", "false");
        }
    }
}

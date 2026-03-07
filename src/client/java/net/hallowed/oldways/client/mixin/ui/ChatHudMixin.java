package net.hallowed.oldways.client.mixin.ui;

import net.hallowed.oldways.client.util.CopyScreenshotHelper;
import net.hallowed.oldways.client.util.SettingsPrefs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


@Mixin(ChatComponent.class)
public abstract class ChatHudMixin {

    @Shadow @Final Minecraft minecraft;

    @Unique private static final Executor OLDWAYS_SHOT_EXEC = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "OldWays-ScreenshotCopy");
        t.setDaemon(true);
        return t;
    });
    @Unique
    private static final SettingsPrefs OW$prefs = SettingsPrefs.get();

    @Unique private static volatile String OLDWAYS_LAST_FILE = null;

    @Inject(method = "addMessage(Lnet/minecraft/network/chat/Component;)V", at = @At("TAIL"))
    private void oldways$copyScreenshotIfVanillaSaved(Component message, CallbackInfo ci) {
        if (!OW$prefs.copyScreenshots) return;
        if (!(message.getContents() instanceof TranslatableContents tc)) return;
        if (!"screenshot.success".equals(tc.getKey())) return;

        final Path shotsDir = this.minecraft.gameDirectory.toPath().resolve("screenshots");

        OLDWAYS_SHOT_EXEC.execute(() -> {
            File dir = shotsDir.toFile();
            File[] pngs = dir.listFiles((d, name) -> {
                int n = name.length();
                return n >= 4 && (name.regionMatches(true, n - 4, ".png", 0, 4));
            });
            if (pngs == null || pngs.length == 0) return;

            File newest = pngs[0];
            for (int i = 1; i < pngs.length; i++) {
                if (pngs[i].lastModified() > newest.lastModified()) newest = pngs[i];
            }

            String path = newest.getAbsolutePath();
            if (path.equals(OLDWAYS_LAST_FILE)) return;
            OLDWAYS_LAST_FILE = path;

            CopyScreenshotHelper.copyFromFile(newest);
        });
    }
}

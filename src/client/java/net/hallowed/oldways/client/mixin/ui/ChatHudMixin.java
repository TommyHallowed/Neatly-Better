package net.hallowed.oldways.client.mixin.ui;

import net.hallowed.oldways.client.clipboard.CopyScreenshotHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
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


@Mixin(ChatHud.class)
public abstract class ChatHudMixin {

    @Shadow @Final private MinecraftClient client;

    // tiny background worker so PNG decode never hitches the render thread
    @Unique private static final Executor OLDWAYS_SHOT_EXEC = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "OldWays-ScreenshotCopy");
        t.setDaemon(true);
        return t;
    });

    // avoid re-decoding the same file if the message fires twice
    @Unique private static volatile String OLDWAYS_LAST_FILE = null;

    @Inject(method = "addMessage(Lnet/minecraft/text/Text;)V", at = @At("TAIL"))
    private void oldways$copyScreenshotIfVanillaSaved(Text message, CallbackInfo ci) {

        if (!(message.getContent() instanceof TranslatableTextContent tc)) return;
        if (!"screenshot.success".equals(tc.getKey())) return;

        // vanilla message is already posted; just copy the newest PNG in background
        final Path shotsDir = this.client.runDirectory.toPath().resolve("screenshots");

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
            if (path.equals(OLDWAYS_LAST_FILE)) return; // already handled
            OLDWAYS_LAST_FILE = path;

            CopyScreenshotHelper.copyFromFile(newest);
        });
    }
}

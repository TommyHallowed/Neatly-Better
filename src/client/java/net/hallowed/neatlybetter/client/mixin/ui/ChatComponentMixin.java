package net.hallowed.neatlybetter.client.mixin.ui;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.hallowed.neatlybetter.client.util.CopyScreenshotHelper;
import net.hallowed.neatlybetter.client.config.NTClientConfig;

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
public abstract class ChatComponentMixin {

    @Shadow @Final
    private Minecraft minecraft;

    @Unique private static final Executor NEATLYBETTER_SHOT_EXEC = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "NeatlyBetter-ScreenshotCopy");
        t.setDaemon(true);
        return t;
    });

    @Unique private static volatile String NEATLYBETTER_LAST_FILE = null;

    @Inject(method = "addClientSystemMessage(Lnet/minecraft/network/chat/Component;)V", at = @At("TAIL"))
    private void neatlybetter$copyScreenshotIfVanillaSaved(Component message, CallbackInfo ci) {
        if (!NTClientConfig.CONFIG.copyScreenshots.get()) return;
        if (!(message.getContents() instanceof TranslatableContents tc)) return;
        if (!"screenshot.success".equals(tc.getKey())) return;

        final Path shotsDir = this.minecraft.gameDirectory.toPath().resolve("screenshots");

        NEATLYBETTER_SHOT_EXEC.execute(() -> {
            File dir = shotsDir.toFile();
            File[] pngs = dir.listFiles((_, name) -> {
                int n = name.length();
                return n >= 4 && (name.regionMatches(true, n - 4, ".png", 0, 4));
            });
            if (pngs == null || pngs.length == 0) return;

            File newest = pngs[0];
            for (int i = 1; i < pngs.length; i++) {
                if (pngs[i].lastModified() > newest.lastModified()) newest = pngs[i];
            }

            String path = newest.getAbsolutePath();
            if (path.equals(NEATLYBETTER_LAST_FILE)) return;
            NEATLYBETTER_LAST_FILE = path;

            CopyScreenshotHelper.copyFromFile(newest);
        });
    }

    @ModifyExpressionValue(
            method = {"addMessageToDisplayQueue", "addMessageToQueue", "addRecentChat"},
            at = @At(value = "CONSTANT", args = "intValue=100")
    )
    public int neatlybetter$increaseMaxHistory(int original) {
        return original + 16284;
    }
}

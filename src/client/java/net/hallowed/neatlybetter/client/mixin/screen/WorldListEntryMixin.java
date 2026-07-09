package net.hallowed.neatlybetter.client.mixin.screen;

import com.mojang.logging.LogUtils;

import net.hallowed.neatlybetter.data.PlaytimeData;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;

import org.slf4j.Logger;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

@Mixin(WorldSelectionList.WorldListEntry.class)
public class WorldListEntryMixin {

    @Unique
    private static final Logger NEATLYBETTER_LOGGER = LogUtils.getLogger();

    @Shadow @Final private LevelSummary summary;
    @Shadow @Final private Minecraft minecraft;
    @Shadow @Final private WorldSelectionList list;

    // ── Playtime display ────────────────────────────────────────────────────

    @Inject(method = "extractContent", at = @At("HEAD"))
    private void neatlybetter$renderPlaytime(
            GuiGraphicsExtractor graphics, int mouseX, int mouseY,
            boolean hovered, float a, CallbackInfo ci
    ) {
        long ticks = PlaytimeData.get(summary);
        if (ticks <= 0L) return;

        String label = neatlybetter$formatPlaytime(ticks);
        Component text = Component.literal(label).withStyle(ChatFormatting.GRAY);

        ObjectSelectionList.Entry<?> entry = (ObjectSelectionList.Entry<?>) (Object) this;
        int contentX = entry.getContentX();
        int contentY = entry.getContentY();

        int textWidth = this.minecraft.font.width(text);
        int x = contentX + this.list.getRowWidth() - textWidth - 5;
        int y = contentY + 1;

        graphics.text(this.minecraft.font, text, x, y, 0xFFAAAAAA);
    }

    @Unique
    private static String neatlybetter$formatPlaytime(long ticks) {
        long totalMinutes = ticks / 20L / 60L;

        if (totalMinutes < 60L) {
            return Math.max(1L, totalMinutes) + "m";
        } else {
            double hours = totalMinutes / 60.0;
            String formatted = String.format("%.1f", hours);
            if (formatted.endsWith(".0")) {
                formatted = formatted.substring(0, formatted.length() - 2);
            }
            return formatted + "h";
        }
    }

    // ── Trash: change confirmation dialog body text ─────────────────────────

    @ModifyArg(
            method = "deleteWorld",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/ConfirmScreen;<init>(Lit/unimi/dsi/fastutil/booleans/BooleanConsumer;Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/Component;)V"),
            index = 2
    )
    private Component neatlybetter$changeDeleteMessage(Component original) {
        if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.MOVE_TO_TRASH)) {
            return original;
        }
        return Component.literal("The world '" + this.summary.getLevelName() + "' will be moved to trash!");
    }

    // ── Move world to trash instead of permanently deleting ─────────────────

    @Inject(method = "doDeleteWorld", at = @At("HEAD"), cancellable = true)
    private void neatlybetter$moveWorldToTrash(CallbackInfo ci) {
        if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.MOVE_TO_TRASH)) {
            return;
        }

        LevelStorageSource levelSource = this.minecraft.getLevelSource();
        String levelId = this.summary.getLevelId();

        try {
            LevelStorageSource.LevelStorageAccess access = levelSource.createAccess(levelId);
            File worldDir = access.getLevelDirectory().path().toFile();
            access.close();

            if (Desktop.getDesktop().moveToTrash(worldDir)) {
                NEATLYBETTER_LOGGER.debug("Moved world '{}' to trash", levelId);
            } else {
                NEATLYBETTER_LOGGER.warn("moveToTrash returned false for world '{}', falling back to permanent deletion", levelId);
                try (LevelStorageSource.LevelStorageAccess fallback = levelSource.createAccess(levelId)) {
                    fallback.deleteLevel();
                }
            }
        } catch (IOException e) {
            SystemToast.onWorldDeleteFailure(this.minecraft, levelId);
            NEATLYBETTER_LOGGER.error("Failed to move world '{}' to trash", levelId, e);
        }

        ci.cancel();
    }
}
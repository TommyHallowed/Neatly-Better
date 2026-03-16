package net.hallowed.neatlybetter.client.render;

import net.hallowed.neatlybetter.util.NTEffectInstance;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;

public class EffectBarRenderer {

    public static void renderHUD(GuiGraphics context, MobEffectInstance effect, int x, int y) {
        int maxDuration = ((NTEffectInstance) effect).neatlybetter$getMaxDuration();

        if (!shouldRenderBar(effect, maxDuration)) return;

        float progress = getSmoothProgress(effect, maxDuration);

        int barWidth = 18;
        int barHeight = 1;
        int barX = x + 3;
        int barY = y + 21;

        drawBar(context, effect, barX, barY, barWidth, barHeight, progress);
    }

    public static void renderInventory(GuiGraphics context, MobEffectInstance effect, int x, int y, int maxWidth, boolean isWide) {
        int maxDuration = ((NTEffectInstance) effect).neatlybetter$getMaxDuration();

        if (!shouldRenderBar(effect, maxDuration)) return;

        float progress = getSmoothProgress(effect, maxDuration);

        int barWidth;
        int barX = x + 4;
        int barY = y + 28;
        int barHeight = 1;

        if (isWide) {
            Minecraft mc = Minecraft.getInstance();
            Font font = mc.font;
            Component name = getEffectName(effect);
            Component duration = MobEffectUtil.formatDuration(effect, 1.0F, mc.level.tickRateManager().tickrate());

            int nameWidth = 32 + font.width(name) + 7;
            int durationWidth = 32 + font.width(duration) + 7;
            int backgroundWidth = Math.min(maxWidth, Math.max(nameWidth, durationWidth));

            barWidth = backgroundWidth - 8;
        } else {
            barWidth = 24;
        }

        drawBar(context, effect, barX, barY, barWidth, barHeight, progress);
    }

    private static boolean shouldRenderBar(MobEffectInstance effect, int maxDuration) {
        if (maxDuration <= 0 || effect.isInfiniteDuration()) return false;

        if (effect.getDuration() > 36000) return false;

        int age = maxDuration - effect.getDuration();
        return !effect.isAmbient() || age >= 100;
    }

    private static float getSmoothProgress(MobEffectInstance effect, int maxDuration) {
        float tickDelta = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
        float exactDuration = Math.max(0.0F, effect.getDuration() - tickDelta);
        float progress = exactDuration / (float) maxDuration;
        return Math.min(1.0F, Math.max(0.0F, progress));
    }

    private static void drawBar(GuiGraphics context, MobEffectInstance effect, int x, int y, int width, int height, float progress) {
        context.fill(x, y, x + width, y + height, 0x66000000);

        int filledWidth = Math.round(width * progress);
        if (filledWidth > 0) {
            int color = effect.getEffect().value().getColor() | 0xCC000000;
            context.fill(x, y, x + filledWidth, y + height, color);
        }
    }

    private static Component getEffectName(MobEffectInstance effect) {
        MutableComponent name = effect.getEffect().value().getDisplayName().copy();
        if (effect.getAmplifier() >= 1 && effect.getAmplifier() <= 9) {
            name.append(CommonComponents.SPACE).append(Component.translatable("enchantment.level." + (effect.getAmplifier() + 1)));
        }
        return name;
    }
}
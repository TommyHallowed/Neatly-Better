package net.hallowed.neatlybetter.client.content.feature;

import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.hallowed.neatlybetter.api.NTCompat;
import net.hallowed.neatlybetter.client.config.NTClientConfig;

import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public final class Zoom {

    public static final KeyMapping.Category CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath("neatly-better", "main"));
    public static final KeyMapping KEY =
            new KeyMapping("key.neatly-better.zoom", InputConstants.KEY_C, CATEGORY);

    private static final double MIN_FACTOR = 1.1;

    private static float progress, progressOld;
    private static double scrollMult = 1.0, smoothMult = 1.0, smoothMultOld = 1.0;

    private Zoom() {}

    public static void register() {
        if (ZOOM_MOD_PRESENT) return;
        KeyMappingHelper.registerKeyMapping(KEY);
    }

    private static final boolean ZOOM_MOD_PRESENT =
            NTCompat.ZOOMIFY || NTCompat.OK_ZOOMER || NTCompat.JUSTZOOM || NTCompat.WI_ZOOM;

    public static boolean isHeld() {
        return !ZOOM_MOD_PRESENT && NTClientConfig.CONFIG.zoomEnabled.isTrue() && KEY.isDown();
    }

    private static double maxFactor() {
        return Math.max(MIN_FACTOR, NTClientConfig.CONFIG.zoomMaxFactor.get());
    }

    public static void tick() {
        boolean held = isHeld();
        float speed = NTClientConfig.CONFIG.zoomTransitionSpeed.get().floatValue();

        progressOld = progress;
        float target = held ? 1.0F : 0.0F;
        progress += (target - progress) * speed;
        if (Math.abs(target - progress) < 0.001F) progress = target;

        smoothMultOld = smoothMult;
        smoothMult += (scrollMult - smoothMult) * speed;
        if (Math.abs(scrollMult - smoothMult) < 0.001) smoothMult = scrollMult;

        if (!held && progress == 0.0F) {
            scrollMult = smoothMult = smoothMultOld = 1.0;
        }
    }

    public static float progress(float partialTicks) {
        return Mth.lerp(partialTicks, progressOld, progress);
    }

    private static double factor(float partialTicks) {
        double mult = Mth.lerp(partialTicks, smoothMultOld, smoothMult);
        return Mth.clamp(NTClientConfig.CONFIG.zoomFactor.get() * mult, MIN_FACTOR, maxFactor());
    }

    public static float fovMultiplier(float partialTicks) {
        float p = progress(partialTicks);
        if (p <= 0.0F) return 1.0F;
        return Mth.lerp(p, 1.0F, (float) (1.0 / factor(partialTicks)));
    }

    public static double sensitivityScale() {
        return NTClientConfig.CONFIG.zoomReduceSensitivity.get() ? fovMultiplier(1.0F) : 1.0;
    }

    public static boolean handleScroll(double yOffset) {
        if (!isHeld() || !NTClientConfig.CONFIG.zoomScroll.get()) return false;

        double base = NTClientConfig.CONFIG.zoomFactor.get();
        double step = 1.0 + NTClientConfig.CONFIG.zoomScrollStep.get();
        scrollMult = Mth.clamp(scrollMult * Math.pow(step, yOffset), MIN_FACTOR / base, maxFactor() / base);
        return true;
    }
}
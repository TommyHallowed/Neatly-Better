package net.hallowed.neatlybetter.client.tooltip;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public final class EffectTooltipRenderer implements ClientTooltipComponent {

    private static final int ICON_SIZE = 18;
    private static final int ICON_TEXT_GAP = 4;
    private static final int ROW_SPACING = 2;
    private static final int ROW_HEIGHT = ICON_SIZE + ROW_SPACING;

    private final RenderedEffect[] effects;

    public EffectTooltipRenderer(EffectTooltipData data) {
        Minecraft mc = Minecraft.getInstance();
        float tickRate = mc.level != null
                ? mc.level.tickRateManager().tickrate()
                : 20.0F;

        this.effects = new RenderedEffect[data.effects().size()];
        for (int i = 0; i < data.effects().size(); i++) {
            this.effects[i] = RenderedEffect.create(
                    data.effects().get(i),
                    data.durationMultiplier(),
                    tickRate,
                    data.getChance(i)
            );
        }
    }

    @Override
    public int getHeight(@NotNull Font font) {
        return ROW_HEIGHT * effects.length + 3;
    }

    @Override
    public int getWidth(@NotNull Font font) {
        return ICON_SIZE + ICON_TEXT_GAP + 2 + Math.max(
                Arrays.stream(effects)
                        .map(this::buildNameText)
                        .mapToInt(c -> font.width(c.getVisualOrderText()))
                        .max().orElse(0),
                Arrays.stream(effects)
                        .map(this::buildDurationText)
                        .mapToInt(c -> font.width(c.getVisualOrderText()))
                        .max().orElse(0)
        );
    }

    @Override
    public void extractImage(@NotNull Font font, int x, int y,
                            int width, int height,
                            @NotNull GuiGraphicsExtractor gfx) {
        int rowOffset = -1;

        for (RenderedEffect effect : effects) {
            int iconX = x + 2;
            int iconY = y + 2 + rowOffset;
            int textX = x + ICON_SIZE + 2 + ICON_TEXT_GAP;

            Identifier spriteId = getEffectSprite(effect.effect());
            gfx.blitSprite(
                    RenderPipelines.GUI_TEXTURED,
                    spriteId,
                    iconX, iconY,
                    ICON_SIZE, ICON_SIZE,
                    0xFFFFFFFF
            );

            gfx.text(font, buildNameText(effect),
                    textX, iconY, 0xFFFFFFFF, false);

            gfx.text(font, buildDurationText(effect),
                    textX, iconY + font.lineHeight, 0xFFFFFFFF, false);

            rowOffset += ROW_HEIGHT;
        }
    }

    private Component buildNameText(RenderedEffect effect) {
        MutableComponent name = Component.translatable(effect.descriptionId());

        if (effect.amplifier() > 0) {
            name.append(CommonComponents.SPACE)
                    .append(Component.translatable("potion.potency." + effect.amplifier()));
        }

        name.withColor(effect.effect().value().getColor());

        if (effect.chance() < 1.0F) {
            int pct = Math.round(effect.chance() * 100);
            name.append(Component.literal(" (" + pct + "%)")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }

        return name;
    }

    private Component buildDurationText(RenderedEffect effect) {
        return effect.durationText().copy()
                .withStyle(ChatFormatting.GRAY);
    }

    private static Identifier getEffectSprite(Holder<MobEffect> effect) {
        return Gui.getMobEffectSprite(effect);
    }

    private record RenderedEffect(
            Holder<MobEffect> effect,
            String descriptionId,
            Component durationText,
            int amplifier,
            float chance
    ) {
        static RenderedEffect create(MobEffectInstance instance,
                                     float durationMultiplier,
                                     float tickRate,
                                     float chance) {
            Component duration = instance.getDuration() <= 1
                    ? Component.literal("Instant")
                    : MobEffectUtil.formatDuration(instance, durationMultiplier, tickRate);

            return new RenderedEffect(
                    instance.getEffect(),
                    instance.getEffect().value().getDescriptionId(),
                    duration,
                    instance.getAmplifier(),
                    chance
            );
        }
    }
}
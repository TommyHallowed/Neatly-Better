package net.hallowed.oldways.client.mixin.ui;


import net.hallowed.oldways.init.ModDataComponents;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.equipment.trim.ArmorTrim;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ArmorTrim.class)
public class ArmorTrimMixin {

    @Unique
    private static final Component EMISSIVE_GLOW_LINE =
            Component.literal(" Glowing").withStyle(ChatFormatting.AQUA);

    @Unique
    private static final Component PULSING_ECHO_LINE =
            Component.literal(" Pulsing").withStyle(ChatFormatting.DARK_AQUA);

    @Inject(
            method = "addToTooltip",
            at = @At("TAIL")
    )
    private void oldways$appendSpecialTrimTooltip(
            Item.TooltipContext context,
            Consumer<Component> consumer,
            TooltipFlag tooltipFlag,
            DataComponentGetter getter,
            CallbackInfo ci
    ) {
        boolean emissive = getter.getOrDefault(ModDataComponents.EMISSIVE_TRIM, false);
        boolean pulsing = getter.getOrDefault(ModDataComponents.PULSING_TRIM, false);

        if (pulsing) {
            consumer.accept(PULSING_ECHO_LINE);
        } else if (emissive) {
            consumer.accept(EMISSIVE_GLOW_LINE);
        }
    }
}
package net.hallowed.neatlybetter.client.mixin.ui;

import net.hallowed.neatlybetter.init.ModDataComponents;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
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
    private void neatlybetter$appendSpecialTrimTooltip(
            Item.TooltipContext context,
            Consumer<Component> consumer,
            TooltipFlag flag,
            DataComponentGetter components,
            CallbackInfo ci
    ) {
        boolean pulsing  = components.getOrDefault(ModDataComponents.PULSING_TRIM, false);
        boolean emissive = components.getOrDefault(ModDataComponents.EMISSIVE_TRIM, false);

        if (pulsing) {
            consumer.accept(PULSING_ECHO_LINE);
        } else if (emissive && !neatlybetter$hasElytraTrimsGlow(components)) {
            consumer.accept(EMISSIVE_GLOW_LINE);
        }
    }

    @Unique
    private static boolean neatlybetter$hasElytraTrimsGlow(DataComponentGetter getter) {
        CustomData customData = getter.get(DataComponents.CUSTOM_DATA);
        return customData != null
                && customData.copyTag().contains("elytratrims:glow");
    }
}
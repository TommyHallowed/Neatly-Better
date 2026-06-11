package net.hallowed.neatlybetter.client.mixin.screen;

import net.hallowed.neatlybetter.api.PremiumBaseAccessor;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.BeaconScreen;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.inventory.BeaconMenu;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.gui.screens.inventory.BeaconScreen$BeaconPowerButton", priority = 900)
public class BeaconScreenMixin {

    @Shadow @Final
    BeaconScreen this$0;

    @Shadow
    private Holder<MobEffect> effect;

    @Unique
    private static final Component PREMIUM_WARNING =
            Component.literal("Needs Diamond / Netherite Base")
                    .withStyle(net.minecraft.ChatFormatting.DARK_RED);

    @Inject(method = "updateStatus", at = @At("TAIL"))
    private void neatlybetter$gateSaturationOnPremiumBase(int levels, CallbackInfo ci) {
        if (!this.effect.is(MobEffects.SATURATION.unwrapKey().orElseThrow())) return;

        BeaconMenu menu = this.this$0.getMenu();
        int premiumBase = ((PremiumBaseAccessor) menu).neatlybetter$getPremiumBase();

        AbstractWidget widget = (AbstractWidget)(Object) this;

        if (premiumBase != 1) {
            widget.active = false;

            Component effectName = Component.translatable(
                    this.effect.value().getDescriptionId());
            Component combined = effectName.copy()
                    .append(Component.literal("\n"))
                    .append(PREMIUM_WARNING);
            widget.setTooltip(Tooltip.create(combined));
        } else {
            Component effectName = Component.translatable(
                    this.effect.value().getDescriptionId());
            widget.setTooltip(Tooltip.create(effectName));
        }
    }
}
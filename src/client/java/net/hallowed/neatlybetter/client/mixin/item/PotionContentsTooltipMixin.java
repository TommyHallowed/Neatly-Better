package net.hallowed.neatlybetter.client.mixin.item;

import net.hallowed.neatlybetter.client.config.NTClientConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.alchemy.PotionContents;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.function.Consumer;

@Mixin(PotionContents.class)
public abstract class PotionContentsTooltipMixin {

    @ModifyVariable(
            method = "addPotionTooltip",
            at = @At("HEAD"),
            argsOnly = true,
            name = "lines")
    private static Consumer<Component> neatlybetter$filterEffectLines(Consumer<Component> lines) {
        if (!NTClientConfig.CONFIG.potionEffectIcons.get()) return lines;

        return new Consumer<>() {
            boolean passThrough = false;

            @Override
            public void accept(Component component) {
                if (!passThrough && component.getString().isEmpty()) {
                    passThrough = true;
                }
                if (passThrough) {
                    lines.accept(component);
                }
            }
        };
    }
}
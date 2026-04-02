package net.hallowed;

import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import fuzs.forgeconfigapiport.fabric.api.v5.client.ConfigScreenFactoryRegistry;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;

import net.hallowed.neatlybetter.client.config.NTClientConfig;
import net.hallowed.neatlybetter.client.feature.AutoRefill;
import net.hallowed.neatlybetter.client.feature.ClientMapPreviewTooltip;
import net.hallowed.neatlybetter.client.init.ModTooltips;
import net.hallowed.neatlybetter.client.tooltip.EffectTooltipData;
import net.hallowed.neatlybetter.client.tooltip.EffectTooltipRenderer;
import net.hallowed.neatlybetter.client.util.BackpackCheckClient;
import net.hallowed.neatlybetter.client.util.EnderCheckClient;
import net.hallowed.neatlybetter.tooltip.MapPreviewTooltip;

import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;

import static net.hallowed.NeatlyBetter.MOD_ID;


public class NeatlyBetterClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ConfigRegistry.INSTANCE.register(MOD_ID, ModConfig.Type.CLIENT, NTClientConfig.CONFIG_SPEC);
        ConfigScreenFactoryRegistry.INSTANCE.register(MOD_ID, ConfigurationScreen::new);
        EnderCheckClient.register();
        BackpackCheckClient.register();
        AutoRefill.register();

        TooltipComponentCallback.EVENT.register(data -> {
            if (data instanceof MapPreviewTooltip mapData) {
                return new ClientMapPreviewTooltip(mapData);
            }
            if (data instanceof EffectTooltipData effectData) {
                return new EffectTooltipRenderer(effectData);
            }
            return null;
        });

        ModTooltips.init();
    }
}

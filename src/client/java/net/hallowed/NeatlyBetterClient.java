package net.hallowed;

import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import fuzs.forgeconfigapiport.fabric.api.v5.client.ConfigScreenFactoryRegistry;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.ClientTooltipComponentCallback;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;

import net.hallowed.neatlybetter.client.config.NTClientConfig;
import net.hallowed.neatlybetter.client.feature.AutoRefill;
import net.hallowed.neatlybetter.client.feature.ClientMapPreviewTooltip;
import net.hallowed.neatlybetter.client.feature.VoidFog;
import net.hallowed.neatlybetter.client.feature.ui.SmallHudOverlay;
import net.hallowed.neatlybetter.client.init.ModTooltips;
import net.hallowed.neatlybetter.client.render.InvisibleSignModelPlugin;
import net.hallowed.neatlybetter.client.tooltip.*;
import net.hallowed.neatlybetter.client.util.EnderCheckClient;
import net.hallowed.neatlybetter.tooltip.MapPreviewTooltip;

import net.minecraft.resources.Identifier;

import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;

import static net.hallowed.NeatlyBetter.MOD_ID;


public class NeatlyBetterClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ConfigRegistry.INSTANCE.register(MOD_ID, ModConfig.Type.CLIENT, NTClientConfig.CONFIG_SPEC);
        ConfigScreenFactoryRegistry.INSTANCE.register(MOD_ID, ConfigurationScreen::new);
        EnderCheckClient.register();
        AutoRefill.register();
        VoidFog.register();

        ModelLoadingPlugin.register(new InvisibleSignModelPlugin());

        HudElementRegistry.attachElementAfter(
                VanillaHudElements.MOB_EFFECTS,
                Identifier.fromNamespaceAndPath("neatly-better", "small_hud"),
                SmallHudOverlay.getInstance()
        );

        ClientTooltipComponentCallback.EVENT.register(data -> switch (data) {
            case MapPreviewTooltip mapData -> new ClientMapPreviewTooltip(mapData);
            case EffectTooltipData effectData -> new EffectTooltipRenderer(effectData);
            case ShulkerBoxTooltipData shulkerBoxTooltip -> new ShulkerBoxTooltipRenderer(shulkerBoxTooltip.items(), shulkerBoxTooltip.color());
            default -> null;
        });

        ModTooltips.init();
    }
}

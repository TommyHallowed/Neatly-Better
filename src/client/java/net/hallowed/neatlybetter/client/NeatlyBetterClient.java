package net.hallowed.neatlybetter.client;

import net.hallowed.neatlybetter.client.feature.autorefill.AutoRefill;
import net.hallowed.neatlybetter.client.feature.fog.FireResistanceLavaFog;
import net.hallowed.neatlybetter.client.feature.fog.VoidFog;
import net.hallowed.neatlybetter.client.feature.tooltips.ClientMapPreviewTooltip;
import net.hallowed.neatlybetter.client.feature.tooltips.EffectTooltipData;
import net.hallowed.neatlybetter.client.feature.tooltips.EffectTooltipRenderer;
import net.hallowed.neatlybetter.client.feature.tooltips.EnderChestTooltipData;
import net.hallowed.neatlybetter.client.feature.tooltips.EnderChestTooltipRenderer;
import net.hallowed.neatlybetter.client.feature.tooltips.QuiverTooltipRenderer;
import net.hallowed.neatlybetter.client.feature.tooltips.ShulkerBoxTooltipData;
import net.hallowed.neatlybetter.client.feature.tooltips.ShulkerBoxTooltipRenderer;
import net.hallowed.neatlybetter.client.feature.zoom.Zoom;
import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import fuzs.forgeconfigapiport.fabric.api.v5.client.ConfigScreenFactoryRegistry;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.ClientTooltipComponentCallback;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;

import net.hallowed.neatlybetter.client.config.NTClientConfig;
import net.hallowed.neatlybetter.client.feature.hud.SmallHudOverlay;
import net.hallowed.neatlybetter.client.registry.ModBlockColors;
import net.hallowed.neatlybetter.client.registry.ModTooltips;
import net.hallowed.neatlybetter.client.render.CarpetPatternModelPlugin;
import net.hallowed.neatlybetter.client.render.InvisibleSignModelPlugin;
import net.hallowed.neatlybetter.client.util.EnderCheckClient;
import net.hallowed.neatlybetter.feature.map.MapPreviewTooltip;

import net.hallowed.neatlybetter.content.item.QuiverTooltip;
import net.minecraft.resources.Identifier;

import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;

import static net.hallowed.neatlybetter.NeatlyBetter.MOD_ID;


public class NeatlyBetterClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ConfigRegistry.INSTANCE.register(MOD_ID, ModConfig.Type.CLIENT, NTClientConfig.CONFIG_SPEC);
        ConfigScreenFactoryRegistry.INSTANCE.register(MOD_ID, ConfigurationScreen::new);
        EnderCheckClient.register();
        AutoRefill.register();
        VoidFog.register();
        FireResistanceLavaFog.register();
        ModBlockColors.register();
        Zoom.register();

        ModelLoadingPlugin.register(new InvisibleSignModelPlugin());
        ModelLoadingPlugin.register(new CarpetPatternModelPlugin());
        CarpetPatternModelPlugin.registerClientEvents();

        HudElementRegistry.attachElementAfter(
                VanillaHudElements.MOB_EFFECTS,
                Identifier.fromNamespaceAndPath("neatly-better", "small_hud"),
                SmallHudOverlay.getInstance()
        );

        ClientTooltipComponentCallback.EVENT.register(data -> switch (data) {
            case MapPreviewTooltip mapData -> new ClientMapPreviewTooltip(mapData);
            case EffectTooltipData effectData -> new EffectTooltipRenderer(effectData);
            case ShulkerBoxTooltipData shulkerBoxTooltip -> new ShulkerBoxTooltipRenderer(shulkerBoxTooltip.items(), shulkerBoxTooltip.color());
            case EnderChestTooltipData enderChestTooltip -> new EnderChestTooltipRenderer(enderChestTooltip.items());
            case QuiverTooltip quiverTooltip -> new QuiverTooltipRenderer(quiverTooltip.contents());
            default -> null;
        });

        ModTooltips.init();
    }
}

package net.hallowed;

import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import fuzs.forgeconfigapiport.fabric.api.v5.client.ConfigScreenFactoryRegistry;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.ClientTooltipComponentCallback;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;

import net.hallowed.neatlybetter.client.config.NTClientConfig;
import net.hallowed.neatlybetter.client.content.feature.AutoRefill;
import net.hallowed.neatlybetter.client.content.feature.ClientMapPreviewTooltip;
import net.hallowed.neatlybetter.client.content.feature.FireResistanceLavaFog;
import net.hallowed.neatlybetter.client.content.feature.VoidFog;
import net.hallowed.neatlybetter.client.content.feature.ui.SmallHudOverlay;
import net.hallowed.neatlybetter.client.init.ModBlockColors;
import net.hallowed.neatlybetter.client.init.ModTooltips;
import net.hallowed.neatlybetter.client.render.CarpetPatternModelPlugin;
import net.hallowed.neatlybetter.client.render.InvisibleSignModelPlugin;
import net.hallowed.neatlybetter.client.tooltip.*;
import net.hallowed.neatlybetter.client.util.EnderCheckClient;
import net.hallowed.neatlybetter.tooltip.MapPreviewTooltip;

import net.hallowed.neatlybetter.tooltip.QuiverTooltip;
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
        FireResistanceLavaFog.register();
        ModBlockColors.register();

        ModelLoadingPlugin.register(new InvisibleSignModelPlugin());
        ModelLoadingPlugin.register(new CarpetPatternModelPlugin());

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

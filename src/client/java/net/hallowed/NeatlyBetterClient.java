package net.hallowed;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;

import net.hallowed.neatlybetter.client.feature.AutoRefill;
import net.hallowed.neatlybetter.client.feature.ClientMapPreviewTooltip;
import net.hallowed.neatlybetter.client.feature.locator.BackpackWaypointsClient;
import net.hallowed.neatlybetter.client.feature.locator.EnderWaypointsClient;
import net.hallowed.neatlybetter.client.init.ModTooltips;
import net.hallowed.neatlybetter.client.tooltip.EffectTooltipData;
import net.hallowed.neatlybetter.client.tooltip.EffectTooltipRenderer;
import net.hallowed.neatlybetter.client.util.BackpackCheckClient;
import net.hallowed.neatlybetter.client.util.EnderCheckClient;
import net.hallowed.neatlybetter.tooltip.MapPreviewTooltip;


public class NeatlyBetterClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EnderCheckClient.register();
        EnderWaypointsClient.register();
        BackpackCheckClient.register();
        BackpackWaypointsClient.register();
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

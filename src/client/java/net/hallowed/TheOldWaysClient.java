package net.hallowed;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.hallowed.oldways.client.feature.AutoRefill;
import net.hallowed.oldways.client.feature.ClientMapPreviewTooltip;
import net.hallowed.oldways.client.feature.locator.BackpackWaypointsClient;
import net.hallowed.oldways.client.feature.locator.EnderWaypointsClient;
import net.hallowed.oldways.client.init.ModTooltips;
import net.hallowed.oldways.client.util.BackpackCheckClient;
import net.hallowed.oldways.client.util.EnderCheckClient;
import net.hallowed.oldways.tooltip.MapPreviewTooltip;


public class TheOldWaysClient implements ClientModInitializer {
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
            return null;
        });

        ModTooltips.init();
    }
}

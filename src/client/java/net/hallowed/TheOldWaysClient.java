package net.hallowed;

import net.fabricmc.api.ClientModInitializer;
import net.hallowed.oldways.client.feature.locator.EnderWaypointsClient;
import net.hallowed.oldways.client.init.ModKeybinds;
import net.hallowed.oldways.client.init.ModTooltips;
import net.hallowed.oldways.client.util.EnderCheckClient;


public class TheOldWaysClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EnderCheckClient.register();
        EnderWaypointsClient.register();
        ModKeybinds.register();
        ModTooltips.init();
    }
}

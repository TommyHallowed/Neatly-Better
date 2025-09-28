package net.hallowed;

import net.fabricmc.api.ClientModInitializer;
import net.hallowed.oldways.client.config.ClientConfigManager;
import net.hallowed.oldways.client.keybinds.ToggleLocatorBarKeybind;
import net.hallowed.oldways.client.locator.EnderWaypointsClient;
import net.hallowed.oldways.client.ui.ModTooltips;
import net.hallowed.oldways.client.ui.SmallHudOverlay;
import net.hallowed.oldways.client.util.EnderCheckClient;


public class TheOldWaysClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientConfigManager.load();
        ModTooltips.init();
        SmallHudOverlay.register();
        EnderCheckClient.register();
        EnderWaypointsClient.register();
        ToggleLocatorBarKeybind.register();
    }
}

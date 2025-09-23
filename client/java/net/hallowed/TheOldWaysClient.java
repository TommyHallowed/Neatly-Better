package net.hallowed;

import net.fabricmc.api.ClientModInitializer;
import net.hallowed.oldways.client.ui.EnderCheckClient;
import net.hallowed.oldways.client.ui.SmallHudOverlay;
import net.hallowed.oldways.client.ui.TotemTooltip;
import net.hallowed.oldways.client.config.ClientConfigManager;


public class TheOldWaysClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
        ClientConfigManager.load();
        TotemTooltip.register();
        SmallHudOverlay.register();
        EnderCheckClient.register();

    }
}
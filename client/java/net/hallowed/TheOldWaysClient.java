package net.hallowed;

import net.fabricmc.api.ClientModInitializer;
import net.hallowed.client.EnderCheckClient;
import net.hallowed.client.SmallHudOverlay;
import net.hallowed.client.TotemTooltip;
import net.hallowed.client.config.ClientConfigManager;


public class TheOldWaysClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
        ClientConfigManager.load();
        TotemTooltip.register();
        SmallHudOverlay.register();
        EnderCheckClient.register();

    }
}
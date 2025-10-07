package net.hallowed;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.hallowed.oldways.api.OWRegistry;
import net.hallowed.oldways.client.config.ClientConfigManager;
import net.hallowed.oldways.client.keybinds.ToggleLocatorBarKeybind;
import net.hallowed.oldways.client.locator.EnderWaypointsClient;
import net.hallowed.oldways.client.ui.ModTooltips;
import net.hallowed.oldways.client.util.EnderCheckClient;
import net.hallowed.oldways.init.ModEntities;
import net.minecraft.client.render.entity.BoatEntityRenderer;
import net.minecraft.client.render.entity.model.BoatEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;


public class TheOldWaysClient implements ClientModInitializer {
    public static final EntityModelLayer WARPED_BOAT_LAYER =
            new EntityModelLayer(Identifier.of(OWRegistry.id("boat/warped").toString()), "main");

    public static final EntityModelLayer CRIMSON_BOAT_LAYER =
            new EntityModelLayer(Identifier.of(OWRegistry.id("boat/crimson").toString()), "main");

    @Override
    public void onInitializeClient() {
        ClientConfigManager.load();
        EntityModelLayerRegistry.registerModelLayer(WARPED_BOAT_LAYER, BoatEntityModel::getTexturedModelData);
        EntityRendererRegistry.register(ModEntities.WARPED_BOAT, ctx -> new BoatEntityRenderer(ctx, WARPED_BOAT_LAYER));
        EntityModelLayerRegistry.registerModelLayer(CRIMSON_BOAT_LAYER, BoatEntityModel::getTexturedModelData);
        EntityRendererRegistry.register(ModEntities.CRIMSON_BOAT, ctx -> new BoatEntityRenderer(ctx, CRIMSON_BOAT_LAYER));
        ModTooltips.init();
        EnderCheckClient.register();
        EnderWaypointsClient.register();
        ToggleLocatorBarKeybind.register();

    }
}

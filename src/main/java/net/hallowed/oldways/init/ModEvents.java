package net.hallowed.oldways.init;

import net.hallowed.oldways.content.feature.*;

public final class ModEvents {
    private ModEvents() {}

    public static void init() {
        NoSleeping.register();
        ShoulderDropOnUse.register();
        AnvilRestoration.register();
        AddedItemProperties.register();
        MinecartChainHandler.register();
        CropHarvester.register();
        DirtToGrassWithSeeds.init();
        MagmaSpawnsLava.init();
        BoneMealExpansion.init();
        ElytraFlightLimiter.init();
        CauldronCleansFilledMap.init();
        CauldronCleansTrims.init();
    }
}

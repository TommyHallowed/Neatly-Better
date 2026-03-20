package net.hallowed.neatlybetter.init;

import net.hallowed.neatlybetter.content.feature.*;

public final class ModEvents {
    private ModEvents() {}

    public static void init() {
        ShoulderDropOnUse.register();
        AnvilRestoration.register();
        AddedItemProperties.register();
        MinecartChainHandler.register();
        CropHarvester.register();
        ChestLockHandler.register();
        ItemCooldownHandler.register();
        ComposterRegistration.register();
        ArmorStandSwapHandler.register();
        DirtToGrassWithSeeds.init();
        MagmaSpawnsLava.init();
        BoneMealExpansion.init();
        ElytraFlightLimiter.init();
        CauldronCleaning.init();
    }
}

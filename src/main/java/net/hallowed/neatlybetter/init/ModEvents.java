package net.hallowed.neatlybetter.init;

import net.hallowed.neatlybetter.content.feature.*;

public final class ModEvents {
    private ModEvents() {}

    public static void init() {
        ShoulderDropOnUse.register();
        AnvilRepair.register();
        AddedItemProperties.register();
        MinecartChainHandler.register();
        CropHarvester.register();
        ChestKeyHandler.register();
        ItemCooldownHandler.register();
        ComposterRegistration.register();
        ArmorStandSwapHandler.register();
        JockeyAdditions.register();
        SleepModeHandler.register();
        WolfDamageByOwnerBypass.register();
        WolfSpawnsCollarOnDeath.register();
        DyeWoolFeature.register();
        DirtToGrassWithSeeds.init();
        MagmaSpawnsLava.init();
        BoneMealExpansion.init();
        //ElytraFlightLimiter.init();
        CauldronCleaning.init();
        MoreXpDrops.init();
        SaplingAutoPlanter.init();
        TorchIgnite.init();
    }
}

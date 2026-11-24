package net.hallowed.neatlybetter.init;

import net.hallowed.neatlybetter.content.feature.*;
import net.hallowed.neatlybetter.content.recipe.StrippingRecipe;
import net.hallowed.neatlybetter.content.spawner.EndPhantomSpawner;
import net.hallowed.neatlybetter.util.ServerTaskQueue;

public final class ModEvents {
    private ModEvents() {}

    public static void init() {
        ServerTaskQueue.register();
        ShoulderDropOnUse.register();
        AnvilRepair.register();
        AddedItemProperties.register();
        MinecartChainHandler.register();
        CropHarvester.register();
        ChestKeyHandler.register();
        ItemCooldownHandler.register();
        ArmorStandSwap.register();
        SleepModeHandler.register();
        TamableDamageByOwnerBypass.register();
        WolfSpawnsCollarOnDeath.register();
        DyeWoolFeature.register();
        InvisibleSign.register();
        LegacyCombatDamageValues.register();
        LadderPillarExtender.register();
        EndPhantomSpawner.register();
        CowBottleInteraction.register();
        StrippingRecipe.register();
        FloatingRailExtender.register();
        DirtToGrassWithSeeds.init();
        MagmaSpawnsLava.init();
        BoneMealExpansion.init();
        ElytraFlightLimiter.init();
        CauldronCleaning.init();
        MoreXpDrops.init();
        TorchIgnite.init();
        BlockGluing.init();
        ItemDeathProtection.init();
    }
}

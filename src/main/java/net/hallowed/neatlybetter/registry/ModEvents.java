package net.hallowed.neatlybetter.registry;

import net.hallowed.neatlybetter.feature.building.AnvilRepair;
import net.hallowed.neatlybetter.feature.building.ArmorStandSwap;
import net.hallowed.neatlybetter.feature.building.BlockGluing;
import net.hallowed.neatlybetter.feature.building.DyeWoolFeature;
import net.hallowed.neatlybetter.feature.building.FloatingRailExtender;
import net.hallowed.neatlybetter.feature.building.InvisibleSign;
import net.hallowed.neatlybetter.feature.building.LadderPillarExtender;
import net.hallowed.neatlybetter.feature.building.MagmaSpawnsLava;
import net.hallowed.neatlybetter.feature.cauldron.CauldronCleaning;
import net.hallowed.neatlybetter.feature.combat.LegacyCombatDamageValues;
import net.hallowed.neatlybetter.feature.combat.TamableDamageByOwnerBypass;
import net.hallowed.neatlybetter.feature.combat.TorchIgnite;
import net.hallowed.neatlybetter.feature.farming.BoneMealExpansion;
import net.hallowed.neatlybetter.feature.farming.CropHarvester;
import net.hallowed.neatlybetter.feature.farming.DirtToGrassWithSeeds;
import net.hallowed.neatlybetter.feature.farming.MoreXpDrops;
import net.hallowed.neatlybetter.feature.items.AddedItemProperties;
import net.hallowed.neatlybetter.feature.items.ElytraFlightLimiter;
import net.hallowed.neatlybetter.feature.items.ItemCooldownHandler;
import net.hallowed.neatlybetter.feature.items.ItemDeathProtection;
import net.hallowed.neatlybetter.feature.minecart.MinecartChainHandler;
import net.hallowed.neatlybetter.feature.mobs.CowBottleInteraction;
import net.hallowed.neatlybetter.feature.mobs.ShoulderDropOnUse;
import net.hallowed.neatlybetter.feature.mobs.WolfSpawnsCollarOnDeath;
import net.hallowed.neatlybetter.feature.sleep.SleepModeHandler;
import net.hallowed.neatlybetter.feature.storage.ChestKeyHandler;
import net.hallowed.neatlybetter.feature.building.StrippingRecipe;
import net.hallowed.neatlybetter.feature.mobs.EndPhantomSpawner;
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
        DirtToGrassWithSeeds.register();
        MagmaSpawnsLava.register();
        BoneMealExpansion.register();
        ElytraFlightLimiter.register();
        CauldronCleaning.register();
        MoreXpDrops.register();
        TorchIgnite.register();
        BlockGluing.register();
        ItemDeathProtection.register();
    }
}

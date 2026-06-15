package net.hallowed.neatlybetter.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public final class NTServerConfig {

    public static final NTServerConfig CONFIG;
    public static final ModConfigSpec CONFIG_SPEC;

    static {
        Pair<NTServerConfig, ModConfigSpec> pair =
                new ModConfigSpec.Builder().configure(NTServerConfig::new);
        CONFIG = pair.getLeft();
        CONFIG_SPEC = pair.getRight();
    }

    public final ModConfigSpec.BooleanValue creeperOrbiting;
    public final ModConfigSpec.BooleanValue creeperWalkIgnite;
    public final ModConfigSpec.BooleanValue creeperSneaky;
    public final ModConfigSpec.BooleanValue villagerOpensFenceGate;
    public final ModConfigSpec.BooleanValue villagerEmeraldBlockTempt;
    public final ModConfigSpec.BooleanValue villagerFarmerReplant;
    public final ModConfigSpec.BooleanValue villagerGlobalCuringPrices;
    public final ModConfigSpec.BooleanValue villagerInfiniteCuringDiscounts;
    public final ModConfigSpec.BooleanValue villagerBookLevelCap;
    public final ModConfigSpec.BooleanValue ironGolemNoSpeedrun;
    public final ModConfigSpec.BooleanValue ironGolemRepairUsingBlocks;
    public final ModConfigSpec.BooleanValue shulkerBulletDespawnsAfterOwner;
    public final ModConfigSpec.BooleanValue piglinRespectsTrims;
    public final ModConfigSpec.BooleanValue runWhileCharging;
    public final ModConfigSpec.BooleanValue mobParkour;
    public final ModConfigSpec.BooleanValue groundItemBreeding;
    public final ModConfigSpec.BooleanValue rangedMobShieldHoldoff;
    public final ModConfigSpec.BooleanValue sheepRunFromWolves;
    public final ModConfigSpec.BooleanValue oldSpiderAttacks;
    public final ModConfigSpec.BooleanValue vexDiesAfterSummoner;
    public final ModConfigSpec.BooleanValue wolfImprovements;
    public final ModConfigSpec.BooleanValue anvilNoRenameCost;
    public final ModConfigSpec.BooleanValue anvilNoItalicsRename;
    public final ModConfigSpec.BooleanValue anvilRenameColors;
    public final ModConfigSpec.BooleanValue anvilNoTooExpensive;
    public final ModConfigSpec.BooleanValue anvilEnchantFeather;
    public final ModConfigSpec.BooleanValue beaconSoakEffects;
    public final ModConfigSpec.BooleanValue beaconSaturationEffect;
    public final ModConfigSpec.IntValue maxBeaconRange;
    public final ModConfigSpec.BooleanValue dispenserCauldronInteraction;
    public final ModConfigSpec.IntValue spongeBlockAbsorbRadius;
    public final ModConfigSpec.BooleanValue rainIncreasesCropGrowth;
    public final ModConfigSpec.BooleanValue harderNetherrack;
    public final ModConfigSpec.BooleanValue magmaSpawnsLava;
    public final ModConfigSpec.IntValue totemCooldown;
    public final ModConfigSpec.IntValue shieldRaiseDelay;
    public final ModConfigSpec.BooleanValue doElytraFireworkBoosting;
    public final ModConfigSpec.BooleanValue featherNoDamage;
    public final ModConfigSpec.EnumValue<MendingScope> mendingInventory;
    public final ModConfigSpec.DoubleValue mendingEfficiency;
    public final ModConfigSpec.BooleanValue protectionOverhaul;
    public final ModConfigSpec.BooleanValue resistanceOverhaul;
    public final ModConfigSpec.EnumValue<SleepMode> allowSleeping;
    public final ModConfigSpec.BooleanValue hungerMechanics;
    public final ModConfigSpec.BooleanValue xpFromMiningNonOre;
    public final ModConfigSpec.BooleanValue xpFromPlacingBlocks;
    public final ModConfigSpec.BooleanValue stepUpDisabledWhileShifting;
    public final ModConfigSpec.BooleanValue lapisStaysInEnchanting;
    public final ModConfigSpec.BooleanValue explosionsDisableShield;
    public final ModConfigSpec.BooleanValue cropHarvester;
    public final ModConfigSpec.BooleanValue armorStandSwap;

    private NTServerConfig(ModConfigSpec.Builder builder) {

        // ══════════════════════════════════════════════
        //  Mob
        // ══════════════════════════════════════════════
        builder.push("mob");

        // ── Creeper ──
        builder.push("creeper");

        creeperOrbiting = builder
                .comment("§eCreeper will orbit around players while igniting.")
                .comment("")
                .comment("§4(Only on Hard Difficulty)")
                .translation("neatly-better.config.creeper_orbiting")
                .define("creeper_orbiting", true);

        creeperWalkIgnite = builder
                .comment("§eCreeper will walk towards players while igniting.")
                .comment("")
                .comment("§6(Only on Normal Difficulty)")
                .translation("neatly-better.config.creeper_walk_ignite")
                .define("creeper_walk_ignite", true);

        creeperSneaky = builder
                .comment("§eCreepers will sneak behind a player if unseen and will ignite only if spotted.")
                .translation("neatly-better.config.creeper_sneaky")
                .define("creeper_sneaky", true);

        builder.pop(); // creeper

        // ── Villager ──
        builder.push("villager");

        villagerOpensFenceGate = builder
                .comment("§eVillagers can now open fence gates.")
                .translation("neatly-better.config.villager_opens_fence_gate")
                .define("villager_opens_fence_gate", true);

        villagerEmeraldBlockTempt = builder
                .comment("§eVillagers will follow a player that is holding emerald block.")
                .translation("neatly-better.config.villager_emerald_block_tempt")
                .define("villager_emerald_block_tempt", true);

        villagerFarmerReplant = builder
                .comment("§eVillagers now re-till farmland if it has turned to dirt.")
                .translation("neatly-better.config.villager_farmer_replant")
                .define("villager_farmer_replant", true);

        villagerGlobalCuringPrices = builder
                .comment("§eCuring a villager will now apply curing discount globally to all players.")
                .comment("§eThe best cure discount is applied if more than one player cures that specific villager.")
                .translation("neatly-better.config.villager_global_curing_prices")
                .define("villager_global_curing_prices", true);

        villagerInfiniteCuringDiscounts = builder
                .comment("§eCuring discounts stack infinitely instead of being capped.")
                .translation("neatly-better.config.villager_infinite_curing_discounts")
                .define("villager_infinite_curing_discounts", false);

        builder.pop(); // villager

        // ── Iron Golem ──
        builder.push("iron_golem");

        ironGolemNoSpeedrun = builder
                .comment("§eIron Golems can reach up to 4 blocks high.")
                .comment("§eMeaning the speedrun strat won't work anymore on them.")
                .translation("neatly-better.config.iron_golem_no_speedrun")
                .define("iron_golem_no_speedrun", true);

        ironGolemRepairUsingBlocks = builder
                .comment("§eIron Golems can be only repaired using Iron Blocks instead of iron ingots.")
                .translation("neatly-better.config.iron_golem_repair_using_blocks")
                .define("iron_golem_repair_using_blocks", true);

        builder.pop(); // iron_golem

        builder.push("generic");

        shulkerBulletDespawnsAfterOwner = builder
                .comment("§eShulker Bullets despawn after their Shulker is killed.")
                .translation("neatly-better.config.shulker_bullet_despawn_after_owner")
                .define("shulker_bullet_despawn_after_owner", true);

        runWhileCharging = builder
                .comment("§ePiglins & Pillagers run away while charging their crossbow.")
                .comment("")
                .comment("§e(At least they care about their lives)")
                .translation("neatly-better.config.run_while_charging")
                .define("run_while_charging", true);

        mobParkour = builder
                .comment("§eSome melee mobs can jump over 1 block gaps to reach their target.")
                .comment("")
                .comment("§eAffected Mobs:")
                .comment("§eVindicators, Piglin Brutes, Zombified Piglins & Zombies")
                .comment("")
                .comment("§4(Only on Hard Difficulty)")
                .translation("neatly-better.config.mob_parkour")
                .define("mob_parkour", true);

        groundItemBreeding = builder
                .comment("§eAnimals can be bread by eating dropped breeding items from the ground.")
                .translation("neatly-better.config.ground_item_breeding")
                .define("ground_item_breeding", true);

        rangedMobShieldHoldoff = builder
                .comment("§eMobs that use bows will holdoff from shooting if the player is actively blocking.")
                .translation("neatly-better.config.ranged_mob_shield_holdoff")
                .define("ranged_mob_shield_holdoff", true);

        builder.pop(); // generic

        builder.push("piglin");

        piglinRespectsTrims = builder
                .comment("§ePiglins do not attack players that have gold or snout armor trim equipped on them.")
                .translation("neatly-better.config.piglin_respects_trims")
                .define("piglin_respects_trims", true);

        builder.pop(); // piglin

        builder.push("sheep");

        sheepRunFromWolves = builder
                .comment("§eSheep runs away from nearby wolves.")
                .translation("neatly-better.config.sheep_run_from_wolves")
                .define("sheep_run_from_wolves", true);

        builder.pop(); //sheep

        builder.push("spider");

        oldSpiderAttacks = builder
                .comment("§eSpiders now have more responsive attacks with leaping mechanic.")
                .comment("")
                .comment("§e(This is inspired directly by old versions of the game)")
                .translation("neatly-better.config.old_spider_attacks")
                .define("old_spider_attacks", true);

        builder.pop(); // spider

        builder.push("vex");

        vexDiesAfterSummoner = builder
                .comment("§eVexes dies instantly the second their Evoker gets killed.")
                .translation("neatly-better.config.vex_dies_after_summoner")
                .define("vex_dies_after_summoner", true);

        builder.pop(); // vex

        builder.push("wolf");

        wolfImprovements = builder
                .comment("§eTamed wolves are improved with:")
                .comment("§e- 20% faster chase & follow speed")
                .comment("§e- 50% reduced attack cooldown")
                .comment("§e- Defend owner from entities targeting them")
                .comment("§e- Natural regen while sitting (1 HP per 3 seconds)")
                .translation("neatly-better.config.wolf_improvements")
                .define("wolf_improvements", true);

        builder.pop(); // wolf

        builder.pop(); // mob

        // ══════════════════════════════════════════════
        //  Block
        // ══════════════════════════════════════════════
        builder.push("block");

        builder.push("generic");

        rainIncreasesCropGrowth = builder
                .comment("§eCrops will grow faster when it's raining.")
                .comment("")
                .comment("§e(This works only if the crops are under the sun)")
                .translation("neatly-better.config.rain_increases_crop_growth")
                .define("rain_increases_crop_growth", true);

        harderNetherrack = builder
                .comment("§eNetherrack and similar blocks are harder to mine.")
                .translation("neatly-better.config.harder_netherrack")
                .define("harder_netherrack", true);

        magmaSpawnsLava = builder
                .comment("§eMining Magma Blocks without silk touch spawns lava.")
                .translation("neatly-better.config.magma_spawns_lava")
                .define("magma_spawns_lava", true);

        builder.pop(); // generic

        // ── Anvil ──
        builder.push("anvil");

        anvilNoRenameCost = builder
                .comment("§eItem renames using anvil do not cost xp.")
                .translation("neatly-better.config.anvil_no_rename_cost")
                .define("anvil_no_rename_cost", true);

        anvilNoItalicsRename = builder
                .comment("§eRemoves italics formatting on freshly renamed items.")
                .translation("neatly-better.config.anvil_no_rename_italics")
                .define("anvil_no_rename_italics", true);

        anvilRenameColors = builder
                .comment("§eRenaming items using anvils now supports formatting/color codes.")
                .translation("neatly-better.config.anvil_rename_colors")
                .define("anvil_rename_colors", true);

        anvilNoTooExpensive = builder
                .comment("§eRemoves too expensive message when applying/combining items.")
                .translation("neatly-better.config.anvil_no_too_expensive")
                .define("anvil_no_too_expensive", true);

        anvilEnchantFeather = builder
                .comment("§eAllows combining knockback enchantments with feathers.")
                .translation("neatly-better.config.anvil_enchant_feather")
                .define("anvil_enchant_feather", true);

        builder.pop(); // anvil

        // ── Beacon ──
        builder.push("beacon");

        beaconSoakEffects = builder
                .comment("§ePlayers soak beacon effects gradually when in range of a beacon.")
                .translation("neatly-better.config.beacon_soak_effects")
                .define("beacon_soak_effects", true);

        beaconSaturationEffect = builder
                .comment("§eSaturation effect is added to the secondary effect list.")
                .comment("§cRequires full Diamond/Netherite beacon base to work.")
                .translation("neatly-better.config.beacon_saturation_effect")
                .define("beacon_saturation_effect", true);

        maxBeaconRange = builder
                .comment("§eMaximum range of a beacon at max tier in blocks.")
                .comment("")
                .comment(" Vanilla Default: 50")
                .translation("neatly-better.config.max_beacon_range")
                .defineInRange("max_beacon_range", 75, 1, 512);

        builder.pop(); // beacon

        builder.push("dispenser");

        dispenserCauldronInteraction = builder
                .comment("§eDispensers can fill/empty cauldrons using buckets.")
                .translation("neatly-better.config.dispenser_cauldron_interaction")
                .define("dispenser_cauldron_interaction", true);

        builder.pop(); // dispenser

        builder.push("sponge");

        spongeBlockAbsorbRadius = builder
                .comment("§eRadius in blocks that a sponge absorbs water.")
                .comment("")
                .comment(" Vanilla Default: 7.")
                .translation("neatly-better.config.sponge_block_absorb_radius")
                .defineInRange("sponge_block_absorb_radius", 10, 1, 64);

        builder.pop(); // sponge

        builder.pop(); // block

        // ══════════════════════════════════════════════
        //  Item
        // ══════════════════════════════════════════════
        builder.push("item");

        totemCooldown = builder
                .comment("§eTotem of undying cooldown in seconds.")
                .comment("")
                .comment(" 0 = no cooldown")
                .translation("neatly-better.config.totem_cooldown")
                .defineInRange("totem_cooldown", 5, 0, 1800);

        shieldRaiseDelay = builder
                .comment("§eDelay in ticks before a shield is fully raised.")
                .comment("")
                .comment(" Vanilla Default: 5")
                .translation("neatly-better.config.shield_raise_delay")
                .defineInRange("shield_raise_delay", 0, 0, 5);

        doElytraFireworkBoosting = builder
                .comment("§eAllow firework rockets to boost elytra flight.")
                .translation("neatly-better.config.do_elytra_firework_boosting")
                .define("do_elytra_firework_boosting", false);

        featherNoDamage = builder
                .comment("§eFeather deals no damage to entities, applies only knockback.")
                .translation("neatly-better.config.feather_no_damage")
                .define("feather_no_damage", true);

        explosionsDisableShield = builder
                .comment("§eShields are disabled if hit directly by explosive damage.")
                .translation("neatly-better.config.explosions_disable_shield")
                .define("explosions_disable_shield", true);

        builder.pop(); // item

        // ══════════════════════════════════════════════
        //  Enchantment
        // ══════════════════════════════════════════════
        builder.push("enchantment");

        builder.push("general");

        stepUpDisabledWhileShifting = builder
                .comment("§eDisable step up enchantment when sneaking.")
                .translation("neatly-better.config.step_up")
                .define("step_up", true);

        lapisStaysInEnchanting = builder
                .comment("§eLapis Lazuli placed in an enchanting table stays stored when the GUI is closed.")
                .translation("neatly-better.config.lapis_stays_in_enchanting")
                .define("lapis_stays_in_enchanting", true);

        builder.pop(); // general

        // ── Mending ──
        builder.push("mending");

        mendingInventory = builder
                .comment("§eHow deep to search for items to repair.")
                .comment("")
                .translation("neatly-better.config.mending_inventory")
                .defineEnum("mending_inventory", MendingScope.INVENTORY);

        mendingEfficiency = builder
                .comment("§eHow efficient the mending enchantment is.")
                .comment("§e(XP to durability conversion)")
                .comment("")
                .comment(" Vanilla Default: 2")
                .translation("neatly-better.config.mending_efficiency")
                .defineInRange("mending_efficiency", 4.0, 1.0, 100.0);

        builder.pop(); // mending

        builder.pop(); // enchantment

        // ══════════════════════════════════════════════
        //  Nerfs
        // ══════════════════════════════════════════════
        builder.push("nerfs");

        protectionOverhaul = builder
                .comment("§eAll protection types have lower damage absorption.")
                .comment("")
                .comment("§eVanilla: Full Protection IV = 64% Damage Reduction.")
                .comment("         §eFull Fire/Projectile/Blast Protection IV = 80% Damage Reduction.")
                .comment("    §eMod: Full Protection IV = 35% Damage Reduction.")
                .comment("         §eFull Fire/Projectile/Blast Protection IV = 50% Damage Reduction.")
                .translation("neatly-better.config.protection_overhaul")
                .define("protection_overhaul", true);

        resistanceOverhaul = builder
                .comment("§eResistance effect applies lower damage absorption %.")
                .comment("")
                .comment("§eVanilla: Damage Reduction per effect level = 20% Damage Reduction.")
                .comment("§eMod: Damage Reduction per effect level = 10% Damage Reduction.")
                .comment("")
                .comment("§e(Resistance V now has 50% Damage Reduction instead of 100%)")
                .translation("neatly-better.config.resistance_overhaul")
                .define("resistance_overhaul", true);

        hungerMechanics = builder
                .comment("§eModifies the hunger system to make healing at full hp slower depending on difficultly.")
                .translation("neatly-better.config.hunger_mechanics")
                .define("hunger_mechanics", true);

        allowSleeping = builder
                .comment("§6VANILLA = No change")
                .comment("§4DENY = Deny sleeping")
                .comment("§5DRAGON = Allow sleeping after the player kills The Ender Dragon§r")
                .comment("")
                .translation("neatly-better.config.allow_sleeping")
                .defineEnum("allow_sleeping", SleepMode.DRAGON);

        villagerBookLevelCap = builder
                .comment("§eLibrarians can sell enchantment books with max level of 1.")
                .comment("")
                .comment("§e(Doesn't affect existing villagers)")
                .translation("neatly-better.config.villager_book_level_cap")
                .define("villager_book_level_cap", true);

        builder.pop(); // nerfs

        // ══════════════════════════════════════════════
        //  Player
        // ══════════════════════════════════════════════
        builder.push("player");

        builder.push("experience");

        xpFromMiningNonOre = builder
                .comment("§eGain XP by mining non-ore blocks.")
                .translation("neatly-better.config.xp_from_mining_non_ore")
                .define("xp_from_mining_non_ore", true);

        xpFromPlacingBlocks = builder
                .comment("§eGain XP by placing blocks.")
                .translation("neatly-better.config.xp_from_placing_blocks")
                .define("xp_from_placing_blocks", true);

        builder.pop(); // experience

        cropHarvester = builder
                .comment("§eHarvest fully grown crops by Right-Clicking.")
                .comment("§eUsing a hoes will harvest 3x3 area.")
                .translation("neatly-better.config.crop_harvester")
                .define("crop_harvester", true);

        armorStandSwap = builder
                .comment("§eQuickly swap armor with armor stands.")
                .comment("")
                .comment("§e(SHIFT + RightClick)")
                .translation("neatly-better.config.armor_stand_swap")
                .define("armor_stand_swap", true);

        builder.pop(); // player


    }

    public enum MendingScope {
        VANILLA,
        HOTBAR,
        INVENTORY
    }

    public enum SleepMode {
        ALLOW,
        DENY,
        DRAGON
    }
}

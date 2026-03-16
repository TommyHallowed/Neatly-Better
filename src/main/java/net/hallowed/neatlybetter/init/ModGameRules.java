package net.hallowed.neatlybetter.init;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;

import net.hallowed.NeatlyBetter;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;

import org.jetbrains.annotations.NotNull;

public final class ModGameRules {

    private ModGameRules() {}

    public static GameRule<@NotNull Boolean> DEATH_CHEST;
    public static GameRule<@NotNull Boolean> BEACON_SOAK_EFFECTS;
    public static GameRule<@NotNull Boolean> XP_FROM_MINING_NON_ORE;
    public static GameRule<@NotNull Boolean> XP_FROM_PLACING_BLOCKS;
    public static GameRule<@NotNull Boolean> VILLAGER_EMERALD_BLOCK_TEMPT;
    public static GameRule<@NotNull Boolean> VILLAGER_GLOBAL_CURING_PRICES;
    public static GameRule<@NotNull Boolean> VILLAGER_INFINITE_CURING_DISCOUNTS;
    public static GameRule<@NotNull Boolean> VILLAGER_BOOK_LEVEL_CAP;
    public static GameRule<@NotNull Boolean> DO_ELYTRA_FIREWORK_BOOSTING;
    public static GameRule<@NotNull Boolean> DISPENSER_CAULDRON_INTERACTION;

    public static GameRule<@NotNull Integer> SPONGE_BLOCK_ABSORB_RADIUS;
    public static GameRule<@NotNull Integer> MAX_BEACON_RANGE;
    public static GameRule<@NotNull Integer> SHIELD_RAISE_DELAY;

    public static void register() {
        DEATH_CHEST                        = bool("death_chest",                         GameRuleCategory.PLAYER,   false);
        BEACON_SOAK_EFFECTS                = bool("beacon_soak_effects",                 GameRuleCategory.MISC,     true);
        XP_FROM_MINING_NON_ORE             = bool("xp_from_mining_non_ore",              GameRuleCategory.DROPS,    true);
        XP_FROM_PLACING_BLOCKS             = bool("xp_from_placing_blocks",              GameRuleCategory.DROPS,    true);
        VILLAGER_EMERALD_BLOCK_TEMPT       = bool("villager_emerald_block_tempt",        GameRuleCategory.MOBS,     true);
        VILLAGER_GLOBAL_CURING_PRICES      = bool("villager_global_curing_prices",       GameRuleCategory.MOBS,     true);
        VILLAGER_INFINITE_CURING_DISCOUNTS = bool("villager_infinite_curing_discounts",  GameRuleCategory.MOBS,     false);
        VILLAGER_BOOK_LEVEL_CAP            = bool("villager_book_level_cap",             GameRuleCategory.MOBS,     false);
        DO_ELYTRA_FIREWORK_BOOSTING        = bool("do_elytra_firework_boosting",         GameRuleCategory.PLAYER,   false);
        DISPENSER_CAULDRON_INTERACTION     = bool("dispenser_cauldron_interaction",      GameRuleCategory.MISC,     true);

        SPONGE_BLOCK_ABSORB_RADIUS         = integer("sponge_block_absorb_radius",       GameRuleCategory.MISC,     10);
        MAX_BEACON_RANGE                   = integer("max_beacon_range",                 GameRuleCategory.MISC,     75);
        SHIELD_RAISE_DELAY                 = integer("shield_raise_delay",               GameRuleCategory.PLAYER,   0);
    }

    private static GameRule<@NotNull Boolean> bool(String id, GameRuleCategory cat, boolean def) {
        return GameRuleBuilder.forBoolean(def)
                .category(cat)
                .buildAndRegister(Identifier.fromNamespaceAndPath(NeatlyBetter.MOD_ID, id));
    }

    private static GameRule<@NotNull Integer> integer(String id, GameRuleCategory cat, int def) {
        return GameRuleBuilder.forInteger(def)
                .category(cat)
                .buildAndRegister(Identifier.fromNamespaceAndPath(NeatlyBetter.MOD_ID, id));
    }
}
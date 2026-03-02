package net.hallowed.oldways.init;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.hallowed.TheOldWays;
import net.minecraft.util.Identifier;
import net.minecraft.world.rule.GameRule;
import net.minecraft.world.rule.GameRuleCategory;

public final class ModGameRules {

    private ModGameRules() {}

    public static GameRule<Boolean> FULL_ENCHANTING_COST;
    public static GameRule<Boolean> BEACON_SOAK_EFFECTS;
    public static GameRule<Boolean> XP_FROM_MINING_NON_ORE;
    public static GameRule<Boolean> XP_FROM_PLACING_BLOCKS;
    public static GameRule<Boolean> VILLAGER_EMERALD_BLOCK_TEMPT;
    public static GameRule<Boolean> VILLAGER_GLOBAL_CURING_PRICES;
    public static GameRule<Boolean> VILLAGER_INFINITE_CURING_DISCOUNTS;
    public static GameRule<Boolean> DO_ELYTRA_FIREWORK_BOOSTING;

    public static GameRule<Integer> SPONGE_BLOCK_ABSORB_RADIUS;
    public static GameRule<Integer> MAX_BEACON_RANGE;
    public static GameRule<Integer> SHIELD_RAISE_DELAY_TICKS;

    public static void register() {
        FULL_ENCHANTING_COST               = bool("full_enchanting_cost",                GameRuleCategory.PLAYER,   true);
        BEACON_SOAK_EFFECTS                = bool("beacon_soak_effects",                 GameRuleCategory.MISC,     true);
        XP_FROM_MINING_NON_ORE             = bool("xp_from_mining_non_ore",              GameRuleCategory.DROPS,    true);
        XP_FROM_PLACING_BLOCKS             = bool("xp_from_placing_blocks",              GameRuleCategory.DROPS,    true);
        VILLAGER_EMERALD_BLOCK_TEMPT       = bool("villager_emerald_block_tempt",        GameRuleCategory.MOBS,     true);
        VILLAGER_GLOBAL_CURING_PRICES      = bool("villager_global_curing_prices",       GameRuleCategory.MOBS,     true);
        VILLAGER_INFINITE_CURING_DISCOUNTS = bool("villager_infinite_curing_discounts",  GameRuleCategory.MOBS,     false);
        DO_ELYTRA_FIREWORK_BOOSTING        = bool("do_elytra_firework_boosting",         GameRuleCategory.PLAYER,   false);

        SPONGE_BLOCK_ABSORB_RADIUS         = integer("sponge_block_absorb_radius",       GameRuleCategory.MISC,     10);
        MAX_BEACON_RANGE                   = integer("max_beacon_range",                 GameRuleCategory.MISC,     75);
        SHIELD_RAISE_DELAY_TICKS           = integer("shield_raise_delay_ticks",         GameRuleCategory.PLAYER,   0);
    }

    private static GameRule<Boolean> bool(String id, GameRuleCategory cat, boolean def) {
        return GameRuleBuilder.forBoolean(def)
                .category(cat)
                .buildAndRegister(Identifier.of(TheOldWays.MOD_ID, id));
    }

    private static GameRule<Integer> integer(String id, GameRuleCategory cat, int def) {
        return GameRuleBuilder.forInteger(def)
                .category(cat)
                .buildAndRegister(Identifier.of(TheOldWays.MOD_ID, id));
    }
}
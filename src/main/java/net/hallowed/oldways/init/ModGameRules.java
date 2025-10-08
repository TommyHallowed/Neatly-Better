package net.hallowed.oldways.init;

import net.hallowed.oldways.mixin.accessor.BooleanRuleAccessor;
import net.hallowed.oldways.mixin.accessor.GameRulesAccessor;
import net.hallowed.oldways.mixin.accessor.IntRuleAccessor;
import net.minecraft.world.GameRules;

import java.util.Map;

public final class ModGameRules {

    private ModGameRules() {}

    public static GameRules.Key<GameRules.BooleanRule> ELYTRA_FIREWORK_BOOSTING;
    public static GameRules.Key<GameRules.BooleanRule> ALLOW_SLEEP_AFTER_ENDER_DRAGON_KILL;
    public static GameRules.Key<GameRules.BooleanRule> FULL_ENCHANTING_COST;
    public static GameRules.Key<GameRules.BooleanRule> BEACON_SOAK_EFFECTS;
    public static GameRules.Key<GameRules.BooleanRule> XP_FROM_MINING_NON_ORE;
    public static GameRules.Key<GameRules.BooleanRule> XP_FROM_PLACING_BLOCKS;
    public static GameRules.Key<GameRules.BooleanRule> OLD_GOLD_XP_FARM;
    public static GameRules.Key<GameRules.BooleanRule> VILLAGER_EMERALD_BLOCK_TEMPT;
    public static GameRules.Key<GameRules.BooleanRule> VILLAGER_GLOBAL_CURING_PRICES;
    public static GameRules.Key<GameRules.BooleanRule> VILLAGER_INFINITE_CURING_DISCOUNTS;

    public static GameRules.Key<GameRules.IntRule> SPONGE_BLOCK_ABSORB_RADIUS;
    public static GameRules.Key<GameRules.IntRule> MAX_BEACON_RANGE;
    public static GameRules.Key<GameRules.IntRule> SHIELD_RAISE_DELAY_TICKS;
    public static GameRules.Key<GameRules.IntRule> MINECART_MAX_SPEED;

    public static void register() {
        ELYTRA_FIREWORK_BOOSTING           = bool("elytraFireworkBoosting",            GameRules.Category.PLAYER, false);
        ALLOW_SLEEP_AFTER_ENDER_DRAGON_KILL= bool("allowSleepAfterEnderDragonKill",    GameRules.Category.PLAYER, false);
        FULL_ENCHANTING_COST               = bool("fullEnchantingCost",                GameRules.Category.PLAYER,   true);
        BEACON_SOAK_EFFECTS                = bool("beaconSoakEffects",                 GameRules.Category.MISC,   true);
        XP_FROM_MINING_NON_ORE             = bool("xpFromMiningNonOre",                GameRules.Category.DROPS, true);
        XP_FROM_PLACING_BLOCKS             = bool("xpFromPlacingBlocks",               GameRules.Category.DROPS, true);
        OLD_GOLD_XP_FARM                   = bool("oldGoldXpFarm",                     GameRules.Category.DROPS,   false);
        VILLAGER_EMERALD_BLOCK_TEMPT       = bool("villagerEmeraldBlockTempt",         GameRules.Category.MOBS,   true);
        VILLAGER_GLOBAL_CURING_PRICES      = bool("villagerGlobalCuringPrices",         GameRules.Category.MOBS,   true);
        VILLAGER_INFINITE_CURING_DISCOUNTS = bool("villagerInfiniteCuringDiscounts",   GameRules.Category.MOBS,   false);

        SPONGE_BLOCK_ABSORB_RADIUS         = integer("spongeBlockAbsorbRadius",        GameRules.Category.MISC,   10);
        MAX_BEACON_RANGE                   = integer("maxbeaconRange",                 GameRules.Category.MISC,   75);
        SHIELD_RAISE_DELAY_TICKS           = integer("shieldRaiseDelayTicks",          GameRules.Category.PLAYER, 0);

        @SuppressWarnings("unchecked")
        GameRules.Key<GameRules.IntRule> existing = (GameRules.Key<GameRules.IntRule>)
                findKeyByName("minecartMaxSpeed");

        if (existing != null) {
            MINECART_MAX_SPEED = existing;
        } else {
            MINECART_MAX_SPEED = integer("minecartMaxSpeed", GameRules.Category.MISC, 48);
        }
    }

    public static boolean vanillaMinecartMaxSpeedPresent() {
        return findKeyByName("minecartMaxSpeed") != null;
    }


    private static GameRules.Key<GameRules.BooleanRule> bool(String id, GameRules.Category cat, boolean def) {
        return GameRulesAccessor.oldways$register(id, cat, BooleanRuleAccessor.oldways$create(def));
    }

    private static GameRules.Key<GameRules.IntRule> integer(String id, GameRules.Category cat, int def) {
        return GameRulesAccessor.oldways$register(id, cat, IntRuleAccessor.oldways$create(def));
    }

    private static GameRules.Key<?> findKeyByName(String name) {
        Map<GameRules.Key<?>, GameRules.Type<?>> map = GameRulesAccessor.oldways$getRuleTypes();
        for (GameRules.Key<?> key : map.keySet()) {
            String keyName;
            try {
                keyName = key.getName();
            } catch (Throwable ignored) {
                keyName = key.toString();
            }
            if (name.equals(keyName)) return key;
        }
        return null;
    }
}

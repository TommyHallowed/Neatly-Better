package net.hallowed.neatlybetter.api;

import net.fabricmc.loader.api.FabricLoader;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class NTCompat {

    private static final Set<String> PRECHECK_MODS = Set.of(
            "horseman",
            "respectmytrims",
            "backpacked",
            "bettertridents",
            "enchancement",
            "doubledoors",
            "rightclickharvest",
            "reap",
            "betterblockentities",
            "combatnouveau",
            "goldenagecombat",
            "extendedbonememeal",
            "universalbonemeal",
            "anvilrestoration",
            "easyanvils",
            "grassseeds"
    );

    private static final Map<String, Boolean> CACHE;

    static {
        Map<String, Boolean> cache = new HashMap<>(PRECHECK_MODS.size() + 4);
        for (String modId : PRECHECK_MODS) {
            boolean loaded = FabricLoader.getInstance().isModLoaded(modId);
            cache.put(modId, loaded);
        }
        CACHE = Collections.synchronizedMap(cache);
    }

    private NTCompat() {}

    public static boolean isLoaded(String modId) {
        Boolean cached = CACHE.get(modId);
        if (cached != null) return cached;

        boolean loaded = FabricLoader.getInstance().isModLoaded(modId);
        CACHE.put(modId, loaded);
        return loaded;
    }

    public static final boolean HORSEMAN          = isLoaded("horseman");
    public static final boolean RESPECTMYTRIMS    = isLoaded("respectmytrims");
    public static final boolean BACKPACKED        = isLoaded("backpacked");
    public static final boolean BETTERTRIDENTS    = isLoaded("bettertridents");
    public static final boolean ENCHANCEMENT      = isLoaded("enchancement");
    public static final boolean DOUBLEDOORS       = isLoaded("doubledoors");
    public static final boolean RIGHTCLICKHARVEST = isLoaded("rightclickharvest");
    public static final boolean REAP              = isLoaded("reap");
    public static final boolean BETTER_BLOCK_ENTITIES  = isLoaded("betterblockentities");
    public static final boolean COMBATNOUVEAU     = isLoaded("combatnouveau");
    public static final boolean GOLDENAGECOMBAT    = isLoaded("goldenagecombat");
    public static final boolean EXTENDEDBONEMEAL = isLoaded("extendedbonememeal");
    public static final boolean UNIVERSALBONEMEAL    = isLoaded("universalbonemeal");
    public static final boolean ANVILRESTORATION = isLoaded("anvilrestoration");
    public static final boolean EASYANVILS        = isLoaded("easyanvils");
    public static final boolean GRASSSEEDS        = isLoaded("grassseeds");
}

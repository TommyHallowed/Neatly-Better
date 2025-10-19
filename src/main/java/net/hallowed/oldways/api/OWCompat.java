package net.hallowed.oldways.api;

import net.fabricmc.loader.api.FabricLoader;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class OWCompat {

    private static final Set<String> PRECHECK_MODS = Set.of(
            "horseman",
            "respectmytrims",
            "mr_dungeons_andtavernsjungletempleoverhaul"
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

    private OWCompat() {}

    public static boolean isLoaded(String modId) {
        Boolean cached = CACHE.get(modId);
        if (cached != null) return cached;

        boolean loaded = FabricLoader.getInstance().isModLoaded(modId);
        CACHE.put(modId, loaded);
        return loaded;
    }

    public static final boolean HORSEMAN = isLoaded("horseman");
    public static final boolean RESPECTMYTRIMS = isLoaded("respectmytrims");
    public static final boolean DNTTEMPLEOVERHAUL = isLoaded("mr_dungeons_andtavernsjungletempleoverhaul");
}

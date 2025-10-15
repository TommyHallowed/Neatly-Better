package net.hallowed.oldways.init;

import net.hallowed.oldways.content.feature.*;

public final class ModEvents {
    private ModEvents() {}

    public static void init() {
        NoSleeping.register();
        ShoulderDropOnUse.register();
        AnvilRestoration.register();
        EdibleGlisteringMelon.register();
        StructureProtection.register();
        MendingNerf.init();
        BoneMealExpansion.init();
        ElytraFlightLimiter.init();
    }
}

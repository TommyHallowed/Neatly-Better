package net.hallowed.neatlybetter.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public final class NTCommonConfig {

    public static final NTCommonConfig CONFIG;
    public static final ModConfigSpec CONFIG_SPEC;

    static {
        Pair<NTCommonConfig, ModConfigSpec> pair =
                new ModConfigSpec.Builder().configure(NTCommonConfig::new);
        CONFIG = pair.getLeft();
        CONFIG_SPEC = pair.getRight();
    }

    public final ModConfigSpec.BooleanValue legacyCombat;

    private NTCommonConfig(ModConfigSpec.Builder builder) {

        legacyCombat = builder
                .comment("§eEnables legacy pre-1.9 combat mechanics including no attack cooldown, sword blocking,")
                .comment("§elegacy attack damage values, critical hits while sprinting, and more.")
                .comment("")
                .comment("§4⚠Requires Restart for attack damage values change!")
                .translation("neatly-better.config.legacy_combat")
                .define("legacy_combat", false);

    }
}

package net.hallowed.oldways.config;

import com.google.gson.annotations.SerializedName;


public final class CommonConfig {
    @SerializedName("Mending Nerf")
    public MendingNerf mendingNerf = new MendingNerf();

    @SerializedName("Elytra Boosting")
    public ElytraBoosting elytraBoosting = new ElytraBoosting();

    @SerializedName("Infinity Fix")
    public InfinityFix infinityFix = new InfinityFix();

    @SerializedName("Bed Nerf")
    public BedNerf bedNerf = new BedNerf();

    @SerializedName("Totem Cooldown")
    public TotemCooldown totemCooldown = new TotemCooldown();

    public static final class MendingNerf {
        public boolean enabled = true;
    }

    public static final class ElytraBoosting {
        // true = vanilla boosting works; false = disable air-use boost
        public boolean enabled = false;
    }

    public static final class InfinityFix {
        // true = You don't require arrow when you have infinity enchant
        public boolean enabled = true;
    }

    public static final class BedNerf {
        // true = Bed let's you sleep only after you killed ender dragon
        public boolean enabled = true;
    }

    public static final class TotemCooldown {
        // Number of seconds to apply as cooldown after a successful totem use.
        // Set to 0 to disable the cooldown entirely.
        public int seconds = 60;
    }
}

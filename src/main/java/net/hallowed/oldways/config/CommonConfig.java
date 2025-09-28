package net.hallowed.oldways.config;

import com.google.gson.annotations.SerializedName;

public final class CommonConfig {

    @SerializedName("Elytra Boosting")
    public ElytraBoosting elytraBoosting = new ElytraBoosting();

    @SerializedName("Infinity Fix")
    public InfinityFix infinityFix = new InfinityFix();

    @SerializedName("Bed Nerf")
    public BedNerf bedNerf = new BedNerf();

    @SerializedName("Mending Nerf")
    public MendingNerf mendingNerf = new MendingNerf();

    @SerializedName("Protection Nerf")
    public ProtectionNerf protectionNerf = new ProtectionNerf();

    @SerializedName("Player Velocity Fix")
    public VelocityFix velocityFix = new VelocityFix();

    @SerializedName("Totem of Undying")
    public TotemCooldown totemCooldown = new TotemCooldown();

    @SerializedName("Old Enchanting Costs")
    public OldEnchant oldEnchant = new OldEnchant();


    // NEW: Villager category
    @SerializedName("Villager Tweaks")
    public Villager villager = new Villager();

    // --- Sections ---

    public static final class ElytraBoosting {
        @SerializedName("_comment")
        public String comment = "Removes (vanilla) firework elytra boosting";
        public boolean enabled = false;
    }

    public static final class InfinityFix {
        @SerializedName("_comment")
        public String comment = "Removes the need for an arrow in the inventory when using infinity";
        public boolean enabled = true;
    }

    public static final class BedNerf {
        @SerializedName("_comment")
        public String comment = "Allows players to sleep only after they kill Ender Dragon";
        public boolean enabled = true;
    }

    public static final class MendingNerf {
        @SerializedName("_comment")
        public String comment = "Changes mending book behaviour to lower repair cost";
        public boolean enabled = true;
    }

    public static final class ProtectionNerf {
        @SerializedName("_comment")
        public String comment = "Reduces protection damage absorptions and burning time.";
        public boolean enabled = true;
    }

    public static final class VelocityFix {
        @SerializedName("_comment")
        public String comment = "Increases player speed limits / fixes rubber-banding.";
        public boolean enabled = false;
    }

    public static final class TotemCooldown {
        @SerializedName("_comment")
        public String comment = "In seconds.";
        @SerializedName("cooldown")
        public int seconds = 60;
    }

    public static final class OldEnchant {
        @SerializedName("_comment")
        public String comment = "Brings back the old enchanting costs from earlier versions.";
        public boolean enabled = false;
    }

    /** New Villager options. Vanilla-faithful, first slot only. */
    public static final class Villager {
        /** Persist & apply lowest first-slot price globally for everyone. */
        @SerializedName("Global Curing Prices")
        public boolean globalCuringPrices = true;

        /** Keep lowering stored floor when curing lowers price again (if false, first low price is locked). */
        @SerializedName("Infinite Curing Discounts")
        public boolean infiniteCuringDiscounts = true;
    }
}

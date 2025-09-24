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

    // Keep the object name as-is to preserve your file layout
    @SerializedName("Protection Damage Reduction")
    public ProtectionDamageAbsorption protection = new ProtectionDamageAbsorption();

    // --- Sections ---

    public static final class MendingNerf {
        @SerializedName("_comment")
        public String comment = "changes mending enchantment to reset anvil repair cost";
        public boolean enabled = true;
    }

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

    public static final class TotemCooldown {
        @SerializedName("_comment")
        public String comment = "Adds cooldown to the totem of undying";
        public int seconds = 60;
    }

    /** Fractions (0.10 = 10%). Scales linearly with total Protection levels up to 16 (Prot IV on 4 pieces). */
    public static final class ProtectionDamageAbsorption {
        @SerializedName("_comment")
        public String comment = "Changes protection enchantment damage reduction [0.1 - 10%]";
        @SerializedName("Max Reduction")            public float genericMax    = 0.10f;
        @SerializedName("Max Fire Reduction")       public float fireMax       = 0.20f;
        @SerializedName("Max Blast Reduction")      public float blastMax      = 0.20f;
        @SerializedName("Max Projectile Reduction") public float projectileMax = 0.20f;
    }
}

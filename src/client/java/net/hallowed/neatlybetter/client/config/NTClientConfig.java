package net.hallowed.neatlybetter.client.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import org.apache.commons.lang3.tuple.Pair;

public final class NTClientConfig {

    public static final NTClientConfig CONFIG;
    public static final ModConfigSpec CONFIG_SPEC;

    static {
        Pair<NTClientConfig, ModConfigSpec> pair =
                new ModConfigSpec.Builder().configure(NTClientConfig::new);
        CONFIG = pair.getLeft();
        CONFIG_SPEC = pair.getRight();
    }

    // ═══════════════════════════════════════════
    //  UI
    // ═══════════════════════════════════════════

    // -- Recipe Book --
    public final ModConfigSpec.EnumValue<RecipeBookMode> recipeBookMode;

    // -- Title Screen --
    public final ModConfigSpec.BooleanValue legacyTitleScreenLayout;
    public final ModConfigSpec.BooleanValue languageButton;
    public final ModConfigSpec.BooleanValue realmsButtons;
    public final ModConfigSpec.BooleanValue customBranding;
    public final ModConfigSpec.BooleanValue accessibilityButton;

    // -- Resourcepacks Screen --
    public final ModConfigSpec.BooleanValue resourcePackCompatibility;

    // -- Tooltips --
    public final ModConfigSpec.BooleanValue tooltipGuide;
    public final ModConfigSpec.BooleanValue potionEffectIcons;
    public final ModConfigSpec.BooleanValue susStewEffectIcons;
    public final ModConfigSpec.BooleanValue mapTooltip;
    public final ModConfigSpec.BooleanValue shulkerBoxTooltip;

    // ═══════════════════════════════════════════
    //  HUD
    // ═══════════════════════════════════════════

    // -- Effects --
    public final ModConfigSpec.BooleanValue effectBars;
    public final ModConfigSpec.BooleanValue hideAirBubbles;

    // -- Coordinates & Time --
    public final ModConfigSpec.BooleanValue showCoords;
    public final ModConfigSpec.BooleanValue showTime;
    public final ModConfigSpec.EnumValue<Corner> coordsPos;
    public final ModConfigSpec.EnumValue<Corner> timePos;
    public final ModConfigSpec.ConfigValue<String> coordsFormat;
    public final ModConfigSpec.ConfigValue<String> timeDayFormat;

    // ═══════════════════════════════════════════
    //  Misc
    // ═══════════════════════════════════════════

    // -- Utilities --
    public final ModConfigSpec.BooleanValue swingThrough;
    public final ModConfigSpec.BooleanValue copyScreenshots;
    public final ModConfigSpec.BooleanValue autoRefill;
    public final ModConfigSpec.BooleanValue telemetryOff;
    public final ModConfigSpec.BooleanValue instantEyeHeight;
    public final ModConfigSpec.BooleanValue denyOffhandWhileHungry;
    public final ModConfigSpec.BooleanValue ladderForceSneakOnGui;

    // -- Render --
    public final ModConfigSpec.BooleanValue showStuckProjectiles;
    public final ModConfigSpec.BooleanValue showPotionGlint;
    public final ModConfigSpec.BooleanValue render2DItems;
    public final ModConfigSpec.BooleanValue showDurability;

    // -- Void Fog --
    public final ModConfigSpec.IntValue MaxHeight;
    public final ModConfigSpec.DoubleValue TransitionDistance;
    public final ModConfigSpec.DoubleValue Density;
    public final ModConfigSpec.IntValue ParticleDensity;
    public final ModConfigSpec.BooleanValue RespectTorches;
    public final ModConfigSpec.BooleanValue ScaleWithDifficulty;
    public final ModConfigSpec.BooleanValue DisableInCreative;
    public final ModConfigSpec.DoubleValue TransitionTicks;
    public final ModConfigSpec.DoubleValue DissipationDelayTicks;

    private NTClientConfig(ModConfigSpec.Builder builder) {

        // ── UI ──────────────────────────────────
        builder.push("ui");

        builder.push("recipe_book");
        recipeBookMode = builder
                .comment("§eControls recipe book visibility mode.")
                .translation("neatly-better.config.recipe_book_mode")
                .defineEnum("recipe_book_mode", RecipeBookMode.AUTOHIDE);
        builder.pop();

        builder.push("title_screen");
        legacyTitleScreenLayout = builder
                .comment("§eOld title screen button layout from pre-26.2 versions.")
                .translation("neatly-better.config.legacy_title_screen_layout")
                .define("legacy_title_screen_layout", true);
        realmsButtons = builder
                .comment("§eShow Realms button in title screen.")
                .translation("neatly-better.config.realms_buttons")
                .define("realms_buttons", true);
        languageButton = builder
                .comment("§eShow language button in title screen.")
                .translation("neatly-better.config.language_button")
                .define("language_button", true);
        customBranding = builder
                .comment("§eReplace the Fabric/modded branding in title screen with clean vanilla one.")
                .translation("neatly-better.config.custom_branding")
                .define("custom_branding", true);
        accessibilityButton = builder
                .comment("§eShow extra accessibility button in title screen.")
                .translation("neatly-better.config.accessibility_button")
                .define("accessibility_button", true);
        builder.pop();

        builder.push("resourcepacks");
        resourcePackCompatibility = builder
                .comment("§eShow resource pack compatibility warnings with red underlay.")
                .translation("neatly-better.config.resource_pack_compatibility")
                .define("resource_pack_compatibility", false);
        builder.pop();

        builder.push("tooltips");
        tooltipGuide = builder
                .comment("§eShow tooltip guides on items.")
                .translation("neatly-better.config.tooltip_guide")
                .define("tooltip_guide", true);
        potionEffectIcons = builder
                .comment("§eShow potion effect icons in item tooltips.")
                .translation("neatly-better.config.potion_effect_icons")
                .define("potion_effect_icons", true);
        susStewEffectIcons = builder
                .comment("§eShow suspicious stew effect icons in tooltips.")
                .comment("")
                .comment("§e(Only works if potion_effect_icons is enabled)")
                .translation("neatly-better.config.sus_stew_effect_icons")
                .define("sus_stew_effect_icons", true);
        mapTooltip = builder
                .comment("§eShow a map preview in the tooltip.")
                .translation("neatly-better.config.map_tooltip")
                .define("map_tooltip", true);
        shulkerBoxTooltip = builder
                .comment("§eShow a preview of items in shulker boxes.")
                .translation("neatly-better.config.shulkerbox_tooltip")
                .define("shulkerbox_tooltip", true);
        builder.pop();

        builder.pop(); // ui

        // ── HUD ─────────────────────────────────
        builder.push("hud");

        builder.push("effects");
        effectBars = builder
                .comment("§eShow status effect bars on the HUD.")
                .translation("neatly-better.config.effect_bars")
                .define("effect_bars", true);
        hideAirBubbles = builder
                .comment("§eHide air bubbles when water breathing effect is applied.")
                .translation("neatly-better.config.hide_air_bubbles")
                .define("hide_air_bubbles", true);
        builder.pop();

        builder.push("coords_time");
        showCoords = builder
                .comment("§eShow coordinates text on the HUD.")
                .translation("neatly-better.config.show_coords")
                .define("show_coords", true);
        showTime = builder
                .comment("§eShow in-game day/time text on the HUD.")
                .translation("neatly-better.config.show_time")
                .define("show_time", true);
        coordsPos = builder
                .comment("§eScreen corner for coordinates hud text.")
                .comment("")
                .translation("neatly-better.config.coords_pos")
                .defineEnum("coords_pos", Corner.TOP_LEFT);
        timePos = builder
                .comment("§eScreen corner for time/day hud text.")
                .comment("")
                .translation("neatly-better.config.time_pos")
                .defineEnum("time_pos", Corner.TOP_LEFT);
        coordsFormat = builder
                .comment("§eFormat string for coordinates. Placeholders: {x}, {y}, {z}")
                .translation("neatly-better.config.coords_format")
                .define("coords_format", "XYZ: {x} | {y} | {z}");
        timeDayFormat = builder
                .comment("§eFormat string for day/time display. Placeholders: {day}, {time}")
                .translation("neatly-better.config.time_day_format")
                .define("time_day_format", "Day: {day} | {time}");
        builder.pop();

        builder.pop(); // hud

        // ── Misc ────────────────────────────────
        builder.push("misc");

        builder.push("utilities");
        swingThrough = builder
                .comment("§eSwing through grass and other non-collidable blocks to hit entities behind it.")
                .translation("neatly-better.config.swing_through")
                .define("swing_through", true);
        copyScreenshots = builder
                .comment("§eAutomatically copy screenshot images to clipboard.")
                .translation("neatly-better.config.copy_screenshots")
                .define("copy_screenshots", true);
        autoRefill = builder
                .comment("§eAuto-refill the hotbar slot when an item stack runs out or gets used up.")
                .translation("neatly-better.config.auto_refill")
                .define("auto_refill", true);
        telemetryOff = builder
                .comment("§eDisable Mojang's telemetry systems.")
                .translation("neatly-better.config.telemetry_off")
                .define("telemetry_off", true);

        showDurability = builder
                .comment("§eShow remaining durability as a small number in the top-right of item slots.")
                .translation("neatly-better.config.show_durability")
                .define("show_durability", true);

        instantEyeHeight = builder
                .comment("§eNo more smooth transitions for changing camera eye height.")
                .translation("neatly-better.config.instant_eye_height")
                .define("instant_eye_height", false);

        denyOffhandWhileHungry = builder
                .comment("§eDeny off-hand block placement after eating.")
                .translation("neatly-better.config.deny_offhand_while_hungry")
                .define("deny_offhand_while_hungry", true);

        ladderForceSneakOnGui = builder
                .comment("§eForce the player to sneak while climbing a ladder with a GUI open.")
                .translation("neatly-better.config.ladder_force_sneak_on_gui")
                .define("ladder_force_sneak_on_gui", true);
        builder.pop();

        builder.push("render");
        showStuckProjectiles = builder
                .comment("§eRender projectiles stuck in entities.")
                .comment("§eWork In Progress")
                .translation("neatly-better.config.show_stuck_projectiles")
                .define("show_stuck_projectiles", false);
        showPotionGlint = builder
                .comment("§eRender enchantment glint on potion items.")
                .translation("neatly-better.config.show_potion_glint")
                .define("show_potion_glint", true);
        render2DItems = builder
                .comment("§eRender items as flat 2D sprites instead of 3D models.")
                .translation("neatly-better.config.render_2d_items")
                .define("render_2d_items", false);
        builder.push("void_fog");

        MaxHeight = builder
                .comment("§eMaximum height in blocks above world bottom that void fog reaches.")
                .translation("neatly-better.config.void_fog_max_height")
                .defineInRange("void_fog_max_height", 20, 1, 256);
        TransitionDistance = builder
                .comment("§eDistance in blocks below the max fog height at which fog starts blending in.")
                .translation("neatly-better.config.void_fog_transition_distance")
                .defineInRange("void_fog_transition_distance", 1.0, 0.1, 32.0);
        Density = builder
                .comment("§eFog density as a 0–1 fraction. Lower values widen the gap between fog start and end.")
                .translation("neatly-better.config.void_fog_density")
                .defineInRange("void_fog_density", 1.0, 0.0, 1.0);
        ParticleDensity = builder
                .comment("§eNumber of void particles spawned per tick near the void.")
                .translation("neatly-better.config.void_fog_particle_density")
                .defineInRange("void_fog_particle_density", 200, 0, 1000);
        RespectTorches = builder
                .comment("§eWhether block/torch light reduces void fog distance.")
                .translation("neatly-better.config.void_fog_respect_torches")
                .define("void_fog_respect_torches", true);
        ScaleWithDifficulty = builder
                .comment("§eWhether difficulty level multiplies the effective fog height.")
                .translation("neatly-better.config.void_fog_scale_with_difficulty")
                .define("void_fog_scale_with_difficulty", true);
        DisableInCreative = builder
                .comment("§eWhether creative-mode players are exempt from void fog.")
                .translation("neatly-better.config.void_fog_disable_in_creative")
                .define("void_fog_disable_in_creative", true);
        TransitionTicks = builder
                .comment("§eTicks for fog and darkness to fully fade in or out (20 ticks = 1 second).")
                .translation("neatly-better.config.void_fog_transition_ticks")
                .defineInRange("void_fog_transition_ticks", 10.0, 1.0, 200.0);
        DissipationDelayTicks = builder
                .comment("§eTicks to wait after the player moves above the fog zone before dissipation begins.")
                .comment("§eSet to 0 for no delay. (20 ticks = 1 second)")
                .translation("neatly-better.config.void_fog_dissipation_delay_ticks")
                .defineInRange("void_fog_dissipation_delay_ticks", 30.0, 0.0, 600.0);
        builder.pop(); // void_fog

        builder.pop(); // render

        builder.pop(); // misc
    }

    public enum RecipeBookMode {
        AUTOHIDE, HIDDEN, SHOWN
    }

    public enum Corner {
        TOP_LEFT, TOP_RIGHT, BOTTOM_RIGHT, BOTTOM_LEFT
    }
}
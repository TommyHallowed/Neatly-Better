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

    // ═══════════════════════════════════════════
    //  HUD
    // ═══════════════════════════════════════════

    // -- Locator Bar --
    public final ModConfigSpec.BooleanValue clientWaypoints;
    public final ModConfigSpec.BooleanValue tabShowsNames;

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

    // -- Render --
    public final ModConfigSpec.BooleanValue showStuckProjectiles;
    public final ModConfigSpec.BooleanValue showPotionGlint;
    public final ModConfigSpec.BooleanValue render2DItems;

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
        realmsButtons = builder
                .comment("§eShow Realms buttons in title screen.")
                .translation("neatly-better.config.realms_buttons")
                .define("realms_buttons", false);
        customBranding = builder
                .comment("§eReplace the Fabric/modded branding in title screen with clean vanilla text.")
                .translation("neatly-better.config.custom_branding")
                .define("custom_branding", true);
        accessibilityButton = builder
                .comment("§eShow extra accessibility button in title screen.")
                .translation("neatly-better.config.accessibility_button")
                .define("accessibility_button", false);
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
        builder.pop();

        builder.pop(); // ui

        // ── HUD ─────────────────────────────────
        builder.push("hud");

        builder.push("locator_bar");
        clientWaypoints = builder
                .comment("§eShow lodestone waypoints on the locator bar.")
                .translation("neatly-better.config.client_waypoints")
                .define("client_waypoints", true);
        tabShowsNames = builder
                .comment("§eShow lodestone waypoint names on locator bar when holding tab.")
                .translation("neatly-better.config.tab_shows_names")
                .define("tab_shows_names", true);
        builder.pop();

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
        builder.pop();

        builder.pop(); // misc
    }

    public enum RecipeBookMode {
        AUTOHIDE, HIDDEN, SHOWN
    }

    public enum Corner {
        TOP_LEFT, TOP_RIGHT, BOTTOM_RIGHT, BOTTOM_LEFT
    }
}
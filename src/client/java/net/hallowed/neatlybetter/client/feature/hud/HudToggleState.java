package net.hallowed.neatlybetter.client.feature.hud;

import com.mojang.logging.LogUtils;

import net.fabricmc.loader.api.FabricLoader;

import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * On/off state of the compass and clock buttons in the inventory (whether the coordinates / time text is shown).
 *
 * <p>Kept apart from the client config on purpose: the config options {@code show_coords} / {@code show_time} turn the
 * whole feature (button and HUD text) on or off, while this is just the player's current choice. Stored in a small
 * file in the config folder so it survives restarts; read once, written only when a button is clicked.
 */
public final class HudToggleState {
    private HudToggleState() {}

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("neatly-better-hud-state.properties");

    private static boolean loaded;
    private static boolean coordsShown = true;
    private static boolean timeShown = true;

    public static boolean coordsShown() {
        load();
        return coordsShown;
    }

    public static boolean timeShown() {
        load();
        return timeShown;
    }

    public static void toggleCoords() {
        load();
        coordsShown = !coordsShown;
        save();
    }

    public static void toggleTime() {
        load();
        timeShown = !timeShown;
        save();
    }

    private static void load() {
        if (loaded) return;
        loaded = true;
        if (!Files.exists(FILE)) return;
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(FILE)) {
            props.load(in);
            coordsShown = Boolean.parseBoolean(props.getProperty("coords_shown", "true"));
            timeShown = Boolean.parseBoolean(props.getProperty("time_shown", "true"));
        } catch (IOException e) {
            LOGGER.warn("[NeatlyBetter] Could not read {}", FILE, e);
        }
    }

    private static void save() {
        Properties props = new Properties();
        props.setProperty("coords_shown", Boolean.toString(coordsShown));
        props.setProperty("time_shown", Boolean.toString(timeShown));
        try (OutputStream out = Files.newOutputStream(FILE)) {
            props.store(out, "Neatly Better: state of the inventory compass/clock buttons");
        } catch (IOException e) {
            LOGGER.warn("[NeatlyBetter] Could not save {}", FILE, e);
        }
    }
}

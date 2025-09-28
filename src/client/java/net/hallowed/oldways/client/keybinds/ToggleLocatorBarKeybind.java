package net.hallowed.oldways.client.keybinds;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.hallowed.oldways.client.config.ClientConfigManager;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public final class ToggleLocatorBarKeybind {
    private static KeyBinding TOGGLE;

    public static void register() {
        // Use the constant to avoid typos in the category id
        TOGGLE = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.old-ways.toggle_locator_bar",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                KeyBinding.MISC_CATEGORY   // <- guaranteed Misc category
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (TOGGLE.wasPressed()) {
                ClientConfigManager.toggleLocatorBar(); // flips + saves
                // no chat message
            }
        });
    }

    private ToggleLocatorBarKeybind() {}
}

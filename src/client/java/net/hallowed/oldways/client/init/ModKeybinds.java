package net.hallowed.oldways.client.init;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.hallowed.oldways.client.network.OldWaysNetworkClient;
import net.hallowed.oldways.content.item.MapBuilderItem;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public final class ModKeybinds {

    public static final KeyBinding.Category OLDWAYS_CATEGORY = KeyBinding.Category.create(Identifier.of("oldways", "keys"));
    private static KeyBinding zoomKey;

    public static void register() {
        zoomKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.oldways.map_zoom",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_Z,
                OLDWAYS_CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            boolean holdingBuilder = client.player.getMainHandStack().getItem() instanceof MapBuilderItem;
            boolean sneaking = client.player.isSneaking();

            if (holdingBuilder && sneaking) {
                while (zoomKey.wasPressed()) {
                    OldWaysNetworkClient.sendMapBuilderAction(0); // 0 is Zoom
                }

                if (client.options.attackKey.wasPressed()) {
                    if (client.crosshairTarget != null && client.crosshairTarget.getType() == HitResult.Type.MISS) {
                        OldWaysNetworkClient.sendMapBuilderAction(1); // 1 is Reset
                    }
                }
            } else {
                while (zoomKey.wasPressed()) {} // Consume to prevent firing later
            }
        });
    }
}
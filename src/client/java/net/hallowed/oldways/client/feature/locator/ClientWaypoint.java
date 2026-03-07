package net.hallowed.oldways.client.feature.locator;

import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

public record ClientWaypoint(Vec3 pos, Optional<Component> text, Identifier style, Optional<Integer> color) {
    public int getColor() {
        return color().orElse(pos().toString().hashCode()) & 0xFFFFFF;
    }
}

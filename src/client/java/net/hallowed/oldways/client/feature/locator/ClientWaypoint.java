package net.hallowed.oldways.client.feature.locator;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;

public record ClientWaypoint(Vec3d pos, Optional<Text> text, Identifier style, Optional<Integer> color) {
    /** RGB (no alpha). Fallback is stable hash of position so icons still differ. */
    public int getColor() {
        return color().orElse(pos().toString().hashCode()) & 0xFFFFFF;
    }
}

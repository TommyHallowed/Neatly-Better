package net.hallowed.neatlybetter.client.feature.locator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class DeathTracker {
    private DeathTracker() {}

    private static final long DURATION_MS = 10L * 60L * 1000L;

    @Nullable private static BlockPos lastKnownDeathPos;
    @Nullable private static ResourceKey<@NotNull Level> lastKnownDeathDim;
    private static long deathTimeMs;
    private static boolean initialized;

    public static boolean tick(@Nullable GlobalPos deathLocation) {
        if (deathLocation == null) {
            if (!initialized) initialized = true;
            return false;
        }

        BlockPos deathPos = deathLocation.pos();
        ResourceKey<@NotNull Level> deathDim = deathLocation.dimension();

        boolean changed = !deathPos.equals(lastKnownDeathPos)
                || !Objects.equals(lastKnownDeathDim, deathDim);

        if (changed) {
            lastKnownDeathPos = deathPos.immutable();
            lastKnownDeathDim = deathDim;

            if (!initialized) {
                initialized = true;
                return false;
            }

            deathTimeMs = System.currentTimeMillis();
        }

        if (deathTimeMs == 0L) return false;
        return (System.currentTimeMillis() - deathTimeMs) < DURATION_MS;
    }

    public static void reset() {
        lastKnownDeathPos = null;
        lastKnownDeathDim = null;
        deathTimeMs = 0L;
        initialized = false;
    }
}

package net.hallowed.oldways.client.feature.locator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Client-side tracker that detects when the player's death location changes
 * and provides a 10-minute timer for showing a death waypoint without
 * needing a Recovery Compass.
 *
 * <p>On first join, existing (stale) death locations are ignored — only
 * deaths that happen <em>during</em> this session start the timer.</p>
 */
public final class DeathTracker {
    private DeathTracker() {}

    /** How long the timed death waypoint stays visible (10 real-time minutes). */
    private static final long DURATION_MS = 10L * 60L * 1000L;

    @Nullable private static BlockPos lastKnownDeathPos;
    @Nullable private static ResourceKey<@NotNull Level> lastKnownDeathDim;
    private static long deathTimeMs;
    private static boolean initialized;

    /**
     * Call every frame from {@link WaypointTracking#update}.
     *
     * @param deathLocation the player's current {@code getLastDeathLocation()}, or {@code null}
     * @return {@code true} if a timed death waypoint should be displayed
     */
    public static boolean tick(@Nullable GlobalPos deathLocation) {
        if (deathLocation == null) {
            // Player has never died (or data was cleared) — mark as initialized
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
                // First check after joining — this is a stale death from a previous session
                initialized = true;
                return false;
            }

            // New death detected during this session — start the 10-minute timer
            deathTimeMs = System.currentTimeMillis();
        }

        if (deathTimeMs == 0L) return false;
        return (System.currentTimeMillis() - deathTimeMs) < DURATION_MS;
    }

    /** Reset all state. Called on disconnect / player == null. */
    public static void reset() {
        lastKnownDeathPos = null;
        lastKnownDeathDim = null;
        deathTimeMs = 0L;
        initialized = false;
    }
}

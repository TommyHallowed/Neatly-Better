package net.hallowed.oldways.mixin.accessor;

import net.minecraft.server.level.ServerChunkCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Exposes package-private {@code runDistanceManagerUpdates()} on
 * {@link ServerChunkCache} so the map scanner can flush newly-added
 * tickets immediately rather than waiting for the next server tick.
 */
@Mixin(ServerChunkCache.class)
public interface ServerChunkCacheAccessor {

    @SuppressWarnings("UnusedReturnValue")
    @Invoker("runDistanceManagerUpdates")
    boolean invokeRunDistanceManagerUpdates();
}

package net.hallowed.neatlybetter.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import java.util.ArrayList;
import java.util.List;

/** Runs tasks at the end of the current server tick (e.g. after a vanilla interaction has finished). */
public final class ServerTaskQueue {
    private ServerTaskQueue() {}

    private static List<Runnable> pending = new ArrayList<>();

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(_ -> {
            if (pending.isEmpty()) return;
            List<Runnable> tasks = pending;
            pending = new ArrayList<>();
            tasks.forEach(Runnable::run);
        });
    }

    /** Main server thread only. */
    public static void runAtEndOfTick(Runnable task) {
        pending.add(task);
    }
}

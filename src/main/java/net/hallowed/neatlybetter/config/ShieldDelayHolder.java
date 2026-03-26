package net.hallowed.neatlybetter.config;

/**
 * Holds the effective shield raise delay, accessible on both client and server.
 *
 * <p><b>Server:</b> Updated from {@code NTServerConfig} and pushed to clients on join/reload.
 * <p><b>Client:</b> Updated by the {@code ShieldDelaySyncPayload} handler.
 * <p>Default is 5 (vanilla). 0 = instant blocking.
 */
public final class ShieldDelayHolder {
    private static volatile int shieldRaiseDelay = 5;

    private ShieldDelayHolder() {}

    public static int getShieldRaiseDelay() {
        return shieldRaiseDelay;
    }

    public static void setShieldRaiseDelay(int delay) {
        shieldRaiseDelay = Math.max(0, delay);
    }
}

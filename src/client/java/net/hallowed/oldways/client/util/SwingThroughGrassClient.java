package net.hallowed.oldways.client.util;

public final class SwingThroughGrassClient {
    private static boolean ENABLED = true;

    public static boolean enabled() {
        return ENABLED;
    }

    public static void setEnabled(boolean on) {
        ENABLED = on;
    }

    private SwingThroughGrassClient() {}
}

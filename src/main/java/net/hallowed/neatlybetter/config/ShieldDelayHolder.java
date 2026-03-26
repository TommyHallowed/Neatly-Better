package net.hallowed.neatlybetter.config;

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

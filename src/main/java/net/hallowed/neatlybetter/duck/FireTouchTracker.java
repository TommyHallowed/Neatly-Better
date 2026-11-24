package net.hallowed.neatlybetter.duck;

/** Implemented on Entity by SoulFireEntityMixin: the fire block or campfire an entity touched this tick. */
public interface FireTouchTracker {
    void neatlybetter$markFireTouch(boolean soul, long gameTime);

    /** True when the last touch happened at {@code gameTime}. */
    boolean neatlybetter$touchedFireAt(long gameTime);

    boolean neatlybetter$lastTouchWasSoul();

    /** Client, local player: fire kind predicted from a touch, so the flames switch without waiting for the server. */
    void neatlybetter$predictFireKind(boolean soul, long gameTime);

    /** The predicted kind if it was made after {@code since}, else null. */
    Boolean neatlybetter$predictedFireKind(long since);
}

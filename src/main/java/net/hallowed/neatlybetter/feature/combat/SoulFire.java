package net.hallowed.neatlybetter.feature.combat;

import net.hallowed.neatlybetter.duck.FireTouchTracker;
import net.hallowed.neatlybetter.registry.ModData;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/**
 * Players burn with soul fire or normal fire depending on what set them on fire.
 *
 * <ul>
 *   <li>Touching soul fire or a lit soul campfire: soul fire. Touching fire, a campfire or lava: normal fire.
 *       The kind switches on touch, also while already burning (the last one touched wins).</li>
 *   <li>Every other ignition (Fire Aspect, flaming arrows, fire charges, lightning...) gives normal fire.</li>
 *   <li>Soul burning shows soul flames (third and first person) and deals 2 damage per second instead of 1,
 *       like the soul fire block.</li>
 * </ul>
 *
 * <p>The kind is a synced player attachment, decided at ignition and never cleared when the fire goes out: clearing
 * it would reach clients a moment before the fire flag and flash normal flames at the end. While not burning it
 * has no effect.
 */
public final class SoulFire {
    private SoulFire() {}

    /** Burn damage per second: vanilla burning deals 1; the soul fire block deals 2 on contact. */
    public static final float SOUL_BURN_DAMAGE = 2.0F;

    /**
     * How long a client-side prediction is trusted over the synced value: covers the server round trip (the synced
     * value then agrees), short enough that a server-side change (e.g. a Fire Aspect hit) shows up soon after.
     */
    private static final int PREDICTION_TICKS = 10;

    public static boolean isSoulBurning(Entity entity) {
        if (!(entity instanceof Player player) || !entity.isOnFire()) return false;
        if (player.isLocalPlayer()) {
            Boolean predicted = ((FireTouchTracker) entity).neatlybetter$predictedFireKind(
                    entity.level().getGameTime() - PREDICTION_TICKS);
            if (predicted != null) return predicted;
        }
        return entity.getAttachedOrElse(ModData.SOUL_FIRE, false);
    }

    /** A player touched a fire block or lit campfire of the given kind (called on both sides). */
    public static void touchedFire(Entity entity, boolean soul) {
        if (!(entity instanceof Player player)) return;
        if (entity.level().isClientSide()) {
            // The local player runs the same block-touch code client side: predict the kind so the flames switch
            // immediately instead of a server round trip (~4 ticks) later.
            if (player.isLocalPlayer()) {
                ((FireTouchTracker) entity).neatlybetter$predictFireKind(soul, entity.level().getGameTime());
            }
            return;
        }
        ((FireTouchTracker) entity).neatlybetter$markFireTouch(soul, entity.level().getGameTime());
        // Set on touch, burning or not: players stand in fire about a second before they ignite, and clients show the
        // fire as soon as it starts, so the kind must already be right by then. The fire touched last wins.
        setKind(entity, soul);
    }

    /** Server. Called before the fire ticks are raised by an ignition. */
    public static void onIgnite(Entity entity) {
        if (!(entity instanceof Player) || entity.level().isClientSide()) return;
        FireTouchTracker touch = (FireTouchTracker) entity;
        boolean fromFireBlock = touch.neatlybetter$touchedFireAt(entity.level().getGameTime());
        setKind(entity, fromFireBlock && touch.neatlybetter$lastTouchWasSoul());
    }

    /** Writes (and syncs) only on change. */
    private static void setKind(Entity entity, boolean soul) {
        if (entity.getAttachedOrElse(ModData.SOUL_FIRE, false) == soul) return;
        if (soul) entity.setAttached(ModData.SOUL_FIRE, true);
        else entity.removeAttached(ModData.SOUL_FIRE);
    }
}

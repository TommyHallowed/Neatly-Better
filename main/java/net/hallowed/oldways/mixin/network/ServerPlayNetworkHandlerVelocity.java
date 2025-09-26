package net.hallowed.oldways.mixin.network;

import net.hallowed.oldways.config.CommonConfigManager;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Loosens server movement checks to finite, sane caps when enabled in config:
 * - "moved quickly" caps (player, elytra, vehicle)
 * - "moved wrongly" epsilon
 */
@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerVelocity {

    /* --- Tunable finite caps --- */
    @Unique private static final float  PLAYER_MAX_SPEED_CAP   = 500.0F;   // was 100.0F
    @Unique private static final float  ELYTRA_MAX_SPEED_CAP   = 1200.0F;  // was 300.0F
    @Unique private static final double VEHICLE_MAX_SPEED_CAP  = 500.0D;   // was 100.0D
    @Unique private static final double MOVED_WRONGLY_EPSILON  = 5.0D;     // was 0.0625D

    @Unique
    private static boolean enabled() { return CommonConfigManager.velocityFix(); }

    /* ===== Player move: speed caps ===== */

    @ModifyConstant(method = "onPlayerMove", constant = @Constant(floatValue = 100.0F), require = 0)
    private float oldways$playerMaxSpeed(float original) {
        return enabled() ? PLAYER_MAX_SPEED_CAP : original;
    }

    @ModifyConstant(method = "onPlayerMove", constant = @Constant(floatValue = 300.0F), require = 0)
    private float oldways$elytraMaxSpeed(float original) {
        return enabled() ? ELYTRA_MAX_SPEED_CAP : original;
    }

    /* ===== Vehicle move: speed cap ===== */

    @ModifyConstant(method = "onVehicleMove", constant = @Constant(doubleValue = 100.0D), require = 0)
    private double oldways$vehicleMaxSpeed(double original) {
        return enabled() ? VEHICLE_MAX_SPEED_CAP : original;
    }

    /* ===== "Moved wrongly" epsilon (player + vehicle) ===== */

    @ModifyConstant(method = "onPlayerMove", constant = @Constant(doubleValue = 0.0625D), require = 0)
    private double oldways$playerEpsilon(double original) {
        return enabled() ? MOVED_WRONGLY_EPSILON : original;
    }

    @ModifyConstant(method = "onVehicleMove", constant = @Constant(doubleValue = 0.0625D), require = 0)
    private double oldways$vehicleEpsilon(double original) {
        return enabled() ? MOVED_WRONGLY_EPSILON : original;
    }
}

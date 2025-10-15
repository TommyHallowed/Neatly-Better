package net.hallowed.oldways.client.mixin.entity;

import net.hallowed.oldways.client.feature.stuckprojectile.StuckProjectilesState;
import net.hallowed.oldways.client.util.SettingsPrefs;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.WeakHashMap;

@Mixin(EntityRenderManager.class)
public class EntityRenderManagerMixin {
    @Unique private static final SettingsPrefs OW$prefs = SettingsPrefs.get();

    @Unique @SuppressWarnings("unused")
    private static final Map<EntityRenderState, Entity> OW$ENTITY_BY_STATE = new WeakHashMap<>();

    @Inject(
            method = "getAndUpdateRenderState(Lnet/minecraft/entity/Entity;F)Lnet/minecraft/client/render/entity/state/EntityRenderState;",
            at = @At("HEAD")
    )
    private <E extends Entity> void oldways$captureCounts(E e, float tickDelta, CallbackInfoReturnable<EntityRenderState> cir) {
        if (!OW$prefs.showStuckProjectiles) {
            StuckProjectilesState.ENTITY_ID = 0;
            StuckProjectilesState.ARROW_COUNT = 0;
            StuckProjectilesState.STINGER_COUNT = 0;
            return;
        }
        StuckProjectilesState.ENTITY_ID = e.getId();
        if (e instanceof LivingEntity living) {
            StuckProjectilesState.ARROW_COUNT   = living.getStuckArrowCount();
            StuckProjectilesState.STINGER_COUNT = living.getStingerCount();
        } else {
            StuckProjectilesState.ARROW_COUNT = 0;
            StuckProjectilesState.STINGER_COUNT = 0;
        }
    }

    @Inject(
            method = "getAndUpdateRenderState(Lnet/minecraft/entity/Entity;F)Lnet/minecraft/client/render/entity/state/EntityRenderState;",
            at = @At("RETURN")
    )
    private <E extends Entity> void oldways$mapState(E e, float tickDelta, CallbackInfoReturnable<EntityRenderState> cir) {
        EntityRenderState state = cir.getReturnValue();
        if (state != null) OW$ENTITY_BY_STATE.put(state, e);
    }

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;" +
                            "submitFire(Lnet/minecraft/client/util/math/MatrixStack;" +
                            "Lnet/minecraft/client/render/entity/state/EntityRenderState;" +
                            "Lorg/joml/Quaternionf;)V"
            )
    )
    private void oldways$redirectSubmitFire(
            OrderedRenderCommandQueue queue,
            MatrixStack matrices,
            EntityRenderState state,
            Quaternionf rotation
    ) {
        queue.submitFire(matrices, state, rotation);
    }
}

package net.hallowed.neatlybetter.client.mixin.render;

import net.hallowed.neatlybetter.client.feature.soulfire.SoulFireRendering;
import net.hallowed.neatlybetter.feature.combat.SoulFire;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererSoulFireMixin {

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void neatlybetter$extractSoulFire(Entity entity, EntityRenderState state, float partialTicks, CallbackInfo ci) {
        state.setData(SoulFireRendering.SOUL_FIRE,
                state.displayFireAnimation && SoulFire.isSoulBurning(entity) ? Boolean.TRUE : null);
    }
}

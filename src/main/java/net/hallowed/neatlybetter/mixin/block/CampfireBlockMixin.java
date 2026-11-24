package net.hallowed.neatlybetter.mixin.block;

import net.hallowed.neatlybetter.config.NTServerConfig;
import net.hallowed.neatlybetter.feature.combat.SoulFire;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CampfireBlock.class)
public abstract class CampfireBlockMixin {

    /** Lit campfires set entities on fire (config {@code campfires_ignite}); soul campfires with soul fire. */
    @Inject(method = "entityInside", at = @At("HEAD"), require = 0)
    private void neatlybetter$igniteOnTouch(BlockState state, Level level, BlockPos pos, Entity entity,
                                            InsideBlockEffectApplier effectApplier, boolean isPrecise, CallbackInfo ci) {
        if (!NTServerConfig.CONFIG.campfiresIgnite.get()) return;
        if (state.getValue(CampfireBlock.LIT) && !entity.fireImmune()) {
            SoulFire.touchedFire(entity, state.is(Blocks.SOUL_CAMPFIRE));
            entity.igniteForSeconds(3);
        }
    }
}

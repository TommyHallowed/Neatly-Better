package net.hallowed.neatlybetter.mixin.block;

import net.hallowed.neatlybetter.feature.combat.SoulFire;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.SoulFireBlock;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BaseFireBlock.class)
public abstract class BaseFireBlockMixin {

    /** The last fire block a player touches decides whether they burn with soul fire. */
    @Inject(method = "entityInside", at = @At("HEAD"))
    private void neatlybetter$trackFireKind(BlockState state, Level level, BlockPos pos, Entity entity,
                                            InsideBlockEffectApplier effectApplier, boolean isPrecise, CallbackInfo ci) {
        SoulFire.touchedFire(entity, (Object) this instanceof SoulFireBlock);
    }
}

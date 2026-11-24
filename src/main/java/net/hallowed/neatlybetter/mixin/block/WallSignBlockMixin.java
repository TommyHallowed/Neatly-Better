package net.hallowed.neatlybetter.mixin.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.hallowed.NeatlyBetter;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Adds the {@code invisible} property used by the invisible sign feature. Vanilla blockstate JSONs select by facing only. */
@Mixin(WallSignBlock.class)
public abstract class WallSignBlockMixin {

    @WrapOperation(
            method = "<init>",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/WallSignBlock;registerDefaultState(Lnet/minecraft/world/level/block/state/BlockState;)V")
    )
    private void neatlybetter$defaultVisible(WallSignBlock instance, BlockState state, Operation<Void> original) {
        original.call(instance, state.setValue(NeatlyBetter.INVISIBLE, false));
    }

    @Inject(method = "createBlockStateDefinition", at = @At("TAIL"))
    private void neatlybetter$addInvisibleProperty(StateDefinition.Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(NeatlyBetter.INVISIBLE);
    }
}

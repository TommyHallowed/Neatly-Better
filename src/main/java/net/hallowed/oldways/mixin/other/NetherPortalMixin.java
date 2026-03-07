package net.hallowed.oldways.mixin.other;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.portal.PortalShape;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PortalShape.class)
public class NetherPortalMixin {
    @Shadow
    @Final
    @Mutable
    private static BlockBehaviour.StatePredicate FRAME;

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void onClassInit(CallbackInfo ci) {
        FRAME = (state, world, pos) ->
                state.is(Blocks.OBSIDIAN) || state.is(Blocks.CRYING_OBSIDIAN);
    }
}

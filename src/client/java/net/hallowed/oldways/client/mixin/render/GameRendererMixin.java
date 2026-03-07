package net.hallowed.oldways.client.mixin.render;

import net.hallowed.oldways.client.util.GameRendererPickHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Shadow @Final private Minecraft minecraft;

    @Unique private boolean ow$attackHeld = false;

    @Inject(method = "pick", at = @At("HEAD"), cancellable = true)
    private void oldways$swingThroughWhenTargetingEntity(float tickProgress, CallbackInfo ci) {
        if (minecraft == null || minecraft.player == null || minecraft.level == null) return;

        final Entity camera = minecraft.getCameraEntity();
        if (camera == null) return;

        final double blockRange  = minecraft.player.blockInteractionRange();
        final double entityRange = minecraft.player.entityInteractionRange();

        // FIX: Replaced the old shadow method with the new 1.21 player method!
        final HitResult vanillaFront = minecraft.player.raycastHitResult(tickProgress, camera);

        final HitResult behindResult = GameRendererPickHelper.pickIgnoringOutlineOnly(camera, blockRange, entityRange, tickProgress);

        if (!(behindResult instanceof EntityHitResult ehr)) {
            return;
        }

        final boolean pressing = minecraft.options.keyAttack.isDown();
        if (pressing && !ow$attackHeld && vanillaFront instanceof BlockHitResult bhr) {
            try {
                BlockPos frontPos = bhr.getBlockPos();
                BlockState frontState = minecraft.level.getBlockState(frontPos);
                if (!frontState.is(Blocks.COBWEB)) {
                    tryBreakIfOneHit(bhr);
                }
            } catch (Throwable ignored) {
                tryBreakIfOneHit(bhr);
            }
        }
        ow$attackHeld = pressing;

        minecraft.hitResult = behindResult;
        minecraft.crosshairPickEntity  = ehr.getEntity();

        ci.cancel();
    }

    @Unique
    private void tryBreakIfOneHit(BlockHitResult bhr) {
        if (minecraft.gameMode == null || minecraft.player == null || minecraft.level == null) return;

        final Level world = minecraft.level;
        final BlockPos pos = bhr.getBlockPos();
        final BlockState state = world.getBlockState(pos);
        if (state.isAir()) return;

        if (state.getDestroyProgress(minecraft.player, world, pos) >= 1.0F) {
            minecraft.gameMode.startDestroyBlock(pos, bhr.getDirection());
        }
    }
}
package net.hallowed.oldways.client.mixin.render;

import net.hallowed.oldways.client.util.GameRendererPickHelper;
import net.hallowed.oldways.client.util.SwingThroughGrassClient;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Shadow @Final private MinecraftClient client;

    @Shadow
    private HitResult findCrosshairTarget(Entity camera, double blockRange, double entityRange, float tickProgress) { return null; }

    @Unique private boolean ow$attackHeld = false;

    @Inject(method = "updateCrosshairTarget", at = @At("HEAD"), cancellable = true)
    private void oldways$swingThroughWhenTargetingEntity(float tickProgress, CallbackInfo ci) {
        if (!SwingThroughGrassClient.enabled()) return;
        if (client == null || client.player == null || client.world == null) return;

        final Entity camera = client.getCameraEntity();
        if (camera == null) return;

        final double blockRange  = client.player.getBlockInteractionRange();
        final double entityRange = client.player.getEntityInteractionRange();

        final HitResult vanillaFront = this.findCrosshairTarget(camera, blockRange, entityRange, tickProgress);

        final HitResult behindResult = GameRendererPickHelper.pickIgnoringOutlineOnly(camera, blockRange, entityRange, tickProgress);

        if (!(behindResult instanceof EntityHitResult ehr)) {
            return;
        }

        final boolean pressing = client.options.attackKey.isPressed();
        if (pressing && !ow$attackHeld && vanillaFront instanceof BlockHitResult bhr) {
            try {
                BlockPos frontPos = bhr.getBlockPos();
                BlockState frontState = client.world.getBlockState(frontPos);
                if (!frontState.isOf(Blocks.COBWEB)) {
                    tryBreakIfOneHit(bhr);
                }
            } catch (Throwable ignored) {
                tryBreakIfOneHit(bhr);
            }
        }
        ow$attackHeld = pressing;

        client.crosshairTarget = behindResult;
        client.targetedEntity  = ehr.getEntity();

        ci.cancel();
    }

    @Unique
    private void tryBreakIfOneHit(BlockHitResult bhr) {
        if (client.interactionManager == null || client.player == null || client.world == null) return;

        final World world = client.world;
        final BlockPos pos = bhr.getBlockPos();
        final BlockState state = world.getBlockState(pos);
        if (state.isAir()) return;

        if (state.calcBlockBreakingDelta(client.player, world, pos) >= 1.0F) {
            client.interactionManager.attackBlock(pos, bhr.getSide());
        }
    }
}

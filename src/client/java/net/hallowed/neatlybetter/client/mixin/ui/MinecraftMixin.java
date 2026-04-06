package net.hallowed.neatlybetter.client.mixin.ui;

import net.hallowed.neatlybetter.client.config.NTClientConfig;

import net.hallowed.neatlybetter.client.util.GameRendererPickHelper;
import net.minecraft.client.Minecraft;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Unique
    private boolean neatlybetter$attackHeld = false;

    @Inject(method = "pick", at = @At("HEAD"), cancellable = true)
    private void neatlybetter$swingThroughWhenTargetingEntity(float partialTicks, CallbackInfo ci) {
        if (!NTClientConfig.CONFIG.swingThrough.get()) return;

        final Minecraft minecraft = (Minecraft)(Object)this;
        if (minecraft.player == null || minecraft.level == null) return;

        final Entity camera = minecraft.getCameraEntity();
        if (camera == null) return;

        final double blockRange  = minecraft.player.blockInteractionRange();
        final double entityRange = minecraft.player.entityInteractionRange();

        final HitResult vanillaFront = minecraft.player.raycastHitResult(partialTicks, camera);

        final HitResult behindResult = GameRendererPickHelper.pickIgnoringOutlineOnly(camera, blockRange, entityRange, partialTicks);

        if (!(behindResult instanceof EntityHitResult ehr)) {
            return;
        }

        final boolean pressing = minecraft.options.keyAttack.isDown();
        if (pressing && !neatlybetter$attackHeld && vanillaFront instanceof BlockHitResult bhr) {
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
        neatlybetter$attackHeld = pressing;

        minecraft.hitResult = behindResult;
        minecraft.crosshairPickEntity  = ehr.getEntity();

        ci.cancel();
    }

    @Unique
    private void tryBreakIfOneHit(BlockHitResult bhr) {
        final Minecraft minecraft = (Minecraft)(Object)this;
        if (minecraft.gameMode == null || minecraft.player == null || minecraft.level == null) return;

        final Level world = minecraft.level;
        final BlockPos pos = bhr.getBlockPos();
        final BlockState state = world.getBlockState(pos);
        if (state.isAir()) return;

        if (state.getDestroyProgress(minecraft.player, world, pos) >= 1.0F) {
            minecraft.gameMode.startDestroyBlock(pos, bhr.getDirection());
        }
    }

    @Inject(method = "allowsTelemetry", at = @At("HEAD"), cancellable = true)
    private void neatlybetter$isTelemetryEnabledByApi(CallbackInfoReturnable<Boolean> cir) {
        if (!NTClientConfig.CONFIG.telemetryOff.get()) return;
        cir.setReturnValue(false);
    }
}
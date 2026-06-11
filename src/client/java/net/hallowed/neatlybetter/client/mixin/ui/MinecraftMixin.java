package net.hallowed.neatlybetter.client.mixin.ui;

import net.hallowed.neatlybetter.client.config.NTClientConfig;

import net.hallowed.neatlybetter.client.util.ClickThroughState;
import net.hallowed.neatlybetter.client.util.GameRendererPickHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Shadow public HitResult hitResult;
    @Shadow public LocalPlayer player;
    @Shadow public ClientLevel level;

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

    @Unique
    private static final SoundSource[] SOURCES_TO_STOP = {
            SoundSource.RECORDS, SoundSource.WEATHER, SoundSource.BLOCKS,
            SoundSource.HOSTILE, SoundSource.NEUTRAL, SoundSource.PLAYERS,
            SoundSource.AMBIENT, SoundSource.VOICE, SoundSource.UI
    };

    @Redirect(
            method = "updateLevelInEngines(Lnet/minecraft/client/multiplayer/ClientLevel;Z)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/SoundManager;stop()V")
    )
    private void NeatlyBetter$RedirectSoundManagerStop(SoundManager instance) {
        for (SoundSource source : SOURCES_TO_STOP) {
            instance.stop(null, source);
        }
    }

    @Inject(method = "startUseItem", at = @At("HEAD"))
    private void neatlybetter$redirectHitResult(CallbackInfo ci) {
        ClickThroughState.isDyeOnSign = false;

        if (this.hitResult == null || this.player == null || this.level == null) return;

        if (this.hitResult.getType() == HitResult.Type.ENTITY
                && ((EntityHitResult) this.hitResult).getEntity() instanceof ItemFrame frame) {
            handleItemFrame(frame);
            return;
        }

        if (this.hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) this.hitResult;
            BlockPos blockPos = blockHit.getBlockPos();
            BlockState state = this.level.getBlockState(blockPos);

            if (state.getBlock() instanceof WallSignBlock) {
                handleWallSign(blockHit, blockPos, state);
            } else if (state.getBlock() instanceof WallBannerBlock) {
                handleWallBanner(blockHit, blockPos, state);
            }
        }
    }

    @Unique
    private void handleItemFrame(ItemFrame frame) {
        BlockPos attachedPos = frame.getPos().relative(frame.getDirection().getOpposite());

        if (!this.player.isSecondaryUseActive() && isClickableBlockAt(attachedPos)) {
            this.hitResult = new BlockHitResult(
                    this.hitResult.getLocation(),
                    frame.getDirection(),
                    attachedPos,
                    false
            );
        }
    }

    @Unique
    private void handleWallSign(BlockHitResult blockHit, BlockPos blockPos, BlockState state) {
        Direction facing = state.getValue(WallSignBlock.FACING);
        BlockPos attachedPos = blockPos.relative(facing.getOpposite());

        if (!isClickableBlockAt(attachedPos)) return;

        BlockEntity be = this.level.getBlockEntity(blockPos);
        if (!(be instanceof SignBlockEntity)) return;

        ItemStack heldMain = this.player.getItemInHand(InteractionHand.MAIN_HAND);
        if (heldMain.getItem() instanceof DyeItem) {
                if (this.player.isSecondaryUseActive()) {
                    ClickThroughState.isDyeOnSign = true;
                } else {
                    this.hitResult = new BlockHitResult(
                            blockHit.getLocation(), blockHit.getDirection(), attachedPos, false);
                }
        } else if (!this.player.isSecondaryUseActive()) {
            this.hitResult = new BlockHitResult(
                    blockHit.getLocation(), blockHit.getDirection(), attachedPos, false);
        }
    }

    @Unique
    private void handleWallBanner(BlockHitResult blockHit, BlockPos blockPos, BlockState state) {
        Direction facing = state.getValue(WallBannerBlock.FACING);
        BlockPos attachedPos = blockPos.relative(facing.getOpposite());

        if (!this.player.isSecondaryUseActive() && isClickableBlockAt(attachedPos)) {
            this.hitResult = new BlockHitResult(
                    blockHit.getLocation(), blockHit.getDirection(), attachedPos, false);
        }
    }

    @Unique
    private boolean isClickableBlockAt(BlockPos pos) {
        BlockEntity entity = this.level.getBlockEntity(pos);
        return entity instanceof Container;
    }
}
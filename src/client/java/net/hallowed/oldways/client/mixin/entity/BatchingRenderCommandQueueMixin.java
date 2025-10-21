package net.hallowed.oldways.client.mixin.entity;

import net.hallowed.oldways.client.accessor.EntityRenderStateAccessor;
import net.hallowed.oldways.client.accessor.ClientPlayerEntityAccessor;
import net.hallowed.oldways.util.FireShapeUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.command.BatchingRenderCommandQueue;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.util.math.MatrixStack;

@Mixin(BatchingRenderCommandQueue.class)
public class BatchingRenderCommandQueueMixin {

    @Inject(method = "submitFire", at = @At("HEAD"))
    private void oldways$flagSoulFire(MatrixStack matrices, EntityRenderState renderState, Quaternionf rotation, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.world == null) return;

        try {
            // Use the entity render state's width/height to build a bounding-box-like region around the entity
            double halfWidth = renderState.width * 0.5D;
            double expand = 0.2D; // small extra margin to detect side contact immediately
            double minX = renderState.x - halfWidth - expand;
            double maxX = renderState.x + halfWidth + expand;
            double minY = renderState.y - expand;
            double maxY = renderState.y + renderState.height + expand;
            double minZ = renderState.z - halfWidth - expand;
            double maxZ = renderState.z + halfWidth + expand;

            int x0 = (int)Math.floor(minX);
            int x1 = (int)Math.floor(maxX);
            int y0 = (int)Math.floor(minY);
            int y1 = (int)Math.floor(maxY);
            int z0 = (int)Math.floor(minZ);
            int z1 = (int)Math.floor(maxZ);

            Box entityBox = new Box(minX, minY, minZ, maxX, maxY, maxZ);

            boolean atSoul = false;
            boolean atFire = false;
            boolean atLava = false;
            for (int xi = x0; xi <= x1 && !(atSoul && atFire && atLava); xi++) {
                for (int yi = y0; yi <= y1 && !(atSoul && atFire && atLava); yi++) {
                    for (int zi = z0; zi <= z1 && !(atSoul && atFire && atLava); zi++) {
                        BlockPos checkPos = new BlockPos(xi, yi, zi);
                        try {
                            BlockState bs = client.world.getBlockState(checkPos);
                            if (bs.isOf(Blocks.SOUL_FIRE) && FireShapeUtils.outlineIntersectsEntity(bs, client.world, checkPos, entityBox)) {
                                atSoul = true;
                            } else if (bs.isOf(Blocks.FIRE) && FireShapeUtils.outlineIntersectsEntity(bs, client.world, checkPos, entityBox)) {
                                atFire = true;
                            } else if (bs.isOf(Blocks.LAVA) && FireShapeUtils.outlineIntersectsEntity(bs, client.world, checkPos, entityBox)) {
                                atLava = true;
                            }
                        } catch (Throwable ignored) {
                        }
                    }
                }
            }

            if (atLava) {
                // If lava is present, it's lava-caused fire; do not force soul/normal fire flags on the render state.
                return;
            }

            if (atSoul) {
                // on soul fire -> immediately mark as soul on the render state
                if (renderState instanceof EntityRenderStateAccessor a) a.oldways$setSoulFire(true);
            } else if (atFire) {
                // on regular fire -> immediately mark as normal on the render state
                if (renderState instanceof EntityRenderStateAccessor a) a.oldways$setSoulFire(false);
            } else {
                // not touching any fire: leave renderState as-is (it should have been filled from the entity's tracked data earlier)
                // but preserve local-player remembered flag if this render state appears to be the player
                try {
                    ClientPlayerEntity player = client.player;
                    if (player != null) {
                        double dx = Math.abs(renderState.x - player.getX());
                        double dy = Math.abs(renderState.y - player.getY());
                        double dz = Math.abs(renderState.z - player.getZ());
                        double widthDiff = Math.abs(renderState.width - player.getWidth());
                        double heightDiff = Math.abs(renderState.height - player.getHeight());

                        if (dx < 0.6D && dy < 1.0D && dz < 0.6D && widthDiff < 0.5D && heightDiff < 0.6D) {
                            if (player instanceof ClientPlayerEntityAccessor acc && renderState instanceof EntityRenderStateAccessor ers) {
                                ers.oldways$setSoulFire(acc.oldways$isSoulFire());
                            }
                        }
                    }
                } catch (Throwable ignored) {
                }
            }
        } catch (Throwable ignored) {
        }
    }
}

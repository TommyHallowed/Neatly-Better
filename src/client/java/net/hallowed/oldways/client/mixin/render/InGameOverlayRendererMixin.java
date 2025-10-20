package net.hallowed.oldways.client.mixin.render;

import net.hallowed.oldways.client.accessor.ClientPlayerEntityAccessor;
import net.hallowed.oldways.client.render.SoulFireSprites;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteHolder;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.render.model.ModelBaker;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(InGameOverlayRenderer.class)
public class InGameOverlayRendererMixin {

    @Redirect(method = "renderOverlays(ZFLnet/minecraft/client/render/command/OrderedRenderCommandQueue;)V",
              at = @At(value = "INVOKE", target = "Lnet/minecraft/client/texture/SpriteHolder;getSprite(Lnet/minecraft/client/util/SpriteIdentifier;)Lnet/minecraft/client/texture/Sprite;"))
    private Sprite oldways$replaceFireSprite(SpriteHolder holder, SpriteIdentifier id) {
        try {
            // handle both FIRE_0 and FIRE_1 so the overlay doesn't alternate between soul/normal frames
            if (id.equals(ModelBaker.FIRE_0) || id.equals(ModelBaker.FIRE_1)) {
                 MinecraftClient client = MinecraftClient.getInstance();
                 if (client != null && client.player != null && client.world != null) {
                     // check all block positions overlapping the player's bounding box (precise contact)
                     var bbox = client.player.getBoundingBox();
                     int x0 = (int)Math.floor(bbox.minX - 0.001D);
                     int x1 = (int)Math.floor(bbox.maxX + 0.001D);
                     int y0 = (int)Math.floor(bbox.minY - 0.001D);
                     int y1 = (int)Math.floor(bbox.maxY + 0.001D);
                     int z0 = (int)Math.floor(bbox.minZ - 0.001D);
                     int z1 = (int)Math.floor(bbox.maxZ + 0.001D);

                     boolean atSoul = false;
                     boolean atFire = false;
                     for (int xi = x0; xi <= x1 && !(atSoul && atFire); xi++) {
                         for (int yi = y0; yi <= y1 && !(atSoul && atFire); yi++) {
                             for (int zi = z0; zi <= z1 && !(atSoul && atFire); zi++) {
                                 BlockPos checkPos = new BlockPos(xi, yi, zi);
                                 try {
                                     if (client.world.getBlockState(checkPos).isOf(Blocks.SOUL_FIRE)) {
                                         atSoul = true;
                                     } else if (client.world.getBlockState(checkPos).isOf(Blocks.FIRE)) {
                                         atFire = true;
                                     }
                                 } catch (Throwable ignored) {
                                 }
                             }
                         }
                     }

                     try {
                         if (client.player instanceof ClientPlayerEntityAccessor acc) {
                            if (atSoul) {
                                acc.oldways$setSoulFire(true);
                                return holder.getSprite(id.equals(ModelBaker.FIRE_0) ? SoulFireSprites.FIRE_SOUL_0 : SoulFireSprites.FIRE_SOUL_1);
                            } else if (atFire) {
                                acc.oldways$setSoulFire(false);
                                return holder.getSprite(id);
                            } else {
                                // not touching any fire -> consult remembered value
                                if (acc.oldways$isSoulFire()) {
                                    return holder.getSprite(id.equals(ModelBaker.FIRE_0) ? SoulFireSprites.FIRE_SOUL_0 : SoulFireSprites.FIRE_SOUL_1);
                                }
                                // fall through to default
                            }
                         }
                     } catch (Throwable ignored) {
                     }
                 }
            }
         } catch (Throwable ignored) {
         }
         return holder.getSprite(id);
     }
 }

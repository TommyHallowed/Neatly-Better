package net.hallowed.neatlybetter.feature.building;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Right-clicking the top of a rail with a rail places the new one a block ahead (the way the player
 * is facing) and one up, or one down while sneaking. Side faces need no handling here: vanilla
 * placement already works once BaseRailBlockMixin lets chained rails survive.
 */
public class FloatingRailExtender {

    public static void register() {
        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            if (hitResult.getDirection() != Direction.UP) return InteractionResult.PASS;

            ItemStack stack = player.getItemInHand(hand);
            if (!(stack.getItem() instanceof BlockItem item) || !(item.getBlock() instanceof BaseRailBlock)) return InteractionResult.PASS;
            if (!BaseRailBlock.isRail(level.getBlockState(hitResult.getBlockPos()))) return InteractionResult.PASS;

            BlockPos target = hitResult.getBlockPos()
                    .relative(player.getDirection())
                    .above(player.isSecondaryUseActive() ? -1 : 1);
            if (level.isOutsideBuildHeight(target) || !level.getBlockState(target).canBeReplaced()) return InteractionResult.PASS;

            BlockHitResult placeHit = new BlockHitResult(Vec3.atCenterOf(target), Direction.UP, target, false);
            return stack.useOn(new UseOnContext(player, hand, placeHit));
        });
    }
}
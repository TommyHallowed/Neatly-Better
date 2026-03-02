package net.hallowed.oldways.content.feature;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;

public final class DirtToGrassWithSeeds {
    private DirtToGrassWithSeeds() {}

    public static void init() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient()) return ActionResult.PASS;

            ItemStack stack = player.getStackInHand(hand);
            if (!stack.isOf(Items.WHEAT_SEEDS)) return ActionResult.PASS;

            BlockPos pos = hitResult.getBlockPos();
            BlockState state = world.getBlockState(pos);

            if (state.isOf(Blocks.DIRT) || state.isOf(Blocks.COARSE_DIRT) || state.isOf(Blocks.ROOTED_DIRT)) {
                world.setBlockState(pos, Blocks.GRASS_BLOCK.getDefaultState());
                    stack.decrementUnlessCreative(1, player);

                world.playSound(
                        null,
                        pos,
                        SoundEvents.ITEM_CROP_PLANT,
                        SoundCategory.BLOCKS,
                        1.0f,
                        1.0f + (world.getRandom().nextFloat() - 0.5f) * 0.2f
                );

                return ActionResult.SUCCESS_SERVER;
            }

            return ActionResult.PASS;
        });
    }
}

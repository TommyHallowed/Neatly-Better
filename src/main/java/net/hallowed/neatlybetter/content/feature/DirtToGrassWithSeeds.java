package net.hallowed.neatlybetter.content.feature;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class DirtToGrassWithSeeds {
    private DirtToGrassWithSeeds() {}

    public static void init() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClientSide()) return InteractionResult.PASS;

            ItemStack stack = player.getItemInHand(hand);
            if (!stack.is(Items.WHEAT_SEEDS)) return InteractionResult.PASS;

            BlockPos pos = hitResult.getBlockPos();
            BlockState state = world.getBlockState(pos);

            if (state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT) || state.is(Blocks.ROOTED_DIRT)) {
                world.setBlockAndUpdate(pos, Blocks.GRASS_BLOCK.defaultBlockState());
                    stack.consume(1, player);

                world.playSound(
                        null,
                        pos,
                        SoundEvents.CROP_PLANTED,
                        SoundSource.BLOCKS,
                        1.0f,
                        1.0f + (world.getRandom().nextFloat() - 0.5f) * 0.2f
                );

                return InteractionResult.SUCCESS_SERVER;
            }

            return InteractionResult.PASS;
        });
    }
}

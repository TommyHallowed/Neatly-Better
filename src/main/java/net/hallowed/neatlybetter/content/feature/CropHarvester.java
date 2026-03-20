package net.hallowed.neatlybetter.content.feature;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;

import net.hallowed.neatlybetter.api.NTCompat;
import net.hallowed.neatlybetter.config.NTServerConfig;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

public class CropHarvester {

    public static void register() {
        UseBlockCallback.EVENT.register(CropHarvester::onBlockInteract);
    }

    private static InteractionResult onBlockInteract(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
        if (!NTServerConfig.CONFIG.cropHarvester.get()) return InteractionResult.PASS;
        if (NTCompat.RIGHTCLICKHARVEST || NTCompat.REAP) return InteractionResult.PASS;
        if (player.isSpectator()) return InteractionResult.PASS;

        BlockPos pos = hitResult.getBlockPos();
        BlockState state = level.getBlockState(pos);

        if (!isMature(state)) {
            return InteractionResult.PASS;
        }

        ItemStack tool = player.getItemInHand(hand);
        boolean isHoe = tool.getItem() instanceof HoeItem;

        player.swing(hand, true);

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ServerLevel serverLevel = (ServerLevel) level;
        EquipmentSlot slot = hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;

        if (isHoe) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos targetPos = pos.offset(x, 0, z);
                    BlockState targetState = serverLevel.getBlockState(targetPos);

                    if (isMature(targetState)) {
                        harvestAndReplant(serverLevel, targetPos, targetState, player, tool, slot);
                    }
                }
            }
        } else {
            harvestAndReplant(serverLevel, pos, state, player, tool, slot);
        }

        return InteractionResult.SUCCESS;
    }

    private static boolean isMature(BlockState state) {
        if (state.getBlock() instanceof CropBlock crop) {
            return crop.isMaxAge(state);
        }
        if (state.getBlock() instanceof NetherWartBlock) {
            return state.getValue(NetherWartBlock.AGE) >= NetherWartBlock.MAX_AGE; // Max age for Nether Warts is 3
        }
        return false;
    }

    private static void harvestAndReplant(ServerLevel level, BlockPos pos, BlockState state, Player player, ItemStack tool, EquipmentSlot slot) {
        Block block = state.getBlock();

        List<ItemStack> drops = Block.getDrops(state, level, pos, null, player, tool);

        Item seedItem = getSeedItem(block);
        boolean deducted = false;

        for (ItemStack drop : drops) {
            if (!deducted && drop.is(seedItem)) {
                drop.shrink(1);
                deducted = true;
            }

            if (!drop.isEmpty()) {
                Block.popResource(level, pos, drop);
            }
        }

        level.playSound(null, pos, state.getSoundType().getBreakSound(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.0f);

        level.setBlockAndUpdate(pos, block.defaultBlockState());

        if (tool.getItem() instanceof HoeItem) {
            tool.hurtAndBreak(1, player, slot);
        }
    }

    private static Item getSeedItem(Block block) {
        if (block == Blocks.WHEAT) return Items.WHEAT_SEEDS;
        if (block == Blocks.CARROTS) return Items.CARROT;
        if (block == Blocks.POTATOES) return Items.POTATO;
        if (block == Blocks.BEETROOTS) return Items.BEETROOT_SEEDS;
        if (block == Blocks.NETHER_WART) return Items.NETHER_WART;
        if (block == Blocks.TORCHFLOWER_CROP) return Items.TORCHFLOWER_SEEDS;
        if (block == Blocks.PITCHER_CROP) return Items.PITCHER_POD;

        return null;
    }
}
package net.hallowed.neatlybetter.content.feature;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class DeathChestHandler {

    private DeathChestHandler() {}

    public static void spawnDeathChest(Player player, ServerLevel level) {
        Inventory inv = player.getInventory();
        String playerName = player.getScoreboardName();
        UUID playerUUID = player.getUUID();

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && EnchantmentHelper.has(stack, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)) {
                inv.removeItemNoUpdate(i);
            }
        }

        List<ItemStack> rawItems = new ArrayList<>();

        for (int i = 0; i <= 8; i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) rawItems.add(stack.copy());
        }

        ItemStack offhand = inv.getItem(40);
        if (!offhand.isEmpty()) rawItems.add(offhand.copy());

        for (int i = 39; i >= 36; i--) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) rawItems.add(stack.copy());
        }

        for (int i = 9; i <= 35; i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) rawItems.add(stack.copy());
        }

        inv.clearContent();

        List<ItemStack> chestItems = compactItems(rawItems);

        if (chestItems.isEmpty()) return;

        BlockPos chestPos = findSafePosition(level, player.blockPosition());

        Direction chestFacing = player.getDirection();

        boolean needsDouble = chestItems.size() > 27;
        DoubleChestPlacement placement = null;

        if (needsDouble) {
            placement = findDoubleChestPlacement(level, chestPos, chestFacing);
        }

        BlockPos mainPos;
        if (placement != null) {
            placeDoubleChest(level, placement, playerName, chestItems);
            mainPos = placement.leftPos;
        } else {
            placeSingleChest(level, chestPos, chestFacing, playerName, chestItems);
            mainPos = chestPos;
        }

        placeSign(level, mainPos, chestFacing, playerName);

        ChestLockHandler.lockContainerForPlayer(level, mainPos, playerUUID, playerName);
    }

    private static List<ItemStack> compactItems(List<ItemStack> items) {
        List<ItemStack> result = new ArrayList<>();

        outer:
        for (ItemStack stack : items) {
            if (stack.isEmpty()) continue;
            ItemStack remaining = stack.copy();

            for (ItemStack existing : result) {
                if (existing.getCount() < existing.getMaxStackSize()
                        && ItemStack.isSameItemSameComponents(existing, remaining)) {
                    int space    = existing.getMaxStackSize() - existing.getCount();
                    int transfer = Math.min(space, remaining.getCount());
                    existing.grow(transfer);
                    remaining.shrink(transfer);
                    if (remaining.isEmpty()) continue outer;
                }
            }

            while (!remaining.isEmpty()) {
                int amount = Math.min(remaining.getMaxStackSize(), remaining.getCount());
                result.add(remaining.copyWithCount(amount));
                remaining.shrink(amount);
            }
        }

        return result;
    }

    private static void placeSingleChest(ServerLevel level, BlockPos pos, Direction facing,
                                         String ownerName, List<ItemStack> items) {
        BlockState state = Blocks.CHEST.defaultBlockState()
                .setValue(ChestBlock.FACING, facing)
                .setValue(ChestBlock.TYPE, ChestType.SINGLE);
        level.setBlock(pos, state, Block.UPDATE_ALL);

        if (level.getBlockEntity(pos) instanceof ChestBlockEntity chest) {
            chest.setComponents(DataComponentMap.builder()
                    .set(DataComponents.CUSTOM_NAME, Component.literal(ownerName + "'s Death Chest"))
                    .build());
            for (int i = 0; i < Math.min(27, items.size()); i++) {
                chest.setItem(i, items.get(i));
            }
        }

        for (int i = 27; i < items.size(); i++) {
            Block.popResource(level, pos, items.get(i));
        }
    }

    private static void placeDoubleChest(ServerLevel level, DoubleChestPlacement placement,
                                         String ownerName, List<ItemStack> items) {
        BlockState leftState = Blocks.CHEST.defaultBlockState()
                .setValue(ChestBlock.FACING, placement.facing)
                .setValue(ChestBlock.TYPE, ChestType.LEFT);

        BlockState rightState = Blocks.CHEST.defaultBlockState()
                .setValue(ChestBlock.FACING, placement.facing)
                .setValue(ChestBlock.TYPE, ChestType.RIGHT);

        level.setBlock(placement.leftPos, leftState, Block.UPDATE_ALL);
        level.setBlock(placement.rightPos, rightState, Block.UPDATE_ALL);

        if (level.getBlockEntity(placement.rightPos) instanceof ChestBlockEntity right) {
            right.setComponents(DataComponentMap.builder()
                    .set(DataComponents.CUSTOM_NAME, Component.literal(ownerName + "'s Death Chest"))
                    .build());
            for (int i = 0; i < Math.min(27, items.size()); i++) {
                right.setItem(i, items.get(i));
            }
        }

        if (level.getBlockEntity(placement.leftPos) instanceof ChestBlockEntity left) {
            left.setComponents(DataComponentMap.builder()
                    .set(DataComponents.CUSTOM_NAME, Component.literal(ownerName + "'s Death Chest"))
                    .build());
            for (int i = 27; i < Math.min(54, items.size()); i++) {
                left.setItem(i - 27, items.get(i));
            }
        }

        for (int i = 54; i < items.size(); i++) {
            Block.popResource(level, placement.leftPos, items.get(i));
        }
    }

    private static void placeSign(ServerLevel level, BlockPos chestPos,
                                  Direction chestFacing, String playerName) {
        BlockPos signPos = chestPos.relative(chestFacing);
        if (!level.getBlockState(signPos).isAir()) return;

        BlockState signState = Blocks.OAK_WALL_SIGN.defaultBlockState()
                .setValue(WallSignBlock.FACING, chestFacing);
        level.setBlock(signPos, signState, Block.UPDATE_ALL);

        if (level.getBlockEntity(signPos) instanceof SignBlockEntity sign) {
            SignText newText = sign.getFrontText()
                    .setMessage(1, Component.literal(playerName));
            sign.setText(newText, true);
        }
    }

    private static BlockPos findSafePosition(ServerLevel level, BlockPos deathPos) {
        int minY = level.getMinY() + 1;
        int maxY = level.getMaxY() - 2;

        int y = Math.max(minY, Math.min(maxY, deathPos.getY()));
        BlockPos pos = new BlockPos(deathPos.getX(), y, deathPos.getZ());

        if (canPlaceAt(level, pos)) {
            for (int dy = 1; dy <= 20; dy++) {
                BlockPos below = pos.below(dy);
                if (below.getY() < minY) break;
                if (!canPlaceAt(level, below)) {
                    return below.above();
                }
            }
            return pos;
        }

        for (int dy = 1; dy <= 15; dy++) {
            BlockPos up = pos.above(dy);
            if (up.getY() > maxY) break;
            if (canPlaceAt(level, up)) return up;
        }

        for (int dy = 1; dy <= 15; dy++) {
            BlockPos down = pos.below(dy);
            if (down.getY() < minY) break;
            if (canPlaceAt(level, down)) return down;
        }

        return pos;
    }

    private static boolean canPlaceAt(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir() || !state.getFluidState().isEmpty();
    }

    private record DoubleChestPlacement(BlockPos leftPos, BlockPos rightPos, Direction facing) {}

    private static DoubleChestPlacement findDoubleChestPlacement(
            ServerLevel level, BlockPos primary, Direction facing) {

        BlockPos ccwPos = primary.relative(facing.getCounterClockWise());
        if (canPlaceAt(level, ccwPos)) {
            return new DoubleChestPlacement(ccwPos, primary, facing);
        }

        BlockPos cwPos = primary.relative(facing.getClockWise());
        if (canPlaceAt(level, cwPos)) {
            return new DoubleChestPlacement(primary, cwPos, facing);
        }

        return null;
    }
}

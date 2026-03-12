package net.hallowed.oldways.content.feature;

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

/**
 * Handles the death chest feature: spawning locked chests with the player's
 * entire inventory on death.
 *
 * <p>All locking is delegated to {@link ChestLockHandler} via its
 * {@link ChestLockHandler#lockContainerForPlayer} API — the same system used
 * for the Chest Lock item. This means death chests and manually locked chests
 * share identical access control, break protection, and data persistence.</p>
 *
 * <p>Called from the death mixin when the death chest gamerule is enabled.
 * No event registration needed — all events are handled by ChestLockHandler.</p>
 */
public final class DeathChestHandler {

    private DeathChestHandler() {}

    // ── Death Chest Spawning ────────────────────────────────────────────────────

    /**
     * Called from the mixin when a player dies with the death chest gamerule enabled.
     * Collects ALL inventory items (including armor), places a chest (or double chest),
     * and locks it via ChestLockHandler.
     */
    public static void spawnDeathChest(Player player, ServerLevel level) {
        Inventory inv = player.getInventory();
        String playerName = player.getScoreboardName();
        UUID playerUUID = player.getUUID();

        // Step 1: Destroy vanishing-cursed items (matches vanilla behavior)
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && EnchantmentHelper.has(stack, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)) {
                inv.removeItemNoUpdate(i);
            }
        }

        // Step 2: Collect ALL items (including armor) for the chest
        List<ItemStack> rawItems = new ArrayList<>();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) {
                rawItems.add(stack.copy());
            }
        }

        // Step 3: Clear inventory so vanilla drops nothing
        inv.clearContent();

        // Step 4: Compact items BEFORE deciding if a double chest is needed.
        // This merges partial stacks (e.g. 3×32 dirt → 1×64 + 1×32) to minimise
        // the number of slots used, avoiding an unnecessary second chest.
        List<ItemStack> chestItems = compactItems(rawItems);

        // Nothing to store — skip chest creation
        if (chestItems.isEmpty()) return;

        // Step 5: Find a safe position for the chest
        BlockPos chestPos = findSafePosition(level, player.blockPosition());

        // Step 6: Pick the facing direction (player's look direction)
        Direction chestFacing = player.getDirection();

        // Step 7: Place chest(s) and fill them
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

        // Step 8: Place a sign in front of the main chest with the player's name
        placeSign(level, mainPos, chestFacing, playerName);

        // Step 9: Lock via ChestLockHandler (unified lock system)
        // lockContainerForPlayer handles both halves of double chests automatically
        ChestLockHandler.lockContainerForPlayer(level, mainPos, playerUUID, playerName);
    }

    // ── Item Compaction ─────────────────────────────────────────────────────────

    /**
     * Merges partial stacks of identical items into full stacks,
     * reducing the number of inventory slots required in the chest.
     *
     * <p>Example: [32 dirt, 32 dirt, 48 dirt] → [64 dirt, 48 dirt] (3 slots → 2 slots)</p>
     */
    private static List<ItemStack> compactItems(List<ItemStack> items) {
        List<ItemStack> result = new ArrayList<>();

        outer:
        for (ItemStack stack : items) {
            if (stack.isEmpty()) continue;
            ItemStack remaining = stack.copy();

            // Try to top-up existing partial stacks of the same item
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

            // Add remainder as one or more new max-size stacks
            while (!remaining.isEmpty()) {
                int amount = Math.min(remaining.getMaxStackSize(), remaining.getCount());
                result.add(remaining.copyWithCount(amount));
                remaining.shrink(amount);
            }
        }

        return result;
    }

    // ── Chest Placement Helpers ─────────────────────────────────────────────────

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

        // Drop overflow as item entities (shouldn't happen after compaction for a normal inventory)
        for (int i = 27; i < items.size(); i++) {
            Block.popResource(level, pos, items.get(i));
        }
    }

    /**
     * Places both halves of a double chest with the correct ChestType so that
     * items 0-26 always appear in the TOP rows and items 27-53 in the BOTTOM rows.
     *
     * <p>In Minecraft's large-chest GUI, ChestType.LEFT is always rendered as the top
     * three rows regardless of which half the player opens. Therefore leftPos must
     * hold items 0-26 and rightPos must hold items 27-53.</p>
     */
    private static void placeDoubleChest(ServerLevel level, DoubleChestPlacement placement,
                                         String ownerName, List<ItemStack> items) {
        // LEFT chest = top rows in the large-chest GUI (items 0-26)
        BlockState leftState = Blocks.CHEST.defaultBlockState()
                .setValue(ChestBlock.FACING, placement.facing)
                .setValue(ChestBlock.TYPE, ChestType.LEFT);

        // RIGHT chest = bottom rows in the large-chest GUI (items 27-53)
        BlockState rightState = Blocks.CHEST.defaultBlockState()
                .setValue(ChestBlock.FACING, placement.facing)
                .setValue(ChestBlock.TYPE, ChestType.RIGHT);

        level.setBlock(placement.leftPos,  leftState,  Block.UPDATE_ALL);
        level.setBlock(placement.rightPos, rightState, Block.UPDATE_ALL);

        // Fill LEFT chest — top rows, first 27 items
        if (level.getBlockEntity(placement.leftPos) instanceof ChestBlockEntity left) {
            left.setComponents(DataComponentMap.builder()
                    .set(DataComponents.CUSTOM_NAME, Component.literal(ownerName + "'s Death Chest"))
                    .build());
            for (int i = 0; i < Math.min(27, items.size()); i++) {
                left.setItem(i, items.get(i));
            }
        }

        // Fill RIGHT chest — bottom rows, items 27-53
        if (level.getBlockEntity(placement.rightPos) instanceof ChestBlockEntity right) {
            right.setComponents(DataComponentMap.builder()
                    .set(DataComponents.CUSTOM_NAME, Component.literal(ownerName + "'s Death Chest"))
                    .build());
            for (int i = 27; i < Math.min(54, items.size()); i++) {
                right.setItem(i - 27, items.get(i));
            }
        }

        // Drop anything beyond 54 slots (shouldn't occur with a normal inventory)
        for (int i = 54; i < items.size(); i++) {
            Block.popResource(level, placement.leftPos, items.get(i));
        }
    }

    // ── Sign Placement ──────────────────────────────────────────────────────────

    /**
     * Places an oak wall sign one block in front of the chest (in the
     * chest's facing direction). The sign is attached to the chest's front face
     * and is readable by a player approaching from the front.
     * The player's name is written on the second line (index 1).
     *
     * <p>If the target position is not air the sign is silently skipped.</p>
     */
    private static void placeSign(ServerLevel level, BlockPos chestPos,
                                  Direction chestFacing, String playerName) {
        BlockPos signPos = chestPos.relative(chestFacing);
        if (!level.getBlockState(signPos).isAir()) return; // Don't overwrite anything

        BlockState signState = Blocks.OAK_WALL_SIGN.defaultBlockState()
                .setValue(WallSignBlock.FACING, chestFacing);
        level.setBlock(signPos, signState, Block.UPDATE_ALL);

        if (level.getBlockEntity(signPos) instanceof SignBlockEntity sign) {
            SignText newText = sign.getFrontText()
                    .setMessage(1, Component.literal(playerName));
            sign.setText(newText, true);
        }
    }

    // ── Position Finding ────────────────────────────────────────────────────────

    private static BlockPos findSafePosition(ServerLevel level, BlockPos deathPos) {
        int minY = level.getMinY() + 1;
        int maxY = level.getMaxY() - 2;

        // Clamp Y
        int y = Math.max(minY, Math.min(maxY, deathPos.getY()));
        BlockPos pos = new BlockPos(deathPos.getX(), y, deathPos.getZ());

        // If the position is replaceable, check if there's ground below
        if (canPlaceAt(level, pos)) {
            // Search downward for solid ground
            for (int dy = 1; dy <= 20; dy++) {
                BlockPos below = pos.below(dy);
                if (below.getY() < minY) break;
                if (!canPlaceAt(level, below)) {
                    return below.above(); // Place on top of the solid block
                }
            }
            return pos; // Floating — no ground found, place at death Y
        }

        // Position is solid — search upward for air
        for (int dy = 1; dy <= 15; dy++) {
            BlockPos up = pos.above(dy);
            if (up.getY() > maxY) break;
            if (canPlaceAt(level, up)) return up;
        }

        // Desperate: search downward
        for (int dy = 1; dy <= 15; dy++) {
            BlockPos down = pos.below(dy);
            if (down.getY() < minY) break;
            if (canPlaceAt(level, down)) return down;
        }

        return pos; // Absolute fallback
    }

    private static boolean canPlaceAt(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir() || !state.getFluidState().isEmpty();
    }

    // ── Double Chest Direction Logic ────────────────────────────────────────────

    /**
     * Tracks which block is the LEFT half (top rows, items 0-26)
     * and which is the RIGHT half (bottom rows, items 27-53).
     *
     * <p>Both halves share the same {@code facing} and will connect automatically
     * because their ChestType values match what Minecraft expects.</p>
     */
    private record DoubleChestPlacement(BlockPos leftPos, BlockPos rightPos, Direction facing) {}

    /**
     * Finds a valid adjacent position for the second chest half and returns the
     * correct LEFT/RIGHT assignment so that items 0-26 always land in the top rows.
     *
     * <p>Minecraft's rules (from {@code ChestBlock.getConnectedDirection}):</p>
     * <ul>
     *   <li>A LEFT chest's partner is at {@code pos + facing.getCounterClockWise()}</li>
     *   <li>A RIGHT chest's partner is at {@code pos + facing.getClockWise()}</li>
     * </ul>
     *
     * <p>Option A — secondary spot is counterclockwise of primary:
     *     primary becomes LEFT (top rows). ✓</p>
     * <p>Option B — secondary spot is clockwise of primary:
     *     the clockwise position becomes LEFT (top rows) and primary becomes RIGHT. ✓</p>
     */
    private static DoubleChestPlacement findDoubleChestPlacement(
            ServerLevel level, BlockPos primary, Direction facing) {

        // Option A: place second chest counterclockwise → primary = LEFT (top rows)
        BlockPos ccwPos = primary.relative(facing.getCounterClockWise());
        if (canPlaceAt(level, ccwPos)) {
            return new DoubleChestPlacement(primary, ccwPos, facing);
        }

        // Option B: place second chest clockwise → clockwise pos = LEFT (top rows)
        BlockPos cwPos = primary.relative(facing.getClockWise());
        if (canPlaceAt(level, cwPos)) {
            return new DoubleChestPlacement(cwPos, primary, facing);
        }

        return null; // No adjacent space found — fall back to single chest
    }
}

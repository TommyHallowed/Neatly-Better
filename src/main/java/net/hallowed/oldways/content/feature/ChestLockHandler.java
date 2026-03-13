package net.hallowed.oldways.content.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.hallowed.oldways.init.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Chest Lock feature using Fabric's Data Attachment API.
 * Owner data (UUID + name) is stored directly on the block entity — no SavedData,
 * no mixin, no access widener needed. Data persists automatically with the block entity.

 * UseBlockCallback intercepts all container interactions:
 *   - Shift + Right-Click with Chest Lock item → lock / unlock
 *   - Non-owner Right-Click → denied
 *   - Owner Right-Click → allowed (event returns PASS, vanilla opens the container)

 * PlayerBlockBreakEvents.BEFORE prevents non-owners from breaking locked containers.

 * Also exposes {@link #lockContainerForPlayer} for programmatic locking (e.g. death chests).
 */
@SuppressWarnings("UnstableApiUsage")
public class ChestLockHandler {

    // ══════════════════════════════════════════════════════════════════════
    //  Fabric Attachment — owner data stored on the block entity itself
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Stores the UUID and display name of the player who locked a container.
     * Serialized via Codec and persisted with the block entity's NBT automatically.
     */
    public record LockOwner(String uuid, String name) {
        public static final Codec<LockOwner> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.STRING.fieldOf("uuid").forGetter(LockOwner::uuid),
                        Codec.STRING.fieldOf("name").forGetter(LockOwner::name)
                ).apply(instance, LockOwner::new)
        );

        public UUID ownerUuid() {
            return UUID.fromString(uuid);
        }
    }

    /**
     * Persistent attachment type — Fabric serializes this into the block entity's
     * NBT automatically. No manual save/load code needed.

     * If createPersistent() doesn't compile, replace with the builder pattern:
     *   AttachmentRegistry.&lt;LockOwner&gt;builder()
     *       .persistent(LockOwner.CODEC)
     *       .buildAndRegister(Identifier.fromNamespaceAndPath("oldways", "lock_owner"));
     */
    public static final AttachmentType<@NotNull LockOwner> LOCK_OWNER = AttachmentRegistry.createPersistent(
            Identifier.fromNamespaceAndPath("oldways", "lock_owner"),
            LockOwner.CODEC
    );

    // ══════════════════════════════════════════════════════════════════════
    //  Registration
    // ══════════════════════════════════════════════════════════════════════

    public static void register() {
        UseBlockCallback.EVENT.register(ChestLockHandler::onUseBlock);
        PlayerBlockBreakEvents.BEFORE.register(ChestLockHandler::onBlockBreak);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Use Block — Lock / Unlock + Access Control
    // ══════════════════════════════════════════════════════════════════════

    private static InteractionResult onUseBlock(Player player, Level world,
                                                InteractionHand hand, BlockHitResult hitResult) {
        if (world.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }

        ServerLevel level = (ServerLevel) world;
        BlockPos pos = hitResult.getBlockPos();
        BlockEntity be = level.getBlockEntity(pos);

        if (!(be instanceof BaseContainerBlockEntity)) {
            return InteractionResult.PASS;
        }

        ItemStack heldItem = serverPlayer.getItemInHand(hand);
        boolean isSneaking = serverPlayer.isShiftKeyDown();
        boolean holdingLock = heldItem.is(ModItems.CHEST_KEY);

        // ── Shift + Right-Click with Lock Item → Toggle lock ──
        if (isSneaking && holdingLock) {
            return handleLockToggle(level, pos, serverPlayer, heldItem, hand);
        }

        // ── Normal Right-Click → Access Control ──
        LockOwner owner = getOwnerChecked(level, pos);
        if (owner == null) {
            return InteractionResult.PASS; // Not locked — vanilla handles it
        }

        if (owner.ownerUuid().equals(serverPlayer.getUUID())) {
            // Owner — let vanilla open the container
            return InteractionResult.PASS;
        } else {
            // Non-owner — denied
            serverPlayer.displayClientMessage(
                    Component.literal("This container is locked by " + owner.name() + "!")
                            .withStyle(ChatFormatting.RED),
                    true
            );
            level.playSound(null, pos, SoundEvents.CHEST_LOCKED, SoundSource.BLOCKS, 1.0f, 1.0f);
            return InteractionResult.FAIL;
        }
    }

    /**
     * Handles shift+right-click with the Chest Lock item: lock or unlock.
     * Plays sound feedback and swings the player's hand on success.
     */
    private static InteractionResult handleLockToggle(ServerLevel level, BlockPos pos,
                                                      ServerPlayer player, ItemStack heldItem,
                                                      InteractionHand hand) {
        LockOwner owner = getOwnerChecked(level, pos);

        if (owner == null) {
            // Not locked → lock it
            lockContainer(level, pos, player);
            player.displayClientMessage(
                    Component.literal("Container locked!").withStyle(ChatFormatting.GREEN),
                    true
            );
            if (!player.isCreative()) {
                heldItem.shrink(1);
            }
            // Sound + hand swing
            level.playSound(null, pos, SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 1.0f, 1.4f);
            player.swing(hand, true);
            return InteractionResult.SUCCESS;
        }

        if (owner.ownerUuid().equals(player.getUUID())) {
            // Owner → unlock it
            unlockContainer(level, pos);
            player.displayClientMessage(
                    Component.literal("Container unlocked!").withStyle(ChatFormatting.YELLOW),
                    true
            );
            // Give the lock item back
            if (!player.isCreative()) {
                ItemStack lockBack = new ItemStack(ModItems.CHEST_KEY);
                if (!player.getInventory().add(lockBack)) {
                    player.drop(lockBack, false);
                }
            }
            // Sound + hand swing
            level.playSound(null, pos, SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.BLOCKS, 1.0f, 1.4f);
            player.swing(hand, true);
            return InteractionResult.SUCCESS;
        }

        // Someone else's lock
        player.displayClientMessage(
                Component.literal("This container is locked by " + owner.name() + "!")
                        .withStyle(ChatFormatting.RED),
                true
        );
        return InteractionResult.FAIL;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Block Break Protection
    // ══════════════════════════════════════════════════════════════════════

    private static boolean onBlockBreak(Level world, Player player, BlockPos pos,
                                        BlockState state, BlockEntity blockEntity) {
        if (world.isClientSide() || !(blockEntity instanceof BaseContainerBlockEntity)) {
            return true; // Allow
        }

        LockOwner owner = getOwnerChecked(world, pos);
        if (owner == null) {
            return true; // Not locked
        }

        if (owner.ownerUuid().equals(player.getUUID())) {
            // Owner → unlock both halves (if double chest) and allow break
            unlockContainer((ServerLevel) world, pos);
            return true;
        }

        // Non-owner → deny
        if (player instanceof ServerPlayer sp) {
            sp.displayClientMessage(
                    Component.literal("This container is locked by " + owner.name() + "!")
                            .withStyle(ChatFormatting.RED),
                    true
            );
        }
        return false;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Lock Operations
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Locks the container at the given position (and its double-chest partner)
     * for the given player.
     */
    private static void lockContainer(ServerLevel level, BlockPos pos, ServerPlayer player) {
        LockOwner owner = new LockOwner(
                player.getUUID().toString(),
                player.getScoreboardName()
        );

        applyLock(level, pos, owner);

        BlockPos otherHalf = getDoubleChestOtherHalf(level, pos);
        if (otherHalf != null) {
            applyLock(level, otherHalf, owner);
        }
    }

    /**
     * Programmatically locks a container for a specific player by UUID and name.
     * Used by DeathChestHandler (and any other code) to lock containers without
     * needing a ServerPlayer instance.
     *
     * <p>Locks both halves of a double chest automatically.</p>
     *
     * @param level      the server level
     * @param pos        the position of the container (or either half of a double chest)
     * @param playerUUID the UUID of the player who should own the lock
     * @param playerName the display name of the player (shown in deny messages)
     */
    public static void lockContainerForPlayer(ServerLevel level, BlockPos pos,
                                              UUID playerUUID, String playerName) {
        LockOwner owner = new LockOwner(playerUUID.toString(), playerName);

        applyLock(level, pos, owner);

        BlockPos otherHalf = getDoubleChestOtherHalf(level, pos);
        if (otherHalf != null) {
            applyLock(level, otherHalf, owner);
        }
    }

    /**
     * Unlocks the container at the given position (and its double-chest partner).
     */
    private static void unlockContainer(ServerLevel level, BlockPos pos) {
        removeLock(level, pos);

        BlockPos otherHalf = getDoubleChestOtherHalf(level, pos);
        if (otherHalf != null) {
            removeLock(level, otherHalf);
        }
    }

    private static void applyLock(ServerLevel level, BlockPos pos, LockOwner owner) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof BaseContainerBlockEntity) {
            be.setAttached(LOCK_OWNER, owner);
            be.setChanged();
        }
    }

    private static void removeLock(ServerLevel level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof BaseContainerBlockEntity) {
            be.removeAttached(LOCK_OWNER);
            be.setChanged();
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Owner Lookup
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Reads the lock owner from a single block entity's attachment.
     */
    private static LockOwner getOwner(BlockEntity be) {
        if (be == null) return null;
        return be.getAttached(LOCK_OWNER);
    }

    /**
     * Checks this container AND the other half of a double chest.
     * Returns the lock owner if either half is locked.
     */
    private static LockOwner getOwnerChecked(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        LockOwner owner = getOwner(be);
        if (owner != null) return owner;

        BlockPos otherHalf = getDoubleChestOtherHalf(level, pos);
        if (otherHalf != null) {
            return getOwner(level.getBlockEntity(otherHalf));
        }

        return null;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Double Chest Helper
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Returns the BlockPos of the other half of a double chest, or null if
     * the block at pos is not part of a double chest.
     */
    private static BlockPos getDoubleChestOtherHalf(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof ChestBlock)) return null;

        ChestType type = state.getValue(ChestBlock.TYPE);
        if (type == ChestType.SINGLE) return null;

        Direction facing = state.getValue(ChestBlock.FACING);
        Direction connected = (type == ChestType.LEFT)
                ? facing.getClockWise()
                : facing.getCounterClockWise();

        return pos.relative(connected);
    }
}

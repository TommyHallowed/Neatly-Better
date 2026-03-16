package net.hallowed.neatlybetter.content.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;

import net.hallowed.neatlybetter.init.ModItems;

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

@SuppressWarnings("UnstableApiUsage")
public class ChestLockHandler {

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

    public static final AttachmentType<@NotNull LockOwner> LOCK_OWNER = AttachmentRegistry.createPersistent(
            Identifier.fromNamespaceAndPath("neatlybetter", "lock_owner"),
            LockOwner.CODEC
    );

    public static void register() {
        UseBlockCallback.EVENT.register(ChestLockHandler::onUseBlock);
        PlayerBlockBreakEvents.BEFORE.register(ChestLockHandler::onBlockBreak);
    }

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

        if (isSneaking && holdingLock) {
            return handleLockToggle(level, pos, serverPlayer, hand);
        }

        LockOwner owner = getOwnerChecked(level, pos);
        if (owner == null) {
            return InteractionResult.PASS;
        }

        if (owner.ownerUuid().equals(serverPlayer.getUUID())) {
            return InteractionResult.PASS;
        } else {
            serverPlayer.displayClientMessage(
                    Component.literal("This container is locked by " + owner.name() + "!")
                            .withStyle(ChatFormatting.RED),
                    true
            );
            level.playSound(null, pos, SoundEvents.CHEST_LOCKED, SoundSource.BLOCKS, 1.0f, 1.0f);
            return InteractionResult.FAIL;
        }
    }

    private static InteractionResult handleLockToggle(ServerLevel level, BlockPos pos,
                                                      ServerPlayer player,
                                                      InteractionHand hand) {
        LockOwner owner = getOwnerChecked(level, pos);

        if (owner == null) {
            lockContainer(level, pos, player);
            player.displayClientMessage(
                    Component.literal("Container locked!").withStyle(ChatFormatting.GREEN),
                    true
            );
            level.playSound(null, pos, SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 1.0f, 1.4f);
            player.swing(hand, true);
            return InteractionResult.SUCCESS;
        }

        if (owner.ownerUuid().equals(player.getUUID())) {
            unlockContainer(level, pos);
            player.displayClientMessage(
                    Component.literal("Container unlocked!").withStyle(ChatFormatting.YELLOW),
                    true
            );
            level.playSound(null, pos, SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.BLOCKS, 1.0f, 1.4f);
            player.swing(hand, true);
            return InteractionResult.SUCCESS;
        }

        player.displayClientMessage(
                Component.literal("This container is locked by " + owner.name() + "!")
                        .withStyle(ChatFormatting.RED),
                true
        );
        return InteractionResult.FAIL;
    }

    private static boolean onBlockBreak(Level world, Player player, BlockPos pos,
                                        BlockState state, BlockEntity blockEntity) {
        if (world.isClientSide() || !(blockEntity instanceof BaseContainerBlockEntity)) {
            return true;
        }

        LockOwner owner = getOwnerChecked(world, pos);
        if (owner == null) {
            return true;
        }

        if (owner.ownerUuid().equals(player.getUUID())) {
            unlockContainer((ServerLevel) world, pos);
            return true;
        }

        if (player instanceof ServerPlayer sp) {
            sp.displayClientMessage(
                    Component.literal("This container is locked by " + owner.name() + "!")
                            .withStyle(ChatFormatting.RED),
                    true
            );
        }
        return false;
    }

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

    public static void lockContainerForPlayer(ServerLevel level, BlockPos pos,
                                              UUID playerUUID, String playerName) {
        LockOwner owner = new LockOwner(playerUUID.toString(), playerName);

        applyLock(level, pos, owner);

        BlockPos otherHalf = getDoubleChestOtherHalf(level, pos);
        if (otherHalf != null) {
            applyLock(level, otherHalf, owner);
        }
    }

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

    private static LockOwner getOwner(BlockEntity be) {
        if (be == null) return null;
        return be.getAttached(LOCK_OWNER);
    }

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

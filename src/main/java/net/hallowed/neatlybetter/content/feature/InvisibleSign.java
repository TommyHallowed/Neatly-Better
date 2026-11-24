package net.hallowed.neatlybetter.content.feature;

import com.mojang.serialization.Codec;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;

import net.hallowed.NeatlyBetter;
import net.hallowed.neatlybetter.config.NTCommonConfig;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Phantom membrane on a wall sign toggles the {@link NeatlyBetter#INVISIBLE} block state property
 * (added to wall signs by WallSignBlockMixin).
 * <p>
 * Being a block state, it syncs to clients, re-meshes the chunk, saves, and survives pistons/structures
 * automatically. Rendering reads the state directly (no block entity lookups).
 */
public final class InvisibleSign {
    private InvisibleSign() {}

    /**
     * Legacy storage from before invisibility was a block state. Still registered so old worlds load;
     * signs carrying it are converted to the block state when they load, see {@link #migrateLegacy}.
     */
    @Deprecated
    public static final AttachmentType<Boolean> LEGACY_INVISIBLE = AttachmentRegistry.createPersistent(
            Identifier.fromNamespaceAndPath("neatly-better", "invisible_sign"),
            Codec.BOOL
    );

    /** Signs found with the legacy attachment, converted at the end of the level tick (not during chunk loading). */
    private static final Map<ServerLevel, List<BlockPos>> PENDING_MIGRATION = new IdentityHashMap<>();

    public static void register() {
        UseBlockCallback.EVENT.register(InvisibleSign::interact);
        ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.register(InvisibleSign::migrateLegacy);
        ServerTickEvents.END_LEVEL_TICK.register(InvisibleSign::flushMigration);
    }

    public static boolean isInvisible(BlockState state) {
        return state.getValueOrElse(NeatlyBetter.INVISIBLE, false);
    }

    private static InteractionResult interact(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.is(Items.PHANTOM_MEMBRANE)) return InteractionResult.PASS;
        if (!player.mayBuild()) return InteractionResult.PASS;
        if (player.isShiftKeyDown() && NTCommonConfig.CONFIG.clickThrough.isFalse()) return InteractionResult.PASS;

        BlockPos pos = hitResult.getBlockPos();
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof WallSignBlock) || !state.hasProperty(NeatlyBetter.INVISIBLE)) return InteractionResult.PASS;
        if (level.getBlockEntity(pos) instanceof SignBlockEntity sign && sign.isWaxed()) return InteractionResult.PASS;

        if (!level.isClientSide()) {
            BlockState toggled = state.cycle(NeatlyBetter.INVISIBLE);
            level.setBlock(pos, toggled, Block.UPDATE_ALL);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, toggled));
            level.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
            stack.consume(1, player);
        }
        return InteractionResult.SUCCESS;
    }

    private static void migrateLegacy(net.minecraft.world.level.block.entity.BlockEntity blockEntity, ServerLevel level) {
        if (!(blockEntity instanceof SignBlockEntity sign)) return;
        if (!Boolean.TRUE.equals(sign.getAttached(LEGACY_INVISIBLE))) return;
        sign.removeAttached(LEGACY_INVISIBLE);
        PENDING_MIGRATION.computeIfAbsent(level, _ -> new ArrayList<>()).add(sign.getBlockPos());
    }

    private static void flushMigration(ServerLevel level) {
        if (PENDING_MIGRATION.isEmpty()) return;
        List<BlockPos> pending = PENDING_MIGRATION.remove(level);
        if (pending == null) return;
        for (BlockPos pos : pending) {
            BlockState state = level.getBlockState(pos);
            if (state.hasProperty(NeatlyBetter.INVISIBLE) && !isInvisible(state)) {
                level.setBlock(pos, state.setValue(NeatlyBetter.INVISIBLE, true), Block.UPDATE_ALL);
            }
        }
    }
}

package net.hallowed.neatlybetter.content.feature;

import com.mojang.serialization.Codec;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;

import net.hallowed.neatlybetter.config.NTCommonConfig;

import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
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
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import org.jspecify.annotations.NonNull;

public final class InvisibleSign implements UseBlockCallback {

    public static final AttachmentType<Boolean> INVISIBLE = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath("neatly-better", "invisible_sign"),
            builder -> builder
                    .persistent(Codec.BOOL)
                    .syncWith(ByteBufCodecs.BOOL, AttachmentSyncPredicate.all())
    );

    public static void register() {
        UseBlockCallback.EVENT.register(new InvisibleSign());
    }

    @Override
    public @NonNull InteractionResult interact(Player player, @NonNull Level level, @NonNull InteractionHand hand, @NonNull BlockHitResult hitResult) {
        ItemStack stack = player.getItemInHand(hand);

        if (!stack.is(Items.PHANTOM_MEMBRANE)) return InteractionResult.PASS;
        if (!player.mayBuild()) return InteractionResult.PASS;
        if (player.isShiftKeyDown() && !NTCommonConfig.CONFIG.clickThrough.get()) return InteractionResult.PASS;

        BlockPos pos = hitResult.getBlockPos();
        BlockState state = level.getBlockState(pos);

        if (!(state.getBlock() instanceof WallSignBlock)) return InteractionResult.PASS;

        if (!(level.getBlockEntity(pos) instanceof SignBlockEntity sign)) return InteractionResult.PASS;
        if (sign.isWaxed()) return InteractionResult.PASS;

        if (level instanceof ServerLevel) {
            boolean wasInvisible = Boolean.TRUE.equals(sign.getAttached(INVISIBLE));
            boolean nowInvisible = !wasInvisible;

            if (nowInvisible) {
                sign.setAttached(INVISIBLE, true);
            } else {
                sign.removeAttached(INVISIBLE);
            }

            sign.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);

            stack.consume(1, player);

            level.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
        }

        return InteractionResult.SUCCESS;
    }
}
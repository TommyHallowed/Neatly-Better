package net.hallowed.neatlybetter.mixin.block;

import net.hallowed.neatlybetter.api.EmissiveBannerAccessor;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractBannerBlock;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AbstractBannerBlock.class)
public abstract class AbstractBannerBlockMixin extends BaseEntityBlock {

    protected AbstractBannerBlockMixin(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected @NonNull InteractionResult useItemOn(@NonNull ItemStack stack, @NonNull BlockState state, Level level, @NonNull BlockPos pos,
                                                   @NonNull Player player, @NonNull InteractionHand hand, @NonNull BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof BannerBlockEntity banner)) {
            return super.useItemOn(stack, state, level, pos, player, hand, hit);
        }

        if (banner.getPatterns().layers().isEmpty()) {
            return super.useItemOn(stack, state, level, pos, player, hand, hit);
        }

        EmissiveBannerAccessor accessor = (EmissiveBannerAccessor) banner;

        if (stack.is(Items.GLOW_INK_SAC) && !accessor.neatlybetter$isEmissive()) {
            accessor.neatlybetter$setEmissive(true);

            if (!level.isClientSide()) {
                banner.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
            }

            level.playSound(player, pos, SoundEvents.GLOW_INK_SAC_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
            return InteractionResult.SUCCESS;
        }

        if (stack.is(Items.INK_SAC) && accessor.neatlybetter$isEmissive()) {
            accessor.neatlybetter$setEmissive(false);

            if (!level.isClientSide()) {
                banner.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
            }

            level.playSound(player, pos, SoundEvents.INK_SAC_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
            return InteractionResult.SUCCESS;
        }

        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }
}

package net.hallowed.neatlybetter.mixin.block;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FireBlock.class)
public class FireBlockMixin {

    @Inject(
            method = "checkBurnOut(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;ILnet/minecraft/util/RandomSource;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;removeBlock(Lnet/minecraft/core/BlockPos;Z)Z")
    )
    private void neatlybetter$dropCharcoalOnLogBurnOut(Level level, BlockPos pos, int chance, RandomSource random, int age, CallbackInfo ci) {
        BlockState blockState = level.getBlockState(pos);

        if (!blockState.is(BlockTags.LOGS)) return;
        if (random.nextFloat() >= 0.50f) return;

        level.addFreshEntity(new ItemEntity(
                level,
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5,
                new ItemStack(Items.CHARCOAL)
        ));
    }
}
package net.hallowed.neatlybetter.mixin.block;

import net.hallowed.neatlybetter.config.NTServerConfig;
import net.hallowed.neatlybetter.util.LapisUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public abstract class BlockMixin {

    @Inject(
            method = "popResource(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void neatlybetter$constrainScaffoldingDrop(
            Level level, BlockPos pos, ItemStack itemStack, CallbackInfo ci
    ) {
        if (!itemStack.is(Items.SCAFFOLDING)) return;
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (itemStack.isEmpty()) return;
        if (!serverLevel.getGameRules().get(GameRules.BLOCK_DROPS)) return;

        double halfHeight = EntityTypes.ITEM.getHeight() / 2.0;

        double x = pos.getX() + 0.5 + Mth.nextDouble(level.getRandom(), -1.5, 1.5);
        double y = pos.getY() + 0.5 + Mth.nextDouble(level.getRandom(), -0.25, 0.25) - halfHeight;
        double z = pos.getZ() + 0.5 + Mth.nextDouble(level.getRandom(), -1.5, 1.5);

        ItemEntity itemEntity = new ItemEntity(level, x, y, z, itemStack, 0.0, 0.0, 0.0);
        itemEntity.setDefaultPickUpDelay();
        level.addFreshEntity(itemEntity);

        ci.cancel();
    }

    @Inject(
            method = "dropResources(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At("HEAD")
    )
    private static void neatlybetter$dropStoredLapisOnBreak(
            BlockState state, Level level, BlockPos pos,
            BlockEntity blockEntity, Entity breaker, ItemStack tool,
            CallbackInfo ci
    ) {
        if (!(blockEntity instanceof EnchantingTableBlockEntity enchTable)) return;
        if (!NTServerConfig.CONFIG.lapisStaysInEnchanting.get()) return;

        int stored = LapisUtil.getLapisCount(level, enchTable);
        if (stored <= 0) return;

        ItemStack lapisStack = new ItemStack(Items.LAPIS_LAZULI, stored);
        level.addFreshEntity(new ItemEntity(
                level,
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5,
                lapisStack
        ));
    }
}
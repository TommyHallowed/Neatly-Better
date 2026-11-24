package net.hallowed.neatlybetter.mixin.block;

import net.hallowed.neatlybetter.feature.building.GlowingBanners;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Banner loot tables copy only a fixed list of components, so the glow is added to the drops here instead of
 * overriding the 16 vanilla banner loot tables.
 */
@Mixin(Block.class)
public abstract class BannerGlowDropMixin {

    @Inject(method = "getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;)Ljava/util/List;",
            at = @At("RETURN"))
    private static void neatlybetter$glowDrops(BlockState state, ServerLevel level, BlockPos pos, BlockEntity blockEntity,
                                               CallbackInfoReturnable<List<ItemStack>> cir) {
        if (blockEntity != null) GlowingBanners.markDrops(blockEntity, cir.getReturnValue());
    }

    @Inject(method = "getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemInstance;)Ljava/util/List;",
            at = @At("RETURN"))
    private static void neatlybetter$glowDropsWithTool(BlockState state, ServerLevel level, BlockPos pos, BlockEntity blockEntity,
                                                       Entity breaker, ItemInstance tool, CallbackInfoReturnable<List<ItemStack>> cir) {
        if (blockEntity != null) GlowingBanners.markDrops(blockEntity, cir.getReturnValue());
    }
}

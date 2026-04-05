package net.hallowed.neatlybetter.mixin.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
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

        double halfHeight = EntityType.ITEM.getHeight() / 2.0;

        double x = pos.getX() + 0.5 + Mth.nextDouble(level.getRandom(), -1.5, 1.5);
        double y = pos.getY() + 0.5 + Mth.nextDouble(level.getRandom(), -0.25, 0.25) - halfHeight;
        double z = pos.getZ() + 0.5 + Mth.nextDouble(level.getRandom(), -1.5, 1.5);

        ItemEntity itemEntity = new ItemEntity(level, x, y, z, itemStack, 0.0, 0.0, 0.0);
        itemEntity.setDefaultPickUpDelay();
        level.addFreshEntity(itemEntity);

        ci.cancel();
    }
}

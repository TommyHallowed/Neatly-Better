package net.hallowed.oldways.content.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.VerticallyAttachableBlockItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class GlowTorchItem extends VerticallyAttachableBlockItem {

    public GlowTorchItem(Block standingBlock, Block wallBlock, Item.Settings settings) {
        super(standingBlock, wallBlock, Direction.UP, settings);
    }

    @Override
    @Nullable
    protected BlockState getPlacementState(ItemPlacementContext ctx) {
        WorldView world = ctx.getWorld();
        BlockPos pos = ctx.getBlockPos();

        if (ctx.getSide() == Direction.UP) {
            BlockState standing = this.getBlock().getPlacementState(ctx);
            if (standing != null && this.canPlaceAt(world, standing, pos)) {
                return standing;
            }
        }
        return super.getPlacementState(ctx);
    }
}

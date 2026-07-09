package net.hallowed.neatlybetter.content.blockentity;

import net.hallowed.neatlybetter.init.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class DyeCauldronBlockEntity extends BlockEntity {
    private int color = -1;

    public DyeCauldronBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DYE_CAULDRON, pos, state);
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
        setChanged();
    }

    @Override
    public Object getRenderData() {
        return color;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public @Nullable ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NonNull CompoundTag getUpdateTag(HolderLookup.@NonNull Provider registries) {
        return saveCustomOnly(registries);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        output.putInt("Color", color);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        color = input.getIntOr("Color", -1);
    }
}

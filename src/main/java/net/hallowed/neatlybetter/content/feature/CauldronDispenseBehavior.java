package net.hallowed.neatlybetter.content.feature;

import java.util.IdentityHashMap;
import java.util.Map;

import net.fabricmc.fabric.api.transfer.v1.fluid.CauldronFluidContent;

import net.hallowed.neatlybetter.config.NTServerConfig;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.item.SolidBucketItem;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import org.jspecify.annotations.NonNull;

public class CauldronDispenseBehavior implements DispenseItemBehavior {

    private final DispenseItemBehavior fallback;

    private static volatile Map<Fluid, Item> fluidToBucket;

    public CauldronDispenseBehavior(DispenseItemBehavior fallback) {
        this.fallback = fallback;
    }

    @Override
    public @NonNull ItemStack dispense(BlockSource source, @NonNull ItemStack stack) {
        ServerLevel level = source.level();

        if (!NTServerConfig.CONFIG.dispenserCauldronInteraction.get()) {
            return fallback.dispense(source, stack);
        }

        Direction facing = source.state().getValue(DispenserBlock.FACING);
        BlockPos targetPos = source.pos().relative(facing);
        BlockState targetState = level.getBlockState(targetPos);

        if (!(targetState.getBlock() instanceof AbstractCauldronBlock)) {
            return fallback.dispense(source, stack);
        }

        ItemStack result = null;

        if (stack.getItem() instanceof SolidBucketItem solidBucket) {
            if (solidBucket.getBlock() == Blocks.POWDER_SNOW) {
                result = tryFillPowderSnow(source, level, targetPos, targetState);
            }
        } else if (stack.getItem() instanceof BucketItem bucketItem) {
            Fluid fluid = bucketItem.getContent();
            if (fluid == Fluids.EMPTY) {
                result = tryCollect(source, stack, level, targetPos, targetState);
                if (result == null) return stack;
            } else {
                result = tryFill(source, level, targetPos, targetState, fluid);
                if (result != null && stack.getItem() instanceof MobBucketItem mobBucket) {
                    mobBucket.checkExtraContent(null, level, stack, targetPos.above());
                }
            }
        }

        return result != null ? result : fallback.dispense(source, stack);
    }

    private ItemStack tryFill(BlockSource source, ServerLevel level,
                              BlockPos pos, BlockState state, Fluid fluid) {

        if (state.is(Blocks.CAULDRON)) {
            BlockState filled = cauldronStateForFluid(fluid);
            if (filled == null) return null;

            level.setBlock(pos, filled, 3);
            playEmptyBucketSound(level, pos, fluid);
            level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
            playDispenserEffects(source);
            return new ItemStack(Items.BUCKET);
        }

        CauldronFluidContent content = CauldronFluidContent.getForBlock(state.getBlock());
        if (content != null && content.fluid.isSame(fluid) && content.levelProperty != null) {
            int cur = state.getValue(content.levelProperty);
            if (cur < content.maxLevel) {
                level.setBlock(pos, state.setValue(content.levelProperty, content.maxLevel), 3);
                playEmptyBucketSound(level, pos, fluid);
                level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
                playDispenserEffects(source);
                return new ItemStack(Items.BUCKET);
            }
        }

        return null;
    }

    private ItemStack tryFillPowderSnow(BlockSource source, ServerLevel level,
                                        BlockPos pos, BlockState state) {
        if (state.is(Blocks.CAULDRON)) {
            level.setBlock(pos, Blocks.POWDER_SNOW_CAULDRON.defaultBlockState()
                    .setValue(LayeredCauldronBlock.LEVEL, 3), 3);
        } else if (state.is(Blocks.POWDER_SNOW_CAULDRON)) {
            if (state.getValue(LayeredCauldronBlock.LEVEL) >= 3) return null;
            level.setBlock(pos, state.setValue(LayeredCauldronBlock.LEVEL, 3), 3);
        } else {
            return null;
        }

        level.playSound(null, pos, SoundEvents.BUCKET_EMPTY_POWDER_SNOW,
                SoundSource.BLOCKS, 1.0f, 1.0f);
        level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
        playDispenserEffects(source);
        return new ItemStack(Items.BUCKET);
    }

    private ItemStack tryCollect(BlockSource source, ItemStack stack,
                                 ServerLevel level, BlockPos pos, BlockState state) {

        ItemStack filled = null;
        SoundEvent sound = SoundEvents.BUCKET_FILL;

        if (state.is(Blocks.POWDER_SNOW_CAULDRON)
                && state.getValue(LayeredCauldronBlock.LEVEL) == 3) {
            filled = new ItemStack(Items.POWDER_SNOW_BUCKET);
            sound  = SoundEvents.BUCKET_FILL_POWDER_SNOW;

        } else {
            CauldronFluidContent content = CauldronFluidContent.getForBlock(state.getBlock());
            if (content != null) {
                boolean full = content.levelProperty == null
                        || state.getValue(content.levelProperty) == content.maxLevel;
                if (!full) return null;

                Item bucket = fluidToBucketMap().get(content.fluid);
                if (bucket == null) return null;

                filled = new ItemStack(bucket);
                sound  = content.fluid.is(FluidTags.LAVA)
                        ? SoundEvents.BUCKET_FILL_LAVA : SoundEvents.BUCKET_FILL;
            }
        }

        if (filled == null) return null;

        level.setBlock(pos, Blocks.CAULDRON.defaultBlockState(), 3);
        level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0f, 1.0f);
        level.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
        playDispenserEffects(source);

        stack.shrink(1);
        if (stack.isEmpty()) return filled;

        DispenserBlockEntity dispenser = source.blockEntity();
        ItemStack remaining = dispenser.insertItem(filled);
        if (!remaining.isEmpty()) {
            new DefaultDispenseItemBehavior().dispense(source, remaining);
        }
        return stack;
    }

    private static BlockState cauldronStateForFluid(Fluid fluid) {
        CauldronFluidContent content = CauldronFluidContent.getForFluid(fluid);
        if (content == null) return null;

        BlockState state = content.block.defaultBlockState();
        if (content.levelProperty != null) {
            state = state.setValue(content.levelProperty, content.maxLevel);
        }
        return state;
    }

    private static void playEmptyBucketSound(ServerLevel level, BlockPos pos, Fluid fluid) {
        SoundEvent sound = fluid.is(FluidTags.LAVA)
                ? SoundEvents.BUCKET_EMPTY_LAVA : SoundEvents.BUCKET_EMPTY;
        level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0f, 1.0f);
    }

    private static void playDispenserEffects(BlockSource source) {
        Direction dir = source.state().getValue(DispenserBlock.FACING);
        source.level().levelEvent(1000, source.pos(), 0);
        source.level().levelEvent(2000, source.pos(), dir.get3DDataValue());
    }

    private static Map<Fluid, Item> fluidToBucketMap() {
        Map<Fluid, Item> map = fluidToBucket;
        if (map == null) {
            synchronized (CauldronDispenseBehavior.class) {
                map = fluidToBucket;
                if (map == null) {
                    map = new IdentityHashMap<>();
                    for (Item item : BuiltInRegistries.ITEM) {
                        if (item instanceof BucketItem bucket) {
                            Fluid f = bucket.getContent();
                            if (f != Fluids.EMPTY) {
                                map.putIfAbsent(f, item);
                            }
                        }
                    }
                    fluidToBucket = map;
                }
            }
        }
        return map;
    }
}

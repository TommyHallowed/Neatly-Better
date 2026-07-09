package net.hallowed.neatlybetter.init;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;

import net.hallowed.neatlybetter.content.block.DyeCauldronBlock;
import net.hallowed.neatlybetter.content.blockentity.DyeCauldronBlockEntity;
import net.hallowed.neatlybetter.util.CauldronDyeData;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.cauldron.CauldronInteractions;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.ARGB;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.List;


public final class ModCauldronInteractions {
    public static final CauldronInteraction.Dispatcher MILK = new CauldronInteraction.Dispatcher();
    public static final CauldronInteraction.Dispatcher DYE = new CauldronInteraction.Dispatcher();

    private ModCauldronInteractions() {
    }

    public static void bootstrap() {
        CauldronInteractions.addDefaultInteractions(MILK);
        CauldronInteractions.addDefaultInteractions(DYE);

        MILK.put(Items.BUCKET, (state, level, pos, player, hand, itemInHand) ->
                CauldronInteractions.fillBucket(
                        state, level, pos, player, hand, itemInHand,
                        new ItemStack(Items.MILK_BUCKET),
                        s -> s.getValue(LayeredCauldronBlock.LEVEL) == 3,
                        SoundEvents.BUCKET_FILL
                ));

        CauldronInteractions.EMPTY.put(Items.MILK_BUCKET, (_, level, pos, player, hand, itemInHand) ->
                CauldronInteractions.emptyBucket(
                        level, pos, player, hand, itemInHand,
                        ModBlocks.MILK_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3),
                        SoundEvents.BUCKET_EMPTY
                ));

        MILK.put(Items.GLASS_BOTTLE, (state, level, pos, player, hand, itemInHand) -> {
            Item usedItem = itemInHand.getItem();
            player.setItemInHand(hand, ItemUtils.createFilledResult(itemInHand, player, new ItemStack(ModItems.MILK_BOTTLE)));
            player.awardStat(Stats.USE_CAULDRON);
            player.awardStat(Stats.ITEM_USED.get(usedItem));
            LayeredCauldronBlock.lowerFillLevel(state, level, pos);
            level.playSound(player, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(player, GameEvent.FLUID_PICKUP, pos);

            return InteractionResult.SUCCESS;
        });

        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof DyeItem) {
                CauldronInteractions.WATER.put(item, ModCauldronInteractions::convertToDyeCauldron);
                DYE.put(item, ModCauldronInteractions::mixDye);
            }
        }

        for (CauldronDyeData.BlockDyeFamily family : CauldronDyeData.BLOCK_DYE_FAMILIES) {
            if (family.plain() == null || family.plain() == Items.SHULKER_BOX) {
                continue;
            }

            for (Item coloredItem : family.colored().asList()) {
                if (coloredItem == family.plain()) {
                    continue;
                }

                CauldronInteractions.WATER.put(coloredItem, (state, level, pos, player, hand, itemInHand) ->
                        washBlockDye(family, state, level, pos, player, hand, itemInHand));
            }
        }

        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            var pos = hitResult.getBlockPos();
            var state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof DyeCauldronBlock) || state.getValue(LayeredCauldronBlock.LEVEL) == 0) {
                return InteractionResult.PASS;
            }

            ItemStack stack = player.getItemInHand(hand);
            if (!CauldronDyeData.isDyeable(stack)) {
                return InteractionResult.PASS;
            }

            if (!(level.getBlockEntity(pos) instanceof DyeCauldronBlockEntity dyeCauldron)) {
                return InteractionResult.PASS;
            }

            DyedItemColor currentColor = stack.get(DataComponents.DYED_COLOR);
            if (currentColor != null && currentColor.rgb() == dyeCauldron.getColor()) {
                return InteractionResult.PASS;
            }

            stack.set(DataComponents.DYED_COLOR, new DyedItemColor(dyeCauldron.getColor()));
            LayeredCauldronBlock.lowerFillLevel(state, level, pos);
            player.awardStat(Stats.USE_CAULDRON);
            player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
            level.playSound(player, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);

            return InteractionResult.SUCCESS;
        });

        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            var pos = hitResult.getBlockPos();
            var state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof DyeCauldronBlock) || state.getValue(LayeredCauldronBlock.LEVEL) == 0) {
                return InteractionResult.PASS;
            }

            ItemStack stack = player.getItemInHand(hand);
            CauldronDyeData.BlockDyeFamily family = CauldronDyeData.BLOCK_DYE_FAMILY_BY_ITEM.get(stack.getItem());
            if (family == null) {
                return InteractionResult.PASS;
            }

            if (!(level.getBlockEntity(pos) instanceof DyeCauldronBlockEntity dyeCauldron)) {
                return InteractionResult.PASS;
            }

            DyeColor targetColor = CauldronDyeData.RGB_TO_DYE_COLOR.get(ARGB.opaque(dyeCauldron.getColor()));
            if (targetColor == null || CauldronDyeData.BLOCK_ITEM_COLORS.get(stack.getItem()) == targetColor) {
                return InteractionResult.PASS;
            }

            int amount = Math.min(stack.getCount(), family.batchSize());
            Item resultItem = family.colored().pick(targetColor);
            ItemStack resultStack = stack.transmuteCopy(resultItem, amount);
            stack.shrink(amount);

            if (stack.isEmpty()) {
                player.setItemInHand(hand, resultStack);
            } else if (!player.getInventory().add(resultStack)) {
                player.drop(resultStack, false);
            }

            LayeredCauldronBlock.lowerFillLevel(state, level, pos);
            player.awardStat(Stats.USE_CAULDRON);
            player.awardStat(Stats.ITEM_USED.get(resultItem));
            level.playSound(player, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);

            return InteractionResult.SUCCESS;
        });
    }

    private static InteractionResult convertToDyeCauldron(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, ItemStack itemInHand) {

        DyeColor dyeColor = itemInHand.get(DataComponents.DYE);
        if (dyeColor == null) {
            return InteractionResult.PASS;
        }

        int currentLevel = state.getValue(LayeredCauldronBlock.LEVEL);
        level.setBlockAndUpdate(pos, ModBlocks.DYE_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, currentLevel));

        if (level.getBlockEntity(pos) instanceof DyeCauldronBlockEntity dyeCauldron) {
            dyeCauldron.setColor(DyedItemColor.applyDyes((DyedItemColor) null, List.of(dyeColor)).rgb());
        }

        if (!player.isCreative()) {
            itemInHand.shrink(1);
        }
        player.awardStat(Stats.USE_CAULDRON);
        player.awardStat(Stats.ITEM_USED.get(itemInHand.getItem()));
        level.playSound(player, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);

        return InteractionResult.SUCCESS;
    }

    private static InteractionResult mixDye(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, ItemStack itemInHand) {

        DyeColor dyeColor = itemInHand.get(DataComponents.DYE);
        if (dyeColor == null) {
            return InteractionResult.PASS;
        }

        if (level.getBlockEntity(pos) instanceof DyeCauldronBlockEntity dyeCauldron) {
            DyedItemColor mixed = DyedItemColor.applyDyes(new DyedItemColor(dyeCauldron.getColor()), List.of(dyeColor));
            dyeCauldron.setColor(mixed.rgb());
        }

        if (!player.isCreative()) {
            itemInHand.shrink(1);
        }
        player.awardStat(Stats.USE_CAULDRON);
        player.awardStat(Stats.ITEM_USED.get(itemInHand.getItem()));
        level.playSound(player, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);

        return InteractionResult.SUCCESS;
    }

    private static InteractionResult washBlockDye(CauldronDyeData.BlockDyeFamily family, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, ItemStack itemInHand) {

        Item plainItem = family.plain();
        if (plainItem == null) {
            return InteractionResult.PASS;
        }

        int amount = Math.min(itemInHand.getCount(), family.batchSize());
        ItemStack resultStack = itemInHand.transmuteCopy(plainItem, amount);
        itemInHand.shrink(amount);

        if (itemInHand.isEmpty()) {
            player.setItemInHand(hand, resultStack);
        } else if (!player.getInventory().add(resultStack)) {
            player.drop(resultStack, false);
        }

        LayeredCauldronBlock.lowerFillLevel(state, level, pos);
        player.awardStat(Stats.USE_CAULDRON);
        player.awardStat(Stats.ITEM_USED.get(plainItem));
        level.playSound(player, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);

        return InteractionResult.SUCCESS;
    }
}
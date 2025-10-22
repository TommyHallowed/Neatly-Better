package net.hallowed.oldways.mixin.block;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.hallowed.oldways.init.ModGameRules;
import net.hallowed.oldways.util.PlacedBlockTracker;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public abstract class BlockMixin {

    @Unique private static final float PLACED_XP_CHANCE = 0.05f;

    @Unique private static final float CROP_XP_CHANCE = 0.33f;
    @Unique private static final int   CROP_XP_MIN    = 1;
    @Unique private static final int   CROP_XP_MAX    = 2;

    @Unique private static final float NATURAL_BASE_CHANCE     = 0.01f;
    @Unique private static final float NATURAL_MAX_CHANCE      = 0.05f;
    @Unique private static final float NATURAL_XP_PER_HARDNESS = 0.01f;
    @Unique private static final int   NATURAL_XP_MIN = 1;
    @Unique private static final int   NATURAL_XP_MAX = 5;

    @Unique private static final int   ORE_IRON_XP_MIN   = 2;
    @Unique private static final int   ORE_IRON_XP_MAX   = 5;

    @Unique private static final int   ORE_COPPER_XP_MIN = 1;
    @Unique private static final int   ORE_COPPER_XP_MAX = 2;

    @Unique private static final int   ORE_GOLD_XP_MIN   = 2;
    @Unique private static final int   ORE_GOLD_XP_MAX   = 4;


    @Inject(method = "onPlaced", at = @At("TAIL"))
    private void oldways$markPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack, CallbackInfo ci) {
        if (!(world instanceof ServerWorld sw)) return;
        if (!sw.getGameRules().getBoolean(ModGameRules.XP_FROM_PLACING_BLOCKS)) return;
        PlacedBlockTracker.markPlaced(sw, pos);
        if (placer instanceof PlayerEntity p && !p.getAbilities().creativeMode) {
            float h = state.getHardness(sw, pos);
            if (h > 0f && sw.getRandom().nextFloat() < PLACED_XP_CHANCE) p.addExperience(1);
        }
    }

    @Inject(method = "afterBreak", at = @At("TAIL"))
    private void oldways$xpOnBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity be, ItemStack tool, CallbackInfo ci) {
        if (!(world instanceof ServerWorld sw)) return;

        boolean wasPlaced = PlacedBlockTracker.wasPlaced(sw, pos);
        PlacedBlockTracker.unmark(sw, pos);

        if (player == null || player.getAbilities().creativeMode) return;

        boolean iron = isIronOre(state);
        boolean copper = isCopperOre(state);
        boolean gold = isGoldOre(state);

        if ((iron || copper || gold) && !hasSilkTouch(tool)) {
            int min;
            int max;
            if (iron) {
                min = ORE_IRON_XP_MIN;
                max = ORE_IRON_XP_MAX;
            } else if (gold) {
                min = ORE_GOLD_XP_MIN;
                max = ORE_GOLD_XP_MAX;
            } else {
                min = ORE_COPPER_XP_MIN;
                max = ORE_COPPER_XP_MAX;
            }
            int range = Math.max(0, max - min);
            int xp = min + sw.getRandom().nextInt(range + 1);
            if (xp > 0) ExperienceOrbEntity.spawn(sw, pos.toCenterPos(), xp);
            return;
        }

        boolean matureCrop = isMatureCrop(state);
        if (matureCrop) {
            if (sw.getRandom().nextFloat() < CROP_XP_CHANCE) {
                int range = Math.max(0, CROP_XP_MAX - CROP_XP_MIN);
                int xp = CROP_XP_MIN + sw.getRandom().nextInt(range + 1);
                if (xp > 0) ExperienceOrbEntity.spawn(sw, pos.toCenterPos(), xp);
            }
        }

        boolean anyOre = isAnyOre(state) || state.isOf(Blocks.NETHER_QUARTZ_ORE);
        if (!sw.getGameRules().getBoolean(ModGameRules.XP_FROM_MINING_NON_ORE)) return;

        if (!anyOre && !wasPlaced) {
            float h = state.getHardness(sw, pos);
            if (h > 0f) {
                float chance = MathHelper.clamp(
                        NATURAL_BASE_CHANCE + (NATURAL_MAX_CHANCE - NATURAL_BASE_CHANCE) * (1f - (float)Math.exp(-0.6f * h)),
                        0f, NATURAL_MAX_CHANCE
                );
                if (sw.getRandom().nextFloat() < chance) {
                    int xp = MathHelper.clamp(NATURAL_XP_MIN + Math.round(h * NATURAL_XP_PER_HARDNESS), NATURAL_XP_MIN, NATURAL_XP_MAX);
                    if (xp > 0) ExperienceOrbEntity.spawn(sw, pos.toCenterPos(), xp);
                }
            }
        }
    }

    @Unique
    private static boolean isMatureCrop(BlockState s) {
        if (s.getBlock() instanceof CropBlock cb) return cb.isMature(s);
        if (s.contains(Properties.AGE_7)) return s.get(Properties.AGE_7) >= 7;
        if (s.contains(Properties.AGE_5)) return s.get(Properties.AGE_5) >= 5;
        if (s.contains(Properties.AGE_3)) return s.get(Properties.AGE_3) >= 3;
        return false;
    }

    @Unique
    private static boolean isAnyOre(BlockState s) {
        return s.isIn(BlockTags.IRON_ORES)
                || s.isIn(BlockTags.GOLD_ORES)
                || s.isIn(BlockTags.DIAMOND_ORES)
                || s.isIn(BlockTags.REDSTONE_ORES)
                || s.isIn(BlockTags.LAPIS_ORES)
                || s.isIn(BlockTags.COAL_ORES)
                || s.isIn(BlockTags.EMERALD_ORES)
                || s.isIn(BlockTags.COPPER_ORES);
    }

    @Unique
    private static boolean isIronOre(BlockState s) {
        return s.isIn(BlockTags.IRON_ORES)
                || s.isOf(Blocks.IRON_ORE)
                || s.isOf(Blocks.DEEPSLATE_IRON_ORE);
    }

    @Unique
    private static boolean isCopperOre(BlockState s) {
        return s.isIn(BlockTags.COPPER_ORES)
                || s.isOf(Blocks.COPPER_ORE)
                || s.isOf(Blocks.DEEPSLATE_COPPER_ORE);
    }

    @Unique
    private static boolean isGoldOre(BlockState s) {
        return s.isIn(BlockTags.GOLD_ORES)
                || s.isOf(Blocks.GOLD_ORE)
                || s.isOf(Blocks.DEEPSLATE_GOLD_ORE);
    }

    @Unique
    private static boolean hasSilkTouch(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        ItemEnchantmentsComponent ench = stack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);
        for (Object2IntMap.Entry<RegistryEntry<Enchantment>> e : ench.getEnchantmentEntries()) {
            if (e.getKey().matchesKey(Enchantments.SILK_TOUCH) && e.getIntValue() > 0) return true;
        }
        return false;
    }

}

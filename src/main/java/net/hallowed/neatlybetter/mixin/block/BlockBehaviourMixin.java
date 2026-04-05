package net.hallowed.neatlybetter.mixin.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockBehaviour.class)
public class BlockBehaviourMixin {

    @Inject(method = "spawnAfterBreak", at = @At("TAIL"))
    private void neatlybetter$addMissingOreXp(BlockState state, ServerLevel level, BlockPos pos,
                                              ItemStack tool, boolean dropExperience, CallbackInfo ci) {
        if (!dropExperience) return;

        IntProvider xpRange = neatlybetter$getOreXpRange(state.getBlock());
        if (xpRange == null) return;

        int xp = EnchantmentHelper.processBlockExperience(level, tool, xpRange.sample(level.getRandom()));

        if (xp > 0 && level.getGameRules().get(GameRules.BLOCK_DROPS)) {
            ExperienceOrb.award(level, Vec3.atCenterOf(pos), xp);
        }
    }

    @Unique
    private static IntProvider neatlybetter$getOreXpRange(Block block) {
        if (block == Blocks.COPPER_ORE || block == Blocks.DEEPSLATE_COPPER_ORE)
            return UniformInt.of(0, 2);
        if (block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE)
            return UniformInt.of(1, 3);
        if (block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE)
            return UniformInt.of(2, 5);
        return null;
    }
}
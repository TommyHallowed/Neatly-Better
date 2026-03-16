package net.hallowed.neatlybetter.mixin.block;

import it.unimi.dsi.fastutil.objects.Object2IntMap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.NotNull;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FarmBlock.class)
public abstract class FarmlandBlockMixin {

    @Inject(method = "turnToDirt", at = @At("HEAD"), cancellable = true, require = 0)
    private static void neatlybetter$skipTrampleA(Entity entity, BlockState state, Level world, BlockPos pos, CallbackInfo ci) {
        if (!(world instanceof ServerLevel)) return;
        if (shouldSkipTrample(entity)) ci.cancel();
    }

    @Unique
    private static boolean shouldSkipTrample(Entity entity) {
        if (!(entity instanceof Player player)) return false;

        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        if (boots.isEmpty()) return false;

        var ench = EnchantmentHelper.getEnchantmentsForCrafting(boots);
        for (Object2IntMap.Entry<Holder<@NotNull Enchantment>> e : ench.entrySet()) {
            if (e.getKey().is(Enchantments.FEATHER_FALLING) && e.getIntValue() > 0) {
                return true;
            }
        }
        return false;
    }
}

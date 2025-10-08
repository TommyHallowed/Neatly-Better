package net.hallowed.oldways.mixin.block;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FarmlandBlock.class)
public abstract class FarmlandBlockMixin {

    @Inject(method = "setToDirt", at = @At("HEAD"), cancellable = true, require = 0)
    private static void oldways$skipTrampleA(Entity entity, BlockState state, World world, BlockPos pos, CallbackInfo ci) {
        if (!(world instanceof ServerWorld)) return;
        if (shouldSkipTrample(entity)) ci.cancel();
    }

    @Unique
    private static boolean shouldSkipTrample(Entity entity) {
        if (!(entity instanceof PlayerEntity player)) return false;

        ItemStack boots = player.getEquippedStack(EquipmentSlot.FEET);
        if (boots.isEmpty()) return false;

        var ench = EnchantmentHelper.getEnchantments(boots);
        for (Object2IntMap.Entry<RegistryEntry<Enchantment>> e : ench.getEnchantmentEntries()) {
            if (e.getKey().matchesKey(Enchantments.FEATHER_FALLING) && e.getIntValue() > 0) {
                return true;
            }
        }
        return false;
    }
}

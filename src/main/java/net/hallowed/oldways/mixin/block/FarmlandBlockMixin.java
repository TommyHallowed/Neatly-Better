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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Players wearing Feather Falling boots (any level) do NOT trample farmland.
 * We cancel FarmlandBlock#setToDirt(...) early; fall damage is still handled by vanilla.
 */
@Mixin(FarmlandBlock.class)
public abstract class FarmlandBlockMixin {

    // ----- Variant A: setToDirt(Entity, BlockState, World, BlockPos) -----
    @Inject(method = "setToDirt", at = @At("HEAD"), cancellable = true, require = 0)
    private static void oldways$skipTrampleA(Entity entity, BlockState state, World world, BlockPos pos, CallbackInfo ci) {
        if (shouldSkipTrample(entity)) ci.cancel();
    }

    /** True if the entity is a player wearing boots with Feather Falling > 0. */
    @Unique
    private static boolean shouldSkipTrample(Entity entity) {
        if (!(entity instanceof PlayerEntity player)) return false;

        ItemStack boots = player.getEquippedStack(EquipmentSlot.FEET);
        if (boots.isEmpty()) return false;

        // Read enchantments directly from the boots and look for FEATHER_FALLING
        var ench = EnchantmentHelper.getEnchantments(boots);
        for (Object2IntMap.Entry<RegistryEntry<Enchantment>> e : ench.getEnchantmentEntries()) {
            if (e.getKey().matchesKey(Enchantments.FEATHER_FALLING) && e.getIntValue() > 0) {
                return true;
            }
        }
        return false;
    }
}

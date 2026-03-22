package net.hallowed.neatlybetter.mixin.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.MagmaBlock;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MagmaBlock.class)
public class MagmaBlockMixin {

    @Inject(method = "stepOn", at = @At("HEAD"), cancellable = true)
    private void neatlybetter$frostWalkerProtectsFromMagma(Level level, BlockPos blockPos, BlockState blockState, Entity entity, CallbackInfo ci) {
        if (entity instanceof LivingEntity livingEntity) {
            ItemStack boots = livingEntity.getItemBySlot(EquipmentSlot.FEET);
            if (!boots.isEmpty()) {
                var registry = livingEntity.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
                var frostWalker = registry.getOrThrow(Enchantments.FROST_WALKER);
                if (EnchantmentHelper.getItemEnchantmentLevel(frostWalker, boots) > 0) {
                    ci.cancel();
                }
            }
        }
    }
}
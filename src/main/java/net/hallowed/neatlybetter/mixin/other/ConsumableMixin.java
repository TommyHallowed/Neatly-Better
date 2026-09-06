package net.hallowed.neatlybetter.mixin.other;

import net.hallowed.neatlybetter.config.NTServerConfig;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Consumable.class)
public abstract class ConsumableMixin {

    @Inject(
            method = "canConsume(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;)Z",
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    private void neatlybetter$saturationEat(LivingEntity user, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (NTServerConfig.CONFIG.saturationEating.isFalse()) return;

        FoodProperties food = stack.get(DataComponents.FOOD);
        if (food == null || food.canAlwaysEat() || !(user instanceof Player player)) return;

        FoodData foodData = player.getFoodData();
        boolean hungerNotFull = foodData.needsFood();
        boolean saturationNotFull = foodData.getSaturationLevel() < (float) foodData.getFoodLevel();

        boolean canEat = food.saturation() > 0.0F
                ? (hungerNotFull || saturationNotFull)
                : hungerNotFull;

        cir.setReturnValue(canEat);
    }
}
package net.hallowed.neatlybetter.client.mixin.feature;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {
    @Shadow private ItemStack destroyingItem;

    @ModifyVariable(method = "sameDestroyTarget", at = @At("STORE"), name = "selected")
    private ItemStack neatlybetter$useSelectedStack(ItemStack selected) {
        return this.destroyingItem;
    }
}
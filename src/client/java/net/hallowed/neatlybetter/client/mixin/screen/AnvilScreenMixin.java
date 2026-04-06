package net.hallowed.neatlybetter.client.mixin.screen;

import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.client.gui.screens.inventory.ItemCombinerScreen;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AnvilMenu;

import org.jetbrains.annotations.NotNull;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(AnvilScreen.class)
public abstract class AnvilScreenMixin extends ItemCombinerScreen<@NotNull AnvilMenu> {
    public AnvilScreenMixin(AnvilMenu handler, Inventory inventory, Component title) {
        super(handler, inventory, title, null);
    }

    @ModifyConstant(method = "extractLabels", constant = @Constant(intValue = 40))
    private int neatlybetter$neverShowTooExpensive(int original) {
        return Integer.MAX_VALUE;
    }
}
package net.hallowed.neatlybetter.client.tooltip;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

import org.jspecify.annotations.Nullable;

import java.util.List;

public record ShulkerBoxTooltipData(List<ItemStack> items, @Nullable DyeColor color) implements TooltipComponent {
}
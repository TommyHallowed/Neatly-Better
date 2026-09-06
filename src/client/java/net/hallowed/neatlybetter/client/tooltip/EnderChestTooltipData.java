package net.hallowed.neatlybetter.client.tooltip;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record EnderChestTooltipData(List<ItemStack> items) implements TooltipComponent {
}
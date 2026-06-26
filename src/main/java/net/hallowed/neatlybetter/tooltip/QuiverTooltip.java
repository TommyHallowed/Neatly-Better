package net.hallowed.neatlybetter.tooltip;

import net.hallowed.neatlybetter.content.component.QuiverContents;

import net.minecraft.world.inventory.tooltip.TooltipComponent;

public record QuiverTooltip(QuiverContents contents) implements TooltipComponent {
}

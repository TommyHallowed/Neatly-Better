package net.hallowed.neatlybetter.tooltip;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.level.saveddata.maps.MapId;

public record MapPreviewTooltip(MapId mapId) implements TooltipComponent {
}
package net.hallowed.oldways.tooltip;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.level.saveddata.maps.MapId;

/**
 * Tooltip data for filled map items.
 * Stores only the MapId — no ItemStack copy needed.
 */
public record MapPreviewTooltip(MapId mapId) implements TooltipComponent {
}
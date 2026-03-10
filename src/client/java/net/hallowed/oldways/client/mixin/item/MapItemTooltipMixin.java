package net.hallowed.oldways.client.mixin.item;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.hallowed.oldways.tooltip.MapPreviewTooltip;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.saveddata.maps.MapId;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(Item.class)
public abstract class MapItemTooltipMixin {

    /**
     * Injects a map preview tooltip for filled map items.

     * Uses explicit descriptor to target ONLY the 1-arg overload,
     * avoiding the 2-arg overload that caused the NPE in v1.
     *
     * @ModifyReturnValue chains cleanly with other mods (unlike @Inject HEAD cancellable).
     */
    @ModifyReturnValue(
            method = "getTooltipImage(Lnet/minecraft/world/item/ItemStack;)Ljava/util/Optional;",
            at = @At("RETURN")
    )
    private Optional<TooltipComponent> oldways$mapPreview(
            Optional<TooltipComponent> original,
            ItemStack stack
    ) {
        // Don't override if another mod already provided a tooltip image
        if (original.isPresent()) return original;

        // Only for filled maps with valid map data
        if (!stack.is(Items.FILLED_MAP)) return original;
        MapId mapId = stack.get(DataComponents.MAP_ID);
        if (mapId == null) return original;

        return Optional.of(new MapPreviewTooltip(mapId));
    }
}
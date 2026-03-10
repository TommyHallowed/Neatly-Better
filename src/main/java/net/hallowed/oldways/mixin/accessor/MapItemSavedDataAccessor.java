package net.hallowed.oldways.mixin.accessor;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Exposes the private {@code addDecoration()} method of {@link MapItemSavedData}
 * so that {@code FastChunkScanner} can write structure icons (and other map
 * decorations) directly into the saved data — bypassing the
 * {@code MAP_DECORATIONS} ItemStack component which is only synced when a
 * player is holding the map.
 *
 * <p>Register in {@code oldways.mixins.json} under the {@code "mixins"} array:
 * <pre>{@code "accessor.MapItemSavedDataAccessor"}</pre>
 */
@Mixin(MapItemSavedData.class)
public interface MapItemSavedDataAccessor {

    /**
     * Calls the private {@code MapItemSavedData.addDecoration()} method.
     *
     * @param type          the decoration type (e.g. {@code MapDecorationTypes.WOODLAND_MANSION})
     * @param levelAccessor the level, used for rotation calculation (may be {@code null})
     * @param key           unique string key for this decoration
     * @param x             world X coordinate of the decoration
     * @param z             world Z coordinate of the decoration
     * @param rotation      yaw angle in degrees (use {@code 180.0} to match vanilla exploration maps)
     * @param name          optional display name shown on the map (may be {@code null})
     */
    @Invoker("addDecoration")
    void invokeAddDecoration(
            Holder<@NotNull MapDecorationType> type,
            @Nullable LevelAccessor levelAccessor,
            String key,
            double x,
            double z,
            double rotation,
            @Nullable Component name
    );
}

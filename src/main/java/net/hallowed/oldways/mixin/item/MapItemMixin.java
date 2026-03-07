package net.hallowed.oldways.mixin.item;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MapItem.class)
public class MapItemMixin {

    @Unique
    private static final Map<MapItemSavedData, Integer> OLDWAYS_FIXED_HEIGHT = new WeakHashMap<>();

    @ModifyExpressionValue(
            method = "update",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/dimension/DimensionType;hasCeiling()Z")
    )
    private boolean oldways$showSurfaceInAllDims(boolean original) {
        return false;
    }

    @Inject(
            method = "update",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/saveddata/maps/MapItemSavedData;getHoldingPlayer(Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/level/saveddata/maps/MapItemSavedData$HoldingPlayer;")
    )
    private void oldways$captureFirstHeight(Level world, Entity entity, MapItemSavedData state, CallbackInfo ci) {
        // FIX 1: Translated from Yarn to Mojang mappings for checking the dimension
        if (world.dimension() == Level.NETHER) {
            OLDWAYS_FIXED_HEIGHT.putIfAbsent(state, (int) Math.floor(entity.getY()));
        }
    }

    @ModifyExpressionValue(
            method = "update",
            // FIX 2: 'sampleHeightmap' translates to 'getHeight' in Mojang mappings
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunk;getHeight(Lnet/minecraft/world/level/levelgen/Heightmap$Types;II)I")
    )
    private int oldways$useFixedStartHeight(int original, Level world, Entity entity, MapItemSavedData state) {
        Integer fixed = OLDWAYS_FIXED_HEIGHT.get(state);
        return fixed != null ? fixed : original;
    }
}
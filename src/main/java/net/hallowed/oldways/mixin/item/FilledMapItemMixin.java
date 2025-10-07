package net.hallowed.oldways.mixin.item;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.map.MapState;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FilledMapItem.class)
public class FilledMapItemMixin {

    @Unique
    private static final Map<MapState, Integer> OLDWAYS_FIXED_HEIGHT = new WeakHashMap<>();

    @ModifyExpressionValue(
            method = "updateColors",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/dimension/DimensionType;hasCeiling()Z")
    )
    private boolean oldways$showSurfaceInAllDims(boolean original) {
        return false;
    }

    @Inject(
            method = "updateColors",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/map/MapState;getPlayerSyncData(Lnet/minecraft/entity/player/PlayerEntity;)Lnet/minecraft/item/map/MapState$PlayerUpdateTracker;")
    )
    private void oldways$captureFirstHeight(World world, Entity entity, MapState state, CallbackInfo ci) {
        if (world.getDimensionEntry().matchesKey(DimensionTypes.THE_NETHER)) {
            OLDWAYS_FIXED_HEIGHT.putIfAbsent(state, (int) Math.floor(entity.getY()));
        }
    }

    @ModifyExpressionValue(
            method = "updateColors",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/WorldChunk;sampleHeightmap(Lnet/minecraft/world/Heightmap$Type;II)I")
    )
    private int oldways$useFixedStartHeight(int original, World world, Entity entity, MapState state) {
        Integer fixed = OLDWAYS_FIXED_HEIGHT.get(state);
        return fixed != null ? fixed : original;
    }
}

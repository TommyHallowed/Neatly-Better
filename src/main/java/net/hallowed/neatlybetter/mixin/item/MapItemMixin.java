package net.hallowed.neatlybetter.mixin.item;

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
    private static final Map<MapItemSavedData, Integer> NEATLYBETTER_FIXED_HEIGHT = new WeakHashMap<>();

    @ModifyExpressionValue(
            method = "update",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/dimension/DimensionType;hasCeiling()Z")
    )
    private boolean neatlybetter$showSurfaceInAllDims(boolean original) {
        return false;
    }

    @Inject(
            method = "update",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/saveddata/maps/MapItemSavedData;getHoldingPlayer(Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/level/saveddata/maps/MapItemSavedData$HoldingPlayer;")
    )
    private void neatlybetter$captureFirstHeight(Level level, Entity player, MapItemSavedData data, CallbackInfo ci) {
        // FIX 1: Translated from Yarn to Mojang mappings for checking the dimension
        if (level.dimension() == Level.NETHER) {
            NEATLYBETTER_FIXED_HEIGHT.putIfAbsent(data, (int) Math.floor(player.getY()));
        }
    }

    @ModifyExpressionValue(
            method = "update",
            // FIX 2: 'sampleHeightmap' translates to 'getHeight' in Mojang mappings
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunk;getHeight(Lnet/minecraft/world/level/levelgen/Heightmap$Types;II)I")
    )
    private int neatlybetter$useFixedStartHeight(int original, Level level, Entity player, MapItemSavedData data) {
        Integer fixed = NEATLYBETTER_FIXED_HEIGHT.get(data);
        return fixed != null ? fixed : original;
    }
}
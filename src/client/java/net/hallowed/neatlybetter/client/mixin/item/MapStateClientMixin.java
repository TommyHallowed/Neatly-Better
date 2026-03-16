package net.hallowed.neatlybetter.client.mixin.item;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import org.jetbrains.annotations.NotNull;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MapItemSavedData.class)
public class MapStateClientMixin {

    @Shadow @Final
    public ResourceKey<@NotNull Level> dimension;

    @Unique
    private static boolean isOffMap(Holder<@NotNull MapDecorationType> t) {
        return t == MapDecorationTypes.PLAYER_OFF_MAP
                || t == MapDecorationTypes.PLAYER_OFF_LIMITS
                || t.equals(MapDecorationTypes.PLAYER_OFF_MAP)
                || t.equals(MapDecorationTypes.PLAYER_OFF_LIMITS);
    }

    @Unique
    private static byte rot(float yaw) {
        int n = ((int) Mth.wrapDegrees(yaw) + 360) % 360;
        return (byte) (Math.round(n / 22.5f) & 15);
    }

    @Inject(method = "getDecorations", at = @At("RETURN"), cancellable = true)
    private void neatlybetter$normalizePlayerArrow(CallbackInfoReturnable<Iterable<MapDecoration>> cir) {
        Iterable<MapDecoration> in = cir.getReturnValue();
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || in == null) return;

        boolean isNether = this.dimension != null && this.dimension == Level.NETHER;
        byte playerRot = rot(mc.player.getYRot());

        List<MapDecoration> out = null;

        for (MapDecoration d : in) {
            boolean replace = isOffMap(d.type()) || (isNether && (d.type() == MapDecorationTypes.PLAYER || d.type().equals(MapDecorationTypes.PLAYER)));
            if (replace) {
                if (out == null) {
                    out = new ArrayList<>();
                    for (MapDecoration prev : in) {
                        if (prev == d) break;
                        out.add(prev);
                    }
                }
                out.add(new MapDecoration(MapDecorationTypes.PLAYER, d.x(), d.y(), playerRot, d.name()));
            } else if (out != null) {
                out.add(d);
            }
        }

        if (out != null) {
            cir.setReturnValue(out);
        }
    }
}

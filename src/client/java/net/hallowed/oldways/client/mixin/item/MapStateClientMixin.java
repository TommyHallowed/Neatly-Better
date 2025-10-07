package net.hallowed.oldways.client.mixin.item;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.map.MapDecoration;
import net.minecraft.item.map.MapDecorationType;
import net.minecraft.item.map.MapDecorationTypes;
import net.minecraft.item.map.MapState;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MapState.class)
public class MapStateClientMixin {

    @Shadow @Final
    public RegistryKey<World> dimension;

    @Unique
    private static boolean isOffMap(RegistryEntry<MapDecorationType> t) {
        return t == MapDecorationTypes.PLAYER_OFF_MAP
                || t == MapDecorationTypes.PLAYER_OFF_LIMITS
                || t.equals(MapDecorationTypes.PLAYER_OFF_MAP)
                || t.equals(MapDecorationTypes.PLAYER_OFF_LIMITS);
    }

    @Unique
    private static byte rot(float yaw) {
        int n = ((int) MathHelper.wrapDegrees(yaw) + 360) % 360;
        return (byte) (Math.round(n / 22.5f) & 15);
    }

    @Inject(method = "getDecorations", at = @At("RETURN"), cancellable = true)
    private void oldways$normalizePlayerArrow(CallbackInfoReturnable<Iterable<MapDecoration>> cir) {
        Iterable<MapDecoration> in = cir.getReturnValue();
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.player == null || in == null) return;

        boolean isNether = this.dimension != null && this.dimension == World.NETHER;
        byte playerRot = rot(mc.player.getYaw());

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
                out.add(new MapDecoration(MapDecorationTypes.PLAYER, d.x(), d.z(), playerRot, d.name()));
            } else if (out != null) {
                out.add(d);
            }
        }

        if (out != null) {
            cir.setReturnValue(out);
        }
    }
}

package net.hallowed.oldways.client.mixin.betterf3;

import me.cominixo.betterf3.modules.BaseModule;
import me.cominixo.betterf3.modules.LocationModule;
import me.cominixo.betterf3.utils.DebugLine;
import net.hallowed.oldways.client.config.ClientConfigManager;
import net.hallowed.oldways.client.util.EnderCheckClient;
import net.hallowed.oldways.client.util.InventoryDeepScan;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = LocationModule.class, remap = false)
public abstract class LocationModuleMixin {
    @Inject(method = "update", at = @At("TAIL"), remap = false)
    private void oldways$gateFacingAndTime(MinecraftClient client, CallbackInfo ci) {
        PlayerEntity p = client.player;
        if (p == null) return;

        BaseModule self = (BaseModule)(Object)this;
        List<DebugLine> lines = self.lines();

        if (ClientConfigManager.f3NeedsCompass()) {
            boolean hasCompass = InventoryDeepScan.hasCompass(p) || EnderCheckClient.enderHasCompass();
            if (!hasCompass) {
                String[] compassIds = { "facing", "rotation" };
                String compassMsg = "You need compass to display this information";
                for (String id : compassIds) {
                    DebugLine line = find(lines, id);
                    if (line != null) line.value(compassMsg);
                }
            }
        }

        if (ClientConfigManager.f3NeedsClock()) {
            boolean hasClock = InventoryDeepScan.hasClock(p) || EnderCheckClient.enderHasClock();
            if (!hasClock) {
                String[] clockIds = { "day_ticks", "days_played" };
                String clockMsg = "You need clock to display this information";
                for (String id : clockIds) {
                    DebugLine line = find(lines, id);
                    if (line != null) line.value(clockMsg);
                }
            }
        }
    }

    @Unique
    private static DebugLine find(List<DebugLine> list, String id) {
        for (DebugLine l : list) if (l != null && id.equals(l.id())) return l;
        return null;
    }
}

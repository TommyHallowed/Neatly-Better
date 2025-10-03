package net.hallowed.oldways.client.mixin.betterf3;

import me.cominixo.betterf3.modules.BaseModule;
import me.cominixo.betterf3.modules.CoordsModule;
import me.cominixo.betterf3.utils.DebugLine;
import me.cominixo.betterf3.utils.Utils;
import net.hallowed.oldways.client.config.ClientConfigManager;
import net.hallowed.oldways.client.util.EnderCheckClient;
import net.hallowed.oldways.client.util.InventoryDeepScan;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = CoordsModule.class, remap = false)
public abstract class CoordsModuleMixin {
    @Shadow public TextColor colorX;
    @Shadow public TextColor colorY;
    @Shadow public TextColor colorZ;

    @Unique private boolean oldways$gated = false;

    @Inject(method = "update", at = @At("TAIL"))
    private void oldways$gateCoords(MinecraftClient client, CallbackInfo ci) {
        if (!ClientConfigManager.f3NeedsCompass()) return;

        PlayerEntity p = client.player;
        if (p == null) return;

        boolean hasCompass = InventoryDeepScan.hasCompass(p) || EnderCheckClient.enderHasCompass();

        BaseModule self = (BaseModule)(Object)this;
        DebugLine l0 = self.line("player_coords");
        DebugLine l1 = self.line("block_coords");
        DebugLine l2 = self.line("chunk_relative_coords");
        DebugLine l3 = self.line("chunk_coords");
        if (l0 == null || l1 == null || l2 == null || l3 == null) return;

        if (!hasCompass) {
            if (!oldways$gated) {
                l0.name("");
                l0.format("format.betterf3.default_no_colon");
                l1.name(null);
                l2.name(null);
                l3.name(null);
                l1.format("format.betterf3.default_format");
                l2.format("format.betterf3.default_format");
                l3.format("format.betterf3.default_format");
                oldways$gated = true;
            }

            Text xyz = Utils.styledText("X", this.colorX)
                    .append(Utils.styledText("Y", this.colorY))
                    .append(Utils.styledText("Z", this.colorZ))
                    .append(Utils.styledText(":", this.colorX));

            Text msg = Utils.styledText("You need compass to display this information", self.valueColor);

            l0.value(List.of(xyz, msg));
            l1.value(List.of(msg));
            l2.value(List.of(msg));
            l3.value(List.of(msg));
        } else if (oldways$gated) {
            l0.name(null);
            l1.name(null);
            l2.name(null);
            l3.name(null);
            l0.format("format.betterf3.coords");
            l1.format("format.betterf3.coords");
            l2.format("format.betterf3.coords");
            l3.format("format.betterf3.coords");
            oldways$gated = false;
        }
    }
}

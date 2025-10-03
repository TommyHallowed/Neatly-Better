package net.hallowed.oldways.client.mixin.betterf3;

import me.cominixo.betterf3.modules.BaseModule;
import me.cominixo.betterf3.modules.TargetModule;
import me.cominixo.betterf3.utils.DebugLine;
import net.hallowed.oldways.client.config.ClientConfigManager;
import net.hallowed.oldways.client.util.EnderCheckClient;
import net.hallowed.oldways.client.util.InventoryDeepScan;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TargetModule.class, remap = false)
public abstract class TargetModuleMixin {

    @Inject(method = "update", at = @At("TAIL"))
    private void oldways$gateTargeting(MinecraftClient client, CallbackInfo ci) {
        if (!ClientConfigManager.f3NeedsCompass()) return;

        PlayerEntity p = client.player;
        if (p == null) return;

        boolean hasCompass = InventoryDeepScan.hasCompass(p) || EnderCheckClient.enderHasCompass();
        if (hasCompass) return;

        BaseModule self = (BaseModule) (Object) this;

        String msg = "You need compass to display this information";

        DebugLine targetedBlock  = self.line("targeted_block");
        DebugLine targetedFluid  = self.line("targeted_fluid");

        if (targetedBlock != null)  targetedBlock.value(msg);
        if (targetedFluid != null)  targetedFluid.value(msg);
    }
}

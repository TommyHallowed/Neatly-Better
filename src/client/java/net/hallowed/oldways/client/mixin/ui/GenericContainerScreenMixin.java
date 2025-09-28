package net.hallowed.oldways.client.mixin.ui;

import net.hallowed.oldways.client.network.OldWaysNetworkClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** When the Ender Chest UI opens, send one C2S check. */
@Mixin(GenericContainerScreen.class)
public abstract class GenericContainerScreenMixin extends HandledScreen<GenericContainerScreenHandler> {

    protected GenericContainerScreenMixin(GenericContainerScreenHandler handler, PlayerInventory inv, Text title) {
        super(handler, inv, title);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void oldways$onConstruct(GenericContainerScreenHandler handler,
                                     PlayerInventory inv,
                                     Text title,
                                     CallbackInfo ci) {
        boolean isEnderChest =
                title.getContent() instanceof TranslatableTextContent tc
                        && "container.enderchest".equals(tc.getKey());

        if (isEnderChest) {
            OldWaysNetworkClient.sendEnderCheck(); // one tiny packet per open
        }
    }
}

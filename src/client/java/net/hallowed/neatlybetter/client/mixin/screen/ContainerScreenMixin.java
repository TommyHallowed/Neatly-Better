package net.hallowed.neatlybetter.client.mixin.screen;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;

import org.jetbrains.annotations.NotNull;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ContainerScreen.class)
public abstract class ContainerScreenMixin extends AbstractContainerScreen<@NotNull ChestMenu> {

    protected ContainerScreenMixin(ChestMenu handler, Inventory inv, Component title) {
        super(handler, inv, title);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void neatlybetter$onConstruct(ChestMenu handler,
                                     Inventory inv,
                                     Component title,
                                     CallbackInfo ci) {
        boolean isEnderChest =
                title.getContents() instanceof TranslatableContents tc
                        && "container.enderchest".equals(tc.getKey());

        if (isEnderChest) {
            net.hallowed.neatlybetter.client.network.NTNetworkClient.sendEnderCheck();
        }
    }
}

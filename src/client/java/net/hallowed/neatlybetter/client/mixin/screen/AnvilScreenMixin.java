package net.hallowed.neatlybetter.client.mixin.screen;

import net.hallowed.neatlybetter.config.NTServerConfig;

import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.client.gui.screens.inventory.ItemCombinerScreen;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AnvilMenu;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(AnvilScreen.class)
public abstract class AnvilScreenMixin extends ItemCombinerScreen<AnvilMenu> {
    public AnvilScreenMixin(AnvilMenu handler, Inventory inventory, Component title) {
        super(handler, inventory, title, null);
    }

    // ── anvilNoTooExpensive: only hide the label when the server also lifts the limit ──
    @ModifyConstant(method = "extractLabels", constant = @Constant(intValue = 40))
    private int neatlybetter$neverShowTooExpensive(int original) {
        return NTServerConfig.CONFIG.anvilNoTooExpensive.isTrue() ? Integer.MAX_VALUE : original;
    }

    // ── anvilRenameColors: show '§' codes as the '&' codes the player typed ──
    // The name box would otherwise hold raw '§' characters, which render as formatting
    // and can't be typed or edited. validateName turns '&' back into '§', so an
    // untouched name still matches the item and isn't counted as a rename.
    @ModifyArg(
            method = "slotChanged",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/EditBox;setValue(Ljava/lang/String;)V")
    )
    private String neatlybetter$showColorCodesAsAmpersand(String value) {
        if (NTServerConfig.CONFIG.anvilRenameColors.isFalse()) return value;
        return value.replace('§', '&');
    }
}

package net.hallowed.oldways.client.mixin.ui;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import net.minecraft.client.gui.components.AbstractScrollArea;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.input.MouseButtonEvent;

@Mixin(ContainerEventHandler.class)
public interface ParentElementMixin {

    // Shadows the children() method from ParentElement
    @Shadow
    List<? extends GuiEventListener> children();

    // Injects into mouseClicked(Click click, boolean doubled)
    @Inject(method = "mouseClicked", at = @At(value = "RETURN", ordinal = 1))
    default void ow$forceUnfocus(MouseButtonEvent click, boolean doubled, CallbackInfoReturnable<Boolean> cir) {
        this.children().forEach(element -> {
            // EditBox -> TextFieldWidget | AbstractScrollArea -> ScrollableWidget
            if (!(element instanceof EditBox) && !(element instanceof AbstractScrollArea)) {
                element.setFocused(false);
            }
        });
    }

    // Injects into mouseReleased(Click click)
    @Inject(method = "mouseReleased", at = @At("RETURN"))
    default void ow$unfocusOnRelease(MouseButtonEvent click, CallbackInfoReturnable<Boolean> cir) {
        this.children().forEach(element -> {
            if (!(element instanceof EditBox) && !(element instanceof AbstractScrollArea)) {
                element.setFocused(false);
            }
        });
    }
}
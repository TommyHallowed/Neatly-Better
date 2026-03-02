package net.hallowed.oldways.client.mixin.gui;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.widget.ScrollableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ParentElement.class)
public interface ParentElementMixin {

    // Shadows the children() method from ParentElement
    @Shadow
    List<? extends Element> children();

    // Injects into mouseClicked(Click click, boolean doubled)
    @Inject(method = "mouseClicked", at = @At(value = "RETURN", ordinal = 1))
    default void ow$forceUnfocus(Click click, boolean doubled, CallbackInfoReturnable<Boolean> cir) {
        this.children().forEach(element -> {
            // EditBox -> TextFieldWidget | AbstractScrollArea -> ScrollableWidget
            if (!(element instanceof TextFieldWidget) && !(element instanceof ScrollableWidget)) {
                element.setFocused(false);
            }
        });
    }

    // Injects into mouseReleased(Click click)
    @Inject(method = "mouseReleased", at = @At("RETURN"))
    default void ow$unfocusOnRelease(Click click, CallbackInfoReturnable<Boolean> cir) {
        this.children().forEach(element -> {
            if (!(element instanceof TextFieldWidget) && !(element instanceof ScrollableWidget)) {
                element.setFocused(false);
            }
        });
    }
}
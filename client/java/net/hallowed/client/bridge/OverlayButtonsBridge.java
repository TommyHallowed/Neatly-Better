package net.hallowed.client.bridge;

import net.minecraft.client.gui.widget.ButtonWidget;

/** Plain helper interface (NOT a mixin). */
public interface OverlayButtonsBridge {
    ButtonWidget hallowed$getCoordsBtn();
    ButtonWidget hallowed$getTimeBtn();
}

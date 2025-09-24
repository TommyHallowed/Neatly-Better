package net.hallowed.oldways.client.ui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/** Invisible button: no vanilla skin, no texture; mixins handle the rendering. */
public class TextureButtonWidget extends ButtonWidget {
    public TextureButtonWidget(int x, int y, int w, int h, Text label, PressAction onPress) {
        super(x, y, w, h, label, onPress, DEFAULT_NARRATION_SUPPLIER);
    }

    @Override
    protected void renderWidget(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Intentionally no draw — HandledScreenButtonsSupportMixin renders the icon + outline.
    }
}

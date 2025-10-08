package net.hallowed.oldways.client.feature.ui;

import java.util.function.BooleanSupplier;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/** Simple skinned button: renders only the face texture (shown/hidden). */
public class TextureButtonWidget extends ButtonWidget {

    private final Identifier faceShown;
    private final Identifier faceHidden;
    private final BooleanSupplier faceShownSupplier;

    public TextureButtonWidget(
            int x, int y, int width, int height,
            Identifier faceShown,
            Identifier faceHidden,
            BooleanSupplier faceShownSupplier,
            PressAction onPress
    ) {
        super(x, y, width, height, Text.empty(), onPress, DEFAULT_NARRATION_SUPPLIER);
        this.faceShown = faceShown;
        this.faceHidden = faceHidden;
        this.faceShownSupplier = faceShownSupplier;
    }

    @Override
    protected void renderWidget(DrawContext ctx, int mouseX, int mouseY, float delta) {
        int x = getX(), y = getY(), w = getWidth(), h = getHeight();

        // pick shown/hidden face and draw it (no tint, no outline)
        boolean shown = faceShownSupplier.getAsBoolean();
        Identifier face = shown ? faceShown : faceHidden;
        ctx.drawTexturedQuad(face, x, y, x + w, y + h, 0f, 1f, 0f, 1f);
    }
}

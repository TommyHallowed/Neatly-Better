package net.hallowed.oldways.client.feature.ui;

import java.util.function.BooleanSupplier;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.util.Identifier;

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
        super(x, y, width, height, net.minecraft.text.Text.empty(), onPress, ButtonWidget.DEFAULT_NARRATION_SUPPLIER);
        this.faceShown = faceShown;
        this.faceHidden = faceHidden;
        this.faceShownSupplier = faceShownSupplier;
    }

    @Override
    protected void drawIcon(DrawContext ctx, int mouseX, int mouseY, float delta) {
        int x = this.getX();
        int y = this.getY();
        int w = this.getWidth();
        int h = this.getHeight();

        boolean shown = this.faceShownSupplier.getAsBoolean();
        Identifier face = shown ? this.faceShown : this.faceHidden;

        // Uses the required RenderPipelines object as the first argument!
        ctx.drawTexture(RenderPipelines.GUI_TEXTURED, face, x, y, 0.0F, 0.0F, w, h, w, h);
    }
}
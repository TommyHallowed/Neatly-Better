package net.hallowed.neatlybetter.client.feature.ui;

import java.util.function.BooleanSupplier;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class TextureButtonWidget extends Button {

    private final Identifier faceShown;
    private final Identifier faceHidden;
    private final BooleanSupplier faceShownSupplier;

    public TextureButtonWidget(
            int x, int y, int width, int height,
            Identifier faceShown,
            Identifier faceHidden,
            BooleanSupplier faceShownSupplier,
            OnPress onPress
    ) {
        super(x, y, width, height, net.minecraft.network.chat.Component.empty(), onPress, Button.DEFAULT_NARRATION);
        this.faceShown = faceShown;
        this.faceHidden = faceHidden;
        this.faceShownSupplier = faceShownSupplier;
    }

    @Override
    protected void renderContents(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
        int x = this.getX();
        int y = this.getY();
        int w = this.getWidth();
        int h = this.getHeight();

        boolean shown = this.faceShownSupplier.getAsBoolean();
        Identifier face = shown ? this.faceShown : this.faceHidden;

        ctx.blit(RenderPipelines.GUI_TEXTURED, face, x, y, 0.0F, 0.0F, w, h, w, h);
    }
}
package net.hallowed.neatlybetter.client.util;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;

import org.joml.Matrix3x2f;

public record SubpixelTexturedQuad(
        RenderPipeline pipeline,
        TextureSetup textureSetup,
        Matrix3x2f pose,
        float x1, float y1,
        float x2, float y2,
        float u1, float u2,
        float v1, float v2,
        int color,
        ScreenRectangle scissorArea,
        ScreenRectangle bounds
) implements GuiElementRenderState {

    public SubpixelTexturedQuad(
            RenderPipeline pipeline,
            TextureSetup textureSetup,
            Matrix3x2f pose,
            float x1, float y1,
            float x2, float y2,
            float u1, float u2,
            float v1, float v2,
            int color,
            ScreenRectangle scissorArea
    ) {
        this(pipeline, textureSetup, pose,
                x1, y1, x2, y2,
                u1, u2, v1, v2,
                color, scissorArea,
                bounds(x1, y1, x2, y2, pose, scissorArea));
    }

    @Override
    public void buildVertices(VertexConsumer vertices) {
        vertices.addVertexWith2DPose(pose, x1, y1).setUv(u1, v1).setColor(color);
        vertices.addVertexWith2DPose(pose, x1, y2).setUv(u1, v2).setColor(color);
        vertices.addVertexWith2DPose(pose, x2, y2).setUv(u2, v2).setColor(color);
        vertices.addVertexWith2DPose(pose, x2, y1).setUv(u2, v1).setColor(color);
    }

    private static ScreenRectangle bounds(float x1, float y1, float x2, float y2,
                                          Matrix3x2f pose, ScreenRectangle scissor) {
        ScreenRectangle rect = new ScreenRectangle(
                Math.round(x1), Math.round(y1),
                Math.round(x2 - x1), Math.round(y2 - y1)
        ).transformMaxBounds(pose);
        return scissor != null ? scissor.intersection(rect) : rect;
    }
}
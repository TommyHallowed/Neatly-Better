package net.hallowed.neatlybetter.client.feature;

import net.hallowed.neatlybetter.tooltip.MapPreviewTooltip;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MapRenderer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2fStack;

public final class ClientMapPreviewTooltip implements ClientTooltipComponent {

    private static final int SIZE_PX = 96;
    private static final int SRC_BG = 128;
    private static final int FRAME_INSET_SRC = 7;
    private static final int MAP_SRC = 128;
    private static final int PADDING_BOTTOM = 6;

    private static final Identifier MAP_BG =
            Identifier.fromNamespaceAndPath("minecraft", "textures/map/map_background.png");

    private static final MapRenderState RENDER_STATE = new MapRenderState();

    private final MapId mapId;

    public ClientMapPreviewTooltip(MapPreviewTooltip data) {
        this.mapId = data.mapId();
    }

    @Override
    public int getWidth(@NotNull Font font) {
        return SIZE_PX;
    }

    @Override
    public int getHeight(@NotNull Font font) {
        return SIZE_PX + PADDING_BOTTOM;
    }

    @Override
    public void renderImage(@NotNull Font font, int x, int y, int width, int height, @NotNull GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        MapItemSavedData mapData = MapItem.getSavedData(mapId, mc.level);
        if (mapData == null) return;

        MapRenderer renderer = mc.getMapRenderer();
        renderer.extractRenderState(mapId, mapData, RENDER_STATE);
        if (RENDER_STATE.texture == null) return;

        float bgScale = SIZE_PX / (float) SRC_BG;
        float frameInsetPx = FRAME_INSET_SRC * bgScale;
        int offset = Math.round(frameInsetPx);
        int innerSizePx = Math.round(SIZE_PX - 2f * frameInsetPx);
        float mapScale = innerSizePx / (float) MAP_SRC;

        Matrix3x2fStack pose = guiGraphics.pose();

        pose.pushMatrix();
        pose.translate(x, y);
        pose.scale(bgScale, bgScale);
        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED, MAP_BG,
                0, 0,
                0f, 0f,
                SRC_BG, SRC_BG,
                SRC_BG, SRC_BG
        );
        pose.popMatrix();

        pose.pushMatrix();
        pose.translate(x + offset, y + offset);
        pose.scale(mapScale, mapScale);
        guiGraphics.submitMapRenderState(RENDER_STATE);
        pose.popMatrix();
    }
}
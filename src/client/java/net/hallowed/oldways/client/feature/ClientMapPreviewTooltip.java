package net.hallowed.oldways.client.feature;

import net.hallowed.oldways.tooltip.MapPreviewTooltip;
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

    // --- Layout constants (pixels) ---
    /** Display size of the entire tooltip image (background + map). */
    private static final int SIZE_PX = 96;
    /** Source size of map_background.png (128×128). */
    private static final int SRC_BG = 128;
    /** Inset of the parchment frame in source pixels. */
    private static final int FRAME_INSET_SRC = 7;
    /** Source size of the map content (128×128). */
    private static final int MAP_SRC = 128;
    /** Extra bottom padding below the image. */
    private static final int PADDING_BOTTOM = 6;

    /** Vanilla parchment background texture. */
    private static final Identifier MAP_BG =
            Identifier.fromNamespaceAndPath("minecraft", "textures/map/map_background.png");

    /**
     * Reusable render state — avoids allocating a new object every frame.
     * Safe because tooltip rendering is always on the main (render) thread.
     */
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

        // --- Resolve map data ---
        MapItemSavedData mapData = MapItem.getSavedData(mapId, mc.level);
        if (mapData == null) return;

        // --- Prepare render state (updates texture if map data changed) ---
        MapRenderer renderer = mc.getMapRenderer();
        renderer.extractRenderState(mapId, mapData, RENDER_STATE);
        if (RENDER_STATE.texture == null) return;

        // --- Compute layout ---
        float bgScale = SIZE_PX / (float) SRC_BG;          // 96/128 = 0.75
        float frameInsetPx = FRAME_INSET_SRC * bgScale;     // 7 * 0.75 = 5.25
        int offset = Math.round(frameInsetPx);               // 5
        int innerSizePx = Math.round(SIZE_PX - 2f * frameInsetPx); // ~86
        float mapScale = innerSizePx / (float) MAP_SRC;     // 86/128 ≈ 0.671

        Matrix3x2fStack pose = guiGraphics.pose();

        // --- Draw parchment background ---
        pose.pushMatrix();
        pose.translate(x, y);
        pose.scale(bgScale, bgScale);
        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED, MAP_BG,
                0, 0,        // dest x, y (in scaled space)
                0f, 0f,      // u, v (texture offset)
                SRC_BG, SRC_BG, // width, height
                SRC_BG, SRC_BG  // texture width, height
        );
        pose.popMatrix();

        // --- Draw map content inside the frame ---
        pose.pushMatrix();
        pose.translate(x + offset, y + offset);
        pose.scale(mapScale, mapScale);
        guiGraphics.submitMapRenderState(RENDER_STATE);
        pose.popMatrix();
    }
}
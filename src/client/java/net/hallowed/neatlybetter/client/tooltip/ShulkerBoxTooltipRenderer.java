package net.hallowed.neatlybetter.client.tooltip;

import net.hallowed.neatlybetter.client.util.ModTextures;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class ShulkerBoxTooltipRenderer implements ClientTooltipComponent {
    private static final int SLOT_SIZE = 18;
    private static final int COLUMNS  = 9;
    private static final int ROWS     = 3;
    private static final int BORDER  = 7;

    private static final int TEX_W = BORDER + COLUMNS * SLOT_SIZE + BORDER; // 162
    private static final int TEX_H = BORDER + ROWS    * SLOT_SIZE + BORDER; // 54

    private final List<ItemStack> items;
    private final @Nullable DyeColor color;

    public ShulkerBoxTooltipRenderer(List<ItemStack> items, @Nullable DyeColor color) {
        this.items = items;
        this.color = color;
    }

    @Override
    public int getWidth(@NonNull Font font) {
        return TEX_W;
    }

    @Override
    public int getHeight(@NonNull Font font) {
        return TEX_H + 2;
    }

    @Override
    public void extractImage(@NonNull Font font, int x, int y, int w, int h, GuiGraphicsExtractor graphics) {

        graphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                ModTextures.SHULKER_TOOLTIP,
                x, y,
                TEX_W, TEX_H,
                colorTintFor(color)
        );

        int slotCount = Math.min(items.size(), COLUMNS * ROWS);

        for (int i = 0; i < slotCount; i++) {
            ItemStack stack = items.get(i);
            if (stack == null || stack.isEmpty()) continue;

            int col  = i % COLUMNS;
            int row  = i / COLUMNS;
            int slotX = x + BORDER + col * SLOT_SIZE + 1;
            int slotY = y + BORDER + row * SLOT_SIZE + 1;

            graphics.item(stack, slotX, slotY);
            graphics.itemDecorations(font, stack, slotX, slotY);
        }
    }

    private static final int DEFAULT_SHULKER_TINT = 0xFFA96BD6;

    private static int colorTintFor(@Nullable DyeColor color) {
        if (color == null) return DEFAULT_SHULKER_TINT;
        return color.getTextureDiffuseColor();
    }
}
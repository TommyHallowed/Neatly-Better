package net.hallowed.neatlybetter.client.tooltip;

import net.hallowed.neatlybetter.client.util.ModTextures;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.world.item.ItemStack;

import org.jspecify.annotations.NonNull;

import java.util.List;

public class EnderChestTooltipRenderer implements ClientTooltipComponent {
    private static final int SLOT_SIZE = 18;
    private static final int COLUMNS  = 9;
    private static final int ROWS     = 3;
    private static final int BORDER  = 7;

    private static final int TEX_W = BORDER + COLUMNS * SLOT_SIZE + BORDER;
    private static final int TEX_H = BORDER + ROWS    * SLOT_SIZE + BORDER;

    private static final int ENDER_CHEST_TINT = 0xFF2C6E6A;

    private final List<ItemStack> items;

    public EnderChestTooltipRenderer(List<ItemStack> items) {
        this.items = items;
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
                ENDER_CHEST_TINT
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
}
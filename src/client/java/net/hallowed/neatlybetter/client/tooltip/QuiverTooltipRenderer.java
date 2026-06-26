package net.hallowed.neatlybetter.client.tooltip;

import net.hallowed.neatlybetter.content.component.QuiverContents;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class QuiverTooltipRenderer implements ClientTooltipComponent {

    private static final Identifier PROGRESSBAR_BORDER_SPRITE = Identifier.withDefaultNamespace("container/bundle/bundle_progressbar_border");
    private static final Identifier PROGRESSBAR_FILL_SPRITE   = Identifier.withDefaultNamespace("container/bundle/bundle_progressbar_fill");
    private static final Identifier PROGRESSBAR_FULL_SPRITE   = Identifier.withDefaultNamespace("container/bundle/bundle_progressbar_full");
    private static final Identifier SLOT_HIGHLIGHT_BACK_SPRITE  = Identifier.withDefaultNamespace("container/bundle/slot_highlight_back");
    private static final Identifier SLOT_HIGHLIGHT_FRONT_SPRITE = Identifier.withDefaultNamespace("container/bundle/slot_highlight_front");
    private static final Identifier SLOT_BACKGROUND_SPRITE      = Identifier.withDefaultNamespace("container/bundle/slot_background");

    private static final int SLOT_SIZE = 24;
    private static final int GRID_WIDTH = 96;
    private static final int PROGRESSBAR_HEIGHT = 13;
    private static final int PROGRESSBAR_FILL_MAX = 94;
    private static final int PROGRESSBAR_MARGIN_Y = 4;
    private static final int COLUMNS = 4;

    private final QuiverContents contents;

    public QuiverTooltipRenderer(final QuiverContents contents) {
        this.contents = contents;
    }

    @Override
    public int getHeight(final @NonNull Font font) {
        return itemGridHeight() + PROGRESSBAR_HEIGHT + 8;
    }

    @Override
    public int getWidth(final @NonNull Font font) {
        return GRID_WIDTH;
    }

    @Override
    public boolean showTooltipWithItemInHand() {
        return true;
    }

    private int slotCount() {
        return Math.min(QuiverContents.MAX_SLOTS, this.contents.size());
    }

    private int gridSizeY() {
        return Mth.positiveCeilDiv(slotCount(), COLUMNS);
    }

    private int itemGridHeight() {
        return gridSizeY() * SLOT_SIZE;
    }

    private static int getContentXOffset(final int tooltipWidth) {
        return (tooltipWidth - GRID_WIDTH) / 2;
    }

    @Override
    public void extractImage(final @NonNull Font font, final int x, final int y, final int w, final int h, final @NonNull GuiGraphicsExtractor graphics) {
        if (this.contents.isEmpty()) return;

        List<ItemStackTemplate> shownItems = this.contents.items();
        int xStartPos = x + getContentXOffset(w) + GRID_WIDTH;
        int yStartPos = y + gridSizeY() * SLOT_SIZE;
        int slotNumber = 1;

        for (int rowNumber = 1; rowNumber <= gridSizeY(); rowNumber++) {
            for (int columnNumber = 1; columnNumber <= COLUMNS; columnNumber++) {
                int drawX = xStartPos - columnNumber * SLOT_SIZE;
                int drawY = yStartPos - rowNumber * SLOT_SIZE;
                if (shownItems.size() >= slotNumber) {
                    extractSlot(slotNumber, drawX, drawY, shownItems, slotNumber, font, graphics);
                    slotNumber++;
                }
            }
        }

        extractSelectedItemTooltip(font, graphics, x, y, w);
        extractProgressbar(x + getContentXOffset(w), y + itemGridHeight() + PROGRESSBAR_MARGIN_Y, font, graphics);
    }

    private void extractSlot(final int slotNumber, final int drawX, final int drawY,
                             final List<ItemStackTemplate> shownItems, final int slotIndex,
                             final Font font, final GuiGraphicsExtractor graphics) {
        int itemVisualOrderIndex = shownItems.size() - slotNumber;
        boolean hasHighlight = itemVisualOrderIndex == this.contents.getSelectedItemIndex();
        ItemStack item = shownItems.get(itemVisualOrderIndex).create();

        if (hasHighlight) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_BACK_SPRITE, drawX, drawY, SLOT_SIZE, SLOT_SIZE);
        } else {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_BACKGROUND_SPRITE, drawX, drawY, SLOT_SIZE, SLOT_SIZE);
        }

        graphics.item(item, drawX + 4, drawY + 4, slotIndex);
        graphics.itemDecorations(font, item, drawX + 4, drawY + 4);

        if (hasHighlight) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_FRONT_SPRITE, drawX, drawY, SLOT_SIZE, SLOT_SIZE);
        }
    }

    private void extractSelectedItemTooltip(final Font font, final GuiGraphicsExtractor graphics,
                                            final int x, final int y, final int w) {
        ItemStackTemplate selectedItem = this.contents.getSelectedItem();
        if (selectedItem == null) return;

        ItemStack itemStack = selectedItem.create();
        Component selectedItemName = itemStack.getStyledHoverName();
        int textWidth = font.width(selectedItemName.getVisualOrderText());
        int centerTooltip = x + w / 2 - 12;
        ClientTooltipComponent nameTooltip = ClientTooltipComponent.create(selectedItemName.getVisualOrderText());
        graphics.tooltip(font, List.of(nameTooltip), centerTooltip - textWidth / 2, y - 15,
                DefaultTooltipPositioner.INSTANCE, itemStack.get(DataComponents.TOOLTIP_STYLE));
    }

    private void extractProgressbar(final int x, final int y, final Font font, final GuiGraphicsExtractor graphics) {
        float fullness = this.contents.fullness();
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, getProgressBarTexture(fullness),
                x + 1, y, getProgressBarFill(fullness), PROGRESSBAR_HEIGHT);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, PROGRESSBAR_BORDER_SPRITE,
                x, y, GRID_WIDTH, PROGRESSBAR_HEIGHT);
    }

    private static int getProgressBarFill(final float fullness) {
        return Mth.clamp((int) (fullness * PROGRESSBAR_FILL_MAX), 0, PROGRESSBAR_FILL_MAX);
    }

    private static Identifier getProgressBarTexture(final float fullness) {
        return fullness >= 1.0F ? PROGRESSBAR_FULL_SPRITE : PROGRESSBAR_FILL_SPRITE;
    }
}

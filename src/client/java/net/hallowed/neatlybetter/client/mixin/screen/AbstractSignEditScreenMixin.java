package net.hallowed.neatlybetter.client.mixin.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.cursor.CursorTypes;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.SignBlockEntity;

import org.joml.Vector3fc;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractSignEditScreen.class)
public abstract class AbstractSignEditScreenMixin extends Screen {

    // A line counts as "full" when none of these narrow characters would still fit
    @Unique private static final String NT$NARROW_PROBES = "i.,:;|!'";

    // Drag-selection state
    @Unique private boolean nt$dragging = false;
    @Unique private int nt$dragLine = 0;
    @Unique private int nt$dragAnchor = 0;

    @Shadow @Final protected SignBlockEntity sign;
    @Shadow @Final private String[] messages;
    @Shadow @Final private TextFieldHelper signField;
    @Shadow private int line;

    @Shadow protected abstract Vector3fc getSignTextScale();
    @Shadow protected abstract float getSignYOffset();

    protected AbstractSignEditScreenMixin(Component title) {
        super(title);
    }

    // ===================== Shared hit-testing =====================

    /**
     * Returns the sign line nearest to the given GUI-space position,
     * or -1 if the position is outside the sign board.
     */
    @Unique
    private int nt$lineAt(double mouseX, double mouseY) {
        Vector3fc scale = this.getSignTextScale();
        int lineHeight = this.sign.getTextLineHeight();
        double halfHeight = 4 * lineHeight / 2.0;
        double halfWidth = this.sign.getMaxTextLineWidth() / 2.0;

        // Screen space -> the same text space extractSignText draws in
        double localX = (mouseX - this.width / 2.0) / scale.x();
        double localY = (mouseY - this.getSignYOffset()) / scale.y();

        if (Math.abs(localX) > halfWidth + 3
                || Math.abs(localY) > halfHeight + 4) {
            return -1;
        }

        // Clicks in the board margin snap to the nearest line
        int hoveredLine = (int) Math.floor((localY + halfHeight) / lineHeight);
        return Math.clamp(hoveredLine, 0, this.messages.length - 1);
    }

    /** Returns the character boundary on the given line closest to the given GUI-space X. */
    @Unique
    private int nt$indexAt(int lineIndex, double mouseX) {
        Vector3fc scale = this.getSignTextScale();
        double localX = (mouseX - this.width / 2.0) / scale.x();

        String text = this.messages[lineIndex];
        double relativeX = localX + this.font.width(text) / 2.0;

        int bestIndex = 0;
        double bestDistance = Double.MAX_VALUE;
        for (int i = 0; i <= text.length(); i++) {
            double distance = Math.abs(this.font.width(text.substring(0, i)) - relativeX);
            if (distance < bestDistance) {
                bestDistance = distance;
                bestIndex = i;
            }
        }
        return bestIndex;
    }

    // ===================== Text cursor over the editable area =====================

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void nt$requestTextCursor(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        // Don't override the cursor of the Done button or other widgets
        if (this.getChildAt(mouseX, mouseY).isPresent()) return;

        if (this.nt$lineAt(mouseX, mouseY) >= 0) {
            graphics.requestCursor(CursorTypes.IBEAM);
        }
    }

    // ===================== Auto-wrap when a line fills up =====================

    /** True if typing at the end of a non-empty, non-last line with no selection could wrap. */
    @Unique
    private boolean nt$canAutoWrap(int codepoint) {
        if (this.line >= this.messages.length - 1) return false;
        if (Character.isISOControl(codepoint)) return false;

        String text = this.messages[this.line];
        if (text.isEmpty()) return false;

        int cursor = this.signField.getCursorPos();
        return cursor == this.signField.getSelectionPos() && cursor >= text.length();
    }

    /** True if at least one narrow character still fits on the end of this text. */
    @Unique
    private boolean nt$canFitMore(String text) {
        int max = this.sign.getMaxTextLineWidth();
        for (char probe : NT$NARROW_PROBES.toCharArray()) {
            if (this.font.width(text + probe) <= max) return true;
        }
        return false;
    }

    // A character that doesn't fit moves to the start of the next line, where vanilla then inserts it
    @Inject(method = "charTyped", at = @At("HEAD"))
    private void nt$overflowToNextLine(CharacterEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (!this.nt$canAutoWrap(event.codepoint())) return;

        String typed = new String(Character.toChars(event.codepoint()));
        if (this.font.width(this.messages[this.line] + typed) > this.sign.getMaxTextLineWidth()) {
            this.line++;
            this.signField.setCursorToStart();
        }
    }

    // After a character is inserted, a line that is now full moves the cursor to the next line
    @Inject(method = "charTyped", at = @At("RETURN"))
    private void nt$wrapWhenFull(CharacterEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (!this.nt$canAutoWrap(event.codepoint())) return;

        if (!this.nt$canFitMore(this.messages[this.line])) {
            this.line++;
            this.signField.setCursorToStart();
        }
    }

    // ===================== Backspace on an empty line goes up =====================

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void nt$backspaceOnEmptyLine(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (event.key() != InputConstants.KEY_BACKSPACE) return;
        if (this.line <= 0 || !this.messages[this.line].isEmpty()) return;

        this.line--;
        this.signField.setCursorToEnd();
        cir.setReturnValue(true);
    }

    // ===================== Arrow keys wrap between lines =====================

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void nt$wrapCursorBetweenLines(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        // Leave selection (shift) and word-jump (ctrl) shortcuts to vanilla
        if (event.hasShiftDown() || event.hasControlDown()) return;

        // Don't wrap away from an empty line
        if (this.messages[this.line].isEmpty()) return;

        int cursor = this.signField.getCursorPos();
        // Only wrap when there is no active selection
        if (cursor != this.signField.getSelectionPos()) return;

        if (event.isRight() && cursor >= this.messages[this.line].length() && this.line < this.messages.length - 1) {
            this.line++;
            this.signField.setCursorToStart();
            cir.setReturnValue(true);
        } else if (event.isLeft() && cursor <= 0 && this.line > 0) {
            this.line--;
            this.signField.setCursorToEnd();
            cir.setReturnValue(true);
        }
    }

    // ===================== Click / double click / drag selection =====================

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean doubleClick) {
        // Let the Done button and other widgets handle their own clicks first
        if (super.mouseClicked(event, doubleClick)) return true;
        if (event.button() != 1) return false;

        int clickedLine = this.nt$lineAt(event.x(), event.y());
        if (clickedLine < 0) return false;

        this.line = clickedLine;

        // Double click: select the whole line (no drag follows)
        if (doubleClick) {
            this.nt$dragging = false;
            this.signField.setSelectionRange(this.messages[clickedLine].length(), 0);
            return true;
        }

        // Single click: place the cursor and start a possible drag
        int index = this.nt$indexAt(clickedLine, event.x());
        this.nt$dragging = true;
        this.nt$dragLine = clickedLine;
        this.nt$dragAnchor = index;
        this.signField.setSelectionRange(index, index);
        return true;
    }

    @Override
    public boolean mouseDragged(@NonNull MouseButtonEvent event, double dx, double dy) {
        if (!this.nt$dragging || event.button() != 1) {
            return super.mouseDragged(event, dx, dy);
        }

        // Selection stays on the line where the drag started, extending to the pointer's X
        int index = this.nt$indexAt(this.nt$dragLine, event.x());
        this.signField.setSelectionRange(index, this.nt$dragAnchor);
        return true;
    }

    @Override
    public boolean mouseReleased(@NonNull MouseButtonEvent event) {
        if (this.nt$dragging && event.button() == 1) {
            this.nt$dragging = false;
            return true;
        }
        return super.mouseReleased(event);
    }
}
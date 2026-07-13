package net.hallowed.neatlybetter.client.screen;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;

import net.hallowed.neatlybetter.client.config.NTClientConfig;
import net.hallowed.neatlybetter.client.util.FullscreenGuard;
import net.hallowed.neatlybetter.client.util.ModTextures;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import javax.imageio.ImageIO;
import java.awt.Desktop;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

public class ScreenshotManagerScreen extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Component TITLE = Component.literal("Screenshots");
    private static final Component DONE = Component.literal("Done");
    private static final Component DELETE = Component.literal("Delete");
    private static final Component OPEN_FOLDER = Component.literal("Open Folder");
    private static final Component EMPTY_HINT = Component.literal("No screenshots yet");
    private static final Component COPY = Component.literal("Copy");
    private static final Component RENAME = Component.literal("Rename");
    private static final Component PREV_ICON = Component.literal("<");
    private static final Component NEXT_ICON = Component.literal(">");
    private static final Component PREVIEW_CLOSE_ICON = Component.literal("x");
    private static final Component COPY_TOGGLE_MESSAGE = Component.literal("Toggle Copy Taken Screenshots");

    private static final int ICON_SIZE = 15;

    private static final int TILE_WIDTH = 128;
    private static final int TILE_HEIGHT = 72;
    private static final int LABEL_HEIGHT = 14;
    private static final int TILE_SPACING = 8;
    private static final int ROW_HEIGHT = TILE_HEIGHT + LABEL_HEIGHT + TILE_SPACING;

    private static final float FILENAME_TEXT_SCALE = 0.75F;
    private static final float TIMESTAMP_TEXT_SCALE = 0.5F;
    private static final int TIMESTAMP_MARGIN = 2;

    private static final int SQUARE_BUTTON_SIZE = 20;
    private static final int DONE_BUTTON_WIDTH = 80;

    private static final int PREVIEW_BACKDROP_COLOR = 0xC8000000;
    private static final int PREVIEW_IMAGE_SHRINK = 6;
    private static final int PREVIEW_ARROW_MARGIN = 18;
    private static final int PREVIEW_NAME_FIELD_WIDTH = 240;
    private static final int PREVIEW_NAME_FIELD_HEIGHT = 14;

    private static final DateTimeFormatter SCREENSHOT_FILENAME_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss", Locale.ROOT);
    private static final DateTimeFormatter TIMESTAMP_DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.ROOT);

    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private final Screen parent;
    private final Path screenshotsDir;

    // Static so thumbnails/timestamps stay loaded across screen open/close, avoiding re-decoding images every time.
    private static final Map<Path, Thumbnail> thumbnailCache = new HashMap<>();
    private static final Map<Path, String> timestampCache = new HashMap<>();
    private final Set<Path> selected = new LinkedHashSet<>();

    private @Nullable ScreenshotGridList grid;
    private @Nullable SpriteIconButton deleteButton;
    private @Nullable SpriteIconButton copyScreenshotsToggleButton;
    private @Nullable StringWidget emptyHint;
    private @Nullable StringWidget headerTitleWidget;
    private @Nullable LinearLayout footer;
    private List<Path> files = List.of();

    private @Nullable Path previewFile;
    private @Nullable SpriteIconButton previewCopyButton;
    private @Nullable Button previewDoneButton;
    private @Nullable SpriteIconButton previewDeleteButton;
    private @Nullable EditBox previewNameField;
    private @Nullable Path previewNameFieldFile;
    private @Nullable NavArrowButton previewPrevButton;
    private @Nullable NavArrowButton previewNextButton;
    private @Nullable NavArrowButton previewCloseButton;
    private int previewImageX0;
    private int previewImageY0;
    private int previewImageWidth;
    private int previewImageHeight;
    private int previewBandTop;
    private int previewBandBottom;

    public ScreenshotManagerScreen(Screen parent) {
        super(TITLE);
        this.parent = parent;
        this.screenshotsDir = this.minecraft.gameDirectory.toPath().resolve("screenshots");
    }

    @Override
    protected void init() {
        this.layout.setHeaderHeight(33);
        LinearLayout header = this.layout.addToHeader(LinearLayout.horizontal().spacing(4));
        this.headerTitleWidget = header.addChild(new StringWidget(this.getTitle(), this.font));

        this.files = scanScreenshots();

        this.grid = this.layout.addToContents(new ScreenshotGridList(this.minecraft, this.width, this.height));
        this.grid.setFiles(this.files);

        if (this.files.isEmpty()) {
            this.emptyHint = new StringWidget(EMPTY_HINT, this.font);
        }

        this.footer = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
        this.deleteButton = this.footer.addChild(SpriteIconButton.builder(DELETE, button -> this.deleteSelected(), true)
                .width(SQUARE_BUTTON_SIZE)
                .sprite(ModTextures.DELETE_ICON, ICON_SIZE, ICON_SIZE)
                .withTootip()
                .build());
        this.deleteButton.active = !this.selected.isEmpty();

        this.footer.addChild(Button.builder(DONE, button -> this.onClose())
                .width(DONE_BUTTON_WIDTH)
                .build());

        this.footer.addChild(SpriteIconButton.builder(OPEN_FOLDER, button -> this.openFolder(), true)
                .width(SQUARE_BUTTON_SIZE)
                .sprite(ModTextures.OPEN_FOLDER_ICON, ICON_SIZE, ICON_SIZE)
                .withTootip()
                .build());

        this.layout.visitWidgets(this::addRenderableWidget);
        if (this.emptyHint != null) {
            this.addRenderableWidget(this.emptyHint);
        }

        this.copyScreenshotsToggleButton = SpriteIconButton.builder(COPY_TOGGLE_MESSAGE,
                        button -> this.toggleCopyScreenshots(), true)
                .width(SQUARE_BUTTON_SIZE)
                .sprite(ModTextures.COPY_TAKEN_ICON, ICON_SIZE, ICON_SIZE)
                .withTootip()
                .build();
        this.copyScreenshotsToggleButton.setTooltip(Tooltip.create(this.copyScreenshotsTooltip()));
        this.copyScreenshotsToggleButton.setPosition(4, 4);
        this.addRenderableWidget(this.copyScreenshotsToggleButton);

        this.previewCopyButton = SpriteIconButton.builder(COPY, button -> this.copyPreviewToClipboard(), true)
                .width(SQUARE_BUTTON_SIZE)
                .sprite(ModTextures.COPY_ICON, ICON_SIZE, ICON_SIZE)
                .withTootip()
                .build();
        this.previewDoneButton = Button.builder(DONE, button -> this.closePreview())
                .width(DONE_BUTTON_WIDTH)
                .build();
        this.previewDeleteButton = SpriteIconButton.builder(DELETE, button -> this.deletePreviewedFile(), true)
                .width(SQUARE_BUTTON_SIZE)
                .sprite(ModTextures.DELETE_ICON, ICON_SIZE, ICON_SIZE)
                .withTootip()
                .build();
        this.previewNameField = new EditBox(this.font, 0, 0,
                PREVIEW_NAME_FIELD_WIDTH, PREVIEW_NAME_FIELD_HEIGHT, RENAME);
        this.previewNameField.setBordered(false);
        this.previewNameField.setCentered(true);
        this.previewNameField.setTextColor(-1);
        this.previewNameField.setMaxLength(96);
        this.previewPrevButton = new NavArrowButton(0, 0, SQUARE_BUTTON_SIZE, PREV_ICON, this.font,
                () -> this.navigatePreview(-1));
        this.previewNextButton = new NavArrowButton(0, 0, SQUARE_BUTTON_SIZE, NEXT_ICON, this.font,
                () -> this.navigatePreview(1));
        this.previewCloseButton = new NavArrowButton(0, 0, SQUARE_BUTTON_SIZE, PREVIEW_CLOSE_ICON, this.font,
                this::closePreview);
        this.updatePreviewLayout();

        this.repositionElements();
    }


    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
        if (this.grid != null) {
            this.grid.updateSize(this.width, this.layout);
        }
        if (this.emptyHint != null) {
            this.emptyHint.setX(this.width / 2 - this.emptyHint.getWidth() / 2);
            this.emptyHint.setY(this.layout.getHeaderHeight() + 20);
        }
        this.updatePreviewLayout();
    }

    private void updatePreviewLayout() {
        if (this.previewFile == null || this.previewDoneButton == null || this.previewCopyButton == null
                || this.previewDeleteButton == null || this.previewNameField == null) {
            return;
        }

        if (!Objects.equals(this.previewNameFieldFile, this.previewFile)) {
            this.previewNameFieldFile = this.previewFile;
            this.previewNameField.setValue(stripExtension(this.previewFile.getFileName().toString()));
        }
        this.previewNameField.setX((this.width - PREVIEW_NAME_FIELD_WIDTH) / 2);
        this.previewNameField.setY(
                this.headerTitleWidget != null ? this.headerTitleWidget.getY() : this.layout.getHeaderHeight() / 2);

        Thumbnail thumbnail = this.getOrLoadThumbnail(this.previewFile);

        int footerTop = this.footer != null ? this.footer.getY() : this.height - SQUARE_BUTTON_SIZE;
        int contentTop = this.layout.getHeaderHeight() - 1;
        int fixedHeight = Math.max(1, footerTop - contentTop - PREVIEW_IMAGE_SHRINK);

        int drawWidth = Math.max(1, Math.round(fixedHeight * ((float) thumbnail.width() / thumbnail.height())));

        this.previewImageWidth = drawWidth;
        this.previewImageHeight = fixedHeight;
        this.previewImageX0 = (this.width - drawWidth) / 2;
        this.previewImageY0 = contentTop;
        this.previewBandTop = contentTop;
        this.previewBandBottom = footerTop;

        int centerX = this.width / 2;
        this.previewDoneButton.setPosition(centerX - DONE_BUTTON_WIDTH / 2, footerTop);
        this.previewDeleteButton.setPosition(centerX - DONE_BUTTON_WIDTH / 2 - 8 - SQUARE_BUTTON_SIZE, footerTop);
        this.previewCopyButton.setPosition(centerX + DONE_BUTTON_WIDTH / 2 + 8, footerTop);

        if (this.previewCloseButton != null) {
            this.previewCloseButton.setPosition(
                    this.previewImageX0 + this.previewImageWidth - SQUARE_BUTTON_SIZE, this.previewImageY0);
        }

        if (this.previewPrevButton != null && this.previewNextButton != null) {
            int bandHeight = footerTop - contentTop;
            int navY = contentTop + (bandHeight - SQUARE_BUTTON_SIZE) / 2;
            this.previewPrevButton.setPosition(PREVIEW_ARROW_MARGIN, navY);
            this.previewNextButton.setPosition(this.width - PREVIEW_ARROW_MARGIN - SQUARE_BUTTON_SIZE, navY);
        }
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(this.parent);
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        if (this.previewFile != null) {
            super.extractRenderState(graphics, -9999, -9999, a);
            this.extractPreviewOverlay(graphics, mouseX, mouseY, a);
        } else {
            super.extractRenderState(graphics, mouseX, mouseY, a);
        }
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean doubleClick) {
        if (this.previewFile != null) {
            if (this.previewNameField != null && this.previewNameField.mouseClicked(event, doubleClick)) {
                this.previewNameField.setFocused(true);
                return true;
            }
            if (this.previewNameField != null && this.previewNameField.isFocused()) {
                this.previewNameField.setFocused(false);
                this.commitPreviewRename();
            }
            if (this.previewCopyButton != null && this.previewCopyButton.mouseClicked(event, doubleClick)) {
                return true;
            }
            if (this.previewDoneButton != null && this.previewDoneButton.mouseClicked(event, doubleClick)) {
                return true;
            }
            if (this.previewDeleteButton != null && this.previewDeleteButton.mouseClicked(event, doubleClick)) {
                return true;
            }
            if (this.previewPrevButton != null && this.previewPrevButton.mouseClicked(event, doubleClick)) {
                return true;
            }
            if (this.previewNextButton != null && this.previewNextButton.mouseClicked(event, doubleClick)) {
                return true;
            }
            if (this.previewCloseButton != null && this.previewCloseButton.mouseClicked(event, doubleClick)) {
                return true;
            }
            if (event.buttonInfo().button() == 0 && this.isInPreviewMargin(event.x(), event.y())) {
                this.closePreview();
                return true;
            }
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    private boolean isInPreviewMargin(double mouseX, double mouseY) {
        if (mouseY < this.previewBandTop || mouseY >= this.previewBandBottom) {
            return false;
        }
        return mouseX < this.previewImageX0 || mouseX >= this.previewImageX0 + this.previewImageWidth;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (this.previewFile != null) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(@NonNull KeyEvent event) {
        if (this.previewFile != null) {
            if (this.previewNameField != null && this.previewNameField.isFocused()) {
                if (event.key() == GLFW.GLFW_KEY_ENTER || event.key() == GLFW.GLFW_KEY_KP_ENTER) {
                    this.previewNameField.setFocused(false);
                    this.commitPreviewRename();
                    return true;
                }
                if (event.isEscape()) {
                    this.previewNameField.setFocused(false);
                    if (this.previewNameFieldFile != null) {
                        this.previewNameField.setValue(stripExtension(this.previewNameFieldFile.getFileName().toString()));
                    }
                    return true;
                }
                this.previewNameField.keyPressed(event);
                return true;
            }
            if (event.isEscape()) {
                this.closePreview();
                return true;
            }
            if (event.hasControlDown() && event.key() == GLFW.GLFW_KEY_C) {
                this.copyPreviewToClipboard();
                return true;
            }
        }
        if (this.previewFile == null && event.key() == GLFW.GLFW_KEY_DELETE && !this.selected.isEmpty()) {
            this.deleteSelected();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(@NonNull CharacterEvent event) {
        if (this.previewFile != null && this.previewNameField != null && this.previewNameField.isFocused()) {
            return this.previewNameField.charTyped(event);
        }
        return super.charTyped(event);
    }

    private void clearSelection() {
        if (this.selected.isEmpty()) {
            return;
        }
        this.selected.clear();
        if (this.deleteButton != null) {
            this.deleteButton.active = false;
        }
    }

    private List<Path> scanScreenshots() {
        if (!Files.isDirectory(this.screenshotsDir)) {
            return List.of();
        }

        try (Stream<Path> stream = Files.list(this.screenshotsDir)) {
            List<Path> files = stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".png"))
                    .sorted(Comparator.comparingLong(this::lastModifiedSafe).reversed())
                    .toList();
            return new ArrayList<>(files);
        } catch (IOException e) {
            LOGGER.warn("[NeatlyBetter/Screenshots] Failed to list screenshots directory {}", this.screenshotsDir, e);
            return List.of();
        }
    }

    private long lastModifiedSafe(Path path) {
        try {
            return Files.getLastModifiedTime(path).toMillis();
        } catch (IOException e) {
            return 0L;
        }
    }

    Thumbnail getOrLoadThumbnail(Path file) {
        Thumbnail cached = thumbnailCache.get(file);
        if (cached != null) {
            return cached;
        }

        try (InputStream in = Files.newInputStream(file)) {
            NativeImage image = NativeImage.read(in);
            Identifier id = Identifier.fromNamespaceAndPath("neatly-better",
                    "screenshot_thumb/" + Util.sanitizeName(file.getFileName().toString(), Identifier::validPathChar));
            this.minecraft.getTextureManager().register(id, new DynamicTexture(file::toString, image));
            Thumbnail thumbnail = new Thumbnail(id, image.getWidth(), image.getHeight());
            thumbnailCache.put(file, thumbnail);
            return thumbnail;
        } catch (IOException e) {
            LOGGER.warn("[NeatlyBetter/Screenshots] Failed to load thumbnail for {}", file, e);
            Thumbnail missing = new Thumbnail(MissingTextureAtlasSprite.getLocation(), 16, 16);
            thumbnailCache.put(file, missing);
            return missing;
        }
    }

    private record Thumbnail(Identifier id, int width, int height) {
    }

    String getOrComputeTimestamp(Path file) {
        return timestampCache.computeIfAbsent(file, this::resolveTimestamp);
    }

    private String resolveTimestamp(Path file) {
        String base = file.getFileName().toString();
        if (base.toLowerCase(Locale.ROOT).endsWith(".png")) {
            base = base.substring(0, base.length() - 4);
        }

        if (base.length() >= 19) {
            try {
                LocalDateTime parsed = LocalDateTime.parse(base.substring(0, 19), SCREENSHOT_FILENAME_TIME_FORMAT);
                return TIMESTAMP_DISPLAY_FORMAT.format(parsed);
            } catch (DateTimeParseException ignored) {
            }
        }

        try {
            Instant modified = Files.getLastModifiedTime(file).toInstant();
            return TIMESTAMP_DISPLAY_FORMAT.format(LocalDateTime.ofInstant(modified, ZoneId.systemDefault()));
        } catch (IOException e) {
            return "";
        }
    }

    void onTileLeftClick(Path file) {
        this.previewFile = file;
        this.updatePreviewLayout();
        AbstractWidget.playButtonClickSound(this.minecraft.getSoundManager());
    }

    void onTileRightClick(Path file) {
        if (!this.selected.remove(file)) {
            this.selected.add(file);
        }
        if (this.deleteButton != null) {
            this.deleteButton.active = !this.selected.isEmpty();
        }
        AbstractWidget.playButtonClickSound(this.minecraft.getSoundManager());
    }

    boolean isSelected(Path file) {
        return this.selected.contains(file);
    }

    private void closePreview() {
        this.previewFile = null;
    }

    private void navigatePreview(int delta) {
        Path current = this.previewFile;
        if (current == null || this.files.isEmpty()) {
            return;
        }
        int index = this.files.indexOf(current);
        if (index < 0) {
            return;
        }
        int nextIndex = index + delta;
        if (nextIndex < 0 || nextIndex >= this.files.size()) {
            return;
        }
        this.previewFile = this.files.get(nextIndex);
        this.updatePreviewLayout();
    }

    private void copyPreviewToClipboard() {
        Path file = this.previewFile;
        if (file == null) {
            return;
        }

        try {
            BufferedImage image = ImageIO.read(file.toFile());
            if (image == null) {
                LOGGER.warn("[NeatlyBetter/Screenshots] Could not decode {} for clipboard copy", file);
                return;
            }
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new ImageTransferable(image), null);
        } catch (IOException | HeadlessException e) {
            LOGGER.warn("[NeatlyBetter/Screenshots] Failed to copy {} to clipboard", file, e);
        }
    }

    private void extractPreviewOverlay(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        Path file = this.previewFile;
        if (file == null) {
            return;
        }

        graphics.fill(0, 0, this.width, this.height, PREVIEW_BACKDROP_COLOR);

        Thumbnail thumbnail = this.getOrLoadThumbnail(file);
        int x0 = this.previewImageX0;
        int y0 = this.previewImageY0;
        int x1 = x0 + this.previewImageWidth;
        int y1 = y0 + this.previewImageHeight;

        graphics.blit(thumbnail.id(), x0, y0, x1, y1, 0.0F, 1.0F, 0.0F, 1.0F);

        if (this.previewNameField != null) {
            this.previewNameField.extractRenderState(graphics, mouseX, mouseY, a);
        }

        if (this.previewCopyButton != null) {
            this.previewCopyButton.extractRenderState(graphics, mouseX, mouseY, a);
        }
        if (this.previewDoneButton != null) {
            this.previewDoneButton.extractRenderState(graphics, mouseX, mouseY, a);
        }
        if (this.previewDeleteButton != null) {
            this.previewDeleteButton.extractRenderState(graphics, mouseX, mouseY, a);
        }
        if (this.previewPrevButton != null) {
            this.previewPrevButton.extractRenderState(graphics, mouseX, mouseY, a);
        }
        if (this.previewNextButton != null) {
            this.previewNextButton.extractRenderState(graphics, mouseX, mouseY, a);
        }
        if (this.previewCloseButton != null) {
            this.previewCloseButton.extractRenderState(graphics, mouseX, mouseY, a);
        }
    }

    private void commitPreviewRename() {
        Path file = this.previewFile;
        if (file == null || this.previewNameField == null) {
            return;
        }

        String currentFileName = file.getFileName().toString();
        String currentBase = stripExtension(currentFileName);
        String extension = extensionOf(currentFileName);
        String newBase = sanitizeFileName(this.previewNameField.getValue());

        if (newBase.isEmpty() || newBase.equals(currentBase)) {
            this.previewNameField.setValue(currentBase);
            return;
        }

        Path target = this.screenshotsDir.resolve(newBase + extension);
        if (Files.exists(target)) {
            LOGGER.warn("[NeatlyBetter/Screenshots] Cannot rename {} to {}, a file with that name already exists",
                    file, target);
            this.previewNameField.setValue(currentBase);
            return;
        }

        try {
            Files.move(file, target);
        } catch (IOException e) {
            LOGGER.warn("[NeatlyBetter/Screenshots] Failed to rename {} to {}", file, target, e);
            this.previewNameField.setValue(currentBase);
            return;
        }

        Thumbnail thumbnail = thumbnailCache.remove(file);
        if (thumbnail != null) {
            thumbnailCache.put(target, thumbnail);
        }
        timestampCache.remove(target);
        timestampCache.remove(file);

        int index = this.files.indexOf(file);
        if (index >= 0) {
            List<Path> updated = new ArrayList<>(this.files);
            updated.set(index, target);
            this.files = updated;
            if (this.grid != null) {
                this.grid.setFiles(this.files);
            }
        }

        if (this.selected.remove(file)) {
            this.selected.add(target);
        }

        this.previewFile = target;
        this.previewNameFieldFile = target;
        this.previewNameField.setValue(newBase);
    }

    private static String stripExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot > 0 ? fileName.substring(0, dot) : fileName;
    }

    private static String extensionOf(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot > 0 ? fileName.substring(dot) : "";
    }

    private static String sanitizeFileName(String name) {
        return name.trim().replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    private static final class NavArrowButton extends AbstractWidget {
        private final Font font;
        private final Runnable onPress;

        NavArrowButton(int x, int y, int size, Component message, Font font, Runnable onPress) {
            super(x, y, size, size, message);
            this.font = font;
            this.onPress = onPress;
        }

        @Override
        public void onClick(@NonNull MouseButtonEvent event, boolean doubleClick) {
            this.onPress.run();
        }

        @Override
        protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
            int color = this.isHovered() ? -1 : 0xA0FFFFFF;
            String text = this.getMessage().getString();
            int textX = this.getX() + (this.getWidth() - this.font.width(text)) / 2;
            int textY = this.getY() + (this.getHeight() - 9) / 2;
            graphics.text(this.font, text, textX, textY, color);
        }

        @Override
        protected void updateWidgetNarration(net.minecraft.client.gui.narration.@NonNull NarrationElementOutput output) {
            this.defaultButtonNarrationText(output);
        }
    }

    private record ImageTransferable(Image image) implements Transferable {

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{DataFlavor.imageFlavor};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return DataFlavor.imageFlavor.equals(flavor);
        }

        @Override
        public @NonNull Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (!this.isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return this.image;
        }
    }

    private void deleteSelected() {
        if (this.selected.isEmpty() || this.grid == null) {
            return;
        }
        this.removeFiles(new ArrayList<>(this.selected));
    }

    private void deletePreviewedFile() {
        Path file = this.previewFile;
        if (file == null || this.grid == null) {
            return;
        }
        this.removeFiles(List.of(file));
    }

    private void removeFiles(List<Path> filesToRemove) {
        if (filesToRemove.isEmpty() || this.grid == null) {
            return;
        }

        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        boolean canTrash = desktop != null && desktop.isSupported(Desktop.Action.MOVE_TO_TRASH);

        List<Path> removedFiles = new ArrayList<>();
        FullscreenGuard.runWithoutAutoIconify(this.minecraft, () -> {
            for (Path file : filesToRemove) {
                try {
                    if (canTrash) {
                        if (desktop.moveToTrash(new File(file.toUri()))) {
                            removedFiles.add(file);
                        } else {
                            LOGGER.warn("[NeatlyBetter/Screenshots] OS refused to trash {}", file);
                        }
                    } else {
                        LOGGER.warn("[NeatlyBetter/Screenshots] Desktop trash unsupported on this platform, deleting {} permanently", file);
                        Files.deleteIfExists(file);
                        removedFiles.add(file);
                    }
                } catch (IOException e) {
                    LOGGER.warn("[NeatlyBetter/Screenshots] Failed to delete {}", file, e);
                }
            }
        });

        for (Path file : removedFiles) {
            Thumbnail thumbnail = thumbnailCache.remove(file);
            if (thumbnail != null) {
                this.minecraft.getTextureManager().release(thumbnail.id());
            }
            timestampCache.remove(file);
            if (file.equals(this.previewFile)) {
                this.closePreview();
            }
        }

        removedFiles.forEach(this.selected::remove);
        if (this.deleteButton != null) {
            this.deleteButton.active = !this.selected.isEmpty();
        }

        this.files = scanScreenshots();
        this.grid.setFiles(this.files);
    }

    private void openFolder() {
        Util.getPlatform().openPath(this.screenshotsDir);
    }

    private void toggleCopyScreenshots() {
        boolean newValue = !NTClientConfig.CONFIG.copyScreenshots.get();
        NTClientConfig.CONFIG.copyScreenshots.set(newValue);
        if (this.copyScreenshotsToggleButton != null) {
            this.copyScreenshotsToggleButton.setTooltip(Tooltip.create(this.copyScreenshotsTooltip()));
        }
    }

    private Component copyScreenshotsTooltip() {
        boolean enabled = NTClientConfig.CONFIG.copyScreenshots.get();
        return Component.literal("Copy Taken Screenshots: " + (enabled ? "True" : "False"));
    }

    private final class ScreenshotGridList extends AbstractSelectionList<ScreenshotRowEntry> {

        ScreenshotGridList(net.minecraft.client.Minecraft minecraft, int width, int height) {
            super(minecraft, width, height, ScreenshotManagerScreen.this.layout.getHeaderHeight(), ROW_HEIGHT);
        }

        void setFiles(List<Path> files) {
            this.clearEntries();
            int columns = Math.max(1, this.getRowWidth() / (TILE_WIDTH + TILE_SPACING));

            for (int i = 0; i < files.size(); i += columns) {
                List<Path> rowFiles = files.subList(i, Math.min(i + columns, files.size()));
                this.addEntry(new ScreenshotRowEntry(ScreenshotManagerScreen.this, new ArrayList<>(rowFiles), columns));
            }
        }

        @Override
        public int getRowWidth() {
            return this.getWidth() - 20;
        }

        @Override
        protected boolean entriesCanBeSelected() {
            return false;
        }

        @Override
        protected void updateWidgetNarration(net.minecraft.client.gui.narration.@NonNull NarrationElementOutput output) {
            ScreenshotRowEntry hovered = this.getHovered();
            if (hovered != null) {
                this.narrateListElementPosition(output, hovered);
            }
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            boolean hitTile = this.isMouseOver(event.x(), event.y())
                    && this.tileAt(event.x(), event.y()) != null;

            boolean consumed = super.mouseClicked(event, doubleClick);

            if (!hitTile && event.buttonInfo().button() == 0 && this.isMouseOver(event.x(), event.y())) {
                ScreenshotManagerScreen.this.clearSelection();
            }

            return consumed;
        }

        private @Nullable Path tileAt(double mouseX, double mouseY) {
            ScreenshotRowEntry entry = this.getEntryAtPosition(mouseX, mouseY);
            return entry == null ? null : entry.tileAt(mouseX, mouseY);
        }
    }

    private static final class ScreenshotRowEntry extends AbstractSelectionList.Entry<ScreenshotRowEntry> {
        private final ScreenshotManagerScreen screen;
        private final List<Path> tiles;
        private final int columns;

        ScreenshotRowEntry(ScreenshotManagerScreen screen, List<Path> tiles, int columns) {
            this.screen = screen;
            this.tiles = tiles;
            this.columns = columns;
        }

        @Override
        public void extractContent(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a) {
            int cellWidth = this.getWidth() / this.columns;

            for (int i = 0; i < this.tiles.size(); i++) {
                Path file = this.tiles.get(i);
                int tileX = this.getX() + i * cellWidth + (cellWidth - TILE_WIDTH) / 2;
                int tileY = this.getY();

                boolean tileHovered = mouseX >= tileX && mouseX < tileX + TILE_WIDTH
                        && mouseY >= tileY && mouseY < tileY + TILE_HEIGHT;

                if (this.screen.isSelected(file)) {
                    graphics.fill(tileX - 2, tileY - 2, tileX + TILE_WIDTH + 2, tileY + TILE_HEIGHT + 2, -1);
                }

                Thumbnail thumbnail = this.screen.getOrLoadThumbnail(file);
                graphics.blit(RenderPipelines.GUI_TEXTURED, thumbnail.id(), tileX, tileY, 0.0F, 0.0F,
                        TILE_WIDTH, TILE_HEIGHT, TILE_WIDTH, TILE_HEIGHT);

                if (tileHovered && !this.screen.isSelected(file)) {
                    graphics.fill(tileX, tileY, tileX + TILE_WIDTH, tileY + TILE_HEIGHT, 0x40FFFFFF);
                }

                this.renderTimestamp(graphics, file, tileX, tileY);

                Component name = Component.literal(file.getFileName().toString());
                graphics.pose().pushMatrix();
                graphics.pose().translate(tileX, tileY + TILE_HEIGHT + 2);
                graphics.pose().scale(FILENAME_TEXT_SCALE, FILENAME_TEXT_SCALE);
                graphics.textRenderer().acceptScrollingWithDefaultCenter(name,
                        0, Math.round(TILE_WIDTH / FILENAME_TEXT_SCALE),
                        0, Math.round(LABEL_HEIGHT / FILENAME_TEXT_SCALE));
                graphics.pose().popMatrix();
            }
        }

        private void renderTimestamp(GuiGraphicsExtractor graphics, Path file, int tileX, int tileY) {
            String timestamp = this.screen.getOrComputeTimestamp(file);
            if (timestamp.isEmpty()) {
                return;
            }

            Font font = this.screen.getFont();
            int scaledWidth = Math.round(font.width(timestamp) * TIMESTAMP_TEXT_SCALE);
            int scaledHeight = Math.round(9 * TIMESTAMP_TEXT_SCALE);
            int textX = tileX + TIMESTAMP_MARGIN;
            int textY = tileY + TILE_HEIGHT - TIMESTAMP_MARGIN - scaledHeight;

            graphics.fill(textX - 1, textY - 1, textX + scaledWidth + 1, textY + scaledHeight + 1, 0x90000000);

            graphics.pose().pushMatrix();
            graphics.pose().translate(textX, textY);
            graphics.pose().scale(TIMESTAMP_TEXT_SCALE, TIMESTAMP_TEXT_SCALE);
            graphics.text(font, timestamp, 0, 0, -1);
            graphics.pose().popMatrix();
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            Path file = this.tileAt(event.x(), event.y());
            if (file == null) {
                return false;
            }

            int button = event.buttonInfo().button();
            if (button == 0) {
                this.screen.onTileLeftClick(file);
                return true;
            } else if (button == 1) {
                this.screen.onTileRightClick(file);
                return true;
            }
            return false;
        }

        private @Nullable Path tileAt(double mouseX, double mouseY) {
            int cellWidth = this.getWidth() / this.columns;

            for (int i = 0; i < this.tiles.size(); i++) {
                int tileX = this.getX() + i * cellWidth + (cellWidth - TILE_WIDTH) / 2;
                int tileY = this.getY();

                boolean withinTile = mouseX >= tileX && mouseX < tileX + TILE_WIDTH
                        && mouseY >= tileY && mouseY < tileY + TILE_HEIGHT;
                if (withinTile) {
                    return this.tiles.get(i);
                }
            }

            return null;
        }
    }
}
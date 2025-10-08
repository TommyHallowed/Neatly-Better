package net.hallowed.oldways.client.util;

import net.hallowed.oldways.client.config.ClientConfigManager;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;

public final class CopyScreenshotHelper {
    private CopyScreenshotHelper() {}

    /** Copy given file (PNG) to system clipboard (AWT). */
    public static void copyFromFile(File file) {
        if (!ClientConfigManager.copyScreenshotsToClipboard()) return;
        try {
            BufferedImage img = ImageIO.read(file);
            if (img == null) return;
            Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
            cb.setContents(new Transferable() {
                @Override public DataFlavor[] getTransferDataFlavors() {
                    return new DataFlavor[]{DataFlavor.imageFlavor};
                }
                @Override public boolean isDataFlavorSupported(DataFlavor f) {
                    return DataFlavor.imageFlavor.equals(f);
                }
                @Override public @NotNull Object getTransferData(DataFlavor f) {
                    return img;
                }
            }, null);
        } catch (Exception ignored) {}
    }

    /** Fallback: find newest .png in `screenshots/` and copy it. */
    public static void copyLatestScreenshot(Path dir) {
        File folder = dir.toFile();
        File[] files = folder.listFiles((d, name) -> name.toLowerCase().endsWith(".png"));
        if (files == null || files.length == 0) return;
        File newest = files[0];
        for (int i = 1; i < files.length; i++) {
            if (files[i].lastModified() > newest.lastModified()) newest = files[i];
        }
        copyFromFile(newest);
    }
}

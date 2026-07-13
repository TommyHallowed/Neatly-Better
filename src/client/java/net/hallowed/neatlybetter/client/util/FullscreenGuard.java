package net.hallowed.neatlybetter.client.util;

import com.mojang.blaze3d.platform.Window;

import net.minecraft.client.Minecraft;

import org.lwjgl.glfw.GLFW;

public final class FullscreenGuard {

    private static final long MIN_SETTLE_NANOS = 200_000_000L;
    private static final long FOCUS_RETURN_TIMEOUT_NANOS = 500_000_000L;
    private static final long POLL_SLEEP_MILLIS = 2L;

    private FullscreenGuard() {
    }

    public static void runWithoutAutoIconify(Minecraft minecraft, Runnable action) {
        Window window = minecraft.getWindow();
        if (!window.isFullscreen()) {
            action.run();
            return;
        }

        long handle = window.handle();
        int previousAutoIconify = GLFW.glfwGetWindowAttrib(handle, GLFW.GLFW_AUTO_ICONIFY);
        GLFW.glfwSetWindowAttrib(handle, GLFW.GLFW_AUTO_ICONIFY, GLFW.GLFW_FALSE);
        try {
            action.run();
            settle(window);
        } finally {
            GLFW.glfwSetWindowAttrib(handle, GLFW.GLFW_AUTO_ICONIFY, previousAutoIconify);
        }
    }

    private static void settle(Window window) {
        pollFor();

        if (!window.isFocused()) {
            long deadline = System.nanoTime() + FOCUS_RETURN_TIMEOUT_NANOS;
            while (!window.isFocused() && System.nanoTime() < deadline) {
                GLFW.glfwPollEvents();
                sleepQuietly();
            }
        }
    }

    private static void pollFor() {
        long deadline = System.nanoTime() + FullscreenGuard.MIN_SETTLE_NANOS;
        while (System.nanoTime() < deadline) {
            GLFW.glfwPollEvents();
            sleepQuietly();
        }
    }

    private static void sleepQuietly() {
        try {
            Thread.sleep(POLL_SLEEP_MILLIS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
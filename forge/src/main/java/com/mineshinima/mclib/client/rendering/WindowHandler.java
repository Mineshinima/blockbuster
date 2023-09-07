package com.mineshinima.mclib.client.rendering;

import com.mineshinima.mclib.utils.rendering.GLUtils;
import net.minecraft.client.Minecraft;
import org.joml.Math;

public class WindowHandler {
    private static boolean overwriteMinecraft;
    private static Runnable resizingQueued;

    /**
     * Call this after everything has been rendered to avoid lags when resizing during rendering process.
     */
    public static void handleFramebuffer() {
        if (resizingQueued != null && overwriteMinecraft) {
            resizingQueued.run();
            resizingQueued = null;
        }
    }

    public static void setOverwriteMinecraft(boolean overwrite) {
        overwriteMinecraft = overwrite;
    }

    public static boolean isOverwriting() {
        return overwriteMinecraft;
    }

    /**
     * This queues resizing the Minecraft {@link com.mojang.blaze3d.platform.Window} instance.
     * The call is not immediately processed, it will be processed after Minecraft rendering calls have finished.
     * Calling this at the end removes stuttering when constantly resizing on every render frame.
     */
    public static void queueResize(int width, int height) {
        resizingQueued = () -> {
            Object window = Minecraft.getInstance().getWindow();
            ((IMixinWindow) window).resize(Math.clamp(1, GLUtils.getMaxTextureSize(), width),
                    Math.clamp(1, GLUtils.getMaxTextureSize(), height));
        };
    }

    /**
     * This will immediately resize the framebuffer to the current Minecraft window size.
     */
    public static void resizeToWindowSize() {
        int width = GLUtils.getGLFWWindowSize(Minecraft.getInstance().getWindow().getWindow())[0];
        int height = GLUtils.getGLFWWindowSize(Minecraft.getInstance().getWindow().getWindow())[1];

        Object window = Minecraft.getInstance().getWindow();
        ((IMixinWindow) window).resize(Math.clamp(1, GLUtils.getMaxTextureSize(), width),
                Math.clamp(1, GLUtils.getMaxTextureSize(), height));
    }
}

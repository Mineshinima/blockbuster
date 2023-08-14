package com.mineshinima.mclib.client.ui;

import com.mineshinima.mclib.client.ui.utils.UIGraphics;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static org.lwjgl.glfw.GLFW.*;

public class UIContext {
    private boolean debug;
    private final Window window;
    private final UIScreen screen;
    private final Map<Integer, Consumer<UIContext>> clickEvents = new HashMap<>();
    //TODO mouse as int? GLFW returns double, and Minecraft returns double because it converts it to GUI space... but we use global
    private UIGraphics graphics;
    private double mouseX;
    private double mouseY;
    private double mouseScroll;
    private int mouseKey;
    private int keyboardKey;
    private float partialTicks;
    /**
     * This cursor might not always be the cursor that is actually rendering.
     */
    private int glfwCursor;
    private final int defaultGlfwCursor = GLFW_ARROW_CURSOR;
    /**
     * The currently rendering cursor.
     */
    private int renderingCursor;

    public UIContext(Window window, UIScreen screen) {
        this.window = window;
        this.screen = screen;
        this.applyDefaultCursor();
    }

    public void setUIGraphics(UIGraphics graphics) {
        this.graphics = graphics;
    }

    public UIGraphics getUIGraphics() {
        return this.graphics;
    }

    public void registerClick(int mouseKey, Consumer<UIContext> callback) {
        this.clickEvents.put(mouseKey, callback);
    }

    public boolean hasClickEvent() {
        return this.clickEvents.containsKey(this.mouseKey);
    }

    /**
     * @throws NullPointerException when there is no click event for the current mouse key.
     *                              You should have used {@link #hasClickEvent()} first.
     */
    public void executeClickEvent() {
        this.clickEvents.get(this.mouseKey).accept(this);
    }

    public void setMouse(double mouseX, double mouseY) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }

    public void setMouseScroll( double mouseScroll) {
        this.mouseScroll = mouseScroll;
    }

    public void ignoreNextMouseMove() {
        Minecraft.getInstance().mouseHandler.setIgnoreFirstMove();
    }

    public Window getWindow() {
        return this.window;
    }

    public UIScreen getScreen() {
        return this.screen;
    }

    public int getKeyboardKey() {
        return this.keyboardKey;
    }

    public void setKeyboardKey(int keyboardKey) {
        this.keyboardKey = keyboardKey;
    }

    public double getMouseX() {
        return this.mouseX;
    }

    public double getMouseY() {
        return this.mouseY;
    }

    public double getMouseScroll() {
        return this.mouseScroll;
    }

    public int getMouseKey() {
        return this.mouseKey;
    }

    public float getPartialTicks() {
        return this.partialTicks;
    }

    public void setMouseKey(int mouseKey) {
        this.mouseKey = mouseKey;
    }

    public void setPartialTicks(float partialTicks) {
        this.partialTicks = partialTicks;
    }

    /**
     * This does not change the appearance of the cursor rendered.
     * After this method you need to still apply the cursor using {@link #applyPreparedCursor()}
     */
    public void resetCursor() {
        this.glfwCursor = this.defaultGlfwCursor;
    }

    public void applyDefaultCursor() {
        this.resetCursor();
        this.applyPreparedCursor();
    }

    /**
     * @return true when the prepared cursor differs from the default cursor
     */
    public boolean cursorChanged() {
        return this.glfwCursor != this.defaultGlfwCursor;
    }

    /**
     * Applies the current cursor to GLFW using {@link org.lwjgl.glfw.GLFW#glfwSetCursor(long, long)}
     * This will change the appearance of the cursor on screen.
     */
    public void applyPreparedCursor() {
        if (this.renderingCursor != this.glfwCursor) {
            glfwSetCursor(this.window.getWindow(), glfwCreateStandardCursor(this.glfwCursor));
            this.renderingCursor = this.glfwCursor;
        }
    }

    /**
     * This does not change the rendering of the cursor. After this call the apply method needs to be called.
     * @param GLFW_CURSOR
     */
    public void prepareCursor(int GLFW_CURSOR) {
        this.glfwCursor = GLFW_CURSOR;
    }

    public int getPreparedCursor() {
        return this.glfwCursor;
    }

    public int getRenderingCursor() {
        return this.renderingCursor;
    }

    public boolean isDebug() {
        return this.debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }
}

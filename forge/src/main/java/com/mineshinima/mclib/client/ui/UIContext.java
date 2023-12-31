package com.mineshinima.mclib.client.ui;

import com.mineshinima.mclib.client.ui.utils.UIGraphics;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;

import java.util.*;

import static org.lwjgl.glfw.GLFW.*;

public class UIContext {
    private boolean debug;
    private final Window window;
    private final UIScreen screen;
    private final Map<Integer, UIElement> clickEvents = new HashMap<>();
    private UIGraphics graphics;
    private double mouseX;
    private double mouseY;
    private double mouseScroll;
    private final Set<Integer> pressedMouseKeys = new HashSet<>();
    private int currentMouseKey;
    private char typedChar;
    private int currentKeyboardKey;
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
        this.renderDefaultCursor();
    }

    public void setUIGraphics(UIGraphics graphics) {
        this.graphics = graphics;
    }

    public UIGraphics getUIGraphics() {
        return this.graphics;
    }

    /**
     * Register a prioritized click event for the given mouseKey.
     * <br><br>
     * Clicking will happen via the {@link UIElement#postRenderedMouseClick(UIContext)} method.
     * @param mouseKey
     * @param element
     */
    public void registerClick(int mouseKey, UIElement element) {
        this.clickEvents.put(mouseKey, element);
    }

    public void removeRegisteredClick(int mouseKey, UIElement element) {
        this.clickEvents.remove(mouseKey, element);
    }

    /**
     * Execute a registered prioritized click event, if present.
     * <br><br>
     * This calls the {@link UIElement#postRenderedMouseClick(UIContext)} method.
     * <br>
     * @return true if there was a registered click event for the current mouseKey
     *         and when its mouseEvent method returned true.
     */
    public boolean mouseClick() {
        if (this.clickEvents.containsKey(this.currentMouseKey)) {
            return this.clickEvents.get(this.currentMouseKey).postRenderedMouseClick(this);
        }

        return false;
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
        return this.currentKeyboardKey;
    }

    public void pressKeyboardKey(int keyboardKey) {
        this.currentKeyboardKey = keyboardKey;
    }

    public void releaseKeyboardKey(int keyboardKey) {
        this.currentKeyboardKey = keyboardKey;
    }

    public void typeChar(char typedChar) {
        this.typedChar = typedChar;
    }

    public char getTypedChar() {
        return this.typedChar;
    }

    /**
     * @param keyboardKey see {@link org.lwjgl.glfw.GLFW} for the key constants
     * @return whether the key is pressed
     */
    public boolean isKeyboardKeyDown(int keyboardKey) {
        return InputConstants.isKeyDown(this.window.getWindow(), keyboardKey);
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
        return this.currentMouseKey;
    }

    /**
     * A convenience method to avoid a lot of repeating code and better readability.
     * @return true when the mouse key is the left mouse button.
     */
    public boolean isLeftMouseKey() {
        return this.currentMouseKey == GLFW_MOUSE_BUTTON_LEFT;
    }

    /**
     * A convenience method to avoid a lot of repeating code and better readability.
     * @return true when the mouse key is the right mouse button.
     */
    public boolean isRightMouseKey() {
        return this.currentMouseKey == GLFW_MOUSE_BUTTON_RIGHT;
    }

    /**
     * A convenience method to avoid a lot of repeating code and better readability.
     * @return true when the mouse key is the middle mouse button.
     */
    public boolean isMiddleMouseKey() {
        return this.currentMouseKey == GLFW_MOUSE_BUTTON_MIDDLE;
    }

    public float getPartialTicks() {
        return this.partialTicks;
    }

    /**
     * This method sets the current mouseKey and adds it to the pressed mouseKeys.
     * This method shall not be called when releasing a mouseKey.
     * @param mouseKey
     */
    public void clickMouseKey(int mouseKey) {
        this.currentMouseKey = mouseKey;
        this.pressedMouseKeys.add(mouseKey);
    }

    /**
     * This method sets the current mouseKey and removes it from the pressed mouseKeys.
     * This method shall not be called when clicking a mouseKey.
     * @param mouseKey
     */
    public void releaseMouseKey(int mouseKey) {
        this.currentMouseKey = mouseKey;
        this.pressedMouseKeys.remove(mouseKey);
    }

    /**
     * @return true when the given mouseKey is being pressed still.
     */
    public boolean isMouseKeyDown(int mouseKey) {
        return this.pressedMouseKeys.contains(mouseKey);
    }

    public void setPartialTicks(float partialTicks) {
        this.partialTicks = partialTicks;
    }

    /**
     * This does not change the appearance of the cursor rendered.
     * After this method you need to still apply the cursor using {@link #renderPreparedCursor()}
     */
    public void resetPreparedCursor() {
        this.glfwCursor = this.defaultGlfwCursor;
    }

    public void renderDefaultCursor() {
        this.resetPreparedCursor();
        this.renderPreparedCursor();
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
    public void renderPreparedCursor() {
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

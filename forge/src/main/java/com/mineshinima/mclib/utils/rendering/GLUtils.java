package com.mineshinima.mclib.utils.rendering;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import static org.lwjgl.glfw.GLFW.glfwGetCursorPos;
import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;

public class GLUtils {

    /**
     * @return array of {width, height}
     */
    public static int[] getGLFWWindowSize(long window) {
        int[] width = new int[1];
        int[] height = new int[1];

        GLFW.glfwGetWindowSize(window, width, height);

        return new int[]{width[0], height[0]};
    }

    public static int[] getGLFWFrameBufferSize(long window) {
        int[] w = new int[1];
        int[] h = new int[1];
        glfwGetFramebufferSize(window, w, h);

        return new int[]{w[0], h[0]};
    }

    public static int getCurrentFramebufferID() {
        int[] oldFramebufferId = new int[1];
        GL20.glGetIntegerv(GL30.GL_DRAW_FRAMEBUFFER_BINDING, oldFramebufferId);

        return oldFramebufferId[0];
    }

    public static int getMaxTextureSize() {
        int[] maxTextureSize = new int[1];
        GL11.glGetIntegerv(GL11.GL_MAX_TEXTURE_SIZE, maxTextureSize);

        return maxTextureSize[0];
    }

    public static double getMousePosX(long window) {
        return getMousePos(window)[0];
    }

    public static double getMousePosY(long window) {
        return getMousePos(window)[1];
    }

    public static double[] getMousePos(long window) {
        double[] mouseXPoint = new double[1];
        double[] mouseYPoint = new double[1];
        glfwGetCursorPos(window, mouseXPoint, mouseYPoint);

        return new double[]{mouseXPoint[0], mouseYPoint[0]};
    }
}

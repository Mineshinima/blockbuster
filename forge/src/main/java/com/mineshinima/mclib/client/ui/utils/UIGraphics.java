package com.mineshinima.mclib.client.ui.utils;

import com.mineshinima.mclib.client.ui.Area;
import com.mineshinima.mclib.utils.Color;
import com.mineshinima.mclib.utils.rendering.GLUtils;
import com.mineshinima.mclib.utils.rendering.RenderingUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.Stack;

/**
 * Stuff for UI rendering.
 * Also an Adapter for Minecraft GUI stuff, they changed this once (1.19-1.20), I bet they will change it again,
 * and we would need to rewrite a lot again.
 * Maybe this makes porting easier :-(
 */
public class UIGraphics {
    private final GuiGraphics graphics;
    private final Stack<Area> scissors = new Stack<>();

    public UIGraphics(GuiGraphics graphics) {
        this.graphics = graphics;
    }

    public void renderArea(int x0, int y0, int x1, int y1, Color color) {
        this.graphics.fill(x0, y0, x1, y1, color.getRGBAColor());
    }

    public void renderArea(Area area, Color color) {
        this.renderArea(area.getX(), area.getY(), area.getEndX(), area.getEndY(), color);
    }

    public void horizontalLine(int x0, int x1, int y, Color color) {
        if (x1 < x0) {
            int i = x0;
            x0 = x1;
            x1 = i;
        }

        this.renderArea(x0, y, x1, y + 1, color);
    }

    public void verticalLine(int x, int y0, int y1, Color color) {
        if (y1 < y0) {
            int i = y0;
            y0 = y1;
            y1 = i;
        }

        this.renderArea(x, y0, x + 1, y1, color);
    }

    /**
     * Renders a border which is inside of this area.
     */
    public void renderBorder(Area area, int thickness, Color color) {
        int x00 = area.getX();
        int x01 = x00 + thickness;

        int x10 = area.getEndX();
        int x11 = x10 - thickness;

        int y00 = area.getY();
        int y01 = y00 + thickness;

        int y10 = area.getEndY();
        int y11 = y10 - thickness;

        /* vertical border */
        this.renderArea(x00, y00, x01, y10, color);
        this.renderArea(x10, y00, x11, y10, color);

        /* horizontal border */
        this.renderArea(x00, y00, x10, y01, color);
        this.renderArea(x00, y10, x10, y11, color);
    }

    /**
     * Scissor (clip) the screen
     * @return the area that has been scissored.
     *         This will be the globally visible scissoring, clamped by the previous scissored areas.
     */
    public Area scissor(Area scissor) {
        Area lastScissor = this.scissors.isEmpty() ? null : this.scissors.peek();

        /* If it was scissored before, then clamp to the bounds of the last one */
        if (lastScissor != null) {
            scissor = lastScissor.intersect(scissor);
        }

        this.scissorArea(scissor.getX(), scissor.getY(), scissor.getWidth(), scissor.getHeight());
        this.scissors.add(scissor);

        return scissor;
    }

    private void scissorArea(int x, int y, int w, int h) {
        Minecraft mc = Minecraft.getInstance();
        int[] displaySize = GLUtils.getGLFWWindowSize(mc.getWindow().getWindow());
        y = displaySize[1] - (y + h);

        RenderSystem.enableScissor(x, y, w, h);
    }

    public void unscissor()
    {
        this.scissors.pop();

        if (this.scissors.isEmpty()){
            RenderSystem.disableScissor();
        } else {
            Area area = this.scissors.peek();

            this.scissorArea(area.getX(), area.getY(), area.getWidth(), area.getHeight());
        }
    }

    public void buildUVQuad(BufferBuilder bufferBuilder, Area area) {
        RenderingUtils.buildBillboard(bufferBuilder, area.getX(), area.getY(), 0, area.getWidth(), area.getHeight(), 1F, 1F);
    }
}

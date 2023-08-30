package com.mineshinima.mclib.client.ui;

import com.mineshinima.mclib.client.rendering.WindowHandler;
import com.mineshinima.mclib.client.ui.space.Orientation;
import com.mineshinima.mclib.client.ui.utils.UIGraphics;
import com.mineshinima.mclib.utils.rendering.GLUtils;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;

@OnlyIn(Dist.CLIENT)
public class UIScreen extends Screen {
    private final UIRootElement root;
    private final UIContext context;

    public UIScreen(Minecraft minecraft) {
        super(GameNarrator.NO_TITLE);
        this.minecraft = minecraft;
        this.context = new UIContext(this.minecraft.getWindow(), this);
        this.root = new UIRootElement(this);

        this.context.setDebug(false);

        UIPanelGrid rootGrid = new UIPanelGrid(this.root, new UIPanel(new UIElement(), new UIViewport()));
        rootGrid.width(1F).height(1F);
        rootGrid.subdivide(Orientation.HORIZONTAL, 0.5F);
        rootGrid.getGrid0().ifPresent(e -> e.subdivide(Orientation.VERTICAL, 0.25F));
        rootGrid.getGrid0().flatMap(UIPanelGrid::getGrid0).ifPresent(x -> x.subdivide(Orientation.VERTICAL, 0.25F));
        rootGrid.getGrid1().ifPresent(e -> e.subdivide(Orientation.VERTICAL, 0.25F));
        /*
         * When there is a UIViewport in the elements, you need to call onClose(), to reset viewport overwriting.
         * so the instantiation of the UIScreen doesn't cause Minecraft rendering to freak out.
         *
         * Note: if this is instantiated on startup, and viewport resized the framebuffer, something might go wrong.
         */
        this.root.onClose();
    }

    public UIContext getContext() {
        return this.context;
    }

    /**
     * This gets also called when resizing,
     * see {@link Screen#resize(Minecraft, int, int)} and {@link Screen#rebuildWidgets()}
     */
    @Override
    protected void init() {
        this.width = GLUtils.getGLFWWindowSize(this.context.getWindow().getWindow())[0];
        this.height = GLUtils.getGLFWWindowSize(this.context.getWindow().getWindow())[1];

        this.root.height(this.height).width(this.width);
        this.root.resize(new DocumentFlowRow());
        this.context.resetCursor();
    }

    @Override
    public void onClose() {
        super.onClose();

        this.root.onClose();
        this.context.applyDefaultCursor();
        WindowHandler.resizeToWindowSize();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (this.minecraft == null) return;

        int oldFramebufferID = GLUtils.getCurrentFramebufferID();

        /* render UI to the default framebuffer as Minecraft's framebuffer is resized */
        if (WindowHandler.isOverwriting()) {
            GlStateManager._glBindFramebuffer(GL_FRAMEBUFFER, 0);
            RenderSystem.viewport(0, 0, this.width, this.height);
        }

        /* we use global screen coordinates instead of Minecraft's GUI scaled space */
        Matrix4f matrix4f = (new Matrix4f()).setOrtho(0.0F, this.width, this.height, 0.0F, GuiGraphics.MIN_GUI_Z, GuiGraphics.MAX_GUI_Z);

        RenderSystem.backupProjectionMatrix();
        RenderSystem.setProjectionMatrix(matrix4f, VertexSorting.ORTHOGRAPHIC_Z);
        RenderSystem.disableDepthTest();

        this.updateMouseContext();
        this.context.setPartialTicks(partialTicks);
        this.context.setUIGraphics(new UIGraphics(graphics));

        /*
         * Changing the cursor rendering throughout rendering will result in flickering of the cursor,
         * as Windows or so is rendering it. So the rendering should only be changed once.
         * Here we check if the UI render wants to change the cursor, if yes, it will be applied at the end once.
         */
        this.context.resetCursor();

        this.root.render(this.context);

        if (!this.context.cursorChanged()) {
            this.context.applyDefaultCursor();
        } else if (this.context.getPreparedCursor() != this.context.getRenderingCursor()) {
            /* to avoid applying a cursor everytime while might already be rendering */
            this.context.applyPreparedCursor();
        }

        if (WindowHandler.isOverwriting()) {
            RenderSystem.restoreProjectionMatrix();
            GlStateManager._glBindFramebuffer(GL_FRAMEBUFFER, oldFramebufferID);
        }
    }

    @Override
    public void tick() { }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        this.updateMouseContext();
        return this.root.isMouseOver(this.context);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        this.updateMouseContext();
        this.root.mouseMoved(this.context);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseKey) {
        this.updateMouseContext(mouseKey);

        if (this.context.mouseClick()) {
            return true;
        }

        return this.root.mouseClicked(this.context);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int mouseKey) {
        this.updateMouseContext(mouseKey);
        return this.root.mouseReleased(this.context);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseKey, double dragX, double dragY) {
        this.updateMouseContext(mouseKey);
        dragX = dragX / (double) this.minecraft.getWindow().getGuiScaledWidth() * (double) this.minecraft.getWindow().getScreenWidth();
        dragY = dragY / (double) this.minecraft.getWindow().getGuiScaledHeight() * (double) this.minecraft.getWindow().getScreenHeight();
        return this.root.mouseDragged(this.context, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scroll) {
        this.updateMouseContext();
        this.context.setMouseScroll(scroll);
        return this.root.mouseScrolled(this.context);
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (key == GLFW_KEY_ESCAPE && this.shouldCloseOnEsc()) {
            this.onClose();
            return true;
        }

        this.context.setKeyboardKey(key);
        return this.root.keyPressed(this.context);
    }

    private void updateMouseContext(int mouseKey) {
        this.context.setMouseKey(mouseKey);
        this.updateMouseContext();
    }

    private void updateMouseContext() {
        /* we cant use the position passed down by minecraft, because in render method it's cast to int already
        while everywhere else it's double. This causes inconsistencies. */
        double mouseX = GLUtils.getMousePosX(this.context.getWindow().getWindow());
        double mouseY = GLUtils.getMousePosY(this.context.getWindow().getWindow());

        this.context.setMouse(mouseX, mouseY);
    }
}

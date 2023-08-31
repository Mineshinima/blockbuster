package com.mineshinima.mclib.client.ui;

import com.mineshinima.mclib.client.ui.space.Orientation;
import com.mineshinima.mclib.client.ui.transformation.UIScrollTransformation;
import com.mineshinima.mclib.utils.Color;
import com.mineshinima.mclib.utils.MathUtils;

import java.lang.Math;

import javax.annotation.Nullable;

import java.util.Optional;

import static org.lwjgl.glfw.GLFW.*;

public class UIScrollElement extends UIElement {
    /**
     * If this variable is null, it means that the scroll direction is both vertical and horizontal.
     */
    @Nullable
    private Orientation scrollDirection;
    private int scrollX;
    private int scrollY;
    private boolean renderScrollbar = true;
    private boolean overlayScrollbar = false;
    private boolean dragHorizontal;
    private int dragHorizontalOffset;
    private boolean dragVertical;
    private int dragVerticalOffset;
    private boolean clicked;
    private int minScrollbarSize = 15;
    private final UIScrollTransformation transformation = new UIScrollTransformation(this);

    public UIScrollElement() {
        this.overflow = false;
    }

    @Override
    public UIScrollTransformation getTransformation() {
        return this.transformation;
    }

    /**
     * A Scroll Element has by default overflow and should not change that.
     * @param overflow
     * @return
     */
    @Override
    public UIElement overflow(boolean overflow) {
        return this;
    }

    public UIScrollElement overlayScrollbar(boolean overlay) {
        this.overlayScrollbar = overlay;
        return this;
    }

    public UIScrollElement scrollbarVisible(boolean visible) {
        this.renderScrollbar = visible;
        return this;
    }

    public UIScrollElement scrollbarWidth(int scrollbarWidth) {
        this.transformation.setScrollbarWidth(scrollbarWidth);
        return this;
    }

    public UIScrollElement scrollDirection(Orientation scrollDirection) {
        this.scrollDirection = scrollDirection;
        return this;
    }

    public boolean isRenderScrollbar() {
        return this.renderScrollbar;
    }

    public boolean isOverlayScrollbar() {
        return this.overlayScrollbar;
    }

    public int getScrollbarWidth() {
        return this.transformation.getScrollbarWidth();
    }

    /**
     * @return null when the scroll direction is both horizontal and vertical.
     */
    @Nullable
    public Orientation getScrollDirection() {
        return this.scrollDirection;
    }

    public boolean isScrollVertical() {
        return this.scrollDirection == Orientation.VERTICAL || this.scrollDirection == null;
    }

    /**
     * @return true if the scrolling includes horizontal scrolling,
     * which is also the case when scrolling is both vertical and horizontal
     */
    public boolean isScrollHorizontal() {
        return this.scrollDirection == Orientation.HORIZONTAL || this.scrollDirection == null;
    }

    public int getScrollX() {
        return this.scrollX;
    }

    /**
     * @param scrollX the horizontal scroll value (which is always negative).
     *                The value will be automatically clamped.
     */
    public void setScrollX(int scrollX) {
        this.scrollX = MathUtils.clamp(scrollX, -this.maxHorizontalScrollOffset(), 0);
    }

    public int getScrollY() {
        return this.scrollY;
    }

    /**
     * @param scrollY the vertical scroll value (which is always negative).
     *                The value will be automatically clamped.
     */
    public void setScrollY(int scrollY) {
        this.scrollY = MathUtils.clamp(scrollY, -this.maxVerticalScrollOffset(), 0);
    }

    @Override
    public boolean mouseDrag(UIContext context, double dragX, double dragY) {
        if (this.dragHorizontal && this.dragVertical) {
            this.scrollOffset((int) Math.round(dragX), (int) Math.round(dragY));

            if (context.getMouseX() < this.contentArea.getX()) {
                glfwSetCursorPos(context.getWindow().getWindow(), this.contentArea.getEndX(), context.getMouseY());
                context.ignoreNextMouseMove();
            } else if (context.getMouseX() > this.contentArea.getEndX()) {
                glfwSetCursorPos(context.getWindow().getWindow(), this.contentArea.getX(), context.getMouseY());
                context.ignoreNextMouseMove();
            }

            if (context.getMouseY() < this.contentArea.getY()) {
                glfwSetCursorPos(context.getWindow().getWindow(), context.getMouseX(), this.contentArea.getEndY());
                context.ignoreNextMouseMove();
            } else if (context.getMouseY() > this.contentArea.getEndY()) {
                glfwSetCursorPos(context.getWindow().getWindow(), context.getMouseX(), this.contentArea.getY());
                context.ignoreNextMouseMove();
            }

            return true;
        } else if (this.dragHorizontal) {
            this.dragX(context.getMouseX() - this.dragHorizontalOffset);

            return true;
        } else if (this.dragVertical) {
            this.dragY(context.getMouseY() - this.dragVerticalOffset);

            return true;
        }

        return false;
    }

    protected void dragX(double mouseX) {
        Area scrollbar;
        if ((scrollbar = this.getHorizontalScrollbar()) == null) return;

        Area container = this.horizontalScrollbarContainer();
        double progress = mouseX - container.getX();
        int scrollableWidth = container.getWidth() - scrollbar.getWidth();
        double ratio = MathUtils.clamp(progress / (float) scrollableWidth, 0, 1);

        this.scrollTo((int) -Math.round(ratio * this.maxHorizontalScrollOffset()), this.scrollY);
    }

    protected void dragY(double mouseY) {
        Area scrollbar;
        if ((scrollbar = this.getVerticalScrollbar()) == null) return;

        Area container = this.verticalScrollbarContainer();
        double progress = mouseY - container.getY();
        int scrollableHeight = container.getHeight() - scrollbar.getHeight();
        double ratio = MathUtils.clamp(progress / (float) scrollableHeight, 0, 1);

        this.scrollTo(this.scrollX, (int) -Math.round(ratio * this.maxVerticalScrollOffset()));
    }

    @Override
    public boolean postRenderedMouseClick(UIContext context) {
        if (this.scrollDirection == null && this.contentArea.isInside(context.getMouseX(), context.getMouseY())
                && context.isMiddleMouseKey()) {
            this.dragVertical = true;
            this.dragHorizontal = true;
            return true;
        }

        if (!this.renderScrollbar || !context.isLeftMouseKey()) return false;

        Area horizontalScrollbar = this.getHorizontalScrollbar();
        Area verticalScrollbar = this.getVerticalScrollbar();
        Area horizontalContainer = this.horizontalScrollbarContainer();
        Area verticalContainer = this.verticalScrollbarContainer();
        Area barSpace = new Area(horizontalContainer.getEndX(), horizontalContainer.getY(), verticalContainer.getWidth(), horizontalContainer.getHeight());

        /* clicking on the space when scrolling is both vertical and horizontal */
        if (this.scrollDirection == null && barSpace.isInside(context.getMouseX(), context.getMouseY())) {
            this.scrollTo(-this.maxHorizontalScrollOffset(), -this.maxVerticalScrollOffset());
            this.clicked = true;
            return true;
        }

        /* clicking on the scrollbars */
        if (horizontalScrollbar != null && horizontalScrollbar.isInside(context.getMouseX(), context.getMouseY())) {
            this.dragHorizontal = true;
            this.dragHorizontalOffset = (int) (context.getMouseX() - horizontalScrollbar.getX());
            return true;
        }

        if (verticalScrollbar != null && verticalScrollbar.isInside(context.getMouseX(), context.getMouseY())) {
            this.dragVertical = true;
            this.dragVerticalOffset = (int) (context.getMouseY() - verticalScrollbar.getY());
            return true;
        }

        /* clicking on the scroll container to jump to a position */
        if (!this.overlayScrollbar) {
            if (this.verticalScrollbarContainer().isInside(context.getMouseX(), context.getMouseY()) && verticalScrollbar != null) {
                this.dragVertical = true;
                this.dragVerticalOffset = (int) (verticalScrollbar.getHeight() / 2F);
                this.dragY(context.getMouseY() - verticalScrollbar.getHeight() / 2F);
                return true;
            } else if (this.horizontalScrollbarContainer().isInside(context.getMouseX(), context.getMouseY()) && horizontalScrollbar != null) {
                this.dragHorizontal = true;
                this.dragHorizontalOffset = (int) (horizontalScrollbar.getWidth() / 2F);
                this.dragX(context.getMouseX() - horizontalScrollbar.getWidth() / 2F);
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean postRenderedMouseRelease(UIContext context) {
        //TODO clicked - could be removed when clicking and mouse release is tied
        if (this.dragVertical || this.dragHorizontal || this.clicked) {
            this.dragVertical = false;
            this.dragHorizontal = false;
            this.dragVerticalOffset = 0;
            this.dragHorizontalOffset = 0;
            this.clicked = false;

            return true;
        }

        return false;
    }

    @Override
    public boolean mouseScroll(UIContext context) {
        if (!this.contentArea.isInside(context.getMouseX(), context.getMouseY()) || this.scrollDirection == null) {
            return false;
        }

        this.scrollOffset((int) Math.round(Math.copySign(-10, context.getMouseScroll())),
                (int) Math.round(Math.copySign(-10, context.getMouseScroll())));

        return true;
    }

    protected void scrollTo(int x, int y) {
        if (this.scrollDirection == Orientation.HORIZONTAL || this.scrollDirection == null) {
            this.scrollX = MathUtils.clamp(x, -this.maxHorizontalScrollOffset(), 0);
        }

        if (this.scrollDirection == Orientation.VERTICAL || this.scrollDirection == null) {
            this.scrollY = MathUtils.clamp(y, -this.maxVerticalScrollOffset(), 0);
        }

        this.transformation.scroll();
    }

    protected void scrollOffset(int x, int y) {
        this.scrollTo(this.scrollX + x, this.scrollY + y);
    }

    @Override
    protected Optional<Area> getScissoringArea() {
        Area scissoring = this.contentArea.copy();

        if (!this.overlayScrollbar) {
            int xOffset = 0, yOffset = 0;

            if (this.isScrollVertical()) {
                xOffset = this.verticalScrollbarContainer().getWidth();
            }

            if (this.isScrollHorizontal()) {
                yOffset = this.horizontalScrollbarContainer().getHeight();
            }

            scissoring.setWidth(scissoring.getWidth() - xOffset);
            scissoring.setHeight(scissoring.getHeight() - yOffset);
        }

        return Optional.of(scissoring);
    }

    @Override
    public void postRender(UIContext context) {
        super.postRender(context);

        if (this.dragHorizontal && this.dragVertical) {
            context.prepareCursor(GLFW_RESIZE_ALL_CURSOR);
        }

        if (!this.renderScrollbar) return;

        Area verticalScrollbar, horizontalScrollbar;

        if ((verticalScrollbar = this.getVerticalScrollbar()) != null) {
            context.getUIGraphics().renderArea(verticalScrollbar, new Color(1,1,1,1));
            context.getUIGraphics().renderBorder(verticalScrollbar, 1, new Color(0,0,0,1));
        }

        if ((horizontalScrollbar = this.getHorizontalScrollbar()) != null) {
            context.getUIGraphics().renderArea(horizontalScrollbar, new Color(1,1,1,1));
            context.getUIGraphics().renderBorder(horizontalScrollbar, 1, new Color(0,0,0,1));
        }
    }

    @Nullable
    public Area getHorizontalScrollbar() {
        int maxOffset = this.maxHorizontalScrollOffset();

        if (!this.isScrollHorizontal() || maxOffset == 0) return null;

        Area container = this.horizontalScrollbarContainer();

        int scrollWidth = this.getTransformation().getHorizontalScrollSize();
        int scrollbarWidth = Math.round(((container.getWidth()) / (float) scrollWidth) * (container.getWidth()));
        scrollbarWidth = MathUtils.clamp(scrollbarWidth, this.minScrollbarSize, container.getWidth());
        float scrollRatio = -this.scrollX / (float) maxOffset;
        int scrollBarX = Math.round((container.getX() + (container.getWidth() - scrollbarWidth) * scrollRatio));

        return new Area(scrollBarX, container.getY(), scrollbarWidth, container.getHeight());
    }

    @Nullable
    public Area getVerticalScrollbar() {
        int maxOffset = this.maxVerticalScrollOffset();

        if (!this.isScrollVertical() || maxOffset == 0) return null;

        Area container = this.verticalScrollbarContainer();

        int scrollHeight = this.getTransformation().getVerticalScrollSize();
        int scrollbarHeight = Math.round(((container.getHeight()) / (float) scrollHeight) * (container.getHeight()));
        scrollbarHeight = MathUtils.clamp(scrollbarHeight, this.minScrollbarSize, container.getHeight());
        float scrollRatio = -this.scrollY / (float) maxOffset;
        int scrollBarY = Math.round(container.getY() + (container.getHeight() - scrollbarHeight) * scrollRatio);

        return new Area(container.getX(), scrollBarY, container.getWidth(), scrollbarHeight);
    }

    protected Area verticalScrollbarContainer() {
        int barOffset = this.isScrollHorizontal() ? this.getScrollbarWidth() : 0;

        return new Area(this.contentArea.getEndX() - this.getScrollbarWidth(), this.contentArea.getY(),
                this.getScrollbarWidth(), this.contentArea.getHeight() - barOffset);
    }

    protected Area horizontalScrollbarContainer() {
        int barOffset = this.isScrollVertical() ? this.getScrollbarWidth() : 0;

        return new Area(this.contentArea.getX(), this.contentArea.getEndY() - this.getScrollbarWidth(),
                this.contentArea.getWidth() - barOffset, this.getScrollbarWidth());
    }

    public int maxHorizontalScrollOffset() {
        int max = this.innerArea.getX()
                - (this.contentArea.getEndX() - this.getTransformation().getCalculatedPadding()[1] - this.transformation.getMaxWidth());
        return Math.max(max, 0);
    }

    public int maxVerticalScrollOffset() {
        int max = this.innerArea.getY()
                - (this.contentArea.getEndY() - this.getTransformation().getCalculatedPadding()[2] - this.transformation.getTotalHeight());
        return Math.max(max, 0);
    }
}

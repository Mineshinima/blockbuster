package com.mineshinima.mclib.client.ui;

import com.mineshinima.mclib.client.ui.space.Direction;
import com.mineshinima.mclib.client.ui.space.HorizontalDirection;
import com.mineshinima.mclib.client.ui.space.Orientation;
import com.mineshinima.mclib.client.ui.space.VerticalDirection;
import com.mineshinima.mclib.client.ui.transformation.UIPanelGridTransformation;
import org.joml.Math;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.lwjgl.glfw.GLFW.*;

public class UIPanelGrid extends UIElement {
    @Nullable
    private UIPanel panel;
    /**
     * The direction is null when this is the leaf of the panelGrid tree (when {@link #panel} is not null)
     */
    @Nullable
    private Orientation direction;
    /**
     * For vertical layout this should be the left element.
     * For horizontal layout this should be the top element.
     */
    @Nullable
    private UIPanelGrid grid0;

    /**
     * For vertical layout this should be the right element.
     * For horizontal layout this should be the bottom element.
     */
    @Nullable
    private UIPanelGrid grid1;
    /**
     * The default border width for panelGrids
     */
    private final int borderWidth = 2;
    private boolean dragging = false;
    private final UIPanelGridTransformation transformation = new UIPanelGridTransformation(this);

    /**
     * The position when dragging started (this is used to reset the dragging)
     */
    private int originalPos;

    public UIPanelGrid(UIElement parent, UIPanel panel) {
        this(panel);

        if (parent instanceof UIPanelGrid) {
            throw new IllegalArgumentException("Parent in the constructor cant be of the type UIPanelGrid. Use the subdivide method to add children.");
        }

        parent.addChildren(this);
    }

    protected UIPanelGrid(UIPanel panel) {
        this.setPanel(panel);
    }

    public UIPanelGrid(UIElement parent, UIPanelGrid grid0, UIPanelGrid grid1) {
        this(grid0, grid1);

        if (parent instanceof UIPanelGrid) {
            throw new IllegalArgumentException("Parent in the constructor cant be of the type UIPanelGrid. Use the subdivide method to add children.");
        }

        parent.addChildren(this);
    }

    protected UIPanelGrid(UIPanelGrid grid0, UIPanelGrid grid1) {
        this.grid0 = grid0;
        this.grid1 = grid1;
        this.grid0.parent = this;
        this.grid1.parent = this;
    }

    @Override
    public UIElement borderWidth(int borderWidth) {
        if (this.panel != null) {
            super.borderWidth(borderWidth);
        }

        return this;
    }

    @Override
    public UIPanelGridTransformation getTransformation() {
        return this.transformation;
    }

    public void subdivide(Orientation direction, float ratio) {
        if (this.panel == null) {
            throw new UnsupportedOperationException("Unsupported call to subdivide. UIPanelGrid cant subdivide an already subdivided panelgrid.");
        }

        if (direction == null) {
            throw new IllegalArgumentException("Direction is null.");
        }

        this.direction = direction;
        this.panel.parent = null;
        this.grid0 = new UIPanelGrid(this.panel);
        this.grid1 = new UIPanelGrid(new UIPanel(new UIElement(), new UIElement()));
        this.grid0.parent = this;
        this.grid1.parent = this;
        this.panel = null;

        super.borderWidth(0);

        if (direction == Orientation.HORIZONTAL) {
            this.grid0.width(1F).height(ratio);
            this.grid1.width(1F).height(1 - ratio);
        } else {
            this.grid0.height(1F).width(ratio);
            this.grid1.height(1F).width(1 - ratio);
            /* vertical elements shouldn't wrap. */
            this.wrap(false);
        }

        this.getRoot().resize(new DocumentFlowRow());
    }

    public Optional<UIPanelGrid> getGrid0() {
        return Optional.ofNullable(this.grid0);
    }

    public Optional<UIPanelGrid> getGrid1() {
        return Optional.ofNullable(this.grid1);
    }

    public void setPanel(UIPanel panel) {
        if (this.panel != null) {
            this.panel.parent = null;
        }

        this.panel = panel;
        this.panel.parent = this;

        this.borderColor(0,0,0,1);
        this.borderWidth(this.borderWidth);

        if (this.grid0 != null) this.grid0.parent = null;
        if (this.grid1 != null) this.grid1.parent = null;

        this.grid0 = null;
        this.grid1 = null;
        this.direction = null;
    }

    public Optional<UIPanel> getPanel() {
        return Optional.ofNullable(this.panel);
    }

    @Override
    public List<UIElement> getChildren() {
        if (this.panel != null) {
            return new ArrayList<>(Arrays.asList(this.panel));
        }

        return new ArrayList<>(Arrays.asList(this.grid0, this.grid1));
    }

    @Override
    public void addChildren(UIElement... elements) {
        throw new UnsupportedOperationException("Unsupported call to addChildren. UIPanelGrid does not support this method.");
    }

    @Override
    public void removeChild(UIElement element) {
        if (element == null) return;

        if (element == this.panel) {
            this.parent.removeChild(this);
        }

        if (element == this.grid0) {
            this.parentReplaceThisWith(this.grid1);
        } else if (element == this.grid1) {
            this.parentReplaceThisWith(this.grid0);
        }
    }

    @Override
    public void replaceChild(UIElement child, UIElement replacement) {
        if (child == null) {
            return;
        }

        if (child == this.panel && replacement instanceof UIPanel) {
            this.setPanel((UIPanel) replacement);
        }

        if (replacement instanceof UIPanelGrid) {
            if (child == this.grid0) {
                this.grid0.parent = null;
                this.grid0 = (UIPanelGrid) replacement;
                this.grid0.parent = this;
                this.getRoot().resize(new DocumentFlowRow());
            } else if (child == this.grid1) {
                this.grid1.parent = null;
                this.grid1 = (UIPanelGrid) replacement;
                this.grid1.parent = this;
                this.getRoot().resize(new DocumentFlowRow());
            }
        }
    }

    /**
     * When one of this child grid elements is replaced, it needs to call the parent to replace this with the specified grid.
     * Also, this does some dimension scale stuff so this grid fits correctly into the parent
     * @param grid teh grid to replace this in the parent with.
     */
    protected void parentReplaceThisWith(UIPanelGrid grid) {
        float height = 1F;
        float width = 1F;

        if (this.parent instanceof UIPanelGrid parentGrid) {
            if (parentGrid.direction == Orientation.HORIZONTAL) {
                height = this.getTransformation().getHeight().getValue();
            } else if (parentGrid.direction == Orientation.VERTICAL) {
                width = this.getTransformation().getWidth().getValue();
            }
        }

        grid.width(width);
        grid.height(height);

        this.parent.replaceChild(this, grid);
    }

    @Override
    public void postRender(UIContext context) {
        super.postRender(context);

        if (this.panel != null) return;

        /*
         * if there is already resize all cursor, it's best not to override it as other panels
         * somewhere in the tree might have prepared the resize all cursor
         */
        if ((this.isOnEdge(context.getMouseX(), context.getMouseY()) || this.dragging)
                && context.getPreparedCursor() != GLFW_RESIZE_ALL_CURSOR) {
            if (context.getPreparedCursor() == GLFW_VRESIZE_CURSOR || context.getPreparedCursor() == GLFW_HRESIZE_CURSOR
                    || this.grid0.isOnEdge(context.getMouseX(), context.getMouseY())
                    || this.grid1.isOnEdge(context.getMouseX(), context.getMouseY())) {
                context.prepareCursor(GLFW_RESIZE_ALL_CURSOR);
            } else if (this.direction == Orientation.HORIZONTAL) {
                context.prepareCursor(GLFW_VRESIZE_CURSOR);
            } else {
                context.prepareCursor(GLFW_HRESIZE_CURSOR);
            }
        }
    }

    protected boolean isOnEdge(double x, double y) {
        if (this.panel != null) return false;

        Area intersectionLine;
        Area grid0Area = this.grid0.getFlowArea();

        if (this.direction == Orientation.HORIZONTAL) {
            intersectionLine = new Area(grid0Area.getX() - 2, grid0Area.getY() + grid0Area.getHeight() - this.grid0.borderWidth - 2,
                    grid0Area.getWidth() + 4, this.grid0.borderWidth + this.grid1.borderWidth + 4);
        } else {
            intersectionLine = new Area(grid0Area.getX() + grid0Area.getWidth() - this.grid0.borderWidth - 2,
                    grid0Area.getY() - 2, this.grid0.borderWidth + this.grid1.borderWidth + 4, grid0Area.getHeight() + 4);
        }

        return intersectionLine.isInside(x, y);
    }

    /**
     * Reset the grids to what they were before dragging
     * @param context
     */
    protected void resetDragging(UIContext context) {
        this.getPanelGridRoot().convertToPixels();
        this.resetGrids();
        this.getRoot().resize(new DocumentFlowRow());
        this.getPanelGridRoot().convertToPercentage();

        this.postRenderedMouseRelease(context);
    }

    protected void resetGrids() {
        if (this.dragging) {
            this.offsetGrid(this.originalPos);
        }

        if (this.panel == null) {
            this.grid0.resetGrids();
            this.grid1.resetGrids();
        }
    }

    @Override
    public boolean postRenderedMouseClick(UIContext context) {
        if (context.isLeftMouseKey()
                && this.panel == null && this.isOnEdge(context.getMouseX(), context.getMouseY())) {
            this.clickGrids(context);

            context.registerClick(GLFW_MOUSE_BUTTON_RIGHT, this);

            return true;
        }

        if (context.isRightMouseKey() && this.dragging) {
            this.resetDragging(context);

            return true;
        }

        return false;
    }

    protected void clickGrids(UIContext context) {
        if (this.panel != null) return;

        if (this.isOnEdge(context.getMouseX(), context.getMouseY())) {
            this.dragging = true;

            if (this.direction == Orientation.VERTICAL) {
                this.originalPos = this.grid0.borderArea.getEndX();
            } else if (this.direction == Orientation.HORIZONTAL) {
                this.originalPos = this.grid0.borderArea.getEndY();
            }
        }

        this.grid0.clickGrids(context);
        this.grid1.clickGrids(context);
    }

    @Override
    public boolean postRenderedMouseRelease(UIContext context) {
        if (this.dragging) {
            this.releaseMouse(context);

            context.removeRegisteredClick(GLFW_MOUSE_BUTTON_RIGHT, this);

            return true;
        }

        return false;
    }

    /**
     * Goes through the panelGrids to release them
     * @param context
     */
    protected void releaseMouse(UIContext context) {
        if (this.dragging) {
            this.dragging = false;
            this.originalPos = 0;
        }

        if (this.panel == null) {
            this.grid0.releaseMouse(context);
            this.grid1.releaseMouse(context);
        }
    }

    /**
     * Override the default behavior to instead propagate the event from top to bottom.
     * This is needed because when you drag the edge of a child panelGrid, you might actually be dragging the bigger edge
     * of the parent grid which is what we want.
     */
    @Override
    public boolean mouseDragged(UIContext context, double dragX, double dragY) {
        /* for dragging we want pixels to prevent other panels from being moved. */
        this.getPanelGridRoot().convertToPixels();
        boolean result =  this._mouseDragged(context, dragX, dragY);
        /* for everything else we want percentages so resizing the entire game window affects the panels correctly */
        this.getPanelGridRoot().convertToPercentage();
        return result;
    }

    protected boolean _mouseDragged(UIContext context, double dragX, double dragY) {
        if (this.mouseDrag(context, dragX, dragY)) return true;

        for (UIElement child : this.getChildren()) {
            if (child instanceof UIPanelGrid) {
                if (((UIPanelGrid) child)._mouseDragged(context, dragX, dragY)) {
                    return true;
                }
            } else {
                if (child.mouseDragged(context, dragX, dragY)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean mouseDrag(UIContext context, double dragX, double dragY) {
        if (this.dragging) {
            this.dragGrids(context.getMouseX(), context.getMouseY());
            this.getRoot().resize(new DocumentFlowRow());

            return true;
        }

        return false;
    }

    protected void dragGrids(double mouseX, double mouseY) {
        if (this.dragging) {
            if (this.direction == Orientation.VERTICAL) {
                this.offsetGrid((int) mouseX);
            } else {
                this.offsetGrid((int) mouseY);
            }
        }

        if (this.panel == null) {
            this.grid0.dragGrids(mouseX, mouseY);
            this.grid1.dragGrids(mouseX, mouseY);
        }
    }

    protected void offsetGrid(int pos) {
        pos = Math.round(pos / 5F) * 5;
        if (this.direction == Orientation.VERTICAL) {
            int offsetX = pos - (this.grid0.borderArea.getEndX());
            int nearestX = offsetX > 0 ? this.grid1.findNearestX(HorizontalDirection.LEFT) : this.grid0.findNearestX(HorizontalDirection.RIGHT);

            if (offsetX < 0) {
                offsetX = Math.max(nearestX, pos) - (this.grid0.borderArea.getEndX());
            } else if (offsetX > 0) {
                offsetX = Math.min(nearestX, pos) - (this.grid0.borderArea.getEndX());
            }

            /* in the right grid it traverses all right vertical grids */
            this.grid0.resizeX(offsetX, Direction.RIGHT);
            /* in the left grid it traverses all left vertical grids */
            this.grid1.resizeX(-offsetX, Direction.LEFT);
        } else {
            int offsetY = pos - (this.grid0.borderArea.getEndY());
            int nearestY = offsetY > 0 ? this.grid1.findNearestY(VerticalDirection.TOP) : this.grid0.findNearestY(VerticalDirection.BOTTOM);

            if (offsetY < 0) {
                offsetY = Math.max(nearestY, pos) - (this.grid0.borderArea.getEndY());
            } else if (offsetY > 0) {
                offsetY = Math.min(nearestY, pos) - (this.grid0.borderArea.getEndY());
            }

            /* in the top grid it traverses all bottom horizontal grids */
            this.grid0.resizeY(offsetY, Direction.BOTTOM);
            /* in the bottom grid it traverses all top horizontal grids */
            this.grid1.resizeY(-offsetY, Direction.TOP);
        }
    }

    /**
     * @param offset
     * @param side which side of the grids to traverse.
     * @throws IllegalArgumentException when the side argument is any other value than TOP or BOTTOM.
     */
    protected void resizeY(int offset, Direction side) {
        this.height(this.borderArea.getHeight() + offset);

        if (this.panel == null) {
            if (this.direction == Orientation.VERTICAL) {
                this.grid0.resizeY(offset, side);
                this.grid1.resizeY(offset, side);
            } else if (this.direction == Orientation.HORIZONTAL) {
                if (side == Direction.TOP) {
                    this.grid0.resizeY(offset, side);
                } else if (side == Direction.BOTTOM) {
                    this.grid1.resizeY(offset, side);
                } else {
                    throw new IllegalArgumentException("The side argument can only be TOP or BOTTOM");
                }
            }
        }
    }

    /**
     *
     * @param side
     * @throws IllegalArgumentException when the side argument is any other value than TOP or BOTTOM.
     */
    protected int findNearestY(VerticalDirection side) {
        if (this.panel == null) {
            if (this.direction == Orientation.VERTICAL) {
                if (side == VerticalDirection.BOTTOM) {
                    return Math.max(this.grid1.findNearestY(side), this.grid0.findNearestY(side));
                } else if (side == VerticalDirection.TOP) {
                    return Math.min(this.grid1.findNearestY(side), this.grid0.findNearestY(side));
                }
            } else if (this.direction == Orientation.HORIZONTAL) {
                if (side == VerticalDirection.TOP) {
                    return this.grid0.findNearestY(side);
                } else if (side == VerticalDirection.BOTTOM) {
                    return this.grid1.findNearestY(side);
                }
            }
        } else {
            if (side == VerticalDirection.BOTTOM) {
                return this.borderArea.getY() + this.panel.getNavbar().getFlowArea().getHeight() + 2 * this.borderWidth;
            } else if (side == VerticalDirection.TOP) {
                return this.borderArea.getY() + this.borderArea.getHeight()
                        - this.panel.getNavbar().getFlowArea().getHeight() -  2 * this.borderWidth;
            }
        }

        return 0;
    }

    protected int findNearestX(HorizontalDirection side) {
        if (this.panel == null) {
            if (this.direction == Orientation.HORIZONTAL) {
                if (side == HorizontalDirection.LEFT) {
                    return Math.min(this.grid1.findNearestX(side), this.grid0.findNearestX(side));
                } else if (side == HorizontalDirection.RIGHT) {
                    return Math.max(this.grid1.findNearestX(side), this.grid0.findNearestX(side));
                }
            } else if (this.direction == Orientation.VERTICAL) {
                if (side == HorizontalDirection.LEFT) {
                    return this.grid0.findNearestX(side);
                } else if (side == HorizontalDirection.RIGHT) {
                    return this.grid1.findNearestX(side);
                }
            }
        } else {
            if (side == HorizontalDirection.LEFT) {
                return this.borderArea.getX() + this.borderArea.getWidth()
                        - this.panel.getNavbar().getFlowArea().getHeight() -  2 * this.borderWidth;
            } else if (side == HorizontalDirection.RIGHT) {
                return this.borderArea.getX() + this.panel.getNavbar().getFlowArea().getHeight() + 2 * this.borderWidth;
            }
        }

        return 0;
    }

    /**
     * @param offset
     * @param side which side of the grids to traverse.
     * @throws IllegalArgumentException when the side argument is any other value than LEFT or RIGHT.
     */
    protected void resizeX(int offset, Direction side) {
        this.width(this.borderArea.getWidth() + offset);

        if (this.panel == null) {
            if (this.direction == Orientation.HORIZONTAL) {
                this.grid0.resizeX(offset, side);
                this.grid1.resizeX(offset, side);
            } else if (this.direction == Orientation.VERTICAL) {
                if (side == Direction.LEFT) {
                    this.grid0.resizeX(offset, side);
                } else if (side == Direction.RIGHT) {
                    this.grid1.resizeX(offset, side);
                } else {
                    throw new IllegalArgumentException("The side argument can only be LEFT or RIGHT");
                }
            }
        }
    }

    protected UIPanelGrid getPanelGridRoot() {
        UIPanelGrid element = this;

        while (element.parent instanceof UIPanelGrid) {
            element = (UIPanelGrid) element.parent;
        }

        return element;
    }

    protected void convertToPixels() {
        this.width(this.borderArea.getWidth());
        this.height(this.borderArea.getHeight());

        if (this.panel == null) {
            this.grid0.convertToPixels();
            this.grid1.convertToPixels();
        }
    }

    protected void convertToPercentage() {
        this.width((float) this.borderArea.getWidth() / this.parent.borderArea.getWidth());
        this.height((float) this.borderArea.getHeight() / this.parent.borderArea.getHeight());

        if (this.panel == null) {
            this.grid0.convertToPercentage();
            this.grid1.convertToPercentage();
        }
    }

}

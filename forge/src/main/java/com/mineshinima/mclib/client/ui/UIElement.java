package com.mineshinima.mclib.client.ui;

import com.mineshinima.mclib.client.ui.transformation.UITransformation;
import com.mineshinima.mclib.utils.Color;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * When overriding internal children storage behaviour, you need to override following methods:
 * {@link #getChildren()}, {@link #addChildren(UIElement...)}, {@link #replaceChild(UIElement, UIElement)} and {@link #removeChild(UIElement)}.
 */
@OnlyIn(Dist.CLIENT)
public class UIElement {
    /**
     * The entire area occupied by this UIElement. This includes the content area and margins.
     * This is used for document flow.
     */
    //TODO should these be private to enforce setting the areas only via the setArea method???
    //  which might enforce more the idea of transformation belonging to UITransformation classes
    protected final Area flowArea = new Area();
    /**
     * This area includes the contentArea and the border.
     * With relative positioning, this area can be outside the document flow area {@link #flowArea}
     */
    protected final Area borderArea = new Area();
    /**
     * This area is offset by the border and is inside the borderArea.
     * The area that the content renders in.
     */
    protected final Area contentArea = new Area();
    /**
     * This is the area that is affected by padding and inside the contentArea.
     * Children render in the innerArea.
     */
    protected final Area innerArea = new Area();
    private final UITransformation<UIElement> transformation = new UITransformation<>(this);
    //TODO JUST FOR TESTING - REMOVE initialization LATER
    protected Color background = new Color((float) Math.random(), (float) Math.random(), (float) Math.random(), 1F);
    protected Color borderColor = new Color((float) Math.random(), (float) Math.random(), (float) Math.random(), 1F);
    private final List<UIElement> children = new ArrayList<>();
    /**
     * <b>Important when removing an element from the tree:</b><br>
     * When you set the parent to null with the intention of removing an element from the tree,
     * you need to call {@link #onClose()}!
     * <br> If you don't do this, it will lead to memory leaks
     * when elements expect a close call, for example, to unregister EventListeners.
     */
    protected UIElement parent;
    /**
     * Whether the element, and children, will be rendered.<br>
     * The element will still contribute to the document flow.
     */
    protected boolean visible = true;
    /**
     * Whether the children that are outside of {@link #contentArea} can be rendered.
     * true means children can render outside. See {@link #getScissoringArea()}.
     */
    protected boolean overflow = true;
    /**
     * This is a cache that will be set after the areas have been calculated.
     * Caching such an attribute will save some performance instead of going through the tree every render frame.
     *
     * This is dependent on overflow, display type and visibility -> if they change, this needs to update!
     */
    protected boolean canRender;
    /**
     * True after the first time rendering, or after the first time rendering after being closed by {@link #onClose()}
     */
    private boolean shown;

    /*
     * STYLE ATTRIBUTES
     * Methods should return this to allow easy method chaining.
     */

    /**
     * This won't render the UIElement, but it will still influence the dcoument flow (like in CSS).
     * If it shouldn't influence the document flow, checkout display none.
     * @param visible
     * @return
     */
    public UIElement visible(boolean visible) {
        this.visible = visible;
        this.canRender = this.canRender();
        return this;
    }

    public UIElement display(UITransformation.DISPLAY displayType) {
        this.getTransformation().setDisplay(displayType);
        this.canRender = this.canRender();
        return this;
    }

    public UIElement overflow(boolean overflow) {
        this.overflow = overflow;
        this.updateChildrenCanRender();
        return this;
    }

    /**
     * Update the {@link #canRender} cache for the children.<br><br>
     *
     * This is especially needed when overflow changes, as it influences the render ability of the children.
     */
    protected void updateChildrenCanRender() {
        //TODO possible bug: if scissoring area changes - maybe caching is not stable enough for the future.
        this.canRender = this.canRender();

        for (UIElement child : this.getChildren()) {
            child.updateChildrenCanRender();
        }
    }

    public UIElement backgroundColor(float r, float g, float b, float a) {
        this.background = new Color(r, g, b, a);
        return this;
    }

    public UIElement backgroundColor(float r, float g, float b) {
        return this.backgroundColor(r, g, b, 1F);
    }

    public UIElement borderWidth(int border) {
        this.getTransformation().getBorder().setValue(border);
        return this;
    }

    public UIElement borderColor(float r, float g, float b, float a) {
        this.borderColor = new Color(r, g, b, a);
        return this;
    }

    public UIElement borderColor(float r, float g, float b) {
        return this.borderColor(r, g, b, 1F);
    }

    public UIElement positionType(UITransformation.POSITION positionType) {
        this.getTransformation().setPositionType(positionType);
        return this;
    }

    public UIElement x(float x) {
        this.getTransformation().getX().setValue(x);
        return this;
    }

    public UIElement x(int x) {
        this.getTransformation().getX().setValue(x);
        return this;
    }

    public UIElement y(float y) {
        this.getTransformation().getY().setValue(y);
        return this;
    }

    public UIElement y(int y) {
        this.getTransformation().getY().setValue(y);
        return this;
    }

    public UIElement anchorX(float x) {
        this.getTransformation().getAnchorX().setValue(x);
        return this;
    }

    public UIElement anchorX(int x) {
        this.getTransformation().getAnchorX().setValue(x);
        return this;
    }

    public UIElement anchorY(float y) {
        this.getTransformation().getAnchorY().setValue(y);
        return this;
    }

    public UIElement anchorY(int y) {
        this.getTransformation().getAnchorY().setValue(y);
        return this;
    }

    public UIElement width(int value) {
        this.getTransformation().getWidth().setValue(value);
        return this;
    }

    public UIElement widthOffset(int value) {
        this.getTransformation().getWidth().setOffset(value);
        return this;
    }

    public UIElement width(float value) {
        this.getTransformation().getWidth().setValue(value);
        return this;
    }

    public UIElement widthAuto() {
        this.getTransformation().getWidth().setAuto();
        return this;
    }

    public UIElement minWidth(int value) {
        this.getTransformation().getMinWidth().setValue(value);
        return this;
    }

    public UIElement minWidth(float value) {
        this.getTransformation().getMinWidth().setValue(value);
        return this;
    }

    public UIElement height(int value) {
        this.getTransformation().getHeight().setValue(value);
        return this;
    }

    public UIElement heightOffset(int value) {
        this.getTransformation().getHeight().setOffset(value);
        return this;
    }

    public UIElement height(float value) {
        this.getTransformation().getHeight().setValue(value);
        return this;
    }

    public UIElement heightAuto() {
        this.getTransformation().getHeight().setAuto();
        return this;
    }

    public UIElement minHeight(int value) {
        this.getTransformation().getMinHeight().setValue(value);
        return this;
    }

    public UIElement minHeight(float value) {
        this.getTransformation().getMinHeight().setValue(value);
        return this;
    }


    /*
     * PADDING
     */


    public UIElement paddingTop(float p) {
        this.getTransformation().getPaddingTop().setValue(p);
        return this;
    }

    public UIElement paddingTop(int p) {
        this.getTransformation().getPaddingTop().setValue(p);
        return this;
    }

    public UIElement paddingBottom(float p) {
        this.getTransformation().getPaddingBottom().setValue(p);
        return this;
    }

    public UIElement paddingBottom(int p) {
        this.getTransformation().getPaddingBottom().setValue(p);
        return this;
    }

    public UIElement paddingLeft(float p) {
        this.getTransformation().getPaddingLeft().setValue(p);
        return this;
    }

    public UIElement paddingLeft(int p) {
        this.getTransformation().getPaddingLeft().setValue(p);
        return this;
    }

    public UIElement paddingRight(float p) {
        this.getTransformation().getPaddingRight().setValue(p);
        return this;
    }

    public UIElement paddingRight(int p) {
        this.getTransformation().getPaddingRight().setValue(p);
        return this;
    }


    /*
     * MARGIN
     */

    public UIElement marginTop(float value) {
        this.getTransformation().getMarginTop().setValue(value);
        return this;
    }

    public UIElement marginTop(int value) {
        this.getTransformation().getMarginTop().setValue(value);
        return this;
    }

    public UIElement marginRight(float value) {
        this.getTransformation().getMarginRight().setValue(value);
        return this;
    }

    public UIElement marginRight(int value) {
        this.getTransformation().getMarginRight().setValue(value);
        return this;
    }

    public UIElement marginRightAuto() {
        this.getTransformation().getMarginRight().setAuto();
        return this;
    }

    public UIElement marginBottom(float value) {
        this.getTransformation().getMarginBottom().setValue(value);
        return this;
    }

    public UIElement marginBottom(int value) {
        this.getTransformation().getMarginBottom().setValue(value);
        return this;
    }

    public UIElement marginLeft(float value) {
        this.getTransformation().getMarginLeft().setValue(value);
        return this;
    }

    public UIElement marginLeft(int value) {
        this.getTransformation().getMarginLeft().setValue(value);
        return this;
    }

    public UIElement marginLeftAuto() {
        this.getTransformation().getMarginLeft().setAuto();
        return this;
    }

    public UIElement wrap(boolean wrap) {
        this.getTransformation().setWrap(wrap);
        return this;
    }

    /*
     * STYLES END
     */

    /**
     * Goes through the tree and intersects all scissoring areas returned by {@link #getScissoringArea()},
     * including this element.
     * @return the global scissoring area when all parents and this have scissored too.
     */
    public Optional<Area> getGlobalScissoringArea() {
        if (this.parent == null) {
            return this.getScissoringArea();
        }

        Optional<Area> parentScissoring = this.parent.getGlobalScissoringArea();
        Optional<Area> currentScissoring = this.getScissoringArea();

        if (parentScissoring.isPresent() && currentScissoring.isPresent()) {
            return Optional.of(parentScissoring.get().intersect(currentScissoring.get()));
        } else if (parentScissoring.isPresent()) {
            return parentScissoring;
        } else if (currentScissoring.isPresent()) {
            return currentScissoring;
        }

        return Optional.empty();
    }

    /**
     * Tests whether the coordinates are inside the provided test area
     * that has been scissored by the global scissoring of the parents.
     * @param test
     * @param x
     * @param y
     * @return
     */
    protected boolean isInsideScissored(Area test, double x, double y) {
        Optional<Area> parentScissor = this.parent != null ? this.parent.getGlobalScissoringArea() : Optional.empty();

        if (parentScissor.isPresent()) {
            if (!parentScissor.get().intersects(test))  {
                return false;
            }

            return parentScissor.get().intersect(test).isInside(x, y);
        }

        return test.isInside(x, y);
    }

    /**
     * This method checks whether this or any parent element is not visible.
     * @return
     */
    public final boolean isVisible() {
        if (!this.visible || this.getTransformation().getDisplay() == UITransformation.DISPLAY.NONE) return false;

        return this.parent == null || this.parent.isVisible();
    }

    /**
     * This method checks if this element can be rendered in general. This includes visibility, display type
     * and whether this element is outside the global scissoring by the parents.
     * @return false if this element cannot be rendered.
     */
    public final boolean canRender() {
        if (!this.isVisible()) return false;

        if (this.parent != null) {
            Optional<Area> parentGlobalScissor = this.parent.getGlobalScissoringArea();

            if (parentGlobalScissor.isPresent() && !this.borderArea.intersects(parentGlobalScissor.get())) {
                return false;
            }
        }

        return true;
    }

    /**
     * This method checks whether this or any parent element has the display set to None.
     * This means it will not render and not contribute to the document flow.
     * @return
     */
    public final boolean isDisplayNone() {
        if (this.getTransformation().getDisplay() == UITransformation.DISPLAY.NONE) return true;

        return this.parent != null && this.parent.isDisplayNone();
    }

    @NotNull
    public final UIElement getRoot() {
        UIElement element = this;

        while (element.getParent().isPresent()) {
            element = element.getParent().get();
        }

        return element;
    }

    /**
     * Gets the {@link UIContext} from the root element if it's present.<br>
     * This is useful for scopes that don't naturally have access to {@link UIContext} through the method parameter.
     * @return
     */
    public Optional<UIContext> getRootContext() {
        if (this.getRoot() instanceof UIRootElement) {
            return Optional.of(((UIRootElement) this.getRoot()).getScreen().getContext());
        }

        return Optional.empty();
    }

    public void addChildren(UIElement... elements) {
        for (UIElement element : elements) {
            element.remove();
            element.parent = this;
        }

        this.children.addAll(Arrays.asList(elements));

    }

    /**
     * @return a copy list of the children of this element. Changes to this list will NOT be reflected in the internal list.
     *          See methods that modify the child/parent relationship
     *          like {@link #remove()} or {@link #removeChild(UIElement)} for alternatives
     */
    public List<UIElement> getChildren() {
        return new ArrayList<>(this.children);
    }

    public void removeChild(UIElement child) {
        this.children.remove(child);
        child.parent = null;
        child.onClose();
    }

    /**
     * Removes this element from the parent.
     */
    public void remove() {
        if (this.parent != null) {
            this.parent.removeChild(this);
            this.parent = null;
            this.onClose();
        }
    }

    public void replaceChild(UIElement child, UIElement replacement) {
        if (child == null) return;

        if (this.children.contains(child)) {
            if (replacement == null) {
                this.removeChild(child);

                return;
            }

            this.children.set(this.children.indexOf(child), replacement);
            replacement.parent = this;
            child.parent = null;
            child.onClose();
        }
    }

    public Optional<UIElement> getParent() {
        return Optional.ofNullable(this.parent);
    }

    /**
     * @return The transformation element responsible for calculating positions and dimensions
     */
    public UITransformation<?> getTransformation() {
        return this.transformation;
    }

    public void resetAreas() {
        this.flowArea.reset();
        this.borderArea.reset();
        this.contentArea.reset();
        this.innerArea.reset();
    }

    /**
     * @return a copy of this {@link #flowArea}.
     */
    public Area getFlowArea() {
        return this.flowArea.copy();
    }

    /**
     * @return a copy of this {@link #borderArea}.
     */
    public Area getBorderArea() {
        return this.borderArea.copy();
    }

    /**
     * @return a copy of this {@link #contentArea}.
     */
    public Area getContentArea() {
        return this.contentArea.copy();
    }

    /**
     * @return a copy of this {@link #innerArea}
     */
    public Area getInnerArea() {
        return this.innerArea.copy();
    }

    /**
     * Set all the areas.
     *
     * Note to future developers: There should only be this one setter method because all areas have a positional relationship.
     * Setting only one is a bad practice and might indicate a wrong calculation.
     * @param flowArea
     * @param contentArea
     * @param innerArea
     */
    public void setAreas(Area flowArea, Area borderArea, Area contentArea, Area innerArea) {
        this.flowArea.copy(flowArea);
        this.borderArea.copy(borderArea);
        this.contentArea.copy(contentArea);
        this.innerArea.copy(innerArea);
        this.canRender = this.canRender();
        this.onAreasSet();
    }

    /**
     * Calculates and caches the positions, widths and heights.
     * This should be called before the first render and when resizing.
     */
    public void resize(DocumentFlowRow parentRow) {
        this.getTransformation().resize(parentRow);
    }

    /**
     * This is called after resizing this element
     * and finishing the calculation of the areas.
     */
    public void onAreasSet() { }

    /**
     * This gets called when the UIElement is closed.
     */
    public final void onClose() {
        this.shown = false;
        this._onClose();

        for (UIElement element : this.getChildren()) {
            element.onClose();
        }
    }

    /**
     * This method is supposed to be overridden to define logic to happen when the UI is closed.
     */
    protected void _onClose() { }

    /**
     * This gets called when the UIElement is rendered for the first time.
     */
    public final void onShow() {
        this._onShow();

        for (UIElement element : this.getChildren()) {
            element.onShow();
        }
    }

    /**
     * This method is supposed to be overridden to define logic to happen when the UI is shown.
     */
    protected void _onShow() { }

    /**
     * Good old template pattern<br><br>
     *
     * This is final to enforce the order of rendering and the order of template methods.
     * The order of rendering and calling {@link #postRender(UIContext)} and {@link #preRender(UIContext)}
     * is important for the methods {@link #mouseClicked(UIContext)} and {@link #mouseReleased(UIContext)}.
     * @param context
     */
    public final void render(UIContext context) {
        if (!this.canRender) return;

        if (!this.shown) this.onShow();

        this.shown = true;

        this.preRender(context);

        if (this.getScissoringArea().isPresent()) {
            context.getUIGraphics().scissor(this.getScissoringArea().get());
        }

        for (UIElement child : this.getChildren()) {
            child.render(context);
        }

        if (this.getScissoringArea().isPresent()) {
            context.getUIGraphics().unscissor();
        }

        this.postRender(context);
    }

    /**
     * @return the area that would be used for scissoring the children.
     *         If this element doesn't perform scissoring, an empty Optional will be returned.
     */
    protected Optional<Area> getScissoringArea() {
        if (!this.overflow) {
            return Optional.of(this.contentArea.copy());
        }

        return Optional.empty();
    }

    /**
     * Render after children have been rendered. This will overlay the children,
     * the order of overlaying matches the traversal "Preorder".
     * @param context
     */
    public void postRender(UIContext context) {
        if (this.borderColor != null && this.getTransformation().getBorder().getValueInt() != 0) {
            context.getUIGraphics().renderBorder(this.borderArea, this.getTransformation().getBorder().getValueInt(), this.borderColor);
        }
    }

    /**
     * Render before children are rendered. This is where the main content normally goes.
     * The tree traversal is "Preorder", which means this rendered content will overlay every node visited before this.
     * @param context
     */
    public void preRender(UIContext context) {
        if (context.isDebug()) {
            //this.flowArea.render(new Color(0F, 0.25F, 1F, 0.25F));
        }

        if (this.background != null) {
            context.getUIGraphics().renderArea(this.borderArea, this.background);
        }

        if (context.isDebug()) {
            context.getUIGraphics().renderBorder(this.borderArea, 1, new Color(1,1,1,1));
            this.drawMargins(context);
            this.drawPaddings(context);
        }
    }

    protected void drawMargins(UIContext context) {
        int[] calculatedMargin = this.getTransformation().getCalculatedMargin();
        Color marginColor = new Color(1F, 0.5F, 0F, 0.5F);
        Area marginTop = new Area(this.borderArea.getX(), this.flowArea.getY(), this.borderArea.getWidth(), calculatedMargin[0]);
        context.getUIGraphics().renderArea(marginTop, marginColor);

        Area marginLeft = new Area(this.flowArea.getX(), this.borderArea.getY(), calculatedMargin[3], this.contentArea.getHeight());
        context.getUIGraphics().renderArea(marginLeft, marginColor);

        Area marginRight = new Area(this.borderArea.getX() + this.borderArea.getWidth(), this.borderArea.getY(), calculatedMargin[1], this.borderArea.getHeight());
        context.getUIGraphics().renderArea(marginRight, marginColor);

        Area marginBottom = new Area(this.borderArea.getX(), this.borderArea.getY() + this.borderArea.getHeight(), this.borderArea.getWidth(), calculatedMargin[2]);
        context.getUIGraphics().renderArea(marginBottom, marginColor);
    }

    protected void drawPaddings(UIContext context) {
        int[] calculatedPadding = this.getTransformation().getCalculatedPadding();
        Color paddingColor = new Color(0F, 1F, 0F, 0.5F);
        Area paddingTop = new Area(this.innerArea.getX(), this.contentArea.getY(), this.innerArea.getWidth(), calculatedPadding[0]);
        context.getUIGraphics().renderArea(paddingTop, paddingColor);

        Area paddingLeft = new Area(this.contentArea.getX(), this.innerArea.getY(), calculatedPadding[3], this.innerArea.getHeight());
        context.getUIGraphics().renderArea(paddingLeft, paddingColor);

        Area paddingRight = new Area(this.innerArea.getX() + this.innerArea.getWidth(), this.innerArea.getY(), calculatedPadding[1], this.innerArea.getHeight());
        context.getUIGraphics().renderArea(paddingRight, paddingColor);

        Area paddingBottom = new Area(this.innerArea.getX(), this.innerArea.getY() + this.innerArea.getHeight(), this.innerArea.getWidth(), calculatedPadding[2]);
        context.getUIGraphics().renderArea(paddingBottom, paddingColor);
    }


    /*
     * Mouse event capturing template pattern
     */

    /**
     * The order of event propagation should match the order of rendering.
     * The last rendered item should be the first to receive the event call.
     * @param context
     * @return
     */
    public final boolean mouseClicked(UIContext context) {
        if (this.postRenderedMouseClick(context)) return true;

        List<UIElement> children = this.getChildren();
        for (int i = children.size() - 1; i >= 0; i--) {
            UIElement element = children.get(i);
            if (!element.canRender) continue;
            if (element.mouseClicked(context)) return true;
        }

        return this.mouseClick(context);
    }

    /**
     * This is for handling clicks on stuff that is rendered in the method {@link #preRender(UIContext)}.
     * The order of calling matches the backwards of "Preorder" traversal.
     *
     * There is {@link #isMouseOver(UIContext)} to check if the mouse is over the element
     * and therefore validating whether you can click the element
     * @param context
     * @return true when this element has been clicked. This will stop the click propagation.
     */
    public boolean mouseClick(UIContext context) {
        return false;
    }

    /**
     * TODO rename this bad boi? McHorse thinks the name is misleading and I don't like it either as it's very long
     * This is for handling clicks on stuff that is rendered in the method {@link #postRender(UIContext)}.
     * This will be called before the children will be traversed.
     * This method helps if there is something that needs to be clicked
     * before the children and prevent them from being clicked.
     * @param context
     * @return true when this element has been clicked. This will stop the click propagation.
     */
    public boolean postRenderedMouseClick(UIContext context) {
        return false;
    }

    public final boolean mouseScrolled(UIContext context) {
        List<UIElement> children = this.getChildren();
        for (int i = children.size() - 1; i >= 0; i--) {
            UIElement element = children.get(i);
            if (!element.canRender) continue;
            if (element.mouseScrolled(context)) return true;
        }

        return this.mouseScroll(context);
    }

    public boolean mouseScroll(UIContext context) {
        return false;
    }

    /**
     * The order of event propagation should match the order of rendering.
     * The last rendered item should be the first to receive the event call.
     * @param context
     * @return
     */
    public final boolean mouseReleased(UIContext context) {
        if (this.postRenderedMouseRelease(context)) return true;

        List<UIElement> children = this.getChildren();
        for (int i = children.size() - 1; i >= 0; i--) {
            UIElement element = children.get(i);
            if (!element.canRender) continue;
            if (element.mouseReleased(context)) return true;
        }

        return this.mouseRelease(context);
    }

    public boolean mouseRelease(UIContext context) {
        return false;
    }

    public boolean postRenderedMouseRelease(UIContext context) {
        return false;
    }

    /**
     * Mouse dragging alone won't suffice. Elements need to catch whether they were clicked to determine
     * if the dragging is meant to influence them. Therefore, the order of event propagation doesn't matter here.
     * @param context
     * @return
     */
    public boolean mouseDragged(UIContext context, double dragX, double dragY) {
        List<UIElement> children = this.getChildren();
        for (int i = children.size() - 1; i >= 0; i--) {
            UIElement element = children.get(i);
            if (!element.canRender) continue;
            if (element.mouseDragged(context, dragX, dragY)) return true;
        }

        return this.mouseDrag(context, dragX, dragY);
    }

    public boolean mouseDrag(UIContext context, double dragX, double dragY) {
        return false;
    }

    /**
     * This just plainly checks if the coordinates are inside the rendering area.
     * This is an important method for user interaction as it determines whether you can click on an area.
     * This does not check whether the element can actually render.
     * @param context
     * @return true if the mouse is inside the area which takes into account any global scissoring area.
     */
    public boolean isMouseOver(UIContext context) {
        return this.isInsideScissored(this.contentArea, context.getMouseX(), context.getMouseY());
    }

    public void mouseMoved(UIContext context) {
        List<UIElement> children = this.getChildren();
        for (int i = children.size() - 1; i >= 0; i--) {
            UIElement element = children.get(i);

            element.mouseMoved(context);
        }

        this.mouseMove(context);
    }

    public void mouseMove(UIContext context) {
    }

    public boolean keyPressed(UIContext context) {
        List<UIElement> children = this.getChildren();
        for (int i = children.size() - 1; i >= 0; i--) {
            UIElement element = children.get(i);
            if (!element.canRender) continue;
            if (element.keyPressed(context)) return true;
        }

        return this.keyPress(context);
    }

    public boolean keyPress(UIContext context) {
        return false;
    }

    public boolean keyReleased(UIContext context) {
        List<UIElement> children = this.getChildren();
        for (int i = children.size() - 1; i >= 0; i--) {
            UIElement element = children.get(i);
            if (!element.canRender) continue;
            if (element.keyReleased(context)) return true;
        }

        return this.keyRelease(context);
    }

    public boolean keyRelease(UIContext context) {
        return false;
    }

    public boolean charTyped(UIContext context) {
        List<UIElement> children = this.getChildren();
        for (int i = children.size() - 1; i >= 0; i--) {
            UIElement element = children.get(i);
            if (!element.canRender) continue;
            if (element.charTyped(context)) return true;
        }

        return this.charType(context);
    }

    public boolean charType(UIContext context) {
        return false;
    }
}

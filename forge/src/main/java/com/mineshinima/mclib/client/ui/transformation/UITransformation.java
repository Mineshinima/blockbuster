package com.mineshinima.mclib.client.ui.transformation;

import com.mineshinima.mclib.client.ui.Area;
import com.mineshinima.mclib.client.ui.DocumentFlowRow;
import com.mineshinima.mclib.client.ui.UIElement;
import com.mineshinima.mclib.client.ui.unit.AbsoluteUnit;
import com.mineshinima.mclib.client.ui.unit.RelativeUnit;
import com.mineshinima.mclib.client.ui.unit.Unit;
import com.mineshinima.mclib.client.ui.unit.UnitType;

import javax.annotation.Nullable;
import java.util.*;

//TODO
//an option how borders should be handled -> should the border be overlaying or should it push the content
//POSITION ABSOLUTE - check CSS again - ABSOLUTE IS RELATIVE TO THE NEAREST RELATIVE ANCESTOR OR SO - do we need this?
public class UITransformation<T extends UIElement> {
    private RelativeUnit x = new RelativeUnit(0);
    private RelativeUnit y = new RelativeUnit(0);
    private POSITION position = POSITION.RELATIVE;
    private RelativeUnit anchorX = new RelativeUnit(0);
    private RelativeUnit anchorY = new RelativeUnit(0);
    private Unit width = new Unit(0);
    private RelativeUnit minWidth = new RelativeUnit(0);
    private Unit height = new Unit(0);
    private RelativeUnit minHeight = new RelativeUnit(0);
    /**
     * NOTE: Border is currently handled like css "box-sizing: border-box"
     */
    private AbsoluteUnit border = new AbsoluteUnit(0);
    /**
     * Whether to break the elements into a new row when they don't fit anymore.
     */
    private boolean wrap = true;
    private final int[] calculatedPadding = new int[4];
    /**
     * padding: top, right, bottom, left
     */
    private RelativeUnit[] padding = new RelativeUnit[]{new RelativeUnit(0), new RelativeUnit(0), new RelativeUnit(0), new RelativeUnit(0)};
    /**
     * margin: top, right, bottom, left
     */
    private RelativeUnit[] margin = new RelativeUnit[]{new RelativeUnit(0), new RelativeUnit(0), new RelativeUnit(0), new RelativeUnit(0)};
    private final int[] calculatedMargin = new int[4];
    private DISPLAY display = DISPLAY.INLINE_BLOCK;

    protected final T target;

    public UITransformation(T target) {
        if (target == null) throw new IllegalArgumentException("WTF ARE YOU DOING!?");

        this.target = target;
    }

    public int[] getCalculatedMargin() {
        return this.calculatedMargin.clone();
    }

    public int[] getCalculatedPadding() {
        return this.calculatedPadding.clone();
    }

    public RelativeUnit getX() {
        return this.x;
    }

    public RelativeUnit getY() {
        return this.y;
    }

    public AbsoluteUnit getBorder() {
        return this.border;
    }

    public POSITION getPositionType() {
        return this.position;
    }

    public void setPositionType(POSITION type) {
        this.position = type;
    }

    public RelativeUnit getAnchorX() {
        return this.anchorX;
    }

    public RelativeUnit getAnchorY() {
        return this.anchorY;
    }

    public Unit getWidth() {
        return this.width;
    }

    public RelativeUnit getMinWidth() {
        return this.minWidth;
    }

    public Unit getHeight() {
        return this.height;
    }

    public RelativeUnit getMinHeight() {
        return this.minHeight;
    }

    public boolean isWrap() {
        return this.wrap;
    }

    public void setWrap(boolean wrap) {
        this.wrap = wrap;
    }


    /*
     * PADDING
     */

    public RelativeUnit getPaddingTop() {
        return this.padding[0];
    }

    public RelativeUnit getPaddingRight() {
        return this.padding[1];
    }

    public RelativeUnit getPaddingBottom() {
        return this.padding[2];
    }

    public RelativeUnit getPaddingLeft() {
        return this.padding[3];
    }


    /*
     * MARGIN
     */

    public RelativeUnit getMarginTop() {
        return this.margin[0];
    }

    public RelativeUnit getMarginRight() {
        return this.margin[1];
    }

    public RelativeUnit getMarginBottom() {
        return this.margin[2];
    }

    public RelativeUnit getMarginLeft() {
        return this.margin[3];
    }

    public DISPLAY getDisplay() {
        return this.display;
    }

    public void setDisplay(DISPLAY display) {
        this.display = display;
    }

    /**
     * This method does the actual calculation and traverses the UI tree.
     * After this method has been called, the provided parentRow may have ended, after which {@link DocumentFlowRow#reset()} must be called.
     * This can happen when the element, upon which this method was called, did not fit into the document flow row anymore.
     * @param parentRow
     */
    public void resize(DocumentFlowRow parentRow) {
        if (this.target.isDisplayNone()) return;

        /*
         * Reset the areas for a clean start.
         */
        this.target.resetAreas();

        boolean widthAuto = this.width.getType() == UnitType.AUTO;
        boolean heightAuto = this.height.getType() == UnitType.AUTO;
        boolean autoDimensions = widthAuto || heightAuto;
        boolean wrap = this.wrap;

        if (autoDimensions) {
            /*
             * To calculate the positions of this target, we don't need document flow.
             * We can't do document flow here, because the document flow is determined by the dimensions,
             * which are not calculated yet since they are AUTO. However, we still need to calculate.
             *
             * The positions need to be calculated before the children are traversed, so they are already correctly positioned
             * and we don't need to traverse the children again after setting the dimensions.
             */
            this.apply(null);

            if (widthAuto) {
                this.setWrap(false);
            }
        } else {
            this.apply(parentRow);
        }

        ChildrenResult childrenResult = this.traverseChildren();

        int[] paddings = this.calculatePaddings();

        if (widthAuto) {
            this.width.setValue(childrenResult.maxWidth + paddings[1] + paddings[3] + 2 * this.border.getValueInt());
        }

        if (heightAuto) {
            this.height.setValue(childrenResult.totalHeight + paddings[0] + paddings[2]);
        }

        if (autoDimensions) {
            this.target.resetAreas();
            this.apply(parentRow);

            /* the children need to know what the auto dimensions were */
            if (widthAuto) {
                this.width.setAuto();
            }

            if (heightAuto) {
                this.height.setAuto();
            }

            /*
             * dimensions influence the document flow which influences the position,
             * so the children need to be recalculated
             */
            this.traverseChildren();
        }

        if (widthAuto) {
            this.setWrap(wrap);
        }
    }

    /**
     * Offset this element and the children element, only if their position type
     * implies a positional relationship between the child and this element.
     * Position type global does not fall under this category, as it ignores the parent's position.
     *
     * The offset will always be applied to this target element,
     * but when the child does not have a positional relationship with this target, it will be ignored.
     * @param x
     * @param y
     */
    public void offsetTree(int x, int y) {
        final DocumentFlowRow.AreaNode root = new DocumentFlowRow.AreaNode(this.target.getFlowArea());
        final DocumentFlowRow.AreaNode borderNode = root.appendChild(this.target.getBorderArea());
        final DocumentFlowRow.AreaNode contentNode = root.appendChild(this.target.getContentArea());
        final DocumentFlowRow.AreaNode innerNode = root.appendChild(this.target.getInnerArea());
        root.addX(x);
        root.addY(y);

        this.target.setAreas(root.getArea(), borderNode.getArea(), contentNode.getArea(), innerNode.getArea());

        for (UIElement child : this.target.getChildren()) {
            child.getTransformation().offsetTree(x, y);
        }
    }

    protected ChildrenResult traverseChildren() {
        boolean rowBreak = false;
        int totalHeight = 0;
        int maxWidth = 0;
        DocumentFlowRow flowRow = new DocumentFlowRow();
        Set<DocumentFlowRow> rows = new LinkedHashSet<>();
        List<UIElement> children = this.target.getChildren();

        for (int i = 0; i < children.size(); i++) {
            UIElement element = children.get(i);
            element.resize(flowRow);

            if (flowRow.isEnd()) {
                maxWidth = Math.max(flowRow.getWidth(), maxWidth);
                totalHeight += flowRow.getMaxHeight();
                rows.add(flowRow);
                flowRow = new DocumentFlowRow();
                rowBreak = true;
            }

            if (element.getTransformation().getPositionType() != UITransformation.POSITION.ABSOLUTE) {
                flowRow.addElement(element);
            }

            if (i == children.size() - 1) {
                totalHeight += flowRow.getMaxHeight();
                maxWidth = Math.max(flowRow.getWidth(), maxWidth);
                rows.add(flowRow);
            }
        }

        return new ChildrenResult(rowBreak, totalHeight, maxWidth, new ArrayList<>(rows));
    }

    /**
     * Calculates the areas for this target element. Traverse the UI tree and call this method.
     * @param row
     */
    protected void apply(@Nullable final DocumentFlowRow row) {
        final Area parentInnerArea = this.getParentInnerArea();

        /**
         * The instances that can be manipulated by calculations and will later be set.
         */
        final Area flowArea = this.target.getFlowArea();
        final Area borderArea = this.target.getBorderArea();
        final Area contentArea = this.target.getContentArea();
        final Area innerArea = this.target.getInnerArea();

        final DocumentFlowRow.AreaNode root = new DocumentFlowRow.AreaNode(flowArea);
        final DocumentFlowRow.AreaNode borderNode = root.appendChild(borderArea);
        final DocumentFlowRow.AreaNode contentNode = root.appendChild(contentArea);
        final DocumentFlowRow.AreaNode innerNode = root.appendChild(innerArea);

        this.setContentAreaDimensions(contentArea);
        this.setBorderAreaDimensions(borderArea, contentArea);

        final int[] margin = this.calculateMargins();
        this.calculatedMargin[0] = margin[0];
        this.calculatedMargin[1] = margin[1];
        this.calculatedMargin[2] = margin[2];
        this.calculatedMargin[3] = margin[3];

        this.setFlowAreaDimensions(flowArea, borderArea, margin);

        root.setX(parentInnerArea.getX());
        root.setY(parentInnerArea.getY());

        int anchorX = this.calculatePixels(borderArea.getWidth(), this.anchorX);
        int anchorY = this.calculatePixels(borderArea.getHeight(), this.anchorY);
        int x = this.calculatePixels(borderArea.getWidth(), this.x) - anchorX;
        int y = this.calculatePixels(borderArea.getHeight(), this.y) - anchorY;

        if (this.position == POSITION.RELATIVE) {
            if (row != null) this.calculateDocumentFlow(row, flowArea, borderArea, contentArea, innerArea);

            /* offset the position */
            borderNode.addX(x);
            borderNode.addY(y);
        } else if (this.position == POSITION.ABSOLUTE){
            /*
             * TODO In CSS absolute positions it without document flow relative to the closest ancestor.
             * What about an easy way of positioning with screen coordinates?
             */
            borderNode.addX(x);
            borderNode.addY(y);
        }

        final int borderWidth = this.getBorder().getValueInt();

        borderNode.addX(margin[3]);
        borderNode.addY(margin[0]);
        contentNode.addX(borderWidth);
        contentNode.addY(borderWidth);

        final int[] padding = this.calculatePaddings();
        this.calculatedPadding[0] = padding[0];
        this.calculatedPadding[1] = padding[1];
        this.calculatedPadding[2] = padding[2];
        this.calculatedPadding[3] = padding[3];

        this.setInnerAreaDimensions(innerArea, contentArea, padding);

        innerNode.addX(padding[3]);
        innerNode.addY(padding[0]);

        this.target.setAreas(flowArea, borderArea, contentArea, innerArea);
    }

    /**
     * Calculates the document flow of the given root area.
     * This method modifies the positions of the areas in the given chain.
     *
     * If it fits besides the previous elements in the given row, it will be placed in the row, if not, the row will end,
     * and it will flow into a new row.
     * @param row
     */
    protected void calculateDocumentFlow(DocumentFlowRow row, Area flowArea, Area borderArea, Area contentArea, Area innerArea) {
        if (row.getLast().isEmpty()) return;

        final DocumentFlowRow.AreaNode root = new DocumentFlowRow.AreaNode(flowArea);
        root.appendChild(borderArea);
        root.appendChild(contentArea);
        root.appendChild(innerArea);

        final boolean parentWrap = this.target.getParent().isEmpty() || this.target.getParent().get().getTransformation().isWrap();
        final Area parentInnerArea = this.getParentInnerArea();
        final boolean isLastBlock = row.getLast().isPresent() && row.getLast().get().getTransformation().display == DISPLAY.BLOCK;

        root.addY(row.getY() - parentInnerArea.getY());

        if (this.display != DISPLAY.BLOCK && !isLastBlock
                && (parentInnerArea.getWidth() - row.getWidth() >= flowArea.getWidth() || !parentWrap)) {
            root.addX(row.getWidth());
        } else {
            /* element doesn't fit -> breaks into new row */
            root.addY(row.getMaxHeight());
            row.end();
        }
    }

    /**
     * Calculate the margins of the given target
     * @return {marginTop, marginRight, marginBottom, marginLeft}
     */
    protected int[] calculateMargins() {
        final Area parentInnerArea = this.getParentInnerArea();
        int marginTop = this.calculatePixels(parentInnerArea.getHeight(), this.getMarginTop());
        int marginBottom = this.calculatePixels(parentInnerArea.getHeight(), this.getMarginBottom());
        int marginLeft = this.calculatePixels(parentInnerArea.getWidth(), this.getMarginLeft());
        int marginRight = this.calculatePixels(parentInnerArea.getWidth(), this.getMarginRight());

        return new int[]{marginTop, marginRight, marginBottom, marginLeft};
    }

    protected int[] calculatePaddings() {
        final Area parentInnerArea = this.getParentInnerArea();
        int paddingTop = this.calculatePixels(parentInnerArea.getHeight(), this.getPaddingTop());
        int paddingBottom = this.calculatePixels(parentInnerArea.getHeight(), this.getPaddingBottom());
        int paddingLeft = this.calculatePixels(parentInnerArea.getWidth(), this.getPaddingLeft());
        int paddingRight = this.calculatePixels(parentInnerArea.getWidth(), this.getPaddingRight());

        return new int[]{paddingTop, paddingRight, paddingBottom, paddingLeft};
    }

    protected void setFlowAreaDimensions(Area flowArea, Area borderArea, int[] margin) {
        flowArea.setHeight(margin[0] + margin[2] + borderArea.getHeight());
        flowArea.setWidth(margin[3] + margin[1] + borderArea.getWidth());
    }

    protected void setContentAreaDimensions(Area contentArea) {
        final Area parentInnerArea = this.getParentInnerArea();

        final int borderWidth = this.border.getValueInt();
        final int minWidth = this.calculatePixels(parentInnerArea.getWidth(), this.minWidth);
        final int minHeigth = this.calculatePixels(parentInnerArea.getHeight(), this.minHeight);

        contentArea.setWidth(this.calculatePixels(parentInnerArea.getWidth(), this.width));
        contentArea.setHeight(this.calculatePixels(parentInnerArea.getHeight(), this.height));

        if (contentArea.getWidth() < minWidth) {
            contentArea.setWidth(minWidth);
        }

        if (contentArea.getHeight() < minHeigth) {
            contentArea.setHeight(minHeigth);
        }

        /* box-sizing: border-box */
        contentArea.setWidth(contentArea.getWidth() - borderWidth * 2);
        contentArea.setHeight(contentArea.getHeight() - borderWidth * 2);
    }

    protected void setBorderAreaDimensions(Area borderArea, Area contentArea) {
        final int borderWidth = this.border.getValueInt();

        borderArea.setWidth(contentArea.getWidth() + borderWidth * 2);
        borderArea.setHeight(contentArea.getHeight() + borderWidth * 2);
    }

    protected void setInnerAreaDimensions(Area innerArea, Area contentArea, int[] padding) {
        innerArea.setWidth(contentArea.getWidth() - padding[1] - padding[3]);
        innerArea.setHeight(contentArea.getHeight() - padding[0] - padding[2]);
    }

    /**
     *
     * @param relative the value to use when the unit is in percentage.
     * @param unit the unit to calculate the pixels
     * @return the pixel value of the given unit.
     */
    protected int calculatePixels(int relative, RelativeUnit unit) {
        if (unit.getType() == UnitType.PERCENTAGE) {
            /*
             * Math.round can cause issues sometimes where with "perfect" percentages,
             * e.g. 4 * 25% width elements might not always fit in one row
             * TODO floor fixes this but could in theory lead to 1 pixel inconsistencies
             */
            return (int) Math.floor(relative * unit.getValue());
        } else if (unit.getType() == UnitType.PIXEL) {
            return (int) unit.getValue();
        } else {
            return 0;
        }
    }

    /**
     * @return a ready to use parentInnerArea. If there is no parent, an empty Area will be returned.
     * If the parent has auto dimensions, the respective inner area dimension will be set to 0
     * to prevent percentages from referring to an auto dimension.
     */
    protected Area getParentInnerArea() {
        final Area parentInnerArea;

        if (this.target.getParent().isPresent()) {
            final UIElement parent = this.target.getParent().get();
            parentInnerArea = parent.getInnerArea();

            /* This ensures that percentages cannot refer to auto dimensions */
            if (parent.getTransformation().width.getType() == UnitType.AUTO) {
                parentInnerArea.setWidth(0);
            }

            if (parent.getTransformation().height.getType() == UnitType.AUTO) {
                parentInnerArea.setHeight(0);
            }
        } else {
            parentInnerArea = new Area(0,0,0,0);
        }

        return parentInnerArea;
    }

    public enum POSITION {
        RELATIVE,
        /**
         * Relative to its own position, but does not contribute to the document flow.
         * TODO in CSS absolute is relative to the closest ancestor that has relative or so. Yay fun with offsetTree method.
         */
        ABSOLUTE
    }

    public enum DISPLAY {
        BLOCK,
        INLINE_BLOCK,
        NONE
    }

    /**
     * Helper class to return the results of traversing the children elements.
     */
    protected class ChildrenResult {
        private boolean rowBreak;
        private int totalHeight;
        private int maxWidth;
        private final List<DocumentFlowRow> rows = new ArrayList<>();

        public ChildrenResult(boolean rowBreak, int totalHeight, int maxWidth, List<DocumentFlowRow> rows) {
            this.totalHeight = totalHeight;
            this.rowBreak = rowBreak;
            this.maxWidth = maxWidth;
            this.rows.addAll(rows);
        }

        public boolean isRowBreak() {
            return this.rowBreak;
        }

        public int getTotalHeight() {
            return this.totalHeight;
        }

        public int getMaxWidth() {
            return this.maxWidth;
        }

        public List<DocumentFlowRow> getRows() {
            return new ArrayList<>(this.rows);
        }
    }
}

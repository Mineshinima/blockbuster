package com.mineshinima.mclib.client.ui.transformation;

import com.mineshinima.mclib.client.ui.Area;
import com.mineshinima.mclib.client.ui.DocumentFlowRow;
import com.mineshinima.mclib.client.ui.UIScrollElement;
import org.joml.Math;

public class UIScrollTransformation extends UITransformation<UIScrollElement> {
    private int maxWidth;
    private int totalHeight;
    private int scrollbarWidth = 12;

    public UIScrollTransformation(UIScrollElement target) {
        super(target);
    }

    public int getScrollbarWidth() {
        return this.scrollbarWidth;
    }

    public void setScrollbarWidth(int scrollbarWidth) {
        this.scrollbarWidth = scrollbarWidth;
    }

    public int getMaxWidth() {
        return this.maxWidth;
    }

    public int getTotalHeight() {
        return this.totalHeight;
    }

    /**
     * @return how much can be scrolled vertically
     */
    public int getVerticalScrollSize() {
        int barOffset = 0;

        if (this.target.isRenderScrollbar() && !this.target.isOverlayScrollbar() && this.target.isScrollHorizontal()) {
            barOffset = this.scrollbarWidth;
        }

        return this.totalHeight + this.getCalculatedPadding()[0] + this.getCalculatedPadding()[2] - barOffset;
    }

    /**
     * @return how much can be scrolled horizontally
     */
    public int getHorizontalScrollSize() {
        int barOffset = 0;

        if (this.target.isRenderScrollbar() && !this.target.isOverlayScrollbar() && this.target.isScrollVertical()) {
            barOffset = this.scrollbarWidth;
        }

        return this.maxWidth + this.getCalculatedPadding()[1] + this.getCalculatedPadding()[3] - barOffset;
    }

    public void scroll() {
        this.traverseChildren();
    }

    @Override
    protected ChildrenResult traverseChildren() {
        Area offsetInnerArea = this.target.getInnerArea();

        /*
         * The area needs to be offset here, so the scrolling will be applied for every resizing situation
         * When resizing the maximum scroll offsets change and the scroll value needs to be clamped again
         */
        this.target.setScrollX(this.target.getScrollX());
        this.target.setScrollY(this.target.getScrollY());

        offsetInnerArea.addX(Math.clamp(-this.target.maxHorizontalScrollOffset(), 0, target.getScrollX()));
        offsetInnerArea.addY(Math.clamp(-this.target.maxVerticalScrollOffset(), 0, target.getScrollY()));

        this.target.setAreas(this.target.getFlowArea(), this.target.getBorderArea(), this.target.getContentArea(), offsetInnerArea);

        ChildrenResult result = super.traverseChildren();

        offsetInnerArea.addX(-this.target.getScrollX());
        offsetInnerArea.addY(-this.target.getScrollY());

        this.target.setAreas(this.target.getFlowArea(), this.target.getBorderArea(), this.target.getContentArea(), offsetInnerArea);

        this.maxWidth = result.getMaxWidth();
        this.totalHeight = result.getTotalHeight();

        return result;
    }

    @Override
    protected int[] calculatePaddings() {
        final int[] padding = super.calculatePaddings();

        if (this.target.isRenderScrollbar() && !this.target.isOverlayScrollbar()) {
            if (this.target.isScrollHorizontal()) {
                padding[2] += this.target.getScrollbarWidth();
            }

            if (this.target.isScrollVertical()) {
                padding[1] += this.target.getScrollbarWidth();
            }
        }

        return padding;
    }
}

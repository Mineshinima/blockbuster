package com.mineshinima.mclib.client.ui;

import com.mineshinima.mclib.utils.ICopy;
import org.joml.Math;

public class Area implements ICopy<Area> {
    private int x;
    private int y;
    private int width;
    private int height;

    public Area() {

    }

    public Area(int x, int y, int width, int height) {
        this.setX(x);
        this.setY(y);
        this.setWidth(width);
        this.setHeight(height);
    }

    public int getX() {
        return this.x;
    }

    public int getEndX() {
        return this.getX() + this.width;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void addX(int x) {
        this.x = this.x + x;
    }

    public int getY() {
        return this.y;
    }

    public int getEndY() {
        return this.getY() + this.height;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void addY(int y) {
        this.y = this.y + y;
    }

    public int getWidth() {
        return this.width;
    }

    public void setWidth(int width) {
        this.width = Math.clamp(0, Integer.MAX_VALUE, width);
    }

    public int getHeight() {
        return this.height;
    }

    public void setHeight(int height) {
        this.height = Math.clamp(0, Integer.MAX_VALUE, height);
    }

    public boolean isInside(double x, double y) {
        return x >= this.x && x < this.x + this.width && y >= this.y && y < this.y + this.height;
    }

    public void reset() {
        this.x = 0;
        this.y = 0;
        this.width = 0;
        this.height = 0;
    }

    /**
     * Check whether given area intersects this area
     */
    public boolean intersects(Area area) {
        return this.x < area.getEndX() && this.y < area.getEndY()
                && area.x < this.getEndX() && area.y < this.getEndY();
    }

    /**
     * @param area the area to intersect this with.
     * @return a new Area that is the result of the intersection with the given area and this.
     */
    public Area intersect(Area area) {
        int x = Math.clamp(this.getX(), this.getEndX(), area.getX());
        int y = Math.clamp(this.getY(), this.getEndY(), area.getY());
        int w = Math.max(Math.min(this.getEndX(), area.getEndX()) - x, 0);
        int h = Math.max(Math.min(this.getEndY(), area.getEndY()) - y, 0);

        return new Area(x, y, w, h);
    }

    @Override
    public Area copy() {
        return new Area(this.x, this.y, this.width, this.height);
    }

    @Override
    public void copy(Area origin) {
        this.x = origin.x;
        this.y = origin.y;
        this.width = origin.width;
        this.height = origin.height;
    }

    @Override
    public String toString() {
        return "Area[x=" + this.x + ", y=" + this.y + ", width=" + this.width + ", height=" + this.height + "]";
    }
}

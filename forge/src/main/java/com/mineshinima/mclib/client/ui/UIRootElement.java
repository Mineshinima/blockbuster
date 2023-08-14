package com.mineshinima.mclib.client.ui;

public class UIRootElement extends UIElement {
    private final UIScreen screen;

    public UIRootElement(UIScreen screen) {
        this.screen = screen;
    }

    public UIScreen getScreen() {
        return this.screen;
    }
}

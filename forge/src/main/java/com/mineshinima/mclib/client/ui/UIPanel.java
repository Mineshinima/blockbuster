package com.mineshinima.mclib.client.ui;

import com.mineshinima.mclib.client.ui.space.Orientation;
import com.mineshinima.mclib.client.ui.transformation.UITransformation;
import com.mineshinima.mclib.utils.Color;
import org.lwjgl.glfw.GLFW;

public class UIPanel extends UIElement {
    private UIElement navbar;
    private UIElement body;

    public UIPanel(UIElement navbar, UIElement body) {
        this.width(1F).height(1F);

        int navbarHeight = 50;

        this.navbar = new UIScrollElement()
                .scrollDirection(Orientation.HORIZONTAL)
                .overlayScrollbar(false)
                .wrap(false)
                .width(1F)
                .height(navbarHeight)
                .paddingBottom(10)
                .paddingTop(10)
                .paddingLeft(10)
                .paddingRight(10)
                .backgroundColor(0.25F, 0.25F, 0.25F, 0.25F);

        this.navbar.addChildren(new UIElement().width(100).height(1F),
                new UIElement().width(100).height(1F),
                new UIElement().width(100).height(1F),
                new UIElement().width(100).height(1F),
                new UIElement().width(100).height(1F),
                new UIElement().width(100).height(1F),
                new UIElement().width(100).height(1F),
                new UIElement().width(100).height(1F),
                new UIElement().width(100).height(1F),
                new UIElement().width(100).height(1F),
                new UIElement().width(100).height(1F));

        this.body = body
                .width(1F)
                .height(1F)
                .heightOffset(-navbarHeight)
                .backgroundColor(0F,0F,0F,0F);

        //TODO we need a GridResizer thing like in McLib that distributes the children space because the body must take up all the left space
        this.addChildren(this.navbar, this.body);
        this.background = new Color(0,0,0,0);
    }

    public UIElement getNavbar() {
        return this.navbar;
    }

    public UIElement getBody() {
        return this.body;
    }

    @Override
    public boolean keyPress(UIContext context) {
        if (!this.isMouseOver(context)) return false;

        //TODO for testing and debugging - remove later
        if (context.getKeyboardKey() == GLFW.GLFW_KEY_4) {
            this.remove();

            return true;
        } else if (context.getKeyboardKey() == GLFW.GLFW_KEY_5) {
            if (this.parent instanceof UIPanelGrid) {
                float ratio = (float) ((context.getMouseY() - this.contentArea.getY()) / this.contentArea.getHeight());

                ((UIPanelGrid) this.parent).subdivide(Orientation.HORIZONTAL, ratio);

                return true;
            }
        } else if (context.getKeyboardKey() == GLFW.GLFW_KEY_6) {
            if (this.parent instanceof UIPanelGrid) {
                float ratio = (float) ((context.getMouseX() - this.contentArea.getX()) / this.contentArea.getWidth());

                ((UIPanelGrid) this.parent).subdivide(Orientation.VERTICAL, ratio);

                return true;
            }
        }

        return false;
    }
}

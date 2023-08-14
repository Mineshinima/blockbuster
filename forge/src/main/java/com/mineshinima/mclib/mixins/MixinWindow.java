package com.mineshinima.mclib.mixins;

import com.mineshinima.mclib.client.rendering.IMixinWindow;
import com.mineshinima.mclib.client.rendering.WindowHandler;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.WindowEventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Window.class)
public abstract class MixinWindow implements IMixinWindow {
    @Shadow
    private int framebufferWidth;
    @Shadow
    private int framebufferHeight;
    @Shadow
    private long window;
    @Shadow
    private int width;
    @Shadow
    private int height;
    @Shadow
    private WindowEventHandler eventHandler;

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void onFramebufferResize(long p_85416_, int p_85417_, int p_85418_) {
        if (p_85416_ == this.window) {
            int i = this.getWidth();
            int j = this.getHeight();
            if (p_85417_ != 0 && p_85418_ != 0) {
                if (WindowHandler.isOverwriting()) {
                    this.eventHandler.resizeDisplay();
                } else {
                    this.framebufferWidth = p_85417_;
                    this.framebufferHeight = p_85418_;
                    if (this.getWidth() != i || this.getHeight() != j) {
                        this.eventHandler.resizeDisplay();
                    }
                }
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        int w = this.framebufferWidth;
        int h = this.framebufferHeight;
        this.framebufferWidth = width;
        this.framebufferHeight = height;
        this.width = width;
        this.height = height;

        if (w != this.framebufferWidth || h != this.framebufferHeight) {
            /*
             * the screen shouldn't resize here, because the resizing might be
             * triggered by UI resizing -> would result in recursion
             */
            Screen tmp = Minecraft.getInstance().screen;
            Minecraft.getInstance().screen = null;
            this.eventHandler.resizeDisplay();
            Minecraft.getInstance().screen = tmp;
        }
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    private void onResize(long p_85428_, int p_85429_, int p_85430_) {
        if (!WindowHandler.isOverwriting()) {
            this.width = p_85429_;
            this.height = p_85430_;
        }
    }

    @Shadow
    public int getWidth() {
        return this.framebufferWidth;
    }

    @Shadow
    public int getHeight() {
        return this.framebufferHeight;
    }
}

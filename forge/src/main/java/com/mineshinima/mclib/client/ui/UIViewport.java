package com.mineshinima.mclib.client.ui;

import com.mineshinima.mclib.client.rendering.WindowHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class UIViewport extends UIElement {
    @Override
    public void onAreasSet() {
        WindowHandler.setOverwriteMinecraft(true);
        WindowHandler.queueResize(this.contentArea.getWidth(), this.contentArea.getHeight());
    }

    @Override
    protected void _onClose() {
        WindowHandler.setOverwriteMinecraft(false);
    }

    @Override
    public void preRender(UIContext context) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, Minecraft.getInstance().getMainRenderTarget().getColorTextureId());

        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        context.getUIGraphics().buildUVQuad(bufferBuilder, this.contentArea);

        BufferUploader.drawWithShader(bufferBuilder.end());
    }
}

package com.mineshinima.mclib.mixins;

import com.mineshinima.mclib.utils.MatrixUtils;
import com.mineshinima.mclib.utils.rendering.RenderingUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer {
    /**
     * We want to capture the projection matrix of the camera after all effects have been applied.
     */
    @Inject(method = "renderLevel(FJLcom/mojang/blaze3d/vertex/PoseStack;)V", at = @At(
            value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;resetProjectionMatrix(Lorg/joml/Matrix4f;)V", shift = At.Shift.AFTER
    ))
    public void afterResetProjectionMatrix(float p_109090_, long p_109091_, PoseStack p_109092_, CallbackInfo ci) {
        RenderingUtils.mixinReadProjectionMatrix();
    }
}

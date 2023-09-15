package com.mineshinima.mclib.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import org.joml.Matrix4f;

public class MatrixUtils {
    private static final Matrix4f cameraProjection = new Matrix4f();

    public static void mixinReadProjectionMatrix() {
        cameraProjection.set(RenderSystem.getProjectionMatrix());
    }

    public static Matrix4f getCameraProjection() {
        return new Matrix4f(cameraProjection);
    }
}

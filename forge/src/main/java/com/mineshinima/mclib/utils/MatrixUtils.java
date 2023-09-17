package com.mineshinima.mclib.utils;

import org.joml.Matrix3f;
import org.joml.Matrix4d;

public class MatrixUtils {
    public static Matrix4d toMatrix4d(Matrix3f matrix3f) {
        return new Matrix4d(matrix3f.m00, matrix3f.m01, matrix3f.m02, 0,
                            matrix3f.m10, matrix3f.m11, matrix3f.m12, 0,
                            matrix3f.m20, matrix3f.m21, matrix3f.m22, 0,
                       0, 0, 0, 1);
    }
}

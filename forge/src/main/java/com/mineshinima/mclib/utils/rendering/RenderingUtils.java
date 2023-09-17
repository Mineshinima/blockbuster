package com.mineshinima.mclib.utils.rendering;

import com.mineshinima.mclib.utils.Color;
import com.mineshinima.mclib.utils.MatrixUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.joml.*;

import javax.annotation.Nullable;
import java.lang.Math;
import java.util.List;

public class RenderingUtils {
    private static final Matrix4f cameraProjection = new Matrix4f();

    /**
     * Read and store the projection matrix used for rendering the world.
     */
    public static void mixinReadProjectionMatrix() {
        cameraProjection.set(RenderSystem.getProjectionMatrix());
    }

    /**
     * @return the stored projection matrix the world was rendered with.
     * This is useful for scopes, like UI rendering, that don't have access to the world projection matrix.
     */
    public static Matrix4f getCameraProjection() {
        return new Matrix4f(cameraProjection);
    }

    public static void buildBillboard(BufferBuilder bufferBuilder, double x, double y, double z, double w, double h, float u, float v) {
        bufferBuilder.vertex(x + w, y, z).uv(u,v).endVertex();
        bufferBuilder.vertex(x, y, z).uv(0,v).endVertex();
        bufferBuilder.vertex(x, y + h, z).uv(0,0).endVertex();
        bufferBuilder.vertex(x + w, y + h, z).uv(u,0).endVertex();
    }

    public static void renderLine(List<Vector3f> points, Color color, float thickness) {

    }

    public static void renderLine(Vector3f a, Vector3f b, Color color, float thickness) {

    }

    /**
     * Project the screen coordinates onto a given plane in world space. The plane is composed of the normal and planeOrigin.
     * @param x screen x coordinate, needs to be in a range between -1 and 1
     * @param y screen y coordinate, needs to be in a range between -1 and 1
     * @param planeNormal
     * @param  planeOrigin
     * @return null if no intersection was found
     */
    @Nullable
    public static Vector3d projectScreenOntoPlane(double x, double y, Vector3d planeNormal, Vector3d planeOrigin) {
        Vec3 cPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        Vector3d cameraPos = new Vector3d(cPos.x, cPos.y, cPos.z);

        return projectScreenOntoPlane(cameraPos, x, y, planeNormal, planeOrigin);
    }

    /**
     * Project the screen coordinates onto a given plane in world space. The plane is composed of the normal and planeOrigin.
     * @param cameraPos the position of the camera from which the ray will be projected of.
     * @param x screen x coordinate, needs to be in a range between -1 and 1
     * @param y screen y coordinate, needs to be in a range between -1 and 1
     * @param planeNormal
     * @param  planeOrigin
     * @return null if no intersection was found
     */
    @Nullable
    public static Vector3d projectScreenOntoPlane(Vector3d cameraPos, double x, double y, Vector3d planeNormal, Vector3d planeOrigin) {
        Matrix4d cameraToWorld = MatrixUtils.toMatrix4d(RenderSystem.getInverseViewRotationMatrix()).mul(getCameraProjection().invert());

        Vector4d mouse = new Vector4d(x, y, 0, 1);
        cameraToWorld.transform(mouse);
        Vector3d ray = new Vector3d(mouse.x, mouse.y, mouse.z).normalize();

        double factor = Intersectiond.intersectRayPlane(cameraPos, ray, planeOrigin, planeNormal, 0.000000001);

        if (factor == -1) {
            factor = Intersectiond.intersectRayPlane(cameraPos, ray, planeOrigin, new Vector3d(planeNormal).mul(-1), 0.000000001);
        }

        return factor == -1 ? null : ray.mul(factor);
    }

    /**
     * Get the rotation matrix needed to rotate a quad facing towards the main camera.
     * @param facing
     * @param position the position of the billboard center to use for facing calculations.
     * @return
     */
    public static Matrix4f getFacingRotation(Facing facing, Vector3f position)
    {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Matrix4f rotation = new Matrix4f();

        float cYaw = camera.getYRot();
        float cPitch = camera.getXRot();
        double cX = camera.getPosition().x;
        double cY = camera.getPosition().y;
        double cZ = camera.getPosition().z;

        boolean lookAt = facing == RenderingUtils.Facing.LOOKAT_XYZ || facing == RenderingUtils.Facing.LOOKAT_Y;

        if (lookAt)
        {
            double dX = cX - position.x;
            double dY = cY - position.y;
            double dZ = cZ - position.z;
            double horizontalDistance = Math.sqrt(dX * dX + dZ * dZ);

            cYaw = 180 + (float) (Math.toDegrees(Math.atan2(dZ, dX)) - 90.0F);
            cPitch = (float) (Math.toDegrees(Math.atan2(dY, horizontalDistance)));
        }

        switch (facing) {
            case LOOKAT_XYZ:
            case ROTATE_XYZ:
                rotation.rotateY((float) Math.toRadians(180 - cYaw));
                rotation.rotateX((float) Math.toRadians(-cPitch));
                break;
            case ROTATE_Y:
            case LOOKAT_Y:
                rotation.rotateY((float) Math.toRadians(180 - cYaw));
                break;
        }

        return rotation;
    }

    public enum Facing
    {
        ROTATE_XYZ("rotate_xyz"),
        ROTATE_Y("rotate_y"),
        LOOKAT_XYZ("lookat_xyz"),
        LOOKAT_Y("lookat_y");

        public final String id;

        public static Facing fromString(String string)
        {
            for (Facing facing : values())
            {
                if (facing.id.equals(string))
                {
                    return facing;
                }
            }

            return null;
        }

        Facing(String id)
        {
            this.id = id;
        }
    }
}

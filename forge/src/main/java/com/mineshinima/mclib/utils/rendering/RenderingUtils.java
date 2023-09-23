package com.mineshinima.mclib.utils.rendering;

import com.mineshinima.mclib.utils.Color;
import com.mineshinima.mclib.utils.MatrixUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Transformation;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
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

    public static void renderCircle(Vector3f center, Vector3f normal, float radius, int divisions, Color color, float thickness) {
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        Matrix3f rotation = new Matrix3f();
        rotation.rotateY((float) Math.toRadians(getYaw(normal)));
        rotation.rotateX((float) Math.toRadians(getPitch(normal)));

        for (int i = 1; i <= divisions; i++) {
            float angle0 = (float) (2 * Math.PI / divisions * (i - 1));
            float angle1 = (float) (2 * Math.PI / divisions * i);

            Vector3f a = new Vector3f(radius * (float) Math.cos(angle0), radius * (float) Math.sin(angle0), 0);
            Vector3f b = new Vector3f(radius * (float) Math.cos(angle1), radius * (float) Math.sin(angle1), 0);
            rotation.transform(a);
            rotation.transform(b);
            a.add(center);
            b.add(center);

            buildLine(builder, a, b, color, thickness);
        }

        Tesselator.getInstance().end();
    }

    /**
     *
     * @param center
     * @param normal
     * @param majorRadius the radius of the torus in total
     * @param minorRadius the radius of the torus actual mesh going around the center
     * @param majorDivisions
     * @param minorDivisions
     * @param color
     */
    public static void renderTorus(Vector3d center, Vector3d normal, float majorRadius, float minorRadius, int majorDivisions, int minorDivisions,  Color color) {
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        Matrix3d rotation = new Matrix3d();
        rotation.rotateY(Math.toRadians(getYaw(normal)));
        rotation.rotateX(Math.toRadians(getPitch(normal)));

        Matrix3d majorRotation = new Matrix3d();

        /*
         * First go through the major shape on the XY plane
         */
        for (int i = 1; i <= majorDivisions; i++) {
            double angle0 = 2 * Math.PI / majorDivisions * (i - 1);
            double angle1 = 2 * Math.PI / majorDivisions * i;

            /*
             * build a circle with minorDivisions on the YZ plane,
             * duplicate it and rotate it by the majorAngles to get the quads.
             * This happens per quad here.
             */
            for (int j = 1; j <= minorDivisions; j++) {
                double angleMinor0 = 2 * Math.PI / minorDivisions * (j - 1);
                double angleMinor1 = 2 * Math.PI / minorDivisions * j;

                Vector3d v0 = new Vector3d(0, majorRadius + minorRadius * Math.sin(angleMinor0), minorRadius * Math.cos(angleMinor0));
                Vector3d v1 = new Vector3d(0, majorRadius + minorRadius * Math.sin(angleMinor1), minorRadius * Math.cos(angleMinor1));
                Vector3d v2 = new Vector3d(v0);
                Vector3d v3 = new Vector3d(v1);
                majorRotation.set(rotation).rotateZ(angle0);
                majorRotation.transform(v0);
                majorRotation.transform(v1);
                majorRotation.set(rotation).rotateZ(angle1);
                majorRotation.transform(v2);
                majorRotation.transform(v3);

                v0.add(center);
                v1.add(center);
                v2.add(center);
                v3.add(center);

                builder.vertex(v1.x, v1.y, v1.z).color(color.getRGBAColor()).endVertex();
                builder.vertex(v3.x, v3.y, v3.z).color(color.getRGBAColor()).endVertex();
                builder.vertex(v2.x, v2.y, v2.z).color(color.getRGBAColor()).endVertex();
                builder.vertex(v0.x, v0.y, v0.z).color(color.getRGBAColor()).endVertex();
            }
        }

        Tesselator.getInstance().end();
    }

    public static void renderLine(List<Vector3f> points, Color color, float thickness) {

    }

    public static void renderLine(Vector3f a, Vector3f b, Color color, float thickness) {
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        buildLine(builder, a, b, color, thickness);
        Tesselator.getInstance().end();
    }

    /**
     * Not advised to perform transformation as this might fuck up look at direction facing.
     * @param builder
     * @param a
     * @param b
     * @param color
     * @param thickness
     */
    public static void buildLine(BufferBuilder builder, Vector3f a, Vector3f b, Color color, float thickness) {
        Vector3f direction = new Vector3f(b).sub(a);
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Matrix4f viewRotation = new Matrix4f(RenderSystem.getInverseViewRotationMatrix()).invert();
        Vector3f cameraPos = new Vector3f((float) camera.getPosition().x, (float) camera.getPosition().y, (float) camera.getPosition().z);
        Vector3f distance = new Matrix3f(viewRotation).transform(new Vector3f(a).add(new Vector3f(direction).mul(0.5F)).sub(cameraPos));
        float fov = RenderSystem.getProjectionMatrix().perspectiveFov();
        thickness = (float) (thickness / 2F * 0.25F * Math.abs(distance.z) * (Math.tan(fov / 2)));

        Matrix4f transformation = getFacingRotation(Facing.LOOKAT_DIRECTION, a, direction);
        transformation.m30(a.x);
        transformation.m31(a.y);
        transformation.m32(a.z);
        Vector4f[] vertices = new Vector4f[4];
        vertices[0] = new Vector4f(-thickness, direction.length(), 0, 1);
        vertices[1] = new Vector4f(-thickness, 0, 0, 1);
        vertices[2] = new Vector4f(thickness, 0, 0, 1);
        vertices[3] = new Vector4f(thickness, direction.length(), 0, 1);

        for (Vector4f vertex : vertices) {
            transformation.transform(vertex);
            builder.vertex(vertex.x, vertex.y, vertex.z).color(color.getR(), color.getG(), color.getB(), color.getA()).endVertex();
        }
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
     * Get the rotation matrix needed to rotate a quad.<br>
     * It's assumed the up-vector is (0,1,0) and the quad faces +Z.
     * @param facing
     * @param position the position of the billboard center to use for facing calculations.
     * @param direction is used when the facing mode {@link Facing#isDirection} is true. Otherwise it can be null
     * @return
     */
    public static Matrix4f getFacingRotation(Facing facing, Vector3f position, @Nullable Vector3f direction) throws IllegalArgumentException {
        if (facing.isDirection && direction == null) {
            throw new IllegalArgumentException("Argument direction cannot be null when the facing mode has isDirection=true");
        }

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

        if (facing.isDirection)
        {
            direction = new Vector3f(direction);
            double lengthSq = direction.lengthSquared();
            if (lengthSq < 0.000001)
            {
                direction.set(1, 0, 0);
            }
            else if (Math.abs(lengthSq - 1) > 0.000001)
            {
                direction.normalize();
            }
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
            case LOOKAT_DIRECTION:
                Vector3f cameraDir = new Vector3f((float) (cX - position.x),
                                                  (float) (cY - position.y),
                                                  (float) (cZ - position.z));
                Vector4f rotatedNormal = new Vector4f(0,0,1,0);


                rotation.rotateY((float) Math.toRadians(getYaw(direction)));
                rotation.rotateX((float) Math.toRadians(getPitch(direction) + 90));

                rotation.transform(rotatedNormal);

                /*
                 * The direction vector is the normal of the plane used for calculating the rotation around local y Axis.
                 * Project the cameraDir onto that plane to find out the axis angle (direction vector is the y axis).
                 */
                cameraDir.sub(new Vector3f(direction).mul(cameraDir.dot(direction)));

                if (cameraDir.lengthSquared() < 1.0e-30) break;

                cameraDir.normalize();

                /*
                 * The angle between two vectors is only between 0 and 180 degrees.
                 * RotationDirection will be parallel to direction but pointing in different directions depending
                 * on the rotation of cameraDir. Use this to find out the sign of the angle
                 * between cameraDir and the rotatedNormal.
                 */
                Vector3f rotationDirection = new Vector3f(rotatedNormal.x, rotatedNormal.y, rotatedNormal.z).cross(cameraDir);
                rotation.rotateY(Math.copySign(cameraDir.angle(new Vector3f(rotatedNormal.x, rotatedNormal.y, rotatedNormal.z)), rotationDirection.dot(direction)));
                break;

        }

        return rotation;
    }

    public static float getYaw(Vector3f direction) {
        return (float) getYaw(new Vector3d(direction));
    }

    public static float getPitch(Vector3f direction) {
        return (float) getPitch(new Vector3d(direction));
    }

    public static double getYaw(Vector3d direction) {
        double yaw = Math.atan2(-direction.x, direction.z);
        yaw = Math.toDegrees(yaw);
        if (yaw < -180) {
            yaw += 360;
        } else if (yaw > 180) {
            yaw -= 360;
        }
        return -yaw;
    }

    public static double getPitch(Vector3d direction) {
        double pitch = Math.atan2(direction.y, Math.sqrt(direction.x * direction.x + direction.z * direction.z));
        return -Math.toDegrees(pitch);
    }

    public enum Facing
    {
        ROTATE_XYZ("rotate_xyz"),
        ROTATE_Y("rotate_y"),
        LOOKAT_XYZ("lookat_xyz", true, false),
        LOOKAT_Y("lookat_y", true, false),
        LOOKAT_DIRECTION("lookat_direction", true, true),
        DIRECTION_X("direction_x", false, true),
        DIRECTION_Y("direction_y", false, true),
        DIRECTION_Z("direction_z", false, true),
        EMITTER_XY("emitter_transform_xy"),
        EMITTER_XZ("emitter_transform_xz"),
        EMITTER_YZ("emitter_transform_yz");

        public final String id;
        public final boolean isLookAt;
        public final boolean isDirection;

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

        Facing(String id, boolean isLookAt, boolean isDirection)
        {
            this.id = id;
            this.isLookAt = isLookAt;
            this.isDirection = isDirection;
        }

        Facing(String id)
        {
            this(id, false, false);
        }
    }
}

package com.mineshinima.mclib.client.ui;

import com.mineshinima.mclib.client.rendering.WindowHandler;
import com.mineshinima.mclib.utils.EntityUtils;
import com.mineshinima.mclib.utils.MathUtils;
import com.mineshinima.mclib.utils.rendering.RenderingUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.joml.*;

import java.lang.Math;

import static org.lwjgl.glfw.GLFW.glfwSetCursorPos;

@OnlyIn(Dist.CLIENT)
public class UIViewport extends UIElement {
    private PlayerState lastPlayerState;
    protected boolean orbiting;
    protected boolean panning;
    protected float yaw;
    protected float pitch;
    protected Vector3d pos = new Vector3d();
    protected Vector3d orbitOrigin = new Vector3d();
    /**
     * The threshold of the distance between orbit and position after which the user will be slowed down.
     */
    protected float zoomThreshold = 1.25F;

    @Override
    public boolean mouseClick(UIContext context) {
        if (this.isMouseOver(context)) {
            if (context.isMiddleMouseKey() && !Screen.hasShiftDown()) {
                this.orbiting = true;

                return true;
            } else if (context.isMiddleMouseKey() && Screen.hasShiftDown()) {
                this.panning = true;

                return true;
            }
        }

        return false;
    }

    @Override
    public boolean mouseRelease(UIContext context) {
        if ((this.orbiting || this.panning) && context.isMiddleMouseKey()) {
            this.orbiting = false;
            this.panning = false;

            return true;
        }

        return false;
    }

    @Override
    public boolean mouseDrag(UIContext context, double dragX, double dragY) {
        if (this.orbiting || this.panning) {
            if (context.getMouseX() < this.contentArea.getX()) {
                glfwSetCursorPos(context.getWindow().getWindow(), this.contentArea.getEndX(), context.getMouseY());
                context.ignoreNextMouseMove();
            } else if (context.getMouseX() > this.contentArea.getEndX()) {
                glfwSetCursorPos(context.getWindow().getWindow(), this.contentArea.getX(), context.getMouseY());
                context.ignoreNextMouseMove();
            }

            if (context.getMouseY() < this.contentArea.getY()) {
                glfwSetCursorPos(context.getWindow().getWindow(), context.getMouseX(), this.contentArea.getEndY());
                context.ignoreNextMouseMove();
            } else if (context.getMouseY() > this.contentArea.getEndY()) {
                glfwSetCursorPos(context.getWindow().getWindow(), context.getMouseX(), this.contentArea.getY());
                context.ignoreNextMouseMove();
            }
        }

        if (this.orbiting) {
            Vector3d relativePosition = new Vector3d(this.pos).sub(this.orbitOrigin);

            this.yaw += dragX * 0.5F;
            this.pitch += dragY * 0.5F;

            Vector3d basis = new Vector3d(0, 0, relativePosition.length());
            this.rotateVectorToView(basis);
            basis.mul(-1);

            this.pos = basis.add(this.orbitOrigin);
            this.updatePlayer();

            return true;
        }

        if (this.panning) {
            Vector3d viewVector = new Vector3d(0,0,1);

            this.rotateVectorToView(viewVector);

            Vec3 cPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
            Vector3d cameraPos = new Vector3d(cPos.x, cPos.y, cPos.z);

            /*
             * When the camera is very near at the orbit position, panning is super slow.
             * Move the camera back along the view vector to counter this a little bit.
             */
            double dist = new Vector3d(cameraPos).sub(this.orbitOrigin).length();
            if (dist <= 0.05) {
                cameraPos.add(new Vector3d(viewVector).mul(-(0.05 - dist)));
            }

            double nx = -MathUtils.mapRange(context.getMouseX(), this.contentArea.getX(), this.contentArea.getEndX(), -1, 1);
            double ny = MathUtils.mapRange(context.getMouseY(), this.contentArea.getY(), this.contentArea.getEndY(), -1, 1);
            double nx0 = -MathUtils.mapRange(context.getMouseX() - dragX, this.contentArea.getX(), this.contentArea.getEndX(), -1, 1);
            double ny0 = MathUtils.mapRange(context.getMouseY() - dragY, this.contentArea.getY(), this.contentArea.getEndY(), -1, 1);

            Vector3d destination = RenderingUtils.projectScreenOntoPlane(cameraPos, nx, ny, viewVector, this.orbitOrigin);
            Vector3d origin = RenderingUtils.projectScreenOntoPlane(cameraPos, nx0, ny0, viewVector, this.orbitOrigin);
            Vector3d drag = destination.sub(origin);

            this.pos.add(drag);
            this.orbitOrigin.add(drag);
            this.updatePlayer();

            return true;
        }

        return false;
    }

    protected void rotateVectorToView(Vector3d view) {
        Matrix3d rot = new Matrix3d();
        rot.rotateY(Math.toRadians(-this.yaw));
        rot.rotateX(Math.toRadians(this.pitch));
        rot.transform(view);
    }

    @Override
    public boolean mouseScroll(UIContext context) {
        if (this.isMouseOver(context) && !this.orbiting && !this.panning) {
            Vector3d viewVector = new Vector3d(0, 0, 0.35F);
            this.rotateVectorToView(viewVector);

            Vector3d relativePos = new Vector3d(this.orbitOrigin).sub(this.pos);

            double factor = Math.max(this.zoomFactor(relativePos.length(), context.getMouseScroll()), 0);
            double derivativeX = this.zoomThreshold;
            double derivative = this.zoomFactorDerivative(derivativeX, context.getMouseScroll());
            double linearYIntersection = this.zoomFactor(derivativeX, context.getMouseScroll()) - derivative * derivativeX;

            /* piece wise function to make zooming out better */
            if (relativePos.length() >= derivativeX) {
                factor = derivative * relativePos.length() + linearYIntersection;
            }

            viewVector.mul(Math.round(factor * context.getMouseScroll() * 10000) / 10000F);

            this.pos.add(viewVector);
            this.updatePlayer();

            return true;
        }

        return false;
    }

    public double zoomFactor(double distance, double scroll) {
        /* add 0.01 to x when zooming in because otherwise you would get stuck - stop before the orbit position */
        return -Math.exp(-distance + Math.max(0.01F * scroll, 0)) + 1;
    }

    public double zoomFactorDerivative(double distance, double scroll) {
        return Math.exp(-distance + Math.max(0.01F * scroll, 0));
    }

    protected void updatePlayer() {
        if (Minecraft.getInstance().player == null) return;

        Entity camera = Minecraft.getInstance().cameraEntity;

        camera.setXRot(this.pitch);
        camera.setYRot(this.yaw);
        camera.setPos(this.pos.x, this.pos.y - camera.getEyeHeight(), this.pos.z);
        camera.xo = this.pos.x;
        camera.yo = this.pos.y - camera.getEyeHeight();
        camera.zo = this.pos.z;
        camera.xOld = this.pos.x;
        camera.yOld = this.pos.y - camera.getEyeHeight();
        camera.zOld = this.pos.z;
    }

    @Override
    public void onAreasSet() {
        WindowHandler.setOverwriteMinecraft(true);
        WindowHandler.queueResize(this.contentArea.getWidth(), this.contentArea.getHeight());
    }

    @Override
    protected void _onClose() {
        WindowHandler.setOverwriteMinecraft(false);
        WindowHandler.resizeToWindowSize();

        if (this.lastPlayerState != null && Minecraft.getInstance().player != null) {
            this.lastPlayerState.apply(Minecraft.getInstance().player);
        }

        this.lastPlayerState = null;
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @Override
    protected void _onShow() {
        if (Minecraft.getInstance().player == null) return;

        LocalPlayer player = Minecraft.getInstance().player;
        player.setDeltaMovement(0,0,0);
        float bodyYaw = player.yBodyRot;
        this.pitch = player.getXRot();
        this.yaw = player.getYRot();
        this.pos = new Vector3d(player.getX(), player.getY() + player.getEyeHeight(), player.getZ());
        Vector3d viewVector = new Vector3d(0, 0, 1);
        this.rotateVectorToView(viewVector);
        this.orbitOrigin = new Vector3d(this.pos).add(new Vector3d(viewVector.x, viewVector.y, viewVector.z).mul(3));
        GameType gamemode = EntityUtils.getGameMode(player);
        CameraType pov = Minecraft.getInstance().options.getCameraType();

        EntityUtils.setGameMode(player, GameType.SPECTATOR);
        Minecraft.getInstance().options.setCameraType(CameraType.FIRST_PERSON);

        this.lastPlayerState = new PlayerState(new Vector3d(player.getX(), player.getY(), player.getZ()), bodyYaw, this.pitch, this.yaw, pov, gamemode);

        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onRenderLast(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) return;

        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Matrix4f viewRotation = new Matrix4f(RenderSystem.getInverseViewRotationMatrix()).invert();
        PoseStack poseStack = RenderSystem.getModelViewStack();
        poseStack.pushPose();
        /* set the pose explicitly so any transformations before get ignored */
        poseStack.last().pose().set(viewRotation);
        poseStack.last().normal().set(viewRotation);
        poseStack.translate(-camera.getPosition().x, -camera.getPosition().y, -camera.getPosition().z);
        RenderSystem.applyModelViewMatrix();
        RenderSystem.disableDepthTest();

        Vector3f pos = new Vector3f(0.5F, 0.5F, 0F);
        Vector3f cameraPos = new Vector3f((float) camera.getPosition().x, (float) camera.getPosition().y, (float) camera.getPosition().z);
        Vector3f distance = new Matrix3f(viewRotation).transform(new Vector3f(pos).sub(cameraPos));
        float fov = RenderSystem.getProjectionMatrix().perspectiveFov();

        TestCube test = new TestCube();
        test.scale.set(Math.max(0.00F, 0.1F * Math.abs(distance.z) * 2 * (Math.tan(fov / 2))));
        test.position.set(pos);
        test.render();

        poseStack.popPose();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.enableDepthTest();
    }

    @Override
    public void preRender(UIContext context) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, Minecraft.getInstance().getMainRenderTarget().getColorTextureId());

        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        context.getUIGraphics().buildUVQuad(bufferBuilder, this.contentArea);

        Tesselator.getInstance().end();
    }

    private record PlayerState(Vector3d pos, float bodyYaw, float headPitch, float headYaw, CameraType pov,
                               GameType gamemode) {
        public void apply(LocalPlayer player) {
            player.setPos(new Vec3(this.pos.x, this.pos.y, this.pos.z));
            player.setYBodyRot(this.bodyYaw);
            player.setYRot(this.headYaw);
            player.setXRot(this.headPitch);
            Minecraft.getInstance().options.setCameraType(this.pov);
            EntityUtils.setGameMode(player, this.gamemode);
        }
    }
}

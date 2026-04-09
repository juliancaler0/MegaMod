package com.ultra.megamod.feature.casino.wheel.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.ultra.megamod.feature.casino.wheel.WheelBlockEntity;
import com.ultra.megamod.feature.casino.wheel.WheelSegment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

/**
 * Renders the casino wheel as a single textured rotating quad on the wall.
 * The wheel texture (wheel_face.png) has all the colored segments pre-drawn.
 */
public class WheelRenderer implements BlockEntityRenderer<WheelBlockEntity, WheelRenderState> {

    private static final Identifier WHEEL_TEXTURE =
            Identifier.fromNamespaceAndPath("megamod", "textures/gui/casino/wheel_face.png");
    private static final Identifier POINTER_TEXTURE =
            Identifier.fromNamespaceAndPath("megamod", "textures/gui/casino/wheel_face.png");

    private static final float RADIUS = 2.5f;

    // Client animation
    private static float displayAngle = 0f;
    private static float targetAngle = 0f;
    private static float spinVelocity = 0f;
    private static String currentPhase = "BETTING";
    private static long lastTickMs = 0;

    public WheelRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public WheelRenderState createRenderState() {
        return new WheelRenderState();
    }

    @Override
    public void extractRenderState(WheelBlockEntity blockEntity, WheelRenderState state, float partialTick,
                                   Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTick, cameraPos, crumbling);

        String phase = WheelBlockEntity.clientPhase;
        float serverAngle = WheelBlockEntity.clientSpinAngle;
        int result = WheelBlockEntity.clientResult;

        long now = System.currentTimeMillis();
        float dt = Math.min((now - lastTickMs) / 1000f, 0.1f);
        if (lastTickMs == 0) dt = 0.016f;
        lastTickMs = now;

        if ("SPINNING".equals(phase)) {
            if (!"SPINNING".equals(currentPhase)) {
                // Reset: start from 0 and animate to serverAngle (absolute target)
                // serverAngle = extraRotations + slicePosition
                // Final displayAngle mod 360 = the slice position
                displayAngle = 0f;
                targetAngle = serverAngle;
                spinVelocity = 500f;
            }
            float remaining = targetAngle - displayAngle;
            if (remaining > 0) {
                // Ease-out: slow down as we approach target
                spinVelocity = Math.max(15f, remaining * 2.0f);
                displayAngle += spinVelocity * dt;
                if (displayAngle >= targetAngle) {
                    displayAngle = targetAngle;
                    spinVelocity = 0f;
                }
            }
        } else if ("RESULT".equals(phase) || "BETTING".equals(phase) || "COOLDOWN".equals(phase)) {
            // Hold at final position - displayAngle stays where it landed
            spinVelocity = 0f;
        }

        currentPhase = phase;

        state.spinAngle = displayAngle;
        state.phase = phase;
        state.resultIndex = result;
        state.timer = WheelBlockEntity.clientTimer;
        state.partialTick = partialTick;

        WheelSegment[] segs = WheelSegment.values();
        int[] colors = new int[segs.length];
        for (int i = 0; i < segs.length; i++) colors[i] = segs[i].color;
        state.segmentColors = colors;
    }

    @Override
    public void submit(WheelRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        // === Draw the spinning wheel texture ===
        poseStack.pushPose();

        // Position: center of block, slightly in front of wall
        poseStack.translate(0.5, 0.0, 0.9);

        // Rotate the wheel
        // Positive rotation = clockwise when viewed from front, matching texture slice order
        poseStack.mulPose(Axis.ZP.rotationDegrees(state.spinAngle));

        // Draw a single textured quad for the wheel face
        collector.submitCustomGeometry(poseStack, RenderTypes.entityCutoutNoCull(WHEEL_TEXTURE), (pose, consumer) -> {
            Matrix4f matrix = pose.pose();
            float r = RADIUS;

            // Quad vertices: bottom-left, bottom-right, top-right, top-left
            // UV maps the full texture (0,0) to (1,1)
            consumer.addVertex(matrix, -r, -r, 0).setColor(255, 255, 255, 255)
                    .setUv(0f, 1f).setOverlay(655360).setLight(15728880).setNormal(pose, 0, 0, 1);
            consumer.addVertex(matrix, r, -r, 0).setColor(255, 255, 255, 255)
                    .setUv(1f, 1f).setOverlay(655360).setLight(15728880).setNormal(pose, 0, 0, 1);
            consumer.addVertex(matrix, r, r, 0).setColor(255, 255, 255, 255)
                    .setUv(1f, 0f).setOverlay(655360).setLight(15728880).setNormal(pose, 0, 0, 1);
            consumer.addVertex(matrix, -r, r, 0).setColor(255, 255, 255, 255)
                    .setUv(0f, 0f).setOverlay(655360).setLight(15728880).setNormal(pose, 0, 0, 1);
        });

        poseStack.popPose();

        // === Draw the pointer (fixed, not rotated) ===
        poseStack.pushPose();
        poseStack.translate(0.5, RADIUS + 0.2, 0.92);

        collector.submitCustomGeometry(poseStack, RenderTypes.entityCutoutNoCull(WHEEL_TEXTURE), (pose, consumer) -> {
            Matrix4f matrix = pose.pose();
            // Red triangle pointing down
            consumer.addVertex(matrix, -0.2f, 0.25f, 0).setColor(220, 20, 20, 255)
                    .setUv(0, 0).setOverlay(655360).setLight(15728880).setNormal(pose, 0, 0, 1);
            consumer.addVertex(matrix, 0.2f, 0.25f, 0).setColor(220, 20, 20, 255)
                    .setUv(1, 0).setOverlay(655360).setLight(15728880).setNormal(pose, 0, 0, 1);
            consumer.addVertex(matrix, 0.0f, -0.15f, 0).setColor(255, 0, 0, 255)
                    .setUv(0.5f, 1).setOverlay(655360).setLight(15728880).setNormal(pose, 0, 0, 1);
            consumer.addVertex(matrix, 0.0f, -0.15f, 0).setColor(255, 0, 0, 255)
                    .setUv(0.5f, 1).setOverlay(655360).setLight(15728880).setNormal(pose, 0, 0, 1);
        });

        poseStack.popPose();
    }
}

package com.ultra.megamod.feature.dimensions.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ultra.megamod.feature.dimensions.PortalBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import org.joml.Matrix4f;

/**
 * Renders the dungeon return portal with an end-portal starfield effect on all 6 faces.
 */
public class PortalBlockRenderer implements BlockEntityRenderer<PortalBlockEntity, BlockEntityRenderState> {

    public PortalBlockRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public BlockEntityRenderState createRenderState() {
        return new BlockEntityRenderState();
    }

    @Override
    public void submit(BlockEntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        collector.submitCustomGeometry(poseStack, RenderTypes.endPortal(), (pose, consumer) -> {
            Matrix4f matrix = pose.pose();
            // South face (z=1)
            renderFace(matrix, consumer, 0f, 1f, 0f, 1f, 1f, 1f, 1f, 1f);
            // North face (z=0)
            renderFace(matrix, consumer, 0f, 1f, 1f, 0f, 0f, 0f, 0f, 0f);
            // East face (x=1)
            renderFace(matrix, consumer, 1f, 1f, 0f, 1f, 1f, 0f, 0f, 1f);
            // West face (x=0)
            renderFace(matrix, consumer, 0f, 0f, 0f, 1f, 0f, 1f, 1f, 0f);
            // Down face (y=0)
            renderFace(matrix, consumer, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 1f);
            // Up face (y=1)
            renderFace(matrix, consumer, 0f, 1f, 1f, 1f, 1f, 1f, 0f, 0f);
        });
    }

    private static void renderFace(Matrix4f matrix, VertexConsumer consumer,
                                   float x0, float x1, float y0, float y1,
                                   float z0, float z1, float z2, float z3) {
        consumer.addVertex(matrix, x0, y0, z0);
        consumer.addVertex(matrix, x1, y0, z1);
        consumer.addVertex(matrix, x1, y1, z2);
        consumer.addVertex(matrix, x0, y1, z3);
    }
}

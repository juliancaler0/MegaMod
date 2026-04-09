package com.ultra.megamod.feature.museum.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.ultra.megamod.feature.museum.MuseumDoorBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

/**
 * Renders the museum door portal plane with an animated end-portal starfield effect.
 * The frame is rendered by the normal block model; this BER only handles the portal surface.
 */
public class MuseumDoorRenderer implements BlockEntityRenderer<MuseumDoorBlockEntity, MuseumDoorRenderState> {

    // Portal plane coordinates (matching the removed JSON model elements)
    private static final float X_MIN = 3f / 16f;
    private static final float X_MAX = 13f / 16f;
    private static final float Z_NORTH = 7.5f / 16f;
    private static final float Z_SOUTH = 8.5f / 16f;

    public MuseumDoorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public MuseumDoorRenderState createRenderState() {
        return new MuseumDoorRenderState();
    }

    @Override
    public void extractRenderState(MuseumDoorBlockEntity blockEntity, MuseumDoorRenderState state, float partialTick,
                                   Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTick, cameraPos, crumbling);
        var blockState = blockEntity.getBlockState();
        state.facing = blockState.getValue(HorizontalDirectionalBlock.FACING);
        state.half = blockState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF);
    }

    @Override
    public void submit(MuseumDoorRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        Direction facing = state.facing;
        DoubleBlockHalf half = state.half;

        // Y range depends on which half we're rendering
        float yMin = half == DoubleBlockHalf.LOWER ? 2f / 16f : 0f;
        float yMax = half == DoubleBlockHalf.LOWER ? 1f : 14f / 16f;

        // Rotate around block center to match facing direction (same rotations as blockstate JSON)
        float rotation = switch (facing) {
            case SOUTH -> 180f;
            case WEST -> 270f;
            case EAST -> 90f;
            default -> 0f; // NORTH
        };

        poseStack.pushPose();
        if (rotation != 0f) {
            poseStack.translate(0.5, 0, 0.5);
            poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
            poseStack.translate(-0.5, 0, -0.5);
        }

        final float capturedYMin = yMin;
        final float capturedYMax = yMax;

        collector.submitCustomGeometry(poseStack, RenderTypes.endPortal(), (pose, consumer) -> {
            Matrix4f matrix = pose.pose();

            // North face (facing -Z)
            consumer.addVertex(matrix, X_MIN, capturedYMax, Z_NORTH);
            consumer.addVertex(matrix, X_MAX, capturedYMax, Z_NORTH);
            consumer.addVertex(matrix, X_MAX, capturedYMin, Z_NORTH);
            consumer.addVertex(matrix, X_MIN, capturedYMin, Z_NORTH);

            // South face (facing +Z)
            consumer.addVertex(matrix, X_MIN, capturedYMin, Z_SOUTH);
            consumer.addVertex(matrix, X_MAX, capturedYMin, Z_SOUTH);
            consumer.addVertex(matrix, X_MAX, capturedYMax, Z_SOUTH);
            consumer.addVertex(matrix, X_MIN, capturedYMax, Z_SOUTH);
        });

        poseStack.popPose();
    }
}

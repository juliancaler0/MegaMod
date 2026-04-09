package com.ultra.megamod.feature.citizen.blueprint.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.citizen.blueprint.BlockInfo;
import com.ultra.megamod.feature.citizen.blueprint.RotationMirror;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Client-side ghost block preview renderer for blueprints.
 * Renders translucent block models at the target world position so the player
 * can see what a blueprint will look like before placement.
 *
 * <p>Registered via {@code @EventBusSubscriber} on the game bus (client dist).
 * Renders during {@link RenderLevelStageEvent.AfterTranslucentBlocks}.
 */
@EventBusSubscriber(modid = MegaMod.MODID, value = Dist.CLIENT)
public class BlueprintRenderer {

    /** Maximum squared distance (in blocks) to render ghost blocks. */
    private static final int RENDER_DISTANCE_SQ = 64 * 64;

    /** The currently previewed blueprint blocks (transformed to world space). */
    @Nullable
    private static List<BlockInfo> currentBlueprint = null;

    /** The world position where the blueprint preview is anchored. */
    @Nullable
    private static BlockPos currentPosition = null;

    /** The rotation/mirror applied to the preview. */
    private static RotationMirror currentRotationMirror = RotationMirror.NONE;

    /**
     * Sets the blueprint preview to render at the specified world position.
     *
     * @param blocks   the blueprint blocks (local-space positions)
     * @param worldPos the world position for the blueprint origin
     * @param rm       the rotation and mirror to apply
     */
    public static void setPreview(List<BlockInfo> blocks, BlockPos worldPos, RotationMirror rm) {
        currentBlueprint = blocks != null ? new ArrayList<>(blocks) : null;
        currentPosition = worldPos;
        currentRotationMirror = rm != null ? rm : RotationMirror.NONE;
    }

    /**
     * Removes the current blueprint preview.
     */
    public static void clearPreview() {
        currentBlueprint = null;
        currentPosition = null;
        currentRotationMirror = RotationMirror.NONE;
    }

    /**
     * Returns true if a blueprint preview is currently active.
     */
    public static boolean hasPreview() {
        return currentBlueprint != null && currentPosition != null;
    }

    /**
     * Returns the current preview anchor position, or null if no preview is active.
     */
    @Nullable
    public static BlockPos getCurrentPosition() {
        return currentPosition;
    }

    /**
     * Returns the current rotation/mirror state.
     */
    public static RotationMirror getCurrentRotationMirror() {
        return currentRotationMirror;
    }

    /**
     * Renders the ghost block preview during the translucent render stage.
     * Each block is rendered as a full-brightness translucent model at its world position.
     */
    @SubscribeEvent
    public static void onRenderWorld(RenderLevelStageEvent.AfterTranslucentBlocks event) {
        if (currentBlueprint == null || currentPosition == null) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        PoseStack poseStack = event.getPoseStack();
        Vec3 cam = mc.player.getEyePosition(mc.getDeltaTracker().getRealtimeDeltaTicks());
        BlockPos playerPos = mc.player.blockPosition();

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        BlockRenderDispatcher blockRenderer = mc.getBlockRenderer();

        for (BlockInfo info : currentBlueprint) {
            if (info.state() == null || info.state().isAir()) continue;

            // Apply rotation/mirror to the local position, then offset to world position
            BlockPos localTransformed = currentRotationMirror.applyToPos(info.pos());
            BlockPos worldPos = currentPosition.offset(localTransformed);

            // Distance culling
            if (playerPos.distSqr(worldPos) > RENDER_DISTANCE_SQ) continue;

            BlockState state = info.state();

            // Rotate/mirror the block state itself (e.g., stairs, logs)
            if (currentRotationMirror.getMirror() != net.minecraft.world.level.block.Mirror.NONE) {
                state = state.mirror(currentRotationMirror.getMirror());
            }
            if (currentRotationMirror.getRotation() != net.minecraft.world.level.block.Rotation.NONE) {
                state = state.rotate(currentRotationMirror.getRotation());
            }

            poseStack.pushPose();
            poseStack.translate(
                    worldPos.getX() - cam.x,
                    worldPos.getY() - cam.y,
                    worldPos.getZ() - cam.z
            );

            try {
                blockRenderer.renderSingleBlock(state, poseStack, bufferSource,
                        0xF000F0, // Full brightness
                        OverlayTexture.NO_OVERLAY);
            } catch (Exception ignored) {
                // Skip blocks that fail to render (e.g., block entities without proper context)
            }

            poseStack.popPose();
        }
    }

    /**
     * Static render entry point for external callers that want to manually invoke rendering.
     * The normal path is via the event subscriber above.
     *
     * @param poseStack the current pose stack
     * @param camera    the render camera
     */
    public static void render(PoseStack poseStack, net.minecraft.client.Camera camera) {
        if (currentBlueprint == null || currentPosition == null) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        Vec3 cam = camera.position();
        BlockPos playerPos = mc.player.blockPosition();

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        BlockRenderDispatcher blockRenderer = mc.getBlockRenderer();

        for (BlockInfo info : currentBlueprint) {
            if (info.state() == null || info.state().isAir()) continue;

            BlockPos localTransformed = currentRotationMirror.applyToPos(info.pos());
            BlockPos worldPos = currentPosition.offset(localTransformed);

            if (playerPos.distSqr(worldPos) > RENDER_DISTANCE_SQ) continue;

            BlockState state = info.state();
            if (currentRotationMirror.getMirror() != net.minecraft.world.level.block.Mirror.NONE) {
                state = state.mirror(currentRotationMirror.getMirror());
            }
            if (currentRotationMirror.getRotation() != net.minecraft.world.level.block.Rotation.NONE) {
                state = state.rotate(currentRotationMirror.getRotation());
            }

            poseStack.pushPose();
            poseStack.translate(
                    worldPos.getX() - cam.x,
                    worldPos.getY() - cam.y,
                    worldPos.getZ() - cam.z
            );

            try {
                blockRenderer.renderSingleBlock(state, poseStack, bufferSource,
                        0xF000F0, OverlayTexture.NO_OVERLAY);
            } catch (Exception ignored) {
            }

            poseStack.popPose();
        }
    }
}

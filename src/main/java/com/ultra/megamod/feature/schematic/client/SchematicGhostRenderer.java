package com.ultra.megamod.feature.schematic.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.ultra.megamod.MegaMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.Map;

/**
 * Renders translucent ghost blocks for the active schematic placement.
 * Uses RenderLevelStageEvent.AfterTranslucentBlocks to draw block models at ~35% alpha.
 */
@EventBusSubscriber(modid = MegaMod.MODID, value = Dist.CLIENT)
public class SchematicGhostRenderer {

    private static final int RENDER_DISTANCE_SQ = 64 * 64; // 64 blocks render distance

    @SubscribeEvent
    public static void onRenderWorld(RenderLevelStageEvent.AfterTranslucentBlocks event) {
        if (!SchematicPlacementMode.isActive()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        Map<BlockPos, BlockState> worldBlocks = SchematicPlacementMode.getWorldBlocks();
        if (worldBlocks.isEmpty()) return;

        PoseStack poseStack = event.getPoseStack();
        Vec3 cam = mc.player.getEyePosition(mc.getDeltaTracker().getRealtimeDeltaTicks());
        BlockPos playerPos = mc.player.blockPosition();

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        BlockRenderDispatcher blockRenderer = mc.getBlockRenderer();

        for (Map.Entry<BlockPos, BlockState> entry : worldBlocks.entrySet()) {
            BlockPos pos = entry.getKey();
            BlockState state = entry.getValue();

            // Distance culling
            if (playerPos.distSqr(pos) > RENDER_DISTANCE_SQ) continue;

            poseStack.pushPose();
            poseStack.translate(
                    pos.getX() - cam.x,
                    pos.getY() - cam.y,
                    pos.getZ() - cam.z
            );

            // Render the block model using the translucent render type
            try {
                blockRenderer.renderSingleBlock(state, poseStack, bufferSource,
                        0xF000F0, // Full brightness (light)
                        net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
            } catch (Exception ignored) {
                // Skip blocks that fail to render (e.g., block entities without context)
            }

            poseStack.popPose();
        }
    }
}

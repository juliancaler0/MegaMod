package com.ultra.megamod.feature.schematic.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.adminmodules.modules.render.ESPRenderHelper;
import com.ultra.megamod.feature.schematic.placement.SchematicPlacement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

/**
 * Renders a wireframe bounding box around the active schematic placement.
 */
@EventBusSubscriber(modid = MegaMod.MODID, value = Dist.CLIENT)
public class SchematicOverlayRenderer {

    @SubscribeEvent
    public static void onRenderWorld(RenderLevelStageEvent.AfterTranslucentBlocks event) {
        if (!SchematicPlacementMode.isActive()) return;

        SchematicPlacement placement = SchematicPlacementMode.getActivePlacement();
        if (placement == null) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        PoseStack poseStack = event.getPoseStack();
        Vec3 cam = mc.player.getEyePosition(mc.getDeltaTracker().getRealtimeDeltaTicks());

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer lines = bufferSource.getBuffer(ESPRenderHelper.LINES_SEE_THROUGH);

        BlockPos origin = placement.getOrigin();
        Vec3i size = placement.getTransformedSize();

        float x1 = (float) (origin.getX() - cam.x);
        float y1 = (float) (origin.getY() - cam.y);
        float z1 = (float) (origin.getZ() - cam.z);
        float x2 = x1 + size.getX();
        float y2 = y1 + size.getY();
        float z2 = z1 + size.getZ();

        poseStack.pushPose();
        Matrix4f matrix = poseStack.last().pose();

        // Cyan wireframe for placement bounds
        ESPRenderHelper.drawWireBox(lines, matrix, x1, y1, z1, x2, y2, z2,
                0.0f, 0.8f, 1.0f, 0.9f);

        // Draw origin marker (small cross at placement origin)
        float ox = (float) (origin.getX() + 0.5 - cam.x);
        float oy = (float) (origin.getY() + 0.5 - cam.y);
        float oz = (float) (origin.getZ() + 0.5 - cam.z);
        ESPRenderHelper.drawCross(lines, matrix, ox, oy, oz, 0.3f,
                1.0f, 1.0f, 0.0f, 1.0f); // Yellow cross at origin

        poseStack.popPose();
        bufferSource.endBatch(ESPRenderHelper.LINES_SEE_THROUGH);
    }
}

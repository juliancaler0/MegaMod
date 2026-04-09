package com.ultra.megamod.feature.map;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.adminmodules.modules.render.ESPRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

import java.util.List;

/**
 * Renders colored beacon beams at waypoint positions in the 3D game world.
 * Beams are thin cross-shaped quads (lines) from Y=0 to Y=320 using
 * the existing ESP see-through render pipeline so they are visible through blocks.
 */
@EventBusSubscriber(modid = MegaMod.MODID, value = Dist.CLIENT)
public class WaypointBeaconRenderer {

    // Waypoint colors matching MapScreen (8 colors) as float RGB
    private static final float[][] BEACON_COLORS = {
            {1.0f, 0.33f, 0.33f},   // 0 = Red
            {0.33f, 0.33f, 1.0f},   // 1 = Blue
            {0.33f, 1.0f, 0.33f},   // 2 = Green
            {1.0f, 1.0f, 0.33f},    // 3 = Yellow
            {0.67f, 0.33f, 1.0f},   // 4 = Purple
            {1.0f, 0.67f, 0.0f},    // 5 = Orange
            {0.33f, 1.0f, 1.0f},    // 6 = Cyan
            {1.0f, 0.33f, 0.67f},   // 7 = Pink
    };

    /** Maximum render distance for beacons (in blocks). */
    private static final double MAX_RENDER_DIST_SQ = 256.0 * 256.0;

    /** Beam extends from Y=0 to Y=320 (full world height). */
    private static final float BEAM_BOTTOM = 0f;
    private static final float BEAM_TOP = 320f;

    @SubscribeEvent
    public static void onRenderWorld(RenderLevelStageEvent.AfterTranslucentBlocks event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        String currentDim = mc.level.dimension().identifier().toString();

        List<MapWaypointSyncManager.BeaconWaypoint> waypoints =
                MapWaypointSyncManager.getActiveBeaconWaypoints(currentDim);
        if (waypoints.isEmpty()) return;

        PoseStack poseStack = event.getPoseStack();
        Vec3 cam = mc.player.getEyePosition(mc.getDeltaTracker().getRealtimeDeltaTicks());

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer lines = bufferSource.getBuffer(ESPRenderHelper.LINES_SEE_THROUGH);

        poseStack.pushPose();
        poseStack.translate(-cam.x, -cam.y, -cam.z);
        Matrix4f matrix = poseStack.last().pose();

        for (MapWaypointSyncManager.BeaconWaypoint wp : waypoints) {
            // Distance check (horizontal only)
            double dx = (wp.x() + 0.5) - cam.x;
            double dz = (wp.z() + 0.5) - cam.z;
            double distSq = dx * dx + dz * dz;
            if (distSq > MAX_RENDER_DIST_SQ) continue;

            float[] color = BEACON_COLORS[wp.colorIndex() % BEACON_COLORS.length];
            float r = color[0];
            float g = color[1];
            float b = color[2];
            float a = 0.8f;

            float cx2 = wp.x() + 0.5f;
            float cz = wp.z() + 0.5f;

            // Render 4 vertical lines forming a cross at the waypoint's X,Z
            // Line 1: north-south axis
            lines.addVertex(matrix, cx2, BEAM_BOTTOM, cz - 0.15f).setColor(r, g, b, a).setNormal(0, 1, 0).setLineWidth(2.0f);
            lines.addVertex(matrix, cx2, BEAM_TOP, cz - 0.15f).setColor(r, g, b, a).setNormal(0, 1, 0).setLineWidth(2.0f);

            lines.addVertex(matrix, cx2, BEAM_BOTTOM, cz + 0.15f).setColor(r, g, b, a).setNormal(0, 1, 0).setLineWidth(2.0f);
            lines.addVertex(matrix, cx2, BEAM_TOP, cz + 0.15f).setColor(r, g, b, a).setNormal(0, 1, 0).setLineWidth(2.0f);

            // Line 2: east-west axis
            lines.addVertex(matrix, cx2 - 0.15f, BEAM_BOTTOM, cz).setColor(r, g, b, a).setNormal(0, 1, 0).setLineWidth(2.0f);
            lines.addVertex(matrix, cx2 - 0.15f, BEAM_TOP, cz).setColor(r, g, b, a).setNormal(0, 1, 0).setLineWidth(2.0f);

            lines.addVertex(matrix, cx2 + 0.15f, BEAM_BOTTOM, cz).setColor(r, g, b, a).setNormal(0, 1, 0).setLineWidth(2.0f);
            lines.addVertex(matrix, cx2 + 0.15f, BEAM_TOP, cz).setColor(r, g, b, a).setNormal(0, 1, 0).setLineWidth(2.0f);
        }

        poseStack.popPose();
    }
}

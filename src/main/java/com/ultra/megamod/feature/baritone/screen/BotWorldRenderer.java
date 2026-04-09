package com.ultra.megamod.feature.baritone.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ultra.megamod.MegaMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

import java.util.List;

/**
 * In-world 3D path rendering using RenderLevelStageEvent.AfterTranslucentBlocks.
 * Draws path lines, goal highlights, and node markers using raw vertex submission.
 */
@EventBusSubscriber(modid = MegaMod.MODID, value = net.neoforged.api.distmarker.Dist.CLIENT)
public class BotWorldRenderer {
    private static boolean renderEnabled = true;

    public static void setRenderEnabled(boolean enabled) {
        renderEnabled = enabled;
    }

    public static boolean isRenderEnabled() {
        return renderEnabled;
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent.AfterTranslucentBlocks event) {
        if (!renderEnabled || !BotPathRenderer.isEnabled()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        PoseStack poseStack = event.getPoseStack();
        try {
            Vec3 camPos = mc.player.getEyePosition(mc.getDeltaTracker().getRealtimeDeltaTicks());
            MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

            poseStack.pushPose();
            poseStack.translate(-camPos.x, -camPos.y, -camPos.z);

            VertexConsumer consumer = bufferSource.getBuffer(RenderTypes.lines());
            Matrix4f matrix = poseStack.last().pose();

            // === 1. Draw main path lines between nodes ===
            List<BotPathRenderer.PathPoint> path = BotPathRenderer.getPath();
            try {
                if (path.size() >= 2) {
                    int segmentCount = path.size() - 1;
                    for (int i = 0; i < segmentCount; i++) {
                        BotPathRenderer.PathPoint a = path.get(i);
                        BotPathRenderer.PathPoint b = path.get(i + 1);

                        double distSq = distSqToCamera(a, camPos);
                        if (distSq > 128 * 128) continue;

                        float alpha = Math.max(0.15f, 0.8f * (1.0f - (float) (Math.sqrt(distSq) / 128.0)));
                        // First 3 segments blue, rest green
                        float r = i < 3 ? 0.34f : 0.25f;
                        float g = i < 3 ? 0.65f : 0.73f;
                        float bCol = i < 3 ? 1.0f : 0.31f;

                        float x1 = (float) (a.x() + 0.5);
                        float y1 = (float) (a.y() + 0.1);
                        float z1 = (float) (a.z() + 0.5);
                        float x2 = (float) (b.x() + 0.5);
                        float y2 = (float) (b.y() + 0.1);
                        float z2 = (float) (b.z() + 0.5);

                        float nx = x2 - x1;
                        float ny = y2 - y1;
                        float nz = z2 - z1;
                        float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
                        if (len < 0.001f) continue;
                        nx /= len; ny /= len; nz /= len;

                        consumer.addVertex(matrix, x1, y1, z1).setColor(r, g, bCol, alpha).setNormal(nx, ny, nz).setLineWidth(1.0f);
                        consumer.addVertex(matrix, x2, y2, z2).setColor(r, g, bCol, alpha).setNormal(nx, ny, nz).setLineWidth(1.0f);
                    }
                }
            } catch (Exception ignored) {}

            // === 2. Draw goal highlight -- wireframe box ===
            try {
                BotPathRenderer.PathPoint dest = BotPathRenderer.getDestination();
                if (dest != null) {
                    double distSq = distSqToCamera(dest, camPos);
                    if (distSq < 128 * 128) {
                        long time = System.currentTimeMillis();
                        float pulse = (float) (0.7 + 0.3 * Math.sin(time * 0.005));
                        float alpha = 0.9f * pulse;
                        drawWireBox(consumer, matrix,
                            (float) dest.x(), (float) dest.y(), (float) dest.z(),
                            (float) dest.x() + 1, (float) dest.y() + 1, (float) dest.z() + 1,
                            1.0f, 0.32f, 0.29f, alpha);
                    }
                }
            } catch (Exception ignored) {}

            // === 3. Draw target block highlights (ore/crop wireframe boxes) ===
            try {
                List<BotPathRenderer.TargetBlock> targets = BotPathRenderer.getTargetBlocks();
                long time = System.currentTimeMillis();
                for (BotPathRenderer.TargetBlock target : targets) {
                    double dx = target.x() + 0.5 - camPos.x;
                    double dy = target.y() + 0.5 - camPos.y;
                    double dz = target.z() + 0.5 - camPos.z;
                    double distSq = dx * dx + dy * dy + dz * dz;
                    if (distSq > 64 * 64) continue;

                    // Pulsing effect unique per block position
                    float pulse = (float) (0.6 + 0.4 * Math.sin(time * 0.003 + target.x() + target.z() * 7));
                    float alpha = 0.7f * pulse;

                    drawWireBox(consumer, matrix,
                        (float) target.x(), (float) target.y(), (float) target.z(),
                        (float) target.x() + 1, (float) target.y() + 1, (float) target.z() + 1,
                        target.r(), target.g(), target.b(), alpha);
                }
            } catch (Exception ignored) {}

            // === 4. Draw calculating/partial path in RED (A* exploration in progress) ===
            try {
                List<BotPathRenderer.PathPoint> calcPath = BotPathRenderer.getCalculatingPath();
                if (BotPathRenderer.isCalculating() && calcPath.size() >= 2) {
                    int segmentCount = calcPath.size() - 1;
                    for (int i = 0; i < segmentCount; i++) {
                        BotPathRenderer.PathPoint a = calcPath.get(i);
                        BotPathRenderer.PathPoint b = calcPath.get(i + 1);

                        double distSq = distSqToCamera(a, camPos);
                        if (distSq > 96 * 96) continue;

                        float alpha = Math.max(0.1f, 0.5f * (1.0f - (float) (Math.sqrt(distSq) / 96.0)));

                        float x1 = (float) (a.x() + 0.5);
                        float y1 = (float) (a.y() + 0.1);
                        float z1 = (float) (a.z() + 0.5);
                        float x2 = (float) (b.x() + 0.5);
                        float y2 = (float) (b.y() + 0.1);
                        float z2 = (float) (b.z() + 0.5);

                        float nx = x2 - x1;
                        float ny = y2 - y1;
                        float nz = z2 - z1;
                        float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
                        if (len < 0.001f) continue;
                        nx /= len; ny /= len; nz /= len;

                        // Red/orange color for calculating path
                        consumer.addVertex(matrix, x1, y1, z1).setColor(1.0f, 0.3f, 0.1f, alpha).setNormal(nx, ny, nz).setLineWidth(1.0f);
                        consumer.addVertex(matrix, x2, y2, z2).setColor(1.0f, 0.3f, 0.1f, alpha).setNormal(nx, ny, nz).setLineWidth(1.0f);
                    }
                }
            } catch (Exception ignored) {}

            // === 5. Draw selection region (quarry/build bounds) as cyan wireframe ===
            try {
                double[] selMin = BotPathRenderer.getSelectionMin();
                double[] selMax = BotPathRenderer.getSelectionMax();
                if (selMin != null && selMax != null) {
                    // Check if selection is within render distance
                    double cx = (selMin[0] + selMax[0]) * 0.5;
                    double cy = (selMin[1] + selMax[1]) * 0.5;
                    double cz = (selMin[2] + selMax[2]) * 0.5;
                    double dx = cx - camPos.x;
                    double dy = cy - camPos.y;
                    double dz = cz - camPos.z;
                    double distSq = dx * dx + dy * dy + dz * dz;
                    if (distSq < 256 * 256) {
                        // Subtle animated pulse
                        long time = System.currentTimeMillis();
                        float pulse = (float) (0.8 + 0.2 * Math.sin(time * 0.002));
                        float alpha = 0.6f * pulse;

                        drawWireBox(consumer, matrix,
                            (float) selMin[0], (float) selMin[1], (float) selMin[2],
                            (float) selMax[0], (float) selMax[1], (float) selMax[2],
                            0.0f, 0.8f, 1.0f, alpha);

                        // Draw cross-hatch lines on the top face for better visibility
                        float topY = (float) selMax[1];
                        float midX = (float) ((selMin[0] + selMax[0]) * 0.5);
                        float midZ = (float) ((selMin[2] + selMax[2]) * 0.5);
                        // X-axis center line on top
                        consumer.addVertex(matrix, (float) selMin[0], topY, midZ).setColor(0.0f, 0.8f, 1.0f, alpha * 0.5f).setNormal(1, 0, 0).setLineWidth(1.0f);
                        consumer.addVertex(matrix, (float) selMax[0], topY, midZ).setColor(0.0f, 0.8f, 1.0f, alpha * 0.5f).setNormal(1, 0, 0).setLineWidth(1.0f);
                        // Z-axis center line on top
                        consumer.addVertex(matrix, midX, topY, (float) selMin[2]).setColor(0.0f, 0.8f, 1.0f, alpha * 0.5f).setNormal(0, 0, 1).setLineWidth(1.0f);
                        consumer.addVertex(matrix, midX, topY, (float) selMax[2]).setColor(0.0f, 0.8f, 1.0f, alpha * 0.5f).setNormal(0, 0, 1).setLineWidth(1.0f);
                    }
                }
            } catch (Exception ignored) {}

            // === 6. Draw waypoint markers as vertical beam lines ===
            try {
                List<BotPathRenderer.PathPoint> waypoints = BotPathRenderer.getWaypointMarkers();
                for (BotPathRenderer.PathPoint wp : waypoints) {
                    double dx = wp.x() + 0.5 - camPos.x;
                    double dy = wp.y() - camPos.y;
                    double dz = wp.z() + 0.5 - camPos.z;
                    double distSq = dx * dx + dy * dy + dz * dz;
                    if (distSq > 128 * 128) continue;

                    float wx = (float) (wp.x() + 0.5);
                    float wz = (float) (wp.z() + 0.5);
                    float wy = (float) wp.y();

                    // Vertical purple beam from y-10 to y+10
                    consumer.addVertex(matrix, wx, wy - 10, wz).setColor(0.7f, 0.3f, 1.0f, 0.8f).setNormal(0, 1, 0).setLineWidth(1.0f);
                    consumer.addVertex(matrix, wx, wy + 10, wz).setColor(0.7f, 0.3f, 1.0f, 0.8f).setNormal(0, 1, 0).setLineWidth(1.0f);

                    // Small wireframe diamond at the waypoint position for visibility
                    float s = 0.3f;
                    // X-axis diamond lines
                    consumer.addVertex(matrix, wx - s, wy, wz).setColor(0.9f, 0.5f, 1.0f, 0.9f).setNormal(1, 1, 0).setLineWidth(1.0f);
                    consumer.addVertex(matrix, wx, wy + s, wz).setColor(0.9f, 0.5f, 1.0f, 0.9f).setNormal(1, 1, 0).setLineWidth(1.0f);
                    consumer.addVertex(matrix, wx, wy + s, wz).setColor(0.9f, 0.5f, 1.0f, 0.9f).setNormal(1, -1, 0).setLineWidth(1.0f);
                    consumer.addVertex(matrix, wx + s, wy, wz).setColor(0.9f, 0.5f, 1.0f, 0.9f).setNormal(1, -1, 0).setLineWidth(1.0f);
                    consumer.addVertex(matrix, wx + s, wy, wz).setColor(0.9f, 0.5f, 1.0f, 0.9f).setNormal(-1, -1, 0).setLineWidth(1.0f);
                    consumer.addVertex(matrix, wx, wy - s, wz).setColor(0.9f, 0.5f, 1.0f, 0.9f).setNormal(-1, -1, 0).setLineWidth(1.0f);
                    consumer.addVertex(matrix, wx, wy - s, wz).setColor(0.9f, 0.5f, 1.0f, 0.9f).setNormal(-1, 1, 0).setLineWidth(1.0f);
                    consumer.addVertex(matrix, wx - s, wy, wz).setColor(0.9f, 0.5f, 1.0f, 0.9f).setNormal(-1, 1, 0).setLineWidth(1.0f);
                    // Z-axis diamond lines
                    consumer.addVertex(matrix, wx, wy, wz - s).setColor(0.9f, 0.5f, 1.0f, 0.9f).setNormal(0, 1, 1).setLineWidth(1.0f);
                    consumer.addVertex(matrix, wx, wy + s, wz).setColor(0.9f, 0.5f, 1.0f, 0.9f).setNormal(0, 1, 1).setLineWidth(1.0f);
                    consumer.addVertex(matrix, wx, wy + s, wz).setColor(0.9f, 0.5f, 1.0f, 0.9f).setNormal(0, -1, 1).setLineWidth(1.0f);
                    consumer.addVertex(matrix, wx, wy, wz + s).setColor(0.9f, 0.5f, 1.0f, 0.9f).setNormal(0, -1, 1).setLineWidth(1.0f);
                    consumer.addVertex(matrix, wx, wy, wz + s).setColor(0.9f, 0.5f, 1.0f, 0.9f).setNormal(0, -1, -1).setLineWidth(1.0f);
                    consumer.addVertex(matrix, wx, wy - s, wz).setColor(0.9f, 0.5f, 1.0f, 0.9f).setNormal(0, -1, -1).setLineWidth(1.0f);
                    consumer.addVertex(matrix, wx, wy - s, wz).setColor(0.9f, 0.5f, 1.0f, 0.9f).setNormal(0, 1, -1).setLineWidth(1.0f);
                    consumer.addVertex(matrix, wx, wy, wz - s).setColor(0.9f, 0.5f, 1.0f, 0.9f).setNormal(0, 1, -1).setLineWidth(1.0f);
                }
            } catch (Exception ignored) {}

            poseStack.popPose();
            bufferSource.endBatch();
        } catch (Exception e) {
            try { poseStack.popPose(); } catch (Exception ignored) {}
        }
    }

    private static void drawWireBox(VertexConsumer consumer, Matrix4f matrix,
                                     float x1, float y1, float z1, float x2, float y2, float z2,
                                     float r, float g, float b, float a) {
        // Bottom edges
        consumer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a).setNormal(1, 0, 0).setLineWidth(1.0f);
        consumer.addVertex(matrix, x2, y1, z1).setColor(r, g, b, a).setNormal(1, 0, 0).setLineWidth(1.0f);
        consumer.addVertex(matrix, x2, y1, z1).setColor(r, g, b, a).setNormal(0, 0, 1).setLineWidth(1.0f);
        consumer.addVertex(matrix, x2, y1, z2).setColor(r, g, b, a).setNormal(0, 0, 1).setLineWidth(1.0f);
        consumer.addVertex(matrix, x2, y1, z2).setColor(r, g, b, a).setNormal(-1, 0, 0).setLineWidth(1.0f);
        consumer.addVertex(matrix, x1, y1, z2).setColor(r, g, b, a).setNormal(-1, 0, 0).setLineWidth(1.0f);
        consumer.addVertex(matrix, x1, y1, z2).setColor(r, g, b, a).setNormal(0, 0, -1).setLineWidth(1.0f);
        consumer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a).setNormal(0, 0, -1).setLineWidth(1.0f);
        // Top edges
        consumer.addVertex(matrix, x1, y2, z1).setColor(r, g, b, a).setNormal(1, 0, 0).setLineWidth(1.0f);
        consumer.addVertex(matrix, x2, y2, z1).setColor(r, g, b, a).setNormal(1, 0, 0).setLineWidth(1.0f);
        consumer.addVertex(matrix, x2, y2, z1).setColor(r, g, b, a).setNormal(0, 0, 1).setLineWidth(1.0f);
        consumer.addVertex(matrix, x2, y2, z2).setColor(r, g, b, a).setNormal(0, 0, 1).setLineWidth(1.0f);
        consumer.addVertex(matrix, x2, y2, z2).setColor(r, g, b, a).setNormal(-1, 0, 0).setLineWidth(1.0f);
        consumer.addVertex(matrix, x1, y2, z2).setColor(r, g, b, a).setNormal(-1, 0, 0).setLineWidth(1.0f);
        consumer.addVertex(matrix, x1, y2, z2).setColor(r, g, b, a).setNormal(0, 0, -1).setLineWidth(1.0f);
        consumer.addVertex(matrix, x1, y2, z1).setColor(r, g, b, a).setNormal(0, 0, -1).setLineWidth(1.0f);
        // Vertical edges
        consumer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a).setNormal(0, 1, 0).setLineWidth(1.0f);
        consumer.addVertex(matrix, x1, y2, z1).setColor(r, g, b, a).setNormal(0, 1, 0).setLineWidth(1.0f);
        consumer.addVertex(matrix, x2, y1, z1).setColor(r, g, b, a).setNormal(0, 1, 0).setLineWidth(1.0f);
        consumer.addVertex(matrix, x2, y2, z1).setColor(r, g, b, a).setNormal(0, 1, 0).setLineWidth(1.0f);
        consumer.addVertex(matrix, x2, y1, z2).setColor(r, g, b, a).setNormal(0, 1, 0).setLineWidth(1.0f);
        consumer.addVertex(matrix, x2, y2, z2).setColor(r, g, b, a).setNormal(0, 1, 0).setLineWidth(1.0f);
        consumer.addVertex(matrix, x1, y1, z2).setColor(r, g, b, a).setNormal(0, 1, 0).setLineWidth(1.0f);
        consumer.addVertex(matrix, x1, y2, z2).setColor(r, g, b, a).setNormal(0, 1, 0).setLineWidth(1.0f);
    }

    private static double distSqToCamera(BotPathRenderer.PathPoint p, Vec3 camera) {
        double dx = p.x() + 0.5 - camera.x;
        double dy = p.y() - camera.y;
        double dz = p.z() + 0.5 - camera.z;
        return dx * dx + dy * dy + dz * dz;
    }
}

package com.ultra.megamod.feature.citizen.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.adminmodules.modules.render.ESPRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Renders claimed territory chunk borders in-world as colored lines.
 * Borders are shown at chunk boundaries (16-block intervals) with vertical pillars at corners.
 * Own faction = gold, ally = green, enemy = red, other = gray.
 */
@EventBusSubscriber(modid = MegaMod.MODID, value = Dist.CLIENT)
public class TerritoryBorderRenderer {

    private static volatile boolean enabled = true;
    private static final Set<ChunkClaim> claimedChunks = new HashSet<>();
    private static final int RENDER_DISTANCE_CHUNKS = 8;
    /** Number of horizontal border lines drawn between bottom and top of the pillar. */
    private static final int HORIZONTAL_LINE_SPACING = 8;

    public record ChunkClaim(int x, int z, String factionId, String relation) {}

    public static void setEnabled(boolean val) {
        enabled = val;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void updateClaims(Set<ChunkClaim> claims) {
        synchronized (claimedChunks) {
            claimedChunks.clear();
            claimedChunks.addAll(claims);
        }
    }

    public static void clearClaims() {
        synchronized (claimedChunks) {
            claimedChunks.clear();
        }
    }

    @SubscribeEvent
    public static void onRenderWorld(RenderLevelStageEvent.AfterTranslucentBlocks event) {
        if (!enabled) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        Set<ChunkClaim> snapshot;
        synchronized (claimedChunks) {
            if (claimedChunks.isEmpty()) return;
            snapshot = new HashSet<>(claimedChunks);
        }

        PoseStack poseStack = event.getPoseStack();
        Vec3 cam = mc.player.getEyePosition(mc.getDeltaTracker().getRealtimeDeltaTicks());
        int playerChunkX = mc.player.blockPosition().getX() >> 4;
        int playerChunkZ = mc.player.blockPosition().getZ() >> 4;

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer lines = bufferSource.getBuffer(ESPRenderHelper.LINES_SEE_THROUGH);

        poseStack.pushPose();
        poseStack.translate(-cam.x, -cam.y, -cam.z);
        Matrix4f matrix = poseStack.last().pose();

        Level world = mc.level;

        // Build a lookup map for O(1) adjacency checks
        Map<Long, String> chunkFactionMap = new HashMap<>(snapshot.size());
        for (ChunkClaim c : snapshot) {
            chunkFactionMap.put(((long) c.x << 32) | (c.z & 0xFFFFFFFFL), c.factionId);
        }

        for (ChunkClaim claim : snapshot) {
            // Only render nearby chunks
            if (Math.abs(claim.x - playerChunkX) > RENDER_DISTANCE_CHUNKS
                || Math.abs(claim.z - playerChunkZ) > RENDER_DISTANCE_CHUNKS) continue;

            float r, g, b;
            switch (claim.relation) {
                case "SELF" -> { r = 1.0f; g = 0.84f; b = 0.0f; } // Gold
                case "ALLY" -> { r = 0.25f; g = 0.73f; b = 0.31f; } // Green
                case "ENEMY" -> { r = 0.73f; g = 0.27f; b = 0.27f; } // Red
                default -> { r = 0.5f; g = 0.5f; b = 0.6f; } // Gray/Neutral
            }
            float a = 0.8f;

            float x1 = claim.x * 16;
            float z1 = claim.z * 16;
            float x2 = x1 + 16;
            float z2 = z1 + 16;

            // Determine terrain-based Y range for this chunk using heightmap
            int minY = world.getMinY();
            int maxTerrainY = minY;
            LevelChunk chunk = world.getChunk(claim.x, claim.z);
            if (chunk != null) {
                // Sample heightmap at 4 corners + center to find the terrain range
                int[] sampleXOffsets = {0, 15, 0, 15, 8};
                int[] sampleZOffsets = {0, 0, 15, 15, 8};
                for (int s = 0; s < sampleXOffsets.length; s++) {
                    int hy = chunk.getHeight(Heightmap.Types.MOTION_BLOCKING, sampleXOffsets[s], sampleZOffsets[s]) + 1;
                    if (hy > maxTerrainY) maxTerrainY = hy;
                }
            }

            // Extend pillars a few blocks above and below the terrain for visibility
            float baseY = Math.max(minY, maxTerrainY - 64);
            float topY = maxTerrainY + 16;

            // Draw 4 vertical pillars at chunk corners
            drawLine(lines, matrix, x1, baseY, z1, x1, topY, z1, r, g, b, a);
            drawLine(lines, matrix, x2, baseY, z1, x2, topY, z1, r, g, b, a);
            drawLine(lines, matrix, x1, baseY, z2, x1, topY, z2, r, g, b, a);
            drawLine(lines, matrix, x2, baseY, z2, x2, topY, z2, r, g, b, a);

            // Determine which edges are faction borders (only draw lines on boundary edges)
            boolean borderN = !claim.factionId.equals(chunkFactionMap.get(((long) claim.x << 32) | ((claim.z - 1) & 0xFFFFFFFFL)));
            boolean borderS = !claim.factionId.equals(chunkFactionMap.get(((long) claim.x << 32) | ((claim.z + 1) & 0xFFFFFFFFL)));
            boolean borderW = !claim.factionId.equals(chunkFactionMap.get(((long) (claim.x - 1) << 32) | (claim.z & 0xFFFFFFFFL)));
            boolean borderE = !claim.factionId.equals(chunkFactionMap.get(((long) (claim.x + 1) << 32) | (claim.z & 0xFFFFFFFFL)));

            // Draw horizontal border lines at evenly spaced heights from terrain base to top
            float totalHeight = topY - baseY;
            float step = Math.max(HORIZONTAL_LINE_SPACING, totalHeight / 10);
            for (float hy = baseY; hy <= topY; hy += step) {
                if (borderN) drawLine(lines, matrix, x1, hy, z1, x2, hy, z1, r, g, b, a);
                if (borderS) drawLine(lines, matrix, x1, hy, z2, x2, hy, z2, r, g, b, a);
                if (borderW) drawLine(lines, matrix, x1, hy, z1, x1, hy, z2, r, g, b, a);
                if (borderE) drawLine(lines, matrix, x2, hy, z1, x2, hy, z2, r, g, b, a);
            }

            // Always draw one line at surface level for clear ground-plane visibility
            float surfaceY = maxTerrainY + 0.5f;
            if (borderN) drawLine(lines, matrix, x1, surfaceY, z1, x2, surfaceY, z1, r, g, b, a * 1.0f);
            if (borderS) drawLine(lines, matrix, x1, surfaceY, z2, x2, surfaceY, z2, r, g, b, a * 1.0f);
            if (borderW) drawLine(lines, matrix, x1, surfaceY, z1, x1, surfaceY, z2, r, g, b, a * 1.0f);
            if (borderE) drawLine(lines, matrix, x2, surfaceY, z1, x2, surfaceY, z2, r, g, b, a * 1.0f);
        }

        poseStack.popPose();
        bufferSource.endBatch(ESPRenderHelper.LINES_SEE_THROUGH);
    }

    private static void drawLine(VertexConsumer c, Matrix4f m, float x1, float y1, float z1, float x2, float y2, float z2, float r, float g, float b, float a) {
        float dx = x2 - x1, dy = y2 - y1, dz = z2 - z1;
        float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 0.001f) return;
        float nx = dx / len, ny = dy / len, nz = dz / len;
        c.addVertex(m, x1, y1, z1).setColor(r, g, b, a).setNormal(nx, ny, nz).setLineWidth(2.0f);
        c.addVertex(m, x2, y2, z2).setColor(r, g, b, a).setNormal(nx, ny, nz).setLineWidth(2.0f);
    }
}

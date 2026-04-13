package com.ultra.megamod.feature.map;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ultra.megamod.MegaMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.List;

/**
 * Renders vanilla-style beacon beams at waypoint positions.
 * Bottom of the beam is clamped to the surface so it doesn't pierce the
 * terrain below; top extends to the world ceiling.
 */
@EventBusSubscriber(modid = MegaMod.MODID, value = Dist.CLIENT)
public class WaypointBeaconRenderer {

    private static final Identifier BEAM_LOCATION = Identifier.withDefaultNamespace("textures/entity/beacon_beam.png");

    /** Waypoint colors as packed 0xRRGGBB ints (matches MapScreen palette). */
    private static final int[] BEACON_COLORS = {
            0xFF5555, 0x5555FF, 0x55FF55, 0xFFFF55,
            0xAA55FF, 0xFFAA00, 0x55FFFF, 0xFF55AA,
    };

    /** Maximum render distance for beacons (horizontal, blocks). */
    private static final double MAX_RENDER_DIST_SQ = 256.0 * 256.0;

    private static final int BEAM_TOP_Y = 320;
    private static final float SOLID_RADIUS = 0.2f;
    private static final float GLOW_RADIUS = 0.25f;
    private static final int FULL_BRIGHT = 0xF000F0;

    @SubscribeEvent
    public static void onRenderWorld(RenderLevelStageEvent.AfterTranslucentBlocks event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        String currentDim = mc.level.dimension().identifier().toString();
        List<MapWaypointSyncManager.BeaconWaypoint> waypoints =
                MapWaypointSyncManager.getActiveBeaconWaypoints(currentDim);
        if (waypoints.isEmpty()) return;

        // Defensive: if any of this throws (bad render type, unloaded chunk, etc.)
        // we want to skip the frame instead of crashing the entire game.
        try {
            renderAll(event, mc, waypoints);
        } catch (Throwable ignored) {}
    }

    private static void renderAll(RenderLevelStageEvent event, Minecraft mc,
                                   List<MapWaypointSyncManager.BeaconWaypoint> waypoints) {
        Vec3 cam = mc.gameRenderer.getMainCamera().position();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        // Use entityTranslucentEmissive so the beam glows full-bright like a beacon
        // and integrates cleanly with the standard buffer-source flush order.
        RenderType beamType = RenderTypes.entityTranslucentEmissive(BEAM_LOCATION);
        VertexConsumer beam = bufferSource.getBuffer(beamType);

        for (MapWaypointSyncManager.BeaconWaypoint wp : waypoints) {
            double dx = (wp.x() + 0.5) - cam.x;
            double dz = (wp.z() + 0.5) - cam.z;
            if (dx * dx + dz * dz > MAX_RENDER_DIST_SQ) continue;

            int surfaceY;
            try {
                surfaceY = mc.level.getHeight(Heightmap.Types.WORLD_SURFACE, wp.x(), wp.z());
            } catch (Exception e) {
                continue; // chunk unloaded / heightmap unavailable
            }
            int height = BEAM_TOP_Y - surfaceY;
            if (height <= 0) continue;

            int rgb = BEACON_COLORS[wp.colorIndex() % BEACON_COLORS.length];

            poseStack.pushPose();
            poseStack.translate(wp.x() + 0.5 - cam.x, surfaceY - cam.y, wp.z() + 0.5 - cam.z);
            PoseStack.Pose pose = poseStack.last();

            emitBeam(pose, beam, ARGB.color(180, rgb), 0, height, SOLID_RADIUS);
            emitBeam(pose, beam, ARGB.color(64, rgb), 0, height, GLOW_RADIUS);

            poseStack.popPose();
        }

        bufferSource.endBatch(beamType);
    }

    /** Emits the four-quad cross that makes up a beacon column. */
    private static void emitBeam(PoseStack.Pose pose, VertexConsumer c, int argb, int yMin, int yMax, float r) {
        emitQuad(pose, c, argb, yMin, yMax, -r, -r, -r,  r);
        emitQuad(pose, c, argb, yMin, yMax, -r,  r,  r,  r);
        emitQuad(pose, c, argb, yMin, yMax,  r,  r,  r, -r);
        emitQuad(pose, c, argb, yMin, yMax,  r, -r, -r, -r);
    }

    private static void emitQuad(PoseStack.Pose pose, VertexConsumer c, int argb,
                                  int yMin, int yMax,
                                  float minX, float minZ, float maxX, float maxZ) {
        addVertex(pose, c, argb, yMax, minX, minZ, 0.0f, 0.0f);
        addVertex(pose, c, argb, yMin, minX, minZ, 0.0f, 1.0f);
        addVertex(pose, c, argb, yMin, maxX, maxZ, 1.0f, 1.0f);
        addVertex(pose, c, argb, yMax, maxX, maxZ, 1.0f, 0.0f);
    }

    private static void addVertex(PoseStack.Pose pose, VertexConsumer c, int argb,
                                   int y, float x, float z, float u, float v) {
        c.addVertex(pose, x, (float) y, z)
            .setColor(argb)
            .setUv(u, v)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(FULL_BRIGHT)
            .setNormal(pose, 0, 1, 0);
    }
}

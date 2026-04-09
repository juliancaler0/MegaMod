package com.ultra.megamod.feature.computer.screen.map;

import com.ultra.megamod.feature.map.MapWaypointSyncManager;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

import java.util.List;
import java.util.Map;

/**
 * Renders a minimap in the top-left corner of the HUD.
 * Uses the existing MapChunkTileManager tile cache — no extra rendering needed.
 */
public class MapMinimapHud {

    private static boolean enabled = true;
    private static final int SIZE = 80;       // minimap square size in screen pixels
    private static final int MARGIN = 4;      // margin from screen edges
    private static final int BORDER = 1;

    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(Identifier.fromNamespaceAndPath("megamod", "minimap"),
            MapMinimapHud::render);
    }

    public static void setEnabled(boolean value) {
        enabled = value;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    private static void render(GuiGraphics g, DeltaTracker dt) {
        if (!enabled) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.screen != null) return;

        MapChunkTileManager mgr = MapChunkTileManager.getInstance();
        if (!mgr.isInitialized()) return;

        int x0 = MARGIN;
        int y0 = MARGIN;

        // Border
        g.fill(x0 - BORDER, y0 - BORDER, x0 + SIZE + BORDER, y0 + SIZE + BORDER, 0xFF333355);
        // Background (unexplored)
        g.fill(x0, y0, x0 + SIZE, y0 + SIZE, 0xFF1A1A2E);

        // Scissor clip to minimap area
        g.enableScissor(x0, y0, x0 + SIZE, y0 + SIZE);

        double playerX = mc.player.getX();
        double playerZ = mc.player.getZ();

        // Zoom: blocks per pixel. At 2.0, the minimap shows 160x160 blocks.
        double zoomScale = 2.0;

        // Render visible tiles
        double halfViewBlocks = SIZE * zoomScale / 2.0;
        int centerXi = (int) Math.floor(playerX);
        int centerZi = (int) Math.floor(playerZ);

        int minTileX = Math.floorDiv((centerXi - (int) halfViewBlocks) >> 4, MapChunkTile.TILE_SIZE);
        int maxTileX = Math.floorDiv((centerXi + (int) halfViewBlocks) >> 4, MapChunkTile.TILE_SIZE);
        int minTileZ = Math.floorDiv((centerZi - (int) halfViewBlocks) >> 4, MapChunkTile.TILE_SIZE);
        int maxTileZ = Math.floorDiv((centerZi + (int) halfViewBlocks) >> 4, MapChunkTile.TILE_SIZE);

        double tileWorldSize = MapChunkTile.TILE_SIZE * 16.0;
        double refScreenX = x0 + SIZE / 2.0 - playerX / zoomScale;
        double refScreenY = y0 + SIZE / 2.0 - playerZ / zoomScale;

        for (int tx = minTileX; tx <= maxTileX; tx++) {
            for (int tz = minTileZ; tz <= maxTileZ; tz++) {
                MapChunkTile tile = mgr.getTileForRendering(tx, tz);
                if (tile == null || tile.getTextureId() == null) continue;

                int screenX = (int) Math.floor(refScreenX + tx * tileWorldSize / zoomScale);
                int screenY = (int) Math.floor(refScreenY + tz * tileWorldSize / zoomScale);
                int nextScreenX = (int) Math.floor(refScreenX + (tx + 1) * tileWorldSize / zoomScale);
                int nextScreenY = (int) Math.floor(refScreenY + (tz + 1) * tileWorldSize / zoomScale);

                int w = nextScreenX - screenX;
                int h = nextScreenY - screenY;
                if (w > 0 && h > 0) {
                    g.blit(RenderPipelines.GUI_TEXTURED, tile.getTextureId(),
                            screenX, screenY, 0f, 0f, w, h, w, h);
                }
            }
        }

        // Waypoint dots (show all waypoints in this dimension, not just beacons)
        String currentDim = mc.level.dimension().identifier().toString();
        List<MapWaypointSyncManager.BeaconWaypoint> beacons =
                MapWaypointSyncManager.getWaypointsForDimension(currentDim);
        int halfMap = SIZE / 2;
        for (MapWaypointSyncManager.BeaconWaypoint wp : beacons) {
            int wpSx = (int) (x0 + halfMap + (wp.x() - playerX) / zoomScale);
            int wpSy = (int) (y0 + halfMap + (wp.z() - playerZ) / zoomScale);
            if (wpSx >= x0 && wpSx < x0 + SIZE && wpSy >= y0 && wpSy < y0 + SIZE) {
                int color = getWaypointColor(wp.colorIndex());
                g.fill(wpSx - 1, wpSy - 1, wpSx + 2, wpSy + 2, color);
            }
        }

        // Player arrow at center
        drawPlayerMarker(g, x0 + halfMap, y0 + halfMap, mc.player.getYRot());

        g.disableScissor();

        // Compass labels outside minimap
        g.drawString(mc.font, "N", x0 + halfMap - mc.font.width("N") / 2, y0 - 9, 0xFFFF4444, false);

        // Coordinate readout below minimap
        int px = (int) mc.player.getX();
        int pz = (int) mc.player.getZ();
        String coordStr = px + ", " + pz;
        int coordW = mc.font.width(coordStr);
        g.drawString(mc.font, coordStr, x0 + halfMap - coordW / 2, y0 + SIZE + 2, 0xFF888899, false);
    }

    private static void drawPlayerMarker(GuiGraphics g, int cx, int cy, float yaw) {
        int color = 0xFF58A6FF;
        // Simple 5-pixel diamond at center
        g.fill(cx, cy - 2, cx + 1, cy - 1, color);
        g.fill(cx - 1, cy - 1, cx + 2, cy, color);
        g.fill(cx - 2, cy, cx + 3, cy + 1, color);
        g.fill(cx - 1, cy + 1, cx + 2, cy + 2, color);
        g.fill(cx, cy + 2, cx + 1, cy + 3, color);
        // White center dot
        g.fill(cx, cy, cx + 1, cy + 1, 0xFFFFFFFF);
    }

    private static final int[] WP_COLORS = {
        0xFFFF4444, 0xFF4488FF, 0xFF44CC44, 0xFFFFCC00,
        0xFFCC44CC, 0xFFFF8844, 0xFF44CCCC, 0xFFFF88BB
    };

    private static int getWaypointColor(int colorIndex) {
        return WP_COLORS[colorIndex % WP_COLORS.length];
    }
}

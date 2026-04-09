package com.ultra.megamod.feature.computer.screen.map;

import net.minecraft.client.gui.GuiGraphics;
import java.util.ArrayList;
import java.util.List;

public class MapHighlightOverlay {

    public record ChunkHighlight(int chunkX, int chunkZ, String type) {}

    private static final List<ChunkHighlight> highlights = new ArrayList<>();
    private static int cachedSpawnX = 0;
    private static int cachedSpawnZ = 0;

    // Colors for highlight types (semi-transparent ARGB)
    private static final int NEW_CHUNK_COLOR = 0x4000FF00;    // green tint
    private static final int PORTAL_COLOR = 0x40AA00FF;       // purple tint

    public static void updateFromServer(List<ChunkHighlight> newHighlights, int serverSpawnX, int serverSpawnZ) {
        highlights.clear();
        highlights.addAll(newHighlights);
        cachedSpawnX = serverSpawnX;
        cachedSpawnZ = serverSpawnZ;
    }

    public static void render(GuiGraphics graphics, int mapLeft, int mapTop, int mapWidth, int mapHeight,
                              double centerWorldX, double centerWorldZ, double zoomScale) {
        // Render chunk highlights
        for (ChunkHighlight h : highlights) {
            int worldX = h.chunkX() * 16;
            int worldZ = h.chunkZ() * 16;

            int sx = (int) (mapLeft + mapWidth / 2.0 + (worldX - centerWorldX) / zoomScale);
            int sy = (int) (mapTop + mapHeight / 2.0 + (worldZ - centerWorldZ) / zoomScale);
            int size = Math.max(1, (int) (16 / zoomScale));

            if (sx + size < mapLeft || sx > mapLeft + mapWidth) continue;
            if (sy + size < mapTop || sy > mapTop + mapHeight) continue;

            int color = switch (h.type()) {
                case "NEW_CHUNK" -> NEW_CHUNK_COLOR;
                case "HAS_PORTAL" -> PORTAL_COLOR;
                default -> 0x30FFFF00;
            };

            graphics.fill(sx, sy, sx + size, sy + size, color);
        }

        // Render spawn chunk overlay using server-provided spawn position
        drawSpawnChunks(graphics, mapLeft, mapTop, mapWidth, mapHeight, centerWorldX, centerWorldZ, zoomScale);
    }

    private static void drawSpawnChunks(GuiGraphics graphics, int mapLeft, int mapTop, int mapWidth, int mapHeight,
                                        double centerWorldX, double centerWorldZ, double zoomScale) {
        // Use the cached spawn position from the server response
        int spawnCX = cachedSpawnX >> 4;
        int spawnCZ = cachedSpawnZ >> 4;

        // Spawn chunks: 19x19 area around world spawn
        for (int dx = -9; dx <= 9; dx++) {
            for (int dz = -9; dz <= 9; dz++) {
                int cx = spawnCX + dx;
                int cz = spawnCZ + dz;
                int worldX = cx * 16;
                int worldZ = cz * 16;

                int sx = (int) (mapLeft + mapWidth / 2.0 + (worldX - centerWorldX) / zoomScale);
                int sy = (int) (mapTop + mapHeight / 2.0 + (worldZ - centerWorldZ) / zoomScale);
                int size = Math.max(1, (int) (16 / zoomScale));

                if (sx + size < mapLeft || sx > mapLeft + mapWidth) continue;
                if (sy + size < mapTop || sy > mapTop + mapHeight) continue;

                boolean inner = Math.abs(dx) <= 4 && Math.abs(dz) <= 4;
                int color = inner ? 0x20FFFF00 : 0x10FFFF00;
                graphics.fill(sx, sy, sx + size, sy + size, color);
            }
        }
    }
}

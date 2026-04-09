package com.ultra.megamod.feature.computer.network.handlers;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;

import java.util.*;

public class MapChunkScanner {

    public enum HighlightType { NEW_CHUNK, HAS_PORTAL }

    public record ChunkHighlight(int chunkX, int chunkZ, HighlightType type) {}

    // In-memory cache per dimension
    private static final Map<String, Map<Long, HighlightType>> dimensionCache = new HashMap<>();

    /**
     * Scan loaded chunks in a radius around the given center for highlights.
     * Only scans already-loaded chunks (no forced chunk loading).
     * Max radius: 32 chunks.
     */
    public static List<ChunkHighlight> scanArea(ServerLevel level, int centerChunkX, int centerChunkZ, int radiusChunks) {
        radiusChunks = Math.min(radiusChunks, 32);
        String dimKey = level.dimension().identifier().toString();
        Map<Long, HighlightType> cache = dimensionCache.computeIfAbsent(dimKey, k -> new HashMap<>());

        List<ChunkHighlight> results = new ArrayList<>();

        for (int cx = centerChunkX - radiusChunks; cx <= centerChunkX + radiusChunks; cx++) {
            for (int cz = centerChunkZ - radiusChunks; cz <= centerChunkZ + radiusChunks; cz++) {
                long key = ChunkPos.asLong(cx, cz);

                // Check cache first
                if (cache.containsKey(key)) {
                    results.add(new ChunkHighlight(cx, cz, cache.get(key)));
                    continue;
                }

                // Only scan loaded chunks
                if (!level.hasChunk(cx, cz)) continue;
                LevelChunk chunk = level.getChunk(cx, cz);

                // New chunk detection: InhabitedTime < 200 ticks
                if (chunk.getInhabitedTime() < 200) {
                    cache.put(key, HighlightType.NEW_CHUNK);
                    results.add(new ChunkHighlight(cx, cz, HighlightType.NEW_CHUNK));
                    continue;
                }

                // Portal detection: scan for portal blocks
                if (hasPortalBlock(chunk, level)) {
                    cache.put(key, HighlightType.HAS_PORTAL);
                    results.add(new ChunkHighlight(cx, cz, HighlightType.HAS_PORTAL));
                }
            }
        }

        return results;
    }

    private static boolean hasPortalBlock(LevelChunk chunk, ServerLevel level) {
        // Quick scan: check a few Y levels for portal blocks
        ChunkPos pos = chunk.getPos();
        int startX = pos.getMinBlockX();
        int startZ = pos.getMinBlockZ();

        // Nether portals are typically at Y 0-128, End portals at specific Y
        for (int y = 0; y < 128; y += 4) { // sample every 4 blocks for speed
            for (int x = startX; x < startX + 16; x += 4) {
                for (int z = startZ; z < startZ + 16; z += 4) {
                    var block = level.getBlockState(new BlockPos(x, y, z)).getBlock();
                    if (block == Blocks.NETHER_PORTAL || block == Blocks.END_PORTAL) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static String buildHighlightsJson(List<ChunkHighlight> highlights, int spawnX, int spawnZ) {
        StringBuilder sb = new StringBuilder("{\"spawnX\":").append(spawnX)
            .append(",\"spawnZ\":").append(spawnZ)
            .append(",\"highlights\":[");
        boolean first = true;
        for (ChunkHighlight h : highlights) {
            if (!first) sb.append(",");
            first = false;
            sb.append("{\"cx\":").append(h.chunkX())
              .append(",\"cz\":").append(h.chunkZ())
              .append(",\"type\":\"").append(h.type().name()).append("\"}");
        }
        sb.append("]}");
        return sb.toString();
    }

    public static void clearCache() {
        dimensionCache.clear();
    }
}

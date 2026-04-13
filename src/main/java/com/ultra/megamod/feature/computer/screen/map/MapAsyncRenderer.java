package com.ultra.megamod.feature.computer.screen.map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.ChunkPos;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

/**
 * Main-thread chunk renderer that processes a few chunks per tick.
 * All chunk data reads (block states, biome colors, heightmaps) happen on
 * the render/main thread, eliminating the thread-safety issues with the
 * previous background-thread approach.
 */
public class MapAsyncRenderer {
    private static MapAsyncRenderer instance;

    private final Deque<ChunkTask> queue = new ArrayDeque<>();
    private final Set<Long> queuedChunks = new HashSet<>();

    /** Max chunks to render per tick (each is 16x16 = 256 block lookups). */
    private static final int MAX_CHUNKS_PER_TICK = 6;
    /** Prevent unbounded queue growth at extreme zoom levels. */
    private static final int MAX_QUEUE_SIZE = 256;

    private record ChunkTask(int tileX, int tileZ, int cxInTile, int czInTile,
                              int chunkX, int chunkZ, boolean caveView) {}

    private MapAsyncRenderer() {}

    public static MapAsyncRenderer getInstance() {
        if (instance == null) instance = new MapAsyncRenderer();
        return instance;
    }

    /**
     * Queue a tile's unrendered chunks for processing.
     * @param skipChunks flattened TILE_SIZE*TILE_SIZE array — true = skip.
     */
    public void submitTile(int tileX, int tileZ, boolean caveView, boolean[] skipChunks) {
        int startCX = MapChunkTile.tileToChunkCoord(tileX);
        int startCZ = MapChunkTile.tileToChunkCoord(tileZ);

        for (int cz = 0; cz < MapChunkTile.TILE_SIZE; cz++) {
            for (int cx = 0; cx < MapChunkTile.TILE_SIZE; cx++) {
                int idx = cz * MapChunkTile.TILE_SIZE + cx;
                if (skipChunks != null && skipChunks[idx]) continue;

                long chunkKey = ChunkPos.asLong(startCX + cx, startCZ + cz);
                if (queuedChunks.contains(chunkKey)) continue;
                if (queue.size() >= MAX_QUEUE_SIZE) return;

                queuedChunks.add(chunkKey);
                queue.add(new ChunkTask(tileX, tileZ, cx, cz, startCX + cx, startCZ + cz, caveView));
            }
        }
    }

    /**
     * Process queued chunks on the main thread and apply pixel data to tiles.
     * Must be called from the render/main thread.
     * @return number of results applied (for upload scheduling).
     */
    public int applyCompletedResults(MapChunkTileManager tileManager) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) return 0;

        Set<MapChunkTile> updatedTiles = new HashSet<>();
        int processed = 0;

        while (processed < MAX_CHUNKS_PER_TICK && !queue.isEmpty()) {
            ChunkTask task = queue.poll();
            queuedChunks.remove(ChunkPos.asLong(task.chunkX, task.chunkZ));

            try {
                if (level.getChunkSource().getChunk(task.chunkX, task.chunkZ, false) == null) continue;

                ChunkPos chunkPos = new ChunkPos(task.chunkX, task.chunkZ);
                MapChunkImage chunkImage = new MapChunkImage(level, chunkPos, task.caveView);

                if (chunkImage.isMeaningful()) {
                    MapChunkTile tile = tileManager.getOrCreateTile(task.tileX, task.tileZ);
                    if (tile != null && tile.getImage() != null) {
                        tile.updateFromChunkImage(chunkImage, task.cxInTile, task.czInTile);
                        tile.markChunkRendered(task.cxInTile, task.czInTile);
                        tile.markNeedsUpdate();
                        updatedTiles.add(tile);
                    }
                }
                chunkImage.close();
                processed++;
            } catch (Exception ignored) {
                // Chunk may have been unloaded during processing
            }
        }

        // Batch GPU texture uploads — one upload per tile, not per chunk
        for (MapChunkTile tile : updatedTiles) {
            try {
                tile.uploadTexture();
            } catch (Exception ignored) {}
        }

        return processed;
    }

    /**
     * Check if any chunks in the given tile are queued.
     */
    public boolean isPending(int tileX, int tileZ) {
        int startCX = MapChunkTile.tileToChunkCoord(tileX);
        int startCZ = MapChunkTile.tileToChunkCoord(tileZ);
        for (int cz = 0; cz < MapChunkTile.TILE_SIZE; cz++) {
            for (int cx = 0; cx < MapChunkTile.TILE_SIZE; cx++) {
                if (queuedChunks.contains(ChunkPos.asLong(startCX + cx, startCZ + cz))) return true;
            }
        }
        return false;
    }

    /**
     * Clear pending render work without destroying the singleton.
     * Call when the tile manager detects a world/dimension switch — otherwise
     * queued tasks from the old world execute against the new ClientLevel and
     * write cross-contaminated pixels into the new world's tile cache.
     */
    public void resetQueue() {
        queue.clear();
        queuedChunks.clear();
    }

    /**
     * Clear all pending work.
     */
    public void close() {
        queue.clear();
        queuedChunks.clear();
        instance = null;
    }
}

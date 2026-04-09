package com.ultra.megamod.feature.baritone.cache;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Manages cached chunks around a bot player for fast block lookups during A* pathfinding.
 * Stores a HashMap of CachedChunk keyed by ChunkPos.toLong().
 * Auto-refreshes stale chunks and evicts chunks outside the configured radius.
 */
public class ChunkCache {
    private final Map<Long, CachedChunk> cache = new HashMap<>();
    private int cacheRadius; // in chunks
    private long staleThresholdMs; // milliseconds before a chunk is considered stale

    /** Default: 4 chunk radius, 10 second stale threshold */
    public ChunkCache() {
        this(4, 10_000L);
    }

    public ChunkCache(int cacheRadius, long staleThresholdMs) {
        this.cacheRadius = cacheRadius;
        this.staleThresholdMs = staleThresholdMs;
    }

    /**
     * Update the cache around a player. Refreshes stale chunks and snapshots new ones.
     * Should be called on the server thread periodically (e.g. every 20 ticks).
     *
     * @param player The bot player to center the cache around
     * @param level  The server level
     */
    public void update(ServerPlayer player, ServerLevel level) {
        int playerCX = player.blockPosition().getX() >> 4;
        int playerCZ = player.blockPosition().getZ() >> 4;
        int minY = level.getMinY();
        int height = level.getHeight();

        // Evict chunks outside radius
        evictDistant(playerCX, playerCZ);

        // Snapshot/refresh chunks within radius
        for (int cx = playerCX - cacheRadius; cx <= playerCX + cacheRadius; cx++) {
            for (int cz = playerCZ - cacheRadius; cz <= playerCZ + cacheRadius; cz++) {
                if (!level.hasChunk(cx, cz)) continue;

                long key = ChunkPos.asLong(cx, cz);
                CachedChunk cached = cache.get(key);

                if (cached == null) {
                    // New chunk, create and snapshot
                    cached = new CachedChunk(cx, cz, minY, height);
                    cached.snapshot(level);
                    cache.put(key, cached);
                } else if (cached.getAge() > staleThresholdMs) {
                    // Stale chunk, re-snapshot
                    cached.snapshot(level);
                }
            }
        }
    }

    /**
     * Get a cached chunk by chunk coordinates.
     *
     * @return The cached chunk, or null if not in cache
     */
    public CachedChunk getChunk(int chunkX, int chunkZ) {
        return cache.get(ChunkPos.asLong(chunkX, chunkZ));
    }

    /**
     * Get the block type at a world position using the cache.
     *
     * @param pos World block position
     * @return Block type constant from CachedChunk, or CachedChunk.SOLID if not cached
     */
    public byte getBlockType(BlockPos pos) {
        return getBlockType(pos.getX(), pos.getY(), pos.getZ());
    }

    /**
     * Get the block type at world coordinates using the cache.
     *
     * @return Block type constant from CachedChunk, or CachedChunk.SOLID if not cached
     */
    public byte getBlockType(int x, int y, int z) {
        int cx = x >> 4;
        int cz = z >> 4;
        CachedChunk chunk = cache.get(ChunkPos.asLong(cx, cz));
        if (chunk == null) return CachedChunk.SOLID; // Unknown = solid (safe default)
        return chunk.getBlockType(x & 15, y, z & 15);
    }

    /**
     * Fast solid check at world coordinates.
     */
    public boolean isSolid(BlockPos pos) {
        return getBlockType(pos) == CachedChunk.SOLID;
    }

    /**
     * Fast solid check at world coordinates.
     */
    public boolean isSolid(int x, int y, int z) {
        return getBlockType(x, y, z) == CachedChunk.SOLID;
    }

    /**
     * Fast air check at world coordinates.
     */
    public boolean isAir(int x, int y, int z) {
        return getBlockType(x, y, z) == CachedChunk.AIR;
    }

    /**
     * Fast water check at world coordinates.
     */
    public boolean isWater(int x, int y, int z) {
        return getBlockType(x, y, z) == CachedChunk.WATER;
    }

    /**
     * Check if a position is passable (air, water, or climbable).
     */
    public boolean isPassable(int x, int y, int z) {
        byte type = getBlockType(x, y, z);
        return type == CachedChunk.AIR || type == CachedChunk.WATER || type == CachedChunk.CLIMBABLE;
    }

    /**
     * Check if a position is dangerous (lava, fire, cactus, etc.).
     */
    public boolean isDangerous(int x, int y, int z) {
        byte type = getBlockType(x, y, z);
        return type == CachedChunk.DANGEROUS || type == CachedChunk.LAVA;
    }

    /**
     * Invalidate a specific block position (e.g. after the bot breaks/places a block).
     * Re-classifies the block from the level on next update, but for now marks as air.
     */
    public void invalidate(BlockPos pos) {
        int cx = pos.getX() >> 4;
        int cz = pos.getZ() >> 4;
        CachedChunk chunk = cache.get(ChunkPos.asLong(cx, cz));
        if (chunk != null) {
            chunk.setBlockType(pos.getX() & 15, pos.getY(), pos.getZ() & 15, CachedChunk.AIR);
        }
    }

    /**
     * Invalidate and immediately reclassify a block from the level.
     */
    public void invalidate(BlockPos pos, ServerLevel level) {
        int cx = pos.getX() >> 4;
        int cz = pos.getZ() >> 4;
        CachedChunk chunk = cache.get(ChunkPos.asLong(cx, cz));
        if (chunk != null) {
            // Force a full re-snapshot of that chunk for accuracy
            chunk.snapshot(level);
        }
    }

    /**
     * Clear the entire cache.
     */
    public void clear() {
        cache.clear();
    }

    /**
     * Get the number of cached chunks.
     */
    public int size() {
        return cache.size();
    }

    /**
     * Check if a world position is within the cached area.
     */
    public boolean isCached(int x, int y, int z) {
        int cx = x >> 4;
        int cz = z >> 4;
        return cache.containsKey(ChunkPos.asLong(cx, cz));
    }

    public void setCacheRadius(int radius) {
        this.cacheRadius = radius;
    }

    public void setStaleThreshold(long ms) {
        this.staleThresholdMs = ms;
    }

    public int getCacheRadius() { return cacheRadius; }

    /**
     * Evict chunks that are beyond the cache radius from the given center.
     */
    private void evictDistant(int centerCX, int centerCZ) {
        Iterator<Map.Entry<Long, CachedChunk>> it = cache.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Long, CachedChunk> entry = it.next();
            CachedChunk chunk = entry.getValue();
            int dx = Math.abs(chunk.getChunkX() - centerCX);
            int dz = Math.abs(chunk.getChunkZ() - centerCZ);
            // Evict if outside radius + 2 buffer to avoid constant re-caching at edges
            if (dx > cacheRadius + 2 || dz > cacheRadius + 2) {
                it.remove();
            }
        }
    }
}

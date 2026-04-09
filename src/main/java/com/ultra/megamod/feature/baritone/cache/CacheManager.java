package com.ultra.megamod.feature.baritone.cache;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton manager for per-player chunk caches.
 * Updated periodically from BotManager.onServerTick.
 * Provides fast block queries for pathfinding without hitting the live world.
 */
public class CacheManager {
    private static final Map<UUID, ChunkCache> playerCaches = new ConcurrentHashMap<>();
    private static int tickCounter = 0;

    private CacheManager() {}

    /**
     * Tick the cache manager. Call every server tick.
     * Only refreshes caches at the configured interval (default every 20 ticks = 1 second).
     *
     * @param refreshRate How often to refresh caches, in ticks
     */
    public static void tick(int refreshRate) {
        tickCounter++;
        if (tickCounter < refreshRate) return;
        tickCounter = 0;

        // Actual update happens per-player when updatePlayer is called
    }

    /**
     * Check if it's time to refresh caches this tick.
     *
     * @param refreshRate Cache refresh rate in ticks
     * @return true if caches should be refreshed this tick
     */
    public static boolean shouldRefresh(int refreshRate) {
        return tickCounter == 0 || refreshRate <= 1;
    }

    /**
     * Update the cache for a specific player. Called on the server thread.
     *
     * @param player The bot's player
     * @param level  The server level
     * @param cacheRadius Chunk cache radius from BotSettings
     */
    public static void updatePlayer(ServerPlayer player, ServerLevel level, int cacheRadius) {
        UUID uuid = player.getUUID();
        ChunkCache cache = playerCaches.computeIfAbsent(uuid, k -> new ChunkCache());
        cache.setCacheRadius(cacheRadius);
        cache.update(player, level);
    }

    /**
     * Get the chunk cache for a player.
     *
     * @return The player's ChunkCache, or null if none exists
     */
    public static ChunkCache getCache(UUID playerUUID) {
        return playerCaches.get(playerUUID);
    }

    /**
     * Get the chunk cache for a player, creating one if it doesn't exist.
     */
    public static ChunkCache getOrCreateCache(UUID playerUUID) {
        return playerCaches.computeIfAbsent(playerUUID, k -> new ChunkCache());
    }

    /**
     * Get the chunk cache for a player, creating one if it doesn't exist,
     * with a specific cache radius.
     */
    public static ChunkCache getOrCreateCache(UUID playerUUID, int cacheRadius) {
        ChunkCache cache = playerCaches.computeIfAbsent(playerUUID, k -> new ChunkCache(cacheRadius, 10_000L));
        cache.setCacheRadius(cacheRadius);
        return cache;
    }

    /**
     * Fast block type query using the cache. Falls back to SOLID if not cached.
     */
    public static byte getBlockType(UUID playerUUID, BlockPos pos) {
        ChunkCache cache = playerCaches.get(playerUUID);
        if (cache == null) return CachedChunk.SOLID;
        return cache.getBlockType(pos);
    }

    /**
     * Fast block type query using the cache.
     */
    public static byte getBlockType(UUID playerUUID, int x, int y, int z) {
        ChunkCache cache = playerCaches.get(playerUUID);
        if (cache == null) return CachedChunk.SOLID;
        return cache.getBlockType(x, y, z);
    }

    /**
     * Invalidate a block in a player's cache (e.g. after mining or placing).
     */
    public static void invalidateBlock(UUID playerUUID, BlockPos pos) {
        ChunkCache cache = playerCaches.get(playerUUID);
        if (cache != null) {
            cache.invalidate(pos);
        }
    }

    /**
     * Invalidate a block and immediately reclassify from the level.
     */
    public static void invalidateBlock(UUID playerUUID, BlockPos pos, ServerLevel level) {
        ChunkCache cache = playerCaches.get(playerUUID);
        if (cache != null) {
            cache.invalidate(pos, level);
        }
    }

    /**
     * Remove a player's cache (on disconnect or bot removal).
     */
    public static void removePlayer(UUID playerUUID) {
        ChunkCache removed = playerCaches.remove(playerUUID);
        if (removed != null) {
            removed.clear();
        }
    }

    /**
     * Clear all player caches (on server shutdown).
     */
    public static void clearAll() {
        for (ChunkCache cache : playerCaches.values()) {
            cache.clear();
        }
        playerCaches.clear();
        tickCounter = 0;
    }

    /**
     * Get the total number of chunks cached across all players.
     */
    public static int getTotalCachedChunks() {
        int total = 0;
        for (ChunkCache cache : playerCaches.values()) {
            total += cache.size();
        }
        return total;
    }

    /**
     * Get the number of players with active caches.
     */
    public static int getPlayerCount() {
        return playerCaches.size();
    }
}

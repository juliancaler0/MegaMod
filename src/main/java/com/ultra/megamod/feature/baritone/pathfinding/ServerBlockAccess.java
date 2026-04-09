package com.ultra.megamod.feature.baritone.pathfinding;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

/**
 * Thread-safe block state snapshot for pathfinding.
 * Adaptive snapshot radius, chunk-loading checks.
 * Captures block states on the server thread, then the A* background thread
 * reads from the snapshot cache, falling back to a safe default.
 */
public class ServerBlockAccess {
    private final ServerLevel level;
    private final Map<Long, BlockState> cache = new HashMap<>();
    private int snapshotRadius = 64;
    private static final int SNAPSHOT_Y_RANGE = 32;

    public ServerBlockAccess(ServerLevel level) {
        this.level = level;
    }

    /**
     * Set the snapshot radius (adaptive based on settings).
     */
    public void setSnapshotRadius(int radius) {
        this.snapshotRadius = Math.max(16, Math.min(radius, 128));
    }

    /**
     * Snapshot block states around a position on the SERVER THREAD.
     * Must be called before A* starts on background thread.
     * Respects chunk loading boundaries.
     */
    public void snapshotAround(BlockPos center) {
        cache.clear();
        int cx = center.getX(), cy = center.getY(), cz = center.getZ();
        int minY = Math.max(cy - SNAPSHOT_Y_RANGE, level.getMinY());
        int maxY = Math.min(cy + SNAPSHOT_Y_RANGE, level.getMaxY());

        for (int x = cx - snapshotRadius; x <= cx + snapshotRadius; x++) {
            for (int z = cz - snapshotRadius; z <= cz + snapshotRadius; z++) {
                // Check chunk is loaded before accessing
                int chunkX = x >> 4;
                int chunkZ = z >> 4;
                if (!level.hasChunk(chunkX, chunkZ)) continue;

                for (int y = minY; y <= maxY; y++) {
                    long key = BlockPos.asLong(x, y, z);
                    cache.put(key, level.getBlockState(new BlockPos(x, y, z)));
                }
            }
        }
    }

    /**
     * Get block state from the snapshot cache. Thread-safe after snapshotAround() completes.
     * Returns null if position is outside the snapshot.
     */
    public BlockState getBlockState(int x, int y, int z) {
        long key = BlockPos.asLong(x, y, z);
        return cache.get(key);
    }

    public BlockState getBlockState(BlockPos pos) {
        return getBlockState(pos.getX(), pos.getY(), pos.getZ());
    }

    public ServerLevel getLevel() { return level; }

    public boolean isInSnapshot(int x, int y, int z) {
        return cache.containsKey(BlockPos.asLong(x, y, z));
    }

    public int cacheSize() { return cache.size(); }
    public int getSnapshotRadius() { return snapshotRadius; }
}

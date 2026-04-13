package com.ultra.megamod.feature.computer.screen.map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;

import com.ultra.megamod.feature.map.SharedMapTileReceiver;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages chunk tile lifecycle, incremental updates, and disk persistence.
 * Ported from recruits ChunkTileManager.java, adapted for MegaMod.
 * Tiles stored in {@code <gameDir>/megamod/worldmap/<worldName>/}.
 */
public class MapChunkTileManager {
    private static MapChunkTileManager instance;
    private final Map<String, MapChunkTile> loadedTiles = new HashMap<>();
    private final Minecraft mc = Minecraft.getInstance();
    private File worldMapDir;
    private String currentDimension = "";
    private String currentWorldId = "";
    private int currentTileX = Integer.MAX_VALUE;
    private int currentTileZ = Integer.MAX_VALUE;
    private final Map<String, Long> lastUpdateTimes = new HashMap<>();
    private long lastNeighborUpdateTime = 0;
    private int lastUpdatedNeighborIndex = 0;
    private boolean caveView = false;
    // Tiles marked dirty by block changes — prioritized for re-render
    private final Set<String> dirtyTiles = ConcurrentHashMap.newKeySet();
    // Per-chunk dirty tracking — so we only re-render changed chunks, not entire tiles
    private final Set<Long> dirtyChunks = ConcurrentHashMap.newKeySet();
    // Track tiles that have been uploaded to the shared server map
    private final Set<String> uploadedToServer = ConcurrentHashMap.newKeySet();

    public static MapChunkTileManager getInstance() {
        if (instance == null) instance = new MapChunkTileManager();
        return instance;
    }

    public void initialize(Level level) {
        if (level == null) return;
        String worldName = detectStorageId();
        String dimId = level.dimension().identifier().getPath();
        String subDir = caveView ? "cave" : "surface";
        this.currentDimension = level.dimension().identifier().toString();
        this.currentWorldId = worldName;
        // worldmap_v2: bumped from v1 to invalidate tiles saved with the legacy
        // ARGB→ABGR byte-swap (R/B inverted, made desert/sand render bluish).
        this.worldMapDir = new File(mc.gameDirectory, "megamod/worldmap_v2/" + worldName + "/" + dimId + "/" + subDir);
        this.worldMapDir.mkdirs();
    }

    /**
     * Set cave view mode. When changed, invalidates all loaded tiles.
     */
    public void setCaveView(boolean cave) {
        if (this.caveView != cave) {
            this.caveView = cave;
            // Invalidate everything — close old tiles, reinitialize with new subdirectory
            closeAllTiles();
            if (mc.level != null) {
                initialize(mc.level);
            }
        }
    }

    public boolean isCaveView() {
        return this.caveView;
    }

    public void toggleCaveView() {
        setCaveView(!this.caveView);
    }

    public void updateCurrentTile() {
        if (mc.level == null || mc.player == null) return;

        // Re-check world/dimension identity every tick — the map screen can
        // stay open across a dimension switch, and waiting for the 2-second
        // background tick lets stale render tasks write to the wrong world.
        initializeIfNeeded(mc.level);

        // Evict old tiles here (during tick), NOT during getOrCreateTile().
        // Evicting during the render loop causes "Sampler0 has been closed" crashes
        // because GuiRenderer batch-executes blits after the textures are freed.
        if (loadedTiles.size() >= MAX_LOADED_TILES) {
            evictOldTiles();
        }

        // Process queued chunk renders on main thread (renders + uploads in one step)
        MapAsyncRenderer renderer = MapAsyncRenderer.getInstance();
        int applied = renderer.applyCompletedResults(this);
        // Save tiles that got new data to disk and upload to shared server map
        if (applied > 0) {
            saveDirtyTilesToDisk();
        }

        int chunkX = mc.player.chunkPosition().x;
        int chunkZ = mc.player.chunkPosition().z;
        int tileX = MapChunkTile.chunkToTileCoord(chunkX);
        int tileZ = MapChunkTile.chunkToTileCoord(chunkZ);
        String currentTileKey = tileX + "_" + tileZ;

        long currentTime = System.currentTimeMillis();
        Long lastUpdate = lastUpdateTimes.get(currentTileKey);

        // Process dirty tiles first (block changes)
        if (!dirtyTiles.isEmpty()) {
            for (String dirtyKey : dirtyTiles) {
                String[] parts = dirtyKey.split("_");
                if (parts.length == 2) {
                    try {
                        int dtx = Integer.parseInt(parts[0]);
                        int dtz = Integer.parseInt(parts[1]);
                        submitTileForRendering(dtx, dtz);
                    } catch (NumberFormatException ignored) {}
                }
            }
            dirtyTiles.clear();
        }

        // Submit current tile for rendering.
        // Also mark the player's current chunk dirty so it re-renders even without
        // a block-change event — needed because BlockEvent.BreakEvent only fires
        // server-side, so multiplayer clients miss block changes otherwise.
        if (tileX != currentTileX || tileZ != currentTileZ ||
                lastUpdate == null || currentTime - lastUpdate > 1000) {
            dirtyChunks.add(ChunkPos.asLong(chunkX, chunkZ));
            submitTileForRendering(tileX, tileZ);
            currentTileX = tileX;
            currentTileZ = tileZ;
        }

        // Submit one neighbor tile per cycle
        if (currentTime - lastNeighborUpdateTime >= 500) {
            updateOneNeighborTile(tileX, tileZ);
            lastNeighborUpdateTime = currentTime;
        }
    }

    private void updateOneNeighborTile(int centerX, int centerZ) {
        int[][] neighbors = {
                {centerX - 1, centerZ - 1}, {centerX, centerZ - 1}, {centerX + 1, centerZ - 1},
                {centerX - 1, centerZ},                              {centerX + 1, centerZ},
                {centerX - 1, centerZ + 1}, {centerX, centerZ + 1}, {centerX + 1, centerZ + 1}
        };

        if (lastUpdatedNeighborIndex >= neighbors.length) lastUpdatedNeighborIndex = 0;
        int[] neighbor = neighbors[lastUpdatedNeighborIndex];

        String neighborKey = neighbor[0] + "_" + neighbor[1];
        Long neighborLastUpdate = lastUpdateTimes.get(neighborKey);
        if (neighborLastUpdate == null || System.currentTimeMillis() - neighborLastUpdate > 10000) {
            submitTileForRendering(neighbor[0], neighbor[1]);
        }
        lastUpdatedNeighborIndex++;
    }

    // Track which tiles have already been merged from disk to avoid redundant I/O
    private final Set<String> mergedFromDisk = ConcurrentHashMap.newKeySet();

    /**
     * Submit a tile for rendering.
     * Computes a skip array so already-rendered chunks are not re-rendered
     * (unless they are dirty from block changes).
     */
    private void submitTileForRendering(int tileX, int tileZ) {
        String key = tileX + "_" + tileZ;
        // Ensure the tile exists and has disk data merged (only once per load)
        MapChunkTile tile = getOrCreateTile(tileX, tileZ);
        if (!mergedFromDisk.contains(key)) {
            File tileFile = getTileFile(tileX, tileZ);
            if (tileFile.exists()) tile.mergeWithExistingTile(tileFile);
            mergedFromDisk.add(key);
        }

        // Build skip array: skip chunks that are already rendered and not dirty
        int startCX = MapChunkTile.tileToChunkCoord(tileX);
        int startCZ = MapChunkTile.tileToChunkCoord(tileZ);
        boolean[] skip = new boolean[MapChunkTile.TILE_SIZE * MapChunkTile.TILE_SIZE];
        boolean anyWork = false;

        for (int cz = 0; cz < MapChunkTile.TILE_SIZE; cz++) {
            for (int cx = 0; cx < MapChunkTile.TILE_SIZE; cx++) {
                int idx = cz * MapChunkTile.TILE_SIZE + cx;
                long chunkKey = ChunkPos.asLong(startCX + cx, startCZ + cz);
                boolean isDirty = dirtyChunks.remove(chunkKey);
                boolean isRendered = tile.isChunkRendered(cx, cz);

                if (isRendered && !isDirty) {
                    skip[idx] = true;
                } else if (isChunkLoaded(new ChunkPos(startCX + cx, startCZ + cz))) {
                    anyWork = true; // Unrendered or dirty chunk that's loaded — needs work
                } else {
                    skip[idx] = true; // Not loaded, can't render
                }
            }
        }

        if (!anyWork) return; // All chunks are either rendered or unloaded — nothing to do

        MapAsyncRenderer.getInstance().submitTile(tileX, tileZ, caveView, skip);
        lastUpdateTimes.put(key, System.currentTimeMillis());
    }

    /**
     * Save tiles that received new pixel data to disk and upload to shared server.
     * Throttled to max 4 per tick to prevent I/O spikes.
     */
    private static final int MAX_SAVES_PER_TICK = 4;

    private void saveDirtyTilesToDisk() {
        int saved = 0;
        for (MapChunkTile tile : loadedTiles.values()) {
            if (saved >= MAX_SAVES_PER_TICK) break;
            File tileFile = getTileFile(tile.getTileX(), tile.getTileZ());
            try {
                tile.saveToFile(tileFile);
                // Upload to shared server map
                String key = tile.getTileX() + "_" + tile.getTileZ();
                if (!uploadedToServer.contains(key)) {
                    queueTileUpload(tile);
                }
                saved++;
            } catch (Exception ignored) {}
        }
    }

    /**
     * Whether the manager has been initialized with a world directory.
     */
    public boolean isInitialized() {
        return worldMapDir != null;
    }

    /**
     * Initialize if not already done. Safe to call repeatedly.
     */
    public void initializeIfNeeded(Level level) {
        if (level == null) return;
        String dimId = level.dimension().identifier().toString();
        String worldId = detectStorageId();
        if (worldMapDir == null) {
            initialize(level);
        } else if (!dimId.equals(currentDimension) || !worldId.equals(currentWorldId)) {
            // Dimension or world changed — save, close, and reinitialize
            closeAllTiles();
            initialize(level);
        }
    }

    /**
     * Background tick — called from MapBackgroundRenderer even when the map screen
     * is closed. Pre-renders chunks near the player so the map opens instantly.
     */
    public void backgroundTick() {
        if (mc.level == null || mc.player == null) return;

        // Process queued renders (even without map open)
        MapAsyncRenderer renderer = MapAsyncRenderer.getInstance();
        int applied = renderer.applyCompletedResults(this);
        if (applied > 0) {
            saveDirtyTilesToDisk();
        }

        // Evict if needed
        if (loadedTiles.size() >= MAX_LOADED_TILES) {
            evictOldTiles();
        }

        // Find one unrendered loaded chunk near the player and submit its tile
        int playerCX = mc.player.chunkPosition().x;
        int playerCZ = mc.player.chunkPosition().z;
        int radius = 8;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int cx = playerCX + dx;
                int cz = playerCZ + dz;
                int tileX = MapChunkTile.chunkToTileCoord(cx);
                int tileZ = MapChunkTile.chunkToTileCoord(cz);
                int localX = cx - MapChunkTile.tileToChunkCoord(tileX);
                int localZ = cz - MapChunkTile.tileToChunkCoord(tileZ);

                // Check if already rendered in a loaded tile
                MapChunkTile tile = loadedTiles.get(tileX + "_" + tileZ);
                if (tile != null && tile.isChunkRendered(localX, localZ)) continue;

                // Check if chunk is loaded in the world
                if (!isChunkLoaded(new ChunkPos(cx, cz))) continue;

                // Submit this tile — submitTileForRendering handles skip logic internally
                submitTileForRendering(tileX, tileZ);
                return; // One tile per background tick to stay lightweight
            }
        }
    }

    /**
     * Mark a tile as dirty (needs re-render due to block change).
     * Called from MapBlockChangeListener.
     */
    public void markTileDirty(int worldX, int worldZ) {
        int chunkX = worldX >> 4;
        int chunkZ = worldZ >> 4;
        int tileX = MapChunkTile.chunkToTileCoord(chunkX);
        int tileZ = MapChunkTile.chunkToTileCoord(chunkZ);
        String tileKey = tileX + "_" + tileZ;
        dirtyTiles.add(tileKey);
        dirtyChunks.add(ChunkPos.asLong(chunkX, chunkZ));
        uploadedToServer.remove(tileKey);
    }

    private boolean isChunkLoaded(ChunkPos chunkPos) {
        if (mc.level == null || mc.player == null) return false;
        try {
            return mc.level.getChunkSource().getChunk(chunkPos.x, chunkPos.z, false) != null;
        } catch (Exception e) {
            return false;
        }
    }

    private static final int MAX_LOADED_TILES = 200;

    /**
     * Get a tile for rendering. Returns null if not loaded — never evicts or creates,
     * so it is safe to call during the render loop without invalidating other tiles' textures.
     */
    public MapChunkTile getTileForRendering(int tileX, int tileZ) {
        String key = tileX + "_" + tileZ;
        MapChunkTile tile = loadedTiles.get(key);
        if (tile != null) tile.markAccessed();
        return tile;
    }

    public MapChunkTile getOrCreateTile(int tileX, int tileZ) {
        String key = tileX + "_" + tileZ;
        MapChunkTile tile = loadedTiles.get(key);
        if (tile == null) {
            tile = new MapChunkTile(tileX, tileZ);
            tile.loadOrCreate(getTileFile(tileX, tileZ));
            loadedTiles.put(key, tile);
            // If tile loaded empty (no local data), request from shared server map
            if (tile.getChunkBitmask() == 0) {
                SharedMapTileReceiver.requestTileFromServer(tileX, tileZ, caveView);
            }
        }
        tile.markAccessed();
        return tile;
    }

    private void queueTileUpload(MapChunkTile tile) {
        if (mc.getSingleplayerServer() != null) return; // Skip singleplayer
        long bitmask = tile.getChunkBitmask();
        if (bitmask == 0) return;
        byte[] pngBytes = tile.toPngBytes();
        if (pngBytes.length == 0 || pngBytes.length > 32768) return;
        String key = tile.getTileX() + "_" + tile.getTileZ();
        SharedMapTileReceiver.uploadTileToServer(
                tile.getTileX(), tile.getTileZ(), caveView, pngBytes, bitmask);
        uploadedToServer.add(key);
    }

    /**
     * Evict roughly half the loaded tiles (those furthest from the player) to keep memory bounded.
     */
    private void evictOldTiles() {
        if (mc.player == null) return;
        int playerTileX = MapChunkTile.chunkToTileCoord(mc.player.chunkPosition().x);
        int playerTileZ = MapChunkTile.chunkToTileCoord(mc.player.chunkPosition().z);

        // Sort tiles by distance from player, evict the farthest half
        java.util.List<Map.Entry<String, MapChunkTile>> entries = new java.util.ArrayList<>(loadedTiles.entrySet());
        entries.sort((a, b) -> {
            int distA = Math.abs(a.getValue().getTileX() - playerTileX) + Math.abs(a.getValue().getTileZ() - playerTileZ);
            int distB = Math.abs(b.getValue().getTileX() - playerTileX) + Math.abs(b.getValue().getTileZ() - playerTileZ);
            return Integer.compare(distB, distA); // farthest first
        });

        int toRemove = entries.size() / 2;
        for (int i = 0; i < toRemove; i++) {
            Map.Entry<String, MapChunkTile> entry = entries.get(i);
            MapChunkTile evicted = entry.getValue();
            evicted.saveToFile(getTileFile(evicted.getTileX(), evicted.getTileZ()));
            evicted.close();
            loadedTiles.remove(entry.getKey());
            lastUpdateTimes.remove(entry.getKey());
            mergedFromDisk.remove(entry.getKey());
        }
    }

    private static String detectStorageId() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.getSingleplayerServer() != null) {
                // Use the world's folder name, not its display name.
                // Display names can collide ("New World" x2) but folders are unique
                // ("New World", "New World (1)", etc.)
                java.nio.file.Path worldRoot = mc.getSingleplayerServer()
                        .getWorldPath(LevelResource.ROOT)
                        .toAbsolutePath().normalize();
                String folderName = worldRoot.getFileName().toString();
                if (folderName != null && !folderName.isEmpty())
                    return folderName.replaceAll("[^a-zA-Z0-9_\\-\\.]", "_");
            }
            ServerData sd = mc.getCurrentServer();
            if (sd != null && sd.ip != null && !sd.ip.isEmpty())
                return sd.ip.replaceAll("[^a-zA-Z0-9_\\-\\.]", "_");
        } catch (Exception ignored) {}
        return "unknown";
    }

    private File getTileFile(int tileX, int tileZ) {
        return new File(worldMapDir, tileX + "_" + tileZ + ".png");
    }

    /**
     * Close all loaded tiles, saving any pending changes.
     */
    private void closeAllTiles() {
        for (MapChunkTile tile : loadedTiles.values()) {
            tile.saveToFile(getTileFile(tile.getTileX(), tile.getTileZ()));
            tile.close();
        }
        loadedTiles.clear();
        lastUpdateTimes.clear();
        mergedFromDisk.clear();
        dirtyTiles.clear();
        dirtyChunks.clear();
        uploadedToServer.clear();
        // Drop any render tasks queued for the previous world/dimension —
        // otherwise they execute against the new ClientLevel and write
        // cross-contaminated pixels into this world's tile cache.
        MapAsyncRenderer.getInstance().resetQueue();
        SharedMapTileReceiver.reset();
        currentTileX = Integer.MAX_VALUE;
        currentTileZ = Integer.MAX_VALUE;
    }

    /**
     * Save all loaded tiles to disk without destroying them.
     * Call when the map screen is closed — tiles stay in memory for background rendering.
     */
    public void saveAll() {
        for (MapChunkTile tile : loadedTiles.values()) {
            tile.saveToFile(getTileFile(tile.getTileX(), tile.getTileZ()));
        }
    }

    /**
     * Close the manager and save all tiles. Call on world disconnect only.
     */
    public void close() {
        MapAsyncRenderer.getInstance().close();
        closeAllTiles();
        worldMapDir = null;
        instance = null;
    }

    public String getCurrentDimension() {
        return currentDimension;
    }

    public String getCurrentWorldId() {
        return currentWorldId;
    }

    public Map<String, MapChunkTile> getLoadedTiles() {
        return loadedTiles;
    }
}

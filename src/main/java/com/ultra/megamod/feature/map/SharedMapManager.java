package com.ultra.megamod.feature.map;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.map.network.MapTileDataPayload;
import com.ultra.megamod.feature.map.network.MapTileRequestPayload;
import com.ultra.megamod.feature.map.network.MapTileUploadPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Server-side shared map tile manager.
 * Stores master tile PNGs in world/data/megamod_map/<surface|cave>/.
 * Merges uploads from multiple players at chunk granularity.
 */
public class SharedMapManager {
    private static SharedMapManager instance;
    private Path mapBaseDir;
    private boolean initialized = false;

    private static final int MAX_RESPONSES_PER_TICK = 4;
    private static final int TILE_PIXEL_SIZE = 128;
    private static final int PIXELS_PER_CHUNK = 16;
    private static final int TILE_SIZE = 8;
    private static final int MEANINGFUL_THRESHOLD = 25;

    // Rate limiting: last upload time per player per tile key
    private final Map<UUID, Map<String, Long>> lastUploadTimes = new ConcurrentHashMap<>();
    // Pending tile responses per player
    private final Map<UUID, Queue<MapTileDataPayload>> pendingResponses = new ConcurrentHashMap<>();
    // Per-tile lock for merge serialization
    private final ConcurrentHashMap<String, ReentrantLock> tileLocks = new ConcurrentHashMap<>();
    // Async executor for merge operations
    private final ExecutorService mergeExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "megamod-map-merge");
        t.setDaemon(true);
        return t;
    });

    public static SharedMapManager getInstance() {
        if (instance == null) instance = new SharedMapManager();
        return instance;
    }

    public void initializeIfNeeded(ServerLevel level) {
        if (initialized) return;
        Path worldDir = level.getServer().getWorldPath(LevelResource.ROOT);
        this.mapBaseDir = worldDir.resolve("data").resolve("megamod_map");
        try {
            Files.createDirectories(mapBaseDir.resolve("surface"));
            Files.createDirectories(mapBaseDir.resolve("cave"));
        } catch (IOException e) {
            MegaMod.LOGGER.warn("Failed to create shared map directories", e);
        }
        this.initialized = true;
    }

    public boolean isInitialized() {
        return initialized;
    }

    // ======================== UPLOAD ========================

    public void receiveTileUpload(ServerPlayer player, MapTileUploadPayload payload) {
        if (!initialized) return;

        // Rate limit: 1 upload per 2 seconds per tile per player per dimension
        String dimKey = payload.dimension() + "/" + payload.tileX() + "_" + payload.tileZ();
        Map<String, Long> playerTimes = lastUploadTimes.computeIfAbsent(
                player.getUUID(), k -> new HashMap<>());
        long now = System.currentTimeMillis();
        Long lastTime = playerTimes.get(dimKey);
        if (lastTime != null && now - lastTime < 2000) return;
        playerTimes.put(dimKey, now);

        // Validate PNG data
        byte[] incoming = payload.pngData();
        if (incoming == null || incoming.length == 0 || incoming.length > 32768) return;
        if (incoming.length < 8 || incoming[0] != (byte) 0x89 || incoming[1] != 'P') return;

        String dimId = sanitizeDimension(payload.dimension());
        String subDir = payload.cave() ? "cave" : "surface";
        String key = payload.tileX() + "_" + payload.tileZ();
        Path dimDir = mapBaseDir.resolve(dimId).resolve(subDir);
        try { Files.createDirectories(dimDir); } catch (IOException ignored) {}
        Path tileFile = dimDir.resolve(key + ".png");
        long bitmask = payload.chunkBitmask();

        mergeExecutor.submit(() -> mergeTile(tileFile, key, incoming, bitmask));
    }

    private void mergeTile(Path tileFile, String key, byte[] incomingPng, long incomingChunkMask) {
        ReentrantLock lock = tileLocks.computeIfAbsent(key, k -> new ReentrantLock());
        lock.lock();
        try {
            BufferedImage incoming = ImageIO.read(new ByteArrayInputStream(incomingPng));
            if (incoming == null || incoming.getWidth() != TILE_PIXEL_SIZE
                    || incoming.getHeight() != TILE_PIXEL_SIZE) return;

            if (Files.exists(tileFile)) {
                BufferedImage existing = ImageIO.read(tileFile.toFile());
                if (existing != null && existing.getWidth() == TILE_PIXEL_SIZE
                        && existing.getHeight() == TILE_PIXEL_SIZE) {
                    // Chunk-level merge
                    for (int cz = 0; cz < TILE_SIZE; cz++) {
                        for (int cx = 0; cx < TILE_SIZE; cx++) {
                            int bit = cz * TILE_SIZE + cx;
                            boolean incomingHasChunk = (incomingChunkMask & (1L << bit)) != 0;
                            if (!incomingHasChunk) continue;

                            boolean existingHasChunk = hasChunkData(existing, cx, cz);
                            if (!existingHasChunk) {
                                copyChunkRegion(incoming, existing, cx, cz);
                            } else {
                                mergeChunkPixels(incoming, existing, cx, cz);
                            }
                        }
                    }
                    ImageIO.write(existing, "PNG", tileFile.toFile());
                } else {
                    ImageIO.write(incoming, "PNG", tileFile.toFile());
                }
            } else {
                tileFile.getParent().toFile().mkdirs();
                ImageIO.write(incoming, "PNG", tileFile.toFile());
            }
        } catch (Exception e) {
            MegaMod.LOGGER.warn("Failed to merge map tile {}", key, e);
        } finally {
            lock.unlock();
        }
    }

    private boolean hasChunkData(BufferedImage img, int cx, int cz) {
        int startX = cx * PIXELS_PER_CHUNK;
        int startZ = cz * PIXELS_PER_CHUNK;
        int meaningful = 0;
        for (int x = 0; x < PIXELS_PER_CHUNK && meaningful < MEANINGFUL_THRESHOLD; x++) {
            for (int z = 0; z < PIXELS_PER_CHUNK && meaningful < MEANINGFUL_THRESHOLD; z++) {
                int argb = img.getRGB(startX + x, startZ + z);
                int alpha = (argb >> 24) & 0xFF;
                if (alpha > 0 && (argb & 0x00FFFFFF) != 0) meaningful++;
            }
        }
        return meaningful >= MEANINGFUL_THRESHOLD;
    }

    private void copyChunkRegion(BufferedImage src, BufferedImage dst, int cx, int cz) {
        int startX = cx * PIXELS_PER_CHUNK;
        int startZ = cz * PIXELS_PER_CHUNK;
        for (int x = 0; x < PIXELS_PER_CHUNK; x++) {
            for (int z = 0; z < PIXELS_PER_CHUNK; z++) {
                dst.setRGB(startX + x, startZ + z, src.getRGB(startX + x, startZ + z));
            }
        }
    }

    private void mergeChunkPixels(BufferedImage src, BufferedImage dst, int cx, int cz) {
        int startX = cx * PIXELS_PER_CHUNK;
        int startZ = cz * PIXELS_PER_CHUNK;
        for (int x = 0; x < PIXELS_PER_CHUNK; x++) {
            for (int z = 0; z < PIXELS_PER_CHUNK; z++) {
                int existingPixel = dst.getRGB(startX + x, startZ + z);
                int existingAlpha = (existingPixel >> 24) & 0xFF;
                if (existingAlpha == 0) {
                    dst.setRGB(startX + x, startZ + z, src.getRGB(startX + x, startZ + z));
                }
            }
        }
    }

    // ======================== REQUEST ========================

    public void handleTileRequest(ServerPlayer player, MapTileRequestPayload payload) {
        if (!initialized) return;
        String dimId = sanitizeDimension(payload.dimension());
        String subDir = payload.cave() ? "cave" : "surface";
        int[] coords = payload.tileCoords();
        String dimension = payload.dimension();

        Queue<MapTileDataPayload> queue = pendingResponses.computeIfAbsent(
                player.getUUID(), k -> new ConcurrentLinkedQueue<>());
        if (queue.size() > 64) return;

        for (int i = 0; i + 1 < coords.length; i += 2) {
            int tileX = coords[i];
            int tileZ = coords[i + 1];
            String key = tileX + "_" + tileZ;
            Path tileFile = mapBaseDir.resolve(dimId).resolve(subDir).resolve(key + ".png");

            mergeExecutor.submit(() -> {
                try {
                    if (Files.exists(tileFile)) {
                        byte[] data = Files.readAllBytes(tileFile);
                        if (data.length > 0 && data.length <= 32768) {
                            long bitmask = computeChunkBitmask(data);
                            queue.add(new MapTileDataPayload(tileX, tileZ, payload.cave(), dimension, bitmask, data));
                        } else {
                            queue.add(new MapTileDataPayload(tileX, tileZ, payload.cave(), dimension, 0L, new byte[0]));
                        }
                    } else {
                        queue.add(new MapTileDataPayload(tileX, tileZ, payload.cave(), dimension, 0L, new byte[0]));
                    }
                } catch (Exception e) {
                    queue.add(new MapTileDataPayload(tileX, tileZ, payload.cave(), dimension, 0L, new byte[0]));
                }
            });
        }
    }

    private long computeChunkBitmask(byte[] pngData) {
        try {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(pngData));
            if (img == null || img.getWidth() != TILE_PIXEL_SIZE || img.getHeight() != TILE_PIXEL_SIZE)
                return 0L;
            long mask = 0;
            for (int cz = 0; cz < TILE_SIZE; cz++) {
                for (int cx = 0; cx < TILE_SIZE; cx++) {
                    if (hasChunkData(img, cx, cz)) {
                        mask |= (1L << (cz * TILE_SIZE + cx));
                    }
                }
            }
            return mask;
        } catch (Exception e) {
            return 0L;
        }
    }

    // ======================== TICK ========================

    public void tick(MinecraftServer server) {
        if (!initialized) return;
        for (var entry : pendingResponses.entrySet()) {
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            if (player == null) {
                entry.getValue().clear();
                continue;
            }
            Queue<MapTileDataPayload> queue = entry.getValue();
            int sent = 0;
            while (sent < MAX_RESPONSES_PER_TICK && !queue.isEmpty()) {
                MapTileDataPayload response = queue.poll();
                PacketDistributor.sendToPlayer(player, response);
                sent++;
            }
        }
    }

    // ======================== LIFECYCLE ========================

    public void onPlayerDisconnect(UUID playerId) {
        lastUploadTimes.remove(playerId);
        Queue<?> queue = pendingResponses.remove(playerId);
        if (queue != null) queue.clear();
    }

    public static void reset() {
        if (instance != null) {
            instance.lastUploadTimes.clear();
            instance.pendingResponses.clear();
            instance.tileLocks.clear();
            instance.initialized = false;
            instance.mapBaseDir = null;
        }
    }

    private static String sanitizeDimension(String dimension) {
        if (dimension == null || dimension.isEmpty()) return "overworld";
        // e.g. "minecraft:overworld" -> "overworld", "minecraft:the_nether" -> "the_nether"
        String dim = dimension.contains(":") ? dimension.substring(dimension.indexOf(':') + 1) : dimension;
        return dim.replaceAll("[^a-zA-Z0-9_\\-]", "_");
    }
}

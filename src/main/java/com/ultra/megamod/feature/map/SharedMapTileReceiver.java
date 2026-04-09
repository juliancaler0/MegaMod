package com.ultra.megamod.feature.map;

import com.mojang.blaze3d.platform.NativeImage;
import com.ultra.megamod.feature.computer.screen.map.MapChunkTile;
import com.ultra.megamod.feature.computer.screen.map.MapChunkTileManager;
import com.ultra.megamod.feature.map.network.MapTileDataPayload;
import com.ultra.megamod.feature.map.network.MapTileRequestPayload;
import com.ultra.megamod.feature.map.network.MapTileUploadPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.Arrays;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Client-side handler for shared map tile synchronization.
 * Manages uploading locally-rendered tiles to the server and
 * receiving tiles explored by other players.
 */
public class SharedMapTileReceiver {
    // Track tiles we've already requested to avoid duplicate requests
    private static final Set<String> requestedTiles = ConcurrentHashMap.newKeySet();
    // Track tiles server confirmed it has no data for
    private static final Set<String> serverEmpty = ConcurrentHashMap.newKeySet();
    // Pending tile requests to batch
    private static final Queue<int[]> pendingRequests = new ConcurrentLinkedQueue<>();

    private static long lastRequestFlush = 0;
    private static long lastUploadTime = 0;

    // ======================== RECEIVE ========================

    public static void receiveTileData(MapTileDataPayload payload) {
        String key = payload.dimension() + "/" + payload.tileX() + "_" + payload.tileZ();
        requestedTiles.remove(key);

        if (payload.pngData().length == 0) {
            serverEmpty.add(key);
            return;
        }

        MapChunkTileManager mgr = MapChunkTileManager.getInstance();
        if (!mgr.isInitialized()) return;
        if (mgr.isCaveView() != payload.cave()) return;
        // Ignore tiles from a different dimension than what we're currently viewing
        if (!mgr.getCurrentDimension().equals(payload.dimension())) return;

        MapChunkTile tile = mgr.getOrCreateTile(payload.tileX(), payload.tileZ());
        if (tile == null || tile.getImage() == null) return;

        try {
            NativeImage serverImage = NativeImage.read(payload.pngData());
            if (serverImage.getWidth() == 128 && serverImage.getHeight() == 128) {
                NativeImage localImage = tile.getImage();
                boolean changed = false;
                for (int y = 0; y < 128; y++) {
                    for (int x = 0; x < 128; x++) {
                        int localPixel = localImage.getPixel(x, y);
                        if (((localPixel >> 24) & 0xFF) == 0) {
                            int serverPixel = serverImage.getPixel(x, y);
                            if (((serverPixel >> 24) & 0xFF) > 0) {
                                localImage.setPixel(x, y, serverPixel);
                                changed = true;
                            }
                        }
                    }
                }
                if (changed) {
                    tile.markNeedsUpdate();
                    tile.uploadTexture();
                    tile.rescanRenderedChunks();
                }
            }
            serverImage.close();
        } catch (Exception ignored) {}
    }

    // ======================== REQUEST ========================

    public static void requestTileFromServer(int tileX, int tileZ, boolean cave) {
        if (isSingleplayer()) return;
        String dimension = MapChunkTileManager.getInstance().getCurrentDimension();
        String key = dimension + "/" + tileX + "_" + tileZ;
        if (requestedTiles.contains(key) || serverEmpty.contains(key)) return;

        requestedTiles.add(key);
        pendingRequests.add(new int[]{tileX, tileZ});
    }

    public static void flushPendingRequests(boolean cave) {
        if (isSingleplayer()) return;
        long now = System.currentTimeMillis();
        if (now - lastRequestFlush < 500) return;
        lastRequestFlush = now;

        if (pendingRequests.isEmpty()) return;

        String dimension = MapChunkTileManager.getInstance().getCurrentDimension();
        int[] coords = new int[Math.min(pendingRequests.size() * 2, 32)];
        int idx = 0;
        while (idx < 32 && !pendingRequests.isEmpty()) {
            int[] pair = pendingRequests.poll();
            if (pair == null) break;
            coords[idx++] = pair[0];
            coords[idx++] = pair[1];
        }
        if (idx == 0) return;
        if (idx < coords.length) coords = Arrays.copyOf(coords, idx);

        ClientPacketDistributor.sendToServer(new MapTileRequestPayload(cave, dimension, coords));
    }

    // ======================== UPLOAD ========================

    public static void uploadTileToServer(int tileX, int tileZ, boolean cave,
                                           byte[] pngData, long chunkBitmask) {
        if (isSingleplayer()) return;
        if (pngData == null || pngData.length == 0 || chunkBitmask == 0) return;

        long now = System.currentTimeMillis();
        if (now - lastUploadTime < 1000) return;
        lastUploadTime = now;

        String dimension = MapChunkTileManager.getInstance().getCurrentDimension();
        serverEmpty.remove(dimension + "/" + tileX + "_" + tileZ);
        ClientPacketDistributor.sendToServer(
                new MapTileUploadPayload(tileX, tileZ, cave, dimension, chunkBitmask, pngData));
    }

    // ======================== UTIL ========================

    private static boolean isSingleplayer() {
        Minecraft mc = Minecraft.getInstance();
        return mc.getSingleplayerServer() != null;
    }

    public static void reset() {
        requestedTiles.clear();
        serverEmpty.clear();
        pendingRequests.clear();
        lastRequestFlush = 0;
        lastUploadTime = 0;
    }
}

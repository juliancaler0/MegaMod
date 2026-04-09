package com.ultra.megamod.feature.citizen.data;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.citizen.CitizenConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.HashMap;

public class ClaimManager {
    private static ClaimManager INSTANCE;
    private static final String FILE_NAME = "megamod_claims.dat";
    private final Map<String, ClaimData> claimsByFaction = new LinkedHashMap<>();
    private final Map<Long, String> chunkToFaction = new HashMap<>();
    private boolean dirty = false;

    public static ClaimManager get(ServerLevel level) {
        if (INSTANCE == null) {
            INSTANCE = new ClaimManager();
            INSTANCE.loadFromDisk(level);
        }
        return INSTANCE;
    }

    public static void reset() { INSTANCE = null; }

    public ClaimData getOrCreateClaim(String factionId) {
        return claimsByFaction.computeIfAbsent(factionId, id -> {
            markDirty();
            return new ClaimData(id);
        });
    }

    public ClaimData getClaim(String factionId) {
        return claimsByFaction.get(factionId);
    }

    private static long encodeChunk(int chunkX, int chunkZ) {
        return ((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL);
    }

    public String getFactionAtChunk(int chunkX, int chunkZ) {
        return chunkToFaction.get(encodeChunk(chunkX, chunkZ));
    }

    public boolean isChunkClaimed(int chunkX, int chunkZ) {
        return getFactionAtChunk(chunkX, chunkZ) != null;
    }

    /**
     * Claims a chunk for the given faction. Checks that the chunk is not already
     * claimed and that the faction has not exceeded MAX_CHUNKS.
     */
    public boolean claimChunk(String factionId, int chunkX, int chunkZ) {
        if (isChunkClaimed(chunkX, chunkZ)) return false;
        ClaimData claim = getOrCreateClaim(factionId);
        if (claim.getChunkCount() >= ClaimData.MAX_CHUNKS) return false;
        claim.claimChunk(chunkX, chunkZ);
        chunkToFaction.put(encodeChunk(chunkX, chunkZ), factionId);
        markDirty();
        return true;
    }

    public boolean unclaimChunk(String factionId, int chunkX, int chunkZ) {
        ClaimData data = claimsByFaction.get(factionId);
        if (data != null && data.isChunkClaimed(chunkX, chunkZ)) {
            data.unclaimChunk(chunkX, chunkZ);
            chunkToFaction.remove(encodeChunk(chunkX, chunkZ));
            if (data.getChunkCount() == 0) claimsByFaction.remove(factionId);
            markDirty();
            return true;
        }
        return false;
    }

    public void removeFactionClaims(String factionId) {
        ClaimData removed = claimsByFaction.remove(factionId);
        if (removed != null) {
            chunkToFaction.values().removeIf(factionId::equals);
            markDirty();
        }
    }

    public void transferClaims(String fromFaction, String toFaction) {
        ClaimData data = claimsByFaction.remove(fromFaction);
        if (data != null) {
            data.setOwnerFactionId(toFaction);
            claimsByFaction.put(toFaction, data);
            // Update reverse lookup
            for (Map.Entry<Long, String> entry : chunkToFaction.entrySet()) {
                if (fromFaction.equals(entry.getValue())) {
                    entry.setValue(toFaction);
                }
            }
            markDirty();
        }
    }

    /**
     * Returns the cost to claim the next chunk for the given faction.
     * Uses cascading cost: base * (1 + currentChunkCount * 0.1)
     * If CASCADE_CLAIM_COST is disabled, returns the flat base cost.
     */
    public int getChunkCost(String factionId) {
        int baseCost = CitizenConfig.CLAIM_BASE_COST;
        if (!CitizenConfig.CASCADE_CLAIM_COST) return baseCost;
        ClaimData data = claimsByFaction.get(factionId);
        int currentChunks = data != null ? data.getChunkCount() : 0;
        return (int) (baseCost * (1.0 + currentChunks * 0.1));
    }

    public Collection<ClaimData> getAllClaims() {
        return Collections.unmodifiableCollection(claimsByFaction.values());
    }

    private void markDirty() { dirty = true; }

    private void loadFromDisk(ServerLevel level) {
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataFile = new File(saveDir, "data" + File.separator + FILE_NAME);
            if (dataFile.exists()) {
                CompoundTag root = NbtIo.readCompressed((Path) dataFile.toPath(), (NbtAccounter) NbtAccounter.unlimitedHeap());
                CompoundTag claims = root.getCompoundOrEmpty("claims");
                for (String key : claims.keySet()) {
                    claimsByFaction.put(key, ClaimData.load(claims.getCompoundOrEmpty(key)));
                }
            }
            // Rebuild reverse-lookup index
            rebuildChunkIndex();
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to load claim data", e);
        }
    }

    private void rebuildChunkIndex() {
        chunkToFaction.clear();
        for (Map.Entry<String, ClaimData> entry : claimsByFaction.entrySet()) {
            String factionId = entry.getKey();
            for (long[] chunk : entry.getValue().getClaimedChunks()) {
                chunkToFaction.put(encodeChunk((int) chunk[0], (int) chunk[1]), factionId);
            }
        }
    }

    public void saveToDisk(ServerLevel level) {
        if (!dirty) return;
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataDir = new File(saveDir, "data");
            if (!dataDir.exists()) dataDir.mkdirs();
            File dataFile = new File(dataDir, FILE_NAME);
            CompoundTag root = new CompoundTag();
            CompoundTag claims = new CompoundTag();
            for (Map.Entry<String, ClaimData> entry : claimsByFaction.entrySet()) {
                claims.put(entry.getKey(), (Tag) entry.getValue().save());
            }
            root.put("claims", (Tag) claims);
            NbtIo.writeCompressed((CompoundTag) root, (Path) dataFile.toPath());
            dirty = false;
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to save claim data", e);
        }
    }
}

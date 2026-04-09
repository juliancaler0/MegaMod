package com.ultra.megamod.feature.citizen.data;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.util.AsyncSaveHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

/**
 * Tracks combat / war statistics per faction (colony).
 * Persists to {@code world/data/megamod_faction_stats.dat}.
 */
public final class FactionStatsManager {

    private static final String SAVE_FILE = "megamod_faction_stats.dat";
    private static FactionStatsManager INSTANCE;

    /**
     * Stats for a single faction.
     */
    public static class FactionStats {
        public int totalKills = 0;
        public int raidsDefended = 0;
        public int raidsFailed = 0;
        public int siegeWins = 0;
        public int siegeLosses = 0;
        public int citizenDeaths = 0;

        public int getWarScore() {
            return totalKills + raidsDefended * 10 + siegeWins * 50 - raidsFailed * 5 - siegeLosses * 25;
        }
    }

    private final Map<String, FactionStats> stats = new HashMap<>();
    private boolean dirty = false;

    private FactionStatsManager() {}

    public static FactionStatsManager get(@NotNull ServerLevel level) {
        if (INSTANCE == null) {
            INSTANCE = new FactionStatsManager();
            INSTANCE.loadFromDisk(level);
        }
        return INSTANCE;
    }

    public static void reset() {
        INSTANCE = null;
    }

    // ==================== Stats Access ====================

    @NotNull
    public FactionStats getStats(@NotNull String factionId) {
        return stats.computeIfAbsent(factionId, k -> new FactionStats());
    }

    public void addKills(@NotNull String factionId, int kills) {
        getStats(factionId).totalKills += kills;
        dirty = true;
    }

    public void recordRaidDefended(@NotNull String factionId) {
        getStats(factionId).raidsDefended++;
        dirty = true;
    }

    public void recordRaidFailed(@NotNull String factionId) {
        getStats(factionId).raidsFailed++;
        dirty = true;
    }

    public void recordSiegeWin(@NotNull String factionId) {
        getStats(factionId).siegeWins++;
        dirty = true;
    }

    public void recordSiegeLoss(@NotNull String factionId) {
        getStats(factionId).siegeLosses++;
        dirty = true;
    }

    public void recordCitizenDeath(@NotNull String factionId) {
        getStats(factionId).citizenDeaths++;
        dirty = true;
    }

    /**
     * Returns factions sorted by war score descending.
     */
    @NotNull
    public List<Map.Entry<String, Integer>> getWarLeaderboard() {
        List<Map.Entry<String, Integer>> entries = new ArrayList<>();
        for (Map.Entry<String, FactionStats> entry : stats.entrySet()) {
            entries.add(Map.entry(entry.getKey(), entry.getValue().getWarScore()));
        }
        entries.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        return entries;
    }

    // ==================== Persistence ====================

    public void saveToDisk(@NotNull ServerLevel level) {
        try {
            CompoundTag root = new CompoundTag();
            ListTag list = new ListTag();
            for (Map.Entry<String, FactionStats> entry : stats.entrySet()) {
                CompoundTag tag = new CompoundTag();
                tag.putString("factionId", entry.getKey());
                FactionStats s = entry.getValue();
                tag.putInt("totalKills", s.totalKills);
                tag.putInt("raidsDefended", s.raidsDefended);
                tag.putInt("raidsFailed", s.raidsFailed);
                tag.putInt("siegeWins", s.siegeWins);
                tag.putInt("siegeLosses", s.siegeLosses);
                tag.putInt("citizenDeaths", s.citizenDeaths);
                list.add(tag);
            }
            root.put("stats", list);

            File dataDir = new File(level.getServer().getWorldPath(
                    net.minecraft.world.level.storage.LevelResource.ROOT).toFile(), "data");
            if (!dataDir.exists()) dataDir.mkdirs();
            File saveFile = new File(dataDir, SAVE_FILE);

            final File fSave = saveFile;
            final CompoundTag fRoot = root;
            try {
                AsyncSaveHelper.saveAsync(() -> {
                    try { NbtIo.writeCompressed(fRoot, fSave.toPath()); }
                    catch (Exception e) { MegaMod.LOGGER.error("Failed to save faction stats", e); }
                });
            } catch (Exception e) {
                NbtIo.writeCompressed(root, saveFile.toPath());
            }
            dirty = false;
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to save faction stats", e);
        }
    }

    public void loadFromDisk(@NotNull ServerLevel level) {
        stats.clear();
        try {
            File dataDir = new File(level.getServer().getWorldPath(
                    net.minecraft.world.level.storage.LevelResource.ROOT).toFile(), "data");
            File saveFile = new File(dataDir, SAVE_FILE);
            if (!saveFile.exists()) return;

            CompoundTag root = NbtIo.readCompressed(saveFile.toPath(),
                    net.minecraft.nbt.NbtAccounter.unlimitedHeap());
            if (root == null) return;

            if (root.contains("stats")) {
                ListTag list = root.getListOrEmpty("stats");
                for (int i = 0; i < list.size(); i++) {
                    if (!(list.get(i) instanceof CompoundTag tag)) continue;
                    String factionId = tag.getStringOr("factionId", "");
                    if (factionId.isEmpty()) continue;
                    FactionStats s = new FactionStats();
                    s.totalKills = tag.getIntOr("totalKills", 0);
                    s.raidsDefended = tag.getIntOr("raidsDefended", 0);
                    s.raidsFailed = tag.getIntOr("raidsFailed", 0);
                    s.siegeWins = tag.getIntOr("siegeWins", 0);
                    s.siegeLosses = tag.getIntOr("siegeLosses", 0);
                    s.citizenDeaths = tag.getIntOr("citizenDeaths", 0);
                    stats.put(factionId, s);
                }
            }
            dirty = false;
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to load faction stats", e);
        }
    }
}

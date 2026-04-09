package com.ultra.megamod.feature.dungeons;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ultra.megamod.MegaMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DungeonLeaderboardManager {
    private static DungeonLeaderboardManager INSTANCE;
    private static final String FILE_NAME = "megamod_dungeon_leaderboard.dat";
    private static final int MAX_ENTRIES_PER_CATEGORY = 10;

    // Key: "TIER_partySize" (e.g., "NORMAL_1", "MYTHIC_4")
    // Value: sorted list of LeaderboardEntry (max 10 per category)
    private final Map<String, List<LeaderboardEntry>> leaderboards = new HashMap<>();
    private boolean dirty = false;

    public record LeaderboardEntry(UUID playerId, String playerName, long clearTimeMs, int partySize, long timestamp) {}

    public static DungeonLeaderboardManager get(ServerLevel level) {
        if (INSTANCE == null) {
            INSTANCE = new DungeonLeaderboardManager();
            INSTANCE.loadFromDisk(level);
        }
        return INSTANCE;
    }

    public static void reset() {
        INSTANCE = null;
    }

    /**
     * Submits a dungeon clear time. Adds the entry, sorts by fastest time, and trims to top 10.
     */
    public void submitTime(UUID playerId, String playerName, DungeonTier tier, int partySize, long clearTimeMs) {
        String key = tier.name() + "_" + partySize;
        List<LeaderboardEntry> entries = leaderboards.computeIfAbsent(key, k -> new ArrayList<>());

        LeaderboardEntry newEntry = new LeaderboardEntry(playerId, playerName, clearTimeMs, partySize, System.currentTimeMillis());
        entries.add(newEntry);

        // Sort by fastest clear time
        entries.sort(Comparator.comparingLong(LeaderboardEntry::clearTimeMs));

        // Trim to top 10
        while (entries.size() > MAX_ENTRIES_PER_CATEGORY) {
            entries.remove(entries.size() - 1);
        }

        dirty = true;
        MegaMod.LOGGER.info("Leaderboard entry: {} cleared {} (party {}) in {}ms",
                playerName, tier.name(), partySize, clearTimeMs);
    }

    /**
     * Returns top entries for a specific tier and party size.
     */
    public List<LeaderboardEntry> getTopTimes(DungeonTier tier, int partySize, int limit) {
        String key = tier.name() + "_" + partySize;
        List<LeaderboardEntry> entries = leaderboards.getOrDefault(key, List.of());
        return entries.subList(0, Math.min(limit, entries.size()));
    }

    /**
     * Returns top entries across all party sizes for a given tier.
     */
    public List<LeaderboardEntry> getTopTimesAllSizes(DungeonTier tier, int limit) {
        List<LeaderboardEntry> combined = new ArrayList<>();
        for (Map.Entry<String, List<LeaderboardEntry>> entry : leaderboards.entrySet()) {
            if (entry.getKey().startsWith(tier.name() + "_")) {
                combined.addAll(entry.getValue());
            }
        }
        combined.sort(Comparator.comparingLong(LeaderboardEntry::clearTimeMs));
        return combined.subList(0, Math.min(limit, combined.size()));
    }

    /**
     * Returns the player's personal best time for a specific tier (across all party sizes).
     */
    public LeaderboardEntry getPersonalBest(UUID playerId, DungeonTier tier) {
        LeaderboardEntry best = null;
        for (Map.Entry<String, List<LeaderboardEntry>> entry : leaderboards.entrySet()) {
            if (!entry.getKey().startsWith(tier.name() + "_")) continue;
            for (LeaderboardEntry le : entry.getValue()) {
                if (le.playerId().equals(playerId)) {
                    if (best == null || le.clearTimeMs() < best.clearTimeMs()) {
                        best = le;
                    }
                }
            }
        }
        return best;
    }

    /**
     * Returns a map of tier -> best entry (the single fastest clear for each tier).
     */
    public Map<DungeonTier, LeaderboardEntry> getAllTimeRecords() {
        Map<DungeonTier, LeaderboardEntry> records = new HashMap<>();
        for (DungeonTier tier : DungeonTier.values()) {
            List<LeaderboardEntry> top = getTopTimesAllSizes(tier, 1);
            if (!top.isEmpty()) {
                records.put(tier, top.get(0));
            }
        }
        return records;
    }

    /**
     * Serializes the leaderboard for a given tier to a JSON string for network sync.
     */
    public String toJson(DungeonTier tier, int limit) {
        JsonObject root = new JsonObject();

        // Top times across all party sizes
        JsonArray topArr = new JsonArray();
        for (LeaderboardEntry entry : getTopTimesAllSizes(tier, limit)) {
            JsonObject obj = new JsonObject();
            obj.addProperty("uuid", entry.playerId().toString());
            obj.addProperty("name", entry.playerName());
            obj.addProperty("time", entry.clearTimeMs());
            obj.addProperty("partySize", entry.partySize());
            obj.addProperty("timestamp", entry.timestamp());
            topArr.add(obj);
        }
        root.add("top", topArr);

        // Per party-size breakdowns (1-4)
        for (int ps = 1; ps <= 4; ps++) {
            JsonArray psArr = new JsonArray();
            for (LeaderboardEntry entry : getTopTimes(tier, ps, limit)) {
                JsonObject obj = new JsonObject();
                obj.addProperty("uuid", entry.playerId().toString());
                obj.addProperty("name", entry.playerName());
                obj.addProperty("time", entry.clearTimeMs());
                obj.addProperty("partySize", entry.partySize());
                obj.addProperty("timestamp", entry.timestamp());
                psArr.add(obj);
            }
            root.add("party_" + ps, psArr);
        }

        return root.toString();
    }

    // ---- Persistence ----

    public void saveToDisk(ServerLevel level) {
        if (!dirty) return;
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataFile = new File(saveDir, "data" + File.separator + FILE_NAME);
            dataFile.getParentFile().mkdirs();

            CompoundTag root = new CompoundTag();
            CompoundTag categoriesTag = new CompoundTag();

            for (Map.Entry<String, List<LeaderboardEntry>> entry : leaderboards.entrySet()) {
                CompoundTag categoryTag = new CompoundTag();
                int idx = 0;
                for (LeaderboardEntry le : entry.getValue()) {
                    CompoundTag entryTag = new CompoundTag();
                    entryTag.putString("uuid", le.playerId().toString());
                    entryTag.putString("name", le.playerName());
                    entryTag.putLong("time", le.clearTimeMs());
                    entryTag.putInt("partySize", le.partySize());
                    entryTag.putLong("timestamp", le.timestamp());
                    categoryTag.put("entry_" + idx, (Tag) entryTag);
                    idx++;
                }
                categoryTag.putInt("count", idx);
                categoriesTag.put(entry.getKey(), (Tag) categoryTag);
            }
            root.put("categories", (Tag) categoriesTag);
            NbtIo.writeCompressed((CompoundTag) root, (Path) dataFile.toPath());
            dirty = false;
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to save dungeon leaderboard data", (Throwable) e);
        }
    }

    public void loadFromDisk(ServerLevel level) {
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataFile = new File(saveDir, "data" + File.separator + FILE_NAME);
            if (!dataFile.exists()) return;

            CompoundTag root = NbtIo.readCompressed((Path) dataFile.toPath(), (NbtAccounter) NbtAccounter.unlimitedHeap());
            CompoundTag categoriesTag = root.getCompoundOrEmpty("categories");

            for (String key : categoriesTag.keySet()) {
                CompoundTag categoryTag = categoriesTag.getCompoundOrEmpty(key);
                int count = categoryTag.getIntOr("count", 0);
                List<LeaderboardEntry> entries = new ArrayList<>();
                for (int i = 0; i < count; i++) {
                    CompoundTag entryTag = categoryTag.getCompoundOrEmpty("entry_" + i);
                    UUID uuid = UUID.fromString(entryTag.getStringOr("uuid", "00000000-0000-0000-0000-000000000000"));
                    String name = entryTag.getStringOr("name", "Unknown");
                    long time = entryTag.getLongOr("time", 0L);
                    int partySize = entryTag.getIntOr("partySize", 1);
                    long timestamp = entryTag.getLongOr("timestamp", 0L);
                    entries.add(new LeaderboardEntry(uuid, name, time, partySize, timestamp));
                }
                entries.sort(Comparator.comparingLong(LeaderboardEntry::clearTimeMs));
                leaderboards.put(key, entries);
            }
            MegaMod.LOGGER.info("Loaded dungeon leaderboard with {} categories", leaderboards.size());
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to load dungeon leaderboard data", (Throwable) e);
        }
    }
}

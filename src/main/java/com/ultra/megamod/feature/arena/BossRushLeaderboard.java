package com.ultra.megamod.feature.arena;

import com.ultra.megamod.MegaMod;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;

/**
 * NbtIo persistent leaderboard for Boss Rush best completion times.
 */
public class BossRushLeaderboard {
    private static BossRushLeaderboard INSTANCE;
    private static final String FILE_NAME = "megamod_boss_rush_leaderboard.dat";

    /** Map of player UUID to best time in milliseconds. */
    private final Map<UUID, Long> bestTimes = new HashMap<>();
    /** Map of player UUID to display name (cached). */
    private final Map<UUID, String> playerNames = new HashMap<>();
    private boolean dirty = false;

    public static BossRushLeaderboard get(ServerLevel level) {
        if (INSTANCE == null) {
            INSTANCE = new BossRushLeaderboard();
            INSTANCE.loadFromDisk(level);
        }
        return INSTANCE;
    }

    public static void reset() {
        INSTANCE = null;
    }

    /**
     * Record a Boss Rush completion time. Only updates if it's a new personal best.
     */
    public void recordTime(UUID playerId, long timeMs) {
        Long current = bestTimes.get(playerId);
        if (current == null || timeMs < current) {
            bestTimes.put(playerId, timeMs);
            dirty = true;
        }
    }

    /**
     * Record or update a player's display name.
     */
    public void setPlayerName(UUID playerId, String name) {
        playerNames.put(playerId, name);
        dirty = true;
    }

    /**
     * Get a player's best time, or 0 if no record.
     */
    public long getBestTime(UUID playerId) {
        return bestTimes.getOrDefault(playerId, 0L);
    }

    /**
     * Get the top N times as a sorted list of entries (fastest first).
     */
    public List<LeaderboardEntry> getTopTimes(int limit) {
        List<LeaderboardEntry> entries = new ArrayList<>();
        for (Map.Entry<UUID, Long> entry : bestTimes.entrySet()) {
            String name = playerNames.getOrDefault(entry.getKey(), entry.getKey().toString().substring(0, 8));
            entries.add(new LeaderboardEntry(entry.getKey(), name, entry.getValue()));
        }
        entries.sort(Comparator.comparingLong(e -> e.timeMs));
        if (entries.size() > limit) {
            entries = entries.subList(0, limit);
        }
        return entries;
    }

    public record LeaderboardEntry(UUID playerId, String playerName, long timeMs) {}

    // --- Persistence ---

    public void saveToDisk(ServerLevel level) {
        if (!dirty) return;
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataDir = new File(saveDir, "data");
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }
            File dataFile = new File(dataDir, FILE_NAME);

            CompoundTag root = new CompoundTag();

            CompoundTag timesTag = new CompoundTag();
            for (Map.Entry<UUID, Long> entry : bestTimes.entrySet()) {
                timesTag.putLong(entry.getKey().toString(), entry.getValue());
            }
            root.put("bestTimes", (Tag) timesTag);

            CompoundTag namesTag = new CompoundTag();
            for (Map.Entry<UUID, String> entry : playerNames.entrySet()) {
                namesTag.putString(entry.getKey().toString(), entry.getValue());
            }
            root.put("playerNames", (Tag) namesTag);

            NbtIo.writeCompressed((CompoundTag) root, (Path) dataFile.toPath());
            dirty = false;
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to save Boss Rush leaderboard", (Throwable) e);
        }
    }

    public void loadFromDisk(ServerLevel level) {
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataFile = new File(saveDir, "data" + File.separator + FILE_NAME);
            if (dataFile.exists()) {
                CompoundTag root = NbtIo.readCompressed((Path) dataFile.toPath(), (NbtAccounter) NbtAccounter.unlimitedHeap());

                CompoundTag timesTag = root.getCompoundOrEmpty("bestTimes");
                for (String uuidStr : timesTag.keySet()) {
                    try {
                        UUID uuid = UUID.fromString(uuidStr);
                        long time = timesTag.getLongOr(uuidStr, 0L);
                        if (time > 0) {
                            bestTimes.put(uuid, time);
                        }
                    } catch (IllegalArgumentException e) {
                        // skip invalid UUIDs
                    }
                }

                CompoundTag namesTag = root.getCompoundOrEmpty("playerNames");
                for (String uuidStr : namesTag.keySet()) {
                    try {
                        UUID uuid = UUID.fromString(uuidStr);
                        String name = namesTag.getStringOr(uuidStr, "");
                        if (!name.isEmpty()) {
                            playerNames.put(uuid, name);
                        }
                    } catch (IllegalArgumentException e) {
                        // skip
                    }
                }

                MegaMod.LOGGER.info("Loaded Boss Rush leaderboard with {} entries", bestTimes.size());
            }
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to load Boss Rush leaderboard", (Throwable) e);
        }
    }
}

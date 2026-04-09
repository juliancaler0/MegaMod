package com.ultra.megamod.feature.computer.minigames;

import com.ultra.megamod.MegaMod;
import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;

/**
 * Tracks per-player high scores for computer minigames.
 * Stores best score per game per player. NbtIo persistence.
 */
public class MinigameScoreManager {
    private static MinigameScoreManager INSTANCE;
    private static final String FILE_NAME = "megamod_minigame_scores.dat";

    public static final String SNAKE = "snake";
    public static final String TETRIS = "tetris";
    public static final String MINESWEEPER = "minesweeper";

    // playerUUID -> (game -> highScore)
    private final Map<UUID, Map<String, Integer>> scores = new HashMap<>();
    private boolean dirty = false;

    public static MinigameScoreManager get(ServerLevel level) {
        if (INSTANCE == null) {
            INSTANCE = new MinigameScoreManager();
            INSTANCE.loadFromDisk(level);
        }
        return INSTANCE;
    }

    public static void reset() {
        INSTANCE = null;
    }

    /**
     * Submit a score. Only saves if it beats the player's current high score.
     * @return true if this was a new high score
     */
    public boolean submitScore(UUID playerId, String game, int score) {
        Map<String, Integer> playerScores = scores.computeIfAbsent(playerId, k -> new HashMap<>());
        int current = playerScores.getOrDefault(game, 0);
        if (score > current) {
            playerScores.put(game, score);
            dirty = true;
            return true;
        }
        return false;
    }

    /**
     * Force-set a score regardless of current value. Used by admin panel.
     */
    public void setScore(UUID playerId, String game, int score) {
        Map<String, Integer> playerScores = scores.computeIfAbsent(playerId, k -> new HashMap<>());
        playerScores.put(game, Math.max(0, score));
        dirty = true;
    }

    public int getHighScore(UUID playerId, String game) {
        Map<String, Integer> playerScores = scores.get(playerId);
        if (playerScores == null) return 0;
        return playerScores.getOrDefault(game, 0);
    }

    /**
     * Get the combined best score across all games for a player.
     * Used for the leaderboard "Games" category.
     */
    public int getCombinedScore(UUID playerId) {
        Map<String, Integer> playerScores = scores.get(playerId);
        if (playerScores == null) return 0;
        int total = 0;
        for (int s : playerScores.values()) {
            total += s;
        }
        return total;
    }

    public Map<UUID, Map<String, Integer>> getAllScores() {
        return Collections.unmodifiableMap(scores);
    }

    private void loadFromDisk(ServerLevel level) {
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataFile = new File(saveDir, "data" + File.separator + FILE_NAME);
            if (dataFile.exists()) {
                CompoundTag root = NbtIo.readCompressed((Path) dataFile.toPath(), (NbtAccounter) NbtAccounter.unlimitedHeap());
                CompoundTag players = root.getCompoundOrEmpty("players");
                for (String uuidStr : players.keySet()) {
                    try {
                        UUID uuid = UUID.fromString(uuidStr);
                        CompoundTag gameScores = players.getCompoundOrEmpty(uuidStr);
                        Map<String, Integer> playerScores = new HashMap<>();
                        for (String game : gameScores.keySet()) {
                            playerScores.put(game, gameScores.getIntOr(game, 0));
                        }
                        scores.put(uuid, playerScores);
                    } catch (Exception e) {
                        // Skip malformed entries
                    }
                }
            }
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to load minigame score data", e);
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
            CompoundTag players = new CompoundTag();
            for (Map.Entry<UUID, Map<String, Integer>> entry : scores.entrySet()) {
                CompoundTag gameScores = new CompoundTag();
                for (Map.Entry<String, Integer> ge : entry.getValue().entrySet()) {
                    gameScores.putInt(ge.getKey(), ge.getValue());
                }
                players.put(entry.getKey().toString(), (Tag) gameScores);
            }
            root.put("players", (Tag) players);
            NbtIo.writeCompressed((CompoundTag) root, (Path) dataFile.toPath());
            dirty = false;
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to save minigame score data", e);
        }
    }
}

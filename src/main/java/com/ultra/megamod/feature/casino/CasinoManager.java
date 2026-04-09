package com.ultra.megamod.feature.casino;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.audit.AuditLogManager;
import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class CasinoManager {
    private static CasinoManager INSTANCE;
    private static final String FILE_NAME = "megamod_casino_stats.dat";

    // Per-player stats: {totalWagered, totalWon, totalLost, gamesPlayed, biggestWin,
    //                     slotsPlayed, slotsWon, blackjackPlayed, blackjackWon, wheelPlayed, wheelWon,
    //                     roulettePlayed, rouletteWon, baccaratPlayed, baccaratWon}
    private static final int STAT_TOTAL_WAGERED = 0;
    private static final int STAT_TOTAL_WON = 1;
    private static final int STAT_TOTAL_LOST = 2;
    private static final int STAT_GAMES_PLAYED = 3;
    private static final int STAT_BIGGEST_WIN = 4;
    private static final int STAT_SLOTS_PLAYED = 5;
    private static final int STAT_SLOTS_WON = 6;
    private static final int STAT_BLACKJACK_PLAYED = 7;
    private static final int STAT_BLACKJACK_WON = 8;
    private static final int STAT_WHEEL_PLAYED = 9;
    private static final int STAT_WHEEL_WON = 10;
    private static final int STAT_ROULETTE_PLAYED = 11;
    private static final int STAT_ROULETTE_WON = 12;
    private static final int STAT_BACCARAT_PLAYED = 13;
    private static final int STAT_BACCARAT_WON = 14;
    private static final int STAT_CRAPS_PLAYED = 15;
    private static final int STAT_CRAPS_WON = 16;
    private static final int STAT_COUNT = 17;

    private static final String[] STAT_KEYS = {
            "totalWagered", "totalWon", "totalLost", "gamesPlayed", "biggestWin",
            "slotsPlayed", "slotsWon", "blackjackPlayed", "blackjackWon", "wheelPlayed", "wheelWon",
            "roulettePlayed", "rouletteWon",
            "baccaratPlayed", "baccaratWon",
            "crapsPlayed", "crapsWon"
    };

    private final Map<UUID, int[]> playerStats = new HashMap<>();
    // Per-game admin always-win flags (in-memory only, not persisted)
    private final Map<UUID, Boolean> alwaysWinSlots = new HashMap<>();
    private final Map<UUID, Boolean> alwaysWinBlackjack = new HashMap<>();
    private final Map<UUID, Boolean> alwaysWinWheel = new HashMap<>();
    private final Map<UUID, Boolean> alwaysWinCraps = new HashMap<>();
    private final Map<UUID, Boolean> alwaysWinRoulette = new HashMap<>();
    private final Map<UUID, Boolean> alwaysWinBaccarat = new HashMap<>();
    private final Map<UUID, Boolean> alwaysWinPayoutOverride = new HashMap<>(); // BJ sub-toggle: win even on early stand
    private boolean dirty = false;

    public static CasinoManager get(ServerLevel level) {
        if (INSTANCE == null) {
            INSTANCE = new CasinoManager();
            INSTANCE.loadFromDisk(level);
        }
        return INSTANCE;
    }

    public static void reset() {
        INSTANCE = null;
    }

    private int[] getOrCreate(UUID playerId) {
        return playerStats.computeIfAbsent(playerId, id -> new int[STAT_COUNT]);
    }

    public void recordWager(UUID playerId, int amount, String gameType) {
        int[] stats = getOrCreate(playerId);
        stats[STAT_TOTAL_WAGERED] += amount;
        stats[STAT_GAMES_PLAYED]++;
        switch (gameType) {
            case "slots" -> stats[STAT_SLOTS_PLAYED]++;
            case "blackjack" -> stats[STAT_BLACKJACK_PLAYED]++;
            case "wheel" -> stats[STAT_WHEEL_PLAYED]++;
            case "roulette" -> stats[STAT_ROULETTE_PLAYED]++;
            case "baccarat" -> stats[STAT_BACCARAT_PLAYED]++;
            case "craps" -> stats[STAT_CRAPS_PLAYED]++;
        }
        markDirty();
    }

    public void recordWin(UUID playerId, int amount, String gameType) {
        int[] stats = getOrCreate(playerId);
        stats[STAT_TOTAL_WON] += amount;
        if (amount > stats[STAT_BIGGEST_WIN]) {
            stats[STAT_BIGGEST_WIN] = amount;
        }
        switch (gameType) {
            case "slots" -> stats[STAT_SLOTS_WON]++;
            case "blackjack" -> stats[STAT_BLACKJACK_WON]++;
            case "wheel" -> stats[STAT_WHEEL_WON]++;
            case "roulette" -> stats[STAT_ROULETTE_WON]++;
            case "baccarat" -> stats[STAT_BACCARAT_WON]++;
            case "craps" -> stats[STAT_CRAPS_WON]++;
        }
        markDirty();
    }

    public void recordLoss(UUID playerId, int amount, String gameType) {
        int[] stats = getOrCreate(playerId);
        stats[STAT_TOTAL_LOST] += amount;
        markDirty();
    }

    public int[] getStats(UUID playerId) {
        return getOrCreate(playerId);
    }

    /** Check always-win for any game (backwards compat - true if ANY game is enabled) */
    public boolean isAlwaysWin(UUID playerId) {
        return isAlwaysWinSlots(playerId) || isAlwaysWinBlackjack(playerId) || isAlwaysWinWheel(playerId) || isAlwaysWinCraps(playerId) || isAlwaysWinRoulette(playerId) || isAlwaysWinBaccarat(playerId);
    }

    public boolean isAlwaysWinSlots(UUID playerId) {
        return alwaysWinSlots.getOrDefault(playerId, false) && isAdminPlayer(playerId);
    }

    public boolean isAlwaysWinBlackjack(UUID playerId) {
        return alwaysWinBlackjack.getOrDefault(playerId, false) && isAdminPlayer(playerId);
    }

    public boolean isAlwaysWinWheel(UUID playerId) {
        return alwaysWinWheel.getOrDefault(playerId, false) && isAdminPlayer(playerId);
    }

    public boolean isAlwaysWinCraps(UUID playerId) {
        return alwaysWinCraps.getOrDefault(playerId, false) && isAdminPlayer(playerId);
    }

    public boolean isAlwaysWinRoulette(UUID playerId) {
        return alwaysWinRoulette.getOrDefault(playerId, false) && isAdminPlayer(playerId);
    }

    public boolean isAlwaysWinBaccarat(UUID playerId) {
        return alwaysWinBaccarat.getOrDefault(playerId, false) && isAdminPlayer(playerId);
    }

    /** Auto-win only works for admin players */
    private boolean isAdminPlayer(UUID playerId) {
        net.minecraft.server.MinecraftServer server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null) return false;
        net.minecraft.server.level.ServerPlayer player = server.getPlayerList().getPlayer(playerId);
        return player != null && com.ultra.megamod.feature.computer.admin.AdminSystem.isAdmin(player);
    }

    public void setAlwaysWin(UUID playerId, boolean value, ServerLevel level) {
        setAlwaysWinSlots(playerId, value, level);
        setAlwaysWinBlackjack(playerId, value, level);
        setAlwaysWinWheel(playerId, value, level);
        setAlwaysWinCraps(playerId, value, level);
        setAlwaysWinRoulette(playerId, value, level);
        setAlwaysWinBaccarat(playerId, value, level);
    }

    public void setAlwaysWinSlots(UUID playerId, boolean value, ServerLevel level) {
        if (value) alwaysWinSlots.put(playerId, true); else alwaysWinSlots.remove(playerId);
        logAlwaysWinAudit(playerId, "Slots", value, level);
    }

    public void setAlwaysWinBlackjack(UUID playerId, boolean value, ServerLevel level) {
        if (value) alwaysWinBlackjack.put(playerId, true); else alwaysWinBlackjack.remove(playerId);
        logAlwaysWinAudit(playerId, "Blackjack", value, level);
    }

    public void setAlwaysWinWheel(UUID playerId, boolean value, ServerLevel level) {
        if (value) alwaysWinWheel.put(playerId, true); else alwaysWinWheel.remove(playerId);
        logAlwaysWinAudit(playerId, "Wheel", value, level);
    }

    public void setAlwaysWinCraps(UUID playerId, boolean value, ServerLevel level) {
        if (value) alwaysWinCraps.put(playerId, true); else alwaysWinCraps.remove(playerId);
        logAlwaysWinAudit(playerId, "Craps", value, level);
    }

    public void setAlwaysWinRoulette(UUID playerId, boolean value, ServerLevel level) {
        if (value) alwaysWinRoulette.put(playerId, true); else alwaysWinRoulette.remove(playerId);
        logAlwaysWinAudit(playerId, "Roulette", value, level);
    }

    public void setAlwaysWinBaccarat(UUID playerId, boolean value, ServerLevel level) {
        if (value) alwaysWinBaccarat.put(playerId, true); else alwaysWinBaccarat.remove(playerId);
        logAlwaysWinAudit(playerId, "Baccarat", value, level);
    }

    public boolean isAlwaysWinPayoutOverride(UUID playerId) {
        return alwaysWinPayoutOverride.getOrDefault(playerId, false) && isAdminPlayer(playerId);
    }

    public void setAlwaysWinPayoutOverride(UUID playerId, boolean value, ServerLevel level) {
        if (value) alwaysWinPayoutOverride.put(playerId, true); else alwaysWinPayoutOverride.remove(playerId);
        logAlwaysWinAudit(playerId, "PayoutOverride", value, level);
    }

    private void logAlwaysWinAudit(UUID playerId, String game, boolean enabled, ServerLevel level) {
        String playerName = "Unknown";
        net.minecraft.server.MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            ServerPlayer player = server.getPlayerList().getPlayer(playerId);
            if (player != null) {
                playerName = player.getGameProfile().name();
            }
        }
        String action = enabled ? "ENABLED" : "DISABLED";
        AuditLogManager.get(level).log(
                playerName,
                playerId.toString(),
                AuditLogManager.EventType.CASINO_ADMIN,
                "Casino always-win " + action + " for " + game
        );
    }

    public int getTotalHouseProfit() {
        int totalWagered = 0;
        int totalWon = 0;
        for (int[] stats : playerStats.values()) {
            totalWagered += stats[STAT_TOTAL_WAGERED];
            totalWon += stats[STAT_TOTAL_WON];
        }
        return totalWagered - totalWon;
    }

    public int getActiveGamesCount() {
        int count = 0;
        // Roulette tables with active games
        count += com.ultra.megamod.feature.casino.network.RouletteActionPayload.RouletteTableRegistry.allGames().size();
        // Blackjack players seated at tables
        count += com.ultra.megamod.feature.casino.blackjack.BlackjackTableBlockEntity.getSeatedPlayerCount();
        // Baccarat active sessions
        count += com.ultra.megamod.feature.casino.network.BaccaratActionPayload.allGames().size();
        // Craps active tables
        count += com.ultra.megamod.feature.casino.network.CrapsActionPayload.gameCount();
        return count;
    }

    public Map<UUID, int[]> getAllPlayerStats() {
        return java.util.Collections.unmodifiableMap(playerStats);
    }

    private void markDirty() {
        this.dirty = true;
    }

    private Path getSavePath(ServerLevel level) {
        return level.getServer().getWorldPath(LevelResource.ROOT)
                .resolve("data").resolve(FILE_NAME);
    }

    private void loadFromDisk(ServerLevel level) {
        try {
            File dataFile = getSavePath(level).toFile();
            if (!dataFile.exists()) {
                MegaMod.LOGGER.info("No casino stats file found, starting fresh.");
                return;
            }
            CompoundTag root = NbtIo.readCompressed(dataFile.toPath(), NbtAccounter.unlimitedHeap());
            CompoundTag players = root.getCompoundOrEmpty("players");
            for (String key : players.keySet()) {
                try {
                    UUID uuid = UUID.fromString(key);
                    CompoundTag pData = players.getCompoundOrEmpty(key);
                    int[] stats = new int[STAT_COUNT];
                    for (int i = 0; i < STAT_COUNT; i++) {
                        stats[i] = pData.getIntOr(STAT_KEYS[i], 0);
                    }
                    playerStats.put(uuid, stats);
                } catch (IllegalArgumentException ignored) {
                }
            }
            MegaMod.LOGGER.info("Casino stats loaded for {} players.", playerStats.size());
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to load casino stats!", e);
        }
    }

    public void saveToDisk(ServerLevel level) {
        if (!dirty) {
            return;
        }
        try {
            File dataFile = getSavePath(level).toFile();
            File dataDir = dataFile.getParentFile();
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }
            CompoundTag root = new CompoundTag();
            CompoundTag players = new CompoundTag();
            for (Map.Entry<UUID, int[]> entry : playerStats.entrySet()) {
                CompoundTag pData = new CompoundTag();
                int[] stats = entry.getValue();
                for (int i = 0; i < STAT_COUNT; i++) {
                    pData.putInt(STAT_KEYS[i], stats[i]);
                }
                players.put(entry.getKey().toString(), (Tag) pData);
            }
            root.put("players", (Tag) players);
            NbtIo.writeCompressed(root, dataFile.toPath());
            dirty = false;
            MegaMod.LOGGER.debug("Casino stats saved.");
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to save casino stats!", e);
        }
    }
}

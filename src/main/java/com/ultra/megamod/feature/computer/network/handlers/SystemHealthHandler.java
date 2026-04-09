package com.ultra.megamod.feature.computer.network.handlers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ultra.megamod.feature.computer.admin.AdminSystem;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.economy.EconomyManager;
import com.ultra.megamod.feature.toggles.FeatureToggleManager;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.network.PacketDistributor;

import java.io.File;
import java.util.*;

/**
 * Network handler for the System Health monitoring panel in the admin terminal.
 */
public class SystemHealthHandler {

    // Ring buffer for recent log entries
    private static final List<String> recentLogEntries = Collections.synchronizedList(new ArrayList<>());
    private static final int MAX_LOG_ENTRIES = 50;

    // Ring buffer for TPS/memory history (one entry per ~3 seconds, 60 entries = ~3 minutes)
    private static final int HISTORY_SIZE = 60;
    private static final List<HistoryEntry> tpsHistory = new ArrayList<>();

    public record HistoryEntry(long timestamp, double tps, double mspt, long usedMemMb, long maxMemMb, int entityCount, int playerCount) {}

    /** Returns the current history list (for other systems to read). */
    public static List<HistoryEntry> getHistory() {
        return Collections.unmodifiableList(new ArrayList<>(tpsHistory));
    }

    /** Clears the history buffer (call on server stop). */
    public static void reset() {
        tpsHistory.clear();
    }

    /** Call from MegaMod.LOGGER or a custom appender to capture log entries */
    public static void addLogEntry(String entry) {
        recentLogEntries.add(entry);
        while (recentLogEntries.size() > MAX_LOG_ENTRIES) {
            recentLogEntries.remove(0);
        }
    }

    public static boolean handle(ServerPlayer player, String action, String jsonData,
                                  ServerLevel level, EconomyManager eco) {
        if (!"system_health_request".equals(action)) return false;
        if (!AdminSystem.isAdmin(player)) return false;

        JsonObject root = new JsonObject();
        MinecraftServer server = level.getServer();

        // TPS & MSPT
        double mspt = server.getAverageTickTimeNanos() / 1_000_000.0;
        double tps = mspt > 0 ? Math.min(20.0, 1000.0 / mspt) : 20.0;
        root.addProperty("tps", Math.round(tps * 10.0) / 10.0);
        root.addProperty("mspt", Math.round(mspt * 10.0) / 10.0);

        // Memory
        Runtime rt = Runtime.getRuntime();
        long maxMem = rt.maxMemory() / (1024 * 1024);
        long totalMem = rt.totalMemory() / (1024 * 1024);
        long freeMem = rt.freeMemory() / (1024 * 1024);
        long usedMem = totalMem - freeMem;
        root.addProperty("memUsed", usedMem);
        root.addProperty("memMax", maxMem);
        root.addProperty("memTotal", totalMem);

        // Entity counts
        int totalEntities = 0;
        int totalPlayers = 0;
        int totalMobs = 0;
        int totalCitizens = 0;
        int totalItems = 0;
        int totalChunks = 0;

        JsonArray dimensionsArr = new JsonArray();
        for (ServerLevel dim : server.getAllLevels()) {
            int dimEntities = 0;
            int dimPlayers = 0;
            int dimMobs = 0;
            int dimCitizens = 0;
            int dimItems = 0;

            for (Entity entity : dim.getAllEntities()) {
                dimEntities++;
                if (entity instanceof Player) {
                    dimPlayers++;
                } else if (entity instanceof Monster) {
                    dimMobs++;
                } else if (entity instanceof ItemEntity) {
                    dimItems++;
                } else if (entity.getType().toString().contains("citizen")) {
                    dimCitizens++;
                }
            }

            int dimChunks = 0;
            try {
                dimChunks = dim.getChunkSource().getLoadedChunksCount();
            } catch (Exception ignored) {}

            totalEntities += dimEntities;
            totalPlayers += dimPlayers;
            totalMobs += dimMobs;
            totalCitizens += dimCitizens;
            totalItems += dimItems;
            totalChunks += dimChunks;

            JsonObject dimObj = new JsonObject();
            dimObj.addProperty("name", dim.dimension().identifier().toString());
            dimObj.addProperty("players", dimPlayers);
            dimObj.addProperty("entities", dimEntities);
            dimObj.addProperty("chunks", dimChunks);
            dimensionsArr.add(dimObj);
        }

        root.addProperty("entityTotal", totalEntities);
        root.addProperty("entityPlayers", totalPlayers);
        root.addProperty("entityMobs", totalMobs);
        root.addProperty("entityCitizens", totalCitizens);
        root.addProperty("entityItems", totalItems);
        root.addProperty("loadedChunks", totalChunks);
        root.add("dimensions", dimensionsArr);

        // Record history data point
        tpsHistory.add(new HistoryEntry(System.currentTimeMillis(), tps, mspt, usedMem, maxMem, totalEntities, totalPlayers));
        while (tpsHistory.size() > HISTORY_SIZE) tpsHistory.remove(0);

        // Build history JSON array
        JsonArray historyArr = new JsonArray();
        for (HistoryEntry he : tpsHistory) {
            JsonObject h = new JsonObject();
            h.addProperty("t", he.timestamp());
            h.addProperty("tps", Math.round(he.tps() * 10.0) / 10.0);
            h.addProperty("mspt", Math.round(he.mspt() * 10.0) / 10.0);
            h.addProperty("mem", he.maxMemMb() > 0 ? (int) (he.usedMemMb() * 100 / he.maxMemMb()) : 0);
            h.addProperty("ent", he.entityCount());
            historyArr.add(h);
        }
        root.add("tpsHistory", historyArr);

        // Persistence file sizes
        JsonArray filesArr = new JsonArray();
        try {
            File saveDir = server.getWorldPath(LevelResource.ROOT).toFile();
            File dataDir = new File(saveDir, "data");
            if (dataDir.exists()) {
                File[] files = dataDir.listFiles((dir, name) -> name.startsWith("megamod_") && name.endsWith(".dat"));
                if (files != null) {
                    for (File f : files) {
                        JsonObject fObj = new JsonObject();
                        fObj.addProperty("name", f.getName());
                        long sizeKB = f.length() / 1024;
                        fObj.addProperty("size", sizeKB + " KB");
                        filesArr.add(fObj);
                    }
                }
            }
        } catch (Exception ignored) {}
        root.add("persistFiles", filesArr);

        // Feature toggles
        int enabled = 0, disabled = 0;
        JsonArray togglesArr = new JsonArray();
        try {
            FeatureToggleManager ftm = FeatureToggleManager.get(level);
            Map<String, Boolean> toggles = ftm.getAllToggles();
            for (Map.Entry<String, Boolean> entry : toggles.entrySet()) {
                JsonObject tObj = new JsonObject();
                tObj.addProperty("feature", entry.getKey());
                tObj.addProperty("enabled", entry.getValue());
                togglesArr.add(tObj);
                if (entry.getValue()) enabled++;
                else disabled++;
            }
        } catch (Exception ignored) {}
        root.addProperty("togglesEnabled", enabled);
        root.addProperty("togglesDisabled", disabled);
        root.add("toggles", togglesArr);

        // Error log
        JsonArray logArr = new JsonArray();
        synchronized (recentLogEntries) {
            for (String entry : recentLogEntries) {
                logArr.add(entry);
            }
        }
        root.add("errorLog", logArr);

        // Issue detection
        JsonArray issuesArr = new JsonArray();
        if (tps < 15) {
            JsonObject issue = new JsonObject();
            issue.addProperty("severity", "ERROR");
            issue.addProperty("description", "Low TPS: " + String.format("%.1f", tps) + " (target: 20)");
            issue.addProperty("link", "");
            issuesArr.add(issue);
        } else if (tps < 18) {
            JsonObject issue = new JsonObject();
            issue.addProperty("severity", "WARN");
            issue.addProperty("description", "TPS slightly low: " + String.format("%.1f", tps));
            issue.addProperty("link", "");
            issuesArr.add(issue);
        }
        if (usedMem > maxMem * 0.85) {
            JsonObject issue = new JsonObject();
            issue.addProperty("severity", "WARN");
            issue.addProperty("description", "High memory usage: " + usedMem + "MB / " + maxMem + "MB (" + (usedMem * 100 / maxMem) + "%)");
            issue.addProperty("link", "");
            issuesArr.add(issue);
        }
        if (totalEntities > 5000) {
            JsonObject issue = new JsonObject();
            issue.addProperty("severity", "WARN");
            issue.addProperty("description", "High entity count: " + totalEntities);
            issue.addProperty("link", "");
            issuesArr.add(issue);
        }
        if (totalItems > 1000) {
            JsonObject issue = new JsonObject();
            issue.addProperty("severity", "WARN");
            issue.addProperty("description", "Many item entities on ground: " + totalItems);
            issue.addProperty("link", "");
            issuesArr.add(issue);
        }
        root.add("issues", issuesArr);

        // Send response
        int wallet = eco.getWallet(player.getUUID());
        int bank = eco.getBank(player.getUUID());
        PacketDistributor.sendToPlayer(player,
                (CustomPacketPayload) new ComputerDataPayload("system_health_data", root.toString(), wallet, bank));

        return true;
    }
}

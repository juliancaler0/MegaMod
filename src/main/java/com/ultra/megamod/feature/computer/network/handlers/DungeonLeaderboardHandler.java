package com.ultra.megamod.feature.computer.network.handlers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.dungeons.DungeonLeaderboardManager;
import com.ultra.megamod.feature.dungeons.DungeonTier;
import com.ultra.megamod.feature.economy.EconomyManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DungeonLeaderboardHandler {

    public static boolean handle(ServerPlayer player, String action, String jsonData, ServerLevel level, EconomyManager eco) {
        if (!"dungeon_leaderboard_request".equals(action)) return false;

        ServerLevel overworld = level.getServer().overworld();
        DungeonLeaderboardManager lb = DungeonLeaderboardManager.get(overworld);
        UUID playerUuid = player.getUUID();

        JsonObject root = new JsonObject();

        // Top 10 times per tier (across all party sizes)
        JsonObject tierBoards = new JsonObject();
        for (DungeonTier tier : DungeonTier.values()) {
            List<DungeonLeaderboardManager.LeaderboardEntry> top = lb.getTopTimesAllSizes(tier, 10);
            JsonArray arr = new JsonArray();
            for (DungeonLeaderboardManager.LeaderboardEntry entry : top) {
                JsonObject obj = new JsonObject();
                obj.addProperty("uuid", entry.playerId().toString());
                obj.addProperty("name", entry.playerName());
                obj.addProperty("time", entry.clearTimeMs());
                obj.addProperty("timeFormatted", formatTime(entry.clearTimeMs()));
                obj.addProperty("partySize", entry.partySize());
                obj.addProperty("timestamp", entry.timestamp());
                arr.add(obj);
            }
            tierBoards.add(tier.name(), arr);
        }
        root.add("tierBoards", tierBoards);

        // Player's personal bests per tier
        JsonObject personalBests = new JsonObject();
        for (DungeonTier tier : DungeonTier.values()) {
            DungeonLeaderboardManager.LeaderboardEntry pb = lb.getPersonalBest(playerUuid, tier);
            if (pb != null) {
                JsonObject obj = new JsonObject();
                obj.addProperty("time", pb.clearTimeMs());
                obj.addProperty("timeFormatted", formatTime(pb.clearTimeMs()));
                obj.addProperty("partySize", pb.partySize());
                obj.addProperty("timestamp", pb.timestamp());
                personalBests.add(tier.name(), obj);
            }
        }
        root.add("personalBests", personalBests);

        // Overall record holders (best entry per tier)
        JsonObject records = new JsonObject();
        Map<DungeonTier, DungeonLeaderboardManager.LeaderboardEntry> allTimeRecords = lb.getAllTimeRecords();
        for (Map.Entry<DungeonTier, DungeonLeaderboardManager.LeaderboardEntry> entry : allTimeRecords.entrySet()) {
            DungeonLeaderboardManager.LeaderboardEntry rec = entry.getValue();
            JsonObject obj = new JsonObject();
            obj.addProperty("uuid", rec.playerId().toString());
            obj.addProperty("name", rec.playerName());
            obj.addProperty("time", rec.clearTimeMs());
            obj.addProperty("timeFormatted", formatTime(rec.clearTimeMs()));
            obj.addProperty("partySize", rec.partySize());
            records.add(entry.getKey().name(), obj);
        }
        root.add("records", records);

        int wallet = eco.getWallet(playerUuid);
        int bank = eco.getBank(playerUuid);
        PacketDistributor.sendToPlayer(player, new ComputerDataPayload("dungeon_leaderboard_data", root.toString(), wallet, bank));
        return true;
    }

    private static String formatTime(long ms) {
        long totalSeconds = ms / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        long millis = ms % 1000;
        if (minutes > 0) {
            return String.format("%dm %d.%03ds", minutes, seconds, millis);
        }
        return String.format("%d.%03ds", seconds, millis);
    }
}

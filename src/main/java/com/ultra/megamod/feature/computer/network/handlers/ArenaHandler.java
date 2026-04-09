package com.ultra.megamod.feature.computer.network.handlers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ultra.megamod.feature.arena.ArenaManager;
import com.ultra.megamod.feature.arena.ArenaPvpManager;
import com.ultra.megamod.feature.arena.BossRushLeaderboard;
import com.ultra.megamod.feature.arena.BossRushManager;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.economy.EconomyManager;

import java.util.List;
import java.util.UUID;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public class ArenaHandler {

    public static boolean handle(ServerPlayer player, String action, String jsonData, ServerLevel level, EconomyManager eco) {
        switch (action) {
            case "arena_request": {
                sendArenaData(player, level, eco);
                return true;
            }
            case "arena_pve_start": {
                handlePveStart(player, jsonData, level, eco);
                return true;
            }
            case "arena_pvp_queue": {
                handlePvpQueue(player, level, eco);
                return true;
            }
            case "arena_boss_rush_start": {
                handleBossRushStart(player, level, eco);
                return true;
            }
            default:
                return false;
        }
    }

    private static void sendArenaData(ServerPlayer player, ServerLevel level, EconomyManager eco) {
        UUID uuid = player.getUUID();
        ServerLevel overworld = level.getServer().overworld();
        ArenaManager arenaManager = ArenaManager.get(overworld);
        ArenaManager.ArenaStats stats = arenaManager.getOrCreateStats(uuid);

        JsonObject root = new JsonObject();

        // PvE stats
        root.addProperty("bestPveWave", stats.bestPveWave);
        root.addProperty("totalPveRuns", stats.totalPveRuns);
        root.addProperty("inArena", arenaManager.isInArena(uuid));

        JsonArray recentArr = new JsonArray();
        for (int wave : stats.recentPveWaves) {
            recentArr.add(wave);
        }
        root.add("recentWaves", recentArr);

        // Challenge unlock status
        JsonObject unlocks = new JsonObject();
        for (ArenaManager.ChallengeMode cm : ArenaManager.ChallengeMode.values()) {
            unlocks.addProperty(cm.name(), arenaManager.isChallengeUnlocked(uuid, cm));
        }
        root.add("challengeUnlocks", unlocks);

        // Challenge completion status
        JsonObject completions = new JsonObject();
        completions.addProperty("STANDARD_5", stats.completed5Rounds);
        completions.addProperty("STANDARD_10", stats.completed10Rounds);
        completions.addProperty("STANDARD_15", stats.completed15Rounds);
        completions.addProperty("STANDARD_20", stats.completed20Rounds);
        completions.addProperty("NO_ARMOR", stats.completedNoArmor);
        completions.addProperty("NO_DAMAGE", stats.completedNoDamage);
        root.add("challengeCompletions", completions);

        // PvP stats
        root.addProperty("eloRating", stats.eloRating);
        root.addProperty("pvpWins", stats.pvpWins);
        root.addProperty("pvpLosses", stats.pvpLosses);
        root.addProperty("inQueue", ArenaPvpManager.isQueued(uuid));

        // Boss Rush
        root.addProperty("bossRushUnlocked", BossRushManager.hasAccess(uuid, overworld));
        String bestTimeStr = stats.bestBossRushTime > 0 ? BossRushManager.formatTime(stats.bestBossRushTime) : "N/A";
        root.addProperty("bestBossRushTime", bestTimeStr);

        // Leaderboard top 5
        BossRushLeaderboard leaderboard = BossRushLeaderboard.get(overworld);
        // Update player name for leaderboard display
        leaderboard.setPlayerName(uuid, player.getGameProfile().name());

        List<BossRushLeaderboard.LeaderboardEntry> top5 = leaderboard.getTopTimes(5);
        JsonArray lbArr = new JsonArray();
        for (BossRushLeaderboard.LeaderboardEntry entry : top5) {
            JsonObject lbObj = new JsonObject();
            lbObj.addProperty("name", entry.playerName());
            lbObj.addProperty("time", BossRushManager.formatTime(entry.timeMs()));
            lbArr.add(lbObj);
        }
        root.add("leaderboard", lbArr);

        sendResponse(player, "arena_data", root.toString(), eco);
    }

    private static void handlePveStart(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        ServerLevel overworld = level.getServer().overworld();
        ArenaManager arenaManager = ArenaManager.get(overworld);

        if (arenaManager.isInArena(player.getUUID())) {
            sendResult(player, false, "You are already in an arena.", eco);
            return;
        }

        // Parse challenge mode from JSON
        ArenaManager.ChallengeMode challengeMode = ArenaManager.ChallengeMode.STANDARD_5;
        if (jsonData != null && !jsonData.isEmpty()) {
            try {
                JsonObject obj = com.google.gson.JsonParser.parseString(jsonData).getAsJsonObject();
                if (obj.has("mode")) {
                    challengeMode = ArenaManager.ChallengeMode.fromString(obj.get("mode").getAsString());
                }
            } catch (Exception ignored) {}
        }

        if (!arenaManager.isChallengeUnlocked(player.getUUID(), challengeMode)) {
            sendResult(player, false, "Challenge not unlocked yet!", eco);
            return;
        }

        String instanceId = arenaManager.createPveArena(player, overworld, challengeMode);
        if (instanceId != null) {
            sendResult(player, true, challengeMode.displayName + " Arena started! Fight!", eco);
        } else {
            sendResult(player, false, "Failed to create arena.", eco);
        }
    }

    private static void handlePvpQueue(ServerPlayer player, ServerLevel level, EconomyManager eco) {
        UUID uuid = player.getUUID();
        ServerLevel overworld = level.getServer().overworld();
        ArenaManager arenaManager = ArenaManager.get(overworld);

        if (arenaManager.isInArena(uuid)) {
            sendResult(player, false, "You are already in an arena.", eco);
            return;
        }

        if (ArenaPvpManager.isQueued(uuid)) {
            // Dequeue
            ArenaPvpManager.dequeueFromMatch(uuid);
            sendResult(player, true, "Left PvP queue.", eco);
        } else {
            // Queue
            ArenaPvpManager.queueForMatch(uuid);
            int queueSize = ArenaPvpManager.getQueueSize();
            sendResult(player, true, "Joined PvP queue! (" + queueSize + " in queue)", eco);
        }
    }

    private static void handleBossRushStart(ServerPlayer player, ServerLevel level, EconomyManager eco) {
        UUID uuid = player.getUUID();
        ServerLevel overworld = level.getServer().overworld();
        ArenaManager arenaManager = ArenaManager.get(overworld);

        if (arenaManager.isInArena(uuid)) {
            sendResult(player, false, "You are already in an arena.", eco);
            return;
        }

        if (!BossRushManager.hasAccess(uuid, overworld)) {
            sendResult(player, false, "You must defeat all 8 bosses on INFERNAL tier first.", eco);
            return;
        }

        String instanceId = arenaManager.createBossRushArena(player, overworld);
        if (instanceId != null) {
            sendResult(player, true, "Boss Rush started! Good luck!", eco);
        } else {
            sendResult(player, false, "Failed to create Boss Rush arena.", eco);
        }
    }

    private static void sendResult(ServerPlayer player, boolean success, String message, EconomyManager eco) {
        JsonObject obj = new JsonObject();
        obj.addProperty("success", success);
        obj.addProperty("message", message);
        sendResponse(player, "arena_result", obj.toString(), eco);
    }

    private static void sendResponse(ServerPlayer player, String type, String json, EconomyManager eco) {
        int wallet = eco.getWallet(player.getUUID());
        int bank = eco.getBank(player.getUUID());
        PacketDistributor.sendToPlayer(player, new ComputerDataPayload(type, json, wallet, bank));
    }
}

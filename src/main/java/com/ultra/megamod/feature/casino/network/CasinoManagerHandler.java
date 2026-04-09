package com.ultra.megamod.feature.casino.network;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ultra.megamod.feature.casino.CasinoManager;
import com.ultra.megamod.feature.computer.admin.AdminSystem;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.economy.EconomyManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Map;
import java.util.UUID;

/**
 * Admin-only handler for casino management actions.
 * Dispatched from the main ComputerActionPayload handler.
 */
public class CasinoManagerHandler {

    public static boolean handle(ServerPlayer player, String action, String jsonData, ServerLevel level, EconomyManager eco) {
        // Admin check first
        if (!AdminSystem.isAdmin(player)) {
            return false;
        }

        switch (action) {
            case "casino_admin_request" -> {
                CasinoManager mgr = CasinoManager.get(level);

                // Calculate global stats
                Map<UUID, int[]> allStats = mgr.getAllPlayerStats();
                int globalTotalWagered = 0;
                int globalTotalWon = 0;
                int globalTotalLost = 0;
                int globalGamesPlayed = 0;
                for (int[] stats : allStats.values()) {
                    globalTotalWagered += stats[0];
                    globalTotalWon += stats[1];
                    globalTotalLost += stats[2];
                    globalGamesPlayed += stats[3];
                }

                JsonObject json = new JsonObject();
                json.addProperty("totalWagered", globalTotalWagered);
                json.addProperty("totalWon", globalTotalWon);
                json.addProperty("totalLost", globalTotalLost);
                json.addProperty("gamesPlayed", globalGamesPlayed);
                json.addProperty("houseProfit", mgr.getTotalHouseProfit());
                json.addProperty("activeGames", mgr.getActiveGamesCount());
                json.addProperty("uniquePlayers", allStats.size());

                json.addProperty("alwaysWinSlots", mgr.isAlwaysWinSlots(player.getUUID()));
                json.addProperty("alwaysWinBlackjack", mgr.isAlwaysWinBlackjack(player.getUUID()));
                json.addProperty("alwaysWinWheel", mgr.isAlwaysWinWheel(player.getUUID()));
                PacketDistributor.sendToPlayer(player, new ComputerDataPayload(
                        "casino_admin_data",
                        json.toString(),
                        eco.getWallet(player.getUUID()),
                        eco.getBank(player.getUUID())
                ));
                return true;
            }
            case "casino_admin_always_win_slots" -> {
                CasinoManager mgr = CasinoManager.get(level);
                mgr.setAlwaysWinSlots(player.getUUID(), !mgr.isAlwaysWinSlots(player.getUUID()), level);
                return sendAlwaysWinStatus(player, level, eco);
            }
            case "casino_admin_always_win_blackjack" -> {
                CasinoManager mgr = CasinoManager.get(level);
                mgr.setAlwaysWinBlackjack(player.getUUID(), !mgr.isAlwaysWinBlackjack(player.getUUID()), level);
                return sendAlwaysWinStatus(player, level, eco);
            }
            case "casino_admin_always_win_wheel" -> {
                CasinoManager mgr = CasinoManager.get(level);
                mgr.setAlwaysWinWheel(player.getUUID(), !mgr.isAlwaysWinWheel(player.getUUID()), level);
                return sendAlwaysWinStatus(player, level, eco);
            }
            case "casino_admin_always_win_roulette" -> {
                CasinoManager mgr = CasinoManager.get(level);
                mgr.setAlwaysWinRoulette(player.getUUID(), !mgr.isAlwaysWinRoulette(player.getUUID()), level);
                return sendAlwaysWinStatus(player, level, eco);
            }
            case "casino_admin_always_win_craps" -> {
                CasinoManager mgr = CasinoManager.get(level);
                mgr.setAlwaysWinCraps(player.getUUID(), !mgr.isAlwaysWinCraps(player.getUUID()), level);
                return sendAlwaysWinStatus(player, level, eco);
            }
            case "casino_admin_always_win_baccarat" -> {
                CasinoManager mgr = CasinoManager.get(level);
                mgr.setAlwaysWinBaccarat(player.getUUID(), !mgr.isAlwaysWinBaccarat(player.getUUID()), level);
                return sendAlwaysWinStatus(player, level, eco);
            }
            case "casino_admin_always_win_payout_override" -> {
                CasinoManager mgr = CasinoManager.get(level);
                mgr.setAlwaysWinPayoutOverride(player.getUUID(), !mgr.isAlwaysWinPayoutOverride(player.getUUID()), level);
                return sendAlwaysWinStatus(player, level, eco);
            }
            case "casino_admin_always_win_status" -> {
                return sendAlwaysWinStatus(player, level, eco);
            }
            case "casino_admin_player_search" -> {
                CasinoManager mgr = CasinoManager.get(level);

                // Parse search query from jsonData
                String searchName = "";
                try {
                    com.google.gson.JsonElement parsed = com.google.gson.JsonParser.parseString(jsonData);
                    if (parsed.isJsonObject()) {
                        searchName = parsed.getAsJsonObject().has("name")
                                ? parsed.getAsJsonObject().get("name").getAsString().toLowerCase()
                                : "";
                    }
                } catch (Exception ignored) {
                    // If jsonData is a plain string, use it directly
                    searchName = jsonData != null ? jsonData.toLowerCase().trim() : "";
                }

                JsonArray results = new JsonArray();
                String finalSearchName = searchName;

                // Iterate through all online players and match by name
                for (ServerPlayer onlinePlayer : level.getServer().getPlayerList().getPlayers()) {
                    String playerName = onlinePlayer.getGameProfile().name();
                    if (finalSearchName.isEmpty() || playerName.toLowerCase().contains(finalSearchName)) {
                        UUID targetId = onlinePlayer.getUUID();
                        int[] stats = mgr.getStats(targetId);

                        JsonObject entry = new JsonObject();
                        entry.addProperty("name", playerName);
                        entry.addProperty("uuid", targetId.toString());
                        entry.addProperty("totalWagered", stats[0]);
                        entry.addProperty("totalWon", stats[1]);
                        entry.addProperty("totalLost", stats[2]);
                        entry.addProperty("gamesPlayed", stats[3]);
                        entry.addProperty("biggestWin", stats[4]);
                        entry.addProperty("slotsPlayed", stats[5]);
                        entry.addProperty("slotsWon", stats[6]);
                        entry.addProperty("blackjackPlayed", stats[7]);
                        entry.addProperty("blackjackWon", stats[8]);
                        entry.addProperty("wheelPlayed", stats[9]);
                        entry.addProperty("wheelWon", stats[10]);
                        int profitLoss = stats[1] - stats[2];
                        entry.addProperty("profitLoss", profitLoss);
                        entry.addProperty("wallet", eco.getWallet(targetId));
                        entry.addProperty("bank", eco.getBank(targetId));
                        results.add(entry);
                    }
                }

                JsonObject json = new JsonObject();
                json.add("players", results);
                json.addProperty("count", results.size());

                PacketDistributor.sendToPlayer(player, new ComputerDataPayload(
                        "casino_admin_player_search",
                        json.toString(),
                        eco.getWallet(player.getUUID()),
                        eco.getBank(player.getUUID())
                ));
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    private static boolean sendAlwaysWinStatus(ServerPlayer player, ServerLevel level, EconomyManager eco) {
        CasinoManager mgr = CasinoManager.get(level);
        JsonObject json = new JsonObject();
        json.addProperty("alwaysWinSlots", mgr.isAlwaysWinSlots(player.getUUID()));
        json.addProperty("alwaysWinBlackjack", mgr.isAlwaysWinBlackjack(player.getUUID()));
        json.addProperty("alwaysWinWheel", mgr.isAlwaysWinWheel(player.getUUID()));
        json.addProperty("alwaysWinRoulette", mgr.isAlwaysWinRoulette(player.getUUID()));
        json.addProperty("alwaysWinCraps", mgr.isAlwaysWinCraps(player.getUUID()));
        json.addProperty("alwaysWinBaccarat", mgr.isAlwaysWinBaccarat(player.getUUID()));
        json.addProperty("alwaysWinPayoutOverride", mgr.isAlwaysWinPayoutOverride(player.getUUID()));
        PacketDistributor.sendToPlayer(player, new ComputerDataPayload(
                "casino_admin_always_win_status",
                json.toString(),
                eco.getWallet(player.getUUID()),
                eco.getBank(player.getUUID())
        ));
        return true;
    }
}

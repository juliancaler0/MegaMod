package com.ultra.megamod.feature.computer.network.handlers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ultra.megamod.feature.computer.admin.AdminSystem;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.economy.EconomyAnalytics;
import com.ultra.megamod.feature.economy.EconomyAnalytics.DailyAggregate;
import com.ultra.megamod.feature.economy.EconomyAnalytics.EconomySnapshot;
import com.ultra.megamod.feature.economy.EconomyAnalytics.WeeklyAggregate;
import com.ultra.megamod.feature.economy.EconomyManager;
import com.ultra.megamod.feature.economy.shop.MegaShop;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

/**
 * Network handler for the enhanced economy dashboard in the admin panel.
 * All actions require admin privileges.
 */
public class EconomyAnalyticsHandler {

    public static boolean handle(ServerPlayer player, String action, String jsonData,
                                  ServerLevel level, EconomyManager eco) {
        if (!action.startsWith("eco_dashboard_")) return false;
        if (!AdminSystem.isAdmin(player)) return false;

        switch (action) {
            case "eco_dashboard_request":
                handleOverviewRequest(player, level, eco);
                return true;
            case "eco_dashboard_players":
                handlePlayersRequest(player, jsonData, level, eco);
                return true;
            case "eco_dashboard_analytics":
                handleAnalyticsRequest(player, level, eco);
                return true;
            case "eco_dashboard_shop":
                handleShopRequest(player, level, eco);
                return true;
            case "eco_dashboard_audit":
                handleAuditRequest(player, jsonData, level, eco);
                return true;
            case "eco_dashboard_set_shop":
                handleSetShop(player, jsonData, level, eco);
                return true;
            case "eco_dashboard_refresh_shop":
                handleRefreshShop(player, level, eco);
                return true;
            case "eco_dashboard_edit_shop_item":
                handleEditShopItem(player, jsonData, level, eco);
                return true;
            case "eco_dashboard_player_detail":
                handlePlayerDetail(player, jsonData, level, eco);
                return true;
            case "eco_dashboard_bulk_action":
                handleBulkAction(player, jsonData, level, eco);
                return true;
            case "eco_mastery_request":
                handleMasteryRequest(player, level, eco);
                return true;
            case "eco_mastery_modify":
                handleMasteryModify(player, jsonData, level, eco);
                return true;
            default:
                return false;
        }
    }

    private static void handleOverviewRequest(ServerPlayer player, ServerLevel level, EconomyManager eco) {
        EconomyAnalytics analytics = EconomyAnalytics.get(level);
        JsonObject root = new JsonObject();

        // Current totals
        Map<UUID, int[]> allData = eco.getAllPlayerData();
        int totalWallets = eco.getTotalWallets();
        int totalBanks = eco.getTotalBanks();
        int totalCirculation = totalWallets + totalBanks;
        int playerCount = allData.size();

        root.addProperty("totalCirculation", totalCirculation);
        root.addProperty("totalWallets", totalWallets);
        root.addProperty("totalBanks", totalBanks);
        root.addProperty("playerCount", playerCount);

        // Gini from latest snapshot
        List<EconomySnapshot> recent = analytics.getRecentSnapshots(1);
        if (!recent.isEmpty()) {
            EconomySnapshot latest = recent.get(0);
            root.addProperty("gini", Math.round(latest.giniCoefficient * 1000.0) / 1000.0);
            root.addProperty("medianWealth", latest.medianWealth);
            root.addProperty("averageWealth", latest.averageWealth);
            root.addProperty("topTenWealth", latest.topTenPercentWealth);
            root.addProperty("activeCount", latest.activePlayerCount);
        } else {
            root.addProperty("gini", 0.0);
            root.addProperty("medianWealth", 0);
            root.addProperty("averageWealth", playerCount > 0 ? totalCirculation / playerCount : 0);
            root.addProperty("topTenWealth", 0);
            root.addProperty("activeCount", level.getServer().getPlayerList().getPlayerCount());
        }

        // Trend: compare with 24h ago snapshot
        List<EconomySnapshot> snaps = analytics.getRecentSnapshots(288);
        if (snaps.size() >= 2) {
            EconomySnapshot now = snaps.get(0);
            // Find snapshot closest to 24h ago
            long target = now.timestamp - 86400000L;
            EconomySnapshot then = snaps.get(snaps.size() - 1);
            for (EconomySnapshot s : snaps) {
                if (s.timestamp <= target) { then = s; break; }
            }
            root.addProperty("circChange24h", now.totalCoinsInCirculation - then.totalCoinsInCirculation);
            root.addProperty("playerChange24h", now.playerCount - then.playerCount);
        } else {
            root.addProperty("circChange24h", 0);
            root.addProperty("playerChange24h", 0);
        }

        root.addProperty("inflationRate", Math.round(analytics.getInflationRate() * 100.0) / 100.0);
        root.addProperty("velocity", Math.round(analytics.getCoinVelocity() * 1000.0) / 1000.0);

        // Inflow/outflow from daily aggregate
        List<DailyAggregate> dailies = analytics.getDailyAggregates(1);
        if (!dailies.isEmpty()) {
            DailyAggregate today = dailies.get(0);
            root.addProperty("todayTransactions", today.totalTransactions);
            root.addProperty("todayVolume", today.totalVolume);
            root.addProperty("todayShopRevenue", today.shopRevenue);
        } else {
            root.addProperty("todayTransactions", 0);
            root.addProperty("todayVolume", 0);
            root.addProperty("todayShopRevenue", 0);
        }

        // Transaction breakdown
        Map<String, Integer> breakdown = analytics.getTransactionBreakdown();
        JsonObject breakdownJson = new JsonObject();
        for (Map.Entry<String, Integer> entry : breakdown.entrySet()) {
            breakdownJson.addProperty(entry.getKey(), entry.getValue());
        }
        root.add("transactionBreakdown", breakdownJson);

        // Wealth distribution (10 buckets)
        int[] dist = analytics.getWealthDistribution(eco);
        JsonArray distArr = new JsonArray();
        for (int d : dist) distArr.add(d);
        root.add("wealthDistribution", distArr);

        // Online players
        root.addProperty("onlinePlayers", level.getServer().getPlayerList().getPlayerCount());

        sendResponse(player, "eco_dashboard_data", root.toString(), eco);
    }

    private static void handlePlayersRequest(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        JsonObject root = new JsonObject();
        Map<UUID, int[]> allData = eco.getAllPlayerData();

        // Parse sort/filter params
        String sortBy = "richest";
        String search = "";
        try {
            if (jsonData != null && !jsonData.isEmpty()) {
                JsonObject params = JsonParser.parseString(jsonData).getAsJsonObject();
                if (params.has("sort")) sortBy = params.get("sort").getAsString();
                if (params.has("search")) search = params.get("search").getAsString().toLowerCase();
            }
        } catch (Exception ignored) {}

        List<Map.Entry<UUID, int[]>> entries = new ArrayList<>(allData.entrySet());

        // Resolve names
        Map<UUID, String> nameCache = new HashMap<>();
        for (Map.Entry<UUID, int[]> entry : entries) {
            ServerPlayer sp = level.getServer().getPlayerList().getPlayer(entry.getKey());
            nameCache.put(entry.getKey(), sp != null ? sp.getGameProfile().name() : entry.getKey().toString().substring(0, 8));
        }

        // Filter by search
        final String searchFinal = search;
        if (!searchFinal.isEmpty()) {
            entries.removeIf(e -> !nameCache.get(e.getKey()).toLowerCase().contains(searchFinal));
        }

        // Sort
        switch (sortBy) {
            case "poorest":
                entries.sort(Comparator.comparingInt(e -> e.getValue()[0] + e.getValue()[1]));
                break;
            case "name":
                entries.sort(Comparator.comparing(e -> nameCache.get(e.getKey())));
                break;
            case "wallet":
                entries.sort((a, b) -> Integer.compare(b.getValue()[0], a.getValue()[0]));
                break;
            case "bank":
                entries.sort((a, b) -> Integer.compare(b.getValue()[1], a.getValue()[1]));
                break;
            default: // richest
                entries.sort((a, b) -> Integer.compare(b.getValue()[0] + b.getValue()[1], a.getValue()[0] + a.getValue()[1]));
                break;
        }

        // Calculate percentile thresholds
        List<Integer> allTotals = new ArrayList<>();
        for (int[] d : allData.values()) allTotals.add(d[0] + d[1]);
        Collections.sort(allTotals);
        int top10Threshold = allTotals.isEmpty() ? 0 : allTotals.get((int)(allTotals.size() * 0.9));
        int top25Threshold = allTotals.isEmpty() ? 0 : allTotals.get((int)(allTotals.size() * 0.75));
        int bottom25Threshold = allTotals.isEmpty() ? 0 : allTotals.get((int)(allTotals.size() * 0.25));

        JsonArray playersArr = new JsonArray();
        Set<UUID> onlineSet = new HashSet<>();
        for (ServerPlayer sp : level.getServer().getPlayerList().getPlayers()) {
            onlineSet.add(sp.getUUID());
        }

        for (Map.Entry<UUID, int[]> entry : entries) {
            JsonObject pObj = new JsonObject();
            UUID uuid = entry.getKey();
            int wallet = entry.getValue()[0];
            int bank = entry.getValue()[1];
            int total = wallet + bank;
            String name = nameCache.get(uuid);

            pObj.addProperty("name", name);
            pObj.addProperty("uuid", uuid.toString());
            pObj.addProperty("wallet", wallet);
            pObj.addProperty("bank", bank);
            pObj.addProperty("total", total);
            pObj.addProperty("online", onlineSet.contains(uuid));

            // Tier: 0=top10, 1=top25, 2=normal, 3=bottom25
            if (total >= top10Threshold && top10Threshold > 0) pObj.addProperty("tier", 0);
            else if (total >= top25Threshold && top25Threshold > 0) pObj.addProperty("tier", 1);
            else if (total <= bottom25Threshold) pObj.addProperty("tier", 3);
            else pObj.addProperty("tier", 2);

            playersArr.add(pObj);
        }

        root.add("players", playersArr);
        root.addProperty("totalPlayers", allData.size());

        sendResponse(player, "eco_players_data", root.toString(), eco);
    }

    private static void handleAnalyticsRequest(ServerPlayer player, ServerLevel level, EconomyManager eco) {
        EconomyAnalytics analytics = EconomyAnalytics.get(level);
        JsonObject root = new JsonObject();

        // Recent snapshots (last 24h) for charts
        List<EconomySnapshot> snaps = analytics.getRecentSnapshots(288);
        JsonArray snapsArr = new JsonArray();
        for (EconomySnapshot s : snaps) {
            JsonObject sObj = new JsonObject();
            sObj.addProperty("ts", s.timestamp);
            sObj.addProperty("circ", s.totalCoinsInCirculation);
            sObj.addProperty("wallets", s.totalWalletCoins);
            sObj.addProperty("banks", s.totalBankCoins);
            sObj.addProperty("txVol", s.totalTransactionVolume);
            sObj.addProperty("txCount", s.transactionCount);
            sObj.addProperty("active", s.activePlayerCount);
            sObj.addProperty("gini", Math.round(s.giniCoefficient * 1000.0) / 1000.0);
            sObj.addProperty("shopRev", s.shopRevenue);
            snapsArr.add(sObj);
        }
        root.add("snapshots", snapsArr);

        // Daily aggregates
        List<DailyAggregate> dailies = analytics.getDailyAggregates(30);
        JsonArray dailyArr = new JsonArray();
        for (DailyAggregate d : dailies) {
            JsonObject dObj = new JsonObject();
            dObj.addProperty("date", d.date);
            dObj.addProperty("peak", d.peakCirculation);
            dObj.addProperty("min", d.minCirculation == Integer.MAX_VALUE ? 0 : d.minCirculation);
            dObj.addProperty("avg", d.avgCirculation);
            dObj.addProperty("txCount", d.totalTransactions);
            dObj.addProperty("txVol", d.totalVolume);
            dObj.addProperty("active", d.activeCount);
            dObj.addProperty("shopRev", d.shopRevenue);
            dObj.addProperty("gini", Math.round(d.avgGini * 1000.0) / 1000.0);
            dailyArr.add(dObj);
        }
        root.add("daily", dailyArr);

        // Weekly aggregates
        List<WeeklyAggregate> weeklies = analytics.getWeeklyAggregates(12);
        JsonArray weeklyArr = new JsonArray();
        for (WeeklyAggregate w : weeklies) {
            JsonObject wObj = new JsonObject();
            wObj.addProperty("week", w.weekKey);
            wObj.addProperty("peak", w.peakCirculation);
            wObj.addProperty("min", w.minCirculation == Integer.MAX_VALUE ? 0 : w.minCirculation);
            wObj.addProperty("avg", w.avgCirculation);
            wObj.addProperty("txCount", w.totalTransactions);
            wObj.addProperty("txVol", w.totalVolume);
            wObj.addProperty("active", w.activeCount);
            wObj.addProperty("shopRev", w.shopRevenue);
            wObj.addProperty("gini", Math.round(w.avgGini * 1000.0) / 1000.0);
            weeklyArr.add(wObj);
        }
        root.add("weekly", weeklyArr);

        // Top 10 earners
        List<String[]> topEarners = analytics.getTopEarners(10, eco, level);
        JsonArray topArr = new JsonArray();
        for (String[] te : topEarners) {
            JsonObject tObj = new JsonObject();
            tObj.addProperty("name", te[0]);
            tObj.addProperty("uuid", te[1]);
            tObj.addProperty("wallet", Integer.parseInt(te[2]));
            tObj.addProperty("bank", Integer.parseInt(te[3]));
            tObj.addProperty("total", Integer.parseInt(te[4]));
            topArr.add(tObj);
        }
        root.add("topEarners", topArr);

        // Wealth distribution histogram
        int[] dist = analytics.getWealthDistribution(eco);
        JsonArray distArr = new JsonArray();
        for (int d : dist) distArr.add(d);
        root.add("wealthDistribution", distArr);

        // Transaction breakdown by type
        Map<String, Integer> breakdown = analytics.getTransactionBreakdown();
        JsonObject breakdownJson = new JsonObject();
        for (Map.Entry<String, Integer> entry : breakdown.entrySet()) {
            breakdownJson.addProperty(entry.getKey(), entry.getValue());
        }
        root.add("transactionBreakdown", breakdownJson);

        sendResponse(player, "eco_analytics_data", root.toString(), eco);
    }

    private static void handleShopRequest(ServerPlayer player, ServerLevel level, EconomyManager eco) {
        JsonObject root = new JsonObject();

        try {
            MegaShop shop = MegaShop.get(level);
            root.addProperty("intervalTicks", shop.getRefreshIntervalTicks());
            root.addProperty("priceMult", shop.getGlobalPriceMultiplier());
            root.addProperty("sellPct", shop.getSellPercentage());

            // Today's shop items
            String itemsJson = shop.getTodaysItemsJson();
            root.addProperty("todaysItems", itemsJson);
        } catch (Exception e) {
            root.addProperty("intervalTicks", 24000);
            root.addProperty("priceMult", 1.0);
            root.addProperty("sellPct", 0.2);
            root.addProperty("todaysItems", "[]");
        }

        // Shop revenue from analytics
        EconomyAnalytics analytics = EconomyAnalytics.get(level);
        List<DailyAggregate> dailies = analytics.getDailyAggregates(7);
        int todayRevenue = 0;
        int weekRevenue = 0;
        for (int i = 0; i < dailies.size(); i++) {
            weekRevenue += dailies.get(i).shopRevenue;
            if (i == 0) todayRevenue = dailies.get(i).shopRevenue;
        }
        root.addProperty("todayRevenue", todayRevenue);
        root.addProperty("weekRevenue", weekRevenue);

        sendResponse(player, "eco_shop_data", root.toString(), eco);
    }

    private static void handleAuditRequest(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        JsonObject root = new JsonObject();

        // Parse filters
        String typeFilter = "ALL";
        String playerFilter = "";
        int minAmount = 0;
        int maxAmount = Integer.MAX_VALUE;
        try {
            if (jsonData != null && !jsonData.isEmpty()) {
                JsonObject params = JsonParser.parseString(jsonData).getAsJsonObject();
                if (params.has("type")) typeFilter = params.get("type").getAsString();
                if (params.has("player")) playerFilter = params.get("player").getAsString().toLowerCase();
                if (params.has("minAmount")) minAmount = params.get("minAmount").getAsInt();
                if (params.has("maxAmount")) maxAmount = params.get("maxAmount").getAsInt();
            }
        } catch (Exception ignored) {}

        List<EconomyManager.AuditEntry> log = eco.getAuditLog();
        JsonArray entries = new JsonArray();

        int totalAdminMods = 0;
        int largestTx = 0;
        Map<String, Integer> modCounts = new HashMap<>();

        for (EconomyManager.AuditEntry entry : log) {
            boolean matchesType = "ALL".equals(typeFilter) || entry.type().equalsIgnoreCase(typeFilter);
            boolean matchesPlayer = playerFilter.isEmpty() || entry.playerName().toLowerCase().contains(playerFilter);
            boolean matchesAmount = Math.abs(entry.amount()) >= minAmount && Math.abs(entry.amount()) <= maxAmount;

            // Track stats regardless of filters
            if (entry.type().startsWith("ADMIN")) {
                totalAdminMods++;
                modCounts.merge(entry.playerName(), 1, Integer::sum);
            }
            largestTx = Math.max(largestTx, Math.abs(entry.amount()));

            if (matchesType && matchesPlayer && matchesAmount) {
                JsonObject eObj = new JsonObject();
                eObj.addProperty("timestamp", entry.timestamp());
                eObj.addProperty("player", entry.playerName());
                eObj.addProperty("type", entry.type());
                eObj.addProperty("amount", entry.amount());
                eObj.addProperty("description", entry.description());
                entries.add(eObj);
            }
        }

        root.add("entries", entries);
        root.addProperty("totalEntries", log.size());
        root.addProperty("filteredEntries", entries.size());
        root.addProperty("totalAdminMods", totalAdminMods);
        root.addProperty("largestTransaction", largestTx);

        // Most modified player
        String mostModified = "";
        int maxMods = 0;
        for (Map.Entry<String, Integer> e : modCounts.entrySet()) {
            if (e.getValue() > maxMods) {
                maxMods = e.getValue();
                mostModified = e.getKey();
            }
        }
        root.addProperty("mostModifiedPlayer", mostModified);
        root.addProperty("mostModifiedCount", maxMods);

        sendResponse(player, "eco_audit_data", root.toString(), eco);
    }

    private static void handleSetShop(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject params = JsonParser.parseString(jsonData).getAsJsonObject();
            MegaShop shop = MegaShop.get(level);

            if (params.has("intervalTicks")) {
                shop.setRefreshInterval(params.get("intervalTicks").getAsInt());
            }
            if (params.has("priceMult")) {
                shop.setGlobalPriceMultiplier(params.get("priceMult").getAsDouble());
            }
            if (params.has("sellPct")) {
                shop.setSellPercentage(params.get("sellPct").getAsDouble());
            }
            shop.saveToDisk(level);
        } catch (Exception ignored) {}

        // Send updated shop data back
        handleShopRequest(player, level, eco);
    }

    private static void handleRefreshShop(ServerPlayer player, ServerLevel level, EconomyManager eco) {
        try {
            MegaShop shop = MegaShop.get(level);
            shop.forceRefresh();
            shop.saveToDisk(level);
        } catch (Exception ignored) {}
        handleShopRequest(player, level, eco);
    }

    private static void handleEditShopItem(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject params = JsonParser.parseString(jsonData).getAsJsonObject();
            int slot = params.get("slot").getAsInt();
            String op = params.get("op").getAsString();

            MegaShop shop = MegaShop.get(level);
            java.util.List<com.ultra.megamod.feature.economy.shop.ShopItem> items = shop.getTodaysItems(level);
            if (slot < 0 || slot >= items.size()) return;

            com.ultra.megamod.feature.economy.shop.ShopItem item = items.get(slot);
            int newBuy = item.buyPrice();
            switch (op) {
                case "down": newBuy = Math.max(1, newBuy - (newBuy > 100 ? 50 : (newBuy > 10 ? 5 : 1))); break;
                case "up": newBuy = newBuy + (newBuy >= 100 ? 50 : (newBuy >= 10 ? 5 : 1)); break;
                case "double": newBuy = newBuy * 2; break;
            }
            int newSell = Math.max(1, (int)(newBuy * shop.getSellPercentage()));
            shop.setManualOverride(slot, new com.ultra.megamod.feature.economy.shop.ShopItem(
                    item.itemId(), item.displayName(), newBuy, newSell));
            shop.saveToDisk(level);
        } catch (Exception ignored) {}
        handleShopRequest(player, level, eco);
    }

    private static void handlePlayerDetail(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        JsonObject root = new JsonObject();
        try {
            UUID targetUuid = UUID.fromString(jsonData.trim());
            int wallet = eco.getWallet(targetUuid);
            int bank = eco.getBank(targetUuid);

            ServerPlayer target = level.getServer().getPlayerList().getPlayer(targetUuid);
            String name = target != null ? target.getGameProfile().name() : targetUuid.toString().substring(0, 8);

            root.addProperty("name", name);
            root.addProperty("uuid", targetUuid.toString());
            root.addProperty("wallet", wallet);
            root.addProperty("bank", bank);
            root.addProperty("total", wallet + bank);
            root.addProperty("online", target != null);

            // Get audit log entries for this player
            JsonArray auditArr = new JsonArray();
            for (EconomyManager.AuditEntry entry : eco.getAuditLog()) {
                if (entry.playerName().equalsIgnoreCase(name)) {
                    JsonObject eObj = new JsonObject();
                    eObj.addProperty("timestamp", entry.timestamp());
                    eObj.addProperty("type", entry.type());
                    eObj.addProperty("amount", entry.amount());
                    eObj.addProperty("description", entry.description());
                    auditArr.add(eObj);
                }
            }
            root.add("auditLog", auditArr);

        } catch (Exception e) {
            root.addProperty("error", "Invalid UUID");
        }

        sendResponse(player, "eco_dashboard_data", root.toString(), eco);
    }

    private static void handleBulkAction(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject params = JsonParser.parseString(jsonData).getAsJsonObject();
            String actionType = params.get("action").getAsString();
            int amount = params.has("amount") ? params.get("amount").getAsInt() : 0;
            String target = params.has("target") ? params.get("target").getAsString() : "wallet";

            Map<UUID, int[]> allData = eco.getAllPlayerData();

            switch (actionType) {
                case "give_all": {
                    for (UUID uuid : allData.keySet()) {
                        if ("wallet".equals(target)) {
                            eco.setWallet(uuid, eco.getWallet(uuid) + amount);
                        } else {
                            eco.setBank(uuid, eco.getBank(uuid) + amount);
                        }
                    }
                    eco.addAuditEntry("ADMIN", "ADMIN_BULK_GIVE", amount, "Give " + amount + " to all " + target + "s");
                    break;
                }
                case "take_all": {
                    for (UUID uuid : allData.keySet()) {
                        if ("wallet".equals(target)) {
                            eco.setWallet(uuid, Math.max(0, eco.getWallet(uuid) - amount));
                        } else {
                            eco.setBank(uuid, Math.max(0, eco.getBank(uuid) - amount));
                        }
                    }
                    eco.addAuditEntry("ADMIN", "ADMIN_BULK_TAKE", amount, "Take " + amount + " from all " + target + "s");
                    break;
                }
                case "reset_player": {
                    if (params.has("uuid")) {
                        UUID targetUuid = UUID.fromString(params.get("uuid").getAsString());
                        eco.setWallet(targetUuid, 0);
                        eco.setBank(targetUuid, 0);
                        eco.addAuditEntry("ADMIN", "ADMIN_RESET", 0, "Reset player economy data");
                    }
                    break;
                }
                case "set_player": {
                    if (params.has("uuid")) {
                        UUID targetUuid = UUID.fromString(params.get("uuid").getAsString());
                        if ("wallet".equals(target)) {
                            eco.setWallet(targetUuid, amount);
                        } else {
                            eco.setBank(targetUuid, amount);
                        }
                        eco.addAuditEntry("ADMIN", "ADMIN_SET", amount, "Set " + target + " to " + amount);
                    }
                    break;
                }
            }

            eco.saveToDisk(level);
            EconomyAnalytics.recordTransaction(EconomyAnalytics.ADMIN, amount);
        } catch (Exception ignored) {}

        // Send updated overview
        handleOverviewRequest(player, level, eco);
    }

    private static void handleMasteryRequest(ServerPlayer player, ServerLevel level, EconomyManager eco) {
        ServerLevel overworld = player.level().getServer().overworld();
        com.ultra.megamod.feature.prestige.MasteryMarkManager marks = com.ultra.megamod.feature.prestige.MasteryMarkManager.get(overworld);
        com.ultra.megamod.feature.skills.prestige.PrestigeManager prestige = com.ultra.megamod.feature.skills.prestige.PrestigeManager.get(overworld);

        JsonObject root = new JsonObject();
        int totalMarks = 0;
        JsonArray players = new JsonArray();

        for (ServerPlayer sp : player.level().getServer().getPlayerList().getPlayers()) {
            UUID uuid = sp.getUUID();
            int m = marks.getMarks(uuid);
            int p = prestige.getTotalPrestige(uuid);
            totalMarks += m;

            JsonObject pe = new JsonObject();
            pe.addProperty("name", sp.getGameProfile().name());
            pe.addProperty("uuid", uuid.toString());
            pe.addProperty("marks", m);
            pe.addProperty("prestige", p);
            players.add(pe);
        }

        root.addProperty("total_marks", totalMarks);
        root.addProperty("player_count", players.size());
        root.add("players", players);
        sendResponse(player, "eco_mastery_data", root.toString(), eco);
    }

    private static void handleMasteryModify(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = com.google.gson.JsonParser.parseString(jsonData).getAsJsonObject();
            String playerName = obj.get("player").getAsString();
            int amount = obj.get("amount").getAsInt();
            String action = obj.get("action").getAsString();

            ServerLevel overworld = player.level().getServer().overworld();
            com.ultra.megamod.feature.prestige.MasteryMarkManager marks = com.ultra.megamod.feature.prestige.MasteryMarkManager.get(overworld);

            // Find target player
            ServerPlayer target = player.level().getServer().getPlayerList().getPlayerByName(playerName);
            if (target == null) {
                sendResponse(player, "eco_mastery_result", "{\"msg\":\"Player not found\"}", eco);
                return;
            }

            if ("grant".equals(action)) {
                marks.addMarks(target.getUUID(), amount);
                marks.saveToDisk(overworld);
                target.sendSystemMessage(net.minecraft.network.chat.Component.literal("+" + amount + " Marks of Mastery (admin grant)").withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE));
                sendResponse(player, "eco_mastery_result", "{\"msg\":\"Granted " + amount + " marks to " + playerName + "\"}", eco);
            } else if ("revoke".equals(action)) {
                int current = marks.getMarks(target.getUUID());
                int toRemove = Math.min(amount, current);
                marks.spendMarks(target.getUUID(), toRemove);
                marks.saveToDisk(overworld);
                sendResponse(player, "eco_mastery_result", "{\"msg\":\"Revoked " + toRemove + " marks from " + playerName + "\"}", eco);
            }
        } catch (Exception e) {
            sendResponse(player, "eco_mastery_result", "{\"msg\":\"Error: " + e.getMessage() + "\"}", eco);
        }
    }

    private static void sendResponse(ServerPlayer player, String dataType, String json, EconomyManager eco) {
        int wallet = eco.getWallet(player.getUUID());
        int bank = eco.getBank(player.getUUID());
        PacketDistributor.sendToPlayer(player, (CustomPacketPayload) new ComputerDataPayload(dataType, json, wallet, bank));
    }
}

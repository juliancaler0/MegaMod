package com.ultra.megamod.feature.computer.network.handlers;

import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.economy.EconomyManager;
import com.ultra.megamod.feature.multiplayer.PlayerStatistics;
import com.ultra.megamod.feature.museum.MuseumData;
import com.ultra.megamod.feature.museum.catalog.AchievementCatalog;
import com.ultra.megamod.feature.museum.catalog.AquariumCatalog;
import com.ultra.megamod.feature.museum.catalog.ArtCatalog;
import com.ultra.megamod.feature.museum.catalog.ItemCatalog;
import com.ultra.megamod.feature.museum.catalog.WildlifeCatalog;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class LeaderboardHandler {

    public static boolean handle(ServerPlayer player, String action, String jsonData,
                                  ServerLevel level, EconomyManager eco) {
        if (!"leaderboard_request".equals(action)) {
            return false;
        }

        String category = jsonData != null ? jsonData.trim() : "Wealth";
        if (category.isEmpty()) {
            category = "Wealth";
        }

        // Gather all known player UUIDs from multiple sources
        Set<UUID> allUUIDs = new HashSet<>();

        // From economy
        Map<UUID, int[]> ecoData = eco.getAllPlayerData();
        allUUIDs.addAll(ecoData.keySet());

        // From online players
        for (ServerPlayer sp : level.getServer().getPlayerList().getPlayers()) {
            allUUIDs.add(sp.getUUID());
        }

        // Skills UUID source removed — old SkillManager deleted

        // Resolve names
        Map<UUID, String> nameCache = new HashMap<>();
        for (UUID uuid : allUUIDs) {
            String name = resolvePlayerName(level, uuid);
            if (name != null) {
                nameCache.put(uuid, name);
            }
        }

        // Build entries based on category
        List<RawEntry> rawEntries = new ArrayList<>();
        NumberFormat nf = NumberFormat.getIntegerInstance(Locale.US);

        switch (category) {
            case "Wealth": {
                for (UUID uuid : allUUIDs) {
                    String name = nameCache.get(uuid);
                    if (name == null) continue;
                    int wallet = eco.getWallet(uuid);
                    int bank = eco.getBank(uuid);
                    long total = wallet + bank;
                    rawEntries.add(new RawEntry(name, uuid.toString(), total, nf.format(total) + " MC"));
                }
                rawEntries.sort(Comparator.comparingLong(RawEntry::score).reversed());
                break;
            }
            case "Kills": {
                PlayerStatistics stats = PlayerStatistics.get(level);
                for (UUID uuid : allUUIDs) {
                    String name = nameCache.get(uuid);
                    if (name == null) continue;
                    int kills = stats.getStat(uuid, PlayerStatistics.KILLS);
                    rawEntries.add(new RawEntry(name, uuid.toString(), kills, nf.format(kills) + " kills"));
                }
                rawEntries.sort(Comparator.comparingLong(RawEntry::score).reversed());
                break;
            }
            case "Dungeons": {
                Map<String, Integer> completionsByUUID = new HashMap<>();
                List<DungeonAnalyticsHandler.RunRecord> history = DungeonAnalyticsHandler.getRunHistory();
                for (DungeonAnalyticsHandler.RunRecord run : history) {
                    if ("Completed".equals(run.result())) {
                        completionsByUUID.merge(run.playerUUID(), 1, Integer::sum);
                    }
                }
                for (UUID uuid : allUUIDs) {
                    String name = nameCache.get(uuid);
                    if (name == null) continue;
                    int clears = completionsByUUID.getOrDefault(uuid.toString(), 0);
                    rawEntries.add(new RawEntry(name, uuid.toString(), clears, nf.format(clears) + " clears"));
                }
                rawEntries.sort(Comparator.comparingLong(RawEntry::score).reversed());
                break;
            }
            case "Museum": {
                MuseumData museum = MuseumData.get(level);
                int totalCatalog = AquariumCatalog.getTotalCount() + WildlifeCatalog.getTotalCount()
                        + ArtCatalog.getTotalCount() + AchievementCatalog.getTotalCount()
                        + ItemCatalog.getTotalItemCount();

                for (UUID uuid : allUUIDs) {
                    String name = nameCache.get(uuid);
                    if (name == null) continue;
                    int donated = countMuseumDonations(museum, uuid);
                    int pct = totalCatalog > 0 ? donated * 100 / totalCatalog : 0;
                    rawEntries.add(new RawEntry(name, uuid.toString(), pct, pct + "%"));
                }
                rawEntries.sort(Comparator.comparingLong(RawEntry::score).reversed());
                break;
            }
            case "Skills": {
                // Sum Pufferfish category levels across all categories per player; only online
                // players contribute because Experience needs a live ServerPlayer handle.
                var cats = com.ultra.megamod.feature.skills.adminbridge.SkillAdminBridge.allCategoryIds();
                for (UUID uuid : allUUIDs) {
                    String name = nameCache.get(uuid);
                    if (name == null) continue;
                    var sp = level.getServer().getPlayerList().getPlayer(uuid);
                    if (sp == null) continue;
                    int total = 0;
                    for (var catId : cats) {
                        total += com.ultra.megamod.lib.pufferfish_skills.api.SkillsAPI.getCategory(catId)
                                .flatMap(cat -> cat.getExperience())
                                .map(exp -> exp.getLevel(sp))
                                .orElse(0);
                    }
                    if (total > 0) {
                        rawEntries.add(new RawEntry(name, uuid.toString(), total, "Lv." + total));
                    }
                }
                rawEntries.sort(Comparator.comparingLong(RawEntry::score).reversed());
                break;
            }
            case "Deaths": {
                PlayerStatistics stats = PlayerStatistics.get(level);
                for (UUID uuid : allUUIDs) {
                    String name = nameCache.get(uuid);
                    if (name == null) continue;
                    int deaths = stats.getStat(uuid, PlayerStatistics.DEATHS);
                    rawEntries.add(new RawEntry(name, uuid.toString(), deaths, nf.format(deaths) + " deaths"));
                }
                // Fewer deaths = better rank (ascending)
                rawEntries.sort(Comparator.comparingLong(RawEntry::score));
                break;
            }
            case "Games": {
                var scoreMgr = com.ultra.megamod.feature.computer.minigames.MinigameScoreManager.get(level);
                allUUIDs.addAll(scoreMgr.getAllScores().keySet());
                // Re-resolve names for new UUIDs
                for (UUID uuid : scoreMgr.getAllScores().keySet()) {
                    if (!nameCache.containsKey(uuid)) {
                        String name = resolvePlayerName(level, uuid);
                        if (name != null) nameCache.put(uuid, name);
                    }
                }
                for (UUID uuid : allUUIDs) {
                    String name = nameCache.get(uuid);
                    if (name == null) continue;
                    int combined = scoreMgr.getCombinedScore(uuid);
                    if (combined <= 0) continue;
                    rawEntries.add(new RawEntry(name, uuid.toString(), combined, nf.format(combined) + " pts"));
                }
                rawEntries.sort(Comparator.comparingLong(RawEntry::score).reversed());
                break;
            }
            case "Factions": {
                // Faction war leaderboard — shows factions, not individual players
                var factionStats = com.ultra.megamod.feature.citizen.data.FactionStatsManager.get(level);
                var factionMgr = com.ultra.megamod.feature.citizen.data.FactionManager.get(level);
                var leaderboard = factionStats.getWarLeaderboard();
                for (var entry : leaderboard) {
                    String factionId = entry.getKey();
                    int score = entry.getValue();
                    String displayName = factionId;
                    for (var f : factionMgr.getAllFactions()) {
                        if (f.getFactionId().equals(factionId)) { displayName = f.getDisplayName(); break; }
                    }
                    var s = factionStats.getStats(factionId);
                    String detail = score + " pts (W:" + s.siegeWins + " L:" + s.siegeLosses + " D:" + s.raidsDefended + ")";
                    rawEntries.add(new RawEntry(displayName, factionId, score, detail));
                }
                // Already sorted by getWarLeaderboard
                break;
            }
            default: {
                // Unknown category, treat as Wealth
                for (UUID uuid : allUUIDs) {
                    String name = nameCache.get(uuid);
                    if (name == null) continue;
                    int wallet = eco.getWallet(uuid);
                    int bank = eco.getBank(uuid);
                    long total = wallet + bank;
                    rawEntries.add(new RawEntry(name, uuid.toString(), total, nf.format(total) + " MC"));
                }
                rawEntries.sort(Comparator.comparingLong(RawEntry::score).reversed());
                break;
            }
        }

        // Find the requesting player's rank
        String playerUUID = player.getUUID().toString();
        int myRank = -1;
        for (int i = 0; i < rawEntries.size(); i++) {
            if (rawEntries.get(i).uuid().equals(playerUUID)) {
                myRank = i + 1;
                break;
            }
        }

        int totalPlayers = rawEntries.size();

        // Build JSON response
        StringBuilder sb = new StringBuilder(2048);
        sb.append("{");
        sb.append("\"category\":\"").append(escapeJson(category)).append("\",");
        sb.append("\"myRank\":").append(myRank).append(",");
        sb.append("\"totalPlayers\":").append(totalPlayers).append(",");
        sb.append("\"entries\":[");

        for (int i = 0; i < rawEntries.size(); i++) {
            if (i > 0) sb.append(",");
            RawEntry e = rawEntries.get(i);
            sb.append("{");
            sb.append("\"name\":\"").append(escapeJson(e.name())).append("\",");
            sb.append("\"uuid\":\"").append(escapeJson(e.uuid())).append("\",");
            sb.append("\"score\":").append(e.score()).append(",");
            sb.append("\"formatted\":\"").append(escapeJson(e.formatted())).append("\"");
            sb.append("}");
        }

        sb.append("]}");

        sendResponse(player, "leaderboard_data", sb.toString(), eco);
        return true;
    }

    private static int countMuseumDonations(MuseumData museum, UUID uuid) {
        Set<String> donatedItems = museum.getDonatedItems(uuid);
        Set<String> mobs = museum.getDonatedMobs(uuid);

        // Count aquarium entries
        int aqDonated = 0;
        for (AquariumCatalog.MobEntry e : AquariumCatalog.ENTRIES) {
            if (mobs.contains(e.entityId())) aqDonated++;
        }

        // Count wildlife entries
        int wlDonated = 0;
        for (WildlifeCatalog.MobEntry e : WildlifeCatalog.ENTRIES) {
            if (mobs.contains(e.entityId())) wlDonated++;
        }

        // Count art entries
        int artDonated = 0;
        for (ArtCatalog.ArtEntry e : ArtCatalog.ENTRIES) {
            if (museum.getDonatedArt(uuid).contains(e.id())) artDonated++;
        }

        // Count achievement entries
        int achDonated = 0;
        for (AchievementCatalog.AchievementEntry e : AchievementCatalog.ENTRIES) {
            if (museum.getCompletedAchievements(uuid).contains(e.advancementId())) achDonated++;
        }

        // Count unique catalog items
        HashSet<String> uniqueCatalogItems = new HashSet<>();
        for (java.util.List<String> catItems : ItemCatalog.ITEMS_BY_CATEGORY.values()) {
            uniqueCatalogItems.addAll(catItems);
        }
        int itemDonated = 0;
        for (String item : uniqueCatalogItems) {
            if (donatedItems.contains(item)) itemDonated++;
        }

        return aqDonated + wlDonated + artDonated + achDonated + itemDonated;
    }

    private static String resolvePlayerName(ServerLevel level, UUID uuid) {
        // Check online players first
        ServerPlayer online = level.getServer().getPlayerList().getPlayer(uuid);
        if (online != null) {
            return online.getGameProfile().name();
        }
        // Profile cache API removed in 1.21.11 - offline player names unavailable
        return null;
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static void sendResponse(ServerPlayer player, String type, String json, EconomyManager eco) {
        int wallet = eco.getWallet(player.getUUID());
        int bank = eco.getBank(player.getUUID());
        PacketDistributor.sendToPlayer(
                (ServerPlayer) player,
                (CustomPacketPayload) new ComputerDataPayload(type, json, wallet, bank),
                (CustomPacketPayload[]) new CustomPacketPayload[0]);
    }

    private record RawEntry(String name, String uuid, long score, String formatted) {}
}

package com.ultra.megamod.feature.computer.network.handlers;

import com.ultra.megamod.feature.computer.admin.AdminSystem;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.dungeons.DungeonManager;
import com.ultra.megamod.feature.dungeons.DungeonTier;
import com.ultra.megamod.feature.dungeons.loot.LootQuality;
import com.ultra.megamod.feature.economy.EconomyManager;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DungeonAnalyticsHandler {

    // Persistent analytics data (static, survives across requests)
    private static final List<RunRecord> runHistory = Collections.synchronizedList(new ArrayList<>());
    private static final Map<String, Map<String, Map<String, Integer>>> bossKillCounts = new HashMap<>();
    // bossKillCounts: playerName -> (bossName -> (tier -> count))
    private static final int[] lootDropCounts = new int[5]; // Common, Uncommon, Rare, Epic, Legendary
    private static final int MAX_HISTORY = 500;

    public record RunRecord(String playerName, String playerUUID, String tier, String theme,
                            String result, long startMs, long endMs, String bossKilled) {
        public int durationSeconds() {
            long diff = endMs - startMs;
            if (diff <= 0) return 0;
            return (int) (diff / 1000);
        }
    }

    /**
     * Record a completed dungeon run. Call from dungeon completion/death/extract events.
     */
    public static void recordRun(String playerName, String uuid, String tier, String theme,
                                  String result, long startMs, long endMs, String boss) {
        RunRecord record = new RunRecord(playerName, uuid, tier, theme, result, startMs, endMs,
                boss != null ? boss : "");
        runHistory.add(0, record); // newest first
        if (runHistory.size() > MAX_HISTORY) {
            runHistory.remove(runHistory.size() - 1);
        }
    }

    /**
     * Record a boss kill. Call when a dungeon boss dies.
     */
    public static void recordBossKill(String playerName, String boss, String tier) {
        bossKillCounts
                .computeIfAbsent(playerName, k -> new HashMap<>())
                .computeIfAbsent(boss, k -> new HashMap<>())
                .merge(tier, 1, Integer::sum);
    }

    /**
     * Get the run history list for leaderboard aggregation.
     */
    public static List<RunRecord> getRunHistory() {
        return Collections.unmodifiableList(runHistory);
    }

    /**
     * Record a loot drop by rarity index (0=Common, 1=Uncommon, 2=Rare, 3=Epic, 4=Legendary).
     */
    public static void recordLootDrop(int rarityIndex) {
        if (rarityIndex >= 0 && rarityIndex < 5) {
            lootDropCounts[rarityIndex]++;
        }
    }

    /**
     * Handle an action from the admin computer client. Returns true if the action was handled.
     */
    public static boolean handle(ServerPlayer player, String action, String jsonData,
                                  ServerLevel level, EconomyManager eco) {
        if (!AdminSystem.isAdmin(player)) {
            return false;
        }
        switch (action) {
            case "dungeon_analytics_request": {
                String json = buildAnalyticsJson(level);
                sendResponse(player, "dungeon_analytics_data", json, eco);
                return true;
            }
            case "dungeon_analytics_force_extract": {
                handleForceExtract(player, jsonData, level, eco);
                return true;
            }
            case "dungeon_analytics_wipe": {
                runHistory.clear();
                bossKillCounts.clear();
                for (int i = 0; i < 5; i++) lootDropCounts[i] = 0;
                String wipeJson = buildAnalyticsJson(level);
                sendResponse(player, "dungeon_analytics_data", wipeJson, eco);
                return true;
            }
            case "dungeon_loot_quality_set": {
                handleLootQualitySet(jsonData);
                // Send updated analytics back so panel refreshes
                String json = buildAnalyticsJson(level);
                sendResponse(player, "dungeon_analytics_data", json, eco);
                return true;
            }
            default:
                return false;
        }
    }

    private static void handleForceExtract(ServerPlayer admin, String instanceId,
                                            ServerLevel level, EconomyManager eco) {
        try {
            DungeonManager dm = DungeonManager.get(level);
            DungeonManager.DungeonInstance instance = dm.getInstance(instanceId);
            if (instance != null) {
                ServerPlayer target = level.getServer().getPlayerList().getPlayer(instance.playerUUID);
                if (target != null) {
                    dm.removePlayerFromDungeon(target);
                }
            }
        } catch (Exception e) {
            // Silently handle
        }
        // Send updated analytics data back
        String json = buildAnalyticsJson(level);
        sendResponse(admin, "dungeon_analytics_data", json, eco);
    }

    private static String buildAnalyticsJson(ServerLevel level) {
        StringBuilder sb = new StringBuilder(2048);
        sb.append("{");

        // Calculate summary stats from run history
        int totalRuns = runHistory.size();
        int totalCompletions = 0;
        int totalDeaths = 0;
        int totalBossKillCount = 0;
        long totalClearTimeSec = 0;
        int clearCount = 0;

        // Per-tier counts
        int[] tRuns = new int[6];
        int[] tCompletions = new int[6];
        int[] tDeaths = new int[6];
        int[] tBossKills = new int[6];
        long[] tTotalTime = new long[6];
        int[] tClearCount = new int[6];

        for (RunRecord run : runHistory) {
            int tierIdx = tierIndex(run.tier());

            if (tierIdx >= 0 && tierIdx < 6) {
                tRuns[tierIdx]++;
            }

            if ("Completed".equals(run.result())) {
                totalCompletions++;
                int dur = run.durationSeconds();
                totalClearTimeSec += dur;
                clearCount++;
                if (tierIdx >= 0 && tierIdx < 6) {
                    tCompletions[tierIdx]++;
                    tTotalTime[tierIdx] += dur;
                    tClearCount[tierIdx]++;
                }
            } else if ("Died".equals(run.result())) {
                totalDeaths++;
                if (tierIdx >= 0 && tierIdx < 6) {
                    tDeaths[tierIdx]++;
                }
            } else {
                // Extracted or other
                if (tierIdx >= 0 && tierIdx < 6) {
                    tDeaths[tierIdx]++; // count extracts in deaths for tier stats
                }
                totalDeaths++;
            }

            if (!run.bossKilled().isEmpty()) {
                totalBossKillCount++;
                if (tierIdx >= 0 && tierIdx < 6) {
                    tBossKills[tierIdx]++;
                }
            }
        }

        int avgClearSec = clearCount > 0 ? (int) (totalClearTimeSec / clearCount) : 0;

        sb.append("\"totalRuns\":").append(totalRuns);
        sb.append(",\"totalCompletions\":").append(totalCompletions);
        sb.append(",\"totalDeaths\":").append(totalDeaths);
        sb.append(",\"totalBossKills\":").append(totalBossKillCount);
        sb.append(",\"avgClearTimeSeconds\":").append(avgClearSec);

        // Tiers array
        sb.append(",\"tiers\":[");
        String[] tierNames = {"Normal", "Hard", "Nightmare", "Infernal", "Mythic", "Eternal"};
        for (int i = 0; i < 6; i++) {
            if (i > 0) sb.append(",");
            int tierAvg = tClearCount[i] > 0 ? (int) (tTotalTime[i] / tClearCount[i]) : 0;
            sb.append("{\"name\":\"").append(tierNames[i]).append("\"");
            sb.append(",\"runs\":").append(tRuns[i]);
            sb.append(",\"completions\":").append(tCompletions[i]);
            sb.append(",\"deaths\":").append(tDeaths[i]);
            sb.append(",\"bossKills\":").append(tBossKills[i]);
            sb.append(",\"avgTimeSeconds\":").append(tierAvg);
            sb.append("}");
        }
        sb.append("]");

        // Recent runs (max 20)
        sb.append(",\"recentRuns\":[");
        int runLimit = Math.min(runHistory.size(), 20);
        for (int i = 0; i < runLimit; i++) {
            if (i > 0) sb.append(",");
            RunRecord run = runHistory.get(i);
            sb.append("{\"player\":\"").append(escapeJson(run.playerName())).append("\"");
            sb.append(",\"tier\":\"").append(escapeJson(run.tier())).append("\"");
            sb.append(",\"theme\":\"").append(escapeJson(run.theme())).append("\"");
            sb.append(",\"result\":\"").append(escapeJson(run.result())).append("\"");
            sb.append(",\"durationSeconds\":").append(run.durationSeconds());
            sb.append(",\"boss\":\"").append(escapeJson(run.bossKilled())).append("\"");
            sb.append("}");
        }
        sb.append("]");

        // Boss kills leaderboard
        sb.append(",\"bossKills\":[");
        List<BossKillFlatEntry> flatKills = new ArrayList<>();
        for (Map.Entry<String, Map<String, Map<String, Integer>>> playerEntry : bossKillCounts.entrySet()) {
            String playerName = playerEntry.getKey();
            for (Map.Entry<String, Map<String, Integer>> bossEntry : playerEntry.getValue().entrySet()) {
                String bossName = bossEntry.getKey();
                for (Map.Entry<String, Integer> tierEntry : bossEntry.getValue().entrySet()) {
                    flatKills.add(new BossKillFlatEntry(playerName, bossName, tierEntry.getKey(), tierEntry.getValue()));
                }
            }
        }
        // Sort by kill count descending
        flatKills.sort((a, b) -> Integer.compare(b.count, a.count));
        int killLimit = Math.min(flatKills.size(), 15);
        for (int i = 0; i < killLimit; i++) {
            if (i > 0) sb.append(",");
            BossKillFlatEntry e = flatKills.get(i);
            sb.append("{\"player\":\"").append(escapeJson(e.player)).append("\"");
            sb.append(",\"boss\":\"").append(escapeJson(e.boss)).append("\"");
            sb.append(",\"tier\":\"").append(escapeJson(e.tier)).append("\"");
            sb.append(",\"count\":").append(e.count);
            sb.append("}");
        }
        sb.append("]");

        // Loot by rarity
        sb.append(",\"lootByRarity\":[");
        for (int i = 0; i < 5; i++) {
            if (i > 0) sb.append(",");
            sb.append(lootDropCounts[i]);
        }
        sb.append("]");

        // Loot quality thresholds (for admin panel editing)
        sb.append(",\"qualityConfig\":[");
        DungeonTier[] tiers = DungeonTier.values();
        LootQuality[] qualities = LootQuality.values();
        for (int ti = 0; ti < tiers.length; ti++) {
            if (ti > 0) sb.append(",");
            sb.append("[");
            for (int qi = 0; qi < qualities.length; qi++) {
                if (qi > 0) sb.append(",");
                sb.append(LootQuality.getPercent(tiers[ti], qualities[qi]));
            }
            sb.append("]");
        }
        sb.append("]");

        // Active instances from DungeonManager
        sb.append(",\"activeInstances\":[");
        try {
            DungeonManager dm = DungeonManager.get(level);
            Map<String, DungeonManager.DungeonInstance> allInstances = dm.getAllInstances();
            boolean first = true;
            for (Map.Entry<String, DungeonManager.DungeonInstance> entry : allInstances.entrySet()) {
                DungeonManager.DungeonInstance inst = entry.getValue();
                if (inst.cleared || inst.abandoned) continue; // Only show active
                if (!first) sb.append(",");
                first = false;

                String playerName = resolvePlayerName(inst.playerUUID, level);
                // Estimate duration from game time
                long gameTimeTicks = level.getServer().overworld().getGameTime() - inst.startTime;
                int durationSeconds = (int) (gameTimeTicks / 20); // 20 ticks per second

                sb.append("{\"player\":\"").append(escapeJson(playerName)).append("\"");
                sb.append(",\"tier\":\"").append(inst.tier.getDisplayName()).append("\"");
                sb.append(",\"theme\":\"").append(inst.theme.getDisplayName()).append("\"");
                sb.append(",\"durationSeconds\":").append(Math.max(0, durationSeconds));
                sb.append(",\"instanceId\":\"").append(escapeJson(inst.instanceId)).append("\"");
                sb.append("}");
            }
        } catch (Exception e) {
            // DungeonManager may not be available
        }
        sb.append("]");

        // Per-player stats aggregation
        sb.append(",\"playerStats\":[");
        Map<String, int[]> playerAgg = new HashMap<>(); // name -> [runs, completions, deaths, bossKills, totalClearSec, clearCount, _, tierCounts[6]]
        for (RunRecord run : runHistory) {
            int[] stats = playerAgg.computeIfAbsent(run.playerName(), k -> new int[13]);
            stats[0]++; // runs
            if ("Completed".equals(run.result())) {
                stats[1]++; // completions
                stats[4] += run.durationSeconds(); // totalClearSec
                stats[5]++; // clearCount
            } else {
                stats[2]++; // deaths
            }
            if (!run.bossKilled().isEmpty()) stats[3]++; // bossKills
            int ti = tierIndex(run.tier());
            if (ti >= 0 && ti < 6) stats[7 + ti]++;
        }
        List<Map.Entry<String, int[]>> playerList = new ArrayList<>(playerAgg.entrySet());
        playerList.sort((a, b) -> Integer.compare(b.getValue()[0], a.getValue()[0]));
        boolean firstPlayer = true;
        String[] tierLabels = {"Normal", "Hard", "Nightmare", "Infernal", "Mythic", "Eternal"};
        for (Map.Entry<String, int[]> pe : playerList) {
            if (!firstPlayer) sb.append(",");
            firstPlayer = false;
            int[] s = pe.getValue();
            int avgSecs = s[5] > 0 ? s[4] / s[5] : 0;
            int maxTierCount = 0;
            String favTier = "Normal";
            for (int i = 0; i < 6; i++) {
                if (s[7 + i] > maxTierCount) { maxTierCount = s[7 + i]; favTier = tierLabels[i]; }
            }
            sb.append("{\"name\":\"").append(escapeJson(pe.getKey())).append("\"");
            sb.append(",\"runs\":").append(s[0]);
            sb.append(",\"completions\":").append(s[1]);
            sb.append(",\"deaths\":").append(s[2]);
            sb.append(",\"bossKills\":").append(s[3]);
            sb.append(",\"avgTimeSeconds\":").append(avgSecs);
            sb.append(",\"favTier\":\"").append(favTier).append("\"");
            sb.append("}");
        }
        sb.append("]");

        sb.append("}");
        return sb.toString();
    }

    private static String resolvePlayerName(UUID uuid, ServerLevel level) {
        ServerPlayer online = level.getServer().getPlayerList().getPlayer(uuid);
        if (online != null) {
            return online.getGameProfile().name();
        }
        return uuid.toString().substring(0, 8);
    }

    private static int tierIndex(String tierName) {
        return switch (tierName) {
            case "Normal" -> 0;
            case "Hard" -> 1;
            case "Nightmare" -> 2;
            case "Infernal" -> 3;
            case "Mythic" -> 4;
            case "Eternal" -> 5;
            default -> -1;
        };
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    private static void sendResponse(ServerPlayer player, String type, String json, EconomyManager eco) {
        int wallet = eco.getWallet(player.getUUID());
        int bank = eco.getBank(player.getUUID());
        PacketDistributor.sendToPlayer(
                (ServerPlayer) player,
                (CustomPacketPayload) new ComputerDataPayload(type, json, wallet, bank),
                (CustomPacketPayload[]) new CustomPacketPayload[0]);
    }

    /**
     * Reset all tracked data. Called on server shutdown/world unload if needed.
     */
    public static void reset() {
        runHistory.clear();
        bossKillCounts.clear();
        for (int i = 0; i < 5; i++) lootDropCounts[i] = 0;
    }

    /**
     * Handle loot quality percentage change from admin panel.
     * jsonData format: "tierIndex,qualityIndex,delta" (e.g. "0,2,5" = Normal, Rare, +5%)
     */
    private static void handleLootQualitySet(String jsonData) {
        try {
            String[] parts = jsonData.split(",");
            if (parts.length != 3) return;
            int tierIdx = Integer.parseInt(parts[0].trim());
            int qualIdx = Integer.parseInt(parts[1].trim());
            int delta = Integer.parseInt(parts[2].trim());

            DungeonTier[] tiers = DungeonTier.values();
            LootQuality[] qualities = LootQuality.values();
            if (tierIdx < 0 || tierIdx >= tiers.length) return;
            if (qualIdx < 0 || qualIdx >= qualities.length) return;

            int current = LootQuality.getPercent(tiers[tierIdx], qualities[qualIdx]);
            LootQuality.setPercent(tiers[tierIdx], qualities[qualIdx], current + delta);
        } catch (Exception e) {
            // Invalid data, ignore
        }
    }

    private record BossKillFlatEntry(String player, String boss, String tier, int count) {}
}

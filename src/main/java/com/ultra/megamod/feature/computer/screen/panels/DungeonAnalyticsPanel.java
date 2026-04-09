package com.ultra.megamod.feature.computer.screen.panels;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ultra.megamod.feature.computer.network.ComputerActionPayload;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class DungeonAnalyticsPanel {

    private static final int BG = 0xFF0D1117;
    private static final int HEADER_BG = 0xFF161B22;
    private static final int BORDER = 0xFF30363D;
    private static final int TEXT = 0xFFE6EDF3;
    private static final int LABEL = 0xFF8B949E;
    private static final int ACCENT = 0xFF58A6FF;
    private static final int SUCCESS = 0xFF3FB950;
    private static final int WARNING = 0xFFD29922;
    private static final int ERROR = 0xFFF85149;
    private static final int PURPLE = 0xFFA371F7;

    private static final int[] TIER_COLORS = {SUCCESS, WARNING, ERROR, PURPLE, 0xFFFF55FF, 0xFFFFAA00};
    private static final String[] TIER_NAMES = {"Normal", "Hard", "Nightmare", "Infernal", "Mythic", "Eternal"};
    private static final String[] RARITY_NAMES = {"Common", "Uncommon", "Rare", "Epic", "Legendary"};
    private static final int[] RARITY_COLORS = {0xFFAAAAAA, SUCCESS, ACCENT, PURPLE, WARNING};

    private static final int ROW_HEIGHT = 14;
    private static final int CARD_HEIGHT = 42;
    private static final int SECTION_GAP = 8;

    private final Font font;
    private int scroll = 0;
    private int maxScroll = 0;
    private int refreshTicks = 0;
    private boolean dataLoaded = false;

    // Summary stats
    private int totalRuns = 0;
    private int totalCompletions = 0;
    private int totalDeaths = 0;
    private int totalBossKills = 0;
    private String avgClearTime = "0:00";

    // Per-tier breakdown
    private final int[] tierRuns = new int[6];
    private final int[] tierCompletions = new int[6];
    private final int[] tierDeaths = new int[6];
    private final int[] tierBossKills = new int[6];
    private final String[] tierAvgTimes = {"0:00", "0:00", "0:00", "0:00", "0:00", "0:00"};

    // Recent runs
    private final List<RunEntry> recentRuns = new ArrayList<>();

    // Boss kill leaderboard
    private final List<BossKillEntry> bossKills = new ArrayList<>();

    // Loot by rarity
    private final int[] lootByRarity = new int[5];

    // Active instances
    private final List<ActiveInstance> activeInstances = new ArrayList<>();

    // Force extract button bounds: [x, y, w, h, index]
    private final List<int[]> extractButtons = new ArrayList<>();

    // Wipe button bounds
    private int[] wipeBtn = null;

    // Per-player stats
    private final List<PlayerStats> playerStats = new ArrayList<>();

    public record RunEntry(String player, String tier, String theme, String result, String duration, String boss) {}
    public record BossKillEntry(String player, String boss, String tier, int count) {}
    public record ActiveInstance(String player, String tier, String theme, String duration, String instanceId) {}
    public record PlayerStats(String name, int runs, int completions, int deaths, int bossKills, String avgTime, String favTier) {}

    public DungeonAnalyticsPanel(Font font) {
        this.font = font;
    }

    public void requestData() {
        ClientPacketDistributor.sendToServer(
                (CustomPacketPayload) new ComputerActionPayload("dungeon_analytics_request", ""),
                (CustomPacketPayload[]) new CustomPacketPayload[0]);
    }

    public void tick() {
        refreshTicks++;
        if (refreshTicks % 100 == 0) {
            requestData();
        }
        ComputerDataPayload response = ComputerDataPayload.lastResponse;
        if (response != null && "dungeon_analytics_data".equals(response.dataType())) {
            handleResponse(response.dataType(), response.jsonData());
            ComputerDataPayload.lastResponse = null;
        }
    }

    public void handleResponse(String type, String jsonData) {
        if (!"dungeon_analytics_data".equals(type)) return;
        try {
            JsonObject root = JsonParser.parseString(jsonData).getAsJsonObject();

            totalRuns = getInt(root, "totalRuns");
            totalCompletions = getInt(root, "totalCompletions");
            totalDeaths = getInt(root, "totalDeaths");
            totalBossKills = getInt(root, "totalBossKills");
            int avgSec = getInt(root, "avgClearTimeSeconds");
            avgClearTime = formatTime(avgSec);

            // Tiers
            if (root.has("tiers")) {
                JsonArray tiers = root.getAsJsonArray("tiers");
                for (int i = 0; i < tiers.size() && i < 6; i++) {
                    JsonObject t = tiers.get(i).getAsJsonObject();
                    tierRuns[i] = getInt(t, "runs");
                    tierCompletions[i] = getInt(t, "completions");
                    tierDeaths[i] = getInt(t, "deaths");
                    tierBossKills[i] = getInt(t, "bossKills");
                    int tierAvgSec = getInt(t, "avgTimeSeconds");
                    tierAvgTimes[i] = formatTime(tierAvgSec);
                }
            }

            // Recent runs
            recentRuns.clear();
            if (root.has("recentRuns")) {
                JsonArray runs = root.getAsJsonArray("recentRuns");
                for (JsonElement el : runs) {
                    JsonObject r = el.getAsJsonObject();
                    recentRuns.add(new RunEntry(
                            getString(r, "player"),
                            getString(r, "tier"),
                            getString(r, "theme"),
                            getString(r, "result"),
                            formatTime(getInt(r, "durationSeconds")),
                            getString(r, "boss")
                    ));
                }
            }

            // Boss kills
            bossKills.clear();
            if (root.has("bossKills")) {
                JsonArray bk = root.getAsJsonArray("bossKills");
                for (JsonElement el : bk) {
                    JsonObject b = el.getAsJsonObject();
                    bossKills.add(new BossKillEntry(
                            getString(b, "player"),
                            getString(b, "boss"),
                            getString(b, "tier"),
                            getInt(b, "count")
                    ));
                }
            }

            // Loot
            if (root.has("lootByRarity")) {
                JsonArray loot = root.getAsJsonArray("lootByRarity");
                for (int i = 0; i < loot.size() && i < 5; i++) {
                    lootByRarity[i] = loot.get(i).getAsInt();
                }
            }

            // Active instances
            activeInstances.clear();
            if (root.has("activeInstances")) {
                JsonArray ai = root.getAsJsonArray("activeInstances");
                for (JsonElement el : ai) {
                    JsonObject a = el.getAsJsonObject();
                    activeInstances.add(new ActiveInstance(
                            getString(a, "player"),
                            getString(a, "tier"),
                            getString(a, "theme"),
                            formatTime(getInt(a, "durationSeconds")),
                            getString(a, "instanceId")
                    ));
                }
            }

            // Per-player stats
            playerStats.clear();
            if (root.has("playerStats")) {
                JsonArray ps = root.getAsJsonArray("playerStats");
                for (JsonElement el : ps) {
                    JsonObject p = el.getAsJsonObject();
                    playerStats.add(new PlayerStats(
                            getString(p, "name"),
                            getInt(p, "runs"),
                            getInt(p, "completions"),
                            getInt(p, "deaths"),
                            getInt(p, "bossKills"),
                            formatTime(getInt(p, "avgTimeSeconds")),
                            getString(p, "favTier")
                    ));
                }
            }

            // Quality config
            if (root.has("qualityConfig")) {
                JsonArray qc = root.getAsJsonArray("qualityConfig");
                for (int ti = 0; ti < qc.size() && ti < 6; ti++) {
                    JsonArray tierArr = qc.get(ti).getAsJsonArray();
                    for (int qi = 0; qi < tierArr.size() && qi < 5; qi++) {
                        qualityConfig[ti][qi] = tierArr.get(qi).getAsInt();
                    }
                }
            }

            dataLoaded = true;
        } catch (Exception e) {
            // Silently fail on parse error
        }
    }

    public void render(GuiGraphics g, int mouseX, int mouseY, int left, int top, int right, int bottom) {
        int panelW = right - left;
        int panelH = bottom - top;

        // Background
        g.fill(left, top, right, bottom, BG);

        // Enable scissor for scrolling content
        g.enableScissor(left, top, right, bottom);

        int y = top - scroll;
        int pad = 6;
        int innerLeft = left + pad;
        int innerRight = right - pad;
        int innerW = innerRight - innerLeft;
        extractButtons.clear();

        // Title bar
        g.fill(left, y, right, y + 20, HEADER_BG);
        drawHLine(g, left, right, y + 20, BORDER);
        g.drawString(font, "Dungeon Analytics", innerLeft + 2, y + 6, ACCENT, false);
        if (dataLoaded) {
            // Wipe Data button
            int wipeBtnW = 52;
            int wipeBtnH = 14;
            int wipeBtnX = innerRight - wipeBtnW - 50;
            int wipeBtnY = y + 3;
            boolean wipeHov = mouseX >= wipeBtnX && mouseX < wipeBtnX + wipeBtnW && mouseY >= wipeBtnY && mouseY < wipeBtnY + wipeBtnH;
            g.fill(wipeBtnX, wipeBtnY, wipeBtnX + wipeBtnW, wipeBtnY + wipeBtnH, wipeHov ? 0xFF8B1A1A : 0xFF4A1A1A);
            drawBorder(g, wipeBtnX, wipeBtnY, wipeBtnX + wipeBtnW, wipeBtnY + wipeBtnH, ERROR);
            int wtw = font.width("Wipe");
            g.drawString(font, "Wipe", wipeBtnX + (wipeBtnW - wtw) / 2, wipeBtnY + 3, ERROR, false);
            wipeBtn = new int[]{wipeBtnX, wipeBtnY, wipeBtnW, wipeBtnH};

            String status = "LIVE";
            int statusW = font.width(status);
            g.drawString(font, status, innerRight - statusW - 2, y + 6, SUCCESS, false);
            // Pulsing dot
            int dotX = innerRight - statusW - 10;
            boolean pulse = (refreshTicks / 10) % 2 == 0;
            g.fill(dotX, y + 8, dotX + 4, y + 12, pulse ? SUCCESS : 0xFF2EA043);
        }
        y += 24;

        if (!dataLoaded) {
            g.drawString(font, "Loading analytics data...", innerLeft + 2, y + 20, LABEL, false);
            g.disableScissor();
            return;
        }

        // ====== SUMMARY CARDS ======
        y = renderSummaryCards(g, mouseX, mouseY, innerLeft, y, innerW);
        y += SECTION_GAP;

        // ====== PER-TIER BREAKDOWN ======
        y = renderTierBreakdown(g, innerLeft, y, innerW);
        y += SECTION_GAP;

        // ====== ACTIVE INSTANCES ======
        y = renderActiveInstances(g, mouseX, mouseY, innerLeft, y, innerW, left, right);
        y += SECTION_GAP;

        // ====== BOSS KILL LEADERBOARD ======
        y = renderBossLeaderboard(g, innerLeft, y, innerW);
        y += SECTION_GAP;

        // ====== RECENT RUNS LOG ======
        y = renderRecentRuns(g, innerLeft, y, innerW);
        y += SECTION_GAP;

        // ====== PER-PLAYER STATS ======
        y = renderPlayerStats(g, innerLeft, y, innerW);
        y += SECTION_GAP;

        // ====== LOOT STATISTICS ======
        y = renderLootStats(g, innerLeft, y, innerW);
        y += SECTION_GAP;

        // ====== LOOT QUALITY CONFIG ======
        y = renderQualityConfig(g, mouseX, mouseY, innerLeft, y, innerW);
        y += SECTION_GAP + 10;

        g.disableScissor();

        // Calculate max scroll
        int totalContentHeight = y + scroll - top;
        maxScroll = Math.max(0, totalContentHeight - panelH);

        // Scrollbar
        if (maxScroll > 0) {
            int barX = right - 4;
            int barH = panelH;
            g.fill(barX, top, barX + 3, bottom, 0xFF21262D);
            int thumbH = Math.max(15, (int) ((float) panelH / totalContentHeight * barH));
            int thumbY = top + (int) ((float) scroll / maxScroll * (barH - thumbH));
            g.fill(barX, thumbY, barX + 3, thumbY + thumbH, LABEL);
        }
    }

    private int renderSummaryCards(GuiGraphics g, int mouseX, int mouseY, int left, int y, int totalW) {
        renderSectionHeader(g, left, y, totalW, "Overview");
        y += 18;

        int cardW = (totalW - 10) / 3;
        int row1Y = y;

        // Row 1: Total Runs, Completions, Deaths
        renderCard(g, left, row1Y, cardW, CARD_HEIGHT, "Total Runs", String.valueOf(totalRuns), ACCENT, mouseX, mouseY);
        renderCard(g, left + cardW + 5, row1Y, cardW, CARD_HEIGHT, "Completions", String.valueOf(totalCompletions), SUCCESS, mouseX, mouseY);
        renderCard(g, left + (cardW + 5) * 2, row1Y, cardW, CARD_HEIGHT, "Deaths", String.valueOf(totalDeaths), ERROR, mouseX, mouseY);
        y += CARD_HEIGHT + 4;

        // Row 2: Completion Rate, Boss Kills, Avg Clear Time
        int compRate = totalRuns > 0 ? (totalCompletions * 100 / totalRuns) : 0;
        renderCard(g, left, y, cardW, CARD_HEIGHT, "Completion Rate", compRate + "%", compRate >= 50 ? SUCCESS : WARNING, mouseX, mouseY);
        renderCard(g, left + cardW + 5, y, cardW, CARD_HEIGHT, "Boss Kills", String.valueOf(totalBossKills), PURPLE, mouseX, mouseY);
        renderCard(g, left + (cardW + 5) * 2, y, cardW, CARD_HEIGHT, "Avg Clear Time", avgClearTime, ACCENT, mouseX, mouseY);
        y += CARD_HEIGHT + 4;

        // Completion rate progress bar
        int barY = y;
        int barW = totalW;
        int barH = 6;
        g.fill(left, barY, left + barW, barY + barH, 0xFF21262D);
        int fillW = totalRuns > 0 ? (int) ((float) totalCompletions / totalRuns * barW) : 0;
        if (fillW > 0) {
            int barColor = compRate >= 70 ? SUCCESS : (compRate >= 40 ? WARNING : ERROR);
            g.fill(left, barY, left + fillW, barY + barH, barColor);
        }
        y += barH + 2;

        return y;
    }

    private int renderTierBreakdown(GuiGraphics g, int left, int y, int totalW) {
        renderSectionHeader(g, left, y, totalW, "Tier Breakdown");
        y += 18;

        int tierCount = TIER_NAMES.length;
        int colW = (totalW - (tierCount - 1) * 5) / tierCount;

        // Column headers
        for (int i = 0; i < tierCount; i++) {
            int cx = left + i * (colW + 5);
            g.fill(cx, y, cx + colW, y + 16, HEADER_BG);
            drawBorder(g, cx, y, cx + colW, y + 16, TIER_COLORS[i]);
            String name = TIER_NAMES[i].length() > 5 ? TIER_NAMES[i].substring(0, 5) : TIER_NAMES[i];
            int tw = font.width(name);
            g.drawString(font, name, cx + (colW - tw) / 2, y + 4, TIER_COLORS[i], false);
        }
        y += 18;

        // Stat rows
        String[] statLabels = {"Runs", "Clears", "Deaths", "Boss Kills", "Avg Time"};
        for (int row = 0; row < statLabels.length; row++) {
            boolean altRow = row % 2 == 0;
            for (int i = 0; i < tierCount; i++) {
                int cx = left + i * (colW + 5);
                g.fill(cx, y, cx + colW, y + ROW_HEIGHT, altRow ? 0xFF161B22 : 0xFF0D1117);

                String label = statLabels[row];
                String value;
                switch (row) {
                    case 0 -> value = String.valueOf(tierRuns[i]);
                    case 1 -> value = String.valueOf(tierCompletions[i]);
                    case 2 -> value = String.valueOf(tierDeaths[i]);
                    case 3 -> value = String.valueOf(tierBossKills[i]);
                    case 4 -> value = tierAvgTimes[i];
                    default -> value = "0";
                }

                g.drawString(font, label, cx + 3, y + 3, LABEL, false);
                int vw = font.width(value);
                g.drawString(font, value, cx + colW - vw - 3, y + 3, TEXT, false);
            }
            y += ROW_HEIGHT;
        }

        // Per-tier mini progress bars (completion rate)
        y += 2;
        for (int i = 0; i < tierCount; i++) {
            int cx = left + i * (colW + 5);
            int barW = colW;
            g.fill(cx, y, cx + barW, y + 4, 0xFF21262D);
            if (tierRuns[i] > 0) {
                int fillW = (int) ((float) tierCompletions[i] / tierRuns[i] * barW);
                if (fillW > 0) {
                    g.fill(cx, y, cx + fillW, y + 4, TIER_COLORS[i]);
                }
            }
        }
        y += 6;

        return y;
    }

    private int renderActiveInstances(GuiGraphics g, int mouseX, int mouseY, int left, int y, int totalW, int panelLeft, int panelRight) {
        String title = "Active Instances (" + activeInstances.size() + ")";
        renderSectionHeader(g, left, y, totalW, title);
        y += 18;

        if (activeInstances.isEmpty()) {
            g.fill(left, y, left + totalW, y + 24, HEADER_BG);
            drawBorder(g, left, y, left + totalW, y + 24, BORDER);
            g.drawString(font, "No active dungeon instances", left + 8, y + 8, LABEL, false);
            y += 26;
            return y;
        }

        // Table header
        g.fill(left, y, left + totalW, y + 14, 0xFF21262D);
        g.drawString(font, "Player", left + 4, y + 3, LABEL, false);
        g.drawString(font, "Tier", left + 80, y + 3, LABEL, false);
        g.drawString(font, "Theme", left + 130, y + 3, LABEL, false);
        g.drawString(font, "Time", left + 210, y + 3, LABEL, false);
        g.drawString(font, "Action", left + totalW - 52, y + 3, LABEL, false);
        y += 14;

        for (int i = 0; i < activeInstances.size(); i++) {
            ActiveInstance inst = activeInstances.get(i);
            boolean alt = i % 2 == 0;
            g.fill(left, y, left + totalW, y + ROW_HEIGHT + 2, alt ? HEADER_BG : BG);

            g.drawString(font, truncate(inst.player(), 10), left + 4, y + 3, TEXT, false);

            int tierColor = getTierColor(inst.tier());
            g.drawString(font, inst.tier(), left + 80, y + 3, tierColor, false);
            g.drawString(font, truncate(inst.theme(), 10), left + 130, y + 3, LABEL, false);
            g.drawString(font, inst.duration(), left + 210, y + 3, TEXT, false);

            // Force Extract button
            int btnX = left + totalW - 58;
            int btnY = y + 1;
            int btnW = 52;
            int btnH = ROW_HEIGHT;
            boolean hovered = mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY && mouseY < btnY + btnH;
            g.fill(btnX, btnY, btnX + btnW, btnY + btnH, hovered ? 0xFF8B1A1A : 0xFF4A1A1A);
            drawBorder(g, btnX, btnY, btnX + btnW, btnY + btnH, ERROR);
            String btnText = "Extract";
            int btw = font.width(btnText);
            g.drawString(font, btnText, btnX + (btnW - btw) / 2, btnY + 2, ERROR, false);

            extractButtons.add(new int[]{btnX, btnY, btnW, btnH, i});
            y += ROW_HEIGHT + 2;
        }

        return y;
    }

    private int renderBossLeaderboard(GuiGraphics g, int left, int y, int totalW) {
        renderSectionHeader(g, left, y, totalW, "Boss Kill Leaderboard");
        y += 18;

        if (bossKills.isEmpty()) {
            g.fill(left, y, left + totalW, y + 24, HEADER_BG);
            drawBorder(g, left, y, left + totalW, y + 24, BORDER);
            g.drawString(font, "No boss kills recorded", left + 8, y + 8, LABEL, false);
            y += 26;
            return y;
        }

        // Table header
        g.fill(left, y, left + totalW, y + 14, 0xFF21262D);
        g.drawString(font, "#", left + 4, y + 3, LABEL, false);
        g.drawString(font, "Player", left + 16, y + 3, LABEL, false);
        g.drawString(font, "Boss", left + 90, y + 3, LABEL, false);
        g.drawString(font, "Tier", left + 180, y + 3, LABEL, false);
        String killsLabel = "Kills";
        g.drawString(font, killsLabel, left + totalW - font.width(killsLabel) - 6, y + 3, LABEL, false);
        y += 14;

        int max = Math.min(bossKills.size(), 10);
        for (int i = 0; i < max; i++) {
            BossKillEntry entry = bossKills.get(i);
            boolean alt = i % 2 == 0;
            g.fill(left, y, left + totalW, y + ROW_HEIGHT, alt ? HEADER_BG : BG);

            // Rank medal colors
            int rankColor = i == 0 ? WARNING : (i == 1 ? 0xFFC0C0C0 : (i == 2 ? 0xFFCD7F32 : LABEL));
            g.drawString(font, String.valueOf(i + 1), left + 4, y + 3, rankColor, false);
            g.drawString(font, truncate(entry.player(), 10), left + 16, y + 3, TEXT, false);
            g.drawString(font, truncate(entry.boss(), 12), left + 90, y + 3, PURPLE, false);

            int tierColor = getTierColor(entry.tier());
            g.drawString(font, entry.tier(), left + 180, y + 3, tierColor, false);

            String kills = String.valueOf(entry.count());
            g.drawString(font, kills, left + totalW - font.width(kills) - 6, y + 3, ACCENT, false);

            // Kill count bar
            int maxKills = bossKills.get(0).count();
            if (maxKills > 0) {
                int barMaxW = 40;
                int barFill = (int) ((float) entry.count() / maxKills * barMaxW);
                int barX = left + 220;
                g.fill(barX, y + 4, barX + barMaxW, y + 10, 0xFF21262D);
                if (barFill > 0) {
                    g.fill(barX, y + 4, barX + barFill, y + 10, ACCENT);
                }
            }

            y += ROW_HEIGHT;
        }

        return y;
    }

    private int renderRecentRuns(GuiGraphics g, int left, int y, int totalW) {
        renderSectionHeader(g, left, y, totalW, "Recent Runs (" + recentRuns.size() + ")");
        y += 18;

        if (recentRuns.isEmpty()) {
            g.fill(left, y, left + totalW, y + 24, HEADER_BG);
            drawBorder(g, left, y, left + totalW, y + 24, BORDER);
            g.drawString(font, "No runs recorded yet", left + 8, y + 8, LABEL, false);
            y += 26;
            return y;
        }

        // Table header
        g.fill(left, y, left + totalW, y + 14, 0xFF21262D);
        g.drawString(font, "Player", left + 4, y + 3, LABEL, false);
        g.drawString(font, "Tier", left + 68, y + 3, LABEL, false);
        g.drawString(font, "Theme", left + 118, y + 3, LABEL, false);
        g.drawString(font, "Result", left + 195, y + 3, LABEL, false);
        g.drawString(font, "Time", left + 255, y + 3, LABEL, false);
        g.drawString(font, "Boss", left + totalW - 55, y + 3, LABEL, false);
        y += 14;

        int max = Math.min(recentRuns.size(), 15);
        for (int i = 0; i < max; i++) {
            RunEntry run = recentRuns.get(i);
            boolean alt = i % 2 == 0;
            g.fill(left, y, left + totalW, y + ROW_HEIGHT, alt ? HEADER_BG : BG);

            g.drawString(font, truncate(run.player(), 9), left + 4, y + 3, TEXT, false);

            int tierColor = getTierColor(run.tier());
            g.drawString(font, run.tier(), left + 68, y + 3, tierColor, false);
            g.drawString(font, truncate(run.theme(), 10), left + 118, y + 3, LABEL, false);

            int resultColor = getResultColor(run.result());
            g.drawString(font, run.result(), left + 195, y + 3, resultColor, false);
            g.drawString(font, run.duration(), left + 255, y + 3, TEXT, false);

            String boss = run.boss().isEmpty() ? "-" : run.boss();
            g.drawString(font, truncate(boss, 8), left + totalW - 55, y + 3, run.boss().isEmpty() ? LABEL : PURPLE, false);

            y += ROW_HEIGHT;
        }

        return y;
    }

    private int renderLootStats(GuiGraphics g, int left, int y, int totalW) {
        renderSectionHeader(g, left, y, totalW, "Loot Distribution");
        y += 18;

        int totalLoot = 0;
        for (int count : lootByRarity) totalLoot += count;

        g.fill(left, y, left + totalW, y + 80, HEADER_BG);
        drawBorder(g, left, y, left + totalW, y + 80, BORDER);

        int barLeft = left + 80;
        int barMaxW = totalW - 90;

        for (int i = 0; i < 5; i++) {
            int ry = y + 6 + i * 15;
            g.drawString(font, RARITY_NAMES[i], left + 4, ry + 1, RARITY_COLORS[i], false);

            // Bar background
            g.fill(barLeft, ry, barLeft + barMaxW, ry + 10, 0xFF21262D);

            // Bar fill
            if (totalLoot > 0 && lootByRarity[i] > 0) {
                int fillW = Math.max(1, (int) ((float) lootByRarity[i] / totalLoot * barMaxW));
                g.fill(barLeft, ry, barLeft + fillW, ry + 10, RARITY_COLORS[i]);
            }

            // Count text
            String count = String.valueOf(lootByRarity[i]);
            int cw = font.width(count);
            int textX = barLeft + 4;
            if (totalLoot > 0 && lootByRarity[i] > 0) {
                int fillW = (int) ((float) lootByRarity[i] / totalLoot * barMaxW);
                if (fillW > cw + 8) {
                    textX = barLeft + fillW / 2 - cw / 2;
                }
            }
            g.drawString(font, count, textX, ry + 1, TEXT, false);
        }
        y += 82;

        // Total loot count
        g.drawString(font, "Total drops: " + totalLoot, left + 4, y, LABEL, false);
        y += 12;

        return y;
    }

    // ====== LOOT QUALITY CONFIG ======

    // qualityConfig[tierIdx][qualityIdx] = percentage
    private final int[][] qualityConfig = {
            {40, 35, 20, 5, 0},   // Normal defaults
            {20, 35, 30, 12, 3},  // Hard
            {5, 20, 40, 25, 10},  // Nightmare
            {0, 10, 30, 40, 20},  // Infernal
            {0, 0, 15, 50, 35},   // Mythic
            {0, 0, 0, 40, 60},    // Eternal
    };
    private final List<int[]> qualityButtons = new ArrayList<>(); // [x,y,w,h, tierIdx, qualIdx, delta]

    private int renderQualityConfig(GuiGraphics g, int mouseX, int mouseY, int left, int y, int totalW) {
        renderSectionHeader(g, left, y, totalW, "Loot Quality Config");
        y += 18;

        qualityButtons.clear();

        // Column headers: quality names
        int colW = (totalW - 60) / 5;
        int labelW = 60;
        for (int qi = 0; qi < 5; qi++) {
            int cx = left + labelW + qi * colW;
            g.drawString(font, RARITY_NAMES[qi].substring(0, Math.min(4, RARITY_NAMES[qi].length())),
                    cx + 2, y, RARITY_COLORS[qi], false);
        }
        y += 12;

        // Per-tier rows
        for (int ti = 0; ti < TIER_NAMES.length; ti++) {
            // Tier name
            g.drawString(font, TIER_NAMES[ti].substring(0, Math.min(5, TIER_NAMES[ti].length())),
                    left + 2, y + 3, TIER_COLORS[ti], false);

            for (int qi = 0; qi < 5; qi++) {
                int cx = left + labelW + qi * colW;
                int pct = qualityConfig[ti][qi];

                // - button
                int btnW = 9;
                int btnH = 11;
                int minusBx = cx;
                int minusBy = y;
                boolean minusHover = mouseX >= minusBx && mouseX < minusBx + btnW
                        && mouseY >= minusBy && mouseY < minusBy + btnH;
                g.fill(minusBx, minusBy, minusBx + btnW, minusBy + btnH,
                        minusHover ? 0xFF2D333B : HEADER_BG);
                drawBorder(g, minusBx, minusBy, minusBx + btnW, minusBy + btnH,
                        minusHover ? ERROR : BORDER);
                g.drawString(font, "-", minusBx + 2, minusBy + 1, ERROR, false);
                qualityButtons.add(new int[]{minusBx, minusBy, btnW, btnH, ti, qi, -5});

                // Value
                String val = pct + "%";
                int valW = font.width(val);
                g.drawString(font, val, minusBx + btnW + 2, y + 1, TEXT, false);

                // + button
                int plusBx = minusBx + btnW + valW + 4;
                int plusBy = y;
                boolean plusHover = mouseX >= plusBx && mouseX < plusBx + btnW
                        && mouseY >= plusBy && mouseY < plusBy + btnH;
                g.fill(plusBx, plusBy, plusBx + btnW, plusBy + btnH,
                        plusHover ? 0xFF2D333B : HEADER_BG);
                drawBorder(g, plusBx, plusBy, plusBx + btnW, plusBy + btnH,
                        plusHover ? SUCCESS : BORDER);
                g.drawString(font, "+", plusBx + 2, plusBy + 1, SUCCESS, false);
                qualityButtons.add(new int[]{plusBx, plusBy, btnW, btnH, ti, qi, 5});
            }
            y += 14;
        }

        y += 4;
        return y;
    }

    private int renderPlayerStats(GuiGraphics g, int left, int y, int totalW) {
        renderSectionHeader(g, left, y, totalW, "Per-Player Stats (" + playerStats.size() + ")");
        y += 18;

        if (playerStats.isEmpty()) {
            g.fill(left, y, left + totalW, y + 24, HEADER_BG);
            drawBorder(g, left, y, left + totalW, y + 24, BORDER);
            g.drawString(font, "No player data yet", left + 8, y + 8, LABEL, false);
            y += 26;
            return y;
        }

        // Table header
        g.fill(left, y, left + totalW, y + 14, 0xFF21262D);
        g.drawString(font, "Player", left + 4, y + 3, LABEL, false);
        g.drawString(font, "Runs", left + 80, y + 3, LABEL, false);
        g.drawString(font, "Clears", left + 115, y + 3, LABEL, false);
        g.drawString(font, "Deaths", left + 155, y + 3, LABEL, false);
        g.drawString(font, "Bosses", left + 200, y + 3, LABEL, false);
        g.drawString(font, "Avg Time", left + 240, y + 3, LABEL, false);
        g.drawString(font, "Fav Tier", left + totalW - 55, y + 3, LABEL, false);
        y += 14;

        int max = Math.min(playerStats.size(), 20);
        for (int i = 0; i < max; i++) {
            PlayerStats ps = playerStats.get(i);
            boolean alt = i % 2 == 0;
            g.fill(left, y, left + totalW, y + ROW_HEIGHT, alt ? HEADER_BG : BG);

            g.drawString(font, truncate(ps.name(), 10), left + 4, y + 3, TEXT, false);
            g.drawString(font, String.valueOf(ps.runs()), left + 80, y + 3, ACCENT, false);
            g.drawString(font, String.valueOf(ps.completions()), left + 115, y + 3, SUCCESS, false);
            g.drawString(font, String.valueOf(ps.deaths()), left + 155, y + 3, ERROR, false);
            g.drawString(font, String.valueOf(ps.bossKills()), left + 200, y + 3, PURPLE, false);
            g.drawString(font, ps.avgTime(), left + 240, y + 3, TEXT, false);
            int tierColor = getTierColor(ps.favTier());
            g.drawString(font, ps.favTier(), left + totalW - 55, y + 3, tierColor, false);

            y += ROW_HEIGHT;
        }

        return y;
    }

    // ====== Helper rendering methods ======

    private void renderSectionHeader(GuiGraphics g, int left, int y, int totalW, String title) {
        g.fill(left, y, left + totalW, y + 16, HEADER_BG);
        drawHLine(g, left, left + totalW, y + 15, ACCENT);
        g.drawString(font, title, left + 4, y + 4, TEXT, false);
    }

    private void renderCard(GuiGraphics g, int x, int y, int w, int h, String label, String value, int valueColor, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
        g.fill(x, y, x + w, y + h, hovered ? 0xFF1C2128 : HEADER_BG);
        drawBorder(g, x, y, x + w, y + h, hovered ? ACCENT : BORDER);

        g.drawString(font, label, x + 4, y + 4, LABEL, false);
        g.drawString(font, value, x + 4, y + 18, valueColor, false);

        // Subtle underline accent
        int underW = Math.min(font.width(value), w - 8);
        g.fill(x + 4, y + 28, x + 4 + underW, y + 29, valueColor & 0x40FFFFFF);
    }

    private void drawBorder(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        g.fill(x1, y1, x2, y1 + 1, color);
        g.fill(x1, y2 - 1, x2, y2, color);
        g.fill(x1, y1, x1 + 1, y2, color);
        g.fill(x2 - 1, y1, x2, y2, color);
    }

    private void drawHLine(GuiGraphics g, int x1, int x2, int y, int color) {
        g.fill(x1, y, x2, y + 1, color);
    }

    // ====== Click handling ======

    public boolean mouseClicked(double mouseX, double mouseY, int button, int left, int top, int right, int bottom) {
        if (mouseX < left || mouseX > right || mouseY < top || mouseY > bottom) return false;
        if (button != 0) return false;

        // Check wipe button
        if (wipeBtn != null) {
            int bx = wipeBtn[0], by = wipeBtn[1], bw = wipeBtn[2], bh = wipeBtn[3];
            if (mouseX >= bx && mouseX < bx + bw && mouseY >= by && mouseY < by + bh) {
                ClientPacketDistributor.sendToServer(
                        (CustomPacketPayload) new ComputerActionPayload("dungeon_analytics_wipe", ""),
                        (CustomPacketPayload[]) new CustomPacketPayload[0]);
                refreshTicks = 95;
                return true;
            }
        }

        // Check quality config buttons
        for (int[] btn : qualityButtons) {
            int bx = btn[0], by = btn[1], bw = btn[2], bh = btn[3];
            int tierIdx = btn[4], qualIdx = btn[5], delta = btn[6];
            if (mouseX >= bx && mouseX < bx + bw && mouseY >= by && mouseY < by + bh) {
                // Send change to server
                String payload = tierIdx + "," + qualIdx + "," + delta;
                ClientPacketDistributor.sendToServer(
                        (CustomPacketPayload) new ComputerActionPayload("dungeon_loot_quality_set", payload),
                        (CustomPacketPayload[]) new CustomPacketPayload[0]);
                // Optimistic local update
                qualityConfig[tierIdx][qualIdx] = Math.max(0, Math.min(100, qualityConfig[tierIdx][qualIdx] + delta));
                refreshTicks = 95; // trigger refresh
                return true;
            }
        }

        // Check extract buttons
        for (int[] btn : extractButtons) {
            int bx = btn[0], by = btn[1], bw = btn[2], bh = btn[3], idx = btn[4];
            if (mouseX >= bx && mouseX < bx + bw && mouseY >= by && mouseY < by + bh) {
                if (idx >= 0 && idx < activeInstances.size()) {
                    ActiveInstance inst = activeInstances.get(idx);
                    ClientPacketDistributor.sendToServer(
                            (CustomPacketPayload) new ComputerActionPayload("dungeon_analytics_force_extract", inst.instanceId()),
                            (CustomPacketPayload[]) new CustomPacketPayload[0]);
                    // Refresh after a short delay via tick
                    refreshTicks = 95;
                    return true;
                }
            }
        }
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int delta = (int) (-scrollY * 12);
        scroll = Math.max(0, Math.min(maxScroll, scroll + delta));
        return true;
    }

    // ====== Utility methods ======

    private int getTierColor(String tier) {
        return switch (tier) {
            case "Normal" -> SUCCESS;
            case "Hard" -> WARNING;
            case "Nightmare" -> ERROR;
            case "Infernal" -> PURPLE;
            case "Mythic" -> 0xFFFF55FF;
            case "Eternal" -> 0xFFFFAA00;
            default -> LABEL;
        };
    }

    private int getResultColor(String result) {
        return switch (result) {
            case "Completed" -> SUCCESS;
            case "Died" -> ERROR;
            case "Extracted" -> WARNING;
            default -> LABEL;
        };
    }

    private String truncate(String s, int maxLen) {
        if (s == null) return "";
        if (s.length() <= maxLen) return s;
        return s.substring(0, maxLen - 1) + "\u2026";
    }

    private static String formatTime(int totalSeconds) {
        if (totalSeconds <= 0) return "0:00";
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return minutes + ":" + String.format("%02d", seconds);
    }

    private static int getInt(JsonObject obj, String key) {
        if (obj.has(key)) {
            try { return obj.get(key).getAsInt(); } catch (Exception e) { return 0; }
        }
        return 0;
    }

    private static String getString(JsonObject obj, String key) {
        if (obj.has(key)) {
            try { return obj.get(key).getAsString(); } catch (Exception e) { return ""; }
        }
        return "";
    }
}

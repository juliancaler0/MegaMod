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

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Full-featured Economy Dashboard panel for the Admin Terminal.
 * Replaces the inline Economy tab with 5 sub-views: Overview, Players, Analytics, Shop Config, Audit.
 */
public class EconomyDashboardPanel {

    // Theme colors
    private static final int BG = 0xFF12121A;
    private static final int CARD_BG = 0xFF1C1C26;
    private static final int BORDER = 0xFF3A3A48;
    private static final int TEXT = 0xFFE6EDF3;
    private static final int LABEL = 0xFF8B949E;
    private static final int ACCENT = 0xFF4CAF50; // Green for economy
    private static final int ACCENT_DARK = 0xFF2E7D32;
    private static final int WARNING = 0xFFD29922;
    private static final int ERROR = 0xFFF85149;
    private static final int BLUE = 0xFF58A6FF;
    private static final int PURPLE = 0xFFA371F7;
    private static final int GOLD = 0xFFFFD700;
    private static final int SILVER = 0xFFC0C0C0;
    private static final int ROW_EVEN = 0xFF1E1E2A;
    private static final int ROW_ODD = 0xFF222234;
    private static final int HEADER_BG = 0xFF161B22;

    private static final int ROW_HEIGHT = 16;
    private static final int CARD_HEIGHT = 46;
    private static final int SECTION_GAP = 8;
    private static final int SUB_TAB_HEIGHT = 18;

    private static final String[] SUB_TABS = {"Overview", "Players", "Analytics", "Shop Config", "Audit", "Mastery"};
    private static final String[] BARS = {"\u2581", "\u2582", "\u2583", "\u2584", "\u2585", "\u2586", "\u2587", "\u2588"};

    private final Font font;
    private int subTab = 0;
    private int scroll = 0;
    private int maxScroll = 0;
    private int refreshTicks = 0;
    private boolean dataLoaded = false;

    // Overview data
    private int totalCirculation, totalWallets, totalBanks, playerCount;
    private double gini;
    private int medianWealth, averageWealth, topTenWealth, activeCount;
    private int circChange24h, playerChange24h;
    private double inflationRate, velocity;
    private int todayTransactions, todayVolume, todayShopRevenue;
    private int onlinePlayers;
    private final Map<String, Integer> transactionBreakdown = new LinkedHashMap<>();
    private final int[] wealthDistribution = new int[10];

    // Players data
    private final List<PlayerEntry> playerList = new ArrayList<>();
    private int totalPlayerCount;
    private String playerSort = "richest";
    private String playerSearch = "";
    private String selectedPlayerUuid = null;

    // Analytics data
    private final List<SnapshotEntry> snapshots = new ArrayList<>();
    private final List<DailyEntry> dailies = new ArrayList<>();
    private final List<WeeklyEntry> weeklies = new ArrayList<>();
    private final List<TopEarnerEntry> topEarners = new ArrayList<>();
    private final int[] analyticsWealthDist = new int[10];
    private final Map<String, Integer> analyticsBreakdown = new LinkedHashMap<>();

    // Shop data
    private int shopInterval = 24000;
    private double shopPriceMult = 1.0;
    private double shopSellPct = 0.2;
    private int todayRevenue, weekRevenue;
    private String todaysItemsJson = "[]";

    // Audit data
    private final List<AuditEntry> auditEntries = new ArrayList<>();
    private int auditTotalEntries, auditFilteredEntries, auditTotalAdminMods, auditLargestTx;
    private String auditMostModified = "";
    private int auditMostModifiedCount;
    private String auditTypeFilter = "ALL";
    private String auditPlayerFilter = "";

    // Mastery data
    private final List<MasteryPlayerEntry> masteryPlayers = new ArrayList<>();
    private int masteryTotalMarks = 0;
    private int masteryPlayerCount = 0;
    private String masteryTargetPlayer = "";
    private String masteryGrantAmount = "";
    private boolean masteryInputFocused = false;
    private boolean masteryAmountFocused = false;

    // Button bounds for click handling
    private final List<int[]> subTabBounds = new ArrayList<>(); // [x, y, w, h, tabIndex]
    private final List<int[]> playerRowBounds = new ArrayList<>(); // [x, y, w, h, listIndex]
    private final List<int[]> actionButtons = new ArrayList<>(); // [x, y, w, h, actionIndex]
    private int[] sortBtn = null;

    // Records
    public record PlayerEntry(String name, String uuid, int wallet, int bank, int total, boolean online, int tier) {}
    public record SnapshotEntry(long ts, int circ, int wallets, int banks, int txVol, int txCount, int active, double gini, int shopRev) {}
    public record DailyEntry(String date, int peak, int min, int avg, int txCount, int txVol, int active, int shopRev, double gini) {}
    public record WeeklyEntry(String week, int peak, int min, int avg, int txCount, int txVol, int active, int shopRev, double gini) {}
    public record TopEarnerEntry(String name, String uuid, int wallet, int bank, int total) {}
    public record AuditEntry(long timestamp, String player, String type, int amount, String description) {}
    public record MasteryPlayerEntry(String name, String uuid, int marks, int prestige) {}

    public EconomyDashboardPanel(Font font) {
        this.font = font;
    }

    public void requestData() {
        scroll = 0;
        requestSubTabData();
    }

    private void requestSubTabData() {
        switch (subTab) {
            case 0:
                sendAction("eco_dashboard_request", "");
                break;
            case 1:
                JsonObject params = new JsonObject();
                params.addProperty("sort", playerSort);
                params.addProperty("search", playerSearch);
                sendAction("eco_dashboard_players", params.toString());
                break;
            case 2:
                sendAction("eco_dashboard_analytics", "");
                break;
            case 3:
                sendAction("eco_dashboard_shop", "");
                break;
            case 4:
                JsonObject auditParams = new JsonObject();
                auditParams.addProperty("type", auditTypeFilter);
                auditParams.addProperty("player", auditPlayerFilter);
                sendAction("eco_dashboard_audit", auditParams.toString());
                break;
            case 5:
                sendAction("eco_mastery_request", "");
                break;
        }
    }

    private void sendAction(String action, String data) {
        ClientPacketDistributor.sendToServer(
                (CustomPacketPayload) new ComputerActionPayload(action, data),
                (CustomPacketPayload[]) new CustomPacketPayload[0]);
    }

    public void tick() {
        refreshTicks++;
        if (refreshTicks % 200 == 0) {
            requestSubTabData();
        }
        ComputerDataPayload response = ComputerDataPayload.lastResponse;
        if (response != null) {
            String type = response.dataType();
            if (type.startsWith("eco_")) {
                handleResponse(type, response.jsonData());
                ComputerDataPayload.lastResponse = null;
            }
        }
    }

    public void handleResponse(String type, String jsonData) {
        try {
            JsonObject root = JsonParser.parseString(jsonData).getAsJsonObject();
            switch (type) {
                case "eco_dashboard_data":
                    parseOverviewData(root);
                    break;
                case "eco_players_data":
                    parsePlayersData(root);
                    break;
                case "eco_analytics_data":
                    parseAnalyticsData(root);
                    break;
                case "eco_shop_data":
                    parseShopData(root);
                    break;
                case "eco_audit_data":
                    parseAuditData(root);
                    break;
                case "eco_mastery_data":
                    parseMasteryData(root);
                    break;
                case "eco_mastery_result":
                    // Refresh mastery data after grant/revoke
                    sendAction("eco_mastery_request", "");
                    break;
            }
            dataLoaded = true;
        } catch (Exception ignored) {}
    }

    private void parseOverviewData(JsonObject root) {
        totalCirculation = getInt(root, "totalCirculation");
        totalWallets = getInt(root, "totalWallets");
        totalBanks = getInt(root, "totalBanks");
        playerCount = getInt(root, "playerCount");
        gini = getDouble(root, "gini");
        medianWealth = getInt(root, "medianWealth");
        averageWealth = getInt(root, "averageWealth");
        topTenWealth = getInt(root, "topTenWealth");
        activeCount = getInt(root, "activeCount");
        circChange24h = getInt(root, "circChange24h");
        playerChange24h = getInt(root, "playerChange24h");
        inflationRate = getDouble(root, "inflationRate");
        velocity = getDouble(root, "velocity");
        todayTransactions = getInt(root, "todayTransactions");
        todayVolume = getInt(root, "todayVolume");
        todayShopRevenue = getInt(root, "todayShopRevenue");
        onlinePlayers = getInt(root, "onlinePlayers");

        transactionBreakdown.clear();
        if (root.has("transactionBreakdown")) {
            JsonObject bd = root.getAsJsonObject("transactionBreakdown");
            for (String key : bd.keySet()) {
                transactionBreakdown.put(key, bd.get(key).getAsInt());
            }
        }

        if (root.has("wealthDistribution")) {
            JsonArray arr = root.getAsJsonArray("wealthDistribution");
            for (int i = 0; i < arr.size() && i < 10; i++) {
                wealthDistribution[i] = arr.get(i).getAsInt();
            }
        }
    }

    private void parsePlayersData(JsonObject root) {
        playerList.clear();
        totalPlayerCount = getInt(root, "totalPlayers");
        if (root.has("players")) {
            JsonArray arr = root.getAsJsonArray("players");
            for (JsonElement el : arr) {
                JsonObject p = el.getAsJsonObject();
                playerList.add(new PlayerEntry(
                        getString(p, "name"),
                        getString(p, "uuid"),
                        getInt(p, "wallet"),
                        getInt(p, "bank"),
                        getInt(p, "total"),
                        p.has("online") && p.get("online").getAsBoolean(),
                        getInt(p, "tier")
                ));
            }
        }
    }

    private void parseAnalyticsData(JsonObject root) {
        snapshots.clear();
        if (root.has("snapshots")) {
            for (JsonElement el : root.getAsJsonArray("snapshots")) {
                JsonObject s = el.getAsJsonObject();
                snapshots.add(new SnapshotEntry(
                        s.get("ts").getAsLong(),
                        getInt(s, "circ"), getInt(s, "wallets"), getInt(s, "banks"),
                        getInt(s, "txVol"), getInt(s, "txCount"), getInt(s, "active"),
                        getDouble(s, "gini"), getInt(s, "shopRev")
                ));
            }
        }
        dailies.clear();
        if (root.has("daily")) {
            for (JsonElement el : root.getAsJsonArray("daily")) {
                JsonObject d = el.getAsJsonObject();
                dailies.add(new DailyEntry(
                        getString(d, "date"), getInt(d, "peak"), getInt(d, "min"),
                        getInt(d, "avg"), getInt(d, "txCount"), getInt(d, "txVol"),
                        getInt(d, "active"), getInt(d, "shopRev"), getDouble(d, "gini")
                ));
            }
        }
        weeklies.clear();
        if (root.has("weekly")) {
            for (JsonElement el : root.getAsJsonArray("weekly")) {
                JsonObject w = el.getAsJsonObject();
                weeklies.add(new WeeklyEntry(
                        getString(w, "week"), getInt(w, "peak"), getInt(w, "min"),
                        getInt(w, "avg"), getInt(w, "txCount"), getInt(w, "txVol"),
                        getInt(w, "active"), getInt(w, "shopRev"), getDouble(w, "gini")
                ));
            }
        }
        topEarners.clear();
        if (root.has("topEarners")) {
            for (JsonElement el : root.getAsJsonArray("topEarners")) {
                JsonObject t = el.getAsJsonObject();
                topEarners.add(new TopEarnerEntry(
                        getString(t, "name"), getString(t, "uuid"),
                        getInt(t, "wallet"), getInt(t, "bank"), getInt(t, "total")
                ));
            }
        }
        if (root.has("wealthDistribution")) {
            JsonArray arr = root.getAsJsonArray("wealthDistribution");
            for (int i = 0; i < arr.size() && i < 10; i++) {
                analyticsWealthDist[i] = arr.get(i).getAsInt();
            }
        }
        analyticsBreakdown.clear();
        if (root.has("transactionBreakdown")) {
            JsonObject bd = root.getAsJsonObject("transactionBreakdown");
            for (String key : bd.keySet()) {
                analyticsBreakdown.put(key, bd.get(key).getAsInt());
            }
        }
    }

    private void parseShopData(JsonObject root) {
        shopInterval = getInt(root, "intervalTicks");
        shopPriceMult = getDouble(root, "priceMult");
        shopSellPct = getDouble(root, "sellPct");
        todayRevenue = getInt(root, "todayRevenue");
        weekRevenue = getInt(root, "weekRevenue");
        todaysItemsJson = getString(root, "todaysItems");
    }

    private void parseAuditData(JsonObject root) {
        auditEntries.clear();
        auditTotalEntries = getInt(root, "totalEntries");
        auditFilteredEntries = getInt(root, "filteredEntries");
        auditTotalAdminMods = getInt(root, "totalAdminMods");
        auditLargestTx = getInt(root, "largestTransaction");
        auditMostModified = getString(root, "mostModifiedPlayer");
        auditMostModifiedCount = getInt(root, "mostModifiedCount");
        if (root.has("entries")) {
            for (JsonElement el : root.getAsJsonArray("entries")) {
                JsonObject e = el.getAsJsonObject();
                auditEntries.add(new AuditEntry(
                        e.get("timestamp").getAsLong(),
                        getString(e, "player"), getString(e, "type"),
                        getInt(e, "amount"), getString(e, "description")
                ));
            }
        }
    }

    // ---- Rendering ----

    public void render(GuiGraphics g, int mouseX, int mouseY, int left, int top, int right, int bottom) {
        int panelW = right - left;
        int panelH = bottom - top;

        g.fill(left, top, right, bottom, BG);

        // Sub-tab bar
        subTabBounds.clear();
        int tabX = left + 4;
        int tabY = top + 2;
        for (int i = 0; i < SUB_TABS.length; i++) {
            int tabW = font.width(SUB_TABS[i]) + 12;
            boolean selected = i == subTab;
            boolean hovered = mouseX >= tabX && mouseX < tabX + tabW && mouseY >= tabY && mouseY < tabY + SUB_TAB_HEIGHT;

            if (selected) {
                g.fill(tabX, tabY, tabX + tabW, tabY + SUB_TAB_HEIGHT, ACCENT_DARK);
                drawBorder(g, tabX, tabY, tabX + tabW, tabY + SUB_TAB_HEIGHT, ACCENT);
            } else if (hovered) {
                g.fill(tabX, tabY, tabX + tabW, tabY + SUB_TAB_HEIGHT, 0xFF2A2A36);
                drawBorder(g, tabX, tabY, tabX + tabW, tabY + SUB_TAB_HEIGHT, BORDER);
            } else {
                g.fill(tabX, tabY, tabX + tabW, tabY + SUB_TAB_HEIGHT, CARD_BG);
                drawBorder(g, tabX, tabY, tabX + tabW, tabY + SUB_TAB_HEIGHT, BORDER);
            }

            int textColor = selected ? TEXT : (hovered ? TEXT : LABEL);
            int tw = font.width(SUB_TABS[i]);
            g.drawString(font, SUB_TABS[i], tabX + (tabW - tw) / 2, tabY + 5, textColor, false);

            subTabBounds.add(new int[]{tabX, tabY, tabW, SUB_TAB_HEIGHT, i});
            tabX += tabW + 2;
        }

        // Content area below tabs
        int contentTop = top + SUB_TAB_HEIGHT + 6;
        int contentLeft = left + 4;
        int contentRight = right - 4;
        int contentBottom = bottom - 2;
        int contentW = contentRight - contentLeft;

        g.enableScissor(contentLeft, contentTop, contentRight, contentBottom);

        int y = contentTop - scroll;
        actionButtons.clear();
        playerRowBounds.clear();

        switch (subTab) {
            case 0: y = renderOverview(g, mouseX, mouseY, contentLeft, y, contentW, contentRight); break;
            case 1: y = renderPlayers(g, mouseX, mouseY, contentLeft, y, contentW, contentRight); break;
            case 2: y = renderAnalytics(g, mouseX, mouseY, contentLeft, y, contentW, contentRight); break;
            case 3: y = renderShopConfig(g, mouseX, mouseY, contentLeft, y, contentW, contentRight); break;
            case 4: y = renderAudit(g, mouseX, mouseY, contentLeft, y, contentW, contentRight); break;
            case 5: y = renderMastery(g, mouseX, mouseY, contentLeft, y, contentW, contentRight); break;
        }

        y += SECTION_GAP + 10;
        g.disableScissor();

        int totalContentH = y + scroll - contentTop;
        maxScroll = Math.max(0, totalContentH - (contentBottom - contentTop));

        // Scrollbar
        if (maxScroll > 0) {
            int barX = right - 4;
            int barH = contentBottom - contentTop;
            g.fill(barX, contentTop, barX + 3, contentBottom, 0xFF21262D);
            int thumbH = Math.max(15, (int)((float)(contentBottom - contentTop) / totalContentH * barH));
            int thumbY = contentTop + (int)((float) scroll / maxScroll * (barH - thumbH));
            g.fill(barX, thumbY, barX + 3, thumbY + thumbH, LABEL);
        }
    }

    // ---- Overview Sub-tab ----

    private int renderOverview(GuiGraphics g, int mx, int my, int left, int y, int w, int right) {
        renderSectionHeader(g, left, y, w, "Economy Overview");
        y += 18;

        if (!dataLoaded) {
            g.drawString(font, "Loading economy data...", left + 4, y + 10, LABEL, false);
            return y + 30;
        }

        // Row 1: Big stats cards
        int cardW = (w - 10) / 3;

        renderStatCard(g, left, y, cardW, CARD_HEIGHT, "Total Circulation", formatCoins(totalCirculation),
                circChange24h > 0 ? "+" + formatCoins(circChange24h) : formatCoins(circChange24h),
                circChange24h >= 0 ? ACCENT : ERROR, mx, my);
        renderStatCard(g, left + cardW + 5, y, cardW, CARD_HEIGHT, "Total in Wallets", formatCoins(totalWallets),
                null, BLUE, mx, my);
        renderStatCard(g, left + (cardW + 5) * 2, y, cardW, CARD_HEIGHT, "Total in Banks", formatCoins(totalBanks),
                null, PURPLE, mx, my);
        y += CARD_HEIGHT + 4;

        // Row 2
        renderStatCard(g, left, y, cardW, CARD_HEIGHT, "Players", String.valueOf(playerCount),
                playerChange24h != 0 ? (playerChange24h > 0 ? "+" + playerChange24h : "" + playerChange24h) : null,
                BLUE, mx, my);

        String giniStr = String.format("%.3f", gini);
        int giniColor = gini < 0.3 ? ACCENT : (gini < 0.5 ? WARNING : ERROR);
        renderStatCard(g, left + cardW + 5, y, cardW, CARD_HEIGHT, "Gini Index", giniStr,
                gini < 0.3 ? "Equitable" : (gini < 0.5 ? "Moderate" : "Unequal"), giniColor, mx, my);

        renderStatCard(g, left + (cardW + 5) * 2, y, cardW, CARD_HEIGHT, "Online", String.valueOf(onlinePlayers),
                null, ACCENT, mx, my);
        y += CARD_HEIGHT + 4;

        // Inflation rate bar
        g.fill(left, y, left + w, y + 22, CARD_BG);
        drawBorder(g, left, y, left + w, y + 22, BORDER);
        String inflLabel = "Inflation (24h): " + String.format("%.2f%%", inflationRate);
        int inflColor = Math.abs(inflationRate) < 5 ? ACCENT : (Math.abs(inflationRate) < 15 ? WARNING : ERROR);
        g.drawString(font, inflLabel, left + 4, y + 7, inflColor, false);

        String velLabel = "Velocity: " + String.format("%.3f", velocity);
        g.drawString(font, velLabel, left + w / 2, y + 7, BLUE, false);
        y += 26;

        // Quick stats row
        y = renderQuickStats(g, left, y, w);
        y += SECTION_GAP;

        // Today's Activity
        renderSectionHeader(g, left, y, w, "Today's Activity");
        y += 18;

        int halfW = (w - 5) / 2;
        g.fill(left, y, left + halfW, y + 36, CARD_BG);
        drawBorder(g, left, y, left + halfW, y + 36, BORDER);
        g.drawString(font, "Transactions: " + todayTransactions, left + 6, y + 4, TEXT, false);
        g.drawString(font, "Volume: " + formatCoins(todayVolume), left + 6, y + 16, LABEL, false);
        g.drawString(font, "Shop Revenue: " + formatCoins(todayShopRevenue), left + 6, y + 28, ACCENT, false);

        // Wealth distribution mini chart
        g.fill(left + halfW + 5, y, left + w, y + 36, CARD_BG);
        drawBorder(g, left + halfW + 5, y, left + w, y + 36, BORDER);
        g.drawString(font, "Wealth Distribution", left + halfW + 9, y + 2, LABEL, false);
        renderMiniBarChart(g, left + halfW + 9, y + 13, halfW - 14, 20, wealthDistribution);
        y += 40;

        // Transaction breakdown
        if (!transactionBreakdown.isEmpty()) {
            y += SECTION_GAP;
            renderSectionHeader(g, left, y, w, "Coin Flow Breakdown");
            y += 18;
            int totalFlow = 0;
            for (int v : transactionBreakdown.values()) totalFlow += v;

            int col = 0;
            int colW = (w - 5) / 2;
            int startY = y;
            for (Map.Entry<String, Integer> entry : transactionBreakdown.entrySet()) {
                int bx = left + col * (colW + 5);
                int pct = totalFlow > 0 ? (int)((double) entry.getValue() / totalFlow * 100) : 0;
                g.fill(bx, y, bx + colW, y + 12, col % 2 == 0 ? ROW_EVEN : ROW_ODD);
                g.drawString(font, entry.getKey(), bx + 4, y + 2, LABEL, false);
                String valStr = formatCoins(entry.getValue()) + " (" + pct + "%)";
                int vw = font.width(valStr);
                g.drawString(font, valStr, bx + colW - vw - 4, y + 2, TEXT, false);
                y += 13;
                if (y - startY > 100 && col == 0) {
                    col = 1;
                    y = startY;
                }
            }
            y = Math.max(y, startY) + 4;
        }

        return y;
    }

    private int renderQuickStats(GuiGraphics g, int left, int y, int w) {
        g.fill(left, y, left + w, y + 14, HEADER_BG);
        drawBorder(g, left, y, left + w, y + 14, BORDER);
        String stats = String.format("Avg: %s  |  Median: %s  |  Top 10%%: %s  |  Active: %d",
                formatCoins(averageWealth), formatCoins(medianWealth), formatCoins(topTenWealth), activeCount);
        g.drawString(font, stats, left + 4, y + 3, LABEL, false);
        return y + 16;
    }

    // ---- Players Sub-tab ----

    private int renderPlayers(GuiGraphics g, int mx, int my, int left, int y, int w, int right) {
        renderSectionHeader(g, left, y, w, "Player Economy (" + totalPlayerCount + " players)");
        y += 18;

        // Sort buttons
        String[] sorts = {"Richest", "Poorest", "Wallet", "Bank", "Name"};
        String[] sortKeys = {"richest", "poorest", "wallet", "bank", "name"};
        int bx = left;
        for (int i = 0; i < sorts.length; i++) {
            int bw = font.width(sorts[i]) + 8;
            boolean active = playerSort.equals(sortKeys[i]);
            boolean hov = mx >= bx && mx < bx + bw && my >= y && my < y + 14;
            g.fill(bx, y, bx + bw, y + 14, active ? ACCENT_DARK : (hov ? 0xFF2A2A36 : CARD_BG));
            drawBorder(g, bx, y, bx + bw, y + 14, active ? ACCENT : BORDER);
            g.drawString(font, sorts[i], bx + 4, y + 3, active ? TEXT : LABEL, false);
            actionButtons.add(new int[]{bx, y, bw, 14, 100 + i}); // 100+ = sort action
            bx += bw + 2;
        }
        y += 18;

        // Table header
        g.fill(left, y, left + w, y + 14, HEADER_BG);
        g.drawString(font, "Player", left + 4, y + 3, LABEL, false);
        g.drawString(font, "Wallet", left + w / 3, y + 3, LABEL, false);
        g.drawString(font, "Bank", left + w / 3 + 60, y + 3, LABEL, false);
        g.drawString(font, "Total", left + w / 3 + 120, y + 3, LABEL, false);
        g.drawString(font, "Status", left + w - 40, y + 3, LABEL, false);
        y += 15;

        // Player rows
        for (int i = 0; i < playerList.size(); i++) {
            PlayerEntry p = playerList.get(i);
            boolean alt = i % 2 == 0;
            boolean rowHov = mx >= left && mx < left + w && my >= y && my < y + ROW_HEIGHT;
            int rowBg = rowHov ? 0xFF2A2A3A : (alt ? ROW_EVEN : ROW_ODD);
            g.fill(left, y, left + w, y + ROW_HEIGHT, rowBg);

            // Tier color indicator
            int tierColor = switch (p.tier) {
                case 0 -> GOLD;
                case 1 -> SILVER;
                case 3 -> ERROR;
                default -> 0xFF555566;
            };
            g.fill(left, y, left + 2, y + ROW_HEIGHT, tierColor);

            // Name with online indicator
            int nameColor = p.online ? ACCENT : TEXT;
            String prefix = p.online ? "\u25CF " : "  ";
            g.drawString(font, prefix + truncate(p.name, 14), left + 4, y + 4, nameColor, false);

            g.drawString(font, formatCoins(p.wallet), left + w / 3, y + 4, TEXT, false);
            g.drawString(font, formatCoins(p.bank), left + w / 3 + 60, y + 4, TEXT, false);

            int totalColor = p.tier == 0 ? GOLD : (p.tier == 1 ? SILVER : TEXT);
            g.drawString(font, formatCoins(p.total), left + w / 3 + 120, y + 4, totalColor, false);

            String status = p.online ? "ON" : "OFF";
            int statusColor = p.online ? ACCENT : LABEL;
            g.drawString(font, status, left + w - 30, y + 4, statusColor, false);

            playerRowBounds.add(new int[]{left, y, w, ROW_HEIGHT, i});
            y += ROW_HEIGHT;
        }

        if (playerList.isEmpty()) {
            g.fill(left, y, left + w, y + 24, CARD_BG);
            drawBorder(g, left, y, left + w, y + 24, BORDER);
            g.drawString(font, "No players found.", left + 8, y + 8, LABEL, false);
            y += 26;
        }

        // Per-player edit panel (when a player row is clicked)
        if (selectedPlayerUuid != null) {
            PlayerEntry selected = null;
            for (PlayerEntry p : playerList) {
                if (p.uuid.equals(selectedPlayerUuid)) { selected = p; break; }
            }
            if (selected != null) {
                y += SECTION_GAP;
                renderSectionHeader(g, left, y, w, "Edit: " + selected.name);
                y += 18;

                // Current balances
                g.drawString(font, "Wallet: " + formatCoins(selected.wallet), left + 4, y, ACCENT, false);
                g.drawString(font, "Bank: " + formatCoins(selected.bank), left + w / 2, y, BLUE, false);
                y += 14;

                // Wallet edit buttons
                g.drawString(font, "Wallet:", left + 4, y + 3, LABEL, false);
                int bx2 = left + 42;
                String[] wLabels = {"-10K", "-1K", "-100", "+100", "+1K", "+10K", "Reset"};
                int[] wActions = {605, 604, 603, 600, 601, 602, 606};
                for (int j = 0; j < wLabels.length; j++) {
                    int bw2 = font.width(wLabels[j]) + 6;
                    boolean isNeg = j < 3;
                    boolean h2 = mx >= bx2 && mx < bx2 + bw2 && my >= y && my < y + 14;
                    g.fill(bx2, y, bx2 + bw2, y + 14, h2 ? (isNeg ? 0xFF4A1A1A : 0xFF1A3A1A) : CARD_BG);
                    drawBorder(g, bx2, y, bx2 + bw2, y + 14, j == 6 ? WARNING : (isNeg ? ERROR : ACCENT));
                    g.drawString(font, wLabels[j], bx2 + 3, y + 3, TEXT, false);
                    actionButtons.add(new int[]{bx2, y, bw2, 14, wActions[j]});
                    bx2 += bw2 + 2;
                }
                y += 18;

                // Bank edit buttons
                g.drawString(font, "Bank:", left + 4, y + 3, LABEL, false);
                bx2 = left + 42;
                String[] bLabels = {"-10K", "-1K", "-100", "+100", "+1K", "+10K", "Reset"};
                int[] bActions2 = {612, 611, 610, 607, 608, 609, 613};
                for (int j = 0; j < bLabels.length; j++) {
                    int bw2 = font.width(bLabels[j]) + 6;
                    boolean isNeg = j < 3;
                    boolean h2 = mx >= bx2 && mx < bx2 + bw2 && my >= y && my < y + 14;
                    g.fill(bx2, y, bx2 + bw2, y + 14, h2 ? (isNeg ? 0xFF4A1A1A : 0xFF1A3A1A) : CARD_BG);
                    drawBorder(g, bx2, y, bx2 + bw2, y + 14, j == 6 ? WARNING : (isNeg ? ERROR : ACCENT));
                    g.drawString(font, bLabels[j], bx2 + 3, y + 3, TEXT, false);
                    actionButtons.add(new int[]{bx2, y, bw2, 14, bActions2[j]});
                    bx2 += bw2 + 2;
                }
                y += 18;
            } else {
                selectedPlayerUuid = null;
            }
        }

        // Bulk action buttons
        y += SECTION_GAP;
        renderSectionHeader(g, left, y, w, "Bulk Actions");
        y += 18;

        String[] bulkLabels = {"Give All 100", "Take All 100", "Give All 1000", "Take All 1000"};
        int[] bulkActions = {200, 201, 202, 203};
        bx = left;
        for (int i = 0; i < bulkLabels.length; i++) {
            int bw = font.width(bulkLabels[i]) + 8;
            boolean hov = mx >= bx && mx < bx + bw && my >= y && my < y + 14;
            int btnBg = (i % 2 == 1) ? (hov ? 0xFF5A1A1A : 0xFF3A1A1A) : (hov ? 0xFF1A3A1A : 0xFF1A2A1A);
            g.fill(bx, y, bx + bw, y + 14, btnBg);
            drawBorder(g, bx, y, bx + bw, y + 14, (i % 2 == 1) ? ERROR : ACCENT);
            g.drawString(font, bulkLabels[i], bx + 4, y + 3, TEXT, false);
            actionButtons.add(new int[]{bx, y, bw, 14, bulkActions[i]});
            bx += bw + 4;
        }
        y += 18;

        return y;
    }

    // ---- Analytics Sub-tab ----

    private int renderAnalytics(GuiGraphics g, int mx, int my, int left, int y, int w, int right) {
        renderSectionHeader(g, left, y, w, "Economy Analytics");
        y += 18;

        // Circulation chart (last 24h)
        y = renderAsciiChart(g, left, y, w, "Circulation (Last 24h)", snapshots, true);
        y += SECTION_GAP;

        // Transaction volume chart
        y = renderAsciiChart(g, left, y, w, "Transaction Volume (Last 24h)", snapshots, false);
        y += SECTION_GAP;

        // Wealth distribution histogram
        renderSectionHeader(g, left, y, w, "Wealth Distribution (10 Buckets)");
        y += 18;
        g.fill(left, y, left + w, y + 50, CARD_BG);
        drawBorder(g, left, y, left + w, y + 50, BORDER);

        int maxDist = 1;
        for (int d : analyticsWealthDist) maxDist = Math.max(maxDist, d);
        int barW = Math.max(4, (w - 20) / 10);
        for (int i = 0; i < 10; i++) {
            int barH = analyticsWealthDist[i] > 0 ? Math.max(2, (int)((double) analyticsWealthDist[i] / maxDist * 35)) : 0;
            int bx2 = left + 4 + i * (barW + 2);
            int by2 = y + 45 - barH;
            int barColor = i < 3 ? ERROR : (i < 7 ? BLUE : ACCENT);
            if (barH > 0) g.fill(bx2, by2, bx2 + barW, y + 45, barColor);
        }
        // Labels
        g.drawString(font, "Poor", left + 4, y + 2, LABEL, false);
        int richW = font.width("Rich");
        g.drawString(font, "Rich", left + w - richW - 6, y + 2, LABEL, false);
        y += 54;

        // Top 10 Earners
        y += SECTION_GAP;
        renderSectionHeader(g, left, y, w, "Top 10 Earners");
        y += 18;

        g.fill(left, y, left + w, y + 14, HEADER_BG);
        g.drawString(font, "#", left + 4, y + 3, LABEL, false);
        g.drawString(font, "Player", left + 20, y + 3, LABEL, false);
        g.drawString(font, "Wallet", left + w / 3, y + 3, LABEL, false);
        g.drawString(font, "Bank", left + w / 3 + 60, y + 3, LABEL, false);
        g.drawString(font, "Total", left + w / 3 + 120, y + 3, LABEL, false);
        y += 15;

        for (int i = 0; i < topEarners.size(); i++) {
            TopEarnerEntry te = topEarners.get(i);
            boolean alt = i % 2 == 0;
            g.fill(left, y, left + w, y + ROW_HEIGHT, alt ? ROW_EVEN : ROW_ODD);

            int rankColor = i == 0 ? GOLD : (i == 1 ? SILVER : (i == 2 ? 0xFFCD7F32 : LABEL));
            g.drawString(font, "#" + (i + 1), left + 4, y + 4, rankColor, false);
            g.drawString(font, truncate(te.name, 14), left + 20, y + 4, TEXT, false);
            g.drawString(font, formatCoins(te.wallet), left + w / 3, y + 4, TEXT, false);
            g.drawString(font, formatCoins(te.bank), left + w / 3 + 60, y + 4, TEXT, false);
            g.drawString(font, formatCoins(te.total), left + w / 3 + 120, y + 4, GOLD, false);
            y += ROW_HEIGHT;
        }

        if (topEarners.isEmpty()) {
            g.fill(left, y, left + w, y + 20, CARD_BG);
            g.drawString(font, "No data yet.", left + 8, y + 6, LABEL, false);
            y += 22;
        }

        // Transaction breakdown
        if (!analyticsBreakdown.isEmpty()) {
            y += SECTION_GAP;
            renderSectionHeader(g, left, y, w, "Coin Sources & Sinks");
            y += 18;

            int totalVol = 0;
            for (int v : analyticsBreakdown.values()) totalVol += v;

            for (Map.Entry<String, Integer> entry : analyticsBreakdown.entrySet()) {
                boolean alt = y % 2 == 0;
                g.fill(left, y, left + w, y + 13, alt ? ROW_EVEN : ROW_ODD);
                g.drawString(font, entry.getKey(), left + 4, y + 2, LABEL, false);

                int pct = totalVol > 0 ? (int)((double) entry.getValue() / totalVol * 100) : 0;
                String valStr = formatCoins(entry.getValue()) + " (" + pct + "%)";

                // Progress bar
                int barMaxW = w / 3;
                int barFillW = totalVol > 0 ? (int)((double) entry.getValue() / totalVol * barMaxW) : 0;
                int barX = left + w / 2;
                g.fill(barX, y + 2, barX + barMaxW, y + 10, 0xFF21262D);
                if (barFillW > 0) g.fill(barX, y + 2, barX + barFillW, y + 10, ACCENT);

                int vw = font.width(valStr);
                g.drawString(font, valStr, left + w - vw - 4, y + 2, TEXT, false);
                y += 14;
            }
        }

        // Daily aggregates table
        if (!dailies.isEmpty()) {
            y += SECTION_GAP;
            renderSectionHeader(g, left, y, w, "Daily Summary (Last " + dailies.size() + " Days)");
            y += 18;

            g.fill(left, y, left + w, y + 14, HEADER_BG);
            g.drawString(font, "Date", left + 4, y + 3, LABEL, false);
            g.drawString(font, "Avg Circ", left + 80, y + 3, LABEL, false);
            g.drawString(font, "Tx Count", left + 150, y + 3, LABEL, false);
            g.drawString(font, "Volume", left + 210, y + 3, LABEL, false);
            g.drawString(font, "Shop Rev", left + w - 60, y + 3, LABEL, false);
            y += 15;

            for (int i = 0; i < Math.min(dailies.size(), 14); i++) {
                DailyEntry d = dailies.get(i);
                boolean alt = i % 2 == 0;
                g.fill(left, y, left + w, y + 13, alt ? ROW_EVEN : ROW_ODD);
                g.drawString(font, d.date.length() > 10 ? d.date.substring(5) : d.date, left + 4, y + 2, TEXT, false);
                g.drawString(font, formatCoins(d.avg), left + 80, y + 2, TEXT, false);
                g.drawString(font, String.valueOf(d.txCount), left + 150, y + 2, TEXT, false);
                g.drawString(font, formatCoins(d.txVol), left + 210, y + 2, TEXT, false);
                g.drawString(font, formatCoins(d.shopRev), left + w - 60, y + 2, ACCENT, false);
                y += 14;
            }
        }

        return y;
    }

    private int renderAsciiChart(GuiGraphics g, int left, int y, int w, String title, List<SnapshotEntry> data, boolean useCirc) {
        renderSectionHeader(g, left, y, w, title);
        y += 18;

        int chartH = 40;
        g.fill(left, y, left + w, y + chartH, CARD_BG);
        drawBorder(g, left, y, left + w, y + chartH, BORDER);

        if (data.isEmpty()) {
            g.drawString(font, "No data available yet.", left + 8, y + chartH / 2 - 4, LABEL, false);
            return y + chartH + 2;
        }

        // Sample up to 48 data points evenly
        int maxBars = Math.min(48, (w - 8) / 4);
        int step = Math.max(1, data.size() / maxBars);
        List<Integer> values = new ArrayList<>();
        for (int i = data.size() - 1; i >= 0; i -= step) {
            values.add(useCirc ? data.get(i).circ : data.get(i).txVol);
        }

        int maxVal = 1;
        for (int v : values) maxVal = Math.max(maxVal, v);

        int barW = Math.max(2, (w - 8) / Math.max(1, values.size()));
        for (int i = 0; i < values.size(); i++) {
            int val = values.get(i);
            int barH = val > 0 ? Math.max(2, (int)((double) val / maxVal * (chartH - 8))) : 0;
            int bx = left + 4 + i * barW;
            int by = y + chartH - 4 - barH;
            int color = useCirc ? ACCENT : BLUE;
            if (barH > 0) g.fill(bx, by, bx + Math.max(1, barW - 1), y + chartH - 4, color);
        }

        // Min/max labels
        g.drawString(font, formatCoins(maxVal), left + 4, y + 2, LABEL, false);

        return y + chartH + 2;
    }

    // ---- Shop Config Sub-tab ----

    private int renderShopConfig(GuiGraphics g, int mx, int my, int left, int y, int w, int right) {
        renderSectionHeader(g, left, y, w, "Shop Configuration");
        y += 18;

        // Current settings
        g.fill(left, y, left + w, y + 70, CARD_BG);
        drawBorder(g, left, y, left + w, y + 70, BORDER);

        g.drawString(font, "Refresh Interval:", left + 6, y + 6, LABEL, false);
        g.drawString(font, shopInterval + " ticks (" + (shopInterval / 20) + "s)", left + 110, y + 6, TEXT, false);

        g.drawString(font, "Price Multiplier:", left + 6, y + 20, LABEL, false);
        g.drawString(font, String.format("%.2fx", shopPriceMult), left + 110, y + 20, TEXT, false);

        g.drawString(font, "Sell Percentage:", left + 6, y + 34, LABEL, false);
        g.drawString(font, String.format("%.0f%%", shopSellPct * 100), left + 110, y + 34, TEXT, false);

        g.drawString(font, "Today Revenue:", left + 6, y + 48, LABEL, false);
        g.drawString(font, formatCoins(todayRevenue), left + 110, y + 48, ACCENT, false);

        g.drawString(font, "Week Revenue:", left + w / 2, y + 48, LABEL, false);
        g.drawString(font, formatCoins(weekRevenue), left + w / 2 + 95, y + 48, ACCENT, false);
        y += 74;

        // Adjustment buttons
        y += 4;
        String[][] adjustments = {
                {"Interval -1000", "310"}, {"Interval +1000", "311"}, {"Interval 24000", "312"},
                {"Price x0.5", "313"}, {"Price x1.0", "314"}, {"Price x2.0", "315"},
                {"Sell 10%", "316"}, {"Sell 20%", "317"}, {"Sell 50%", "318"},
                {"\u21BB Refresh Shop", "319"}
        };

        int bx = left;
        for (String[] adj : adjustments) {
            int bw = font.width(adj[0]) + 8;
            if (bx + bw > left + w) { bx = left; y += 16; }
            boolean isRefresh = "319".equals(adj[1]);
            boolean hov = mx >= bx && mx < bx + bw && my >= y && my < y + 14;
            g.fill(bx, y, bx + bw, y + 14, isRefresh ? (hov ? 0xFF2E7D32 : 0xFF1B5E20) : (hov ? 0xFF2A2A36 : CARD_BG));
            drawBorder(g, bx, y, bx + bw, y + 14, hov ? ACCENT : BORDER);
            g.drawString(font, adj[0], bx + 4, y + 3, isRefresh ? 0xFFFFFFFF : TEXT, false);
            actionButtons.add(new int[]{bx, y, bw, 14, Integer.parseInt(adj[1])});
            bx += bw + 3;
        }
        y += 22;

        // Today's Shop Items
        renderSectionHeader(g, left, y, w, "Today's Shop Items");
        y += 18;

        // Parse today's items from JSON
        try {
            if (todaysItemsJson != null && todaysItemsJson.length() > 2) {
                // Simple parser for ShopItem JSON array: [{"id":"...","name":"...","buy":N,"sell":N}, ...]
                String arr = todaysItemsJson;
                int pos = 1;
                int slot = 0;
                while (pos < arr.length() && slot < 12) {
                    int objStart = arr.indexOf('{', pos);
                    if (objStart < 0) break;
                    int objEnd = arr.indexOf('}', objStart);
                    if (objEnd < 0) break;
                    String obj = arr.substring(objStart, objEnd + 1);

                    String itemId = extractStr(obj, "id");
                    String itemName = extractStr(obj, "name");
                    int buyPrice = extractInt(obj, "buy");
                    int sellPrice = extractInt(obj, "sell");

                    // Draw item row
                    boolean rowHov = mx >= left && mx < left + w && my >= y && my < y + 18;
                    g.fill(left, y, left + w, y + 18, slot % 2 == 0 ? CARD_BG : ROW_ODD);

                    // Slot number
                    g.drawString(font, "#" + slot, left + 4, y + 5, LABEL, false);

                    // Item name (truncated if needed)
                    String displayName = itemName.length() > 24 ? itemName.substring(0, 22) + ".." : itemName;
                    g.drawString(font, displayName, left + 22, y + 5, TEXT, false);

                    // Buy/Sell prices
                    String priceStr = "B:" + buyPrice + " S:" + sellPrice;
                    int priceW = font.width(priceStr);
                    g.drawString(font, priceStr, right - priceW - 80, y + 5, ACCENT, false);

                    // Price +/- buttons
                    int btnX = right - 72;
                    boolean minHov = mx >= btnX && mx < btnX + 16 && my >= y + 2 && my < y + 16;
                    boolean plusHov = mx >= btnX + 18 && mx < btnX + 34 && my >= y + 2 && my < y + 16;
                    boolean dblHov = mx >= btnX + 36 && mx < btnX + 52 && my >= y + 2 && my < y + 16;

                    g.fill(btnX, y + 2, btnX + 16, y + 16, minHov ? 0xFF993333 : CARD_BG);
                    drawBorder(g, btnX, y + 2, btnX + 16, y + 16, minHov ? ACCENT : BORDER);
                    g.drawString(font, "-", btnX + 5, y + 4, TEXT, false);
                    actionButtons.add(new int[]{btnX, y + 2, 16, 14, 320 + slot * 3}); // price down

                    g.fill(btnX + 18, y + 2, btnX + 34, y + 16, plusHov ? 0xFF339933 : CARD_BG);
                    drawBorder(g, btnX + 18, y + 2, btnX + 34, y + 16, plusHov ? ACCENT : BORDER);
                    g.drawString(font, "+", btnX + 23, y + 4, TEXT, false);
                    actionButtons.add(new int[]{btnX + 18, y + 2, 16, 14, 321 + slot * 3}); // price up

                    g.fill(btnX + 36, y + 2, btnX + 52, y + 16, dblHov ? 0xFF336699 : CARD_BG);
                    drawBorder(g, btnX + 36, y + 2, btnX + 52, y + 16, dblHov ? ACCENT : BORDER);
                    g.drawString(font, "x2", btnX + 38, y + 4, TEXT, false);
                    actionButtons.add(new int[]{btnX + 36, y + 2, 16, 14, 322 + slot * 3}); // price x2

                    y += 18;
                    slot++;
                    pos = objEnd + 1;
                }
            } else {
                g.drawString(font, "No shop data loaded. Switch away and back.", left + 6, y + 4, LABEL, false);
                y += 16;
            }
        } catch (Exception e) {
            g.drawString(font, "Error parsing shop data", left + 6, y + 4, 0xFFFF4444, false);
            y += 16;
        }

        return y;
    }

    private static String extractStr(String json, String key) {
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search);
        if (start < 0) return "";
        start += search.length();
        int end = json.indexOf("\"", start);
        return end < 0 ? "" : json.substring(start, end);
    }

    private static int extractInt(String json, String key) {
        String search = "\"" + key + "\":";
        int start = json.indexOf(search);
        if (start < 0) return 0;
        start += search.length();
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) end++;
        try { return Integer.parseInt(json.substring(start, end)); } catch (Exception e) { return 0; }
    }

    // ---- Audit Sub-tab ----

    private int renderAudit(GuiGraphics g, int mx, int my, int left, int y, int w, int right) {
        renderSectionHeader(g, left, y, w, "Audit Log (" + auditTotalEntries + " entries)");
        y += 18;

        // Filter buttons
        String[] filters = {"ALL", "SHOP_BUY", "SHOP_SELL", "TRADE", "ADMIN_ADD", "ADMIN_SET", "ADMIN_REMOVE"};
        int bx = left;
        for (int i = 0; i < filters.length; i++) {
            int bw = font.width(filters[i]) + 6;
            if (bx + bw > left + w) { bx = left; y += 15; }
            boolean active = auditTypeFilter.equals(filters[i]);
            boolean hov = mx >= bx && mx < bx + bw && my >= y && my < y + 13;
            g.fill(bx, y, bx + bw, y + 13, active ? ACCENT_DARK : (hov ? 0xFF2A2A36 : CARD_BG));
            drawBorder(g, bx, y, bx + bw, y + 13, active ? ACCENT : BORDER);
            g.drawString(font, filters[i], bx + 3, y + 2, active ? TEXT : LABEL, false);
            actionButtons.add(new int[]{bx, y, bw, 13, 400 + i}); // 400+ = audit filter
            bx += bw + 2;
        }
        y += 17;

        // Stats summary
        g.fill(left, y, left + w, y + 24, CARD_BG);
        drawBorder(g, left, y, left + w, y + 24, BORDER);
        g.drawString(font, "Admin Mods: " + auditTotalAdminMods, left + 6, y + 3, WARNING, false);
        g.drawString(font, "Largest Tx: " + formatCoins(auditLargestTx), left + w / 3, y + 3, TEXT, false);
        if (!auditMostModified.isEmpty()) {
            g.drawString(font, "Most Modified: " + auditMostModified + " (" + auditMostModifiedCount + "x)", left + 6, y + 14, LABEL, false);
        }
        y += 28;

        // Audit entries table
        g.fill(left, y, left + w, y + 14, HEADER_BG);
        g.drawString(font, "Time", left + 4, y + 3, LABEL, false);
        g.drawString(font, "Player", left + 55, y + 3, LABEL, false);
        g.drawString(font, "Type", left + 130, y + 3, LABEL, false);
        g.drawString(font, "Amount", left + 210, y + 3, LABEL, false);
        g.drawString(font, "Description", left + 270, y + 3, LABEL, false);
        y += 15;

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        for (int i = 0; i < auditEntries.size(); i++) {
            AuditEntry e = auditEntries.get(i);
            boolean alt = i % 2 == 0;
            g.fill(left, y, left + w, y + 13, alt ? ROW_EVEN : ROW_ODD);

            String time = sdf.format(new Date(e.timestamp));
            g.drawString(font, time, left + 4, y + 2, LABEL, false);
            g.drawString(font, truncate(e.player, 10), left + 55, y + 2, TEXT, false);

            int typeColor = e.type.startsWith("ADMIN") ? WARNING : (e.type.contains("BUY") || e.type.contains("SPEND") ? ERROR : ACCENT);
            g.drawString(font, truncate(e.type, 10), left + 130, y + 2, typeColor, false);

            g.drawString(font, formatCoins(e.amount), left + 210, y + 2, e.amount >= 0 ? ACCENT : ERROR, false);
            g.drawString(font, truncate(e.description, 30), left + 270, y + 2, LABEL, false);
            y += 14;
        }

        if (auditEntries.isEmpty()) {
            g.fill(left, y, left + w, y + 20, CARD_BG);
            g.drawString(font, "No audit entries found.", left + 8, y + 6, LABEL, false);
            y += 22;
        }

        return y;
    }

    // ---- Click Handling ----

    public boolean mouseClicked(double mouseX, double mouseY, int button, int left, int top, int right, int bottom) {
        int mx = (int) mouseX;
        int my = (int) mouseY;

        // Sub-tab clicks
        for (int[] tb : subTabBounds) {
            if (mx >= tb[0] && mx < tb[0] + tb[2] && my >= tb[1] && my < tb[1] + tb[3]) {
                subTab = tb[4];
                scroll = 0;
                requestSubTabData();
                return true;
            }
        }

        // Action button clicks
        for (int[] ab : actionButtons) {
            if (mx >= ab[0] && mx < ab[0] + ab[2] && my >= ab[1] && my < ab[1] + ab[3]) {
                handleActionButton(ab[4]);
                return true;
            }
        }

        // Player row clicks (for future detail view)
        for (int[] pr : playerRowBounds) {
            if (mx >= pr[0] && mx < pr[0] + pr[2] && my >= pr[1] && my < pr[1] + pr[3]) {
                int idx = pr[4];
                if (idx >= 0 && idx < playerList.size()) {
                    String uuid = playerList.get(idx).uuid;
                    selectedPlayerUuid = uuid.equals(selectedPlayerUuid) ? null : uuid;
                }
                return true;
            }
        }

        return false;
    }

    private void handleActionButton(int actionId) {
        // Sort actions (100+)
        if (actionId >= 100 && actionId < 105) {
            String[] sortKeys = {"richest", "poorest", "wallet", "bank", "name"};
            playerSort = sortKeys[actionId - 100];
            scroll = 0;
            requestSubTabData();
            return;
        }

        // Bulk actions (200+)
        if (actionId >= 200 && actionId < 210) {
            JsonObject params = new JsonObject();
            switch (actionId) {
                case 200: params.addProperty("action", "give_all"); params.addProperty("amount", 100); params.addProperty("target", "wallet"); break;
                case 201: params.addProperty("action", "take_all"); params.addProperty("amount", 100); params.addProperty("target", "wallet"); break;
                case 202: params.addProperty("action", "give_all"); params.addProperty("amount", 1000); params.addProperty("target", "wallet"); break;
                case 203: params.addProperty("action", "take_all"); params.addProperty("amount", 1000); params.addProperty("target", "wallet"); break;
            }
            sendAction("eco_dashboard_bulk_action", params.toString());
            return;
        }

        // Shop config adjustments (310+)
        if (actionId >= 310 && actionId < 320) {
            if (actionId == 319) {
                // Force refresh shop rotation
                sendAction("eco_dashboard_refresh_shop", "{}");
                return;
            }
            JsonObject params = new JsonObject();
            switch (actionId) {
                case 310: params.addProperty("intervalTicks", Math.max(1000, shopInterval - 1000)); break;
                case 311: params.addProperty("intervalTicks", shopInterval + 1000); break;
                case 312: params.addProperty("intervalTicks", 24000); break;
                case 313: params.addProperty("priceMult", 0.5); break;
                case 314: params.addProperty("priceMult", 1.0); break;
                case 315: params.addProperty("priceMult", 2.0); break;
                case 316: params.addProperty("sellPct", 0.10); break;
                case 317: params.addProperty("sellPct", 0.20); break;
                case 318: params.addProperty("sellPct", 0.50); break;
            }
            sendAction("eco_dashboard_set_shop", params.toString());
            return;
        }

        // Shop item price adjustments (320+): each slot has 3 actions (down, up, x2)
        if (actionId >= 320 && actionId < 360) {
            int slot = (actionId - 320) / 3;
            int op = (actionId - 320) % 3; // 0=down, 1=up, 2=x2
            JsonObject params = new JsonObject();
            params.addProperty("slot", slot);
            params.addProperty("op", op == 0 ? "down" : (op == 1 ? "up" : "double"));
            sendAction("eco_dashboard_edit_shop_item", params.toString());
            return;
        }

        // Per-player wallet/bank edit (600+)
        if (actionId >= 600 && actionId < 614) {
            if (selectedPlayerUuid == null) return;
            PlayerEntry sel = null;
            for (PlayerEntry p : playerList) {
                if (p.uuid.equals(selectedPlayerUuid)) { sel = p; break; }
            }
            if (sel == null) return;

            String target;
            int newAmount;
            if (actionId < 607) {
                target = "wallet";
                newAmount = switch (actionId) {
                    case 600 -> sel.wallet + 100;
                    case 601 -> sel.wallet + 1000;
                    case 602 -> sel.wallet + 10000;
                    case 603 -> Math.max(0, sel.wallet - 100);
                    case 604 -> Math.max(0, sel.wallet - 1000);
                    case 605 -> Math.max(0, sel.wallet - 10000);
                    case 606 -> 0;
                    default -> sel.wallet;
                };
            } else {
                target = "bank";
                newAmount = switch (actionId) {
                    case 607 -> sel.bank + 100;
                    case 608 -> sel.bank + 1000;
                    case 609 -> sel.bank + 10000;
                    case 610 -> Math.max(0, sel.bank - 100);
                    case 611 -> Math.max(0, sel.bank - 1000);
                    case 612 -> Math.max(0, sel.bank - 10000);
                    case 613 -> 0;
                    default -> sel.bank;
                };
            }

            JsonObject params = new JsonObject();
            params.addProperty("action", "set_player");
            params.addProperty("uuid", selectedPlayerUuid);
            params.addProperty("target", target);
            params.addProperty("amount", newAmount);
            sendAction("eco_dashboard_bulk_action", params.toString());
            // Refresh player list to show updated values
            JsonObject refresh = new JsonObject();
            refresh.addProperty("sort", playerSort);
            refresh.addProperty("search", playerSearch);
            sendAction("eco_dashboard_players", refresh.toString());
            return;
        }

        // Mastery mark actions (500+)
        if (actionId >= 500 && actionId < 510) {
            switch (actionId) {
                case 500: masteryInputFocused = true; masteryAmountFocused = false; break;
                case 501: masteryInputFocused = false; masteryAmountFocused = true; break;
                case 502: // Grant
                    masteryInputFocused = false; masteryAmountFocused = false;
                    if (!masteryTargetPlayer.isEmpty() && !masteryGrantAmount.isEmpty()) {
                        JsonObject p = new JsonObject();
                        p.addProperty("player", masteryTargetPlayer);
                        p.addProperty("amount", Integer.parseInt(masteryGrantAmount));
                        p.addProperty("action", "grant");
                        sendAction("eco_mastery_modify", p.toString());
                    }
                    break;
                case 503: // Revoke
                    masteryInputFocused = false; masteryAmountFocused = false;
                    if (!masteryTargetPlayer.isEmpty() && !masteryGrantAmount.isEmpty()) {
                        JsonObject p = new JsonObject();
                        p.addProperty("player", masteryTargetPlayer);
                        p.addProperty("amount", Integer.parseInt(masteryGrantAmount));
                        p.addProperty("action", "revoke");
                        sendAction("eco_mastery_modify", p.toString());
                    }
                    break;
            }
            return;
        }

        // Audit filter actions (400+)
        if (actionId >= 400 && actionId < 410) {
            String[] filters = {"ALL", "SHOP_BUY", "SHOP_SELL", "TRADE", "ADMIN_ADD", "ADMIN_SET", "ADMIN_REMOVE"};
            if (actionId - 400 < filters.length) {
                auditTypeFilter = filters[actionId - 400];
                scroll = 0;
                requestSubTabData();
            }
            return;
        }
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scroll = Math.max(0, Math.min(maxScroll, scroll - (int)(scrollY * 12)));
        return true;
    }

    // ---- Rendering Helpers ----

    private void renderSectionHeader(GuiGraphics g, int left, int y, int w, String title) {
        g.fill(left, y, left + w, y + 16, HEADER_BG);
        drawHLine(g, left, left + w, y + 16, BORDER);
        g.drawString(font, title, left + 4, y + 4, ACCENT, false);
    }

    private void renderStatCard(GuiGraphics g, int x, int y, int w, int h, String label, String value, String subtext, int color, int mx, int my) {
        boolean hov = mx >= x && mx < x + w && my >= y && my < y + h;
        g.fill(x, y, x + w, y + h, hov ? 0xFF222230 : CARD_BG);
        drawBorder(g, x, y, x + w, y + h, hov ? color : BORDER);

        g.drawString(font, label, x + 4, y + 4, LABEL, false);
        g.drawString(font, value, x + 4, y + 16, color, false);
        if (subtext != null && !subtext.isEmpty()) {
            int stColor = subtext.startsWith("+") ? ACCENT : (subtext.startsWith("-") ? ERROR : LABEL);
            g.drawString(font, subtext, x + 4, y + 28, stColor, false);
        }
    }

    private void renderMiniBarChart(GuiGraphics g, int x, int y, int w, int h, int[] data) {
        int maxVal = 1;
        for (int d : data) maxVal = Math.max(maxVal, d);

        int barW = Math.max(2, (w - 4) / data.length);
        for (int i = 0; i < data.length; i++) {
            int barH = data[i] > 0 ? Math.max(1, (int)((double) data[i] / maxVal * (h - 4))) : 0;
            int bx = x + i * barW;
            int by = y + h - 2 - barH;
            int color = i < 3 ? ERROR : (i < 7 ? BLUE : ACCENT);
            if (barH > 0) g.fill(bx, by, bx + Math.max(1, barW - 1), y + h - 2, color);
        }
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

    private String truncate(String s, int maxLen) {
        return s.length() > maxLen ? s.substring(0, maxLen) + ".." : s;
    }

    private String formatCoins(int amount) {
        if (Math.abs(amount) >= 1000000) return String.format("%.1fM", amount / 1000000.0);
        if (Math.abs(amount) >= 1000) return String.format("%.1fK", amount / 1000.0);
        return String.valueOf(amount);
    }

    private void parseMasteryData(JsonObject root) {
        masteryPlayers.clear();
        masteryTotalMarks = getInt(root, "total_marks");
        masteryPlayerCount = getInt(root, "player_count");
        if (root.has("players")) {
            JsonArray arr = root.getAsJsonArray("players");
            for (JsonElement el : arr) {
                JsonObject p = el.getAsJsonObject();
                masteryPlayers.add(new MasteryPlayerEntry(
                    getString(p, "name"), getString(p, "uuid"),
                    getInt(p, "marks"), getInt(p, "prestige")
                ));
            }
        }
    }

    // ═══════════════════════ MASTERY MARKS TAB ═══════════════════════

    private int renderMastery(GuiGraphics g, int mx, int my, int left, int y, int w, int right) {
        // Header card
        g.fill(left, y, right, y + 36, CARD_BG);
        g.fill(left, y, right, y + 1, PURPLE);
        g.drawString(font, "\u2726 Mastery Marks Management", left + 6, y + 4, PURPLE, false);
        g.drawString(font, "Total in circulation: " + masteryTotalMarks + " marks across " + masteryPlayerCount + " players", left + 6, y + 16, LABEL, false);
        y += 42;

        // Grant marks section
        g.fill(left, y, right, y + CARD_HEIGHT, CARD_BG);
        g.drawString(font, "Grant / Revoke Marks", left + 6, y + 4, ACCENT, false);

        int inputW = 90;
        int amountW = 50;
        int inputX = left + 6;
        int amountX = inputX + inputW + 6;
        int inputY = y + 18;

        // Player name input
        g.fill(inputX, inputY, inputX + inputW, inputY + 14, masteryInputFocused ? 0xFF1E1E33 : 0xFF141420);
        g.fill(inputX, inputY + 13, inputX + inputW, inputY + 14, BORDER);
        String nameDisplay = masteryTargetPlayer.isEmpty() && !masteryInputFocused ? "Player..." : masteryTargetPlayer;
        g.drawString(font, nameDisplay, inputX + 3, inputY + 3, masteryTargetPlayer.isEmpty() ? 0xFF555566 : TEXT, false);
        actionButtons.add(new int[]{inputX, inputY, inputW, 14, 500}); // 500 = mastery name input

        // Amount input
        g.fill(amountX, inputY, amountX + amountW, inputY + 14, masteryAmountFocused ? 0xFF1E1E33 : 0xFF141420);
        g.fill(amountX, inputY + 13, amountX + amountW, inputY + 14, BORDER);
        String amtDisplay = masteryGrantAmount.isEmpty() && !masteryAmountFocused ? "Amount" : masteryGrantAmount;
        g.drawString(font, amtDisplay, amountX + 3, inputY + 3, masteryGrantAmount.isEmpty() ? 0xFF555566 : TEXT, false);
        actionButtons.add(new int[]{amountX, inputY, amountW, 14, 501}); // 401 = mastery amount input

        // Grant button
        int grantX = amountX + amountW + 6;
        int grantW = font.width("Grant") + 10;
        boolean grantHover = mx >= grantX && mx < grantX + grantW && my >= inputY && my < inputY + 14;
        g.fill(grantX, inputY, grantX + grantW, inputY + 14, grantHover ? 0xFF30363D : 0xFF21262D);
        g.drawString(font, "Grant", grantX + 5, inputY + 3, ACCENT, false);
        actionButtons.add(new int[]{grantX, inputY, grantW, 14, 502}); // 402 = grant

        // Revoke button
        int revokeX = grantX + grantW + 4;
        int revokeW = font.width("Revoke") + 10;
        boolean revokeHover = mx >= revokeX && mx < revokeX + revokeW && my >= inputY && my < inputY + 14;
        g.fill(revokeX, inputY, revokeX + revokeW, inputY + 14, revokeHover ? 0xFF30363D : 0xFF21262D);
        g.drawString(font, "Revoke", revokeX + 5, inputY + 3, ERROR, false);
        actionButtons.add(new int[]{revokeX, inputY, revokeW, 14, 503}); // 403 = revoke

        y += CARD_HEIGHT + SECTION_GAP;

        // Player list
        g.drawString(font, "Player Mastery Balances", left + 6, y, PURPLE, false);
        y += 14;

        // Table header
        g.fill(left, y, right, y + ROW_HEIGHT, HEADER_BG);
        g.drawString(font, "Player", left + 6, y + 4, LABEL, false);
        g.drawString(font, "Marks", right - 120, y + 4, LABEL, false);
        g.drawString(font, "Prestige", right - 60, y + 4, LABEL, false);
        y += ROW_HEIGHT;

        for (int i = 0; i < masteryPlayers.size(); i++) {
            MasteryPlayerEntry p = masteryPlayers.get(i);
            int rowBg = (i % 2 == 0) ? ROW_EVEN : ROW_ODD;
            g.fill(left, y, right, y + ROW_HEIGHT, rowBg);

            g.drawString(font, p.name(), left + 6, y + 4, TEXT, false);
            g.drawString(font, String.valueOf(p.marks()), right - 120, y + 4, PURPLE, false);
            g.drawString(font, String.valueOf(p.prestige()), right - 60, y + 4, GOLD, false);

            y += ROW_HEIGHT;
        }

        if (masteryPlayers.isEmpty()) {
            g.drawString(font, "No players with mastery marks", left + 6, y + 4, LABEL, false);
            y += ROW_HEIGHT;
        }

        return y;
    }

    public boolean handleMasteryInput(char ch) {
        if (masteryInputFocused && masteryTargetPlayer.length() < 16) {
            if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || (ch >= '0' && ch <= '9') || ch == '_') {
                masteryTargetPlayer += ch;
                return true;
            }
        }
        if (masteryAmountFocused && masteryGrantAmount.length() < 6) {
            if (ch >= '0' && ch <= '9') {
                masteryGrantAmount += ch;
                return true;
            }
        }
        return false;
    }

    public boolean handleMasteryBackspace() {
        if (masteryInputFocused && !masteryTargetPlayer.isEmpty()) {
            masteryTargetPlayer = masteryTargetPlayer.substring(0, masteryTargetPlayer.length() - 1);
            return true;
        }
        if (masteryAmountFocused && !masteryGrantAmount.isEmpty()) {
            masteryGrantAmount = masteryGrantAmount.substring(0, masteryGrantAmount.length() - 1);
            return true;
        }
        return false;
    }

    private int getInt(JsonObject obj, String key) {
        return obj.has(key) ? obj.get(key).getAsInt() : 0;
    }

    private double getDouble(JsonObject obj, String key) {
        return obj.has(key) ? obj.get(key).getAsDouble() : 0.0;
    }

    private String getString(JsonObject obj, String key) {
        return obj.has(key) ? obj.get(key).getAsString() : "";
    }
}

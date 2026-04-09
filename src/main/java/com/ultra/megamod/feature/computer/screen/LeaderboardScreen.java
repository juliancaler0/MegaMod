package com.ultra.megamod.feature.computer.screen;

import com.ultra.megamod.feature.computer.network.ComputerActionPayload;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.ui.UIHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class LeaderboardScreen extends Screen {
    private final Screen parent;
    private int scroll = 0;
    private String category = "Wealth";
    private List<LeaderboardEntry> entries = new ArrayList<>();
    private int myRank = -1;
    private int totalPlayers = 0;
    private boolean dataLoaded = false;
    private boolean waiting = false;

    private static final String[] CATEGORIES = {"Wealth", "Kills", "Dungeons", "Museum", "Skills", "Deaths", "Games", "Factions"};
    private static final String[] CATEGORY_ICONS = {"\u2726", "\u2694", "\u2620", "\u2302", "\u2605", "\u2620", "\u2663", "\u2691"};
    private static final int[] CATEGORY_COLORS = {
        0xFFFFD700, // Gold for Wealth
        0xFFFF4444, // Red for Kills
        0xFF9B59B6, // Purple for Dungeons
        0xFF3498DB, // Blue for Museum
        0xFF2ECC71, // Green for Skills
        0xFF95A5A6, // Grey for Deaths
        0xFFFF9800, // Orange for Games
        0xFF2E7D32  // Forest green for Factions
    };

    // Medal colors
    private static final int GOLD_MEDAL = 0xFFFFD700;
    private static final int SILVER_MEDAL = 0xFFC0C0C0;
    private static final int BRONZE_MEDAL = 0xFFCD7F32;
    private static final int SCORE_GREEN = 0xFF3FB950;
    private static final int TAB_SELECTED = 0xFF58A6FF;
    private static final int TAB_UNSELECTED = 0xFF8B949E;
    private static final int MY_ROW_BG = 0xFF1A3A5C;

    private static final int ROW_HEIGHT = 16;
    private static final int MAX_VISIBLE_ROWS = 12;

    private int titleBarH;
    private int contentTop;
    private int contentBottom;
    private int tableX;
    private int tableW;
    private int tableTop;
    private int tableBottom;

    // Button bounds
    private int backX, backY, backW, backH;
    private int refreshX, refreshY, refreshW, refreshH;
    private int[][] tabBounds;

    public record LeaderboardEntry(String name, String uuid, long score, String formattedScore) {}

    public LeaderboardScreen(Screen parent) {
        super((Component) Component.literal((String) "Leaderboards"));
        this.parent = parent;
    }

    protected void init() {
        super.init();
        Objects.requireNonNull(this.font);
        this.titleBarH = 9 + 16;
        this.contentTop = this.titleBarH + 4;
        this.contentBottom = this.height - 24;

        // Back button
        this.backW = 50;
        this.backH = 16;
        this.backX = 8;
        this.backY = (this.titleBarH - this.backH) / 2;

        // Refresh button
        this.refreshW = 60;
        this.refreshH = 16;
        this.refreshX = this.width - this.refreshW - 8;
        this.refreshY = (this.titleBarH - this.refreshH) / 2;

        // Tab bounds
        int tabW = 60;
        int tabH = 18;
        int tabGap = 2;
        int totalTabW = CATEGORIES.length * (tabW + tabGap) - tabGap;
        int tabStartX = (this.width - totalTabW) / 2;
        int tabY = this.contentTop;
        this.tabBounds = new int[CATEGORIES.length][4];
        for (int i = 0; i < CATEGORIES.length; i++) {
            this.tabBounds[i] = new int[]{tabStartX + i * (tabW + tabGap), tabY, tabW, tabH};
        }

        // Table area
        this.tableW = Math.min(400, this.width - 40);
        this.tableX = (this.width - this.tableW) / 2;
        this.tableTop = tabY + tabH + 6;
        this.tableBottom = this.contentBottom;

        // Request initial data
        this.requestData();
    }

    private void requestData() {
        this.dataLoaded = false;
        this.waiting = true;
        this.scroll = 0;
        ClientPacketDistributor.sendToServer(
            (CustomPacketPayload) new ComputerActionPayload("leaderboard_request", this.category),
            (CustomPacketPayload[]) new CustomPacketPayload[0]);
    }

    public void tick() {
        super.tick();
        ComputerDataPayload response = ComputerDataPayload.lastResponse;
        if (response != null && "leaderboard_data".equals(response.dataType())) {
            ComputerDataPayload.lastResponse = null;
            this.parseResponse(response.jsonData());
            this.dataLoaded = true;
            this.waiting = false;
        }
        // Consume error responses so the screen doesn't stay stuck
        if (response != null && "error".equals(response.dataType())) {
            ComputerDataPayload.lastResponse = null;
            this.dataLoaded = true;
        }
    }

    private void parseResponse(String json) {
        this.entries.clear();
        this.myRank = -1;
        this.totalPlayers = 0;

        if (json == null || json.length() < 3) {
            return;
        }

        try {
            // Parse category
            String catKey = "\"category\":\"";
            int catIdx = json.indexOf(catKey);
            if (catIdx >= 0) {
                int catStart = catIdx + catKey.length();
                int catEnd = json.indexOf('"', catStart);
                if (catEnd > catStart) {
                    this.category = json.substring(catStart, catEnd);
                }
            }

            // Parse myRank
            String rankKey = "\"myRank\":";
            int rankIdx = json.indexOf(rankKey);
            if (rankIdx >= 0) {
                int rankStart = rankIdx + rankKey.length();
                int rankEnd = findNumberEnd(json, rankStart);
                this.myRank = Integer.parseInt(json.substring(rankStart, rankEnd).trim());
            }

            // Parse totalPlayers
            String totalKey = "\"totalPlayers\":";
            int totalIdx = json.indexOf(totalKey);
            if (totalIdx >= 0) {
                int totalStart = totalIdx + totalKey.length();
                int totalEnd = findNumberEnd(json, totalStart);
                this.totalPlayers = Integer.parseInt(json.substring(totalStart, totalEnd).trim());
            }

            // Parse entries array
            String entriesKey = "\"entries\":[";
            int entriesIdx = json.indexOf(entriesKey);
            if (entriesIdx < 0) return;
            int arrStart = entriesIdx + entriesKey.length();

            // Find matching ]
            int depth = 1;
            int pos = arrStart;
            while (pos < json.length() && depth > 0) {
                char c = json.charAt(pos);
                if (c == '[') depth++;
                else if (c == ']') depth--;
                pos++;
            }
            String arrContent = json.substring(arrStart, pos - 1);

            // Parse each entry object
            int searchFrom = 0;
            while (searchFrom < arrContent.length()) {
                int objStart = arrContent.indexOf('{', searchFrom);
                if (objStart < 0) break;
                int objEnd = arrContent.indexOf('}', objStart);
                if (objEnd < 0) break;
                String obj = arrContent.substring(objStart, objEnd + 1);
                searchFrom = objEnd + 1;

                String name = extractStringField(obj, "name");
                String uuid = extractStringField(obj, "uuid");
                String formatted = extractStringField(obj, "formatted");
                long score = extractLongField(obj, "score");

                if (name != null && formatted != null) {
                    this.entries.add(new LeaderboardEntry(name, uuid != null ? uuid : "", score, formatted));
                }
            }
        } catch (Exception e) {
            // Silently handle parse errors
        }
    }

    private static int findNumberEnd(String s, int start) {
        int i = start;
        while (i < s.length()) {
            char c = s.charAt(i);
            if (c != '-' && c != '.' && !Character.isDigit(c)) break;
            i++;
        }
        return i;
    }

    private static String extractStringField(String obj, String field) {
        String key = "\"" + field + "\":\"";
        int idx = obj.indexOf(key);
        if (idx < 0) return null;
        int start = idx + key.length();
        int end = obj.indexOf('"', start);
        if (end < 0) return null;
        return obj.substring(start, end);
    }

    private static long extractLongField(String obj, String field) {
        String key = "\"" + field + "\":";
        int idx = obj.indexOf(key);
        if (idx < 0) return 0;
        int start = idx + key.length();
        int end = findNumberEnd(obj, start);
        try {
            return Long.parseLong(obj.substring(start, end).trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Background
        g.fill(0, 0, this.width, this.height, 0xFF0D1117);
        UIHelper.drawPanel(g, 0, 0, this.width, this.height);

        // Title bar
        UIHelper.drawTitleBar(g, 0, 0, this.width, this.titleBarH);
        Objects.requireNonNull(this.font);
        int titleY = (this.titleBarH - 9) / 2;
        UIHelper.drawCenteredTitle(g, this.font, "Leaderboards", this.width / 2, titleY);

        // Back button
        boolean backHover = mouseX >= this.backX && mouseX < this.backX + this.backW
                && mouseY >= this.backY && mouseY < this.backY + this.backH;
        UIHelper.drawButton(g, this.backX, this.backY, this.backW, this.backH, backHover);
        int backTextX = this.backX + (this.backW - this.font.width("< Back")) / 2;
        Objects.requireNonNull(this.font);
        g.drawString(this.font, "< Back", backTextX, this.backY + (this.backH - 9) / 2, UIHelper.CREAM_TEXT, false);

        // Refresh button
        boolean refreshHover = mouseX >= this.refreshX && mouseX < this.refreshX + this.refreshW
                && mouseY >= this.refreshY && mouseY < this.refreshY + this.refreshH;
        UIHelper.drawButton(g, this.refreshX, this.refreshY, this.refreshW, this.refreshH, refreshHover);
        String refreshLabel = this.waiting ? "..." : "Refresh";
        int refreshTextX = this.refreshX + (this.refreshW - this.font.width(refreshLabel)) / 2;
        Objects.requireNonNull(this.font);
        g.drawString(this.font, refreshLabel, refreshTextX, this.refreshY + (this.refreshH - 9) / 2,
                UIHelper.CREAM_TEXT, false);

        // Category tabs
        this.renderTabs(g, mouseX, mouseY);

        if (!this.dataLoaded) {
            // Loading state
            int loadW = 180;
            Objects.requireNonNull(this.font);
            int loadH = 9 + 20;
            int loadX = (this.width - loadW) / 2;
            int loadY = (this.tableTop + this.tableBottom) / 2 - loadH / 2;
            UIHelper.drawPanel(g, loadX, loadY, loadW, loadH);
            Objects.requireNonNull(this.font);
            UIHelper.drawCenteredLabel(g, this.font, "Loading leaderboard...",
                    this.width / 2, loadY + (loadH - 9) / 2);
        } else if (this.entries.isEmpty()) {
            Objects.requireNonNull(this.font);
            UIHelper.drawCenteredLabel(g, this.font, "No data available",
                    this.width / 2, (this.tableTop + this.tableBottom) / 2 - 4);
        } else {
            this.renderTable(g, mouseX, mouseY);
        }

        // Footer: player's own rank
        if (this.dataLoaded && this.myRank > 0) {
            int footerY = this.contentBottom + 4;
            UIHelper.drawCard(g, this.tableX, footerY, this.tableW, 16, false);
            String rankStr = "Your Rank: #" + this.myRank + " / " + this.totalPlayers + " players";
            int rankW = this.font.width(rankStr);
            Objects.requireNonNull(this.font);
            g.drawString(this.font, rankStr, this.tableX + (this.tableW - rankW) / 2,
                    footerY + (16 - 9) / 2, TAB_SELECTED, false);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void renderTabs(GuiGraphics g, int mouseX, int mouseY) {
        for (int i = 0; i < CATEGORIES.length; i++) {
            int[] b = this.tabBounds[i];
            boolean selected = CATEGORIES[i].equals(this.category);
            boolean hovered = mouseX >= b[0] && mouseX < b[0] + b[2]
                    && mouseY >= b[1] && mouseY < b[1] + b[3];

            int accentColor = CATEGORY_COLORS[i];
            UIHelper.drawTab(g, b[0], b[1], b[2], b[3], selected, accentColor);

            // Tab label
            String label = CATEGORIES[i];
            int labelW = this.font.width(label);
            Objects.requireNonNull(this.font);
            int labelY = b[1] + (b[3] - 9) / 2;
            int labelColor = selected ? TAB_SELECTED : (hovered ? UIHelper.CREAM_TEXT : TAB_UNSELECTED);
            g.drawString(this.font, label, b[0] + (b[2] - labelW) / 2, labelY, labelColor, false);
        }
    }

    private void renderTable(GuiGraphics g, int mouseX, int mouseY) {
        // Table header
        int headerY = this.tableTop;
        int headerH = 14;
        g.fill(this.tableX, headerY, this.tableX + this.tableW, headerY + headerH, 0xFF21262D);
        g.fill(this.tableX, headerY + headerH - 1, this.tableX + this.tableW, headerY + headerH, 0xFF30363D);

        int rankColX = this.tableX + 8;
        int nameColX = this.tableX + 60;
        int scoreColX = this.tableX + this.tableW - 8;

        Objects.requireNonNull(this.font);
        int headerTextY = headerY + (headerH - 9) / 2;
        g.drawString(this.font, "Rank", rankColX, headerTextY, TAB_UNSELECTED, false);
        g.drawString(this.font, "Player", nameColX, headerTextY, TAB_UNSELECTED, false);
        String scoreHeader = getScoreHeader();
        int scoreHW = this.font.width(scoreHeader);
        g.drawString(this.font, scoreHeader, scoreColX - scoreHW, headerTextY, TAB_UNSELECTED, false);

        // Table rows
        int rowAreaTop = headerY + headerH;
        int maxVisible = Math.min(MAX_VISIBLE_ROWS, (this.tableBottom - rowAreaTop) / ROW_HEIGHT);
        int maxScroll = Math.max(0, this.entries.size() - maxVisible);
        if (this.scroll > maxScroll) this.scroll = maxScroll;

        // Scissor/clip: we'll just draw within bounds
        String currentPlayerName = Minecraft.getInstance().player != null
                ? Minecraft.getInstance().player.getGameProfile().name() : "";

        for (int i = 0; i < maxVisible && (i + this.scroll) < this.entries.size(); i++) {
            int entryIdx = i + this.scroll;
            LeaderboardEntry entry = this.entries.get(entryIdx);
            int rank = entryIdx + 1;
            int rowY = rowAreaTop + i * ROW_HEIGHT;

            // Row background
            boolean isCurrentPlayer = entry.name().equals(currentPlayerName);
            boolean isEven = i % 2 == 0;

            if (isCurrentPlayer) {
                g.fill(this.tableX, rowY, this.tableX + this.tableW, rowY + ROW_HEIGHT, MY_ROW_BG);
                g.fill(this.tableX, rowY, this.tableX + 2, rowY + ROW_HEIGHT, TAB_SELECTED);
            } else {
                UIHelper.drawRowBg(g, this.tableX, rowY, this.tableW, ROW_HEIGHT, isEven);
            }

            Objects.requireNonNull(this.font);
            int textY = rowY + (ROW_HEIGHT - 9) / 2;

            // Rank column with medal colors
            String rankStr;
            int rankColor;
            if (rank == 1) {
                rankStr = "\u2726 #1";
                rankColor = GOLD_MEDAL;
            } else if (rank == 2) {
                rankStr = "\u2726 #2";
                rankColor = SILVER_MEDAL;
            } else if (rank == 3) {
                rankStr = "\u2726 #3";
                rankColor = BRONZE_MEDAL;
            } else {
                rankStr = "#" + rank;
                rankColor = TAB_UNSELECTED;
            }
            g.drawString(this.font, rankStr, rankColX, textY, rankColor, false);

            // Player name
            int nameColor = isCurrentPlayer ? 0xFFFFFFFF : UIHelper.CREAM_TEXT;
            g.drawString(this.font, entry.name(), nameColX, textY, nameColor, false);

            // Score
            int scoreW = this.font.width(entry.formattedScore());
            g.drawString(this.font, entry.formattedScore(), scoreColX - scoreW, textY, SCORE_GREEN, false);
        }

        // Scrollbar
        if (this.entries.size() > maxVisible) {
            float scrollProgress = maxScroll > 0 ? (float) this.scroll / maxScroll : 0f;
            int scrollBarX = this.tableX + this.tableW + 2;
            int scrollBarH = this.tableBottom - rowAreaTop;
            UIHelper.drawScrollbar(g, scrollBarX, rowAreaTop, scrollBarH, scrollProgress);
        }

        // Bottom border
        g.fill(this.tableX, this.tableBottom, this.tableX + this.tableW, this.tableBottom + 1, 0xFF30363D);
    }

    private String getScoreHeader() {
        switch (this.category) {
            case "Wealth": return "MegaCoins";
            case "Kills": return "Kills";
            case "Dungeons": return "Clears";
            case "Museum": return "Completion";
            case "Skills": return "Total Level";
            case "Deaths": return "Deaths";
            case "Games": return "High Score";
            default: return "Score";
        }
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean consumed) {
        if (consumed) {
            return super.mouseClicked(event, consumed);
        }
        int mx = (int) event.x();
        int my = (int) event.y();

        // Back button
        if (mx >= this.backX && mx < this.backX + this.backW
                && my >= this.backY && my < this.backY + this.backH) {
            if (this.minecraft != null) {
                this.minecraft.setScreen(this.parent);
            }
            return true;
        }

        // Refresh button
        if (mx >= this.refreshX && mx < this.refreshX + this.refreshW
                && my >= this.refreshY && my < this.refreshY + this.refreshH) {
            if (!this.waiting) {
                this.requestData();
            }
            return true;
        }

        // Category tabs
        for (int i = 0; i < CATEGORIES.length; i++) {
            int[] b = this.tabBounds[i];
            if (mx >= b[0] && mx < b[0] + b[2] && my >= b[1] && my < b[1] + b[3]) {
                if (!CATEGORIES[i].equals(this.category)) {
                    this.category = CATEGORIES[i];
                    this.requestData();
                }
                return true;
            }
        }

        return super.mouseClicked(event, consumed);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (verticalAmount < 0) {
            int maxVisible = Math.min(MAX_VISIBLE_ROWS,
                    (this.tableBottom - (this.tableTop + 14)) / ROW_HEIGHT);
            int maxScroll = Math.max(0, this.entries.size() - maxVisible);
            if (this.scroll < maxScroll) {
                this.scroll++;
            }
        } else if (verticalAmount > 0) {
            if (this.scroll > 0) {
                this.scroll--;
            }
        }
        return true;
    }

    public boolean isPauseScreen() {
        return false;
    }
}

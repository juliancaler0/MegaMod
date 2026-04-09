package com.ultra.megamod.feature.casino.screen.panels;

import com.ultra.megamod.feature.computer.network.ComputerActionPayload;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class CasinoManagerPanel {

    // Dark GitHub theme matching FeatureTogglesPanel
    private static final int BG_COLOR = 0xFF0D1117;
    private static final int HEADER_COLOR = 0xFF161B22;
    private static final int BORDER_COLOR = 0xFF30363D;
    private static final int TEXT_COLOR = 0xFFE6EDF3;
    private static final int LABEL_COLOR = 0xFF8B949E;
    private static final int ACCENT_BLUE = 0xFF58A6FF;
    private static final int ACCENT_GOLD = 0xFFD29922;
    private static final int TOGGLE_ON = 0xFF3FB950;
    private static final int TOGGLE_OFF = 0xFFF85149;
    private static final int CARD_BG = 0xFF161B22;
    private static final int CARD_HOVER = 0xFF1C2128;
    private static final int WARNING_YELLOW = 0xFFE3B341;
    private static final int DANGER_RED = 0xFFF85149;

    private static final String[] TABS = {"Dashboard", "Active Games", "Always Win"};
    private static final int TAB_HEIGHT = 18;

    private final Font font;
    private int selectedTab = 0;

    // Dashboard data
    private long totalWagered = 0;
    private long houseProfit = 0;
    private int activeGames = 0;
    private boolean dataLoaded = false;

    // Per-game Always Win state
    private boolean alwaysWinSlots = false;
    private boolean alwaysWinBlackjack = false;
    private boolean alwaysWinWheel = false;
    private boolean alwaysWinRoulette = false;
    private boolean alwaysWinCraps = false;
    private boolean alwaysWinBaccarat = false;
    private boolean alwaysWinPayoutOverride = false; // sub-toggle: override payout even if admin stands early

    public CasinoManagerPanel(Font font) {
        this.font = font;
    }

    public void render(GuiGraphics g, int mouseX, int mouseY, int left, int top, int right, int bottom) {
        int panelW = right - left;
        int panelH = bottom - top;

        // Background fill
        g.fill(left, top, right, bottom, BG_COLOR);

        // --- Header ---
        int headerY = top;
        int headerH = 20;
        g.fill(left, headerY, right, headerY + headerH, HEADER_COLOR);
        g.fill(left, headerY + headerH - 1, right, headerY + headerH, BORDER_COLOR);

        g.drawString(font, "Casino Manager", left + 6, headerY + 6, TEXT_COLOR, false);

        // Status indicator
        String statusStr = dataLoaded ? "Connected" : "Loading...";
        int statusColor = dataLoaded ? TOGGLE_ON : LABEL_COLOR;
        int statusW = font.width(statusStr);
        g.drawString(font, statusStr, right - statusW - 6, headerY + 6, statusColor, false);

        int yOffset = headerY + headerH + 2;

        // --- Sub-tabs ---
        int tabX = left + 4;
        int tabY = yOffset;
        for (int i = 0; i < TABS.length; i++) {
            String tab = TABS[i];
            int tw = font.width(tab) + 10;
            boolean isSelected = selectedTab == i;
            boolean isHovered = mouseX >= tabX && mouseX < tabX + tw
                    && mouseY >= tabY && mouseY < tabY + TAB_HEIGHT;

            int bgCol = isSelected ? ACCENT_BLUE : (isHovered ? 0xFF21262D : HEADER_COLOR);
            int txtCol = isSelected ? 0xFF000000 : TEXT_COLOR;

            g.fill(tabX, tabY, tabX + tw, tabY + TAB_HEIGHT, bgCol);
            if (!isSelected) {
                drawRectOutline(g, tabX, tabY, tabX + tw, tabY + TAB_HEIGHT, BORDER_COLOR);
            }
            g.drawString(font, tab, tabX + 5, tabY + 5, txtCol, false);
            tabX += tw + 3;
        }

        yOffset = tabY + TAB_HEIGHT + 6;

        // --- Content area ---
        switch (selectedTab) {
            case 0 -> renderDashboard(g, mouseX, mouseY, left, yOffset, right, bottom);
            case 1 -> renderActiveGames(g, mouseX, mouseY, left, yOffset, right, bottom);
            case 2 -> renderAlwaysWin(g, mouseX, mouseY, left, yOffset, right, bottom);
        }
    }

    private void renderDashboard(GuiGraphics g, int mouseX, int mouseY, int left, int top, int right, int bottom) {
        int panelW = right - left;
        int cardW = (panelW - 20) / 2;
        int cardH = 50;
        int cardGap = 8;

        if (!dataLoaded) {
            String loadStr = "Loading casino data...";
            int loadW = font.width(loadStr);
            g.drawString(font, loadStr, left + (panelW - loadW) / 2, top + 30, LABEL_COLOR, false);
            return;
        }

        // Card 1: Total Wagered
        int card1X = left + 6;
        int card1Y = top;
        renderStatCard(g, card1X, card1Y, cardW, cardH, "Total MegaCoins Wagered",
                formatNumber(totalWagered) + " MC", ACCENT_GOLD, mouseX, mouseY);

        // Card 2: House Profit/Loss
        int card2X = left + 6 + cardW + cardGap;
        int card2Y = top;
        String profitStr = (houseProfit >= 0 ? "+" : "") + formatNumber(houseProfit) + " MC";
        int profitColor = houseProfit >= 0 ? TOGGLE_ON : DANGER_RED;
        renderStatCard(g, card2X, card2Y, cardW, cardH, "House Profit/Loss",
                profitStr, profitColor, mouseX, mouseY);

        // Card 3: Active Games
        int card3X = left + 6;
        int card3Y = top + cardH + cardGap;
        renderStatCard(g, card3X, card3Y, cardW, cardH, "Active Games",
                String.valueOf(activeGames), ACCENT_BLUE, mouseX, mouseY);

        // Card 4: House Edge Info
        int card4X = left + 6 + cardW + cardGap;
        int card4Y = top + cardH + cardGap;
        renderStatCard(g, card4X, card4Y, cardW, cardH, "House Edge",
                "~4.2%", LABEL_COLOR, mouseX, mouseY);
    }

    private void renderStatCard(GuiGraphics g, int x, int y, int w, int h,
                                String label, String value, int valueColor, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
        int bg = hovered ? CARD_HOVER : CARD_BG;
        g.fill(x, y, x + w, y + h, bg);
        drawRectOutline(g, x, y, x + w, y + h, BORDER_COLOR);

        g.drawString(font, label, x + 8, y + 6, LABEL_COLOR, false);

        // Large value text
        g.drawString(font, value, x + 8, y + 22, valueColor, false);
    }

    private void renderActiveGames(GuiGraphics g, int mouseX, int mouseY, int left, int top, int right, int bottom) {
        int panelW = right - left;

        // Placeholder for active games list
        g.fill(left + 6, top, right - 6, top + 30, CARD_BG);
        drawRectOutline(g, left + 6, top, right - 6, top + 30, BORDER_COLOR);

        String emptyStr = "No active games";
        int emptyW = font.width(emptyStr);
        g.drawString(font, emptyStr, left + (panelW - emptyW) / 2, top + 10, LABEL_COLOR, false);

        // Info text
        int infoY = top + 40;
        g.drawString(font, "Active games will appear here when", left + 6, infoY, LABEL_COLOR, false);
        g.drawString(font, "players are using casino machines.", left + 6, infoY + 12, LABEL_COLOR, false);
    }

    private void renderAlwaysWin(GuiGraphics g, int mouseX, int mouseY, int left, int top, int right, int bottom) {
        int panelW = right - left;

        // Warning header
        g.fill(left + 6, top, right - 6, top + 22, 0xFF2D1A00);
        drawRectOutline(g, left + 6, top, right - 6, top + 22, WARNING_YELLOW);
        g.drawString(font, "! Always Win Mode (Per-Game)", left + 12, top + 7, WARNING_YELLOW, false);

        int yOffset = top + 30;

        g.drawString(font, "Toggle always-win for each game independently:", left + 6, yOffset, LABEL_COLOR, false);
        yOffset += 16;

        // 7 toggle rows
        yOffset = drawGameToggle(g, mouseX, mouseY, left + 6, yOffset, panelW - 12,
                "Slots", "Always jackpot (Diamond 500x)", alwaysWinSlots, "casino_admin_always_win_slots");
        yOffset += 4;
        yOffset = drawGameToggle(g, mouseX, mouseY, left + 6, yOffset, panelW - 12,
                "Blackjack", "Rigged cards: always reach 21", alwaysWinBlackjack, "casino_admin_always_win_blackjack");
        yOffset += 4;
        yOffset = drawGameToggle(g, mouseX, mouseY, left + 6, yOffset, panelW - 12,
                "  \u2514 Payout Override", "Win even if you stand early", alwaysWinPayoutOverride, "casino_admin_always_win_payout_override");
        yOffset += 4;
        yOffset = drawGameToggle(g, mouseX, mouseY, left + 6, yOffset, panelW - 12,
                "Wheel", "Always lands on your bet", alwaysWinWheel, "casino_admin_always_win_wheel");
        yOffset += 4;
        yOffset = drawGameToggle(g, mouseX, mouseY, left + 6, yOffset, panelW - 12,
                "Roulette", "Ball always lands on your bet", alwaysWinRoulette, "casino_admin_always_win_roulette");
        yOffset += 4;
        yOffset = drawGameToggle(g, mouseX, mouseY, left + 6, yOffset, panelW - 12,
                "Craps", "Dice rigged to always win", alwaysWinCraps, "casino_admin_always_win_craps");
        yOffset += 4;
        yOffset = drawGameToggle(g, mouseX, mouseY, left + 6, yOffset, panelW - 12,
                "Baccarat", "Your chosen side always wins", alwaysWinBaccarat, "casino_admin_always_win_baccarat");
    }

    private int drawGameToggle(GuiGraphics g, int mouseX, int mouseY, int x, int y, int w,
                                String gameName, String desc, boolean enabled, String actionId) {
        int rowH = 28;
        g.fill(x, y, x + w, y + rowH, CARD_BG);
        drawRectOutline(g, x, y, x + w, y + rowH, BORDER_COLOR);

        // Game name
        g.drawString(font, gameName, x + 6, y + 4, TEXT_COLOR, false);
        // Description
        g.drawString(font, desc, x + 6, y + 16, LABEL_COLOR, false);

        // Toggle button on right
        int btnW = 60;
        int btnH = 16;
        int btnX = x + w - btnW - 6;
        int btnY = y + (rowH - btnH) / 2;
        boolean hover = mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY && mouseY < btnY + btnH;

        int bgColor = enabled ? (hover ? 0xFF2D8A3E : TOGGLE_ON) : (hover ? 0xFFB83A33 : TOGGLE_OFF);
        g.fill(btnX, btnY, btnX + btnW, btnY + btnH, bgColor);
        String label = enabled ? "ON" : "OFF";
        int labelW = font.width(label);
        g.drawString(font, label, btnX + (btnW - labelW) / 2, btnY + (btnH - 9) / 2, 0xFFFFFFFF, false);

        return y + rowH;
    }

    public boolean mouseClicked(int mx, int my, int button, int left, int top, int right, int bottom) {
        if (button != 0) return false;

        int headerH = 20;
        int headerY = top;
        int yOffset = headerY + headerH + 2;

        // --- Tab clicks ---
        int tabX = left + 4;
        int tabY = yOffset;
        for (int i = 0; i < TABS.length; i++) {
            String tab = TABS[i];
            int tw = font.width(tab) + 10;
            if (mx >= tabX && mx < tabX + tw && my >= tabY && my < tabY + TAB_HEIGHT) {
                selectedTab = i;
                return true;
            }
            tabX += tw + 3;
        }

        yOffset = tabY + TAB_HEIGHT + 6;

        // --- Per-game Always Win toggles ---
        if (selectedTab == 2) {
            int panelW = right - left;
            int rowH = 28;
            int rowGap = 6;
            int baseY = yOffset + 30 + 16; // matches renderAlwaysWin layout
            int btnW = 60;
            int btnH = 16;
            int rowW = panelW - 12;

            // 7 game toggles
            String[] actions = {
                "casino_admin_always_win_slots", "casino_admin_always_win_blackjack",
                "casino_admin_always_win_payout_override", "casino_admin_always_win_wheel",
                "casino_admin_always_win_roulette", "casino_admin_always_win_craps",
                "casino_admin_always_win_baccarat"
            };
            for (int i = 0; i < actions.length; i++) {
                int rowY = baseY + i * (rowH + rowGap - 2); // tighter spacing
                int btnX = left + 6 + rowW - btnW - 6;
                int btnY2 = rowY + (rowH - btnH) / 2;
                if (mx >= btnX && mx < btnX + btnW && my >= btnY2 && my < btnY2 + btnH) {
                    switch (i) {
                        case 0 -> alwaysWinSlots = !alwaysWinSlots;
                        case 1 -> alwaysWinBlackjack = !alwaysWinBlackjack;
                        case 2 -> alwaysWinPayoutOverride = !alwaysWinPayoutOverride;
                        case 3 -> alwaysWinWheel = !alwaysWinWheel;
                        case 4 -> alwaysWinRoulette = !alwaysWinRoulette;
                        case 5 -> alwaysWinCraps = !alwaysWinCraps;
                        case 6 -> alwaysWinBaccarat = !alwaysWinBaccarat;
                    }
                    ClientPacketDistributor.sendToServer(
                        (CustomPacketPayload) new ComputerActionPayload(actions[i], ""),
                        (CustomPacketPayload[]) new CustomPacketPayload[0]
                    );
                    return true;
                }
            }
        }

        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    public void tick() {
        // Poll for responses
        ComputerDataPayload response = ComputerDataPayload.lastResponse;
        if (response != null) {
            if ("casino_admin_data".equals(response.dataType())) {
                ComputerDataPayload.lastResponse = null;
                handleResponse(response.dataType(), response.jsonData());
            } else if ("casino_admin_always_win_status".equals(response.dataType())) {
                ComputerDataPayload.lastResponse = null;
                handleResponse(response.dataType(), response.jsonData());
            }
        }
    }

    public void requestData() {
        ClientPacketDistributor.sendToServer(
            (CustomPacketPayload) new ComputerActionPayload("casino_admin_request", ""),
            (CustomPacketPayload[]) new CustomPacketPayload[0]
        );
    }

    public void handleResponse(String dataType, String json) {
        try {
            if ("casino_admin_data".equals(dataType)) {
                JsonObject root = JsonParser.parseString(json).getAsJsonObject();
                this.totalWagered = root.has("totalWagered") ? root.get("totalWagered").getAsLong() : 0;
                this.houseProfit = root.has("houseProfit") ? root.get("houseProfit").getAsLong() : 0;
                this.activeGames = root.has("activeGames") ? root.get("activeGames").getAsInt() : 0;
                parseAlwaysWinFlags(root);
                this.dataLoaded = true;
            } else if ("casino_admin_always_win_status".equals(dataType)) {
                JsonObject root = JsonParser.parseString(json).getAsJsonObject();
                parseAlwaysWinFlags(root);
                this.dataLoaded = true;
            }
        } catch (Exception e) {
            // Parse error, ignore
        }
    }

    private void parseAlwaysWinFlags(JsonObject root) {
        this.alwaysWinSlots = root.has("alwaysWinSlots") && root.get("alwaysWinSlots").getAsBoolean();
        this.alwaysWinBlackjack = root.has("alwaysWinBlackjack") && root.get("alwaysWinBlackjack").getAsBoolean();
        this.alwaysWinWheel = root.has("alwaysWinWheel") && root.get("alwaysWinWheel").getAsBoolean();
        this.alwaysWinRoulette = root.has("alwaysWinRoulette") && root.get("alwaysWinRoulette").getAsBoolean();
        this.alwaysWinCraps = root.has("alwaysWinCraps") && root.get("alwaysWinCraps").getAsBoolean();
        this.alwaysWinBaccarat = root.has("alwaysWinBaccarat") && root.get("alwaysWinBaccarat").getAsBoolean();
        this.alwaysWinPayoutOverride = root.has("alwaysWinPayoutOverride") && root.get("alwaysWinPayoutOverride").getAsBoolean();
    }

    // --- Helpers ---

    private static String formatNumber(long value) {
        if (value >= 1_000_000) {
            return String.format("%.1fM", value / 1_000_000.0);
        } else if (value >= 1_000) {
            return String.format("%.1fK", value / 1_000.0);
        }
        return String.valueOf(value);
    }

    private static void drawRectOutline(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        g.fill(x1, y1, x2, y1 + 1, color);
        g.fill(x1, y2 - 1, x2, y2, color);
        g.fill(x1, y1, x1 + 1, y2, color);
        g.fill(x2 - 1, y1, x2, y2, color);
    }
}

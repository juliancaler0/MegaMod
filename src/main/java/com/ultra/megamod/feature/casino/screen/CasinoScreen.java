package com.ultra.megamod.feature.casino.screen;

import com.ultra.megamod.feature.computer.network.ComputerActionPayload;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.ui.UIHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class CasinoScreen extends Screen {

    private final Screen parent;
    private boolean dataLoaded = false;

    // Stats fields parsed from server JSON
    private int totalWagered = 0;
    private int totalWon = 0;
    private int totalLost = 0;
    private int gamesPlayed = 0;
    private int biggestWin = 0;
    private int profitLoss = 0;
    private int slotsPlayed = 0;
    private int slotsWon = 0;
    private int blackjackPlayed = 0;
    private int blackjackWon = 0;
    private int wheelPlayed = 0;
    private int wheelWon = 0;
    private int roulettePlayed = 0;
    private int rouletteWon = 0;
    private int crapsPlayed = 0;
    private int crapsWon = 0;
    private int baccaratPlayed = 0;
    private int baccaratWon = 0;

    // Layout
    private int titleBarH;
    private int backX;
    private int backY;
    private int backW;
    private int backH;
    private final List<ClickRect> clickRects = new ArrayList<>();

    // Colors
    private static final int BG_COLOR = 0xFF0E0E18;
    private static final int GOLD_ACCENT = 0xFFD4AF37;
    private static final int GOLD_DARK = 0xFF8B7535;
    private static final int PROFIT_GREEN = 0xFF3FB950;
    private static final int LOSS_RED = 0xFFF85149;
    private static final int TEXT_PRIMARY = 0xFFCCCCDD;
    private static final int TEXT_DIM = 0xFF666677;
    private static final int SECTION_BG = 0xFF161B22;
    private static final int SECTION_BORDER = 0xFF30363D;
    private static final int CASINO_BTN_BG = 0xFFD4AF37;
    private static final int CASINO_BTN_HOVER = 0xFFE5C348;
    private static final int CASINO_BTN_TEXT = 0xFF000000;

    // Pulse animation
    private int animTick = 0;

    public CasinoScreen(Screen parent) {
        super(Component.literal("Casino"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        Objects.requireNonNull(this.font);
        this.titleBarH = 9 + 16;
        this.backW = 50;
        this.backH = 16;
        this.backX = 8;
        this.backY = (this.titleBarH - this.backH) / 2;
        ClientPacketDistributor.sendToServer(
            (CustomPacketPayload) new ComputerActionPayload("casino_request", ""),
            (CustomPacketPayload[]) new CustomPacketPayload[0]
        );
    }

    @Override
    public void tick() {
        super.tick();
        this.animTick++;

        ComputerDataPayload response = ComputerDataPayload.lastResponse;
        if (response != null && "casino_stats".equals(response.dataType())) {
            ComputerDataPayload.lastResponse = null;
            parseStats(response.jsonData());
            this.dataLoaded = true;
        }
        // Consume error responses so the screen doesn't stay stuck
        if (response != null && "error".equals(response.dataType())) {
            ComputerDataPayload.lastResponse = null;
            this.dataLoaded = true;
        }
        // Auto-retry if data hasn't loaded
        if (!this.dataLoaded && this.animTick % 60 == 0 && this.animTick > 0) {
            ClientPacketDistributor.sendToServer(
                (CustomPacketPayload) new ComputerActionPayload("casino_request", ""),
                (CustomPacketPayload[]) new CustomPacketPayload[0]
            );
        }
    }

    private void parseStats(String json) {
        if (json == null || json.length() < 3) return;
        try {
            // Simple manual JSON parsing matching codebase style
            String inner = json.substring(1, json.length() - 1);
            for (String pair : inner.split(",")) {
                String[] kv = pair.split(":");
                if (kv.length < 2) continue;
                String key = kv[0].replace("\"", "").trim();
                int value = Integer.parseInt(kv[1].replace("\"", "").trim());
                switch (key) {
                    case "totalWagered" -> this.totalWagered = value;
                    case "totalWon" -> this.totalWon = value;
                    case "totalLost" -> this.totalLost = value;
                    case "gamesPlayed" -> this.gamesPlayed = value;
                    case "biggestWin" -> this.biggestWin = value;
                    case "profitLoss" -> this.profitLoss = value;
                    case "slotsPlayed" -> this.slotsPlayed = value;
                    case "slotsWon" -> this.slotsWon = value;
                    case "blackjackPlayed" -> this.blackjackPlayed = value;
                    case "blackjackWon" -> this.blackjackWon = value;
                    case "wheelPlayed" -> this.wheelPlayed = value;
                    case "wheelWon" -> this.wheelWon = value;
                    case "roulettePlayed" -> this.roulettePlayed = value;
                    case "rouletteWon" -> this.rouletteWon = value;
                    case "crapsPlayed" -> this.crapsPlayed = value;
                    case "crapsWon" -> this.crapsWon = value;
                    case "baccaratPlayed" -> this.baccaratPlayed = value;
                    case "baccaratWon" -> this.baccaratWon = value;
                }
            }
            this.profitLoss = this.totalWon - this.totalLost;
        } catch (Exception e) {
            // Parsing failed, leave defaults
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Dark background
        g.fill(0, 0, this.width, this.height, BG_COLOR);
        UIHelper.drawPanel(g, 0, 0, this.width, this.height);
        UIHelper.drawTitleBar(g, 0, 0, this.width, this.titleBarH);

        Objects.requireNonNull(this.font);
        int titleY = (this.titleBarH - 9) / 2;
        UIHelper.drawCenteredTitle(g, this.font, "Casino", this.width / 2, titleY);

        // Back button
        boolean backHover = mouseX >= this.backX && mouseX < this.backX + this.backW
                && mouseY >= this.backY && mouseY < this.backY + this.backH;
        UIHelper.drawButton(g, this.backX, this.backY, this.backW, this.backH, backHover);
        int backTextX = this.backX + (this.backW - this.font.width("< Back")) / 2;
        g.drawString(this.font, "< Back", backTextX, this.backY + (this.backH - 9) / 2, TEXT_PRIMARY, false);

        this.clickRects.clear();

        if (!this.dataLoaded) {
            int loadW = 180;
            int loadH = 29;
            int loadX = (this.width - loadW) / 2;
            int loadY = this.height / 2 - loadH / 2;
            UIHelper.drawPanel(g, loadX, loadY, loadW, loadH);
            UIHelper.drawCenteredLabel(g, this.font, "Loading casino stats...", this.width / 2, this.height / 2 - 4);
        } else {
            renderStats(g, mouseX, mouseY);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void renderStats(GuiGraphics g, int mouseX, int mouseY) {
        int contentTop = this.titleBarH + 8;
        int panelW = Math.min(280, this.width - 20);
        int panelX = (this.width - panelW) / 2;
        int y = contentTop;

        // --- Section: Your Casino Stats ---
        g.drawString(this.font, "Your Casino Stats", panelX, y, GOLD_ACCENT, false);
        y += 12;

        // Stats panel background
        int statsH = 90;
        g.fill(panelX, y, panelX + panelW, y + statsH, SECTION_BG);
        drawRectOutline(g, panelX, y, panelX + panelW, y + statsH, SECTION_BORDER);

        int innerX = panelX + 8;
        int innerY = y + 6;
        int lineH = 12;

        // Profit/Loss
        int plColor = this.profitLoss >= 0 ? PROFIT_GREEN : LOSS_RED;
        String plSign = this.profitLoss >= 0 ? "+" : "";
        g.drawString(this.font, "Profit/Loss:", innerX, innerY, TEXT_DIM, false);
        String plStr = plSign + this.profitLoss + " MC";
        g.drawString(this.font, plStr, panelX + panelW - this.font.width(plStr) - 8, innerY, plColor, false);
        innerY += lineH;

        // Total Wagered
        g.drawString(this.font, "Total Wagered:", innerX, innerY, TEXT_DIM, false);
        String wagStr = this.totalWagered + " MC";
        g.drawString(this.font, wagStr, panelX + panelW - this.font.width(wagStr) - 8, innerY, TEXT_PRIMARY, false);
        innerY += lineH;

        // Total Won
        g.drawString(this.font, "Total Won:", innerX, innerY, TEXT_DIM, false);
        String wonStr = this.totalWon + " MC";
        g.drawString(this.font, wonStr, panelX + panelW - this.font.width(wonStr) - 8, innerY, PROFIT_GREEN, false);
        innerY += lineH;

        // Total Lost
        g.drawString(this.font, "Total Lost:", innerX, innerY, TEXT_DIM, false);
        String lostStr = this.totalLost + " MC";
        g.drawString(this.font, lostStr, panelX + panelW - this.font.width(lostStr) - 8, innerY, LOSS_RED, false);
        innerY += lineH;

        // Games Played
        g.drawString(this.font, "Games Played:", innerX, innerY, TEXT_DIM, false);
        String gpStr = String.valueOf(this.gamesPlayed);
        g.drawString(this.font, gpStr, panelX + panelW - this.font.width(gpStr) - 8, innerY, TEXT_PRIMARY, false);
        innerY += lineH;

        // Biggest Win
        g.drawString(this.font, "Biggest Win:", innerX, innerY, TEXT_DIM, false);
        String bwStr = this.biggestWin + " MC";
        int bwColor = this.biggestWin > 0 ? GOLD_ACCENT : TEXT_DIM;
        g.drawString(this.font, bwStr, panelX + panelW - this.font.width(bwStr) - 8, innerY, bwColor, false);

        y += statsH + 8;

        // --- Per-Game Breakdown ---
        g.drawString(this.font, "Game Breakdown", panelX, y, GOLD_ACCENT, false);
        y += 12;

        int breakdownH = 84;
        g.fill(panelX, y, panelX + panelW, y + breakdownH, SECTION_BG);
        drawRectOutline(g, panelX, y, panelX + panelW, y + breakdownH, SECTION_BORDER);

        innerX = panelX + 8;
        innerY = y + 6;
        int col2X = panelX + panelW / 2 + 10;

        // Slots
        g.drawString(this.font, "Slots:", innerX, innerY, TEXT_DIM, false);
        g.drawString(this.font, this.slotsPlayed + " played / " + this.slotsWon + " won", col2X, innerY, TEXT_PRIMARY, false);
        innerY += lineH;

        // Blackjack
        g.drawString(this.font, "Blackjack:", innerX, innerY, TEXT_DIM, false);
        g.drawString(this.font, this.blackjackPlayed + " played / " + this.blackjackWon + " won", col2X, innerY, TEXT_PRIMARY, false);
        innerY += lineH;

        // Wheel
        g.drawString(this.font, "Wheel:", innerX, innerY, TEXT_DIM, false);
        g.drawString(this.font, this.wheelPlayed + " played / " + this.wheelWon + " won", col2X, innerY, TEXT_PRIMARY, false);
        innerY += lineH;

        // Roulette
        g.drawString(this.font, "Roulette:", innerX, innerY, TEXT_DIM, false);
        g.drawString(this.font, this.roulettePlayed + " played / " + this.rouletteWon + " won", col2X, innerY, TEXT_PRIMARY, false);
        innerY += lineH;

        // Craps
        g.drawString(this.font, "Craps:", innerX, innerY, TEXT_DIM, false);
        g.drawString(this.font, this.crapsPlayed + " played / " + this.crapsWon + " won", col2X, innerY, TEXT_PRIMARY, false);
        innerY += lineH;

        // Baccarat
        g.drawString(this.font, "Baccarat:", innerX, innerY, TEXT_DIM, false);
        g.drawString(this.font, this.baccaratPlayed + " played / " + this.baccaratWon + " won", col2X, innerY, TEXT_PRIMARY, false);

        y += breakdownH + 16;

        // --- GO TO CASINO button ---
        int btnW = 180;
        int btnH = 24;
        int btnX = (this.width - btnW) / 2;
        int btnY = y;

        boolean btnHover = mouseX >= btnX && mouseX < btnX + btnW
                && mouseY >= btnY && mouseY < btnY + btnH;

        // Pulsing glow effect
        float pulse = (float) (Math.sin(this.animTick * 0.15) * 0.5 + 0.5);
        int glowAlpha = (int) (40 * pulse);
        int glowColor = (glowAlpha << 24) | (0xD4AF37 & 0x00FFFFFF);

        // Glow outline (2px)
        g.fill(btnX - 2, btnY - 2, btnX + btnW + 2, btnY + btnH + 2, glowColor);

        // Button fill
        int btnBg = btnHover ? CASINO_BTN_HOVER : CASINO_BTN_BG;
        g.fill(btnX, btnY, btnX + btnW, btnY + btnH, btnBg);

        // Button border
        drawRectOutline(g, btnX, btnY, btnX + btnW, btnY + btnH, GOLD_DARK);

        // Button text
        String btnText = "GO TO CASINO";
        int btnTextW = this.font.width(btnText);
        g.drawString(this.font, btnText, btnX + (btnW - btnTextW) / 2, btnY + (btnH - 9) / 2, CASINO_BTN_TEXT, false);

        this.clickRects.add(new ClickRect(btnX, btnY, btnW, btnH, "go_to_casino"));
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

        // Click rects
        for (ClickRect rect : this.clickRects) {
            if (mx >= rect.x && mx < rect.x + rect.w
                    && my >= rect.y && my < rect.y + rect.h) {
                handleAction(rect.action);
                return true;
            }
        }

        return super.mouseClicked(event, consumed);
    }

    private void handleAction(String action) {
        if ("go_to_casino".equals(action)) {
            ClientPacketDistributor.sendToServer(
                (CustomPacketPayload) new ComputerActionPayload("casino_teleport", ""),
                (CustomPacketPayload[]) new CustomPacketPayload[0]
            );
            if (this.minecraft != null) {
                this.minecraft.setScreen(null);
            }
        }
    }

    public boolean keyPressed(KeyEvent event) {
        if (event.key() == 256) { // ESC
            if (this.minecraft != null) {
                this.minecraft.setScreen(this.parent);
            }
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    public boolean isPauseScreen() {
        return false;
    }

    private static void drawRectOutline(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        g.fill(x1, y1, x2, y1 + 1, color);
        g.fill(x1, y2 - 1, x2, y2, color);
        g.fill(x1, y1, x1 + 1, y2, color);
        g.fill(x2 - 1, y1, x2, y2, color);
    }

    private record ClickRect(int x, int y, int w, int h, String action) {}
}

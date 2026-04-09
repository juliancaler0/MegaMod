package com.ultra.megamod.feature.casino.screen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ultra.megamod.feature.casino.network.BaccaratActionPayload;
import com.ultra.megamod.feature.casino.network.BaccaratGameSyncPayload;
import com.ultra.megamod.feature.casino.chips.ChipDenomination;
import com.ultra.megamod.feature.casino.chips.ChipRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.ArrayList;
import java.util.List;

/**
 * Baccarat GUI screen. Right-side panel layout following WheelScreen pattern.
 * Shows Player and Banker card hands, bet selector, amount input, and result display.
 */
public class BaccaratScreen extends Screen {

    private final BlockPos tablePos;

    // Game state parsed from server sync
    private String phase = "BETTING";
    private final List<Integer> playerCards = new ArrayList<>();
    private final List<Integer> bankerCards = new ArrayList<>();
    private int playerValue = 0;
    private int bankerValue = 0;
    private String betSide = "";
    private int betAmount = 0;
    private String result = "";
    private String resultMessage = "";
    private int payout = 0;

    // UI state
    private int selectedSide = -1; // -1=none, 0=Player, 1=Banker, 2=Tie
    private int currentBetAmount = 0;
    private int chipTrayX, chipTrayY, chipTrayW;
    private int resultDisplayTimer = 0;

    private static final String[] SIDE_NAMES = {"Player", "Banker", "Tie"};
    private static final String[] SIDE_KEYS = {"player", "banker", "tie"};
    private static final String[] SIDE_PAYOUTS = {"1:1", "0.95:1", "8:1"};

    // Layout constants
    private static final int PANEL_W = 180;
    private static final int BG = 0xAA000000;
    private static final int BORDER = 0xFFD4AF37;
    private static final int TEXT = 0xFFCCCCDD;
    private static final int GOLD = 0xFFD4AF37;
    private static final int DIM = 0xFF888899;
    private static final int GREEN = 0xFF4CAF50;
    private static final int RED = 0xFFE53935;

    // Card rendering
    private static final int CARD_W = 24;
    private static final int CARD_H = 34;
    private static final int CARD_GAP = 4;

    public BaccaratScreen(BlockPos tablePos) {
        super(Component.literal("Baccarat"));
        this.tablePos = tablePos;
    }

    @Override
    protected void init() {
        super.init();
        // Request initial sync
        sendAction("sync", "", 0);
    }

    @Override
    public void onClose() {
        ChipRenderer.clearDrag();
        com.ultra.megamod.feature.casino.CasinoClientEvents.onBaccaratDismissed();
        super.onClose();
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Let the world show through
    }

    @Override
    public void tick() {
        super.tick();
        if (resultDisplayTimer > 0) resultDisplayTimer--;

        BaccaratGameSyncPayload sync = BaccaratGameSyncPayload.lastSync;
        if (sync != null) {
            BaccaratGameSyncPayload.lastSync = null;
            parseSync(sync.gameStateJson());
        }
    }

    private void parseSync(String json) {
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            this.phase = root.has("phase") ? root.get("phase").getAsString() : "BETTING";
            this.betSide = root.has("betSide") ? root.get("betSide").getAsString() : "";
            this.betAmount = root.has("betAmount") ? root.get("betAmount").getAsInt() : 0;
            this.playerValue = root.has("playerValue") ? root.get("playerValue").getAsInt() : 0;
            this.bankerValue = root.has("bankerValue") ? root.get("bankerValue").getAsInt() : 0;
            this.result = root.has("result") ? root.get("result").getAsString() : "";
            this.resultMessage = root.has("resultMessage") ? root.get("resultMessage").getAsString() : "";
            this.payout = root.has("payout") ? root.get("payout").getAsInt() : 0;

            this.playerCards.clear();
            if (root.has("playerCards")) {
                JsonArray arr = root.getAsJsonArray("playerCards");
                for (int i = 0; i < arr.size(); i++) {
                    this.playerCards.add(arr.get(i).getAsInt());
                }
            }

            this.bankerCards.clear();
            if (root.has("bankerCards")) {
                JsonArray arr = root.getAsJsonArray("bankerCards");
                for (int i = 0; i < arr.size(); i++) {
                    this.bankerCards.add(arr.get(i).getAsInt());
                }
            }

            if ("RESULT".equals(this.phase) && !this.resultMessage.isEmpty()) {
                resultDisplayTimer = 100; // 5 seconds
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        int rx = this.width - PANEL_W - 10;
        int ty = 10;
        int ph = Math.min(this.height - 20, 380);

        // Panel background
        g.fill(rx, ty, rx + PANEL_W, ty + ph, BG);
        g.fill(rx, ty, rx + PANEL_W, ty + 1, BORDER);
        g.fill(rx, ty + ph - 1, rx + PANEL_W, ty + ph, BORDER);
        g.fill(rx, ty, rx + 1, ty + ph, BORDER);
        g.fill(rx + PANEL_W - 1, ty, rx + PANEL_W, ty + ph, BORDER);

        // Title
        g.drawString(this.font, "BACCARAT", rx + 6, ty + 4, GOLD, false);

        // Wallet balance
        int wallet = ChipRenderer.clientChipTotal;
        String walletStr = "Chips: " + wallet + " MC";
        int walletW = this.font.width(walletStr);
        g.drawString(this.font, walletStr, rx + PANEL_W - walletW - 6, ty + 4, GOLD, false);

        int y = ty + 18;

        // Separator
        g.fill(rx + 4, y, rx + PANEL_W - 4, y + 1, BORDER);
        y += 5;

        // ---- Player Hand ----
        boolean playerWon = "player".equals(result);
        int playerLabelColor = playerWon ? GREEN : TEXT;
        g.drawString(this.font, "Player Hand", rx + 6, y, playerLabelColor, false);
        if (!playerCards.isEmpty()) {
            String valStr = "= " + playerValue;
            int valW = this.font.width(valStr);
            g.drawString(this.font, valStr, rx + PANEL_W - valW - 6, y, playerWon ? GREEN : GOLD, false);
        }
        y += 12;

        // Player cards
        if (playerCards.isEmpty()) {
            g.drawString(this.font, "No cards yet", rx + 10, y + 8, DIM, false);
        } else {
            int cardStartX = rx + 6;
            for (int i = 0; i < playerCards.size(); i++) {
                int cx = cardStartX + i * (CARD_W + CARD_GAP);
                renderCard(g, cx, y, playerCards.get(i), playerWon);
            }
        }
        y += CARD_H + 8;

        // ---- Banker Hand ----
        boolean bankerWon = "banker".equals(result);
        int bankerLabelColor = bankerWon ? GREEN : TEXT;
        g.drawString(this.font, "Banker Hand", rx + 6, y, bankerLabelColor, false);
        if (!bankerCards.isEmpty()) {
            String valStr = "= " + bankerValue;
            int valW = this.font.width(valStr);
            g.drawString(this.font, valStr, rx + PANEL_W - valW - 6, y, bankerWon ? GREEN : GOLD, false);
        }
        y += 12;

        // Banker cards
        if (bankerCards.isEmpty()) {
            g.drawString(this.font, "No cards yet", rx + 10, y + 8, DIM, false);
        } else {
            int cardStartX = rx + 6;
            for (int i = 0; i < bankerCards.size(); i++) {
                int cx = cardStartX + i * (CARD_W + CARD_GAP);
                renderCard(g, cx, y, bankerCards.get(i), bankerWon);
            }
        }
        y += CARD_H + 8;

        // ---- Active bet chip stack (shown during DEALING and RESULT) ----
        if (("DEALING".equals(phase) || "RESULT".equals(phase)) && betAmount > 0 && !betSide.isEmpty()) {
            // Show which side the bet is on and the chip stack
            String betLabel = "Bet on " + betSide.substring(0, 1).toUpperCase() + betSide.substring(1) + ":";
            g.drawString(this.font, betLabel, rx + 6, y, GOLD, false);
            int chipCenterX = rx + PANEL_W / 2;
            int chipBottomY = y + 24;
            ChipRenderer.renderChipStack(g, this.font, chipCenterX, chipBottomY, betAmount);
            y += 40;
        }

        // Separator
        g.fill(rx + 4, y, rx + PANEL_W - 4, y + 1, BORDER);
        y += 5;

        // ---- Result display ----
        if ("RESULT".equals(phase) && !resultMessage.isEmpty()) {
            // Result background
            g.fill(rx + 4, y, rx + PANEL_W - 4, y + 24, 0x66000000);
            drawRectOutline(g, rx + 4, y, rx + PANEL_W - 4, y + 24, GOLD);

            // Winner label
            String winnerLabel = "tie".equals(result) ? "TIE!" : result.toUpperCase() + " WINS!";
            int winnerW = this.font.width(winnerLabel);
            int winnerColor = "tie".equals(result) ? GOLD : (result.equals(betSide) ? GREEN : RED);
            g.drawString(this.font, winnerLabel, rx + (PANEL_W - winnerW) / 2, y + 3, winnerColor, false);

            // Payout message
            int msgW = this.font.width(resultMessage);
            int msgColor = payout > 0 ? GREEN : RED;
            g.drawString(this.font, resultMessage, rx + (PANEL_W - msgW) / 2, y + 14, msgColor, false);

            y += 28;

            // New Round button
            int btnW = 80;
            int btnH = 14;
            int btnX = rx + (PANEL_W - btnW) / 2;
            boolean btnHover = mouseX >= btnX && mouseX < btnX + btnW && mouseY >= y && mouseY < y + btnH;
            g.fill(btnX, y, btnX + btnW, y + btnH, btnHover ? 0xFF4CAF50 : 0xFF2E7D32);
            String newRoundStr = "New Round";
            int nrW = this.font.width(newRoundStr);
            g.drawString(this.font, newRoundStr, btnX + (btnW - nrW) / 2, y + 3, 0xFFFFFFFF, false);
            y += btnH + 6;
        }

        // ---- Betting controls (only in BETTING phase) ----
        if ("BETTING".equals(phase)) {
            renderBettingControls(g, mouseX, mouseY, rx, y);
        }

        // Bottom hint
        g.drawString(this.font, "ESC close", rx + 6, ty + ph - 12, DIM, false);

        super.render(g, mouseX, mouseY, partialTick);

        // Dragged chip always on top of everything
        ChipRenderer.renderDraggedChip(g, this.font, mouseX, mouseY);
    }

    private void renderBettingControls(GuiGraphics g, int mouseX, int mouseY, int rx, int y) {
        // --- Side drop zones (Player / Banker / Tie) ---
        int zoneW = PANEL_W - 16;
        int zoneH = 24;
        int zoneX = rx + 8;

        for (int i = 0; i < SIDE_NAMES.length; i++) {
            int zoneY = y + i * (zoneH + 3);
            boolean sel = i == selectedSide;
            boolean overZone = mouseX >= zoneX && mouseX < zoneX + zoneW
                    && mouseY >= zoneY && mouseY < zoneY + zoneH;
            boolean draggingOverZone = ChipRenderer.isDragging() && overZone;

            // Background
            int zoneColor;
            if (draggingOverZone) {
                zoneColor = 0x6600FF00;
            } else if (sel) {
                zoneColor = 0x44D4AF37;
            } else {
                zoneColor = 0x33FFFFFF;
            }
            g.fill(zoneX, zoneY, zoneX + zoneW, zoneY + zoneH, zoneColor);

            // Border
            int borderCol = draggingOverZone ? 0xFF00FF00 : (sel ? GOLD : 0xFF555555);
            drawRectOutline(g, zoneX, zoneY, zoneX + zoneW, zoneY + zoneH, borderCol);

            // Label: side name and payout
            String label = SIDE_NAMES[i] + " (" + SIDE_PAYOUTS[i] + ")";
            int labelColor = draggingOverZone ? 0xFF00FF00 : (sel ? GOLD : TEXT);
            g.drawString(this.font, label, zoneX + 4, zoneY + (zoneH - 9) / 2, labelColor, false);

            // Selection indicator
            if (sel) {
                g.drawString(this.font, "\u25B6", zoneX + zoneW - 12, zoneY + (zoneH - 9) / 2, GOLD, false);
            }

            // Chip stack on selected side with active bet
            if (sel && currentBetAmount > 0) {
                int chipCenterX = zoneX + zoneW - 28;
                int chipBottomY = zoneY + zoneH - 6;
                ChipRenderer.renderChipStack(g, this.font, chipCenterX, chipBottomY, currentBetAmount);
            }
        }
        y += SIDE_NAMES.length * (zoneH + 3) + 4;

        // Current bet display
        String sideLabel = (selectedSide >= 0 && selectedSide < SIDE_NAMES.length)
                ? SIDE_NAMES[selectedSide] : "---";
        String curBetStr = "Bet: " + currentBetAmount + " MC on " + sideLabel;
        g.drawString(this.font, curBetStr, rx + 6, y, TEXT, false);
        y += 14;

        // Button row: Clear and Deal
        int clearBtnW = 52;
        int dealBtnW = 52;
        int btnH = 16;
        int clearBtnX = rx + 8;
        int dealBtnX = rx + PANEL_W - dealBtnW - 8;

        // Clear button
        boolean clearHover = mouseX >= clearBtnX && mouseX < clearBtnX + clearBtnW
                && mouseY >= y && mouseY < y + btnH;
        g.fill(clearBtnX, y, clearBtnX + clearBtnW, y + btnH, clearHover ? 0xFFEF5350 : 0xFFC62828);
        String clearStr = "CLEAR";
        int clearStrW = this.font.width(clearStr);
        g.drawString(this.font, clearStr, clearBtnX + (clearBtnW - clearStrW) / 2, y + (btnH - 9) / 2, 0xFFFFFFFF, false);

        // Deal button
        boolean canDeal = currentBetAmount > 0 && selectedSide >= 0;
        boolean dealHover = canDeal && mouseX >= dealBtnX && mouseX < dealBtnX + dealBtnW
                && mouseY >= y && mouseY < y + btnH;
        int dealBg = canDeal ? (dealHover ? 0xFF4CAF50 : 0xFF2E7D32) : 0xFF333333;
        g.fill(dealBtnX, y, dealBtnX + dealBtnW, y + btnH, dealBg);
        String dealStr = "DEAL";
        int dealStrW = this.font.width(dealStr);
        g.drawString(this.font, dealStr, dealBtnX + (dealBtnW - dealStrW) / 2, y + (btnH - 9) / 2,
                canDeal ? 0xFFFFFFFF : 0xFF666666, false);
        y += btnH + 6;

        // --- Chip tray ---
        this.chipTrayW = PANEL_W - 8;
        this.chipTrayX = rx + 4;
        this.chipTrayY = y;
        ChipRenderer.renderChipTray(g, this.font, this.chipTrayX, this.chipTrayY, this.chipTrayW, mouseX, mouseY);

        // Hint
        String hint = "Drag chips onto a side";
        int hintW = this.font.width(hint);
        g.drawString(this.font, hint, rx + (PANEL_W - hintW) / 2, this.chipTrayY - 8, DIM, false);
    }

    /**
     * Renders a single baccarat card as a rectangle with the card value.
     */
    private void renderCard(GuiGraphics g, int x, int y, int value, boolean highlight) {
        // Card outline
        int borderColor = highlight ? GREEN : 0xFF666666;
        g.fill(x, y, x + CARD_W, y + CARD_H, borderColor);
        // Card body
        g.fill(x + 1, y + 1, x + CARD_W - 1, y + CARD_H - 1, 0xFFEEEEEE);

        // Card value text (centered)
        String valStr = String.valueOf(value);
        int valW = this.font.width(valStr);
        int textColor = value == 0 ? 0xFF999999 : 0xFF111111;
        g.drawString(this.font, valStr, x + (CARD_W - valW) / 2, y + (CARD_H - 9) / 2, textColor, false);

        // Small value in corner
        g.drawString(this.font, valStr, x + 2, y + 2, textColor, false);
    }

    private void drawRectOutline(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        g.fill(x1, y1, x2, y1 + 1, color);
        g.fill(x1, y2 - 1, x2, y2, color);
        g.fill(x1, y1, x1 + 1, y2, color);
        g.fill(x2 - 1, y1, x2, y2, color);
    }

    // ---- Input handling ----

    /** Compute the Y offset where betting controls start (must match render layout). */
    private int computeBettingControlsY() {
        int ty = 10;
        int y = ty + 18 + 5; // after title and separator
        y += 12 + CARD_H + 8; // player hand section
        y += 12 + CARD_H + 8; // banker hand section
        y += 5; // separator
        return y;
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean consumed) {
        if (consumed) return super.mouseClicked(event, consumed);
        int mx = (int) event.x();
        int my = (int) event.y();

        int rx = this.width - PANEL_W - 10;
        int y = computeBettingControlsY();

        // ---- Result phase: New Round button ----
        if ("RESULT".equals(phase)) {
            // Must match render layout: bet display + separator + result box
            if (betAmount > 0 && !betSide.isEmpty()) {
                y += 40; // bet chip stack display
            }
            y += 6; // separator
            y += 28; // result display box
            int btnW = 80;
            int btnH = 14;
            int btnX = rx + (PANEL_W - btnW) / 2;
            if (mx >= btnX && mx < btnX + btnW && my >= y && my < y + btnH) {
                sendAction("reset", "", 0);
                return true;
            }
            return super.mouseClicked(event, consumed);
        }

        // ---- Betting phase ----
        if ("BETTING".equals(phase)) {
            // Side drop zones
            int zoneW = PANEL_W - 16;
            int zoneH = 24;
            int zoneX = rx + 8;

            // Click directly on a zone to select it (without dragging)
            for (int i = 0; i < SIDE_NAMES.length; i++) {
                int zoneY = y + i * (zoneH + 3);
                if (mx >= zoneX && mx < zoneX + zoneW && my >= zoneY && my < zoneY + zoneH) {
                    selectedSide = i;
                    return true;
                }
            }
            int btnAreaY = y + SIDE_NAMES.length * (zoneH + 3) + 4 + 14; // skip zones + bet label

            // Clear button
            int clearBtnW = 52;
            int btnH = 16;
            int clearBtnX = rx + 8;
            if (mx >= clearBtnX && mx < clearBtnX + clearBtnW && my >= btnAreaY && my < btnAreaY + btnH) {
                currentBetAmount = 0;
                selectedSide = -1;
                return true;
            }

            // Deal button
            int dealBtnW = 52;
            int dealBtnX = rx + PANEL_W - dealBtnW - 8;
            if (currentBetAmount > 0 && selectedSide >= 0
                    && mx >= dealBtnX && mx < dealBtnX + dealBtnW && my >= btnAreaY && my < btnAreaY + btnH) {
                sendAction("bet", SIDE_KEYS[selectedSide], currentBetAmount);
                return true;
            }

            // Chip tray click — start dragging
            if (ChipRenderer.handleTrayClick(mx, my, this.chipTrayX, this.chipTrayY, this.chipTrayW)) {
                return true;
            }
        }

        return super.mouseClicked(event, consumed);
    }

    public boolean mouseReleased(MouseButtonEvent e) {
        int mx = (int) e.x();
        int my = (int) e.y();
        if (ChipRenderer.isDragging() && "BETTING".equals(phase)) {
            int rx = this.width - PANEL_W - 10;
            int y = computeBettingControlsY();

            // Side drop zone bounds
            int zoneW = PANEL_W - 16;
            int zoneH = 24;
            int zoneX = rx + 8;

            ChipDenomination dropped = ChipRenderer.completeDrop();
            if (dropped != null) {
                for (int i = 0; i < SIDE_NAMES.length; i++) {
                    int zoneY = y + i * (zoneH + 3);
                    if (mx >= zoneX && mx < zoneX + zoneW
                            && my >= zoneY && my < zoneY + zoneH) {
                        selectedSide = i;
                        currentBetAmount += dropped.value;
                        return true;
                    }
                }
            }
            return true;
        }
        return super.mouseReleased(e);
    }

    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();
        if (keyCode == 256) {
            this.onClose();
            return true;
        }

        // R to reset (new round) when in RESULT phase
        if (keyCode == 82 && "RESULT".equals(phase)) {
            sendAction("reset", "", 0);
            return true;
        }

        return super.keyPressed(event);
    }

    private void sendAction(String action, String side, int amount) {
        ClientPacketDistributor.sendToServer(
                (CustomPacketPayload) new BaccaratActionPayload(action, side, amount, tablePos),
                (CustomPacketPayload[]) new CustomPacketPayload[0]);
    }

    public boolean isPauseScreen() {
        return false;
    }
}

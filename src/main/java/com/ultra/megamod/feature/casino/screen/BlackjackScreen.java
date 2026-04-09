package com.ultra.megamod.feature.casino.screen;

import com.ultra.megamod.feature.casino.chips.ChipRenderer;
import com.ultra.megamod.feature.casino.chips.ChipDenomination;
import com.ultra.megamod.feature.casino.network.BlackjackActionPayload;
import com.ultra.megamod.feature.casino.network.BlackjackSyncPayload;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class BlackjackScreen extends Screen {

    private final BlockPos tablePos;

    // Game state parsed from server sync
    private String phase = "WAITING";
    private final List<String> dealerCards = new ArrayList<>();
    private boolean dealerRevealed = false;
    private int dealerValue = 0;
    private final List<SeatInfo> seats = new ArrayList<>();
    private int currentSeat = -1;
    private final List<String> availableActions = new ArrayList<>();
    private String statusText = "Waiting for players...";
    private int mySeatIndex = -1;
    private String myPlayerId = "";

    // Active player tracking
    private String activePlayerName = "";
    private final List<PlayerBetInfo> playerSummary = new ArrayList<>();
    private record PlayerBetInfo(String name, int bet, int handValue, boolean isActive, boolean done, boolean bust, boolean blackjack) {}

    // Betting phase state — chip drag-and-drop
    private int betAmount = 0;
    private boolean betPlaced = false;
    private int chipTrayX, chipTrayY, chipTrayW;

    // Bet zone (drop target) — centered on screen
    private static final int BET_ZONE_W = 120;
    private static final int BET_ZONE_H = 40;

    // Win/loss result
    private String resultMessage = "";
    private int resultTimer = 0;

    // Layout constants
    private static final int CARD_W = 20;
    private static final int CARD_H = 28;
    private static final int CARD_OVERLAP = 14;
    private static final int HUD_BAR_HEIGHT = 60;
    private static final int STATUS_BAR_HEIGHT = 18;

    // Card rendering for small floating cards
    private static final int SMALL_CARD_W = 14;
    private static final int SMALL_CARD_H = 18;
    private static final int SMALL_CARD_OVERLAP = 10;

    // Colors
    private static final int CARD_WHITE = 0xFFEEEEEE;
    private static final int CARD_BACK = 0xFF1A237E;
    private static final int CARD_BACK_PATTERN = 0xFF283593;
    private static final int GOLD_BORDER = 0xFFD4AF37;
    private static final int TEXT_WHITE = 0xFFCCCCDD;
    private static final int TEXT_DIM = 0xFF666677;
    private static final int TEXT_GOLD = 0xFFD4AF37;
    private static final int BTN_HIT = 0xFF2E7D32;
    private static final int BTN_HIT_HOVER = 0xFF4CAF50;
    private static final int BTN_STAND = 0xFFE6A800;
    private static final int BTN_STAND_HOVER = 0xFFFFD54F;
    private static final int BTN_DOUBLE = 0xFF1565C0;
    private static final int BTN_DOUBLE_HOVER = 0xFF42A5F5;
    private static final int BTN_SPLIT = 0xFF6A1B9A;
    private static final int BTN_SPLIT_HOVER = 0xFFAB47BC;
    private static final int BTN_SURRENDER = 0xFFC62828;
    private static final int BTN_SURRENDER_HOVER = 0xFFEF5350;
    private static final int BET_BTN = 0xFF2E7D32;
    private static final int BET_BTN_HOVER = 0xFF4CAF50;

    // Suit symbols
    private static final String HEART = "H";
    private static final String DIAMOND = "D";

    private record SeatInfo(String playerId, String playerName, int bet,
                            List<List<String>> hands, List<Integer> handValues,
                            int activeHand, boolean done) {}

    public BlackjackScreen(BlockPos tablePos) {
        super(Component.literal("Blackjack"));
        this.tablePos = tablePos;
    }

    @Override
    protected void init() {
        super.init();
        // Player already joined from the BlackjackChairBlock - no need to send join again
    }

    @Override
    public void onClose() {
        // Just close the screen - DON'T leave the table
        // Player can reopen by right-clicking the chair again
        // They leave the table when they dismount (handled by CasinoEvents)
        ChipRenderer.clearDrag();
        super.onClose();
    }

    @Override
    public void tick() {
        super.tick();

        if (this.resultTimer > 0) {
            this.resultTimer--;
        }

        BlackjackSyncPayload sync = BlackjackSyncPayload.lastSync;
        if (sync != null) {
            BlackjackSyncPayload.lastSync = null;
            parseSync(sync.gameStateJson());
        }
    }

    private void parseSync(String json) {
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();

            this.phase = root.has("phase") ? root.get("phase").getAsString() : "WAITING";
            this.statusText = root.has("statusText") ? root.get("statusText").getAsString() : "";
            this.currentSeat = root.has("currentSeat") ? root.get("currentSeat").getAsInt() : -1;
            this.dealerRevealed = root.has("dealerRevealed") && root.get("dealerRevealed").getAsBoolean();
            this.dealerValue = root.has("dealerValue") ? root.get("dealerValue").getAsInt() : 0;
            this.mySeatIndex = root.has("mySeatIndex") ? root.get("mySeatIndex").getAsInt() : -1;
            this.myPlayerId = root.has("myPlayerId") ? root.get("myPlayerId").getAsString() : "";

            this.dealerCards.clear();
            if (root.has("dealerCards")) {
                JsonArray arr = root.getAsJsonArray("dealerCards");
                for (JsonElement el : arr) {
                    this.dealerCards.add(el.getAsString());
                }
            }

            this.availableActions.clear();
            if (root.has("availableActions")) {
                JsonArray arr = root.getAsJsonArray("availableActions");
                for (JsonElement el : arr) {
                    this.availableActions.add(el.getAsString());
                }
            }

            this.seats.clear();
            if (root.has("seats")) {
                JsonArray seatsArr = root.getAsJsonArray("seats");
                for (JsonElement seatEl : seatsArr) {
                    JsonObject seatObj = seatEl.getAsJsonObject();
                    String pid = seatObj.has("playerId") ? seatObj.get("playerId").getAsString() : "";
                    String pname = seatObj.has("playerName") ? seatObj.get("playerName").getAsString() : "Empty";
                    int bet = seatObj.has("bet") ? seatObj.get("bet").getAsInt() : 0;
                    int activeHand = seatObj.has("activeHand") ? seatObj.get("activeHand").getAsInt() : 0;
                    boolean done = seatObj.has("done") && seatObj.get("done").getAsBoolean();

                    List<List<String>> hands = new ArrayList<>();
                    List<Integer> handValues = new ArrayList<>();
                    if (seatObj.has("hands")) {
                        JsonArray handsArr = seatObj.getAsJsonArray("hands");
                        for (JsonElement handEl : handsArr) {
                            JsonObject handObj = handEl.getAsJsonObject();
                            List<String> cards = new ArrayList<>();
                            if (handObj.has("cards")) {
                                JsonArray cardsArr = handObj.getAsJsonArray("cards");
                                for (JsonElement cEl : cardsArr) {
                                    cards.add(cEl.getAsString());
                                }
                            }
                            hands.add(cards);
                            handValues.add(handObj.has("value") ? handObj.get("value").getAsInt() : 0);
                        }
                    }

                    this.seats.add(new SeatInfo(pid, pname, bet, hands, handValues, activeHand, done));
                }
            }

            if (root.has("resultMessage")) {
                this.resultMessage = root.get("resultMessage").getAsString();
                this.resultTimer = 80;
            }

            if ("BETTING".equals(this.phase) && this.mySeatIndex >= 0 && this.mySeatIndex < this.seats.size()) {
                this.betPlaced = this.seats.get(this.mySeatIndex).bet > 0;
            }
            if (!"BETTING".equals(this.phase)) {
                this.betPlaced = false;
                this.betAmount = 0;
            }

            // Parse active player name
            this.activePlayerName = root.has("activePlayerName") ? root.get("activePlayerName").getAsString() : "";

            // Parse player summary (all bets + status)
            this.playerSummary.clear();
            if (root.has("playerSummary")) {
                for (com.google.gson.JsonElement el : root.getAsJsonArray("playerSummary")) {
                    com.google.gson.JsonObject ps = el.getAsJsonObject();
                    this.playerSummary.add(new PlayerBetInfo(
                            ps.has("name") ? ps.get("name").getAsString() : "?",
                            ps.has("bet") ? ps.get("bet").getAsInt() : 0,
                            ps.has("handValue") ? ps.get("handValue").getAsInt() : 0,
                            ps.has("isActive") && ps.get("isActive").getAsBoolean(),
                            ps.has("done") && ps.get("done").getAsBoolean(),
                            ps.has("bust") && ps.get("bust").getAsBoolean(),
                            ps.has("blackjack") && ps.get("blackjack").getAsBoolean()
                    ));
                }
            }

        } catch (Exception e) {
            // Parse error, ignore
        }
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Do nothing - let the world show through
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // NO background fill - world is visible behind everything

        int centerX = this.width / 2;

        // --- Status text (top center, semi-transparent background) ---
        if (!this.statusText.isEmpty()) {
            int statusW = this.font.width(this.statusText);
            int statusX = centerX - statusW / 2;
            int statusY = 6;
            g.fill(statusX - 6, statusY - 2, statusX + statusW + 6, statusY + STATUS_BAR_HEIGHT - 4,
                    0x88000000);
            g.drawString(this.font, this.statusText, statusX, statusY + 2, TEXT_WHITE, false);
        }

        // --- Phase indicator (just below status) ---
        String phaseLabel = "Phase: " + this.phase;
        int phaseLabelW = this.font.width(phaseLabel);
        g.fill(centerX - phaseLabelW / 2 - 4, 20, centerX + phaseLabelW / 2 + 4, 32, 0x66000000);
        g.drawString(this.font, phaseLabel, centerX - phaseLabelW / 2, 22, TEXT_DIM, false);

        // --- Player summary panel (left side) - like wheel HUD ---
        if (!playerSummary.isEmpty()) {
            int psX = 8;
            int psY = 40;
            int psW = 150;

            // Calculate total pot
            int totalPot = 0;
            for (PlayerBetInfo pbi : playerSummary) totalPot += pbi.bet;

            int psH = 28 + playerSummary.size() * 14;
            g.fill(psX, psY, psX + psW, psY + psH, 0xAA000000);
            g.fill(psX, psY, psX + psW, psY + 1, GOLD_BORDER);
            g.fill(psX, psY + psH - 1, psX + psW, psY + psH, GOLD_BORDER);
            g.fill(psX, psY, psX + 1, psY + psH, GOLD_BORDER);
            g.fill(psX + psW - 1, psY, psX + psW, psY + psH, GOLD_BORDER);

            g.drawString(this.font, "Blackjack Table", psX + 4, psY + 3, TEXT_GOLD, false);
            g.drawString(this.font, "Pot: " + totalPot + " MC", psX + psW - this.font.width("Pot: " + totalPot + " MC") - 4, psY + 3, 0xFF00FF00, false);

            g.fill(psX + 2, psY + 14, psX + psW - 2, psY + 15, 0xFF444444);

            for (int i = 0; i < playerSummary.size(); i++) {
                PlayerBetInfo pbi = playerSummary.get(i);
                int py = psY + 16 + i * 14;

                // Active indicator arrow
                if (pbi.isActive) {
                    g.drawString(this.font, "\u25B6", psX + 4, py, 0xFFFFFF00, false);
                }

                // Player name
                int nameColor = pbi.isActive ? 0xFFFFFF00 : (pbi.bust ? 0xFFFF4444 : (pbi.blackjack ? 0xFF00FF00 : TEXT_WHITE));
                g.drawString(this.font, pbi.name, psX + 14, py, nameColor, false);

                // Bet amount
                if (pbi.bet > 0) {
                    String betStr = pbi.bet + "MC";
                    g.drawString(this.font, betStr, psX + psW - this.font.width(betStr) - 4, py, 0xFFD4AF37, false);
                }

                // Status below name
                String status = "";
                if (pbi.bust) status = "BUST";
                else if (pbi.blackjack) status = "BLACKJACK!";
                else if (pbi.handValue > 0) status = "Hand: " + pbi.handValue;
                if (pbi.done && !pbi.bust && !pbi.blackjack) status += " (done)";

                if (!status.isEmpty()) {
                    int statusColor = pbi.bust ? 0xFFFF4444 : (pbi.blackjack ? 0xFF00FF00 : 0xFF888899);
                    // Render status inline after bet
                }
            }
        }

        // --- Floating cards for other players (above the HUD bar) ---
        renderOtherPlayersFloating(g, mouseX, mouseY);

        // --- Bottom HUD bar ---
        int barY = this.height - HUD_BAR_HEIGHT;
        g.fill(0, barY, this.width, this.height, 0xAA000000);
        // Thin gold line at top of bar
        g.fill(0, barY, this.width, barY + 1, GOLD_BORDER);

        // === Left section: Dealer info ===
        int dealerSectionX = 8;
        int dealerLabelY = barY + 4;
        g.drawString(this.font, "Dealer:", dealerSectionX, dealerLabelY, TEXT_GOLD, false);

        // Dealer cards drawn inline
        int dealerCardX = dealerSectionX + this.font.width("Dealer: ");
        for (int i = 0; i < this.dealerCards.size(); i++) {
            String cardStr = this.dealerCards.get(i);
            int cx = dealerCardX + i * (SMALL_CARD_W + 2);
            if ("??".equals(cardStr) && !this.dealerRevealed) {
                renderSmallCardBack(g, cx, dealerLabelY - 2);
            } else {
                renderSmallCard(g, cx, dealerLabelY - 2, cardStr);
            }
        }

        // Dealer value
        if (this.dealerRevealed && this.dealerValue > 0) {
            int dealerValX = dealerCardX + this.dealerCards.size() * (SMALL_CARD_W + 2) + 4;
            g.drawString(this.font, "(" + this.dealerValue + ")", dealerValX, dealerLabelY, TEXT_WHITE, false);
        }

        // === Center section: Your hand ===
        if (this.mySeatIndex >= 0 && this.mySeatIndex < this.seats.size()) {
            SeatInfo mySeat = this.seats.get(this.mySeatIndex);
            if (!mySeat.playerId.isEmpty() && !mySeat.hands.isEmpty()) {
                String youLabel = "You:";
                int youLabelW = this.font.width(youLabel);
                int handAreaStartX = centerX - 60;

                g.drawString(this.font, youLabel, handAreaStartX, dealerLabelY, TEXT_GOLD, false);

                int myCardX = handAreaStartX + youLabelW + 4;

                for (int h = 0; h < mySeat.hands.size(); h++) {
                    List<String> hand = mySeat.hands.get(h);
                    for (int c = 0; c < hand.size(); c++) {
                        int cardDrawX = myCardX + c * (SMALL_CARD_W + 2);
                        renderSmallCard(g, cardDrawX, dealerLabelY - 2, hand.get(c));
                    }

                    // Hand value
                    if (h < mySeat.handValues.size()) {
                        int valX = myCardX + hand.size() * (SMALL_CARD_W + 2) + 2;
                        g.drawString(this.font, "(" + mySeat.handValues.get(h) + ")",
                                valX, dealerLabelY, TEXT_WHITE, false);
                        myCardX = valX + this.font.width("(" + mySeat.handValues.get(h) + ")") + 6;
                    }
                }

                // Bet display with chip stack
                if (mySeat.bet > 0) {
                    String betStr = "Bet: " + mySeat.bet + " MC";
                    g.drawString(this.font, betStr, handAreaStartX, barY + 20, TEXT_GOLD, false);
                    // Render chip stack next to the bet text
                    int chipStackX = handAreaStartX + this.font.width(betStr) + 16;
                    int chipStackY = barY + 22;
                    ChipRenderer.renderChipStack(g, this.font, chipStackX, chipStackY, mySeat.bet);
                }
            }
        }

        // === Right section: Action buttons ===
        boolean isMyTurn = "PLAYER_TURN".equals(this.phase) && this.mySeatIndex == this.currentSeat;
        if (isMyTurn && !this.availableActions.isEmpty()) {
            renderActionButtons(g, mouseX, mouseY, barY);
        }

        // --- Second row in HUD: additional info ---
        int secondRowY = barY + 38;
        // Turn indicator
        if ("PLAYER_TURN".equals(this.phase)) {
            String turnStr;
            if (isMyTurn) {
                turnStr = "YOUR TURN";
            } else if (this.currentSeat >= 0 && this.currentSeat < this.seats.size()) {
                SeatInfo turnSeat = this.seats.get(this.currentSeat);
                turnStr = turnSeat.playerName + "'s turn";
            } else {
                turnStr = "Waiting...";
            }
            int turnW = this.font.width(turnStr);
            g.drawString(this.font, turnStr, centerX - turnW / 2, secondRowY, isMyTurn ? TEXT_GOLD : TEXT_DIM, false);
        }

        // --- Betting phase UI (centered overlay panel) ---
        if ("BETTING".equals(this.phase) && !this.betPlaced && this.mySeatIndex >= 0) {
            renderBettingUI(g, mouseX, mouseY);
        }

        // --- Result message overlay (center screen) ---
        if (this.resultTimer > 0 && !this.resultMessage.isEmpty()) {
            int msgW = this.font.width(this.resultMessage);
            int msgX = centerX - msgW / 2;
            int msgY = this.height / 2 - 20;
            g.fill(msgX - 8, msgY - 6, msgX + msgW + 8, msgY + 16, 0xCC000000);
            drawRectOutline(g, msgX - 8, msgY - 6, msgX + msgW + 8, msgY + 16, GOLD_BORDER);
            g.drawString(this.font, this.resultMessage, msgX, msgY, TEXT_GOLD, false);
        }

        super.render(g, mouseX, mouseY, partialTick);

        // Dragged chip always on top of everything
        ChipRenderer.renderDraggedChip(g, this.font, mouseX, mouseY);
    }

    private void renderOtherPlayersFloating(GuiGraphics g, int mouseX, int mouseY) {
        // Render other players' hands as small floating indicators above the HUD bar
        int floatingY = this.height - HUD_BAR_HEIGHT - 40;
        int maxSeats = Math.max(this.seats.size(), 4);

        // Only render other players (not myself)
        List<SeatInfo> others = new ArrayList<>();
        for (int i = 0; i < this.seats.size(); i++) {
            if (i != this.mySeatIndex && !this.seats.get(i).playerId.isEmpty()) {
                others.add(this.seats.get(i));
            }
        }

        if (others.isEmpty()) return;

        int cardGroupW = 80;
        int totalW = others.size() * cardGroupW + (others.size() - 1) * 10;
        int startX = (this.width - totalW) / 2;

        for (int i = 0; i < others.size(); i++) {
            SeatInfo seat = others.get(i);
            int groupX = startX + i * (cardGroupW + 10);

            // Semi-transparent background for this player's floating area
            g.fill(groupX - 2, floatingY - 2, groupX + cardGroupW + 2, floatingY + 34, 0x55000000);

            // Player name
            String name = seat.playerName;
            if (name.length() > 8) name = name.substring(0, 7) + ".";
            g.drawString(this.font, name, groupX + 2, floatingY, TEXT_DIM, false);

            // Cards
            int cardDrawY = floatingY + 12;
            if (!seat.hands.isEmpty()) {
                List<String> hand = seat.hands.get(0);
                for (int c = 0; c < hand.size() && c < 6; c++) {
                    renderSmallCard(g, groupX + 2 + c * SMALL_CARD_OVERLAP, cardDrawY, hand.get(c));
                }

                // Value
                if (!seat.handValues.isEmpty()) {
                    String val = "(" + seat.handValues.get(0) + ")";
                    int valX = groupX + cardGroupW - this.font.width(val) - 2;
                    g.drawString(this.font, val, valX, floatingY, TEXT_WHITE, false);
                }
            }

            // Done indicator
            if (seat.done) {
                g.drawString(this.font, "DONE", groupX + 2, cardDrawY + SMALL_CARD_H + 2, TEXT_DIM, false);
            }
        }
    }

    private void renderActionButtons(GuiGraphics g, int mouseX, int mouseY, int barY) {
        int btnW = 50;
        int btnH = 18;
        int btnGap = 4;
        int totalW = this.availableActions.size() * (btnW + btnGap) - btnGap;
        int startX = this.width - totalW - 10;
        int btnY = barY + 6;

        for (int i = 0; i < this.availableActions.size(); i++) {
            String action = this.availableActions.get(i);
            int bx = startX + i * (btnW + btnGap);
            boolean hover = mouseX >= bx && mouseX < bx + btnW && mouseY >= btnY && mouseY < btnY + btnH;

            int bgColor;
            int hoverColor;
            switch (action.toUpperCase()) {
                case "HIT" -> { bgColor = BTN_HIT; hoverColor = BTN_HIT_HOVER; }
                case "STAND" -> { bgColor = BTN_STAND; hoverColor = BTN_STAND_HOVER; }
                case "DOUBLE" -> { bgColor = BTN_DOUBLE; hoverColor = BTN_DOUBLE_HOVER; }
                case "SPLIT" -> { bgColor = BTN_SPLIT; hoverColor = BTN_SPLIT_HOVER; }
                case "SURRENDER" -> { bgColor = BTN_SURRENDER; hoverColor = BTN_SURRENDER_HOVER; }
                default -> { bgColor = 0xFF555555; hoverColor = 0xFF777777; }
            }

            g.fill(bx, btnY, bx + btnW, btnY + btnH, hover ? hoverColor : bgColor);
            drawRectOutline(g, bx, btnY, bx + btnW, btnY + btnH, 0xFF000000);

            String label = action.substring(0, 1).toUpperCase() + action.substring(1).toLowerCase();
            int labelW = this.font.width(label);
            g.drawString(this.font, label, bx + (btnW - labelW) / 2, btnY + (btnH - 9) / 2, 0xFFFFFFFF, false);
        }
    }

    private void renderBettingUI(GuiGraphics g, int mouseX, int mouseY) {
        int centerX = this.width / 2;

        // --- Bet zone (drop target) in the center of the screen ---
        int betZoneX = centerX - BET_ZONE_W / 2;
        int betZoneY = this.height / 2 - BET_ZONE_H / 2 - 20;
        boolean overBetZone = mouseX >= betZoneX && mouseX < betZoneX + BET_ZONE_W
                && mouseY >= betZoneY && mouseY < betZoneY + BET_ZONE_H;
        boolean draggingOverZone = ChipRenderer.isDragging() && overBetZone;

        // Background — highlighted when dragging a chip over it
        int zoneColor = draggingOverZone ? 0x6600FF00 : 0x44FFFFFF;
        g.fill(betZoneX, betZoneY, betZoneX + BET_ZONE_W, betZoneY + BET_ZONE_H, zoneColor);
        int borderColor = draggingOverZone ? 0xFF00FF00 : 0xFFD4AF37;
        drawRectOutline(g, betZoneX, betZoneY, betZoneX + BET_ZONE_W, betZoneY + BET_ZONE_H, borderColor);

        // Label inside zone
        if (this.betAmount == 0) {
            String zoneLabel = "DROP CHIPS HERE";
            int zlW = this.font.width(zoneLabel);
            g.drawString(this.font, zoneLabel, centerX - zlW / 2, betZoneY + (BET_ZONE_H - 9) / 2,
                    draggingOverZone ? 0xFF00FF00 : TEXT_DIM, false);
        } else {
            // Show chip stack pile inside the bet zone
            int betZoneCenterX = centerX;
            int betZoneCenterY = betZoneY + BET_ZONE_H / 2;
            ChipRenderer.renderChipStack(g, this.font, betZoneCenterX, betZoneCenterY - 5, this.betAmount);
        }

        // "Current Bet" label above zone
        String curBetStr = "Current Bet: " + this.betAmount + " MC";
        int cbW = this.font.width(curBetStr);
        g.drawString(this.font, curBetStr, centerX - cbW / 2, betZoneY - 14, TEXT_GOLD, false);

        // --- Buttons row below the bet zone ---
        int btnRowY = betZoneY + BET_ZONE_H + 6;

        // Clear Bet button
        int clearBtnW = 60;
        int clearBtnH = 16;
        int clearBtnX = centerX - clearBtnW / 2 - 38;
        boolean clearHover = mouseX >= clearBtnX && mouseX < clearBtnX + clearBtnW
                && mouseY >= btnRowY && mouseY < btnRowY + clearBtnH;
        g.fill(clearBtnX, btnRowY, clearBtnX + clearBtnW, btnRowY + clearBtnH,
                clearHover ? BTN_SURRENDER_HOVER : BTN_SURRENDER);
        String clearStr = "CLEAR";
        int clearStrW = this.font.width(clearStr);
        g.drawString(this.font, clearStr, clearBtnX + (clearBtnW - clearStrW) / 2,
                btnRowY + (clearBtnH - 9) / 2, 0xFFFFFFFF, false);

        // Place Bet / Deal button
        int placeBtnW = 60;
        int placeBtnH = 16;
        int placeBtnX = centerX - placeBtnW / 2 + 38;
        boolean placeHover = this.betAmount > 0 && mouseX >= placeBtnX && mouseX < placeBtnX + placeBtnW
                && mouseY >= btnRowY && mouseY < btnRowY + placeBtnH;
        int placeBg = this.betAmount > 0 ? (placeHover ? BET_BTN_HOVER : BET_BTN) : 0xFF333333;
        g.fill(placeBtnX, btnRowY, placeBtnX + placeBtnW, btnRowY + placeBtnH, placeBg);
        String placeStr = "DEAL";
        int placeStrW = this.font.width(placeStr);
        g.drawString(this.font, placeStr, placeBtnX + (placeBtnW - placeStrW) / 2,
                btnRowY + (placeBtnH - 9) / 2, this.betAmount > 0 ? 0xFFFFFFFF : 0xFF666666, false);

        // --- Chip tray (bottom of the betting area) ---
        this.chipTrayW = 200;
        this.chipTrayX = centerX - this.chipTrayW / 2;
        this.chipTrayY = btnRowY + clearBtnH + 10;
        ChipRenderer.renderChipTray(g, this.font, this.chipTrayX, this.chipTrayY, this.chipTrayW, mouseX, mouseY);

        // Hint text below tray
        String hint = "Drag chips onto the bet zone";
        int hintW = this.font.width(hint);
        g.drawString(this.font, hint, centerX - hintW / 2, this.chipTrayY - 10, TEXT_DIM, false);
    }

    private void renderSmallCard(GuiGraphics g, int x, int y, String cardStr) {
        g.fill(x, y, x + SMALL_CARD_W, y + SMALL_CARD_H, 0xFF444444);
        g.fill(x + 1, y + 1, x + SMALL_CARD_W - 1, y + SMALL_CARD_H - 1, CARD_WHITE);

        if ("??".equals(cardStr)) {
            g.fill(x + 1, y + 1, x + SMALL_CARD_W - 1, y + SMALL_CARD_H - 1, CARD_BACK);
            return;
        }

        if (cardStr.length() < 2) return;

        String rank;
        String suit;
        if (cardStr.length() == 3) {
            rank = cardStr.substring(0, 2);
            suit = cardStr.substring(2);
        } else {
            rank = cardStr.substring(0, 1);
            suit = cardStr.substring(1);
        }

        int suitColor = (HEART.equals(suit) || DIAMOND.equals(suit)) ? 0xFFCC0000 : 0xFF111111;
        g.drawString(this.font, rank, x + 2, y + 2, suitColor, false);
        g.drawString(this.font, suit, x + 2, y + SMALL_CARD_H - 10, suitColor, false);
    }

    private void renderSmallCardBack(GuiGraphics g, int x, int y) {
        g.fill(x, y, x + SMALL_CARD_W, y + SMALL_CARD_H, 0xFF444444);
        g.fill(x + 1, y + 1, x + SMALL_CARD_W - 1, y + SMALL_CARD_H - 1, CARD_BACK);
        for (int i = 0; i < SMALL_CARD_H; i += 4) {
            g.fill(x + 2, y + 1 + i, x + SMALL_CARD_W - 2, y + 2 + i, CARD_BACK_PATTERN);
        }
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean consumed) {
        if (consumed) {
            return super.mouseClicked(event, consumed);
        }

        int mx = (int) event.x();
        int my = (int) event.y();

        // --- Action buttons ---
        boolean isMyTurn = "PLAYER_TURN".equals(this.phase) && this.mySeatIndex == this.currentSeat;
        if (isMyTurn && !this.availableActions.isEmpty()) {
            int barY = this.height - HUD_BAR_HEIGHT;
            int btnW = 50;
            int btnH = 18;
            int btnGap = 4;
            int totalW = this.availableActions.size() * (btnW + btnGap) - btnGap;
            int startX = this.width - totalW - 10;
            int btnY = barY + 6;

            for (int i = 0; i < this.availableActions.size(); i++) {
                int bx = startX + i * (btnW + btnGap);
                if (mx >= bx && mx < bx + btnW && my >= btnY && my < btnY + btnH) {
                    String action = this.availableActions.get(i);
                    sendAction(action, "");
                    return true;
                }
            }
        }

        // --- Betting UI (chip drag-and-drop) ---
        if ("BETTING".equals(this.phase) && !this.betPlaced && this.mySeatIndex >= 0) {
            int centerX = this.width / 2;

            // Check chip tray click (starts drag)
            if (ChipRenderer.handleTrayClick(mx, my, this.chipTrayX, this.chipTrayY, this.chipTrayW)) {
                return true;
            }

            // Button row positions (must match renderBettingUI layout)
            int betZoneY = this.height / 2 - BET_ZONE_H / 2 - 20;
            int btnRowY = betZoneY + BET_ZONE_H + 6;
            int btnH = 16;

            // Clear Bet button
            int clearBtnW = 60;
            int clearBtnX = centerX - clearBtnW / 2 - 38;
            if (mx >= clearBtnX && mx < clearBtnX + clearBtnW && my >= btnRowY && my < btnRowY + btnH) {
                this.betAmount = 0;
                return true;
            }

            // Place Bet / Deal button
            int placeBtnW = 60;
            int placeBtnX = centerX - placeBtnW / 2 + 38;
            if (this.betAmount > 0 && mx >= placeBtnX && mx < placeBtnX + placeBtnW
                    && my >= btnRowY && my < btnRowY + btnH) {
                sendAction("bet", String.valueOf(this.betAmount));
                this.betPlaced = true;
                return true;
            }
        }

        return super.mouseClicked(event, consumed);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        // No scroll-based bet adjustment — use chip drag-and-drop instead
        return false;
    }

    public boolean mouseReleased(MouseButtonEvent e) {
        int mx = (int) e.x();
        int my = (int) e.y();
        if (ChipRenderer.isDragging()) {
            int centerX = this.width / 2;
            int betZoneX = centerX - BET_ZONE_W / 2;
            int betZoneY = this.height / 2 - BET_ZONE_H / 2 - 20;

            boolean overBetZone = mx >= betZoneX && mx < betZoneX + BET_ZONE_W
                    && my >= betZoneY && my < betZoneY + BET_ZONE_H;

            if (overBetZone) {
                ChipDenomination dropped = ChipRenderer.completeDrop();
                if (dropped != null) {
                    this.betAmount += dropped.value;
                    // Send chip bet to server so it can validate and deduct the chip
                    sendAction("chipbet", String.valueOf(dropped.value));
                }
            } else {
                ChipRenderer.cancelDrag();
            }
            return true;
        }
        return super.mouseReleased(e);
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            sendAction("leave", "");
            this.onClose();
            return true;
        }
        return false;
    }

    private void sendAction(String action, String data) {
        ClientPacketDistributor.sendToServer(
                new BlackjackActionPayload(this.tablePos, action, data));
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
}

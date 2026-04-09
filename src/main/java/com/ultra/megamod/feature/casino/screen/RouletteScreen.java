package com.ultra.megamod.feature.casino.screen;

import com.ultra.megamod.feature.casino.network.RouletteActionPayload;
import com.ultra.megamod.feature.casino.network.RouletteGameSyncPayload;
import com.ultra.megamod.feature.casino.roulette.RouletteGame;
import com.ultra.megamod.feature.casino.chips.ChipRenderer;
import com.ultra.megamod.feature.casino.chips.ChipDenomination;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Client-side roulette GUI.
 * Layout: right-side panel with bet controls, number grid, outside bets,
 * active bets list, timer bar, and result display.
 */
public class RouletteScreen extends Screen {

    private final BlockPos tablePos;

    // Synced state from server
    private String phase = "BETTING";
    private int timer = 0;
    private int maxTimer = 300;
    private int resultNumber = -1;
    private String resultColor = "";
    private int totalPot = 0;
    private int totalPlayers = 0;

    private final List<BetEntry> activeBets = new ArrayList<>();
    record BetEntry(String player, String betType, int amount) {}

    // Client-side bet input (chip drag-and-drop)
    private String selectedBetType = "red";
    private int selectedStraightNumber = -1; // -1 = no straight number selected
    private int currentBetAmount = 0;
    private int chipTrayX, chipTrayY, chipTrayW;

    // Scroll offset for active bets list
    private int betsScrollOffset = 0;

    // Spinning wheel animation state
    private float wheelAngle = 0;        // current rotation angle in radians
    private float wheelSpeed = 0;        // current rotation speed (radians per tick)
    private int winningNumber = -1;      // the result number for positioning the wheel stop
    private boolean wheelSpinning = false;

    // Split mode toggle for split bets
    private boolean splitMode = false;

    // Tracks all placed bets (betType -> amount) for chip stack display
    private final Map<String, Integer> placedBets = new LinkedHashMap<>();

    // Wheel segment order (European roulette wheel sequence)
    private static final int[] WHEEL_ORDER = {
            0, 32, 15, 19, 4, 21, 2, 25, 17, 34, 6, 27, 13, 36,
            11, 30, 8, 23, 10, 5, 24, 16, 33, 1, 20, 14, 31, 9,
            22, 18, 29, 7, 28, 12, 35, 3, 26
    };

    // Layout constants
    private static final int PANEL_W = 200;
    private static final int BG = 0xAA000000;
    private static final int BORDER = 0xFFD4AF37;
    private static final int TEXT = 0xFFCCCCDD;
    private static final int GOLD = 0xFFD4AF37;
    private static final int DIM = 0xFF888899;
    private static final int RED = 0xFFCC2222;
    private static final int BLACK_COL = 0xFF222222;
    private static final int GREEN = 0xFF008800;

    // Number grid layout: 3 columns x 12 rows + 0 at top
    private static final int GRID_CELL_W = 16;
    private static final int GRID_CELL_H = 12;

    // Roulette red numbers
    private static final Set<Integer> REDS = RouletteGame.REDS;

    // Animation timing
    private int spinTicksElapsed = 0;
    private float targetWheelAngle = 0;

    public RouletteScreen(BlockPos tablePos) {
        super(Component.literal("Roulette"));
        this.tablePos = tablePos;
    }

    @Override
    protected void init() {
        super.init();
        // Request initial game state sync from server
        ClientPacketDistributor.sendToServer(
                (CustomPacketPayload) new RouletteActionPayload("", 0, tablePos),
                (CustomPacketPayload[]) new CustomPacketPayload[0]);
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Don't render default background - let world show through
    }

    @Override
    public void tick() {
        super.tick();
        RouletteGameSyncPayload sync = RouletteGameSyncPayload.lastSync;
        if (sync != null) {
            RouletteGameSyncPayload.lastSync = null;
            parseSync(sync.gameStateJson());
        }

        // Advance wheel animation during SPINNING phase
        if (wheelSpinning) {
            spinTicksElapsed++;
            wheelAngle += wheelSpeed;

            if ("SPINNING".equals(phase)) {
                // Decelerate smoothly — ease out over the full spin phase
                float progress = Math.min(1.0f, spinTicksElapsed / 55.0f);
                // Ease-out curve: starts fast, slows gradually
                wheelSpeed = 0.5f * (1.0f - progress * progress);
                if (wheelSpeed < 0.01f) wheelSpeed = 0.01f; // keep a tiny spin so it feels alive
            } else if ("RESULT".equals(phase)) {
                // Decelerate to the target angle over the first ~20 ticks of RESULT
                float diff = targetWheelAngle - wheelAngle;
                // Normalize the difference to the shortest path
                while (diff > Math.PI) diff -= (float)(2 * Math.PI);
                while (diff < -Math.PI) diff += (float)(2 * Math.PI);
                wheelAngle += diff * 0.15f;
                wheelSpeed *= 0.85f;
                if (Math.abs(diff) < 0.01f && wheelSpeed < 0.005f) {
                    wheelAngle = targetWheelAngle;
                    wheelSpeed = 0;
                    wheelSpinning = false;
                }
            }
        }
    }

    private void parseSync(String json) {
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            String prevPhase = this.phase;
            this.phase = root.has("phase") ? root.get("phase").getAsString() : "BETTING";
            this.timer = root.has("timer") ? root.get("timer").getAsInt() : 0;
            this.maxTimer = root.has("maxTimer") ? root.get("maxTimer").getAsInt() : 300;
            this.totalPot = root.has("totalPot") ? root.get("totalPot").getAsInt() : 0;
            this.totalPlayers = root.has("totalPlayers") ? root.get("totalPlayers").getAsInt() : 0;

            if (root.has("resultNumber")) {
                this.resultNumber = root.get("resultNumber").getAsInt();
                this.winningNumber = this.resultNumber;
            }
            if (root.has("resultColor")) {
                this.resultColor = root.get("resultColor").getAsString();
            }

            // Detect phase transitions for wheel animation
            if ("SPINNING".equals(this.phase) && !"SPINNING".equals(prevPhase)) {
                // Start spinning the wheel
                wheelSpeed = 0.5f;
                wheelSpinning = true;
                spinTicksElapsed = 0;
            } else if ("RESULT".equals(this.phase) && !"RESULT".equals(prevPhase)) {
                // Calculate the target angle so the winning segment lands at the pointer (top)
                if (winningNumber >= 0) {
                    int segIndex = getWheelSegmentIndex(winningNumber);
                    float segAngle = (float) (2 * Math.PI * segIndex / 37.0);
                    // Add extra full rotations so the wheel always spins forward to the target
                    targetWheelAngle = wheelAngle - segAngle;
                    // Normalize: ensure the target is ahead of current angle (smooth forward spin)
                    while (targetWheelAngle > wheelAngle) targetWheelAngle -= (float)(2 * Math.PI);
                    while (wheelAngle - targetWheelAngle > 2 * Math.PI) targetWheelAngle += (float)(2 * Math.PI);
                }
                // Keep spinning — tick() will decelerate to the target
                if (!wheelSpinning) {
                    // Edge case: if spinning never started, snap immediately
                    wheelSpinning = false;
                    if (winningNumber >= 0) {
                        int segIndex = getWheelSegmentIndex(winningNumber);
                        float segAngle = (float) (2 * Math.PI * segIndex / 37.0);
                        wheelAngle = -segAngle;
                    }
                }
            } else if ("BETTING".equals(this.phase) && !"BETTING".equals(prevPhase)) {
                // New round: clear placed bets display and reset wheel
                placedBets.clear();
                winningNumber = -1;
                wheelSpinning = false;
                wheelSpeed = 0;
                spinTicksElapsed = 0;
            }

            activeBets.clear();
            if (root.has("activeBets")) {
                for (JsonElement el : root.getAsJsonArray("activeBets")) {
                    JsonObject b = el.getAsJsonObject();
                    activeBets.add(new BetEntry(
                            b.get("player").getAsString(),
                            b.get("betType").getAsString(),
                            b.get("amount").getAsInt()));
                }
            }
        } catch (Exception ignored) {}
    }

    /** Returns the index of a number in the WHEEL_ORDER array. */
    private static int getWheelSegmentIndex(int number) {
        for (int i = 0; i < WHEEL_ORDER.length; i++) {
            if (WHEEL_ORDER[i] == number) return i;
        }
        return 0;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        int rx = this.width - PANEL_W - 10;
        int ty = 6;
        int ph = Math.min(this.height - 12, 400);

        // Panel background
        g.fill(rx, ty, rx + PANEL_W, ty + ph, BG);
        g.fill(rx, ty, rx + PANEL_W, ty + 1, BORDER);
        g.fill(rx, ty + ph - 1, rx + PANEL_W, ty + ph, BORDER);
        g.fill(rx, ty, rx + 1, ty + ph, BORDER);
        g.fill(rx + PANEL_W - 1, ty, rx + PANEL_W, ty + ph, BORDER);

        // Title
        g.drawString(this.font, "ROULETTE", rx + 6, ty + 4, GOLD, false);

        // Phase and timer
        int sec = timer / 20;
        String ps = switch (phase) {
            case "BETTING" -> "Place Bets: " + sec + "s";
            case "SPINNING" -> "Spinning...";
            case "RESULT" -> {
                String numStr = resultNumber >= 0 ? String.valueOf(resultNumber) : "?";
                yield "Result: " + numStr + " (" + resultColor + ")";
            }
            default -> phase;
        };
        g.drawString(this.font, ps, rx + 6, ty + 16, TEXT, false);

        // Timer bar
        int by = ty + 27;
        g.fill(rx + 6, by, rx + PANEL_W - 6, by + 4, 0xFF333333);
        if (maxTimer > 0) {
            int bw = (int) ((PANEL_W - 12) * ((float) timer / maxTimer));
            int barColor = "BETTING".equals(phase) ? 0xFF4CAF50 : 0xFFFF9800;
            g.fill(rx + 6, by, rx + 6 + bw, by + 4, barColor);
        }

        // Pot and wallet
        g.drawString(this.font, "Pot: " + totalPot + " MC", rx + 6, by + 8, DIM, false);
        int wallet = ChipRenderer.clientChipTotal;
        String walletStr = "Chips: " + wallet + " MC";
        int walletW = this.font.width(walletStr);
        g.drawString(this.font, walletStr, rx + PANEL_W - walletW - 6, by + 8, GOLD, false);

        int y = by + 22;

        // === Roulette wheel — always visible on the left side of the screen ===
        {
            // Bigger wheel: use available space left of the panel
            int maxR = Math.min((rx - 24) / 2, (this.height - 50) / 2);
            int wheelRadius = Math.max(60, Math.min(140, maxR));
            int wheelCX = rx / 2; // center of the space left of the panel
            int wheelCY = this.height / 2;
            // Clamp so it doesn't overlap the panel
            if (wheelCX + wheelRadius + 10 > rx) {
                wheelCX = (rx - wheelRadius) / 2 + wheelRadius / 2;
            }
            if (wheelCX < wheelRadius + 10) wheelCX = wheelRadius + 10;

            // Subtle dark background behind the wheel area
            g.fill(wheelCX - wheelRadius - 8, wheelCY - wheelRadius - 18,
                    wheelCX + wheelRadius + 8, wheelCY + wheelRadius + 28, 0x66000000);

            // Label above wheel
            String wheelLabel = switch (phase) {
                case "SPINNING" -> "SPINNING...";
                case "RESULT" -> "RESULT";
                default -> "ROULETTE WHEEL";
            };
            int labelW = this.font.width(wheelLabel);
            g.drawString(this.font, wheelLabel, wheelCX - labelW / 2, wheelCY - wheelRadius - 14, GOLD, false);

            // Interpolate wheel angle for smooth rendering between ticks
            float renderAngle = wheelAngle + wheelSpeed * partialTick;
            renderRouletteWheel(g, wheelCX, wheelCY, wheelRadius, renderAngle);

            // Result number display below the wheel
            if ("RESULT".equals(phase) && resultNumber >= 0) {
                int resultBgColor = getNumberBgColor(resultNumber);
                int resultBoxX = wheelCX - 20;
                int resultBoxY = wheelCY + wheelRadius + 8;
                g.fill(resultBoxX, resultBoxY, resultBoxX + 40, resultBoxY + 18, resultBgColor);
                drawRectOutline(g, resultBoxX, resultBoxY, resultBoxX + 40, resultBoxY + 18, GOLD);
                String numStr = String.valueOf(resultNumber);
                int numW = this.font.width(numStr);
                g.drawString(this.font, numStr, resultBoxX + 20 - numW / 2, resultBoxY + 5, 0xFFFFFFFF, false);
            } else if ("BETTING".equals(phase)) {
                // Show last result or "No Spin Yet"
                String hint = winningNumber >= 0 ? "Last: " + winningNumber : "No Spin Yet";
                int hintW = this.font.width(hint);
                g.drawString(this.font, hint, wheelCX - hintW / 2, wheelCY + wheelRadius + 10, DIM, false);
            }
        }

        // Betting controls (only during BETTING phase)
        if ("BETTING".equals(phase)) {
            g.fill(rx + 4, y, rx + PANEL_W - 4, y + 1, BORDER);
            y += 4;
            g.drawString(this.font, "Place Bet:", rx + 6, y, GOLD, false);
            y += 12;

            // === Number grid: 0 on top, then 3 columns x 12 rows ===
            int gridX = rx + 6;

            // Zero button
            boolean zeroSel = selectedBetType.equals("straight:0");
            boolean zeroHov = mouseX >= gridX && mouseX < gridX + GRID_CELL_W * 3 && mouseY >= y && mouseY < y + GRID_CELL_H;
            g.fill(gridX, y, gridX + GRID_CELL_W * 3, y + GRID_CELL_H, zeroSel ? 0xFF00CC00 : GREEN);
            if (zeroHov && !zeroSel) g.fill(gridX, y, gridX + GRID_CELL_W * 3, y + GRID_CELL_H, 0x44FFFFFF);
            g.drawString(this.font, "0", gridX + GRID_CELL_W * 3 / 2 - 2, y + 2, 0xFFFFFFFF, false);
            y += GRID_CELL_H;

            // Numbers 1-36: laid out as 12 rows x 3 columns
            // Row i: numbers 3i+1, 3i+2, 3i+3 (standard roulette board layout)
            int gridTopY = y; // save for street/corner markers
            for (int row = 0; row < 12; row++) {
                for (int col = 0; col < 3; col++) {
                    int num = row * 3 + col + 1;
                    int cx = gridX + col * GRID_CELL_W;
                    int cy = y + row * GRID_CELL_H;
                    int bgCol = getNumberBgColor(num);
                    boolean sel = selectedBetType.equals("straight:" + num);
                    boolean hov = mouseX >= cx && mouseX < cx + GRID_CELL_W && mouseY >= cy && mouseY < cy + GRID_CELL_H;

                    g.fill(cx, cy, cx + GRID_CELL_W, cy + GRID_CELL_H, sel ? GOLD : bgCol);
                    if (hov && !sel) g.fill(cx, cy, cx + GRID_CELL_W, cy + GRID_CELL_H, 0x44FFFFFF);
                    // outline
                    g.fill(cx, cy, cx + GRID_CELL_W, cy + 1, 0xFF444444);
                    g.fill(cx, cy, cx + 1, cy + GRID_CELL_H, 0xFF444444);

                    String numStr = String.valueOf(num);
                    int nw = this.font.width(numStr);
                    g.drawString(this.font, numStr, cx + (GRID_CELL_W - nw) / 2, cy + 2, 0xFFFFFFFF, false);

                    // Render chip stack on this number if bet placed
                    renderChipStackOnZone(g, "straight:" + num, cx + 2, cy + 1);
                }
            }

            // Street bet markers on the left edge of each row (small clickable strip)
            int streetMarkerW = 4;
            for (int row = 0; row < 12; row++) {
                int streetNum = row * 3 + 1;
                int smx = gridX - streetMarkerW - 1;
                int smy = gridTopY + row * GRID_CELL_H;
                String streetBet = "street:" + streetNum;
                boolean streetSel = selectedBetType.equals(streetBet);
                boolean streetHov = mouseX >= smx && mouseX < smx + streetMarkerW && mouseY >= smy && mouseY < smy + GRID_CELL_H;
                int streetColor = streetSel ? GOLD : (streetHov ? 0xFF66AA66 : 0xFF336633);
                g.fill(smx, smy, smx + streetMarkerW, smy + GRID_CELL_H, streetColor);
            }

            // Corner bet markers at intersections of 4 numbers (small dots)
            for (int row = 0; row < 11; row++) {
                for (int col = 0; col < 2; col++) {
                    int topLeft = row * 3 + col + 1;
                    String cornerBet = "corner:" + topLeft;
                    int dotX = gridX + (col + 1) * GRID_CELL_W - 2;
                    int dotY = gridTopY + (row + 1) * GRID_CELL_H - 2;
                    boolean cornerSel = selectedBetType.equals(cornerBet);
                    boolean cornerHov = mouseX >= dotX - 1 && mouseX < dotX + 5 && mouseY >= dotY - 1 && mouseY < dotY + 5;
                    int dotColor = cornerSel ? GOLD : (cornerHov ? 0xFFAAAA44 : 0xFF888844);
                    g.fill(dotX, dotY, dotX + 4, dotY + 4, dotColor);
                }
            }

            // Render chip stacks on zero
            renderChipStackOnZone(g, "straight:0", gridX + GRID_CELL_W, gridTopY - GRID_CELL_H + 1);

            y += 12 * GRID_CELL_H + 4;

            // === Outside bet buttons ===
            int btnW = (PANEL_W - 18) / 3;
            int btnH = 13;

            // Row 1: Red, Black, Green(0)
            renderOutsideBetButton(g, rx + 6, y, btnW, btnH, "Red", "red", RED, mouseX, mouseY);
            renderOutsideBetButton(g, rx + 6 + btnW + 2, y, btnW, btnH, "Black", "black", BLACK_COL, mouseX, mouseY);
            // The third slot: Odd
            renderOutsideBetButton(g, rx + 6 + (btnW + 2) * 2, y, btnW, btnH, "Odd", "odd", 0xFF555588, mouseX, mouseY);
            y += btnH + 2;

            // Row 2: Even, 1-18, 19-36
            renderOutsideBetButton(g, rx + 6, y, btnW, btnH, "Even", "even", 0xFF555588, mouseX, mouseY);
            renderOutsideBetButton(g, rx + 6 + btnW + 2, y, btnW, btnH, "1-18", "low", 0xFF336633, mouseX, mouseY);
            renderOutsideBetButton(g, rx + 6 + (btnW + 2) * 2, y, btnW, btnH, "19-36", "high", 0xFF336633, mouseX, mouseY);
            y += btnH + 2;

            // Row 3: Dozens
            renderOutsideBetButton(g, rx + 6, y, btnW, btnH, "1st 12", "dozen1", 0xFF444466, mouseX, mouseY);
            renderOutsideBetButton(g, rx + 6 + btnW + 2, y, btnW, btnH, "2nd 12", "dozen2", 0xFF444466, mouseX, mouseY);
            renderOutsideBetButton(g, rx + 6 + (btnW + 2) * 2, y, btnW, btnH, "3rd 12", "dozen3", 0xFF444466, mouseX, mouseY);
            y += btnH + 2;

            // Row 4: Column bets
            renderOutsideBetButton(g, rx + 6, y, btnW, btnH, "1st Col", "column1", 0xFF445544, mouseX, mouseY);
            renderOutsideBetButton(g, rx + 6 + btnW + 2, y, btnW, btnH, "2nd Col", "column2", 0xFF445544, mouseX, mouseY);
            renderOutsideBetButton(g, rx + 6 + (btnW + 2) * 2, y, btnW, btnH, "3rd Col", "column3", 0xFF445544, mouseX, mouseY);
            y += btnH + 2;

            // Row 5: Split mode toggle
            boolean splitHov = mouseX >= rx + 6 && mouseX < rx + 6 + btnW && mouseY >= y && mouseY < y + btnH;
            int splitBg = splitMode ? 0xFF7744AA : (splitHov ? 0xFF554477 : 0xFF443366);
            g.fill(rx + 6, y, rx + 6 + btnW, y + btnH, splitBg);
            drawRectOutline(g, rx + 6, y, rx + 6 + btnW, y + btnH, 0xFF555555);
            String splitLabel = splitMode ? "Split: ON" : "Split: OFF";
            int splitLabelW = this.font.width(splitLabel);
            g.drawString(this.font, splitLabel, rx + 6 + (btnW - splitLabelW) / 2, y + (btnH - 9) / 2, 0xFFFFFFFF, false);
            y += btnH + 6;

            // Selected bet display with current amount
            String betLabel = currentBetAmount > 0
                    ? "Bet: " + currentBetAmount + " MC on " + formatBetType(selectedBetType)
                    : "Bet: " + formatBetType(selectedBetType);
            g.drawString(this.font, betLabel, rx + 6, y, TEXT, false);
            y += 12;

            // Place Bet + Clear buttons
            int placeBtnW = 60;
            int clearBtnW = 40;
            int placeBtnX = rx + 6;
            int clearBtnX = placeBtnX + placeBtnW + 4;
            boolean placeHov = currentBetAmount > 0
                    && mouseX >= placeBtnX && mouseX < placeBtnX + placeBtnW
                    && mouseY >= y - 1 && mouseY < y + 12;
            boolean clearHov = currentBetAmount > 0
                    && mouseX >= clearBtnX && mouseX < clearBtnX + clearBtnW
                    && mouseY >= y - 1 && mouseY < y + 12;
            int placeBg = currentBetAmount > 0 ? (placeHov ? 0xFF4CAF50 : 0xFF2E7D32) : 0xFF333333;
            g.fill(placeBtnX, y - 1, placeBtnX + placeBtnW, y + 12, placeBg);
            String placeStr = "Place Bet";
            int placeStrW = this.font.width(placeStr);
            g.drawString(this.font, placeStr, placeBtnX + (placeBtnW - placeStrW) / 2, y + 1,
                    currentBetAmount > 0 ? 0xFFFFFFFF : 0xFF777777, false);
            if (currentBetAmount > 0) {
                g.fill(clearBtnX, y - 1, clearBtnX + clearBtnW, y + 12, clearHov ? 0xFFCC4444 : 0xFF993333);
                String clearStr = "Clear";
                int clearStrW = this.font.width(clearStr);
                g.drawString(this.font, clearStr, clearBtnX + (clearBtnW - clearStrW) / 2, y + 1, 0xFFFFFFFF, false);
            }
            y += 16;

            // Chip tray (drag chips onto bet zones above)
            chipTrayX = rx + 4;
            chipTrayY = y;
            chipTrayW = PANEL_W - 8;
            int trayHeight = ChipRenderer.renderChipTray(g, this.font, chipTrayX, chipTrayY, chipTrayW, mouseX, mouseY);
            y += trayHeight + 4;

        }

        // === Active bets list ===
        g.fill(rx + 4, y, rx + PANEL_W - 4, y + 1, BORDER);
        y += 4;
        g.drawString(this.font, "Active Bets (" + totalPlayers + " players):", rx + 6, y, GOLD, false);
        y += 12;
        if (activeBets.isEmpty()) {
            g.drawString(this.font, "No bets yet", rx + 10, y, DIM, false);
            y += 12;
        } else {
            int maxVisible = Math.min(activeBets.size() - betsScrollOffset, 6);
            for (int i = 0; i < maxVisible; i++) {
                int idx = i + betsScrollOffset;
                if (idx >= activeBets.size()) break;
                BetEntry be = activeBets.get(idx);
                String betLabel = formatBetType(be.betType());
                String s = be.player() + ": " + be.amount() + " on " + betLabel;
                if (this.font.width(s) > PANEL_W - 16) {
                    s = be.player().substring(0, Math.min(6, be.player().length())) + ":" + be.amount() + " " + betLabel;
                }
                g.drawString(this.font, s, rx + 10, y, TEXT, false);
                y += 11;
            }
            if (activeBets.size() > 6) {
                g.drawString(this.font, "Scroll for more (" + activeBets.size() + " total)", rx + 10, y, DIM, false);
                y += 11;
            }
        }

        // Footer
        g.drawString(this.font, "ESC close | Drag chips to bet", rx + 6, ty + ph - 12, DIM, false);

        // Dragged chip overlay (must be last so it renders on top)
        ChipRenderer.renderDraggedChip(g, this.font, mouseX, mouseY);

        super.render(g, mouseX, mouseY, partialTick);
    }

    /**
     * Renders a clickable outside bet button and highlights if selected.
     */
    private void renderOutsideBetButton(GuiGraphics g, int x, int y, int w, int h,
                                         String label, String betType, int bgColor,
                                         int mouseX, int mouseY) {
        boolean sel = selectedBetType.equals(betType);
        boolean hov = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;

        g.fill(x, y, x + w, y + h, sel ? GOLD : bgColor);
        if (hov && !sel) g.fill(x, y, x + w, y + h, 0x44FFFFFF);
        drawRectOutline(g, x, y, x + w, y + h, 0xFF555555);

        int labelW = this.font.width(label);
        g.drawString(this.font, label, x + (w - labelW) / 2, y + (h - 9) / 2, 0xFFFFFFFF, false);

        // Render chip stack if bet placed on this zone
        renderChipStackOnZone(g, betType, x + w - 10, y + 1);
    }

    /**
     * Returns the background color for a roulette number cell.
     */
    private static int getNumberBgColor(int number) {
        if (number == 0) return GREEN;
        return REDS.contains(number) ? RED : BLACK_COL;
    }

    /**
     * Formats a bet type string for display.
     */
    private static String formatBetType(String betType) {
        if (betType.startsWith("straight:")) {
            return "#" + betType.substring(9);
        }
        if (betType.startsWith("split:")) {
            String[] parts = betType.split(":");
            return "Split " + parts[1] + "/" + parts[2];
        }
        if (betType.startsWith("street:")) {
            int start = Integer.parseInt(betType.substring(7));
            return "Street " + start + "-" + (start + 2);
        }
        if (betType.startsWith("corner:")) {
            int tl = Integer.parseInt(betType.substring(7));
            return "Corner " + tl + "/" + (tl + 1) + "/" + (tl + 3) + "/" + (tl + 4);
        }
        return switch (betType) {
            case "red" -> "Red";
            case "black" -> "Black";
            case "odd" -> "Odd";
            case "even" -> "Even";
            case "low" -> "1-18";
            case "high" -> "19-36";
            case "dozen1" -> "1st Dozen";
            case "dozen2" -> "2nd Dozen";
            case "dozen3" -> "3rd Dozen";
            case "column1" -> "1st Column";
            case "column2" -> "2nd Column";
            case "column3" -> "3rd Column";
            default -> betType;
        };
    }

    // ==========================================================================
    // Input handling
    // ==========================================================================

    public boolean mouseClicked(MouseButtonEvent event, boolean consumed) {
        if (consumed) return super.mouseClicked(event, consumed);
        int mx = (int) event.x();
        int my = (int) event.y();

        if (!"BETTING".equals(phase)) return super.mouseClicked(event, consumed);

        // Check chip tray click first (starts drag)
        if (ChipRenderer.handleTrayClick(mx, my, chipTrayX, chipTrayY, chipTrayW)) {
            return true;
        }

        int rx = this.width - PANEL_W - 10;
        int ty = 6;
        int by = ty + 27;

        // Calculate y positions to match render()
        int y = by + 22;

        // Skip result display area (not shown during BETTING)
        // "Place Bet:" header
        y += 4 + 12; // separator + label

        int gridX = rx + 6;

        // Zero button
        if (mx >= gridX && mx < gridX + GRID_CELL_W * 3 && my >= y && my < y + GRID_CELL_H) {
            selectedBetType = "straight:0";
            selectedStraightNumber = 0;
            return true;
        }
        y += GRID_CELL_H;

        // Street bet markers on left edge of each row
        int streetMarkerW = 4;
        int gridTopY = y; // matches render gridTopY
        for (int row = 0; row < 12; row++) {
            int streetNum = row * 3 + 1;
            int smx = gridX - streetMarkerW - 1;
            int smy = y + row * GRID_CELL_H;
            if (mx >= smx && mx < smx + streetMarkerW && my >= smy && my < smy + GRID_CELL_H) {
                selectedBetType = "street:" + streetNum;
                selectedStraightNumber = -1;
                return true;
            }
        }

        // Corner bet markers at intersections
        for (int row = 0; row < 11; row++) {
            for (int col = 0; col < 2; col++) {
                int topLeft = row * 3 + col + 1;
                int dotX = gridX + (col + 1) * GRID_CELL_W - 2;
                int dotY = y + (row + 1) * GRID_CELL_H - 2;
                if (mx >= dotX - 1 && mx < dotX + 5 && my >= dotY - 1 && my < dotY + 5) {
                    selectedBetType = "corner:" + topLeft;
                    selectedStraightNumber = -1;
                    return true;
                }
            }
        }

        // Number grid 1-36
        for (int row = 0; row < 12; row++) {
            for (int col = 0; col < 3; col++) {
                int num = row * 3 + col + 1;
                int cx = gridX + col * GRID_CELL_W;
                int cy = y + row * GRID_CELL_H;
                if (mx >= cx && mx < cx + GRID_CELL_W && my >= cy && my < cy + GRID_CELL_H) {
                    if (splitMode) {
                        // In split mode: bet on this number and adjacent (right, or below if in last column)
                        int other;
                        if (col < 2) {
                            other = num + 1; // number to the right
                        } else {
                            other = num + 3; // number below (next row, same column)
                            if (other > 36) other = num - 1; // fallback to left if at bottom-right
                        }
                        selectedBetType = "split:" + Math.min(num, other) + ":" + Math.max(num, other);
                    } else {
                        selectedBetType = "straight:" + num;
                    }
                    selectedStraightNumber = splitMode ? -1 : num;
                    return true;
                }
            }
        }
        y += 12 * GRID_CELL_H + 4;

        // Outside bet buttons
        int btnW = (PANEL_W - 18) / 3;
        int btnH = 13;

        // Row 1: Red, Black, Odd
        if (clickOutsideBet(mx, my, rx + 6, y, btnW, btnH, "red")) return true;
        if (clickOutsideBet(mx, my, rx + 6 + btnW + 2, y, btnW, btnH, "black")) return true;
        if (clickOutsideBet(mx, my, rx + 6 + (btnW + 2) * 2, y, btnW, btnH, "odd")) return true;
        y += btnH + 2;

        // Row 2: Even, 1-18, 19-36
        if (clickOutsideBet(mx, my, rx + 6, y, btnW, btnH, "even")) return true;
        if (clickOutsideBet(mx, my, rx + 6 + btnW + 2, y, btnW, btnH, "low")) return true;
        if (clickOutsideBet(mx, my, rx + 6 + (btnW + 2) * 2, y, btnW, btnH, "high")) return true;
        y += btnH + 2;

        // Row 3: Dozens
        if (clickOutsideBet(mx, my, rx + 6, y, btnW, btnH, "dozen1")) return true;
        if (clickOutsideBet(mx, my, rx + 6 + btnW + 2, y, btnW, btnH, "dozen2")) return true;
        if (clickOutsideBet(mx, my, rx + 6 + (btnW + 2) * 2, y, btnW, btnH, "dozen3")) return true;
        y += btnH + 2;

        // Row 4: Column bets
        if (clickOutsideBet(mx, my, rx + 6, y, btnW, btnH, "column1")) return true;
        if (clickOutsideBet(mx, my, rx + 6 + btnW + 2, y, btnW, btnH, "column2")) return true;
        if (clickOutsideBet(mx, my, rx + 6 + (btnW + 2) * 2, y, btnW, btnH, "column3")) return true;
        y += btnH + 2;

        // Row 5: Split mode toggle
        if (mx >= rx + 6 && mx < rx + 6 + btnW && my >= y && my < y + btnH) {
            splitMode = !splitMode;
            return true;
        }
        y += btnH + 6;

        // Selected bet label
        y += 12;

        // Place Bet button
        int placeBtnW = 60;
        int clearBtnW = 40;
        int placeBtnX = rx + 6;
        int clearBtnX = placeBtnX + placeBtnW + 4;
        if (currentBetAmount > 0 && mx >= placeBtnX && mx < placeBtnX + placeBtnW && my >= y - 1 && my < y + 12) {
            placeBet();
            return true;
        }
        // Clear button
        if (currentBetAmount > 0 && mx >= clearBtnX && mx < clearBtnX + clearBtnW && my >= y - 1 && my < y + 12) {
            currentBetAmount = 0;
            return true;
        }

        return super.mouseClicked(event, consumed);
    }

    private boolean clickOutsideBet(int mx, int my, int x, int y, int w, int h, String betType) {
        if (mx >= x && mx < x + w && my >= y && my < y + h) {
            selectedBetType = betType;
            selectedStraightNumber = -1;
            return true;
        }
        return false;
    }

    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();
        if (keyCode == 256) {
            ChipRenderer.clearDrag();
            this.onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    /**
     * Handle chip drop on mouse release. If dragging a chip and releasing over a
     * bet zone (number grid cell or outside bet button), add the chip value to
     * the current bet amount for the hovered zone's bet type.
     */
    public boolean mouseReleased(MouseButtonEvent e) {
        int mx = (int) e.x();
        int my = (int) e.y();
        if (ChipRenderer.isDragging()) {
            // Determine which bet zone the chip was dropped on
            String dropZone = getBetZoneAt(mx, my);
            if (dropZone != null) {
                ChipDenomination dropped = ChipRenderer.completeDrop();
                if (dropped != null) {
                    // If dropping on a different bet type, reset the amount for the new type
                    if (!dropZone.equals(selectedBetType)) {
                        selectedBetType = dropZone;
                        selectedStraightNumber = dropZone.startsWith("straight:") ? Integer.parseInt(dropZone.substring(9)) : -1;
                    }
                    currentBetAmount += dropped.value;
                    return true;
                }
            }
            // Dropped outside any zone — cancel the drag
            ChipRenderer.completeDrop();
            return true;
        }
        return super.mouseReleased(e);
    }

    /**
     * Determine which bet zone (bet type string) the given coordinates fall on,
     * or null if not over any zone. Mirrors the layout logic in render().
     */
    private String getBetZoneAt(int mx, int my) {
        int rx = this.width - PANEL_W - 10;
        int ty = 6;
        int by = ty + 27;
        int y = by + 22;
        y += 4 + 12; // separator + label

        int gridX = rx + 6;

        // Zero button
        if (mx >= gridX && mx < gridX + GRID_CELL_W * 3 && my >= y && my < y + GRID_CELL_H) {
            return "straight:0";
        }
        y += GRID_CELL_H;

        // Street bet markers on left edge
        int streetMarkerW = 4;
        for (int row = 0; row < 12; row++) {
            int streetNum = row * 3 + 1;
            int smx = gridX - streetMarkerW - 1;
            int smy = y + row * GRID_CELL_H;
            if (mx >= smx && mx < smx + streetMarkerW && my >= smy && my < smy + GRID_CELL_H) {
                return "street:" + streetNum;
            }
        }

        // Corner bet markers at intersections
        for (int row = 0; row < 11; row++) {
            for (int col = 0; col < 2; col++) {
                int topLeft = row * 3 + col + 1;
                int dotX = gridX + (col + 1) * GRID_CELL_W - 2;
                int dotY = y + (row + 1) * GRID_CELL_H - 2;
                if (mx >= dotX - 1 && mx < dotX + 5 && my >= dotY - 1 && my < dotY + 5) {
                    return "corner:" + topLeft;
                }
            }
        }

        // Number grid 1-36
        for (int row = 0; row < 12; row++) {
            for (int col = 0; col < 3; col++) {
                int num = row * 3 + col + 1;
                int cx = gridX + col * GRID_CELL_W;
                int cy = y + row * GRID_CELL_H;
                if (mx >= cx && mx < cx + GRID_CELL_W && my >= cy && my < cy + GRID_CELL_H) {
                    if (splitMode) {
                        int other;
                        if (col < 2) {
                            other = num + 1;
                        } else {
                            other = num + 3;
                            if (other > 36) other = num - 1;
                        }
                        return "split:" + Math.min(num, other) + ":" + Math.max(num, other);
                    }
                    return "straight:" + num;
                }
            }
        }
        y += 12 * GRID_CELL_H + 4;

        // Outside bet buttons
        int btnW = (PANEL_W - 18) / 3;
        int btnH = 13;

        // Row 1: Red, Black, Odd
        if (hitTest(mx, my, rx + 6, y, btnW, btnH)) return "red";
        if (hitTest(mx, my, rx + 6 + btnW + 2, y, btnW, btnH)) return "black";
        if (hitTest(mx, my, rx + 6 + (btnW + 2) * 2, y, btnW, btnH)) return "odd";
        y += btnH + 2;

        // Row 2: Even, 1-18, 19-36
        if (hitTest(mx, my, rx + 6, y, btnW, btnH)) return "even";
        if (hitTest(mx, my, rx + 6 + btnW + 2, y, btnW, btnH)) return "low";
        if (hitTest(mx, my, rx + 6 + (btnW + 2) * 2, y, btnW, btnH)) return "high";
        y += btnH + 2;

        // Row 3: Dozens
        if (hitTest(mx, my, rx + 6, y, btnW, btnH)) return "dozen1";
        if (hitTest(mx, my, rx + 6 + btnW + 2, y, btnW, btnH)) return "dozen2";
        if (hitTest(mx, my, rx + 6 + (btnW + 2) * 2, y, btnW, btnH)) return "dozen3";
        y += btnH + 2;

        // Row 4: Column bets
        if (hitTest(mx, my, rx + 6, y, btnW, btnH)) return "column1";
        if (hitTest(mx, my, rx + 6 + btnW + 2, y, btnW, btnH)) return "column2";
        if (hitTest(mx, my, rx + 6 + (btnW + 2) * 2, y, btnW, btnH)) return "column3";

        return null;
    }

    private static boolean hitTest(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        // Check if hovering over the bets list area - allow scrolling bets
        int rx = this.width - PANEL_W - 10;
        if (mouseX >= rx && mouseX <= rx + PANEL_W) {
            if (activeBets.size() > 6) {
                if (scrollY < 0) {
                    betsScrollOffset = Math.min(betsScrollOffset + 1, activeBets.size() - 6);
                } else if (scrollY > 0) {
                    betsScrollOffset = Math.max(betsScrollOffset - 1, 0);
                }
                return true;
            }
        }
        return false;
    }

    private void placeBet() {
        if (currentBetAmount > 0 && !selectedBetType.isEmpty()) {
            ClientPacketDistributor.sendToServer(
                    (CustomPacketPayload) new RouletteActionPayload(selectedBetType, currentBetAmount, tablePos),
                    (CustomPacketPayload[]) new CustomPacketPayload[0]);
            // Track placed bet for chip stack display
            placedBets.merge(selectedBetType, currentBetAmount, Integer::sum);
            currentBetAmount = 0;
        }
    }

    @Override
    public void onClose() {
        ChipRenderer.clearDrag();
        com.ultra.megamod.feature.casino.CasinoClientEvents.onRouletteDismissed();
        RouletteGameSyncPayload.lastSync = null;
        super.onClose();
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

    // ==========================================================================
    // Roulette Wheel Rendering
    // ==========================================================================

    /**
     * Renders the roulette wheel with proper filled circular segments, a ball
     * with glow, decorative hub, and a triangular pointer arrow.
     */
    private void renderRouletteWheel(GuiGraphics g, int cx, int cy, int radius, float renderAngle) {
        int segments = WHEEL_ORDER.length; // 37
        double segAngle = 2 * Math.PI / segments;
        int innerR = Math.max((int)(radius * 0.36), 14);
        int outerR = radius - 2;

        // Outer wood-colored rim (two-tone for depth)
        drawFilledCircle(g, cx, cy, radius + 2, 0xFF4A3828);
        drawFilledCircle(g, cx, cy, radius, 0xFF3A2A1A);

        // Dark background behind segments to catch any sub-pixel gaps
        drawFilledCircle(g, cx, cy, outerR, 0xFF0C0C0C);

        // Draw each colored segment as a filled wedge using triangles.
        // Each segment is split at the midpoint angle into 4 triangles for smooth arcs.
        for (int i = 0; i < segments; i++) {
            int number = WHEEL_ORDER[i];
            int segColor = getNumberBgColor(number);
            if ("RESULT".equals(phase) && number == winningNumber) {
                segColor = 0xFFD4AF37;
            }

            double a1 = renderAngle + i * segAngle - Math.PI / 2;
            double a2 = renderAngle + (i + 1) * segAngle - Math.PI / 2;
            double aMid = renderAngle + (i + 0.5) * segAngle - Math.PI / 2;

            int ix1 = cx + (int)(Math.cos(a1) * innerR);
            int iy1 = cy + (int)(Math.sin(a1) * innerR);
            int ix2 = cx + (int)(Math.cos(a2) * innerR);
            int iy2 = cy + (int)(Math.sin(a2) * innerR);
            int ixM = cx + (int)(Math.cos(aMid) * innerR);
            int iyM = cy + (int)(Math.sin(aMid) * innerR);

            int ox1 = cx + (int)(Math.cos(a1) * outerR);
            int oy1 = cy + (int)(Math.sin(a1) * outerR);
            int ox2 = cx + (int)(Math.cos(a2) * outerR);
            int oy2 = cy + (int)(Math.sin(a2) * outerR);
            int oxM = cx + (int)(Math.cos(aMid) * outerR);
            int oyM = cy + (int)(Math.sin(aMid) * outerR);

            // Two sub-wedges (4 triangles) for smooth arc fill
            fillTriangle(g, ix1, iy1, ox1, oy1, oxM, oyM, segColor);
            fillTriangle(g, ix1, iy1, oxM, oyM, ixM, iyM, segColor);
            fillTriangle(g, ixM, iyM, oxM, oyM, ox2, oy2, segColor);
            fillTriangle(g, ixM, iyM, ox2, oy2, ix2, iy2, segColor);
        }

        // Thin divider lines between segments
        for (int i = 0; i < segments; i++) {
            double a = renderAngle + i * segAngle - Math.PI / 2;
            drawLine(g,
                cx + (int)(Math.cos(a) * (innerR + 1)), cy + (int)(Math.sin(a) * (innerR + 1)),
                cx + (int)(Math.cos(a) * outerR), cy + (int)(Math.sin(a) * outerR),
                0xBB2A2A22);
        }

        // Number labels on each segment (with drop shadow)
        if (radius >= 55) {
            for (int i = 0; i < segments; i++) {
                int number = WHEEL_ORDER[i];
                double aMid = renderAngle + (i + 0.5) * segAngle - Math.PI / 2;
                String numStr = String.valueOf(number);
                int textR = innerR + (outerR - innerR) * 55 / 100;
                int tx = cx + (int)(Math.cos(aMid) * textR) - this.font.width(numStr) / 2;
                int ty = cy + (int)(Math.sin(aMid) * textR) - 4;
                g.drawString(this.font, numStr, tx + 1, ty + 1, 0xAA000000, false);
                g.drawString(this.font, numStr, tx, ty, 0xFFFFFFFF, false);
            }
        }

        // Ball track groove ring
        drawCircleOutline(g, cx, cy, outerR + 1, 0xFF555544);

        // Inner hub — filled circle with decorative gold spokes
        drawFilledCircle(g, cx, cy, innerR, 0xFF181510);
        drawFilledCircle(g, cx, cy, innerR - 2, 0xFF201D15);
        drawCircleOutline(g, cx, cy, innerR, 0xFFD4AF37);
        for (int i = 0; i < 8; i++) {
            double a = i * Math.PI / 4;
            drawLine(g,
                cx + (int)(Math.cos(a) * 3), cy + (int)(Math.sin(a) * 3),
                cx + (int)(Math.cos(a) * (innerR - 3)), cy + (int)(Math.sin(a) * (innerR - 3)),
                0x778B7500);
        }
        // Gold center pip
        g.fill(cx - 2, cy - 2, cx + 3, cy + 3, 0xFFD4AF37);
        g.fill(cx - 1, cy - 1, cx + 2, cy + 2, 0xFFE8C840);

        // Outer golden rim
        drawCircleOutline(g, cx, cy, radius, 0xFFD4AF37);
        drawCircleOutline(g, cx, cy, radius + 1, 0xFFAA8B20);

        // Ball
        if (wheelSpinning || "RESULT".equals(phase)) {
            float ballAngle;
            if ("RESULT".equals(phase) && winningNumber >= 0) {
                int segIdx = getWheelSegmentIndex(winningNumber);
                ballAngle = renderAngle + (float)(segIdx * segAngle + segAngle / 2.0) - (float)(Math.PI / 2);
            } else {
                ballAngle = -renderAngle * 1.3f + spinTicksElapsed * 0.1f;
            }
            int bR = outerR + 1;
            int ballX = cx + (int)(Math.cos(ballAngle) * bR);
            int ballY = cy + (int)(Math.sin(ballAngle) * bR);
            // Soft glow
            g.fill(ballX - 4, ballY - 4, ballX + 5, ballY + 5, 0x22FFFFAA);
            g.fill(ballX - 3, ballY - 3, ballX + 4, ballY + 4, 0x44FFFFCC);
            // Ball body (cross shape for pseudo-circle)
            g.fill(ballX - 2, ballY - 1, ballX + 3, ballY + 2, 0xFFEEEEDD);
            g.fill(ballX - 1, ballY - 2, ballX + 2, ballY + 3, 0xFFEEEEDD);
            // Bright highlight
            g.fill(ballX - 1, ballY - 1, ballX + 1, ballY, 0xFFFFFFFF);
        }

        // Pointer arrow at top (triangular, pointing down at the wheel)
        int ptrX = cx;
        int ptrY = cy - radius - 4;
        fillTriangle(g, ptrX - 7, ptrY - 6, ptrX + 8, ptrY - 6, ptrX, ptrY + 10, 0xFFFFFFFF);
        fillTriangle(g, ptrX - 5, ptrY - 5, ptrX + 6, ptrY - 5, ptrX, ptrY + 8, 0xFFD4AF37);
    }

    /**
     * Fill a triangle defined by three points using horizontal scanline approach.
     * Uses g.fill() with small rectangles to approximate the triangle.
     */
    private static void fillTriangle(GuiGraphics g, int x0, int y0, int x1, int y1, int x2, int y2, int color) {
        // Sort vertices by y coordinate
        int[] xs = {x0, x1, x2};
        int[] ys = {y0, y1, y2};
        // Simple bubble sort by y
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2 - i; j++) {
                if (ys[j] > ys[j + 1]) {
                    int tmp = ys[j]; ys[j] = ys[j + 1]; ys[j + 1] = tmp;
                    tmp = xs[j]; xs[j] = xs[j + 1]; xs[j + 1] = tmp;
                }
            }
        }

        int topY = ys[0], midY = ys[1], botY = ys[2];
        if (topY == botY) return; // degenerate

        // Scanline fill
        for (int sy = topY; sy <= botY; sy++) {
            // Compute x intersections with triangle edges
            float xA, xB;
            if (sy < midY) {
                // Upper half
                float tAC = (botY - topY) != 0 ? (float)(sy - topY) / (botY - topY) : 0;
                float tAB = (midY - topY) != 0 ? (float)(sy - topY) / (midY - topY) : 0;
                xA = xs[0] + tAC * (xs[2] - xs[0]);
                xB = xs[0] + tAB * (xs[1] - xs[0]);
            } else {
                // Lower half
                float tAC = (botY - topY) != 0 ? (float)(sy - topY) / (botY - topY) : 0;
                float tBC = (botY - midY) != 0 ? (float)(sy - midY) / (botY - midY) : 0;
                xA = xs[0] + tAC * (xs[2] - xs[0]);
                xB = xs[1] + tBC * (xs[2] - xs[1]);
            }

            int leftX = (int) Math.min(xA, xB);
            int rightX = (int) Math.max(xA, xB);
            if (rightX >= leftX) {
                g.fill(leftX, sy, rightX + 1, sy + 1, color);
            }
        }
    }

    /**
     * Draws a 1px line between two points using g.fill() with small rectangles.
     */
    private static void drawLine(GuiGraphics g, int x0, int y0, int x1, int y1, int color) {
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int steps = Math.max(dx, dy);
        if (steps == 0) {
            g.fill(x0, y0, x0 + 1, y0 + 1, color);
            return;
        }
        float xInc = (float)(x1 - x0) / steps;
        float yInc = (float)(y1 - y0) / steps;
        float px = x0, py = y0;
        for (int i = 0; i <= steps; i++) {
            g.fill((int) px, (int) py, (int) px + 1, (int) py + 1, color);
            px += xInc;
            py += yInc;
        }
    }

    /** Draws a 1px circle outline using fills along the circumference. */
    private static void drawCircleOutline(GuiGraphics g, int cx, int cy, int radius, int color) {
        int steps = Math.max(72, radius * 4);
        for (int i = 0; i < steps; i++) {
            double a = 2 * Math.PI * i / steps;
            int px = cx + (int)(Math.cos(a) * radius);
            int py = cy + (int)(Math.sin(a) * radius);
            g.fill(px, py, px + 1, py + 1, color);
        }
    }

    /** Draws a filled circle using horizontal scanline fills. */
    private static void drawFilledCircle(GuiGraphics g, int cx, int cy, int radius, int color) {
        for (int dy = -radius; dy <= radius; dy++) {
            int halfW = (int)(Math.sqrt((double) radius * radius - (double) dy * dy));
            g.fill(cx - halfW, cy + dy, cx + halfW + 1, cy + dy + 1, color);
        }
    }

    // ==========================================================================
    // Chip Stack Rendering on Bet Zones
    // ==========================================================================

    /**
     * Renders a small chip stack icon on a bet zone if a bet has been placed there.
     * Uses ChipDenomination.breakdown() to determine chip colors, then renders
     * 2-3 small colored circles stacked with slight vertical offset.
     */
    private void renderChipStackOnZone(GuiGraphics g, String betType, int x, int y) {
        Integer amount = placedBets.get(betType);
        if (amount == null || amount <= 0) return;

        int[] counts = ChipDenomination.breakdown(amount);
        ChipDenomination[] denoms = ChipDenomination.values();

        // Collect the distinct chip colors to show (max 3)
        List<Integer> chipColors = new ArrayList<>();
        for (int i = denoms.length - 1; i >= 0 && chipColors.size() < 3; i--) {
            if (counts[i] > 0) {
                chipColors.add(denoms[i].color);
            }
        }
        if (chipColors.isEmpty()) return;

        // Render small stacked circles (3x3 pixels each, offset by 2 pixels vertically)
        int chipSize = 3;
        for (int i = 0; i < chipColors.size(); i++) {
            int cy = y + (chipColors.size() - 1 - i) * 2; // stack bottom to top
            int chipColor = chipColors.get(i);
            // Small circle approximated as a filled rectangle with darker border
            g.fill(x, cy, x + chipSize, cy + chipSize, chipColor);
            // Tiny border
            g.fill(x, cy, x + chipSize, cy + 1, 0xFF000000);
            g.fill(x, cy + chipSize - 1, x + chipSize, cy + chipSize, 0xFF000000);
        }
    }
}

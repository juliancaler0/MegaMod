package com.ultra.megamod.feature.casino.screen;

import com.ultra.megamod.feature.casino.network.CrapsActionPayload;
import com.ultra.megamod.feature.casino.network.CrapsGameSyncPayload;
import com.ultra.megamod.feature.casino.chips.ChipDenomination;
import com.ultra.megamod.feature.casino.chips.ChipRenderer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.*;

/**
 * Client-side craps GUI. Right-side panel with full bet layout.
 * Shows two dice with bounce/tumble animation, all bet zones, chip stacks on bets.
 */
public class CrapsScreen extends Screen {

    private final BlockPos tablePos;

    // Game state from server sync
    private String phase = "BETTING";
    private int die1 = 0;
    private int die2 = 0;
    private int point = 0;
    private int comePoint = 0;
    private String resultMessage = "";

    /** All active bets keyed by type, synced from server. */
    private final Map<String, Integer> bets = new LinkedHashMap<>();

    // Chip drag-and-drop betting - staged bets not yet sent to server
    private final Map<String, Integer> stagedBets = new LinkedHashMap<>();

    private int chipTrayX, chipTrayY, chipTrayW;

    // Dice roll animation
    private int diceAnimTimer = 0;
    private int animDie1 = 0;
    private int animDie2 = 0;
    private boolean soundCueFlag = false; // set true when animation starts; client can use to trigger sound
    private String pendingResultMessage = ""; // held until dice animation finishes
    private String pendingPhase = ""; // phase to apply after animation

    // Layout
    private static final int PANEL_W = 220;
    private static final int BG = 0xAA000000;
    private static final int BORDER = 0xFFD4AF37;
    private static final int TEXT = 0xFFCCCCDD;
    private static final int GOLD = 0xFFD4AF37;
    private static final int DIM = 0xFF888899;

    // Dice rendering
    private static final int DIE_SIZE = 36;
    private static final int DIE_GAP = 10;
    private static final int DOT_RADIUS = 3;
    private static final int DIE_BG = 0xFFEEEEEE;
    private static final int DIE_BORDER = 0xFF333333;
    private static final int DOT_COLOR = 0xFF111111;

    // Dice animation constants
    private static final int ANIM_TOTAL_TICKS = 18; // ~0.9 seconds
    private static final int ANIM_SETTLE_TICKS = 3;  // last 3 ticks = settling

    // Bet zone colors
    private static final int ZONE_PASS_BORDER = 0xFF00CC00;    // green
    private static final int ZONE_DONTPASS_BORDER = 0xFFFF3333; // red
    private static final int ZONE_FIELD_BORDER = 0xFFDDCC33;    // yellow
    private static final int ZONE_COME_BORDER = 0xFF3399FF;     // blue
    private static final int ZONE_PLACE_BORDER = 0xFFCC88FF;    // purple
    private static final int ZONE_HARD_BORDER = 0xFFFF8844;     // orange

    // Bet zone layout tracking (computed during render, used for hit-testing)
    private final List<BetZone> betZones = new ArrayList<>();

    // Button positions (set during render, used for click detection)
    private int rollBtnX, rollBtnY, rollBtnW, rollBtnH;
    private int clearBtnX, clearBtnY, clearBtnW, clearBtnH;
    private int confirmBtnX, confirmBtnY, confirmBtnW, confirmBtnH;
    private int placeBetsBtnX, placeBetsBtnY, placeBetsBtnW, placeBetsBtnH;
    private boolean placeBetsBtnVisible = false;

    private record BetZone(String betType, int x, int y, int w, int h, int borderColor, String label, String payout) {}

    public CrapsScreen(BlockPos tablePos) {
        super(Component.literal("Craps"));
        this.tablePos = tablePos;
    }

    @Override
    public void onClose() {
        ChipRenderer.clearDrag();
        com.ultra.megamod.feature.casino.CasinoClientEvents.onCrapsDismissed();
        super.onClose();
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Transparent - world visible behind
    }

    @Override
    public void tick() {
        super.tick();

        if (diceAnimTimer > 0) {
            diceAnimTimer--;
            int ticksElapsed = ANIM_TOTAL_TICKS - diceAnimTimer;
            boolean settling = diceAnimTimer <= ANIM_SETTLE_TICKS;

            if (settling) {
                // Settling phase: slow down, show near-final values more often
                if (diceAnimTimer == ANIM_SETTLE_TICKS) {
                    // First settling tick: start showing real values sometimes
                    animDie1 = Math.random() < 0.5 ? die1 : 1 + (int) (Math.random() * 6);
                    animDie2 = Math.random() < 0.5 ? die2 : 1 + (int) (Math.random() * 6);
                } else if (diceAnimTimer == 0) {
                    // Done: snap to real result and reveal pending message
                    animDie1 = die1;
                    animDie2 = die2;
                    if (!pendingResultMessage.isEmpty()) {
                        this.resultMessage = pendingResultMessage;
                        pendingResultMessage = "";
                    }
                    if (!pendingPhase.isEmpty()) {
                        this.phase = pendingPhase;
                        pendingPhase = "";
                    }
                } else {
                    // Settling: mostly show real values
                    animDie1 = Math.random() < 0.7 ? die1 : 1 + (int) (Math.random() * 6);
                    animDie2 = Math.random() < 0.7 ? die2 : 1 + (int) (Math.random() * 6);
                }
            } else {
                // Full tumble: random faces
                animDie1 = 1 + (int) (Math.random() * 6);
                animDie2 = 1 + (int) (Math.random() * 6);
            }
        }

        CrapsGameSyncPayload sync = CrapsGameSyncPayload.lastSync;
        if (sync != null) {
            CrapsGameSyncPayload.lastSync = null;
            parseSync(sync.gameStateJson());
        }
    }

    private void parseSync(String json) {
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            String newPhase = root.has("phase") ? root.get("phase").getAsString() : "BETTING";

            int newDie1 = root.has("die1") ? root.get("die1").getAsInt() : 0;
            int newDie2 = root.has("die2") ? root.get("die2").getAsInt() : 0;

            String newResultMsg = root.has("resultMessage") ? root.get("resultMessage").getAsString() : "";

            // Trigger dice animation if dice changed and are non-zero
            if ((newDie1 != die1 || newDie2 != die2) && newDie1 > 0 && newDie2 > 0) {
                diceAnimTimer = ANIM_TOTAL_TICKS;
                soundCueFlag = true;
                // Defer result message and phase until animation finishes
                this.pendingResultMessage = newResultMsg;
                this.pendingPhase = newPhase;
                this.die1 = newDie1;
                this.die2 = newDie2;
                this.resultMessage = ""; // hide until dice stop
            } else {
                this.phase = newPhase;
                this.die1 = newDie1;
                this.die2 = newDie2;
                this.resultMessage = newResultMsg;
            }

            this.point = root.has("point") ? root.get("point").getAsInt() : 0;
            this.comePoint = root.has("comePoint") ? root.get("comePoint").getAsInt() : 0;

            // Parse all bets
            bets.clear();
            if (root.has("bets") && root.get("bets").isJsonObject()) {
                JsonObject betsJson = root.getAsJsonObject("bets");
                for (Map.Entry<String, JsonElement> entry : betsJson.entrySet()) {
                    bets.put(entry.getKey(), entry.getValue().getAsInt());
                }
            }

            // Clear staged bets when phase changes or round ends
            if ("BETTING".equals(newPhase) || "RESULT".equals(newPhase)) {
                stagedBets.clear();
            }
        } catch (Exception ignored) {}
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        betZones.clear();
        int rx = this.width - PANEL_W - 10;
        int ty = 10;
        int ph = Math.min(this.height - 20, 480);

        // Panel background with gold border
        g.fill(rx, ty, rx + PANEL_W, ty + ph, BG);
        g.fill(rx, ty, rx + PANEL_W, ty + 1, BORDER);
        g.fill(rx, ty + ph - 1, rx + PANEL_W, ty + ph, BORDER);
        g.fill(rx, ty, rx + 1, ty + ph, BORDER);
        g.fill(rx + PANEL_W - 1, ty, rx + PANEL_W, ty + ph, BORDER);

        // Title
        g.drawString(this.font, "CRAPS", rx + 6, ty + 4, GOLD, false);

        // Phase display
        int y = ty + 16;
        String phaseStr = switch (phase) {
            case "BETTING" -> "Place your bets!";
            case "COME_OUT_ROLL" -> "Come Out Roll";
            case "POINT_PHASE" -> "Point Phase - Point: " + point;
            case "RESULT" -> "Result";
            default -> phase;
        };
        g.drawString(this.font, phaseStr, rx + 6, y, TEXT, false);
        y += 14;

        // Wallet balance
        int wallet = ChipRenderer.clientChipTotal;
        String walletStr = "Chips: " + wallet + " MC";
        int walletW = this.font.width(walletStr);
        g.drawString(this.font, walletStr, rx + PANEL_W - walletW - 6, ty + 4, GOLD, false);

        // Separator
        g.fill(rx + 4, y, rx + PANEL_W - 4, y + 1, BORDER);
        y += 6;

        // Dice display area with animation
        y = renderDiceArea(g, rx, y);

        // Point indicator
        if (point > 0 && !"RESULT".equals(phase)) {
            String pointStr = "POINT: " + point;
            int pointW = this.font.width(pointStr);
            int pointBoxX = rx + (PANEL_W - pointW) / 2 - 6;
            g.fill(pointBoxX, y - 2, pointBoxX + pointW + 12, y + 12, 0xFF1A237E);
            drawRectOutline(g, pointBoxX, y - 2, pointBoxX + pointW + 12, y + 12, GOLD);
            g.drawString(this.font, pointStr, pointBoxX + 6, y, 0xFF00CCFF, false);
            y += 18;
        }

        // Come point indicator
        if (comePoint > 0 && "POINT_PHASE".equals(phase)) {
            String cpStr = "Come Point: " + comePoint;
            g.drawString(this.font, cpStr, rx + 6, y, 0xFF3399FF, false);
            y += 12;
        }

        // Result message
        if (!resultMessage.isEmpty()) {
            boolean isWin = resultMessage.contains("win");
            int msgColor = isWin ? 0xFF00FF00 : 0xFFFF4444;
            // Word-wrap long messages
            List<String> lines = wrapText(resultMessage, PANEL_W - 12);
            for (String line : lines) {
                g.drawString(this.font, line, rx + 6, y, msgColor, false);
                y += 10;
            }
            y += 2;
        }

        // Separator
        g.fill(rx + 4, y, rx + PANEL_W - 4, y + 1, BORDER);
        y += 6;

        // Bet zones
        y = renderBetZones(g, mouseX, mouseY, rx, y);

        // Separator
        g.fill(rx + 4, y, rx + PANEL_W - 4, y + 1, BORDER);
        y += 4;

        // Controls based on phase
        if ("BETTING".equals(phase)) {
            y = renderBettingControls(g, mouseX, mouseY, rx, y);
        } else if ("COME_OUT_ROLL".equals(phase) || "POINT_PHASE".equals(phase)) {
            y = renderRollControls(g, mouseX, mouseY, rx, y);
        } else if ("RESULT".equals(phase)) {
            String waitStr = "Next round starting...";
            int waitW = this.font.width(waitStr);
            g.drawString(this.font, waitStr, rx + (PANEL_W - waitW) / 2, y, DIM, false);
            y += 14;
        }

        // Footer hint
        g.drawString(this.font, "ESC close", rx + 6, ty + ph - 12, DIM, false);

        super.render(g, mouseX, mouseY, partialTick);

        // Dragged chip always on top of everything
        ChipRenderer.renderDraggedChip(g, this.font, mouseX, mouseY);
    }

    // ---- Dice area with bounce/tumble animation ----

    private int renderDiceArea(GuiGraphics g, int rx, int y) {
        int d1 = (diceAnimTimer > 0) ? animDie1 : die1;
        int d2 = (diceAnimTimer > 0) ? animDie2 : die2;

        // Calculate bounce scale during animation
        float scale = 1.0f;
        int tumbleOffset = 0;
        if (diceAnimTimer > 0) {
            int elapsed = ANIM_TOTAL_TICKS - diceAnimTimer;
            boolean settling = diceAnimTimer <= ANIM_SETTLE_TICKS;

            if (!settling) {
                // Bounce: oscillate scale between 0.8 and 1.2
                double bouncePhase = elapsed * 0.6;
                scale = 1.0f + 0.2f * (float) Math.sin(bouncePhase);

                // Tumble: offset pip positions slightly (simulate via position jitter)
                tumbleOffset = (int) (Math.sin(elapsed * 1.3) * 2);
            } else {
                // Settling: dampen
                float dampFactor = (float) diceAnimTimer / ANIM_SETTLE_TICKS;
                scale = 1.0f + 0.05f * dampFactor;
                tumbleOffset = (int) (dampFactor * 1);
            }
        }

        int scaledSize = (int) (DIE_SIZE * scale);
        int diceAreaX = rx + (PANEL_W - (scaledSize * 2 + DIE_GAP)) / 2;
        int diceAreaY = y;

        if (d1 > 0 && d2 > 0) {
            renderDie(g, diceAreaX + tumbleOffset, diceAreaY - Math.abs(tumbleOffset), scaledSize, d1);
            renderDie(g, diceAreaX + scaledSize + DIE_GAP - tumbleOffset, diceAreaY + Math.abs(tumbleOffset), scaledSize, d2);

            // Total below dice
            if (diceAnimTimer <= 0) {
                int total = die1 + die2;
                String totalStr = "Total: " + total;
                int totalW = this.font.width(totalStr);
                g.drawString(this.font, totalStr, rx + (PANEL_W - totalW) / 2,
                        diceAreaY + DIE_SIZE + 4, TEXT, false);
            }
        } else {
            // No dice rolled yet - show empty placeholders
            renderDiePlaceholder(g, diceAreaX, diceAreaY, DIE_SIZE);
            renderDiePlaceholder(g, diceAreaX + DIE_SIZE + DIE_GAP, diceAreaY, DIE_SIZE);
        }

        return diceAreaY + DIE_SIZE + 18;
    }

    // ---- Bet Zones ----

    private int renderBetZones(GuiGraphics g, int mouseX, int mouseY, int rx, int y) {
        int zoneW = PANEL_W - 16;
        int zoneH = 22;
        int zoneX = rx + 8;
        boolean isPointPhase = "POINT_PHASE".equals(phase);
        boolean isBetting = "BETTING".equals(phase);
        boolean canBet = isBetting || "COME_OUT_ROLL".equals(phase) || isPointPhase;

        // --- PASS LINE ---
        y = renderBetZone(g, mouseX, mouseY, zoneX, y, zoneW, zoneH,
                "pass", "PASS LINE", "1:1", ZONE_PASS_BORDER, true, canBet);

        // --- DON'T PASS ---
        y = renderBetZone(g, mouseX, mouseY, zoneX, y, zoneW, zoneH,
                "dontpass", "DON'T PASS", "1:1", ZONE_DONTPASS_BORDER, true, canBet);

        // --- FIELD ---
        y = renderBetZone(g, mouseX, mouseY, zoneX, y, zoneW, zoneH,
                "field", "FIELD", "1:1 / 2x / 3x", ZONE_FIELD_BORDER, true, canBet);

        // --- COME (only during POINT_PHASE) ---
        if (isPointPhase) {
            y = renderBetZone(g, mouseX, mouseY, zoneX, y, zoneW, zoneH,
                    "come", "COME", "1:1", ZONE_COME_BORDER, true, true);
        }

        // --- PLACE BETS row (only during POINT_PHASE) ---
        if (isPointPhase) {
            g.drawString(this.font, "PLACE BETS", zoneX, y, DIM, false);
            y += 10;
            int[] placeNums = {4, 5, 6, 8, 9, 10};
            int smallW = (zoneW - (placeNums.length - 1) * 2) / placeNums.length;
            int sx = zoneX;
            for (int num : placeNums) {
                String key = "place_" + num;
                String payout = switch (num) {
                    case 4, 10 -> "9:5";
                    case 5, 9 -> "7:5";
                    case 6, 8 -> "7:6";
                    default -> "";
                };
                renderSmallBetZone(g, mouseX, mouseY, sx, y, smallW, zoneH,
                        key, String.valueOf(num), payout, ZONE_PLACE_BORDER, true);
                sx += smallW + 2;
            }
            y += zoneH + 4;
        }

        // --- HARDWAYS row (only during POINT_PHASE) ---
        if (isPointPhase) {
            g.drawString(this.font, "HARDWAYS", zoneX, y, DIM, false);
            y += 10;
            int[] hardNums = {4, 6, 8, 10};
            int smallW = (zoneW - (hardNums.length - 1) * 2) / hardNums.length;
            int sx = zoneX;
            for (int num : hardNums) {
                String key = "hard_" + num;
                String payout = (num == 4 || num == 10) ? "7:1" : "9:1";
                renderSmallBetZone(g, mouseX, mouseY, sx, y, smallW, zoneH,
                        key, "H" + num, payout, ZONE_HARD_BORDER, true);
                sx += smallW + 2;
            }
            y += zoneH + 4;
        }

        // --- PROPOSITION BETS (one-roll, always available) ---
        g.drawString(this.font, "PROPOSITIONS", zoneX, y, DIM, false);
        y += 10;
        // Row 1: Any 7, Any Craps, Yo 11
        int propW = (zoneW - 4) / 3;
        renderSmallBetZone(g, mouseX, mouseY, zoneX, y, propW, zoneH,
                "any_seven", "Any 7", "4:1", 0xFFDD8844, true);
        renderSmallBetZone(g, mouseX, mouseY, zoneX + propW + 2, y, propW, zoneH,
                "any_craps", "Craps", "7:1", 0xFFCC4444, true);
        renderSmallBetZone(g, mouseX, mouseY, zoneX + (propW + 2) * 2, y, propW, zoneH,
                "yo_eleven", "Yo 11", "15:1", 0xFF44CC44, true);
        y += zoneH + 2;

        // Row 2: Aces, Boxcars, Horn, Hi-Lo
        int prop2W = (zoneW - 6) / 4;
        renderSmallBetZone(g, mouseX, mouseY, zoneX, y, prop2W, zoneH,
                "aces", "Aces", "30:1", 0xFFFFFFFF, true);
        renderSmallBetZone(g, mouseX, mouseY, zoneX + prop2W + 2, y, prop2W, zoneH,
                "boxcars", "12s", "30:1", 0xFFFFFFFF, true);
        renderSmallBetZone(g, mouseX, mouseY, zoneX + (prop2W + 2) * 2, y, prop2W, zoneH,
                "horn", "Horn", "3-7:1", 0xFFBB88DD, true);
        renderSmallBetZone(g, mouseX, mouseY, zoneX + (prop2W + 2) * 3, y, prop2W, zoneH,
                "hi_lo", "Hi-Lo", "15:1", 0xFF88BBDD, true);
        y += zoneH + 4;

        return y;
    }

    private int renderBetZone(GuiGraphics g, int mouseX, int mouseY,
                              int zx, int zy, int zw, int zh,
                              String betType, String label, String payout,
                              int borderColor, boolean visible, boolean canDrop) {
        if (!visible) return zy;

        boolean overZone = mouseX >= zx && mouseX < zx + zw
                && mouseY >= zy && mouseY < zy + zh;
        boolean draggingOver = ChipRenderer.isDragging() && overZone && canDrop;

        // Background
        int bgColor = draggingOver ? 0x6600FF00 : 0x33FFFFFF;
        g.fill(zx, zy, zx + zw, zy + zh, bgColor);

        // Border
        int bc = draggingOver ? 0xFF00FF00 : borderColor;
        drawRectOutline(g, zx, zy, zx + zw, zy + zh, bc);

        // Bet amount (server confirmed + staged)
        int serverBet = bets.getOrDefault(betType, 0);
        int stagedBet = stagedBets.getOrDefault(betType, 0);
        int totalBet = serverBet + stagedBet;

        // Label
        g.drawString(this.font, label, zx + 4, zy + (zh - 9) / 2,
                draggingOver ? 0xFF00FF00 : TEXT, false);

        // Payout text (dim, right-aligned)
        int payoutW = this.font.width(payout);
        g.drawString(this.font, payout, zx + zw - payoutW - 4, zy + (zh - 9) / 2, DIM, false);

        // Chip stack on bet zone if amount > 0
        if (totalBet > 0) {
            renderChipStack(g, zx + zw - 40, zy + 2, totalBet, zh - 4);
            // Bet amount text
            String betStr = totalBet + "";
            int betW = this.font.width(betStr);
            g.drawString(this.font, betStr, zx + zw - 42 - betW, zy + (zh - 9) / 2, GOLD, false);
        }

        // Register for hit-testing
        betZones.add(new BetZone(betType, zx, zy, zw, zh, borderColor, label, payout));

        return zy + zh + 3;
    }

    private void renderSmallBetZone(GuiGraphics g, int mouseX, int mouseY,
                                    int zx, int zy, int zw, int zh,
                                    String betType, String label, String payout,
                                    int borderColor, boolean canDrop) {
        boolean overZone = mouseX >= zx && mouseX < zx + zw
                && mouseY >= zy && mouseY < zy + zh;
        boolean draggingOver = ChipRenderer.isDragging() && overZone && canDrop;

        // Background
        int bgColor = draggingOver ? 0x6600FF00 : 0x33FFFFFF;
        g.fill(zx, zy, zx + zw, zy + zh, bgColor);

        // Border
        int bc = draggingOver ? 0xFF00FF00 : borderColor;
        drawRectOutline(g, zx, zy, zx + zw, zy + zh, bc);

        // Label centered
        int labelW = this.font.width(label);
        g.drawString(this.font, label, zx + (zw - labelW) / 2, zy + 2,
                draggingOver ? 0xFF00FF00 : TEXT, false);

        // Payout text small below label
        int payoutW = this.font.width(payout);
        g.drawString(this.font, payout, zx + (zw - payoutW) / 2, zy + 12, DIM, false);

        // Bet amount on zone
        int serverBet = bets.getOrDefault(betType, 0);
        int stagedBet = stagedBets.getOrDefault(betType, 0);
        int totalBet = serverBet + stagedBet;
        if (totalBet > 0) {
            renderChipStack(g, zx + zw - 12, zy + 2, totalBet, zh - 4);
        }

        // Register for hit-testing
        betZones.add(new BetZone(betType, zx, zy, zw, zh, borderColor, label, payout));
    }

    // ---- Chip Stack Rendering ----

    private void renderChipStack(GuiGraphics g, int x, int y, int amount, int maxHeight) {
        int[] counts = ChipDenomination.breakdown(amount);
        ChipDenomination[] denoms = ChipDenomination.values();

        int chipW = 8;
        int chipH = 3;
        int stackY = y + maxHeight; // bottom of area
        int stackCount = 0;

        // Render from lowest denom to highest (bottom to top)
        for (int i = 0; i < denoms.length && stackCount < 8; i++) {
            for (int j = 0; j < counts[i] && stackCount < 8; j++) {
                int cy = stackY - (stackCount + 1) * chipH;
                if (cy < y) break;

                // Chip with offset for 3D pile effect
                int offsetX = (stackCount % 2 == 0) ? 0 : 1;
                g.fill(x + offsetX, cy, x + offsetX + chipW, cy + chipH - 1, denoms[i].borderColor);
                g.fill(x + offsetX + 1, cy, x + offsetX + chipW - 1, cy + chipH - 1, denoms[i].color);
                stackCount++;
            }
        }
    }

    // ---- Betting Controls ----

    private int renderBettingControls(GuiGraphics g, int mouseX, int mouseY, int rx, int y) {
        // Summary of staged bets
        int totalStaged = stagedBets.values().stream().mapToInt(Integer::intValue).sum();
        if (totalStaged > 0) {
            String stagedStr = "Staged: " + totalStaged + " MC";
            g.drawString(this.font, stagedStr, rx + 6, y, GOLD, false);
            y += 12;
        }

        // Button row: Clear and Confirm
        this.clearBtnW = 52;
        this.confirmBtnW = 100;
        this.clearBtnH = 16;
        this.confirmBtnH = 16;
        this.clearBtnX = rx + 8;
        this.clearBtnY = y;
        this.confirmBtnX = rx + PANEL_W - confirmBtnW - 8;
        this.confirmBtnY = y;

        // Clear button
        boolean clearHover = mouseX >= clearBtnX && mouseX < clearBtnX + clearBtnW
                && mouseY >= clearBtnY && mouseY < clearBtnY + clearBtnH;
        g.fill(clearBtnX, clearBtnY, clearBtnX + clearBtnW, clearBtnY + clearBtnH, clearHover ? 0xFFEF5350 : 0xFFC62828);
        String clearStr = "CLEAR";
        int clearStrW = this.font.width(clearStr);
        g.drawString(this.font, clearStr, clearBtnX + (clearBtnW - clearStrW) / 2, clearBtnY + (clearBtnH - 9) / 2, 0xFFFFFFFF, false);

        // Confirm Bet & Roll button
        boolean canConfirm = totalStaged > 0;
        boolean confirmHover = canConfirm && mouseX >= confirmBtnX && mouseX < confirmBtnX + confirmBtnW
                && mouseY >= confirmBtnY && mouseY < confirmBtnY + confirmBtnH;
        int confirmBg = canConfirm ? (confirmHover ? 0xFF4CAF50 : 0xFF2E7D32) : 0xFF333333;
        g.fill(confirmBtnX, confirmBtnY, confirmBtnX + confirmBtnW, confirmBtnY + confirmBtnH, confirmBg);
        String confirmStr = "CONFIRM & ROLL";
        int confirmStrW = this.font.width(confirmStr);
        g.drawString(this.font, confirmStr, confirmBtnX + (confirmBtnW - confirmStrW) / 2, confirmBtnY + (confirmBtnH - 9) / 2,
                canConfirm ? 0xFFFFFFFF : 0xFF666666, false);
        y += clearBtnH + 6;

        // Chip tray
        this.chipTrayW = PANEL_W - 8;
        this.chipTrayX = rx + 4;
        this.chipTrayY = y;
        ChipRenderer.renderChipTray(g, this.font, this.chipTrayX, this.chipTrayY, this.chipTrayW, mouseX, mouseY);

        // Hint
        String hint = "Drag chips onto bet zones";
        int hintW = this.font.width(hint);
        g.drawString(this.font, hint, rx + (PANEL_W - hintW) / 2, this.chipTrayY - 8, DIM, false);

        return y;
    }

    // ---- Roll Controls (during COME_OUT_ROLL and POINT_PHASE) ----

    private int renderRollControls(GuiGraphics g, int mouseX, int mouseY, int rx, int y) {
        boolean isPointPhase = "POINT_PHASE".equals(phase);
        placeBetsBtnVisible = false;

        // During point phase, show staged bets and Place Bets button
        if (isPointPhase) {
            int totalStaged = stagedBets.values().stream().mapToInt(Integer::intValue).sum();
            if (totalStaged > 0) {
                String stagedStr = "New bets: " + totalStaged + " MC";
                g.drawString(this.font, stagedStr, rx + 6, y, GOLD, false);
                y += 12;

                // Place Bets button
                this.placeBetsBtnW = 80;
                this.placeBetsBtnH = 14;
                this.placeBetsBtnX = rx + 6;
                this.placeBetsBtnY = y;
                this.placeBetsBtnVisible = true;
                boolean placeHover = mouseX >= placeBetsBtnX && mouseX < placeBetsBtnX + placeBetsBtnW
                        && mouseY >= placeBetsBtnY && mouseY < placeBetsBtnY + placeBetsBtnH;
                g.fill(placeBetsBtnX, placeBetsBtnY, placeBetsBtnX + placeBetsBtnW, placeBetsBtnY + placeBetsBtnH,
                        placeHover ? 0xFF4CAF50 : 0xFF2E7D32);
                String placeStr = "PLACE BETS";
                int placeStrW = this.font.width(placeStr);
                g.drawString(this.font, placeStr, placeBetsBtnX + (placeBetsBtnW - placeStrW) / 2,
                        placeBetsBtnY + (placeBetsBtnH - 9) / 2, 0xFFFFFFFF, false);
                y += placeBetsBtnH + 4;
            }
        }

        // Roll button
        this.rollBtnW = 80;
        this.rollBtnH = 20;
        this.rollBtnX = rx + (PANEL_W - rollBtnW) / 2;
        this.rollBtnY = y;

        boolean hover = mouseX >= rollBtnX && mouseX < rollBtnX + rollBtnW
                && mouseY >= rollBtnY && mouseY < rollBtnY + rollBtnH;
        boolean canRoll = diceAnimTimer <= 0;

        int bgColor = canRoll ? (hover ? 0xFFE6A800 : 0xFFCC8800) : 0xFF555555;
        g.fill(rollBtnX, rollBtnY, rollBtnX + rollBtnW, rollBtnY + rollBtnH, bgColor);
        drawRectOutline(g, rollBtnX, rollBtnY, rollBtnX + rollBtnW, rollBtnY + rollBtnH, GOLD);

        String rollLabel = "ROLL DICE";
        int labelW = this.font.width(rollLabel);
        g.drawString(this.font, rollLabel, rollBtnX + (rollBtnW - labelW) / 2,
                rollBtnY + (rollBtnH - 9) / 2, 0xFFFFFFFF, false);
        y += rollBtnH + 6;

        // Info text
        if ("COME_OUT_ROLL".equals(phase)) {
            g.drawString(this.font, "7 or 11 = Win", rx + 6, y, 0xFF00CC00, false);
            y += 10;
            g.drawString(this.font, "2, 3, or 12 = Lose", rx + 6, y, 0xFFFF4444, false);
            y += 10;
            g.drawString(this.font, "Other = Point set", rx + 6, y, DIM, false);
            y += 10;
        } else if ("POINT_PHASE".equals(phase)) {
            g.drawString(this.font, "Hit " + point + " = Win", rx + 6, y, 0xFF00CC00, false);
            y += 10;
            g.drawString(this.font, "Roll 7 = Lose", rx + 6, y, 0xFFFF4444, false);
            y += 10;
        }

        // Chip tray during point phase for additional bets
        if (isPointPhase) {
            y += 2;
            this.chipTrayW = PANEL_W - 8;
            this.chipTrayX = rx + 4;
            this.chipTrayY = y;
            ChipRenderer.renderChipTray(g, this.font, this.chipTrayX, this.chipTrayY, this.chipTrayW, mouseX, mouseY);
        }

        return y;
    }

    // ---- Dice rendering with dots (supports variable size) ----

    private void renderDie(GuiGraphics g, int x, int y, int size, int value) {
        // Die background
        g.fill(x, y, x + size, y + size, DIE_BORDER);
        g.fill(x + 1, y + 1, x + size - 1, y + size - 1, DIE_BG);
        g.fill(x + 2, y + 2, x + size - 2, y + size - 2, DIE_BG);

        // Draw dots based on value
        int cx = x + size / 2;
        int cy = y + size / 2;
        int offset = size / 4;
        int dotR = Math.max(2, size / 12);

        switch (value) {
            case 1 -> drawDot(g, cx, cy, dotR);
            case 2 -> {
                drawDot(g, cx - offset, cy - offset, dotR);
                drawDot(g, cx + offset, cy + offset, dotR);
            }
            case 3 -> {
                drawDot(g, cx - offset, cy - offset, dotR);
                drawDot(g, cx, cy, dotR);
                drawDot(g, cx + offset, cy + offset, dotR);
            }
            case 4 -> {
                drawDot(g, cx - offset, cy - offset, dotR);
                drawDot(g, cx + offset, cy - offset, dotR);
                drawDot(g, cx - offset, cy + offset, dotR);
                drawDot(g, cx + offset, cy + offset, dotR);
            }
            case 5 -> {
                drawDot(g, cx - offset, cy - offset, dotR);
                drawDot(g, cx + offset, cy - offset, dotR);
                drawDot(g, cx, cy, dotR);
                drawDot(g, cx - offset, cy + offset, dotR);
                drawDot(g, cx + offset, cy + offset, dotR);
            }
            case 6 -> {
                drawDot(g, cx - offset, cy - offset, dotR);
                drawDot(g, cx + offset, cy - offset, dotR);
                drawDot(g, cx - offset, cy, dotR);
                drawDot(g, cx + offset, cy, dotR);
                drawDot(g, cx - offset, cy + offset, dotR);
                drawDot(g, cx + offset, cy + offset, dotR);
            }
        }
    }

    private void drawDot(GuiGraphics g, int cx, int cy, int radius) {
        for (int dy = -radius; dy <= radius; dy++) {
            int halfWidth = (int) Math.round(Math.sqrt(radius * radius - dy * dy));
            g.fill(cx - halfWidth, cy + dy, cx + halfWidth + 1, cy + dy + 1, DOT_COLOR);
        }
    }

    private void renderDiePlaceholder(GuiGraphics g, int x, int y, int size) {
        g.fill(x, y, x + size, y + size, DIE_BORDER);
        g.fill(x + 1, y + 1, x + size - 1, y + size - 1, 0xFF555555);
        String q = "?";
        int qw = this.font.width(q);
        g.drawString(this.font, q, x + (size - qw) / 2, y + (size - 9) / 2, DIM, false);
    }

    private void drawRectOutline(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        g.fill(x1, y1, x2, y1 + 1, color);
        g.fill(x1, y2 - 1, x2, y2, color);
        g.fill(x1, y1, x1 + 1, y2, color);
        g.fill(x2 - 1, y1, x2, y2, color);
    }

    private List<String> wrapText(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        for (String word : words) {
            if (line.length() > 0 && this.font.width(line + " " + word) > maxWidth) {
                lines.add(line.toString());
                line = new StringBuilder(word);
            } else {
                if (line.length() > 0) line.append(" ");
                line.append(word);
            }
        }
        if (line.length() > 0) lines.add(line.toString());
        return lines;
    }

    // ---- Input handling ----

    public boolean mouseClicked(MouseButtonEvent event, boolean consumed) {
        if (consumed) return super.mouseClicked(event, consumed);
        int mx = (int) event.x();
        int my = (int) event.y();

        if ("BETTING".equals(phase)) {
            // Clear button
            if (mx >= clearBtnX && mx < clearBtnX + clearBtnW
                    && my >= clearBtnY && my < clearBtnY + clearBtnH) {
                stagedBets.clear();
                return true;
            }

            // Confirm button
            int totalStaged = stagedBets.values().stream().mapToInt(Integer::intValue).sum();
            if (totalStaged > 0
                    && mx >= confirmBtnX && mx < confirmBtnX + confirmBtnW
                    && my >= confirmBtnY && my < confirmBtnY + confirmBtnH) {
                // Send all staged bets to server
                for (Map.Entry<String, Integer> entry : stagedBets.entrySet()) {
                    sendAction("bet:" + entry.getKey(), entry.getValue());
                }
                stagedBets.clear();
                return true;
            }

            // Chip tray click - start dragging
            if (ChipRenderer.handleTrayClick(mx, my, this.chipTrayX, this.chipTrayY, this.chipTrayW)) {
                return true;
            }

        } else if ("COME_OUT_ROLL".equals(phase) || "POINT_PHASE".equals(phase)) {
            // Place Bets button during point phase
            if (placeBetsBtnVisible
                    && mx >= placeBetsBtnX && mx < placeBetsBtnX + placeBetsBtnW
                    && my >= placeBetsBtnY && my < placeBetsBtnY + placeBetsBtnH) {
                // Send all staged bets to server
                for (Map.Entry<String, Integer> entry : stagedBets.entrySet()) {
                    sendAction("bet:" + entry.getKey(), entry.getValue());
                }
                stagedBets.clear();
                return true;
            }

            // Roll button
            if (diceAnimTimer <= 0
                    && mx >= rollBtnX && mx < rollBtnX + rollBtnW
                    && my >= rollBtnY && my < rollBtnY + rollBtnH) {
                sendAction("roll", 0);
                return true;
            }

            // Chip tray during point phase
            if ("POINT_PHASE".equals(phase)) {
                if (ChipRenderer.handleTrayClick(mx, my, this.chipTrayX, this.chipTrayY, this.chipTrayW)) {
                    return true;
                }
            }
        }

        return super.mouseClicked(event, consumed);
    }

    public boolean mouseReleased(MouseButtonEvent e) {
        int mx = (int) e.x();
        int my = (int) e.y();
        if (ChipRenderer.isDragging()) {
            ChipDenomination dropped = ChipRenderer.completeDrop();
            if (dropped == null) return true;

            // Check which bet zone the chip was dropped on
            for (BetZone zone : betZones) {
                if (mx >= zone.x && mx < zone.x + zone.w
                        && my >= zone.y && my < zone.y + zone.h) {

                    // Validate bet type for current phase
                    if (canPlaceBetType(zone.betType)) {
                        if ("BETTING".equals(phase)) {
                            // Stage the bet (not yet sent to server)
                            stagedBets.merge(zone.betType, dropped.value, Integer::sum);
                        } else {
                            // During rolling phases, send immediately
                            sendAction("bet:" + zone.betType, dropped.value);
                        }
                    }
                    return true;
                }
            }
            // Dropped outside any zone - chip is lost back to tray (no action)
            return true;
        }
        return super.mouseReleased(e);
    }

    private static final Set<String> PROP_BET_TYPES = Set.of(
            "any_seven", "any_craps", "yo_eleven", "aces", "boxcars", "horn", "hi_lo", "field");

    private boolean canPlaceBetType(String betType) {
        if ("BETTING".equals(phase)) {
            return "pass".equals(betType) || "dontpass".equals(betType) || PROP_BET_TYPES.contains(betType);
        }
        if ("COME_OUT_ROLL".equals(phase)) {
            return PROP_BET_TYPES.contains(betType);
        }
        if ("POINT_PHASE".equals(phase)) {
            return "come".equals(betType) || "field".equals(betType)
                    || betType.startsWith("place_") || betType.startsWith("hard_")
                    || PROP_BET_TYPES.contains(betType);
        }
        return false;
    }

    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();
        if (keyCode == 256) {
            this.onClose();
            return true;
        }
        // Space or Enter to roll during rolling phases
        if ((keyCode == 32 || keyCode == 257) &&
                ("COME_OUT_ROLL".equals(phase) || "POINT_PHASE".equals(phase)) &&
                diceAnimTimer <= 0) {
            sendAction("roll", 0);
            return true;
        }
        return super.keyPressed(event);
    }

    private void sendAction(String action, int amount) {
        ClientPacketDistributor.sendToServer(
                (CustomPacketPayload) new CrapsActionPayload(action, amount, tablePos),
                (CustomPacketPayload[]) new CustomPacketPayload[0]);
    }

    public boolean isPauseScreen() { return false; }
}

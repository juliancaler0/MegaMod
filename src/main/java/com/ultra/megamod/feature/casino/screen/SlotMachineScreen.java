package com.ultra.megamod.feature.casino.screen;

import com.ultra.megamod.feature.casino.chips.ChipRenderer;
import com.ultra.megamod.feature.casino.network.SlotSpinPayload;
import com.ultra.megamod.feature.casino.network.SlotResultPayload;
import com.ultra.megamod.feature.casino.network.SlotConfigPayload;
import com.ultra.megamod.feature.casino.slots.SlotSymbol;
import com.ultra.megamod.feature.casino.slots.SlotReels;
import com.ultra.megamod.feature.casino.slots.SlotBetConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class SlotMachineScreen extends Screen {

    private final BlockPos machinePos;
    private int betIndex;
    private int lineMode;
    private int wallet;

    // --- GUI texture ---
    private static final Identifier GUI_TEXTURE =
            Identifier.fromNamespaceAndPath("megamod", "textures/gui/slot_machine/slot_machine_gui.png");
    // Actual PNG is 256x256 but the visible slot machine content is ~220x205 within it
    private static final int GUI_TEX_W = 256;
    private static final int GUI_TEX_H = 256;
    private static final int GUI_RENDER_W = 220;
    private static final int GUI_RENDER_H = 205;

    // --- Reel layout (relative to GUI origin) ---
    private static final int SYMBOL_SIZE = 32;
    private static final float REEL_SPEED = 24.0f;
    private static final int REEL_COUNT = 3;
    private static final int VISIBLE_ROWS = 3;
    private static final int[] REEL_X_OFFSETS = {55, 94, 133}; // from gui left
    private static final int REEL_ROW_Y = 85; // center row Y from gui top
    private static final int REEL_TOP_Y = 47; // top of reel window in texture
    private static final int REEL_BOTTOM_Y = 148; // bottom of reel window in texture

    // Reel animation state
    private final float[] reelOffset = new float[REEL_COUNT];
    private final boolean[] reelSpinning = new boolean[REEL_COUNT];
    private final int[] reelIndex = new int[REEL_COUNT];
    private final int[] targetStops = new int[REEL_COUNT];
    private final int[] reelTimers = new int[REEL_COUNT];
    private boolean spinning = false;

    // Win display
    private int lastWin = 0;
    private String lastWinText = "";
    private int winFlashTimer = 0;
    private String winsJson = "";

    // Pending results (shown only after reels stop)
    private int pendingWin = -1;
    private int pendingWallet = -1;

    // Colors
    private static final int GOLD_TEXT = 0xFFD4AF37;
    private static final int WHITE_TEXT = 0xFFCCCCDD;
    private static final int DIM_TEXT = 0xFF666677;
    private static final int SPIN_BTN_READY = 0xFFCC2222;
    private static final int SPIN_BTN_HOVER = 0xFFEE3333;
    private static final int SPIN_BTN_DISABLED = 0xFF555555;
    private static final int WIN_GOLD = 0xFFFFD700;

    public SlotMachineScreen(BlockPos machinePos, int betIndex, int lineMode, int wallet) {
        super(Component.literal("Mega Slots"));
        this.machinePos = machinePos;
        this.betIndex = betIndex;
        this.lineMode = lineMode;
        this.wallet = wallet;

        for (int i = 0; i < REEL_COUNT; i++) {
            this.reelIndex[i] = i * 37;
            this.reelOffset[i] = 0;
            this.reelSpinning[i] = false;
        }
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void tick() {
        super.tick();

        // Poll for spin result
        SlotResultPayload result = SlotResultPayload.lastResult;
        if (result != null) {
            SlotResultPayload.lastResult = null;
            this.targetStops[0] = result.stop0();
            this.targetStops[1] = result.stop1();
            this.targetStops[2] = result.stop2();
            // Store pending results - don't show until reels stop
            this.pendingWin = result.totalWin();
            this.pendingWallet = result.newWallet();
            this.winsJson = result.winsJson();

            // Start spinning animation
            this.spinning = true;
            for (int i = 0; i < REEL_COUNT; i++) {
                this.reelSpinning[i] = true;
                this.reelOffset[i] = 0;
            }
            // Staggered stop timers
            this.reelTimers[0] = 40;
            this.reelTimers[1] = 46;
            this.reelTimers[2] = 54;
        }

        // Animate reels
        if (this.spinning) {
            boolean allStopped = true;
            for (int i = 0; i < REEL_COUNT; i++) {
                if (this.reelSpinning[i]) {
                    allStopped = false;
                    this.reelTimers[i]--;

                    this.reelOffset[i] += REEL_SPEED;
                    if (this.reelOffset[i] >= SYMBOL_SIZE) {
                        this.reelOffset[i] -= SYMBOL_SIZE;
                        this.reelIndex[i] = (this.reelIndex[i] + 1) % SlotReels.getReelSize();
                    }

                    if (this.reelTimers[i] <= 0) {
                        this.reelIndex[i] = this.targetStops[i];
                        this.reelOffset[i] = 0;
                        this.reelSpinning[i] = false;
                    }
                }
            }

            if (allStopped) {
                this.spinning = false;
                // Now reveal the results
                if (this.pendingWin >= 0) {
                    this.lastWin = this.pendingWin;
                    this.wallet = this.pendingWallet;
                    this.pendingWin = -1;
                    this.pendingWallet = -1;
                }
                if (this.lastWin > 0) {
                    this.lastWinText = "WIN: " + this.lastWin + " MC!";
                    this.winFlashTimer = 60;
                } else {
                    this.lastWinText = "";
                    this.winFlashTimer = 0;
                }
            }
        }

        if (this.winFlashTimer > 0) {
            this.winFlashTimer--;
        }
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Don't render default darkening - we do our own
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Semi-transparent darkening so world is visible behind
        g.fill(0, 0, this.width, this.height, 0x88000000);

        // GUI origin (centered)
        int guiX = (this.width - GUI_RENDER_W) / 2;
        int guiY = (this.height - GUI_RENDER_H) / 2;

        // Render the slot machine GUI texture as background
        // blit(pipeline, texture, x, y, u, v, blitWidth, blitHeight, texWidth, texHeight)
        g.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED,
                GUI_TEXTURE, guiX, guiY, 0f, 0f, GUI_RENDER_W, GUI_RENDER_H, GUI_TEX_W, GUI_TEX_H);

        // --- Current bet display (top right of GUI) ---
        int betValue = SlotBetConfig.getBetValue(this.betIndex);
        int lineCount = SlotBetConfig.getLineMultiplier(this.lineMode);
        int totalBet = SlotBetConfig.getTotalBet(this.betIndex, this.lineMode);

        // Show bet per spin in top right of GUI (clear and visible)
        String betLabel = "Bet: " + totalBet + " MC";
        int betLabelW = this.font.width(betLabel);
        g.drawString(this.font, betLabel, guiX + GUI_RENDER_W - betLabelW - 6, guiY + 20, 0xFF00FF00, false);

        // --- Reel area rendering ---
        // Clip to the actual visible 3-row reel area based on center row position
        int reelLeft = guiX + REEL_X_OFFSETS[0];
        int reelRight = guiX + REEL_X_OFFSETS[2] + SYMBOL_SIZE;
        int reelTop = guiY + REEL_ROW_Y - SYMBOL_SIZE;       // top row start
        int reelBottom = guiY + REEL_ROW_Y + 2 * SYMBOL_SIZE; // bottom row end

        // Scissor clip to keep symbols inside the reel window
        g.enableScissor(reelLeft, reelTop, reelRight, reelBottom);

        for (int reel = 0; reel < REEL_COUNT; reel++) {
            int colX = guiX + REEL_X_OFFSETS[reel];

            // Draw visible rows + extra for scrolling
            // reelIndex is the CENTER stop position (matches server matrix[1][col])
            // row 0 = top, row 1 = center (stop), row 2 = bottom
            for (int row = -2; row <= VISIBLE_ROWS + 1; row++) {
                // Offset by -1 so row=1 (center visual) maps to reelIndex (the stop)
                int symIdx = (this.reelIndex[reel] + row - 1 + SlotReels.getReelSize()) % SlotReels.getReelSize();
                SlotSymbol sym = SlotReels.getSymbol(reel, symIdx);

                // row 0 = top, row 1 = center (REEL_ROW_Y), row 2 = bottom
                int symY = guiY + REEL_ROW_Y + (row - 1) * SYMBOL_SIZE - (int) this.reelOffset[reel];

                // Render symbol texture
                Identifier symTex = Identifier.fromNamespaceAndPath("megamod",
                        "textures/gui/slot_machine/symbols/" + sym.getTextureId() + ".png");
                g.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED,
                        symTex, colX, symY, 0f, 0f, SYMBOL_SIZE, SYMBOL_SIZE, SYMBOL_SIZE, SYMBOL_SIZE);
            }
        }

        g.disableScissor();

        // --- Center row highlight (pay line indicators) ---
        int centerRowY = guiY + REEL_ROW_Y;
        // Gold lines above and below center row
        g.fill(reelLeft - 2, centerRowY - 1, reelRight + 2, centerRowY, GOLD_TEXT);
        g.fill(reelLeft - 2, centerRowY + SYMBOL_SIZE, reelRight + 2, centerRowY + SYMBOL_SIZE + 1, GOLD_TEXT);

        // Multi-line indicators
        if (this.lineMode >= 1) {
            // Top row markers
            int topRowY = guiY + REEL_ROW_Y - SYMBOL_SIZE;
            g.fill(reelLeft - 3, topRowY, reelLeft - 1, topRowY + SYMBOL_SIZE, 0x88FF6666);
            // Bottom row markers
            int botRowY = guiY + REEL_ROW_Y + SYMBOL_SIZE;
            g.fill(reelLeft - 3, botRowY, reelLeft - 1, botRowY + SYMBOL_SIZE, 0x88FF6666);
        }
        if (this.lineMode >= 2) {
            // Diagonal indicators
            int topRowY = guiY + REEL_ROW_Y - SYMBOL_SIZE;
            int botRowY = guiY + REEL_ROW_Y + SYMBOL_SIZE;
            g.fill(reelRight + 1, topRowY, reelRight + 3, topRowY + SYMBOL_SIZE, 0x886666FF);
            g.fill(reelRight + 1, botRowY, reelRight + 3, botRowY + SYMBOL_SIZE, 0x886666FF);
        }

        // --- Win display (flashing) ---
        if (this.winFlashTimer > 0 && !this.lastWinText.isEmpty()) {
            boolean visible = (this.winFlashTimer / 5) % 2 == 0;
            if (visible) {
                int winW = this.font.width(this.lastWinText);
                int winY = guiY + REEL_ROW_Y + SYMBOL_SIZE + SYMBOL_SIZE + 6;
                g.drawString(this.font, this.lastWinText,
                        guiX + (GUI_RENDER_W - winW) / 2, winY, WIN_GOLD, false);
            }
        }

        // --- Bet controls below the reels, above spin button ---
        int controlsY = guiY + 152;
        int btnH = 14;
        int btnW = 20;

        // Minus button
        int minusBtnX = guiX + 14;
        boolean minusHover = mouseX >= minusBtnX && mouseX < minusBtnX + btnW
                && mouseY >= controlsY && mouseY < controlsY + btnH;
        g.fill(minusBtnX, controlsY, minusBtnX + btnW, controlsY + btnH,
                minusHover ? 0xFF555566 : 0xFF333344);
        g.drawString(this.font, "-", minusBtnX + 7, controlsY + 3, 0xFFFFFFFF, false);

        // Bet value display
        String betValStr = String.valueOf(betValue);
        int betValW = this.font.width(betValStr);
        g.drawString(this.font, betValStr, minusBtnX + btnW + 6, controlsY + 3, 0xFFFFFFFF, false);

        // Plus button
        int plusBtnX = minusBtnX + btnW + 6 + betValW + 6;
        boolean plusHover = mouseX >= plusBtnX && mouseX < plusBtnX + btnW
                && mouseY >= controlsY && mouseY < controlsY + btnH;
        g.fill(plusBtnX, controlsY, plusBtnX + btnW, controlsY + btnH,
                plusHover ? 0xFF555566 : 0xFF333344);
        g.drawString(this.font, "+", plusBtnX + 7, controlsY + 3, 0xFFFFFFFF, false);

        // Line mode button
        int linesBtnX = guiX + GUI_RENDER_W - 60;
        int linesBtnW = 50;
        boolean linesHover = mouseX >= linesBtnX && mouseX < linesBtnX + linesBtnW
                && mouseY >= controlsY && mouseY < controlsY + btnH;
        g.fill(linesBtnX, controlsY, linesBtnX + linesBtnW, controlsY + btnH,
                linesHover ? 0xFF555566 : 0xFF333344);
        String linesLabel = lineCount + " Lines";
        int linesLabelW = this.font.width(linesLabel);
        g.drawString(this.font, linesLabel, linesBtnX + (linesBtnW - linesLabelW) / 2, controlsY + 3,
                linesHover ? 0xFFFFFF00 : 0xFFFFFFFF, false);

        // --- SPIN button (red circle with SPIN text) ---
        int spinCenterX = guiX + 90 + 20; // center of the 40px area
        int spinCenterY = guiY + 161 + 20;
        int spinRadius = 20;

        boolean canSpin = !this.spinning && this.wallet >= totalBet;
        boolean spinHover = mouseX >= spinCenterX - spinRadius && mouseX < spinCenterX + spinRadius
                && mouseY >= spinCenterY - spinRadius && mouseY < spinCenterY + spinRadius;

        int spinBtnColor;
        if (!canSpin) {
            spinBtnColor = SPIN_BTN_DISABLED;
        } else if (spinHover) {
            spinBtnColor = SPIN_BTN_HOVER;
        } else {
            spinBtnColor = SPIN_BTN_READY;
        }

        // Draw circular spin button as a filled rounded shape (approximate with rectangles)
        drawFilledCircle(g, spinCenterX, spinCenterY, spinRadius, spinBtnColor);
        drawCircleOutline(g, spinCenterX, spinCenterY, spinRadius, 0xFF880000);

        String spinText = this.spinning ? "..." : "SPIN";
        int spinTextW = this.font.width(spinText);
        g.drawString(this.font, spinText,
                spinCenterX - spinTextW / 2,
                spinCenterY - 4,
                0xFFFFFFFF, false);

        // --- Balance display (bottom left, under BALANCE label in texture) ---
        // Texture has "BALANCE" label built in, just show the number below it
        String walletStr = String.valueOf(this.wallet);
        g.drawString(this.font, walletStr, guiX + 30, guiY + 187, 0xFF00FF00, false);

        // --- Last win display (bottom right, under LAST WIN label in texture) ---
        String winStr = this.lastWin > 0 ? String.valueOf(this.lastWin) : "0";
        int winStrW = this.font.width(winStr);
        g.drawString(this.font, winStr, guiX + GUI_RENDER_W - winStrW - 30, guiY + 187, 0xFF00FF00, false);

        super.render(g, mouseX, mouseY, partialTick);
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean consumed) {
        if (consumed) {
            return super.mouseClicked(event, consumed);
        }

        int mx = (int) event.x();
        int my = (int) event.y();

        int guiX = (this.width - GUI_RENDER_W) / 2;
        int guiY = (this.height - GUI_RENDER_H) / 2;

        // --- Bet controls area (below reels, matches render) ---
        int controlsY = guiY + 152;
        int btnH = 14;
        int btnW = 20;

        int betValue = SlotBetConfig.getBetValue(this.betIndex);
        String betValStr = String.valueOf(betValue);
        int betValW = this.font.width(betValStr);

        // Minus button
        int minusBtnX = guiX + 14;
        if (mx >= minusBtnX && mx < minusBtnX + btnW && my >= controlsY && my < controlsY + btnH) {
            if (this.betIndex > 0 && !this.spinning) {
                this.betIndex--;
                sendConfig();
            }
            return true;
        }

        // Plus button
        int plusBtnX = minusBtnX + btnW + 6 + betValW + 6;
        if (mx >= plusBtnX && mx < plusBtnX + btnW && my >= controlsY && my < controlsY + btnH) {
            if (this.betIndex < SlotBetConfig.BET_VALUES.length - 1 && !this.spinning) {
                this.betIndex++;
                sendConfig();
            }
            return true;
        }

        // Line mode button
        int linesBtnX = guiX + GUI_RENDER_W - 60;
        int linesBtnW = 50;
        if (mx >= linesBtnX && mx < linesBtnX + linesBtnW && my >= controlsY && my < controlsY + btnH) {
            if (!this.spinning) {
                this.lineMode = (this.lineMode + 1) % SlotBetConfig.LINE_MULTIPLIERS.length;
                sendConfig();
            }
            return true;
        }

        // --- SPIN button (circular hit test) ---
        int spinCenterX = guiX + 90 + 20;
        int spinCenterY = guiY + 161 + 20;
        int spinRadius = 20;

        int dx = mx - spinCenterX;
        int dy = my - spinCenterY;
        if (dx * dx + dy * dy <= spinRadius * spinRadius) {
            int totalBet = SlotBetConfig.getTotalBet(this.betIndex, this.lineMode);
            boolean canSpin = !this.spinning && this.wallet >= totalBet;
            if (canSpin) {
                ClientPacketDistributor.sendToServer(new SlotSpinPayload(this.machinePos));
                return true;
            }
        }

        return super.mouseClicked(event, consumed);
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            this.onClose();
            return true;
        }
        return false;
    }

    @Override
    public void onClose() {
        ChipRenderer.clearDrag();
        super.onClose();
    }

    private void sendConfig() {
        ClientPacketDistributor.sendToServer(
                new SlotConfigPayload(this.machinePos, this.betIndex, this.lineMode));
    }

    /**
     * Draws a filled circle approximation using horizontal lines.
     */
    private static void drawFilledCircle(GuiGraphics g, int cx, int cy, int radius, int color) {
        for (int dy = -radius; dy <= radius; dy++) {
            int halfWidth = (int) Math.sqrt(radius * radius - dy * dy);
            g.fill(cx - halfWidth, cy + dy, cx + halfWidth, cy + dy + 1, color);
        }
    }

    /**
     * Draws a circle outline approximation.
     */
    private static void drawCircleOutline(GuiGraphics g, int cx, int cy, int radius, int color) {
        int segments = 64;
        for (int i = 0; i < segments; i++) {
            double angle = 2.0 * Math.PI * i / segments;
            int px = cx + (int) (radius * Math.cos(angle));
            int py = cy + (int) (radius * Math.sin(angle));
            g.fill(px, py, px + 1, py + 1, color);
        }
    }

    public boolean isPauseScreen() {
        return false;
    }
}

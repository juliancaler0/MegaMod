package com.ultra.megamod.feature.computer.screen;

import com.ultra.megamod.feature.computer.network.ComputerActionPayload;
import com.ultra.megamod.feature.ui.UIHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class MinigamesScreen extends Screen {

    private final Screen parent;
    private int currentGame = -1; // -1=menu, 0=minesweeper, 1=snake, 2=tetris

    // Layout
    private int titleBarH;

    // ========== Minesweeper state ==========
    private static final int MS_ROWS = 16;
    private static final int MS_COLS = 16;
    private static final int MS_MINES = 40;
    private int[][] msGrid;
    private boolean[][] msRevealed;
    private boolean[][] msFlagged;
    private boolean msGameOver = false;
    private boolean msWon = false;
    private int msFlagCount = 0;
    private int msTimer = 0;
    private boolean msStarted = false;
    private boolean msFirstClick = true;

    // ========== Snake state ==========
    private static final int SN_COLS = 20;
    private static final int SN_ROWS = 15;
    private List<int[]> snakeBody = new ArrayList<>();
    private int snakeDirX = 1, snakeDirY = 0;
    private int snakeNextDirX = 1, snakeNextDirY = 0;
    private int[] snakeFood;
    private int snakeScore = 0;
    private boolean snakeGameOver = false;
    private boolean snakePaused = false;
    private int snakeTickCounter = 0;
    private int snakeSpeed = 6;

    // ========== Tetris state ==========
    private static final int TET_COLS = 10;
    private static final int TET_ROWS = 20;
    private int[][] tetBoard = new int[TET_ROWS][TET_COLS]; // 0=empty, 1-7=piece color
    private int[][] tetPiece; // current piece shape (relative coords)
    private int tetPieceType; // 1-7
    private int tetPieceX, tetPieceY; // position of piece pivot on board
    private int tetNextType; // next piece type
    private int tetScore = 0;
    private int tetLines = 0;
    private int tetLevel = 1;
    private boolean tetGameOver = false;
    private boolean tetPaused = false;
    private int tetTickCounter = 0;
    private final Random tetRandom = new Random();

    // Tetris piece definitions: each is array of {row, col} offsets from pivot
    private static final int[][][] TET_PIECES = {
        null, // index 0 unused
        {{0,0},{0,1},{1,0},{1,1}},       // 1: O (square)
        {{0,0},{0,-1},{0,1},{0,2}},      // 2: I (line)
        {{0,0},{0,-1},{0,1},{1,1}},      // 3: L
        {{0,0},{0,-1},{0,1},{1,-1}},     // 4: J
        {{0,0},{0,-1},{1,0},{1,1}},      // 5: S
        {{0,0},{0,1},{1,0},{1,-1}},      // 6: Z
        {{0,0},{0,-1},{0,1},{1,0}}       // 7: T
    };

    // Tetris piece colors
    private static final int[] TET_COLORS = {
        0, // 0 unused
        0xFFFFDD44, // O - yellow
        0xFF44DDFF, // I - cyan
        0xFFFF8844, // L - orange
        0xFF4466FF, // J - blue
        0xFF44FF66, // S - green
        0xFFFF4444, // Z - red
        0xFFAA44FF  // T - purple
    };

    // Minesweeper number colors
    private static final int[] MS_NUM_COLORS = {
        0xFF000000, 0xFF4488FF, 0xFF44BB44, 0xFFFF4444, 0xFFAA44FF,
        0xFF884422, 0xFF44AAAA, 0xFF222222, 0xFF888888
    };

    // Colors
    private static final int BG_DARK = 0xFF0D1117;
    private static final int CELL_UNREVEALED = 0xFF3A3A40;
    private static final int CELL_UNREVEALED_LIGHT = 0xFF4A4A50;
    private static final int CELL_UNREVEALED_DARK = 0xFF2A2A30;
    private static final int CELL_REVEALED = 0xFF1A1A20;
    private static final int CELL_REVEALED_BORDER = 0xFF252530;
    private static final int FLAG_COLOR = 0xFFFF4444;
    private static final int MINE_COLOR = 0xFFFFFFFF;
    private static final int SNAKE_BODY = 0xFF3FB950;
    private static final int SNAKE_HEAD = 0xFF56D364;
    private static final int SNAKE_FOOD = 0xFFF85149;
    private static final int SNAKE_GRID_LINE = 0xFF161B22;
    private static final int HEADER_BG = 0xFF161B22;
    private static final int TEXT_WHITE = 0xFFE6EDF3;
    private static final int TEXT_DIM = 0xFF8B949E;
    private static final int BUTTON_BG = 0xFF21262D;
    private static final int BUTTON_HOVER = 0xFF30363D;
    private static final int BUTTON_BORDER = 0xFF363B42;
    private static final int WIN_COLOR = 0xFF3FB950;
    private static final int LOSE_COLOR = 0xFFF85149;
    private static final int TET_GRID_LINE = 0xFF1A1A24;
    private static final int TET_GHOST = 0x33FFFFFF;
    private static final int TET_BORDER = 0xFF30363D;

    public MinigamesScreen(Screen parent) {
        super(Component.literal("Mini-Games"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        Objects.requireNonNull(this.font);
        this.titleBarH = 9 + 16;
    }

    // ========================================
    //               RENDERING
    // ========================================

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, this.width, this.height, 0xFF0A0A10);
        UIHelper.drawPanel(g, 0, 0, this.width, this.height);
        UIHelper.drawTitleBar(g, 0, 0, this.width, this.titleBarH);
        Objects.requireNonNull(this.font);
        int titleY = (this.titleBarH - 9) / 2;

        if (currentGame == -1) {
            UIHelper.drawCenteredTitle(g, this.font, "Mini-Games", this.width / 2, titleY);
            renderMenu(g, mouseX, mouseY);
        } else if (currentGame == 0) {
            UIHelper.drawCenteredTitle(g, this.font, "Minesweeper", this.width / 2, titleY);
            renderMinesweeper(g, mouseX, mouseY);
        } else if (currentGame == 1) {
            UIHelper.drawCenteredTitle(g, this.font, "Snake", this.width / 2, titleY);
            renderSnake(g, mouseX, mouseY);
        } else if (currentGame == 2) {
            UIHelper.drawCenteredTitle(g, this.font, "Tetris", this.width / 2, titleY);
            renderTetris(g, mouseX, mouseY);
        }

        // Back button
        int backW = 50;
        int backH = 16;
        int backX = 8;
        int backY = (this.titleBarH - backH) / 2;
        boolean backHover = mouseX >= backX && mouseX < backX + backW && mouseY >= backY && mouseY < backY + backH;
        UIHelper.drawButton(g, backX, backY, backW, backH, backHover);
        int backTextX = backX + (backW - this.font.width("< Back")) / 2;
        g.drawString(this.font, "< Back", backTextX, backY + (backH - 9) / 2, UIHelper.CREAM_TEXT, false);

        super.render(g, mouseX, mouseY, partialTick);
    }

    // ---------- Game Selector Menu ----------

    private void renderMenu(GuiGraphics g, int mouseX, int mouseY) {
        int cardW = 120;
        int cardH = 80;
        int gap = 16;
        int totalW = cardW * 3 + gap * 2;
        int startX = (this.width - totalW) / 2;
        int startY = (this.height - cardH) / 2;

        String[] names = {"Minesweeper", "Snake", "Tetris"};
        String[] icons = {"*", "~", "T"};
        int[] iconColors = {LOSE_COLOR, SNAKE_BODY, TET_COLORS[7]};

        for (int i = 0; i < 3; i++) {
            int cx = startX + i * (cardW + gap);
            int cy = startY;
            boolean hovered = mouseX >= cx && mouseX < cx + cardW && mouseY >= cy && mouseY < cy + cardH;
            UIHelper.drawCard(g, cx, cy, cardW, cardH, hovered);

            int iconW = this.font.width(icons[i]);
            g.drawString(this.font, icons[i], cx + (cardW - iconW) / 2, cy + 20, iconColors[i], false);

            int nameW = this.font.width(names[i]);
            g.drawString(this.font, names[i], cx + (cardW - nameW) / 2, cy + cardH - 22, UIHelper.CREAM_TEXT, false);
        }
    }

    // ---------- Minesweeper Render ----------

    private void renderMinesweeper(GuiGraphics g, int mouseX, int mouseY) {
        int cellSize = Math.min(18, Math.min((this.width - 40) / MS_COLS, (this.height - this.titleBarH - 70) / MS_ROWS));
        if (cellSize < 10) cellSize = 10;
        int gridW = cellSize * MS_COLS;
        int gridH = cellSize * MS_ROWS;
        int gridX = (this.width - gridW) / 2;
        int gridY = this.titleBarH + 40;

        int headerY = this.titleBarH + 6;
        int headerH = 28;
        g.fill(gridX, headerY, gridX + gridW, headerY + headerH, HEADER_BG);
        g.fill(gridX, headerY + headerH - 1, gridX + gridW, headerY + headerH, TET_BORDER);

        int flagsRemaining = MS_MINES - msFlagCount;
        g.drawString(this.font, "Mines: " + flagsRemaining, gridX + 6, headerY + (headerH - 9) / 2, LOSE_COLOR, false);

        String timeStr = "Time: " + msTimer;
        int timeW = this.font.width(timeStr);
        g.drawString(this.font, timeStr, gridX + gridW - timeW - 6, headerY + (headerH - 9) / 2, TEXT_WHITE, false);

        String newGameStr = "New Game";
        int btnW = this.font.width(newGameStr) + 12;
        int btnH = 16;
        int btnX = gridX + (gridW - btnW) / 2;
        int btnY = headerY + (headerH - btnH) / 2;
        boolean btnHover = mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY && mouseY < btnY + btnH;
        drawGameButton(g, btnX, btnY, btnW, btnH, btnHover);
        g.drawString(this.font, newGameStr, btnX + (btnW - this.font.width(newGameStr)) / 2, btnY + (btnH - 9) / 2, TEXT_WHITE, false);

        g.fill(gridX - 1, gridY - 1, gridX + gridW + 1, gridY + gridH + 1, 0xFF000000);

        if (msGrid == null) return;

        for (int r = 0; r < MS_ROWS; r++) {
            for (int c = 0; c < MS_COLS; c++) {
                int cx = gridX + c * cellSize;
                int cy = gridY + r * cellSize;
                boolean cellHover = mouseX >= cx && mouseX < cx + cellSize && mouseY >= cy && mouseY < cy + cellSize;

                if (msRevealed[r][c]) {
                    g.fill(cx, cy, cx + cellSize, cy + cellSize, CELL_REVEALED);
                    g.fill(cx, cy, cx + cellSize, cy + 1, CELL_REVEALED_BORDER);
                    g.fill(cx, cy, cx + 1, cy + cellSize, CELL_REVEALED_BORDER);

                    if (msGrid[r][c] == -1) {
                        String mine = "*";
                        int mw = this.font.width(mine);
                        g.drawString(this.font, mine, cx + (cellSize - mw) / 2, cy + (cellSize - 9) / 2, MINE_COLOR, false);
                    } else if (msGrid[r][c] > 0) {
                        String num = String.valueOf(msGrid[r][c]);
                        int nw = this.font.width(num);
                        g.drawString(this.font, num, cx + (cellSize - nw) / 2, cy + (cellSize - 9) / 2, MS_NUM_COLORS[msGrid[r][c]], false);
                    }
                } else {
                    g.fill(cx, cy, cx + cellSize, cy + cellSize, CELL_UNREVEALED);
                    g.fill(cx, cy, cx + cellSize, cy + 1, CELL_UNREVEALED_LIGHT);
                    g.fill(cx, cy, cx + 1, cy + cellSize, CELL_UNREVEALED_LIGHT);
                    g.fill(cx, cy + cellSize - 1, cx + cellSize, cy + cellSize, CELL_UNREVEALED_DARK);
                    g.fill(cx + cellSize - 1, cy, cx + cellSize, cy + cellSize, CELL_UNREVEALED_DARK);

                    if (cellHover && !msGameOver) {
                        g.fill(cx + 1, cy + 1, cx + cellSize - 1, cy + cellSize - 1, 0x22FFFFFF);
                    }

                    if (msFlagged[r][c]) {
                        String flag = "F";
                        int fw = this.font.width(flag);
                        g.drawString(this.font, flag, cx + (cellSize - fw) / 2, cy + (cellSize - 9) / 2, FLAG_COLOR, false);
                    }
                }
            }
        }

        if (msGameOver) {
            String msg = msWon ? "YOU WIN!" : "GAME OVER";
            int msgColor = msWon ? WIN_COLOR : LOSE_COLOR;
            int msgW = this.font.width(msg);
            g.drawString(this.font, msg, this.width / 2 - msgW / 2 + 1, gridY + gridH + 9, 0xFF000000, false);
            g.drawString(this.font, msg, this.width / 2 - msgW / 2, gridY + gridH + 8, msgColor, false);
        }
    }

    // ---------- Snake Render ----------

    private void renderSnake(GuiGraphics g, int mouseX, int mouseY) {
        int cellSize = Math.min(18, Math.min((this.width - 40) / SN_COLS, (this.height - this.titleBarH - 70) / SN_ROWS));
        if (cellSize < 10) cellSize = 10;
        int gridW = cellSize * SN_COLS;
        int gridH = cellSize * SN_ROWS;
        int gridX = (this.width - gridW) / 2;
        int gridY = this.titleBarH + 40;

        int headerY = this.titleBarH + 6;
        int headerH = 28;
        g.fill(gridX, headerY, gridX + gridW, headerY + headerH, HEADER_BG);
        g.fill(gridX, headerY + headerH - 1, gridX + gridW, headerY + headerH, TET_BORDER);

        g.drawString(this.font, "Score: " + snakeScore, gridX + 6, headerY + (headerH - 9) / 2, SNAKE_BODY, false);

        String statusStr;
        if (snakeGameOver) statusStr = "GAME OVER";
        else if (snakePaused) statusStr = "PAUSED";
        else statusStr = "Speed: " + (11 - snakeSpeed);
        int statusW = this.font.width(statusStr);
        int statusColor = snakeGameOver ? LOSE_COLOR : (snakePaused ? TET_COLORS[4] : TEXT_DIM);
        g.drawString(this.font, statusStr, gridX + (gridW - statusW) / 2, headerY + (headerH - 9) / 2, statusColor, false);

        String newGameStr = "New";
        int btnW = this.font.width(newGameStr) + 12;
        int btnH = 16;
        int btnX = gridX + gridW - btnW - 4;
        int btnY = headerY + (headerH - btnH) / 2;
        boolean btnHover = mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY && mouseY < btnY + btnH;
        drawGameButton(g, btnX, btnY, btnW, btnH, btnHover);
        g.drawString(this.font, newGameStr, btnX + (btnW - this.font.width(newGameStr)) / 2, btnY + (btnH - 9) / 2, TEXT_WHITE, false);

        g.fill(gridX - 1, gridY - 1, gridX + gridW + 1, gridY + gridH + 1, 0xFF000000);
        g.fill(gridX, gridY, gridX + gridW, gridY + gridH, BG_DARK);

        for (int c = 1; c < SN_COLS; c++) {
            g.fill(gridX + c * cellSize, gridY, gridX + c * cellSize + 1, gridY + gridH, SNAKE_GRID_LINE);
        }
        for (int r = 1; r < SN_ROWS; r++) {
            g.fill(gridX, gridY + r * cellSize, gridX + gridW, gridY + r * cellSize + 1, SNAKE_GRID_LINE);
        }

        if (snakeFood != null) {
            int fx = gridX + snakeFood[0] * cellSize;
            int fy = gridY + snakeFood[1] * cellSize;
            int inset = Math.max(1, cellSize / 5);
            g.fill(fx + inset, fy + inset, fx + cellSize - inset, fy + cellSize - inset, SNAKE_FOOD);
            g.fill(fx + inset, fy + inset, fx + cellSize - inset, fy + inset + 1, 0xFFFF8888);
        }

        for (int i = 0; i < snakeBody.size(); i++) {
            int[] seg = snakeBody.get(i);
            int sx = gridX + seg[0] * cellSize;
            int sy = gridY + seg[1] * cellSize;
            int inset = Math.max(1, cellSize / 8);
            boolean isHead = (i == 0);
            g.fill(sx + inset, sy + inset, sx + cellSize - inset, sy + cellSize - inset, isHead ? SNAKE_HEAD : SNAKE_BODY);
            if (isHead) g.fill(sx + inset, sy + inset, sx + cellSize - inset, sy + inset + 1, 0xFF7EE787);
        }

        if (!snakeGameOver && !snakePaused && !snakeBody.isEmpty()) {
            String hint = "Arrow keys to move, Space to pause";
            int hintW = this.font.width(hint);
            g.drawString(this.font, hint, this.width / 2 - hintW / 2, gridY + gridH + 6, TEXT_DIM, false);
        }
    }

    // ---------- Tetris Render ----------

    private void renderTetris(GuiGraphics g, int mouseX, int mouseY) {
        int cellSize = Math.min(16, (this.height - this.titleBarH - 60) / TET_ROWS);
        if (cellSize < 8) cellSize = 8;
        int gridW = cellSize * TET_COLS;
        int gridH = cellSize * TET_ROWS;
        int gridX = (this.width - gridW) / 2 - 40;
        int gridY = this.titleBarH + 40;

        // Header
        int headerY = this.titleBarH + 6;
        int headerH = 28;
        int headerW = gridW + 100;
        int headerX = gridX;
        g.fill(headerX, headerY, headerX + headerW, headerY + headerH, HEADER_BG);
        g.fill(headerX, headerY + headerH - 1, headerX + headerW, headerY + headerH, TET_BORDER);

        g.drawString(this.font, "Score: " + tetScore, headerX + 6, headerY + (headerH - 9) / 2, TET_COLORS[7], false);
        String lvlStr = "Lvl:" + tetLevel + " Lines:" + tetLines;
        int lvlW = this.font.width(lvlStr);
        g.drawString(this.font, lvlStr, headerX + headerW - lvlW - 6, headerY + (headerH - 9) / 2, TEXT_WHITE, false);

        // New Game button
        String newGameStr = "New";
        int btnW = this.font.width(newGameStr) + 12;
        int btnH = 16;
        int btnX = headerX + (headerW - btnW) / 2;
        int btnY = headerY + (headerH - btnH) / 2;
        boolean btnHover = mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY && mouseY < btnY + btnH;
        drawGameButton(g, btnX, btnY, btnW, btnH, btnHover);
        g.drawString(this.font, newGameStr, btnX + (btnW - this.font.width(newGameStr)) / 2, btnY + (btnH - 9) / 2, TEXT_WHITE, false);

        // Board border
        g.fill(gridX - 2, gridY - 2, gridX + gridW + 2, gridY + gridH + 2, TET_BORDER);
        g.fill(gridX, gridY, gridX + gridW, gridY + gridH, BG_DARK);

        // Grid lines
        for (int c = 1; c < TET_COLS; c++) {
            g.fill(gridX + c * cellSize, gridY, gridX + c * cellSize + 1, gridY + gridH, TET_GRID_LINE);
        }
        for (int r = 1; r < TET_ROWS; r++) {
            g.fill(gridX, gridY + r * cellSize, gridX + gridW, gridY + r * cellSize + 1, TET_GRID_LINE);
        }

        // Placed blocks
        for (int r = 0; r < TET_ROWS; r++) {
            for (int c = 0; c < TET_COLS; c++) {
                if (tetBoard[r][c] > 0) {
                    drawTetCell(g, gridX + c * cellSize, gridY + r * cellSize, cellSize, TET_COLORS[tetBoard[r][c]]);
                }
            }
        }

        // Ghost piece (drop preview)
        if (tetPiece != null && !tetGameOver && !tetPaused) {
            int ghostY = tetPieceY;
            while (tetCanPlace(tetPiece, tetPieceX, ghostY + 1)) ghostY++;
            if (ghostY != tetPieceY) {
                for (int[] block : tetPiece) {
                    int br = ghostY + block[0];
                    int bc = tetPieceX + block[1];
                    if (br >= 0 && br < TET_ROWS && bc >= 0 && bc < TET_COLS) {
                        g.fill(gridX + bc * cellSize + 1, gridY + br * cellSize + 1,
                               gridX + bc * cellSize + cellSize - 1, gridY + br * cellSize + cellSize - 1, TET_GHOST);
                    }
                }
            }
        }

        // Current piece
        if (tetPiece != null && !tetGameOver) {
            for (int[] block : tetPiece) {
                int br = tetPieceY + block[0];
                int bc = tetPieceX + block[1];
                if (br >= 0 && br < TET_ROWS && bc >= 0 && bc < TET_COLS) {
                    drawTetCell(g, gridX + bc * cellSize, gridY + br * cellSize, cellSize, TET_COLORS[tetPieceType]);
                }
            }
        }

        // Side panel: Next piece
        int sideX = gridX + gridW + 10;
        int sideY = gridY;
        g.fill(sideX, sideY, sideX + 70, sideY + 60, HEADER_BG);
        g.fill(sideX, sideY, sideX + 70, sideY + 1, TET_BORDER);
        g.fill(sideX, sideY + 59, sideX + 70, sideY + 60, TET_BORDER);
        g.drawString(this.font, "Next", sideX + 6, sideY + 4, TEXT_DIM, false);
        if (tetNextType > 0) {
            int[][] nextShape = TET_PIECES[tetNextType];
            int previewCell = 10;
            int previewX = sideX + 15;
            int previewY = sideY + 25;
            for (int[] block : nextShape) {
                drawTetCell(g, previewX + block[1] * previewCell, previewY + block[0] * previewCell,
                           previewCell, TET_COLORS[tetNextType]);
            }
        }

        // Controls hint
        g.drawString(this.font, "Controls", sideX + 6, sideY + 70, TEXT_DIM, false);
        g.drawString(this.font, "< > Move", sideX + 6, sideY + 82, TEXT_DIM, false);
        g.drawString(this.font, "Up  Rotate", sideX + 6, sideY + 92, TEXT_DIM, false);
        g.drawString(this.font, "Dn  Soft drop", sideX + 6, sideY + 102, TEXT_DIM, false);
        g.drawString(this.font, "Spc Hard drop", sideX + 6, sideY + 112, TEXT_DIM, false);
        g.drawString(this.font, "P   Pause", sideX + 6, sideY + 122, TEXT_DIM, false);

        // Game over / paused overlay
        if (tetGameOver) {
            String msg = "GAME OVER";
            int msgW = this.font.width(msg);
            int msgX = gridX + (gridW - msgW) / 2;
            int msgY = gridY + gridH / 2 - 5;
            g.fill(msgX - 4, msgY - 4, msgX + msgW + 4, msgY + 13, 0xCC000000);
            g.drawString(this.font, msg, msgX, msgY, LOSE_COLOR, false);
        } else if (tetPaused) {
            String msg = "PAUSED";
            int msgW = this.font.width(msg);
            int msgX = gridX + (gridW - msgW) / 2;
            int msgY = gridY + gridH / 2 - 5;
            g.fill(msgX - 4, msgY - 4, msgX + msgW + 4, msgY + 13, 0xCC000000);
            g.drawString(this.font, msg, msgX, msgY, TET_COLORS[2], false);
        }
    }

    private void drawTetCell(GuiGraphics g, int x, int y, int size, int color) {
        g.fill(x, y, x + size, y + size, color);
        // Highlight (top+left)
        int light = brighten(color, 40);
        g.fill(x, y, x + size, y + 1, light);
        g.fill(x, y, x + 1, y + size, light);
        // Shadow (bottom+right)
        int dark = darken(color, 60);
        g.fill(x, y + size - 1, x + size, y + size, dark);
        g.fill(x + size - 1, y, x + size, y + size, dark);
    }

    private static int brighten(int color, int amount) {
        int r = Math.min(255, ((color >> 16) & 0xFF) + amount);
        int g = Math.min(255, ((color >> 8) & 0xFF) + amount);
        int b = Math.min(255, (color & 0xFF) + amount);
        return (color & 0xFF000000) | (r << 16) | (g << 8) | b;
    }

    private static int darken(int color, int amount) {
        int r = Math.max(0, ((color >> 16) & 0xFF) - amount);
        int g = Math.max(0, ((color >> 8) & 0xFF) - amount);
        int b = Math.max(0, (color & 0xFF) - amount);
        return (color & 0xFF000000) | (r << 16) | (g << 8) | b;
    }

    // ========================================
    //            HELPER DRAWING
    // ========================================

    private void drawGameButton(GuiGraphics g, int x, int y, int w, int h, boolean hovered) {
        int bg = hovered ? BUTTON_HOVER : BUTTON_BG;
        g.fill(x, y, x + w, y + h, BUTTON_BORDER);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, bg);
    }

    // ========================================
    //              TICK (game loop)
    // ========================================

    @Override
    public void tick() {
        super.tick();

        // Minesweeper timer
        if (currentGame == 0 && msStarted && !msGameOver && !msWon) {
            msTimer++;
        }

        // Snake movement
        if (currentGame == 1 && !snakeGameOver && !snakePaused && !snakeBody.isEmpty()) {
            snakeTickCounter++;
            if (snakeTickCounter >= snakeSpeed) {
                snakeTickCounter = 0;
                snakeStep();
            }
        }

        // Tetris gravity
        if (currentGame == 2 && !tetGameOver && !tetPaused && tetPiece != null) {
            tetTickCounter++;
            int dropSpeed = Math.max(2, 20 - (tetLevel - 1) * 2);
            if (tetTickCounter >= dropSpeed) {
                tetTickCounter = 0;
                tetDropStep();
            }
        }
    }

    // ========================================
    //            MINESWEEPER LOGIC
    // ========================================

    private void msNewGame() {
        msGrid = new int[MS_ROWS][MS_COLS];
        msRevealed = new boolean[MS_ROWS][MS_COLS];
        msFlagged = new boolean[MS_ROWS][MS_COLS];
        msGameOver = false;
        msWon = false;
        msStarted = false;
        msFirstClick = true;
        msTimer = 0;
        msFlagCount = 0;
    }

    private void msPlaceMines(int safeR, int safeC) {
        Random rand = new Random();
        int placed = 0;
        while (placed < MS_MINES) {
            int r = rand.nextInt(MS_ROWS);
            int c = rand.nextInt(MS_COLS);
            if (Math.abs(r - safeR) <= 1 && Math.abs(c - safeC) <= 1) continue;
            if (msGrid[r][c] != -1) {
                msGrid[r][c] = -1;
                placed++;
            }
        }
        for (int r = 0; r < MS_ROWS; r++) {
            for (int c = 0; c < MS_COLS; c++) {
                if (msGrid[r][c] == -1) continue;
                int count = 0;
                for (int dr = -1; dr <= 1; dr++) {
                    for (int dc = -1; dc <= 1; dc++) {
                        int nr = r + dr, nc = c + dc;
                        if (nr >= 0 && nr < MS_ROWS && nc >= 0 && nc < MS_COLS && msGrid[nr][nc] == -1) count++;
                    }
                }
                msGrid[r][c] = count;
            }
        }
    }

    private void msReveal(int r, int c) {
        if (r < 0 || r >= MS_ROWS || c < 0 || c >= MS_COLS) return;
        if (msRevealed[r][c] || msFlagged[r][c]) return;
        msRevealed[r][c] = true;
        if (msGrid[r][c] == -1) {
            msGameOver = true;
            msWon = false;
            for (int mr = 0; mr < MS_ROWS; mr++)
                for (int mc = 0; mc < MS_COLS; mc++)
                    if (msGrid[mr][mc] == -1) msRevealed[mr][mc] = true;
            return;
        }
        if (msGrid[r][c] == 0) {
            for (int dr = -1; dr <= 1; dr++)
                for (int dc = -1; dc <= 1; dc++)
                    if (dr != 0 || dc != 0) msReveal(r + dr, c + dc);
        }
        msCheckWin();
    }

    private void msCheckWin() {
        for (int r = 0; r < MS_ROWS; r++)
            for (int c = 0; c < MS_COLS; c++)
                if (msGrid[r][c] != -1 && !msRevealed[r][c]) return;
        msWon = true;
        msGameOver = true;
        // Score: faster wins score higher (max 1000 - time in ticks)
        int msScore = Math.max(1, 1000 - msTimer);
        submitScore("minesweeper", msScore);
    }

    // ========================================
    //              SNAKE LOGIC
    // ========================================

    private void snNewGame() {
        snakeBody.clear();
        snakeBody.add(new int[]{SN_COLS / 2, SN_ROWS / 2});
        snakeBody.add(new int[]{SN_COLS / 2 - 1, SN_ROWS / 2});
        snakeBody.add(new int[]{SN_COLS / 2 - 2, SN_ROWS / 2});
        snakeDirX = 1; snakeDirY = 0;
        snakeNextDirX = 1; snakeNextDirY = 0;
        snakeScore = 0;
        snakeGameOver = false;
        snakePaused = false;
        snakeTickCounter = 0;
        snakeSpeed = 6;
        snSpawnFood();
    }

    private void snSpawnFood() {
        Random rand = new Random();
        int attempts = 0;
        while (attempts < 1000) {
            int fx = rand.nextInt(SN_COLS);
            int fy = rand.nextInt(SN_ROWS);
            boolean onSnake = false;
            for (int[] seg : snakeBody) {
                if (seg[0] == fx && seg[1] == fy) { onSnake = true; break; }
            }
            if (!onSnake) { snakeFood = new int[]{fx, fy}; return; }
            attempts++;
        }
        for (int x = 0; x < SN_COLS; x++) {
            for (int y = 0; y < SN_ROWS; y++) {
                boolean onSnake = false;
                for (int[] seg : snakeBody) {
                    if (seg[0] == x && seg[1] == y) { onSnake = true; break; }
                }
                if (!onSnake) { snakeFood = new int[]{x, y}; return; }
            }
        }
    }

    private void snakeStep() {
        snakeDirX = snakeNextDirX;
        snakeDirY = snakeNextDirY;
        int[] head = snakeBody.get(0);
        int newX = head[0] + snakeDirX;
        int newY = head[1] + snakeDirY;
        if (newX < 0 || newX >= SN_COLS || newY < 0 || newY >= SN_ROWS) { snakeGameOver = true; submitScore("snake", snakeScore); return; }
        for (int[] seg : snakeBody) {
            if (seg[0] == newX && seg[1] == newY) { snakeGameOver = true; submitScore("snake", snakeScore); return; }
        }
        snakeBody.add(0, new int[]{newX, newY});
        if (snakeFood != null && snakeFood[0] == newX && snakeFood[1] == newY) {
            snakeScore++;
            if (snakeScore % 5 == 0 && snakeSpeed > 2) snakeSpeed--;
            snSpawnFood();
        } else {
            snakeBody.remove(snakeBody.size() - 1);
        }
    }

    // ========================================
    //             TETRIS LOGIC
    // ========================================

    private void tetNewGame() {
        tetBoard = new int[TET_ROWS][TET_COLS];
        tetScore = 0;
        tetLines = 0;
        tetLevel = 1;
        tetGameOver = false;
        tetPaused = false;
        tetTickCounter = 0;
        tetNextType = 1 + tetRandom.nextInt(7);
        tetSpawnPiece();
    }

    private void tetSpawnPiece() {
        tetPieceType = tetNextType;
        tetNextType = 1 + tetRandom.nextInt(7);
        tetPiece = deepCopy(TET_PIECES[tetPieceType]);
        tetPieceX = TET_COLS / 2;
        tetPieceY = 0;
        if (!tetCanPlace(tetPiece, tetPieceX, tetPieceY)) {
            tetGameOver = true;
            submitScore("tetris", tetScore);
        }
    }

    private void tetDropStep() {
        if (tetCanPlace(tetPiece, tetPieceX, tetPieceY + 1)) {
            tetPieceY++;
        } else {
            tetLockPiece();
            tetClearLines();
            tetSpawnPiece();
        }
    }

    private void tetHardDrop() {
        while (tetCanPlace(tetPiece, tetPieceX, tetPieceY + 1)) {
            tetPieceY++;
            tetScore += 2;
        }
        tetLockPiece();
        tetClearLines();
        tetSpawnPiece();
    }

    private void tetLockPiece() {
        for (int[] block : tetPiece) {
            int r = tetPieceY + block[0];
            int c = tetPieceX + block[1];
            if (r >= 0 && r < TET_ROWS && c >= 0 && c < TET_COLS) {
                tetBoard[r][c] = tetPieceType;
            }
        }
    }

    private void tetClearLines() {
        int cleared = 0;
        for (int r = TET_ROWS - 1; r >= 0; r--) {
            boolean full = true;
            for (int c = 0; c < TET_COLS; c++) {
                if (tetBoard[r][c] == 0) { full = false; break; }
            }
            if (full) {
                // Shift everything above down
                for (int rr = r; rr > 0; rr--) {
                    System.arraycopy(tetBoard[rr - 1], 0, tetBoard[rr], 0, TET_COLS);
                }
                tetBoard[0] = new int[TET_COLS];
                cleared++;
                r++; // recheck this row since we shifted
            }
        }
        if (cleared > 0) {
            tetLines += cleared;
            // Scoring: 100, 300, 500, 800 for 1-4 lines
            int[] lineScores = {0, 100, 300, 500, 800};
            tetScore += lineScores[Math.min(cleared, 4)] * tetLevel;
            tetLevel = 1 + tetLines / 10;
        }
    }

    private boolean tetCanPlace(int[][] piece, int px, int py) {
        for (int[] block : piece) {
            int r = py + block[0];
            int c = px + block[1];
            if (r < 0 || r >= TET_ROWS || c < 0 || c >= TET_COLS) return false;
            if (tetBoard[r][c] != 0) return false;
        }
        return true;
    }

    private void tetRotate() {
        if (tetPieceType == 1) return; // O piece doesn't rotate
        int[][] rotated = new int[tetPiece.length][2];
        for (int i = 0; i < tetPiece.length; i++) {
            rotated[i][0] = tetPiece[i][1];
            rotated[i][1] = -tetPiece[i][0];
        }
        // Try basic rotation, then wall kicks
        int[] kicks = {0, -1, 1, -2, 2};
        for (int kick : kicks) {
            if (tetCanPlace(rotated, tetPieceX + kick, tetPieceY)) {
                tetPiece = rotated;
                tetPieceX += kick;
                return;
            }
        }
    }

    private static int[][] deepCopy(int[][] src) {
        int[][] dst = new int[src.length][2];
        for (int i = 0; i < src.length; i++) {
            dst[i][0] = src[i][0];
            dst[i][1] = src[i][1];
        }
        return dst;
    }

    // ========================================
    //            INPUT HANDLING
    // ========================================

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean consumed) {
        if (consumed) return super.mouseClicked(event, consumed);
        int mx = (int) event.x();
        int my = (int) event.y();
        int button = event.button();

        // Back button
        int backW = 50;
        int backH = 16;
        int backX = 8;
        int backY = (this.titleBarH - backH) / 2;
        if (mx >= backX && mx < backX + backW && my >= backY && my < backY + backH) {
            if (currentGame == -1) {
                if (this.minecraft != null) this.minecraft.setScreen(this.parent);
            } else {
                currentGame = -1;
            }
            return true;
        }

        if (currentGame == -1) return handleMenuClick(mx, my);
        else if (currentGame == 0) return handleMinesweeperClick(mx, my, button);
        else if (currentGame == 1) return handleSnakeClick(mx, my);
        else if (currentGame == 2) return handleTetrisClick(mx, my);

        return super.mouseClicked(event, consumed);
    }

    private boolean handleMenuClick(int mx, int my) {
        int cardW = 120;
        int cardH = 80;
        int gap = 16;
        int totalW = cardW * 3 + gap * 2;
        int startX = (this.width - totalW) / 2;
        int startY = (this.height - cardH) / 2;

        for (int i = 0; i < 3; i++) {
            int cx = startX + i * (cardW + gap);
            int cy = startY;
            if (mx >= cx && mx < cx + cardW && my >= cy && my < cy + cardH) {
                currentGame = i;
                if (i == 0) msNewGame();
                else if (i == 1) snNewGame();
                else if (i == 2) tetNewGame();
                return true;
            }
        }
        return false;
    }

    private boolean handleMinesweeperClick(int mx, int my, int button) {
        if (msGrid == null) return false;
        int cellSize = Math.min(18, Math.min((this.width - 40) / MS_COLS, (this.height - this.titleBarH - 70) / MS_ROWS));
        if (cellSize < 10) cellSize = 10;
        int gridW = cellSize * MS_COLS;
        int gridX = (this.width - gridW) / 2;
        int gridY = this.titleBarH + 40;

        int headerY = this.titleBarH + 6;
        int headerH = 28;
        String newGameStr = "New Game";
        int btnW = this.font.width(newGameStr) + 12;
        int btnX = gridX + (gridW - btnW) / 2;
        int btnY = headerY + (headerH - 16) / 2;
        if (mx >= btnX && mx < btnX + btnW && my >= btnY && my < btnY + 16) { msNewGame(); return true; }
        if (msGameOver) return false;

        int col = (mx - gridX) / cellSize;
        int row = (my - gridY) / cellSize;
        if (row < 0 || row >= MS_ROWS || col < 0 || col >= MS_COLS || mx < gridX || my < gridY) return false;

        if (button == 0) {
            if (msFlagged[row][col]) return true;
            if (msFirstClick) {
                msFirstClick = false;
                msStarted = true;
                msPlaceMines(row, col);
            }
            msReveal(row, col);
            return true;
        } else if (button == 1) {
            if (msRevealed[row][col]) return true;
            msFlagged[row][col] = !msFlagged[row][col];
            msFlagCount += msFlagged[row][col] ? 1 : -1;
            return true;
        }
        return false;
    }

    private boolean handleSnakeClick(int mx, int my) {
        int cellSize = Math.min(18, Math.min((this.width - 40) / SN_COLS, (this.height - this.titleBarH - 70) / SN_ROWS));
        if (cellSize < 10) cellSize = 10;
        int gridW = cellSize * SN_COLS;
        int gridX = (this.width - gridW) / 2;

        int headerY = this.titleBarH + 6;
        int headerH = 28;
        String newGameStr = "New";
        int btnW = this.font.width(newGameStr) + 12;
        int btnX = gridX + gridW - btnW - 4;
        int btnY = headerY + (headerH - 16) / 2;
        if (mx >= btnX && mx < btnX + btnW && my >= btnY && my < btnY + 16) { snNewGame(); return true; }
        return false;
    }

    private boolean handleTetrisClick(int mx, int my) {
        int cellSize = Math.min(16, (this.height - this.titleBarH - 60) / TET_ROWS);
        if (cellSize < 8) cellSize = 8;
        int gridW = cellSize * TET_COLS;
        int gridX = (this.width - gridW) / 2 - 40;

        int headerY = this.titleBarH + 6;
        int headerH = 28;
        int headerW = gridW + 100;
        String newGameStr = "New";
        int btnW = this.font.width(newGameStr) + 12;
        int btnX = gridX + (headerW - btnW) / 2;
        int btnY = headerY + (headerH - 16) / 2;
        if (mx >= btnX && mx < btnX + btnW && my >= btnY && my < btnY + 16) { tetNewGame(); return true; }
        return false;
    }

    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();

        // Escape
        if (keyCode == 256) {
            if (currentGame == -1) {
                if (this.minecraft != null) this.minecraft.setScreen(this.parent);
            } else {
                currentGame = -1;
            }
            return true;
        }

        // Snake controls
        if (currentGame == 1 && !snakeGameOver) {
            if (keyCode == 32) { snakePaused = !snakePaused; return true; }
            if (!snakePaused) {
                if (keyCode == 265 && snakeDirY != 1) { snakeNextDirX = 0; snakeNextDirY = -1; return true; }
                if (keyCode == 264 && snakeDirY != -1) { snakeNextDirX = 0; snakeNextDirY = 1; return true; }
                if (keyCode == 263 && snakeDirX != 1) { snakeNextDirX = -1; snakeNextDirY = 0; return true; }
                if (keyCode == 262 && snakeDirX != -1) { snakeNextDirX = 1; snakeNextDirY = 0; return true; }
            }
        }

        // Tetris controls
        if (currentGame == 2 && !tetGameOver && tetPiece != null) {
            // P = pause
            if (keyCode == 80) { tetPaused = !tetPaused; return true; }
            if (tetPaused) return false;

            if (keyCode == 263) { // LEFT
                if (tetCanPlace(tetPiece, tetPieceX - 1, tetPieceY)) tetPieceX--;
                return true;
            }
            if (keyCode == 262) { // RIGHT
                if (tetCanPlace(tetPiece, tetPieceX + 1, tetPieceY)) tetPieceX++;
                return true;
            }
            if (keyCode == 264) { // DOWN - soft drop
                if (tetCanPlace(tetPiece, tetPieceX, tetPieceY + 1)) {
                    tetPieceY++;
                    tetScore++;
                }
                return true;
            }
            if (keyCode == 265) { // UP - rotate
                tetRotate();
                return true;
            }
            if (keyCode == 32) { // SPACE - hard drop
                tetHardDrop();
                return true;
            }
        }

        return super.keyPressed(event);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void submitScore(String game, int score) {
        if (score <= 0) return;
        ClientPacketDistributor.sendToServer(
            (CustomPacketPayload) new ComputerActionPayload("minigame_submit_score", game + ":" + score),
            (CustomPacketPayload[]) new CustomPacketPayload[0]);
    }
}

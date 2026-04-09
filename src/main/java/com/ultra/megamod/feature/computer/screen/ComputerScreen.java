package com.ultra.megamod.feature.computer.screen;

import com.ultra.megamod.feature.computer.network.ComputerActionPayload;
import com.ultra.megamod.feature.economy.screen.BankScreen;
import com.ultra.megamod.feature.economy.screen.ShopScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class ComputerScreen extends Screen {
    private final boolean isAdmin;
    private int wallet;
    private int bank;
    private int hoverTicks = 0;
    private int hoveredApp = -1;
    private int animTicks = 0;

    // ─── App registry ─── grouped by category across rows
    // Row 1: Economy & Progress | Row 2: Knowledge & Media | Row 3: Social
    // Row 4: Activities         | Row 5: Configuration
    private static final String[] APP_NAMES = {
        "Shop", "Bank", "Market", "Stats", "Skills",           // Economy & Progress
        "Recipes", "Wiki", "Map", "Music", "Notes",            // Knowledge & Media
        "Friends", "Mail", "Party", "Ranks", "Colony",          // Social & Community
        "Arena", "Casino", "Games", "Challenges", "Bounties",  // Activities
        "Quests", "Settings", "Customize", "Admin"              // Progression & Configuration
    };
    private static final String[] APP_DESCS = {
        "Daily rotating item shop", "Deposit & withdraw MegaCoins",
        "Buy & sell with players", "Your gameplay statistics", "Level up 5 skill trees",
        "Browse crafting recipes", "Discover the world's secrets", "World map & waypoints",
        "Play your music collection", "Your personal notepad",
        "Manage your friends", "Send mail & items", "Create & manage parties",
        "Server-wide leaderboards", "Overview of your colony",
        "PvE waves & PvP combat arena", "Try your luck at the casino",
        "Minesweeper, Snake & more", "Weekly skill challenges", "Hunt mobs for rewards",
        "Guided progression through MegaMod",
        "Personal preferences", "Badge, name color & mastery marks",
        "Server administration"
    };
    // Each app's accent color — carefully chosen palette, no adjacent duplicates
    private static final int[] APP_ACCENT = {
        0xFFE8A838, // Shop       - warm gold
        0xFF4CAF50, // Bank       - money green
        0xFF00ACC1, // Market     - teal
        0xFF78909C, // Stats      - slate grey
        0xFFFFC107, // Skills     - amber
        0xFFFF7043, // Recipes    - warm orange
        0xFFAB47BC, // Wiki       - purple
        0xFF8D6E63, // Map        - earth brown
        0xFFEF5350, // Music      - vibrant red
        0xFF42A5F5, // Notes      - sky blue
        0xFF26C6DA, // Friends    - cyan
        0xFFEC407A, // Mail       - pink
        0xFF9B59B6, // Party      - purple
        0xFFFFCA28, // Ranks      - bright gold
        0xFF2E7D32, // Colony     - forest green
        0xFFE53935, // Arena      - arena red
        0xFFD4AF37, // Casino     - casino gold
        0xFF66BB6A, // Games      - fresh green
        0xFF00BCD4, // Challenges - teal
        0xFFFFB300, // Bounties   - amber
        0xFF58A6FF, // Quests     - quest blue
        0xFF607D8B, // Settings   - blue grey
        0xFFD4A0FF, // Customize  - lavender
        0xFFFF5722, // Admin      - deep orange
    };

    // Layout
    private static final int CARD_W = 56;
    private static final int CARD_H = 62;
    private static final int CARD_GAP = 10;
    private static final int ICON_SZ = 12; // pixel icon size
    private int titleBarH, statusBarH;
    private int[][] cardBounds;
    private int appCount;

    public ComputerScreen(boolean isAdmin, int wallet, int bank) {
        super(Component.literal("MegaMod Computer"));
        this.isAdmin = isAdmin;
        this.wallet = wallet;
        this.bank = bank;
    }

    protected void init() {
        super.init();
        this.titleBarH = 24;
        this.statusBarH = 18;
        this.appCount = this.isAdmin ? 24 : 23;

        // 5 columns, 3 rows (15 apps max)
        int cols = 5;
        int rows = (appCount + cols - 1) / cols;
        int gridW = cols * (CARD_W + CARD_GAP) - CARD_GAP;
        int gridH = rows * (CARD_H + CARD_GAP) - CARD_GAP;
        int contentTop = titleBarH + 8;
        int contentBottom = this.height - statusBarH - 8;
        int startX = (this.width - gridW) / 2;
        int startY = contentTop + Math.max(0, (contentBottom - contentTop - gridH) / 2);

        this.cardBounds = new int[appCount][4];
        for (int i = 0; i < appCount; i++) {
            int col = i % cols;
            int row = i / cols;
            this.cardBounds[i] = new int[]{
                startX + col * (CARD_W + CARD_GAP),
                startY + row * (CARD_H + CARD_GAP),
                CARD_W, CARD_H
            };
        }
    }

    public void tick() {
        super.tick();
        hoverTicks++;
        animTicks++;
    }

    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // ─── Background — dark desktop with subtle noise ───
        g.fill(0, 0, this.width, this.height, 0xFF12121A);
        // Outer frame (thin bright border → dark inset → content)
        g.fill(0, 0, this.width, 1, 0xFF2A2A3A);                         // top edge
        g.fill(0, this.height - 1, this.width, this.height, 0xFF0A0A10); // bottom edge
        g.fill(0, 0, 1, this.height, 0xFF2A2A3A);                        // left edge
        g.fill(this.width - 1, 0, this.width, this.height, 0xFF0A0A10);  // right edge
        // Subtle vertical gradient on content area (lighter center, darker edges)
        int contentH = this.height - titleBarH - statusBarH;
        for (int stripe = 0; stripe < 3; stripe++) {
            int sy2 = titleBarH + contentH / 3 * stripe;
            int alpha = stripe == 1 ? 0x08 : 0x04;
            g.fill(1, sy2, this.width - 1, sy2 + contentH / 3, (alpha << 24) | 0xFFFFFF);
        }

        // ─── Title bar — sleek dark header ───
        g.fill(0, 0, this.width, titleBarH, 0xFF1A1A28);
        g.fill(0, titleBarH - 1, this.width, titleBarH, 0xFF2A2A3A);     // bottom accent line
        g.fill(0, titleBarH, this.width, titleBarH + 1, 0xFF08080E);     // shadow below
        // Subtle gradient on title bar
        g.fill(1, 1, this.width - 1, 2, 0x10FFFFFF);
        String playerName = Minecraft.getInstance().player != null
            ? Minecraft.getInstance().player.getGameProfile().name() : "Unknown";
        int ty = (titleBarH - 9) / 2;
        // Player name with status dot
        g.fill(8, ty + 1, 11, ty + 4, 0xFF4CAF50); // green online dot
        g.drawString(this.font, playerName, 14, ty, 0xFFCCCCDD, false);
        // Title centered
        String title = "MegaMod OS";
        int titleW = this.font.width(title);
        g.drawString(this.font, title, (this.width - titleW) / 2, ty, 0xFF888899, false);
        // Wallet on right with coin icon
        String coinStr = fmtCoins(this.wallet) + " MC";
        int coinW = this.font.width(coinStr);
        g.fill(this.width - coinW - 18, ty + 1, this.width - coinW - 15, ty + 4, 0xFFE8A838); // gold dot
        g.drawString(this.font, coinStr, this.width - coinW - 10, ty, 0xFFE8A838, false);

        // ─── App cards ───
        int newHovered = -1;
        for (int i = 0; i < appCount; i++) {
            int[] b = cardBounds[i];
            boolean hov = mouseX >= b[0] && mouseX < b[0] + b[2] && mouseY >= b[1] && mouseY < b[1] + b[3];
            if (hov) newHovered = i;
            drawAppCard(g, b[0], b[1], b[2], b[3], i, hov);
        }

        // ─── Hover tracking + tooltip ───
        if (newHovered != hoveredApp) {
            hoveredApp = newHovered;
            hoverTicks = 0;
        }
        if (hoveredApp >= 0 && hoverTicks > 8 && hoveredApp < appCount) {
            String desc = APP_DESCS[hoveredApp];
            int tw = this.font.width(desc) + 10;
            int th = 15;
            int[] b = cardBounds[hoveredApp];
            int tx = b[0] + b[2] / 2 - tw / 2;
            int tty = b[1] + b[3] + 3;
            tx = Math.max(4, Math.min(tx, this.width - tw - 4));
            if (tty + th > this.height - statusBarH - 2) tty = b[1] - th - 3;
            // Dark tooltip
            g.fill(tx - 1, tty - 1, tx + tw + 1, tty + th + 1, 0xFF3A3A4A); // border
            g.fill(tx, tty, tx + tw, tty + th, 0xF0181822);                   // bg
            g.fill(tx, tty, tx + tw, tty + 1, 0xFF4A4A5A);                    // top highlight
            g.drawString(this.font, desc, tx + 5, tty + 3, 0xFFDDDDEE, false);
        }

        // ─── Status bar — matching dark footer ───
        int sy = this.height - statusBarH;
        g.fill(0, sy, this.width, this.height, 0xFF1A1A28);
        g.fill(0, sy, this.width, sy + 1, 0xFF08080E);       // shadow above
        g.fill(0, sy + 1, this.width, sy + 2, 0xFF2A2A3A);   // accent line
        int sty = sy + (statusBarH - 9) / 2 + 1;
        g.drawString(this.font, playerName, 10, sty, 0xFF777788, false);
        String bankStr = "Bank: " + fmtCoins(this.bank) + " MC";
        int bw = this.font.width(bankStr);
        g.drawString(this.font, bankStr, this.width - bw - 10, sty, 0xFF4CAF50, false);
        String appLabel = appCount + " apps available";
        int alw = this.font.width(appLabel);
        g.drawString(this.font, appLabel, (this.width - alw) / 2, sty, 0xFF555566, false);

        super.render(g, mouseX, mouseY, partialTick);
    }

    // ─── Custom card rendering with pixel icon ───
    private void drawAppCard(GuiGraphics g, int x, int y, int w, int h, int appIdx, boolean hov) {
        int accent = APP_ACCENT[appIdx];

        // Card body
        int bg = hov ? 0xFF2A2A36 : 0xFF1C1C26;
        int border = hov ? mix(accent, 0xFFFFFFFF, 0.4f) : 0xFF3A3A48;
        // Outer border
        g.fill(x, y, x + w, y + h, border);
        // Inner fill
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, bg);
        // Top accent stripe (3px)
        int stripeColor = hov ? accent : mix(accent, 0xFF000000, 0.5f);
        g.fill(x + 1, y + 1, x + w - 1, y + 4, stripeColor);
        // Subtle inner highlight on hover
        if (hov) {
            g.fill(x + 1, y + 4, x + w - 1, y + 5, mix(accent, bg, 0.3f));
            // Glow effect — translucent accent color around card
            g.fill(x - 1, y - 1, x + w + 1, y, (accent & 0x00FFFFFF) | 0x30000000);
            g.fill(x - 1, y + h, x + w + 1, y + h + 1, (accent & 0x00FFFFFF) | 0x30000000);
            g.fill(x - 1, y, x, y + h, (accent & 0x00FFFFFF) | 0x30000000);
            g.fill(x + w, y, x + w + 1, y + h, (accent & 0x00FFFFFF) | 0x30000000);
        }

        // ─── Pixel icon (drawn in center of card) ───
        int iconX = x + (w - ICON_SZ) / 2;
        int iconY = y + 12;
        int ic = hov ? brighten(accent, 30) : accent;
        int id = darken(accent, 60);
        drawPixelIcon(g, iconX, iconY, appIdx, ic, id, hov);

        // ─── Label ───
        String name = APP_NAMES[appIdx];
        int nw = this.font.width(name);
        int nx = x + (w - nw) / 2;
        int ny = y + h - 14;
        int nc = hov ? 0xFFFFFFFF : 0xFFBBBBCC;
        g.drawString(this.font, name, nx, ny, nc, false);
    }

    // ─── Hand-drawn pixel icons (12x12 each) ───
    private void drawPixelIcon(GuiGraphics g, int x, int y, int app, int c, int d, boolean hov) {
        switch (app) {
            case 0  -> drawIconShop(g, x, y, c, d);       // Shop
            case 1  -> drawIconBank(g, x, y, c, d);       // Bank
            case 2  -> drawIconMarket(g, x, y, c, d);     // Market
            case 3  -> drawIconStats(g, x, y, c, d);      // Stats
            case 4  -> drawIconSkills(g, x, y, c, d);     // Skills
            case 5  -> drawIconRecipes(g, x, y, c, d);    // Recipes
            case 6  -> drawIconBook(g, x, y, c, d);       // Wiki
            case 7  -> drawIconMap(g, x, y, c, d);        // Map
            case 8  -> drawIconMusic(g, x, y, c, d);      // Music
            case 9  -> drawIconNotes(g, x, y, c, d);      // Notes
            case 10 -> drawIconFriends(g, x, y, c, d);    // Friends
            case 11 -> drawIconMail(g, x, y, c, d);       // Mail
            case 12 -> drawIconParty(g, x, y, c, d);      // Party
            case 13 -> drawIconTrophy(g, x, y, c, d);     // Ranks
            case 14 -> drawIconColony(g, x, y, c, d);     // Colony
            case 15 -> drawIconArena(g, x, y, c, d);      // Arena
            case 16 -> drawIconCasino(g, x, y, c, d);     // Casino
            case 17 -> drawIconGames(g, x, y, c, d);      // Games
            case 18 -> drawIconChallenges(g, x, y, c, d); // Challenges
            case 19 -> drawIconBounty(g, x, y, c, d);     // Bounties
            case 20 -> drawIconQuests(g, x, y, c, d);     // Quests
            case 21 -> drawIconSettings(g, x, y, c, d);   // Settings
            case 22 -> drawIconCustomize(g, x, y, c, d);  // Customize
            case 23 -> drawIconAdmin(g, x, y, c, d);      // Admin
        }
    }

    // Shop — coin/diamond
    private void drawIconShop(GuiGraphics g, int x, int y, int c, int d) {
        g.fill(x + 4, y, x + 8, y + 1, c);            // top
        g.fill(x + 2, y + 1, x + 10, y + 2, c);
        g.fill(x + 1, y + 2, x + 11, y + 4, c);       // wide middle
        g.fill(x, y + 4, x + 12, y + 8, c);            // widest
        g.fill(x + 1, y + 8, x + 11, y + 10, c);
        g.fill(x + 2, y + 10, x + 10, y + 11, c);
        g.fill(x + 4, y + 11, x + 8, y + 12, c);      // bottom
        // inner $ symbol
        g.fill(x + 5, y + 3, x + 7, y + 4, d);
        g.fill(x + 4, y + 4, x + 5, y + 5, d);
        g.fill(x + 5, y + 5, x + 7, y + 6, d);
        g.fill(x + 7, y + 6, x + 8, y + 7, d);
        g.fill(x + 5, y + 7, x + 7, y + 8, d);
        g.fill(x + 5, y + 8, x + 7, y + 9, d);
    }

    // Bank — vault door
    private void drawIconBank(GuiGraphics g, int x, int y, int c, int d) {
        g.fill(x + 1, y, x + 11, y + 12, c);           // body
        g.fill(x + 2, y + 1, x + 10, y + 11, d);       // inner
        g.fill(x, y, x + 12, y + 1, brighten(c, 30));   // top edge
        // vault circle
        g.fill(x + 4, y + 3, x + 8, y + 4, c);
        g.fill(x + 3, y + 4, x + 4, y + 8, c);
        g.fill(x + 8, y + 4, x + 9, y + 8, c);
        g.fill(x + 4, y + 8, x + 8, y + 9, c);
        // handle
        g.fill(x + 5, y + 5, x + 7, y + 7, c);
        g.fill(x + 9, y + 5, x + 10, y + 7, c);
    }

    // Stats — bar chart
    private void drawIconStats(GuiGraphics g, int x, int y, int c, int d) {
        g.fill(x, y + 11, x + 12, y + 12, c);          // baseline
        g.fill(x + 1, y + 7, x + 3, y + 11, c);        // bar1
        g.fill(x + 4, y + 4, x + 6, y + 11, c);        // bar2
        g.fill(x + 7, y + 2, x + 9, y + 11, c);        // bar3
        g.fill(x + 10, y + 5, x + 12, y + 11, c);      // bar4
        // highlights on top of bars
        g.fill(x + 1, y + 7, x + 3, y + 8, brighten(c, 40));
        g.fill(x + 4, y + 4, x + 6, y + 5, brighten(c, 40));
        g.fill(x + 7, y + 2, x + 9, y + 3, brighten(c, 40));
    }

    // Skills — star
    private void drawIconSkills(GuiGraphics g, int x, int y, int c, int d) {
        g.fill(x + 5, y, x + 7, y + 2, c);             // top point
        g.fill(x + 4, y + 2, x + 8, y + 4, c);
        g.fill(x, y + 4, x + 12, y + 6, c);            // wide arms
        g.fill(x + 2, y + 6, x + 10, y + 8, c);
        g.fill(x + 1, y + 8, x + 4, y + 10, c);        // left leg
        g.fill(x + 8, y + 8, x + 11, y + 10, c);       // right leg
        g.fill(x + 0, y + 10, x + 3, y + 12, c);
        g.fill(x + 9, y + 10, x + 12, y + 12, c);
        // inner highlight
        g.fill(x + 5, y + 4, x + 7, y + 6, brighten(c, 50));
    }

    // Recipes — crafting grid
    private void drawIconRecipes(GuiGraphics g, int x, int y, int c, int d) {
        g.fill(x, y, x + 12, y + 12, d);                // bg
        // 3x3 grid cells
        for (int r = 0; r < 3; r++) {
            for (int col = 0; col < 3; col++) {
                int cx = x + col * 4;
                int cy = y + r * 4;
                g.fill(cx, cy, cx + 3, cy + 3, c);
                g.fill(cx, cy, cx + 3, cy + 1, brighten(c, 30));
            }
        }
    }

    // Wiki — open book
    private void drawIconBook(GuiGraphics g, int x, int y, int c, int d) {
        g.fill(x, y + 1, x + 5, y + 11, c);            // left page
        g.fill(x + 7, y + 1, x + 12, y + 11, c);       // right page
        g.fill(x + 5, y, x + 7, y + 12, d);            // spine
        // text lines left
        g.fill(x + 1, y + 3, x + 4, y + 4, d);
        g.fill(x + 1, y + 5, x + 4, y + 6, d);
        g.fill(x + 1, y + 7, x + 3, y + 8, d);
        // text lines right
        g.fill(x + 8, y + 3, x + 11, y + 4, d);
        g.fill(x + 8, y + 5, x + 11, y + 6, d);
        g.fill(x + 8, y + 7, x + 10, y + 8, d);
    }

    // Music — note
    private void drawIconMusic(GuiGraphics g, int x, int y, int c, int d) {
        g.fill(x + 4, y, x + 5, y + 9, c);             // stem1
        g.fill(x + 9, y + 2, x + 10, y + 9, c);        // stem2
        g.fill(x + 5, y, x + 10, y + 1, c);            // beam top
        g.fill(x + 5, y + 2, x + 10, y + 3, c);        // beam bottom
        // note heads
        g.fill(x + 2, y + 8, x + 5, y + 10, c);
        g.fill(x + 1, y + 9, x + 6, y + 11, c);
        g.fill(x + 7, y + 8, x + 10, y + 10, c);
        g.fill(x + 6, y + 9, x + 11, y + 11, c);
    }

    // Games — dice
    private void drawIconGames(GuiGraphics g, int x, int y, int c, int d) {
        g.fill(x + 1, y + 1, x + 11, y + 11, c);       // body
        g.fill(x, y, x + 12, y + 1, brighten(c, 30));   // top edge
        g.fill(x, y, x + 1, y + 12, brighten(c, 20));
        g.fill(x + 11, y, x + 12, y + 12, darken(c, 30));
        g.fill(x, y + 11, x + 12, y + 12, darken(c, 30));
        // dots (6 face: 2-1-2-1)
        g.fill(x + 3, y + 3, x + 5, y + 5, d);
        g.fill(x + 7, y + 3, x + 9, y + 5, d);
        g.fill(x + 5, y + 5, x + 7, y + 7, d);
        g.fill(x + 3, y + 7, x + 5, y + 9, d);
        g.fill(x + 7, y + 7, x + 9, y + 9, d);
    }

    // Notes — pencil
    private void drawIconNotes(GuiGraphics g, int x, int y, int c, int d) {
        // pencil body (diagonal)
        g.fill(x + 8, y, x + 10, y + 2, c);
        g.fill(x + 7, y + 1, x + 9, y + 3, c);
        g.fill(x + 6, y + 2, x + 8, y + 4, c);
        g.fill(x + 5, y + 3, x + 7, y + 5, c);
        g.fill(x + 4, y + 4, x + 6, y + 6, c);
        g.fill(x + 3, y + 5, x + 5, y + 7, c);
        g.fill(x + 2, y + 6, x + 4, y + 8, c);
        g.fill(x + 1, y + 7, x + 3, y + 9, c);
        // tip
        g.fill(x, y + 9, x + 2, y + 10, d);
        g.fill(x, y + 10, x + 1, y + 11, d);
        // eraser
        g.fill(x + 9, y, x + 11, y + 1, 0xFFFF8A80);
        // paper bg
        g.fill(x + 1, y + 3, x + 4, y + 12, 0xFF555566);
        g.fill(x + 2, y + 5, x + 3, y + 6, 0xFF777788);
        g.fill(x + 2, y + 7, x + 3, y + 8, 0xFF777788);
    }

    // Map — compass
    private void drawIconMap(GuiGraphics g, int x, int y, int c, int d) {
        // compass circle
        g.fill(x + 3, y, x + 9, y + 1, c);
        g.fill(x + 1, y + 1, x + 11, y + 3, c);
        g.fill(x, y + 3, x + 12, y + 9, c);
        g.fill(x + 1, y + 9, x + 11, y + 11, c);
        g.fill(x + 3, y + 11, x + 9, y + 12, c);
        // inner
        g.fill(x + 2, y + 2, x + 10, y + 10, d);
        // needle (N=red, S=white)
        g.fill(x + 5, y + 2, x + 7, y + 6, 0xFFEF5350);  // north red
        g.fill(x + 5, y + 6, x + 7, y + 10, 0xFFEEEEEE);  // south white
        g.fill(x + 5, y + 5, x + 7, y + 7, 0xFFFFFFFF);   // center
    }

    // Friends — two people
    private void drawIconFriends(GuiGraphics g, int x, int y, int c, int d) {
        // person 1 (left)
        g.fill(x + 2, y + 1, x + 5, y + 4, c);         // head
        g.fill(x + 1, y + 4, x + 6, y + 8, c);         // body
        g.fill(x + 1, y + 8, x + 3, y + 11, c);        // left leg
        g.fill(x + 4, y + 8, x + 6, y + 11, c);        // right leg
        // person 2 (right, slightly offset)
        g.fill(x + 7, y + 2, x + 10, y + 5, c);
        g.fill(x + 6, y + 5, x + 11, y + 9, c);
        g.fill(x + 6, y + 9, x + 8, y + 12, c);
        g.fill(x + 9, y + 9, x + 11, y + 12, c);
    }

    // Ranks — trophy
    private void drawIconTrophy(GuiGraphics g, int x, int y, int c, int d) {
        g.fill(x + 3, y, x + 9, y + 1, c);             // rim
        g.fill(x + 2, y + 1, x + 10, y + 6, c);        // cup
        g.fill(x + 3, y + 6, x + 9, y + 7, c);
        g.fill(x + 4, y + 7, x + 8, y + 8, c);
        g.fill(x + 5, y + 8, x + 7, y + 10, c);        // stem
        g.fill(x + 3, y + 10, x + 9, y + 12, c);       // base
        // handles
        g.fill(x, y + 2, x + 2, y + 5, c);
        g.fill(x + 10, y + 2, x + 12, y + 5, c);
        // inner shine
        g.fill(x + 4, y + 2, x + 5, y + 4, brighten(c, 60));
    }

    // Mail — letter with seal
    private void drawIconMail(GuiGraphics g, int x, int y, int c, int d) {
        g.fill(x, y + 3, x + 12, y + 11, c);           // envelope
        g.fill(x + 1, y + 4, x + 11, y + 10, d);       // inner
        // V flap
        g.fill(x, y + 3, x + 2, y + 4, c);
        g.fill(x + 2, y + 4, x + 4, y + 6, c);
        g.fill(x + 4, y + 5, x + 6, y + 7, c);
        g.fill(x + 6, y + 5, x + 8, y + 6, c);
        g.fill(x + 8, y + 4, x + 10, y + 6, c);
        g.fill(x + 10, y + 3, x + 12, y + 4, c);
        // wax seal (red circle)
        g.fill(x + 4, y + 7, x + 8, y + 8, 0xFFEF5350);
        g.fill(x + 3, y + 8, x + 9, y + 9, 0xFFEF5350);
        g.fill(x + 4, y + 9, x + 8, y + 10, 0xFFEF5350);
    }

    // Colony — cluster of buildings (village silhouette)
    private void drawIconColony(GuiGraphics g, int x, int y, int c, int d) {
        // Left small house
        g.fill(x, y + 6, x + 4, y + 11, c);
        g.fill(x + 1, y + 7, x + 3, y + 10, d);
        g.fill(x + 1, y + 4, x + 3, y + 6, c); // roof
        // Center tall building (town hall)
        g.fill(x + 4, y + 2, x + 9, y + 11, c);
        g.fill(x + 5, y + 3, x + 8, y + 10, d);
        g.fill(x + 5, y, x + 8, y + 2, c); // peaked roof
        g.fill(x + 6, y + 4, x + 7, y + 6, brighten(c, 40)); // window
        g.fill(x + 6, y + 7, x + 7, y + 11, c); // door
        // Right medium house
        g.fill(x + 9, y + 5, x + 12, y + 11, c);
        g.fill(x + 10, y + 6, x + 11, y + 10, d);
        g.fill(x + 9, y + 3, x + 12, y + 5, c); // roof
        // Foundation
        g.fill(x, y + 11, x + 12, y + 12, c);
        // Flag on town hall
        g.fill(x + 7, y, x + 8, y + 1, brighten(c, 60));
    }

    // Casino — dice
    private void drawIconCasino(GuiGraphics g, int x, int y, int c, int d) {
        g.fill(x + 1, y + 1, x + 11, y + 11, c);              // dice body
        g.fill(x + 2, y + 2, x + 10, y + 10, d);              // inner
        // pips (showing 5)
        g.fill(x + 3, y + 3, x + 5, y + 5, c);                // top-left
        g.fill(x + 7, y + 3, x + 9, y + 5, c);                // top-right
        g.fill(x + 5, y + 5, x + 7, y + 7, c);                // center
        g.fill(x + 3, y + 7, x + 5, y + 9, c);                // bottom-left
        g.fill(x + 7, y + 7, x + 9, y + 9, c);                // bottom-right
    }

    // Challenges — trophy
    private void drawIconChallenges(GuiGraphics g, int x, int y, int c, int d) {
        g.fill(x + 3, y + 0, x + 9, y + 1, c);               // rim
        g.fill(x + 2, y + 1, x + 10, y + 5, c);              // cup body
        g.fill(x + 3, y + 5, x + 9, y + 6, c);               // cup lower
        g.fill(x + 4, y + 6, x + 8, y + 7, c);               // cup taper
        g.fill(x + 5, y + 7, x + 7, y + 9, c);               // stem
        g.fill(x + 3, y + 9, x + 9, y + 10, c);              // base
        // inner cup
        g.fill(x + 3, y + 2, x + 9, y + 4, d);
        // star on cup
        g.fill(x + 5, y + 2, x + 7, y + 4, c);
        // handles
        g.fill(x + 1, y + 2, x + 2, y + 4, c);
        g.fill(x + 10, y + 2, x + 11, y + 4, c);
    }

    // Arena — crossed swords
    private void drawIconArena(GuiGraphics g, int x, int y, int c, int d) {
        // Left sword (diagonal)
        g.fill(x + 1, y + 1, x + 3, y + 3, c);
        g.fill(x + 3, y + 3, x + 5, y + 5, c);
        g.fill(x + 5, y + 5, x + 7, y + 7, c);
        // Right sword (diagonal)
        g.fill(x + 9, y + 1, x + 11, y + 3, c);
        g.fill(x + 7, y + 3, x + 9, y + 5, c);
        g.fill(x + 5, y + 5, x + 7, y + 7, c);
        // Cross point
        g.fill(x + 4, y + 4, x + 8, y + 8, c);
        g.fill(x + 5, y + 5, x + 7, y + 7, d);
        // Handles
        g.fill(x + 0, y + 9, x + 4, y + 11, c);
        g.fill(x + 8, y + 9, x + 12, y + 11, c);
    }

    // Admin — shield with star
    private void drawIconAdmin(GuiGraphics g, int x, int y, int c, int d) {
        // Shield outline
        g.fill(x + 2, y, x + 10, y + 1, c);                  // top edge
        g.fill(x + 1, y + 1, x + 11, y + 7, c);              // body
        g.fill(x + 2, y + 7, x + 10, y + 9, c);              // lower
        g.fill(x + 3, y + 9, x + 9, y + 10, c);              // taper
        g.fill(x + 4, y + 10, x + 8, y + 11, c);             // point upper
        g.fill(x + 5, y + 11, x + 7, y + 12, c);             // point tip
        // Inner fill
        g.fill(x + 2, y + 1, x + 10, y + 7, d);
        g.fill(x + 3, y + 7, x + 9, y + 9, d);
        g.fill(x + 4, y + 9, x + 8, y + 10, d);
        // Star/cross emblem
        g.fill(x + 5, y + 2, x + 7, y + 8, c);               // vertical
        g.fill(x + 3, y + 4, x + 9, y + 6, c);               // horizontal
    }

    // Customize — paint palette
    private void drawIconCustomize(GuiGraphics g, int x, int y, int c, int d) {
        // Palette body (rounded blob)
        g.fill(x + 2, y + 1, x + 10, y + 2, c);
        g.fill(x + 1, y + 2, x + 11, y + 9, c);
        g.fill(x + 2, y + 9, x + 10, y + 11, c);
        g.fill(x + 3, y + 11, x + 8, y + 12, c);
        // Thumb hole
        g.fill(x + 3, y + 7, x + 5, y + 9, d);
        // Color dots on palette
        g.fill(x + 3, y + 3, x + 5, y + 5, 0xFFEF5350);  // red
        g.fill(x + 6, y + 2, x + 8, y + 4, 0xFF42A5F5);  // blue
        g.fill(x + 8, y + 4, x + 10, y + 6, 0xFFFFC107); // yellow
        g.fill(x + 6, y + 5, x + 8, y + 7, 0xFF66BB6A);  // green
        g.fill(x + 3, y + 5, x + 5, y + 7, 0xFFAB47BC);  // purple
    }

    // Party — two people
    private void drawIconParty(GuiGraphics g, int x, int y, int c, int d) {
        // Person 1 (left)
        g.fill(x + 2, y + 1, x + 5, y + 4, c);             // head
        g.fill(x + 1, y + 5, x + 6, y + 9, c);             // body
        g.fill(x + 2, y + 5, x + 5, y + 6, d);             // collar
        // Person 2 (right)
        g.fill(x + 7, y + 1, x + 10, y + 4, c);            // head
        g.fill(x + 6, y + 5, x + 11, y + 9, c);            // body
        g.fill(x + 7, y + 5, x + 10, y + 6, d);            // collar
        // Connection (link)
        g.fill(x + 4, y + 9, x + 8, y + 11, c);
        g.fill(x + 5, y + 10, x + 7, y + 12, brighten(c, 30));
    }

    // Settings — sliders
    private void drawIconSettings(GuiGraphics g, int x, int y, int c, int d) {
        // Three horizontal slider tracks
        g.fill(x + 1, y + 2, x + 11, y + 3, d);
        g.fill(x + 1, y + 5, x + 11, y + 6, d);
        g.fill(x + 1, y + 8, x + 11, y + 9, d);
        // Slider knobs at different positions
        g.fill(x + 3, y + 1, x + 5, y + 4, c);
        g.fill(x + 7, y + 4, x + 9, y + 7, c);
        g.fill(x + 5, y + 7, x + 7, y + 10, c);
        // Highlights
        g.fill(x + 3, y + 1, x + 5, y + 2, brighten(c, 40));
        g.fill(x + 7, y + 4, x + 9, y + 5, brighten(c, 40));
    }

    // Market — storefront / price tag
    private void drawIconMarket(GuiGraphics g, int x, int y, int c, int d) {
        // Right arrow (top)
        g.fill(x + 2, y + 2, x + 8, y + 4, c);
        g.fill(x + 8, y + 1, x + 10, y + 5, c);
        g.fill(x + 10, y + 2, x + 11, y + 4, c);
        // Left arrow (bottom)
        g.fill(x + 4, y + 7, x + 10, y + 9, c);
        g.fill(x + 2, y + 6, x + 4, y + 10, c);
        g.fill(x + 1, y + 7, x + 2, y + 9, c);
        // Coin in center
        g.fill(x + 5, y + 4, x + 7, y + 7, brighten(c, 40));
    }

    // Bounty — wanted poster / scroll
    private void drawIconBounty(GuiGraphics g, int x, int y, int c, int d) {
        // Scroll body
        g.fill(x + 2, y + 1, x + 10, y + 11, c);
        g.fill(x + 3, y + 2, x + 9, y + 10, d);
        // Scroll rolls (top/bottom)
        g.fill(x + 1, y, x + 11, y + 2, c);
        g.fill(x + 1, y + 10, x + 11, y + 12, c);
        // Text lines
        g.fill(x + 4, y + 3, x + 8, y + 4, c);
        g.fill(x + 4, y + 5, x + 7, y + 6, c);
        g.fill(x + 4, y + 7, x + 8, y + 8, c);
        // Highlight
        g.fill(x + 1, y, x + 11, y + 1, brighten(c, 30));
    }

    // Quests — scroll with checkmark
    private void drawIconQuests(GuiGraphics g, int x, int y, int c, int d) {
        // Scroll body
        g.fill(x + 2, y + 1, x + 10, y + 11, c);
        g.fill(x + 3, y + 2, x + 9, y + 10, d);
        // Scroll rolls
        g.fill(x + 1, y, x + 11, y + 2, c);
        g.fill(x + 1, y + 10, x + 11, y + 12, c);
        // Checkmark
        g.fill(x + 4, y + 6, x + 5, y + 7, c);
        g.fill(x + 5, y + 7, x + 6, y + 8, c);
        g.fill(x + 6, y + 6, x + 7, y + 7, c);
        g.fill(x + 7, y + 5, x + 8, y + 6, c);
        g.fill(x + 8, y + 4, x + 9, y + 5, c);
        // Progress line
        g.fill(x + 4, y + 3, x + 8, y + 4, c);
        // Highlight
        g.fill(x + 1, y, x + 11, y + 1, brighten(c, 30));
    }

    // ─── Input ───

    public boolean mouseClicked(MouseButtonEvent event, boolean consumed) {
        if (consumed) return super.mouseClicked(event, consumed);
        int mx = (int) event.x();
        int my = (int) event.y();
        for (int i = 0; i < appCount; i++) {
            int[] b = cardBounds[i];
            if (mx >= b[0] && mx < b[0] + b[2] && my >= b[1] && my < b[1] + b[3]) {
                onAppClicked(i);
                return true;
            }
        }
        return super.mouseClicked(event, consumed);
    }

    private void onAppClicked(int index) {
        Minecraft mc = Minecraft.getInstance();
        switch (index) {
            case 0 -> mc.setScreen(new ShopScreen(this, this.wallet, this.bank)); // Shop
            case 1  -> mc.setScreen(new BankScreen(this, this.wallet, this.bank));       // Bank
            case 2  -> mc.setScreen(new MarketplaceScreen(this));                         // Market
            case 3 -> mc.setScreen(new StatsScreen(this, this.wallet, this.bank)); // Stats
            case 4  -> mc.setScreen(new SkillsAppScreen(this));                           // Skills
            case 5  -> mc.setScreen(new RecipeBrowserScreen(this));                       // Recipes
            case 6  -> mc.setScreen(new EncyclopediaScreen(this));                        // Wiki
            case 7  -> mc.setScreen(new MapScreen(this));                                 // Map
            case 8  -> mc.setScreen(new MusicPlayerScreen(this));                         // Music
            case 9  -> mc.setScreen(new NotesScreen(this));                               // Notes
            case 10 -> mc.setScreen(new FriendsScreen(this));                             // Friends
            case 11 -> mc.setScreen(new MailScreen(this));                                // Mail
            case 12 -> mc.setScreen(new PartyScreen(this));                               // Party
            case 13 -> mc.setScreen(new LeaderboardScreen(this));                         // Ranks
            case 14 -> { // Colony — open Town Hall window via BlockUI
                var townHall = new com.ultra.megamod.feature.citizen.screen.townhall.WindowMainPage(null);
                mc.setScreen(new com.ultra.megamod.feature.citizen.blockui.BOScreen(townHall));
            }
            case 15 -> mc.setScreen(new com.ultra.megamod.feature.computer.screen.ArenaScreen(this)); // Arena
            case 16 -> mc.setScreen(new com.ultra.megamod.feature.casino.screen.CasinoScreen(this)); // Casino
            case 17 -> mc.setScreen(new MinigamesScreen(this));                           // Games
            case 18 -> mc.setScreen(new ChallengesScreen(this));                          // Challenges
            case 19 -> mc.setScreen(new BountyBoardScreen(this));                         // Bounties
            case 20 -> mc.setScreen(new QuestsScreen(this));                              // Quests
            case 21 -> mc.setScreen(new SettingsScreen(this));                            // Settings
            case 22 -> mc.setScreen(new CustomizeScreen(this));                           // Customize
            case 23 -> mc.setScreen(new AdminTerminalScreen(this));                       // Admin
        }
    }

    public boolean isPauseScreen() { return false; }
    public int getWallet() { return this.wallet; }
    public int getBank() { return this.bank; }

    // ─── Helpers ───

    private static String fmtCoins(int n) {
        if (n >= 1_000_000) return String.format("%.1fM", n / 1_000_000.0);
        if (n >= 1_000) return String.format("%,d", n);
        return String.valueOf(n);
    }

    private static int brighten(int color, int amt) {
        int a = (color >> 24) & 0xFF, r = Math.min(255, ((color >> 16) & 0xFF) + amt);
        int gr = Math.min(255, ((color >> 8) & 0xFF) + amt), b = Math.min(255, (color & 0xFF) + amt);
        return (a << 24) | (r << 16) | (gr << 8) | b;
    }

    private static int darken(int color, int amt) {
        int a = (color >> 24) & 0xFF, r = Math.max(0, ((color >> 16) & 0xFF) - amt);
        int gr = Math.max(0, ((color >> 8) & 0xFF) - amt), b = Math.max(0, (color & 0xFF) - amt);
        return (a << 24) | (r << 16) | (gr << 8) | b;
    }

    private static int mix(int c1, int c2, float t) {
        float u = 1 - t;
        int r = (int) (((c1 >> 16) & 0xFF) * u + ((c2 >> 16) & 0xFF) * t);
        int gr = (int) (((c1 >> 8) & 0xFF) * u + ((c2 >> 8) & 0xFF) * t);
        int b = (int) ((c1 & 0xFF) * u + (c2 & 0xFF) * t);
        return 0xFF000000 | (r << 16) | (gr << 8) | b;
    }
}

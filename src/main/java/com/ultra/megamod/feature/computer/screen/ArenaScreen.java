package com.ultra.megamod.feature.computer.screen;

import com.ultra.megamod.MegaMod;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

public class ArenaScreen extends Screen {
    private final Screen parent;

    private int activeTab = 0; // 0=PvE, 1=PvP, 2=Boss Rush
    private boolean dataLoaded = false;
    private int refreshTimer = 0;
    private int scroll = 0;
    private String statusMsg = "";
    private int statusTimer = 0;

    // PvE data
    private int bestPveWave = 0;
    private int totalPveRuns = 0;
    private List<Integer> recentWaves = new ArrayList<>();
    private boolean inArena = false;

    // Challenge unlock/completion data
    private final java.util.Map<String, Boolean> challengeUnlocks = new java.util.LinkedHashMap<>();
    private final java.util.Map<String, Boolean> challengeCompletions = new java.util.LinkedHashMap<>();

    // PvP data
    private int eloRating = 1000;
    private int pvpWins = 0;
    private int pvpLosses = 0;
    private boolean inQueue = false;

    // Boss Rush data
    private boolean bossRushUnlocked = false;
    private String bestBossRushTime = "N/A";
    private List<LeaderboardEntry> leaderboard = new ArrayList<>();

    // Layout
    private int titleBarH;
    private int contentTop;
    private final List<ClickRect> clickRects = new ArrayList<>();

    // Colors
    private static final int ARENA_RED = 0xFFE53935;
    private static final int ARENA_GOLD = 0xFFFFB300;
    private static final int ONLINE_GREEN = 0xFF3FB950;
    private static final int ERROR_RED = 0xFFFF6B6B;
    private static final int SUCCESS_GREEN = 0xFF3FB950;
    private static final int BG_DARK = 0xFF0D1117;
    private static final int PANEL_BG = 0xFF161B22;
    private static final int ROW_EVEN = 0xFF1C2128;
    private static final int ROW_ODD = 0xFF21262D;
    private static final int ROW_HEIGHT = 18;
    private static final int OFFLINE_GREY = 0xFF4A4A50;
    private static final int TAB_ACTIVE = 0xFF21262D;
    private static final int TAB_INACTIVE = 0xFF161B22;
    private static final int TEXT_WHITE = 0xFFE6EDF3;
    private static final int TEXT_DIM = 0xFF8B949E;

    private record LeaderboardEntry(String playerName, String time, int rank) {}

    public ArenaScreen(Screen parent) {
        super(Component.literal("Arena"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        Objects.requireNonNull(this.font);
        this.titleBarH = 9 + 16;
        this.contentTop = this.titleBarH + 6;
        if (!this.dataLoaded) {
            requestArenaData();
        }
    }

    private void requestArenaData() {
        ClientPacketDistributor.sendToServer(
                (CustomPacketPayload) new ComputerActionPayload("arena_request", ""),
                (CustomPacketPayload[]) new CustomPacketPayload[0]
        );
    }

    @Override
    public void tick() {
        super.tick();

        if (this.statusTimer > 0) {
            this.statusTimer--;
            if (this.statusTimer <= 0) {
                this.statusMsg = "";
            }
        }

        // Poll for data
        ComputerDataPayload resp = ComputerDataPayload.lastResponse;
        if (resp != null && resp.dataType().equals("arena_data")) {
            ComputerDataPayload.lastResponse = null;
            parseArenaData(resp.jsonData());
            this.dataLoaded = true;
        }

        if (resp != null && resp.dataType().equals("arena_result")) {
            ComputerDataPayload.lastResponse = null;
            parseResult(resp.jsonData());
        }

        // Consume error responses so the screen doesn't stay stuck
        if (resp != null && "error".equals(resp.dataType())) {
            ComputerDataPayload.lastResponse = null;
            this.dataLoaded = true;
        }

        // Auto-refresh every 60 ticks
        this.refreshTimer++;
        if (this.refreshTimer >= 60) {
            this.refreshTimer = 0;
            requestArenaData();
        }
    }

    private void parseArenaData(String json) {
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();

            // PvE stats
            this.bestPveWave = obj.has("bestPveWave") ? obj.get("bestPveWave").getAsInt() : 0;
            this.totalPveRuns = obj.has("totalPveRuns") ? obj.get("totalPveRuns").getAsInt() : 0;
            this.inArena = obj.has("inArena") && obj.get("inArena").getAsBoolean();

            this.recentWaves.clear();
            if (obj.has("recentWaves")) {
                JsonArray arr = obj.getAsJsonArray("recentWaves");
                for (JsonElement el : arr) {
                    this.recentWaves.add(el.getAsInt());
                }
            }

            // Challenge unlocks
            this.challengeUnlocks.clear();
            if (obj.has("challengeUnlocks")) {
                JsonObject cu = obj.getAsJsonObject("challengeUnlocks");
                for (String key : cu.keySet()) {
                    this.challengeUnlocks.put(key, cu.get(key).getAsBoolean());
                }
            }

            // Challenge completions
            this.challengeCompletions.clear();
            if (obj.has("challengeCompletions")) {
                JsonObject cc = obj.getAsJsonObject("challengeCompletions");
                for (String key : cc.keySet()) {
                    this.challengeCompletions.put(key, cc.get(key).getAsBoolean());
                }
            }

            // PvP stats
            this.eloRating = obj.has("eloRating") ? obj.get("eloRating").getAsInt() : 1000;
            this.pvpWins = obj.has("pvpWins") ? obj.get("pvpWins").getAsInt() : 0;
            this.pvpLosses = obj.has("pvpLosses") ? obj.get("pvpLosses").getAsInt() : 0;
            this.inQueue = obj.has("inQueue") && obj.get("inQueue").getAsBoolean();

            // Boss Rush
            this.bossRushUnlocked = obj.has("bossRushUnlocked") && obj.get("bossRushUnlocked").getAsBoolean();
            this.bestBossRushTime = obj.has("bestBossRushTime") ? obj.get("bestBossRushTime").getAsString() : "N/A";

            this.leaderboard.clear();
            if (obj.has("leaderboard")) {
                JsonArray arr = obj.getAsJsonArray("leaderboard");
                for (int i = 0; i < arr.size(); i++) {
                    JsonObject entry = arr.get(i).getAsJsonObject();
                    this.leaderboard.add(new LeaderboardEntry(
                            entry.get("name").getAsString(),
                            entry.get("time").getAsString(),
                            i + 1
                    ));
                }
            }
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to parse arena data", e);
        }
    }

    private void parseResult(String json) {
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            boolean success = obj.has("success") && obj.get("success").getAsBoolean();
            String msg = obj.has("message") ? obj.get("message").getAsString() : (success ? "Done!" : "Failed.");
            this.statusMsg = msg;
            this.statusTimer = 80;
            requestArenaData();
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to handle arena action response", e);
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.clickRects.clear();

        // Background
        g.fill(0, 0, this.width, this.height, BG_DARK);
        UIHelper.drawPanel(g, 0, 0, this.width, this.height);

        // Title bar
        UIHelper.drawTitleBar(g, 0, 0, this.width, this.titleBarH);
        Objects.requireNonNull(this.font);
        int titleY = (this.titleBarH - 9) / 2;
        UIHelper.drawCenteredTitle(g, this.font, "Arena", this.width / 2, titleY);

        // Back button
        int backW = 50;
        int backH = 16;
        int backX = 8;
        int backY = (this.titleBarH - backH) / 2;
        boolean backHover = mouseX >= backX && mouseX < backX + backW && mouseY >= backY && mouseY < backY + backH;
        UIHelper.drawButton(g, backX, backY, backW, backH, backHover);
        int backTextX = backX + (backW - this.font.width("< Back")) / 2;
        g.drawString(this.font, "< Back", backTextX, backY + (backH - 9) / 2, -661816, false);
        this.clickRects.add(new ClickRect(backX, backY, backW, backH, "back"));

        // Tabs
        renderTabBar(g, mouseX, mouseY);

        // Tab content
        int panelTop = this.contentTop + 18;
        int panelBottom = this.height - 30;
        int panelX = 4;
        int panelW = this.width - 8;
        UIHelper.drawInsetPanel(g, panelX, panelTop, panelW, panelBottom - panelTop);

        switch (this.activeTab) {
            case 0 -> renderPveTab(g, mouseX, mouseY, panelX, panelTop, panelW, panelBottom);
            case 1 -> renderPvpTab(g, mouseX, mouseY, panelX, panelTop, panelW, panelBottom);
            case 2 -> renderBossRushTab(g, mouseX, mouseY, panelX, panelTop, panelW, panelBottom);
        }

        // Status message
        if (!this.statusMsg.isEmpty() && this.statusTimer > 0) {
            int msgW = this.font.width(this.statusMsg) + 20;
            int msgX = (this.width - msgW) / 2;
            int msgY = this.height - 24;
            UIHelper.drawCard(g, msgX, msgY, msgW, 18);
            boolean isError = this.statusMsg.contains("already") || this.statusMsg.contains("Cannot")
                    || this.statusMsg.contains("not") || this.statusMsg.contains("Failed")
                    || this.statusMsg.contains("must") || this.statusMsg.contains("Need");
            int msgColor = isError ? ERROR_RED : SUCCESS_GREEN;
            g.drawCenteredString(this.font, this.statusMsg, this.width / 2, msgY + 5, msgColor);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void renderTabBar(GuiGraphics g, int mouseX, int mouseY) {
        String[] tabNames = {"PvE Arena", "PvP Arena", "Boss Rush"};
        int tabW = 70;
        int tabH = 16;
        int tabY = this.contentTop - 2;

        for (int i = 0; i < tabNames.length; i++) {
            int tabX = 8 + i * (tabW + 4);
            boolean hover = mouseX >= tabX && mouseX < tabX + tabW && mouseY >= tabY && mouseY < tabY + tabH;
            int bg = this.activeTab == i ? TAB_ACTIVE : TAB_INACTIVE;
            g.fill(tabX, tabY, tabX + tabW, tabY + tabH, bg);
            if (hover && this.activeTab != i) {
                g.fill(tabX, tabY, tabX + tabW, tabY + tabH, 0x18FFFFFF);
            }
            if (this.activeTab == i) {
                g.fill(tabX, tabY + tabH - 2, tabX + tabW, tabY + tabH, ARENA_RED);
            }
            g.drawCenteredString(this.font, tabNames[i], tabX + tabW / 2, tabY + (tabH - 9) / 2,
                    this.activeTab == i ? ARENA_RED : OFFLINE_GREY);
            this.clickRects.add(new ClickRect(tabX, tabY, tabW, tabH, "tab_" + i));
        }
    }

    private static final String[][] CHALLENGE_DEFS = {
        // {mode_key, display_name, description, reward, unlock_hint}
        {"STANDARD_5",  "5 Rounds",     "Beat 5 waves of enemies",           "75 MC",   ""},
        {"STANDARD_10", "10 Rounds",    "Beat 10 waves of enemies",          "200 MC",  "Beat 5 Rounds"},
        {"STANDARD_15", "15 Rounds",    "Beat 15 waves of enemies",          "400 MC",  "Beat 10 Rounds"},
        {"STANDARD_20", "20 Rounds",    "Beat 20 waves of enemies",          "750 MC",  "Beat 15 Rounds"},
        {"ENDLESS",     "Endless",      "Survive as long as you can",        "Dynamic", "Beat 20 Rounds"},
        {"NO_ARMOR",    "No Armor",     "10 waves without any armor",        "500 MC",  "Beat 10 Rounds"},
        {"NO_DAMAGE",   "No Damage",    "10 waves — take ANY damage = fail", "5,000 MC", "Beat No Armor"},
    };

    private void renderPveTab(GuiGraphics g, int mouseX, int mouseY, int panelX, int panelTop, int panelW, int panelBottom) {
        int y = panelTop + 8;
        int leftCol = panelX + 8;

        // Stats bar
        g.drawString(this.font, "Best Wave: ", leftCol, y, TEXT_DIM, false);
        g.drawString(this.font, String.valueOf(this.bestPveWave), leftCol + 62, y, ARENA_GOLD, false);
        g.drawString(this.font, "  Runs: ", leftCol + 85, y, TEXT_DIM, false);
        g.drawString(this.font, String.valueOf(this.totalPveRuns), leftCol + 130, y, TEXT_WHITE, false);
        y += 14;

        UIHelper.drawHorizontalDivider(g, leftCol, y, panelW - 16);
        y += 6;

        // Challenge grid
        g.drawString(this.font, "Select Challenge:", leftCol, y, ARENA_RED, false);
        y += 13;

        int cardW = panelW - 20;
        int cardH = 28;
        int cardGap = 3;

        for (String[] def : CHALLENGE_DEFS) {
            String modeKey = def[0];
            String name = def[1];
            String desc = def[2];
            String reward = def[3];
            String hint = def[4];

            boolean unlocked = this.challengeUnlocks.getOrDefault(modeKey, modeKey.equals("STANDARD_5"));
            boolean completed = this.challengeCompletions.getOrDefault(modeKey, false);

            // Card background
            int cardBg = unlocked ? ROW_EVEN : 0xFF111518;
            boolean hover = unlocked && !this.inArena && mouseX >= leftCol && mouseX < leftCol + cardW && mouseY >= y && mouseY < y + cardH;
            if (hover) cardBg = 0xFF2A3038;

            g.fill(leftCol, y, leftCol + cardW, y + cardH, cardBg);

            // Left: status icon + name
            int iconX = leftCol + 4;
            int textY = y + 4;

            if (completed) {
                g.drawString(this.font, "\u2713", iconX, textY, ONLINE_GREEN, false); // checkmark
            } else if (unlocked) {
                g.drawString(this.font, "\u25CF", iconX, textY, ARENA_GOLD, false); // dot
            } else {
                g.drawString(this.font, "\u2716", iconX, textY, 0xFF555555, false); // X
            }

            int nameColor = unlocked ? (completed ? ONLINE_GREEN : TEXT_WHITE) : 0xFF555555;
            g.drawString(this.font, name, iconX + 12, textY, nameColor, false);

            // Description
            String descStr = unlocked ? desc : "Locked: " + hint;
            g.drawString(this.font, descStr, iconX + 12, textY + 11, unlocked ? TEXT_DIM : 0xFF444444, false);

            // Right: reward + start button
            int rightEdge = leftCol + cardW - 6;
            g.drawString(this.font, reward, rightEdge - this.font.width(reward), textY, unlocked ? ARENA_GOLD : 0xFF444444, false);

            if (unlocked && !this.inArena) {
                int btnW = 36;
                int btnH2 = 14;
                int btnX = rightEdge - btnW;
                int btnY = textY + 10;
                boolean btnHov = mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY && mouseY < btnY + btnH2;
                int btnBg = btnHov ? 0xFF2E4B2E : 0xFF1A2E1A;
                g.fill(btnX, btnY, btnX + btnW, btnY + btnH2, btnBg);
                g.drawCenteredString(this.font, "GO", btnX + btnW / 2, btnY + 3, btnHov ? 0xFFFFFFFF : ONLINE_GREEN);
                this.clickRects.add(new ClickRect(btnX, btnY, btnW, btnH2, "pve_" + modeKey));
            }

            y += cardH + cardGap;
        }

        // In arena status
        if (this.inArena) {
            y += 4;
            g.drawCenteredString(this.font, "Currently in arena...", panelX + panelW / 2, y, ARENA_GOLD);
        }
    }

    private void renderPvpTab(GuiGraphics g, int mouseX, int mouseY, int panelX, int panelTop, int panelW, int panelBottom) {
        int y = panelTop + 10;
        int leftCol = panelX + 12;
        int rightCol = panelX + panelW / 2;

        // Stats
        g.drawString(this.font, "PvP Arena Stats", leftCol, y, ARENA_RED, false);
        y += 14;

        g.drawString(this.font, "ELO Rating:", leftCol, y, TEXT_DIM, false);
        int eloColor = this.eloRating >= 1200 ? ONLINE_GREEN : (this.eloRating >= 800 ? ARENA_GOLD : ERROR_RED);
        g.drawString(this.font, String.valueOf(this.eloRating), leftCol + 72, y, eloColor, false);
        y += 12;

        g.drawString(this.font, "Wins:", leftCol, y, TEXT_DIM, false);
        g.drawString(this.font, String.valueOf(this.pvpWins), leftCol + 72, y, ONLINE_GREEN, false);
        y += 12;

        g.drawString(this.font, "Losses:", leftCol, y, TEXT_DIM, false);
        g.drawString(this.font, String.valueOf(this.pvpLosses), leftCol + 72, y, ERROR_RED, false);
        y += 12;

        int total = this.pvpWins + this.pvpLosses;
        String winRate = total > 0 ? String.format("%.0f%%", (float) this.pvpWins / total * 100) : "N/A";
        g.drawString(this.font, "Win Rate:", leftCol, y, TEXT_DIM, false);
        g.drawString(this.font, winRate, leftCol + 72, y, TEXT_WHITE, false);

        // Queue button
        int btnW = 120;
        int btnH = 20;
        int btnX = rightCol + 10;
        int btnY = panelTop + 20;
        boolean btnEnabled = !this.inArena;
        boolean btnHover = btnEnabled && mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY && mouseY < btnY + btnH;

        if (btnEnabled) {
            UIHelper.drawButton(g, btnX, btnY, btnW, btnH, btnHover);
        } else {
            g.fill(btnX, btnY, btnX + btnW, btnY + btnH, 0xFF333333);
        }

        String queueLabel;
        int queueColor;
        if (this.inArena) {
            queueLabel = "In Arena...";
            queueColor = OFFLINE_GREY;
        } else if (this.inQueue) {
            queueLabel = "Leave Queue";
            queueColor = ERROR_RED;
        } else {
            queueLabel = "Queue for Match";
            queueColor = ARENA_GOLD;
        }
        g.drawCenteredString(this.font, queueLabel, btnX + btnW / 2, btnY + (btnH - 9) / 2, queueColor);
        if (btnEnabled) {
            this.clickRects.add(new ClickRect(btnX, btnY, btnW, btnH, "pvp_queue"));
        }

        // Description
        int descY = btnY + btnH + 10;
        g.drawString(this.font, "Best of 3 rounds, 60s each.", rightCol + 10, descY, TEXT_DIM, false);
        g.drawString(this.font, "ELO-based matchmaking.", rightCol + 10, descY + 11, TEXT_DIM, false);
        g.drawString(this.font, "Winner earns 150 MC!", rightCol + 10, descY + 22, TEXT_DIM, false);
    }

    private void renderBossRushTab(GuiGraphics g, int mouseX, int mouseY, int panelX, int panelTop, int panelW, int panelBottom) {
        int y = panelTop + 10;
        int leftCol = panelX + 12;
        int rightCol = panelX + panelW / 2;

        g.drawString(this.font, "Boss Rush", leftCol, y, ARENA_RED, false);
        y += 14;

        g.drawString(this.font, "Best Time:", leftCol, y, TEXT_DIM, false);
        g.drawString(this.font, this.bestBossRushTime, leftCol + 65, y, ARENA_GOLD, false);
        y += 12;

        String statusStr = this.bossRushUnlocked ? "UNLOCKED" : "LOCKED";
        int statusColor = this.bossRushUnlocked ? ONLINE_GREEN : ERROR_RED;
        g.drawString(this.font, "Status:", leftCol, y, TEXT_DIM, false);
        g.drawString(this.font, statusStr, leftCol + 65, y, statusColor, false);
        y += 16;

        if (!this.bossRushUnlocked) {
            g.drawString(this.font, "Defeat all 8 bosses on", leftCol, y, OFFLINE_GREY, false);
            y += 11;
            g.drawString(this.font, "INFERNAL tier to unlock.", leftCol, y, OFFLINE_GREY, false);
            y += 16;
        }

        // Leaderboard
        g.drawString(this.font, "Top 5 Leaderboard:", leftCol, y, ARENA_GOLD, false);
        y += 12;

        UIHelper.drawHorizontalDivider(g, leftCol, y, panelW / 2 - 20);
        y += 4;

        if (this.leaderboard.isEmpty()) {
            g.drawString(this.font, "No completions yet", leftCol + 10, y, OFFLINE_GREY, false);
        } else {
            for (LeaderboardEntry entry : this.leaderboard) {
                int rowBg = entry.rank % 2 == 0 ? ROW_EVEN : ROW_ODD;
                g.fill(leftCol, y, leftCol + panelW / 2 - 20, y + ROW_HEIGHT, rowBg);

                String rankStr = "#" + entry.rank;
                int rankColor = entry.rank == 1 ? ARENA_GOLD : (entry.rank == 2 ? 0xFFC0C0C0 : (entry.rank == 3 ? 0xFFCD7F32 : TEXT_DIM));
                g.drawString(this.font, rankStr, leftCol + 4, y + (ROW_HEIGHT - 9) / 2, rankColor, false);
                g.drawString(this.font, entry.playerName, leftCol + 24, y + (ROW_HEIGHT - 9) / 2, TEXT_WHITE, false);
                g.drawString(this.font, entry.time, leftCol + panelW / 2 - 80, y + (ROW_HEIGHT - 9) / 2, ARENA_GOLD, false);

                y += ROW_HEIGHT;
            }
        }

        // Start button
        int btnW = 120;
        int btnH = 20;
        int btnX = rightCol + 10;
        int btnY = panelTop + 20;
        boolean btnEnabled = this.bossRushUnlocked && !this.inArena;
        boolean btnHover = btnEnabled && mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY && mouseY < btnY + btnH;

        if (btnEnabled) {
            UIHelper.drawButton(g, btnX, btnY, btnW, btnH, btnHover);
        } else {
            g.fill(btnX, btnY, btnX + btnW, btnY + btnH, 0xFF333333);
        }

        String startLabel = this.inArena ? "In Arena..." : (this.bossRushUnlocked ? "Start Boss Rush" : "Locked");
        int labelColor = btnEnabled ? ARENA_RED : OFFLINE_GREY;
        g.drawCenteredString(this.font, startLabel, btnX + btnW / 2, btnY + (btnH - 9) / 2, labelColor);
        if (btnEnabled) {
            this.clickRects.add(new ClickRect(btnX, btnY, btnW, btnH, "boss_rush_start"));
        }

        // Description
        int descY = btnY + btnH + 10;
        g.drawString(this.font, "Fight all 8 dungeon bosses", rightCol + 10, descY, TEXT_DIM, false);
        g.drawString(this.font, "back to back!", rightCol + 10, descY + 11, TEXT_DIM, false);
        g.drawString(this.font, "Death = run over.", rightCol + 10, descY + 22, TEXT_DIM, false);
        g.drawString(this.font, "Reward: 1,000 MC!", rightCol + 10, descY + 33, TEXT_DIM, false);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean consumed) {
        if (consumed) {
            return super.mouseClicked(event, consumed);
        }
        int mx = (int) event.x();
        int my = (int) event.y();

        for (ClickRect r : this.clickRects) {
            if (mx >= r.x && mx < r.x + r.w && my >= r.y && my < r.y + r.h) {
                handleClick(r.action);
                return true;
            }
        }

        return super.mouseClicked(event, consumed);
    }

    private void handleClick(String action) {
        switch (action) {
            case "back" -> {
                if (this.minecraft != null) {
                    this.minecraft.setScreen(this.parent);
                }
            }
            case "tab_0" -> {
                this.activeTab = 0;
                this.scroll = 0;
            }
            case "tab_1" -> {
                this.activeTab = 1;
                this.scroll = 0;
            }
            case "tab_2" -> {
                this.activeTab = 2;
                this.scroll = 0;
            }
            case "pvp_queue" -> {
                ClientPacketDistributor.sendToServer(
                        (CustomPacketPayload) new ComputerActionPayload("arena_pvp_queue", ""),
                        (CustomPacketPayload[]) new CustomPacketPayload[0]
                );
            }
            case "boss_rush_start" -> {
                ClientPacketDistributor.sendToServer(
                        (CustomPacketPayload) new ComputerActionPayload("arena_boss_rush_start", ""),
                        (CustomPacketPayload[]) new CustomPacketPayload[0]
                );
            }
            default -> {
                // Handle pve_<MODE> clicks
                if (action.startsWith("pve_")) {
                    String modeKey = action.substring(4);
                    String json = "{\"mode\":\"" + modeKey + "\"}";
                    ClientPacketDistributor.sendToServer(
                            (CustomPacketPayload) new ComputerActionPayload("arena_pve_start", json),
                            (CustomPacketPayload[]) new CustomPacketPayload[0]
                    );
                }
            }
        }
    }

    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();

        if (keyCode == 256) {
            if (this.minecraft != null) {
                this.minecraft.setScreen(this.parent);
            }
            return true;
        }

        return super.keyPressed(event);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        this.scroll -= (int) (scrollY * ROW_HEIGHT);
        this.scroll = Math.max(0, this.scroll);
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private record ClickRect(int x, int y, int w, int h, String action) {}
}

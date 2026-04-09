package com.ultra.megamod.feature.computer.screen;

import com.ultra.megamod.feature.computer.network.ComputerActionPayload;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.ui.UIHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Computer app for players to customize their badge, prestige name color,
 * view Mastery Marks, and spend marks on cosmetic rewards.
 */
public class CustomizeScreen extends Screen {

    private final Screen parent;
    private int titleBarH;
    private int contentTop, contentBottom, contentLeft, contentRight;
    private final List<ClickRect> clickRects = new ArrayList<>();
    private int scrollOffset = 0;
    private String statusMessage = "";
    private int statusTicks = 0;
    private boolean dataLoaded = false;
    private int retryTicks = 0;

    // Synced data from server
    private int masteryMarks = 0;
    private String currentBadgeTitle = "";
    private String currentBadgeColor = "";
    private boolean hasCustomBadge = false;
    private int totalPrestige = 0;
    private String currentPrestigeColor = "Default";
    private String[] unlockedBranches = new String[0]; // branches at tier 3+ for badge selection
    private int coinBoostLevel = 0;
    private int xpBoostLevel = 0;
    private boolean isAdmin = false;
    private boolean hasPrestiged = false;

    // Tabs
    private static final String[] TAB_NAMES = {"Badge", "Name Color", "Mastery Marks"};
    private int activeTab = 0;

    // Badge color options
    private static final String[][] BADGE_COLORS = {
        {"red", "Red"}, {"gold", "Gold"}, {"green", "Green"}, {"purple", "Purple"},
        {"yellow", "Yellow"}, {"aqua", "Aqua"}, {"blue", "Blue"}, {"white", "White"},
        {"dark_red", "Dark Red"}, {"dark_green", "Dark Green"}, {"dark_purple", "Dark Purple"},
        {"dark_aqua", "Dark Aqua"}, {"gray", "Gray"}, {"pink", "Pink"}
    };
    private static final int[] BADGE_COLOR_HEX = {
        0xFFFF5555, 0xFFFFAA00, 0xFF55FF55, 0xFFFF55FF,
        0xFFFFFF55, 0xFF55FFFF, 0xFF5555FF, 0xFFFFFFFF,
        0xFFAA0000, 0xFF00AA00, 0xFFAA00AA,
        0xFF00AAAA, 0xFFAAAAAA, 0xFFFF55FF
    };

    // Prestige color options
    private static final String[][] PRESTIGE_COLORS = {
        {"default", "Default (Auto)"}, {"red", "Red"}, {"gold", "Gold"},
        {"yellow", "Yellow"}, {"aqua", "Aqua"}, {"green", "Green"},
        {"blue", "Blue"}, {"light_purple", "Light Purple"}, {"white", "White"},
        {"dark_red", "Dark Red"}, {"dark_aqua", "Dark Aqua"}, {"dark_purple", "Dark Purple"}
    };
    private static final int[] PRESTIGE_COLOR_HEX = {
        0xFF888899, 0xFFFF5555, 0xFFFFAA00,
        0xFFFFFF55, 0xFF55FFFF, 0xFF55FF55,
        0xFF5555FF, 0xFFFF55FF, 0xFFFFFFFF,
        0xFFAA0000, 0xFF00AAAA, 0xFFAA00AA
    };

    // Mastery shop items
    private static final String[][] SHOP_ITEMS = {
        {"buy_coin_boost", "Permanent +5% Coin Drops", "100"},
        {"buy_xp_boost", "Permanent +5% XP Gain", "100"},
        {"buy_furniture_crate", "Exclusive Furniture Crate", "30"},
    };

    public CustomizeScreen(Screen parent) {
        super(Component.literal("Customize"));
        this.parent = parent;
    }

    protected void init() {
        super.init();
        Objects.requireNonNull(this.font);
        this.titleBarH = 9 + 16;
        this.contentLeft = 16;
        this.contentRight = this.width - 16;
        this.contentTop = this.titleBarH + 22;
        this.contentBottom = this.height - 14;
        // Request data from server
        ClientPacketDistributor.sendToServer(new ComputerActionPayload("customize_request", ""));
    }

    public void tick() {
        super.tick();
        if (this.statusTicks > 0) --this.statusTicks;
        ComputerDataPayload response = ComputerDataPayload.lastResponse;
        if (response != null) {
            if ("customize_data".equals(response.dataType())) {
                ComputerDataPayload.lastResponse = null;
                parseData(response.jsonData());
                dataLoaded = true;
            } else if ("customize_result".equals(response.dataType())) {
                ComputerDataPayload.lastResponse = null;
                try {
                    JsonObject obj = JsonParser.parseString(response.jsonData()).getAsJsonObject();
                    boolean success = obj.has("success") && obj.get("success").getAsBoolean();
                    String msg = obj.has("msg") ? obj.get("msg").getAsString() : (success ? "Done!" : "Failed!");
                    statusMessage = msg;
                    statusTicks = 80;
                    // Re-request data to refresh
                    ClientPacketDistributor.sendToServer(new ComputerActionPayload("customize_request", ""));
                } catch (Exception ignored) {
                    statusMessage = "Updated!";
                    statusTicks = 60;
                }
            }
            // Consume error responses so the screen doesn't stay stuck
            if (response != null && "error".equals(response.dataType())) {
                ComputerDataPayload.lastResponse = null;
                this.dataLoaded = true;
            }
        }
        // Auto-retry if data hasn't loaded (every 60 ticks)
        if (!dataLoaded) {
            retryTicks++;
            if (retryTicks % 60 == 0) {
                ClientPacketDistributor.sendToServer(new ComputerActionPayload("customize_request", ""));
            }
        }
    }

    private void parseData(String json) {
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            masteryMarks = obj.has("marks") ? obj.get("marks").getAsInt() : 0;
            currentBadgeTitle = obj.has("badge_title") ? obj.get("badge_title").getAsString() : "";
            currentBadgeColor = obj.has("badge_color") ? obj.get("badge_color").getAsString() : "";
            hasCustomBadge = obj.has("has_custom") && obj.get("has_custom").getAsBoolean();
            totalPrestige = obj.has("total_prestige") ? obj.get("total_prestige").getAsInt() : 0;
            currentPrestigeColor = obj.has("prestige_color") ? obj.get("prestige_color").getAsString() : "default";
            coinBoostLevel = obj.has("coin_boost") ? obj.get("coin_boost").getAsInt() : 0;
            xpBoostLevel = obj.has("xp_boost") ? obj.get("xp_boost").getAsInt() : 0;
            isAdmin = obj.has("is_admin") && obj.get("is_admin").getAsBoolean();
            hasPrestiged = obj.has("has_prestiged") && obj.get("has_prestiged").getAsBoolean();

            if (obj.has("branches")) {
                JsonArray arr = obj.getAsJsonArray("branches");
                unlockedBranches = new String[arr.size()];
                for (int i = 0; i < arr.size(); i++) {
                    unlockedBranches[i] = arr.get(i).getAsString();
                }
            }
        } catch (Exception ignored) {}
    }

    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        clickRects.clear();
        g.fill(0, 0, width, height, 0xFF0E0E18);
        UIHelper.drawPanel(g, 0, 0, width, height);
        UIHelper.drawTitleBar(g, 0, 0, width, titleBarH);
        UIHelper.drawCenteredTitle(g, font, "Customize", width / 2, (titleBarH - 9) / 2);

        // Back button
        int backW = 50, backH = 16, backX = 8, backY = (titleBarH - backH) / 2;
        boolean backHover = mouseX >= backX && mouseX < backX + backW && mouseY >= backY && mouseY < backY + backH;
        UIHelper.drawButton(g, backX, backY, backW, backH, backHover);
        g.drawString(font, "< Back", backX + (backW - font.width("< Back")) / 2, backY + 3, 0xFFCCCCDD, false);
        clickRects.add(new ClickRect(backX, backY, backW, backH, "back", -1));

        // Mastery Marks display (top right)
        String marksStr = "\u2726 " + masteryMarks + " Marks";
        int marksW = font.width(marksStr) + 12;
        int marksX = width - marksW - 10;
        UIHelper.drawInsetPanel(g, marksX, backY, marksW, backH);
        g.drawString(font, marksStr, marksX + 6, backY + 3, 0xFFD4A0FF, false);

        // Tabs
        int tabY = titleBarH + 3;
        int tabW = 80, tabH = 14;
        int totalW = TAB_NAMES.length * (tabW + 4) - 4;
        int tabStartX = (width - totalW) / 2;
        for (int i = 0; i < TAB_NAMES.length; i++) {
            int tx = tabStartX + i * (tabW + 4);
            boolean hover = mouseX >= tx && mouseX < tx + tabW && mouseY >= tabY && mouseY < tabY + tabH;
            boolean active = i == activeTab;
            g.fill(tx, tabY, tx + tabW, tabY + tabH, active ? 0xFF2A2A44 : (hover ? 0xFF1E1E33 : 0xFF161628));
            if (active) g.fill(tx, tabY, tx + tabW, tabY + 1, 0xFFD4A0FF);
            int tc = active ? 0xFFD4A0FF : (hover ? 0xFFAAAABB : 0xFF777788);
            g.drawString(font, TAB_NAMES[i], tx + (tabW - font.width(TAB_NAMES[i])) / 2, tabY + 3, tc, false);
            clickRects.add(new ClickRect(tx, tabY, tabW, tabH, "tab_" + i, i));
        }

        UIHelper.drawInsetPanel(g, contentLeft - 4, contentTop - 4, contentRight - contentLeft + 8, contentBottom - contentTop + 8);

        if (!dataLoaded) {
            UIHelper.drawCenteredLabel(g, font, "Loading...", width / 2, height / 2);
        } else {
            g.enableScissor(contentLeft, contentTop, contentRight, contentBottom);
            switch (activeTab) {
                case 0 -> renderBadgeTab(g, mouseX, mouseY);
                case 1 -> renderNameColorTab(g, mouseX, mouseY);
                case 2 -> {
                    if (!hasPrestiged && !isAdmin) {
                        renderMasteryLocked(g);
                    } else {
                        renderMasteryTab(g, mouseX, mouseY);
                    }
                }
            }
            g.disableScissor();
        }

        // Status
        if (statusTicks > 0 && !statusMessage.isEmpty()) {
            int sc = statusMessage.contains("!") && !statusMessage.contains("Not") && !statusMessage.contains("need") ? 0xFF55BB55 : 0xFFCC5555;
            g.drawCenteredString(font, statusMessage, width / 2, height - 12, sc);
        }
        super.render(g, mouseX, mouseY, partialTick);
    }

    private void renderBadgeTab(GuiGraphics g, int mx, int my) {
        int y = contentTop + 4 - scrollOffset;
        int left = contentLeft + 4;
        int right = contentRight - 4;

        // Current badge preview
        g.drawString(font, "Current Badge:", left, y, 0xFFD4A0FF, false);
        y += 12;
        String preview = currentBadgeTitle.isEmpty() ? "(Auto - based on skill tree)" : "[" + currentBadgeTitle + "]";
        g.drawString(font, preview, left + 8, y, hasCustomBadge ? 0xFFFFAA00 : 0xFF888899, false);
        y += 12;
        if (hasCustomBadge) {
            g.drawString(font, "Color: " + currentBadgeColor, left + 8, y, 0xFF888899, false);
        } else {
            g.drawString(font, "Using auto-badge from highest skill branch", left + 8, y, 0xFF666677, false);
        }
        y += 18;

        // Reset to auto button
        if (hasCustomBadge) {
            int btnW = font.width("Reset to Auto") + 12;
            boolean hover = mx >= left && mx < left + btnW && my >= y && my < y + 14;
            g.fill(left, y, left + btnW, y + 14, hover ? 0xFF30363D : 0xFF21262D);
            g.drawString(font, "Reset to Auto", left + 6, y + 3, 0xFF58A6FF, false);
            clickRects.add(new ClickRect(left, y, btnW, 14, "badge_reset", -1));
            y += 20;
        }

        // Available badge titles (from unlocked branches)
        g.drawString(font, "Choose Badge Title: (Tier 3+ branches)", left, y, 0xFFD4A0FF, false);
        y += 14;

        if (unlockedBranches.length == 0) {
            g.drawString(font, "Reach Tier 3 in any skill branch to unlock badges", left + 8, y, 0xFF666677, false);
            y += 14;
        } else {
            for (String branch : unlockedBranches) {
                int rowH = 16;
                boolean hover = mx >= left && mx < right && my >= y && my < y + rowH;
                g.fill(left, y, right, y + rowH, hover ? 0xFF1A1A33 : 0xFF131322);
                g.drawString(font, branch, left + 6, y + 4, 0xFFCCCCDD, false);

                int setBtnW = 36;
                int setBtnX = right - setBtnW - 4;
                boolean btnHover = mx >= setBtnX && mx < setBtnX + setBtnW && my >= y + 1 && my < y + rowH - 1;
                g.fill(setBtnX, y + 1, setBtnX + setBtnW, y + rowH - 1, btnHover ? 0xFF30363D : 0xFF21262D);
                g.drawString(font, "Set", setBtnX + (setBtnW - font.width("Set")) / 2, y + 4, 0xFF3FB950, false);
                clickRects.add(new ClickRect(setBtnX, y + 1, setBtnW, rowH - 2, "badge_set_" + branch, -1));
                y += rowH + 1;
            }
        }
        y += 10;

        // Badge color picker
        g.drawString(font, "Badge Color:", left, y, 0xFFD4A0FF, false);
        y += 14;

        int colsPerRow = 7;
        int swatchSize = 18;
        int gap = 4;
        for (int i = 0; i < BADGE_COLORS.length; i++) {
            int col = i % colsPerRow;
            int row = i / colsPerRow;
            int sx = left + col * (swatchSize + gap);
            int sy = y + row * (swatchSize + gap);

            boolean selected = BADGE_COLORS[i][0].equals(currentBadgeColor);
            boolean hover = mx >= sx && mx < sx + swatchSize && my >= sy && my < sy + swatchSize;

            g.fill(sx, sy, sx + swatchSize, sy + swatchSize, BADGE_COLOR_HEX[i]);
            if (selected) {
                // White border for selected
                g.fill(sx - 1, sy - 1, sx + swatchSize + 1, sy, 0xFFFFFFFF);
                g.fill(sx - 1, sy + swatchSize, sx + swatchSize + 1, sy + swatchSize + 1, 0xFFFFFFFF);
                g.fill(sx - 1, sy, sx, sy + swatchSize, 0xFFFFFFFF);
                g.fill(sx + swatchSize, sy, sx + swatchSize + 1, sy + swatchSize, 0xFFFFFFFF);
            } else if (hover) {
                g.fill(sx - 1, sy - 1, sx + swatchSize + 1, sy, 0xFF888899);
                g.fill(sx - 1, sy + swatchSize, sx + swatchSize + 1, sy + swatchSize + 1, 0xFF888899);
                g.fill(sx - 1, sy, sx, sy + swatchSize, 0xFF888899);
                g.fill(sx + swatchSize, sy, sx + swatchSize + 1, sy + swatchSize, 0xFF888899);
            }

            // Tooltip on hover
            if (hover) {
                g.drawString(font, BADGE_COLORS[i][1], sx, sy - 10, 0xFFCCCCDD, false);
            }

            clickRects.add(new ClickRect(sx, sy, swatchSize, swatchSize, "badge_color_" + BADGE_COLORS[i][0], -1));
        }
    }

    private void renderNameColorTab(GuiGraphics g, int mx, int my) {
        int y = contentTop + 4 - scrollOffset;
        int left = contentLeft + 4;
        int right = contentRight - 4;

        g.drawString(font, "Prestige Name Color", left, y, 0xFFD4A0FF, false);
        y += 12;
        g.drawString(font, "Total Prestige: " + totalPrestige, left + 8, y, 0xFF888899, false);
        y += 12;
        g.drawString(font, "Current: " + currentPrestigeColor, left + 8, y, 0xFF888899, false);
        y += 18;

        if (totalPrestige < 5) {
            g.drawString(font, "Reach prestige 5+ to unlock name colors", left + 8, y, 0xFF666677, false);
            y += 14;
            g.drawString(font, "Default colors: Yellow (5+), Gold (15+), Red (25+)", left + 8, y, 0xFF555566, false);
            return;
        }

        g.drawString(font, "Choose Name Color:", left, y, 0xFFD4A0FF, false);
        y += 14;

        for (int i = 0; i < PRESTIGE_COLORS.length; i++) {
            int rowH = 18;
            boolean selected = PRESTIGE_COLORS[i][0].equals(currentPrestigeColor);
            boolean hover = mx >= left && mx < right && my >= y && my < y + rowH;

            g.fill(left, y, right, y + rowH, selected ? 0xFF2A2A44 : (hover ? 0xFF1A1A33 : 0xFF131322));
            if (selected) g.fill(left, y, left + 2, y + rowH, 0xFFD4A0FF);

            // Color swatch
            g.fill(left + 6, y + 3, left + 18, y + rowH - 3, PRESTIGE_COLOR_HEX[i]);

            g.drawString(font, PRESTIGE_COLORS[i][1], left + 22, y + 5, selected ? 0xFFD4A0FF : 0xFFCCCCDD, false);

            int setBtnW = 36;
            int setBtnX = right - setBtnW - 4;
            boolean btnHover = mx >= setBtnX && mx < setBtnX + setBtnW && my >= y + 2 && my < y + rowH - 2;
            g.fill(setBtnX, y + 2, setBtnX + setBtnW, y + rowH - 2, btnHover ? 0xFF30363D : 0xFF21262D);
            g.drawString(font, "Set", setBtnX + (setBtnW - font.width("Set")) / 2, y + 5, 0xFF3FB950, false);
            clickRects.add(new ClickRect(setBtnX, y + 2, setBtnW, rowH - 4, "prestige_color_" + PRESTIGE_COLORS[i][0], -1));

            y += rowH + 1;
        }
    }

    private void renderMasteryLocked(GuiGraphics g) {
        int cy = (contentTop + contentBottom) / 2;
        g.drawCenteredString(font, "\u26D4 Mastery Marks Locked", width / 2, cy - 16, 0xFFCC5555);
        g.drawCenteredString(font, "Prestige a Skill Tree to unlock", width / 2, cy, 0xFF888899);
        g.drawCenteredString(font, "Press K to open the Skill Tree", width / 2, cy + 14, 0xFF666677);
    }

    private void renderMasteryTab(GuiGraphics g, int mx, int my) {
        int y = contentTop + 4 - scrollOffset;
        int left = contentLeft + 4;
        int right = contentRight - 4;

        // Marks balance
        g.drawString(font, "Marks of Mastery", left, y, 0xFFD4A0FF, false);
        y += 14;
        g.drawString(font, "\u2726 " + masteryMarks + " Marks Available", left + 8, y, 0xFFE8A838, false);
        y += 20;

        // How to earn
        g.drawString(font, "How to Earn:", left, y, 0xFFD4A0FF, false);
        y += 12;
        String[][] sources = {
            {"Clear Normal Dungeon", "5"}, {"Clear Hard Dungeon", "10"},
            {"Clear Nightmare Dungeon", "20"}, {"Clear Infernal Dungeon", "40"},
            {"Prestige a Skill Tree", "25"}, {"Complete Museum Wing", "15"},
            {"100% Museum", "50"}, {"50 Bounties Filled", "10"},
            {"100 Bounties Filled", "20"}, {"50 Colony Citizens", "15"},
            {"Win a Siege", "10"}
        };
        for (String[] src : sources) {
            g.drawString(font, "  " + src[0], left + 4, y, 0xFF888899, false);
            String markStr = "+" + src[1];
            g.drawString(font, markStr, right - font.width(markStr) - 4, y, 0xFF3FB950, false);
            y += 11;
        }
        y += 10;

        // Shop
        g.drawString(font, "Mastery Shop:", left, y, 0xFFD4A0FF, false);
        y += 14;

        // Coin boost
        {
            int rowH = 24;
            g.fill(left, y, right, y + rowH, 0xFF131322);
            String name = "Permanent +5% Coin Drops";
            String level = coinBoostLevel > 0 ? " (Lv " + coinBoostLevel + ")" : "";
            g.drawString(font, name + level, left + 6, y + 3, 0xFFCCCCDD, false);
            g.drawString(font, "Cost: 100 Marks", left + 6, y + 13, 0xFF888899, false);

            int btnW = 36;
            int btnX = right - btnW - 4;
            boolean hover = mx >= btnX && mx < btnX + btnW && my >= y + 4 && my < y + rowH - 4;
            g.fill(btnX, y + 4, btnX + btnW, y + rowH - 4, hover ? 0xFF30363D : 0xFF21262D);
            boolean canAfford = masteryMarks >= 100;
            g.drawString(font, "Buy", btnX + (btnW - font.width("Buy")) / 2, y + 8, canAfford ? 0xFFE8A838 : 0xFF555566, false);
            clickRects.add(new ClickRect(btnX, y + 4, btnW, rowH - 8, "buy_coin_boost", -1));
            y += rowH + 2;
        }

        // XP boost
        {
            int rowH = 24;
            g.fill(left, y, right, y + rowH, 0xFF131322);
            String name = "Permanent +5% XP Gain";
            String level = xpBoostLevel > 0 ? " (Lv " + xpBoostLevel + ")" : "";
            g.drawString(font, name + level, left + 6, y + 3, 0xFFCCCCDD, false);
            g.drawString(font, "Cost: 100 Marks", left + 6, y + 13, 0xFF888899, false);

            int btnW = 36;
            int btnX = right - btnW - 4;
            boolean hover = mx >= btnX && mx < btnX + btnW && my >= y + 4 && my < y + rowH - 4;
            g.fill(btnX, y + 4, btnX + btnW, y + rowH - 4, hover ? 0xFF30363D : 0xFF21262D);
            boolean canAfford = masteryMarks >= 100;
            g.drawString(font, "Buy", btnX + (btnW - font.width("Buy")) / 2, y + 8, canAfford ? 0xFFE8A838 : 0xFF555566, false);
            clickRects.add(new ClickRect(btnX, y + 4, btnW, rowH - 8, "buy_xp_boost", -1));
            y += rowH + 2;
        }

        // Exclusive furniture
        {
            int rowH = 24;
            g.fill(left, y, right, y + rowH, 0xFF131322);
            g.drawString(font, "Exclusive Furniture Crate", left + 6, y + 3, 0xFFCCCCDD, false);
            g.drawString(font, "Cost: 30 Marks  |  Random rare furniture", left + 6, y + 13, 0xFF888899, false);

            int btnW = 36;
            int btnX = right - btnW - 4;
            boolean hover = mx >= btnX && mx < btnX + btnW && my >= y + 4 && my < y + rowH - 4;
            g.fill(btnX, y + 4, btnX + btnW, y + rowH - 4, hover ? 0xFF30363D : 0xFF21262D);
            boolean canAfford = masteryMarks >= 30;
            g.drawString(font, "Buy", btnX + (btnW - font.width("Buy")) / 2, y + 8, canAfford ? 0xFFE8A838 : 0xFF555566, false);
            clickRects.add(new ClickRect(btnX, y + 4, btnW, rowH - 8, "buy_furniture_crate", -1));
        }
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean consumed) {
        if (consumed) return super.mouseClicked(event, consumed);
        int mx = (int) event.x();
        int my = (int) event.y();
        for (ClickRect r : clickRects) {
            if (mx < r.x || mx >= r.x + r.w || my < r.y || my >= r.y + r.h) continue;

            if ("back".equals(r.action)) {
                if (minecraft != null) minecraft.setScreen(parent);
                return true;
            }
            if (r.action.startsWith("tab_")) {
                int t = r.index;
                if (t != activeTab) { activeTab = t; scrollOffset = 0; }
                return true;
            }
            if ("badge_reset".equals(r.action)) {
                sendAction("customize_badge_reset", "{}");
                return true;
            }
            if (r.action.startsWith("badge_set_")) {
                String title = r.action.substring(10);
                sendAction("customize_badge_title", "{\"title\":\"" + title + "\"}");
                return true;
            }
            if (r.action.startsWith("badge_color_")) {
                String color = r.action.substring(12);
                sendAction("customize_badge_color", "{\"color\":\"" + color + "\"}");
                return true;
            }
            if (r.action.startsWith("prestige_color_")) {
                String color = r.action.substring(15);
                sendAction("customize_prestige_color", "{\"color\":\"" + color + "\"}");
                return true;
            }
            if (r.action.startsWith("buy_")) {
                sendAction("customize_buy", "{\"item\":\"" + r.action + "\"}");
                return true;
            }
            return true;
        }
        return super.mouseClicked(event, consumed);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scrollOffset -= (int)(scrollY * 20);
        scrollOffset = Math.max(0, scrollOffset);
        return true;
    }

    private void sendAction(String action, String data) {
        ClientPacketDistributor.sendToServer(new ComputerActionPayload(action, data));
    }

    public boolean isPauseScreen() { return false; }

    private record ClickRect(int x, int y, int w, int h, String action, int index) {}
}

/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.input.MouseButtonEvent
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.neoforged.neoforge.client.network.ClientPacketDistributor
 */
package com.ultra.megamod.feature.computer.screen;

import com.ultra.megamod.feature.computer.network.ComputerActionPayload;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.ui.UIHelper;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class StatsScreen
extends Screen {
    private final Screen parent;
    private int wallet;
    private int bank;
    private boolean dataLoaded = false;
    private final Map<String, Integer> stats = new HashMap<String, Integer>();
    private static final int MARGIN = 10;
    private int titleBarH;
    private static final String[] STAT_LABELS = new String[]{"Player Kills", "Deaths", "Mob Kills", "Blocks Broken", "Blocks Placed", "Play Time", "Damage Dealt", "Damage Taken"};
    private static final String[] STAT_KEYS = new String[]{"kills", "deaths", "mobKills", "blocksBroken", "blocksPlaced", "playTimeTicks", "damageDealt", "damageTaken"};
    private static final int[] STAT_COLORS = new int[]{-3394765, -7658974, -9728434, -3626932, -10843458, -6591602, -39220, -1684992};
    private static final int[] MAX_VALUES = new int[]{100, 50, 500, 10000, 10000, 0x6DDD00, 10000, 10000};
    private int backX;
    private int backY;
    private int backW;
    private int backH;

    public StatsScreen(Screen parent, int wallet, int bank) {
        super((Component)Component.literal((String)"Player Stats"));
        this.parent = parent;
        this.wallet = wallet;
        this.bank = bank;
    }

    protected void init() {
        super.init();
        Objects.requireNonNull(this.font);
        this.titleBarH = 9 + 16;
        ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("request_stats", ""), (CustomPacketPayload[])new CustomPacketPayload[0]);
        this.backW = 50;
        this.backH = 16;
        this.backX = 8;
        this.backY = (this.titleBarH - this.backH) / 2;
    }

    public void tick() {
        super.tick();
        ComputerDataPayload response = ComputerDataPayload.lastResponse;
        if (response != null && "stats_data".equals(response.dataType())) {
            ComputerDataPayload.lastResponse = null;
            this.wallet = response.wallet();
            this.bank = response.bank();
            this.parseStats(response.jsonData());
            this.dataLoaded = true;
        }
        // Consume error responses so the screen doesn't stay stuck
        if (response != null && "error".equals(response.dataType())) {
            ComputerDataPayload.lastResponse = null;
            this.dataLoaded = true;
        }
    }

    private void parseStats(String json) {
        this.stats.clear();
        if (json == null || json.length() < 3) {
            return;
        }
        String inner = json.substring(1, json.length() - 1);
        for (String pair : inner.split(",")) {
            String[] kv = pair.split(":");
            String key = kv[0].replace("\"", "").trim();
            int value = Integer.parseInt(kv[1].trim());
            this.stats.put(key, value);
        }
    }

    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, this.width, this.height, 0xFF0E0E18);
        UIHelper.drawPanel(g, 0, 0, this.width, this.height);
        UIHelper.drawTitleBar(g, 0, 0, this.width, this.titleBarH);
        Objects.requireNonNull(this.font);
        int titleY = (this.titleBarH - 9) / 2;
        UIHelper.drawCenteredTitle(g, this.font, "Player Statistics", this.width / 2, titleY);
        boolean backHover = mouseX >= this.backX && mouseX < this.backX + this.backW && mouseY >= this.backY && mouseY < this.backY + this.backH;
        UIHelper.drawButton(g, this.backX, this.backY, this.backW, this.backH, backHover);
        int backTextX = this.backX + (this.backW - this.font.width("< Back")) / 2;
        Objects.requireNonNull(this.font);
        g.drawString(this.font, "< Back", backTextX, this.backY + (this.backH - 9) / 2, 0xFFCCCCDD, false);
        String balStr = "W: $" + this.wallet + "  B: $" + this.bank;
        int balW = this.font.width(balStr);
        g.drawString(this.font, balStr, this.width - balW - 10, titleY, 0xFF666677, false);
        if (!this.dataLoaded) {
            int loadW = 180;
            Objects.requireNonNull(this.font);
            int loadH = 9 + 20;
            int loadX = (this.width - loadW) / 2;
            int loadY = this.height / 2 - loadH / 2;
            UIHelper.drawPanel(g, loadX, loadY, loadW, loadH);
            int n = this.width / 2;
            int n2 = this.height / 2;
            Objects.requireNonNull(this.font);
            UIHelper.drawCenteredLabel(g, this.font, "Loading stats...", n, n2 - 9 / 2);
        } else {
            this.renderStatCards(g, mouseX, mouseY);
        }
        super.render(g, mouseX, mouseY, partialTick);
    }

    private void renderStatCards(GuiGraphics g, int mouseX, int mouseY) {
        int contentTop = this.titleBarH + 10;
        int cardW = 220;
        int cardH = 44;
        int cardGap = 8;
        int cols = 2;
        int totalW = cardW * cols + cardGap;
        int startX = (this.width - totalW) / 2;
        int totalCards = STAT_LABELS.length;
        int rows = (totalCards + cols - 1) / cols;
        int totalH = rows * (cardH + cardGap) - cardGap;
        int startY = contentTop + Math.max(0, (this.height - contentTop - 10 - totalH) / 2);
        for (int i = 0; i < totalCards; ++i) {
            float progress;
            Object displayValue;
            int col = i % cols;
            int row = i / cols;
            int cx = startX + col * (cardW + cardGap);
            int cy = startY + row * (cardH + cardGap);
            int rawValue = this.stats.getOrDefault(STAT_KEYS[i], 0);
            if (i == 5) {
                int totalMinutes = rawValue / 20 / 60;
                int hours = totalMinutes / 60;
                int minutes = totalMinutes % 60;
                displayValue = hours + "h " + minutes + "m";
                progress = Math.min(1.0f, (float)rawValue / (float)MAX_VALUES[i]);
            } else {
                displayValue = String.valueOf(rawValue);
                progress = Math.min(1.0f, (float)rawValue / (float)MAX_VALUES[i]);
            }
            boolean hovered = mouseX >= cx && mouseX < cx + cardW && mouseY >= cy && mouseY < cy + cardH;
            UIHelper.drawCard(g, cx, cy, cardW, cardH, hovered);
            g.drawString(this.font, STAT_LABELS[i], cx + 8, cy + 6, 0xFF666677, false);
            int valueW = this.font.width((String)displayValue);
            g.drawString(this.font, (String)displayValue, cx + cardW - valueW - 8, cy + 6, 0xFFCCCCDD, false);
            int barX = cx + 8;
            Objects.requireNonNull(this.font);
            int barY = cy + 6 + 9 + 6;
            int barW = cardW - 16;
            int barH = 8;
            UIHelper.drawProgressBar(g, barX, barY, barW, barH, progress, STAT_COLORS[i]);
        }
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean consumed) {
        if (consumed) {
            return super.mouseClicked(event, consumed);
        }
        int mx = (int)event.x();
        int my = (int)event.y();
        if (mx >= this.backX && mx < this.backX + this.backW && my >= this.backY && my < this.backY + this.backH) {
            if (this.minecraft != null) {
                this.minecraft.setScreen(this.parent);
            }
            return true;
        }
        return super.mouseClicked(event, consumed);
    }

    public boolean isPauseScreen() {
        return false;
    }
}


package com.ultra.megamod.feature.computer.screen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ultra.megamod.feature.computer.network.ComputerActionPayload;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.ui.UIHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.ArrayList;
import java.util.List;

/**
 * Computer app screen displaying weekly challenges with progress bars.
 */
public class ChallengesScreen extends Screen {
    private final Screen parent;
    private boolean dataLoaded = false;
    private int refreshTimer = 0;

    private final List<ChallengeEntry> challenges = new ArrayList<>();
    private long nextWeekMs = 0;

    private record ChallengeEntry(String name, String type, int target, int progress,
                                   String tree, int rewardXp, int rewardCoins, boolean done) {}

    public ChallengesScreen(Screen parent) {
        super(Component.literal("Weekly Challenges"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        requestData();
    }

    private void requestData() {
        ClientPacketDistributor.sendToServer(
            (CustomPacketPayload) new ComputerActionPayload("challenges_request", ""),
            new CustomPacketPayload[0]);
    }

    @Override
    public void tick() {
        super.tick();
        ComputerDataPayload resp = ComputerDataPayload.lastResponse;
        if (resp != null && "challenges_data".equals(resp.dataType())) {
            ComputerDataPayload.lastResponse = null;
            parseData(resp.jsonData());
            dataLoaded = true;
        }
        // Consume error responses so the screen doesn't stay stuck
        if (resp != null && "error".equals(resp.dataType())) {
            ComputerDataPayload.lastResponse = null;
            this.dataLoaded = true;
        }
        if (++refreshTimer >= 600) { // Refresh every 30s
            refreshTimer = 0;
            requestData();
        }
    }

    private void parseData(String json) {
        try {
            challenges.clear();
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            nextWeekMs = root.get("nextWeekMs").getAsLong();
            JsonArray arr = root.getAsJsonArray("challenges");
            for (JsonElement el : arr) {
                JsonObject obj = el.getAsJsonObject();
                challenges.add(new ChallengeEntry(
                    obj.get("name").getAsString(),
                    obj.get("type").getAsString(),
                    obj.get("target").getAsInt(),
                    obj.get("progress").getAsInt(),
                    obj.get("tree").getAsString(),
                    obj.get("rewardXp").getAsInt(),
                    obj.get("rewardCoins").getAsInt(),
                    obj.get("done").getAsBoolean()
                ));
            }
        } catch (Exception e) {
            // ignore parse errors
        }
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        UIHelper.drawScreenBg(g, 0, 0, width, height);

        int pw = Math.min(360, width - 40);
        int px = (width - pw) / 2;
        int py = 30;

        // Title
        UIHelper.drawPanel(g, px, py, pw, height - 60);
        String title = "\u2605 Weekly Challenges";
        g.drawCenteredString(font, title, width / 2, py + 8, 0xFFFFD700);

        // Countdown
        String countdown = formatCountdown(nextWeekMs);
        g.drawCenteredString(font, "Resets in: " + countdown, width / 2, py + 22, 0xFF888888);

        if (!dataLoaded) {
            g.drawCenteredString(font, "Loading...", width / 2, height / 2, 0xFFAAAAAA);
            super.render(g, mx, my, pt);
            return;
        }

        if (challenges.isEmpty()) {
            g.drawCenteredString(font, "No active challenges", width / 2, height / 2, 0xFFAAAAAA);
            super.render(g, mx, my, pt);
            return;
        }

        // Draw each challenge
        int cy = py + 40;
        int cardH = 64;
        int cardGap = 8;

        for (int i = 0; i < challenges.size(); i++) {
            ChallengeEntry c = challenges.get(i);
            int cardY = cy + i * (cardH + cardGap);

            // Card background
            int bg = c.done ? 0xFF1A2A1A : 0xFF1A1A28;
            g.fill(px + 10, cardY, px + pw - 10, cardY + cardH, bg);
            UIHelper.drawHorizontalDivider(g, px + 10, cardY + cardH, pw - 20);

            // Challenge name
            int nameColor = c.done ? 0xFF44FF44 : 0xFFEEDDCC;
            String statusIcon = c.done ? "\u2714 " : "\u25B6 ";
            g.drawString(font, statusIcon + c.name, px + 16, cardY + 6, nameColor, false);

            // Reward info
            String reward = "+" + c.rewardXp + " " + c.tree + " XP, +" + c.rewardCoins + " MC";
            g.drawString(font, reward, px + pw - 16 - font.width(reward), cardY + 6, 0xFF888888, false);

            // Progress bar
            int barX = px + 16, barY = cardY + 22, barW = pw - 42, barH = 12;
            float pct = c.target > 0 ? (float) c.progress / c.target : 0;
            pct = Math.min(1.0f, pct);

            g.fill(barX, barY, barX + barW, barY + barH, 0xFF222233);
            int fillColor = c.done ? 0xFF44CC44 : 0xFF4488CC;
            g.fill(barX + 1, barY + 1, barX + 1 + (int)((barW - 2) * pct), barY + barH - 1, fillColor);

            // Progress text
            String progText = c.progress + " / " + c.target;
            g.drawCenteredString(font, progText, barX + barW / 2, barY + 2, 0xFFFFFFFF);

            // Type tag
            g.drawString(font, "[" + c.type + "]", px + 16, cardY + 40, 0xFF666677, false);

            if (c.done) {
                g.drawString(font, "COMPLETED!", px + pw - 16 - font.width("COMPLETED!"), cardY + 40, 0xFF44FF44, false);
            }
        }

        // Back button
        int bbW = 60, bbH = 18, bbX = px + 10, bbY = height - 28;
        boolean bbHov = mx >= bbX && mx < bbX + bbW && my >= bbY && my < bbY + bbH;
        UIHelper.drawButton(g, bbX, bbY, bbW, bbH, bbHov);
        g.drawCenteredString(font, "Back", bbX + bbW / 2, bbY + (bbH - 9) / 2, bbHov ? 0xFFFFFFFF : 0xFFAAAAAA);

        super.render(g, mx, my, pt);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean consumed) {
        if (consumed) return super.mouseClicked(event, consumed);
        if (event.button() != 0) return super.mouseClicked(event, consumed);

        int pw = Math.min(360, width - 40);
        int px = (width - pw) / 2;
        int bbW = 60, bbH = 18, bbX = px + 10, bbY = height - 28;
        if (event.x() >= bbX && event.x() < bbX + bbW && event.y() >= bbY && event.y() < bbY + bbH) {
            Minecraft.getInstance().setScreen(parent);
            return true;
        }
        return super.mouseClicked(event, consumed);
    }

    public boolean keyPressed(KeyEvent event) {
        if (event.key() == 256) { // ESC
            Minecraft.getInstance().setScreen(parent);
            return true;
        }
        return super.keyPressed(event);
    }

    private String formatCountdown(long ms) {
        if (ms <= 0) return "Soon...";
        long totalSeconds = ms / 1000;
        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        if (days > 0) return days + "d " + hours + "h";
        if (hours > 0) return hours + "h " + minutes + "m";
        return minutes + "m";
    }
}

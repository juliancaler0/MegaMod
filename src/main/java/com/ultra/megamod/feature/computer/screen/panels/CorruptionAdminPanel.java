package com.ultra.megamod.feature.computer.screen.panels;

import com.ultra.megamod.MegaMod;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ultra.megamod.feature.computer.network.ComputerActionPayload;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.ui.UIHelper;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class CorruptionAdminPanel {
    private final Font font;
    private int scroll = 0;
    private String statusMsg = "";
    private int statusTimer = 0;
    private int selectedZoneId = -1;

    // Cached data
    private List<ZoneEntry> zones = new ArrayList<>();
    private int totalZones = 0;
    private int totalCorruptedChunks = 0;
    private boolean spreadEnabled = true;
    private int maxActiveZones = 8;
    private PurgeInfo activePurge = null;
    private StatsInfo stats = null;

    // Colors
    private static final int ROW_HEIGHT = 18;
    private static final int ROW_EVEN = 0xFF1C2128;
    private static final int ROW_ODD = 0xFF21262D;
    private static final int ROW_SELECTED = 0xFF1A2838;
    private static final int HEADER_COLOR = 0xFF58A6FF;
    private static final int TEXT_PRIMARY = 0xFFE6EDF3;
    private static final int TEXT_SECONDARY = 0xFF8B949E;
    private static final int TEXT_MUTED = 0xFF555566;
    private static final int SUCCESS_GREEN = 0xFF3FB950;
    private static final int ERROR_RED = 0xFFFF6B6B;
    private static final int WARN_YELLOW = 0xFFFFD700;
    private static final int TIER_1_COLOR = 0xFF44FF44;
    private static final int TIER_2_COLOR = 0xFFFFFF00;
    private static final int TIER_3_COLOR = 0xFFFFAA00;
    private static final int TIER_4_COLOR = 0xFFFF4444;
    private static final int CORRUPTION_PURPLE = 0xFF9B59B6;

    private record ZoneEntry(int id, long centerX, long centerZ, int radius, int maxRadius,
                             int tier, int corruptionLevel, String sourceType, boolean active,
                             int chunksAffected, String age) {}

    private record PurgeInfo(int purgeId, int zoneId, String progress, String timeLeft, int participants) {}

    private record StatsInfo(int zonesCreated, int zonesDestroyed, int purgesCompleted, int purgesFailed) {}

    public CorruptionAdminPanel(Font font) {
        this.font = font;
    }

    public void requestData() {
        ClientPacketDistributor.sendToServer(
            (CustomPacketPayload) new ComputerActionPayload("corruption_request", ""),
            (CustomPacketPayload[]) new CustomPacketPayload[0]
        );
    }

    public void handleResponse(String type, String jsonData) {
        if ("corruption_data".equals(type)) {
            parseCorruptionData(jsonData);
        } else if ("corruption_result".equals(type)) {
            try {
                JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
                boolean success = obj.has("success") && obj.get("success").getAsBoolean();
                this.statusMsg = obj.has("message") ? obj.get("message").getAsString() : (success ? "Done!" : "Failed.");
                this.statusTimer = 80;
                requestData(); // Re-fetch after action
            } catch (Exception e) {
                MegaMod.LOGGER.error("Failed to handle corruption admin action", e);
            }
        } else if ("purge_data".equals(type)) {
            try {
                JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
                if (obj.has("active") && obj.get("active").getAsBoolean()) {
                    activePurge = new PurgeInfo(
                        obj.has("purgeId") ? obj.get("purgeId").getAsInt() : 0,
                        obj.has("zoneId") ? obj.get("zoneId").getAsInt() : 0,
                        obj.has("kills") ? obj.get("kills").getAsInt() + "/" + obj.get("killsRequired").getAsInt() : "?",
                        obj.has("timeLeft") ? obj.get("timeLeft").getAsString() : "?",
                        obj.has("participants") ? obj.get("participants").getAsInt() : 0
                    );
                } else {
                    activePurge = null;
                }
            } catch (Exception e) {
                MegaMod.LOGGER.error("Failed to parse corruption status data", e);
            }
        }
    }

    private void parseCorruptionData(String jsonData) {
        try {
            JsonObject root = JsonParser.parseString(jsonData).getAsJsonObject();
            this.zones.clear();

            if (root.has("zones")) {
                JsonArray arr = root.getAsJsonArray("zones");
                for (JsonElement el : arr) {
                    JsonObject z = el.getAsJsonObject();
                    this.zones.add(new ZoneEntry(
                        z.has("id") ? z.get("id").getAsInt() : 0,
                        z.has("centerX") ? z.get("centerX").getAsLong() : 0,
                        z.has("centerZ") ? z.get("centerZ").getAsLong() : 0,
                        z.has("radius") ? z.get("radius").getAsInt() : 0,
                        z.has("maxRadius") ? z.get("maxRadius").getAsInt() : 0,
                        z.has("tier") ? z.get("tier").getAsInt() : 1,
                        z.has("corruptionLevel") ? z.get("corruptionLevel").getAsInt() : 0,
                        z.has("sourceType") ? z.get("sourceType").getAsString() : "natural",
                        z.has("active") && z.get("active").getAsBoolean(),
                        z.has("chunksAffected") ? z.get("chunksAffected").getAsInt() : 0,
                        z.has("age") ? z.get("age").getAsString() : "?"
                    ));
                }
            }

            this.totalZones = root.has("totalZones") ? root.get("totalZones").getAsInt() : 0;
            this.totalCorruptedChunks = root.has("totalCorruptedChunks") ? root.get("totalCorruptedChunks").getAsInt() : 0;
            this.spreadEnabled = !root.has("spreadEnabled") || root.get("spreadEnabled").getAsBoolean();
            this.maxActiveZones = root.has("maxActiveZones") ? root.get("maxActiveZones").getAsInt() : 8;

            if (root.has("activePurge")) {
                JsonObject p = root.getAsJsonObject("activePurge");
                this.activePurge = new PurgeInfo(
                    p.has("purgeId") ? p.get("purgeId").getAsInt() : 0,
                    p.has("zoneId") ? p.get("zoneId").getAsInt() : 0,
                    p.has("progress") ? p.get("progress").getAsString() : "?",
                    p.has("timeLeft") ? p.get("timeLeft").getAsString() : "?",
                    p.has("participants") ? p.get("participants").getAsInt() : 0
                );
            } else {
                this.activePurge = null;
            }

            if (root.has("stats")) {
                JsonObject s = root.getAsJsonObject("stats");
                this.stats = new StatsInfo(
                    s.has("zonesCreated") ? s.get("zonesCreated").getAsInt() : 0,
                    s.has("zonesDestroyed") ? s.get("zonesDestroyed").getAsInt() : 0,
                    s.has("purgesCompleted") ? s.get("purgesCompleted").getAsInt() : 0,
                    s.has("purgesFailed") ? s.get("purgesFailed").getAsInt() : 0
                );
            }
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to parse corruption zone data", e);
        }
    }

    public void tick() {
        if (this.statusTimer > 0) {
            this.statusTimer--;
            if (this.statusTimer <= 0) this.statusMsg = "";
        }
    }

    public void render(GuiGraphics g, int mouseX, int mouseY, int left, int top, int right, int bottom) {
        int w = right - left;
        int y = top + 4;

        // Header
        g.drawString(this.font, "Corruption Management", left + 8, y, CORRUPTION_PURPLE, false);
        y += 14;

        // Action buttons row
        int btnW = 70;
        int btnH = 14;
        int gap = 4;
        int btnX = left + 8;

        // Refresh button
        boolean refreshHover = isHovered(mouseX, mouseY, btnX, y, btnW, btnH);
        UIHelper.drawButton(g, btnX, y, btnW, btnH, refreshHover);
        g.drawString(this.font, "Refresh", btnX + (btnW - this.font.width("Refresh")) / 2, y + 3, HEADER_COLOR, false);
        btnX += btnW + gap;

        // Create Zone button
        int createW = 80;
        boolean createHover = isHovered(mouseX, mouseY, btnX, y, createW, btnH);
        UIHelper.drawButton(g, btnX, y, createW, btnH, createHover);
        g.drawString(this.font, "Create Zone", btnX + (createW - this.font.width("Create Zone")) / 2, y + 3, SUCCESS_GREEN, false);
        btnX += createW + gap;

        // Clear All button
        int clearW = 65;
        boolean clearHover = isHovered(mouseX, mouseY, btnX, y, clearW, btnH);
        UIHelper.drawButton(g, btnX, y, clearW, btnH, clearHover);
        g.drawString(this.font, "Clear All", btnX + (clearW - this.font.width("Clear All")) / 2, y + 3, ERROR_RED, false);
        btnX += clearW + gap;

        // Toggle Spread button
        int toggleW = 80;
        boolean toggleHover = isHovered(mouseX, mouseY, btnX, y, toggleW, btnH);
        UIHelper.drawButton(g, btnX, y, toggleW, btnH, toggleHover);
        String toggleLabel = spreadEnabled ? "Spread: ON" : "Spread: OFF";
        int toggleColor = spreadEnabled ? SUCCESS_GREEN : ERROR_RED;
        g.drawString(this.font, toggleLabel, btnX + (toggleW - this.font.width(toggleLabel)) / 2, y + 3, toggleColor, false);

        y += btnH + 8;

        // Statistics row
        UIHelper.drawInsetPanel(g, left + 4, y, w - 8, 36);
        int statY = y + 4;
        g.drawString(this.font, "Active: " + totalZones, left + 10, statY, TEXT_PRIMARY, false);
        g.drawString(this.font, "Chunks: ~" + totalCorruptedChunks, left + 80, statY, TEXT_PRIMARY, false);
        g.drawString(this.font, "Max: " + maxActiveZones, left + 180, statY, TEXT_SECONDARY, false);
        statY += 12;
        if (stats != null) {
            g.drawString(this.font, "Created: " + stats.zonesCreated() + "  Destroyed: " + stats.zonesDestroyed() +
                    "  Purges: " + stats.purgesCompleted() + " OK / " + stats.purgesFailed() + " failed",
                    left + 10, statY, TEXT_MUTED, false);
        }
        y += 40;

        // Active Purge section
        if (activePurge != null) {
            UIHelper.drawInsetPanel(g, left + 4, y, w - 8, 34);
            g.drawString(this.font, "PURGE ACTIVE", left + 10, y + 4, WARN_YELLOW, false);
            g.drawString(this.font, "Zone #" + activePurge.zoneId() + " | " + activePurge.progress() +
                    " kills | " + activePurge.timeLeft() + " left | " + activePurge.participants() + " players",
                    left + 10, y + 16, TEXT_PRIMARY, false);

            // Stop Purge button
            int stopW = 75;
            int stopX = right - stopW - 10;
            boolean stopHover = isHovered(mouseX, mouseY, stopX, y + 4, stopW, 12);
            UIHelper.drawButton(g, stopX, y + 4, stopW, 12, stopHover);
            g.drawString(this.font, "Stop Purge", stopX + (stopW - this.font.width("Stop Purge")) / 2, y + 6, ERROR_RED, false);

            y += 38;
        }

        // Zone list
        g.drawString(this.font, "Corruption Zones", left + 8, y, HEADER_COLOR, false);
        y += 12;

        // List area
        int listTop = y;
        int listBottom = bottom - 24;
        int listX = left + 4;
        int listW = w - 8;

        UIHelper.drawInsetPanel(g, listX, listTop, listW, listBottom - listTop);
        listTop += 2;
        listBottom -= 2;
        listX += 2;
        listW -= 4;

        g.enableScissor(listX, listTop, listX + listW, listBottom);

        int visibleH = listBottom - listTop;
        int totalH = (this.zones.size() + 1) * ROW_HEIGHT; // +1 for header
        int maxScroll = Math.max(0, totalH - visibleH);
        this.scroll = Math.max(0, Math.min(this.scroll, maxScroll));

        int rowY = listTop - this.scroll;

        // Table header
        g.fill(listX, rowY, listX + listW, rowY + ROW_HEIGHT, 0xFF0D1117);
        g.drawString(this.font, "ID", listX + 4, rowY + 5, HEADER_COLOR, false);
        g.drawString(this.font, "Center", listX + 30, rowY + 5, HEADER_COLOR, false);
        g.drawString(this.font, "Radius", listX + 120, rowY + 5, HEADER_COLOR, false);
        g.drawString(this.font, "Tier", listX + 170, rowY + 5, HEADER_COLOR, false);
        g.drawString(this.font, "Source", listX + 200, rowY + 5, HEADER_COLOR, false);
        g.drawString(this.font, "Age", listX + 260, rowY + 5, HEADER_COLOR, false);
        g.drawString(this.font, "Actions", listX + 310, rowY + 5, HEADER_COLOR, false);
        rowY += ROW_HEIGHT;

        if (this.zones.isEmpty()) {
            g.drawString(this.font, "No corruption zones active.", listX + 10, rowY + 5, TEXT_SECONDARY, false);
        }

        for (int i = 0; i < this.zones.size(); i++) {
            if (rowY + ROW_HEIGHT < listTop || rowY > listBottom) {
                rowY += ROW_HEIGHT;
                continue;
            }

            ZoneEntry zone = this.zones.get(i);
            boolean isSelected = zone.id() == selectedZoneId;
            int rowBg = isSelected ? ROW_SELECTED : (i % 2 == 0 ? ROW_EVEN : ROW_ODD);
            g.fill(listX, rowY, listX + listW, rowY + ROW_HEIGHT, rowBg);

            g.drawString(this.font, "#" + zone.id(), listX + 4, rowY + 5, TEXT_PRIMARY, false);
            g.drawString(this.font, zone.centerX() + ", " + zone.centerZ(), listX + 30, rowY + 5, TEXT_SECONDARY, false);
            g.drawString(this.font, zone.radius() + "/" + zone.maxRadius(), listX + 120, rowY + 5, TEXT_SECONDARY, false);

            int tierColor = switch (zone.tier()) {
                case 1 -> TIER_1_COLOR;
                case 2 -> TIER_2_COLOR;
                case 3 -> TIER_3_COLOR;
                case 4 -> TIER_4_COLOR;
                default -> TEXT_PRIMARY;
            };
            g.drawString(this.font, "T" + zone.tier(), listX + 170, rowY + 5, tierColor, false);
            g.drawString(this.font, zone.sourceType(), listX + 200, rowY + 5, TEXT_MUTED, false);
            g.drawString(this.font, zone.age(), listX + 260, rowY + 5, TEXT_MUTED, false);

            // Action buttons: Purge | Tier+ | Del
            int actX = listX + 310;
            int actY = rowY + 2;
            int actBtnH = ROW_HEIGHT - 4;
            int purgeW = 35;
            boolean purgeHover = isHovered(mouseX, mouseY, actX, actY, purgeW, actBtnH) && mouseY >= listTop && mouseY < listBottom;
            UIHelper.drawButton(g, actX, actY, purgeW, actBtnH, purgeHover);
            g.drawString(this.font, "Prg", actX + (purgeW - this.font.width("Prg")) / 2, actY + 2, WARN_YELLOW, false);

            actX += purgeW + 2;
            int tierBtnW = 25;
            boolean tierHover = isHovered(mouseX, mouseY, actX, actY, tierBtnW, actBtnH) && mouseY >= listTop && mouseY < listBottom;
            UIHelper.drawButton(g, actX, actY, tierBtnW, actBtnH, tierHover);
            g.drawString(this.font, "T+", actX + (tierBtnW - this.font.width("T+")) / 2, actY + 2, TIER_3_COLOR, false);

            actX += tierBtnW + 2;
            int delBtnW = 25;
            boolean delHover = isHovered(mouseX, mouseY, actX, actY, delBtnW, actBtnH) && mouseY >= listTop && mouseY < listBottom;
            UIHelper.drawButton(g, actX, actY, delBtnW, actBtnH, delHover);
            g.drawString(this.font, "X", actX + (delBtnW - this.font.width("X")) / 2, actY + 2, ERROR_RED, false);

            rowY += ROW_HEIGHT;
        }

        g.disableScissor();

        // Scrollbar
        if (totalH > visibleH) {
            float progress = maxScroll > 0 ? (float) this.scroll / (float) maxScroll : 0.0f;
            UIHelper.drawScrollbar(g, listX + listW + 2, listTop, listBottom - listTop, progress);
        }

        // Selected zone details
        if (selectedZoneId >= 0) {
            ZoneEntry sel = null;
            for (ZoneEntry z : zones) {
                if (z.id() == selectedZoneId) { sel = z; break; }
            }
            if (sel != null) {
                int detailY = bottom - 22;
                g.drawString(this.font, "Zone #" + sel.id() + ": " + sel.chunksAffected() + " chunks, corruption " +
                        sel.corruptionLevel() + "%, " + sel.sourceType(), left + 8, detailY, TEXT_SECONDARY, false);
            }
        }

        // Status message
        if (!this.statusMsg.isEmpty() && this.statusTimer > 0) {
            int msgW = this.font.width(this.statusMsg) + 20;
            int msgX = left + (w - msgW) / 2;
            int msgY = bottom - 20;
            UIHelper.drawCard(g, msgX, msgY, msgW, 16);
            boolean isError = this.statusMsg.contains("Failed") || this.statusMsg.contains("Error") || this.statusMsg.contains("error");
            g.drawCenteredString(this.font, this.statusMsg, left + w / 2, msgY + 4, isError ? ERROR_RED : SUCCESS_GREEN);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button, int left, int top, int right, int bottom) {
        int w = right - left;
        int y = top + 18; // after header

        int btnW = 70;
        int btnH = 14;
        int gap = 4;
        int btnX = left + 8;

        // Refresh button
        if (isHovered((int) mouseX, (int) mouseY, btnX, y, btnW, btnH)) {
            requestData();
            return true;
        }
        btnX += btnW + gap;

        // Create Zone button
        int createW = 80;
        if (isHovered((int) mouseX, (int) mouseY, btnX, y, createW, btnH)) {
            // Create zone at player position with default tier 1
            ClientPacketDistributor.sendToServer(
                (CustomPacketPayload) new ComputerActionPayload("corruption_create_zone", "{\"tier\":1}"),
                (CustomPacketPayload[]) new CustomPacketPayload[0]
            );
            return true;
        }
        btnX += createW + gap;

        // Clear All button
        int clearW = 65;
        if (isHovered((int) mouseX, (int) mouseY, btnX, y, clearW, btnH)) {
            ClientPacketDistributor.sendToServer(
                (CustomPacketPayload) new ComputerActionPayload("corruption_clear_all", ""),
                (CustomPacketPayload[]) new CustomPacketPayload[0]
            );
            return true;
        }
        btnX += clearW + gap;

        // Toggle Spread button
        int toggleW = 80;
        if (isHovered((int) mouseX, (int) mouseY, btnX, y, toggleW, btnH)) {
            ClientPacketDistributor.sendToServer(
                (CustomPacketPayload) new ComputerActionPayload("corruption_toggle", ""),
                (CustomPacketPayload[]) new CustomPacketPayload[0]
            );
            return true;
        }

        y += btnH + 8 + 40; // past stats panel

        // Stop Purge button (if purge is active)
        if (activePurge != null) {
            int stopW = 75;
            int stopX = right - stopW - 10;
            if (isHovered((int) mouseX, (int) mouseY, stopX, y - 38 + 4, stopW, 12)) {
                ClientPacketDistributor.sendToServer(
                    (CustomPacketPayload) new ComputerActionPayload("corruption_purge_stop", ""),
                    (CustomPacketPayload[]) new CustomPacketPayload[0]
                );
                return true;
            }
            y += 38;
        }

        // Zone list header offset
        y += 12;
        int listTop2 = y + 2 + ROW_HEIGHT; // skip header row
        int listX = left + 6;
        int listW = w - 12;
        int listBottom2 = bottom - 26;

        // Zone rows
        for (int i = 0; i < this.zones.size(); i++) {
            int rowY = listTop2 + i * ROW_HEIGHT - this.scroll;
            if (rowY + ROW_HEIGHT < listTop2 || rowY > listBottom2) continue;

            ZoneEntry zone = this.zones.get(i);

            // Row click = select
            if (mouseX >= listX && mouseX < listX + listW && mouseY >= rowY && mouseY < rowY + ROW_HEIGHT
                    && mouseY >= listTop2 && mouseY < listBottom2) {

                // Check action buttons first
                int actX = listX + 310;
                int actY = rowY + 2;
                int actBtnH = ROW_HEIGHT - 4;

                // Purge button
                int purgeW = 35;
                if (mouseX >= actX && mouseX < actX + purgeW && mouseY >= actY && mouseY < actY + actBtnH) {
                    ClientPacketDistributor.sendToServer(
                        (CustomPacketPayload) new ComputerActionPayload("corruption_start_purge", String.valueOf(zone.id())),
                        (CustomPacketPayload[]) new CustomPacketPayload[0]
                    );
                    return true;
                }
                actX += purgeW + 2;

                // Tier+ button
                int tierBtnW = 25;
                if (mouseX >= actX && mouseX < actX + tierBtnW && mouseY >= actY && mouseY < actY + actBtnH) {
                    int newTier = Math.min(4, zone.tier() + 1);
                    ClientPacketDistributor.sendToServer(
                        (CustomPacketPayload) new ComputerActionPayload("corruption_set_tier", "{\"id\":" + zone.id() + ",\"tier\":" + newTier + "}"),
                        (CustomPacketPayload[]) new CustomPacketPayload[0]
                    );
                    return true;
                }
                actX += tierBtnW + 2;

                // Delete button
                int delBtnW = 25;
                if (mouseX >= actX && mouseX < actX + delBtnW && mouseY >= actY && mouseY < actY + actBtnH) {
                    ClientPacketDistributor.sendToServer(
                        (CustomPacketPayload) new ComputerActionPayload("corruption_remove_zone", String.valueOf(zone.id())),
                        (CustomPacketPayload[]) new CustomPacketPayload[0]
                    );
                    return true;
                }

                // Otherwise, select the row
                selectedZoneId = zone.id();
                return true;
            }
        }

        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        this.scroll = Math.max(0, this.scroll - (int)(scrollY * ROW_HEIGHT));
        return true;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    public boolean charTyped(char ch, int modifiers) {
        return false;
    }

    private static boolean isHovered(int mouseX, int mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }
}

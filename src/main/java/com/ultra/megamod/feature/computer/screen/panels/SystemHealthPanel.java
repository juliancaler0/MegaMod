package com.ultra.megamod.feature.computer.screen.panels;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ultra.megamod.feature.computer.network.ComputerActionPayload;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.*;

/**
 * System health monitoring panel: TPS, entity counts, memory, dimensions, feature toggles, error log.
 */
public class SystemHealthPanel {

    private static final int BG = 0xFF12121A;
    private static final int CARD_BG = 0xFF1C1C26;
    private static final int BORDER = 0xFF3A3A48;
    private static final int TEXT = 0xFFE6EDF3;
    private static final int LABEL = 0xFF8B949E;
    private static final int ACCENT = 0xFF58A6FF;
    private static final int SUCCESS = 0xFF4CAF50;
    private static final int WARNING = 0xFFD29922;
    private static final int ERROR = 0xFFF85149;
    private static final int PURPLE = 0xFFA371F7;
    private static final int ROW_EVEN = 0xFF1E1E2A;
    private static final int ROW_ODD = 0xFF222234;
    private static final int HEADER_BG = 0xFF161B22;
    private static final int ROW_HEIGHT = 14;

    private final Font font;
    private int scroll = 0;
    private int maxScroll = 0;
    private int refreshTicks = 0;
    private boolean dataLoaded = false;

    // Server stats
    private double tps, mspt;
    private long memUsed, memMax, memTotal;
    private int entityTotal, entityPlayers, entityMobs, entityCitizens, entityItems;
    private int loadedChunks;

    // Dimensions
    private final List<DimensionEntry> dimensions = new ArrayList<>();

    // Persistence file sizes
    private final List<String[]> persistFiles = new ArrayList<>(); // [filename, sizeKB]

    // Feature toggles summary
    private int togglesEnabled, togglesDisabled;
    private final List<String[]> togglesList = new ArrayList<>(); // [feature, enabled]

    // Error log
    private final List<String> errorLog = new ArrayList<>();

    // Issues detected
    private final List<String[]> issues = new ArrayList<>(); // [severity, description, link]

    // Button bounds
    private final List<int[]> actionBtns = new ArrayList<>();

    public record DimensionEntry(String name, int playerCount, int entityCount, int chunkCount) {}

    public SystemHealthPanel(Font font) {
        this.font = font;
    }

    public void requestData() {
        scroll = 0;
        sendAction("system_health_request", "");
    }

    private void sendAction(String action, String data) {
        ClientPacketDistributor.sendToServer(
                (CustomPacketPayload) new ComputerActionPayload(action, data),
                (CustomPacketPayload[]) new CustomPacketPayload[0]);
    }

    public void tick() {
        refreshTicks++;
        if (refreshTicks % 60 == 0) requestData(); // Refresh every 3 seconds

        ComputerDataPayload response = ComputerDataPayload.lastResponse;
        if (response != null && "system_health_data".equals(response.dataType())) {
            handleResponse(response.dataType(), response.jsonData());
            ComputerDataPayload.lastResponse = null;
        }
    }

    public void handleResponse(String type, String jsonData) {
        if (!"system_health_data".equals(type)) return;
        try {
            JsonObject root = JsonParser.parseString(jsonData).getAsJsonObject();

            tps = getDouble(root, "tps");
            mspt = getDouble(root, "mspt");
            memUsed = root.has("memUsed") ? root.get("memUsed").getAsLong() : 0;
            memMax = root.has("memMax") ? root.get("memMax").getAsLong() : 0;
            memTotal = root.has("memTotal") ? root.get("memTotal").getAsLong() : 0;

            entityTotal = getInt(root, "entityTotal");
            entityPlayers = getInt(root, "entityPlayers");
            entityMobs = getInt(root, "entityMobs");
            entityCitizens = getInt(root, "entityCitizens");
            entityItems = getInt(root, "entityItems");
            loadedChunks = getInt(root, "loadedChunks");

            dimensions.clear();
            if (root.has("dimensions")) {
                for (JsonElement el : root.getAsJsonArray("dimensions")) {
                    JsonObject d = el.getAsJsonObject();
                    dimensions.add(new DimensionEntry(
                            getString(d, "name"), getInt(d, "players"),
                            getInt(d, "entities"), getInt(d, "chunks")
                    ));
                }
            }

            persistFiles.clear();
            if (root.has("persistFiles")) {
                for (JsonElement el : root.getAsJsonArray("persistFiles")) {
                    JsonObject pf = el.getAsJsonObject();
                    persistFiles.add(new String[]{getString(pf, "name"), getString(pf, "size")});
                }
            }

            togglesEnabled = getInt(root, "togglesEnabled");
            togglesDisabled = getInt(root, "togglesDisabled");
            togglesList.clear();
            if (root.has("toggles")) {
                for (JsonElement el : root.getAsJsonArray("toggles")) {
                    JsonObject t = el.getAsJsonObject();
                    togglesList.add(new String[]{getString(t, "feature"), t.has("enabled") && t.get("enabled").getAsBoolean() ? "true" : "false"});
                }
            }

            errorLog.clear();
            if (root.has("errorLog")) {
                for (JsonElement el : root.getAsJsonArray("errorLog")) {
                    errorLog.add(el.getAsString());
                }
            }

            issues.clear();
            if (root.has("issues")) {
                for (JsonElement el : root.getAsJsonArray("issues")) {
                    JsonObject is = el.getAsJsonObject();
                    issues.add(new String[]{getString(is, "severity"), getString(is, "description"), getString(is, "link")});
                }
            }

            dataLoaded = true;
        } catch (Exception ignored) {}
    }

    public void render(GuiGraphics g, int mouseX, int mouseY, int left, int top, int right, int bottom) {
        int w = right - left;
        g.fill(left, top, right, bottom, BG);

        actionBtns.clear();

        int contentLeft = left + 4;
        int contentRight = right - 4;
        int contentW = contentRight - contentLeft;
        int contentTop = top + 2;

        g.enableScissor(contentLeft, contentTop, contentRight, bottom - 2);
        int y = contentTop - scroll;

        // Title
        renderSectionHeader(g, contentLeft, y, contentW, "System Health Monitor");
        y += 18;

        if (!dataLoaded) {
            g.drawString(font, "Loading system health data...", contentLeft + 4, y + 10, LABEL, false);
            g.disableScissor();
            return;
        }

        // ---- TPS & Performance ----
        int cardW = (contentW - 10) / 3;
        int tpsColor = tps >= 18 ? SUCCESS : (tps >= 15 ? WARNING : ERROR);
        renderStatCard(g, contentLeft, y, cardW, 46, "TPS", String.format("%.1f / 20.0", tps),
                String.format("MSPT: %.1fms", mspt), tpsColor, mouseX, mouseY);

        long memPct = memMax > 0 ? (memUsed * 100 / memMax) : 0;
        int memColor = memPct < 70 ? SUCCESS : (memPct < 85 ? WARNING : ERROR);
        renderStatCard(g, contentLeft + cardW + 5, y, cardW, 46, "Memory",
                memUsed + "MB / " + memMax + "MB",
                memPct + "% used", memColor, mouseX, mouseY);

        renderStatCard(g, contentLeft + (cardW + 5) * 2, y, cardW, 46, "Entities",
                String.valueOf(entityTotal),
                "Chunks: " + loadedChunks, ACCENT, mouseX, mouseY);
        y += 50;

        // TPS bar
        int barW = contentW;
        int barH = 8;
        g.fill(contentLeft, y, contentLeft + barW, y + barH, 0xFF21262D);
        int tpsFill = (int)(tps / 20.0 * barW);
        if (tpsFill > 0) g.fill(contentLeft, y, contentLeft + Math.min(tpsFill, barW), y + barH, tpsColor);
        y += barH + 4;

        // Memory bar
        g.fill(contentLeft, y, contentLeft + barW, y + barH, 0xFF21262D);
        int memFill = (int)((double) memUsed / Math.max(1, memMax) * barW);
        if (memFill > 0) g.fill(contentLeft, y, contentLeft + Math.min(memFill, barW), y + barH, memColor);
        y += barH + 6;

        // ---- Entity Breakdown ----
        renderSectionHeader(g, contentLeft, y, contentW, "Entity Breakdown");
        y += 18;

        g.fill(contentLeft, y, contentLeft + contentW, y + 42, CARD_BG);
        drawBorder(g, contentLeft, y, contentLeft + contentW, y + 42, BORDER);

        int colW = contentW / 4;
        g.drawString(font, "Players: " + entityPlayers, contentLeft + 6, y + 4, ACCENT, false);
        g.drawString(font, "Mobs: " + entityMobs, contentLeft + colW, y + 4, ERROR, false);
        g.drawString(font, "Citizens: " + entityCitizens, contentLeft + colW * 2, y + 4, SUCCESS, false);
        g.drawString(font, "Items: " + entityItems, contentLeft + colW * 3, y + 4, WARNING, false);

        // Entity composition bar
        int barY = y + 18;
        int totalEntities = Math.max(1, entityPlayers + entityMobs + entityCitizens + entityItems);
        int pW = (int)((double) entityPlayers / totalEntities * (contentW - 12));
        int mW = (int)((double) entityMobs / totalEntities * (contentW - 12));
        int cW = (int)((double) entityCitizens / totalEntities * (contentW - 12));
        int iW = (int)((double) entityItems / totalEntities * (contentW - 12));
        int bx = contentLeft + 6;
        if (pW > 0) { g.fill(bx, barY, bx + pW, barY + 6, ACCENT); bx += pW; }
        if (mW > 0) { g.fill(bx, barY, bx + mW, barY + 6, ERROR); bx += mW; }
        if (cW > 0) { g.fill(bx, barY, bx + cW, barY + 6, SUCCESS); bx += cW; }
        if (iW > 0) { g.fill(bx, barY, bx + iW, barY + 6, WARNING); }

        // Other count
        int otherCount = entityTotal - entityPlayers - entityMobs - entityCitizens - entityItems;
        g.drawString(font, "Other: " + Math.max(0, otherCount) + "  |  Total: " + entityTotal, contentLeft + 6, y + 30, LABEL, false);
        y += 46;

        // ---- Dimensions ----
        y += 4;
        renderSectionHeader(g, contentLeft, y, contentW, "Active Dimensions (" + dimensions.size() + ")");
        y += 18;

        g.fill(contentLeft, y, contentLeft + contentW, y + 14, HEADER_BG);
        g.drawString(font, "Dimension", contentLeft + 4, y + 3, LABEL, false);
        g.drawString(font, "Players", contentLeft + 180, y + 3, LABEL, false);
        g.drawString(font, "Entities", contentLeft + 240, y + 3, LABEL, false);
        g.drawString(font, "Chunks", contentLeft + 310, y + 3, LABEL, false);
        y += 15;

        for (int i = 0; i < dimensions.size(); i++) {
            DimensionEntry d = dimensions.get(i);
            boolean alt = i % 2 == 0;
            g.fill(contentLeft, y, contentLeft + contentW, y + ROW_HEIGHT, alt ? ROW_EVEN : ROW_ODD);

            g.drawString(font, truncate(d.name, 24), contentLeft + 4, y + 3, TEXT, false);
            g.drawString(font, String.valueOf(d.playerCount), contentLeft + 180, y + 3, d.playerCount > 0 ? ACCENT : LABEL, false);
            g.drawString(font, String.valueOf(d.entityCount), contentLeft + 240, y + 3, TEXT, false);
            g.drawString(font, String.valueOf(d.chunkCount), contentLeft + 310, y + 3, TEXT, false);
            y += ROW_HEIGHT;
        }

        // ---- Persistence Files ----
        y += 8;
        renderSectionHeader(g, contentLeft, y, contentW, "Persistence Files");
        y += 18;

        for (int i = 0; i < persistFiles.size(); i++) {
            String[] pf = persistFiles.get(i);
            boolean alt = i % 2 == 0;
            g.fill(contentLeft, y, contentLeft + contentW, y + 13, alt ? ROW_EVEN : ROW_ODD);
            g.drawString(font, pf[0], contentLeft + 4, y + 2, TEXT, false);
            int sw = font.width(pf[1]);
            g.drawString(font, pf[1], contentLeft + contentW - sw - 4, y + 2, LABEL, false);
            y += 14;
        }

        if (persistFiles.isEmpty()) {
            g.fill(contentLeft, y, contentLeft + contentW, y + 16, CARD_BG);
            g.drawString(font, "No persistence files found.", contentLeft + 8, y + 4, LABEL, false);
            y += 18;
        }

        // ---- Feature Toggles Summary ----
        y += 8;
        renderSectionHeader(g, contentLeft, y, contentW, "Feature Toggles (" + togglesEnabled + " on / " + togglesDisabled + " off)");
        y += 18;

        // Render as a compact grid
        int tColW = (contentW - 4) / 3;
        int tIdx = 0;
        for (String[] t : togglesList) {
            int tx = contentLeft + (tIdx % 3) * tColW;
            int ty = y + (tIdx / 3) * 13;
            boolean on = "true".equals(t[1]);
            g.drawString(font, (on ? "\u25CF " : "\u25CB ") + truncate(t[0], 14), tx, ty + 2, on ? SUCCESS : LABEL, false);
            tIdx++;
        }
        y += (togglesList.size() / 3 + 1) * 13 + 4;

        // ---- Issues Detected ----
        if (!issues.isEmpty()) {
            y += 4;
            renderSectionHeader(g, contentLeft, y, contentW, "Issues Detected (" + issues.size() + ")");
            y += 18;

            for (int i = 0; i < issues.size(); i++) {
                String[] is = issues.get(i);
                int severityColor = "ERROR".equals(is[0]) ? ERROR : ("WARN".equals(is[0]) ? WARNING : ACCENT);
                g.fill(contentLeft, y, contentLeft + contentW, y + 16, CARD_BG);
                g.fill(contentLeft, y, contentLeft + 3, y + 16, severityColor);
                g.drawString(font, "[" + is[0] + "] " + is[1], contentLeft + 6, y + 4, TEXT, false);
                y += 18;
            }
        }

        // ---- Error/Warning Log ----
        y += 8;
        renderSectionHeader(g, contentLeft, y, contentW, "Recent Log (" + errorLog.size() + " entries)");
        y += 18;

        // Refresh button
        int refreshBtnW = 60;
        int refreshBtnX = contentLeft + contentW - refreshBtnW;
        int refreshBtnY = y - 16;
        boolean refreshHov = mouseX >= refreshBtnX && mouseX < refreshBtnX + refreshBtnW && mouseY >= refreshBtnY && mouseY < refreshBtnY + 14;
        g.fill(refreshBtnX, refreshBtnY, refreshBtnX + refreshBtnW, refreshBtnY + 14, refreshHov ? 0xFF2A3A2A : CARD_BG);
        drawBorder(g, refreshBtnX, refreshBtnY, refreshBtnX + refreshBtnW, refreshBtnY + 14, refreshHov ? SUCCESS : BORDER);
        g.drawString(font, "Refresh", refreshBtnX + 10, refreshBtnY + 3, SUCCESS, false);
        actionBtns.add(new int[]{refreshBtnX, refreshBtnY, refreshBtnW, 14, 0});

        for (int i = 0; i < errorLog.size(); i++) {
            String line = errorLog.get(i);
            boolean alt = i % 2 == 0;
            int lineColor = line.contains("ERROR") ? ERROR : (line.contains("WARN") ? WARNING : LABEL);
            g.fill(contentLeft, y, contentLeft + contentW, y + 11, alt ? ROW_EVEN : ROW_ODD);
            g.drawString(font, truncate(line, 60), contentLeft + 4, y + 1, lineColor, false);
            y += 12;
        }

        if (errorLog.isEmpty()) {
            g.fill(contentLeft, y, contentLeft + contentW, y + 16, CARD_BG);
            g.drawString(font, "No recent log entries.", contentLeft + 8, y + 4, SUCCESS, false);
            y += 18;
        }

        y += 20;
        g.disableScissor();

        int totalContentH = y + scroll - (top + 2);
        maxScroll = Math.max(0, totalContentH - (bottom - 2 - top - 2));

        if (maxScroll > 0) {
            int barX = right - 4;
            int areaH = bottom - 2 - top - 2;
            g.fill(barX, top + 2, barX + 3, bottom - 2, 0xFF21262D);
            int thumbH = Math.max(15, (int)((float) areaH / totalContentH * areaH));
            int thumbY = top + 2 + (int)((float) scroll / maxScroll * (areaH - thumbH));
            g.fill(barX, thumbY, barX + 3, thumbY + thumbH, LABEL);
        }

        return;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button, int left, int top, int right, int bottom) {
        int mx = (int) mouseX;
        int my = (int) mouseY;

        for (int[] ab : actionBtns) {
            if (mx >= ab[0] && mx < ab[0] + ab[2] && my >= ab[1] && my < ab[1] + ab[3]) {
                if (ab[4] == 0) {
                    requestData();
                }
                return true;
            }
        }
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scroll = Math.max(0, Math.min(maxScroll, scroll - (int)(scrollY * 12)));
        return true;
    }

    // Helpers
    private void renderSectionHeader(GuiGraphics g, int left, int y, int w, String title) {
        g.fill(left, y, left + w, y + 16, HEADER_BG);
        g.fill(left, y + 16, left + w, y + 17, BORDER);
        g.drawString(font, title, left + 4, y + 4, ACCENT, false);
    }

    private void renderStatCard(GuiGraphics g, int x, int y, int w, int h, String label, String value, String sub, int color, int mx, int my) {
        boolean hov = mx >= x && mx < x + w && my >= y && my < y + h;
        g.fill(x, y, x + w, y + h, hov ? 0xFF222230 : CARD_BG);
        drawBorder(g, x, y, x + w, y + h, hov ? color : BORDER);
        g.drawString(font, label, x + 4, y + 4, LABEL, false);
        g.drawString(font, value, x + 4, y + 16, color, false);
        if (sub != null) g.drawString(font, sub, x + 4, y + 28, LABEL, false);
    }

    private void drawBorder(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        g.fill(x1, y1, x2, y1 + 1, color);
        g.fill(x1, y2 - 1, x2, y2, color);
        g.fill(x1, y1, x1 + 1, y2, color);
        g.fill(x2 - 1, y1, x2, y2, color);
    }

    private String truncate(String s, int max) { return s.length() > max ? s.substring(0, max) + ".." : s; }
    private int getInt(JsonObject o, String k) { return o.has(k) ? o.get(k).getAsInt() : 0; }
    private double getDouble(JsonObject o, String k) { return o.has(k) ? o.get(k).getAsDouble() : 0.0; }
    private String getString(JsonObject o, String k) { return o.has(k) ? o.get(k).getAsString() : ""; }
}

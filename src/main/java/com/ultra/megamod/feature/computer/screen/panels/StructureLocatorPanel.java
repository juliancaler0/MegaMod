package com.ultra.megamod.feature.computer.screen.panels;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ultra.megamod.feature.computer.network.ComputerActionPayload;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class StructureLocatorPanel {
    private final Font font;

    // Visual style constants
    private static final int BG = 0xFF0D1117;
    private static final int HEADER_BG = 0xFF161B22;
    private static final int BORDER = 0xFF30363D;
    private static final int TEXT = 0xFFE6EDF3;
    private static final int LABEL = 0xFF8B949E;
    private static final int ACCENT = 0xFF58A6FF;
    private static final int CLOSE_COLOR = 0xFF3FB950;
    private static final int MEDIUM_COLOR = 0xFFD29922;
    private static final int FAR_COLOR = 0xFFF85149;

    // Sub-tabs
    private int subTab = 0; // 0=Structures, 1=Biomes
    private static final String[] SUB_TAB_NAMES = {"Structures", "Biomes"};

    // Scroll
    private int scroll = 0;

    // Results
    private List<LocateResult> results = new ArrayList<>();
    private List<LocateResult> recentSearches = new ArrayList<>();
    private String statusMessage = "";
    private int statusColor = LABEL;
    private long statusExpiry = 0;

    // Structures: display name -> locate command ID
    private static final String[][] STRUCTURES = {
        {"Village", "minecraft:village"},
        {"Desert Temple", "minecraft:desert_pyramid"},
        {"Jungle Temple", "minecraft:jungle_pyramid"},
        {"Swamp Hut", "minecraft:swamp_hut"},
        {"Igloo", "minecraft:igloo"},
        {"Stronghold", "minecraft:stronghold"},
        {"Monument", "minecraft:monument"},
        {"Fortress", "minecraft:fortress"},
        {"End City", "minecraft:end_city"},
        {"Mansion", "minecraft:mansion"},
        {"Trail Ruins", "minecraft:trail_ruins"},
        {"Trial Chambers", "minecraft:trial_chambers"},
        {"Ancient City", "minecraft:ancient_city"},
        {"Ruined Portal", "minecraft:ruined_portal"},
        {"Mineshaft", "minecraft:mineshaft"},
        {"Pillager Outpost", "minecraft:pillager_outpost"},
        {"Ocean Ruin", "minecraft:ocean_ruin"},
        {"Shipwreck", "minecraft:shipwreck"},
        {"Bastion", "minecraft:bastion_remnant"}
    };

    // Biomes: display name -> biome ID
    private static final String[][] BIOMES = {
        {"Plains", "minecraft:plains"},
        {"Desert", "minecraft:desert"},
        {"Jungle", "minecraft:jungle"},
        {"Taiga", "minecraft:taiga"},
        {"Snowy Plains", "minecraft:snowy_plains"},
        {"Mushroom Fields", "minecraft:mushroom_fields"},
        {"Deep Dark", "minecraft:deep_dark"},
        {"Cherry Grove", "minecraft:cherry_grove"},
        {"Badlands", "minecraft:badlands"},
        {"Flower Forest", "minecraft:flower_forest"},
        {"Dark Forest", "minecraft:dark_forest"},
        {"Ice Spikes", "minecraft:ice_spikes"},
        {"Mangrove Swamp", "minecraft:mangrove_swamp"},
        {"Meadow", "minecraft:meadow"},
        {"Lush Caves", "minecraft:lush_caves"}
    };

    // Layout constants
    private static final int ROW_H = 12;
    private static final int BTN_H = 16;
    private static final int BTN_PAD = 4;
    private static final int SECTION_GAP = 8;
    private static final int GRID_BTN_W = 90;
    private static final int GRID_BTN_H = 18;
    private static final int GRID_GAP = 3;
    private static final int RESULT_CARD_H = 36;
    private static final int MAX_RECENT = 10;

    public record LocateResult(String name, int x, int y, int z, int distance, String type) {}

    public StructureLocatorPanel(Font font) {
        this.font = font;
    }

    public void render(GuiGraphics g, int mouseX, int mouseY, int left, int top, int right, int bottom) {
        int w = right - left;
        int h = bottom - top;

        // Background
        g.fill(left, top, right, bottom, BG);

        // Title bar
        g.fill(left, top, right, top + 20, HEADER_BG);
        drawBorder(g, left, top, right, top + 20, BORDER);
        g.drawString(font, "Structure & Biome Locator", left + 6, top + 6, ACCENT, false);

        // Sub-tabs
        int tabX = left + 6;
        int tabY = top + 24;
        for (int i = 0; i < SUB_TAB_NAMES.length; i++) {
            int tw = font.width(SUB_TAB_NAMES[i]) + 12;
            boolean active = (i == subTab);
            boolean hovered = isHovered(mouseX, mouseY, tabX, tabY, tabX + tw, tabY + BTN_H);
            int tabBg = active ? 0xFF21262D : (hovered ? 0xFF1C2128 : HEADER_BG);
            int tabBorder = active ? ACCENT : (hovered ? LABEL : BORDER);
            g.fill(tabX, tabY, tabX + tw, tabY + BTN_H, tabBorder);
            g.fill(tabX + 1, tabY + 1, tabX + tw - 1, tabY + BTN_H - 1, tabBg);
            g.drawString(font, SUB_TAB_NAMES[i], tabX + 6, tabY + 4, active ? ACCENT : TEXT, false);
            tabX += tw + 2;
        }

        // Complete All Builds button (admin — instantly finishes all active build orders)
        int completeBtnW = font.width("Complete All Builds") + 12;
        int completeBtnX = right - completeBtnW - 6;
        renderButton(g, mouseX, mouseY, completeBtnX, tabY, completeBtnW, BTN_H, "Complete All Builds", 0xFF3FB950);

        // Locate All button
        int locateAllW = font.width("Locate All") + 12;
        int locateAllX = completeBtnX - locateAllW - 4;
        renderButton(g, mouseX, mouseY, locateAllX, tabY, locateAllW, BTN_H, "Locate All", ACCENT);

        int contentTop = tabY + BTN_H + SECTION_GAP;
        int contentBottom = bottom - 4;

        // Scissor for scrollable content
        g.enableScissor(left, contentTop, right, contentBottom);

        int y = contentTop - scroll;

        if (subTab == 0) {
            y = renderStructureGrid(g, mouseX, mouseY, left, right, y);
        } else {
            y = renderBiomeGrid(g, mouseX, mouseY, left, right, y);
        }

        y += SECTION_GAP;

        // Results section
        if (!results.isEmpty()) {
            g.fill(left + 4, y, right - 4, y + 18, HEADER_BG);
            drawBorder(g, left + 4, y, right - 4, y + 18, BORDER);
            g.drawString(font, "Results (" + results.size() + ")", left + 10, y + 5, TEXT, false);

            // Clear button
            int clearW = font.width("Clear") + 8;
            int clearX = right - clearW - 10;
            renderSmallButton(g, mouseX, mouseY, clearX, y + 2, clearW, 14, "Clear", FAR_COLOR);

            y += 20;
            for (int i = 0; i < results.size(); i++) {
                y = renderResultCard(g, mouseX, mouseY, left + 4, right - 4, y, results.get(i), i, false);
            }
        }

        y += SECTION_GAP;

        // Recent searches section
        if (!recentSearches.isEmpty()) {
            g.fill(left + 4, y, right - 4, y + 18, HEADER_BG);
            drawBorder(g, left + 4, y, right - 4, y + 18, BORDER);
            g.drawString(font, "Recent Searches (" + recentSearches.size() + ")", left + 10, y + 5, LABEL, false);
            y += 20;
            for (int i = 0; i < recentSearches.size(); i++) {
                y = renderResultCard(g, mouseX, mouseY, left + 4, right - 4, y, recentSearches.get(i), i, true);
            }
        }

        g.disableScissor();

        // Status message at the bottom
        if (!statusMessage.isEmpty() && System.currentTimeMillis() < statusExpiry) {
            g.drawString(font, statusMessage, left + 6, bottom - 14, statusColor, false);
        } else {
            statusMessage = "";
        }
    }

    private int renderStructureGrid(GuiGraphics g, int mouseX, int mouseY, int left, int right, int y) {
        int contentW = right - left - 12;
        int cols = Math.max(1, contentW / (GRID_BTN_W + GRID_GAP));
        int x = left + 6;

        g.fill(left + 4, y, right - 4, y + 16, HEADER_BG);
        drawBorder(g, left + 4, y, right - 4, y + 16, BORDER);
        g.drawString(font, "Structures", left + 10, y + 4, TEXT, false);
        y += 18;

        for (int i = 0; i < STRUCTURES.length; i++) {
            int col = i % cols;
            int row = i / cols;
            int bx = x + col * (GRID_BTN_W + GRID_GAP);
            int by = y + row * (GRID_BTN_H + GRID_GAP);

            renderLocateButton(g, mouseX, mouseY, bx, by, GRID_BTN_W, GRID_BTN_H, STRUCTURES[i][0]);
        }

        int totalRows = (STRUCTURES.length + cols - 1) / cols;
        y += totalRows * (GRID_BTN_H + GRID_GAP);

        return y;
    }

    private int renderBiomeGrid(GuiGraphics g, int mouseX, int mouseY, int left, int right, int y) {
        int contentW = right - left - 12;
        int cols = Math.max(1, contentW / (GRID_BTN_W + GRID_GAP));
        int x = left + 6;

        g.fill(left + 4, y, right - 4, y + 16, HEADER_BG);
        drawBorder(g, left + 4, y, right - 4, y + 16, BORDER);
        g.drawString(font, "Biomes", left + 10, y + 4, TEXT, false);
        y += 18;

        for (int i = 0; i < BIOMES.length; i++) {
            int col = i % cols;
            int row = i / cols;
            int bx = x + col * (GRID_BTN_W + GRID_GAP);
            int by = y + row * (GRID_BTN_H + GRID_GAP);

            renderLocateButton(g, mouseX, mouseY, bx, by, GRID_BTN_W, GRID_BTN_H, BIOMES[i][0]);
        }

        int totalRows = (BIOMES.length + cols - 1) / cols;
        y += totalRows * (GRID_BTN_H + GRID_GAP);

        return y;
    }

    private void renderLocateButton(GuiGraphics g, int mouseX, int mouseY, int x, int y, int w, int h, String label) {
        boolean hovered = isHovered(mouseX, mouseY, x, y, x + w, y + h);
        int bg = hovered ? 0xFF21262D : HEADER_BG;
        int border = hovered ? ACCENT : BORDER;
        g.fill(x, y, x + w, y + h, border);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, bg);

        // Truncate label if needed
        String displayLabel = truncate(label, w - 6);
        int textW = font.width(displayLabel);
        g.drawString(font, displayLabel, x + (w - textW) / 2, y + (h - 8) / 2, hovered ? TEXT : LABEL, false);
    }

    private int renderResultCard(GuiGraphics g, int mouseX, int mouseY, int left, int right, int y, LocateResult result, int index, boolean isRecent) {
        int cardH = RESULT_CARD_H;
        int cardW = right - left;

        // Card background with alternating shading
        int cardBg = (index % 2 == 0) ? 0xFF0F1318 : BG;
        g.fill(left, y, right, y + cardH, cardBg);
        drawBorder(g, left, y, right, y + cardH, BORDER);

        // Structure/biome name
        int distColor = getDistanceColor(result.distance);
        String typeTag = result.type.equals("biome") ? "[B]" : "[S]";
        int tagColor = result.type.equals("biome") ? MEDIUM_COLOR : ACCENT;
        g.drawString(font, typeTag, left + 6, y + 4, tagColor, false);
        g.drawString(font, result.name, left + 6 + font.width(typeTag) + 4, y + 4, TEXT, false);

        // Coordinates
        String coords = "X: " + result.x + "  Y: " + result.y + "  Z: " + result.z;
        g.drawString(font, coords, left + 6, y + 16, LABEL, false);

        // Distance with color coding
        String distStr = result.distance + " blocks";
        int distStrW = font.width(distStr);
        g.drawString(font, distStr, right - distStrW - 60, y + 4, distColor, false);

        // Distance indicator dot
        g.fill(right - 58, y + 5, right - 54, y + 9, distColor);

        // Teleport button
        int tpW = 46;
        int tpX = right - tpW - 4;
        int tpY = y + 16;
        int tpH = 14;
        boolean tpHovered = isHovered(mouseX, mouseY, tpX, tpY, tpX + tpW, tpY + tpH);
        int tpBg = tpHovered ? brighten(ACCENT, 0.3f) : darken(ACCENT, 0.6f);
        g.fill(tpX, tpY, tpX + tpW, tpY + tpH, tpBg);
        drawBorder(g, tpX, tpY, tpX + tpW, tpY + tpH, tpHovered ? ACCENT : darken(ACCENT, 0.4f));
        int tpTextW = font.width("Teleport");
        g.drawString(font, "Teleport", tpX + (tpW - tpTextW) / 2, tpY + 3, tpHovered ? 0xFFFFFFFF : ACCENT, false);

        return y + cardH + 2;
    }

    public boolean mouseClicked(double mx, double my, int btn, int left, int top, int right, int bottom) {
        if (btn != 0) return false;
        int mouseX = (int) mx;
        int mouseY = (int) my;
        int w = right - left;

        // Sub-tab clicks
        int tabX = left + 6;
        int tabY = top + 24;
        for (int i = 0; i < SUB_TAB_NAMES.length; i++) {
            int tw = font.width(SUB_TAB_NAMES[i]) + 12;
            if (isHovered(mouseX, mouseY, tabX, tabY, tabX + tw, tabY + BTN_H)) {
                subTab = i;
                scroll = 0;
                return true;
            }
            tabX += tw + 2;
        }

        // Complete All Builds button
        int completeBtnW = font.width("Complete All Builds") + 12;
        int completeBtnX = right - completeBtnW - 6;
        if (isHovered(mouseX, mouseY, completeBtnX, tabY, completeBtnX + completeBtnW, tabY + BTN_H)) {
            ClientPacketDistributor.sendToServer(
                (CustomPacketPayload) new ComputerActionPayload("admin_complete_build_orders", ""),
                (CustomPacketPayload[]) new CustomPacketPayload[0]);
            setStatus("Completing all build orders...", 0xFF3FB950);
            return true;
        }

        // Locate All button
        int locateAllW = font.width("Locate All") + 12;
        int locateAllX = completeBtnX - locateAllW - 4;
        if (isHovered(mouseX, mouseY, locateAllX, tabY, locateAllX + locateAllW, tabY + BTN_H)) {
            if (subTab == 0) {
                ClientPacketDistributor.sendToServer((CustomPacketPayload) new ComputerActionPayload("locate_all_structures", ""), (CustomPacketPayload[]) new CustomPacketPayload[0]);
                setStatus("Locating all structures...", ACCENT);
            } else {
                // Locate all biomes one by one
                for (String[] biome : BIOMES) {
                    ClientPacketDistributor.sendToServer((CustomPacketPayload) new ComputerActionPayload("locate_biome", biome[1]), (CustomPacketPayload[]) new CustomPacketPayload[0]);
                }
                setStatus("Locating all biomes...", ACCENT);
            }
            return true;
        }

        int contentTop = tabY + BTN_H + SECTION_GAP;
        int contentBottom = bottom - 4;

        // Adjust for scroll
        int adjustedMouseY = mouseY + scroll;
        int contentW = right - left - 12;
        int cols = Math.max(1, contentW / (GRID_BTN_W + GRID_GAP));
        int x = left + 6;

        // Grid buttons
        int gridY = contentTop + 18; // after the section header
        if (subTab == 0) {
            for (int i = 0; i < STRUCTURES.length; i++) {
                int col = i % cols;
                int row = i / cols;
                int bx = x + col * (GRID_BTN_W + GRID_GAP);
                int by = gridY + row * (GRID_BTN_H + GRID_GAP) - scroll;

                if (isHovered(mouseX, mouseY, bx, by, bx + GRID_BTN_W, by + GRID_BTN_H)) {
                    if (mouseY >= contentTop && mouseY <= contentBottom) {
                        ClientPacketDistributor.sendToServer((CustomPacketPayload) new ComputerActionPayload("locate_structure", STRUCTURES[i][1]), (CustomPacketPayload[]) new CustomPacketPayload[0]);
                        setStatus("Locating " + STRUCTURES[i][0] + "...", ACCENT);
                        return true;
                    }
                }
            }

            int totalRows = (STRUCTURES.length + cols - 1) / cols;
            gridY += totalRows * (GRID_BTN_H + GRID_GAP);
        } else {
            for (int i = 0; i < BIOMES.length; i++) {
                int col = i % cols;
                int row = i / cols;
                int bx = x + col * (GRID_BTN_W + GRID_GAP);
                int by = gridY + row * (GRID_BTN_H + GRID_GAP) - scroll;

                if (isHovered(mouseX, mouseY, bx, by, bx + GRID_BTN_W, by + GRID_BTN_H)) {
                    if (mouseY >= contentTop && mouseY <= contentBottom) {
                        ClientPacketDistributor.sendToServer((CustomPacketPayload) new ComputerActionPayload("locate_biome", BIOMES[i][1]), (CustomPacketPayload[]) new CustomPacketPayload[0]);
                        setStatus("Locating " + BIOMES[i][0] + "...", ACCENT);
                        return true;
                    }
                }
            }

            int totalRows = (BIOMES.length + cols - 1) / cols;
            gridY += totalRows * (GRID_BTN_H + GRID_GAP);
        }

        // Results section buttons
        int ry = gridY + SECTION_GAP - scroll;
        if (!results.isEmpty()) {
            // Clear button
            int clearW = font.width("Clear") + 8;
            int clearX = right - clearW - 10;
            if (isHovered(mouseX, mouseY, clearX, ry + 2, clearX + clearW, ry + 16)) {
                if (mouseY >= contentTop && mouseY <= contentBottom) {
                    results.clear();
                    return true;
                }
            }

            ry += 20;
            for (int i = 0; i < results.size(); i++) {
                LocateResult result = results.get(i);
                // Teleport button
                int tpW = 46;
                int tpX = right - 4 - tpW - 4;
                int tpY = ry + 16;
                int tpH = 14;
                if (isHovered(mouseX, mouseY, tpX, tpY, tpX + tpW, tpY + tpH)) {
                    if (mouseY >= contentTop && mouseY <= contentBottom) {
                        ClientPacketDistributor.sendToServer((CustomPacketPayload) new ComputerActionPayload("locate_teleport", result.x + ":" + result.y + ":" + result.z), (CustomPacketPayload[]) new CustomPacketPayload[0]);
                        setStatus("Teleporting to " + result.name + "...", CLOSE_COLOR);
                        return true;
                    }
                }
                ry += RESULT_CARD_H + 2;
            }
        }

        ry += SECTION_GAP;

        // Recent searches teleport buttons
        if (!recentSearches.isEmpty()) {
            ry += 20; // header
            for (int i = 0; i < recentSearches.size(); i++) {
                LocateResult result = recentSearches.get(i);
                int tpW = 46;
                int tpX = right - 4 - tpW - 4;
                int tpY = ry + 16;
                int tpH = 14;
                if (isHovered(mouseX, mouseY, tpX, tpY, tpX + tpW, tpY + tpH)) {
                    if (mouseY >= contentTop && mouseY <= contentBottom) {
                        ClientPacketDistributor.sendToServer((CustomPacketPayload) new ComputerActionPayload("locate_teleport", result.x + ":" + result.y + ":" + result.z), (CustomPacketPayload[]) new CustomPacketPayload[0]);
                        setStatus("Teleporting to " + result.name + "...", CLOSE_COLOR);
                        return true;
                    }
                }
                ry += RESULT_CARD_H + 2;
            }
        }

        return false;
    }

    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        scroll = Math.max(0, scroll - (int)(sy * 15));
        return true;
    }

    public void handleResponse(String type, String jsonData) {
        if ("locate_result".equals(type)) {
            parseSingleResult(jsonData);
        } else if ("locate_all_result".equals(type)) {
            parseAllResults(jsonData);
        }
    }

    private void parseSingleResult(String jsonData) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            boolean found = obj.has("found") && obj.get("found").getAsBoolean();
            if (!found) {
                String name = obj.has("name") ? obj.get("name").getAsString() : "Unknown";
                setStatus("Could not find " + name + ".", FAR_COLOR);
                return;
            }

            String name = obj.get("name").getAsString();
            int x = obj.get("x").getAsInt();
            int y = obj.get("y").getAsInt();
            int z = obj.get("z").getAsInt();
            int distance = obj.get("distance").getAsInt();
            String locType = obj.has("locType") ? obj.get("locType").getAsString() : "structure";

            LocateResult result = new LocateResult(name, x, y, z, distance, locType);

            // Add to results (replace if same name exists)
            results.removeIf(r -> r.name.equals(name));
            results.add(result);
            results.sort(Comparator.comparingInt(LocateResult::distance));

            // Add to recent searches
            recentSearches.removeIf(r -> r.name.equals(name));
            recentSearches.add(0, result);
            while (recentSearches.size() > MAX_RECENT) {
                recentSearches.remove(recentSearches.size() - 1);
            }

            setStatus("Found " + name + " at " + distance + " blocks away.", CLOSE_COLOR);
        } catch (Exception e) {
            setStatus("Failed to parse locate result.", FAR_COLOR);
        }
    }

    private void parseAllResults(String jsonData) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            JsonArray arr = obj.getAsJsonArray("results");

            results.clear();
            for (JsonElement el : arr) {
                JsonObject entry = el.getAsJsonObject();
                String name = entry.get("name").getAsString();
                int x = entry.get("x").getAsInt();
                int y = entry.get("y").getAsInt();
                int z = entry.get("z").getAsInt();
                int distance = entry.get("distance").getAsInt();
                String locType = entry.has("locType") ? entry.get("locType").getAsString() : "structure";

                LocateResult result = new LocateResult(name, x, y, z, distance, locType);
                results.add(result);

                // Also add to recent
                recentSearches.removeIf(r -> r.name.equals(name));
                recentSearches.add(0, result);
            }

            results.sort(Comparator.comparingInt(LocateResult::distance));

            while (recentSearches.size() > MAX_RECENT) {
                recentSearches.remove(recentSearches.size() - 1);
            }

            setStatus("Found " + results.size() + " structures.", CLOSE_COLOR);
        } catch (Exception e) {
            setStatus("Failed to parse locate all results.", FAR_COLOR);
        }
    }

    // ---- Helpers ----

    private int getDistanceColor(int distance) {
        if (distance < 500) return CLOSE_COLOR;
        if (distance < 2000) return MEDIUM_COLOR;
        return FAR_COLOR;
    }

    private void setStatus(String message, int color) {
        this.statusMessage = message;
        this.statusColor = color;
        this.statusExpiry = System.currentTimeMillis() + 5000;
    }

    private void renderButton(GuiGraphics g, int mouseX, int mouseY, int x, int y, int w, int h, String label, int color) {
        boolean hovered = isHovered(mouseX, mouseY, x, y, x + w, y + h);
        int bg = hovered ? brighten(color, 0.3f) : darken(color, 0.6f);
        g.fill(x, y, x + w, y + h, bg);
        drawBorder(g, x, y, x + w, y + h, hovered ? color : darken(color, 0.4f));
        int textW = font.width(label);
        int tx = x + (w - textW) / 2;
        int ty = y + (h - 8) / 2;
        g.drawString(font, label, tx, ty, hovered ? 0xFFFFFFFF : color, false);
    }

    private void renderSmallButton(GuiGraphics g, int mouseX, int mouseY, int x, int y, int w, int h, String label, int color) {
        boolean hovered = isHovered(mouseX, mouseY, x, y, x + w, y + h);
        int bg = hovered ? brighten(color, 0.2f) : darken(color, 0.6f);
        g.fill(x, y, x + w, y + h, bg);
        drawBorder(g, x, y, x + w, y + h, hovered ? color : darken(color, 0.4f));
        int textW = font.width(label);
        g.drawString(font, label, x + (w - textW) / 2, y + (h - 8) / 2, hovered ? 0xFFFFFFFF : color, false);
    }

    private static void drawBorder(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        g.fill(x1, y1, x2, y1 + 1, color);
        g.fill(x1, y2 - 1, x2, y2, color);
        g.fill(x1, y1, x1 + 1, y2, color);
        g.fill(x2 - 1, y1, x2, y2, color);
    }

    private static boolean isHovered(int mx, int my, int x1, int y1, int x2, int y2) {
        return mx >= x1 && mx < x2 && my >= y1 && my < y2;
    }

    private String truncate(String text, int maxWidth) {
        if (font.width(text) <= maxWidth) return text;
        String ellipsis = "...";
        int ellipsisW = font.width(ellipsis);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            if (font.width(sb.toString() + text.charAt(i)) + ellipsisW > maxWidth) break;
            sb.append(text.charAt(i));
        }
        return sb + ellipsis;
    }

    private static int darken(int color, float factor) {
        int a = (color >> 24) & 0xFF;
        int r = (int) (((color >> 16) & 0xFF) * factor);
        int g = (int) (((color >> 8) & 0xFF) * factor);
        int b = (int) ((color & 0xFF) * factor);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static int brighten(int color, float factor) {
        int a = (color >> 24) & 0xFF;
        int r = Math.min(255, (int) (((color >> 16) & 0xFF) * (1f + factor)));
        int g = Math.min(255, (int) (((color >> 8) & 0xFF) * (1f + factor)));
        int b = Math.min(255, (int) ((color & 0xFF) * (1f + factor)));
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}

package com.ultra.megamod.feature.computer.screen.panels;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ultra.megamod.feature.computer.network.ComputerActionPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MuseumManagerPanel {
    private final Font font;

    // Visual style constants
    private static final int BG = 0xFF0D1117;
    private static final int HEADER_BG = 0xFF161B22;
    private static final int BORDER = 0xFF30363D;
    private static final int TEXT = 0xFFE6EDF3;
    private static final int LABEL = 0xFF8B949E;
    private static final int ACCENT = 0xFF58A6FF;
    private static final int SUCCESS = 0xFF3FB950;
    private static final int WARNING = 0xFFD29922;
    private static final int ERROR = 0xFFF85149;

    // Player selector state
    private List<String> playerNames = new ArrayList<>();
    private List<String> playerUUIDs = new ArrayList<>();
    private int selectedPlayerIndex = 0;
    private boolean playerDropdownOpen = false;
    private int playerDropdownScroll = 0;

    // Museum data from server
    private String dataPlayerName = "";
    private int itemsDonated = 0, itemsTotal = 0;
    private int aquariumDonated = 0, aquariumTotal = 0;
    private int wildlifeDonated = 0, wildlifeTotal = 0;
    private int artDonated = 0, artTotal = 0;
    private int achievementsDonated = 0, achievementsTotal = 0;
    private List<String> missingItems = new ArrayList<>();
    private List<String> missingAquarium = new ArrayList<>();
    private List<String> missingWildlife = new ArrayList<>();
    private List<String> missingArt = new ArrayList<>();
    private List<String> missingAchievements = new ArrayList<>();
    private boolean dataLoaded = false;

    // Missing items scroll
    private int missingScroll = 0;
    private int selectedWingTab = 0;
    private static final String[] WING_NAMES = {"Items", "Aquarium", "Wildlife", "Art", "Achievements"};
    private static final String[] WING_KEYS = {"items", "aquarium", "wildlife", "art", "achievements"};

    // Status message
    private String statusMessage = "";
    private int statusColor = TEXT;
    private long statusExpiry = 0;

    // Auto-refresh
    private int refreshCooldown = 0;

    // Layout constants
    private static final int ROW_H = 12;
    private static final int BTN_H = 16;
    private static final int BTN_PAD = 4;
    private static final int SECTION_GAP = 8;
    private static final int BAR_H = 8;
    private static final int DROPDOWN_ITEM_H = 14;
    private static final int DROPDOWN_MAX_VISIBLE = 8;

    public MuseumManagerPanel(Font font) {
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
        g.drawString(font, "Museum Manager", left + 6, top + 6, ACCENT, false);

        int y = top + 24;

        // Player selector section
        g.drawString(font, "Player:", left + 6, y + 3, LABEL, false);
        int selectorX = left + 50;
        int selectorW = Math.min(160, w - 56);
        int selectorH = 16;

        // Dropdown button
        int selBg = isHovered(mouseX, mouseY, selectorX, y, selectorX + selectorW, y + selectorH) ? 0xFF21262D : HEADER_BG;
        g.fill(selectorX, y, selectorX + selectorW, y + selectorH, selBg);
        drawBorder(g, selectorX, y, selectorX + selectorW, y + selectorH, BORDER);
        String displayName = playerNames.isEmpty() ? "No players" : playerNames.get(selectedPlayerIndex);
        g.drawString(font, truncate(displayName, selectorW - 16), selectorX + 4, y + 4, TEXT, false);
        g.drawString(font, playerDropdownOpen ? "^" : "v", selectorX + selectorW - 10, y + 4, LABEL, false);

        // Refresh button next to player selector
        int refreshX = selectorX + selectorW + 4;
        int refreshW = 50;
        renderButton(g, mouseX, mouseY, refreshX, y, refreshW, selectorH, "Fetch", ACCENT);

        y += selectorH + SECTION_GAP;

        if (!dataLoaded) {
            g.drawString(font, "Select a player and click Fetch to load data.", left + 6, y, LABEL, false);
        } else {
            // Stats card
            int cardLeft = left + 4;
            int cardRight = right - 4;
            int cardTop = y;

            g.fill(cardLeft, cardTop, cardRight, cardTop + 18, HEADER_BG);
            drawBorder(g, cardLeft, cardTop, cardRight, cardTop + 18, BORDER);
            g.drawString(font, "Museum Stats: " + dataPlayerName, cardLeft + 6, cardTop + 5, TEXT, false);

            int overallDonated = itemsDonated + aquariumDonated + wildlifeDonated + artDonated + achievementsDonated;
            int overallTotal = itemsTotal + aquariumTotal + wildlifeTotal + artTotal + achievementsTotal;
            int overallPct = overallTotal > 0 ? (overallDonated * 100 / overallTotal) : 0;
            String overallStr = overallPct + "% Complete";
            int overallColor = overallPct >= 100 ? SUCCESS : (overallPct >= 50 ? WARNING : ERROR);
            g.drawString(font, overallStr, cardRight - font.width(overallStr) - 6, cardTop + 5, overallColor, false);

            y = cardTop + 22;

            // Wing stats rows with progress bars and action buttons
            y = renderWingRow(g, mouseX, mouseY, cardLeft, cardRight, y, "Items", itemsDonated, itemsTotal, 0);
            y = renderWingRow(g, mouseX, mouseY, cardLeft, cardRight, y, "Aquarium", aquariumDonated, aquariumTotal, 1);
            y = renderWingRow(g, mouseX, mouseY, cardLeft, cardRight, y, "Wildlife", wildlifeDonated, wildlifeTotal, 2);
            y = renderWingRow(g, mouseX, mouseY, cardLeft, cardRight, y, "Art", artDonated, artTotal, 3);
            y = renderWingRow(g, mouseX, mouseY, cardLeft, cardRight, y, "Achievements", achievementsDonated, achievementsTotal, 4);

            y += SECTION_GAP;

            // Global action buttons
            int btnW = 70;
            int gx = cardLeft + 6;
            renderButton(g, mouseX, mouseY, gx, y, btnW, BTN_H, "Fill All", SUCCESS);
            gx += btnW + BTN_PAD;
            renderButton(g, mouseX, mouseY, gx, y, btnW, BTN_H, "Clear All", ERROR);
            gx += btnW + BTN_PAD;
            int resetW = 110;
            renderButton(g, mouseX, mouseY, gx, y, resetW, BTN_H, "Reset Structure", WARNING);

            y += BTN_H + SECTION_GAP;

            // Missing items section with wing tabs
            int missingTop = y;
            int missingBottom = bottom - 4;
            if (missingBottom - missingTop > 30) {
                g.fill(cardLeft, missingTop, cardRight, missingTop + 18, HEADER_BG);
                drawBorder(g, cardLeft, missingTop, cardRight, missingTop + 18, BORDER);
                g.drawString(font, "Missing Items", cardLeft + 6, missingTop + 5, TEXT, false);

                // Wing tabs for missing items
                int tabX = cardLeft + 80;
                for (int i = 0; i < WING_NAMES.length; i++) {
                    int tw = font.width(WING_NAMES[i]) + 8;
                    int tabColor = (i == selectedWingTab) ? ACCENT : LABEL;
                    if (isHovered(mouseX, mouseY, tabX, missingTop + 1, tabX + tw, missingTop + 17)) {
                        tabColor = TEXT;
                    }
                    if (i == selectedWingTab) {
                        g.fill(tabX, missingTop + 15, tabX + tw, missingTop + 17, ACCENT);
                    }
                    g.drawString(font, WING_NAMES[i], tabX + 4, missingTop + 5, tabColor, false);
                    tabX += tw + 2;
                }

                int listTop = missingTop + 20;
                int listBottom = missingBottom;
                g.fill(cardLeft, listTop, cardRight, listBottom, BG);
                drawBorder(g, cardLeft, listTop, cardRight, listBottom, BORDER);

                List<String> currentMissing = getMissingForWing(selectedWingTab);
                int visibleLines = (listBottom - listTop - 4) / ROW_H;
                int maxScroll = Math.max(0, currentMissing.size() - visibleLines);
                missingScroll = Math.min(missingScroll, maxScroll);

                if (currentMissing.isEmpty()) {
                    g.drawString(font, "All donated!", cardLeft + 6, listTop + 4, SUCCESS, false);
                } else {
                    // Enable scissor for clipping
                    g.enableScissor(cardLeft + 1, listTop + 1, cardRight - 1, listBottom - 1);
                    int iy = listTop + 2;
                    for (int i = missingScroll; i < currentMissing.size() && iy < listBottom; i++) {
                        String entry = currentMissing.get(i);
                        int lineColor = (i % 2 == 0) ? TEXT : LABEL;
                        g.drawString(font, truncate(entry, cardRight - cardLeft - 16), cardLeft + 6, iy, lineColor, false);
                        iy += ROW_H;
                    }
                    g.disableScissor();

                    // Scrollbar
                    if (currentMissing.size() > visibleLines) {
                        int scrollbarH = listBottom - listTop - 4;
                        int thumbH = Math.max(10, scrollbarH * visibleLines / currentMissing.size());
                        int thumbY = listTop + 2 + (maxScroll > 0 ? (missingScroll * (scrollbarH - thumbH) / maxScroll) : 0);
                        g.fill(cardRight - 4, listTop + 2, cardRight - 1, listBottom - 2, HEADER_BG);
                        g.fill(cardRight - 4, thumbY, cardRight - 1, thumbY + thumbH, LABEL);
                    }
                }
            }
        }

        // Status message
        if (!statusMessage.isEmpty() && System.currentTimeMillis() < statusExpiry) {
            g.drawString(font, statusMessage, left + 6, bottom - 14, statusColor, false);
        } else {
            statusMessage = "";
        }

        // Render dropdown LAST (on top of everything)
        if (playerDropdownOpen && !playerNames.isEmpty()) {
            int ddX = left + 50;
            int ddW = Math.min(160, w - 56);
            int ddY = top + 24 + 16;
            int ddVisible = Math.min(DROPDOWN_MAX_VISIBLE, playerNames.size());
            int ddH = ddVisible * DROPDOWN_ITEM_H + 2;
            g.fill(ddX, ddY, ddX + ddW, ddY + ddH, 0xFF1C2128);
            drawBorder(g, ddX, ddY, ddX + ddW, ddY + ddH, ACCENT);

            for (int i = 0; i < ddVisible; i++) {
                int idx = playerDropdownScroll + i;
                if (idx >= playerNames.size()) break;
                int ry = ddY + 1 + i * DROPDOWN_ITEM_H;
                boolean hovered = isHovered(mouseX, mouseY, ddX, ry, ddX + ddW, ry + DROPDOWN_ITEM_H);
                if (hovered) {
                    g.fill(ddX + 1, ry, ddX + ddW - 1, ry + DROPDOWN_ITEM_H, 0xFF30363D);
                }
                int tc = (idx == selectedPlayerIndex) ? ACCENT : TEXT;
                g.drawString(font, truncate(playerNames.get(idx), ddW - 8), ddX + 4, ry + 3, tc, false);
            }

            // Dropdown scrollbar
            if (playerNames.size() > DROPDOWN_MAX_VISIBLE) {
                int sbH = ddH - 4;
                int thumbH = Math.max(6, sbH * DROPDOWN_MAX_VISIBLE / playerNames.size());
                int maxSc = playerNames.size() - DROPDOWN_MAX_VISIBLE;
                int thumbY = ddY + 2 + (maxSc > 0 ? (playerDropdownScroll * (sbH - thumbH) / maxSc) : 0);
                g.fill(ddX + ddW - 4, ddY + 2, ddX + ddW - 1, ddY + ddH - 2, HEADER_BG);
                g.fill(ddX + ddW - 4, thumbY, ddX + ddW - 1, thumbY + thumbH, LABEL);
            }
        }
    }

    private int renderWingRow(GuiGraphics g, int mouseX, int mouseY, int cardLeft, int cardRight, int y, String name, int donated, int total, int wingIndex) {
        int rowH = 20;
        int pad = 6;
        int labelW = 80;
        int btnW = 36;
        int barLeft = cardLeft + pad + labelW;
        int barRight = cardRight - pad - btnW * 2 - BTN_PAD * 2 - 4;
        int barW = barRight - barLeft;

        // Alternate row background
        if (wingIndex % 2 == 0) {
            g.fill(cardLeft + 1, y, cardRight - 1, y + rowH, 0xFF0F1318);
        }

        // Label
        g.drawString(font, name, cardLeft + pad, y + 6, LABEL, false);

        // Count text
        int pct = total > 0 ? (donated * 100 / total) : 0;
        String countStr = donated + "/" + total + " (" + pct + "%)";
        int countColor = pct >= 100 ? SUCCESS : (pct >= 50 ? WARNING : TEXT);
        g.drawString(font, countStr, barLeft, y + 2, countColor, false);

        // Progress bar
        int barY = y + 12;
        g.fill(barLeft, barY, barLeft + barW, barY + BAR_H, 0xFF21262D);
        if (total > 0) {
            int fillW = Math.min(barW, barW * donated / total);
            int barColor = pct >= 100 ? SUCCESS : (pct >= 50 ? WARNING : ACCENT);
            if (fillW > 0) {
                g.fill(barLeft, barY, barLeft + fillW, barY + BAR_H, barColor);
            }
        }
        drawBorder(g, barLeft, barY, barLeft + barW, barY + BAR_H, BORDER);

        // Fill/Clear buttons
        int bx = barRight + 4;
        renderButton(g, mouseX, mouseY, bx, y + 2, btnW, BTN_H, "Fill", SUCCESS);
        bx += btnW + BTN_PAD;
        renderButton(g, mouseX, mouseY, bx, y + 2, btnW, BTN_H, "Clear", ERROR);

        return y + rowH;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button, int left, int top, int right, int bottom) {
        if (button != 0) return false;
        int mx = (int) mouseX;
        int my = (int) mouseY;
        int w = right - left;

        // If dropdown is open, check dropdown clicks first
        if (playerDropdownOpen && !playerNames.isEmpty()) {
            int ddX = left + 50;
            int ddW = Math.min(160, w - 56);
            int ddY = top + 24 + 16;
            int ddVisible = Math.min(DROPDOWN_MAX_VISIBLE, playerNames.size());
            int ddH = ddVisible * DROPDOWN_ITEM_H + 2;

            if (isHovered(mx, my, ddX, ddY, ddX + ddW, ddY + ddH)) {
                int relY = my - ddY - 1;
                int clickIdx = playerDropdownScroll + relY / DROPDOWN_ITEM_H;
                if (clickIdx >= 0 && clickIdx < playerNames.size()) {
                    selectedPlayerIndex = clickIdx;
                    playerDropdownOpen = false;
                    return true;
                }
            }
            // Click outside dropdown = close it
            playerDropdownOpen = false;
            return true;
        }

        int y = top + 24;
        int selectorX = left + 50;
        int selectorW = Math.min(160, w - 56);
        int selectorH = 16;

        // Player selector dropdown toggle
        if (isHovered(mx, my, selectorX, y, selectorX + selectorW, y + selectorH)) {
            refreshPlayerList();
            playerDropdownOpen = !playerDropdownOpen;
            playerDropdownScroll = 0;
            return true;
        }

        // Fetch button
        int refreshX = selectorX + selectorW + 4;
        int refreshW = 50;
        if (isHovered(mx, my, refreshX, y, refreshX + refreshW, y + selectorH)) {
            refreshPlayerList();
            requestData();
            return true;
        }

        y += selectorH + SECTION_GAP;

        if (!dataLoaded) return false;

        // Skip the stats card header
        y += 22;

        // Wing row buttons - each wing row is 20px high
        int cardLeft = left + 4;
        int cardRight = right - 4;
        for (int i = 0; i < 5; i++) {
            int rowY = y + i * 20;
            int btnW = 36;
            int labelW = 80;
            int barRight2 = cardRight - 6 - btnW * 2 - BTN_PAD * 2 - 4;
            int bx = barRight2 + 4;

            // Fill button for wing
            if (isHovered(mx, my, bx, rowY + 2, bx + btnW, rowY + 2 + BTN_H)) {
                sendWingAction("museum_fill_wing", i);
                setStatus("Filling " + WING_NAMES[i] + "...", SUCCESS);
                return true;
            }

            // Clear button for wing
            bx += btnW + BTN_PAD;
            if (isHovered(mx, my, bx, rowY + 2, bx + btnW, rowY + 2 + BTN_H)) {
                sendWingAction("museum_clear_wing", i);
                setStatus("Clearing " + WING_NAMES[i] + "...", WARNING);
                return true;
            }
        }

        y += 5 * 20 + SECTION_GAP;

        // Global action buttons
        int btnW = 70;
        int gx = cardLeft + 6;

        // Fill All
        if (isHovered(mx, my, gx, y, gx + btnW, y + BTN_H)) {
            sendGlobalAction("museum_fill_all");
            setStatus("Filling all wings...", SUCCESS);
            return true;
        }
        gx += btnW + BTN_PAD;

        // Clear All
        if (isHovered(mx, my, gx, y, gx + btnW, y + BTN_H)) {
            sendGlobalAction("museum_clear_all");
            setStatus("Clearing all wings...", WARNING);
            return true;
        }
        gx += btnW + BTN_PAD;

        // Reset Structure
        int resetW = 110;
        if (isHovered(mx, my, gx, y, gx + resetW, y + BTN_H)) {
            sendGlobalAction("museum_reset_structure");
            setStatus("Resetting museum structure...", WARNING);
            return true;
        }

        y += BTN_H + SECTION_GAP;

        // Missing items wing tabs
        int missingTop = y;
        if (missingTop + 18 < bottom - 4) {
            int tabX = cardLeft + 80;
            for (int i = 0; i < WING_NAMES.length; i++) {
                int tw = font.width(WING_NAMES[i]) + 8;
                if (isHovered(mx, my, tabX, missingTop + 1, tabX + tw, missingTop + 17)) {
                    selectedWingTab = i;
                    missingScroll = 0;
                    return true;
                }
                tabX += tw + 2;
            }
        }

        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (playerDropdownOpen) {
            int maxScroll = Math.max(0, playerNames.size() - DROPDOWN_MAX_VISIBLE);
            playerDropdownScroll = Math.max(0, Math.min(maxScroll, playerDropdownScroll - (int) scrollY));
            return true;
        }

        List<String> currentMissing = getMissingForWing(selectedWingTab);
        if (!currentMissing.isEmpty()) {
            missingScroll = Math.max(0, missingScroll - (int) scrollY * 3);
            return true;
        }
        return false;
    }

    public void handleResponse(String type, String jsonData) {
        if ("museum_manager_data".equals(type)) {
            parseMuseumData(jsonData);
        } else if ("museum_action_result".equals(type)) {
            try {
                JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
                boolean success = obj.has("success") && obj.get("success").getAsBoolean();
                String msg = obj.has("message") ? obj.get("message").getAsString() : (success ? "Done!" : "Failed.");
                setStatus(msg, success ? SUCCESS : ERROR);
                if (success) {
                    // Auto-refresh after a successful action
                    refreshCooldown = 10;
                }
            } catch (Exception e) {
                setStatus("Action completed.", ACCENT);
                refreshCooldown = 10;
            }
        }
    }

    public void requestData() {
        refreshPlayerList();
        if (playerUUIDs.isEmpty()) {
            setStatus("No players online.", ERROR);
            return;
        }
        if (selectedPlayerIndex < 0 || selectedPlayerIndex >= playerUUIDs.size()) {
            selectedPlayerIndex = 0;
        }
        String uuid = playerUUIDs.get(selectedPlayerIndex);
        ClientPacketDistributor.sendToServer((CustomPacketPayload) new ComputerActionPayload("museum_request_data", uuid), (CustomPacketPayload[]) new CustomPacketPayload[0]);
        setStatus("Loading museum data...", ACCENT);
    }

    public void tick() {
        if (refreshCooldown > 0) {
            refreshCooldown--;
            if (refreshCooldown == 0 && dataLoaded && !playerUUIDs.isEmpty()) {
                requestData();
            }
        }
    }

    // ---- Private helpers ----

    private void parseMuseumData(String jsonData) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            dataPlayerName = obj.has("playerName") ? obj.get("playerName").getAsString() : "Unknown";

            JsonObject items = obj.getAsJsonObject("items");
            itemsDonated = items.get("donated").getAsInt();
            itemsTotal = items.get("total").getAsInt();
            missingItems = parseStringArray(items.getAsJsonArray("missing"));

            JsonObject aquarium = obj.getAsJsonObject("aquarium");
            aquariumDonated = aquarium.get("donated").getAsInt();
            aquariumTotal = aquarium.get("total").getAsInt();
            missingAquarium = parseStringArray(aquarium.getAsJsonArray("missing"));

            JsonObject wildlife = obj.getAsJsonObject("wildlife");
            wildlifeDonated = wildlife.get("donated").getAsInt();
            wildlifeTotal = wildlife.get("total").getAsInt();
            missingWildlife = parseStringArray(wildlife.getAsJsonArray("missing"));

            JsonObject art = obj.getAsJsonObject("art");
            artDonated = art.get("donated").getAsInt();
            artTotal = art.get("total").getAsInt();
            missingArt = parseStringArray(art.getAsJsonArray("missing"));

            JsonObject achievements = obj.getAsJsonObject("achievements");
            achievementsDonated = achievements.get("donated").getAsInt();
            achievementsTotal = achievements.get("total").getAsInt();
            missingAchievements = parseStringArray(achievements.getAsJsonArray("missing"));

            dataLoaded = true;
            missingScroll = 0;
            setStatus("Data loaded for " + dataPlayerName, SUCCESS);
        } catch (Exception e) {
            setStatus("Failed to parse museum data.", ERROR);
            dataLoaded = false;
        }
    }

    private List<String> parseStringArray(JsonArray arr) {
        List<String> result = new ArrayList<>();
        if (arr == null) return result;
        for (JsonElement el : arr) {
            result.add(el.getAsString());
        }
        return result;
    }

    private void refreshPlayerList() {
        playerNames.clear();
        playerUUIDs.clear();
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() != null) {
            Collection<PlayerInfo> players = mc.getConnection().getOnlinePlayers();
            for (PlayerInfo info : players) {
                String name = info.getProfile().name();
                String uuid = info.getProfile().id().toString();
                playerNames.add(name);
                playerUUIDs.add(uuid);
            }
        }
        if (selectedPlayerIndex >= playerNames.size()) {
            selectedPlayerIndex = Math.max(0, playerNames.size() - 1);
        }
    }

    private void sendWingAction(String action, int wingIndex) {
        if (playerUUIDs.isEmpty() || selectedPlayerIndex >= playerUUIDs.size()) return;
        String uuid = playerUUIDs.get(selectedPlayerIndex);
        String wingKey = WING_KEYS[wingIndex];
        ClientPacketDistributor.sendToServer((CustomPacketPayload) new ComputerActionPayload(action, uuid + ":" + wingKey), (CustomPacketPayload[]) new CustomPacketPayload[0]);
    }

    private void sendGlobalAction(String action) {
        if (playerUUIDs.isEmpty() || selectedPlayerIndex >= playerUUIDs.size()) return;
        String uuid = playerUUIDs.get(selectedPlayerIndex);
        ClientPacketDistributor.sendToServer((CustomPacketPayload) new ComputerActionPayload(action, uuid), (CustomPacketPayload[]) new CustomPacketPayload[0]);
    }

    private List<String> getMissingForWing(int wingTab) {
        switch (wingTab) {
            case 0: return missingItems;
            case 1: return missingAquarium;
            case 2: return missingWildlife;
            case 3: return missingArt;
            case 4: return missingAchievements;
            default: return missingItems;
        }
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

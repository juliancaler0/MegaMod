package com.ultra.megamod.feature.computer.screen.panels;

import com.ultra.megamod.MegaMod;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ultra.megamod.feature.computer.network.ComputerActionPayload;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class FeatureTogglesPanel {

    // Visual style constants
    private static final int BG_COLOR = 0xFF0D1117;
    private static final int HEADER_COLOR = 0xFF161B22;
    private static final int BORDER_COLOR = 0xFF30363D;
    private static final int TEXT_COLOR = 0xFFE6EDF3;
    private static final int LABEL_COLOR = 0xFF8B949E;
    private static final int ACCENT_BLUE = 0xFF58A6FF;
    private static final int TOGGLE_ON = 0xFF3FB950;
    private static final int TOGGLE_OFF = 0xFFF85149;

    private static final int CAT_VANILLA = 0xFF58A6FF;
    private static final int CAT_ECONOMY = 0xFFD29922;
    private static final int CAT_COMBAT = 0xFFF85149;
    private static final int CAT_EXPLORATION = 0xFF3FB950;
    private static final int CAT_ADMIN = 0xFFA371F7;
    private static final int CAT_MULTIPLAYER = 0xFF79C0FF;
    private static final int CAT_DECORATION = 0xFFE09956;
    private static final int CAT_QOL = 0xFF56D4DD;
    private static final int CAT_COLONY = 0xFFD2A062;

    private static final String[] CATEGORIES = {"All", "Vanilla Refresh", "Economy", "Combat", "Exploration", "Admin", "Multiplayer", "Decoration", "QoL", "Colony"};

    private static final int ROW_HEIGHT = 28;
    private static final int SEARCH_HEIGHT = 16;
    private static final int CATEGORY_TAB_HEIGHT = 18;
    private static final int BULK_BAR_HEIGHT = 22;

    private final Font font;
    private String searchText = "";
    private String selectedCategory = "All";
    private int scroll = 0;
    private int maxScroll = 0;
    private final List<FeatureEntry> features = new ArrayList<>();
    private final List<FeatureEntry> filteredFeatures = new ArrayList<>();
    private int enabledCount = 0;
    private int totalCount = 0;
    private boolean searchFocused = false;
    private int searchCursorPos = 0;
    private long searchBlinkTick = 0;
    private int builderSpeedMultiplier = 1;

    public record FeatureEntry(String id, String name, String category, String description, boolean enabled) {}

    public FeatureTogglesPanel(Font font) {
        this.font = font;
    }

    public void render(GuiGraphics g, int mouseX, int mouseY, int left, int top, int right, int bottom) {
        int panelW = right - left;
        int panelH = bottom - top;

        // Background fill
        g.fill(left, top, right, bottom, BG_COLOR);

        // --- Header: Title + active count ---
        int headerY = top;
        int headerH = 20;
        g.fill(left, headerY, right, headerY + headerH, HEADER_COLOR);
        g.fill(left, headerY + headerH - 1, right, headerY + headerH, BORDER_COLOR);

        g.drawString(font, "Feature Toggles", left + 6, headerY + 6, TEXT_COLOR, false);
        String countStr = enabledCount + "/" + totalCount + " enabled";
        int countW = font.width(countStr);
        int countColor = enabledCount == totalCount ? TOGGLE_ON : (enabledCount == 0 ? TOGGLE_OFF : ACCENT_BLUE);
        g.drawString(font, countStr, right - countW - 6, headerY + 6, countColor, false);

        int yOffset = headerY + headerH + 2;

        // --- Category filter tabs ---
        int catTabX = left + 4;
        int catTabY = yOffset;
        for (int i = 0; i < CATEGORIES.length; i++) {
            String cat = CATEGORIES[i];
            int tw = font.width(cat) + 10;
            boolean isSelected = selectedCategory.equals(cat);
            boolean isHovered = mouseX >= catTabX && mouseX < catTabX + tw && mouseY >= catTabY && mouseY < catTabY + CATEGORY_TAB_HEIGHT;

            int bgCol = isSelected ? ACCENT_BLUE : (isHovered ? 0xFF21262D : HEADER_COLOR);
            int txtCol = isSelected ? 0xFF000000 : TEXT_COLOR;

            g.fill(catTabX, catTabY, catTabX + tw, catTabY + CATEGORY_TAB_HEIGHT, bgCol);
            if (!isSelected) {
                // Border
                drawRectOutline(g, catTabX, catTabY, catTabX + tw, catTabY + CATEGORY_TAB_HEIGHT, BORDER_COLOR);
            }
            g.drawString(font, cat, catTabX + 5, catTabY + 5, txtCol, false);
            catTabX += tw + 3;
        }

        yOffset = catTabY + CATEGORY_TAB_HEIGHT + 4;

        // --- Search box ---
        int searchX = left + 4;
        int searchY = yOffset;
        int searchW = panelW - 8;
        g.fill(searchX, searchY, searchX + searchW, searchY + SEARCH_HEIGHT, 0xFF21262D);
        drawRectOutline(g, searchX, searchY, searchX + searchW, searchY + SEARCH_HEIGHT, searchFocused ? ACCENT_BLUE : BORDER_COLOR);

        String displaySearch = searchText.isEmpty() && !searchFocused ? "Search features..." : searchText;
        int searchTextColor = searchText.isEmpty() && !searchFocused ? LABEL_COLOR : TEXT_COLOR;
        g.drawString(font, displaySearch, searchX + 4, searchY + 4, searchTextColor, false);

        // Cursor blink
        if (searchFocused) {
            long now = System.currentTimeMillis();
            if ((now / 500) % 2 == 0) {
                int cursorX = searchX + 4 + font.width(searchText.substring(0, Math.min(searchCursorPos, searchText.length())));
                g.fill(cursorX, searchY + 3, cursorX + 1, searchY + SEARCH_HEIGHT - 3, TEXT_COLOR);
            }
        }

        yOffset = searchY + SEARCH_HEIGHT + 4;

        // --- Feature list (scrollable) ---
        int listTop = yOffset;
        int listBottom = bottom - BULK_BAR_HEIGHT - 4;
        int listH = listBottom - listTop;
        int visibleRows = listH / ROW_HEIGHT;

        // Clip rendering to list area
        updateFiltered();

        // Build row positions (some features have sub-rows that add extra height)
        int[] rowYPositions = new int[filteredFeatures.size()];
        int cumulativeY = 0;
        for (int i = 0; i < filteredFeatures.size(); i++) {
            rowYPositions[i] = cumulativeY;
            cumulativeY += ROW_HEIGHT;
            // Add sub-row height for builder_admin_bypass when enabled
            if ("builder_admin_bypass".equals(filteredFeatures.get(i).id()) && filteredFeatures.get(i).enabled()) {
                cumulativeY += ROW_HEIGHT;
            }
        }
        int totalContentHeight = cumulativeY;

        maxScroll = Math.max(0, totalContentHeight - listH);
        if (scroll > maxScroll) scroll = maxScroll;
        if (scroll < 0) scroll = 0;

        // Render feature rows
        g.enableScissor(left, listTop, right, listBottom);
        for (int i = 0; i < filteredFeatures.size(); i++) {
            FeatureEntry fe = filteredFeatures.get(i);
            int rowY = listTop + rowYPositions[i] - scroll;

            // Skip offscreen rows
            if (rowY + ROW_HEIGHT < listTop || rowY > listBottom) continue;

            boolean rowHovered = mouseX >= left + 4 && mouseX < right - 4 && mouseY >= rowY && mouseY < rowY + ROW_HEIGHT && mouseY >= listTop && mouseY < listBottom;

            // Row background
            int rowBg = rowHovered ? 0xFF1C2128 : (i % 2 == 0 ? BG_COLOR : 0xFF0F1318);
            g.fill(left + 4, rowY, right - 4, rowY + ROW_HEIGHT, rowBg);

            // Separator line
            g.fill(left + 4, rowY + ROW_HEIGHT - 1, right - 4, rowY + ROW_HEIGHT, 0xFF1C2128);

            // Category badge
            int catColor = getCategoryColor(fe.category());
            int badgeX = left + 8;
            int badgeY = rowY + 3;
            String catShort = getCategoryShort(fe.category());
            int badgeW = font.width(catShort) + 6;
            g.fill(badgeX, badgeY, badgeX + badgeW, badgeY + 11, catColor & 0x33FFFFFF);
            drawRectOutline(g, badgeX, badgeY, badgeX + badgeW, badgeY + 11, catColor);
            g.drawString(font, catShort, badgeX + 3, badgeY + 2, catColor, false);

            // Feature name
            int nameX = badgeX + badgeW + 6;
            g.drawString(font, fe.name(), nameX, rowY + 4, TEXT_COLOR, false);

            // Description (below name, smaller)
            g.drawString(font, fe.description(), left + 8, rowY + 16, LABEL_COLOR, false);

            // Toggle button
            int toggleW = 32;
            int toggleH = 14;
            int toggleX = right - toggleW - 12;
            int toggleY = rowY + (ROW_HEIGHT - toggleH) / 2;

            int toggleBg = fe.enabled() ? TOGGLE_ON : TOGGLE_OFF;
            int toggleDarkBg = fe.enabled() ? 0xFF2D8A3E : 0xFFB83A33;
            g.fill(toggleX, toggleY, toggleX + toggleW, toggleY + toggleH, toggleDarkBg);
            g.fill(toggleX + 1, toggleY + 1, toggleX + toggleW - 1, toggleY + toggleH - 1, toggleBg);

            // Toggle knob
            int knobSize = toggleH - 4;
            int knobX = fe.enabled() ? (toggleX + toggleW - knobSize - 3) : (toggleX + 3);
            int knobY = toggleY + 2;
            g.fill(knobX, knobY, knobX + knobSize, knobY + knobSize, 0xFFFFFFFF);

            // ON/OFF text
            String toggleLabel = fe.enabled() ? "ON" : "OFF";
            int labelX = fe.enabled() ? (toggleX + 4) : (toggleX + toggleW - font.width("OFF") - 4);
            g.drawString(font, toggleLabel, labelX, toggleY + 3, 0xFFFFFFFF, false);

            // Sub-row: speed multiplier under builder_admin_bypass when enabled
            if ("builder_admin_bypass".equals(fe.id()) && fe.enabled()) {
                int subRowY = rowY + ROW_HEIGHT;
                if (subRowY + ROW_HEIGHT >= listTop && subRowY <= listBottom) {
                    // Indented sub-row background
                    g.fill(left + 4, subRowY, right - 4, subRowY + ROW_HEIGHT, 0xFF161B22);
                    g.fill(left + 4, subRowY + ROW_HEIGHT - 1, right - 4, subRowY + ROW_HEIGHT, 0xFF1C2128);

                    // Indent marker
                    g.fill(left + 10, subRowY + 4, left + 12, subRowY + ROW_HEIGHT - 4, CAT_ADMIN);

                    // Label
                    g.drawString(font, "Speed Multiplier", left + 18, subRowY + 4, TEXT_COLOR, false);
                    g.drawString(font, "Blocks placed per tick cycle (1 = normal)", left + 18, subRowY + 16, LABEL_COLOR, false);

                    // [-] value [+] controls
                    int ctrlX = right - 80;
                    int ctrlY = subRowY + (ROW_HEIGHT - 14) / 2;
                    int btnSize = 14;

                    // [-] button
                    boolean minusHovered = mouseX >= ctrlX && mouseX < ctrlX + btnSize && mouseY >= ctrlY && mouseY < ctrlY + btnSize;
                    g.fill(ctrlX, ctrlY, ctrlX + btnSize, ctrlY + btnSize, minusHovered ? 0xFF3D4450 : 0xFF21262D);
                    drawRectOutline(g, ctrlX, ctrlY, ctrlX + btnSize, ctrlY + btnSize, BORDER_COLOR);
                    g.drawString(font, "-", ctrlX + (btnSize - font.width("-")) / 2, ctrlY + 3, TEXT_COLOR, false);

                    // Value display
                    String valStr = builderSpeedMultiplier + "x";
                    int valW = font.width(valStr);
                    int valX = ctrlX + btnSize + (30 - valW) / 2;
                    g.drawString(font, valStr, valX, ctrlY + 3, ACCENT_BLUE, false);

                    // [+] button
                    int plusX = ctrlX + btnSize + 30;
                    boolean plusHovered = mouseX >= plusX && mouseX < plusX + btnSize && mouseY >= ctrlY && mouseY < ctrlY + btnSize;
                    g.fill(plusX, ctrlY, plusX + btnSize, ctrlY + btnSize, plusHovered ? 0xFF3D4450 : 0xFF21262D);
                    drawRectOutline(g, plusX, ctrlY, plusX + btnSize, ctrlY + btnSize, BORDER_COLOR);
                    g.drawString(font, "+", plusX + (btnSize - font.width("+")) / 2, ctrlY + 3, TEXT_COLOR, false);
                }
            }
        }
        g.disableScissor();

        // Scrollbar
        if (maxScroll > 0) {
            int scrollBarX = right - 6;
            int scrollBarW = 3;
            int scrollTrackH = listH;
            int scrollThumbH = Math.max(10, (int) ((float) listH / totalContentHeight * scrollTrackH));
            int scrollThumbY = listTop + (int) ((float) scroll / maxScroll * (scrollTrackH - scrollThumbH));

            g.fill(scrollBarX, listTop, scrollBarX + scrollBarW, listBottom, 0xFF1C2128);
            g.fill(scrollBarX, scrollThumbY, scrollBarX + scrollBarW, scrollThumbY + scrollThumbH, LABEL_COLOR);
        }

        // Empty state
        if (filteredFeatures.isEmpty()) {
            String emptyMsg = features.isEmpty() ? "Loading features..." : "No features match your filter";
            int emptyW = font.width(emptyMsg);
            g.drawString(font, emptyMsg, left + (panelW - emptyW) / 2, listTop + 20, LABEL_COLOR, false);
        }

        // --- Bulk action bar ---
        int bulkY = bottom - BULK_BAR_HEIGHT - 2;
        g.fill(left, bulkY, right, bottom, HEADER_COLOR);
        g.fill(left, bulkY, right, bulkY + 1, BORDER_COLOR);

        int btnW = 70;
        int btnH = 16;
        int btnY = bulkY + (BULK_BAR_HEIGHT - btnH) / 2 + 1;
        int btnGap = 6;

        // Enable All button
        int enableBtnX = left + 6;
        boolean enableHovered = mouseX >= enableBtnX && mouseX < enableBtnX + btnW && mouseY >= btnY && mouseY < btnY + btnH;
        g.fill(enableBtnX, btnY, enableBtnX + btnW, btnY + btnH, enableHovered ? TOGGLE_ON : 0xFF2D5A1E);
        drawRectOutline(g, enableBtnX, btnY, enableBtnX + btnW, btnY + btnH, TOGGLE_ON);
        String enableText = "Enable All";
        g.drawString(font, enableText, enableBtnX + (btnW - font.width(enableText)) / 2, btnY + 4, TEXT_COLOR, false);

        // Disable All button
        int disableBtnX = enableBtnX + btnW + btnGap;
        boolean disableHovered = mouseX >= disableBtnX && mouseX < disableBtnX + btnW && mouseY >= btnY && mouseY < btnY + btnH;
        g.fill(disableBtnX, btnY, disableBtnX + btnW, btnY + btnH, disableHovered ? TOGGLE_OFF : 0xFF5A1E1E);
        drawRectOutline(g, disableBtnX, btnY, disableBtnX + btnW, btnY + btnH, TOGGLE_OFF);
        String disableText = "Disable All";
        g.drawString(font, disableText, disableBtnX + (btnW - font.width(disableText)) / 2, btnY + 4, TEXT_COLOR, false);

        // Reset Defaults button
        int resetW = 88;
        int resetBtnX = disableBtnX + btnW + btnGap;
        boolean resetHovered = mouseX >= resetBtnX && mouseX < resetBtnX + resetW && mouseY >= btnY && mouseY < btnY + btnH;
        g.fill(resetBtnX, btnY, resetBtnX + resetW, btnY + btnH, resetHovered ? 0xFF3D4450 : 0xFF21262D);
        drawRectOutline(g, resetBtnX, btnY, resetBtnX + resetW, btnY + btnH, ACCENT_BLUE);
        String resetText = "Reset Defaults";
        g.drawString(font, resetText, resetBtnX + (resetW - font.width(resetText)) / 2, btnY + 4, ACCENT_BLUE, false);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button, int left, int top, int right, int bottom) {
        if (button != 0) return false;

        int panelW = right - left;
        int headerH = 20;
        int headerY = top;

        int yOffset = headerY + headerH + 2;

        // --- Category tab clicks ---
        int catTabX = left + 4;
        int catTabY = yOffset;
        for (int i = 0; i < CATEGORIES.length; i++) {
            String cat = CATEGORIES[i];
            int tw = font.width(cat) + 10;
            if (mouseX >= catTabX && mouseX < catTabX + tw && mouseY >= catTabY && mouseY < catTabY + CATEGORY_TAB_HEIGHT) {
                selectedCategory = cat;
                scroll = 0;
                updateFiltered();
                return true;
            }
            catTabX += tw + 3;
        }

        yOffset = catTabY + CATEGORY_TAB_HEIGHT + 4;

        // --- Search box click ---
        int searchX = left + 4;
        int searchY = yOffset;
        int searchW = panelW - 8;
        if (mouseX >= searchX && mouseX < searchX + searchW && mouseY >= searchY && mouseY < searchY + SEARCH_HEIGHT) {
            searchFocused = true;
            return true;
        } else {
            searchFocused = false;
        }

        yOffset = searchY + SEARCH_HEIGHT + 4;

        // --- Feature toggle clicks ---
        int listTop = yOffset;
        int listBottom = bottom - BULK_BAR_HEIGHT - 4;

        if (mouseY >= listTop && mouseY < listBottom) {
            // Build row positions for click detection (same logic as render)
            int[] clickRowY = new int[filteredFeatures.size()];
            int cumY = 0;
            for (int i = 0; i < filteredFeatures.size(); i++) {
                clickRowY[i] = cumY;
                cumY += ROW_HEIGHT;
                if ("builder_admin_bypass".equals(filteredFeatures.get(i).id()) && filteredFeatures.get(i).enabled()) {
                    cumY += ROW_HEIGHT;
                }
            }

            for (int i = 0; i < filteredFeatures.size(); i++) {
                FeatureEntry fe = filteredFeatures.get(i);
                int rowY = listTop + clickRowY[i] - scroll;

                if (rowY + ROW_HEIGHT < listTop || rowY > listBottom) continue;

                // Toggle button area
                int toggleW = 32;
                int toggleH = 14;
                int toggleX = right - toggleW - 12;
                int toggleY = rowY + (ROW_HEIGHT - toggleH) / 2;

                if (mouseX >= toggleX && mouseX < toggleX + toggleW && mouseY >= toggleY && mouseY < toggleY + toggleH) {
                    // Toggle this feature
                    boolean newState = !fe.enabled();
                    String data = fe.id() + ":" + newState;
                    ClientPacketDistributor.sendToServer((CustomPacketPayload) new ComputerActionPayload("feature_toggle_set", data), (CustomPacketPayload[]) new CustomPacketPayload[0]);

                    // Optimistic local update
                    int idx = -1;
                    for (int j = 0; j < features.size(); j++) {
                        if (features.get(j).id().equals(fe.id())) {
                            idx = j;
                            break;
                        }
                    }
                    if (idx >= 0) {
                        FeatureEntry old = features.get(idx);
                        features.set(idx, new FeatureEntry(old.id(), old.name(), old.category(), old.description(), newState));
                        if (newState) enabledCount++;
                        else enabledCount--;
                        updateFiltered();
                    }
                    return true;
                }

                // Speed multiplier +/- buttons (sub-row under builder_admin_bypass)
                if ("builder_admin_bypass".equals(fe.id()) && fe.enabled()) {
                    int subRowY = rowY + ROW_HEIGHT;
                    int ctrlX = right - 80;
                    int ctrlYPos = subRowY + (ROW_HEIGHT - 14) / 2;
                    int btnSize = 14;

                    // [-] button
                    if (mouseX >= ctrlX && mouseX < ctrlX + btnSize && mouseY >= ctrlYPos && mouseY < ctrlYPos + btnSize) {
                        builderSpeedMultiplier = Math.max(1, builderSpeedMultiplier - 1);
                        ClientPacketDistributor.sendToServer((CustomPacketPayload) new ComputerActionPayload("feature_numeric_set", "builder_speed_multiplier:" + builderSpeedMultiplier), (CustomPacketPayload[]) new CustomPacketPayload[0]);
                        return true;
                    }
                    // [+] button
                    int plusX = ctrlX + btnSize + 30;
                    if (mouseX >= plusX && mouseX < plusX + btnSize && mouseY >= ctrlYPos && mouseY < ctrlYPos + btnSize) {
                        builderSpeedMultiplier = Math.min(50, builderSpeedMultiplier + 1);
                        ClientPacketDistributor.sendToServer((CustomPacketPayload) new ComputerActionPayload("feature_numeric_set", "builder_speed_multiplier:" + builderSpeedMultiplier), (CustomPacketPayload[]) new CustomPacketPayload[0]);
                        return true;
                    }
                }
            }
        }

        // --- Bulk action button clicks ---
        int bulkY = bottom - BULK_BAR_HEIGHT - 2;
        int btnW = 70;
        int btnH = 16;
        int btnY = bulkY + (BULK_BAR_HEIGHT - btnH) / 2 + 1;
        int btnGap = 6;

        // Enable All
        int enableBtnX = left + 6;
        if (mouseX >= enableBtnX && mouseX < enableBtnX + btnW && mouseY >= btnY && mouseY < btnY + btnH) {
            ClientPacketDistributor.sendToServer((CustomPacketPayload) new ComputerActionPayload("feature_toggles_enable_all", ""), (CustomPacketPayload[]) new CustomPacketPayload[0]);
            // Optimistic update
            for (int i = 0; i < features.size(); i++) {
                FeatureEntry old = features.get(i);
                features.set(i, new FeatureEntry(old.id(), old.name(), old.category(), old.description(), true));
            }
            enabledCount = totalCount;
            updateFiltered();
            return true;
        }

        // Disable All
        int disableBtnX = enableBtnX + btnW + btnGap;
        if (mouseX >= disableBtnX && mouseX < disableBtnX + btnW && mouseY >= btnY && mouseY < btnY + btnH) {
            ClientPacketDistributor.sendToServer((CustomPacketPayload) new ComputerActionPayload("feature_toggles_disable_all", ""), (CustomPacketPayload[]) new CustomPacketPayload[0]);
            for (int i = 0; i < features.size(); i++) {
                FeatureEntry old = features.get(i);
                features.set(i, new FeatureEntry(old.id(), old.name(), old.category(), old.description(), false));
            }
            enabledCount = 0;
            updateFiltered();
            return true;
        }

        // Reset Defaults
        int resetW = 88;
        int resetBtnX = disableBtnX + btnW + btnGap;
        if (mouseX >= resetBtnX && mouseX < resetBtnX + resetW && mouseY >= btnY && mouseY < btnY + btnH) {
            ClientPacketDistributor.sendToServer((CustomPacketPayload) new ComputerActionPayload("feature_toggles_reset", ""), (CustomPacketPayload[]) new CustomPacketPayload[0]);
            for (int i = 0; i < features.size(); i++) {
                FeatureEntry old = features.get(i);
                features.set(i, new FeatureEntry(old.id(), old.name(), old.category(), old.description(), true));
            }
            enabledCount = totalCount;
            updateFiltered();
            return true;
        }

        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int scrollAmount = (int) (-scrollY * 12);
        scroll += scrollAmount;
        if (scroll < 0) scroll = 0;
        if (scroll > maxScroll) scroll = maxScroll;
        return true;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!searchFocused) return false;

        // Backspace
        if (keyCode == 259) {
            if (!searchText.isEmpty() && searchCursorPos > 0) {
                searchText = searchText.substring(0, searchCursorPos - 1) + searchText.substring(searchCursorPos);
                searchCursorPos--;
                scroll = 0;
                updateFiltered();
            }
            return true;
        }
        // Delete
        if (keyCode == 261) {
            if (searchCursorPos < searchText.length()) {
                searchText = searchText.substring(0, searchCursorPos) + searchText.substring(searchCursorPos + 1);
                scroll = 0;
                updateFiltered();
            }
            return true;
        }
        // Left arrow
        if (keyCode == 263) {
            if (searchCursorPos > 0) searchCursorPos--;
            return true;
        }
        // Right arrow
        if (keyCode == 262) {
            if (searchCursorPos < searchText.length()) searchCursorPos++;
            return true;
        }
        // Home
        if (keyCode == 268) {
            searchCursorPos = 0;
            return true;
        }
        // End
        if (keyCode == 269) {
            searchCursorPos = searchText.length();
            return true;
        }
        // Escape - unfocus search
        if (keyCode == 256) {
            searchFocused = false;
            return true;
        }
        // Ctrl+A select all (clear on next type)
        if (keyCode == 65 && (modifiers & 2) != 0) {
            return true;
        }

        return false;
    }

    public boolean charTyped(char c, int modifiers) {
        if (!searchFocused) return false;

        // Only accept printable characters
        if (c >= 32 && c != 127) {
            if (searchText.length() < 64) {
                searchText = searchText.substring(0, searchCursorPos) + c + searchText.substring(searchCursorPos);
                searchCursorPos++;
                scroll = 0;
                updateFiltered();
            }
            return true;
        }
        return false;
    }

    public void handleResponse(String type, String jsonData) {
        if (!"feature_toggles_data".equals(type)) return;

        try {
            JsonObject root = JsonParser.parseString(jsonData).getAsJsonObject();
            enabledCount = root.get("enabledCount").getAsInt();
            totalCount = root.get("totalCount").getAsInt();

            features.clear();
            JsonArray arr = root.getAsJsonArray("features");
            for (JsonElement el : arr) {
                JsonObject obj = el.getAsJsonObject();
                features.add(new FeatureEntry(
                    obj.get("id").getAsString(),
                    obj.get("name").getAsString(),
                    obj.get("category").getAsString(),
                    obj.get("description").getAsString(),
                    obj.get("enabled").getAsBoolean()
                ));
            }
            // Parse numeric settings
            if (root.has("numeric")) {
                JsonObject numeric = root.getAsJsonObject("numeric");
                if (numeric.has("builder_speed_multiplier")) {
                    builderSpeedMultiplier = numeric.get("builder_speed_multiplier").getAsInt();
                }
            }
            updateFiltered();
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to parse feature toggles data", e);
        }
    }

    public void requestData() {
        ClientPacketDistributor.sendToServer((CustomPacketPayload) new ComputerActionPayload("feature_toggles_request", ""), (CustomPacketPayload[]) new CustomPacketPayload[0]);
    }

    // --- Private helpers ---

    private void updateFiltered() {
        filteredFeatures.clear();
        String lowerSearch = searchText.toLowerCase(Locale.ROOT);
        for (FeatureEntry fe : features) {
            // Category filter
            if (!"All".equals(selectedCategory) && !fe.category().equals(selectedCategory)) {
                continue;
            }
            // Search filter
            if (!lowerSearch.isEmpty()) {
                if (!fe.name().toLowerCase(Locale.ROOT).contains(lowerSearch)
                    && !fe.description().toLowerCase(Locale.ROOT).contains(lowerSearch)
                    && !fe.id().toLowerCase(Locale.ROOT).contains(lowerSearch)) {
                    continue;
                }
            }
            filteredFeatures.add(fe);
        }
    }

    private static int getCategoryColor(String category) {
        switch (category) {
            case "Vanilla Refresh": return CAT_VANILLA;
            case "Economy": return CAT_ECONOMY;
            case "Combat": return CAT_COMBAT;
            case "Exploration": return CAT_EXPLORATION;
            case "Admin": return CAT_ADMIN;
            case "Multiplayer": return CAT_MULTIPLAYER;
            case "Decoration": return CAT_DECORATION;
            case "QoL": return CAT_QOL;
            case "Colony": return CAT_COLONY;
            default: return LABEL_COLOR;
        }
    }

    private static String getCategoryShort(String category) {
        switch (category) {
            case "Vanilla Refresh": return "VR";
            case "Economy": return "ECO";
            case "Combat": return "CMB";
            case "Exploration": return "EXP";
            case "Admin": return "ADM";
            case "Multiplayer": return "MP";
            case "Decoration": return "DEC";
            case "QoL": return "QOL";
            case "Colony": return "COL";
            default: return "???";
        }
    }

    private static void drawRectOutline(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        g.fill(x1, y1, x2, y1 + 1, color);      // top
        g.fill(x1, y2 - 1, x2, y2, color);       // bottom
        g.fill(x1, y1, x1 + 1, y2, color);       // left
        g.fill(x2 - 1, y1, x2, y2, color);       // right
    }
}

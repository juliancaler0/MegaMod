package com.ultra.megamod.feature.computer.screen.panels;

import com.ultra.megamod.MegaMod;
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
import java.util.List;
import java.util.Locale;

/**
 * Admin panel GUI tab for managing client-side modules.
 * Tab 21 in the AdminTerminalScreen.
 * Split-panel layout: scrollable module list (left 60%) + settings editor (right 40%).
 */
public class AdminModulesPanel {

    // Visual style constants (matching other panels)
    private static final int BG = 0xFF0D1117;
    private static final int HEADER_BG = 0xFF161B22;
    private static final int BORDER = 0xFF30363D;
    private static final int TEXT = 0xFFE6EDF3;
    private static final int LABEL = 0xFF8B949E;
    private static final int ACCENT = 0xFF58A6FF;
    private static final int SUCCESS = 0xFF3FB950;
    private static final int ERROR = 0xFFF85149;
    private static final int WARNING = 0xFFD29922;
    private static final int BTN_BG = 0xFF21262D;
    private static final int BTN_HOVER = 0xFF30363D;

    // Category colors
    private static final int CAT_COMBAT = 0xFFF85149;
    private static final int CAT_MOVEMENT = 0xFF58A6FF;
    private static final int CAT_RENDER = 0xFFA371F7;
    private static final int CAT_PLAYER = 0xFF3FB950;
    private static final int CAT_WORLD = 0xFFD29922;
    private static final int CAT_MISC = 0xFF8B949E;

    // Layout constants
    private static final int ROW_HEIGHT = 28;
    private static final int HEADER_HEIGHT = 20;
    private static final int SEARCH_HEIGHT = 16;
    private static final int CATEGORY_TAB_HEIGHT = 18;
    private static final int BULK_BAR_HEIGHT = 22;
    private static final int SETTING_ROW_HEIGHT = 22;
    private static final int SLIDER_HEIGHT = 8;
    private static final int SLIDER_KNOB_W = 6;
    private static final int CHECKBOX_SIZE = 10;

    private static final String[] CATEGORIES = {"All", "Combat", "Movement", "Render", "Player", "World", "Misc"};

    // State
    private final Font font;
    private String searchText = "";
    private String selectedCategory = "All";
    private int scroll = 0;
    private int maxScroll = 0;
    private int selectedModuleIdx = -1;
    private boolean searchFocused = false;
    private int searchCursorPos = 0;

    private final List<ModuleEntry> allModules = new ArrayList<>();
    private final List<ModuleEntry> filteredModules = new ArrayList<>();
    private int enabledCount = 0;
    private int totalCount = 0;

    // Settings panel scroll
    private int settingsScroll = 0;
    private int settingsMaxScroll = 0;

    // Slider drag state
    private int draggingSettingIdx = -1;
    private int dragSliderLeft = 0;
    private int dragSliderRight = 0;
    private double dragMin = 0;
    private double dragMax = 0;
    private String dragModuleId = "";
    private String dragSettingName = "";
    private String dragSettingType = "";

    // Tooltip state
    private String tooltipText = null;
    private int tooltipX = 0;
    private int tooltipY = 0;

    // Keybind listening state
    private boolean listeningForKeybind = false;
    private int listeningSettingIdx = -1;
    private String listeningModuleId = "";
    private boolean listeningForToggleKey = false;
    private String listeningToggleKeyModuleId = "";

    // Data records
    private record ModuleEntry(String id, String name, String desc, String category,
                                boolean enabled, String toggleKey, List<SettingEntry> settings) {}

    private record SettingEntry(String name, String type, String value, String desc,
                                 double min, double max, List<String> options) {}

    public AdminModulesPanel(Font font) {
        this.font = font;
    }

    public void requestData() {
        sendAction("adminmod_request", "");
    }

    // ==================== RENDER ====================

    public void render(GuiGraphics g, int mouseX, int mouseY, int left, int top, int right, int bottom) {
        int panelW = right - left;
        tooltipText = null;

        // Background
        g.fill(left, top, right, bottom, BG);

        // === Header bar ===
        int headerY = top;
        g.fill(left, headerY, right, headerY + HEADER_HEIGHT, HEADER_BG);
        g.fill(left, headerY + HEADER_HEIGHT - 1, right, headerY + HEADER_HEIGHT, BORDER);

        g.drawString(font, "Client Modules (Admin Only)", left + 6, headerY + 6, TEXT, false);

        // Enabled count (right side of header)
        String countStr = enabledCount + "/" + totalCount + " enabled";
        int countW = font.width(countStr);
        int countColor;
        if (totalCount == 0) {
            countColor = LABEL;
        } else if (enabledCount == totalCount) {
            countColor = SUCCESS;
        } else if (enabledCount == 0) {
            countColor = ERROR;
        } else {
            countColor = ACCENT;
        }

        // Search field inline in header (right side, before count)
        int searchFieldW = 80;
        int searchFieldX = right - countW - 12 - searchFieldW - 6;
        int searchFieldY = headerY + 4;
        g.fill(searchFieldX, searchFieldY, searchFieldX + searchFieldW, searchFieldY + 12, BTN_BG);
        drawRectOutline(g, searchFieldX, searchFieldY, searchFieldX + searchFieldW, searchFieldY + 12,
                searchFocused ? ACCENT : BORDER);

        String displaySearch = searchText.isEmpty() && !searchFocused ? "Search..." : searchText;
        int searchTextColor = searchText.isEmpty() && !searchFocused ? LABEL : TEXT;
        String clippedSearch = displaySearch;
        if (font.width(clippedSearch) > searchFieldW - 8) {
            while (font.width(clippedSearch + "..") > searchFieldW - 8 && clippedSearch.length() > 0) {
                clippedSearch = clippedSearch.substring(1);
            }
            clippedSearch = ".." + clippedSearch;
        }
        g.drawString(font, clippedSearch, searchFieldX + 4, searchFieldY + 2, searchTextColor, false);

        // Search cursor blink
        if (searchFocused) {
            long now = System.currentTimeMillis();
            if ((now / 500) % 2 == 0) {
                int cursorX = searchFieldX + 4 + font.width(searchText.substring(0, Math.min(searchCursorPos, searchText.length())));
                cursorX = Math.min(cursorX, searchFieldX + searchFieldW - 4);
                g.fill(cursorX, searchFieldY + 2, cursorX + 1, searchFieldY + 10, TEXT);
            }
        }

        g.drawString(font, countStr, right - countW - 6, headerY + 6, countColor, false);

        int yOffset = headerY + HEADER_HEIGHT + 2;

        // === Category filter tabs ===
        int catTabX = left + 4;
        int catTabY = yOffset;
        for (int i = 0; i < CATEGORIES.length; i++) {
            String cat = CATEGORIES[i];
            int catCount = countInCategory(cat);
            int catEnabled = countEnabledInCategory(cat);
            String tabLabel = cat;
            if (!"All".equals(cat)) {
                tabLabel = cat + " (" + catEnabled + "/" + catCount + ")";
            }
            int tw = font.width(tabLabel) + 10;
            boolean isSelected = selectedCategory.equals(cat);
            boolean isHovered = mouseX >= catTabX && mouseX < catTabX + tw
                    && mouseY >= catTabY && mouseY < catTabY + CATEGORY_TAB_HEIGHT;

            int catColor = getCategoryColor(cat);
            int bgCol = isSelected ? catColor : (isHovered ? BTN_HOVER : HEADER_BG);
            int txtCol = isSelected ? 0xFF000000 : TEXT;

            g.fill(catTabX, catTabY, catTabX + tw, catTabY + CATEGORY_TAB_HEIGHT, bgCol);
            if (!isSelected) {
                drawRectOutline(g, catTabX, catTabY, catTabX + tw, catTabY + CATEGORY_TAB_HEIGHT, BORDER);
            }
            // Underline for selected tab
            if (isSelected) {
                g.fill(catTabX, catTabY + CATEGORY_TAB_HEIGHT - 2, catTabX + tw, catTabY + CATEGORY_TAB_HEIGHT, catColor);
            }
            g.drawString(font, tabLabel, catTabX + 5, catTabY + 5, txtCol, false);
            catTabX += tw + 3;
        }

        yOffset = catTabY + CATEGORY_TAB_HEIGHT + 4;

        // === Split panel divider ===
        int splitX = left + (int) (panelW * 0.6);
        int listLeft = left;
        int listRight = splitX - 2;
        int settingsLeft = splitX + 2;
        int settingsRight = right;

        int contentTop = yOffset;
        int contentBottom = bottom - BULK_BAR_HEIGHT - 4;

        // Vertical divider line
        g.fill(splitX - 1, contentTop, splitX, contentBottom, BORDER);

        // === Left panel - Module list ===
        renderModuleList(g, mouseX, mouseY, listLeft, contentTop, listRight, contentBottom);

        // === Right panel - Settings ===
        renderSettingsPanel(g, mouseX, mouseY, settingsLeft, contentTop, settingsRight, contentBottom);

        // === Bulk action bar (bottom) ===
        renderBulkBar(g, mouseX, mouseY, left, bottom - BULK_BAR_HEIGHT - 2, right, bottom);

        // === Tooltip (render last, on top of everything) ===
        if (tooltipText != null) {
            int ttW = font.width(tooltipText) + 8;
            int ttH = 12;
            int ttX = Math.min(tooltipX, right - ttW - 4);
            int ttY = tooltipY - ttH - 2;
            if (ttY < top) ttY = tooltipY + 12;
            g.fill(ttX, ttY, ttX + ttW, ttY + ttH, 0xEE161B22);
            drawRectOutline(g, ttX, ttY, ttX + ttW, ttY + ttH, BORDER);
            g.drawString(font, tooltipText, ttX + 4, ttY + 2, TEXT, false);
        }
    }

    private void renderModuleList(GuiGraphics g, int mouseX, int mouseY, int left, int top, int right, int bottom) {
        int listH = bottom - top;

        applyFilter();
        int totalRows = filteredModules.size();
        maxScroll = Math.max(0, (totalRows * ROW_HEIGHT) - listH);
        if (scroll > maxScroll) scroll = maxScroll;
        if (scroll < 0) scroll = 0;

        g.enableScissor(left, top, right, bottom);

        for (int i = 0; i < filteredModules.size(); i++) {
            ModuleEntry mod = filteredModules.get(i);
            int rowY = top + (i * ROW_HEIGHT) - scroll;

            // Skip offscreen
            if (rowY + ROW_HEIGHT < top || rowY > bottom) continue;

            boolean isSelected = i == selectedModuleIdx;
            boolean rowHovered = mouseX >= left + 2 && mouseX < right - 2
                    && mouseY >= Math.max(rowY, top) && mouseY < Math.min(rowY + ROW_HEIGHT, bottom)
                    && mouseY >= top && mouseY < bottom;

            // Row background
            int rowBg;
            if (isSelected) {
                rowBg = BTN_HOVER;
            } else if (rowHovered) {
                rowBg = 0xFF1C2128;
            } else {
                rowBg = i % 2 == 0 ? BG : 0xFF0F1318;
            }
            g.fill(left + 2, rowY, right - 2, rowY + ROW_HEIGHT, rowBg);

            // Selected indicator bar
            if (isSelected) {
                g.fill(left + 2, rowY, left + 4, rowY + ROW_HEIGHT, ACCENT);
            }

            // Separator
            g.fill(left + 2, rowY + ROW_HEIGHT - 1, right - 2, rowY + ROW_HEIGHT, 0xFF1C2128);

            // Status dot
            int dotX = left + 8;
            int dotY = rowY + 5;
            int dotColor = mod.enabled() ? SUCCESS : LABEL;
            g.fill(dotX, dotY, dotX + 4, dotY + 4, dotColor);
            // Inner dot highlight
            if (mod.enabled()) {
                g.fill(dotX + 1, dotY + 1, dotX + 2, dotY + 2, 0xFFFFFFFF);
            }

            // Module name
            int nameX = dotX + 8;
            int catColor = getCategoryColor(mod.category());
            g.drawString(font, mod.name(), nameX, rowY + 4, TEXT, false);

            // Category indicator (small colored bar after name)
            int nameEndX = nameX + font.width(mod.name()) + 3;
            g.fill(nameEndX, rowY + 5, nameEndX + 2, rowY + 11, catColor);

            // Description (below name)
            String desc = mod.desc();
            int maxDescW = right - 50 - left - 8;
            if (font.width(desc) > maxDescW) {
                while (font.width(desc + "...") > maxDescW && desc.length() > 0) {
                    desc = desc.substring(0, desc.length() - 1);
                }
                desc = desc + "...";
            }
            g.drawString(font, desc, left + 16, rowY + 16, LABEL, false);

            // Toggle button (ON/OFF)
            int toggleW = 30;
            int toggleH = 14;
            int toggleX = right - toggleW - 8;
            int toggleY = rowY + (ROW_HEIGHT - toggleH) / 2;

            boolean toggleHovered = mouseX >= toggleX && mouseX < toggleX + toggleW
                    && mouseY >= toggleY && mouseY < toggleY + toggleH
                    && mouseY >= top && mouseY < bottom;

            int toggleBg = mod.enabled() ? SUCCESS : ERROR;
            int toggleDark = mod.enabled() ? 0xFF2D8A3E : 0xFFB83A33;
            if (toggleHovered) {
                toggleBg = mod.enabled() ? 0xFF4FD970 : 0xFFFF6A62;
            }
            g.fill(toggleX, toggleY, toggleX + toggleW, toggleY + toggleH, toggleDark);
            g.fill(toggleX + 1, toggleY + 1, toggleX + toggleW - 1, toggleY + toggleH - 1, toggleBg);

            // Toggle knob
            int knobSize = toggleH - 4;
            int knobX = mod.enabled() ? (toggleX + toggleW - knobSize - 3) : (toggleX + 3);
            int knobY = toggleY + 2;
            g.fill(knobX, knobY, knobX + knobSize, knobY + knobSize, 0xFFFFFFFF);

            // ON/OFF text
            String toggleLabel = mod.enabled() ? "ON" : "OFF";
            int labelX = mod.enabled() ? (toggleX + 4) : (toggleX + toggleW - font.width("OFF") - 4);
            g.drawString(font, toggleLabel, labelX, toggleY + 3, 0xFFFFFFFF, false);
        }

        g.disableScissor();

        // Scrollbar
        if (maxScroll > 0) {
            int scrollBarX = right - 4;
            int scrollBarW = 3;
            int scrollTrackH = bottom - top;
            int scrollThumbH = Math.max(10, (int) ((float) scrollTrackH / (totalRows * ROW_HEIGHT) * scrollTrackH));
            int scrollThumbY = top + (int) ((float) scroll / maxScroll * (scrollTrackH - scrollThumbH));

            g.fill(scrollBarX, top, scrollBarX + scrollBarW, bottom, 0xFF1C2128);
            g.fill(scrollBarX, scrollThumbY, scrollBarX + scrollBarW, scrollThumbY + scrollThumbH, LABEL);
        }

        // Empty state
        if (filteredModules.isEmpty()) {
            String emptyMsg = allModules.isEmpty() ? "Loading modules..." : "No modules match filter";
            int emptyW = font.width(emptyMsg);
            int panelW = right - left;
            g.drawString(font, emptyMsg, left + (panelW - emptyW) / 2, top + 20, LABEL, false);
        }
    }

    private void renderSettingsPanel(GuiGraphics g, int mouseX, int mouseY, int left, int top, int right, int bottom) {
        int panelW = right - left;
        int panelH = bottom - top;

        if (selectedModuleIdx < 0 || selectedModuleIdx >= filteredModules.size()) {
            // No module selected - show placeholder
            String hint1 = "Select a module";
            String hint2 = "to view settings";
            int h1W = font.width(hint1);
            int h2W = font.width(hint2);
            g.drawString(font, hint1, left + (panelW - h1W) / 2, top + panelH / 2 - 10, LABEL, false);
            g.drawString(font, hint2, left + (panelW - h2W) / 2, top + panelH / 2 + 2, LABEL, false);
            return;
        }

        ModuleEntry mod = filteredModules.get(selectedModuleIdx);
        int y = top + 4;

        // Module name (prominent)
        g.drawString(font, mod.name(), left + 6, y, TEXT, false);
        y += 12;

        // Category badge
        int catColor = getCategoryColor(mod.category());
        String catLabel = mod.category();
        int badgeW = font.width(catLabel) + 6;
        g.fill(left + 6, y, left + 6 + badgeW, y + 11, catColor & 0x33FFFFFF);
        drawRectOutline(g, left + 6, y, left + 6 + badgeW, y + 11, catColor);
        g.drawString(font, catLabel, left + 9, y + 2, catColor, false);

        // Enabled status badge next to category
        String statusLabel = mod.enabled() ? "ENABLED" : "DISABLED";
        int statusColor = mod.enabled() ? SUCCESS : ERROR;
        int statusBadgeX = left + 6 + badgeW + 6;
        int statusBadgeW = font.width(statusLabel) + 6;
        g.fill(statusBadgeX, y, statusBadgeX + statusBadgeW, y + 11, statusColor & 0x33FFFFFF);
        drawRectOutline(g, statusBadgeX, y, statusBadgeX + statusBadgeW, y + 11, statusColor);
        g.drawString(font, statusLabel, statusBadgeX + 3, y + 2, statusColor, false);
        y += 14;

        // Toggle keybind row
        String toggleKeyLabel = "Toggle Key: ";
        boolean isListeningToggle = listeningForToggleKey && listeningToggleKeyModuleId.equals(mod.id());
        String toggleKeyValue = isListeningToggle ? "> Press key... <" : (mod.toggleKey().equals("NONE") ? "None" : mod.toggleKey());
        g.drawString(font, toggleKeyLabel, left + 6, y + 1, LABEL, false);
        int tkBtnX = left + 6 + font.width(toggleKeyLabel);
        int tkBtnW = font.width(toggleKeyValue) + 8;
        int tkBtnY = y - 1;
        int tkBtnH = 12;
        boolean tkHovered = mouseX >= tkBtnX && mouseX < tkBtnX + tkBtnW
                && mouseY >= tkBtnY && mouseY < tkBtnY + tkBtnH;
        int tkBg = isListeningToggle ? WARNING : (tkHovered ? BTN_HOVER : BTN_BG);
        int tkBorder = isListeningToggle ? WARNING : (tkHovered ? ACCENT : BORDER);
        g.fill(tkBtnX, tkBtnY, tkBtnX + tkBtnW, tkBtnY + tkBtnH, tkBg);
        drawRectOutline(g, tkBtnX, tkBtnY, tkBtnX + tkBtnW, tkBtnY + tkBtnH, tkBorder);
        g.drawString(font, toggleKeyValue, tkBtnX + 4, y, isListeningToggle ? 0xFF000000 : ACCENT, false);
        y += 14;

        // Description
        String desc = mod.desc();
        // Word wrap description
        List<String> descLines = wrapText(desc, panelW - 14);
        for (String line : descLines) {
            g.drawString(font, line, left + 6, y, LABEL, false);
            y += 10;
        }
        y += 4;

        // Separator
        g.fill(left + 6, y, right - 6, y + 1, BORDER);
        y += 6;

        // Settings header
        if (mod.settings().isEmpty()) {
            g.drawString(font, "No configurable settings", left + 6, y, LABEL, false);
            return;
        }

        g.drawString(font, "Settings", left + 6, y, TEXT, false);
        y += 14;

        // Settings list (scrollable)
        int settingsTop = y;
        int settingsBottom = bottom - 4;
        int totalSettingsH = mod.settings().size() * SETTING_ROW_HEIGHT;
        settingsMaxScroll = Math.max(0, totalSettingsH - (settingsBottom - settingsTop));
        if (settingsScroll > settingsMaxScroll) settingsScroll = settingsMaxScroll;
        if (settingsScroll < 0) settingsScroll = 0;

        g.enableScissor(left, settingsTop, right, settingsBottom);

        for (int i = 0; i < mod.settings().size(); i++) {
            SettingEntry setting = mod.settings().get(i);
            int settingY = settingsTop + (i * SETTING_ROW_HEIGHT) - settingsScroll;

            // Skip offscreen
            if (settingY + SETTING_ROW_HEIGHT < settingsTop || settingY > settingsBottom) continue;

            boolean settingHovered = mouseX >= left + 4 && mouseX < right - 4
                    && mouseY >= settingY && mouseY < settingY + SETTING_ROW_HEIGHT
                    && mouseY >= settingsTop && mouseY < settingsBottom;

            // Hover highlight
            if (settingHovered) {
                g.fill(left + 4, settingY, right - 4, settingY + SETTING_ROW_HEIGHT, 0xFF151A23);
            }

            // Show tooltip for description on hover
            if (settingHovered && setting.desc() != null && !setting.desc().isEmpty()) {
                tooltipText = setting.desc();
                tooltipX = mouseX + 8;
                tooltipY = mouseY;
            }

            switch (setting.type()) {
                case "bool" -> renderBoolSetting(g, mouseX, mouseY, setting, left + 6, settingY, right - 6, settingsTop, settingsBottom);
                case "int", "double" -> renderSliderSetting(g, mouseX, mouseY, setting, i, mod.id(), left + 6, settingY, right - 6, settingsTop, settingsBottom);
                case "enum" -> renderEnumSetting(g, mouseX, mouseY, setting, left + 6, settingY, right - 6, settingsTop, settingsBottom);
                case "keybind" -> renderKeybindSetting(g, mouseX, mouseY, setting, i, mod.id(), left + 6, settingY, right - 6, settingsTop, settingsBottom);
                default -> {
                    g.drawString(font, setting.name() + ": " + setting.value(), left + 6, settingY + 4, TEXT, false);
                }
            }
        }

        g.disableScissor();

        // Settings scrollbar
        if (settingsMaxScroll > 0) {
            int sbX = right - 4;
            int sbW = 2;
            int trackH = settingsBottom - settingsTop;
            int thumbH = Math.max(8, (int) ((float) trackH / totalSettingsH * trackH));
            int thumbY = settingsTop + (int) ((float) settingsScroll / settingsMaxScroll * (trackH - thumbH));
            g.fill(sbX, settingsTop, sbX + sbW, settingsBottom, 0xFF1C2128);
            g.fill(sbX, thumbY, sbX + sbW, thumbY + thumbH, LABEL);
        }
    }

    private void renderBoolSetting(GuiGraphics g, int mouseX, int mouseY, SettingEntry setting,
                                     int left, int y, int right, int clipTop, int clipBottom) {
        boolean value = "true".equalsIgnoreCase(setting.value());
        int cbX = left;
        int cbY = y + 4;

        // Checkbox
        int cbColor = value ? SUCCESS : BORDER;
        g.fill(cbX, cbY, cbX + CHECKBOX_SIZE, cbY + CHECKBOX_SIZE, BTN_BG);
        drawRectOutline(g, cbX, cbY, cbX + CHECKBOX_SIZE, cbY + CHECKBOX_SIZE, cbColor);

        if (value) {
            // Draw check mark (X shape for simplicity)
            g.fill(cbX + 2, cbY + 2, cbX + CHECKBOX_SIZE - 2, cbY + CHECKBOX_SIZE - 2, SUCCESS);
            // Inner highlight
            g.fill(cbX + 3, cbY + 3, cbX + CHECKBOX_SIZE - 3, cbY + CHECKBOX_SIZE - 3, 0xFF5FD97A);
        }

        // Label
        g.drawString(font, setting.name(), cbX + CHECKBOX_SIZE + 4, y + 5, TEXT, false);

        // Value text
        String valStr = value ? "ON" : "OFF";
        int valColor = value ? SUCCESS : ERROR;
        g.drawString(font, valStr, right - font.width(valStr), y + 5, valColor, false);
    }

    private void renderSliderSetting(GuiGraphics g, int mouseX, int mouseY, SettingEntry setting,
                                       int settingIdx, String moduleId, int left, int y, int right,
                                       int clipTop, int clipBottom) {
        // Label + value
        g.drawString(font, setting.name(), left, y + 1, TEXT, false);

        // Value display
        String valStr = setting.value();
        if ("double".equals(setting.type())) {
            try {
                double d = Double.parseDouble(valStr);
                valStr = String.format("%.2f", d);
            } catch (NumberFormatException ignored) {}
        }
        int valW = font.width(valStr);
        g.drawString(font, valStr, right - valW, y + 1, ACCENT, false);

        // Slider bar
        int sliderLeft = left;
        int sliderRight = right - valW - 6;
        int sliderW = sliderRight - sliderLeft;
        int sliderY = y + 12;

        if (sliderW < 20) return; // Not enough room

        // Track
        g.fill(sliderLeft, sliderY, sliderRight, sliderY + SLIDER_HEIGHT, 0xFF1C2128);
        drawRectOutline(g, sliderLeft, sliderY, sliderRight, sliderY + SLIDER_HEIGHT, BORDER);

        // Fill
        double min = setting.min();
        double max = setting.max();
        double current;
        try {
            current = Double.parseDouble(setting.value());
        } catch (NumberFormatException e) {
            current = min;
        }
        double ratio = (max > min) ? (current - min) / (max - min) : 0;
        ratio = Math.max(0, Math.min(1, ratio));
        int fillW = (int) (ratio * (sliderW - 2));
        g.fill(sliderLeft + 1, sliderY + 1, sliderLeft + 1 + fillW, sliderY + SLIDER_HEIGHT - 1, ACCENT & 0x66FFFFFF);

        // Knob
        int knobX = sliderLeft + (int) (ratio * (sliderW - SLIDER_KNOB_W));
        boolean knobHovered = mouseX >= knobX - 1 && mouseX < knobX + SLIDER_KNOB_W + 1
                && mouseY >= sliderY - 2 && mouseY < sliderY + SLIDER_HEIGHT + 2
                && mouseY >= clipTop && mouseY < clipBottom;
        boolean isDragging = draggingSettingIdx == settingIdx;

        int knobColor = isDragging ? 0xFFFFFFFF : (knobHovered ? ACCENT : TEXT);
        g.fill(knobX, sliderY - 1, knobX + SLIDER_KNOB_W, sliderY + SLIDER_HEIGHT + 1, knobColor);

        // Min/Max labels
        String minStr = formatMinMax(min, setting.type());
        String maxStr = formatMinMax(max, setting.type());
        g.drawString(font, minStr, sliderLeft, sliderY + SLIDER_HEIGHT + 2, 0xFF484F58, false);
        int maxStrW = font.width(maxStr);
        g.drawString(font, maxStr, sliderRight - maxStrW, sliderY + SLIDER_HEIGHT + 2, 0xFF484F58, false);
    }

    private void renderEnumSetting(GuiGraphics g, int mouseX, int mouseY, SettingEntry setting,
                                     int left, int y, int right, int clipTop, int clipBottom) {
        // Label
        g.drawString(font, setting.name(), left, y + 5, TEXT, false);

        // Dropdown button
        String currentVal = setting.value();
        int btnW = font.width(currentVal) + 16;
        int btnX = right - btnW;
        int btnY = y + 2;
        int btnH = 14;

        boolean hovered = mouseX >= btnX && mouseX < btnX + btnW
                && mouseY >= btnY && mouseY < btnY + btnH
                && mouseY >= clipTop && mouseY < clipBottom;

        g.fill(btnX, btnY, btnX + btnW, btnY + btnH, hovered ? BTN_HOVER : BTN_BG);
        drawRectOutline(g, btnX, btnY, btnX + btnW, btnY + btnH, hovered ? ACCENT : BORDER);
        g.drawString(font, currentVal, btnX + 4, btnY + 3, TEXT, false);

        // Dropdown arrow
        int arrowX = btnX + btnW - 10;
        int arrowY = btnY + 5;
        g.fill(arrowX, arrowY, arrowX + 5, arrowY + 1, LABEL);
        g.fill(arrowX + 1, arrowY + 1, arrowX + 4, arrowY + 2, LABEL);
        g.fill(arrowX + 2, arrowY + 2, arrowX + 3, arrowY + 3, LABEL);
    }

    private void renderKeybindSetting(GuiGraphics g, int mouseX, int mouseY, SettingEntry setting,
                                        int settingIdx, String moduleId, int left, int y, int right,
                                        int clipTop, int clipBottom) {
        // Label
        g.drawString(font, setting.name(), left, y + 5, TEXT, false);

        // Keybind button
        boolean isListening = listeningForKeybind && listeningSettingIdx == settingIdx
                && listeningModuleId.equals(moduleId);
        String keyText = isListening ? "> ... <" : "[" + setting.value() + "]";
        int btnW = font.width(keyText) + 10;
        int btnX = right - btnW;
        int btnY = y + 2;
        int btnH = 14;

        boolean hovered = mouseX >= btnX && mouseX < btnX + btnW
                && mouseY >= btnY && mouseY < btnY + btnH
                && mouseY >= clipTop && mouseY < clipBottom;

        int btnBg = isListening ? WARNING : (hovered ? BTN_HOVER : BTN_BG);
        int btnBorder = isListening ? WARNING : (hovered ? ACCENT : BORDER);
        g.fill(btnX, btnY, btnX + btnW, btnY + btnH, btnBg);
        drawRectOutline(g, btnX, btnY, btnX + btnW, btnY + btnH, btnBorder);
        g.drawString(font, keyText, btnX + 5, btnY + 3, isListening ? 0xFF000000 : TEXT, false);
    }

    private void renderBulkBar(GuiGraphics g, int mouseX, int mouseY, int left, int top, int right, int bottom) {
        g.fill(left, top, right, bottom, HEADER_BG);
        g.fill(left, top, right, top + 1, BORDER);

        int btnW = 70;
        int btnH = 16;
        int btnY = top + (bottom - top - btnH) / 2;
        int btnGap = 6;

        // "Enable All" button
        int enableBtnX = left + 6;
        boolean enableHovered = mouseX >= enableBtnX && mouseX < enableBtnX + btnW
                && mouseY >= btnY && mouseY < btnY + btnH;
        g.fill(enableBtnX, btnY, enableBtnX + btnW, btnY + btnH, enableHovered ? SUCCESS : 0xFF2D5A1E);
        drawRectOutline(g, enableBtnX, btnY, enableBtnX + btnW, btnY + btnH, SUCCESS);
        String enableText = "Enable All";
        g.drawString(font, enableText, enableBtnX + (btnW - font.width(enableText)) / 2, btnY + 4, TEXT, false);

        // "Disable All" button
        int disableBtnX = enableBtnX + btnW + btnGap;
        boolean disableHovered = mouseX >= disableBtnX && mouseX < disableBtnX + btnW
                && mouseY >= btnY && mouseY < btnY + btnH;
        g.fill(disableBtnX, btnY, disableBtnX + btnW, btnY + btnH, disableHovered ? ERROR : 0xFF5A1E1E);
        drawRectOutline(g, disableBtnX, btnY, disableBtnX + btnW, btnY + btnH, ERROR);
        String disableText = "Disable All";
        g.drawString(font, disableText, disableBtnX + (btnW - font.width(disableText)) / 2, btnY + 4, TEXT, false);

        // Category label (shows what category the bulk action applies to)
        if (!"All".equals(selectedCategory)) {
            String scopeLabel = "Scope: " + selectedCategory;
            int scopeColor = getCategoryColor(selectedCategory);
            g.drawString(font, scopeLabel, disableBtnX + btnW + 12, btnY + 4, scopeColor, false);
        }
    }

    // ==================== MOUSE CLICKED ====================

    public boolean mouseClicked(double mouseX, double mouseY, int button, int left, int top, int right, int bottom) {
        if (button != 0) return false;

        int panelW = right - left;
        int headerY = top;

        // === Search field click (in header) ===
        String countStr = enabledCount + "/" + totalCount + " enabled";
        int countW = font.width(countStr);
        int searchFieldW = 80;
        int searchFieldX = right - countW - 12 - searchFieldW - 6;
        int searchFieldY = headerY + 4;

        if (mouseX >= searchFieldX && mouseX < searchFieldX + searchFieldW
                && mouseY >= searchFieldY && mouseY < searchFieldY + 12) {
            searchFocused = true;
            return true;
        }

        // Unfocus search if clicking elsewhere
        if (searchFocused && (mouseX < searchFieldX || mouseX >= searchFieldX + searchFieldW
                || mouseY < searchFieldY || mouseY >= searchFieldY + 12)) {
            searchFocused = false;
        }

        int yOffset = headerY + HEADER_HEIGHT + 2;

        // === Category tab clicks ===
        int catTabX = left + 4;
        int catTabY = yOffset;
        for (int i = 0; i < CATEGORIES.length; i++) {
            String cat = CATEGORIES[i];
            int catCount = countInCategory(cat);
            int catEnabled = countEnabledInCategory(cat);
            String tabLabel = cat;
            if (!"All".equals(cat)) {
                tabLabel = cat + " (" + catEnabled + "/" + catCount + ")";
            }
            int tw = font.width(tabLabel) + 10;

            if (mouseX >= catTabX && mouseX < catTabX + tw
                    && mouseY >= catTabY && mouseY < catTabY + CATEGORY_TAB_HEIGHT) {
                selectedCategory = cat;
                scroll = 0;
                selectedModuleIdx = -1;
                settingsScroll = 0;
                applyFilter();
                return true;
            }
            catTabX += tw + 3;
        }

        yOffset = catTabY + CATEGORY_TAB_HEIGHT + 4;

        // === Split panel areas ===
        int splitX = left + (int) (panelW * 0.6);
        int listLeft = left;
        int listRight = splitX - 2;
        int settingsLeft = splitX + 2;
        int settingsRight = right;

        int contentTop = yOffset;
        int contentBottom = bottom - BULK_BAR_HEIGHT - 4;

        // === Left panel - Module list clicks ===
        if (mouseX >= listLeft && mouseX < listRight && mouseY >= contentTop && mouseY < contentBottom) {
            return handleModuleListClick(mouseX, mouseY, listLeft, contentTop, listRight, contentBottom);
        }

        // === Right panel - Settings clicks ===
        if (mouseX >= settingsLeft && mouseX < settingsRight && mouseY >= contentTop && mouseY < contentBottom) {
            return handleSettingsClick(mouseX, mouseY, settingsLeft, contentTop, settingsRight, contentBottom);
        }

        // === Bulk action bar clicks ===
        int bulkTop = bottom - BULK_BAR_HEIGHT - 2;
        if (mouseY >= bulkTop && mouseY < bottom) {
            return handleBulkBarClick(mouseX, mouseY, left, bulkTop, right, bottom);
        }

        return false;
    }

    private boolean handleModuleListClick(double mouseX, double mouseY, int left, int top, int right, int bottom) {
        for (int i = 0; i < filteredModules.size(); i++) {
            ModuleEntry mod = filteredModules.get(i);
            int rowY = top + (i * ROW_HEIGHT) - scroll;

            if (rowY + ROW_HEIGHT < top || rowY > bottom) continue;

            if (mouseY >= rowY && mouseY < rowY + ROW_HEIGHT) {
                // Check toggle button click
                int toggleW = 30;
                int toggleH = 14;
                int toggleX = right - toggleW - 8;
                int toggleY = rowY + (ROW_HEIGHT - toggleH) / 2;

                if (mouseX >= toggleX && mouseX < toggleX + toggleW
                        && mouseY >= toggleY && mouseY < toggleY + toggleH) {
                    // Toggle module
                    toggleModule(mod, i);
                    return true;
                }

                // Select module
                selectedModuleIdx = i;
                settingsScroll = 0;
                return true;
            }
        }
        return false;
    }

    private boolean handleSettingsClick(double mouseX, double mouseY, int left, int top, int right, int bottom) {
        if (selectedModuleIdx < 0 || selectedModuleIdx >= filteredModules.size()) return false;

        ModuleEntry mod = filteredModules.get(selectedModuleIdx);

        // Calculate settings content start Y (account for header content)
        int y = top + 4; // Module name
        y += 12; // Category badge

        // Toggle key button click check
        String toggleKeyLabel = "Toggle Key: ";
        int tkBtnX = left + 6 + font.width(toggleKeyLabel);
        boolean isListeningToggle = listeningForToggleKey && listeningToggleKeyModuleId.equals(mod.id());
        String toggleKeyValue = isListeningToggle ? "> Press key... <" : (mod.toggleKey().equals("NONE") ? "None" : mod.toggleKey());
        int tkBtnW = font.width(toggleKeyValue) + 8;
        int tkBtnY = y - 1;
        int tkBtnH = 12;
        if (mouseX >= tkBtnX && mouseX < tkBtnX + tkBtnW && mouseY >= tkBtnY && mouseY < tkBtnY + tkBtnH) {
            listeningForToggleKey = !listeningForToggleKey;
            listeningToggleKeyModuleId = mod.id();
            // Cancel any keybind setting listening
            listeningForKeybind = false;
            return true;
        }
        y += 14; // Toggle key row

        // Description lines
        int panelW = right - left;
        List<String> descLines = wrapText(mod.desc(), panelW - 14);
        y += descLines.size() * 10;
        y += 4; // Gap
        y += 1; // Separator
        y += 6; // Gap after separator

        if (mod.settings().isEmpty()) return false;

        y += 14; // "Settings" header

        int settingsTop = y;

        for (int i = 0; i < mod.settings().size(); i++) {
            SettingEntry setting = mod.settings().get(i);
            int settingY = settingsTop + (i * SETTING_ROW_HEIGHT) - settingsScroll;

            if (settingY + SETTING_ROW_HEIGHT < top || settingY > bottom) continue;
            if (mouseY < settingY || mouseY >= settingY + SETTING_ROW_HEIGHT) continue;

            switch (setting.type()) {
                case "bool" -> {
                    // Toggle boolean
                    boolean current = "true".equalsIgnoreCase(setting.value());
                    String newVal = current ? "false" : "true";
                    sendSettingChange(mod.id(), setting.name(), newVal);
                    updateLocalSetting(mod.id(), i, newVal);
                    return true;
                }
                case "int", "double" -> {
                    // Check if click is on slider area
                    int sliderLeft = left + 6;
                    String valStr = setting.value();
                    if ("double".equals(setting.type())) {
                        try {
                            valStr = String.format("%.2f", Double.parseDouble(valStr));
                        } catch (NumberFormatException ignored) {}
                    }
                    int valW = font.width(valStr);
                    int sliderRight = right - 6 - valW - 6;
                    int sliderY = settingY + 12;

                    if (mouseY >= sliderY - 2 && mouseY < sliderY + SLIDER_HEIGHT + 2
                            && mouseX >= sliderLeft && mouseX < sliderRight) {
                        // Start dragging
                        draggingSettingIdx = i;
                        dragSliderLeft = sliderLeft;
                        dragSliderRight = sliderRight;
                        dragMin = setting.min();
                        dragMax = setting.max();
                        dragModuleId = mod.id();
                        dragSettingName = setting.name();
                        dragSettingType = setting.type();

                        // Immediately update to clicked position
                        updateSliderFromMouse(mouseX);
                        return true;
                    }
                }
                case "enum" -> {
                    // Cycle to next option
                    if (setting.options() != null && !setting.options().isEmpty()) {
                        int currentIdx = setting.options().indexOf(setting.value());
                        int nextIdx = (currentIdx + 1) % setting.options().size();
                        String newVal = setting.options().get(nextIdx);
                        sendSettingChange(mod.id(), setting.name(), newVal);
                        updateLocalSetting(mod.id(), i, newVal);
                        return true;
                    }
                }
                case "keybind" -> {
                    // Enter listening mode for this keybind setting
                    listeningForKeybind = true;
                    listeningSettingIdx = i;
                    listeningModuleId = mod.id();
                    // Cancel toggle key listening
                    listeningForToggleKey = false;
                    return true;
                }
            }
        }
        return false;
    }

    private boolean handleBulkBarClick(double mouseX, double mouseY, int left, int top, int right, int bottom) {
        int btnW = 70;
        int btnH = 16;
        int btnY = top + (bottom - top - btnH) / 2;
        int btnGap = 6;

        // Enable All
        int enableBtnX = left + 6;
        if (mouseX >= enableBtnX && mouseX < enableBtnX + btnW && mouseY >= btnY && mouseY < btnY + btnH) {
            String category = "All".equals(selectedCategory) ? "all" : selectedCategory.toLowerCase(Locale.ROOT);
            sendAction("adminmod_enable_category", category);
            // Optimistic update
            for (int i = 0; i < allModules.size(); i++) {
                ModuleEntry m = allModules.get(i);
                if ("All".equals(selectedCategory) || m.category().equalsIgnoreCase(selectedCategory)) {
                    allModules.set(i, new ModuleEntry(m.id(), m.name(), m.desc(), m.category(), true, m.toggleKey(), m.settings()));
                }
            }
            recountEnabled();
            applyFilter();
            return true;
        }

        // Disable All
        int disableBtnX = enableBtnX + btnW + btnGap;
        if (mouseX >= disableBtnX && mouseX < disableBtnX + btnW && mouseY >= btnY && mouseY < btnY + btnH) {
            String category = "All".equals(selectedCategory) ? "all" : selectedCategory.toLowerCase(Locale.ROOT);
            sendAction("adminmod_disable_category", category);
            // Optimistic update
            for (int i = 0; i < allModules.size(); i++) {
                ModuleEntry m = allModules.get(i);
                if ("All".equals(selectedCategory) || m.category().equalsIgnoreCase(selectedCategory)) {
                    allModules.set(i, new ModuleEntry(m.id(), m.name(), m.desc(), m.category(), false, m.toggleKey(), m.settings()));
                }
            }
            recountEnabled();
            applyFilter();
            return true;
        }

        return false;
    }

    // ==================== MOUSE DRAGGED ====================

    public boolean mouseDragged(double mouseX, double mouseY, int button) {
        if (button == 0 && draggingSettingIdx >= 0) {
            updateSliderFromMouse(mouseX);
            return true;
        }
        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && draggingSettingIdx >= 0) {
            // Send the final value
            if (selectedModuleIdx >= 0 && selectedModuleIdx < filteredModules.size()) {
                ModuleEntry mod = filteredModules.get(selectedModuleIdx);
                if (draggingSettingIdx < mod.settings().size()) {
                    SettingEntry setting = mod.settings().get(draggingSettingIdx);
                    sendSettingChange(dragModuleId, dragSettingName, setting.value());
                }
            }
            draggingSettingIdx = -1;
            return true;
        }
        return false;
    }

    private void updateSliderFromMouse(double mouseX) {
        if (draggingSettingIdx < 0) return;
        if (selectedModuleIdx < 0 || selectedModuleIdx >= filteredModules.size()) return;

        ModuleEntry mod = filteredModules.get(selectedModuleIdx);
        if (draggingSettingIdx >= mod.settings().size()) return;

        int sliderW = dragSliderRight - dragSliderLeft;
        double ratio = (mouseX - dragSliderLeft) / (double) sliderW;
        ratio = Math.max(0, Math.min(1, ratio));

        double newVal = dragMin + ratio * (dragMax - dragMin);

        String newValStr;
        if ("int".equals(dragSettingType)) {
            int intVal = (int) Math.round(newVal);
            intVal = Math.max((int) dragMin, Math.min((int) dragMax, intVal));
            newValStr = String.valueOf(intVal);
        } else {
            newVal = Math.max(dragMin, Math.min(dragMax, newVal));
            newValStr = String.format("%.2f", newVal);
        }

        updateLocalSetting(mod.id(), draggingSettingIdx, newValStr);
    }

    // ==================== MOUSE SCROLLED ====================

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        // Determine which panel the mouse is over
        // We need the layout bounds, which depend on the render area.
        // For simplicity, scroll the module list by default; settings panel scrolls if
        // the mouse is on the right side (handled by checking X position later if needed).
        // Since we don't have the panel bounds here, always scroll the module list.
        int scrollAmount = (int) (-scrollY * 12);
        scroll += scrollAmount;
        if (scroll < 0) scroll = 0;
        if (scroll > maxScroll) scroll = maxScroll;
        return true;
    }

    /**
     * Extended scroll handler that receives panel bounds for context-aware scrolling.
     */
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY,
                                   int left, int top, int right, int bottom) {
        int panelW = right - left;
        int splitX = left + (int) (panelW * 0.6);

        int scrollAmount = (int) (-scrollY * 12);

        if (mouseX >= splitX) {
            // Scroll settings panel
            settingsScroll += scrollAmount;
            if (settingsScroll < 0) settingsScroll = 0;
            if (settingsScroll > settingsMaxScroll) settingsScroll = settingsMaxScroll;
        } else {
            // Scroll module list
            scroll += scrollAmount;
            if (scroll < 0) scroll = 0;
            if (scroll > maxScroll) scroll = maxScroll;
        }
        return true;
    }

    // ==================== KEY / CHAR INPUT ====================

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Handle keybind listening mode for toggle key
        if (listeningForToggleKey) {
            if (keyCode == 256) {
                // Escape = cancel
                listeningForToggleKey = false;
                return true;
            }
            if (keyCode == 261) {
                // Delete = clear keybind
                String moduleId = listeningToggleKeyModuleId;
                sendAction("adminmod_set_togglekey", moduleId + ":NONE");
                updateLocalToggleKey(moduleId, "NONE");
                listeningForToggleKey = false;
                return true;
            }
            String keyName = getKeyName(keyCode);
            if (keyName != null) {
                String moduleId = listeningToggleKeyModuleId;
                sendAction("adminmod_set_togglekey", moduleId + ":" + keyName);
                updateLocalToggleKey(moduleId, keyName);
                listeningForToggleKey = false;
                return true;
            }
            return true;
        }

        // Handle keybind listening mode for keybind settings
        if (listeningForKeybind) {
            if (keyCode == 256) {
                // Escape = cancel
                listeningForKeybind = false;
                return true;
            }
            if (keyCode == 261) {
                // Delete = clear keybind
                sendSettingChange(listeningModuleId, getSettingNameForListening(), "NONE");
                updateLocalSetting(listeningModuleId, listeningSettingIdx, "NONE");
                listeningForKeybind = false;
                return true;
            }
            String keyName = getKeyName(keyCode);
            if (keyName != null) {
                sendSettingChange(listeningModuleId, getSettingNameForListening(), keyName);
                updateLocalSetting(listeningModuleId, listeningSettingIdx, keyName);
                listeningForKeybind = false;
                return true;
            }
            return true;
        }

        if (!searchFocused) return false;

        // Backspace
        if (keyCode == 259) {
            if (!searchText.isEmpty() && searchCursorPos > 0) {
                searchText = searchText.substring(0, searchCursorPos - 1) + searchText.substring(searchCursorPos);
                searchCursorPos--;
                scroll = 0;
                selectedModuleIdx = -1;
                applyFilter();
            }
            return true;
        }
        // Delete
        if (keyCode == 261) {
            if (searchCursorPos < searchText.length()) {
                searchText = searchText.substring(0, searchCursorPos) + searchText.substring(searchCursorPos + 1);
                scroll = 0;
                selectedModuleIdx = -1;
                applyFilter();
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
        // Escape
        if (keyCode == 256) {
            searchFocused = false;
            return true;
        }
        // Ctrl+A
        if (keyCode == 65 && (modifiers & 2) != 0) {
            return true;
        }

        return false;
    }

    public boolean charTyped(char c, int modifiers) {
        // Consume char input when listening for keybinds to prevent it going to search
        if (listeningForKeybind || listeningForToggleKey) return true;
        if (!searchFocused) return false;

        if (c >= 32 && c != 127) {
            if (searchText.length() < 64) {
                searchText = searchText.substring(0, searchCursorPos) + c + searchText.substring(searchCursorPos);
                searchCursorPos++;
                scroll = 0;
                selectedModuleIdx = -1;
                applyFilter();
            }
            return true;
        }
        return false;
    }

    // ==================== HANDLE RESPONSE ====================

    public void handleResponse(String type, String jsonData) {
        if (!"adminmod_data".equals(type)) return;

        try {
            JsonObject root = JsonParser.parseString(jsonData).getAsJsonObject();
            enabledCount = root.has("enabled") ? root.get("enabled").getAsInt() : 0;
            totalCount = root.has("total") ? root.get("total").getAsInt() : 0;

            allModules.clear();
            if (root.has("modules")) {
                JsonArray arr = root.getAsJsonArray("modules");
                for (JsonElement el : arr) {
                    JsonObject obj = el.getAsJsonObject();

                    List<SettingEntry> settings = new ArrayList<>();
                    if (obj.has("settings")) {
                        JsonArray settingsArr = obj.getAsJsonArray("settings");
                        for (JsonElement sEl : settingsArr) {
                            JsonObject sObj = sEl.getAsJsonObject();

                            List<String> options = new ArrayList<>();
                            if (sObj.has("options")) {
                                JsonArray optArr = sObj.getAsJsonArray("options");
                                for (JsonElement optEl : optArr) {
                                    options.add(optEl.getAsString());
                                }
                            }

                            settings.add(new SettingEntry(
                                    sObj.has("name") ? sObj.get("name").getAsString() : "",
                                    sObj.has("type") ? sObj.get("type").getAsString() : "string",
                                    sObj.has("value") ? sObj.get("value").getAsString() : "",
                                    sObj.has("desc") ? sObj.get("desc").getAsString() : "",
                                    sObj.has("min") ? sObj.get("min").getAsDouble() : 0,
                                    sObj.has("max") ? sObj.get("max").getAsDouble() : 100,
                                    options
                            ));
                        }
                    }

                    allModules.add(new ModuleEntry(
                            obj.has("id") ? obj.get("id").getAsString() : "",
                            obj.has("name") ? obj.get("name").getAsString() : "",
                            obj.has("desc") ? obj.get("desc").getAsString() : "",
                            obj.has("category") ? obj.get("category").getAsString() : "Misc",
                            obj.has("enabled") && obj.get("enabled").getAsBoolean(),
                            obj.has("toggleKey") ? obj.get("toggleKey").getAsString() : "NONE",
                            settings
                    ));
                }
            }

            applyFilter();
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to parse admin modules data", e);
        }
    }

    // ==================== PRIVATE HELPERS ====================

    private void toggleModule(ModuleEntry mod, int filteredIdx) {
        boolean newState = !mod.enabled();
        sendAction("adminmod_toggle", mod.id());

        // Optimistic local update
        for (int j = 0; j < allModules.size(); j++) {
            if (allModules.get(j).id().equals(mod.id())) {
                ModuleEntry old = allModules.get(j);
                allModules.set(j, new ModuleEntry(old.id(), old.name(), old.desc(), old.category(), newState, old.toggleKey(), old.settings()));
                break;
            }
        }
        if (newState) enabledCount++;
        else enabledCount--;
        applyFilter();

        // Keep selection stable
        if (selectedModuleIdx == filteredIdx) {
            // Re-find the module in the new filtered list
            for (int k = 0; k < filteredModules.size(); k++) {
                if (filteredModules.get(k).id().equals(mod.id())) {
                    selectedModuleIdx = k;
                    break;
                }
            }
        }
    }

    private void sendSettingChange(String moduleId, String settingName, String newValue) {
        sendAction("adminmod_set_setting", moduleId + ":" + settingName + ":" + newValue);
    }

    private void updateLocalSetting(String moduleId, int settingIdx, String newValue) {
        for (int i = 0; i < allModules.size(); i++) {
            ModuleEntry m = allModules.get(i);
            if (m.id().equals(moduleId)) {
                List<SettingEntry> newSettings = new ArrayList<>(m.settings());
                if (settingIdx < newSettings.size()) {
                    SettingEntry old = newSettings.get(settingIdx);
                    newSettings.set(settingIdx, new SettingEntry(
                            old.name(), old.type(), newValue, old.desc(), old.min(), old.max(), old.options()));
                }
                allModules.set(i, new ModuleEntry(m.id(), m.name(), m.desc(), m.category(), m.enabled(), m.toggleKey(), newSettings));
                break;
            }
        }
        applyFilter();

        // Re-select the module by id to keep selection stable
        if (selectedModuleIdx >= 0 && selectedModuleIdx < filteredModules.size()) {
            String selectedId = filteredModules.get(selectedModuleIdx).id();
            // The filter may have reordered; find again
            for (int k = 0; k < filteredModules.size(); k++) {
                if (filteredModules.get(k).id().equals(selectedId)) {
                    selectedModuleIdx = k;
                    break;
                }
            }
        }
    }

    private String getSettingNameForListening() {
        if (selectedModuleIdx >= 0 && selectedModuleIdx < filteredModules.size()) {
            ModuleEntry mod = filteredModules.get(selectedModuleIdx);
            if (listeningSettingIdx >= 0 && listeningSettingIdx < mod.settings().size()) {
                return mod.settings().get(listeningSettingIdx).name();
            }
        }
        return "";
    }

    private void updateLocalToggleKey(String moduleId, String newKey) {
        for (int i = 0; i < allModules.size(); i++) {
            ModuleEntry m = allModules.get(i);
            if (m.id().equals(moduleId)) {
                allModules.set(i, new ModuleEntry(m.id(), m.name(), m.desc(), m.category(), m.enabled(), newKey, m.settings()));
                break;
            }
        }
        applyFilter();
    }

    private static String getKeyName(int keyCode) {
        return switch (keyCode) {
            case 65 -> "A"; case 66 -> "B"; case 67 -> "C"; case 68 -> "D";
            case 69 -> "E"; case 70 -> "F"; case 71 -> "G"; case 72 -> "H";
            case 73 -> "I"; case 74 -> "J"; case 75 -> "K"; case 76 -> "L";
            case 77 -> "M"; case 78 -> "N"; case 79 -> "O"; case 80 -> "P";
            case 81 -> "Q"; case 82 -> "R"; case 83 -> "S"; case 84 -> "T";
            case 85 -> "U"; case 86 -> "V"; case 87 -> "W"; case 88 -> "X";
            case 89 -> "Y"; case 90 -> "Z";
            case 48 -> "0"; case 49 -> "1"; case 50 -> "2"; case 51 -> "3";
            case 52 -> "4"; case 53 -> "5"; case 54 -> "6"; case 55 -> "7";
            case 56 -> "8"; case 57 -> "9";
            case 290 -> "F1"; case 291 -> "F2"; case 292 -> "F3"; case 293 -> "F4";
            case 294 -> "F5"; case 295 -> "F6"; case 296 -> "F7"; case 297 -> "F8";
            case 298 -> "F9"; case 299 -> "F10"; case 300 -> "F11"; case 301 -> "F12";
            case 340 -> "LSHIFT"; case 344 -> "RSHIFT";
            case 341 -> "LCTRL"; case 345 -> "RCTRL";
            case 342 -> "LALT"; case 346 -> "RALT";
            case 320 -> "KP0"; case 321 -> "KP1"; case 322 -> "KP2"; case 323 -> "KP3";
            case 324 -> "KP4"; case 325 -> "KP5"; case 326 -> "KP6"; case 327 -> "KP7";
            case 328 -> "KP8"; case 329 -> "KP9";
            case 45 -> "MINUS"; case 61 -> "EQUALS"; case 91 -> "LBRACKET";
            case 93 -> "RBRACKET"; case 59 -> "SEMICOLON"; case 39 -> "APOSTROPHE";
            case 44 -> "COMMA"; case 46 -> "PERIOD"; case 47 -> "SLASH";
            case 92 -> "BACKSLASH"; case 96 -> "GRAVE";
            default -> null;
        };
    }

    private void applyFilter() {
        String selectedId = null;
        if (selectedModuleIdx >= 0 && selectedModuleIdx < filteredModules.size()) {
            selectedId = filteredModules.get(selectedModuleIdx).id();
        }

        filteredModules.clear();
        String lowerSearch = searchText.toLowerCase(Locale.ROOT);

        for (ModuleEntry m : allModules) {
            // Category filter
            if (!"All".equals(selectedCategory) && !m.category().equalsIgnoreCase(selectedCategory)) {
                continue;
            }
            // Search filter
            if (!lowerSearch.isEmpty()) {
                if (!m.name().toLowerCase(Locale.ROOT).contains(lowerSearch)
                        && !m.id().toLowerCase(Locale.ROOT).contains(lowerSearch)
                        && !m.desc().toLowerCase(Locale.ROOT).contains(lowerSearch)) {
                    continue;
                }
            }
            filteredModules.add(m);
        }

        // Restore selection by id
        if (selectedId != null) {
            selectedModuleIdx = -1;
            for (int k = 0; k < filteredModules.size(); k++) {
                if (filteredModules.get(k).id().equals(selectedId)) {
                    selectedModuleIdx = k;
                    break;
                }
            }
        }
        if (selectedModuleIdx >= filteredModules.size()) {
            selectedModuleIdx = -1;
        }
    }

    private void recountEnabled() {
        enabledCount = 0;
        for (ModuleEntry m : allModules) {
            if (m.enabled()) enabledCount++;
        }
        totalCount = allModules.size();
    }

    private int countInCategory(String category) {
        if ("All".equals(category)) return allModules.size();
        int count = 0;
        for (ModuleEntry m : allModules) {
            if (m.category().equalsIgnoreCase(category)) count++;
        }
        return count;
    }

    private int countEnabledInCategory(String category) {
        if ("All".equals(category)) return enabledCount;
        int count = 0;
        for (ModuleEntry m : allModules) {
            if (m.category().equalsIgnoreCase(category) && m.enabled()) count++;
        }
        return count;
    }

    private static int getCategoryColor(String category) {
        if (category == null) return CAT_MISC;
        switch (category.toLowerCase(Locale.ROOT)) {
            case "combat": return CAT_COMBAT;
            case "movement": return CAT_MOVEMENT;
            case "render": return CAT_RENDER;
            case "player": return CAT_PLAYER;
            case "world": return CAT_WORLD;
            case "misc": return CAT_MISC;
            case "all": return ACCENT;
            default: return LABEL;
        }
    }

    private String formatMinMax(double val, String type) {
        if ("int".equals(type)) {
            return String.valueOf((int) val);
        }
        return String.format("%.1f", val);
    }

    private List<String> wrapText(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            lines.add("");
            return lines;
        }

        String[] words = text.split("\\s+");
        StringBuilder current = new StringBuilder();

        for (String word : words) {
            if (current.length() == 0) {
                current.append(word);
            } else {
                String test = current + " " + word;
                if (font.width(test) > maxWidth) {
                    lines.add(current.toString());
                    current = new StringBuilder(word);
                } else {
                    current.append(" ").append(word);
                }
            }
        }
        if (current.length() > 0) {
            lines.add(current.toString());
        }
        return lines;
    }

    private void sendAction(String action, String data) {
        ClientPacketDistributor.sendToServer(
                (CustomPacketPayload) new ComputerActionPayload(action, data),
                (CustomPacketPayload[]) new CustomPacketPayload[0]);
    }

    private static void drawRectOutline(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        g.fill(x1, y1, x2, y1 + 1, color);      // top
        g.fill(x1, y2 - 1, x2, y2, color);       // bottom
        g.fill(x1, y1, x1 + 1, y2, color);       // left
        g.fill(x2 - 1, y1, x2, y2, color);       // right
    }
}

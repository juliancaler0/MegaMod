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
import java.util.List;

public class SchedulerPanel {
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

    // Layout constants
    private static final int ROW_H = 12;
    private static final int BTN_H = 16;
    private static final int BTN_PAD = 4;
    private static final int SECTION_GAP = 8;
    private static final int SCHEDULE_ROW_H = 28;
    private static final int LOG_ROW_H = 11;

    // Scroll state
    private int scheduleScroll = 0;
    private int logScroll = 0;

    // Text input state
    private String inputName = "";
    private String inputCommand = "";
    private String inputInterval = "5m";
    private boolean inputRepeat = true;
    private boolean nameBoxFocused = false;
    private boolean commandBoxFocused = false;
    private boolean intervalBoxFocused = false;

    // Cursor blink
    private long lastBlinkTime = 0;
    private boolean cursorVisible = true;

    // Data from server
    private List<ScheduleEntry> schedules = new ArrayList<>();
    private List<LogEntry> executionLog = new ArrayList<>();

    // Status message
    private String statusMessage = "";
    private int statusColor = TEXT;
    private long statusExpiry = 0;

    // Interval presets
    private static final String[] INTERVAL_PRESETS = {"1m", "5m", "10m", "30m", "1h", "6h", "12h", "24h"};

    // Quick-add presets
    private static final String[] PRESET_NAMES = {"Auto-Save", "Day Cycle", "Clear Mobs", "Restart Warning", "Double XP", "Weather Clear", "Auto-Backup", "Mob Cap", "Night Skip"};
    private static final String[] PRESET_KEYS = {"auto_save", "day_cycle", "clear_mobs", "restart_warn", "double_xp", "weather_clear", "auto_backup", "mob_cap", "night_skip"};
    private static final String[] PRESET_DESCS = {"save-all / 5m", "time set day / 5m", "kill hostiles / 10m", "broadcast / 6h", "broadcast / toggle", "weather clear / 30m", "save-all flush / 30m", "kill nearby mobs / 15m", "time set day / 10m"};

    // View mode: "schedules" or "log"
    private String viewMode = "schedules";

    public record ScheduleEntry(String id, String name, String command, String interval, String nextRun, int runCount, boolean active) {}
    public record LogEntry(String timestamp, String name, String command, String result) {}

    public SchedulerPanel(Font font) {
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
        g.drawString(font, "Scheduled Commands", left + 6, top + 6, ACCENT, false);

        // View mode tabs (right-aligned in title bar)
        int tabW = 60;
        int tabX = right - tabW * 2 - 8;
        renderTabButton(g, mouseX, mouseY, tabX, top + 2, tabW, 16, "Schedules", "schedules".equals(viewMode));
        tabX += tabW + 2;
        renderTabButton(g, mouseX, mouseY, tabX, top + 2, tabW, 16, "Log", "log".equals(viewMode));

        int y = top + 24;

        if ("log".equals(viewMode)) {
            renderLogView(g, mouseX, mouseY, left, y, right, bottom);
        } else {
            renderSchedulesView(g, mouseX, mouseY, left, y, right, bottom);
        }

        // Status message at bottom
        if (!statusMessage.isEmpty() && System.currentTimeMillis() < statusExpiry) {
            g.drawString(font, statusMessage, left + 6, bottom - 14, statusColor, false);
        } else {
            statusMessage = "";
        }
    }

    private void renderSchedulesView(GuiGraphics g, int mouseX, int mouseY, int left, int y, int right, int bottom) {
        int w = right - left;

        // --- CREATE NEW SCHEDULE section ---
        int createTop = y;
        g.fill(left + 2, createTop, right - 2, createTop + 18, HEADER_BG);
        drawBorder(g, left + 2, createTop, right - 2, createTop + 18, BORDER);
        g.drawString(font, "Create New Schedule", left + 8, createTop + 5, TEXT, false);
        y = createTop + 20;

        // Name input
        g.drawString(font, "Name:", left + 6, y + 4, LABEL, false);
        int nameBoxX = left + 42;
        int nameBoxW = Math.min(140, w - 48);
        renderInputBox(g, mouseX, mouseY, nameBoxX, y, nameBoxW, 14, inputName, nameBoxFocused);
        y += 18;

        // Command input
        g.drawString(font, "Cmd:", left + 6, y + 4, LABEL, false);
        int cmdBoxX = left + 42;
        int cmdBoxW = Math.min(200, w - 48);
        renderInputBox(g, mouseX, mouseY, cmdBoxX, y, cmdBoxW, 14, inputCommand, commandBoxFocused);
        y += 18;

        // Interval row: label + preset buttons + custom input
        g.drawString(font, "Interval:", left + 6, y + 4, LABEL, false);
        int ix = left + 56;
        for (String preset : INTERVAL_PRESETS) {
            int pw = font.width(preset) + 8;
            boolean selected = preset.equals(inputInterval) && !intervalBoxFocused;
            int btnBg = selected ? darken(ACCENT, 0.5f) : (isHovered(mouseX, mouseY, ix, y, ix + pw, y + 14) ? 0xFF21262D : HEADER_BG);
            g.fill(ix, y, ix + pw, y + 14, btnBg);
            drawBorder(g, ix, y, ix + pw, y + 14, selected ? ACCENT : BORDER);
            g.drawString(font, preset, ix + 4, y + 3, selected ? TEXT : LABEL, false);
            ix += pw + 2;
        }

        // Custom interval input
        g.drawString(font, "or", ix + 2, y + 3, LABEL, false);
        ix += 16;
        int intervalBoxW = 50;
        renderInputBox(g, mouseX, mouseY, ix, y, intervalBoxW, 14, inputInterval, intervalBoxFocused);
        y += 18;

        // Repeat toggle + Create button
        int toggleX = left + 6;
        boolean toggleHover = isHovered(mouseX, mouseY, toggleX, y, toggleX + 80, y + BTN_H);
        int toggleBg = inputRepeat ? darken(SUCCESS, 0.5f) : darken(ERROR, 0.5f);
        if (toggleHover) toggleBg = brighten(toggleBg, 0.2f);
        g.fill(toggleX, y, toggleX + 80, y + BTN_H, toggleBg);
        drawBorder(g, toggleX, y, toggleX + 80, y + BTN_H, inputRepeat ? SUCCESS : ERROR);
        g.drawString(font, inputRepeat ? "Repeat: ON" : "Repeat: OFF", toggleX + 4, y + 4, inputRepeat ? SUCCESS : ERROR, false);

        int createBtnX = toggleX + 84;
        int createBtnW = 60;
        renderButton(g, mouseX, mouseY, createBtnX, y, createBtnW, BTN_H, "Create", ACCENT);
        y += BTN_H + SECTION_GAP;

        // --- PRESETS section ---
        g.fill(left + 2, y, right - 2, y + 18, HEADER_BG);
        drawBorder(g, left + 2, y, right - 2, y + 18, BORDER);
        g.drawString(font, "Quick-Add Presets", left + 8, y + 5, TEXT, false);
        y += 20;

        int presetBtnW = (w - 16) / 3;
        for (int i = 0; i < PRESET_NAMES.length; i++) {
            int col = i % 3;
            int row = i / 3;
            int px = left + 4 + col * (presetBtnW + 2);
            int py = y + row * (BTN_H + 2);
            boolean hovered = isHovered(mouseX, mouseY, px, py, px + presetBtnW, py + BTN_H);
            int bg = hovered ? 0xFF21262D : HEADER_BG;
            g.fill(px, py, px + presetBtnW, py + BTN_H, bg);
            drawBorder(g, px, py, px + presetBtnW, py + BTN_H, hovered ? ACCENT : BORDER);
            g.drawString(font, truncate(PRESET_NAMES[i], presetBtnW - 6), px + 3, py + 1, hovered ? TEXT : ACCENT, false);
            g.drawString(font, truncate(PRESET_DESCS[i], presetBtnW - 6), px + 3, py + 9, LABEL, false);
        }
        int presetRows = (PRESET_NAMES.length + 2) / 3;
        y += presetRows * (BTN_H + 2) + SECTION_GAP;

        // --- ACTIVE SCHEDULES list ---
        g.fill(left + 2, y, right - 2, y + 18, HEADER_BG);
        drawBorder(g, left + 2, y, right - 2, y + 18, BORDER);
        g.drawString(font, "Active Schedules (" + schedules.size() + ")", left + 8, y + 5, TEXT, false);

        // Refresh button in header
        int refreshBtnX = right - 52;
        renderButton(g, mouseX, mouseY, refreshBtnX, y + 1, 48, 16, "Refresh", ACCENT);
        y += 20;

        int listTop = y;
        int listBottom = bottom - 18;
        int listH = listBottom - listTop;

        g.fill(left + 2, listTop, right - 2, listBottom, BG);
        drawBorder(g, left + 2, listTop, right - 2, listBottom, BORDER);

        if (schedules.isEmpty()) {
            g.drawString(font, "No scheduled commands. Create one above.", left + 8, listTop + 6, LABEL, false);
        } else {
            int visibleRows = Math.max(1, listH / SCHEDULE_ROW_H);
            int maxScroll = Math.max(0, schedules.size() - visibleRows);
            scheduleScroll = Math.min(scheduleScroll, maxScroll);

            g.enableScissor(left + 3, listTop + 1, right - 3, listBottom - 1);
            int ry = listTop + 2;
            for (int i = scheduleScroll; i < schedules.size() && ry + SCHEDULE_ROW_H <= listBottom; i++) {
                ScheduleEntry entry = schedules.get(i);
                renderScheduleRow(g, mouseX, mouseY, left + 4, ry, right - 4, entry, i);
                ry += SCHEDULE_ROW_H;
            }
            g.disableScissor();

            // Scrollbar
            if (schedules.size() > visibleRows) {
                int sbH = listH - 4;
                int thumbH = Math.max(10, sbH * visibleRows / schedules.size());
                int thumbY = listTop + 2 + (maxScroll > 0 ? (scheduleScroll * (sbH - thumbH) / maxScroll) : 0);
                g.fill(right - 6, listTop + 2, right - 3, listBottom - 2, HEADER_BG);
                g.fill(right - 6, thumbY, right - 3, thumbY + thumbH, LABEL);
            }
        }
    }

    private void renderScheduleRow(GuiGraphics g, int mouseX, int mouseY, int left, int top, int right, ScheduleEntry entry, int index) {
        int w = right - left;

        // Row background
        int rowBg = (index % 2 == 0) ? 0xFF0F1318 : BG;
        g.fill(left, top, right, top + SCHEDULE_ROW_H - 1, rowBg);

        // Status indicator
        int statusColor = entry.active() ? SUCCESS : WARNING;
        g.fill(left + 1, top + 2, left + 4, top + SCHEDULE_ROW_H - 3, statusColor);

        // Name + command (line 1)
        String nameStr = entry.name();
        g.drawString(font, truncate(nameStr, 100), left + 7, top + 2, TEXT, false);
        g.drawString(font, truncate("/" + entry.command(), w - 120), left + 7 + Math.min(font.width(nameStr) + 6, 106), top + 2, LABEL, false);

        // Interval + next run + run count (line 2)
        String statusStr = entry.active() ? "Active" : "Paused";
        String infoStr = "Every " + entry.interval() + " | Next: " + entry.nextRun() + " | Runs: " + entry.runCount() + " | " + statusStr;
        g.drawString(font, truncate(infoStr, w - 130), left + 7, top + 13, LABEL, false);

        // Action buttons (right-aligned)
        int btnW = 30;
        int bx = right - btnW * 4 - BTN_PAD * 3 - 8;
        int by = top + 6;

        // Pause/Resume button
        if (entry.active()) {
            renderButton(g, mouseX, mouseY, bx, by, btnW, 14, "Pause", WARNING);
        } else {
            renderButton(g, mouseX, mouseY, bx, by, btnW, 14, "Play", SUCCESS);
        }
        bx += btnW + BTN_PAD;

        // Run Now button
        renderButton(g, mouseX, mouseY, bx, by, btnW + 4, 14, "Run", ACCENT);
        bx += btnW + 4 + BTN_PAD;

        // Duplicate button
        renderButton(g, mouseX, mouseY, bx, by, btnW - 4, 14, "Dup", ACCENT);
        bx += btnW - 4 + BTN_PAD;

        // Delete button
        renderButton(g, mouseX, mouseY, bx, by, btnW - 4, 14, "Del", ERROR);
    }

    private void renderLogView(GuiGraphics g, int mouseX, int mouseY, int left, int y, int right, int bottom) {
        // Log header
        g.fill(left + 2, y, right - 2, y + 18, HEADER_BG);
        drawBorder(g, left + 2, y, right - 2, y + 18, BORDER);
        g.drawString(font, "Execution Log (" + executionLog.size() + " entries)", left + 8, y + 5, TEXT, false);

        int refreshBtnX = right - 52;
        renderButton(g, mouseX, mouseY, refreshBtnX, y + 1, 48, 16, "Refresh", ACCENT);
        y += 20;

        // Column headers
        g.fill(left + 2, y, right - 2, y + 12, 0xFF0F1318);
        int col1 = left + 6;
        int col2 = left + 66;
        int col3 = left + 150;
        int col4 = right - 50;
        g.drawString(font, "Time", col1, y + 2, LABEL, false);
        g.drawString(font, "Schedule", col2, y + 2, LABEL, false);
        g.drawString(font, "Command", col3, y + 2, LABEL, false);
        g.drawString(font, "Result", col4, y + 2, LABEL, false);
        y += 14;

        int listTop = y;
        int listBottom = bottom - 18;
        int listH = listBottom - listTop;

        g.fill(left + 2, listTop, right - 2, listBottom, BG);
        drawBorder(g, left + 2, listTop, right - 2, listBottom, BORDER);

        if (executionLog.isEmpty()) {
            g.drawString(font, "No executions logged yet.", left + 8, listTop + 6, LABEL, false);
        } else {
            int visibleRows = Math.max(1, listH / LOG_ROW_H);
            int maxScroll = Math.max(0, executionLog.size() - visibleRows);
            logScroll = Math.min(logScroll, maxScroll);

            g.enableScissor(left + 3, listTop + 1, right - 3, listBottom - 1);
            int ry = listTop + 1;
            for (int i = logScroll; i < executionLog.size() && ry + LOG_ROW_H <= listBottom; i++) {
                LogEntry entry = executionLog.get(i);

                // Alternate row bg
                if (i % 2 == 0) {
                    g.fill(left + 3, ry, right - 3, ry + LOG_ROW_H, 0xFF0F1318);
                }

                g.drawString(font, entry.timestamp(), col1, ry + 1, LABEL, false);
                g.drawString(font, truncate(entry.name(), 80), col2, ry + 1, TEXT, false);
                g.drawString(font, truncate(entry.command(), col4 - col3 - 6), col3, ry + 1, LABEL, false);

                int resultColor = "OK".equals(entry.result()) ? SUCCESS : ERROR;
                g.drawString(font, truncate(entry.result(), right - col4 - 8), col4, ry + 1, resultColor, false);

                ry += LOG_ROW_H;
            }
            g.disableScissor();

            // Scrollbar
            if (executionLog.size() > visibleRows) {
                int sbH = listH - 4;
                int thumbH = Math.max(10, sbH * visibleRows / executionLog.size());
                int thumbY = listTop + 2 + (maxScroll > 0 ? (logScroll * (sbH - thumbH) / maxScroll) : 0);
                g.fill(right - 6, listTop + 2, right - 3, listBottom - 2, HEADER_BG);
                g.fill(right - 6, thumbY, right - 3, thumbY + thumbH, LABEL);
            }
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button, int left, int top, int right, int bottom) {
        if (button != 0) return false;
        int mx = (int) mouseX;
        int my = (int) mouseY;
        int w = right - left;

        // Unfocus all text boxes by default
        boolean prevNameFocused = nameBoxFocused;
        boolean prevCmdFocused = commandBoxFocused;
        boolean prevIntervalFocused = intervalBoxFocused;
        nameBoxFocused = false;
        commandBoxFocused = false;
        intervalBoxFocused = false;

        // View mode tabs
        int tabW = 60;
        int tabX = right - tabW * 2 - 8;
        if (isHovered(mx, my, tabX, top + 2, tabX + tabW, top + 18)) {
            viewMode = "schedules";
            return true;
        }
        tabX += tabW + 2;
        if (isHovered(mx, my, tabX, top + 2, tabX + tabW, top + 18)) {
            viewMode = "log";
            return true;
        }

        int y = top + 24;

        if ("log".equals(viewMode)) {
            return handleLogClicks(mx, my, left, y, right, bottom);
        }

        // --- CREATE section clicks ---
        int createTop = y;
        y = createTop + 20;

        // Name input box click
        int nameBoxX = left + 42;
        int nameBoxW = Math.min(140, w - 48);
        if (isHovered(mx, my, nameBoxX, y, nameBoxX + nameBoxW, y + 14)) {
            nameBoxFocused = true;
            return true;
        }
        y += 18;

        // Command input box click
        int cmdBoxX = left + 42;
        int cmdBoxW = Math.min(200, w - 48);
        if (isHovered(mx, my, cmdBoxX, y, cmdBoxX + cmdBoxW, y + 14)) {
            commandBoxFocused = true;
            return true;
        }
        y += 18;

        // Interval preset buttons
        int ix = left + 56;
        for (String preset : INTERVAL_PRESETS) {
            int pw = font.width(preset) + 8;
            if (isHovered(mx, my, ix, y, ix + pw, y + 14)) {
                inputInterval = preset;
                intervalBoxFocused = false;
                return true;
            }
            ix += pw + 2;
        }

        // Custom interval input
        ix += 16; // skip "or" text
        int intervalBoxW = 50;
        if (isHovered(mx, my, ix, y, ix + intervalBoxW, y + 14)) {
            intervalBoxFocused = true;
            return true;
        }
        y += 18;

        // Repeat toggle
        int toggleX = left + 6;
        if (isHovered(mx, my, toggleX, y, toggleX + 80, y + BTN_H)) {
            inputRepeat = !inputRepeat;
            return true;
        }

        // Create button
        int createBtnX = toggleX + 84;
        int createBtnW = 60;
        if (isHovered(mx, my, createBtnX, y, createBtnX + createBtnW, y + BTN_H)) {
            createSchedule();
            return true;
        }
        y += BTN_H + SECTION_GAP;

        // --- PRESETS section ---
        y += 20; // header
        int presetBtnW = (w - 16) / 3;
        for (int i = 0; i < PRESET_NAMES.length; i++) {
            int col = i % 3;
            int row = i / 3;
            int px = left + 4 + col * (presetBtnW + 2);
            int py = y + row * (BTN_H + 2);
            if (isHovered(mx, my, px, py, px + presetBtnW, py + BTN_H)) {
                sendAction("scheduler_add_preset", PRESET_KEYS[i]);
                setStatus("Adding preset: " + PRESET_NAMES[i], ACCENT);
                return true;
            }
        }
        int presetRows = (PRESET_NAMES.length + 2) / 3;
        y += presetRows * (BTN_H + 2) + SECTION_GAP;

        // --- ACTIVE SCHEDULES header ---
        // Refresh button
        int refreshBtnX = right - 52;
        if (isHovered(mx, my, refreshBtnX, y + 1, refreshBtnX + 48, y + 17)) {
            requestData();
            return true;
        }
        y += 20;

        // Schedule list row buttons
        int listTop = y;
        int listBottom = bottom - 18;
        if (!schedules.isEmpty()) {
            int visibleRows = Math.max(1, (listBottom - listTop) / SCHEDULE_ROW_H);
            for (int i = scheduleScroll; i < schedules.size() && i < scheduleScroll + visibleRows; i++) {
                ScheduleEntry entry = schedules.get(i);
                int ry = listTop + 2 + (i - scheduleScroll) * SCHEDULE_ROW_H;
                int rowRight = right - 4;

                int btnW = 30;
                int bx = rowRight - btnW * 4 - BTN_PAD * 3 - 8;
                int by = ry + 6;

                // Pause/Resume
                if (isHovered(mx, my, bx, by, bx + btnW, by + 14)) {
                    if (entry.active()) {
                        sendAction("scheduler_pause", entry.id());
                        setStatus("Pausing: " + entry.name(), WARNING);
                    } else {
                        sendAction("scheduler_resume", entry.id());
                        setStatus("Resuming: " + entry.name(), SUCCESS);
                    }
                    return true;
                }
                bx += btnW + BTN_PAD;

                // Run Now
                if (isHovered(mx, my, bx, by, bx + btnW + 4, by + 14)) {
                    sendAction("scheduler_run_now", entry.id());
                    setStatus("Running now: " + entry.name(), ACCENT);
                    return true;
                }
                bx += btnW + 4 + BTN_PAD;

                // Duplicate
                if (isHovered(mx, my, bx, by, bx + btnW - 4, by + 14)) {
                    sendAction("scheduler_duplicate", entry.id());
                    setStatus("Duplicating: " + entry.name(), ACCENT);
                    return true;
                }
                bx += btnW - 4 + BTN_PAD;

                // Delete
                if (isHovered(mx, my, bx, by, bx + btnW - 4, by + 14)) {
                    sendAction("scheduler_delete", entry.id());
                    setStatus("Deleting: " + entry.name(), ERROR);
                    return true;
                }
            }
        }

        // Restore focus if nothing else was clicked
        if (!nameBoxFocused && !commandBoxFocused && !intervalBoxFocused) {
            // No input was clicked - leave all unfocused
        }

        return false;
    }

    private boolean handleLogClicks(int mx, int my, int left, int y, int right, int bottom) {
        // Refresh button in log header
        int refreshBtnX = right - 52;
        if (isHovered(mx, my, refreshBtnX, y + 1, refreshBtnX + 48, y + 17)) {
            requestData();
            return true;
        }
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if ("log".equals(viewMode)) {
            int maxScroll = Math.max(0, executionLog.size() - 10);
            logScroll = Math.max(0, Math.min(maxScroll, logScroll - (int) scrollY * 3));
            return true;
        }
        // Scroll the schedules list
        if (!schedules.isEmpty()) {
            int maxScroll = Math.max(0, schedules.size() - 5);
            scheduleScroll = Math.max(0, Math.min(maxScroll, scheduleScroll - (int) scrollY));
            return true;
        }
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!nameBoxFocused && !commandBoxFocused && !intervalBoxFocused) {
            return false;
        }

        // Tab to cycle focus
        if (keyCode == 258) { // Tab
            if (nameBoxFocused) {
                nameBoxFocused = false;
                commandBoxFocused = true;
            } else if (commandBoxFocused) {
                commandBoxFocused = false;
                intervalBoxFocused = true;
            } else {
                intervalBoxFocused = false;
                nameBoxFocused = true;
            }
            return true;
        }

        // Enter to create
        if (keyCode == 257) { // Enter
            createSchedule();
            return true;
        }

        // Escape to unfocus
        if (keyCode == 256) { // Escape
            nameBoxFocused = false;
            commandBoxFocused = false;
            intervalBoxFocused = false;
            return true;
        }

        // Backspace
        if (keyCode == 259) { // Backspace
            if (nameBoxFocused && !inputName.isEmpty()) {
                inputName = inputName.substring(0, inputName.length() - 1);
            } else if (commandBoxFocused && !inputCommand.isEmpty()) {
                inputCommand = inputCommand.substring(0, inputCommand.length() - 1);
            } else if (intervalBoxFocused && !inputInterval.isEmpty()) {
                inputInterval = inputInterval.substring(0, inputInterval.length() - 1);
            }
            return true;
        }

        // Ctrl+A select all (clear)
        if (keyCode == 65 && (modifiers & 2) != 0) {
            if (nameBoxFocused) inputName = "";
            else if (commandBoxFocused) inputCommand = "";
            else if (intervalBoxFocused) inputInterval = "";
            return true;
        }

        return false;
    }

    public boolean charTyped(char c, int modifiers) {
        if (!nameBoxFocused && !commandBoxFocused && !intervalBoxFocused) {
            return false;
        }
        // Filter control characters
        if (c < 32) return false;

        if (nameBoxFocused && inputName.length() < 32) {
            inputName += c;
            return true;
        }
        if (commandBoxFocused && inputCommand.length() < 200) {
            inputCommand += c;
            return true;
        }
        if (intervalBoxFocused && inputInterval.length() < 10) {
            // Only allow valid interval characters: digits, m, h, s
            if (Character.isDigit(c) || c == 'm' || c == 'h' || c == 's' || c == 'd') {
                inputInterval += c;
            }
            return true;
        }
        return false;
    }

    public void handleResponse(String type, String jsonData) {
        if ("scheduler_data".equals(type)) {
            parseSchedulerData(jsonData);
        } else if ("scheduler_result".equals(type)) {
            try {
                JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
                boolean success = obj.has("success") && obj.get("success").getAsBoolean();
                String msg = obj.has("message") ? obj.get("message").getAsString() : (success ? "Done!" : "Failed.");
                setStatus(msg, success ? SUCCESS : ERROR);
                if (success) {
                    // Auto-refresh after successful action
                    requestData();
                }
            } catch (Exception e) {
                setStatus("Action completed.", ACCENT);
                requestData();
            }
        }
    }

    public void requestData() {
        sendAction("scheduler_request", "");
    }

    // ---- Private helpers ----

    private void createSchedule() {
        if (inputName.isEmpty()) {
            setStatus("Name is required.", ERROR);
            return;
        }
        if (inputCommand.isEmpty()) {
            setStatus("Command is required.", ERROR);
            return;
        }
        if (inputInterval.isEmpty()) {
            setStatus("Interval is required.", ERROR);
            return;
        }

        long intervalMs = parseIntervalToMs(inputInterval);
        if (intervalMs <= 0) {
            setStatus("Invalid interval format. Use e.g. 5m, 1h, 30s", ERROR);
            return;
        }

        String data = inputName + "|" + inputCommand + "|" + intervalMs + "|" + inputRepeat;
        sendAction("scheduler_create", data);
        setStatus("Creating schedule: " + inputName, ACCENT);

        // Clear inputs after creation
        inputName = "";
        inputCommand = "";
    }

    private void parseSchedulerData(String jsonData) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            schedules.clear();
            executionLog.clear();

            if (obj.has("schedules")) {
                JsonArray arr = obj.getAsJsonArray("schedules");
                for (JsonElement el : arr) {
                    JsonObject s = el.getAsJsonObject();
                    String id = s.get("id").getAsString();
                    String name = s.get("name").getAsString();
                    String command = s.get("command").getAsString();
                    long intervalMs = s.get("intervalMs").getAsLong();
                    long nextRunMs = s.has("nextRunMs") ? s.get("nextRunMs").getAsLong() : 0;
                    int runCount = s.has("runCount") ? s.get("runCount").getAsInt() : 0;
                    boolean active = s.has("active") && s.get("active").getAsBoolean();

                    String intervalStr = formatInterval(intervalMs);
                    String nextRunStr = formatCountdown(nextRunMs);

                    schedules.add(new ScheduleEntry(id, name, command, intervalStr, nextRunStr, runCount, active));
                }
            }

            if (obj.has("log")) {
                JsonArray arr = obj.getAsJsonArray("log");
                for (JsonElement el : arr) {
                    JsonObject l = el.getAsJsonObject();
                    String timestamp = l.has("timestamp") ? l.get("timestamp").getAsString() : "??:??:??";
                    String name = l.has("name") ? l.get("name").getAsString() : "Unknown";
                    String command = l.has("command") ? l.get("command").getAsString() : "";
                    String result = l.has("result") ? l.get("result").getAsString() : "?";
                    executionLog.add(new LogEntry(timestamp, name, command, result));
                }
            }

            setStatus("Scheduler data loaded. " + schedules.size() + " schedule(s).", SUCCESS);
        } catch (Exception e) {
            setStatus("Failed to parse scheduler data.", ERROR);
        }
    }

    private static long parseIntervalToMs(String interval) {
        if (interval == null || interval.isEmpty()) return -1;
        interval = interval.trim().toLowerCase();
        try {
            // Pure number = treat as minutes
            if (interval.matches("\\d+")) {
                return Long.parseLong(interval) * 60000L;
            }
            // Parse format like 5m, 1h, 30s, 2d, 10m, etc.
            long total = 0;
            StringBuilder num = new StringBuilder();
            for (int i = 0; i < interval.length(); i++) {
                char c = interval.charAt(i);
                if (Character.isDigit(c)) {
                    num.append(c);
                } else {
                    if (num.length() == 0) return -1;
                    long val = Long.parseLong(num.toString());
                    switch (c) {
                        case 's': total += val * 1000L; break;
                        case 'm': total += val * 60000L; break;
                        case 'h': total += val * 3600000L; break;
                        case 'd': total += val * 86400000L; break;
                        default: return -1;
                    }
                    num = new StringBuilder();
                }
            }
            // Trailing digits with no unit = minutes
            if (num.length() > 0) {
                total += Long.parseLong(num.toString()) * 60000L;
            }
            return total > 0 ? total : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static String formatInterval(long ms) {
        if (ms <= 0) return "0s";
        long seconds = ms / 1000;
        if (seconds < 60) return seconds + "s";
        long minutes = seconds / 60;
        if (minutes < 60) {
            long remainSec = seconds % 60;
            return remainSec > 0 ? minutes + "m" + remainSec + "s" : minutes + "m";
        }
        long hours = minutes / 60;
        if (hours < 24) {
            long remainMin = minutes % 60;
            return remainMin > 0 ? hours + "h" + remainMin + "m" : hours + "h";
        }
        long days = hours / 24;
        long remainHours = hours % 24;
        return remainHours > 0 ? days + "d" + remainHours + "h" : days + "d";
    }

    private static String formatCountdown(long ms) {
        if (ms <= 0) return "now";
        long seconds = ms / 1000;
        if (seconds < 60) return seconds + "s";
        long minutes = seconds / 60;
        long remainSec = seconds % 60;
        if (minutes < 60) return minutes + "m " + remainSec + "s";
        long hours = minutes / 60;
        long remainMin = minutes % 60;
        return hours + "h " + remainMin + "m";
    }

    private void sendAction(String action, String data) {
        ClientPacketDistributor.sendToServer(
                (CustomPacketPayload) new ComputerActionPayload(action, data),
                (CustomPacketPayload[]) new CustomPacketPayload[0]);
    }

    private void setStatus(String message, int color) {
        this.statusMessage = message;
        this.statusColor = color;
        this.statusExpiry = System.currentTimeMillis() + 5000;
    }

    private void renderInputBox(GuiGraphics g, int mouseX, int mouseY, int x, int y, int w, int h, String text, boolean focused) {
        int bg = focused ? 0xFF1C2128 : HEADER_BG;
        int border = focused ? ACCENT : BORDER;
        if (!focused && isHovered(mouseX, mouseY, x, y, x + w, y + h)) {
            bg = 0xFF1C2128;
        }
        g.fill(x, y, x + w, y + h, bg);
        drawBorder(g, x, y, x + w, y + h, border);

        // Blink cursor
        long now = System.currentTimeMillis();
        if (now - lastBlinkTime > 500) {
            cursorVisible = !cursorVisible;
            lastBlinkTime = now;
        }

        String display = truncate(text, w - 8);
        g.drawString(font, display, x + 3, y + 3, TEXT, false);

        if (focused && cursorVisible) {
            int cursorX = x + 3 + font.width(display);
            g.fill(cursorX, y + 2, cursorX + 1, y + h - 2, TEXT);
        }
    }

    private void renderTabButton(GuiGraphics g, int mouseX, int mouseY, int x, int y, int w, int h, String label, boolean selected) {
        boolean hovered = isHovered(mouseX, mouseY, x, y, x + w, y + h);
        int bg = selected ? darken(ACCENT, 0.4f) : (hovered ? 0xFF21262D : HEADER_BG);
        g.fill(x, y, x + w, y + h, bg);
        drawBorder(g, x, y, x + w, y + h, selected ? ACCENT : BORDER);
        int textW = font.width(label);
        g.drawString(font, label, x + (w - textW) / 2, y + (h - 8) / 2, selected ? TEXT : LABEL, false);
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

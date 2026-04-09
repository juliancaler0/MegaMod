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

public class ModerationPanel {
    private final Font font;

    // Sub-tab: 0=Bans, 1=Mutes, 2=Warnings, 3=Action Log
    private int subTab = 0;
    private int scroll = 0;

    // Text input state
    private String inputPlayerName = "";
    private String inputReason = "";
    private int selectedDuration = 0;
    private boolean nameBoxFocused = false;
    private boolean reasonBoxFocused = false;
    private int nameBoxCursor = 0;
    private int reasonBoxCursor = 0;
    private long cursorBlinkTime = 0;

    // Data lists
    private List<BanEntry> bans = new ArrayList<>();
    private List<MuteEntry> mutes = new ArrayList<>();
    private List<WarnEntry> warnings = new ArrayList<>();
    private List<LogEntry> actionLog = new ArrayList<>();
    private String logFilter = "All";

    // Status message
    private String statusMessage = "";
    private int statusColor = TEXT;
    private long statusExpiry = 0;

    // Auto-refresh
    private int refreshCooldown = 0;

    // Duration options per sub-tab
    private static final String[] BAN_DURATIONS = {"Permanent", "1 Hour", "1 Day", "7 Days", "30 Days"};
    private static final int[] BAN_DURATION_MINUTES = {0, 60, 1440, 10080, 43200};
    private static final String[] MUTE_DURATIONS = {"Permanent", "5 Min", "15 Min", "1 Hour"};
    private static final int[] MUTE_DURATION_MINUTES = {0, 5, 15, 60};
    private static final String[] LOG_FILTERS = {"All", "Bans", "Kicks", "Mutes", "Warns"};
    private static final String[] SUB_TAB_NAMES = {"Bans", "Mutes", "Warnings", "Action Log"};

    // Visual style
    private static final int BG = 0xFF0D1117;
    private static final int HEADER_BG = 0xFF161B22;
    private static final int BORDER = 0xFF30363D;
    private static final int TEXT = 0xFFE6EDF3;
    private static final int LABEL = 0xFF8B949E;
    private static final int ACCENT = 0xFF58A6FF;
    private static final int BAN_RED = 0xFFF85149;
    private static final int MUTE_ORANGE = 0xFFD29922;
    private static final int WARN_YELLOW = 0xFFE3B341;
    private static final int KICK_PURPLE = 0xFFA371F7;
    private static final int SUCCESS = 0xFF3FB950;
    private static final int INPUT_BG = 0xFF0D1117;
    private static final int INPUT_BORDER = 0xFF30363D;
    private static final int INPUT_BORDER_FOCUS = 0xFF58A6FF;
    private static final int HOVER_BG = 0xFF21262D;
    private static final int BTN_BG = 0xFF21262D;

    // Layout
    private static final int ROW_H = 14;
    private static final int BTN_H = 16;
    private static final int INPUT_H = 16;
    private static final int SUB_TAB_H = 20;
    private static final int SECTION_GAP = 6;

    // Records
    public record BanEntry(String name, String uuid, String reason, String date, String duration, String bannedBy, boolean permanent) {}
    public record MuteEntry(String name, String uuid, String reason, long expiresAt, String duration) {}
    public record WarnEntry(String name, String uuid, int count, String lastReason, String lastDate) {}
    public record LogEntry(String timestamp, String action, String admin, String target, String details, String type) {}

    public ModerationPanel(Font font) {
        this.font = font;
    }

    // ===================== RENDER =====================

    public void render(GuiGraphics g, int mouseX, int mouseY, int left, int top, int right, int bottom) {
        int w = right - left;
        int h = bottom - top;
        cursorBlinkTime = System.currentTimeMillis();

        // Background
        g.fill(left, top, right, bottom, BG);

        // Title bar
        g.fill(left, top, right, top + 20, HEADER_BG);
        drawBorder(g, left, top, right, top + 20, BORDER);
        g.drawString(font, "Moderation Panel", left + 6, top + 6, ACCENT, false);

        // Status message (right side of title)
        if (!statusMessage.isEmpty() && System.currentTimeMillis() < statusExpiry) {
            int sw = font.width(statusMessage);
            g.drawString(font, statusMessage, right - sw - 8, top + 6, statusColor, false);
        }

        int y = top + 22;

        // Sub-tab buttons
        g.fill(left, y, right, y + SUB_TAB_H, HEADER_BG);
        drawBorder(g, left, y, right, y + SUB_TAB_H, BORDER);
        int tabX = left + 4;
        for (int i = 0; i < SUB_TAB_NAMES.length; i++) {
            int tw = font.width(SUB_TAB_NAMES[i]) + 12;
            boolean hovered = isHovered(mouseX, mouseY, tabX, y + 2, tabX + tw, y + SUB_TAB_H - 2);
            boolean selected = i == subTab;
            if (selected) {
                g.fill(tabX, y + 2, tabX + tw, y + SUB_TAB_H - 2, ACCENT);
                g.drawString(font, SUB_TAB_NAMES[i], tabX + 6, y + 6, 0xFF000000, false);
            } else {
                g.fill(tabX, y + 2, tabX + tw, y + SUB_TAB_H - 2, hovered ? HOVER_BG : BTN_BG);
                g.drawString(font, SUB_TAB_NAMES[i], tabX + 6, y + 6, hovered ? TEXT : LABEL, false);
            }
            tabX += tw + 3;
        }
        // Count badges
        tabX = left + 4;
        for (int i = 0; i < SUB_TAB_NAMES.length; i++) {
            int tw = font.width(SUB_TAB_NAMES[i]) + 12;
            int count = switch (i) {
                case 0 -> bans.size();
                case 1 -> mutes.size();
                case 2 -> warnings.size();
                case 3 -> actionLog.size();
                default -> 0;
            };
            if (count > 0 && i != subTab) {
                String badge = String.valueOf(count);
                int bw = font.width(badge) + 4;
                int bx = tabX + tw - bw + 1;
                int by = y + 1;
                g.fill(bx, by, bx + bw, by + 9, getSubTabColor(i));
                g.drawString(font, badge, bx + 2, by + 1, 0xFF000000, false);
            }
            tabX += tw + 3;
        }

        y += SUB_TAB_H + 2;

        // Content area with scroll clip
        int contentTop = y;
        int contentBottom = bottom - 4;
        int contentH = contentBottom - contentTop;

        g.enableScissor(left, contentTop, right, contentBottom);

        switch (subTab) {
            case 0 -> renderBans(g, mouseX, mouseY, left, contentTop, right, contentBottom);
            case 1 -> renderMutes(g, mouseX, mouseY, left, contentTop, right, contentBottom);
            case 2 -> renderWarnings(g, mouseX, mouseY, left, contentTop, right, contentBottom);
            case 3 -> renderActionLog(g, mouseX, mouseY, left, contentTop, right, contentBottom);
        }

        g.disableScissor();

        // Scroll indicator
        int totalContentH = getContentHeight(left, right);
        if (totalContentH > contentH) {
            int scrollBarH = Math.max(20, contentH * contentH / totalContentH);
            int maxScroll = totalContentH - contentH;
            int scrollBarY = contentTop + (int) ((float) scroll / maxScroll * (contentH - scrollBarH));
            g.fill(right - 4, contentTop, right - 1, contentBottom, 0xFF161B22);
            g.fill(right - 3, scrollBarY, right - 1, scrollBarY + scrollBarH, BORDER);
        }
    }

    private void renderBans(GuiGraphics g, int mx, int my, int left, int top, int right, int bottom) {
        int w = right - left;
        int y = top - scroll;

        // Form section
        g.fill(left + 4, y, right - 4, y + 76, HEADER_BG);
        drawBorder(g, left + 4, y, right - 4, y + 76, BORDER);
        g.drawString(font, "Ban Player", left + 10, y + 4, BAN_RED, false);

        int formY = y + 16;
        // Player name input
        g.drawString(font, "Name:", left + 10, formY + 3, LABEL, false);
        renderInputBox(g, mx, my, left + 50, formY, Math.min(140, w / 2 - 30), INPUT_H, inputPlayerName, nameBoxFocused, nameBoxCursor);

        // Reason input
        g.drawString(font, "Reason:", left + 10, formY + 20, LABEL, false);
        renderInputBox(g, mx, my, left + 50, formY + 18, Math.min(200, w - 90), INPUT_H, inputReason, reasonBoxFocused, reasonBoxCursor);

        // Duration selector
        int durY = formY + 38;
        g.drawString(font, "Duration:", left + 10, durY + 3, LABEL, false);
        int durX = left + 60;
        for (int i = 0; i < BAN_DURATIONS.length; i++) {
            int dw = font.width(BAN_DURATIONS[i]) + 8;
            boolean sel = i == selectedDuration;
            boolean hov = isHovered(mx, my, durX, durY, durX + dw, durY + 14);
            g.fill(durX, durY, durX + dw, durY + 14, sel ? ACCENT : (hov ? HOVER_BG : BTN_BG));
            g.drawString(font, BAN_DURATIONS[i], durX + 4, durY + 3, sel ? 0xFF000000 : (hov ? TEXT : LABEL), false);
            durX += dw + 2;
        }

        // Ban button
        int banBtnX = right - 58;
        int banBtnW = 50;
        boolean banHov = isHovered(mx, my, banBtnX, durY, banBtnX + banBtnW, durY + BTN_H);
        g.fill(banBtnX, durY, banBtnX + banBtnW, durY + BTN_H, banHov ? BAN_RED : 0xFF8B2020);
        centerText(g, "BAN", banBtnX, durY, banBtnW, BTN_H, TEXT);

        y += 80;

        // Ban list header
        g.fill(left + 4, y, right - 4, y + 14, 0xFF1A1F27);
        g.drawString(font, "Player", left + 10, y + 3, LABEL, false);
        g.drawString(font, "Reason", left + 90, y + 3, LABEL, false);
        g.drawString(font, "Duration", right - 130, y + 3, LABEL, false);
        g.drawString(font, "By", right - 70, y + 3, LABEL, false);
        y += 16;

        if (bans.isEmpty()) {
            g.drawString(font, "No banned players.", left + 10, y + 2, LABEL, false);
            y += ROW_H;
        } else {
            for (int i = 0; i < bans.size(); i++) {
                BanEntry ban = bans.get(i);
                boolean rowHov = isHovered(mx, my, left + 4, y, right - 4, y + ROW_H + 4);
                if (rowHov) {
                    g.fill(left + 4, y, right - 4, y + ROW_H + 4, 0xFF1A1F27);
                }
                g.drawString(font, truncate(ban.name, 70), left + 10, y + 2, BAN_RED, false);
                g.drawString(font, truncate(ban.reason, 100), left + 90, y + 2, TEXT, false);
                String durText = ban.permanent ? "Perm" : ban.duration;
                g.drawString(font, truncate(durText, 50), right - 130, y + 2, ban.permanent ? BAN_RED : MUTE_ORANGE, false);
                g.drawString(font, truncate(ban.bannedBy, 50), right - 70, y + 2, LABEL, false);

                // Unban button
                int ubX = right - 46;
                int ubW = 38;
                boolean ubHov = isHovered(mx, my, ubX, y, ubX + ubW, y + 13);
                g.fill(ubX, y, ubX + ubW, y + 13, ubHov ? SUCCESS : 0xFF1A4F2A);
                centerText(g, "Unban", ubX, y, ubW, 13, TEXT);

                y += ROW_H + 6;
            }
        }
    }

    private void renderMutes(GuiGraphics g, int mx, int my, int left, int top, int right, int bottom) {
        int w = right - left;
        int y = top - scroll;

        // Form section
        g.fill(left + 4, y, right - 4, y + 56, HEADER_BG);
        drawBorder(g, left + 4, y, right - 4, y + 56, BORDER);
        g.drawString(font, "Mute Player", left + 10, y + 4, MUTE_ORANGE, false);

        int formY = y + 16;
        // Player name input
        g.drawString(font, "Name:", left + 10, formY + 3, LABEL, false);
        renderInputBox(g, mx, my, left + 50, formY, Math.min(140, w / 2 - 30), INPUT_H, inputPlayerName, nameBoxFocused, nameBoxCursor);

        // Duration selector
        int durY = formY + 20;
        g.drawString(font, "Duration:", left + 10, durY + 3, LABEL, false);
        int durX = left + 60;
        for (int i = 0; i < MUTE_DURATIONS.length; i++) {
            int dw = font.width(MUTE_DURATIONS[i]) + 8;
            boolean sel = i == selectedDuration;
            boolean hov = isHovered(mx, my, durX, durY, durX + dw, durY + 14);
            g.fill(durX, durY, durX + dw, durY + 14, sel ? ACCENT : (hov ? HOVER_BG : BTN_BG));
            g.drawString(font, MUTE_DURATIONS[i], durX + 4, durY + 3, sel ? 0xFF000000 : (hov ? TEXT : LABEL), false);
            durX += dw + 2;
        }

        // Mute button
        int muteBtnX = right - 58;
        int muteBtnW = 50;
        boolean muteHov = isHovered(mx, my, muteBtnX, durY, muteBtnX + muteBtnW, durY + BTN_H);
        g.fill(muteBtnX, durY, muteBtnX + muteBtnW, durY + BTN_H, muteHov ? MUTE_ORANGE : 0xFF7A5810);
        centerText(g, "MUTE", muteBtnX, durY, muteBtnW, BTN_H, TEXT);

        y += 60;

        // Mute list header
        g.fill(left + 4, y, right - 4, y + 14, 0xFF1A1F27);
        g.drawString(font, "Player", left + 10, y + 3, LABEL, false);
        g.drawString(font, "Reason", left + 90, y + 3, LABEL, false);
        g.drawString(font, "Duration", right - 110, y + 3, LABEL, false);
        y += 16;

        if (mutes.isEmpty()) {
            g.drawString(font, "No muted players.", left + 10, y + 2, LABEL, false);
            y += ROW_H;
        } else {
            for (int i = 0; i < mutes.size(); i++) {
                MuteEntry mute = mutes.get(i);
                boolean rowHov = isHovered(mx, my, left + 4, y, right - 4, y + ROW_H + 4);
                if (rowHov) {
                    g.fill(left + 4, y, right - 4, y + ROW_H + 4, 0xFF1A1F27);
                }
                g.drawString(font, truncate(mute.name, 70), left + 10, y + 2, MUTE_ORANGE, false);
                g.drawString(font, truncate(mute.reason, 100), left + 90, y + 2, TEXT, false);
                g.drawString(font, truncate(mute.duration, 70), right - 110, y + 2, LABEL, false);

                // Unmute button
                int ubX = right - 50;
                int ubW = 42;
                boolean ubHov = isHovered(mx, my, ubX, y, ubX + ubW, y + 13);
                g.fill(ubX, y, ubX + ubW, y + 13, ubHov ? SUCCESS : 0xFF1A4F2A);
                centerText(g, "Unmute", ubX, y, ubW, 13, TEXT);

                y += ROW_H + 6;
            }
        }
    }

    private void renderWarnings(GuiGraphics g, int mx, int my, int left, int top, int right, int bottom) {
        int w = right - left;
        int y = top - scroll;

        // Form section
        g.fill(left + 4, y, right - 4, y + 56, HEADER_BG);
        drawBorder(g, left + 4, y, right - 4, y + 56, BORDER);
        g.drawString(font, "Warn Player", left + 10, y + 4, WARN_YELLOW, false);

        int formY = y + 16;
        // Player name input
        g.drawString(font, "Name:", left + 10, formY + 3, LABEL, false);
        renderInputBox(g, mx, my, left + 50, formY, Math.min(140, w / 2 - 30), INPUT_H, inputPlayerName, nameBoxFocused, nameBoxCursor);

        // Reason input
        g.drawString(font, "Reason:", left + 10, formY + 20, LABEL, false);
        renderInputBox(g, mx, my, left + 50, formY + 18, Math.min(200, w - 90), INPUT_H, inputReason, reasonBoxFocused, reasonBoxCursor);

        // Warn button
        int warnBtnX = right - 58;
        int warnBtnW = 50;
        int warnBtnY = formY + 18;
        boolean warnHov = isHovered(mx, my, warnBtnX, warnBtnY, warnBtnX + warnBtnW, warnBtnY + BTN_H);
        g.fill(warnBtnX, warnBtnY, warnBtnX + warnBtnW, warnBtnY + BTN_H, warnHov ? WARN_YELLOW : 0xFF7A6810);
        centerText(g, "WARN", warnBtnX, warnBtnY, warnBtnW, BTN_H, 0xFF000000);

        y += 60;

        // Warning list header
        g.fill(left + 4, y, right - 4, y + 14, 0xFF1A1F27);
        g.drawString(font, "Player", left + 10, y + 3, LABEL, false);
        g.drawString(font, "#", left + 90, y + 3, LABEL, false);
        g.drawString(font, "Last Reason", left + 110, y + 3, LABEL, false);
        g.drawString(font, "Date", right - 100, y + 3, LABEL, false);
        y += 16;

        if (warnings.isEmpty()) {
            g.drawString(font, "No warnings recorded.", left + 10, y + 2, LABEL, false);
            y += ROW_H;
        } else {
            for (int i = 0; i < warnings.size(); i++) {
                WarnEntry warn = warnings.get(i);
                boolean rowHov = isHovered(mx, my, left + 4, y, right - 4, y + ROW_H + 4);
                if (rowHov) {
                    g.fill(left + 4, y, right - 4, y + ROW_H + 4, 0xFF1A1F27);
                }
                g.drawString(font, truncate(warn.name, 70), left + 10, y + 2, WARN_YELLOW, false);
                g.drawString(font, String.valueOf(warn.count), left + 90, y + 2, warn.count >= 3 ? BAN_RED : TEXT, false);
                g.drawString(font, truncate(warn.lastReason, 120), left + 110, y + 2, TEXT, false);
                g.drawString(font, warn.lastDate, right - 100, y + 2, LABEL, false);

                // Clear button
                int clX = right - 46;
                int clW = 38;
                boolean clHov = isHovered(mx, my, clX, y, clX + clW, y + 13);
                g.fill(clX, y, clX + clW, y + 13, clHov ? BAN_RED : 0xFF5A2020);
                centerText(g, "Clear", clX, y, clW, 13, TEXT);

                y += ROW_H + 6;
            }
        }
    }

    private void renderActionLog(GuiGraphics g, int mx, int my, int left, int top, int right, int bottom) {
        int y = top - scroll;

        // Filter buttons
        g.fill(left + 4, y, right - 4, y + 20, HEADER_BG);
        drawBorder(g, left + 4, y, right - 4, y + 20, BORDER);
        g.drawString(font, "Filter:", left + 10, y + 6, LABEL, false);
        int filterX = left + 48;
        for (String filter : LOG_FILTERS) {
            int fw = font.width(filter) + 8;
            boolean sel = filter.equals(logFilter);
            boolean hov = isHovered(mx, my, filterX, y + 2, filterX + fw, y + 18);
            int filterColor = getFilterColor(filter);
            if (sel) {
                g.fill(filterX, y + 2, filterX + fw, y + 18, filterColor);
                g.drawString(font, filter, filterX + 4, y + 6, 0xFF000000, false);
            } else {
                g.fill(filterX, y + 2, filterX + fw, y + 18, hov ? HOVER_BG : BTN_BG);
                g.drawString(font, filter, filterX + 4, y + 6, hov ? TEXT : LABEL, false);
            }
            filterX += fw + 2;
        }

        y += 24;

        // Log header
        g.fill(left + 4, y, right - 4, y + 14, 0xFF1A1F27);
        g.drawString(font, "Time", left + 10, y + 3, LABEL, false);
        g.drawString(font, "Action", left + 120, y + 3, LABEL, false);
        g.drawString(font, "Admin", left + 190, y + 3, LABEL, false);
        g.drawString(font, "Target", left + 270, y + 3, LABEL, false);
        g.drawString(font, "Details", right - 180, y + 3, LABEL, false);
        y += 16;

        if (actionLog.isEmpty()) {
            g.drawString(font, "No moderation actions recorded.", left + 10, y + 2, LABEL, false);
            y += ROW_H;
        } else {
            List<LogEntry> filtered = getFilteredLog();
            for (int i = 0; i < filtered.size(); i++) {
                LogEntry entry = filtered.get(i);
                boolean rowHov = isHovered(mx, my, left + 4, y, right - 4, y + ROW_H);
                if (rowHov) {
                    g.fill(left + 4, y, right - 4, y + ROW_H, 0xFF1A1F27);
                }

                int actionColor = getActionColor(entry.type);
                g.drawString(font, truncate(entry.timestamp, 110), left + 10, y + 2, LABEL, false);
                g.drawString(font, truncate(entry.action, 60), left + 120, y + 2, actionColor, false);
                g.drawString(font, truncate(entry.admin, 70), left + 190, y + 2, ACCENT, false);
                g.drawString(font, truncate(entry.target, 70), left + 270, y + 2, TEXT, false);
                g.drawString(font, truncate(entry.details, 160), right - 180, y + 2, LABEL, false);

                y += ROW_H + 2;
            }
        }
    }

    // ===================== INPUT BOX RENDERING =====================

    private void renderInputBox(GuiGraphics g, int mx, int my, int x, int y, int w, int h, String text, boolean focused, int cursor) {
        boolean hovered = isHovered(mx, my, x, y, x + w, y + h);
        int borderCol = focused ? INPUT_BORDER_FOCUS : (hovered ? ACCENT : INPUT_BORDER);
        g.fill(x, y, x + w, y + h, INPUT_BG);
        drawBorder(g, x, y, x + w, y + h, borderCol);

        // Text clipping
        String displayText = text;
        int textW = font.width(displayText);
        int maxTextW = w - 6;
        int textOffset = 0;
        if (focused && textW > maxTextW) {
            // Scroll text so cursor is visible
            String beforeCursor = text.substring(0, Math.min(cursor, text.length()));
            int cursorPixelX = font.width(beforeCursor);
            if (cursorPixelX > maxTextW) {
                textOffset = cursorPixelX - maxTextW + 10;
            }
        }

        g.enableScissor(x + 3, y, x + w - 3, y + h);
        g.drawString(font, displayText, x + 3 - textOffset, y + 4, TEXT, false);

        // Cursor blink
        if (focused && (cursorBlinkTime / 500) % 2 == 0) {
            String before = text.substring(0, Math.min(cursor, text.length()));
            int cx = x + 3 + font.width(before) - textOffset;
            g.fill(cx, y + 3, cx + 1, y + h - 3, TEXT);
        }
        g.disableScissor();

        // Placeholder
        if (text.isEmpty() && !focused) {
            String placeholder = (nameBoxFocused || (!nameBoxFocused && !reasonBoxFocused && text == inputPlayerName)) ? "" : "";
            // We can derive which input this is by text reference check
        }
    }

    // ===================== MOUSE CLICK =====================

    public boolean mouseClicked(double mouseX, double mouseY, int button, int left, int top, int right, int bottom) {
        if (button != 0) return false;
        int mx = (int) mouseX;
        int my = (int) mouseY;
        int w = right - left;

        // Sub-tab clicks
        int tabY = top + 22;
        int tabX = left + 4;
        for (int i = 0; i < SUB_TAB_NAMES.length; i++) {
            int tw = font.width(SUB_TAB_NAMES[i]) + 12;
            if (isHovered(mx, my, tabX, tabY + 2, tabX + tw, tabY + SUB_TAB_H - 2)) {
                subTab = i;
                scroll = 0;
                selectedDuration = 0;
                inputPlayerName = "";
                inputReason = "";
                nameBoxCursor = 0;
                reasonBoxCursor = 0;
                nameBoxFocused = false;
                reasonBoxFocused = false;
                if (i == 3) {
                    // Request log data
                    ClientPacketDistributor.sendToServer((CustomPacketPayload) new ComputerActionPayload("mod_request_log", ""), (CustomPacketPayload[]) new CustomPacketPayload[0]);
                }
                return true;
            }
            tabX += tw + 3;
        }

        // Content area
        int contentTop = top + 22 + SUB_TAB_H + 2;
        int contentBottom = bottom - 4;

        switch (subTab) {
            case 0: return handleBansClick(mx, my, left, contentTop, right, contentBottom);
            case 1: return handleMutesClick(mx, my, left, contentTop, right, contentBottom);
            case 2: return handleWarningsClick(mx, my, left, contentTop, right, contentBottom);
            case 3: return handleLogClick(mx, my, left, contentTop, right, contentBottom);
        }

        return false;
    }

    private boolean handleBansClick(int mx, int my, int left, int top, int right, int bottom) {
        int w = right - left;
        int y = top - scroll;

        // Name input box
        int nameBoxX = left + 50;
        int nameBoxY = y + 16;
        int nameBoxW = Math.min(140, w / 2 - 30);
        if (isHovered(mx, my, nameBoxX, nameBoxY, nameBoxX + nameBoxW, nameBoxY + INPUT_H)) {
            nameBoxFocused = true;
            reasonBoxFocused = false;
            nameBoxCursor = getClickCursorPos(inputPlayerName, mx - nameBoxX - 3);
            return true;
        }

        // Reason input box
        int reasonBoxX = left + 50;
        int reasonBoxY = y + 34;
        int reasonBoxW = Math.min(200, w - 90);
        if (isHovered(mx, my, reasonBoxX, reasonBoxY, reasonBoxX + reasonBoxW, reasonBoxY + INPUT_H)) {
            reasonBoxFocused = true;
            nameBoxFocused = false;
            reasonBoxCursor = getClickCursorPos(inputReason, mx - reasonBoxX - 3);
            return true;
        }

        // Duration buttons
        int durY = y + 54;
        int durX = left + 60;
        for (int i = 0; i < BAN_DURATIONS.length; i++) {
            int dw = font.width(BAN_DURATIONS[i]) + 8;
            if (isHovered(mx, my, durX, durY, durX + dw, durY + 14)) {
                selectedDuration = i;
                return true;
            }
            durX += dw + 2;
        }

        // Ban button
        int banBtnX = right - 58;
        int banBtnW = 50;
        if (isHovered(mx, my, banBtnX, durY, banBtnX + banBtnW, durY + BTN_H)) {
            if (!inputPlayerName.isEmpty()) {
                String reason = inputReason.isEmpty() ? "Banned by admin" : inputReason;
                int durMin = BAN_DURATION_MINUTES[Math.min(selectedDuration, BAN_DURATION_MINUTES.length - 1)];
                ClientPacketDistributor.sendToServer((CustomPacketPayload) new ComputerActionPayload("mod_ban", inputPlayerName + ":" + reason + ":" + durMin), (CustomPacketPayload[]) new CustomPacketPayload[0]);
                setStatus("Banning " + inputPlayerName + "...", ACCENT);
                inputPlayerName = "";
                inputReason = "";
                nameBoxCursor = 0;
                reasonBoxCursor = 0;
            } else {
                setStatus("Enter a player name!", BAN_RED);
            }
            return true;
        }

        // Unfocus when clicking elsewhere in form area
        nameBoxFocused = false;
        reasonBoxFocused = false;

        // Unban buttons in the list
        int listY = y + 96; // after form (80) + header (16)
        for (int i = 0; i < bans.size(); i++) {
            int rowY = listY + i * (ROW_H + 6);
            int ubX = right - 46;
            int ubW = 38;
            if (isHovered(mx, my, ubX, rowY, ubX + ubW, rowY + 13)) {
                ClientPacketDistributor.sendToServer((CustomPacketPayload) new ComputerActionPayload("mod_unban", bans.get(i).name), (CustomPacketPayload[]) new CustomPacketPayload[0]);
                setStatus("Unbanning " + bans.get(i).name + "...", SUCCESS);
                return true;
            }
        }

        return false;
    }

    private boolean handleMutesClick(int mx, int my, int left, int top, int right, int bottom) {
        int w = right - left;
        int y = top - scroll;

        // Name input box
        int nameBoxX = left + 50;
        int nameBoxY = y + 16;
        int nameBoxW = Math.min(140, w / 2 - 30);
        if (isHovered(mx, my, nameBoxX, nameBoxY, nameBoxX + nameBoxW, nameBoxY + INPUT_H)) {
            nameBoxFocused = true;
            reasonBoxFocused = false;
            nameBoxCursor = getClickCursorPos(inputPlayerName, mx - nameBoxX - 3);
            return true;
        }

        // Duration buttons
        int durY = y + 36;
        int durX = left + 60;
        for (int i = 0; i < MUTE_DURATIONS.length; i++) {
            int dw = font.width(MUTE_DURATIONS[i]) + 8;
            if (isHovered(mx, my, durX, durY, durX + dw, durY + 14)) {
                selectedDuration = i;
                return true;
            }
            durX += dw + 2;
        }

        // Mute button
        int muteBtnX = right - 58;
        int muteBtnW = 50;
        if (isHovered(mx, my, muteBtnX, durY, muteBtnX + muteBtnW, durY + BTN_H)) {
            if (!inputPlayerName.isEmpty()) {
                int durMin = MUTE_DURATION_MINUTES[Math.min(selectedDuration, MUTE_DURATION_MINUTES.length - 1)];
                ClientPacketDistributor.sendToServer((CustomPacketPayload) new ComputerActionPayload("mod_mute", inputPlayerName + ":" + durMin), (CustomPacketPayload[]) new CustomPacketPayload[0]);
                setStatus("Muting " + inputPlayerName + "...", ACCENT);
                inputPlayerName = "";
                nameBoxCursor = 0;
            } else {
                setStatus("Enter a player name!", MUTE_ORANGE);
            }
            return true;
        }

        nameBoxFocused = false;
        reasonBoxFocused = false;

        // Unmute buttons
        int listY = y + 76; // after form (60) + header (16)
        for (int i = 0; i < mutes.size(); i++) {
            int rowY = listY + i * (ROW_H + 6);
            int ubX = right - 50;
            int ubW = 42;
            if (isHovered(mx, my, ubX, rowY, ubX + ubW, rowY + 13)) {
                ClientPacketDistributor.sendToServer((CustomPacketPayload) new ComputerActionPayload("mod_unmute", mutes.get(i).name), (CustomPacketPayload[]) new CustomPacketPayload[0]);
                setStatus("Unmuting " + mutes.get(i).name + "...", SUCCESS);
                return true;
            }
        }

        return false;
    }

    private boolean handleWarningsClick(int mx, int my, int left, int top, int right, int bottom) {
        int w = right - left;
        int y = top - scroll;

        // Name input box
        int nameBoxX = left + 50;
        int nameBoxY = y + 16;
        int nameBoxW = Math.min(140, w / 2 - 30);
        if (isHovered(mx, my, nameBoxX, nameBoxY, nameBoxX + nameBoxW, nameBoxY + INPUT_H)) {
            nameBoxFocused = true;
            reasonBoxFocused = false;
            nameBoxCursor = getClickCursorPos(inputPlayerName, mx - nameBoxX - 3);
            return true;
        }

        // Reason input box
        int reasonBoxX = left + 50;
        int reasonBoxY = y + 34;
        int reasonBoxW = Math.min(200, w - 90);
        if (isHovered(mx, my, reasonBoxX, reasonBoxY, reasonBoxX + reasonBoxW, reasonBoxY + INPUT_H)) {
            reasonBoxFocused = true;
            nameBoxFocused = false;
            reasonBoxCursor = getClickCursorPos(inputReason, mx - reasonBoxX - 3);
            return true;
        }

        // Warn button
        int warnBtnX = right - 58;
        int warnBtnW = 50;
        int warnBtnY = y + 34;
        if (isHovered(mx, my, warnBtnX, warnBtnY, warnBtnX + warnBtnW, warnBtnY + BTN_H)) {
            if (!inputPlayerName.isEmpty()) {
                String reason = inputReason.isEmpty() ? "Warned by admin" : inputReason;
                ClientPacketDistributor.sendToServer((CustomPacketPayload) new ComputerActionPayload("mod_warn", inputPlayerName + ":" + reason), (CustomPacketPayload[]) new CustomPacketPayload[0]);
                setStatus("Warning " + inputPlayerName + "...", ACCENT);
                inputPlayerName = "";
                inputReason = "";
                nameBoxCursor = 0;
                reasonBoxCursor = 0;
            } else {
                setStatus("Enter a player name!", WARN_YELLOW);
            }
            return true;
        }

        nameBoxFocused = false;
        reasonBoxFocused = false;

        // Clear buttons
        int listY = y + 76; // after form (60) + header (16)
        for (int i = 0; i < warnings.size(); i++) {
            int rowY = listY + i * (ROW_H + 6);
            int clX = right - 46;
            int clW = 38;
            if (isHovered(mx, my, clX, rowY, clX + clW, rowY + 13)) {
                ClientPacketDistributor.sendToServer((CustomPacketPayload) new ComputerActionPayload("mod_clear_warnings", warnings.get(i).name), (CustomPacketPayload[]) new CustomPacketPayload[0]);
                setStatus("Cleared warnings for " + warnings.get(i).name, SUCCESS);
                return true;
            }
        }

        return false;
    }

    private boolean handleLogClick(int mx, int my, int left, int top, int right, int bottom) {
        int y = top - scroll;

        // Filter buttons
        int filterX = left + 48;
        for (String filter : LOG_FILTERS) {
            int fw = font.width(filter) + 8;
            if (isHovered(mx, my, filterX, y + 2, filterX + fw, y + 18)) {
                logFilter = filter;
                scroll = 0;
                // Request filtered log
                ClientPacketDistributor.sendToServer((CustomPacketPayload) new ComputerActionPayload("mod_request_log_filtered", filter), (CustomPacketPayload[]) new CustomPacketPayload[0]);
                return true;
            }
            filterX += fw + 2;
        }

        nameBoxFocused = false;
        reasonBoxFocused = false;
        return false;
    }

    // ===================== SCROLL =====================

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scroll -= (int) (scrollY * 12.0);
        int maxScroll = Math.max(0, getContentHeight(0, 400) - 200);
        scroll = Math.max(0, Math.min(scroll, maxScroll));
        return true;
    }

    // ===================== KEY INPUT =====================

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!nameBoxFocused && !reasonBoxFocused) return false;

        // Backspace
        if (keyCode == 259) {
            if (nameBoxFocused && nameBoxCursor > 0) {
                inputPlayerName = inputPlayerName.substring(0, nameBoxCursor - 1) + inputPlayerName.substring(nameBoxCursor);
                nameBoxCursor--;
            } else if (reasonBoxFocused && reasonBoxCursor > 0) {
                inputReason = inputReason.substring(0, reasonBoxCursor - 1) + inputReason.substring(reasonBoxCursor);
                reasonBoxCursor--;
            }
            return true;
        }
        // Delete
        if (keyCode == 261) {
            if (nameBoxFocused && nameBoxCursor < inputPlayerName.length()) {
                inputPlayerName = inputPlayerName.substring(0, nameBoxCursor) + inputPlayerName.substring(nameBoxCursor + 1);
            } else if (reasonBoxFocused && reasonBoxCursor < inputReason.length()) {
                inputReason = inputReason.substring(0, reasonBoxCursor) + inputReason.substring(reasonBoxCursor + 1);
            }
            return true;
        }
        // Left arrow
        if (keyCode == 263) {
            if (nameBoxFocused && nameBoxCursor > 0) nameBoxCursor--;
            if (reasonBoxFocused && reasonBoxCursor > 0) reasonBoxCursor--;
            return true;
        }
        // Right arrow
        if (keyCode == 262) {
            if (nameBoxFocused && nameBoxCursor < inputPlayerName.length()) nameBoxCursor++;
            if (reasonBoxFocused && reasonBoxCursor < inputReason.length()) reasonBoxCursor++;
            return true;
        }
        // Home
        if (keyCode == 268) {
            if (nameBoxFocused) nameBoxCursor = 0;
            if (reasonBoxFocused) reasonBoxCursor = 0;
            return true;
        }
        // End
        if (keyCode == 269) {
            if (nameBoxFocused) nameBoxCursor = inputPlayerName.length();
            if (reasonBoxFocused) reasonBoxCursor = inputReason.length();
            return true;
        }
        // Tab: switch focus between name and reason
        if (keyCode == 258) {
            if (nameBoxFocused && (subTab == 0 || subTab == 2)) {
                nameBoxFocused = false;
                reasonBoxFocused = true;
                reasonBoxCursor = inputReason.length();
            } else if (reasonBoxFocused) {
                reasonBoxFocused = false;
                nameBoxFocused = true;
                nameBoxCursor = inputPlayerName.length();
            }
            return true;
        }
        // Enter: submit form
        if (keyCode == 257) {
            submitCurrentForm();
            return true;
        }
        // Escape: unfocus
        if (keyCode == 256) {
            nameBoxFocused = false;
            reasonBoxFocused = false;
            return true;
        }
        // Ctrl+A: select all
        if (keyCode == 65 && (modifiers & 2) != 0) {
            if (nameBoxFocused) {
                nameBoxCursor = inputPlayerName.length();
            }
            if (reasonBoxFocused) {
                reasonBoxCursor = inputReason.length();
            }
            return true;
        }

        return false;
    }

    public boolean charTyped(char c, int modifiers) {
        if (!nameBoxFocused && !reasonBoxFocused) return false;

        // Filter printable characters
        if (c < 32) return false;

        if (nameBoxFocused) {
            // Player names: alphanumeric and underscore only, max 16 chars
            if (inputPlayerName.length() < 16 && (Character.isLetterOrDigit(c) || c == '_')) {
                inputPlayerName = inputPlayerName.substring(0, nameBoxCursor) + c + inputPlayerName.substring(nameBoxCursor);
                nameBoxCursor++;
            }
            return true;
        }
        if (reasonBoxFocused) {
            // Reason: any printable char, max 100 chars, no colons (used as delimiter)
            if (inputReason.length() < 100 && c != ':') {
                inputReason = inputReason.substring(0, reasonBoxCursor) + c + inputReason.substring(reasonBoxCursor);
                reasonBoxCursor++;
            }
            return true;
        }

        return false;
    }

    // ===================== RESPONSE HANDLING =====================

    public void handleResponse(String type, String jsonData) {
        if ("mod_data".equals(type)) {
            parseModerationData(jsonData);
        } else if ("mod_log_data".equals(type)) {
            parseLogData(jsonData);
        } else if ("mod_action_result".equals(type)) {
            try {
                JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
                boolean success = obj.has("success") && obj.get("success").getAsBoolean();
                String msg = obj.has("msg") ? obj.get("msg").getAsString() : (success ? "Done." : "Failed.");
                setStatus(msg, success ? SUCCESS : BAN_RED);
                if (success) {
                    refreshCooldown = 5;
                }
            } catch (Exception e) {
                setStatus("Action completed.", ACCENT);
                refreshCooldown = 5;
            }
        }
    }

    public void requestData() {
        ClientPacketDistributor.sendToServer((CustomPacketPayload) new ComputerActionPayload("mod_request_data", ""), (CustomPacketPayload[]) new CustomPacketPayload[0]);
    }

    public void tick() {
        if (refreshCooldown > 0) {
            refreshCooldown--;
            if (refreshCooldown == 0) {
                requestData();
            }
        }
    }

    // ===================== PARSING =====================

    private void parseModerationData(String jsonData) {
        try {
            JsonObject root = JsonParser.parseString(jsonData).getAsJsonObject();

            // Parse bans
            bans.clear();
            if (root.has("bans")) {
                JsonArray bansArr = root.getAsJsonArray("bans");
                for (JsonElement el : bansArr) {
                    JsonObject b = el.getAsJsonObject();
                    bans.add(new BanEntry(
                        getStr(b, "name"),
                        getStr(b, "uuid"),
                        getStr(b, "reason"),
                        getStr(b, "date"),
                        getStr(b, "duration"),
                        getStr(b, "bannedBy"),
                        b.has("permanent") && b.get("permanent").getAsBoolean()
                    ));
                }
            }

            // Parse mutes
            mutes.clear();
            if (root.has("mutes")) {
                JsonArray mutesArr = root.getAsJsonArray("mutes");
                for (JsonElement el : mutesArr) {
                    JsonObject m = el.getAsJsonObject();
                    mutes.add(new MuteEntry(
                        getStr(m, "name"),
                        getStr(m, "uuid"),
                        getStr(m, "reason"),
                        m.has("expiresAt") ? m.get("expiresAt").getAsLong() : 0,
                        getStr(m, "duration")
                    ));
                }
            }

            // Parse warnings
            warnings.clear();
            if (root.has("warnings")) {
                JsonArray warnsArr = root.getAsJsonArray("warnings");
                for (JsonElement el : warnsArr) {
                    JsonObject w = el.getAsJsonObject();
                    warnings.add(new WarnEntry(
                        getStr(w, "name"),
                        getStr(w, "uuid"),
                        w.has("count") ? w.get("count").getAsInt() : 0,
                        getStr(w, "lastReason"),
                        getStr(w, "lastDate")
                    ));
                }
            }
        } catch (Exception e) {
            setStatus("Failed to parse moderation data.", BAN_RED);
        }
    }

    private void parseLogData(String jsonData) {
        try {
            actionLog.clear();
            JsonArray arr = JsonParser.parseString(jsonData).getAsJsonArray();
            for (JsonElement el : arr) {
                JsonObject o = el.getAsJsonObject();
                actionLog.add(new LogEntry(
                    getStr(o, "timestamp"),
                    getStr(o, "action"),
                    getStr(o, "admin"),
                    getStr(o, "target"),
                    getStr(o, "details"),
                    getStr(o, "type")
                ));
            }
        } catch (Exception e) {
            setStatus("Failed to parse log data.", BAN_RED);
        }
    }

    // ===================== HELPERS =====================

    private void submitCurrentForm() {
        switch (subTab) {
            case 0: {
                // Ban
                if (!inputPlayerName.isEmpty()) {
                    String reason = inputReason.isEmpty() ? "Banned by admin" : inputReason;
                    int durMin = BAN_DURATION_MINUTES[Math.min(selectedDuration, BAN_DURATION_MINUTES.length - 1)];
                    ClientPacketDistributor.sendToServer((CustomPacketPayload) new ComputerActionPayload("mod_ban", inputPlayerName + ":" + reason + ":" + durMin), (CustomPacketPayload[]) new CustomPacketPayload[0]);
                    setStatus("Banning " + inputPlayerName + "...", ACCENT);
                    inputPlayerName = "";
                    inputReason = "";
                    nameBoxCursor = 0;
                    reasonBoxCursor = 0;
                }
                break;
            }
            case 1: {
                // Mute
                if (!inputPlayerName.isEmpty()) {
                    int durMin = MUTE_DURATION_MINUTES[Math.min(selectedDuration, MUTE_DURATION_MINUTES.length - 1)];
                    ClientPacketDistributor.sendToServer((CustomPacketPayload) new ComputerActionPayload("mod_mute", inputPlayerName + ":" + durMin), (CustomPacketPayload[]) new CustomPacketPayload[0]);
                    setStatus("Muting " + inputPlayerName + "...", ACCENT);
                    inputPlayerName = "";
                    nameBoxCursor = 0;
                }
                break;
            }
            case 2: {
                // Warn
                if (!inputPlayerName.isEmpty()) {
                    String reason = inputReason.isEmpty() ? "Warned by admin" : inputReason;
                    ClientPacketDistributor.sendToServer((CustomPacketPayload) new ComputerActionPayload("mod_warn", inputPlayerName + ":" + reason), (CustomPacketPayload[]) new CustomPacketPayload[0]);
                    setStatus("Warning " + inputPlayerName + "...", ACCENT);
                    inputPlayerName = "";
                    inputReason = "";
                    nameBoxCursor = 0;
                    reasonBoxCursor = 0;
                }
                break;
            }
        }
        nameBoxFocused = false;
        reasonBoxFocused = false;
    }

    private int getContentHeight(int left, int right) {
        switch (subTab) {
            case 0: return 96 + Math.max(1, bans.size()) * (ROW_H + 6) + 20;
            case 1: return 76 + Math.max(1, mutes.size()) * (ROW_H + 6) + 20;
            case 2: return 76 + Math.max(1, warnings.size()) * (ROW_H + 6) + 20;
            case 3: {
                List<LogEntry> filtered = getFilteredLog();
                return 40 + Math.max(1, filtered.size()) * (ROW_H + 2) + 20;
            }
            default: return 200;
        }
    }

    private List<LogEntry> getFilteredLog() {
        if ("All".equals(logFilter)) return actionLog;
        List<LogEntry> filtered = new ArrayList<>();
        for (LogEntry entry : actionLog) {
            String t = entry.type.toUpperCase();
            boolean match = switch (logFilter) {
                case "Bans" -> t.contains("BAN");
                case "Kicks" -> t.equals("KICK");
                case "Mutes" -> t.contains("MUTE");
                case "Warns" -> t.contains("WARN");
                default -> true;
            };
            if (match) filtered.add(entry);
        }
        return filtered;
    }

    private int getActionColor(String type) {
        if (type == null) return TEXT;
        String upper = type.toUpperCase();
        if (upper.contains("BAN")) return BAN_RED;
        if (upper.equals("KICK")) return KICK_PURPLE;
        if (upper.contains("MUTE")) return MUTE_ORANGE;
        if (upper.contains("WARN")) return WARN_YELLOW;
        return LABEL;
    }

    private int getSubTabColor(int tab) {
        return switch (tab) {
            case 0 -> BAN_RED;
            case 1 -> MUTE_ORANGE;
            case 2 -> WARN_YELLOW;
            case 3 -> ACCENT;
            default -> LABEL;
        };
    }

    private int getFilterColor(String filter) {
        return switch (filter) {
            case "Bans" -> BAN_RED;
            case "Kicks" -> KICK_PURPLE;
            case "Mutes" -> MUTE_ORANGE;
            case "Warns" -> WARN_YELLOW;
            default -> ACCENT;
        };
    }

    private int getClickCursorPos(String text, int clickPixelX) {
        if (clickPixelX <= 0) return 0;
        for (int i = 1; i <= text.length(); i++) {
            int w = font.width(text.substring(0, i));
            if (w > clickPixelX) {
                int prevW = font.width(text.substring(0, i - 1));
                return (clickPixelX - prevW < w - clickPixelX) ? i - 1 : i;
            }
        }
        return text.length();
    }

    private void setStatus(String msg, int color) {
        statusMessage = msg;
        statusColor = color;
        statusExpiry = System.currentTimeMillis() + 5000;
    }

    private String getStr(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : "";
    }

    private String truncate(String text, int maxPixels) {
        if (text == null) return "";
        if (font.width(text) <= maxPixels) return text;
        for (int i = text.length() - 1; i > 0; i--) {
            if (font.width(text.substring(0, i) + "..") <= maxPixels) {
                return text.substring(0, i) + "..";
            }
        }
        return "..";
    }

    private void centerText(GuiGraphics g, String text, int x, int y, int w, int h, int color) {
        int tw = font.width(text);
        g.drawString(font, text, x + (w - tw) / 2, y + (h - 8) / 2, color, false);
    }

    private static boolean isHovered(int mx, int my, int x1, int y1, int x2, int y2) {
        return mx >= x1 && mx < x2 && my >= y1 && my < y2;
    }

    private static void drawBorder(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        g.fill(x1, y1, x2, y1 + 1, color);
        g.fill(x1, y2 - 1, x2, y2, color);
        g.fill(x1, y1, x1 + 1, y2, color);
        g.fill(x2 - 1, y1, x2, y2, color);
    }

    private void renderButton(GuiGraphics g, int mx, int my, int x, int y, int w, int h, String text, int color) {
        boolean hov = isHovered(mx, my, x, y, x + w, y + h);
        g.fill(x, y, x + w, y + h, hov ? color : BTN_BG);
        drawBorder(g, x, y, x + w, y + h, color);
        centerText(g, text, x, y, w, h, hov ? 0xFF000000 : TEXT);
    }
}

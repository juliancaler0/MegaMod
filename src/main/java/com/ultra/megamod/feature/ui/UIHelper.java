package com.ultra.megamod.feature.ui;

import java.util.Objects;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class UIHelper {
    // ─── Dark theme palette ───
    public static final int GOLD_BRIGHT = 0xFFDDDDEE;
    public static final int GOLD_MID = 0xFF888899;
    public static final int GOLD_DARK = 0xFF666677;
    public static final int GOLD_SHADOW = 0xFF333344;
    public static final int GOLD_DEEP = 0xFF1A1A28;
    public static final int LEATHER = 0xFF1C1C28;
    public static final int LEATHER_LITE = 0xFF242434;
    public static final int LEATHER_DARK = 0xFF141420;
    public static final int BG_DARK = 0xFF0E0E18;
    public static final int BLUE_ACCENT = 0xFF58A6FF;
    public static final int CREAM_TEXT = 0xFFCCCCDD;
    public static final int XP_GREEN = 0xFF44BF44;

    // Internal
    private static final int HIGHLIGHT = 0xFF2A2A3A;
    private static final int SHADOW = 0xFF0A0A14;
    private static final int SLOT_BG = 0xFF0E0E18;
    private static final int SCROLL_TRACK = 0xFF161622;
    private static final int PROGRESS_TROUGH = 0xFF0E0E18;
    private static final int BUTTON_PRESSED_BG = 0xFF141420;
    private static final int CARD_HOVER_BG = 0xFF262636;
    private static final int TAB_INACTIVE_BG = 0xFF161622;
    private static final int XP_GREEN_BRIGHT = 0xFF66D166;

    // HUD (semi-transparent, used for in-game overlays — keep separate)
    private static final int HUD_BG = 0xCC0E0E18;
    private static final int HUD_BORDER = 0xBB2A2A3A;
    private static final int HUD_HIGHLIGHT = 0x882A2A3A;
    private static final int HUD_SHADOW = 0x88141420;

    // ═══════════════════════════════════════════
    //                  PANELS
    // ═══════════════════════════════════════════

    public static void drawPanel(GuiGraphics g, int x, int y, int w, int h) {
        if (w < 6 || h < 6) {
            g.fill(x, y, x + w, y + h, LEATHER);
            return;
        }
        // Outer edge
        g.fill(x - 1, y - 1, x + w + 1, y + h + 1, GOLD_DEEP);
        // Frame
        g.fill(x, y, x + w, y + h, GOLD_SHADOW);
        g.fill(x, y, x + w, y + 1, HIGHLIGHT);
        g.fill(x, y, x + 1, y + h, HIGHLIGHT);
        g.fill(x, y + h - 1, x + w, y + h, SHADOW);
        g.fill(x + w - 1, y, x + w, y + h, SHADOW);
        // Inner area
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, LEATHER);
        g.fill(x + 1, y + 1, x + w - 1, y + 2, HIGHLIGHT);
        g.fill(x + 1, y + 1, x + 2, y + h - 1, HIGHLIGHT);
        g.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, SHADOW);
        g.fill(x + w - 2, y + 1, x + w - 1, y + h - 1, SHADOW);
    }

    public static void drawInsetPanel(GuiGraphics g, int x, int y, int w, int h) {
        if (w < 4 || h < 4) {
            g.fill(x, y, x + w, y + h, LEATHER_DARK);
            return;
        }
        g.fill(x, y, x + w, y + h, GOLD_DEEP);
        g.fill(x, y, x + w, y + 1, SHADOW);
        g.fill(x, y, x + 1, y + h, SHADOW);
        g.fill(x, y + h - 1, x + w, y + h, HIGHLIGHT);
        g.fill(x + w - 1, y, x + w, y + h, HIGHLIGHT);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, LEATHER_DARK);
        g.fill(x + 1, y + 1, x + w - 1, y + 2, SHADOW);
        g.fill(x + 1, y + 1, x + 2, y + h - 1, SHADOW);
    }

    public static void drawHudPanel(GuiGraphics g, int x, int y, int w, int h) {
        if (w < 4 || h < 4) {
            g.fill(x, y, x + w, y + h, HUD_BG);
            return;
        }
        g.fill(x, y, x + w, y + h, HUD_BG);
        g.fill(x, y, x + w, y + 1, HUD_BORDER);
        g.fill(x, y + h - 1, x + w, y + h, HUD_BORDER);
        g.fill(x, y, x + 1, y + h, HUD_BORDER);
        g.fill(x + w - 1, y, x + w, y + h, HUD_BORDER);
        g.fill(x + 1, y + 1, x + w - 1, y + 2, HUD_HIGHLIGHT);
        g.fill(x + 1, y + 1, x + 2, y + h - 1, HUD_HIGHLIGHT);
        g.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, HUD_SHADOW);
        g.fill(x + w - 2, y + 1, x + w - 1, y + h - 1, HUD_SHADOW);
        g.fill(x, y, x + 2, y + 2, HUD_BORDER);
        g.fill(x + w - 2, y, x + w, y + 2, HUD_BORDER);
        g.fill(x, y + h - 2, x + 2, y + h, HUD_BORDER);
        g.fill(x + w - 2, y + h - 2, x + w, y + h, HUD_BORDER);
    }

    public static void drawScreenBg(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + h, 0xCC000000);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, GOLD_DEEP);
        UIHelper.drawPanel(g, x + 2, y + 2, w - 4, h - 4);
    }

    // ═══════════════════════════════════════════
    //                  SLOTS
    // ═══════════════════════════════════════════

    public static void drawSlot(GuiGraphics g, int x, int y, int size) {
        UIHelper.drawSlot(g, x, y, size, false, false);
    }

    public static void drawSlot(GuiGraphics g, int x, int y, int size, boolean hovered, boolean active) {
        int borderColor = active ? BLUE_ACCENT : (hovered ? GOLD_MID : GOLD_SHADOW);
        g.fill(x, y, x + size, y + size, borderColor);
        g.fill(x, y, x + size, y + 1, hovered ? GOLD_MID : GOLD_SHADOW);
        g.fill(x, y, x + 1, y + size, hovered ? GOLD_MID : GOLD_SHADOW);
        g.fill(x, y + size - 1, x + size, y + size, hovered ? HIGHLIGHT : GOLD_DARK);
        g.fill(x + size - 1, y, x + size, y + size, hovered ? HIGHLIGHT : GOLD_DARK);
        g.fill(x + 1, y + 1, x + size - 1, y + size - 1, SLOT_BG);
        g.fill(x + 1, y + 1, x + size - 1, y + 2, SHADOW);
        g.fill(x + 1, y + 1, x + 2, y + size - 1, SHADOW);
        if (active) {
            g.fill(x + 1, y + size - 2, x + size - 1, y + size - 1, BLUE_ACCENT);
            g.fill(x + size - 2, y + 1, x + size - 1, y + size - 1, BLUE_ACCENT);
        }
        if (hovered && !active) {
            g.fill(x + 2, y + 2, x + size - 2, y + size - 2, 0x22FFFFFF);
        }
    }

    public static void drawSlotWithLabel(GuiGraphics g, Font font, int x, int y, int size, String label, boolean hovered) {
        UIHelper.drawSlot(g, x, y, size, hovered, false);
        int textW = font.width(label);
        int labelX = x + (size - textW) / 2;
        int labelY = y + size + 2;
        g.drawString(font, label, labelX, labelY, GOLD_DARK, false);
    }

    // ═══════════════════════════════════════════
    //                 BUTTONS
    // ═══════════════════════════════════════════

    public static void drawButton(GuiGraphics g, int x, int y, int w, int h, boolean hover) {
        UIHelper.drawButton(g, x, y, w, h, hover, false);
    }

    public static void drawButton(GuiGraphics g, int x, int y, int w, int h, boolean hovered, boolean pressed) {
        if (w < 4 || h < 4) {
            g.fill(x, y, x + w, y + h, LEATHER);
            return;
        }
        // Border
        g.fill(x, y, x + w, y + h, GOLD_SHADOW);
        if (pressed) {
            g.fill(x, y, x + w, y + 1, SHADOW);
            g.fill(x, y, x + 1, y + h, SHADOW);
            g.fill(x, y + h - 1, x + w, y + h, HIGHLIGHT);
            g.fill(x + w - 1, y, x + w, y + h, HIGHLIGHT);
            g.fill(x + 1, y + 1, x + w - 1, y + h - 1, BUTTON_PRESSED_BG);
            g.fill(x + 1, y + 1, x + w - 1, y + 2, SHADOW);
            g.fill(x + 1, y + 1, x + 2, y + h - 1, SHADOW);
        } else {
            int highlightColor = hovered ? GOLD_MID : HIGHLIGHT;
            int shadowColor = hovered ? GOLD_SHADOW : SHADOW;
            g.fill(x, y, x + w, y + 1, highlightColor);
            g.fill(x, y, x + 1, y + h, highlightColor);
            g.fill(x, y + h - 1, x + w, y + h, shadowColor);
            g.fill(x + w - 1, y, x + w, y + h, shadowColor);
            int bgColor = hovered ? LEATHER_LITE : LEATHER;
            g.fill(x + 1, y + 1, x + w - 1, y + h - 1, bgColor);
            g.fill(x + 1, y + 1, x + w - 1, y + 2, hovered ? HIGHLIGHT : 0xFF222230);
            g.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, SHADOW);
        }
    }

    public static void drawIconButton(GuiGraphics g, Font font, int x, int y, int size, String iconChar, int iconColor, boolean hovered) {
        UIHelper.drawButton(g, x, y, size, size, hovered, false);
        int textW = font.width(iconChar);
        int textX = x + (size - textW) / 2;
        Objects.requireNonNull(font);
        int textY = y + (size - 9) / 2;
        g.drawString(font, iconChar, textX, textY, iconColor, false);
    }

    // ═══════════════════════════════════════════
    //                  TABS
    // ═══════════════════════════════════════════

    public static void drawTab(GuiGraphics g, int x, int y, int w, int h, boolean selected) {
        UIHelper.drawTab(g, x, y, w, h, selected, BLUE_ACCENT);
    }

    public static void drawTab(GuiGraphics g, int x, int y, int w, int h, boolean selected, int accentColor) {
        if (w < 4 || h < 4) {
            g.fill(x, y, x + w, y + h, selected ? LEATHER : TAB_INACTIVE_BG);
            return;
        }
        if (selected) {
            g.fill(x, y, x + w, y + 1, HIGHLIGHT);
            g.fill(x, y, x + 1, y + h, HIGHLIGHT);
            g.fill(x + w - 1, y, x + w, y + h, SHADOW);
            g.fill(x + 1, y + 1, x + w - 1, y + h, LEATHER);
            g.fill(x + 1, y + 1, x + w - 1, y + 2, HIGHLIGHT);
            g.fill(x + 1, y + 1, x + 2, y + h, HIGHLIGHT);
            // Accent stripe at top
            g.fill(x + 2, y + 1, x + w - 2, y + 3, accentColor);
            // Blend bottom into panel
            g.fill(x + 1, y + h - 1, x + w - 1, y + h, LEATHER);
        } else {
            g.fill(x, y, x + w, y + 1, GOLD_DEEP);
            g.fill(x, y, x + 1, y + h, GOLD_DEEP);
            g.fill(x + w - 1, y, x + w, y + h, GOLD_DEEP);
            g.fill(x + 1, y + 1, x + w - 1, y + h, TAB_INACTIVE_BG);
            g.fill(x, y + h - 1, x + w, y + h, GOLD_DEEP);
            g.fill(x + 1, y + 1, x + w - 1, y + 2, SHADOW);
        }
    }

    // ═══════════════════════════════════════════
    //              PROGRESS BARS
    // ═══════════════════════════════════════════

    public static void drawProgressBarBg(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + h, GOLD_SHADOW);
        g.fill(x, y, x + w, y + 1, SHADOW);
        g.fill(x, y, x + 1, y + h, SHADOW);
        g.fill(x, y + h - 1, x + w, y + h, HIGHLIGHT);
        g.fill(x + w - 1, y, x + w, y + h, HIGHLIGHT);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, PROGRESS_TROUGH);
        g.fill(x + 1, y + 1, x + w - 1, y + 2, SHADOW);
    }

    public static void drawProgressBarFill(GuiGraphics g, int x, int y, int w, int h) {
        UIHelper.drawProgressBarFillColored(g, x, y, w, h, GOLD_MID, GOLD_BRIGHT, GOLD_SHADOW);
    }

    public static void drawProgressBar(GuiGraphics g, int x, int y, int w, int h, float progress, int fillColor) {
        UIHelper.drawProgressBarBg(g, x, y, w, h);
        int fillW = Math.round((float)(w - 2) * Math.max(0.0f, Math.min(1.0f, progress)));
        if (fillW > 0) {
            int bright = UIHelper.brightenColor(fillColor, 40);
            int dark = UIHelper.darkenColor(fillColor, 40);
            UIHelper.drawProgressBarFillColored(g, x + 1, y + 1, fillW, h - 2, fillColor, bright, dark);
        }
    }

    public static void drawXpBar(GuiGraphics g, Font font, int x, int y, int w, int h, float progress, int level) {
        UIHelper.drawProgressBarBg(g, x, y, w, h);
        int fillW = Math.round((float)(w - 2) * Math.max(0.0f, Math.min(1.0f, progress)));
        if (fillW > 0) {
            UIHelper.drawProgressBarFillColored(g, x + 1, y + 1, fillW, h - 2, XP_GREEN, XP_GREEN_BRIGHT, UIHelper.darkenColor(XP_GREEN, 60));
        }
        String levelStr = String.valueOf(level);
        int textW = font.width(levelStr);
        int textX = x + (w - textW) / 2;
        Objects.requireNonNull(font);
        int textY = y + (h - 9) / 2;
        g.drawString(font, levelStr, textX + 1, textY, 0xFF000000, false);
        g.drawString(font, levelStr, textX - 1, textY, 0xFF000000, false);
        g.drawString(font, levelStr, textX, textY + 1, 0xFF000000, false);
        g.drawString(font, levelStr, textX, textY - 1, 0xFF000000, false);
        g.drawString(font, levelStr, textX, textY, GOLD_BRIGHT, false);
    }

    private static void drawProgressBarFillColored(GuiGraphics g, int x, int y, int w, int h, int mainColor, int brightColor, int darkColor) {
        if (w <= 0 || h <= 0) {
            return;
        }
        g.fill(x, y, x + w, y + h, mainColor);
        if (h > 2) {
            g.fill(x, y, x + w, y + 1, brightColor);
        }
        if (h > 3) {
            g.fill(x, y + h - 1, x + w, y + h, darkColor);
        }
        if (w > 1 && h > 2) {
            g.fill(x + w - 1, y + 1, x + w, y + h - 1, brightColor);
        }
    }

    // ═══════════════════════════════════════════
    //                DIVIDERS
    // ═══════════════════════════════════════════

    public static void drawHorizontalDivider(GuiGraphics g, int x, int y, int w) {
        if (w < 10) {
            g.fill(x, y, x + w, y + 1, GOLD_SHADOW);
            return;
        }
        g.fill(x + 2, y, x + w - 2, y + 1, GOLD_SHADOW);
        g.fill(x + 2, y - 1, x + w - 2, y, SHADOW);
        // Subtle end caps
        g.fill(x + 1, y, x + 2, y + 1, GOLD_DARK);
        g.fill(x + w - 2, y, x + w - 1, y + 1, GOLD_DARK);
    }

    public static void drawVerticalDivider(GuiGraphics g, int x, int y, int h) {
        if (h < 10) {
            g.fill(x, y, x + 1, y + h, GOLD_SHADOW);
            return;
        }
        g.fill(x, y + 2, x + 1, y + h - 2, GOLD_SHADOW);
        g.fill(x + 1, y + 2, x + 2, y + h - 2, SHADOW);
        g.fill(x, y + 1, x + 1, y + 2, GOLD_DARK);
        g.fill(x, y + h - 2, x + 1, y + h - 1, GOLD_DARK);
    }

    public static void drawDivider(GuiGraphics g, int x, int y, int w) {
        UIHelper.drawHorizontalDivider(g, x, y, w);
    }

    // ═══════════════════════════════════════════
    //                  TEXT
    // ═══════════════════════════════════════════

    public static void drawTitle(GuiGraphics g, Font font, String text, int x, int y) {
        // Outline shadow
        g.drawString(font, text, x + 1, y, SHADOW, false);
        g.drawString(font, text, x - 1, y, SHADOW, false);
        g.drawString(font, text, x, y + 1, SHADOW, false);
        g.drawString(font, text, x, y - 1, SHADOW, false);
        g.drawString(font, text, x, y, GOLD_BRIGHT, false);
    }

    public static void drawLabel(GuiGraphics g, Font font, String text, int x, int y) {
        g.drawString(font, text, x, y, CREAM_TEXT, false);
    }

    public static void drawCenteredTitle(GuiGraphics g, Font font, String text, int centerX, int y) {
        int textW = font.width(text);
        int x = centerX - textW / 2;
        UIHelper.drawTitle(g, font, text, x, y);
    }

    public static void drawCenteredLabel(GuiGraphics g, Font font, String text, int centerX, int y) {
        int textW = font.width(text);
        g.drawString(font, text, centerX - textW / 2, y, CREAM_TEXT, false);
    }

    public static void drawShadowedText(GuiGraphics g, Font font, String text, int x, int y, int color) {
        g.drawString(font, text, x + 1, y + 1, UIHelper.darkenColor(color, 120), false);
        g.drawString(font, text, x, y, color, false);
    }

    // ═══════════════════════════════════════════
    //               CARDS & ROWS
    // ═══════════════════════════════════════════

    public static void drawCard(GuiGraphics g, int x, int y, int w, int h) {
        UIHelper.drawCard(g, x, y, w, h, false);
    }

    public static void drawCard(GuiGraphics g, int x, int y, int w, int h, boolean hovered) {
        if (w < 4 || h < 4) {
            g.fill(x, y, x + w, y + h, hovered ? CARD_HOVER_BG : LEATHER);
            return;
        }
        // Border
        g.fill(x, y, x + w, y + h, GOLD_SHADOW);
        g.fill(x, y, x + w, y + 1, hovered ? HIGHLIGHT : GOLD_SHADOW);
        g.fill(x, y, x + 1, y + h, hovered ? HIGHLIGHT : GOLD_SHADOW);
        g.fill(x, y + h - 1, x + w, y + h, GOLD_DEEP);
        g.fill(x + w - 1, y, x + w, y + h, GOLD_DEEP);
        // Background
        int bgColor = hovered ? CARD_HOVER_BG : LEATHER;
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, bgColor);
        g.fill(x + 1, y + 1, x + w - 1, y + 2, hovered ? HIGHLIGHT : 0xFF222230);
        g.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, SHADOW);
        if (hovered) {
            g.fill(x + 2, y + 1, x + w - 2, y + 2, GOLD_MID);
        }
    }

    public static void drawTitleBar(GuiGraphics g, int x, int y, int w, int h) {
        if (w < 4 || h < 4) {
            g.fill(x, y, x + w, y + h, GOLD_SHADOW);
            return;
        }
        // Dark header
        g.fill(x, y, x + w, y + h, LEATHER_DARK);
        g.fill(x, y, x + w, y + 1, HIGHLIGHT);
        g.fill(x, y, x + 1, y + h, HIGHLIGHT);
        g.fill(x, y + h - 1, x + w, y + h, SHADOW);
        g.fill(x + w - 1, y, x + w, y + h, SHADOW);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xFF1A1A28);
        g.fill(x + 1, y + 1, x + w - 1, y + 2, HIGHLIGHT);
        // Bottom accent line
        g.fill(x + 1, y + h - 3, x + w - 1, y + h - 1, GOLD_SHADOW);
        g.fill(x + 1, y + h - 3, x + w - 1, y + h - 2, HIGHLIGHT);
        g.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, SHADOW);
    }

    public static void drawTooltipBackground(GuiGraphics g, int x, int y, int w, int h) {
        if (w < 4 || h < 4) {
            g.fill(x, y, x + w, y + h, 0xF0141420);
            return;
        }
        // Drop shadow
        g.fill(x + 1, y + 1, x + w + 1, y + h + 1, 0x66000000);
        // Border
        g.fill(x, y, x + w, y + h, 0xF02A2A3A);
        g.fill(x, y, x + w, y + 1, HIGHLIGHT);
        g.fill(x, y, x + 1, y + h, HIGHLIGHT);
        g.fill(x, y + h - 1, x + w, y + h, SHADOW);
        g.fill(x + w - 1, y, x + w, y + h, SHADOW);
        // Background
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xF0141420);
        g.fill(x + 1, y + 1, x + w - 1, y + 2, 0x33FFFFFF);
    }

    public static void drawScrollbar(GuiGraphics g, int x, int y, int h, float scrollProgress) {
        int trackWidth = 6;
        int thumbHeight = Math.max(16, h / 5);
        int thumbTravel = h - thumbHeight;
        int thumbY = y + Math.round((float)thumbTravel * Math.max(0.0f, Math.min(1.0f, scrollProgress)));
        // Track
        g.fill(x, y, x + trackWidth, y + h, SCROLL_TRACK);
        g.fill(x, y, x + 1, y + h, GOLD_DEEP);
        g.fill(x + trackWidth - 1, y, x + trackWidth, y + h, GOLD_DEEP);
        g.fill(x + 1, y, x + trackWidth - 1, y + 1, SHADOW);
        // Thumb
        g.fill(x, thumbY, x + trackWidth, thumbY + thumbHeight, GOLD_SHADOW);
        g.fill(x, thumbY, x + trackWidth, thumbY + 1, HIGHLIGHT);
        g.fill(x, thumbY, x + 1, thumbY + thumbHeight, HIGHLIGHT);
        g.fill(x, thumbY + thumbHeight - 1, x + trackWidth, thumbY + thumbHeight, SHADOW);
        g.fill(x + trackWidth - 1, thumbY, x + trackWidth, thumbY + thumbHeight, SHADOW);
        // Grip lines
        int gripY = thumbY + thumbHeight / 2 - 2;
        g.fill(x + 2, gripY, x + trackWidth - 2, gripY + 1, GOLD_MID);
        g.fill(x + 2, gripY + 2, x + trackWidth - 2, gripY + 3, GOLD_MID);
        g.fill(x + 2, gripY + 4, x + trackWidth - 2, gripY + 5, GOLD_MID);
    }

    public static void drawRowBg(GuiGraphics g, int x, int y, int w, int h, boolean even) {
        int bgColor = even ? LEATHER_DARK : 0xFF181824;
        g.fill(x, y, x + w, y + h, bgColor);
        g.fill(x, y, x + w, y + 1, even ? 0x12FFFFFF : 0x08FFFFFF);
    }

    // ═══════════════════════════════════════════
    //               UTILITIES
    // ═══════════════════════════════════════════

    public static int brightenColor(int color, int amount) {
        int a = color >> 24 & 0xFF;
        int r = Math.min(255, (color >> 16 & 0xFF) + amount);
        int g = Math.min(255, (color >> 8 & 0xFF) + amount);
        int b = Math.min(255, (color & 0xFF) + amount);
        return a << 24 | r << 16 | g << 8 | b;
    }

    public static int darkenColor(int color, int amount) {
        int a = color >> 24 & 0xFF;
        int r = Math.max(0, (color >> 16 & 0xFF) - amount);
        int g = Math.max(0, (color >> 8 & 0xFF) - amount);
        int b = Math.max(0, (color & 0xFF) - amount);
        return a << 24 | r << 16 | g << 8 | b;
    }
}

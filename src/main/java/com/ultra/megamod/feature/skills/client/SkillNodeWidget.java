/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphics
 */
package com.ultra.megamod.feature.skills.client;

import com.ultra.megamod.feature.skills.SkillNode;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class SkillNodeWidget {
    public static final int NODE_RADIUS = 16;
    public static final int NODE_DIAMETER = 32;
    private static final int UNLOCKED_BG = -12965880;
    private static final int UNLOCKED_BORDER = -10496;
    private static final int UNLOCKED_INNER = -2448096;
    private static final int UNLOCKED_GLOW = 1358944000;
    private static final int AVAILABLE_BG = -14018032;
    private static final int AVAILABLE_BORDER = -3355444;
    private static final int AVAILABLE_BORDER_PULSE = -1;
    private static final int LOCKED_BG = -15069688;
    private static final int LOCKED_BORDER = -11184811;
    private static final int BRANCH_LOCKED_BG = -15071224;
    private static final int BRANCH_LOCKED_BORDER = -10083806;
    private static final int BRANCH_LOCKED_TEXT = -11193549;
    private static final int HOVERED_OUTER_GLOW = 0x30FFFFFF;
    private static final int TEXT_UNLOCKED = -10496;
    private static final int TEXT_AVAILABLE = -991040;
    private static final int TEXT_LOCKED = -9807286;
    private static final int TEXT_NAME = -4151176;
    private static final int TEXT_NAME_UNLOCKED = -10496;
    private static final int TEXT_NAME_LOCKED = -11184811;
    private static final int TOOLTIP_BG = -266727928;
    private static final int TOOLTIP_BORDER = -3626932;
    private static final int TOOLTIP_TITLE = -10496;
    private static final int TOOLTIP_DESC = -991040;
    private static final int TOOLTIP_COST = -2448096;
    private static final int TOOLTIP_BONUS = -11751600;
    private static final int TOOLTIP_PREREQ = -3394765;

    private SkillNodeWidget() {
    }

    public static void draw(GuiGraphics g, int cx, int cy, SkillNode node, boolean unlocked, boolean available, boolean hovered, float animTick, float zoom, boolean branchLocked) {
        int nameColor;
        int textColor;
        int borderColor;
        int bgColor;
        Font font = Minecraft.getInstance().font;
        int r = (int)(16.0f * Math.min(1.5f, Math.max(0.7f, zoom)));
        if (unlocked) {
            int glowColor = node.branch().getTreeType().getColor();
            int glowAlpha = 64;
            glowColor = glowAlpha << 24 | glowColor & 0xFFFFFF;
            SkillNodeWidget.drawFilledCircle(g, cx, cy, r + 4, glowColor);
        }
        if (hovered) {
            SkillNodeWidget.drawFilledCircle(g, cx, cy, r + 3, 0x30FFFFFF);
        }
        if (branchLocked) {
            bgColor = -15071224;
            borderColor = -10083806;
            textColor = -11193549;
            nameColor = -11193549;
        } else if (unlocked) {
            bgColor = -12965880;
            borderColor = node.branch().getTreeType().getColor();
            textColor = -10496;
            nameColor = -10496;
        } else if (available) {
            bgColor = -14018032;
            float pulse = (float)(Math.sin((double)animTick * 0.1) * 0.5 + 0.5);
            borderColor = SkillNodeWidget.lerpColor(-3355444, -1, pulse);
            textColor = -991040;
            nameColor = -4151176;
        } else {
            bgColor = -15069688;
            borderColor = -11184811;
            textColor = -9807286;
            nameColor = -11184811;
        }
        SkillNodeWidget.drawFilledCircle(g, cx, cy, r, bgColor);
        SkillNodeWidget.drawCircleRing(g, cx, cy, r, borderColor);
        if (unlocked && !branchLocked) {
            SkillNodeWidget.drawCircleRing(g, cx, cy, r - 2, -2448096);
        }
        if (zoom >= 0.6f) {
            String initials = node.displayName().length() >= 2 ? node.displayName().substring(0, 2).toUpperCase() : node.displayName().toUpperCase();
            int textW = font.width(initials);
            int n = cx - textW / 2;
            Objects.requireNonNull(font);
            g.drawString(font, initials, n, cy - 9 / 2, textColor, false);
        }
        if (branchLocked && zoom >= 0.6f) {
            String lockIcon = "X";
            int lockW = font.width(lockIcon);
            int n = cx - lockW / 2;
            Objects.requireNonNull(font);
            g.drawString(font, lockIcon, n, cy - 9 / 2, -3394765, false);
        }
        if (zoom >= 0.8f) {
            Object name = zoom < 1.2f ? (node.displayName().length() > 4 ? node.displayName().substring(0, 4) + "." : node.displayName()) : node.displayName();
            int nameW = font.width((String)name);
            g.drawString(font, (String)name, cx - nameW / 2, cy + r + 3, nameColor, false);
            String tierStr = SkillNodeWidget.toRoman(node.tier());
            int tierW = font.width(tierStr);
            int n = cx - tierW / 2;
            Objects.requireNonNull(font);
            g.drawString(font, tierStr, n, cy + r + 3 + 9 + 1, -7833512, false);
        }
        if (available && zoom >= 0.6f) {
            String costStr = String.valueOf(node.cost());
            int costW = font.width(costStr);
            int badgeX = cx + r - 2;
            int badgeY = cy - r - 2;
            Objects.requireNonNull(font);
            g.fill(badgeX - 1, badgeY - 1, badgeX + costW + 3, badgeY + 9 + 1, -872415232);
            Objects.requireNonNull(font);
            g.fill(badgeX, badgeY, badgeX + costW + 2, badgeY + 9, -14018032);
            g.drawString(font, costStr, badgeX + 1, badgeY, -10496, false);
        }
    }

    public static void draw(GuiGraphics g, int cx, int cy, SkillNode node, boolean unlocked, boolean available, boolean hovered, float animTick) {
        SkillNodeWidget.draw(g, cx, cy, node, unlocked, available, hovered, animTick, 1.0f, false);
    }

    private static final int MAX_TOOLTIP_TEXT_WIDTH = 220;

    /** Word-wraps a string to fit within maxPixelWidth and adds each wrapped line. */
    private static void addWrappedLines(ArrayList<TooltipLine> lines, String text, int color, Font font, int maxPixelWidth) {
        if (font.width(text) <= maxPixelWidth) {
            lines.add(new TooltipLine(text, color));
            return;
        }
        String[] words = text.split(" ");
        StringBuilder current = new StringBuilder();
        for (String word : words) {
            String test = current.isEmpty() ? word : current + " " + word;
            if (font.width(test) > maxPixelWidth && !current.isEmpty()) {
                lines.add(new TooltipLine(current.toString(), color));
                current = new StringBuilder(word);
            } else {
                if (!current.isEmpty()) current.append(" ");
                current.append(word);
            }
        }
        if (!current.isEmpty()) {
            lines.add(new TooltipLine(current.toString(), color));
        }
    }

    public static void drawTooltip(GuiGraphics g, int mouseX, int mouseY, SkillNode node, boolean unlocked, boolean available, int points, boolean branchLocked) {
        Font font = Minecraft.getInstance().font;
        ArrayList<TooltipLine> lines = new ArrayList<TooltipLine>();
        lines.add(new TooltipLine(node.displayName(), -10496));
        lines.add(new TooltipLine(node.branch().getDisplayName() + " | " + node.branch().getTreeType().getDisplayName() + " | Tier " + node.tier(), -5728136));
        lines.add(new TooltipLine("Cost: " + node.cost() + " pts", -2448096));
        lines.add(new TooltipLine("", 0));
        addWrappedLines(lines, node.description(), -991040, font, MAX_TOOLTIP_TEXT_WIDTH);
        if (!node.bonuses().isEmpty()) {
            lines.add(new TooltipLine("", 0));
            lines.add(new TooltipLine("--- Bonuses ---", -5728136));
            for (Map.Entry<String, Double> bonus : node.bonuses().entrySet()) {
                String bonusName = SkillNodeWidget.formatBonusName(bonus.getKey());
                String bonusVal = SkillNodeWidget.formatBonusValue(bonus.getValue());
                lines.add(new TooltipLine("  +" + bonusVal + " " + bonusName, -11751600));
            }
        }
        if (!node.prerequisites().isEmpty()) {
            lines.add(new TooltipLine("", 0));
            addWrappedLines(lines, "Requires: " + String.join((CharSequence)", ", node.prerequisites()), -3394765, font, MAX_TOOLTIP_TEXT_WIDTH);
        }
        lines.add(new TooltipLine("", 0));
        lines.add(new TooltipLine("Branch: " + node.branch().getDisplayName(), -5728136));
        lines.add(new TooltipLine("", 0));
        if (branchLocked) {
            lines.add(new TooltipLine("BRANCH LOCKED", -3394765));
            lines.add(new TooltipLine("Already specialized in 2 other branches", -6737101));
            lines.add(new TooltipLine("Respec to unlock this branch", -6737101));
        } else if (unlocked) {
            lines.add(new TooltipLine("UNLOCKED", -11751600));
        } else if (available) {
            lines.add(new TooltipLine("Click to unlock (" + points + " pts)", -991040));
        } else {
            lines.add(new TooltipLine("Locked", -3394765));
        }
        int padding = 8;
        int lineSpacing = 2;
        int maxWidth = 0;
        int totalHeight = padding * 2;
        for (TooltipLine line : lines) {
            if (line.text.isEmpty()) {
                totalHeight += 4;
                continue;
            }
            int w = font.width(line.text);
            if (w > maxWidth) {
                maxWidth = w;
            }
            Objects.requireNonNull(font);
            totalHeight += 9 + lineSpacing;
        }
        int tooltipW = maxWidth + padding * 2;
        int tooltipH = totalHeight -= lineSpacing;
        int screenW = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int screenH = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        int tx = mouseX + 14;
        int ty = mouseY - 4;
        if (tx + tooltipW > screenW - 4) {
            tx = mouseX - tooltipW - 4;
        }
        if (ty + tooltipH > screenH - 4) {
            ty = screenH - tooltipH - 4;
        }
        if (ty < 4) {
            ty = 4;
        }
        g.fill(tx - 2, ty - 2, tx + tooltipW + 2, ty + tooltipH + 2, -3626932);
        g.fill(tx - 1, ty - 1, tx + tooltipW + 1, ty + tooltipH + 1, -16120316);
        g.fill(tx, ty, tx + tooltipW, ty + tooltipH, -266727928);
        g.fill(tx - 2, ty - 2, tx + 4, ty - 1, -10496);
        g.fill(tx - 2, ty - 2, tx - 1, ty + 4, -10496);
        g.fill(tx + tooltipW - 4, ty - 2, tx + tooltipW + 2, ty - 1, -10496);
        g.fill(tx + tooltipW + 1, ty - 2, tx + tooltipW + 2, ty + 4, -10496);
        int drawY = ty + padding;
        for (TooltipLine line : lines) {
            if (line.text.isEmpty()) {
                drawY += 4;
                continue;
            }
            g.drawString(font, line.text, tx + padding, drawY, line.color, false);
            Objects.requireNonNull(font);
            drawY += 9 + lineSpacing;
        }
    }

    public static void drawTooltip(GuiGraphics g, int mouseX, int mouseY, SkillNode node, boolean unlocked, boolean available, int points) {
        SkillNodeWidget.drawTooltip(g, mouseX, mouseY, node, unlocked, available, points, false);
    }

    public static void drawFilledCircle(GuiGraphics g, int cx, int cy, int radius, int color) {
        for (int dy = -radius; dy <= radius; ++dy) {
            int dx = (int)Math.sqrt(radius * radius - dy * dy);
            g.fill(cx - dx, cy + dy, cx + dx, cy + dy + 1, color);
        }
    }

    private static void drawCircleRing(GuiGraphics g, int cx, int cy, int radius, int color) {
        int segments = Math.max(16, radius * 4);
        for (int i = 0; i < segments; ++i) {
            double angle = Math.PI * 2 * (double)i / (double)segments;
            int px = cx + (int)(Math.cos(angle) * (double)radius);
            int py = cy + (int)(Math.sin(angle) * (double)radius);
            g.fill(px, py, px + 1, py + 1, color);
        }
    }

    private static int lerpColor(int c1, int c2, float t) {
        int a1 = c1 >> 24 & 0xFF;
        int r1 = c1 >> 16 & 0xFF;
        int g1 = c1 >> 8 & 0xFF;
        int b1 = c1 & 0xFF;
        int a2 = c2 >> 24 & 0xFF;
        int r2 = c2 >> 16 & 0xFF;
        int g2 = c2 >> 8 & 0xFF;
        int b2 = c2 & 0xFF;
        int a = (int)((float)a1 + (float)(a2 - a1) * t);
        int r = (int)((float)r1 + (float)(r2 - r1) * t);
        int gg = (int)((float)g1 + (float)(g2 - g1) * t);
        int b = (int)((float)b1 + (float)(b2 - b1) * t);
        return a << 24 | r << 16 | gg << 8 | b;
    }

    private static String toRoman(int tier) {
        return switch (tier) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> String.valueOf(tier);
        };
    }

    private static String formatBonusName(String key) {
        StringBuilder sb = new StringBuilder();
        String[] parts = key.split("_");
        for (int i = 0; i < parts.length; ++i) {
            if (i > 0) {
                sb.append(" ");
            }
            if (parts[i].isEmpty()) continue;
            sb.append(Character.toUpperCase(parts[i].charAt(0)));
            if (parts[i].length() <= 1) continue;
            sb.append(parts[i].substring(1));
        }
        return sb.toString();
    }

    private static String formatBonusValue(double value) {
        if (value == (double)((int)value)) {
            return String.valueOf((int)value);
        }
        return String.format("%.1f", value);
    }

    private record TooltipLine(String text, int color) {
    }
}


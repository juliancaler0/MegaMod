package com.ultra.megamod.feature.ambientsounds.util;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class AmbientDebugRenderer {

    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.##");

    private final List<String> lines = new ArrayList<>();
    private StringBuilder currentLine = new StringBuilder();

    public static String format(double val) {
        return DECIMAL_FORMAT.format(val);
    }

    public void text(String line) {
        if (currentLine.length() > 0)
            currentLine.append(" ");
        currentLine.append(line);
    }

    public void detail(String key, Object value) {
        if (currentLine.length() > 0)
            currentLine.append(" ");
        currentLine.append(key).append(": ").append(value);
    }

    public void detail(String key, double value) {
        if (currentLine.length() > 0)
            currentLine.append(" ");
        currentLine.append(key).append(": ").append(DECIMAL_FORMAT.format(value));
    }

    public void detail(String key, boolean value) {
        if (currentLine.length() > 0)
            currentLine.append(" ");
        currentLine.append(key).append(": ").append(value);
    }

    /** Flush the current line and start a new one. */
    public void newLine() {
        lines.add(currentLine.toString());
        currentLine = new StringBuilder();
    }

    public List<String> getLines() {
        // Flush any remaining content
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
            currentLine = new StringBuilder();
        }
        return lines;
    }

    public void clear() {
        lines.clear();
        currentLine = new StringBuilder();
    }

    /**
     * Renders all accumulated debug text lines to the screen.
     * Replaces CreativeCore's DebugTextRenderer.render().
     */
    public void render(Font font, GuiGraphics graphics) {
        // Flush any remaining content
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
            currentLine = new StringBuilder();
        }

        int y = 2;
        int lineHeight = font.lineHeight + 2;
        for (String line : lines) {
            if (!line.isEmpty()) {
                // Draw background for readability
                int width = font.width(line);
                graphics.fill(1, y - 1, 3 + width, y + font.lineHeight, 0x80000000);
                // Draw text
                graphics.drawString(font, line, 2, y, 0xFFFFFFFF, false);
            }
            y += lineHeight;
        }
    }
}

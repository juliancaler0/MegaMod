package com.ultra.megamod.feature.computer.screen.map;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

public class MapDrawingOverlay {

    public record DrawingLine(String id, int x1, int z1, int x2, int z2, int color, boolean shared) {}
    public record DrawingText(String id, String text, int x, int z, int color, boolean shared) {}

    private static final List<DrawingLine> lines = new ArrayList<>();
    private static final List<DrawingText> texts = new ArrayList<>();

    public static void updateFromServer(List<DrawingLine> newLines, List<DrawingText> newTexts) {
        lines.clear();
        lines.addAll(newLines);
        texts.clear();
        texts.addAll(newTexts);
    }

    public static void addLineLocal(DrawingLine line) {
        lines.add(line);
    }

    public static void addTextLocal(DrawingText text) {
        texts.add(text);
    }

    public static List<DrawingLine> getLines() {
        return lines;
    }

    public static List<DrawingText> getTexts() {
        return texts;
    }

    public static void render(GuiGraphics g, Font font, int mapLeft, int mapTop,
            int mapWidth, int mapHeight, double centerWorldX, double centerWorldZ, double zoomScale) {

        // Render lines
        for (DrawingLine line : lines) {
            int sx1 = worldToScreenX(line.x1, centerWorldX, mapLeft, mapWidth, zoomScale);
            int sy1 = worldToScreenY(line.z1, centerWorldZ, mapTop, mapHeight, zoomScale);
            int sx2 = worldToScreenX(line.x2, centerWorldX, mapLeft, mapWidth, zoomScale);
            int sy2 = worldToScreenY(line.z2, centerWorldZ, mapTop, mapHeight, zoomScale);

            // Clip check - skip if entirely off-screen
            if ((sx1 < mapLeft && sx2 < mapLeft) || (sx1 > mapLeft + mapWidth && sx2 > mapLeft + mapWidth)) continue;
            if ((sy1 < mapTop && sy2 < mapTop) || (sy1 > mapTop + mapHeight && sy2 > mapTop + mapHeight)) continue;

            drawLine(g, sx1, sy1, sx2, sy2, line.color, mapLeft, mapTop, mapWidth, mapHeight);
        }

        // Render text annotations
        for (DrawingText text : texts) {
            int sx = worldToScreenX(text.x, centerWorldX, mapLeft, mapWidth, zoomScale);
            int sy = worldToScreenY(text.z, centerWorldZ, mapTop, mapHeight, zoomScale);

            if (sx >= mapLeft && sx < mapLeft + mapWidth && sy >= mapTop && sy < mapTop + mapHeight) {
                // Background for readability
                int textW = font.width(text.text);
                g.fill(sx - 1, sy - 1, sx + textW + 1, sy + 9, 0xAA000000);
                g.drawString(font, text.text, sx, sy, text.color, false);
            }
        }
    }

    /**
     * Render a dashed line between two screen points (for measuring).
     */
    public static void renderDashedLine(GuiGraphics g, int sx1, int sy1, int sx2, int sy2, int color,
            int mapLeft, int mapTop, int mapWidth, int mapHeight) {
        int dx = sx2 - sx1;
        int dy = sy2 - sy1;
        double length = Math.sqrt(dx * dx + dy * dy);
        if (length < 1) return;

        int dashLen = 4;
        int gapLen = 3;
        double stepX = dx / length;
        double stepY = dy / length;

        double x = sx1;
        double y = sy1;
        double traveled = 0;
        boolean drawing = true;

        while (traveled < length) {
            int segLen = drawing ? dashLen : gapLen;
            double endTravel = Math.min(traveled + segLen, length);

            if (drawing) {
                int px1 = (int) x;
                int py1 = (int) y;
                double advance = endTravel - traveled;
                int px2 = (int) (x + stepX * advance);
                int py2 = (int) (y + stepY * advance);
                drawLine(g, px1, py1, px2, py2, color, mapLeft, mapTop, mapWidth, mapHeight);
            }

            double advance = endTravel - traveled;
            x += stepX * advance;
            y += stepY * advance;
            traveled = endTravel;
            drawing = !drawing;
        }
    }

    /**
     * Draw a line between two screen coordinates using Bresenham's algorithm.
     */
    private static void drawLine(GuiGraphics g, int x1, int y1, int x2, int y2, int color,
            int mapLeft, int mapTop, int mapWidth, int mapHeight) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;

        int maxSteps = dx + dy + 1;
        int steps = 0;

        while (steps < maxSteps) {
            if (x1 >= mapLeft && x1 < mapLeft + mapWidth && y1 >= mapTop && y1 < mapTop + mapHeight) {
                g.fill(x1, y1, x1 + 1, y1 + 1, color);
            }
            if (x1 == x2 && y1 == y2) break;
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x1 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y1 += sy;
            }
            steps++;
        }
    }

    public static int worldToScreenX(int worldX, double centerWorldX, int mapLeft, int mapWidth, double zoomScale) {
        return (int) (mapLeft + mapWidth / 2.0 + (worldX - centerWorldX) / zoomScale);
    }

    public static int worldToScreenY(int worldZ, double centerWorldZ, int mapTop, int mapHeight, double zoomScale) {
        return (int) (mapTop + mapHeight / 2.0 + (worldZ - centerWorldZ) / zoomScale);
    }

    public static int screenToWorldX(int screenX, double centerWorldX, int mapLeft, int mapWidth, double zoomScale) {
        return (int) (centerWorldX + (screenX - mapLeft - mapWidth / 2.0) * zoomScale);
    }

    public static int screenToWorldZ(int screenY, double centerWorldZ, int mapTop, int mapHeight, double zoomScale) {
        return (int) (centerWorldZ + (screenY - mapTop - mapHeight / 2.0) * zoomScale);
    }
}

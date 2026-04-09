package com.ultra.megamod.feature.computer.screen.map;

import com.ultra.megamod.feature.ui.UIHelper;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class MapLayerRenderer {

    // Layer visibility toggles
    public boolean showChunkGrid = false;
    public boolean showRegionGrid = false;
    public boolean showHighlights = false;
    public boolean showStructures = false;
    public boolean isAdmin = false;
    public boolean showPlayers = true;
    public boolean showWaypoints = false;
    public boolean showDrawings = false;
    public boolean showEntities = false;
    public boolean caveView = false;

    private static final String[] TOGGLE_LABELS = {"CG", "RG", "HL", "ST", "PL", "WP", "DR", "EN", "CV"};
    private static final String[] TOGGLE_TOOLTIPS = {
        "Chunk Grid", "Region Grid", "Highlights", "Structures",
        "Players", "Waypoints", "Drawings", "Entities", "Cave View"
    };
    private static final int TOGGLE_SIZE = 14;
    private static final int TOGGLE_GAP = 2;

    // Track which toggle is hovered for tooltip rendering (drawn last to be on top)
    private int hoveredToggleIndex = -1;
    private int hoveredToggleY = 0;

    /**
     * Returns the list of toggle indices visible to the current player.
     * Structures (index 3) is admin-only.
     */
    private int[] getVisibleToggles() {
        if (this.isAdmin) {
            return new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8};
        }
        return new int[]{0, 1, 2, 4, 5, 6, 7, 8}; // skip 3 (ST)
    }

    /**
     * Render toggle buttons as a vertical column.
     */
    public void renderToggles(GuiGraphics g, Font font, int x, int y, int mouseX, int mouseY) {
        boolean[] states = {
            this.showChunkGrid, this.showRegionGrid, this.showHighlights,
            this.showStructures, this.showPlayers, this.showWaypoints, this.showDrawings,
            this.showEntities, this.caveView
        };

        this.hoveredToggleIndex = -1;

        int[] visible = getVisibleToggles();
        for (int slot = 0; slot < visible.length; slot++) {
            int i = visible[slot];
            int btnY = y + slot * (TOGGLE_SIZE + TOGGLE_GAP);
            boolean active = states[i];
            boolean hover = mouseX >= x && mouseX < x + TOGGLE_SIZE
                    && mouseY >= btnY && mouseY < btnY + TOGGLE_SIZE;

            if (hover) {
                this.hoveredToggleIndex = i;
                this.hoveredToggleY = btnY;
            }

            // Background
            int bgColor = active ? 0xFF1E3050 : 0xFF141428;
            int borderColor = active ? 0xFF58A6FF : 0xFF333344;
            g.fill(x, btnY, x + TOGGLE_SIZE, btnY + TOGGLE_SIZE, bgColor);
            // Border
            g.fill(x, btnY, x + TOGGLE_SIZE, btnY + 1, borderColor);
            g.fill(x, btnY + TOGGLE_SIZE - 1, x + TOGGLE_SIZE, btnY + TOGGLE_SIZE, borderColor);
            g.fill(x, btnY, x + 1, btnY + TOGGLE_SIZE, borderColor);
            g.fill(x + TOGGLE_SIZE - 1, btnY, x + TOGGLE_SIZE, btnY + TOGGLE_SIZE, borderColor);

            if (hover) {
                g.fill(x + 1, btnY + 1, x + TOGGLE_SIZE - 1, btnY + TOGGLE_SIZE - 1, 0x22FFFFFF);
            }

            // Label
            String label = TOGGLE_LABELS[i];
            int labelW = font.width(label);
            int textColor = active ? 0xFF58A6FF : 0xFF666688;
            g.drawString(font, label, x + (TOGGLE_SIZE - labelW) / 2, btnY + 3, textColor, false);
        }

        // Draw tooltip last so it renders on top of everything
        if (this.hoveredToggleIndex >= 0) {
            String tooltip = TOGGLE_TOOLTIPS[this.hoveredToggleIndex];
            boolean active = states[this.hoveredToggleIndex];
            String status = active ? "ON" : "OFF";
            String fullTooltip = tooltip + " [" + status + "]";
            int tipW = font.width(fullTooltip);
            int tipX = x + TOGGLE_SIZE + 4;
            int tipY = this.hoveredToggleY + 1;
            g.fill(tipX - 2, tipY - 2, tipX + tipW + 3, tipY + 11, 0xEE111122);
            g.fill(tipX - 2, tipY - 2, tipX + tipW + 3, tipY - 1, 0xFF333355);
            g.fill(tipX - 2, tipY + 10, tipX + tipW + 3, tipY + 11, 0xFF333355);
            g.fill(tipX - 2, tipY - 1, tipX - 1, tipY + 10, 0xFF333355);
            g.fill(tipX + tipW + 2, tipY - 1, tipX + tipW + 3, tipY + 10, 0xFF333355);
            int tipColor = active ? 0xFF58A6FF : 0xFFAAAAAA;
            g.drawString(font, fullTooltip, tipX, tipY, tipColor, false);
        }
    }

    /**
     * Handle a click on one of the toggle buttons.
     * Returns true if a toggle was clicked.
     */
    public boolean handleToggleClick(int mouseX, int mouseY, int x, int y) {
        int[] visible = getVisibleToggles();
        for (int slot = 0; slot < visible.length; slot++) {
            int btnY = y + slot * (TOGGLE_SIZE + TOGGLE_GAP);
            if (mouseX >= x && mouseX < x + TOGGLE_SIZE
                    && mouseY >= btnY && mouseY < btnY + TOGGLE_SIZE) {
                int i = visible[slot];
                switch (i) {
                    case 0 -> this.showChunkGrid = !this.showChunkGrid;
                    case 1 -> this.showRegionGrid = !this.showRegionGrid;
                    case 2 -> this.showHighlights = !this.showHighlights;
                    case 3 -> this.showStructures = !this.showStructures;
                    case 4 -> this.showPlayers = !this.showPlayers;
                    case 5 -> this.showWaypoints = !this.showWaypoints;
                    case 6 -> this.showDrawings = !this.showDrawings;
                    case 7 -> this.showEntities = !this.showEntities;
                    case 8 -> this.caveView = !this.caveView;
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Get the total height of the toggle column.
     */
    public int getToggleColumnHeight() {
        return getVisibleToggles().length * (TOGGLE_SIZE + TOGGLE_GAP) - TOGGLE_GAP;
    }

    /**
     * Get the toggle column width.
     */
    public int getToggleColumnWidth() {
        return TOGGLE_SIZE;
    }

    /**
     * Check if a named layer is enabled.
     */
    public boolean isLayerEnabled(String layerName) {
        return switch (layerName) {
            case "chunkGrid" -> this.showChunkGrid;
            case "regionGrid" -> this.showRegionGrid;
            case "highlights" -> this.showHighlights;
            case "structures" -> this.showStructures;
            case "players" -> this.showPlayers;
            case "waypoints" -> this.showWaypoints;
            case "drawings" -> this.showDrawings;
            case "entities" -> this.showEntities;
            case "caveView" -> this.caveView;
            default -> true;
        };
    }

    /**
     * Render region grid overlay (512-block boundaries).
     */
    public static void renderRegionGrid(GuiGraphics g, Font font, int mapLeft, int mapTop,
            int mapWidth, int mapHeight, double centerWorldX, double centerWorldZ, double zoomScale) {
        int halfMapW = mapWidth / 2;
        int halfMapH = mapHeight / 2;
        int regionSize = 512;
        int lineColor = 0x30FFAA44;

        // Vertical region lines
        int startWorldX = (int) (centerWorldX - halfMapW * zoomScale);
        int firstRegionX = (startWorldX / regionSize) * regionSize;
        if (startWorldX < 0 && startWorldX % regionSize != 0) firstRegionX -= regionSize;
        for (int rx = firstRegionX; ; rx += regionSize) {
            int screenX = (int) (mapLeft + halfMapW + (rx - centerWorldX) / zoomScale);
            if (screenX > mapLeft + mapWidth) break;
            if (screenX >= mapLeft && screenX < mapLeft + mapWidth) {
                // Thicker line (2px)
                g.fill(screenX, mapTop, screenX + 1, mapTop + mapHeight, lineColor);
                if (screenX + 1 < mapLeft + mapWidth) {
                    g.fill(screenX + 1, mapTop, screenX + 2, mapTop + mapHeight, lineColor);
                }
                // Label at top
                int regionCoord = rx / regionSize;
                String label = "r." + regionCoord;
                int labelW = font.width(label);
                if (screenX + 3 + labelW < mapLeft + mapWidth) {
                    g.fill(screenX + 2, mapTop + 1, screenX + 4 + labelW, mapTop + 11, 0xAA000000);
                    g.drawString(font, label, screenX + 3, mapTop + 2, 0xFFFFAA44, false);
                }
            }
        }

        // Horizontal region lines
        int startWorldZ = (int) (centerWorldZ - halfMapH * zoomScale);
        int firstRegionZ = (startWorldZ / regionSize) * regionSize;
        if (startWorldZ < 0 && startWorldZ % regionSize != 0) firstRegionZ -= regionSize;
        for (int rz = firstRegionZ; ; rz += regionSize) {
            int screenY = (int) (mapTop + halfMapH + (rz - centerWorldZ) / zoomScale);
            if (screenY > mapTop + mapHeight) break;
            if (screenY >= mapTop && screenY < mapTop + mapHeight) {
                g.fill(mapLeft, screenY, mapLeft + mapWidth, screenY + 1, lineColor);
                if (screenY + 1 < mapTop + mapHeight) {
                    g.fill(mapLeft, screenY + 1, mapLeft + mapWidth, screenY + 2, lineColor);
                }
                // Label on left side
                int regionCoord = rz / regionSize;
                String label = "r." + regionCoord;
                g.fill(mapLeft + 1, screenY + 2, mapLeft + 3 + font.width(label), screenY + 12, 0xAA000000);
                g.drawString(font, label, mapLeft + 2, screenY + 3, 0xFFFFAA44, false);
            }
        }
    }
}

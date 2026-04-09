package com.ultra.megamod.feature.casino.chips;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Client-side utility for rendering casino chips and managing drag state.
 *
 * Usage in game screens:
 *   1. Call renderChipTray() to draw the selectable chip denominations
 *   2. Call renderDraggedChip() after all other rendering to show chip on cursor
 *   3. Call handleTrayClick() on mouse down to start dragging
 *   4. Call handleDrop() on mouse up to place the chip
 */
public class ChipRenderer {

    private static final int CHIP_SIZE = 18;    // diameter in pixels
    private static final int CHIP_GAP = 3;
    private static final int TRAY_PAD = 4;

    // Drag state (shared across screens — only one drag at a time)
    private static ChipDenomination draggedChip = null;
    private static boolean isDragging = false;

    // Client-synced chip counts (set by network sync)
    public static int[] clientChips = new int[ChipDenomination.values().length];
    public static int clientChipTotal = 0;

    /** Clear drag state (call on screen close). */
    public static void clearDrag() {
        draggedChip = null;
        isDragging = false;
    }

    /** Is a chip currently being dragged? */
    public static boolean isDragging() { return isDragging && draggedChip != null; }

    /** Get the currently dragged chip denomination. */
    public static ChipDenomination getDraggedChip() { return draggedChip; }

    /**
     * Render the chip tray at the given position.
     * Shows each denomination as a clickable chip with count below.
     * Returns the total tray height.
     */
    public static int renderChipTray(GuiGraphics g, Font font, int x, int y, int maxWidth, int mouseX, int mouseY) {
        ChipDenomination[] denoms = ChipDenomination.values();
        int chipsPerRow = Math.min(denoms.length, (maxWidth - TRAY_PAD * 2) / (CHIP_SIZE + CHIP_GAP));
        if (chipsPerRow <= 0) chipsPerRow = 5;

        // Background
        int rows = (denoms.length + chipsPerRow - 1) / chipsPerRow;
        int trayH = TRAY_PAD * 2 + rows * (CHIP_SIZE + 12 + CHIP_GAP);
        g.fill(x, y, x + maxWidth, y + trayH, 0xCC0A0A14);
        g.fill(x, y, x + maxWidth, y + 1, 0xFFD4AF37);

        // Label
        g.drawString(font, "Chips", x + TRAY_PAD, y + 2, 0xFFD4AF37, false);
        String totalStr = clientChipTotal + " MC";
        g.drawString(font, totalStr, x + maxWidth - font.width(totalStr) - TRAY_PAD, y + 2, 0xFF888899, false);

        int startY = y + 12;
        for (int i = 0; i < denoms.length; i++) {
            ChipDenomination denom = denoms[i];
            int col = i % chipsPerRow;
            int row = i / chipsPerRow;
            int cx = x + TRAY_PAD + col * (CHIP_SIZE + CHIP_GAP) + CHIP_SIZE / 2;
            int cy = startY + row * (CHIP_SIZE + 12 + CHIP_GAP) + CHIP_SIZE / 2;

            int count = i < clientChips.length ? clientChips[i] : 0;
            boolean hovered = mouseX >= cx - CHIP_SIZE / 2 && mouseX < cx + CHIP_SIZE / 2
                    && mouseY >= cy - CHIP_SIZE / 2 && mouseY < cy + CHIP_SIZE / 2;
            boolean canUse = count > 0;

            renderChip(g, font, cx, cy, denom, hovered && canUse, !canUse);

            // Count below chip
            String countStr = String.valueOf(count);
            int cw = font.width(countStr);
            g.drawString(font, countStr, cx - cw / 2, cy + CHIP_SIZE / 2 + 2,
                    canUse ? 0xFFCCCCDD : 0xFF555555, false);
        }

        return trayH;
    }

    /**
     * Render a single chip at center position (cx, cy).
     */
    public static void renderChip(GuiGraphics g, Font font, int cx, int cy, ChipDenomination denom,
                                    boolean hovered, boolean dimmed) {
        int r = CHIP_SIZE / 2;
        int mainColor = dimmed ? darken(denom.color) : denom.color;
        int borderCol = dimmed ? darken(denom.borderColor) : denom.borderColor;

        if (hovered) {
            // Glow
            fillCircle(g, cx, cy, r + 2, 0x44FFFFFF);
        }

        // Outer ring
        fillCircle(g, cx, cy, r, borderCol);
        // Inner fill
        fillCircle(g, cx, cy, r - 2, mainColor);
        // Inner ring detail
        drawRing(g, cx, cy, r - 4, dimmed ? 0x22FFFFFF : 0x44FFFFFF);

        // Label
        String label = denom.label;
        int lw = font.width(label);
        int textColor = denom == ChipDenomination.CHIP_100 ? 0xFFFFFFFF : 0xFF000000;
        if (dimmed) textColor = 0xFF666666;
        g.drawString(font, label, cx - lw / 2, cy - 4, textColor, false);
    }

    /**
     * Render the dragged chip following the mouse cursor.
     * Call this LAST in render() so it draws on top of everything.
     */
    public static void renderDraggedChip(GuiGraphics g, Font font, int mouseX, int mouseY) {
        if (!isDragging || draggedChip == null) return;
        // Render chip centered on cursor, slightly transparent
        renderChip(g, font, mouseX, mouseY, draggedChip, false, false);
        // Shadow
        g.fill(mouseX - CHIP_SIZE / 2 + 2, mouseY + CHIP_SIZE / 2, mouseX + CHIP_SIZE / 2 + 2, mouseY + CHIP_SIZE / 2 + 2, 0x44000000);
    }

    /**
     * Handle mouse press on the chip tray. Returns true if a chip was picked up.
     */
    public static boolean handleTrayClick(int mouseX, int mouseY, int trayX, int trayY, int maxWidth) {
        ChipDenomination[] denoms = ChipDenomination.values();
        int chipsPerRow = Math.min(denoms.length, (maxWidth - TRAY_PAD * 2) / (CHIP_SIZE + CHIP_GAP));
        if (chipsPerRow <= 0) chipsPerRow = 5;

        int startY = trayY + 12;
        for (int i = 0; i < denoms.length; i++) {
            int count = i < clientChips.length ? clientChips[i] : 0;
            if (count <= 0) continue;

            int col = i % chipsPerRow;
            int row = i / chipsPerRow;
            int cx = trayX + TRAY_PAD + col * (CHIP_SIZE + CHIP_GAP) + CHIP_SIZE / 2;
            int cy = startY + row * (CHIP_SIZE + 12 + CHIP_GAP) + CHIP_SIZE / 2;

            if (mouseX >= cx - CHIP_SIZE / 2 && mouseX < cx + CHIP_SIZE / 2
                    && mouseY >= cy - CHIP_SIZE / 2 && mouseY < cy + CHIP_SIZE / 2) {
                draggedChip = denoms[i];
                isDragging = true;
                return true;
            }
        }
        return false;
    }

    /**
     * Cancel the current drag (right-click or ESC).
     */
    public static void cancelDrag() {
        draggedChip = null;
        isDragging = false;
    }

    /**
     * Complete a drop. Returns the denomination that was dropped, or null if not dragging.
     * Caller is responsible for validating the drop zone and sending the bet to the server.
     */
    public static ChipDenomination completeDrop() {
        if (!isDragging || draggedChip == null) return null;
        ChipDenomination dropped = draggedChip;
        draggedChip = null;
        isDragging = false;
        return dropped;
    }

    /**
     * Render a stack of chips representing a total bet amount.
     * Shows 2-5 chips stacked with slight offset for a 3D pile effect.
     * Uses ChipDenomination.breakdown() to pick realistic chip colors.
     */
    public static void renderChipStack(GuiGraphics g, Font font, int centerX, int bottomY, int totalAmount) {
        if (totalAmount <= 0) return;

        int[] counts = ChipDenomination.breakdown(totalAmount);
        ChipDenomination[] denoms = ChipDenomination.values();

        // Collect the significant chips to display (up to 5, highest denomination at top)
        java.util.List<ChipDenomination> stackChips = new java.util.ArrayList<>();
        for (int i = denoms.length - 1; i >= 0 && stackChips.size() < 5; i--) {
            int show = Math.min(counts[i], 5 - stackChips.size());
            for (int j = 0; j < show; j++) {
                stackChips.add(denoms[i]);
            }
        }
        // Ensure at least 2 chips for visual effect
        if (stackChips.size() < 2 && !stackChips.isEmpty()) {
            stackChips.add(stackChips.get(0));
        }

        // Reverse so lowest denomination draws first (bottom of stack)
        java.util.Collections.reverse(stackChips);

        int chipRadius = 7;
        int stackOffset = 3; // vertical offset per chip

        // Draw each chip from bottom to top
        for (int i = 0; i < stackChips.size(); i++) {
            ChipDenomination denom = stackChips.get(i);
            int cy = bottomY - i * stackOffset;
            int cx = centerX;

            // Shadow under each chip
            fillCircle(g, cx + 1, cy + 1, chipRadius, 0x33000000);
            // Outer ring
            fillCircle(g, cx, cy, chipRadius, denom.borderColor);
            // Inner fill
            fillCircle(g, cx, cy, chipRadius - 1, denom.color);
            // Inner ring detail
            drawRing(g, cx, cy, chipRadius - 3, 0x44FFFFFF);

            // Label on top chip only
            if (i == stackChips.size() - 1) {
                String label = denom.label;
                int lw = font.width(label);
                int textColor = denom == ChipDenomination.CHIP_100 ? 0xFFFFFFFF : 0xFF000000;
                g.drawString(font, label, cx - lw / 2, cy - 4, textColor, false);
            }
        }

        // Total amount text below the stack
        String amountStr = totalAmount + " MC";
        int aw = font.width(amountStr);
        g.drawString(font, amountStr, centerX - aw / 2, bottomY + chipRadius + 2, 0xFFD4AF37, false);
    }

    // --- Drawing helpers ---

    private static void fillCircle(GuiGraphics g, int cx, int cy, int r, int color) {
        for (int dy = -r; dy <= r; dy++) {
            int dx = (int) Math.sqrt(r * r - dy * dy);
            g.fill(cx - dx, cy + dy, cx + dx, cy + dy + 1, color);
        }
    }

    private static void drawRing(GuiGraphics g, int cx, int cy, int r, int color) {
        int steps = Math.max(16, r * 4);
        for (int i = 0; i < steps; i++) {
            double angle = Math.PI * 2 * i / steps;
            int px = cx + (int)(Math.cos(angle) * r);
            int py = cy + (int)(Math.sin(angle) * r);
            g.fill(px, py, px + 1, py + 1, color);
        }
    }

    private static int darken(int color) {
        int a = (color >> 24) & 0xFF;
        int r = Math.max(0, ((color >> 16) & 0xFF) / 2);
        int gr = Math.max(0, ((color >> 8) & 0xFF) / 2);
        int b = Math.max(0, (color & 0xFF) / 2);
        return (a << 24) | (r << 16) | (gr << 8) | b;
    }
}

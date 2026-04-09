package com.ultra.megamod.feature.citizen.block;

import com.ultra.megamod.feature.ui.UIHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * Scrollable Town Chest screen — renders 6 visible rows with a scrollbar.
 * Uses proxy slots in TownChestMenu that swap their backing data on scroll,
 * so AbstractContainerScreen handles all slot rendering and interaction normally.
 */
public class TownChestScreen extends AbstractContainerScreen<TownChestMenu> {

    private static final int GOLD = 0xFFFFD700;
    private static final int TEXT = 0xFFCCCCDD;
    private static final int DIM = 0xFF888899;
    private static final int BG = 0xFF0E0E18;
    private static final int PANEL_BG = 0xFF141420;
    private static final int SLOT_BG = 0xFF1C1C2C;
    private static final int SLOT_BORDER = 0xFF2A2A3A;
    private static final int SCROLLBAR_BG = 0xFF0A0A14;
    private static final int SCROLLBAR_THUMB = 0xFF555577;

    private boolean isDraggingScrollbar = false;

    public TownChestScreen(TownChestMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageWidth = 176;
        this.imageHeight = 18 + TownChestMenu.VISIBLE_ROWS * 18 + 14 + 76;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        this.titleLabelY = 6;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        int w = this.imageWidth;
        int h = this.imageHeight;

        // Full background
        g.fill(x - 4, y - 4, x + w + 4, y + h + 4, BG);
        UIHelper.drawPanel(g, x - 2, y - 2, w + 4, h + 4);

        int chestAreaH = TownChestMenu.VISIBLE_ROWS * 18;
        // Chest slots start at Y=18 in the menu
        int chestTop = y + 18;

        // Chest area background
        g.fill(x + 6, chestTop - 2, x + w - 12, chestTop + chestAreaH + 2, PANEL_BG);

        // Draw slot backgrounds for visible chest area (match menu slot positions)
        for (int row = 0; row < TownChestMenu.VISIBLE_ROWS; row++) {
            for (int col = 0; col < TownChestMenu.COLS; col++) {
                int sx = x + 7 + col * 18;
                int sy = chestTop + row * 18;
                g.fill(sx, sy, sx + 17, sy + 17, SLOT_BORDER);
                g.fill(sx + 1, sy + 1, sx + 16, sy + 16, SLOT_BG);
            }
        }

        // Player inventory area (slots at Y = 18 + VISIBLE_ROWS*18 + 14 = 140)
        int playerInvY = 18 + TownChestMenu.VISIBLE_ROWS * 18 + 14;
        g.fill(x + 6, y + playerInvY - 2, x + w - 6, y + playerInvY + 56, PANEL_BG);
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int sx = x + 7 + col * 18;
                int sy = y + playerInvY + row * 18;
                g.fill(sx, sy, sx + 17, sy + 17, SLOT_BORDER);
                g.fill(sx + 1, sy + 1, sx + 16, sy + 16, SLOT_BG);
            }
        }

        // Hotbar area (slots at Y = playerInvY + 58 = 198)
        int hotbarY = playerInvY + 58;
        g.fill(x + 6, y + hotbarY - 2, x + w - 6, y + hotbarY + 18, PANEL_BG);
        for (int col = 0; col < 9; col++) {
            int sx = x + 7 + col * 18;
            g.fill(sx, y + hotbarY, sx + 17, y + hotbarY + 17, SLOT_BORDER);
            g.fill(sx + 1, y + hotbarY + 1, sx + 16, y + hotbarY + 16, SLOT_BG);
        }

        // Scrollbar
        int sbX = x + w - 10;
        int sbY = chestTop + 1;
        int sbH = chestAreaH;
        g.fill(sbX, sbY, sbX + 6, sbY + sbH, SCROLLBAR_BG);
        int maxRow = this.menu.getMaxScrollRow();
        if (maxRow > 0) {
            int thumbH = Math.max(10, sbH * TownChestMenu.VISIBLE_ROWS / TownChestMenu.TOTAL_ROWS);
            int thumbY = sbY + (int) ((float) this.menu.getScrollRow() / maxRow * (sbH - thumbH));
            g.fill(sbX, thumbY, sbX + 6, thumbY + thumbH, SCROLLBAR_THUMB);
        }

        // Row indicator (below scrollbar, right-aligned)
        int curRow = this.menu.getScrollRow() + 1;
        String info = curRow + "-" + Math.min(curRow + TownChestMenu.VISIBLE_ROWS - 1, TownChestMenu.TOTAL_ROWS)
                + "/" + TownChestMenu.TOTAL_ROWS;
        g.drawString(this.font, info, sbX + 6 - this.font.width(info), chestTop + chestAreaH + 3, DIM, false);

        // Slot count
        int used = this.menu.getUsedSlotCount();
        g.drawString(this.font, used + "/" + TownChestMenu.CHEST_SIZE, x + 8, y + h - 10, DIM, false);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        this.renderTooltip(g, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, GOLD, false);
        g.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, TEXT, false);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int newRow = this.menu.getScrollRow() - (int) scrollY;
        this.menu.setScrollRow(newRow);
        return true;
    }

    // Scrollbar drag handling - these use the old signature without @Override
    // They don't call super to avoid the signature mismatch with 1.21.11
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int sbX = this.leftPos + this.imageWidth - 10;
        int sbY = this.topPos + 17;
        int sbH = TownChestMenu.VISIBLE_ROWS * 18;
        if (mouseX >= sbX && mouseX <= sbX + 6 && mouseY >= sbY && mouseY <= sbY + sbH) {
            isDraggingScrollbar = true;
            scrollFromMouse(mouseY, sbY, sbH);
            return true;
        }
        return false; // let AbstractContainerScreen handle slot clicks
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isDraggingScrollbar) {
            int sbY = this.topPos + 17;
            int sbH = TownChestMenu.VISIBLE_ROWS * 18;
            scrollFromMouse(mouseY, sbY, sbH);
            return true;
        }
        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        isDraggingScrollbar = false;
        return false;
    }

    private void scrollFromMouse(double mouseY, int sbY, int sbH) {
        float pct = (float) (mouseY - sbY) / sbH;
        pct = Math.max(0, Math.min(1, pct));
        this.menu.setScrollRow((int) (pct * this.menu.getMaxScrollRow()));
    }
}

package com.ultra.megamod.feature.alchemy.block;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * Screen for the Alchemy Shelf — double chest style layout (6 rows of 9).
 * Custom drawn UI matching MegaMod's dark theme.
 */
public class AlchemyShelfScreen extends AbstractContainerScreen<AlchemyShelfMenu> {

    private static final int GOLD = 0xFFFFD700;
    private static final int TEXT = 0xFFCCCCDD;
    private static final int BG = 0xFF0E0E18;
    private static final int PANEL_BG = 0xFF141420;
    private static final int SLOT_BG = 0xFF1C1C2C;
    private static final int SLOT_BORDER = 0xFF2A2A3A;
    private static final int ACCENT = 0xFF6B3FA0;

    public AlchemyShelfScreen(AlchemyShelfMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageWidth = 176;
        this.imageHeight = 18 + AlchemyShelfMenu.ROWS * 18 + 14 + 76; // title + shelf rows + gap + player inv
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

        // Outer border
        g.fill(x - 4, y - 4, x + w + 4, y + h + 4, BG);
        // Accent border
        g.fill(x - 3, y - 3, x + w + 3, y + h + 3, ACCENT);
        // Inner background
        g.fill(x - 2, y - 2, x + w + 2, y + h + 2, PANEL_BG);

        // Shelf slots area
        int shelfStartY = y + 18;
        int shelfEndY = shelfStartY + AlchemyShelfMenu.ROWS * 18;
        g.fill(x + 6, shelfStartY - 2, x + w - 6, shelfEndY + 2, 0xFF101018);

        // Draw individual shelf slots
        for (int row = 0; row < AlchemyShelfMenu.ROWS; row++) {
            for (int col = 0; col < AlchemyShelfMenu.COLS; col++) {
                int sx = x + 7 + col * 18;
                int sy = shelfStartY - 1 + row * 18;
                g.fill(sx, sy, sx + 18, sy + 18, SLOT_BORDER);
                g.fill(sx + 1, sy + 1, sx + 17, sy + 17, SLOT_BG);
            }
        }

        // Player inventory area
        int playerInvY = y + this.inventoryLabelY + 11;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int sx = x + 7 + col * 18;
                int sy = playerInvY - 1 + row * 18;
                g.fill(sx, sy, sx + 18, sy + 18, SLOT_BORDER);
                g.fill(sx + 1, sy + 1, sx + 17, sy + 17, SLOT_BG);
            }
        }

        // Hotbar
        int hotbarY = playerInvY + 58;
        for (int col = 0; col < 9; col++) {
            int sx = x + 7 + col * 18;
            int sy = hotbarY - 1;
            g.fill(sx, sy, sx + 18, sy + 18, SLOT_BORDER);
            g.fill(sx + 1, sy + 1, sx + 17, sy + 17, SLOT_BG);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, GOLD, false);
        g.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, TEXT, false);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        this.renderTooltip(g, mouseX, mouseY);
    }
}

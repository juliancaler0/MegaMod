package com.ultra.megamod.feature.casino.chips;

import com.ultra.megamod.feature.economy.network.PlayerInfoSyncPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

/**
 * Cashier screen — exchange wallet MegaCoins for casino chips and vice versa.
 * Supports buying/selling 1, 5, 10, or max at a time.
 */
public class CashierScreen extends Screen {

    private static final int PANEL_W = 280;
    private static final int PANEL_H = 300;
    private static final int GOLD = 0xFFD4AF37;
    private static final int TEXT = 0xFFCCCCDD;
    private static final int DIM = 0xFF888899;
    private static final int BG = 0xEE0D1117;
    private static final int BORDER = 0xFF30363D;
    private static final int BUY_COLOR = 0xFF2E7D32;
    private static final int SELL_COLOR = 0xFFCC4444;

    // Buy quantity: 1, 5, 10
    private int buyQty = 1;

    public CashierScreen() {
        super(Component.literal("Casino Cashier"));
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        int px = (this.width - PANEL_W) / 2;
        int py = (this.height - PANEL_H) / 2;

        // Dim background
        g.fill(0, 0, this.width, this.height, 0x88000000);

        // Panel
        g.fill(px, py, px + PANEL_W, py + PANEL_H, BG);
        g.fill(px, py, px + PANEL_W, py + 1, GOLD);
        g.fill(px, py + PANEL_H - 1, px + PANEL_W, py + PANEL_H, GOLD);
        g.fill(px, py, px + 1, py + PANEL_H, GOLD);
        g.fill(px + PANEL_W - 1, py, px + PANEL_W, py + PANEL_H, GOLD);

        int y = py + 8;

        // Title
        String title = "CASINO CASHIER";
        g.drawString(this.font, title, px + (PANEL_W - this.font.width(title)) / 2, y, GOLD, false);
        y += 14;

        // Wallet balance
        int wallet = PlayerInfoSyncPayload.clientWallet;
        String walletStr = "Wallet: " + wallet + " MC";
        g.drawString(this.font, walletStr, px + 8, y, TEXT, false);

        // Chip total
        String chipStr = "Chips: " + ChipRenderer.clientChipTotal + " MC";
        int chipW = this.font.width(chipStr);
        g.drawString(this.font, chipStr, px + PANEL_W - chipW - 8, y, GOLD, false);
        y += 14;

        // Quantity selector row
        String qtyLabel = "Quantity:";
        g.drawString(this.font, qtyLabel, px + 8, y + 2, DIM, false);
        int qx = px + 70;
        int[] qtys = {1, 5, 10};
        for (int q : qtys) {
            boolean selected = buyQty == q;
            int qw = 28;
            int qh = 14;
            boolean qHov = mouseX >= qx && mouseX < qx + qw && mouseY >= y && mouseY < y + qh;
            g.fill(qx, y, qx + qw, y + qh, selected ? GOLD : (qHov ? 0xFF444455 : 0xFF333344));
            String qs = "x" + q;
            g.drawString(this.font, qs, qx + (qw - this.font.width(qs)) / 2, y + 3,
                    selected ? 0xFF000000 : 0xFFCCCCCC, false);
            qx += qw + 4;
        }
        y += 18;

        // Divider
        g.fill(px + 8, y, px + PANEL_W - 8, y + 1, BORDER);
        y += 6;

        // Chip rows
        ChipDenomination[] denoms = ChipDenomination.values();
        for (int i = 0; i < denoms.length; i++) {
            ChipDenomination d = denoms[i];
            int count = i < ChipRenderer.clientChips.length ? ChipRenderer.clientChips[i] : 0;

            // Chip icon
            ChipRenderer.renderChip(g, this.font, px + 14, y + 9, d, false, false);

            // Value label
            g.drawString(this.font, "$" + d.value, px + 30, y + 4, TEXT, false);

            // Count
            String cStr = "x" + count;
            g.drawString(this.font, cStr, px + 68, y + 4, count > 0 ? TEXT : DIM, false);

            int totalCost = d.value * buyQty;

            // Buy button
            int buyX = px + PANEL_W - 130;
            int buyW = 55;
            int btnH = 14;
            boolean canBuy = wallet >= totalCost;
            boolean buyHov = mouseX >= buyX && mouseX < buyX + buyW && mouseY >= y + 2 && mouseY < y + 2 + btnH;
            g.fill(buyX, y + 2, buyX + buyW, y + 2 + btnH, canBuy ? (buyHov ? 0xFF3E8E42 : BUY_COLOR) : 0xFF333333);
            String buyLabel = "Buy " + buyQty;
            g.drawString(this.font, buyLabel, buyX + (buyW - this.font.width(buyLabel)) / 2, y + 4, 0xFFFFFFFF, false);

            // Sell button
            int sellX = buyX + buyW + 4;
            int sellW = 55;
            int sellQty = Math.min(buyQty, count);
            boolean canSell = count > 0;
            boolean sellHov = mouseX >= sellX && mouseX < sellX + sellW && mouseY >= y + 2 && mouseY < y + 2 + btnH;
            g.fill(sellX, y + 2, sellX + sellW, y + 2 + btnH, canSell ? (sellHov ? 0xFFDD5555 : SELL_COLOR) : 0xFF333333);
            String sellLabel = "Sell " + (canSell ? sellQty : buyQty);
            g.drawString(this.font, sellLabel, sellX + (sellW - this.font.width(sellLabel)) / 2, y + 4, 0xFFFFFFFF, false);

            y += 22;
        }

        y += 4;
        // Cash Out All button
        int cashX = px + (PANEL_W - 120) / 2;
        int cashW = 120;
        int cashH = 18;
        boolean cashHov = mouseX >= cashX && mouseX < cashX + cashW && mouseY >= y && mouseY < y + cashH;
        boolean hasCash = ChipRenderer.clientChipTotal > 0;
        g.fill(cashX, y, cashX + cashW, y + cashH, hasCash ? (cashHov ? 0xFFE6A800 : GOLD) : 0xFF333333);
        String cashLabel = "Cash Out All";
        g.drawString(this.font, cashLabel, cashX + (cashW - this.font.width(cashLabel)) / 2, y + (cashH - 9) / 2,
                hasCash ? 0xFF000000 : 0xFF666666, false);

        // ESC hint
        g.drawString(this.font, "ESC to close", px + (PANEL_W - this.font.width("ESC to close")) / 2, py + PANEL_H - 14, DIM, false);

        super.render(g, mouseX, mouseY, partialTick);
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean consumed) {
        if (consumed) return super.mouseClicked(event, consumed);
        int mx = (int) event.x();
        int my = (int) event.y();

        int px = (this.width - PANEL_W) / 2;
        int py = (this.height - PANEL_H) / 2;
        int wallet = PlayerInfoSyncPayload.clientWallet;

        int y = py + 8 + 14 + 14; // skip title, wallet line

        // Quantity selector buttons
        int qx = px + 70;
        int[] qtys = {1, 5, 10};
        for (int q : qtys) {
            int qw = 28;
            int qh = 14;
            if (mx >= qx && mx < qx + qw && my >= y && my < y + qh) {
                buyQty = q;
                return true;
            }
            qx += qw + 4;
        }
        y += 18 + 7; // qty row + divider

        ChipDenomination[] denoms = ChipDenomination.values();
        for (int i = 0; i < denoms.length; i++) {
            ChipDenomination d = denoms[i];
            int count = i < ChipRenderer.clientChips.length ? ChipRenderer.clientChips[i] : 0;
            int totalCost = d.value * buyQty;

            // Buy button
            int buyX = px + PANEL_W - 130;
            int buyW = 55;
            int btnH = 14;
            if (mx >= buyX && mx < buyX + buyW && my >= y + 2 && my < y + 2 + btnH) {
                if (wallet >= totalCost) {
                    ClientPacketDistributor.sendToServer(
                            new ChipActionPayload("buy", d.value, buyQty),
                            new CustomPacketPayload[0]);
                }
                return true;
            }

            // Sell button
            int sellX = buyX + buyW + 4;
            int sellW = 55;
            if (mx >= sellX && mx < sellX + sellW && my >= y + 2 && my < y + 2 + btnH) {
                if (count > 0) {
                    int sellQty = Math.min(buyQty, count);
                    ClientPacketDistributor.sendToServer(
                            new ChipActionPayload("sell", d.value, sellQty),
                            new CustomPacketPayload[0]);
                }
                return true;
            }

            y += 22;
        }

        y += 4;
        // Cash Out All
        int cashX = px + (PANEL_W - 120) / 2;
        int cashW = 120;
        int cashH = 18;
        if (mx >= cashX && mx < cashX + cashW && my >= y && my < y + cashH) {
            if (ChipRenderer.clientChipTotal > 0) {
                ClientPacketDistributor.sendToServer(
                        new ChipActionPayload("cashout", 0, 0),
                        new CustomPacketPayload[0]);
            }
            return true;
        }

        return super.mouseClicked(event, consumed);
    }

    public boolean keyPressed(KeyEvent event) {
        if (event.key() == 256) { this.onClose(); return true; }
        return super.keyPressed(event);
    }

    @Override
    public void onClose() {
        ChipRenderer.clearDrag();
        ChipSyncPayload.shouldOpenCashier = false;
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() { return false; }
}

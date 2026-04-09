/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.input.MouseButtonEvent
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.neoforged.neoforge.client.network.ClientPacketDistributor
 */
package com.ultra.megamod.feature.economy.screen;

import com.ultra.megamod.feature.computer.network.ComputerActionPayload;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.ui.UIHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class BankScreen
extends Screen {
    private final Screen parent;
    private int wallet;
    private int bank;
    private String statusMessage = "";
    private int statusTicks = 0;
    private static final int MARGIN = 10;
    private int titleBarH;
    private static final int[] AMOUNTS = new int[]{1, 10, 100, -1};
    private static final String[] AMOUNT_LABELS = new String[]{"+1", "+10", "+100", "ALL"};
    private final List<ClickRect> clickRects = new ArrayList<ClickRect>();

    public BankScreen(Screen parent, int wallet, int bank) {
        super((Component)Component.literal((String)"MegaBank"));
        this.parent = parent;
        this.wallet = wallet;
        this.bank = bank;
    }

    protected void init() {
        super.init();
        Objects.requireNonNull(this.font);
        this.titleBarH = 9 + 16;
    }

    public void tick() {
        ComputerDataPayload response;
        super.tick();
        if (this.statusTicks > 0) {
            --this.statusTicks;
        }
        if ((response = ComputerDataPayload.lastResponse) != null && "transfer_result".equals(response.dataType())) {
            ComputerDataPayload.lastResponse = null;
            this.wallet = response.wallet();
            this.bank = response.bank();
            boolean success = response.jsonData().contains("true");
            this.statusMessage = success ? "Transfer complete!" : "Insufficient funds!";
            this.statusTicks = 60;
        } else if (response != null && "error".equals(response.dataType())) {
            ComputerDataPayload.lastResponse = null;
            this.statusMessage = "Server error!";
            this.statusTicks = 60;
        }
    }

    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.clickRects.clear();
        g.fill(0, 0, this.width, this.height, 0xFF0E0E18);
        UIHelper.drawPanel(g, 0, 0, this.width, this.height);
        UIHelper.drawTitleBar(g, 0, 0, this.width, this.titleBarH);
        Objects.requireNonNull(this.font);
        int titleY = (this.titleBarH - 9) / 2;
        UIHelper.drawCenteredTitle(g, this.font, "MegaBank", this.width / 2, titleY);
        int backW = 50;
        int backH = 16;
        int backX = 8;
        int backY = (this.titleBarH - backH) / 2;
        boolean backHover = mouseX >= backX && mouseX < backX + backW && mouseY >= backY && mouseY < backY + backH;
        UIHelper.drawButton(g, backX, backY, backW, backH, backHover);
        int backTextX = backX + (backW - this.font.width("< Back")) / 2;
        Objects.requireNonNull(this.font);
        g.drawString(this.font, "< Back", backTextX, backY + (backH - 9) / 2, 0xFFCCCCDD, false);
        this.clickRects.add(new ClickRect(backX, backY, backW, backH, "back"));
        int contentTop = this.titleBarH + 10;
        int centerX = this.width / 2;
        int balancePanelW = 160;
        int balancePanelH = 40;
        int walletPanelX = centerX - balancePanelW - 8;
        int balancePanelY = contentTop;
        UIHelper.drawCard(g, walletPanelX, balancePanelY, balancePanelW, balancePanelH);
        UIHelper.drawShadowedText(g, this.font, "Wallet", walletPanelX + 10, balancePanelY + 6, 0xFF666677);
        String walletStr = "$" + this.wallet;
        Objects.requireNonNull(this.font);
        UIHelper.drawShadowedText(g, this.font, walletStr, walletPanelX + 10, balancePanelY + 6 + 9 + 4, 0xFFDDDDEE);
        int bankPanelX = centerX + 8;
        UIHelper.drawCard(g, bankPanelX, balancePanelY, balancePanelW, balancePanelH);
        UIHelper.drawShadowedText(g, this.font, "Bank", bankPanelX + 10, balancePanelY + 6, 0xFF666677);
        String bankStr = "$" + this.bank;
        Objects.requireNonNull(this.font);
        UIHelper.drawShadowedText(g, this.font, bankStr, bankPanelX + 10, balancePanelY + 6 + 9 + 4, 0xFFDDDDEE);
        int divY = balancePanelY + balancePanelH + 10 + 4;
        UIHelper.drawHorizontalDivider(g, 30, divY, this.width - 20 - 40);
        int sectionTop = divY + 12;
        int sectionW = Math.min(180, (this.width - 30) / 2);
        int depositX = centerX - sectionW - 12;
        int withdrawX = centerX + 12;
        this.renderTransferSection(g, mouseX, mouseY, depositX, sectionTop, sectionW, "Deposit to Bank", -11751600, "deposit");
        UIHelper.drawVerticalDivider(g, centerX, sectionTop, 100);
        this.renderTransferSection(g, mouseX, mouseY, withdrawX, sectionTop, sectionW, "Withdraw to Wallet", 0xFF888899, "withdraw");
        if (this.statusTicks > 0 && !this.statusMessage.isEmpty()) {
            int msgColor = this.statusMessage.contains("complete") ? -11751600 : -3394765;
            Objects.requireNonNull(this.font);
            g.drawCenteredString(this.font, this.statusMessage, centerX, this.height - 10 - 9, msgColor);
        }
        super.render(g, mouseX, mouseY, partialTick);
    }

    private void renderTransferSection(GuiGraphics g, int mouseX, int mouseY, int x, int y, int w, String title, int titleColor, String actionPrefix) {
        Objects.requireNonNull(this.font);
        int sectionH = 9 + 12 + 56 + 8;
        UIHelper.drawPanel(g, x, y, w, sectionH);
        int titleW = this.font.width(title);
        g.drawString(this.font, title, x + (w - titleW) / 2, y + 8, titleColor, false);
        int btnW = (w - 20) / 2;
        int btnH = 22;
        int btnGap = 4;
        Objects.requireNonNull(this.font);
        int btnStartY = y + 9 + 16;
        for (int i = 0; i < AMOUNTS.length; ++i) {
            int col = i % 2;
            int row = i / 2;
            int btnX = x + 8 + col * (btnW + btnGap);
            int btnY = btnStartY + row * (btnH + btnGap);
            boolean hovered = mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY && mouseY < btnY + btnH;
            UIHelper.drawButton(g, btnX, btnY, btnW, btnH, hovered);
            String label = AMOUNT_LABELS[i];
            int labelW = this.font.width(label);
            int n = btnX + (btnW - labelW) / 2;
            Objects.requireNonNull(this.font);
            g.drawString(this.font, label, n, btnY + (btnH - 9) / 2, 0xFFCCCCDD, false);
            int amount = AMOUNTS[i];
            if (amount == -1) {
                amount = actionPrefix.equals("deposit") ? this.wallet : this.bank;
            }
            this.clickRects.add(new ClickRect(btnX, btnY, btnW, btnH, actionPrefix + ":" + amount));
        }
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean consumed) {
        if (consumed) {
            return super.mouseClicked(event, consumed);
        }
        int mx = (int)event.x();
        int my = (int)event.y();
        for (ClickRect r : this.clickRects) {
            if (mx < r.x || mx >= r.x + r.w || my < r.y || my >= r.y + r.h) continue;
            this.handleClick(r.action);
            return true;
        }
        return super.mouseClicked(event, consumed);
    }

    private void handleClick(String action) {
        int amount;
        if ("back".equals(action)) {
            if (this.minecraft != null) {
                this.minecraft.setScreen(this.parent);
            }
            return;
        }
        String[] parts = action.split(":");
        if (parts.length != 2) {
            return;
        }
        String direction = parts[0];
        try {
            amount = Integer.parseInt(parts[1]);
        }
        catch (NumberFormatException e) {
            return;
        }
        if (amount <= 0) {
            return;
        }
        if ("deposit".equals(direction)) {
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("transfer_to_bank", String.valueOf(amount)), (CustomPacketPayload[])new CustomPacketPayload[0]);
        } else if ("withdraw".equals(direction)) {
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("transfer_to_wallet", String.valueOf(amount)), (CustomPacketPayload[])new CustomPacketPayload[0]);
        }
    }

    public boolean isPauseScreen() {
        return false;
    }

    private record ClickRect(int x, int y, int w, int h, String action) {
    }
}


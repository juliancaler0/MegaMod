/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.EditBox
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.input.MouseButtonEvent
 *  net.minecraft.client.multiplayer.PlayerInfo
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.neoforged.neoforge.client.network.ClientPacketDistributor
 */
package com.ultra.megamod.feature.computer.screen;

import com.ultra.megamod.feature.computer.network.ComputerActionPayload;
import com.ultra.megamod.feature.ui.UIHelper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class MessagingScreen
extends Screen {
    private final Screen parent;
    private static final int MARGIN = 10;
    private static final int ROW_HEIGHT = 24;
    private static final int ROW_GAP = 2;
    private int titleBarH;
    private int contentTop;
    private final List<String> onlinePlayers = new ArrayList<String>();
    private int scrollOffset = 0;
    private boolean composing = false;
    private String selectedPlayer = null;
    private EditBox messageInput;
    private String confirmationMessage = null;
    private int confirmationTicks = 0;
    private final List<ClickRect> clickRects = new ArrayList<ClickRect>();

    public MessagingScreen(Screen parent) {
        super((Component)Component.literal((String)"Messages"));
        this.parent = parent;
    }

    protected void init() {
        super.init();
        Objects.requireNonNull(this.font);
        this.titleBarH = 9 + 16;
        this.contentTop = this.titleBarH + 10;
        this.onlinePlayers.clear();
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() != null) {
            Collection<PlayerInfo> players = mc.getConnection().getOnlinePlayers();
            for (PlayerInfo info : players) {
                String name = info.getProfile().name();
                if (mc.player == null || name.equals(mc.player.getGameProfile().name())) continue;
                this.onlinePlayers.add(name);
            }
        }
        if (this.composing && this.selectedPlayer != null) {
            int inputW = Math.min(300, this.width - 40);
            int inputX = (this.width - inputW) / 2;
            int inputY = this.height / 2;
            this.messageInput = new EditBox(this.font, inputX, inputY, inputW, 20, (Component)Component.literal((String)"Type message..."));
            this.messageInput.setMaxLength(256);
            this.messageInput.setTextColor(-1);
            this.addRenderableWidget(this.messageInput);
        }
    }

    public void tick() {
        super.tick();
        if (this.confirmationTicks > 0) {
            --this.confirmationTicks;
            if (this.confirmationTicks <= 0) {
                this.confirmationMessage = null;
            }
        }
    }

    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.clickRects.clear();
        g.fill(0, 0, this.width, this.height, 0xFF0E0E18);
        UIHelper.drawPanel(g, 0, 0, this.width, this.height);
        UIHelper.drawTitleBar(g, 0, 0, this.width, this.titleBarH);
        Objects.requireNonNull(this.font);
        int titleY = (this.titleBarH - 9) / 2;
        UIHelper.drawCenteredTitle(g, this.font, this.composing ? "Compose Message" : "Messages", this.width / 2, titleY);
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
        if (this.composing && this.selectedPlayer != null) {
            this.renderComposeMode(g, mouseX, mouseY);
        } else {
            this.renderPlayerList(g, mouseX, mouseY);
        }
        if (this.confirmationMessage != null && this.confirmationTicks > 0) {
            int msgW = this.font.width(this.confirmationMessage) + 20;
            int msgX = (this.width - msgW) / 2;
            Objects.requireNonNull(this.font);
            int msgY = this.height - 10 - 9 - 10;
            Objects.requireNonNull(this.font);
            UIHelper.drawCard(g, msgX, msgY, msgW, 9 + 10);
            g.drawCenteredString(this.font, this.confirmationMessage, this.width / 2, msgY + 5, -11751600);
        }
        super.render(g, mouseX, mouseY, partialTick);
    }

    private void renderPlayerList(GuiGraphics g, int mouseX, int mouseY) {
        if (this.onlinePlayers.isEmpty()) {
            int panelW = 220;
            int panelH = 40;
            int px = (this.width - panelW) / 2;
            int py = this.height / 2 - panelH / 2;
            UIHelper.drawPanel(g, px, py, panelW, panelH);
            int n = this.width / 2;
            int n2 = this.height / 2;
            Objects.requireNonNull(this.font);
            UIHelper.drawCenteredLabel(g, this.font, "No other players online.", n, n2 - 9 / 2);
            return;
        }
        String header = "Select Player (" + this.onlinePlayers.size() + ")";
        g.drawString(this.font, header, 20, this.contentTop, 0xFF888899, false);
        Objects.requireNonNull(this.font);
        int listTop = this.contentTop + 9 + 6;
        int listW = Math.min(260, this.width - 20);
        int listX = (this.width - listW) / 2;
        int listBottom = this.height - 10 - 20;
        UIHelper.drawInsetPanel(g, listX - 4, listTop - 4, listW + 8, listBottom - listTop + 8);
        g.enableScissor(listX, listTop, listX + listW, listBottom);
        int visibleH = listBottom - listTop;
        int totalH = this.onlinePlayers.size() * 26 - 2;
        int maxScroll = Math.max(0, totalH - visibleH);
        this.scrollOffset = Math.max(0, Math.min(this.scrollOffset, maxScroll));
        for (int i = 0; i < this.onlinePlayers.size(); ++i) {
            int rowY = listTop + i * 26 - this.scrollOffset;
            if (rowY + 24 < listTop || rowY > listBottom) continue;
            String pName = this.onlinePlayers.get(i);
            boolean hovered = mouseX >= listX && mouseX < listX + listW && mouseY >= rowY && mouseY < rowY + 24;
            UIHelper.drawRowBg(g, listX, rowY, listW, 24, i % 2 == 0);
            if (hovered) {
                g.fill(listX, rowY, listX + listW, rowY + 24, 0x18FFFFFF);
            }
            g.fill(listX + 6, rowY + 10, listX + 10, rowY + 14, 0xFF888899);
            Objects.requireNonNull(this.font);
            g.drawString(this.font, pName, listX + 16, rowY + (24 - 9) / 2, 0xFFCCCCDD, false);
            this.clickRects.add(new ClickRect(listX, rowY, listW, 24, "select:" + pName));
        }
        g.disableScissor();
        if (totalH > visibleH) {
            float progress = maxScroll > 0 ? (float)this.scrollOffset / (float)maxScroll : 0.0f;
            UIHelper.drawScrollbar(g, listX + listW + 4, listTop, listBottom - listTop, progress);
        }
    }

    private void renderComposeMode(GuiGraphics g, int mouseX, int mouseY) {
        int panelW = Math.min(340, this.width - 20);
        int panelH = 100;
        int panelX = (this.width - panelW) / 2;
        int panelY = this.height / 2 - panelH / 2 - 10;
        UIHelper.drawPanel(g, panelX, panelY, panelW, panelH);
        int toY = panelY + 10;
        g.drawString(this.font, "To:", panelX + 10, toY, 0xFF666677, false);
        g.drawString(this.font, this.selectedPlayer, panelX + 10 + this.font.width("To: "), toY, 0xFFDDDDEE, false);
        int sendW = 60;
        int sendH = 18;
        int sendX = (this.width - sendW) / 2 - 35;
        int sendY = this.height / 2 + 26;
        boolean sendHover = mouseX >= sendX && mouseX < sendX + sendW && mouseY >= sendY && mouseY < sendY + sendH;
        UIHelper.drawButton(g, sendX, sendY, sendW, sendH, sendHover);
        int sendTextX = sendX + (sendW - this.font.width("Send")) / 2;
        Objects.requireNonNull(this.font);
        g.drawString(this.font, "Send", sendTextX, sendY + (sendH - 9) / 2, -11751600, false);
        this.clickRects.add(new ClickRect(sendX, sendY, sendW, sendH, "send"));
        int cancelW = 60;
        int cancelX = (this.width - cancelW) / 2 + 35;
        boolean cancelHover = mouseX >= cancelX && mouseX < cancelX + cancelW && mouseY >= sendY && mouseY < sendY + sendH;
        UIHelper.drawButton(g, cancelX, sendY, cancelW, sendH, cancelHover);
        int cancelTextX = cancelX + (cancelW - this.font.width("Cancel")) / 2;
        Objects.requireNonNull(this.font);
        g.drawString(this.font, "Cancel", cancelTextX, sendY + (sendH - 9) / 2, -3394765, false);
        this.clickRects.add(new ClickRect(cancelX, sendY, cancelW, sendH, "cancel"));
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
        switch (action) {
            case "back": {
                if (this.composing) {
                    this.composing = false;
                    this.selectedPlayer = null;
                    this.rebuildWidgets();
                    break;
                }
                if (this.minecraft == null) break;
                this.minecraft.setScreen(this.parent);
                break;
            }
            case "send": {
                this.sendMessage();
                break;
            }
            case "cancel": {
                this.composing = false;
                this.selectedPlayer = null;
                this.rebuildWidgets();
                break;
            }
            default: {
                if (!action.startsWith("select:")) break;
                this.selectedPlayer = action.substring(7);
                this.composing = true;
                this.rebuildWidgets();
            }
        }
    }

    private void sendMessage() {
        if (this.messageInput == null || this.selectedPlayer == null) {
            return;
        }
        String text = this.messageInput.getValue().trim();
        if (text.isEmpty()) {
            return;
        }
        ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("send_message", this.selectedPlayer + ":" + text), (CustomPacketPayload[])new CustomPacketPayload[0]);
        this.confirmationMessage = "Message sent to " + this.selectedPlayer + "!";
        this.confirmationTicks = 60;
        this.messageInput.setValue("");
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!this.composing) {
            this.scrollOffset -= (int)(scrollY * 26.0);
            this.scrollOffset = Math.max(0, this.scrollOffset);
            return true;
        }
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 257 && this.composing && this.messageInput != null && this.messageInput.isFocused()) {
            this.sendMessage();
            return true;
        }
        return false;
    }

    public boolean isPauseScreen() {
        return false;
    }

    private record ClickRect(int x, int y, int w, int h, String action) {
    }
}


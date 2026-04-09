package com.ultra.megamod.feature.arena.client;

import com.ultra.megamod.feature.arena.network.ArenaCheckpointPayload;
import com.ultra.megamod.feature.arena.network.ArenaCheckpointResponsePayload;
import com.ultra.megamod.feature.ui.UIHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

/**
 * Arena checkpoint screen — shown every 5 waves.
 * Player can choose to continue fighting or leave with earned rewards.
 */
public class ArenaCheckpointScreen extends Screen {

    private final int wave;
    private final int reward;
    private boolean responded = false;

    private static final int PANEL_W = 260;
    private static final int PANEL_H = 160;

    public ArenaCheckpointScreen(int wave, int reward) {
        super(Component.literal("Arena Checkpoint"));
        this.wave = wave;
        this.reward = reward;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Dim background
        g.fill(0, 0, this.width, this.height, 0x88000000);

        int px = (this.width - PANEL_W) / 2;
        int py = (this.height - PANEL_H) / 2;

        UIHelper.drawPanel(g, px, py, PANEL_W, PANEL_H);

        int cx = px + PANEL_W / 2;
        int y = py + 12;

        // Title
        String title = "\u2605 CHECKPOINT \u2605";
        g.drawString(this.font, title, cx - this.font.width(title) / 2, y, 0xFFFFDD44, false);
        y += 16;

        // Divider
        g.fill(px + 12, y, px + PANEL_W - 12, y + 1, 0xFFD4AF37);
        y += 8;

        // Wave info
        String waveStr = "Wave " + wave + " cleared!";
        g.drawString(this.font, waveStr, cx - this.font.width(waveStr) / 2, y, 0xFF44CC44, false);
        y += 14;

        // Reward info
        String rewardStr = "Rewards earned: " + reward + " MC";
        g.drawString(this.font, rewardStr, cx - this.font.width(rewardStr) / 2, y, 0xFFD4AF37, false);
        y += 14;

        // Health restored
        String healStr = "Health & hunger restored!";
        g.drawString(this.font, healStr, cx - this.font.width(healStr) / 2, y, 0xFF88AAFF, false);
        y += 18;

        // Buttons
        int btnW = 100;
        int btnH = 22;
        int btnGap = 16;

        // Continue button
        int contX = cx - btnW - btnGap / 2;
        boolean contHov = mouseX >= contX && mouseX < contX + btnW && mouseY >= y && mouseY < y + btnH;
        UIHelper.drawButton(g, contX, y, btnW, btnH, contHov);
        String contLabel = "\u2694 Continue";
        g.drawString(this.font, contLabel, contX + (btnW - this.font.width(contLabel)) / 2, y + (btnH - 9) / 2,
                contHov ? 0xFFFFFFFF : 0xFF44CC44, false);

        // Leave button
        int leaveX = cx + btnGap / 2;
        boolean leaveHov = mouseX >= leaveX && mouseX < leaveX + btnW && mouseY >= y && mouseY < y + btnH;
        UIHelper.drawButton(g, leaveX, y, btnW, btnH, leaveHov);
        String leaveLabel = "\u2716 Leave (" + reward + "MC)";
        g.drawString(this.font, leaveLabel, leaveX + (btnW - this.font.width(leaveLabel)) / 2, y + (btnH - 9) / 2,
                leaveHov ? 0xFFFFFFFF : 0xFFFFAA44, false);

        super.render(g, mouseX, mouseY, partialTick);
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean consumed) {
        if (consumed || responded) return super.mouseClicked(event, consumed);
        int mx = (int) event.x();
        int my = (int) event.y();

        int cx = this.width / 2;
        int py = (this.height - PANEL_H) / 2;
        int y = py + 12 + 16 + 8 + 14 + 14 + 18;
        int btnW = 100;
        int btnH = 22;
        int btnGap = 16;

        int contX = cx - btnW - btnGap / 2;
        int leaveX = cx + btnGap / 2;

        if (mx >= contX && mx < contX + btnW && my >= y && my < y + btnH) {
            sendResponse(true);
            return true;
        }
        if (mx >= leaveX && mx < leaveX + btnW && my >= y && my < y + btnH) {
            sendResponse(false);
            return true;
        }

        return super.mouseClicked(event, consumed);
    }

    private void sendResponse(boolean continueArena) {
        if (responded) return;
        responded = true;
        ClientPacketDistributor.sendToServer(
                new ArenaCheckpointResponsePayload(continueArena),
                new CustomPacketPayload[0]);
        ArenaCheckpointPayload.clearClientState();
        this.onClose();
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void onClose() {
        ArenaCheckpointPayload.clearClientState();
        super.onClose();
    }
}

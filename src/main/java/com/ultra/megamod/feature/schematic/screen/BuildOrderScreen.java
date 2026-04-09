package com.ultra.megamod.feature.schematic.screen;

import com.ultra.megamod.feature.schematic.network.BuildOrderPayload;
import com.ultra.megamod.feature.schematic.network.BuildProgressPayload;
import com.ultra.megamod.feature.ui.UIHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays active build orders with progress bars.
 * Allows assigning builders and cancelling orders.
 */
public class BuildOrderScreen extends Screen {

    private List<BuildOrderInfo> orders = new ArrayList<>();
    private int scrollOffset = 0;
    private static final int ENTRY_HEIGHT = 36;
    private static final int VISIBLE_ENTRIES = 6;

    public record BuildOrderInfo(String orderId, String name, int progress, int total, String status) {}

    public BuildOrderScreen() {
        super(Component.literal("Build Orders"));
    }

    @Override
    protected void init() {
        super.init();
        // Request build order data from server
        ClientPacketDistributor.sendToServer(new BuildOrderPayload("list", "", ""));
    }

    @Override
    public void tick() {
        super.tick();
        // Poll for server response
        BuildProgressPayload response = BuildProgressPayload.lastResponse;
        if (response != null && response.status().startsWith("LIST:")) {
            parseOrderList(response.missingItems());
            BuildProgressPayload.lastResponse = null;
        }
    }

    private void parseOrderList(String json) {
        orders.clear();
        // Simple parsing - server sends comma-separated order entries
        if (json.isEmpty()) return;
        for (String entry : json.split("\\|")) {
            String[] parts = entry.split(";");
            if (parts.length >= 4) {
                orders.add(new BuildOrderInfo(parts[0], parts[1],
                        Integer.parseInt(parts[2]), Integer.parseInt(parts[3]),
                        parts.length > 4 ? parts[4] : "pending"));
            }
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        super.render(g, mouseX, mouseY, delta);

        int panelW = 340;
        int panelH = 280;
        int px = (width - panelW) / 2;
        int py = (height - panelH) / 2;

        UIHelper.drawPanel(g, px, py, panelW, panelH);

        // Title
        g.drawCenteredString(font, "Build Orders", width / 2, py + 6, UIHelper.BLUE_ACCENT);
        g.drawCenteredString(font, orders.size() + " active", width / 2, py + 18, UIHelper.GOLD_MID);

        int listX = px + 8;
        int listY = py + 32;
        int listW = panelW - 16;

        if (orders.isEmpty()) {
            g.drawCenteredString(font, "No active build orders",
                    width / 2, py + panelH / 2, 0xFF888899);
            g.drawCenteredString(font, "Load a schematic with [L] to create one",
                    width / 2, py + panelH / 2 + 12, 0xFF666677);
        }

        for (int i = 0; i < VISIBLE_ENTRIES && i + scrollOffset < orders.size(); i++) {
            int idx = i + scrollOffset;
            BuildOrderInfo order = orders.get(idx);
            int ey = listY + i * ENTRY_HEIGHT;

            boolean hovered = mouseX >= listX && mouseX <= listX + listW
                    && mouseY >= ey && mouseY <= ey + ENTRY_HEIGHT - 1;
            g.fill(listX, ey, listX + listW, ey + ENTRY_HEIGHT - 1,
                    hovered ? 0xFF1E1E30 : 0xFF141420);

            // Name
            g.drawString(font, order.name, listX + 4, ey + 2, UIHelper.CREAM_TEXT, false);

            // Progress bar
            int barX = listX + 4;
            int barY = ey + 14;
            int barW = listW - 80;
            int barH = 8;
            g.fill(barX, barY, barX + barW, barY + barH, 0xFF0E0E18);
            if (order.total > 0) {
                float pct = (float) order.progress / order.total;
                int fillW = (int) (barW * pct);
                g.fill(barX, barY, barX + fillW, barY + barH, UIHelper.XP_GREEN);
            }

            // Progress text
            String pctStr = order.total > 0
                    ? (int) ((float) order.progress / order.total * 100) + "%"
                    : "0%";
            g.drawString(font, pctStr + " (" + order.progress + "/" + order.total + ")",
                    barX, barY + barH + 2, UIHelper.GOLD_MID, false);

            // Cancel button
            int cancelX = listX + listW - 60;
            int cancelY = ey + 4;
            boolean cancelHovered = mouseX >= cancelX && mouseX <= cancelX + 56
                    && mouseY >= cancelY && mouseY <= cancelY + 14;
            g.fill(cancelX, cancelY, cancelX + 56, cancelY + 14,
                    cancelHovered ? 0xFF4A2020 : 0xFF2A1818);
            g.drawCenteredString(font, "Cancel", cancelX + 28, cancelY + 3,
                    cancelHovered ? 0xFFFF5555 : 0xFFAA6666);
        }

        // Close button
        int btnY = py + panelH - 24;
        drawButton(g, px + panelW / 2 - 40, btnY, 80, 18, "Close", mouseX, mouseY);
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean consumed) {
        if (consumed) return super.mouseClicked(event, consumed);
        double mouseX = event.x();
        double mouseY = event.y();

        int panelW = 340;
        int panelH = 280;
        int px = (width - panelW) / 2;
        int py = (height - panelH) / 2;
        int listX = px + 8;
        int listY = py + 32;
        int listW = panelW - 16;

        // Check cancel buttons
        for (int i = 0; i < VISIBLE_ENTRIES && i + scrollOffset < orders.size(); i++) {
            int idx = i + scrollOffset;
            int ey = listY + i * ENTRY_HEIGHT;
            int cancelX = listX + listW - 60;
            int cancelY = ey + 4;
            if (mouseX >= cancelX && mouseX <= cancelX + 56
                    && mouseY >= cancelY && mouseY <= cancelY + 14) {
                ClientPacketDistributor.sendToServer(
                        new BuildOrderPayload("cancel", orders.get(idx).orderId, ""));
                orders.remove(idx);
                return true;
            }
        }

        // Close button
        if (isOver(px + panelW / 2 - 40, py + panelH - 24, 80, 18, mouseX, mouseY)) {
            onClose();
            return true;
        }

        return super.mouseClicked(event, consumed);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY > 0 && scrollOffset > 0) scrollOffset--;
        else if (scrollY < 0 && scrollOffset < orders.size() - VISIBLE_ENTRIES) scrollOffset++;
        return true;
    }

    private void drawButton(GuiGraphics g, int x, int y, int w, int h,
                            String text, int mouseX, int mouseY) {
        boolean hovered = isOver(x, y, w, h, mouseX, mouseY);
        g.fill(x, y, x + w, y + h, hovered ? 0xFF2A2A4A : 0xFF1C1C28);
        g.drawCenteredString(font, text, x + w / 2, y + (h - 8) / 2,
                hovered ? 0xFF55FFFF : UIHelper.CREAM_TEXT);
    }

    private boolean isOver(int x, int y, int w, int h, double mx, double my) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    @Override
    public boolean isPauseScreen() { return false; }
}

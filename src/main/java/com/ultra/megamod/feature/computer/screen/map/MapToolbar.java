package com.ultra.megamod.feature.computer.screen.map;

import com.ultra.megamod.feature.ui.UIHelper;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class MapToolbar {

    public enum Tool { PAN, DRAW_LINE, PLACE_TEXT, PLACE_WAYPOINT, MEASURE }

    private Tool currentTool = Tool.PAN;
    private int x, y, width;

    private static final String[] TOOL_LABELS = {"Pan", "Line", "Text", "WP", "Ruler"};
    private static final int TOOL_BTN_W = 28;
    private static final int TOOL_BTN_H = 14;
    private static final int TOOL_GAP = 2;
    private static final int BAR_H = 18;

    // "Go To" button position (computed during render)
    private int gotoBtnX, gotoBtnY, gotoBtnW, gotoBtnH;
    private boolean gotoClicked = false;

    public MapToolbar(int x, int y, int width) {
        this.x = x;
        this.y = y;
        this.width = width;
    }

    public void updatePosition(int x, int y, int width) {
        this.x = x;
        this.y = y;
        this.width = width;
    }

    public int getBarHeight() {
        return BAR_H;
    }

    public void render(GuiGraphics g, Font font, int mouseX, int mouseY) {
        // Toolbar background bar
        g.fill(this.x, this.y, this.x + this.width, this.y + BAR_H, 0xFF16162A);
        // Bottom border
        g.fill(this.x, this.y + BAR_H - 1, this.x + this.width, this.y + BAR_H, 0xFF333355);

        Tool[] tools = Tool.values();
        int btnY = this.y + 2;

        for (int i = 0; i < tools.length; i++) {
            int btnX = this.x + 4 + i * (TOOL_BTN_W + TOOL_GAP);
            boolean isActive = this.currentTool == tools[i];
            boolean hover = mouseX >= btnX && mouseX < btnX + TOOL_BTN_W
                    && mouseY >= btnY && mouseY < btnY + TOOL_BTN_H;

            if (isActive) {
                g.fill(btnX, btnY, btnX + TOOL_BTN_W, btnY + TOOL_BTN_H, 0xFF2A3A5E);
                // Accent underline
                g.fill(btnX, btnY + TOOL_BTN_H - 1, btnX + TOOL_BTN_W, btnY + TOOL_BTN_H, 0xFF58A6FF);
            } else {
                UIHelper.drawButton(g, btnX, btnY, TOOL_BTN_W, TOOL_BTN_H, hover);
            }

            String label = TOOL_LABELS[i];
            int labelW = font.width(label);
            int textColor = isActive ? 0xFF58A6FF : (hover ? 0xFFEEEEFF : 0xFFAABBCC);
            g.drawString(font, label, btnX + (TOOL_BTN_W - labelW) / 2, btnY + 3, textColor, false);
        }

        // "Go To" button (right side)
        this.gotoBtnW = 30;
        this.gotoBtnH = TOOL_BTN_H;
        this.gotoBtnX = this.x + this.width - this.gotoBtnW - 4;
        this.gotoBtnY = btnY;

        boolean gotoHover = mouseX >= this.gotoBtnX && mouseX < this.gotoBtnX + this.gotoBtnW
                && mouseY >= this.gotoBtnY && mouseY < this.gotoBtnY + this.gotoBtnH;
        UIHelper.drawButton(g, this.gotoBtnX, this.gotoBtnY, this.gotoBtnW, this.gotoBtnH, gotoHover);
        String gotoLabel = "GoTo";
        int gotoLabelW = font.width(gotoLabel);
        g.drawString(font, gotoLabel, this.gotoBtnX + (this.gotoBtnW - gotoLabelW) / 2,
                this.gotoBtnY + 3, gotoHover ? 0xFFEEEEFF : 0xFFCCCCDD, false);

        // Zoom indicator (between tools and GoTo)
        // (zoom text drawn by the caller in title bar, so skip here)
    }

    /**
     * Handle a click on the toolbar.
     * Returns true if the click was consumed.
     */
    public boolean handleClick(int mouseX, int mouseY) {
        int btnY = this.y + 2;
        this.gotoClicked = false;

        // Check tool buttons
        Tool[] tools = Tool.values();
        for (int i = 0; i < tools.length; i++) {
            int btnX = this.x + 4 + i * (TOOL_BTN_W + TOOL_GAP);
            if (mouseX >= btnX && mouseX < btnX + TOOL_BTN_W
                    && mouseY >= btnY && mouseY < btnY + TOOL_BTN_H) {
                this.currentTool = tools[i];
                return true;
            }
        }

        // Check "Go To" button
        if (mouseX >= this.gotoBtnX && mouseX < this.gotoBtnX + this.gotoBtnW
                && mouseY >= this.gotoBtnY && mouseY < this.gotoBtnY + this.gotoBtnH) {
            this.gotoClicked = true;
            return true;
        }

        // Check if click is anywhere on the toolbar bar
        if (mouseX >= this.x && mouseX < this.x + this.width
                && mouseY >= this.y && mouseY < this.y + BAR_H) {
            return true; // consume but do nothing
        }

        return false;
    }

    public Tool getCurrentTool() {
        return this.currentTool;
    }

    public void setCurrentTool(Tool tool) {
        this.currentTool = tool;
    }

    public boolean wasGotoClicked() {
        boolean result = this.gotoClicked;
        this.gotoClicked = false;
        return result;
    }
}

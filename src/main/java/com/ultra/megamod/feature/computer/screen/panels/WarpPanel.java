package com.ultra.megamod.feature.computer.screen.panels;

import com.ultra.megamod.MegaMod;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ultra.megamod.feature.computer.network.ComputerActionPayload;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.ui.UIHelper;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class WarpPanel {
    private final Font font;
    private int scroll = 0;
    private String warpNameInput = "";
    private boolean inputFocused = false;
    private int cursorBlink = 0;
    private String statusMsg = "";
    private int statusTimer = 0;

    private List<WarpEntry> warps = new ArrayList<>();

    private static final int ROW_HEIGHT = 22;
    private static final int ROW_EVEN = 0xFF1C2128;
    private static final int ROW_ODD = 0xFF21262D;
    private static final int WARP_PURPLE = 0xFF9B59B6;
    private static final int SUCCESS_GREEN = 0xFF3FB950;
    private static final int ERROR_RED = 0xFFFF6B6B;
    private static final int INPUT_BG = 0xFF0D1117;
    private static final int INPUT_BORDER = 0xFF4A4A50;
    private static final int INPUT_ACTIVE = 0xFF58A6FF;

    private record WarpEntry(String name, double x, double y, double z, String dimension) {}

    public WarpPanel(Font font) {
        this.font = font;
    }

    public void requestData() {
        ClientPacketDistributor.sendToServer(
            (CustomPacketPayload) new ComputerActionPayload("warp_request", ""),
            (CustomPacketPayload[]) new CustomPacketPayload[0]
        );
    }

    public void handleResponse(String type, String jsonData) {
        if ("warp_data".equals(type)) {
            try {
                JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
                this.warps.clear();
                if (obj.has("warps")) {
                    JsonArray arr = obj.getAsJsonArray("warps");
                    for (JsonElement el : arr) {
                        JsonObject w = el.getAsJsonObject();
                        this.warps.add(new WarpEntry(
                            w.get("name").getAsString(),
                            w.get("x").getAsDouble(),
                            w.get("y").getAsDouble(),
                            w.get("z").getAsDouble(),
                            w.has("dimension") ? w.get("dimension").getAsString() : "overworld"
                        ));
                    }
                }
            } catch (Exception e) {
                MegaMod.LOGGER.error("Failed to parse warp data", e);
            }
        } else if ("warp_result".equals(type)) {
            try {
                JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
                boolean success = obj.has("success") && obj.get("success").getAsBoolean();
                this.statusMsg = obj.has("message") ? obj.get("message").getAsString() : (success ? "Done!" : "Failed.");
                this.statusTimer = 80;
                requestData();
            } catch (Exception e) {
                MegaMod.LOGGER.error("Failed to handle warp action response", e);
            }
        }
    }

    public void tick() {
        this.cursorBlink++;
        if (this.statusTimer > 0) {
            this.statusTimer--;
            if (this.statusTimer <= 0) this.statusMsg = "";
        }
    }

    public void render(GuiGraphics g, int mouseX, int mouseY, int left, int top, int right, int bottom) {
        int w = right - left;
        int y = top + 4;

        // Header
        g.drawString(this.font, "Admin Warps", left + 8, y, UIHelper.GOLD_MID, false);
        y += 14;

        // Save warp bar
        int inputX = left + 8;
        int inputW = w - 130;
        int inputH = 14;
        int border = this.inputFocused ? INPUT_ACTIVE : INPUT_BORDER;
        g.fill(inputX - 1, y - 1, inputX + inputW + 1, y + inputH + 1, border);
        g.fill(inputX, y, inputX + inputW, y + inputH, INPUT_BG);

        if (this.warpNameInput.isEmpty() && !this.inputFocused) {
            g.drawString(this.font, "Warp name...", inputX + 3, y + 3, 0xFF4A4A50, false);
        } else {
            String clipped = this.warpNameInput;
            int maxW = inputW - 8;
            while (this.font.width(clipped) > maxW && clipped.length() > 0) {
                clipped = clipped.substring(1);
            }
            g.drawString(this.font, clipped, inputX + 3, y + 3, 0xFFE6EDF3, false);
            if (this.inputFocused && (this.cursorBlink / 10) % 2 == 0) {
                int cx = inputX + 3 + this.font.width(clipped);
                g.fill(cx, y + 2, cx + 1, y + inputH - 2, INPUT_ACTIVE);
            }
        }

        // Save button
        int saveBtnW = 70;
        int saveBtnX = inputX + inputW + 6;
        boolean saveHover = mouseX >= saveBtnX && mouseX < saveBtnX + saveBtnW && mouseY >= y && mouseY < y + inputH;
        UIHelper.drawButton(g, saveBtnX, y, saveBtnW, inputH, saveHover);
        String saveLabel = "Save Here";
        g.drawString(this.font, saveLabel, saveBtnX + (saveBtnW - this.font.width(saveLabel)) / 2, y + 3, SUCCESS_GREEN, false);

        y += inputH + 8;

        // Warps list
        UIHelper.drawInsetPanel(g, left + 4, y, w - 8, bottom - y - 24);
        int listTop = y + 2;
        int listBottom = bottom - 26;
        int listX = left + 8;
        int listW = w - 16;

        g.enableScissor(listX, listTop, listX + listW, listBottom);

        int visibleH = listBottom - listTop;
        int totalH = this.warps.size() * ROW_HEIGHT;
        int maxScroll = Math.max(0, totalH - visibleH);
        this.scroll = Math.max(0, Math.min(this.scroll, maxScroll));

        if (this.warps.isEmpty()) {
            g.drawString(this.font, "No warps saved.", listX + 10, listTop + 10, 0xFF8B949E, false);
        }

        for (int i = 0; i < this.warps.size(); i++) {
            int rowY = listTop + i * ROW_HEIGHT - this.scroll;
            if (rowY + ROW_HEIGHT < listTop || rowY > listBottom) continue;

            WarpEntry warp = this.warps.get(i);
            int rowBg = i % 2 == 0 ? ROW_EVEN : ROW_ODD;
            g.fill(listX, rowY, listX + listW, rowY + ROW_HEIGHT, rowBg);

            // Warp name
            g.drawString(this.font, warp.name, listX + 4, rowY + (ROW_HEIGHT - 9) / 2, WARP_PURPLE, false);

            // Coordinates
            String coords = String.format("%.0f, %.0f, %.0f", warp.x, warp.y, warp.z);
            g.drawString(this.font, coords, listX + 80, rowY + (ROW_HEIGHT - 9) / 2, 0xFF8B949E, false);

            // Dimension (short)
            String dim = warp.dimension;
            if (dim.contains(":")) dim = dim.substring(dim.indexOf(':') + 1);
            g.drawString(this.font, dim, listX + 180, rowY + (ROW_HEIGHT - 9) / 2, 0xFF555566, false);

            // Goto button
            int btnH = ROW_HEIGHT - 6;
            int btnY = rowY + 3;
            int gotoBtnW = 30;
            int gotoBtnX = listX + listW - gotoBtnW - 38;
            boolean gotoHover = mouseX >= gotoBtnX && mouseX < gotoBtnX + gotoBtnW && mouseY >= btnY && mouseY < btnY + btnH
                    && mouseY >= listTop && mouseY < listBottom;
            UIHelper.drawButton(g, gotoBtnX, btnY, gotoBtnW, btnH, gotoHover);
            g.drawString(this.font, "Go", gotoBtnX + (gotoBtnW - this.font.width("Go")) / 2, btnY + (btnH - 9) / 2, SUCCESS_GREEN, false);

            // Delete button
            int delBtnW = 30;
            int delBtnX = gotoBtnX + gotoBtnW + 4;
            boolean delHover = mouseX >= delBtnX && mouseX < delBtnX + delBtnW && mouseY >= btnY && mouseY < btnY + btnH
                    && mouseY >= listTop && mouseY < listBottom;
            UIHelper.drawButton(g, delBtnX, btnY, delBtnW, btnH, delHover);
            g.drawString(this.font, "Del", delBtnX + (delBtnW - this.font.width("Del")) / 2, btnY + (btnH - 9) / 2, ERROR_RED, false);
        }

        g.disableScissor();

        // Scrollbar
        if (totalH > visibleH) {
            float progress = maxScroll > 0 ? (float) this.scroll / (float) maxScroll : 0.0f;
            UIHelper.drawScrollbar(g, listX + listW + 2, listTop, listBottom - listTop, progress);
        }

        // Status message
        if (!this.statusMsg.isEmpty() && this.statusTimer > 0) {
            int msgW = this.font.width(this.statusMsg) + 20;
            int msgX = left + (w - msgW) / 2;
            int msgY = bottom - 20;
            UIHelper.drawCard(g, msgX, msgY, msgW, 16);
            boolean isError = this.statusMsg.contains("Failed") || this.statusMsg.contains("error") || this.statusMsg.contains("empty");
            g.drawCenteredString(this.font, this.statusMsg, left + w / 2, msgY + 4, isError ? ERROR_RED : SUCCESS_GREEN);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button, int left, int top, int right, int bottom) {
        int w = right - left;
        int y = top + 18;
        int inputX = left + 8;
        int inputW = w - 130;
        int inputH = 14;

        // Check input focus
        if (mouseX >= inputX && mouseX < inputX + inputW && mouseY >= y && mouseY < y + inputH) {
            this.inputFocused = true;
            this.cursorBlink = 0;
            return true;
        } else {
            this.inputFocused = false;
        }

        // Save button
        int saveBtnW = 70;
        int saveBtnX = inputX + inputW + 6;
        if (mouseX >= saveBtnX && mouseX < saveBtnX + saveBtnW && mouseY >= y && mouseY < y + inputH) {
            if (!this.warpNameInput.trim().isEmpty()) {
                ClientPacketDistributor.sendToServer(
                    (CustomPacketPayload) new ComputerActionPayload("warp_save", this.warpNameInput.trim()),
                    (CustomPacketPayload[]) new CustomPacketPayload[0]
                );
                this.warpNameInput = "";
            }
            return true;
        }

        // Warp list buttons
        int listTop2 = y + inputH + 10;
        int listX = left + 8;
        int listW = w - 16;
        int listBottom = bottom - 26;

        for (int i = 0; i < this.warps.size(); i++) {
            int rowY = listTop2 + i * ROW_HEIGHT - this.scroll;
            if (rowY + ROW_HEIGHT < listTop2 || rowY > listBottom) continue;

            int btnH = ROW_HEIGHT - 6;
            int btnY2 = rowY + 3;

            // Goto
            int gotoBtnW = 30;
            int gotoBtnX = listX + listW - gotoBtnW - 38;
            if (mouseX >= gotoBtnX && mouseX < gotoBtnX + gotoBtnW && mouseY >= btnY2 && mouseY < btnY2 + btnH
                    && mouseY >= listTop2 && mouseY < listBottom) {
                ClientPacketDistributor.sendToServer(
                    (CustomPacketPayload) new ComputerActionPayload("warp_goto", this.warps.get(i).name),
                    (CustomPacketPayload[]) new CustomPacketPayload[0]
                );
                return true;
            }

            // Delete
            int delBtnW = 30;
            int delBtnX = gotoBtnX + gotoBtnW + 4;
            if (mouseX >= delBtnX && mouseX < delBtnX + delBtnW && mouseY >= btnY2 && mouseY < btnY2 + btnH
                    && mouseY >= listTop2 && mouseY < listBottom) {
                ClientPacketDistributor.sendToServer(
                    (CustomPacketPayload) new ComputerActionPayload("warp_delete", this.warps.get(i).name),
                    (CustomPacketPayload[]) new CustomPacketPayload[0]
                );
                return true;
            }
        }

        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        this.scroll = Math.max(0, this.scroll - (int)(scrollY * ROW_HEIGHT));
        return true;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.inputFocused) {
            if (keyCode == 259 && !this.warpNameInput.isEmpty()) { // Backspace
                this.warpNameInput = this.warpNameInput.substring(0, this.warpNameInput.length() - 1);
                this.cursorBlink = 0;
                return true;
            }
            if (keyCode == 257) { // Enter
                if (!this.warpNameInput.trim().isEmpty()) {
                    ClientPacketDistributor.sendToServer(
                        (CustomPacketPayload) new ComputerActionPayload("warp_save", this.warpNameInput.trim()),
                        (CustomPacketPayload[]) new CustomPacketPayload[0]
                    );
                    this.warpNameInput = "";
                }
                return true;
            }
        }
        return false;
    }

    public boolean charTyped(char ch, int modifiers) {
        if (this.inputFocused && ch >= 32 && this.warpNameInput.length() < 24) {
            this.warpNameInput += ch;
            this.cursorBlink = 0;
            return true;
        }
        return false;
    }
}

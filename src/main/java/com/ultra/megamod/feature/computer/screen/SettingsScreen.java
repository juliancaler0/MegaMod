package com.ultra.megamod.feature.computer.screen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ultra.megamod.feature.computer.network.ComputerActionPayload;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.ui.UIHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class SettingsScreen extends Screen {
    private final Screen parent;
    private List<SettingCategory> categories = new ArrayList<>();
    private int scroll = 0;
    private int maxScroll = 0;
    private boolean dataLoaded = false;
    private int refreshTimer = 0;
    private String statusMessage = null;
    private int statusTicks = 0;
    private boolean statusSuccess = false;

    // Layout
    private static final int TITLE_BAR_H = 25;
    private static final int ROW_HEIGHT = 20;
    private static final int CAT_HEIGHT = 16;
    private static final int PADDING = 10;
    private static final int TOGGLE_W = 20;
    private static final int TOGGLE_H = 10;
    private static final int TOGGLE_KNOB = 8;
    private static final int RESET_BTN_W = 70;
    private static final int RESET_BTN_H = 16;

    // Colors
    private static final int TOGGLE_ON = 0xFF3FB950;
    private static final int TOGGLE_OFF = 0xFF4A4A50;
    private static final int BG_DARK = 0xFF0D1117;
    private static final int PANEL_BG = 0xFF161B22;
    private static final int ROW_EVEN = 0xFF1C2128;
    private static final int ROW_ODD = 0xFF21262D;
    private static final int CAT_COLOR = 0xFFFFD700;
    private static final int TEXT_COLOR = 0xFFE6EDF3;
    private static final int DIM_TEXT = 0xFF8B949E;
    private static final int ERROR_RED = 0xFFFF6B6B;
    private static final int SUCCESS_GREEN = 0xFF3FB950;
    private static final int DIVIDER = 0xFF30363D;

    // Click rects rebuilt each frame
    private final List<ClickRect> clickRects = new ArrayList<>();

    private record SettingCategory(String name, List<SettingEntry> settings) {}
    private record SettingEntry(String key, String name, boolean enabled) {}
    private record ClickRect(int x, int y, int w, int h, String action) {}

    public SettingsScreen(Screen parent) {
        super(Component.literal("Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        if (!this.dataLoaded) {
            requestSettingsData();
        }
    }

    private void requestSettingsData() {
        ClientPacketDistributor.sendToServer(
            (CustomPacketPayload) new ComputerActionPayload("settings_request", ""),
            (CustomPacketPayload[]) new CustomPacketPayload[0]
        );
    }

    @Override
    public void tick() {
        super.tick();

        // Poll for settings data
        ComputerDataPayload resp = ComputerDataPayload.lastResponse;
        if (resp != null && "settings_data".equals(resp.dataType())) {
            ComputerDataPayload.lastResponse = null;
            parseSettingsData(resp.jsonData());
            this.dataLoaded = true;
        } else if (resp != null && "settings_result".equals(resp.dataType())) {
            ComputerDataPayload.lastResponse = null;
            parseResult(resp.jsonData());
        } else if (resp != null && "error".equals(resp.dataType())) {
            // Consume error responses so the screen doesn't stay stuck
            ComputerDataPayload.lastResponse = null;
            this.dataLoaded = true;
        }

        // Status message fade
        if (this.statusTicks > 0) {
            this.statusTicks--;
            if (this.statusTicks <= 0) {
                this.statusMessage = null;
            }
        }

        // Auto-refresh every 100 ticks
        this.refreshTimer++;
        if (this.refreshTimer >= 100) {
            this.refreshTimer = 0;
            requestSettingsData();
        }
    }

    private void parseSettingsData(String json) {
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            JsonArray catsArr = obj.getAsJsonArray("categories");
            this.categories.clear();
            for (JsonElement catEl : catsArr) {
                JsonObject catObj = catEl.getAsJsonObject();
                String catName = catObj.get("name").getAsString();
                JsonArray settingsArr = catObj.getAsJsonArray("settings");
                List<SettingEntry> entries = new ArrayList<>();
                for (JsonElement sEl : settingsArr) {
                    JsonObject sObj = sEl.getAsJsonObject();
                    entries.add(new SettingEntry(
                        sObj.get("key").getAsString(),
                        sObj.get("name").getAsString(),
                        sObj.get("enabled").getAsBoolean()
                    ));
                }
                this.categories.add(new SettingCategory(catName, entries));
            }
        } catch (Exception e) {
            // ignore parse errors
        }
    }

    private void parseResult(String json) {
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            this.statusSuccess = obj.get("success").getAsBoolean();
            this.statusMessage = obj.get("message").getAsString();
            this.statusTicks = 60;
        } catch (Exception e) {
            // ignore
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.clickRects.clear();

        // Full background
        g.fill(0, 0, this.width, this.height, BG_DARK);

        // Title bar
        UIHelper.drawTitleBar(g, 0, 0, this.width, TITLE_BAR_H);
        Objects.requireNonNull(this.font);
        int titleY = (TITLE_BAR_H - 9) / 2;
        UIHelper.drawCenteredTitle(g, this.font, "Settings", this.width / 2, titleY);

        // Back button
        int backW = 50;
        int backH = 16;
        int backX = 8;
        int backY = (TITLE_BAR_H - backH) / 2;
        boolean backHover = mouseX >= backX && mouseX < backX + backW && mouseY >= backY && mouseY < backY + backH;
        UIHelper.drawButton(g, backX, backY, backW, backH, backHover);
        int backTextX = backX + (backW - this.font.width("< Back")) / 2;
        g.drawString(this.font, "< Back", backTextX, backY + (backH - 9) / 2, UIHelper.CREAM_TEXT, false);
        this.clickRects.add(new ClickRect(backX, backY, backW, backH, "back"));

        int contentTop = TITLE_BAR_H + 2;
        int contentBottom = this.height - 4;
        int contentH = contentBottom - contentTop;

        // Panel background
        int panelX = PADDING;
        int panelW = this.width - PADDING * 2;
        g.fill(panelX, contentTop, panelX + panelW, contentBottom, PANEL_BG);

        if (!this.dataLoaded) {
            String loadMsg = "Loading settings...";
            int loadW = this.font.width(loadMsg);
            g.drawString(this.font, loadMsg, (this.width - loadW) / 2, contentTop + contentH / 2 - 4, DIM_TEXT, false);
            super.render(g, mouseX, mouseY, partialTick);
            return;
        }

        // Calculate total content height
        int totalH = 0;
        for (SettingCategory cat : this.categories) {
            totalH += CAT_HEIGHT; // category header
            totalH += cat.settings.size() * ROW_HEIGHT; // setting rows
        }
        totalH += RESET_BTN_H + 12; // reset button + padding

        this.maxScroll = Math.max(0, totalH - contentH);
        this.scroll = Math.max(0, Math.min(this.scroll, this.maxScroll));

        // Scissor the content area
        g.enableScissor(panelX, contentTop, panelX + panelW, contentBottom);

        int drawY = contentTop + 4 - this.scroll;
        int rowIndex = 0;

        for (SettingCategory cat : this.categories) {
            // Category header
            if (drawY + CAT_HEIGHT > contentTop - CAT_HEIGHT && drawY < contentBottom) {
                g.drawString(this.font, cat.name, panelX + 8, drawY + (CAT_HEIGHT - 9) / 2, CAT_COLOR, false);
                // Divider below header
                g.fill(panelX + 8, drawY + CAT_HEIGHT - 2, panelX + panelW - 8, drawY + CAT_HEIGHT - 1, DIVIDER);
            }
            drawY += CAT_HEIGHT;

            for (SettingEntry entry : cat.settings) {
                if (drawY + ROW_HEIGHT > contentTop && drawY < contentBottom) {
                    // Alternating row background
                    int rowBg = (rowIndex % 2 == 0) ? ROW_EVEN : ROW_ODD;
                    g.fill(panelX + 4, drawY, panelX + panelW - 4, drawY + ROW_HEIGHT, rowBg);

                    // Hover highlight
                    boolean rowHover = mouseX >= panelX + 4 && mouseX < panelX + panelW - 4
                            && mouseY >= drawY && mouseY < drawY + ROW_HEIGHT;
                    if (rowHover) {
                        g.fill(panelX + 4, drawY, panelX + panelW - 4, drawY + ROW_HEIGHT, 0x10FFFFFF);
                    }

                    // Setting name
                    int textY = drawY + (ROW_HEIGHT - 9) / 2;
                    g.drawString(this.font, entry.name, panelX + 14, textY, TEXT_COLOR, false);

                    // Toggle switch on right side
                    int toggleX = panelX + panelW - 14 - TOGGLE_W;
                    int toggleY = drawY + (ROW_HEIGHT - TOGGLE_H) / 2;
                    renderToggle(g, toggleX, toggleY, entry.enabled, mouseX, mouseY);
                    this.clickRects.add(new ClickRect(toggleX - 2, toggleY - 2, TOGGLE_W + 4, TOGGLE_H + 4, "toggle:" + entry.key));
                }
                drawY += ROW_HEIGHT;
                rowIndex++;
            }
        }

        // Reset All button at bottom
        int resetX = panelX + (panelW - RESET_BTN_W) / 2;
        int resetY = drawY + 6;
        if (resetY + RESET_BTN_H > contentTop && resetY < contentBottom) {
            boolean resetHover = mouseX >= resetX && mouseX < resetX + RESET_BTN_W
                    && mouseY >= resetY && mouseY < resetY + RESET_BTN_H;
            // Red-tinted button for reset
            int resetBg = resetHover ? 0xFF8B2020 : 0xFF4A1A1A;
            int resetBorder = resetHover ? 0xFFCC4444 : 0xFF6B3030;
            g.fill(resetX, resetY, resetX + RESET_BTN_W, resetY + RESET_BTN_H, resetBorder);
            g.fill(resetX + 1, resetY + 1, resetX + RESET_BTN_W - 1, resetY + RESET_BTN_H - 1, resetBg);
            String resetLabel = "Reset All";
            int resetLabelX = resetX + (RESET_BTN_W - this.font.width(resetLabel)) / 2;
            g.drawString(this.font, resetLabel, resetLabelX, resetY + (RESET_BTN_H - 9) / 2, ERROR_RED, false);
            this.clickRects.add(new ClickRect(resetX, resetY, RESET_BTN_W, RESET_BTN_H, "reset"));
        }

        g.disableScissor();

        // Scrollbar
        if (this.maxScroll > 0) {
            float progress = (float) this.scroll / (float) this.maxScroll;
            UIHelper.drawScrollbar(g, panelX + panelW - 8, contentTop, contentH, progress);
        }

        // Status message overlay
        if (this.statusMessage != null && this.statusTicks > 0) {
            int msgW = this.font.width(this.statusMessage) + 16;
            int msgH = 16;
            int msgX = (this.width - msgW) / 2;
            int msgY = this.height - 28;
            int msgBg = this.statusSuccess ? 0xE0143D1E : 0xE0501010;
            int msgBorder = this.statusSuccess ? SUCCESS_GREEN : ERROR_RED;
            g.fill(msgX - 1, msgY - 1, msgX + msgW + 1, msgY + msgH + 1, msgBorder);
            g.fill(msgX, msgY, msgX + msgW, msgY + msgH, msgBg);
            int msgColor = this.statusSuccess ? SUCCESS_GREEN : ERROR_RED;
            g.drawString(this.font, this.statusMessage, msgX + 8, msgY + (msgH - 9) / 2, msgColor, false);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void renderToggle(GuiGraphics g, int x, int y, boolean enabled, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX < x + TOGGLE_W && mouseY >= y && mouseY < y + TOGGLE_H;
        int trackColor = enabled ? TOGGLE_ON : TOGGLE_OFF;
        if (hovered) {
            // Slightly brighter on hover
            trackColor = enabled ? 0xFF4FCA60 : 0xFF5A5A64;
        }

        // Track (rounded rect approximation)
        g.fill(x + 1, y, x + TOGGLE_W - 1, y + TOGGLE_H, trackColor);
        g.fill(x, y + 1, x + TOGGLE_W, y + TOGGLE_H - 1, trackColor);

        // Knob (white circle approximation)
        int knobX;
        if (enabled) {
            knobX = x + TOGGLE_W - TOGGLE_KNOB - 1;
        } else {
            knobX = x + 1;
        }
        int knobY = y + (TOGGLE_H - TOGGLE_KNOB) / 2;
        int knobColor = 0xFFFFFFFF;
        // Rounded knob
        g.fill(knobX + 1, knobY, knobX + TOGGLE_KNOB - 1, knobY + TOGGLE_KNOB, knobColor);
        g.fill(knobX, knobY + 1, knobX + TOGGLE_KNOB, knobY + TOGGLE_KNOB - 1, knobColor);
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean consumed) {
        if (consumed) {
            return super.mouseClicked(event, consumed);
        }
        int mx = (int) event.x();
        int my = (int) event.y();

        for (ClickRect rect : this.clickRects) {
            if (mx >= rect.x && mx < rect.x + rect.w && my >= rect.y && my < rect.y + rect.h) {
                if ("back".equals(rect.action)) {
                    Minecraft.getInstance().setScreen(this.parent);
                    return true;
                } else if (rect.action.startsWith("toggle:")) {
                    String key = rect.action.substring(7);
                    ClientPacketDistributor.sendToServer(
                        (CustomPacketPayload) new ComputerActionPayload("settings_toggle", key),
                        (CustomPacketPayload[]) new CustomPacketPayload[0]
                    );
                    // Optimistic local toggle for immediate feedback
                    for (SettingCategory cat : this.categories) {
                        for (int i = 0; i < cat.settings.size(); i++) {
                            SettingEntry entry = cat.settings.get(i);
                            if (entry.key.equals(key)) {
                                cat.settings.set(i, new SettingEntry(entry.key, entry.name, !entry.enabled));
                                break;
                            }
                        }
                    }
                    return true;
                } else if ("reset".equals(rect.action)) {
                    ClientPacketDistributor.sendToServer(
                        (CustomPacketPayload) new ComputerActionPayload("settings_reset", ""),
                        (CustomPacketPayload[]) new CustomPacketPayload[0]
                    );
                    return true;
                }
            }
        }

        return super.mouseClicked(event, consumed);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        this.scroll -= (int) (scrollY * 10);
        this.scroll = Math.max(0, Math.min(this.maxScroll, this.scroll));
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

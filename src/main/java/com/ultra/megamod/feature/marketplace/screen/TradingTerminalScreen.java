package com.ultra.megamod.feature.marketplace.screen;

import com.ultra.megamod.MegaMod;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ultra.megamod.feature.computer.network.ComputerActionPayload;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.ui.UIHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Client-side screen for interacting with a Trading Terminal block entity.
 * Uses the computer action handler pattern with terminal_ prefixed actions.
 */
public class TradingTerminalScreen extends Screen {

    // State from server
    private String trader1Uuid = "";
    private String trader2Uuid = "";
    private String trader1Name = "";
    private String trader2Name = "";

    private final List<ItemOffer> trader1Items = new ArrayList<>();
    private int trader1Coins = 0;
    private boolean trader1Confirmed = false;

    private final List<ItemOffer> trader2Items = new ArrayList<>();
    private int trader2Coins = 0;
    private boolean trader2Confirmed = false;

    private int timeRemaining = 120;

    // Local state
    private String coinsInput = "";
    private boolean coinsBoxFocused = false;
    private int cursorBlink = 0;
    private int refreshTimer = 0;
    private String statusMsg = "";
    private int statusTimer = 0;
    private boolean dataLoaded = false;
    private boolean tradeCancelled = false;
    private boolean tradeCompleted = false;
    private String completionMessage = "";
    private int completionTimer = 0; // auto-close after trade completes
    private static final int AUTO_CLOSE_TICKS = 60; // 3 seconds

    // Layout
    private int titleBarH;
    private final List<ClickRect> clickRects = new ArrayList<>();

    // Colors
    private static final int TERMINAL_CYAN = 0xFF26C6DA;
    private static final int CONFIRM_GREEN = 0xFF3FB950;
    private static final int CANCEL_RED = 0xFFFF6B6B;
    private static final int GOLD_COIN = 0xFFE8A838;
    private static final int BG_DARK = 0xFF0D1117;
    private static final int PANEL_BG = 0xFF161B22;
    private static final int ROW_EVEN = 0xFF1C2128;
    private static final int ROW_ODD = 0xFF21262D;
    private static final int OFFLINE_GREY = 0xFF4A4A50;
    private static final int LOCK_COLOR = 0xFF4ADE80;
    private static final int UNLOCK_COLOR = 0xFF6B7280;
    private static final int WARNING_AMBER = 0xFFFFB300;

    private record ItemOffer(String itemId, String itemName, int count) {}
    private record ClickRect(int x, int y, int w, int h, String action) {}

    public TradingTerminalScreen() {
        super(Component.literal("Trading Terminal"));
    }

    @Override
    protected void init() {
        super.init();
        Objects.requireNonNull(this.font);
        this.titleBarH = 9 + 16;
        if (!this.dataLoaded) {
            requestState();
        }
    }

    private void requestState() {
        ClientPacketDistributor.sendToServer(
                (CustomPacketPayload) new ComputerActionPayload("terminal_state", ""),
                (CustomPacketPayload[]) new CustomPacketPayload[0]
        );
    }

    @Override
    public void tick() {
        super.tick();
        this.cursorBlink++;

        if (this.statusTimer > 0) {
            this.statusTimer--;
            if (this.statusTimer <= 0) {
                this.statusMsg = "";
            }
        }

        // Poll for terminal state
        ComputerDataPayload resp = ComputerDataPayload.lastResponse;
        if (resp != null && resp.dataType().equals("terminal_state")) {
            ComputerDataPayload.lastResponse = null;
            parseTerminalState(resp.jsonData());
            this.dataLoaded = true;
        }

        if (resp != null && resp.dataType().equals("terminal_complete")) {
            ComputerDataPayload.lastResponse = null;
            this.tradeCompleted = true;
            this.completionTimer = 0;
            try {
                JsonObject obj = JsonParser.parseString(resp.jsonData()).getAsJsonObject();
                this.completionMessage = obj.has("message") ? obj.get("message").getAsString() : "Trade complete!";
            } catch (Exception e) {
                this.completionMessage = "Trade complete!";
            }
        }

        if (resp != null && resp.dataType().equals("terminal_cancelled")) {
            ComputerDataPayload.lastResponse = null;
            this.tradeCancelled = true;
            this.completionTimer = 0;
            try {
                JsonObject obj = JsonParser.parseString(resp.jsonData()).getAsJsonObject();
                this.completionMessage = obj.has("message") ? obj.get("message").getAsString() : "Trade cancelled.";
            } catch (Exception e) {
                this.completionMessage = "Trade cancelled.";
            }
        }
        // Consume error responses so the screen doesn't stay stuck
        if (resp != null && "error".equals(resp.dataType())) {
            ComputerDataPayload.lastResponse = null;
            this.dataLoaded = true;
        }

        // Auto-close after trade completes or cancels
        if (this.tradeCompleted || this.tradeCancelled) {
            this.completionTimer++;
            if (this.completionTimer >= AUTO_CLOSE_TICKS && this.minecraft != null) {
                this.minecraft.setScreen(null);
            }
        }

        // Auto-refresh every 20 ticks (1 second)
        this.refreshTimer++;
        if (this.refreshTimer >= 20 && !this.tradeCompleted && !this.tradeCancelled) {
            this.refreshTimer = 0;
            requestState();
        }
    }

    private void parseTerminalState(String json) {
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();

            this.trader1Uuid = obj.has("trader1Uuid") ? obj.get("trader1Uuid").getAsString() : "";
            this.trader2Uuid = obj.has("trader2Uuid") ? obj.get("trader2Uuid").getAsString() : "";
            this.trader1Name = obj.has("trader1Name") ? obj.get("trader1Name").getAsString() : "";
            this.trader2Name = obj.has("trader2Name") ? obj.get("trader2Name").getAsString() : "";

            // Trader 1 offer
            this.trader1Items.clear();
            if (obj.has("trader1Offer")) {
                JsonObject t1 = obj.getAsJsonObject("trader1Offer");
                this.trader1Coins = t1.get("coins").getAsInt();
                this.trader1Confirmed = t1.get("confirmed").getAsBoolean();
                if (t1.has("items")) {
                    JsonArray items = t1.getAsJsonArray("items");
                    for (JsonElement el : items) {
                        JsonObject item = el.getAsJsonObject();
                        this.trader1Items.add(new ItemOffer(
                                item.get("itemId").getAsString(),
                                item.get("itemName").getAsString(),
                                item.get("count").getAsInt()
                        ));
                    }
                }
            }

            // Trader 2 offer
            this.trader2Items.clear();
            if (obj.has("trader2Offer")) {
                JsonObject t2 = obj.getAsJsonObject("trader2Offer");
                this.trader2Coins = t2.get("coins").getAsInt();
                this.trader2Confirmed = t2.get("confirmed").getAsBoolean();
                if (t2.has("items")) {
                    JsonArray items = t2.getAsJsonArray("items");
                    for (JsonElement el : items) {
                        JsonObject item = el.getAsJsonObject();
                        this.trader2Items.add(new ItemOffer(
                                item.get("itemId").getAsString(),
                                item.get("itemName").getAsString(),
                                item.get("count").getAsInt()
                        ));
                    }
                }
            }

            this.timeRemaining = obj.has("timeRemaining") ? obj.get("timeRemaining").getAsInt() : 0;
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to parse trading terminal data", e);
        }
    }

    private boolean isTrader1() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return false;
        return mc.player.getUUID().toString().equals(this.trader1Uuid);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.clickRects.clear();

        g.fill(0, 0, this.width, this.height, BG_DARK);
        UIHelper.drawPanel(g, 0, 0, this.width, this.height);

        // Title bar
        UIHelper.drawTitleBar(g, 0, 0, this.width, this.titleBarH);
        Objects.requireNonNull(this.font);
        int titleY = (this.titleBarH - 9) / 2;
        UIHelper.drawCenteredTitle(g, this.font, "Trading Terminal", this.width / 2, titleY);

        // Timer in title bar
        int timerColor = this.timeRemaining > 60 ? CONFIRM_GREEN :
                (this.timeRemaining > 30 ? WARNING_AMBER : CANCEL_RED);
        String timerStr = this.timeRemaining + "s";
        g.drawString(this.font, timerStr, this.width - this.font.width(timerStr) - 8,
                titleY, timerColor, false);

        if (this.tradeCompleted || this.tradeCancelled) {
            renderCompletionScreen(g, mouseX, mouseY);
        } else if (!this.dataLoaded) {
            g.drawCenteredString(this.font, "Connecting...", this.width / 2, this.height / 2, OFFLINE_GREY);
        } else if (this.trader2Uuid.isEmpty()) {
            renderWaitingScreen(g, mouseX, mouseY);
        } else {
            renderTradeScreen(g, mouseX, mouseY);
        }

        // Status message
        if (!this.statusMsg.isEmpty() && this.statusTimer > 0) {
            int msgW = this.font.width(this.statusMsg) + 20;
            int msgX = (this.width - msgW) / 2;
            int msgY = this.height - 24;
            UIHelper.drawCard(g, msgX, msgY, msgW, 18);
            g.drawCenteredString(this.font, this.statusMsg, this.width / 2, msgY + 5, TERMINAL_CYAN);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void renderWaitingScreen(GuiGraphics g, int mouseX, int mouseY) {
        int centerY = this.height / 2 - 30;
        g.drawCenteredString(this.font, "Waiting for trade partner...", this.width / 2, centerY - 20, TERMINAL_CYAN);
        g.drawCenteredString(this.font, "Ask another player to right-click this terminal.",
                this.width / 2, centerY, OFFLINE_GREY);

        // How to Trade instructions
        int guideW = 220;
        int guideX = (this.width - guideW) / 2;
        int guideY = centerY + 20;
        int lineH = 12;

        g.drawCenteredString(this.font, "-- How to Trade --", this.width / 2, guideY, WARNING_AMBER);
        guideY += lineH + 4;

        String[] steps = {
                "1. Hold an item and click '+ Held Item'",
                "2. Enter a coin amount (optional)",
                "3. Review both offers carefully",
                "4. Both players click 'Confirm'",
                "5. Trade completes automatically!"
        };
        for (String step : steps) {
            g.drawCenteredString(this.font, step, this.width / 2, guideY, 0xFFB0B8C4);
            guideY += lineH;
        }

        // Cancel button
        int btnW = 80;
        int btnH = 18;
        int btnX = (this.width - btnW) / 2;
        int btnY = guideY + 14;
        boolean hover = mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY && mouseY < btnY + btnH;
        UIHelper.drawButton(g, btnX, btnY, btnW, btnH, hover);
        g.drawCenteredString(this.font, "Leave", btnX + btnW / 2, btnY + (btnH - 9) / 2, CANCEL_RED);
        this.clickRects.add(new ClickRect(btnX, btnY, btnW, btnH, "cancel"));
    }

    private void renderCompletionScreen(GuiGraphics g, int mouseX, int mouseY) {
        int centerY = this.height / 2;
        int color = this.tradeCompleted ? CONFIRM_GREEN : CANCEL_RED;
        g.drawCenteredString(this.font, this.completionMessage, this.width / 2, centerY - 10, color);

        // Auto-close countdown
        int secondsLeft = Math.max(0, (AUTO_CLOSE_TICKS - this.completionTimer) / 20 + 1);
        g.drawCenteredString(this.font, "Closing in " + secondsLeft + "s...",
                this.width / 2, centerY + 8, OFFLINE_GREY);

        // Close button (immediate)
        int btnW = 80;
        int btnH = 18;
        int btnX = (this.width - btnW) / 2;
        int btnY = centerY + 28;
        boolean hover = mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY && mouseY < btnY + btnH;
        UIHelper.drawButton(g, btnX, btnY, btnW, btnH, hover);
        g.drawCenteredString(this.font, "Close", btnX + btnW / 2, btnY + (btnH - 9) / 2, TERMINAL_CYAN);
        this.clickRects.add(new ClickRect(btnX, btnY, btnW, btnH, "close"));
    }

    private void renderTradeScreen(GuiGraphics g, int mouseX, int mouseY) {
        boolean amTrader1 = isTrader1();

        // Get "your" and "their" data
        String yourName = amTrader1 ? this.trader1Name : this.trader2Name;
        String theirName = amTrader1 ? this.trader2Name : this.trader1Name;
        List<ItemOffer> yourItems = amTrader1 ? this.trader1Items : this.trader2Items;
        int yourCoins = amTrader1 ? this.trader1Coins : this.trader2Coins;
        boolean yourConfirmed = amTrader1 ? this.trader1Confirmed : this.trader2Confirmed;
        List<ItemOffer> theirItems = amTrader1 ? this.trader2Items : this.trader1Items;
        int theirCoins = amTrader1 ? this.trader2Coins : this.trader1Coins;
        boolean theirConfirmed = amTrader1 ? this.trader2Confirmed : this.trader1Confirmed;

        int contentTop = this.titleBarH + 4;
        int contentBottom = this.height - 40;
        int halfW = (this.width - 12) / 2;

        // === LEFT: Your Offer ===
        int leftX = 4;
        UIHelper.drawInsetPanel(g, leftX, contentTop, halfW, contentBottom - contentTop);

        // Header
        String yourHeader = "Your Offer (" + yourName + ")";
        g.drawString(this.font, yourHeader, leftX + 6, contentTop + 4, TERMINAL_CYAN, false);

        // Confirmed indicator
        if (yourConfirmed) {
            g.drawString(this.font, "LOCKED", leftX + halfW - this.font.width("LOCKED") - 8,
                    contentTop + 4, LOCK_COLOR, false);
        }

        // Items section
        int itemsY = contentTop + 18;
        g.drawString(this.font, "Items:", leftX + 8, itemsY, UIHelper.GOLD_MID, false);

        // Add Held Item button
        if (!yourConfirmed) {
            int addBtnW = 70;
            int addBtnH = 14;
            int addBtnX = leftX + halfW - addBtnW - 8;
            boolean addHover = mouseX >= addBtnX && mouseX < addBtnX + addBtnW
                    && mouseY >= itemsY && mouseY < itemsY + addBtnH;
            UIHelper.drawButton(g, addBtnX, itemsY, addBtnW, addBtnH, addHover);
            g.drawString(this.font, "+ Held Item",
                    addBtnX + (addBtnW - this.font.width("+ Held Item")) / 2,
                    itemsY + 3, TERMINAL_CYAN, false);
            this.clickRects.add(new ClickRect(addBtnX, itemsY, addBtnW, addBtnH, "add_held"));
        }

        int listY = itemsY + 16;
        if (yourItems.isEmpty()) {
            g.drawString(this.font, "No items offered", leftX + 12, listY, OFFLINE_GREY, false);
            listY += 12;
        } else {
            for (int i = 0; i < yourItems.size(); i++) {
                ItemOffer offer = yourItems.get(i);
                int rowBg = i % 2 == 0 ? ROW_EVEN : ROW_ODD;
                g.fill(leftX + 4, listY, leftX + halfW - 4, listY + 14, rowBg);
                g.drawString(this.font, offer.count + "x " + offer.itemName,
                        leftX + 8, listY + 3, 0xFFE6EDF3, false);
                listY += 14;
            }
        }

        // Coins section
        listY += 6;
        g.drawString(this.font, "Coins:", leftX + 8, listY, UIHelper.GOLD_MID, false);

        if (!yourConfirmed) {
            int coinInputX = leftX + 8 + this.font.width("Coins: ") + 4;
            int coinInputW = 80;
            int coinInputH = 14;
            int coinBorder = this.coinsBoxFocused ? TERMINAL_CYAN : OFFLINE_GREY;
            g.fill(coinInputX - 1, listY - 1, coinInputX + coinInputW + 1, listY + coinInputH + 1, coinBorder);
            g.fill(coinInputX, listY, coinInputX + coinInputW, listY + coinInputH, 0xFF0D1117);

            if (this.coinsInput.isEmpty() && !this.coinsBoxFocused) {
                g.drawString(this.font, "0", coinInputX + 3, listY + 3, OFFLINE_GREY, false);
            } else {
                String clipped = this.coinsInput;
                int maxTextW = coinInputW - 8;
                while (this.font.width(clipped) > maxTextW && clipped.length() > 0) {
                    clipped = clipped.substring(1);
                }
                g.drawString(this.font, clipped, coinInputX + 3, listY + 3, 0xFFE6EDF3, false);
                if (this.coinsBoxFocused && (this.cursorBlink / 10) % 2 == 0) {
                    int cursorX = coinInputX + 3 + this.font.width(clipped);
                    g.fill(cursorX, listY + 2, cursorX + 1, listY + coinInputH - 2, TERMINAL_CYAN);
                }
            }
            this.clickRects.add(new ClickRect(coinInputX, listY, coinInputW, coinInputH, "focus_coins"));
            g.drawString(this.font, "MC", coinInputX + coinInputW + 4, listY + 3, GOLD_COIN, false);
        } else {
            g.drawString(this.font, yourCoins + " MC",
                    leftX + 8 + this.font.width("Coins: ") + 4, listY, GOLD_COIN, false);
        }

        // === RIGHT: Their Offer ===
        int rightX = leftX + halfW + 4;
        UIHelper.drawInsetPanel(g, rightX, contentTop, halfW, contentBottom - contentTop);

        String theirHeader = "Their Offer (" + theirName + ")";
        g.drawString(this.font, theirHeader, rightX + 6, contentTop + 4, WARNING_AMBER, false);

        if (theirConfirmed) {
            g.drawString(this.font, "LOCKED", rightX + halfW - this.font.width("LOCKED") - 8,
                    contentTop + 4, LOCK_COLOR, false);
        }

        // Their items
        int theirItemsY = contentTop + 18;
        g.drawString(this.font, "Items:", rightX + 8, theirItemsY, UIHelper.GOLD_MID, false);

        int theirListY = theirItemsY + 16;
        if (theirItems.isEmpty()) {
            g.drawString(this.font, "No items offered", rightX + 12, theirListY, OFFLINE_GREY, false);
            theirListY += 12;
        } else {
            for (int i = 0; i < theirItems.size(); i++) {
                ItemOffer offer = theirItems.get(i);
                int rowBg = i % 2 == 0 ? ROW_EVEN : ROW_ODD;
                g.fill(rightX + 4, theirListY, rightX + halfW - 4, theirListY + 14, rowBg);
                g.drawString(this.font, offer.count + "x " + offer.itemName,
                        rightX + 8, theirListY + 3, 0xFFE6EDF3, false);
                theirListY += 14;
            }
        }

        // Their coins
        theirListY += 6;
        g.drawString(this.font, "Coins:", rightX + 8, theirListY, UIHelper.GOLD_MID, false);
        g.drawString(this.font, theirCoins + " MC",
                rightX + 8 + this.font.width("Coins: ") + 4, theirListY, GOLD_COIN, false);

        // === Bottom buttons ===
        int btnY = contentBottom + 4;
        int btnH = 20;

        // Confirm button
        int confirmW = 100;
        int confirmX = this.width / 2 - confirmW - 8;
        boolean confirmHover = mouseX >= confirmX && mouseX < confirmX + confirmW
                && mouseY >= btnY && mouseY < btnY + btnH;
        if (yourConfirmed) {
            g.fill(confirmX, btnY, confirmX + confirmW, btnY + btnH, 0xFF1A3A1A);
            g.drawCenteredString(this.font, "Confirmed", confirmX + confirmW / 2,
                    btnY + (btnH - 9) / 2, LOCK_COLOR);
        } else {
            UIHelper.drawButton(g, confirmX, btnY, confirmW, btnH, confirmHover);
            g.drawCenteredString(this.font, "Confirm",
                    confirmX + confirmW / 2, btnY + (btnH - 9) / 2, CONFIRM_GREEN);
            this.clickRects.add(new ClickRect(confirmX, btnY, confirmW, btnH, "confirm"));
        }

        // Cancel button
        int cancelW = 100;
        int cancelX = this.width / 2 + 8;
        boolean cancelHover = mouseX >= cancelX && mouseX < cancelX + cancelW
                && mouseY >= btnY && mouseY < btnY + btnH;
        UIHelper.drawButton(g, cancelX, btnY, cancelW, btnH, cancelHover);
        g.drawCenteredString(this.font, "Cancel",
                cancelX + cancelW / 2, btnY + (btnH - 9) / 2, CANCEL_RED);
        this.clickRects.add(new ClickRect(cancelX, btnY, cancelW, btnH, "cancel"));

        // Status: "Both must confirm"
        if (!yourConfirmed || !theirConfirmed) {
            String hint;
            if (!yourConfirmed && !theirConfirmed) {
                hint = "Both players must confirm to complete the trade.";
            } else if (yourConfirmed) {
                hint = "Waiting for " + theirName + " to confirm...";
            } else {
                hint = theirName + " has confirmed. Review and confirm your offer.";
            }
            g.drawCenteredString(this.font, hint, this.width / 2, btnY + btnH + 6, OFFLINE_GREY);
        }
    }

    // --- Input handling ---

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean consumed) {
        if (consumed) return super.mouseClicked(event, consumed);
        int mx = (int) event.x();
        int my = (int) event.y();

        boolean clickedCoins = false;

        for (ClickRect r : this.clickRects) {
            if (mx >= r.x && mx < r.x + r.w && my >= r.y && my < r.y + r.h) {
                if (r.action.equals("focus_coins")) {
                    clickedCoins = true;
                } else {
                    handleClick(r.action);
                    return true;
                }
            }
        }

        this.coinsBoxFocused = clickedCoins;
        if (clickedCoins) {
            this.cursorBlink = 0;
            return true;
        }

        this.coinsBoxFocused = false;
        return super.mouseClicked(event, consumed);
    }

    private void handleClick(String action) {
        if (action.equals("close") || action.equals("cancel")) {
            if (action.equals("cancel") && !this.tradeCancelled && !this.tradeCompleted) {
                ClientPacketDistributor.sendToServer(
                        (CustomPacketPayload) new ComputerActionPayload("terminal_cancel", ""),
                        (CustomPacketPayload[]) new CustomPacketPayload[0]
                );
            }
            if (this.minecraft != null) {
                this.minecraft.setScreen(null);
            }
            return;
        }

        if (action.equals("confirm")) {
            // First send coin offer if the input has changed
            if (!this.coinsInput.isEmpty()) {
                ClientPacketDistributor.sendToServer(
                        (CustomPacketPayload) new ComputerActionPayload("terminal_offer_coins", this.coinsInput),
                        (CustomPacketPayload[]) new CustomPacketPayload[0]
                );
            }
            ClientPacketDistributor.sendToServer(
                    (CustomPacketPayload) new ComputerActionPayload("terminal_confirm", ""),
                    (CustomPacketPayload[]) new CustomPacketPayload[0]
            );
            return;
        }

        if (action.equals("add_held")) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                net.minecraft.world.item.ItemStack held = mc.player.getMainHandItem();
                if (!held.isEmpty()) {
                    net.minecraft.resources.Identifier itemKey = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(held.getItem());
                    if (itemKey != null) {
                        // Build the offer string from current items + new held item
                        String itemId = itemKey.toString();
                        int count = held.getCount();

                        // Send as comma-separated items
                        StringBuilder offerStr = new StringBuilder();
                        boolean amTrader1 = isTrader1();
                        List<ItemOffer> currentItems = amTrader1 ? this.trader1Items : this.trader2Items;

                        // Include existing items
                        for (ItemOffer existing : currentItems) {
                            if (offerStr.length() > 0) offerStr.append(",");
                            offerStr.append(existing.itemId).append(":").append(existing.count);
                        }

                        // Add the new item
                        if (offerStr.length() > 0) offerStr.append(",");
                        offerStr.append(itemId).append(":").append(count);

                        ClientPacketDistributor.sendToServer(
                                (CustomPacketPayload) new ComputerActionPayload("terminal_offer_items", offerStr.toString()),
                                (CustomPacketPayload[]) new CustomPacketPayload[0]
                        );
                    }
                } else {
                    this.statusMsg = "Hold an item in your main hand.";
                    this.statusTimer = 60;
                }
            }
            return;
        }
    }

    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();

        if (keyCode == 256) { // Escape
            // Cancel trade and close
            if (!this.tradeCancelled && !this.tradeCompleted) {
                ClientPacketDistributor.sendToServer(
                        (CustomPacketPayload) new ComputerActionPayload("terminal_cancel", ""),
                        (CustomPacketPayload[]) new CustomPacketPayload[0]
                );
            }
            if (this.minecraft != null) {
                this.minecraft.setScreen(null);
            }
            return true;
        }

        if (keyCode == 257 && this.coinsBoxFocused) { // Enter
            if (!this.coinsInput.isEmpty()) {
                ClientPacketDistributor.sendToServer(
                        (CustomPacketPayload) new ComputerActionPayload("terminal_offer_coins", this.coinsInput),
                        (CustomPacketPayload[]) new CustomPacketPayload[0]
                );
            }
            this.coinsBoxFocused = false;
            return true;
        }

        if (keyCode == 259 && this.coinsBoxFocused && !this.coinsInput.isEmpty()) { // Backspace
            this.coinsInput = this.coinsInput.substring(0, this.coinsInput.length() - 1);
            this.cursorBlink = 0;
            return true;
        }

        return super.keyPressed(event);
    }

    public boolean charTyped(net.minecraft.client.input.CharacterEvent event) {
        char codePoint = (char) event.codepoint();
        int modifiers = event.modifiers();
        if (codePoint < '0' || codePoint > '9') return false;

        if (this.coinsBoxFocused) {
            if (this.coinsInput.length() < 7) {
                this.coinsInput += codePoint;
                this.cursorBlink = 0;
            }
            return true;
        }

        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

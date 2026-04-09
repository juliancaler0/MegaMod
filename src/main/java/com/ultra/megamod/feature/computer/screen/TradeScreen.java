package com.ultra.megamod.feature.computer.screen;

import com.ultra.megamod.MegaMod;
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
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class TradeScreen extends Screen {
    private final Screen parent;
    private int scroll = 0;
    private String statusMsg = "";
    private int statusTimer = 0;
    private boolean dataLoaded = false;
    private int refreshTimer = 0;
    private int cursorBlink = 0;

    // Data from server
    private List<PlayerEntry> onlinePlayers = new ArrayList<>();
    private List<TradeOfferEntry> incomingOffers = new ArrayList<>();
    private TradeOfferEntry outgoingOffer = null;

    // Compose state
    private boolean composingOffer = false;
    private String offerTarget = "";
    private String offerTargetUuid = "";
    private String coinsInput = "";
    private boolean coinsBoxFocused = false;
    private String selectedItemId = "";
    private String selectedItemName = "";
    private int selectedItemCount = 0;

    // Layout
    private int titleBarH;
    private int contentTop;
    private final List<ClickRect> clickRects = new ArrayList<>();

    // Colors
    private static final int TRADE_GOLD = 0xFFE8A838;
    private static final int ONLINE_GREEN = 0xFF3FB950;
    private static final int ERROR_RED = 0xFFFF6B6B;
    private static final int SUCCESS_GREEN = 0xFF3FB950;
    private static final int BG_DARK = 0xFF0D1117;
    private static final int PANEL_BG = 0xFF161B22;
    private static final int ROW_EVEN = 0xFF1C2128;
    private static final int ROW_ODD = 0xFF21262D;
    private static final int ROW_HEIGHT = 22;
    private static final int OFFLINE_GREY = 0xFF4A4A50;
    private static final int REQUEST_BLUE = 0xFF58A6FF;

    private record PlayerEntry(String name, String uuid) {}
    private record TradeOfferEntry(String name, String uuid, int coins, String itemId,
                                   String itemName, int itemCount, int timeLeft) {}
    private record ClickRect(int x, int y, int w, int h, String action) {}

    public TradeScreen(Screen parent) {
        super(Component.literal("Trade"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        Objects.requireNonNull(this.font);
        this.titleBarH = 9 + 16;
        this.contentTop = this.titleBarH + 6;
        if (!this.dataLoaded) {
            requestTradeData();
        }
    }

    private void requestTradeData() {
        ClientPacketDistributor.sendToServer(
            (CustomPacketPayload) new ComputerActionPayload("trade_request", ""),
            (CustomPacketPayload[]) new CustomPacketPayload[0]
        );
    }

    @Override
    public void tick() {
        super.tick();
        this.cursorBlink++;

        // Status message timer
        if (this.statusTimer > 0) {
            this.statusTimer--;
            if (this.statusTimer <= 0) {
                this.statusMsg = "";
            }
        }

        // Poll for data response
        ComputerDataPayload resp = ComputerDataPayload.lastResponse;
        if (resp != null && resp.dataType().equals("trade_data")) {
            ComputerDataPayload.lastResponse = null;
            parseTradeData(resp.jsonData());
            this.dataLoaded = true;
        }

        // Check for action results
        if (resp != null && resp.dataType().equals("trade_result")) {
            ComputerDataPayload.lastResponse = null;
            parseResult(resp.jsonData());
        }

        // Consume error responses so the screen doesn't stay stuck
        if (resp != null && "error".equals(resp.dataType())) {
            ComputerDataPayload.lastResponse = null;
            this.dataLoaded = true;
        }

        // Auto-refresh every 40 ticks (2 seconds)
        this.refreshTimer++;
        if (this.refreshTimer >= 40) {
            this.refreshTimer = 0;
            requestTradeData();
        }
    }

    private void parseTradeData(String json) {
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();

            this.onlinePlayers.clear();
            if (obj.has("onlinePlayers")) {
                JsonArray arr = obj.getAsJsonArray("onlinePlayers");
                for (JsonElement el : arr) {
                    JsonObject p = el.getAsJsonObject();
                    this.onlinePlayers.add(new PlayerEntry(
                        p.get("name").getAsString(),
                        p.get("uuid").getAsString()
                    ));
                }
            }
            // Sort alphabetically
            this.onlinePlayers.sort((a, b) -> String.CASE_INSENSITIVE_ORDER.compare(a.name, b.name));

            this.incomingOffers.clear();
            if (obj.has("incomingOffers")) {
                JsonArray arr = obj.getAsJsonArray("incomingOffers");
                for (JsonElement el : arr) {
                    JsonObject o = el.getAsJsonObject();
                    this.incomingOffers.add(new TradeOfferEntry(
                        o.get("from").getAsString(),
                        o.get("fromUuid").getAsString(),
                        o.get("coins").getAsInt(),
                        o.has("itemId") ? o.get("itemId").getAsString() : "",
                        o.has("itemName") ? o.get("itemName").getAsString() : "",
                        o.get("itemCount").getAsInt(),
                        o.get("timeLeft").getAsInt()
                    ));
                }
            }

            if (obj.has("outgoingOffer") && !obj.get("outgoingOffer").isJsonNull()) {
                JsonObject out = obj.getAsJsonObject("outgoingOffer");
                this.outgoingOffer = new TradeOfferEntry(
                    out.get("to").getAsString(),
                    "", // no UUID needed for display
                    out.get("coins").getAsInt(),
                    out.has("itemId") ? out.get("itemId").getAsString() : "",
                    out.has("itemName") ? out.get("itemName").getAsString() : "",
                    out.get("itemCount").getAsInt(),
                    out.get("timeLeft").getAsInt()
                );
            } else {
                this.outgoingOffer = null;
            }
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to parse trade data", e);
        }
    }

    private void parseResult(String json) {
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            boolean success = obj.has("success") && obj.get("success").getAsBoolean();
            String msg = obj.has("message") ? obj.get("message").getAsString() : (success ? "Done!" : "Failed.");
            this.statusMsg = msg;
            this.statusTimer = 80;
            if (success && this.composingOffer) {
                this.composingOffer = false;
                this.coinsInput = "";
                this.coinsBoxFocused = false;
                this.selectedItemId = "";
                this.selectedItemName = "";
                this.selectedItemCount = 0;
            }
            // Refresh data
            requestTradeData();
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to handle trade action response", e);
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.clickRects.clear();

        // Background
        g.fill(0, 0, this.width, this.height, BG_DARK);
        UIHelper.drawPanel(g, 0, 0, this.width, this.height);

        // Title bar
        UIHelper.drawTitleBar(g, 0, 0, this.width, this.titleBarH);
        Objects.requireNonNull(this.font);
        int titleY = (this.titleBarH - 9) / 2;
        UIHelper.drawCenteredTitle(g, this.font, "Trade", this.width / 2, titleY);

        // Back button
        int backW = 50;
        int backH = 16;
        int backX = 8;
        int backY = (this.titleBarH - backH) / 2;
        boolean backHover = mouseX >= backX && mouseX < backX + backW && mouseY >= backY && mouseY < backY + backH;
        UIHelper.drawButton(g, backX, backY, backW, backH, backHover);
        int backTextX = backX + (backW - this.font.width("< Back")) / 2;
        g.drawString(this.font, "< Back", backTextX, backY + (backH - 9) / 2, -661816, false);
        this.clickRects.add(new ClickRect(backX, backY, backW, backH, "back"));

        if (this.composingOffer) {
            renderComposeOffer(g, mouseX, mouseY);
        } else {
            renderMainView(g, mouseX, mouseY);
        }

        // Status message at bottom
        if (!this.statusMsg.isEmpty() && this.statusTimer > 0) {
            int msgW = this.font.width(this.statusMsg) + 20;
            int msgX = (this.width - msgW) / 2;
            int msgY = this.height - 24;
            UIHelper.drawCard(g, msgX, msgY, msgW, 18);
            boolean isError = this.statusMsg.contains("Not enough") || this.statusMsg.contains("Failed")
                    || this.statusMsg.contains("already") || this.statusMsg.contains("Cannot")
                    || this.statusMsg.contains("expired") || this.statusMsg.contains("yourself")
                    || this.statusMsg.contains("offline") || this.statusMsg.contains("Invalid")
                    || this.statusMsg.contains("negative") || this.statusMsg.contains("no longer")
                    || this.statusMsg.contains("don't have") || this.statusMsg.contains("must offer");
            int msgColor = isError ? ERROR_RED : SUCCESS_GREEN;
            g.drawCenteredString(this.font, this.statusMsg, this.width / 2, msgY + 5, msgColor);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void renderMainView(GuiGraphics g, int mouseX, int mouseY) {
        int leftPanelW = (int) (this.width * 0.6);
        int rightPanelX = leftPanelW + 4;
        int rightPanelW = this.width - rightPanelX - 4;
        int panelTop = this.contentTop;
        int panelBottom = this.height - 30;

        // === LEFT PANEL: Online Players ===
        UIHelper.drawInsetPanel(g, 4, panelTop, leftPanelW - 4, panelBottom - panelTop);

        // Header
        String countStr = "Online Players: " + this.onlinePlayers.size();
        g.drawString(this.font, countStr, 10, panelTop + 4, UIHelper.GOLD_MID, false);

        // Player list area
        int listTop = panelTop + 16;
        int listBottom = panelBottom - 6;
        int listX = 8;
        int listW = leftPanelW - 20;

        g.enableScissor(listX, listTop, listX + listW, listBottom);

        int visibleH = listBottom - listTop;
        int totalH = this.onlinePlayers.size() * ROW_HEIGHT;
        int maxScroll = Math.max(0, totalH - visibleH);
        this.scroll = Math.max(0, Math.min(this.scroll, maxScroll));

        if (this.onlinePlayers.isEmpty()) {
            g.drawString(this.font, "No other players online.", listX + 10, listTop + 10, OFFLINE_GREY, false);
        }

        for (int i = 0; i < this.onlinePlayers.size(); i++) {
            int rowY = listTop + i * ROW_HEIGHT - this.scroll;
            if (rowY + ROW_HEIGHT < listTop || rowY > listBottom) continue;

            PlayerEntry p = this.onlinePlayers.get(i);
            boolean rowHover = mouseX >= listX && mouseX < listX + listW && mouseY >= rowY && mouseY < rowY + ROW_HEIGHT
                    && mouseY >= listTop && mouseY < listBottom;

            // Row background
            int rowBg = i % 2 == 0 ? ROW_EVEN : ROW_ODD;
            g.fill(listX, rowY, listX + listW, rowY + ROW_HEIGHT, rowBg);
            if (rowHover) {
                g.fill(listX, rowY, listX + listW, rowY + ROW_HEIGHT, 0x18FFFFFF);
            }

            // Online dot
            int dotX = listX + 6;
            int dotY = rowY + (ROW_HEIGHT - 4) / 2;
            g.fill(dotX, dotY, dotX + 4, dotY + 4, ONLINE_GREEN);

            // Name
            g.drawString(this.font, p.name, listX + 14, rowY + (ROW_HEIGHT - 9) / 2, 0xFFFFFFFF, false);

            // Offer button
            int btnW = 40;
            int btnH = ROW_HEIGHT - 6;
            int btnX = listX + listW - btnW - 4;
            int btnY = rowY + 3;
            boolean btnHover = mouseX >= btnX && mouseX < btnX + btnW
                    && mouseY >= btnY && mouseY < btnY + btnH
                    && mouseY >= listTop && mouseY < listBottom;
            UIHelper.drawButton(g, btnX, btnY, btnW, btnH, btnHover);
            int labelX = btnX + (btnW - this.font.width("Offer")) / 2;
            g.drawString(this.font, "Offer", labelX, btnY + (btnH - 9) / 2, TRADE_GOLD, false);
            this.clickRects.add(new ClickRect(btnX, btnY, btnW, btnH, "offer:" + p.uuid + ":" + p.name));
        }

        g.disableScissor();

        // Scrollbar
        if (totalH > visibleH) {
            float progress = maxScroll > 0 ? (float) this.scroll / (float) maxScroll : 0.0f;
            UIHelper.drawScrollbar(g, listX + listW + 2, listTop, listBottom - listTop, progress);
        }

        // === RIGHT PANEL: Trade Status ===
        UIHelper.drawInsetPanel(g, rightPanelX, panelTop, rightPanelW, panelBottom - panelTop);

        int rY = panelTop + 4;

        // Incoming Offers Section
        g.drawString(this.font, "Incoming Offers", rightPanelX + 6, rY, UIHelper.GOLD_MID, false);
        rY += 14;

        if (this.incomingOffers.isEmpty()) {
            g.drawString(this.font, "None", rightPanelX + 10, rY, OFFLINE_GREY, false);
            rY += 14;
        } else {
            for (int i = 0; i < this.incomingOffers.size(); i++) {
                TradeOfferEntry offer = this.incomingOffers.get(i);
                if (rY + 50 > panelBottom - 6) break;

                int cardBg = i % 2 == 0 ? ROW_EVEN : ROW_ODD;
                int cardH = 46;
                g.fill(rightPanelX + 4, rY, rightPanelX + rightPanelW - 4, rY + cardH, cardBg);

                // From + timer
                int timerColor = offer.timeLeft > 30 ? ONLINE_GREEN : (offer.timeLeft > 10 ? TRADE_GOLD : ERROR_RED);
                String fromLabel = offer.name + " (" + offer.timeLeft + "s)";
                g.drawString(this.font, fromLabel, rightPanelX + 8, rY + 3, timerColor, false);

                // Offer contents
                StringBuilder contents = new StringBuilder();
                if (offer.coins > 0) {
                    contents.append(offer.coins).append(" MC");
                }
                if (!offer.itemId.isEmpty() && offer.itemCount > 0) {
                    if (contents.length() > 0) contents.append(" + ");
                    String displayName = offer.itemName.isEmpty() ? offer.itemId : offer.itemName;
                    contents.append(offer.itemCount).append("x ").append(displayName);
                }
                g.drawString(this.font, contents.toString(), rightPanelX + 8, rY + 15, TRADE_GOLD, false);

                // Accept button
                int accBtnW = 46;
                int accBtnH = 14;
                int accBtnX = rightPanelX + 8;
                int accBtnY = rY + 28;
                boolean accHover = mouseX >= accBtnX && mouseX < accBtnX + accBtnW
                        && mouseY >= accBtnY && mouseY < accBtnY + accBtnH;
                UIHelper.drawButton(g, accBtnX, accBtnY, accBtnW, accBtnH, accHover);
                g.drawString(this.font, "Accept", accBtnX + (accBtnW - this.font.width("Accept")) / 2, accBtnY + 3, SUCCESS_GREEN, false);
                this.clickRects.add(new ClickRect(accBtnX, accBtnY, accBtnW, accBtnH, "accept:" + offer.uuid));

                // Decline button
                int decBtnW = 50;
                int decBtnX = accBtnX + accBtnW + 4;
                boolean decHover = mouseX >= decBtnX && mouseX < decBtnX + decBtnW
                        && mouseY >= accBtnY && mouseY < accBtnY + accBtnH;
                UIHelper.drawButton(g, decBtnX, accBtnY, decBtnW, accBtnH, decHover);
                g.drawString(this.font, "Decline", decBtnX + (decBtnW - this.font.width("Decline")) / 2, accBtnY + 3, ERROR_RED, false);
                this.clickRects.add(new ClickRect(decBtnX, accBtnY, decBtnW, accBtnH, "decline:" + offer.uuid));

                rY += cardH + 4;
            }
        }

        // Divider
        rY += 4;
        UIHelper.drawHorizontalDivider(g, rightPanelX + 6, rY, rightPanelW - 12);
        rY += 8;

        // Outgoing Offer Section
        g.drawString(this.font, "Your Offer", rightPanelX + 6, rY, UIHelper.GOLD_MID, false);
        rY += 14;

        if (this.outgoingOffer == null) {
            g.drawString(this.font, "Select a player to trade", rightPanelX + 10, rY, OFFLINE_GREY, false);
            g.drawString(this.font, "with from the list.", rightPanelX + 10, rY + 12, OFFLINE_GREY, false);
        } else {
            // Show outgoing offer details
            String toLabel = "To: " + this.outgoingOffer.name;
            int timerColor = this.outgoingOffer.timeLeft > 30 ? ONLINE_GREEN :
                    (this.outgoingOffer.timeLeft > 10 ? TRADE_GOLD : ERROR_RED);
            g.drawString(this.font, toLabel, rightPanelX + 8, rY, REQUEST_BLUE, false);
            rY += 12;

            // Offer contents
            StringBuilder contents = new StringBuilder();
            if (this.outgoingOffer.coins > 0) {
                contents.append(this.outgoingOffer.coins).append(" MC");
            }
            if (!this.outgoingOffer.itemId.isEmpty() && this.outgoingOffer.itemCount > 0) {
                if (contents.length() > 0) contents.append(" + ");
                String displayName = this.outgoingOffer.itemName.isEmpty() ? this.outgoingOffer.itemId : this.outgoingOffer.itemName;
                contents.append(this.outgoingOffer.itemCount).append("x ").append(displayName);
            }
            g.drawString(this.font, contents.toString(), rightPanelX + 8, rY, TRADE_GOLD, false);
            rY += 12;

            g.drawString(this.font, "Waiting... (" + this.outgoingOffer.timeLeft + "s)", rightPanelX + 8, rY, timerColor, false);
            rY += 14;

            // Cancel button
            int cancelBtnW = 50;
            int cancelBtnH = 14;
            int cancelBtnX = rightPanelX + 8;
            boolean cancelHover = mouseX >= cancelBtnX && mouseX < cancelBtnX + cancelBtnW
                    && mouseY >= rY && mouseY < rY + cancelBtnH;
            UIHelper.drawButton(g, cancelBtnX, rY, cancelBtnW, cancelBtnH, cancelHover);
            g.drawString(this.font, "Cancel", cancelBtnX + (cancelBtnW - this.font.width("Cancel")) / 2, rY + 3, ERROR_RED, false);
            this.clickRects.add(new ClickRect(cancelBtnX, rY, cancelBtnW, cancelBtnH, "cancel_offer"));
        }
    }

    private void renderComposeOffer(GuiGraphics g, int mouseX, int mouseY) {
        int panelW = Math.min(340, this.width - 20);
        int panelH = 140;
        int panelX = (this.width - panelW) / 2;
        int panelY = this.height / 2 - panelH / 2 - 10;

        UIHelper.drawPanel(g, panelX, panelY, panelW, panelH);

        // "To:" label
        int toY = panelY + 10;
        g.drawString(this.font, "To:", panelX + 10, toY, UIHelper.GOLD_DARK, false);
        g.drawString(this.font, this.offerTarget, panelX + 10 + this.font.width("To: "), toY, UIHelper.GOLD_BRIGHT, false);

        // Coins input
        int coinsLabelY = panelY + 28;
        g.drawString(this.font, "Coins:", panelX + 10, coinsLabelY, UIHelper.GOLD_DARK, false);

        int inputX = panelX + 10 + this.font.width("Coins: ") + 4;
        int inputW = 80;
        int inputH = 14;

        int inputBorder = this.coinsBoxFocused ? REQUEST_BLUE : OFFLINE_GREY;
        g.fill(inputX - 1, coinsLabelY - 1, inputX + inputW + 1, coinsLabelY + inputH + 1, inputBorder);
        g.fill(inputX, coinsLabelY, inputX + inputW, coinsLabelY + inputH, 0xFF0D1117);

        if (this.coinsInput.isEmpty() && !this.coinsBoxFocused) {
            g.drawString(this.font, "0", inputX + 3, coinsLabelY + 3, OFFLINE_GREY, false);
        } else {
            String clipped = this.coinsInput;
            int maxTextW = inputW - 8;
            while (this.font.width(clipped) > maxTextW && clipped.length() > 0) {
                clipped = clipped.substring(1);
            }
            g.drawString(this.font, clipped, inputX + 3, coinsLabelY + 3, 0xFFE6EDF3, false);
            if (this.coinsBoxFocused && (this.cursorBlink / 10) % 2 == 0) {
                int cursorX = inputX + 3 + this.font.width(clipped);
                g.fill(cursorX, coinsLabelY + 2, cursorX + 1, coinsLabelY + inputH - 2, REQUEST_BLUE);
            }
        }
        this.clickRects.add(new ClickRect(inputX, coinsLabelY, inputW, inputH, "focus_coins"));

        g.drawString(this.font, "MC", inputX + inputW + 4, coinsLabelY + 3, TRADE_GOLD, false);

        // Item section
        int itemLabelY = panelY + 50;
        g.drawString(this.font, "Item:", panelX + 10, itemLabelY, UIHelper.GOLD_DARK, false);

        if (this.selectedItemId.isEmpty()) {
            // "Held Item" button
            int heldBtnW = 70;
            int heldBtnH = 14;
            int heldBtnX = panelX + 10 + this.font.width("Item: ") + 4;
            boolean heldHover = mouseX >= heldBtnX && mouseX < heldBtnX + heldBtnW
                    && mouseY >= itemLabelY && mouseY < itemLabelY + heldBtnH;
            UIHelper.drawButton(g, heldBtnX, itemLabelY, heldBtnW, heldBtnH, heldHover);
            g.drawString(this.font, "Held Item", heldBtnX + (heldBtnW - this.font.width("Held Item")) / 2, itemLabelY + 3, REQUEST_BLUE, false);
            this.clickRects.add(new ClickRect(heldBtnX, itemLabelY, heldBtnW, heldBtnH, "select_held"));

            // "None" label
            int noneBtnW = 40;
            int noneBtnX = heldBtnX + heldBtnW + 6;
            boolean noneHover = mouseX >= noneBtnX && mouseX < noneBtnX + noneBtnW
                    && mouseY >= itemLabelY && mouseY < itemLabelY + heldBtnH;
            UIHelper.drawButton(g, noneBtnX, itemLabelY, noneBtnW, 14, noneHover);
            g.drawString(this.font, "None", noneBtnX + (noneBtnW - this.font.width("None")) / 2, itemLabelY + 3, OFFLINE_GREY, false);
            this.clickRects.add(new ClickRect(noneBtnX, itemLabelY, noneBtnW, 14, "select_none"));
        } else {
            // Show selected item
            String displayStr = this.selectedItemCount + "x " + this.selectedItemName;
            g.drawString(this.font, displayStr, panelX + 10 + this.font.width("Item: ") + 4, itemLabelY, TRADE_GOLD, false);

            // Clear button
            int clearBtnW = 14;
            int clearBtnX = panelX + panelW - clearBtnW - 10;
            boolean clearHover = mouseX >= clearBtnX && mouseX < clearBtnX + clearBtnW
                    && mouseY >= itemLabelY && mouseY < itemLabelY + 14;
            UIHelper.drawButton(g, clearBtnX, itemLabelY, clearBtnW, 14, clearHover);
            g.drawString(this.font, "X", clearBtnX + (clearBtnW - this.font.width("X")) / 2, itemLabelY + 3, ERROR_RED, false);
            this.clickRects.add(new ClickRect(clearBtnX, itemLabelY, clearBtnW, 14, "clear_item"));
        }

        // Hint text
        int hintY = panelY + 70;
        g.drawString(this.font, "Offer coins, an item, or both.", panelX + 10, hintY, OFFLINE_GREY, false);

        // Send Offer button
        int sendW = 80;
        int sendH = 18;
        int sendX = panelX + panelW / 2 - sendW - 5;
        int sendY = panelY + panelH - sendH - 10;
        boolean sendHover = mouseX >= sendX && mouseX < sendX + sendW && mouseY >= sendY && mouseY < sendY + sendH;
        UIHelper.drawButton(g, sendX, sendY, sendW, sendH, sendHover);
        int sendTextX = sendX + (sendW - this.font.width("Send Offer")) / 2;
        g.drawString(this.font, "Send Offer", sendTextX, sendY + (sendH - 9) / 2, SUCCESS_GREEN, false);
        this.clickRects.add(new ClickRect(sendX, sendY, sendW, sendH, "send_offer"));

        // Cancel button
        int cancelW = 60;
        int cancelX = panelX + panelW / 2 + 5;
        boolean cancelHover = mouseX >= cancelX && mouseX < cancelX + cancelW && mouseY >= sendY && mouseY < sendY + sendH;
        UIHelper.drawButton(g, cancelX, sendY, cancelW, sendH, cancelHover);
        int cancelTextX = cancelX + (cancelW - this.font.width("Cancel")) / 2;
        g.drawString(this.font, "Cancel", cancelTextX, sendY + (sendH - 9) / 2, ERROR_RED, false);
        this.clickRects.add(new ClickRect(cancelX, sendY, cancelW, sendH, "cancel_compose"));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean consumed) {
        if (consumed) {
            return super.mouseClicked(event, consumed);
        }
        int mx = (int) event.x();
        int my = (int) event.y();

        boolean clickedCoinsBox = false;

        for (ClickRect r : this.clickRects) {
            if (mx >= r.x && mx < r.x + r.w && my >= r.y && my < r.y + r.h) {
                if (r.action.equals("focus_coins")) {
                    clickedCoinsBox = true;
                } else {
                    handleClick(r.action);
                    return true;
                }
            }
        }

        this.coinsBoxFocused = clickedCoinsBox;
        if (clickedCoinsBox) {
            this.cursorBlink = 0;
            return true;
        }

        // Clicked outside any rect
        this.coinsBoxFocused = false;
        return super.mouseClicked(event, consumed);
    }

    private void handleClick(String action) {
        if (action.equals("back")) {
            if (this.composingOffer) {
                this.composingOffer = false;
                this.coinsInput = "";
                this.coinsBoxFocused = false;
                this.selectedItemId = "";
                this.selectedItemName = "";
                this.selectedItemCount = 0;
            } else if (this.minecraft != null) {
                this.minecraft.setScreen(this.parent);
            }
            return;
        }

        if (action.startsWith("offer:")) {
            // Format: "offer:uuid:name"
            String[] parts = action.substring(6).split(":", 2);
            if (parts.length == 2) {
                this.offerTargetUuid = parts[0];
                this.offerTarget = parts[1];
                this.composingOffer = true;
                this.coinsInput = "";
                this.coinsBoxFocused = true;
                this.cursorBlink = 0;
                this.selectedItemId = "";
                this.selectedItemName = "";
                this.selectedItemCount = 0;
            }
            return;
        }

        if (action.equals("select_held")) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                ItemStack held = mc.player.getMainHandItem();
                if (!held.isEmpty()) {
                    this.selectedItemId = BuiltInRegistries.ITEM.getKey(held.getItem()).toString();
                    this.selectedItemName = held.getHoverName().getString();
                    this.selectedItemCount = held.getCount();
                } else {
                    this.statusMsg = "Not holding any item.";
                    this.statusTimer = 60;
                }
            }
            return;
        }

        if (action.equals("select_none")) {
            this.selectedItemId = "";
            this.selectedItemName = "";
            this.selectedItemCount = 0;
            return;
        }

        if (action.equals("clear_item")) {
            this.selectedItemId = "";
            this.selectedItemName = "";
            this.selectedItemCount = 0;
            return;
        }

        if (action.equals("send_offer")) {
            sendTradeOffer();
            return;
        }

        if (action.equals("cancel_compose")) {
            this.composingOffer = false;
            this.coinsInput = "";
            this.coinsBoxFocused = false;
            this.selectedItemId = "";
            this.selectedItemName = "";
            this.selectedItemCount = 0;
            return;
        }

        if (action.startsWith("accept:")) {
            String uuid = action.substring(7);
            ClientPacketDistributor.sendToServer(
                (CustomPacketPayload) new ComputerActionPayload("trade_accept", uuid),
                (CustomPacketPayload[]) new CustomPacketPayload[0]
            );
            return;
        }

        if (action.startsWith("decline:")) {
            String uuid = action.substring(8);
            ClientPacketDistributor.sendToServer(
                (CustomPacketPayload) new ComputerActionPayload("trade_decline", uuid),
                (CustomPacketPayload[]) new CustomPacketPayload[0]
            );
            return;
        }

        if (action.equals("cancel_offer")) {
            ClientPacketDistributor.sendToServer(
                (CustomPacketPayload) new ComputerActionPayload("trade_cancel", ""),
                (CustomPacketPayload[]) new CustomPacketPayload[0]
            );
            return;
        }
    }

    private void sendTradeOffer() {
        int coins = 0;
        if (!this.coinsInput.isEmpty()) {
            try {
                coins = Integer.parseInt(this.coinsInput);
            } catch (NumberFormatException e) {
                this.statusMsg = "Invalid coin amount.";
                this.statusTimer = 60;
                return;
            }
        }

        if (coins <= 0 && this.selectedItemId.isEmpty()) {
            this.statusMsg = "You must offer coins or an item.";
            this.statusTimer = 60;
            return;
        }

        String itemId = this.selectedItemId;
        int itemCount = this.selectedItemCount;
        if (itemId.isEmpty()) {
            itemCount = 0;
        }

        // Format: targetUuid:coinsOffered:itemId:itemCount
        String data = this.offerTargetUuid + ":" + coins + ":" + itemId + ":" + itemCount;
        ClientPacketDistributor.sendToServer(
            (CustomPacketPayload) new ComputerActionPayload("trade_send_offer", data),
            (CustomPacketPayload[]) new CustomPacketPayload[0]
        );
    }

    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();

        // Escape
        if (keyCode == 256) {
            if (this.composingOffer) {
                this.composingOffer = false;
                this.coinsInput = "";
                this.coinsBoxFocused = false;
                this.selectedItemId = "";
                this.selectedItemName = "";
                this.selectedItemCount = 0;
                return true;
            }
            if (this.minecraft != null) {
                this.minecraft.setScreen(this.parent);
            }
            return true;
        }

        // Enter - send offer if composing
        if (keyCode == 257) {
            if (this.composingOffer && this.coinsBoxFocused) {
                sendTradeOffer();
                return true;
            }
        }

        // Backspace
        if (keyCode == 259) {
            if (this.coinsBoxFocused && !this.coinsInput.isEmpty()) {
                this.coinsInput = this.coinsInput.substring(0, this.coinsInput.length() - 1);
                this.cursorBlink = 0;
                return true;
            }
        }

        return super.keyPressed(event);
    }

    public boolean charTyped(net.minecraft.client.input.CharacterEvent event) {
        char codePoint = (char) event.codepoint();
        int modifiers = event.modifiers();
        if (codePoint < 32) return false;

        // Coins field: digits only
        if (this.coinsBoxFocused) {
            if (Character.isDigit(codePoint) && this.coinsInput.length() < 10) {
                this.coinsInput += codePoint;
                this.cursorBlink = 0;
            }
            return true;
        }

        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!this.composingOffer) {
            this.scroll -= (int) (scrollY * ROW_HEIGHT);
            this.scroll = Math.max(0, this.scroll);
            return true;
        }
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

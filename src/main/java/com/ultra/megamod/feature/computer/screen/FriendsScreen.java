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
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class FriendsScreen extends Screen {
    private final Screen parent;
    private int scroll = 0;
    private String addInput = "";
    private boolean addBoxFocused = false;
    private String statusMsg = "";
    private int statusTimer = 0;
    private List<FriendEntry> friends = new ArrayList<>();
    private List<RequestEntry> friendRequests = new ArrayList<>();
    private List<RequestEntry> tpRequests = new ArrayList<>();

    private boolean dataLoaded = false;
    private int pollTimer = 0;
    private int refreshTimer = 0;
    private int cursorBlink = 0;

    // Cached online count (recalculated every 20 frames)
    private long cachedOnlineCount = 0;
    private int onlineCountFrameCounter = 19; // start high so first frame triggers recalculation

    // Message compose state
    private boolean composingMessage = false;
    private String messageTarget = "";
    private String messageTargetUuid = "";
    private String messageInput = "";
    private boolean messageBoxFocused = false;

    // Layout
    private int titleBarH;
    private int contentTop;
    private final List<ClickRect> clickRects = new ArrayList<>();

    // Colors
    private static final int ONLINE_GREEN = 0xFF3FB950;
    private static final int OFFLINE_GREY = 0xFF4A4A50;
    private static final int OFFLINE_NAME = 0xFF8B949E;
    private static final int REQUEST_BLUE = 0xFF58A6FF;
    private static final int ERROR_RED = 0xFFFF6B6B;
    private static final int SUCCESS_GREEN = 0xFF3FB950;
    private static final int BG_DARK = 0xFF0D1117;
    private static final int PANEL_BG = 0xFF161B22;
    private static final int ROW_EVEN = 0xFF1C2128;
    private static final int ROW_ODD = 0xFF21262D;
    private static final int ROW_HEIGHT = 22;

    public record FriendEntry(String name, String uuid, boolean online, String lastSeen) {}
    public record RequestEntry(String name, String uuid, long timestamp) {}

    public FriendsScreen(Screen parent) {
        super(Component.literal("Friends"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        Objects.requireNonNull(this.font);
        this.titleBarH = 9 + 16;
        this.contentTop = this.titleBarH + 6;
        if (!this.dataLoaded) {
            requestFriendsData();
        }
    }

    private void requestFriendsData() {
        ClientPacketDistributor.sendToServer(
            (CustomPacketPayload) new ComputerActionPayload("friends_request", ""),
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
        if (resp != null && resp.dataType().equals("friends_data")) {
            ComputerDataPayload.lastResponse = null;
            parseFriendsData(resp.jsonData());
            this.dataLoaded = true;
            this.pollTimer = 0;
        }

        // Check for action results (add, remove, accept, decline, tp, message)
        if (resp != null && resp.dataType().equals("friends_result")) {
            ComputerDataPayload.lastResponse = null;
            parseResult(resp.jsonData());
        }

        // Consume error responses so the screen doesn't stay stuck
        if (resp != null && "error".equals(resp.dataType())) {
            ComputerDataPayload.lastResponse = null;
            this.dataLoaded = true;
        }

        // Auto-refresh every 3 seconds (60 ticks)
        this.refreshTimer++;
        if (this.refreshTimer >= 60) {
            this.refreshTimer = 0;
            requestFriendsData();
        }
    }

    private void parseFriendsData(String json) {
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();

            this.friends.clear();
            if (obj.has("friends")) {
                JsonArray arr = obj.getAsJsonArray("friends");
                for (JsonElement el : arr) {
                    JsonObject f = el.getAsJsonObject();
                    this.friends.add(new FriendEntry(
                        f.get("name").getAsString(),
                        f.get("uuid").getAsString(),
                        f.get("online").getAsBoolean(),
                        f.has("lastSeen") ? f.get("lastSeen").getAsString() : ""
                    ));
                }
            }
            // Sort: online first, then alphabetical
            this.friends.sort(Comparator.<FriendEntry, Boolean>comparing(e -> !e.online)
                .thenComparing(FriendEntry::name, String.CASE_INSENSITIVE_ORDER));

            this.friendRequests.clear();
            if (obj.has("friendRequests")) {
                JsonArray arr = obj.getAsJsonArray("friendRequests");
                for (JsonElement el : arr) {
                    JsonObject r = el.getAsJsonObject();
                    this.friendRequests.add(new RequestEntry(
                        r.get("name").getAsString(),
                        r.get("uuid").getAsString(),
                        r.get("timestamp").getAsLong()
                    ));
                }
            }

            this.tpRequests.clear();
            if (obj.has("tpRequests")) {
                JsonArray arr = obj.getAsJsonArray("tpRequests");
                for (JsonElement el : arr) {
                    JsonObject r = el.getAsJsonObject();
                    this.tpRequests.add(new RequestEntry(
                        r.get("name").getAsString(),
                        r.get("uuid").getAsString(),
                        r.get("timestamp").getAsLong()
                    ));
                }
            }
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to parse friends data", e);
        }
    }

    private void parseResult(String json) {
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            boolean success = obj.has("success") && obj.get("success").getAsBoolean();
            String msg = obj.has("message") ? obj.get("message").getAsString() : (success ? "Done!" : "Failed.");
            this.statusMsg = msg;
            this.statusTimer = 80;
            // Refresh data
            requestFriendsData();
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to handle friends action response", e);
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
        UIHelper.drawCenteredTitle(g, this.font, "Friends List", this.width / 2, titleY);

        // Back button
        int backW = 50;
        int backH = 16;
        int backX = 8;
        int backY2 = (this.titleBarH - backH) / 2;
        boolean backHover = mouseX >= backX && mouseX < backX + backW && mouseY >= backY2 && mouseY < backY2 + backH;
        UIHelper.drawButton(g, backX, backY2, backW, backH, backHover);
        int backTextX = backX + (backW - this.font.width("< Back")) / 2;
        g.drawString(this.font, "< Back", backTextX, backY2 + (backH - 9) / 2, -661816, false);
        this.clickRects.add(new ClickRect(backX, backY2, backW, backH, "back"));

        if (this.composingMessage) {
            renderMessageCompose(g, mouseX, mouseY);
        } else {
            renderMainView(g, mouseX, mouseY);
        }

        // Status message at bottom
        if (!this.statusMsg.isEmpty() && this.statusTimer > 0) {
            int msgW = this.font.width(this.statusMsg) + 20;
            int msgX = (this.width - msgW) / 2;
            int msgY = this.height - 24;
            UIHelper.drawCard(g, msgX, msgY, msgW, 18);
            boolean isError = this.statusMsg.contains("not found") || this.statusMsg.contains("Failed")
                    || this.statusMsg.contains("already") || this.statusMsg.contains("Cannot")
                    || this.statusMsg.contains("expired") || this.statusMsg.contains("yourself");
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

        // === LEFT PANEL: Friends List ===
        UIHelper.drawInsetPanel(g, 4, panelTop, leftPanelW - 4, panelBottom - panelTop);

        // Friend count header (cached, recalculated every 20 frames)
        this.onlineCountFrameCounter++;
        if (this.onlineCountFrameCounter >= 20) {
            this.cachedOnlineCount = this.friends.stream().filter(FriendEntry::online).count();
            this.onlineCountFrameCounter = 0;
        }
        long onlineCount = this.cachedOnlineCount;
        String countStr = "Friends: " + this.friends.size() + " (" + onlineCount + " online)";
        g.drawString(this.font, countStr, 10, panelTop + 4, UIHelper.GOLD_MID, false);

        // Add friend bar
        int addBarY = panelTop + 16;
        int addInputW = leftPanelW - 80;
        int addInputX = 10;
        int addInputH = 14;

        // Draw text input box
        int inputBorder = this.addBoxFocused ? REQUEST_BLUE : OFFLINE_GREY;
        g.fill(addInputX - 1, addBarY - 1, addInputX + addInputW + 1, addBarY + addInputH + 1, inputBorder);
        g.fill(addInputX, addBarY, addInputX + addInputW, addBarY + addInputH, 0xFF0D1117);

        // Input text + cursor
        String displayText = this.addInput;
        if (displayText.isEmpty() && !this.addBoxFocused) {
            g.drawString(this.font, "Enter player name...", addInputX + 3, addBarY + 3, OFFLINE_GREY, false);
        } else {
            String clipped = displayText;
            int maxTextW = addInputW - 8;
            while (this.font.width(clipped) > maxTextW && clipped.length() > 0) {
                clipped = clipped.substring(1);
            }
            g.drawString(this.font, clipped, addInputX + 3, addBarY + 3, 0xFFE6EDF3, false);
            if (this.addBoxFocused && (this.cursorBlink / 10) % 2 == 0) {
                int cursorX = addInputX + 3 + this.font.width(clipped);
                g.fill(cursorX, addBarY + 2, cursorX + 1, addBarY + addInputH - 2, REQUEST_BLUE);
            }
        }
        this.clickRects.add(new ClickRect(addInputX, addBarY, addInputW, addInputH, "focus_add"));

        // "Add" button
        int addBtnW = 40;
        int addBtnX = addInputX + addInputW + 6;
        boolean addHover = mouseX >= addBtnX && mouseX < addBtnX + addBtnW && mouseY >= addBarY && mouseY < addBarY + addInputH;
        UIHelper.drawButton(g, addBtnX, addBarY, addBtnW, addInputH, addHover);
        int addLabelX = addBtnX + (addBtnW - this.font.width("Add")) / 2;
        g.drawString(this.font, "Add", addLabelX, addBarY + 3, SUCCESS_GREEN, false);
        this.clickRects.add(new ClickRect(addBtnX, addBarY, addBtnW, addInputH, "add_friend"));

        // Friends list area
        int listTop = addBarY + addInputH + 6;
        int listBottom = panelBottom - 6;
        int listX = 8;
        int listW = leftPanelW - 20;

        g.enableScissor(listX, listTop, listX + listW, listBottom);

        int visibleH = listBottom - listTop;
        int totalH = this.friends.size() * ROW_HEIGHT;
        int maxScroll = Math.max(0, totalH - visibleH);
        this.scroll = Math.max(0, Math.min(this.scroll, maxScroll));

        if (this.friends.isEmpty()) {
            g.drawString(this.font, "No friends yet. Add someone!", listX + 10, listTop + 10, OFFLINE_NAME, false);
        }

        for (int i = 0; i < this.friends.size(); i++) {
            int rowY = listTop + i * ROW_HEIGHT - this.scroll;
            if (rowY + ROW_HEIGHT < listTop || rowY > listBottom) continue;

            FriendEntry f = this.friends.get(i);
            boolean rowHover = mouseX >= listX && mouseX < listX + listW && mouseY >= rowY && mouseY < rowY + ROW_HEIGHT
                    && mouseY >= listTop && mouseY < listBottom;

            // Row background
            int rowBg = i % 2 == 0 ? ROW_EVEN : ROW_ODD;
            g.fill(listX, rowY, listX + listW, rowY + ROW_HEIGHT, rowBg);
            if (rowHover) {
                g.fill(listX, rowY, listX + listW, rowY + ROW_HEIGHT, 0x18FFFFFF);
            }

            // Online/offline dot
            int dotColor = f.online ? ONLINE_GREEN : OFFLINE_GREY;
            int dotX = listX + 6;
            int dotY = rowY + (ROW_HEIGHT - 4) / 2;
            g.fill(dotX, dotY, dotX + 4, dotY + 4, dotColor);

            // Name
            int nameColor = f.online ? 0xFFFFFFFF : OFFLINE_NAME;
            g.drawString(this.font, f.name, listX + 14, rowY + (ROW_HEIGHT - 9) / 2, nameColor, false);

            // Last seen (for offline)
            if (!f.online && !f.lastSeen.isEmpty()) {
                String lastStr = f.lastSeen;
                int lastW = this.font.width(lastStr);
                g.drawString(this.font, lastStr, listX + 14 + this.font.width(f.name) + 8, rowY + (ROW_HEIGHT - 9) / 2, OFFLINE_GREY, false);
            }

            // Action buttons (right side)
            int btnY = rowY + 3;
            int btnH = ROW_HEIGHT - 6;

            // Remove button
            int removeBtnW = 14;
            int removeBtnX = listX + listW - removeBtnW - 4;
            boolean removeHover = mouseX >= removeBtnX && mouseX < removeBtnX + removeBtnW
                    && mouseY >= btnY && mouseY < btnY + btnH && mouseY >= listTop && mouseY < listBottom;
            UIHelper.drawButton(g, removeBtnX, btnY, removeBtnW, btnH, removeHover);
            g.drawString(this.font, "X", removeBtnX + (removeBtnW - this.font.width("X")) / 2, btnY + (btnH - 9) / 2, ERROR_RED, false);
            this.clickRects.add(new ClickRect(removeBtnX, btnY, removeBtnW, btnH, "remove:" + f.uuid));

            // Message button
            int msgBtnW = 30;
            int msgBtnX = removeBtnX - msgBtnW - 3;
            boolean msgHover = mouseX >= msgBtnX && mouseX < msgBtnX + msgBtnW
                    && mouseY >= btnY && mouseY < btnY + btnH && mouseY >= listTop && mouseY < listBottom;
            UIHelper.drawButton(g, msgBtnX, btnY, msgBtnW, btnH, msgHover);
            g.drawString(this.font, "Msg", msgBtnX + (msgBtnW - this.font.width("Msg")) / 2, btnY + (btnH - 9) / 2, REQUEST_BLUE, false);
            this.clickRects.add(new ClickRect(msgBtnX, btnY, msgBtnW, btnH, "message:" + f.uuid + ":" + f.name));

            // TP Request button (only for online friends)
            int nextBtnX = msgBtnX;
            if (f.online) {
                int tpBtnW = 22;
                int tpBtnX = nextBtnX - tpBtnW - 3;
                boolean tpHover = mouseX >= tpBtnX && mouseX < tpBtnX + tpBtnW
                        && mouseY >= btnY && mouseY < btnY + btnH && mouseY >= listTop && mouseY < listBottom;
                UIHelper.drawButton(g, tpBtnX, btnY, tpBtnW, btnH, tpHover);
                g.drawString(this.font, "TP", tpBtnX + (tpBtnW - this.font.width("TP")) / 2, btnY + (btnH - 9) / 2, ONLINE_GREEN, false);
                this.clickRects.add(new ClickRect(tpBtnX, btnY, tpBtnW, btnH, "tp_request:" + f.uuid));
                nextBtnX = tpBtnX;
            }

            // Visit Museum button (works for online and offline friends)
            int musBtnW = 42;
            int musBtnX = nextBtnX - musBtnW - 3;
            boolean musHover = mouseX >= musBtnX && mouseX < musBtnX + musBtnW
                    && mouseY >= btnY && mouseY < btnY + btnH && mouseY >= listTop && mouseY < listBottom;
            UIHelper.drawButton(g, musBtnX, btnY, musBtnW, btnH, musHover);
            int musColor = 0xFFE8A838; // gold
            g.drawString(this.font, "Museum", musBtnX + (musBtnW - this.font.width("Museum")) / 2, btnY + (btnH - 9) / 2, musColor, false);
            this.clickRects.add(new ClickRect(musBtnX, btnY, musBtnW, btnH, "visit_museum:" + f.uuid));
        }

        g.disableScissor();

        // Scrollbar
        if (totalH > visibleH) {
            float progress = maxScroll > 0 ? (float) this.scroll / (float) maxScroll : 0.0f;
            UIHelper.drawScrollbar(g, listX + listW + 2, listTop, listBottom - listTop, progress);
        }

        // === RIGHT PANEL: Incoming Requests ===
        UIHelper.drawInsetPanel(g, rightPanelX, panelTop, rightPanelW, panelBottom - panelTop);

        int rY = panelTop + 4;

        // Friend Requests Section
        g.drawString(this.font, "Friend Requests", rightPanelX + 6, rY, UIHelper.GOLD_MID, false);
        rY += 12;

        if (this.friendRequests.isEmpty()) {
            g.drawString(this.font, "None", rightPanelX + 10, rY, OFFLINE_NAME, false);
            rY += 14;
        } else {
            for (int i = 0; i < this.friendRequests.size(); i++) {
                RequestEntry req = this.friendRequests.get(i);
                if (rY + ROW_HEIGHT > panelBottom - 6) break;

                int reqRowBg = i % 2 == 0 ? ROW_EVEN : ROW_ODD;
                g.fill(rightPanelX + 4, rY, rightPanelX + rightPanelW - 4, rY + ROW_HEIGHT, reqRowBg);

                // Name
                g.drawString(this.font, req.name, rightPanelX + 8, rY + (ROW_HEIGHT - 9) / 2, REQUEST_BLUE, false);

                // Accept button
                int accBtnW = 14;
                int accBtnX = rightPanelX + rightPanelW - accBtnW - 24;
                int accBtnY = rY + 3;
                int accBtnH = ROW_HEIGHT - 6;
                boolean accHover = mouseX >= accBtnX && mouseX < accBtnX + accBtnW
                        && mouseY >= accBtnY && mouseY < accBtnY + accBtnH;
                UIHelper.drawButton(g, accBtnX, accBtnY, accBtnW, accBtnH, accHover);
                g.drawString(this.font, "\u2713", accBtnX + (accBtnW - this.font.width("\u2713")) / 2, accBtnY + (accBtnH - 9) / 2, SUCCESS_GREEN, false);
                this.clickRects.add(new ClickRect(accBtnX, accBtnY, accBtnW, accBtnH, "accept_friend:" + req.uuid));

                // Decline button
                int decBtnW = 14;
                int decBtnX = accBtnX + accBtnW + 3;
                boolean decHover = mouseX >= decBtnX && mouseX < decBtnX + decBtnW
                        && mouseY >= accBtnY && mouseY < accBtnY + accBtnH;
                UIHelper.drawButton(g, decBtnX, accBtnY, decBtnW, accBtnH, decHover);
                g.drawString(this.font, "X", decBtnX + (decBtnW - this.font.width("X")) / 2, accBtnY + (accBtnH - 9) / 2, ERROR_RED, false);
                this.clickRects.add(new ClickRect(decBtnX, accBtnY, decBtnW, accBtnH, "decline_friend:" + req.uuid));

                rY += ROW_HEIGHT;
            }
        }

        // Divider
        rY += 4;
        UIHelper.drawHorizontalDivider(g, rightPanelX + 6, rY, rightPanelW - 12);
        rY += 8;

        // TP Requests Section
        g.drawString(this.font, "TP Requests", rightPanelX + 6, rY, UIHelper.GOLD_MID, false);
        rY += 12;

        if (this.tpRequests.isEmpty()) {
            g.drawString(this.font, "None", rightPanelX + 10, rY, OFFLINE_NAME, false);
        } else {
            for (int i = 0; i < this.tpRequests.size(); i++) {
                RequestEntry req = this.tpRequests.get(i);
                if (rY + ROW_HEIGHT > panelBottom - 6) break;

                int reqRowBg = i % 2 == 0 ? ROW_EVEN : ROW_ODD;
                g.fill(rightPanelX + 4, rY, rightPanelX + rightPanelW - 4, rY + ROW_HEIGHT, reqRowBg);

                // Name + expiry hint
                long elapsed = (System.currentTimeMillis() - req.timestamp) / 1000;
                long remaining = Math.max(0, 60 - elapsed);
                String tpLabel = req.name + " (" + remaining + "s)";
                int tpLabelColor = remaining > 10 ? REQUEST_BLUE : ERROR_RED;
                g.drawString(this.font, tpLabel, rightPanelX + 8, rY + (ROW_HEIGHT - 9) / 2, tpLabelColor, false);

                // Accept button
                int accBtnW = 14;
                int accBtnX = rightPanelX + rightPanelW - accBtnW - 24;
                int accBtnY = rY + 3;
                int accBtnH = ROW_HEIGHT - 6;
                boolean accHover = mouseX >= accBtnX && mouseX < accBtnX + accBtnW
                        && mouseY >= accBtnY && mouseY < accBtnY + accBtnH;
                UIHelper.drawButton(g, accBtnX, accBtnY, accBtnW, accBtnH, accHover);
                g.drawString(this.font, "\u2713", accBtnX + (accBtnW - this.font.width("\u2713")) / 2, accBtnY + (accBtnH - 9) / 2, SUCCESS_GREEN, false);
                this.clickRects.add(new ClickRect(accBtnX, accBtnY, accBtnW, accBtnH, "accept_tp:" + req.uuid));

                // Decline button
                int decBtnW = 14;
                int decBtnX = accBtnX + accBtnW + 3;
                boolean decHover = mouseX >= decBtnX && mouseX < decBtnX + decBtnW
                        && mouseY >= accBtnY && mouseY < accBtnY + accBtnH;
                UIHelper.drawButton(g, decBtnX, accBtnY, decBtnW, accBtnH, decHover);
                g.drawString(this.font, "X", decBtnX + (decBtnW - this.font.width("X")) / 2, accBtnY + (accBtnH - 9) / 2, ERROR_RED, false);
                this.clickRects.add(new ClickRect(decBtnX, accBtnY, decBtnW, accBtnH, "decline_tp:" + req.uuid));

                rY += ROW_HEIGHT;
            }
        }
    }

    private void renderMessageCompose(GuiGraphics g, int mouseX, int mouseY) {
        int panelW = Math.min(340, this.width - 20);
        int panelH = 100;
        int panelX = (this.width - panelW) / 2;
        int panelY = this.height / 2 - panelH / 2 - 10;

        UIHelper.drawPanel(g, panelX, panelY, panelW, panelH);

        // "To:" label
        int toY = panelY + 10;
        g.drawString(this.font, "To:", panelX + 10, toY, UIHelper.GOLD_DARK, false);
        g.drawString(this.font, this.messageTarget, panelX + 10 + this.font.width("To: "), toY, UIHelper.GOLD_BRIGHT, false);

        // Message input box
        int inputX = panelX + 10;
        int inputY = panelY + 28;
        int inputW = panelW - 20;
        int inputH = 16;

        int inputBorder = this.messageBoxFocused ? REQUEST_BLUE : OFFLINE_GREY;
        g.fill(inputX - 1, inputY - 1, inputX + inputW + 1, inputY + inputH + 1, inputBorder);
        g.fill(inputX, inputY, inputX + inputW, inputY + inputH, 0xFF0D1117);

        if (this.messageInput.isEmpty() && !this.messageBoxFocused) {
            g.drawString(this.font, "Type your message...", inputX + 3, inputY + 4, OFFLINE_GREY, false);
        } else {
            String clipped = this.messageInput;
            int maxTextW = inputW - 8;
            while (this.font.width(clipped) > maxTextW && clipped.length() > 0) {
                clipped = clipped.substring(1);
            }
            g.drawString(this.font, clipped, inputX + 3, inputY + 4, 0xFFE6EDF3, false);
            if (this.messageBoxFocused && (this.cursorBlink / 10) % 2 == 0) {
                int cursorX = inputX + 3 + this.font.width(clipped);
                g.fill(cursorX, inputY + 2, cursorX + 1, inputY + inputH - 2, REQUEST_BLUE);
            }
        }
        this.clickRects.add(new ClickRect(inputX, inputY, inputW, inputH, "focus_message"));

        // Send button
        int sendW = 60;
        int sendH = 18;
        int sendX = panelX + panelW / 2 - sendW - 5;
        int sendY = panelY + panelH - sendH - 10;
        boolean sendHover = mouseX >= sendX && mouseX < sendX + sendW && mouseY >= sendY && mouseY < sendY + sendH;
        UIHelper.drawButton(g, sendX, sendY, sendW, sendH, sendHover);
        int sendTextX = sendX + (sendW - this.font.width("Send")) / 2;
        g.drawString(this.font, "Send", sendTextX, sendY + (sendH - 9) / 2, SUCCESS_GREEN, false);
        this.clickRects.add(new ClickRect(sendX, sendY, sendW, sendH, "send_message"));

        // Cancel button
        int cancelW = 60;
        int cancelX = panelX + panelW / 2 + 5;
        boolean cancelHover = mouseX >= cancelX && mouseX < cancelX + cancelW && mouseY >= sendY && mouseY < sendY + sendH;
        UIHelper.drawButton(g, cancelX, sendY, cancelW, sendH, cancelHover);
        int cancelTextX = cancelX + (cancelW - this.font.width("Cancel")) / 2;
        g.drawString(this.font, "Cancel", cancelTextX, sendY + (sendH - 9) / 2, ERROR_RED, false);
        this.clickRects.add(new ClickRect(cancelX, sendY, cancelW, sendH, "cancel_message"));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean consumed) {
        if (consumed) {
            return super.mouseClicked(event, consumed);
        }
        int mx = (int) event.x();
        int my = (int) event.y();

        // Unfocus all by default
        boolean clickedAddBox = false;
        boolean clickedMsgBox = false;

        for (ClickRect r : this.clickRects) {
            if (mx >= r.x && mx < r.x + r.w && my >= r.y && my < r.y + r.h) {
                if (r.action.equals("focus_add")) {
                    clickedAddBox = true;
                } else if (r.action.equals("focus_message")) {
                    clickedMsgBox = true;
                } else {
                    handleClick(r.action);
                    return true;
                }
            }
        }

        this.addBoxFocused = clickedAddBox;
        this.messageBoxFocused = clickedMsgBox || this.composingMessage;
        if (clickedAddBox || clickedMsgBox) {
            this.cursorBlink = 0;
            return true;
        }

        // Clicked outside any rect — unfocus
        this.addBoxFocused = false;
        this.messageBoxFocused = false;
        return super.mouseClicked(event, consumed);
    }

    private void handleClick(String action) {
        if (action.equals("back")) {
            if (this.composingMessage) {
                this.composingMessage = false;
                this.messageInput = "";
                this.messageBoxFocused = false;
            } else if (this.minecraft != null) {
                this.minecraft.setScreen(this.parent);
            }
            return;
        }

        if (action.equals("add_friend")) {
            String name = this.addInput.trim();
            if (!name.isEmpty()) {
                ClientPacketDistributor.sendToServer(
                    (CustomPacketPayload) new ComputerActionPayload("friends_add", name),
                    (CustomPacketPayload[]) new CustomPacketPayload[0]
                );
                this.addInput = "";
            }
            return;
        }

        if (action.startsWith("remove:")) {
            String uuid = action.substring(7);
            ClientPacketDistributor.sendToServer(
                (CustomPacketPayload) new ComputerActionPayload("friends_remove", uuid),
                (CustomPacketPayload[]) new CustomPacketPayload[0]
            );
            return;
        }

        if (action.startsWith("message:")) {
            // format: "message:uuid:name"
            String[] parts = action.substring(8).split(":", 2);
            if (parts.length == 2) {
                this.messageTargetUuid = parts[0];
                this.messageTarget = parts[1];
                this.composingMessage = true;
                this.messageInput = "";
                this.messageBoxFocused = true;
                this.cursorBlink = 0;
            }
            return;
        }

        if (action.startsWith("tp_request:")) {
            String uuid = action.substring(11);
            ClientPacketDistributor.sendToServer(
                (CustomPacketPayload) new ComputerActionPayload("friends_tp_request", uuid),
                (CustomPacketPayload[]) new CustomPacketPayload[0]
            );
            return;
        }

        if (action.startsWith("visit_museum:")) {
            String uuid = action.substring(13);
            ClientPacketDistributor.sendToServer(
                (CustomPacketPayload) new ComputerActionPayload("friends_visit_museum", uuid),
                (CustomPacketPayload[]) new CustomPacketPayload[0]
            );
            return;
        }

        if (action.startsWith("accept_friend:")) {
            String uuid = action.substring(14);
            ClientPacketDistributor.sendToServer(
                (CustomPacketPayload) new ComputerActionPayload("friends_accept", uuid),
                (CustomPacketPayload[]) new CustomPacketPayload[0]
            );
            return;
        }

        if (action.startsWith("decline_friend:")) {
            String uuid = action.substring(15);
            ClientPacketDistributor.sendToServer(
                (CustomPacketPayload) new ComputerActionPayload("friends_decline", uuid),
                (CustomPacketPayload[]) new CustomPacketPayload[0]
            );
            return;
        }

        if (action.startsWith("accept_tp:")) {
            String uuid = action.substring(10);
            ClientPacketDistributor.sendToServer(
                (CustomPacketPayload) new ComputerActionPayload("friends_tp_accept", uuid),
                (CustomPacketPayload[]) new CustomPacketPayload[0]
            );
            return;
        }

        if (action.startsWith("decline_tp:")) {
            String uuid = action.substring(11);
            ClientPacketDistributor.sendToServer(
                (CustomPacketPayload) new ComputerActionPayload("friends_tp_decline", uuid),
                (CustomPacketPayload[]) new CustomPacketPayload[0]
            );
            return;
        }

        if (action.equals("send_message")) {
            sendFriendMessage();
            return;
        }

        if (action.equals("cancel_message")) {
            this.composingMessage = false;
            this.messageInput = "";
            this.messageBoxFocused = false;
            return;
        }
    }

    private void sendFriendMessage() {
        String text = this.messageInput.trim();
        if (text.isEmpty() || this.messageTargetUuid.isEmpty()) return;
        ClientPacketDistributor.sendToServer(
            (CustomPacketPayload) new ComputerActionPayload("friends_message", this.messageTargetUuid + ":" + text),
            (CustomPacketPayload[]) new CustomPacketPayload[0]
        );
        this.statusMsg = "Message sent to " + this.messageTarget + "!";
        this.statusTimer = 60;
        this.composingMessage = false;
        this.messageInput = "";
        this.messageBoxFocused = false;
    }

    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();

        // Escape
        if (keyCode == 256) {
            if (this.composingMessage) {
                this.composingMessage = false;
                this.messageInput = "";
                this.messageBoxFocused = false;
                return true;
            }
            if (this.minecraft != null) {
                this.minecraft.setScreen(this.parent);
            }
            return true;
        }

        // Enter - send message if composing, or add friend if add box focused
        if (keyCode == 257) {
            if (this.composingMessage && this.messageBoxFocused) {
                sendFriendMessage();
                return true;
            }
            if (this.addBoxFocused) {
                String name = this.addInput.trim();
                if (!name.isEmpty()) {
                    ClientPacketDistributor.sendToServer(
                        (CustomPacketPayload) new ComputerActionPayload("friends_add", name),
                        (CustomPacketPayload[]) new CustomPacketPayload[0]
                    );
                    this.addInput = "";
                }
                return true;
            }
        }

        // Backspace
        if (keyCode == 259) {
            if (this.composingMessage && this.messageBoxFocused && !this.messageInput.isEmpty()) {
                this.messageInput = this.messageInput.substring(0, this.messageInput.length() - 1);
                this.cursorBlink = 0;
                return true;
            }
            if (this.addBoxFocused && !this.addInput.isEmpty()) {
                this.addInput = this.addInput.substring(0, this.addInput.length() - 1);
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

        if (this.composingMessage && this.messageBoxFocused) {
            if (this.messageInput.length() < 256) {
                this.messageInput += codePoint;
                this.cursorBlink = 0;
            }
            return true;
        }

        if (this.addBoxFocused) {
            if (this.addInput.length() < 32) {
                this.addInput += codePoint;
                this.cursorBlink = 0;
            }
            return true;
        }

        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!this.composingMessage) {
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

    private record ClickRect(int x, int y, int w, int h, String action) {}
}

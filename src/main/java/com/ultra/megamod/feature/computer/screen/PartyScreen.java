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
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class PartyScreen extends Screen {
    private final Screen parent;
    private int scroll = 0;
    private String inviteInput = "";
    private boolean inviteBoxFocused = false;
    private String statusMsg = "";
    private int statusTimer = 0;

    private boolean dataLoaded = false;
    private int pollTimer = 0;
    private int refreshTimer = 0;
    private int cursorBlink = 0;

    // Party state from server
    private boolean inParty = false;
    private String leaderName = "";
    private String leaderUuid = "";
    private List<MemberEntry> members = new ArrayList<>();
    private List<PendingEntry> pendingInvites = new ArrayList<>();
    private IncomingInvite incomingInvite = null;
    private boolean isLeader = false;

    // Layout
    private int titleBarH;
    private int contentTop;
    private final List<ClickRect> clickRects = new ArrayList<>();

    // Colors
    private static final int PARTY_PURPLE = 0xFF9B59B6;
    private static final int ONLINE_GREEN = 0xFF3FB950;
    private static final int ERROR_RED = 0xFFFF6B6B;
    private static final int SUCCESS_GREEN = 0xFF3FB950;
    private static final int BG_DARK = 0xFF0D1117;
    private static final int PANEL_BG = 0xFF161B22;
    private static final int ROW_EVEN = 0xFF1C2128;
    private static final int ROW_ODD = 0xFF21262D;
    private static final int ROW_HEIGHT = 22;

    public record MemberEntry(String name, String uuid, boolean online) {}
    public record PendingEntry(String name, String uuid) {}
    public record IncomingInvite(String from, String fromUuid) {}

    public PartyScreen(Screen parent) {
        super(Component.literal("Party"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        Objects.requireNonNull(this.font);
        this.titleBarH = 9 + 16;
        this.contentTop = this.titleBarH + 6;
        if (!this.dataLoaded) {
            requestPartyData();
        }
    }

    private void requestPartyData() {
        ClientPacketDistributor.sendToServer(
            (CustomPacketPayload) new ComputerActionPayload("party_request", ""),
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
        if (resp != null && resp.dataType().equals("party_data")) {
            ComputerDataPayload.lastResponse = null;
            parsePartyData(resp.jsonData());
            this.dataLoaded = true;
            this.pollTimer = 0;
        }

        // Check for action results
        if (resp != null && resp.dataType().equals("party_result")) {
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
            requestPartyData();
        }
    }

    private void parsePartyData(String json) {
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();

            this.inParty = obj.has("inParty") && obj.get("inParty").getAsBoolean();

            this.members.clear();
            this.pendingInvites.clear();
            this.leaderName = "";
            this.leaderUuid = "";
            this.isLeader = false;

            if (this.inParty) {
                // Leader info
                if (obj.has("leader") && !obj.get("leader").isJsonNull()) {
                    JsonObject leader = obj.getAsJsonObject("leader");
                    this.leaderName = leader.get("name").getAsString();
                    this.leaderUuid = leader.get("uuid").getAsString();
                }

                // Members
                if (obj.has("members")) {
                    JsonArray arr = obj.getAsJsonArray("members");
                    for (JsonElement el : arr) {
                        JsonObject m = el.getAsJsonObject();
                        this.members.add(new MemberEntry(
                            m.get("name").getAsString(),
                            m.get("uuid").getAsString(),
                            m.get("online").getAsBoolean()
                        ));
                    }
                }

                // Pending invites (outgoing)
                if (obj.has("pendingInvites")) {
                    JsonArray arr = obj.getAsJsonArray("pendingInvites");
                    for (JsonElement el : arr) {
                        JsonObject p = el.getAsJsonObject();
                        this.pendingInvites.add(new PendingEntry(
                            p.get("name").getAsString(),
                            p.get("uuid").getAsString()
                        ));
                    }
                }

                // Determine if current player is the leader
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) {
                    String myUuid = mc.player.getUUID().toString();
                    this.isLeader = myUuid.equals(this.leaderUuid);
                }
            }

            // Incoming invite
            if (obj.has("incomingInvite") && !obj.get("incomingInvite").isJsonNull()) {
                JsonObject inv = obj.getAsJsonObject("incomingInvite");
                this.incomingInvite = new IncomingInvite(
                    inv.get("from").getAsString(),
                    inv.get("fromUuid").getAsString()
                );
            } else {
                this.incomingInvite = null;
            }
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to parse party data", e);
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
            requestPartyData();
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to handle party action response", e);
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
        UIHelper.drawCenteredTitle(g, this.font, "Party", this.width / 2, titleY);

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

        if (this.inParty) {
            renderPartyView(g, mouseX, mouseY);
        } else {
            renderNoPartyView(g, mouseX, mouseY);
        }

        // Status message at bottom
        if (!this.statusMsg.isEmpty() && this.statusTimer > 0) {
            int msgW = this.font.width(this.statusMsg) + 20;
            int msgX = (this.width - msgW) / 2;
            int msgY = this.height - 24;
            UIHelper.drawCard(g, msgX, msgY, msgW, 18);
            boolean isError = this.statusMsg.contains("not found") || this.statusMsg.contains("Failed")
                    || this.statusMsg.contains("already") || this.statusMsg.contains("Cannot")
                    || this.statusMsg.contains("full") || this.statusMsg.contains("yourself")
                    || this.statusMsg.contains("not in") || this.statusMsg.contains("Only")
                    || this.statusMsg.contains("offline") || this.statusMsg.contains("no longer");
            int msgColor = isError ? ERROR_RED : SUCCESS_GREEN;
            g.drawCenteredString(this.font, this.statusMsg, this.width / 2, msgY + 5, msgColor);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void renderNoPartyView(GuiGraphics g, int mouseX, int mouseY) {
        int centerX = this.width / 2;
        int panelW = Math.min(280, this.width - 40);
        int panelX = centerX - panelW / 2;
        int panelTop = this.contentTop + 20;

        // "No Party" info panel
        UIHelper.drawInsetPanel(g, panelX, panelTop, panelW, 80);

        String noPartyText = "You are not in a party.";
        int textX = centerX - this.font.width(noPartyText) / 2;
        g.drawString(this.font, noPartyText, textX, panelTop + 12, UIHelper.GOLD_MID, false);

        String infoText = "Create a party or accept an invite.";
        int infoX = centerX - this.font.width(infoText) / 2;
        g.drawString(this.font, infoText, infoX, panelTop + 26, UIHelper.GOLD_MID, false);

        // "Create Party" button
        int createW = 100;
        int createH = 20;
        int createX = centerX - createW / 2;
        int createY = panelTop + 48;
        boolean createHover = mouseX >= createX && mouseX < createX + createW && mouseY >= createY && mouseY < createY + createH;
        UIHelper.drawButton(g, createX, createY, createW, createH, createHover);
        int createTextX = createX + (createW - this.font.width("Create Party")) / 2;
        g.drawString(this.font, "Create Party", createTextX, createY + (createH - 9) / 2, PARTY_PURPLE, false);
        this.clickRects.add(new ClickRect(createX, createY, createW, createH, "create_party"));

        // Incoming invite section
        if (this.incomingInvite != null) {
            int inviteTop = panelTop + 100;
            UIHelper.drawInsetPanel(g, panelX, inviteTop, panelW, 60);

            g.drawString(this.font, "Party Invite", panelX + 8, inviteTop + 6, UIHelper.GOLD_MID, false);

            String invText = "From: " + this.incomingInvite.from;
            g.drawString(this.font, invText, panelX + 12, inviteTop + 20, PARTY_PURPLE, false);

            // Accept button
            int accBtnW = 50;
            int accBtnH = 16;
            int accBtnX = panelX + panelW / 2 - accBtnW - 6;
            int accBtnY = inviteTop + 36;
            boolean accHover = mouseX >= accBtnX && mouseX < accBtnX + accBtnW && mouseY >= accBtnY && mouseY < accBtnY + accBtnH;
            UIHelper.drawButton(g, accBtnX, accBtnY, accBtnW, accBtnH, accHover);
            int accTextX = accBtnX + (accBtnW - this.font.width("Accept")) / 2;
            g.drawString(this.font, "Accept", accTextX, accBtnY + (accBtnH - 9) / 2, SUCCESS_GREEN, false);
            this.clickRects.add(new ClickRect(accBtnX, accBtnY, accBtnW, accBtnH, "accept_invite"));

            // Decline button
            int decBtnW = 50;
            int decBtnX = panelX + panelW / 2 + 6;
            boolean decHover = mouseX >= decBtnX && mouseX < decBtnX + decBtnW && mouseY >= accBtnY && mouseY < accBtnY + accBtnH;
            UIHelper.drawButton(g, decBtnX, accBtnY, decBtnW, accBtnH, decHover);
            int decTextX = decBtnX + (decBtnW - this.font.width("Decline")) / 2;
            g.drawString(this.font, "Decline", decTextX, accBtnY + (accBtnH - 9) / 2, ERROR_RED, false);
            this.clickRects.add(new ClickRect(decBtnX, accBtnY, decBtnW, accBtnH, "decline_invite"));
        }
    }

    private void renderPartyView(GuiGraphics g, int mouseX, int mouseY) {
        int leftPanelW = (int) (this.width * 0.6);
        int rightPanelX = leftPanelW + 4;
        int rightPanelW = this.width - rightPanelX - 4;
        int panelTop = this.contentTop;
        int panelBottom = this.height - 30;

        // === LEFT PANEL: Party Members ===
        UIHelper.drawInsetPanel(g, 4, panelTop, leftPanelW - 4, panelBottom - panelTop);

        // Party header with leader name
        String headerStr = "Party (" + this.members.size() + "/4)";
        g.drawString(this.font, headerStr, 10, panelTop + 4, UIHelper.GOLD_MID, false);

        // Leader name with crown indicator
        String leaderLabel = "\u2654 " + this.leaderName + " (Leader)";
        int leaderLabelX = 10 + this.font.width(headerStr) + 12;
        if (leaderLabelX + this.font.width(leaderLabel) < leftPanelW - 10) {
            g.drawString(this.font, leaderLabel, leaderLabelX, panelTop + 4, 0xFFFFD700, false);
        }

        // Invite bar (only for leader)
        int listTop;
        if (this.isLeader) {
            int inviteBarY = panelTop + 16;
            int inviteInputW = leftPanelW - 90;
            int inviteInputX = 10;
            int inviteInputH = 14;

            // Draw text input box
            int inputBorder = this.inviteBoxFocused ? PARTY_PURPLE : 0xFF4A4A50;
            g.fill(inviteInputX - 1, inviteBarY - 1, inviteInputX + inviteInputW + 1, inviteBarY + inviteInputH + 1, inputBorder);
            g.fill(inviteInputX, inviteBarY, inviteInputX + inviteInputW, inviteBarY + inviteInputH, 0xFF0D1117);

            // Input text + cursor
            String displayText = this.inviteInput;
            if (displayText.isEmpty() && !this.inviteBoxFocused) {
                g.drawString(this.font, "Invite player...", inviteInputX + 3, inviteBarY + 3, 0xFF4A4A50, false);
            } else {
                String clipped = displayText;
                int maxTextW = inviteInputW - 8;
                while (this.font.width(clipped) > maxTextW && clipped.length() > 0) {
                    clipped = clipped.substring(1);
                }
                g.drawString(this.font, clipped, inviteInputX + 3, inviteBarY + 3, 0xFFE6EDF3, false);
                if (this.inviteBoxFocused && (this.cursorBlink / 10) % 2 == 0) {
                    int cursorX = inviteInputX + 3 + this.font.width(clipped);
                    g.fill(cursorX, inviteBarY + 2, cursorX + 1, inviteBarY + inviteInputH - 2, PARTY_PURPLE);
                }
            }
            this.clickRects.add(new ClickRect(inviteInputX, inviteBarY, inviteInputW, inviteInputH, "focus_invite"));

            // "Invite" button
            int invBtnW = 50;
            int invBtnX = inviteInputX + inviteInputW + 6;
            boolean invHover = mouseX >= invBtnX && mouseX < invBtnX + invBtnW && mouseY >= inviteBarY && mouseY < inviteBarY + inviteInputH;
            UIHelper.drawButton(g, invBtnX, inviteBarY, invBtnW, inviteInputH, invHover);
            int invLabelX = invBtnX + (invBtnW - this.font.width("Invite")) / 2;
            g.drawString(this.font, "Invite", invLabelX, inviteBarY + 3, PARTY_PURPLE, false);
            this.clickRects.add(new ClickRect(invBtnX, inviteBarY, invBtnW, inviteInputH, "send_invite"));

            listTop = inviteBarY + inviteInputH + 6;
        } else {
            listTop = panelTop + 18;
        }

        // Members list area
        int listBottom = panelBottom - 30;
        int listX = 8;
        int listW = leftPanelW - 20;

        g.enableScissor(listX, listTop, listX + listW, listBottom);

        int visibleH = listBottom - listTop;
        int totalH = this.members.size() * ROW_HEIGHT;
        int maxScroll = Math.max(0, totalH - visibleH);
        this.scroll = Math.max(0, Math.min(this.scroll, maxScroll));

        for (int i = 0; i < this.members.size(); i++) {
            int rowY = listTop + i * ROW_HEIGHT - this.scroll;
            if (rowY + ROW_HEIGHT < listTop || rowY > listBottom) continue;

            MemberEntry m = this.members.get(i);
            boolean rowHover = mouseX >= listX && mouseX < listX + listW && mouseY >= rowY && mouseY < rowY + ROW_HEIGHT
                    && mouseY >= listTop && mouseY < listBottom;

            // Row background
            int rowBg = i % 2 == 0 ? ROW_EVEN : ROW_ODD;
            g.fill(listX, rowY, listX + listW, rowY + ROW_HEIGHT, rowBg);
            if (rowHover) {
                g.fill(listX, rowY, listX + listW, rowY + ROW_HEIGHT, 0x18FFFFFF);
            }

            // Online dot
            int dotColor = m.online ? ONLINE_GREEN : 0xFF4A4A50;
            int dotX = listX + 6;
            int dotY = rowY + (ROW_HEIGHT - 4) / 2;
            g.fill(dotX, dotY, dotX + 4, dotY + 4, dotColor);

            // Name (gold for leader, white for others)
            boolean isMemberLeader = m.uuid.equals(this.leaderUuid);
            int nameColor;
            String displayName;
            if (isMemberLeader) {
                nameColor = 0xFFFFD700;
                displayName = "\u2654 " + m.name;
            } else {
                nameColor = m.online ? 0xFFFFFFFF : 0xFF8B949E;
                displayName = m.name;
            }
            g.drawString(this.font, displayName, listX + 14, rowY + (ROW_HEIGHT - 9) / 2, nameColor, false);

            // Kick button (only for leader, not on self)
            if (this.isLeader && !isMemberLeader) {
                int btnY = rowY + 3;
                int btnH = ROW_HEIGHT - 6;
                int kickBtnW = 30;
                int kickBtnX = listX + listW - kickBtnW - 4;
                boolean kickHover = mouseX >= kickBtnX && mouseX < kickBtnX + kickBtnW
                        && mouseY >= btnY && mouseY < btnY + btnH && mouseY >= listTop && mouseY < listBottom;
                UIHelper.drawButton(g, kickBtnX, btnY, kickBtnW, btnH, kickHover);
                int kickTextX = kickBtnX + (kickBtnW - this.font.width("Kick")) / 2;
                g.drawString(this.font, "Kick", kickTextX, btnY + (btnH - 9) / 2, ERROR_RED, false);
                this.clickRects.add(new ClickRect(kickBtnX, btnY, kickBtnW, btnH, "kick:" + m.uuid));
            }
        }

        g.disableScissor();

        // Scrollbar
        if (totalH > visibleH) {
            float progress = maxScroll > 0 ? (float) this.scroll / (float) maxScroll : 0.0f;
            UIHelper.drawScrollbar(g, listX + listW + 2, listTop, listBottom - listTop, progress);
        }

        // Leave/Disband button at bottom of left panel
        int actionBtnW = 90;
        int actionBtnH = 18;
        int actionBtnX = 4 + (leftPanelW - 4 - actionBtnW) / 2;
        int actionBtnY = panelBottom - 24;
        String actionLabel = this.isLeader ? "Disband" : "Leave Party";
        String actionId = this.isLeader ? "disband_party" : "leave_party";
        boolean actionHover = mouseX >= actionBtnX && mouseX < actionBtnX + actionBtnW && mouseY >= actionBtnY && mouseY < actionBtnY + actionBtnH;
        UIHelper.drawButton(g, actionBtnX, actionBtnY, actionBtnW, actionBtnH, actionHover);
        int actionTextX = actionBtnX + (actionBtnW - this.font.width(actionLabel)) / 2;
        g.drawString(this.font, actionLabel, actionTextX, actionBtnY + (actionBtnH - 9) / 2, ERROR_RED, false);
        this.clickRects.add(new ClickRect(actionBtnX, actionBtnY, actionBtnW, actionBtnH, actionId));

        // === RIGHT PANEL: Pending Invites ===
        UIHelper.drawInsetPanel(g, rightPanelX, panelTop, rightPanelW, panelBottom - panelTop);

        int rY = panelTop + 4;
        g.drawString(this.font, "Pending Invites", rightPanelX + 6, rY, UIHelper.GOLD_MID, false);
        rY += 12;

        if (this.pendingInvites.isEmpty()) {
            g.drawString(this.font, "None", rightPanelX + 10, rY, 0xFF8B949E, false);
        } else {
            for (int i = 0; i < this.pendingInvites.size(); i++) {
                PendingEntry pe = this.pendingInvites.get(i);
                if (rY + ROW_HEIGHT > panelBottom - 6) break;

                int reqRowBg = i % 2 == 0 ? ROW_EVEN : ROW_ODD;
                g.fill(rightPanelX + 4, rY, rightPanelX + rightPanelW - 4, rY + ROW_HEIGHT, reqRowBg);

                // Name with "Invited" label
                g.drawString(this.font, pe.name, rightPanelX + 8, rY + (ROW_HEIGHT - 9) / 2, PARTY_PURPLE, false);

                String waitLabel = "Waiting...";
                int waitX = rightPanelX + rightPanelW - this.font.width(waitLabel) - 10;
                g.drawString(this.font, waitLabel, waitX, rY + (ROW_HEIGHT - 9) / 2, 0xFF8B949E, false);

                rY += ROW_HEIGHT;
            }
        }

        // Incoming invite (if the player is in a party but also has an invite, show nothing here --
        // incoming invites only appear when NOT in a party, which is handled in renderNoPartyView)
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean consumed) {
        if (consumed) {
            return super.mouseClicked(event, consumed);
        }
        int mx = (int) event.x();
        int my = (int) event.y();

        boolean clickedInviteBox = false;

        for (ClickRect r : this.clickRects) {
            if (mx >= r.x && mx < r.x + r.w && my >= r.y && my < r.y + r.h) {
                if (r.action.equals("focus_invite")) {
                    clickedInviteBox = true;
                } else {
                    handleClick(r.action);
                    return true;
                }
            }
        }

        this.inviteBoxFocused = clickedInviteBox;
        if (clickedInviteBox) {
            this.cursorBlink = 0;
            return true;
        }

        // Clicked outside any rect — unfocus
        this.inviteBoxFocused = false;
        return super.mouseClicked(event, consumed);
    }

    private void handleClick(String action) {
        if (action.equals("back")) {
            if (this.minecraft != null) {
                this.minecraft.setScreen(this.parent);
            }
            return;
        }

        if (action.equals("create_party")) {
            ClientPacketDistributor.sendToServer(
                (CustomPacketPayload) new ComputerActionPayload("party_create", ""),
                (CustomPacketPayload[]) new CustomPacketPayload[0]
            );
            return;
        }

        if (action.equals("send_invite")) {
            String name = this.inviteInput.trim();
            if (!name.isEmpty()) {
                ClientPacketDistributor.sendToServer(
                    (CustomPacketPayload) new ComputerActionPayload("party_invite", name),
                    (CustomPacketPayload[]) new CustomPacketPayload[0]
                );
                this.inviteInput = "";
            }
            return;
        }

        if (action.equals("accept_invite")) {
            ClientPacketDistributor.sendToServer(
                (CustomPacketPayload) new ComputerActionPayload("party_accept", ""),
                (CustomPacketPayload[]) new CustomPacketPayload[0]
            );
            return;
        }

        if (action.equals("decline_invite")) {
            ClientPacketDistributor.sendToServer(
                (CustomPacketPayload) new ComputerActionPayload("party_decline", ""),
                (CustomPacketPayload[]) new CustomPacketPayload[0]
            );
            return;
        }

        if (action.startsWith("kick:")) {
            String uuid = action.substring(5);
            ClientPacketDistributor.sendToServer(
                (CustomPacketPayload) new ComputerActionPayload("party_kick", uuid),
                (CustomPacketPayload[]) new CustomPacketPayload[0]
            );
            return;
        }

        if (action.equals("leave_party")) {
            ClientPacketDistributor.sendToServer(
                (CustomPacketPayload) new ComputerActionPayload("party_leave", ""),
                (CustomPacketPayload[]) new CustomPacketPayload[0]
            );
            return;
        }

        if (action.equals("disband_party")) {
            ClientPacketDistributor.sendToServer(
                (CustomPacketPayload) new ComputerActionPayload("party_disband", ""),
                (CustomPacketPayload[]) new CustomPacketPayload[0]
            );
            return;
        }
    }

    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();

        // Escape
        if (keyCode == 256) {
            if (this.minecraft != null) {
                this.minecraft.setScreen(this.parent);
            }
            return true;
        }

        // Enter - send invite if invite box focused
        if (keyCode == 257) {
            if (this.inviteBoxFocused) {
                String name = this.inviteInput.trim();
                if (!name.isEmpty()) {
                    ClientPacketDistributor.sendToServer(
                        (CustomPacketPayload) new ComputerActionPayload("party_invite", name),
                        (CustomPacketPayload[]) new CustomPacketPayload[0]
                    );
                    this.inviteInput = "";
                }
                return true;
            }
        }

        // Backspace
        if (keyCode == 259) {
            if (this.inviteBoxFocused && !this.inviteInput.isEmpty()) {
                this.inviteInput = this.inviteInput.substring(0, this.inviteInput.length() - 1);
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

        if (this.inviteBoxFocused) {
            if (this.inviteInput.length() < 32) {
                this.inviteInput += codePoint;
                this.cursorBlink = 0;
            }
            return true;
        }

        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        this.scroll -= (int) (scrollY * ROW_HEIGHT);
        this.scroll = Math.max(0, this.scroll);
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private record ClickRect(int x, int y, int w, int h, String action) {}
}

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

public class BountyBoardScreen extends Screen {
    private final Screen parent;

    private int activeTab = 2; // Only Hunt tab remains
    private List<BountyEntry> allBounties = new ArrayList<>();
    private List<BountyEntry> myBounties = new ArrayList<>();

    // Hunt tab data
    private List<HuntBountyEntry> availableHunts = new ArrayList<>();
    private List<ActiveHuntEntry> activeHunts = new ArrayList<>();
    private boolean huntDataLoaded = false;
    private boolean composingBounty = false;
    private String itemInput = ""; // item ID
    private String itemDisplayName = "";
    private String quantityInput = "1";
    private String priceInput = "";
    private boolean quantityFocused = false;
    private boolean priceFocused = false;
    private int scroll = 0;

    private boolean dataLoaded = false;
    private int refreshTimer = 0;
    private int cursorBlink = 0;
    private String statusMsg = "";
    private int statusTimer = 0;

    // Layout
    private int titleBarH;
    private int contentTop;
    private final List<ClickRect> clickRects = new ArrayList<>();

    // Colors
    private static final int BOUNTY_AMBER = 0xFFFFB300;
    private static final int ONLINE_GREEN = 0xFF3FB950;
    private static final int ERROR_RED = 0xFFFF6B6B;
    private static final int SUCCESS_GREEN = 0xFF3FB950;
    private static final int BG_DARK = 0xFF0D1117;
    private static final int PANEL_BG = 0xFF161B22;
    private static final int ROW_EVEN = 0xFF1C2128;
    private static final int ROW_ODD = 0xFF21262D;
    private static final int ROW_HEIGHT = 22;
    private static final int OFFLINE_GREY = 0xFF4A4A50;
    private static final int TAB_ACTIVE = 0xFF21262D;
    private static final int TAB_INACTIVE = 0xFF161B22;

    private record BountyEntry(int id, String posterName, String itemId, String itemName, int quantity,
                               int price, String timeAgo, boolean isOwn, boolean fulfilled, String fulfillerName) {}

    private record HuntBountyEntry(int id, String mobType, String mobDisplayName, String biomeHint, int reward) {}
    private record ActiveHuntEntry(int bountyId, String targetName, boolean completed, String timeLeft, String mobDisplayName) {}

    public BountyBoardScreen(Screen parent) {
        super(Component.literal("Bounties"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        Objects.requireNonNull(this.font);
        this.titleBarH = 9 + 16;
        this.contentTop = this.titleBarH + 6;
        if (!this.dataLoaded) {
            requestBountyData();
        }
        if (!this.huntDataLoaded) {
            requestHuntData();
        }
    }

    private void requestBountyData() {
        ClientPacketDistributor.sendToServer(
            (CustomPacketPayload) new ComputerActionPayload("bounty_request", ""),
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
        if (resp != null && resp.dataType().equals("bounty_data")) {
            ComputerDataPayload.lastResponse = null;
            parseBountyData(resp.jsonData());
            this.dataLoaded = true;
        }

        // Poll for hunt data response
        if (resp != null && resp.dataType().equals("bounty_hunt_data")) {
            ComputerDataPayload.lastResponse = null;
            parseHuntData(resp.jsonData());
            this.huntDataLoaded = true;
        }

        // Check for action results
        if (resp != null && resp.dataType().equals("bounty_result")) {
            ComputerDataPayload.lastResponse = null;
            parseResult(resp.jsonData());
        }

        // Consume error responses so the screen doesn't stay stuck
        if (resp != null && "error".equals(resp.dataType())) {
            ComputerDataPayload.lastResponse = null;
            this.dataLoaded = true;
            this.huntDataLoaded = true;
        }

        // Auto-refresh every 60 ticks
        this.refreshTimer++;
        if (this.refreshTimer >= 60) {
            this.refreshTimer = 0;
            requestBountyData();
            if (this.activeTab == 2) {
                requestHuntData();
            }
        }
    }

    private void parseBountyData(String json) {
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();

            this.allBounties.clear();
            if (obj.has("bounties")) {
                JsonArray arr = obj.getAsJsonArray("bounties");
                for (JsonElement el : arr) {
                    JsonObject b = el.getAsJsonObject();
                    this.allBounties.add(new BountyEntry(
                        b.get("id").getAsInt(),
                        b.get("posterName").getAsString(),
                        b.get("itemId").getAsString(),
                        b.get("itemName").getAsString(),
                        b.get("quantity").getAsInt(),
                        b.get("price").getAsInt(),
                        b.has("timeAgo") ? b.get("timeAgo").getAsString() : "",
                        b.has("isOwn") && b.get("isOwn").getAsBoolean(),
                        false, ""
                    ));
                }
            }

            this.myBounties.clear();
            if (obj.has("myBounties")) {
                JsonArray arr = obj.getAsJsonArray("myBounties");
                for (JsonElement el : arr) {
                    JsonObject b = el.getAsJsonObject();
                    this.myBounties.add(new BountyEntry(
                        b.get("id").getAsInt(),
                        "", // posterName not needed for own
                        "", // itemId not needed for display
                        b.get("itemName").getAsString(),
                        b.get("quantity").getAsInt(),
                        b.get("price").getAsInt(),
                        "",
                        true,
                        b.has("fulfilled") && b.get("fulfilled").getAsBoolean(),
                        b.has("fulfillerName") ? b.get("fulfillerName").getAsString() : ""
                    ));
                }
            }
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to parse bounty board data", e);
        }
    }

    private void requestHuntData() {
        ClientPacketDistributor.sendToServer(
            (CustomPacketPayload) new ComputerActionPayload("bounty_hunt_request", ""),
            (CustomPacketPayload[]) new CustomPacketPayload[0]
        );
    }

    private void parseHuntData(String json) {
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();

            this.availableHunts.clear();
            if (obj.has("availableBounties")) {
                JsonArray arr = obj.getAsJsonArray("availableBounties");
                for (JsonElement el : arr) {
                    JsonObject h = el.getAsJsonObject();
                    this.availableHunts.add(new HuntBountyEntry(
                        h.get("id").getAsInt(),
                        h.get("mobType").getAsString(),
                        h.get("mobDisplayName").getAsString(),
                        h.get("biomeHint").getAsString(),
                        h.get("reward").getAsInt()
                    ));
                }
            }

            this.activeHunts.clear();
            if (obj.has("activeBounties")) {
                JsonArray arr = obj.getAsJsonArray("activeBounties");
                for (JsonElement el : arr) {
                    JsonObject a = el.getAsJsonObject();
                    this.activeHunts.add(new ActiveHuntEntry(
                        a.get("bountyId").getAsInt(),
                        a.get("targetName").getAsString(),
                        a.has("completed") && a.get("completed").getAsBoolean(),
                        a.has("timeLeft") ? a.get("timeLeft").getAsString() : "",
                        a.has("mobDisplayName") ? a.get("mobDisplayName").getAsString() : ""
                    ));
                }
            }
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to parse bounty board action response", e);
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
            requestBountyData();
            if (this.activeTab == 2) {
                requestHuntData();
            }
            // Close compose modal on success
            if (success && this.composingBounty) {
                this.composingBounty = false;
                this.itemInput = "";
                this.itemDisplayName = "";
                this.quantityInput = "1";
                this.priceInput = "";
            }
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to handle bounty board action", e);
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
        UIHelper.drawCenteredTitle(g, this.font, "Bounties", this.width / 2, titleY);

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

        if (this.composingBounty) {
            renderComposeModal(g, mouseX, mouseY);
        } else {
            renderHuntTab(g, mouseX, mouseY);
        }

        // Status message at bottom
        if (!this.statusMsg.isEmpty() && this.statusTimer > 0) {
            int msgW = this.font.width(this.statusMsg) + 20;
            int msgX = (this.width - msgW) / 2;
            int msgY = this.height - 24;
            UIHelper.drawCard(g, msgX, msgY, msgW, 18);
            boolean isError = this.statusMsg.contains("not enough") || this.statusMsg.contains("Not enough")
                    || this.statusMsg.contains("Failed") || this.statusMsg.contains("Cannot")
                    || this.statusMsg.contains("already") || this.statusMsg.contains("need")
                    || this.statusMsg.contains("Need") || this.statusMsg.contains("Unknown")
                    || this.statusMsg.contains("Invalid") || this.statusMsg.contains("Minimum")
                    || this.statusMsg.contains("not found") || this.statusMsg.contains("own bounty");
            int msgColor = isError ? ERROR_RED : SUCCESS_GREEN;
            g.drawCenteredString(this.font, this.statusMsg, this.width / 2, msgY + 5, msgColor);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void renderTabBar(GuiGraphics g, int mouseX, int mouseY) {
        int tabW = 80;
        int tabH = 16;
        int tabY = this.contentTop - 2;

        // Browse tab
        int tab0X = 8;
        boolean tab0Hover = mouseX >= tab0X && mouseX < tab0X + tabW && mouseY >= tabY && mouseY < tabY + tabH;
        int tab0Bg = this.activeTab == 0 ? TAB_ACTIVE : TAB_INACTIVE;
        g.fill(tab0X, tabY, tab0X + tabW, tabY + tabH, tab0Bg);
        if (tab0Hover && this.activeTab != 0) {
            g.fill(tab0X, tabY, tab0X + tabW, tabY + tabH, 0x18FFFFFF);
        }
        if (this.activeTab == 0) {
            g.fill(tab0X, tabY + tabH - 2, tab0X + tabW, tabY + tabH, BOUNTY_AMBER);
        }
        g.drawCenteredString(this.font, "Browse", tab0X + tabW / 2, tabY + (tabH - 9) / 2,
                this.activeTab == 0 ? BOUNTY_AMBER : OFFLINE_GREY);
        this.clickRects.add(new ClickRect(tab0X, tabY, tabW, tabH, "tab_browse"));

        // My Bounties tab
        int tab1X = tab0X + tabW + 4;
        boolean tab1Hover = mouseX >= tab1X && mouseX < tab1X + tabW && mouseY >= tabY && mouseY < tabY + tabH;
        int tab1Bg = this.activeTab == 1 ? TAB_ACTIVE : TAB_INACTIVE;
        g.fill(tab1X, tabY, tab1X + tabW, tabY + tabH, tab1Bg);
        if (tab1Hover && this.activeTab != 1) {
            g.fill(tab1X, tabY, tab1X + tabW, tabY + tabH, 0x18FFFFFF);
        }
        if (this.activeTab == 1) {
            g.fill(tab1X, tabY + tabH - 2, tab1X + tabW, tabY + tabH, BOUNTY_AMBER);
        }
        g.drawCenteredString(this.font, "My Bounties", tab1X + tabW / 2, tabY + (tabH - 9) / 2,
                this.activeTab == 1 ? BOUNTY_AMBER : OFFLINE_GREY);
        this.clickRects.add(new ClickRect(tab1X, tabY, tabW, tabH, "tab_my"));

        // Hunt tab
        int tab2X = tab1X + tabW + 4;
        boolean tab2Hover = mouseX >= tab2X && mouseX < tab2X + tabW && mouseY >= tabY && mouseY < tabY + tabH;
        int tab2Bg = this.activeTab == 2 ? TAB_ACTIVE : TAB_INACTIVE;
        g.fill(tab2X, tabY, tab2X + tabW, tabY + tabH, tab2Bg);
        if (tab2Hover && this.activeTab != 2) {
            g.fill(tab2X, tabY, tab2X + tabW, tabY + tabH, 0x18FFFFFF);
        }
        if (this.activeTab == 2) {
            g.fill(tab2X, tabY + tabH - 2, tab2X + tabW, tabY + tabH, BOUNTY_AMBER);
        }
        g.drawCenteredString(this.font, "Hunt", tab2X + tabW / 2, tabY + (tabH - 9) / 2,
                this.activeTab == 2 ? BOUNTY_AMBER : OFFLINE_GREY);
        this.clickRects.add(new ClickRect(tab2X, tabY, tabW, tabH, "tab_hunt"));
    }

    private void renderBrowseTab(GuiGraphics g, int mouseX, int mouseY) {
        int panelTop = this.contentTop + 18;
        int panelBottom = this.height - 30;
        int panelX = 4;
        int panelW = this.width - 8;

        UIHelper.drawInsetPanel(g, panelX, panelTop, panelW, panelBottom - panelTop);

        // Column headers
        int headerY = panelTop + 4;
        int colItem = panelX + 8;
        int colQty = panelX + panelW / 3;
        int colPrice = panelX + (int)(panelW * 0.45);
        int colPoster = panelX + (int)(panelW * 0.6);
        int colAction = panelX + panelW - 60;

        g.drawString(this.font, "Item", colItem, headerY, UIHelper.GOLD_MID, false);
        g.drawString(this.font, "Qty", colQty, headerY, UIHelper.GOLD_MID, false);
        g.drawString(this.font, "Price", colPrice, headerY, UIHelper.GOLD_MID, false);
        g.drawString(this.font, "Posted By", colPoster, headerY, UIHelper.GOLD_MID, false);

        // Divider
        int divY = headerY + 11;
        UIHelper.drawHorizontalDivider(g, panelX + 6, divY, panelW - 12);

        // List area
        int listTop = divY + 4;
        int listBottom = panelBottom - 6;
        int listW = panelW - 12;

        g.enableScissor(panelX + 4, listTop, panelX + panelW - 4, listBottom);

        int visibleH = listBottom - listTop;
        int totalH = this.allBounties.size() * ROW_HEIGHT;
        int maxScroll = Math.max(0, totalH - visibleH);
        this.scroll = Math.max(0, Math.min(this.scroll, maxScroll));

        if (this.allBounties.isEmpty()) {
            g.drawString(this.font, "No active bounties. Post one!", panelX + 20, listTop + 10, OFFLINE_GREY, false);
        }

        for (int i = 0; i < this.allBounties.size(); i++) {
            int rowY = listTop + i * ROW_HEIGHT - this.scroll;
            if (rowY + ROW_HEIGHT < listTop || rowY > listBottom) continue;

            BountyEntry b = this.allBounties.get(i);
            boolean rowHover = mouseX >= panelX + 4 && mouseX < panelX + panelW - 4
                    && mouseY >= rowY && mouseY < rowY + ROW_HEIGHT
                    && mouseY >= listTop && mouseY < listBottom;

            // Row background
            int rowBg = i % 2 == 0 ? ROW_EVEN : ROW_ODD;
            g.fill(panelX + 4, rowY, panelX + panelW - 4, rowY + ROW_HEIGHT, rowBg);
            if (rowHover) {
                g.fill(panelX + 4, rowY, panelX + panelW - 4, rowY + ROW_HEIGHT, 0x18FFFFFF);
            }

            int textY = rowY + (ROW_HEIGHT - 9) / 2;

            // Item name (truncate if too long)
            String displayItem = b.itemName;
            int maxItemW = colQty - colItem - 4;
            while (this.font.width(displayItem) > maxItemW && displayItem.length() > 1) {
                displayItem = displayItem.substring(0, displayItem.length() - 1);
            }
            g.drawString(this.font, displayItem, colItem, textY, 0xFFE6EDF3, false);

            // Quantity
            g.drawString(this.font, String.valueOf(b.quantity), colQty, textY, 0xFFE6EDF3, false);

            // Price
            g.drawString(this.font, b.price + " MC", colPrice, textY, BOUNTY_AMBER, false);

            // Poster name + time ago
            String posterInfo = b.posterName;
            if (!b.timeAgo.isEmpty()) {
                posterInfo += " (" + b.timeAgo + ")";
            }
            int maxPosterW = colAction - colPoster - 4;
            while (this.font.width(posterInfo) > maxPosterW && posterInfo.length() > 1) {
                posterInfo = posterInfo.substring(0, posterInfo.length() - 1);
            }
            g.drawString(this.font, posterInfo, colPoster, textY, OFFLINE_GREY, false);

            // Fulfill button (not shown for own bounties)
            if (!b.isOwn) {
                int btnW = 44;
                int btnH = ROW_HEIGHT - 6;
                int btnX = panelX + panelW - btnW - 10;
                int btnY = rowY + 3;
                boolean btnHover = mouseX >= btnX && mouseX < btnX + btnW
                        && mouseY >= btnY && mouseY < btnY + btnH
                        && mouseY >= listTop && mouseY < listBottom;
                UIHelper.drawButton(g, btnX, btnY, btnW, btnH, btnHover);
                int labelX = btnX + (btnW - this.font.width("Fulfill")) / 2;
                g.drawString(this.font, "Fulfill", labelX, btnY + (btnH - 9) / 2, ONLINE_GREEN, false);
                this.clickRects.add(new ClickRect(btnX, btnY, btnW, btnH, "fulfill:" + b.id));
            } else {
                g.drawString(this.font, "(yours)", panelX + panelW - 50, textY, OFFLINE_GREY, false);
            }
        }

        g.disableScissor();

        // Scrollbar
        if (totalH > visibleH) {
            float progress = maxScroll > 0 ? (float) this.scroll / (float) maxScroll : 0.0f;
            UIHelper.drawScrollbar(g, panelX + panelW - 6, listTop, listBottom - listTop, progress);
        }
    }

    private void renderMyBountiesTab(GuiGraphics g, int mouseX, int mouseY) {
        int panelTop = this.contentTop + 18;
        int panelBottom = this.height - 30;
        int panelX = 4;
        int panelW = this.width - 8;

        UIHelper.drawInsetPanel(g, panelX, panelTop, panelW, panelBottom - panelTop);

        // Post Bounty button
        int postBtnW = 100;
        int postBtnH = 16;
        int postBtnX = panelX + panelW - postBtnW - 10;
        int postBtnY = panelTop + 4;
        boolean postHover = mouseX >= postBtnX && mouseX < postBtnX + postBtnW
                && mouseY >= postBtnY && mouseY < postBtnY + postBtnH;
        UIHelper.drawButton(g, postBtnX, postBtnY, postBtnW, postBtnH, postHover);
        int postLabelX = postBtnX + (postBtnW - this.font.width("+ Post Bounty")) / 2;
        g.drawString(this.font, "+ Post Bounty", postLabelX, postBtnY + (postBtnH - 9) / 2, BOUNTY_AMBER, false);
        this.clickRects.add(new ClickRect(postBtnX, postBtnY, postBtnW, postBtnH, "compose"));

        // Active bounty count
        long activeCount = this.myBounties.stream().filter(b -> !b.fulfilled).count();
        g.drawString(this.font, "Active: " + activeCount + "/5", panelX + 10, panelTop + 8, UIHelper.GOLD_MID, false);

        // Column headers
        int headerY = panelTop + 24;
        int colItem = panelX + 8;
        int colQty = panelX + panelW / 3;
        int colPrice = panelX + (int)(panelW * 0.45);
        int colStatus = panelX + (int)(panelW * 0.6);
        int colAction = panelX + panelW - 60;

        g.drawString(this.font, "Item", colItem, headerY, UIHelper.GOLD_MID, false);
        g.drawString(this.font, "Qty", colQty, headerY, UIHelper.GOLD_MID, false);
        g.drawString(this.font, "Price", colPrice, headerY, UIHelper.GOLD_MID, false);
        g.drawString(this.font, "Status", colStatus, headerY, UIHelper.GOLD_MID, false);

        // Divider
        int divY = headerY + 11;
        UIHelper.drawHorizontalDivider(g, panelX + 6, divY, panelW - 12);

        // List area
        int listTop = divY + 4;
        int listBottom = panelBottom - 6;

        g.enableScissor(panelX + 4, listTop, panelX + panelW - 4, listBottom);

        int visibleH = listBottom - listTop;
        int totalH = this.myBounties.size() * ROW_HEIGHT;
        int maxScroll = Math.max(0, totalH - visibleH);
        this.scroll = Math.max(0, Math.min(this.scroll, maxScroll));

        if (this.myBounties.isEmpty()) {
            g.drawString(this.font, "You have no bounties.", panelX + 20, listTop + 10, OFFLINE_GREY, false);
        }

        for (int i = 0; i < this.myBounties.size(); i++) {
            int rowY = listTop + i * ROW_HEIGHT - this.scroll;
            if (rowY + ROW_HEIGHT < listTop || rowY > listBottom) continue;

            BountyEntry b = this.myBounties.get(i);
            boolean rowHover = mouseX >= panelX + 4 && mouseX < panelX + panelW - 4
                    && mouseY >= rowY && mouseY < rowY + ROW_HEIGHT
                    && mouseY >= listTop && mouseY < listBottom;

            int rowBg = i % 2 == 0 ? ROW_EVEN : ROW_ODD;
            g.fill(panelX + 4, rowY, panelX + panelW - 4, rowY + ROW_HEIGHT, rowBg);
            if (rowHover) {
                g.fill(panelX + 4, rowY, panelX + panelW - 4, rowY + ROW_HEIGHT, 0x18FFFFFF);
            }

            int textY = rowY + (ROW_HEIGHT - 9) / 2;

            // Item name
            String displayItem = b.itemName;
            int maxItemW = colQty - colItem - 4;
            while (this.font.width(displayItem) > maxItemW && displayItem.length() > 1) {
                displayItem = displayItem.substring(0, displayItem.length() - 1);
            }
            g.drawString(this.font, displayItem, colItem, textY, 0xFFE6EDF3, false);

            // Quantity
            g.drawString(this.font, String.valueOf(b.quantity), colQty, textY, 0xFFE6EDF3, false);

            // Price
            g.drawString(this.font, b.price + " MC", colPrice, textY, BOUNTY_AMBER, false);

            // Status
            if (b.fulfilled) {
                String statusStr = "Fulfilled";
                if (!b.fulfillerName.isEmpty()) {
                    statusStr += " by " + b.fulfillerName;
                }
                int maxStatusW = colAction - colStatus - 4;
                while (this.font.width(statusStr) > maxStatusW && statusStr.length() > 1) {
                    statusStr = statusStr.substring(0, statusStr.length() - 1);
                }
                g.drawString(this.font, statusStr, colStatus, textY, ONLINE_GREEN, false);

                // Collect button
                int btnW = 46;
                int btnH = ROW_HEIGHT - 6;
                int btnX = panelX + panelW - btnW - 10;
                int btnY = rowY + 3;
                boolean btnHover = mouseX >= btnX && mouseX < btnX + btnW
                        && mouseY >= btnY && mouseY < btnY + btnH
                        && mouseY >= listTop && mouseY < listBottom;
                UIHelper.drawButton(g, btnX, btnY, btnW, btnH, btnHover);
                int labelX = btnX + (btnW - this.font.width("Collect")) / 2;
                g.drawString(this.font, "Collect", labelX, btnY + (btnH - 9) / 2, ONLINE_GREEN, false);
                this.clickRects.add(new ClickRect(btnX, btnY, btnW, btnH, "collect:" + b.id));
            } else {
                g.drawString(this.font, "Active", colStatus, textY, BOUNTY_AMBER, false);

                // Cancel button
                int btnW = 44;
                int btnH = ROW_HEIGHT - 6;
                int btnX = panelX + panelW - btnW - 10;
                int btnY = rowY + 3;
                boolean btnHover = mouseX >= btnX && mouseX < btnX + btnW
                        && mouseY >= btnY && mouseY < btnY + btnH
                        && mouseY >= listTop && mouseY < listBottom;
                UIHelper.drawButton(g, btnX, btnY, btnW, btnH, btnHover);
                int labelX = btnX + (btnW - this.font.width("Cancel")) / 2;
                g.drawString(this.font, "Cancel", labelX, btnY + (btnH - 9) / 2, ERROR_RED, false);
                this.clickRects.add(new ClickRect(btnX, btnY, btnW, btnH, "cancel:" + b.id));
            }
        }

        g.disableScissor();

        // Scrollbar
        if (totalH > visibleH) {
            float progress = maxScroll > 0 ? (float) this.scroll / (float) maxScroll : 0.0f;
            UIHelper.drawScrollbar(g, panelX + panelW - 6, listTop, listBottom - listTop, progress);
        }
    }

    private void renderHuntTab(GuiGraphics g, int mouseX, int mouseY) {
        int panelTop = this.contentTop + 18;
        int panelBottom = this.height - 30;
        int panelX = 4;
        int panelW = this.width - 8;

        UIHelper.drawInsetPanel(g, panelX, panelTop, panelW, panelBottom - panelTop);

        // ---- Available Bounties Section ----
        int sectionLabelY = panelTop + 4;
        g.drawString(this.font, "Available Bounties", panelX + 8, sectionLabelY, BOUNTY_AMBER, false);

        // Column headers
        int headerY = sectionLabelY + 12;
        int colMob = panelX + 8;
        int colBiome = panelX + (int)(panelW * 0.35);
        int colReward = panelX + (int)(panelW * 0.55);
        int colAction = panelX + panelW - 60;

        g.drawString(this.font, "Target", colMob, headerY, UIHelper.GOLD_MID, false);
        g.drawString(this.font, "Biome", colBiome, headerY, UIHelper.GOLD_MID, false);
        g.drawString(this.font, "Reward", colReward, headerY, UIHelper.GOLD_MID, false);

        int divY = headerY + 11;
        UIHelper.drawHorizontalDivider(g, panelX + 6, divY, panelW - 12);

        // Available bounties list
        int availListTop = divY + 4;
        int availableCount = this.availableHunts.size();
        int maxAvailableRows = 8;
        int availListBottom = availListTop + Math.min(availableCount, maxAvailableRows) * ROW_HEIGHT;

        if (this.availableHunts.isEmpty()) {
            g.drawString(this.font, "No bounties available today.", panelX + 20, availListTop + 4, OFFLINE_GREY, false);
            availListBottom = availListTop + ROW_HEIGHT;
        }

        for (int i = 0; i < this.availableHunts.size() && i < maxAvailableRows; i++) {
            int rowY = availListTop + i * ROW_HEIGHT;
            HuntBountyEntry h = this.availableHunts.get(i);

            int rowBg = i % 2 == 0 ? ROW_EVEN : ROW_ODD;
            g.fill(panelX + 4, rowY, panelX + panelW - 4, rowY + ROW_HEIGHT, rowBg);

            boolean rowHover = mouseX >= panelX + 4 && mouseX < panelX + panelW - 4
                    && mouseY >= rowY && mouseY < rowY + ROW_HEIGHT;
            if (rowHover) {
                g.fill(panelX + 4, rowY, panelX + panelW - 4, rowY + ROW_HEIGHT, 0x18FFFFFF);
            }

            int textY = rowY + (ROW_HEIGHT - 9) / 2;

            // Mob display name
            g.drawString(this.font, h.mobDisplayName, colMob, textY, 0xFFE6EDF3, false);

            // Biome hint
            g.drawString(this.font, h.biomeHint, colBiome, textY, OFFLINE_GREY, false);

            // Reward
            g.drawString(this.font, h.reward + " MC", colReward, textY, BOUNTY_AMBER, false);

            // Accept button
            int btnW = 46;
            int btnH = ROW_HEIGHT - 6;
            int btnX = panelX + panelW - btnW - 10;
            int btnY = rowY + 3;
            boolean btnHover = mouseX >= btnX && mouseX < btnX + btnW
                    && mouseY >= btnY && mouseY < btnY + btnH;
            UIHelper.drawButton(g, btnX, btnY, btnW, btnH, btnHover);
            int labelX = btnX + (btnW - this.font.width("Accept")) / 2;
            g.drawString(this.font, "Accept", labelX, btnY + (btnH - 9) / 2, ONLINE_GREEN, false);
            this.clickRects.add(new ClickRect(btnX, btnY, btnW, btnH, "hunt_accept:" + h.id));
        }

        // ---- Active Hunts Section ----
        int activeTop = availListBottom + 10;
        g.drawString(this.font, "Active Hunts (" + this.activeHunts.size() + "/3)", panelX + 8, activeTop, BOUNTY_AMBER, false);

        int activeHeaderY = activeTop + 12;
        g.drawString(this.font, "Target", colMob, activeHeaderY, UIHelper.GOLD_MID, false);
        g.drawString(this.font, "Status", colBiome, activeHeaderY, UIHelper.GOLD_MID, false);
        g.drawString(this.font, "Time Left", colReward, activeHeaderY, UIHelper.GOLD_MID, false);

        int activeDiv = activeHeaderY + 11;
        UIHelper.drawHorizontalDivider(g, panelX + 6, activeDiv, panelW - 12);

        int activeListTop = activeDiv + 4;
        if (this.activeHunts.isEmpty()) {
            g.drawString(this.font, "No active hunts. Accept a bounty above!", panelX + 20, activeListTop + 4, OFFLINE_GREY, false);
        }

        for (int i = 0; i < this.activeHunts.size(); i++) {
            int rowY = activeListTop + i * ROW_HEIGHT;
            if (rowY + ROW_HEIGHT > panelBottom - 6) break;

            ActiveHuntEntry ah = this.activeHunts.get(i);

            int rowBg = i % 2 == 0 ? ROW_EVEN : ROW_ODD;
            g.fill(panelX + 4, rowY, panelX + panelW - 4, rowY + ROW_HEIGHT, rowBg);

            boolean rowHover = mouseX >= panelX + 4 && mouseX < panelX + panelW - 4
                    && mouseY >= rowY && mouseY < rowY + ROW_HEIGHT;
            if (rowHover) {
                g.fill(panelX + 4, rowY, panelX + panelW - 4, rowY + ROW_HEIGHT, 0x18FFFFFF);
            }

            int textY = rowY + (ROW_HEIGHT - 9) / 2;

            // Mob display name
            g.drawString(this.font, ah.mobDisplayName, colMob, textY, 0xFFE6EDF3, false);

            // Status
            if (ah.completed) {
                g.drawString(this.font, "Completed!", colBiome, textY, ONLINE_GREEN, false);
            } else {
                g.drawString(this.font, "Hunting...", colBiome, textY, BOUNTY_AMBER, false);
            }

            // Time left
            g.drawString(this.font, ah.timeLeft, colReward, textY, OFFLINE_GREY, false);

            // Abandon button (only for non-completed)
            if (!ah.completed) {
                int btnW = 52;
                int btnH = ROW_HEIGHT - 6;
                int btnX = panelX + panelW - btnW - 10;
                int btnY = rowY + 3;
                boolean btnHover = mouseX >= btnX && mouseX < btnX + btnW
                        && mouseY >= btnY && mouseY < btnY + btnH;
                UIHelper.drawButton(g, btnX, btnY, btnW, btnH, btnHover);
                int labelX = btnX + (btnW - this.font.width("Abandon")) / 2;
                g.drawString(this.font, "Abandon", labelX, btnY + (btnH - 9) / 2, ERROR_RED, false);
                this.clickRects.add(new ClickRect(btnX, btnY, btnW, btnH, "hunt_abandon:" + ah.bountyId));
            }
        }
    }

    private void renderComposeModal(GuiGraphics g, int mouseX, int mouseY) {
        int panelW = Math.min(300, this.width - 20);
        int panelH = 150;
        int panelX = (this.width - panelW) / 2;
        int panelY = this.height / 2 - panelH / 2 - 10;

        UIHelper.drawPanel(g, panelX, panelY, panelW, panelH);

        // Title
        g.drawCenteredString(this.font, "Post Bounty", panelX + panelW / 2, panelY + 8, BOUNTY_AMBER);

        // Item row
        int rowY = panelY + 24;
        g.drawString(this.font, "Item:", panelX + 10, rowY + 3, UIHelper.GOLD_MID, false);

        // "Use Held Item" button
        int heldBtnW = 90;
        int heldBtnX = panelX + 50;
        int heldBtnH = 14;
        boolean heldHover = mouseX >= heldBtnX && mouseX < heldBtnX + heldBtnW
                && mouseY >= rowY && mouseY < rowY + heldBtnH;
        UIHelper.drawButton(g, heldBtnX, rowY, heldBtnW, heldBtnH, heldHover);
        g.drawString(this.font, "Use Held Item", heldBtnX + (heldBtnW - this.font.width("Use Held Item")) / 2,
                rowY + 3, 0xFFE6EDF3, false);
        this.clickRects.add(new ClickRect(heldBtnX, rowY, heldBtnW, heldBtnH, "use_held"));

        // Display selected item
        if (!this.itemDisplayName.isEmpty()) {
            g.drawString(this.font, this.itemDisplayName, heldBtnX + heldBtnW + 8, rowY + 3, ONLINE_GREEN, false);
        } else {
            g.drawString(this.font, "None", heldBtnX + heldBtnW + 8, rowY + 3, OFFLINE_GREY, false);
        }

        // Quantity row
        int qtyRowY = rowY + 22;
        g.drawString(this.font, "Qty:", panelX + 10, qtyRowY + 3, UIHelper.GOLD_MID, false);
        int qtyInputX = panelX + 50;
        int qtyInputW = 60;
        int qtyInputH = 14;

        int qtyBorder = this.quantityFocused ? BOUNTY_AMBER : OFFLINE_GREY;
        g.fill(qtyInputX - 1, qtyRowY - 1, qtyInputX + qtyInputW + 1, qtyRowY + qtyInputH + 1, qtyBorder);
        g.fill(qtyInputX, qtyRowY, qtyInputX + qtyInputW, qtyRowY + qtyInputH, 0xFF0D1117);

        String qtyDisplay = this.quantityInput;
        if (qtyDisplay.isEmpty() && !this.quantityFocused) {
            g.drawString(this.font, "1", qtyInputX + 3, qtyRowY + 3, OFFLINE_GREY, false);
        } else {
            g.drawString(this.font, qtyDisplay, qtyInputX + 3, qtyRowY + 3, 0xFFE6EDF3, false);
            if (this.quantityFocused && (this.cursorBlink / 10) % 2 == 0) {
                int cursorX = qtyInputX + 3 + this.font.width(qtyDisplay);
                g.fill(cursorX, qtyRowY + 2, cursorX + 1, qtyRowY + qtyInputH - 2, BOUNTY_AMBER);
            }
        }
        this.clickRects.add(new ClickRect(qtyInputX, qtyRowY, qtyInputW, qtyInputH, "focus_qty"));

        // Price row
        int priceRowY = qtyRowY + 22;
        g.drawString(this.font, "Price:", panelX + 10, priceRowY + 3, UIHelper.GOLD_MID, false);
        int priceInputX = panelX + 50;
        int priceInputW = 80;
        int priceInputH = 14;

        int priceBorder = this.priceFocused ? BOUNTY_AMBER : OFFLINE_GREY;
        g.fill(priceInputX - 1, priceRowY - 1, priceInputX + priceInputW + 1, priceRowY + priceInputH + 1, priceBorder);
        g.fill(priceInputX, priceRowY, priceInputX + priceInputW, priceRowY + priceInputH, 0xFF0D1117);

        String priceDisplay = this.priceInput;
        if (priceDisplay.isEmpty() && !this.priceFocused) {
            g.drawString(this.font, "MC", priceInputX + 3, priceRowY + 3, OFFLINE_GREY, false);
        } else {
            g.drawString(this.font, priceDisplay, priceInputX + 3, priceRowY + 3, 0xFFE6EDF3, false);
            if (this.priceFocused && (this.cursorBlink / 10) % 2 == 0) {
                int cursorX = priceInputX + 3 + this.font.width(priceDisplay);
                g.fill(cursorX, priceRowY + 2, cursorX + 1, priceRowY + priceInputH - 2, BOUNTY_AMBER);
            }
        }
        this.clickRects.add(new ClickRect(priceInputX, priceRowY, priceInputW, priceInputH, "focus_price"));

        // Min price hint
        if (!this.itemInput.isEmpty()) {
            int qty = 1;
            try { qty = Integer.parseInt(this.quantityInput); } catch (NumberFormatException ignored) {}
            if (qty <= 0) qty = 1;
            int minPerItem = getMinPriceForItem(this.itemInput);
            int minTotal = minPerItem * qty;
            g.drawString(this.font, "Min: " + minTotal + " MC", priceInputX + priceInputW + 6, priceRowY + 3, OFFLINE_GREY, false);
        }

        // Post button
        int postW = 60;
        int postH = 18;
        int postX = panelX + panelW / 2 - postW - 5;
        int postY = panelY + panelH - postH - 10;
        boolean postHover = mouseX >= postX && mouseX < postX + postW && mouseY >= postY && mouseY < postY + postH;
        UIHelper.drawButton(g, postX, postY, postW, postH, postHover);
        g.drawString(this.font, "Post", postX + (postW - this.font.width("Post")) / 2,
                postY + (postH - 9) / 2, ONLINE_GREEN, false);
        this.clickRects.add(new ClickRect(postX, postY, postW, postH, "submit_post"));

        // Cancel button
        int cancelW = 60;
        int cancelX = panelX + panelW / 2 + 5;
        boolean cancelHover = mouseX >= cancelX && mouseX < cancelX + cancelW && mouseY >= postY && mouseY < postY + postH;
        UIHelper.drawButton(g, cancelX, postY, cancelW, postH, cancelHover);
        g.drawString(this.font, "Cancel", cancelX + (cancelW - this.font.width("Cancel")) / 2,
                postY + (postH - 9) / 2, ERROR_RED, false);
        this.clickRects.add(new ClickRect(cancelX, postY, cancelW, postH, "cancel_compose"));
    }

    private int getMinPriceForItem(String itemId) {
        // Mirror of server-side MIN_PRICES default
        // For client display hint, we use a simple default
        return 3;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean consumed) {
        if (consumed) {
            return super.mouseClicked(event, consumed);
        }
        int mx = (int) event.x();
        int my = (int) event.y();

        boolean clickedQty = false;
        boolean clickedPrice = false;

        for (ClickRect r : this.clickRects) {
            if (mx >= r.x && mx < r.x + r.w && my >= r.y && my < r.y + r.h) {
                if (r.action.equals("focus_qty")) {
                    clickedQty = true;
                } else if (r.action.equals("focus_price")) {
                    clickedPrice = true;
                } else {
                    handleClick(r.action);
                    return true;
                }
            }
        }

        this.quantityFocused = clickedQty;
        this.priceFocused = clickedPrice;
        if (clickedQty || clickedPrice) {
            this.cursorBlink = 0;
            return true;
        }

        // Clicked outside — unfocus
        this.quantityFocused = false;
        this.priceFocused = false;
        return super.mouseClicked(event, consumed);
    }

    private void handleClick(String action) {
        if (action.equals("back")) {
            if (this.composingBounty) {
                this.composingBounty = false;
                resetComposeState();
            } else if (this.minecraft != null) {
                this.minecraft.setScreen(this.parent);
            }
            return;
        }

        // Tab handlers removed — only Hunt tab remains

        if (action.startsWith("hunt_accept:")) {
            String idStr = action.substring(12);
            ClientPacketDistributor.sendToServer(
                (CustomPacketPayload) new ComputerActionPayload("bounty_hunt_accept", idStr),
                (CustomPacketPayload[]) new CustomPacketPayload[0]
            );
            // Refresh hunt data after a short delay via the result handler
            return;
        }

        if (action.startsWith("hunt_abandon:")) {
            String idStr = action.substring(13);
            ClientPacketDistributor.sendToServer(
                (CustomPacketPayload) new ComputerActionPayload("bounty_hunt_abandon", idStr),
                (CustomPacketPayload[]) new CustomPacketPayload[0]
            );
            return;
        }

        if (action.equals("compose")) {
            this.composingBounty = true;
            resetComposeState();
            return;
        }

        if (action.equals("cancel_compose")) {
            this.composingBounty = false;
            resetComposeState();
            return;
        }

        if (action.equals("use_held")) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                net.minecraft.world.item.ItemStack held = mc.player.getMainHandItem();
                if (!held.isEmpty()) {
                    net.minecraft.resources.Identifier itemKey = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(held.getItem());
                    if (itemKey != null) {
                        this.itemInput = itemKey.toString();
                        this.itemDisplayName = held.getHoverName().getString();
                    }
                } else {
                    this.statusMsg = "Hold an item in your main hand.";
                    this.statusTimer = 60;
                }
            }
            return;
        }

        if (action.equals("submit_post")) {
            submitBounty();
            return;
        }

        if (action.startsWith("fulfill:")) {
            String idStr = action.substring(8);
            ClientPacketDistributor.sendToServer(
                (CustomPacketPayload) new ComputerActionPayload("bounty_fulfill", idStr),
                (CustomPacketPayload[]) new CustomPacketPayload[0]
            );
            return;
        }

        if (action.startsWith("cancel:")) {
            String idStr = action.substring(7);
            ClientPacketDistributor.sendToServer(
                (CustomPacketPayload) new ComputerActionPayload("bounty_cancel", idStr),
                (CustomPacketPayload[]) new CustomPacketPayload[0]
            );
            return;
        }

        if (action.startsWith("collect:")) {
            String idStr = action.substring(8);
            ClientPacketDistributor.sendToServer(
                (CustomPacketPayload) new ComputerActionPayload("bounty_collect", idStr),
                (CustomPacketPayload[]) new CustomPacketPayload[0]
            );
            return;
        }
    }

    private void submitBounty() {
        if (this.itemInput.isEmpty()) {
            this.statusMsg = "Select an item first.";
            this.statusTimer = 60;
            return;
        }

        int qty;
        try {
            qty = Integer.parseInt(this.quantityInput);
        } catch (NumberFormatException e) {
            this.statusMsg = "Invalid quantity.";
            this.statusTimer = 60;
            return;
        }

        if (qty <= 0) {
            this.statusMsg = "Quantity must be at least 1.";
            this.statusTimer = 60;
            return;
        }

        int price;
        try {
            price = Integer.parseInt(this.priceInput);
        } catch (NumberFormatException e) {
            this.statusMsg = "Invalid price.";
            this.statusTimer = 60;
            return;
        }

        if (price <= 0) {
            this.statusMsg = "Price must be at least 1 MC.";
            this.statusTimer = 60;
            return;
        }

        // Send: "itemId:quantity:priceOffered" (itemId contains a colon, e.g., "minecraft:diamond")
        String data = this.itemInput + ":" + qty + ":" + price;
        ClientPacketDistributor.sendToServer(
            (CustomPacketPayload) new ComputerActionPayload("bounty_post", data),
            (CustomPacketPayload[]) new CustomPacketPayload[0]
        );
    }

    private void resetComposeState() {
        this.itemInput = "";
        this.itemDisplayName = "";
        this.quantityInput = "1";
        this.priceInput = "";
        this.quantityFocused = false;
        this.priceFocused = false;
    }

    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();

        // Escape
        if (keyCode == 256) {
            if (this.composingBounty) {
                this.composingBounty = false;
                resetComposeState();
                return true;
            }
            if (this.minecraft != null) {
                this.minecraft.setScreen(this.parent);
            }
            return true;
        }

        // Enter — submit if composing
        if (keyCode == 257) {
            if (this.composingBounty && (this.quantityFocused || this.priceFocused)) {
                submitBounty();
                return true;
            }
        }

        // Backspace
        if (keyCode == 259) {
            if (this.quantityFocused && !this.quantityInput.isEmpty()) {
                this.quantityInput = this.quantityInput.substring(0, this.quantityInput.length() - 1);
                this.cursorBlink = 0;
                return true;
            }
            if (this.priceFocused && !this.priceInput.isEmpty()) {
                this.priceInput = this.priceInput.substring(0, this.priceInput.length() - 1);
                this.cursorBlink = 0;
                return true;
            }
        }

        return super.keyPressed(event);
    }

    public boolean charTyped(net.minecraft.client.input.CharacterEvent event) {
        char codePoint = (char) event.codepoint();
        int modifiers = event.modifiers();
        // Only allow digits in quantity and price fields
        if (codePoint < '0' || codePoint > '9') return false;

        if (this.quantityFocused) {
            if (this.quantityInput.length() < 5) {
                this.quantityInput += codePoint;
                this.cursorBlink = 0;
            }
            return true;
        }

        if (this.priceFocused) {
            if (this.priceInput.length() < 7) {
                this.priceInput += codePoint;
                this.cursorBlink = 0;
            }
            return true;
        }

        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!this.composingBounty) {
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

package com.ultra.megamod.feature.computer.screen;

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

public class MarketplaceScreen extends Screen {
    private final Screen parent;

    // Tabs: 0=Buy (WTS listings), 1=Sell (WTB listings), 2=My Listings, 3=Notifications, 4=Trade Floor
    private int activeTab = 0;
    private int scroll = 0;

    // Data
    private List<ListingEntry> allListings = new ArrayList<>();
    private List<ListingEntry> myListings = new ArrayList<>();
    private List<NotifEntry> notifications = new ArrayList<>();
    private int unreadCount = 0;

    // Pending trade floor invite (incoming)
    private boolean hasInvite = false;
    private String inviterName = "";
    private String inviteItemName = "";
    private int inviteQuantity = 0;
    private String inviteListingType = "";
    private int inviteRemainingSec = 0;

    // Compose state
    private boolean composing = false;
    private String composeType = "WTS"; // WTS or WTB
    private String itemInput = "";
    private String itemDisplayName = "";
    private String quantityInput = "1";
    private String priceInput = "";
    private boolean quantityFocused = false;
    private boolean priceFocused = false;

    // Search
    private String searchQuery = "";
    private boolean searchFocused = false;

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
    private static final int MARKET_TEAL = 0xFF26C6DA;
    private static final int WTS_GREEN = 0xFF3FB950;
    private static final int WTB_BLUE = 0xFF58A6FF;
    private static final int ONLINE_GREEN = 0xFF3FB950;
    private static final int OFFLINE_GREY = 0xFF4A4A50;
    private static final int ERROR_RED = 0xFFFF6B6B;
    private static final int SUCCESS_GREEN = 0xFF3FB950;
    private static final int BG_DARK = 0xFF0D1117;
    private static final int PANEL_BG = 0xFF161B22;
    private static final int ROW_EVEN = 0xFF1C2128;
    private static final int ROW_ODD = 0xFF21262D;
    private static final int ROW_HEIGHT = 24;
    private static final int TAB_ACTIVE = 0xFF21262D;
    private static final int TAB_INACTIVE = 0xFF161B22;
    private static final int GOLD_COIN = 0xFFE8A838;
    private static final int NOTIF_RED = 0xFFFF4444;
    private static final int FLOOR_ORANGE = 0xFFFF9800;

    private record ListingEntry(int id, String type, String sellerName, String sellerUuid,
                                String itemId, String itemName, int quantity, int pricePerUnit,
                                int totalPrice, String timeLeft, boolean isOwn, boolean online) {}

    private record NotifEntry(String fromName, String fromUuid, int listingId,
                              String message, String timeAgo, boolean read) {}

    private record ClickRect(int x, int y, int w, int h, String action) {}

    public MarketplaceScreen(Screen parent) {
        super(Component.literal("Marketplace"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        Objects.requireNonNull(this.font);
        this.titleBarH = 9 + 16;
        this.contentTop = this.titleBarH + 6;
        if (!this.dataLoaded) {
            requestMarketData();
        }
    }

    private void requestMarketData() {
        ClientPacketDistributor.sendToServer(
                (CustomPacketPayload) new ComputerActionPayload("market_request", ""),
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

        // Poll for data response
        ComputerDataPayload resp = ComputerDataPayload.lastResponse;
        if (resp != null && resp.dataType().equals("market_data")) {
            ComputerDataPayload.lastResponse = null;
            parseMarketData(resp.jsonData());
            this.dataLoaded = true;
        }

        if (resp != null && resp.dataType().equals("market_result")) {
            ComputerDataPayload.lastResponse = null;
            parseResult(resp.jsonData());
        }

        if (resp != null && resp.dataType().equals("market_my_listings")) {
            ComputerDataPayload.lastResponse = null;
            parseMyListings(resp.jsonData());
        }

        // Consume error responses so the screen doesn't stay stuck
        if (resp != null && "error".equals(resp.dataType())) {
            ComputerDataPayload.lastResponse = null;
            this.dataLoaded = true;
        }

        // Auto-refresh every 60 ticks (3 seconds)
        this.refreshTimer++;
        if (this.refreshTimer >= 60) {
            this.refreshTimer = 0;
            if (!this.composing) {
                if (!this.searchQuery.isEmpty()) {
                    ClientPacketDistributor.sendToServer(
                            (CustomPacketPayload) new ComputerActionPayload("market_search", this.searchQuery),
                            (CustomPacketPayload[]) new CustomPacketPayload[0]
                    );
                } else {
                    requestMarketData();
                }
            }
        }
    }

    private void parseMarketData(String json) {
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();

            this.allListings.clear();
            if (obj.has("listings")) {
                JsonArray arr = obj.getAsJsonArray("listings");
                for (JsonElement el : arr) {
                    JsonObject l = el.getAsJsonObject();
                    this.allListings.add(parseListingEntry(l));
                }
            }

            this.myListings.clear();
            if (obj.has("myListings")) {
                JsonArray arr = obj.getAsJsonArray("myListings");
                for (JsonElement el : arr) {
                    JsonObject l = el.getAsJsonObject();
                    this.myListings.add(parseListingEntry(l));
                }
            }

            this.notifications.clear();
            if (obj.has("notifications")) {
                JsonArray arr = obj.getAsJsonArray("notifications");
                for (JsonElement el : arr) {
                    JsonObject n = el.getAsJsonObject();
                    this.notifications.add(new NotifEntry(
                            n.get("fromName").getAsString(),
                            n.has("fromUuid") ? n.get("fromUuid").getAsString() : "",
                            n.get("listingId").getAsInt(),
                            n.get("message").getAsString(),
                            n.has("timeAgo") ? n.get("timeAgo").getAsString() : "",
                            n.has("read") && n.get("read").getAsBoolean()
                    ));
                }
            }

            if (obj.has("unreadCount")) {
                this.unreadCount = obj.get("unreadCount").getAsInt();
            }

            // Parse pending invite
            if (obj.has("pendingInvite")) {
                JsonObject inv = obj.getAsJsonObject("pendingInvite");
                this.hasInvite = true;
                this.inviterName = inv.get("inviterName").getAsString();
                this.inviteRemainingSec = inv.get("remainingSec").getAsInt();
                this.inviteItemName = inv.has("itemName") ? inv.get("itemName").getAsString() : "";
                this.inviteQuantity = inv.has("quantity") ? inv.get("quantity").getAsInt() : 0;
                this.inviteListingType = inv.has("listingType") ? inv.get("listingType").getAsString() : "";
            } else {
                this.hasInvite = false;
            }
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to parse marketplace data", e);
        }
    }

    private void parseMyListings(String json) {
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            this.myListings.clear();
            if (obj.has("myListings")) {
                JsonArray arr = obj.getAsJsonArray("myListings");
                for (JsonElement el : arr) {
                    this.myListings.add(parseListingEntry(el.getAsJsonObject()));
                }
            }
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to parse marketplace listings", e);
        }
    }

    private ListingEntry parseListingEntry(JsonObject l) {
        return new ListingEntry(
                l.get("id").getAsInt(),
                l.get("type").getAsString(),
                l.get("sellerName").getAsString(),
                l.has("sellerUuid") ? l.get("sellerUuid").getAsString() : "",
                l.get("itemId").getAsString(),
                l.get("itemName").getAsString(),
                l.get("quantity").getAsInt(),
                l.get("pricePerUnit").getAsInt(),
                l.get("totalPrice").getAsInt(),
                l.has("timeLeft") ? l.get("timeLeft").getAsString() : "",
                l.has("isOwn") && l.get("isOwn").getAsBoolean(),
                l.has("online") && l.get("online").getAsBoolean()
        );
    }

    private void parseResult(String json) {
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            boolean success = obj.has("success") && obj.get("success").getAsBoolean();
            String msg = obj.has("message") ? obj.get("message").getAsString() : (success ? "Done!" : "Failed.");
            this.statusMsg = msg;
            this.statusTimer = 80;
            if (success && this.composing) {
                this.composing = false;
                resetComposeState();
            }
            requestMarketData();
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to handle marketplace action response", e);
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
        UIHelper.drawCenteredTitle(g, this.font, "Marketplace", this.width / 2, titleY);

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

        if (this.composing) {
            renderComposeModal(g, mouseX, mouseY);
        } else {
            renderTabBar(g, mouseX, mouseY);

            // Incoming invite banner
            if (this.hasInvite) {
                renderInviteBanner(g, mouseX, mouseY);
            }

            if (this.activeTab == 0 || this.activeTab == 1) {
                renderListingsTab(g, mouseX, mouseY);
            } else if (this.activeTab == 2) {
                renderMyListingsTab(g, mouseX, mouseY);
            } else if (this.activeTab == 3) {
                renderNotificationsTab(g, mouseX, mouseY);
            } else if (this.activeTab == 4) {
                renderTradeFloorTab(g, mouseX, mouseY);
            }
        }

        // Status message at bottom
        if (!this.statusMsg.isEmpty() && this.statusTimer > 0) {
            int msgW = this.font.width(this.statusMsg) + 20;
            int msgX = (this.width - msgW) / 2;
            int msgY = this.height - 24;
            UIHelper.drawCard(g, msgX, msgY, msgW, 18);
            boolean isError = this.statusMsg.contains("Not enough") || this.statusMsg.contains("Failed")
                    || this.statusMsg.contains("already") || this.statusMsg.contains("Cannot")
                    || this.statusMsg.contains("Invalid") || this.statusMsg.contains("Minimum")
                    || this.statusMsg.contains("Unknown") || this.statusMsg.contains("not found")
                    || this.statusMsg.contains("cannot") || this.statusMsg.contains("can only");
            int msgColor = isError ? ERROR_RED : SUCCESS_GREEN;
            g.drawCenteredString(this.font, this.statusMsg, this.width / 2, msgY + 5, msgColor);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    // --- Tab bar ---

    private void renderTabBar(GuiGraphics g, int mouseX, int mouseY) {
        int tabH = 16;
        int tabY = this.contentTop - 2;

        String[] tabNames = {"Buy", "Sell", "My Listings", "Inbox", "Trade Floor"};
        int[] tabColors = {WTS_GREEN, WTB_BLUE, MARKET_TEAL, GOLD_COIN, FLOOR_ORANGE};
        int[] tabWidths = {60, 60, 80, 60, 80};

        for (int t = 0; t < 5; t++) {
            int tw = tabWidths[t];
            int tabX = 8;
            for (int j = 0; j < t; j++) {
                tabX += tabWidths[j] + 4;
            }

            boolean hover = mouseX >= tabX && mouseX < tabX + tw && mouseY >= tabY && mouseY < tabY + tabH;
            int bg = this.activeTab == t ? TAB_ACTIVE : TAB_INACTIVE;
            g.fill(tabX, tabY, tabX + tw, tabY + tabH, bg);
            if (hover && this.activeTab != t) {
                g.fill(tabX, tabY, tabX + tw, tabY + tabH, 0x18FFFFFF);
            }
            if (this.activeTab == t) {
                g.fill(tabX, tabY + tabH - 2, tabX + tw, tabY + tabH, tabColors[t]);
            }

            String label = tabNames[t];
            // Add unread badge to Inbox
            if (t == 3 && this.unreadCount > 0) {
                label = tabNames[t] + " (" + this.unreadCount + ")";
            }

            g.drawCenteredString(this.font, label, tabX + tw / 2, tabY + (tabH - 9) / 2,
                    this.activeTab == t ? tabColors[t] : OFFLINE_GREY);
            this.clickRects.add(new ClickRect(tabX, tabY, tw, tabH, "tab:" + t));
        }
    }

    // --- Listings tab (Buy = WTS listings, Sell = WTB listings) ---

    private void renderListingsTab(GuiGraphics g, int mouseX, int mouseY) {
        int panelTop = this.contentTop + 18;
        int panelBottom = this.height - 30;
        int panelX = 4;
        int panelW = this.width - 8;

        UIHelper.drawInsetPanel(g, panelX, panelTop, panelW, panelBottom - panelTop);

        // Search bar
        int searchBarY = panelTop + 4;
        int searchBarX = panelX + 8;
        int searchBarW = panelW / 2 - 16;
        int searchBarH = 14;

        g.drawString(this.font, "Search:", searchBarX, searchBarY + 3, UIHelper.GOLD_MID, false);
        int inputX = searchBarX + this.font.width("Search: ") + 4;
        int inputW = searchBarW - this.font.width("Search: ") - 4;

        int searchBorder = this.searchFocused ? MARKET_TEAL : OFFLINE_GREY;
        g.fill(inputX - 1, searchBarY - 1, inputX + inputW + 1, searchBarY + searchBarH + 1, searchBorder);
        g.fill(inputX, searchBarY, inputX + inputW, searchBarY + searchBarH, 0xFF0D1117);

        if (this.searchQuery.isEmpty() && !this.searchFocused) {
            g.drawString(this.font, "Item name...", inputX + 3, searchBarY + 3, OFFLINE_GREY, false);
        } else {
            String clipped = this.searchQuery;
            int maxTextW = inputW - 8;
            while (this.font.width(clipped) > maxTextW && clipped.length() > 0) {
                clipped = clipped.substring(1);
            }
            g.drawString(this.font, clipped, inputX + 3, searchBarY + 3, 0xFFE6EDF3, false);
            if (this.searchFocused && (this.cursorBlink / 10) % 2 == 0) {
                int cursorX = inputX + 3 + this.font.width(clipped);
                g.fill(cursorX, searchBarY + 2, cursorX + 1, searchBarY + searchBarH - 2, MARKET_TEAL);
            }
        }
        this.clickRects.add(new ClickRect(inputX, searchBarY, inputW, searchBarH, "focus_search"));

        // Post button
        int postBtnW = 100;
        int postBtnH = 14;
        int postBtnX = panelX + panelW - postBtnW - 10;
        int postBtnY = searchBarY;
        boolean postHover = mouseX >= postBtnX && mouseX < postBtnX + postBtnW
                && mouseY >= postBtnY && mouseY < postBtnY + postBtnH;
        UIHelper.drawButton(g, postBtnX, postBtnY, postBtnW, postBtnH, postHover);
        int postLabelX = postBtnX + (postBtnW - this.font.width("+ Post Listing")) / 2;
        g.drawString(this.font, "+ Post Listing", postLabelX, postBtnY + (postBtnH - 9) / 2, MARKET_TEAL, false);
        this.clickRects.add(new ClickRect(postBtnX, postBtnY, postBtnW, postBtnH, "compose"));

        // Column headers
        int headerY = searchBarY + searchBarH + 8;
        int colItem = panelX + 8;
        int colQty = panelX + (int) (panelW * 0.30);
        int colPrice = panelX + (int) (panelW * 0.40);
        int colTotal = panelX + (int) (panelW * 0.52);
        int colSeller = panelX + (int) (panelW * 0.64);
        int colTime = panelX + (int) (panelW * 0.78);
        int colAction = panelX + panelW - 60;

        g.drawString(this.font, "Item", colItem, headerY, UIHelper.GOLD_MID, false);
        g.drawString(this.font, "Qty", colQty, headerY, UIHelper.GOLD_MID, false);
        g.drawString(this.font, "Unit", colPrice, headerY, UIHelper.GOLD_MID, false);
        g.drawString(this.font, "Total", colTotal, headerY, UIHelper.GOLD_MID, false);
        g.drawString(this.font, "Player", colSeller, headerY, UIHelper.GOLD_MID, false);
        g.drawString(this.font, "Expires", colTime, headerY, UIHelper.GOLD_MID, false);

        int divY = headerY + 11;
        UIHelper.drawHorizontalDivider(g, panelX + 6, divY, panelW - 12);

        // Filter listings by type
        String filterType = this.activeTab == 0 ? "WTS" : "WTB";
        List<ListingEntry> filtered = new ArrayList<>();
        for (ListingEntry l : this.allListings) {
            if (l.type.equals(filterType)) {
                filtered.add(l);
            }
        }

        // List area
        int listTop = divY + 4;
        int listBottom = panelBottom - 6;
        int listW = panelW - 12;

        g.enableScissor(panelX + 4, listTop, panelX + panelW - 4, listBottom);

        int visibleH = listBottom - listTop;
        int totalH = filtered.size() * ROW_HEIGHT;
        int maxScroll = Math.max(0, totalH - visibleH);
        this.scroll = Math.max(0, Math.min(this.scroll, maxScroll));

        if (filtered.isEmpty()) {
            String emptyMsg = this.activeTab == 0 ? "No items for sale right now." : "No buy requests right now.";
            g.drawString(this.font, emptyMsg, panelX + 20, listTop + 10, OFFLINE_GREY, false);
        }

        for (int i = 0; i < filtered.size(); i++) {
            int rowY = listTop + i * ROW_HEIGHT - this.scroll;
            if (rowY + ROW_HEIGHT < listTop || rowY > listBottom) continue;

            ListingEntry l = filtered.get(i);
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
            String displayItem = l.itemName;
            int maxItemW = colQty - colItem - 4;
            while (this.font.width(displayItem) > maxItemW && displayItem.length() > 1) {
                displayItem = displayItem.substring(0, displayItem.length() - 1);
            }
            int itemColor = l.type.equals("WTS") ? WTS_GREEN : WTB_BLUE;
            g.drawString(this.font, displayItem, colItem, textY, 0xFFE6EDF3, false);

            // Quantity
            g.drawString(this.font, String.valueOf(l.quantity), colQty, textY, 0xFFE6EDF3, false);

            // Price per unit
            g.drawString(this.font, l.pricePerUnit + " MC", colPrice, textY, GOLD_COIN, false);

            // Total price
            g.drawString(this.font, l.totalPrice + " MC", colTotal, textY, GOLD_COIN, false);

            // Seller + online dot
            int dotX = colSeller;
            int dotY = rowY + (ROW_HEIGHT - 4) / 2;
            int dotColor = l.online ? ONLINE_GREEN : OFFLINE_GREY;
            g.fill(dotX, dotY, dotX + 4, dotY + 4, dotColor);

            String sellerDisp = l.sellerName;
            int maxSellerW = colTime - colSeller - 10;
            while (this.font.width(sellerDisp) > maxSellerW && sellerDisp.length() > 1) {
                sellerDisp = sellerDisp.substring(0, sellerDisp.length() - 1);
            }
            g.drawString(this.font, sellerDisp, colSeller + 8, textY, l.isOwn ? MARKET_TEAL : 0xFFCCCCDD, false);

            // Time remaining
            int maxTimeW = colAction - colTime - 4;
            String timeDisp = l.timeLeft;
            while (this.font.width(timeDisp) > maxTimeW && timeDisp.length() > 1) {
                timeDisp = timeDisp.substring(0, timeDisp.length() - 1);
            }
            g.drawString(this.font, timeDisp, colTime, textY, OFFLINE_GREY, false);

            // Invite button (not for own listings)
            if (!l.isOwn) {
                int btnW = 50;
                int btnH = ROW_HEIGHT - 8;
                int btnX = panelX + panelW - btnW - 10;
                int btnY = rowY + 4;
                boolean btnHover = mouseX >= btnX && mouseX < btnX + btnW
                        && mouseY >= btnY && mouseY < btnY + btnH
                        && mouseY >= listTop && mouseY < listBottom;
                UIHelper.drawButton(g, btnX, btnY, btnW, btnH, btnHover);
                String btnLabel = l.online ? "Invite" : "Offline";
                int btnColor = l.online ? FLOOR_ORANGE : OFFLINE_GREY;
                int labelX = btnX + (btnW - this.font.width(btnLabel)) / 2;
                g.drawString(this.font, btnLabel, labelX, btnY + (btnH - 9) / 2, btnColor, false);
                if (l.online) {
                    this.clickRects.add(new ClickRect(btnX, btnY, btnW, btnH, "contact:" + l.id));
                }
            } else {
                g.drawString(this.font, "(you)", panelX + panelW - 42, textY, OFFLINE_GREY, false);
            }
        }

        g.disableScissor();

        // Scrollbar
        if (totalH > visibleH) {
            float progress = maxScroll > 0 ? (float) this.scroll / (float) maxScroll : 0.0f;
            UIHelper.drawScrollbar(g, panelX + panelW - 6, listTop, listBottom - listTop, progress);
        }
    }

    // --- My Listings tab ---

    private void renderMyListingsTab(GuiGraphics g, int mouseX, int mouseY) {
        int panelTop = this.contentTop + 18;
        int panelBottom = this.height - 30;
        int panelX = 4;
        int panelW = this.width - 8;

        UIHelper.drawInsetPanel(g, panelX, panelTop, panelW, panelBottom - panelTop);

        // Post button
        int postBtnW = 100;
        int postBtnH = 16;
        int postBtnX = panelX + panelW - postBtnW - 10;
        int postBtnY = panelTop + 4;
        boolean postHover = mouseX >= postBtnX && mouseX < postBtnX + postBtnW
                && mouseY >= postBtnY && mouseY < postBtnY + postBtnH;
        UIHelper.drawButton(g, postBtnX, postBtnY, postBtnW, postBtnH, postHover);
        int postLabelX = postBtnX + (postBtnW - this.font.width("+ Post Listing")) / 2;
        g.drawString(this.font, "+ Post Listing", postLabelX, postBtnY + (postBtnH - 9) / 2, MARKET_TEAL, false);
        this.clickRects.add(new ClickRect(postBtnX, postBtnY, postBtnW, postBtnH, "compose"));

        // Active count
        g.drawString(this.font, "Active: " + this.myListings.size() + "/10",
                panelX + 10, panelTop + 8, UIHelper.GOLD_MID, false);

        // Column headers
        int headerY = panelTop + 24;
        int colType = panelX + 8;
        int colItem = panelX + 50;
        int colQty = panelX + (int) (panelW * 0.35);
        int colPrice = panelX + (int) (panelW * 0.48);
        int colTime = panelX + (int) (panelW * 0.62);
        int colAction = panelX + panelW - 60;

        g.drawString(this.font, "Type", colType, headerY, UIHelper.GOLD_MID, false);
        g.drawString(this.font, "Item", colItem, headerY, UIHelper.GOLD_MID, false);
        g.drawString(this.font, "Qty", colQty, headerY, UIHelper.GOLD_MID, false);
        g.drawString(this.font, "Price", colPrice, headerY, UIHelper.GOLD_MID, false);
        g.drawString(this.font, "Expires", colTime, headerY, UIHelper.GOLD_MID, false);

        int divY = headerY + 11;
        UIHelper.drawHorizontalDivider(g, panelX + 6, divY, panelW - 12);

        int listTop = divY + 4;
        int listBottom = panelBottom - 6;

        g.enableScissor(panelX + 4, listTop, panelX + panelW - 4, listBottom);

        int visibleH = listBottom - listTop;
        int totalH = this.myListings.size() * ROW_HEIGHT;
        int maxScroll = Math.max(0, totalH - visibleH);
        this.scroll = Math.max(0, Math.min(this.scroll, maxScroll));

        if (this.myListings.isEmpty()) {
            g.drawString(this.font, "You have no active listings.", panelX + 20, listTop + 10, OFFLINE_GREY, false);
        }

        for (int i = 0; i < this.myListings.size(); i++) {
            int rowY = listTop + i * ROW_HEIGHT - this.scroll;
            if (rowY + ROW_HEIGHT < listTop || rowY > listBottom) continue;

            ListingEntry l = this.myListings.get(i);
            boolean rowHover = mouseX >= panelX + 4 && mouseX < panelX + panelW - 4
                    && mouseY >= rowY && mouseY < rowY + ROW_HEIGHT
                    && mouseY >= listTop && mouseY < listBottom;

            int rowBg = i % 2 == 0 ? ROW_EVEN : ROW_ODD;
            g.fill(panelX + 4, rowY, panelX + panelW - 4, rowY + ROW_HEIGHT, rowBg);
            if (rowHover) {
                g.fill(panelX + 4, rowY, panelX + panelW - 4, rowY + ROW_HEIGHT, 0x18FFFFFF);
            }

            int textY = rowY + (ROW_HEIGHT - 9) / 2;

            // Type badge
            int typeColor = l.type.equals("WTS") ? WTS_GREEN : WTB_BLUE;
            String typeLabel = l.type.equals("WTS") ? "SELL" : "BUY";
            g.drawString(this.font, typeLabel, colType, textY, typeColor, false);

            // Item
            String displayItem = l.itemName;
            int maxItemW = colQty - colItem - 4;
            while (this.font.width(displayItem) > maxItemW && displayItem.length() > 1) {
                displayItem = displayItem.substring(0, displayItem.length() - 1);
            }
            g.drawString(this.font, displayItem, colItem, textY, 0xFFE6EDF3, false);

            // Qty
            g.drawString(this.font, String.valueOf(l.quantity), colQty, textY, 0xFFE6EDF3, false);

            // Price
            g.drawString(this.font, l.pricePerUnit + "/ea (" + l.totalPrice + ")", colPrice, textY, GOLD_COIN, false);

            // Time remaining
            g.drawString(this.font, l.timeLeft, colTime, textY, OFFLINE_GREY, false);

            // Cancel button
            int btnW = 50;
            int btnH = ROW_HEIGHT - 8;
            int btnX = panelX + panelW - btnW - 10;
            int btnY = rowY + 4;
            boolean btnHover = mouseX >= btnX && mouseX < btnX + btnW
                    && mouseY >= btnY && mouseY < btnY + btnH
                    && mouseY >= listTop && mouseY < listBottom;
            UIHelper.drawButton(g, btnX, btnY, btnW, btnH, btnHover);
            int labelX = btnX + (btnW - this.font.width("Cancel")) / 2;
            g.drawString(this.font, "Cancel", labelX, btnY + (btnH - 9) / 2, ERROR_RED, false);
            this.clickRects.add(new ClickRect(btnX, btnY, btnW, btnH, "cancel:" + l.id));
        }

        g.disableScissor();

        if (totalH > visibleH) {
            float progress = maxScroll > 0 ? (float) this.scroll / (float) maxScroll : 0.0f;
            UIHelper.drawScrollbar(g, panelX + panelW - 6, listTop, listBottom - listTop, progress);
        }
    }

    // --- Notifications tab ---

    private void renderNotificationsTab(GuiGraphics g, int mouseX, int mouseY) {
        int panelTop = this.contentTop + 18;
        int panelBottom = this.height - 30;
        int panelX = 4;
        int panelW = this.width - 8;

        UIHelper.drawInsetPanel(g, panelX, panelTop, panelW, panelBottom - panelTop);

        // Clear button
        if (!this.notifications.isEmpty()) {
            int clearBtnW = 100;
            int clearBtnH = 14;
            int clearBtnX = panelX + panelW - clearBtnW - 10;
            int clearBtnY = panelTop + 4;
            boolean clearHover = mouseX >= clearBtnX && mouseX < clearBtnX + clearBtnW
                    && mouseY >= clearBtnY && mouseY < clearBtnY + clearBtnH;
            UIHelper.drawButton(g, clearBtnX, clearBtnY, clearBtnW, clearBtnH, clearHover);
            g.drawString(this.font, "Mark Read",
                    clearBtnX + (clearBtnW - this.font.width("Mark Read")) / 2,
                    clearBtnY + (clearBtnH - 9) / 2, GOLD_COIN, false);
            this.clickRects.add(new ClickRect(clearBtnX, clearBtnY, clearBtnW, clearBtnH, "clear_notifs"));
        }

        g.drawString(this.font, "Notifications (" + this.notifications.size() + ")",
                panelX + 10, panelTop + 8, UIHelper.GOLD_MID, false);

        int listTop = panelTop + 24;
        int listBottom = panelBottom - 6;
        int notifHeight = 30;

        g.enableScissor(panelX + 4, listTop, panelX + panelW - 4, listBottom);

        int visibleH = listBottom - listTop;
        int totalH = this.notifications.size() * notifHeight;
        int maxScroll = Math.max(0, totalH - visibleH);
        this.scroll = Math.max(0, Math.min(this.scroll, maxScroll));

        if (this.notifications.isEmpty()) {
            g.drawString(this.font, "No notifications yet.", panelX + 20, listTop + 10, OFFLINE_GREY, false);
        }

        for (int i = 0; i < this.notifications.size(); i++) {
            int rowY = listTop + i * notifHeight - this.scroll;
            if (rowY + notifHeight < listTop || rowY > listBottom) continue;

            NotifEntry n = this.notifications.get(i);

            int rowBg = n.read ? ROW_EVEN : 0xFF1A2233;
            g.fill(panelX + 4, rowY, panelX + panelW - 4, rowY + notifHeight, rowBg);

            // Unread indicator
            if (!n.read) {
                g.fill(panelX + 6, rowY + 4, panelX + 10, rowY + 8, NOTIF_RED);
            }

            // Message
            String msg = n.message;
            int maxMsgW = panelW - 80;
            while (this.font.width(msg) > maxMsgW && msg.length() > 1) {
                msg = msg.substring(0, msg.length() - 1);
            }
            g.drawString(this.font, msg, panelX + 16, rowY + 4, 0xFFE6EDF3, false);

            // Time ago
            g.drawString(this.font, n.timeAgo, panelX + 16, rowY + 16, OFFLINE_GREY, false);

            // From name highlight
            int fromLabelX = panelX + panelW - this.font.width(n.fromName) - 12;
            g.drawString(this.font, n.fromName, fromLabelX, rowY + 4, MARKET_TEAL, false);
        }

        g.disableScissor();

        if (totalH > visibleH) {
            float progress = maxScroll > 0 ? (float) this.scroll / (float) maxScroll : 0.0f;
            UIHelper.drawScrollbar(g, panelX + panelW - 6, listTop, listBottom - listTop, progress);
        }
    }

    // --- Trade Floor tab ---

    private void renderTradeFloorTab(GuiGraphics g, int mouseX, int mouseY) {
        int panelTop = this.contentTop + 18;
        int panelBottom = this.height - 30;
        int panelX = 4;
        int panelW = this.width - 8;

        UIHelper.drawInsetPanel(g, panelX, panelTop, panelW, panelBottom - panelTop);

        int centerX = panelX + panelW / 2;
        int cy = panelTop + 20;

        g.drawCenteredString(this.font, "Trading Floor", centerX, cy, FLOOR_ORANGE);
        cy += 16;

        g.drawCenteredString(this.font, "A shared marketplace dimension where players", centerX, cy, 0xFFCCCCDD);
        cy += 12;
        g.drawCenteredString(this.font, "can meet face-to-face to exchange goods.", centerX, cy, 0xFFCCCCDD);
        cy += 20;

        g.drawCenteredString(this.font, "Browse stalls, meet other traders, and trade", centerX, cy, OFFLINE_GREY);
        cy += 12;
        g.drawCenteredString(this.font, "in person. Walk into the portal to return home.", centerX, cy, OFFLINE_GREY);
        cy += 30;

        // Enter button
        int btnW = 140;
        int btnH = 20;
        int btnX = centerX - btnW / 2;
        int btnY = cy;
        boolean btnHover = mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY && mouseY < btnY + btnH;
        UIHelper.drawButton(g, btnX, btnY, btnW, btnH, btnHover);
        g.drawCenteredString(this.font, "Enter Trade Floor", btnX + btnW / 2, btnY + (btnH - 9) / 2, FLOOR_ORANGE);
        this.clickRects.add(new ClickRect(btnX, btnY, btnW, btnH, "enter_floor"));
    }

    // --- Invite banner ---

    private void renderInviteBanner(GuiGraphics g, int mouseX, int mouseY) {
        int bannerH = 28;
        int bannerX = 4;
        int bannerW = this.width - 8;
        int bannerY = this.height - 56;

        // Background
        g.fill(bannerX, bannerY, bannerX + bannerW, bannerY + bannerH, 0xFF1A2A1A);
        g.fill(bannerX, bannerY, bannerX + bannerW, bannerY + 2, FLOOR_ORANGE);

        // Invite text
        String action = this.inviteListingType.equals("WTS") ? "buy" : "sell";
        String inviteText = this.inviterName + " invites you to the Trade Floor";
        if (!this.inviteItemName.isEmpty()) {
            inviteText += " (" + action + " " + this.inviteQuantity + "x " + this.inviteItemName + ")";
        }
        int maxTextW = bannerW - 170;
        while (this.font.width(inviteText) > maxTextW && inviteText.length() > 1) {
            inviteText = inviteText.substring(0, inviteText.length() - 1);
        }
        g.drawString(this.font, inviteText, bannerX + 8, bannerY + 5, FLOOR_ORANGE, false);

        // Timer
        String timer = this.inviteRemainingSec + "s";
        g.drawString(this.font, timer, bannerX + 8, bannerY + 16, OFFLINE_GREY, false);

        // Accept button
        int acceptW = 60;
        int acceptH = 16;
        int acceptX = bannerX + bannerW - acceptW - 70;
        int acceptY = bannerY + (bannerH - acceptH) / 2;
        boolean acceptHover = mouseX >= acceptX && mouseX < acceptX + acceptW
                && mouseY >= acceptY && mouseY < acceptY + acceptH;
        UIHelper.drawButton(g, acceptX, acceptY, acceptW, acceptH, acceptHover);
        g.drawCenteredString(this.font, "Accept", acceptX + acceptW / 2, acceptY + (acceptH - 9) / 2, SUCCESS_GREEN);
        this.clickRects.add(new ClickRect(acceptX, acceptY, acceptW, acceptH, "accept_invite"));

        // Decline button
        int declineW = 60;
        int declineX = bannerX + bannerW - declineW - 6;
        int declineY = acceptY;
        boolean declineHover = mouseX >= declineX && mouseX < declineX + declineW
                && mouseY >= declineY && mouseY < declineY + acceptH;
        UIHelper.drawButton(g, declineX, declineY, declineW, acceptH, declineHover);
        g.drawCenteredString(this.font, "Decline", declineX + declineW / 2, declineY + (acceptH - 9) / 2, ERROR_RED);
        this.clickRects.add(new ClickRect(declineX, declineY, declineW, acceptH, "decline_invite"));
    }

    // --- Compose modal ---

    private void renderComposeModal(GuiGraphics g, int mouseX, int mouseY) {
        int panelW = Math.min(360, this.width - 20);
        int panelH = 170;
        int panelX = (this.width - panelW) / 2;
        int panelY = this.height / 2 - panelH / 2 - 10;

        UIHelper.drawPanel(g, panelX, panelY, panelW, panelH);

        // Title
        g.drawString(this.font, "Post New Listing", panelX + 10, panelY + 8, MARKET_TEAL, false);

        // Type toggle
        int typeY = panelY + 24;
        int wtsBtnW = 60;
        int wtsBtnH = 14;
        int wtsBtnX = panelX + 10;
        boolean wtsHover = mouseX >= wtsBtnX && mouseX < wtsBtnX + wtsBtnW
                && mouseY >= typeY && mouseY < typeY + wtsBtnH;
        int wtsBg = this.composeType.equals("WTS") ? 0xFF1A3A1A : TAB_INACTIVE;
        g.fill(wtsBtnX, typeY, wtsBtnX + wtsBtnW, typeY + wtsBtnH, wtsBg);
        if (wtsHover) g.fill(wtsBtnX, typeY, wtsBtnX + wtsBtnW, typeY + wtsBtnH, 0x18FFFFFF);
        if (this.composeType.equals("WTS")) {
            g.fill(wtsBtnX, typeY + wtsBtnH - 2, wtsBtnX + wtsBtnW, typeY + wtsBtnH, WTS_GREEN);
        }
        g.drawCenteredString(this.font, "Selling", wtsBtnX + wtsBtnW / 2, typeY + 3,
                this.composeType.equals("WTS") ? WTS_GREEN : OFFLINE_GREY);
        this.clickRects.add(new ClickRect(wtsBtnX, typeY, wtsBtnW, wtsBtnH, "type_wts"));

        int wtbBtnX = wtsBtnX + wtsBtnW + 4;
        boolean wtbHover = mouseX >= wtbBtnX && mouseX < wtbBtnX + wtsBtnW
                && mouseY >= typeY && mouseY < typeY + wtsBtnH;
        int wtbBg = this.composeType.equals("WTB") ? 0xFF1A2A3A : TAB_INACTIVE;
        g.fill(wtbBtnX, typeY, wtbBtnX + wtsBtnW, typeY + wtsBtnH, wtbBg);
        if (wtbHover) g.fill(wtbBtnX, typeY, wtbBtnX + wtsBtnW, typeY + wtsBtnH, 0x18FFFFFF);
        if (this.composeType.equals("WTB")) {
            g.fill(wtbBtnX, typeY + wtsBtnH - 2, wtbBtnX + wtsBtnW, typeY + wtsBtnH, WTB_BLUE);
        }
        g.drawCenteredString(this.font, "Buying", wtbBtnX + wtsBtnW / 2, typeY + 3,
                this.composeType.equals("WTB") ? WTB_BLUE : OFFLINE_GREY);
        this.clickRects.add(new ClickRect(wtbBtnX, typeY, wtsBtnW, wtsBtnH, "type_wtb"));

        // Type description
        String typeDesc = this.composeType.equals("WTS")
                ? "You want to sell this item"
                : "You want to buy this item (coins from bank)";
        g.drawString(this.font, typeDesc, panelX + 140, typeY + 3, OFFLINE_GREY, false);

        // Item row
        int itemRowY = panelY + 46;
        g.drawString(this.font, "Item:", panelX + 10, itemRowY, UIHelper.GOLD_DARK, false);

        if (this.itemInput.isEmpty()) {
            int heldBtnW = 70;
            int heldBtnH = 14;
            int heldBtnX = panelX + 10 + this.font.width("Item: ") + 4;
            boolean heldHover = mouseX >= heldBtnX && mouseX < heldBtnX + heldBtnW
                    && mouseY >= itemRowY && mouseY < itemRowY + heldBtnH;
            UIHelper.drawButton(g, heldBtnX, itemRowY, heldBtnW, heldBtnH, heldHover);
            g.drawString(this.font, "Held Item",
                    heldBtnX + (heldBtnW - this.font.width("Held Item")) / 2,
                    itemRowY + 3, WTB_BLUE, false);
            this.clickRects.add(new ClickRect(heldBtnX, itemRowY, heldBtnW, heldBtnH, "select_held"));
        } else {
            g.drawString(this.font, this.itemDisplayName + " (" + this.itemInput + ")",
                    panelX + 10 + this.font.width("Item: ") + 4, itemRowY, 0xFFE6EDF3, false);

            // Clear button
            int clrBtnW = 14;
            int clrBtnH = 14;
            int clrBtnX = panelX + panelW - clrBtnW - 10;
            boolean clrHover = mouseX >= clrBtnX && mouseX < clrBtnX + clrBtnW
                    && mouseY >= itemRowY && mouseY < itemRowY + clrBtnH;
            g.fill(clrBtnX, itemRowY, clrBtnX + clrBtnW, itemRowY + clrBtnH,
                    clrHover ? 0xFF3A2020 : 0xFF2A1818);
            g.drawCenteredString(this.font, "X", clrBtnX + clrBtnW / 2, itemRowY + 3, ERROR_RED);
            this.clickRects.add(new ClickRect(clrBtnX, itemRowY, clrBtnW, clrBtnH, "clear_item"));
        }

        // Quantity row
        int qtyRowY = panelY + 68;
        g.drawString(this.font, "Quantity:", panelX + 10, qtyRowY + 3, UIHelper.GOLD_DARK, false);
        int qtyInputX = panelX + 10 + this.font.width("Quantity: ") + 4;
        int qtyInputW = 80;
        int qtyInputH = 14;

        int qtyBorder = this.quantityFocused ? MARKET_TEAL : OFFLINE_GREY;
        g.fill(qtyInputX - 1, qtyRowY - 1, qtyInputX + qtyInputW + 1, qtyRowY + qtyInputH + 1, qtyBorder);
        g.fill(qtyInputX, qtyRowY, qtyInputX + qtyInputW, qtyRowY + qtyInputH, 0xFF0D1117);

        if (this.quantityInput.isEmpty() && !this.quantityFocused) {
            g.drawString(this.font, "1", qtyInputX + 3, qtyRowY + 3, OFFLINE_GREY, false);
        } else {
            g.drawString(this.font, this.quantityInput, qtyInputX + 3, qtyRowY + 3, 0xFFE6EDF3, false);
            if (this.quantityFocused && (this.cursorBlink / 10) % 2 == 0) {
                int cursorX = qtyInputX + 3 + this.font.width(this.quantityInput);
                g.fill(cursorX, qtyRowY + 2, cursorX + 1, qtyRowY + qtyInputH - 2, MARKET_TEAL);
            }
        }
        this.clickRects.add(new ClickRect(qtyInputX, qtyRowY, qtyInputW, qtyInputH, "focus_qty"));

        // Price row
        int priceRowY = panelY + 90;
        g.drawString(this.font, "Price/unit:", panelX + 10, priceRowY + 3, UIHelper.GOLD_DARK, false);
        int priceInputX = panelX + 10 + this.font.width("Price/unit: ") + 4;
        int priceInputW = 80;
        int priceInputH = 14;

        int priceBorder = this.priceFocused ? MARKET_TEAL : OFFLINE_GREY;
        g.fill(priceInputX - 1, priceRowY - 1, priceInputX + priceInputW + 1, priceRowY + priceInputH + 1, priceBorder);
        g.fill(priceInputX, priceRowY, priceInputX + priceInputW, priceRowY + priceInputH, 0xFF0D1117);

        if (this.priceInput.isEmpty() && !this.priceFocused) {
            g.drawString(this.font, "0", priceInputX + 3, priceRowY + 3, OFFLINE_GREY, false);
        } else {
            g.drawString(this.font, this.priceInput, priceInputX + 3, priceRowY + 3, 0xFFE6EDF3, false);
            if (this.priceFocused && (this.cursorBlink / 10) % 2 == 0) {
                int cursorX = priceInputX + 3 + this.font.width(this.priceInput);
                g.fill(cursorX, priceRowY + 2, cursorX + 1, priceRowY + priceInputH - 2, MARKET_TEAL);
            }
        }
        this.clickRects.add(new ClickRect(priceInputX, priceRowY, priceInputW, priceInputH, "focus_price"));
        g.drawString(this.font, "MC each", priceInputX + priceInputW + 4, priceRowY + 3, GOLD_COIN, false);

        // Total preview
        int previewY = panelY + 112;
        int qty = 0;
        int price = 0;
        try { qty = Integer.parseInt(this.quantityInput); } catch (NumberFormatException ignored) {}
        try { price = Integer.parseInt(this.priceInput); } catch (NumberFormatException ignored) {}
        int total = qty * price;
        String totalLabel = "Total: " + total + " MC";
        if (this.composeType.equals("WTB")) {
            totalLabel += " (from bank)";
        }
        g.drawString(this.font, totalLabel, panelX + 10, previewY, GOLD_COIN, false);

        // Submit button
        int submitY = panelY + panelH - 28;
        int submitW = 80;
        int submitH = 16;
        int submitX = panelX + panelW / 2 - submitW - 4;
        boolean submitHover = mouseX >= submitX && mouseX < submitX + submitW
                && mouseY >= submitY && mouseY < submitY + submitH;
        UIHelper.drawButton(g, submitX, submitY, submitW, submitH, submitHover);
        g.drawCenteredString(this.font, "Post",
                submitX + submitW / 2, submitY + (submitH - 9) / 2, SUCCESS_GREEN);
        this.clickRects.add(new ClickRect(submitX, submitY, submitW, submitH, "submit_post"));

        // Cancel button
        int cancelW = 80;
        int cancelX = panelX + panelW / 2 + 4;
        boolean cancelHover = mouseX >= cancelX && mouseX < cancelX + cancelW
                && mouseY >= submitY && mouseY < submitY + submitH;
        UIHelper.drawButton(g, cancelX, submitY, cancelW, submitH, cancelHover);
        g.drawCenteredString(this.font, "Cancel",
                cancelX + cancelW / 2, submitY + (submitH - 9) / 2, ERROR_RED);
        this.clickRects.add(new ClickRect(cancelX, submitY, cancelW, submitH, "cancel_compose"));
    }

    // --- Mouse / key handling ---

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean consumed) {
        if (consumed) {
            return super.mouseClicked(event, consumed);
        }
        int mx = (int) event.x();
        int my = (int) event.y();

        boolean clickedQty = false;
        boolean clickedPrice = false;
        boolean clickedSearch = false;

        for (ClickRect r : this.clickRects) {
            if (mx >= r.x && mx < r.x + r.w && my >= r.y && my < r.y + r.h) {
                if (r.action.equals("focus_qty")) {
                    clickedQty = true;
                } else if (r.action.equals("focus_price")) {
                    clickedPrice = true;
                } else if (r.action.equals("focus_search")) {
                    clickedSearch = true;
                } else {
                    handleClick(r.action);
                    return true;
                }
            }
        }

        this.quantityFocused = clickedQty;
        this.priceFocused = clickedPrice;
        this.searchFocused = clickedSearch;
        if (clickedQty || clickedPrice || clickedSearch) {
            this.cursorBlink = 0;
            return true;
        }

        this.quantityFocused = false;
        this.priceFocused = false;
        this.searchFocused = false;
        return super.mouseClicked(event, consumed);
    }

    private void handleClick(String action) {
        if (action.equals("back")) {
            if (this.composing) {
                this.composing = false;
                resetComposeState();
            } else if (this.minecraft != null) {
                this.minecraft.setScreen(this.parent);
            }
            return;
        }

        if (action.startsWith("tab:")) {
            int tab = Integer.parseInt(action.substring(4));
            this.activeTab = tab;
            this.scroll = 0;
            this.searchFocused = false;
            return;
        }

        if (action.equals("compose")) {
            this.composing = true;
            resetComposeState();
            return;
        }

        if (action.equals("cancel_compose")) {
            this.composing = false;
            resetComposeState();
            return;
        }

        if (action.equals("type_wts")) {
            this.composeType = "WTS";
            return;
        }

        if (action.equals("type_wtb")) {
            this.composeType = "WTB";
            return;
        }

        if (action.equals("select_held")) {
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

        if (action.equals("clear_item")) {
            this.itemInput = "";
            this.itemDisplayName = "";
            return;
        }

        if (action.equals("submit_post")) {
            submitListing();
            return;
        }

        if (action.startsWith("contact:")) {
            String idStr = action.substring(8);
            ClientPacketDistributor.sendToServer(
                    (CustomPacketPayload) new ComputerActionPayload("market_contact", idStr),
                    (CustomPacketPayload[]) new CustomPacketPayload[0]
            );
            return;
        }

        if (action.startsWith("cancel:")) {
            String idStr = action.substring(7);
            ClientPacketDistributor.sendToServer(
                    (CustomPacketPayload) new ComputerActionPayload("market_cancel", idStr),
                    (CustomPacketPayload[]) new CustomPacketPayload[0]
            );
            return;
        }

        if (action.equals("clear_notifs")) {
            ClientPacketDistributor.sendToServer(
                    (CustomPacketPayload) new ComputerActionPayload("market_clear_notifs", ""),
                    (CustomPacketPayload[]) new CustomPacketPayload[0]
            );
            return;
        }

        if (action.equals("enter_floor")) {
            ClientPacketDistributor.sendToServer(
                    (CustomPacketPayload) new ComputerActionPayload("market_enter_floor", ""),
                    (CustomPacketPayload[]) new CustomPacketPayload[0]
            );
            return;
        }

        if (action.equals("accept_invite")) {
            ClientPacketDistributor.sendToServer(
                    (CustomPacketPayload) new ComputerActionPayload("market_accept_invite", ""),
                    (CustomPacketPayload[]) new CustomPacketPayload[0]
            );
            return;
        }

        if (action.equals("decline_invite")) {
            ClientPacketDistributor.sendToServer(
                    (CustomPacketPayload) new ComputerActionPayload("market_decline_invite", ""),
                    (CustomPacketPayload[]) new CustomPacketPayload[0]
            );
            return;
        }
    }

    private void submitListing() {
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

        // Send: "type:itemId:quantity:pricePerUnit"
        String data = this.composeType + ":" + this.itemInput + ":" + qty + ":" + price;
        ClientPacketDistributor.sendToServer(
                (CustomPacketPayload) new ComputerActionPayload("market_post", data),
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
        this.composeType = "WTS";
    }

    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();

        // Escape
        if (keyCode == 256) {
            if (this.composing) {
                this.composing = false;
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
            if (this.composing && (this.quantityFocused || this.priceFocused)) {
                submitListing();
                return true;
            }
            if (this.searchFocused && !this.searchQuery.isEmpty()) {
                ClientPacketDistributor.sendToServer(
                        (CustomPacketPayload) new ComputerActionPayload("market_search", this.searchQuery),
                        (CustomPacketPayload[]) new CustomPacketPayload[0]
                );
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
            if (this.searchFocused && !this.searchQuery.isEmpty()) {
                this.searchQuery = this.searchQuery.substring(0, this.searchQuery.length() - 1);
                this.cursorBlink = 0;
                // If search cleared, refresh full data
                if (this.searchQuery.isEmpty()) {
                    requestMarketData();
                }
                return true;
            }
        }

        return super.keyPressed(event);
    }

    public boolean charTyped(net.minecraft.client.input.CharacterEvent event) {
        char codePoint = (char) event.codepoint();
        int modifiers = event.modifiers();
        // Search accepts letters, numbers, spaces, underscores
        if (this.searchFocused) {
            if (this.searchQuery.length() < 30 && (Character.isLetterOrDigit(codePoint) || codePoint == ' ' || codePoint == '_')) {
                this.searchQuery += codePoint;
                this.cursorBlink = 0;
                return true;
            }
            return false;
        }

        // Only digits for quantity and price
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
        if (!this.composing) {
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

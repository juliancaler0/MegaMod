package com.ultra.megamod.feature.economy.screen;

import com.ultra.megamod.feature.computer.network.ComputerActionPayload;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.economy.shop.FurnitureShop;
import com.ultra.megamod.feature.economy.shop.ShopItem;
import com.ultra.megamod.feature.ui.UIHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class ShopScreen extends Screen {
    private final Screen parent;
    private int wallet;
    private int bank;
    private final List<ShopItem> items = new ArrayList<>();
    private final List<ShopItem> furnitureItems = new ArrayList<>();
    private boolean dataLoaded = false;
    private boolean furnitureLoaded = false;
    private String statusMessage = "";
    private int statusTicks = 0;
    private int titleBarH;
    private int listTop;
    private int listBottom;
    private int listLeft;
    private int listRight;
    private int scrollOffset = 0;
    private final List<ClickRect> clickRects = new ArrayList<>();

    private static final String[] TAB_NAMES = {"Daily Deals", "Furniture Store"};
    private int activeTab = 0;
    private int tabBarBottom;

    // Furniture store filtering
    private String furnitureSearch = "";
    private boolean searchFocused = false;
    private String selectedCategory = "All";
    private List<String> furnitureCategories = new ArrayList<>();
    private int categoryScroll = 0;

    // Scrollbar drag state
    private boolean draggingScrollbar = false;
    private double dragStartY = 0;
    private int dragStartScroll = 0;
    private int dragMaxScroll = 0;
    private int dragTrackH = 0;
    private int dragThumbH = 0;

    public ShopScreen(Screen parent, int wallet, int bank) {
        super(Component.literal("MegaShop"));
        this.parent = parent;
        this.wallet = wallet;
        this.bank = bank;
    }

    protected void init() {
        super.init();
        Objects.requireNonNull(this.font);
        this.titleBarH = 9 + 16;
        this.tabBarBottom = this.titleBarH + 18;
        this.listLeft = 16;
        this.listRight = this.width - 10 - 6;
        this.listTop = this.tabBarBottom + 6;
        Objects.requireNonNull(this.font);
        this.listBottom = this.height - 10 - 9 - 12;
        ClientPacketDistributor.sendToServer(new ComputerActionPayload("request_shop", ""));
        // Pre-load furniture categories
        furnitureCategories.clear();
        furnitureCategories.add("All");
        furnitureCategories.addAll(FurnitureShop.getCategories());
    }

    private int refreshTimer = 0;

    public void tick() {
        super.tick();
        if (this.statusTicks > 0) --this.statusTicks;
        ComputerDataPayload response = ComputerDataPayload.lastResponse;
        if (response != null) {
            if ("shop_data".equals(response.dataType())) {
                ComputerDataPayload.lastResponse = null;
                this.wallet = response.wallet();
                this.bank = response.bank();
                this.parseShopData(response.jsonData(), this.items);
                this.dataLoaded = true;
            } else if ("furniture_data".equals(response.dataType())) {
                ComputerDataPayload.lastResponse = null;
                this.wallet = response.wallet();
                this.bank = response.bank();
                this.parseShopData(response.jsonData(), this.furnitureItems);
                this.furnitureLoaded = true;
            } else if ("buy_result".equals(response.dataType())) {
                ComputerDataPayload.lastResponse = null;
                this.wallet = response.wallet();
                this.bank = response.bank();
                boolean success = response.jsonData().contains("true");
                this.statusMessage = success ? "Purchase successful!" : "Not enough coins!";
                this.statusTicks = 60;
                // Re-request shop data to update stock after purchase
                ClientPacketDistributor.sendToServer(new ComputerActionPayload("request_shop", ""));
            } else if ("sell_result".equals(response.dataType())) {
                ComputerDataPayload.lastResponse = null;
                this.wallet = response.wallet();
                this.bank = response.bank();
                boolean success = response.jsonData().contains("true");
                this.statusMessage = success ? "Item sold!" : "Item not in inventory!";
                this.statusTicks = 60;
                // Re-request shop data to update stock after sell
                ClientPacketDistributor.sendToServer(new ComputerActionPayload("request_shop", ""));
            } else if ("error".equals(response.dataType())) {
                ComputerDataPayload.lastResponse = null;
                this.dataLoaded = true;
            }
        }
        // Auto-retry if data hasn't loaded
        if (!this.dataLoaded) {
            this.refreshTimer++;
            if (this.refreshTimer >= 60) {
                this.refreshTimer = 0;
                ClientPacketDistributor.sendToServer(new ComputerActionPayload("request_shop", ""));
            }
        }
    }

    private void parseShopData(String json, List<ShopItem> target) {
        target.clear();
        if (json == null || json.length() < 3) return;
        String inner = json.substring(1, json.length() - 1);
        ArrayList<String> chunks = new ArrayList<>();
        int depth = 0;
        int start = 0;
        for (int i = 0; i < inner.length(); i++) {
            char c = inner.charAt(i);
            if (c == '{') { depth++; continue; }
            if (c == '}') { depth--; continue; }
            if (c == ',' && depth == 0) {
                chunks.add(inner.substring(start, i));
                start = i + 1;
            }
        }
        chunks.add(inner.substring(start));
        for (String chunk : chunks) {
            target.add(ShopItem.fromJson(chunk));
        }
    }

    /** Build filtered list of furniture indices matching current category + search. */
    private List<Integer> getFilteredFurnitureIndices() {
        List<Integer> result = new ArrayList<>();
        String searchLower = furnitureSearch.toLowerCase();
        for (int i = 0; i < furnitureItems.size(); i++) {
            if (!"All".equals(selectedCategory)) {
                String cat = FurnitureShop.getCategoryForIndex(i);
                if (!selectedCategory.equals(cat)) continue;
            }
            if (!searchLower.isEmpty()) {
                if (!furnitureItems.get(i).displayName().toLowerCase().contains(searchLower)) continue;
            }
            result.add(i);
        }
        return result;
    }

    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.clickRects.clear();
        g.fill(0, 0, this.width, this.height, 0xFF0E0E18);
        UIHelper.drawPanel(g, 0, 0, this.width, this.height);
        UIHelper.drawTitleBar(g, 0, 0, this.width, this.titleBarH);
        Objects.requireNonNull(this.font);
        int titleY = (this.titleBarH - 9) / 2;
        String titleText = activeTab == 0 ? "MegaShop - Daily Deals" : "MegaShop - Furniture Store";
        UIHelper.drawCenteredTitle(g, this.font, titleText, this.width / 2, titleY);

        // Back button
        int backW = 50;
        int backH = 16;
        int backX = 8;
        int backY = (this.titleBarH - backH) / 2;
        boolean backHover = mouseX >= backX && mouseX < backX + backW && mouseY >= backY && mouseY < backY + backH;
        UIHelper.drawButton(g, backX, backY, backW, backH, backHover);
        int backTextX = backX + (backW - this.font.width("< Back")) / 2;
        g.drawString(this.font, "< Back", backTextX, backY + (backH - 9) / 2, 0xFFCCCCDD, false);
        this.clickRects.add(new ClickRect(backX, backY, backW, backH, "back", -1));

        // Wallet display
        String walletStr = "Wallet: $" + this.wallet;
        int walletW = this.font.width(walletStr) + 12;
        int walletPanelX = this.width - walletW - 10;
        UIHelper.drawInsetPanel(g, walletPanelX, backY, walletW, backH);
        g.drawString(this.font, walletStr, walletPanelX + 6, backY + (backH - 9) / 2, 0xFF888899, false);

        // Tab bar
        renderTabs(g, mouseX, mouseY);

        if (activeTab == 0) {
            // Content area for daily deals
            UIHelper.drawInsetPanel(g, this.listLeft - 4, this.listTop - 4, this.listRight - this.listLeft + 8, this.listBottom - this.listTop + 8);
            renderDailyDeals(g, mouseX, mouseY);
        } else {
            renderFurnitureStoreWithSidebar(g, mouseX, mouseY);
        }

        // Status message
        if (this.statusTicks > 0 && !this.statusMessage.isEmpty()) {
            int msgColor = this.statusMessage.contains("successful") || this.statusMessage.contains("sold") ? 0xFF55BB55 : 0xFFCC5555;
            g.drawCenteredString(this.font, this.statusMessage, this.width / 2, this.height - 10 - 9, msgColor);
        }
        super.render(g, mouseX, mouseY, partialTick);
    }

    private void renderTabs(GuiGraphics g, int mouseX, int mouseY) {
        int tabW = 100;
        int tabH = 14;
        int tabY = this.titleBarH + 2;
        int totalTabsW = TAB_NAMES.length * (tabW + 4) - 4;
        int tabStartX = (this.width - totalTabsW) / 2;

        for (int i = 0; i < TAB_NAMES.length; i++) {
            int tx = tabStartX + i * (tabW + 4);
            boolean hover = mouseX >= tx && mouseX < tx + tabW && mouseY >= tabY && mouseY < tabY + tabH;
            boolean active = (i == this.activeTab);
            if (active) {
                g.fill(tx, tabY, tx + tabW, tabY + tabH, 0xFF2A2A44);
                g.fill(tx, tabY, tx + tabW, tabY + 1, 0xFFE8A838);
            } else {
                g.fill(tx, tabY, tx + tabW, tabY + tabH, hover ? 0xFF1E1E33 : 0xFF161628);
            }
            int textColor = active ? 0xFFE8A838 : (hover ? 0xFFAAAABB : 0xFF777788);
            int lw = this.font.width(TAB_NAMES[i]);
            g.drawString(this.font, TAB_NAMES[i], tx + (tabW - lw) / 2, tabY + 3, textColor, false);
            this.clickRects.add(new ClickRect(tx, tabY, tabW, tabH, "tab_" + i, i));
        }
    }

    private void renderDailyDeals(GuiGraphics g, int mouseX, int mouseY) {
        if (!this.dataLoaded) {
            UIHelper.drawCenteredLabel(g, this.font, "Loading shop data...", this.width / 2, this.height / 2);
            return;
        }
        if (this.items.isEmpty()) {
            UIHelper.drawCenteredLabel(g, this.font, "No items available today.", this.width / 2, this.height / 2);
            return;
        }

        // Collect featured items (last items with Relic: or Weapon: prefix)
        List<Integer> featuredIndices = new ArrayList<>();
        for (int i = this.items.size() - 1; i >= 0; i--) {
            String name = this.items.get(i).displayName();
            if (name.contains("Relic:") || name.contains("Weapon:")) {
                featuredIndices.add(0, i);
            } else {
                break;
            }
        }
        boolean hasFeatured = !featuredIndices.isEmpty();

        int featuredH = 0;
        if (hasFeatured) {
            // ── Daily Showcase banner ──
            int cardH = 24; // per-item row height
            featuredH = 18 + featuredIndices.size() * cardH + 6;
            int fx = this.listLeft;
            int fy = this.listTop;
            int fw = this.listRight - this.listLeft;

            // Gold border + dark background
            g.fill(fx, fy, fx + fw, fy + featuredH, 0xFF1A1628);
            g.fill(fx, fy, fx + fw, fy + 1, 0xFFE8A838);
            g.fill(fx, fy + featuredH - 1, fx + fw, fy + featuredH, 0xFFE8A838);
            g.fill(fx, fy, fx + 1, fy + featuredH, 0xFFE8A838);
            g.fill(fx + fw - 1, fy, fx + fw, fy + featuredH, 0xFFE8A838);

            // Header
            String header = "\u2605 Daily Showcase \u2605";
            int headerW = this.font.width(header);
            g.drawString(this.font, header, fx + (fw - headerW) / 2, fy + 4, 0xFFE8A838, false);

            String qualityLabel = "Common - Uncommon  |  Limit 1 per day";
            int qlW = this.font.width(qualityLabel);
            g.drawString(this.font, qualityLabel, fx + fw - qlW - 8, fy + 4, 0xFF666677, false);

            // Render each featured item
            for (int fi = 0; fi < featuredIndices.size(); fi++) {
                int idx = featuredIndices.get(fi);
                ShopItem fItem = this.items.get(idx);
                int rowY = fy + 16 + fi * cardH;

                // Alternating subtle row bg
                if (fi % 2 == 0) {
                    g.fill(fx + 2, rowY, fx + fw - 2, rowY + cardH, 0xFF14112A);
                }

                // Icon
                int iconX = fx + 6;
                int iconY2 = rowY + 3;
                g.fill(iconX, iconY2, iconX + 18, iconY2 + 18, 0xFF0E0E18);
                ItemStack fStack = this.resolveItemStack(fItem.itemId());
                if (!fStack.isEmpty()) {
                    g.renderItem(fStack, iconX + 1, iconY2 + 1);
                }

                // Name + price
                int textX = iconX + 22;
                String cleanName = fItem.displayName().replaceAll("\u00A7.", "");
                boolean isWeapon = fItem.displayName().contains("Weapon:");
                int nameColor = isWeapon ? 0xFFFF9944 : 0xFFFFDD44;
                g.drawString(this.font, cleanName, textX, iconY2 + 1, nameColor, false);
                g.drawString(this.font, "$" + fItem.buyPrice(), textX, iconY2 + 11, 0xFF55BB55, false);

                // Single "Buy x1" button
                int btnW = 36;
                int btnH2 = 14;
                int btnX = fx + fw - btnW - 8;
                int btnY = rowY + (cardH - btnH2) / 2;
                boolean btnHover = mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY && mouseY < btnY + btnH2;
                UIHelper.drawButton(g, btnX, btnY, btnW, btnH2, btnHover);
                String buyLbl = "Buy";
                g.drawString(this.font, buyLbl, btnX + (btnW - this.font.width(buyLbl)) / 2, btnY + 3, 0xFF55BB55, false);
                this.clickRects.add(new ClickRect(btnX, btnY, btnW, btnH2, "buy_1", idx));
            }

            featuredH += 4; // padding below the card
        }

        // Regular items (exclude featured)
        List<Integer> regularIndices = new ArrayList<>();
        int regularEnd = this.items.size() - featuredIndices.size();
        for (int i = 0; i < regularEnd; i++) {
            regularIndices.add(i);
        }
        int regularTop = this.listTop + featuredH;
        renderItemList(g, mouseX, mouseY, this.items, regularIndices, true, this.listLeft, this.listRight, regularTop, this.listBottom);
    }

    private void renderFurnitureStoreWithSidebar(GuiGraphics g, int mouseX, int mouseY) {
        if (!this.furnitureLoaded) {
            UIHelper.drawInsetPanel(g, this.listLeft - 4, this.listTop - 4, this.listRight - this.listLeft + 8, this.listBottom - this.listTop + 8);
            UIHelper.drawCenteredLabel(g, this.font, "Loading furniture catalog...", this.width / 2, this.height / 2);
            return;
        }

        // --- Category sidebar (left) ---
        int sidebarW = 90;
        int sidebarLeft = this.listLeft - 4;
        int sidebarRight = sidebarLeft + sidebarW;
        int sidebarTop = this.listTop - 4;
        int sidebarBottom = this.listBottom + 4;
        g.fill(sidebarLeft, sidebarTop, sidebarRight, sidebarBottom, 0xFF111122);
        g.fill(sidebarRight - 1, sidebarTop, sidebarRight, sidebarBottom, 0xFF30363D);

        // Category header
        g.drawString(this.font, "Categories", sidebarLeft + 4, sidebarTop + 3, 0xFFE8A838, false);
        int catY = sidebarTop + 14;
        int catH = 14;

        g.enableScissor(sidebarLeft, catY, sidebarRight - 1, sidebarBottom);
        for (int i = 0; i < furnitureCategories.size(); i++) {
            int cy = catY + i * catH - categoryScroll;
            if (cy + catH < catY || cy > sidebarBottom) continue;
            String cat = furnitureCategories.get(i);
            boolean selected = cat.equals(selectedCategory);
            boolean hover = mouseX >= sidebarLeft && mouseX < sidebarRight - 1 && mouseY >= cy && mouseY < cy + catH;

            if (selected) {
                g.fill(sidebarLeft, cy, sidebarRight - 1, cy + catH, 0xFF2A2A44);
                g.fill(sidebarLeft, cy, sidebarLeft + 2, cy + catH, 0xFFE8A838);
            } else if (hover) {
                g.fill(sidebarLeft, cy, sidebarRight - 1, cy + catH, 0xFF1A1A33);
            }

            String label = cat;
            if (label.length() > 12) label = label.substring(0, 11) + "..";
            int textColor = selected ? 0xFFE8A838 : (hover ? 0xFFCCCCDD : 0xFF888899);
            g.drawString(this.font, label, sidebarLeft + 6, cy + 3, textColor, false);
            this.clickRects.add(new ClickRect(sidebarLeft, cy, sidebarW - 1, catH, "fcat_" + i, i));
        }
        g.disableScissor();

        // --- Search bar + item list (right of sidebar) ---
        int contentLeft = sidebarRight + 2;
        int contentRight = this.listRight + 4;

        // Search bar
        int searchY = sidebarTop;
        int searchH = 14;
        int searchBarLeft = contentLeft;
        int searchBarRight = contentRight;
        g.fill(searchBarLeft, searchY, searchBarRight, searchY + searchH, searchFocused ? 0xFF1E1E33 : 0xFF141420);
        g.fill(searchBarLeft, searchY + searchH - 1, searchBarRight, searchY + searchH, 0xFF30363D);

        String searchIcon = "\u2315 ";
        g.drawString(this.font, searchIcon, searchBarLeft + 3, searchY + 3, 0xFF666677, false);
        int searchTextX = searchBarLeft + 3 + this.font.width(searchIcon);
        String displaySearch = furnitureSearch.isEmpty() && !searchFocused ? "Search furniture..." : furnitureSearch + (searchFocused && System.currentTimeMillis() % 1000 < 500 ? "_" : "");
        int searchColor = furnitureSearch.isEmpty() && !searchFocused ? 0xFF555566 : 0xFFCCCCDD;
        g.drawString(this.font, displaySearch, searchTextX, searchY + 3, searchColor, false);
        this.clickRects.add(new ClickRect(searchBarLeft, searchY, searchBarRight - searchBarLeft, searchH, "search_focus", -1));

        // Count display
        List<Integer> filtered = getFilteredFurnitureIndices();
        String countStr = filtered.size() + "/" + furnitureItems.size();
        int countW = this.font.width(countStr);
        g.drawString(this.font, countStr, searchBarRight - countW - 4, searchY + 3, 0xFF666677, false);

        // Item list panel
        int itemListTop = searchY + searchH + 2;
        int itemListBottom = sidebarBottom;
        UIHelper.drawInsetPanel(g, contentLeft - 2, itemListTop - 2, contentRight - contentLeft + 4, itemListBottom - itemListTop + 4);

        // Render filtered items
        renderItemList(g, mouseX, mouseY, this.furnitureItems, filtered, false, contentLeft, contentRight - 2, itemListTop, itemListBottom);
    }

    private void renderItemList(GuiGraphics g, int mouseX, int mouseY, List<ShopItem> displayItems, List<Integer> filteredIndices, boolean showSell, int left, int right) {
        renderItemList(g, mouseX, mouseY, displayItems, filteredIndices, showSell, left, right, this.listTop, this.listBottom);
    }

    private void renderItemList(GuiGraphics g, int mouseX, int mouseY, List<ShopItem> displayItems, List<Integer> filteredIndices, boolean showSell, int left, int right, int top, int bottom) {
        g.enableScissor(left, top, right, bottom);
        int visibleH = bottom - top;

        // Use filtered indices if provided, otherwise all
        List<Integer> indices;
        if (filteredIndices != null) {
            indices = filteredIndices;
        } else {
            indices = new ArrayList<>();
            for (int i = 0; i < displayItems.size(); i++) indices.add(i);
        }

        int totalH = indices.size() * 36 - 4;
        int maxScroll = Math.max(0, totalH - visibleH);
        this.scrollOffset = Math.max(0, Math.min(this.scrollOffset, maxScroll));

        int qBtnW = 22;
        int qBtnH = 12;
        int btnAreaRight = right - 4;

        for (int vi = 0; vi < indices.size(); vi++) {
            int realIndex = indices.get(vi);
            ShopItem item = displayItems.get(realIndex);
            int rowY = top + vi * 36 - this.scrollOffset;
            if (rowY + 32 < top || rowY > bottom) continue;
            UIHelper.drawRowBg(g, left, rowY, right - left, 32, vi % 2 == 0);

            ItemStack iconStack = this.resolveItemStack(item.itemId());
            if (!iconStack.isEmpty()) {
                g.fill(left + 2, rowY + 2, left + 22, rowY + 22, 0xFF181824);
                g.renderItem(iconStack, left + 3, rowY + 3);
            }

            int textX = left + 26;
            int textY = rowY + 3;
            g.drawString(this.font, item.displayName(), textX, textY, 0xFFCCCCDD, false);
            String buyStr = "Buy: $" + item.buyPrice();
            g.drawString(this.font, buyStr, textX, textY + 10, 0xFF55BB55, false);
            if (showSell && item.sellPrice() > 0) {
                int sellStrX = textX + this.font.width(buyStr) + 12;
                g.drawString(this.font, "Sell: $" + item.sellPrice(), sellStrX, textY + 10, 0xFFCC5555, false);
            }

            // Buy buttons
            int btnRow1Y = rowY + 2;
            String prefix = showSell ? "buy_" : "fbuy_";
            String[] buyLabels = {"x1", "x16", "x32", "x64"};
            int[] buyAmounts = {1, 16, 32, 64};
            for (int q = 0; q < 4; q++) {
                int bx = btnAreaRight - (4 - q) * (qBtnW + 2);
                boolean bHover = mouseX >= bx && mouseX < bx + qBtnW && mouseY >= btnRow1Y && mouseY < btnRow1Y + qBtnH;
                UIHelper.drawButton(g, bx, btnRow1Y, qBtnW, qBtnH, bHover);
                int lw = this.font.width(buyLabels[q]);
                g.drawString(this.font, buyLabels[q], bx + (qBtnW - lw) / 2, btnRow1Y + 2, 0xFF55BB55, false);
                this.clickRects.add(new ClickRect(bx, btnRow1Y, qBtnW, qBtnH, prefix + buyAmounts[q], realIndex));
            }

            // Sell buttons (daily deals only)
            if (showSell) {
                int btnRow2Y = rowY + 2 + qBtnH + 2;
                String[] sellLabels = {"S1", "S16", "S32", "All"};
                String[] sellActions = {"sell_1", "sell_16", "sell_32", "sell_all"};
                for (int q = 0; q < 4; q++) {
                    int sx = btnAreaRight - (4 - q) * (qBtnW + 2);
                    boolean sHover = mouseX >= sx && mouseX < sx + qBtnW && mouseY >= btnRow2Y && mouseY < btnRow2Y + qBtnH;
                    UIHelper.drawButton(g, sx, btnRow2Y, qBtnW, qBtnH, sHover);
                    int lw = this.font.width(sellLabels[q]);
                    g.drawString(this.font, sellLabels[q], sx + (qBtnW - lw) / 2, btnRow2Y + 2, 0xFFCC5555, false);
                    this.clickRects.add(new ClickRect(sx, btnRow2Y, qBtnW, qBtnH, sellActions[q], realIndex));
                }
            }
        }
        g.disableScissor();

        if (totalH > visibleH) {
            float progress = maxScroll > 0 ? (float) this.scrollOffset / (float) maxScroll : 0.0f;
            UIHelper.drawScrollbar(g, right + 2, top, bottom - top, progress);
        }
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean consumed) {
        if (consumed) return super.mouseClicked(event, consumed);
        int mx = (int) event.x();
        int my = (int) event.y();
        for (ClickRect r : this.clickRects) {
            if (mx < r.x || mx >= r.x + r.w || my < r.y || my >= r.y + r.h) continue;
            if (r.action.startsWith("tab_")) {
                int newTab = r.index;
                if (newTab != this.activeTab) {
                    this.activeTab = newTab;
                    this.scrollOffset = 0;
                    this.searchFocused = false;
                    if (newTab == 1 && !this.furnitureLoaded) {
                        ClientPacketDistributor.sendToServer(new ComputerActionPayload("request_furniture", ""));
                    }
                }
                return true;
            }
            if (r.action.equals("search_focus")) {
                this.searchFocused = true;
                return true;
            }
            if (r.action.startsWith("fcat_")) {
                int catIdx = r.index;
                if (catIdx >= 0 && catIdx < furnitureCategories.size()) {
                    selectedCategory = furnitureCategories.get(catIdx);
                    this.scrollOffset = 0;
                }
                return true;
            }
            switch (r.action) {
                case "back" -> { if (this.minecraft != null) this.minecraft.setScreen(this.parent); }
                case "buy_1" -> sendBuyAction(r.index, 1);
                case "buy_16" -> sendBuyAction(r.index, 16);
                case "buy_32" -> sendBuyAction(r.index, 32);
                case "buy_64" -> sendBuyAction(r.index, 64);
                case "sell_1" -> sendSellAction(r.index, 1);
                case "sell_16" -> sendSellAction(r.index, 16);
                case "sell_32" -> sendSellAction(r.index, 32);
                case "sell_all" -> sendSellAction(r.index, 9999);
                case "fbuy_1" -> sendFurnitureBuyAction(r.index, 1);
                case "fbuy_16" -> sendFurnitureBuyAction(r.index, 16);
                case "fbuy_32" -> sendFurnitureBuyAction(r.index, 32);
                case "fbuy_64" -> sendFurnitureBuyAction(r.index, 64);
            }
            return true;
        }
        // Check scrollbar click
        if (tryStartScrollbarDrag(mx, my)) {
            return true;
        }
        // Click outside search defocuses
        if (searchFocused) {
            searchFocused = false;
        }
        return super.mouseClicked(event, consumed);
    }

    private boolean tryStartScrollbarDrag(int mx, int my) {
        // Determine scrollbar position based on active tab
        int sbRight;
        int sbTop;
        int sbBottom;
        if (activeTab == 0) {
            sbRight = this.listRight;
            sbTop = this.listTop;
            sbBottom = this.listBottom;
        } else {
            if (!this.furnitureLoaded) return false;
            int sidebarW = 90;
            int sidebarRight = this.listLeft - 4 + sidebarW;
            int contentRight = this.listRight + 4;
            sbRight = contentRight - 2;
            sbTop = this.listTop - 4 + 14 + 2; // below search bar
            sbBottom = this.listBottom + 4;
        }
        int sbX = sbRight + 2;
        int trackW = 6;
        int trackH = sbBottom - sbTop;
        if (mx < sbX || mx >= sbX + trackW || my < sbTop || my >= sbBottom) return false;

        // Compute max scroll for current content
        int totalH;
        if (activeTab == 0) {
            // Compute featured height same as renderDailyDeals
            List<Integer> featuredIndices = new ArrayList<>();
            for (int i = this.items.size() - 1; i >= 0; i--) {
                String name = this.items.get(i).displayName();
                if (name.contains("Relic:") || name.contains("Weapon:")) {
                    featuredIndices.add(0, i);
                } else break;
            }
            int featuredH = featuredIndices.isEmpty() ? 0 : (18 + featuredIndices.size() * 24 + 6 + 4);
            int regularCount = this.items.size() - featuredIndices.size();
            int regularTop = this.listTop + featuredH;
            int visibleH = this.listBottom - regularTop;
            totalH = regularCount * 36 - 4;
            this.dragMaxScroll = Math.max(0, totalH - visibleH);
            this.dragTrackH = this.listBottom - regularTop;
        } else {
            List<Integer> filtered = getFilteredFurnitureIndices();
            int visibleH = sbBottom - sbTop;
            totalH = filtered.size() * 36 - 4;
            this.dragMaxScroll = Math.max(0, totalH - visibleH);
            this.dragTrackH = trackH;
        }
        if (this.dragMaxScroll <= 0) return false;

        this.dragThumbH = Math.max(16, this.dragTrackH / 5);
        this.draggingScrollbar = true;
        this.dragStartY = my;
        this.dragStartScroll = this.scrollOffset;
        return true;
    }

    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();
        if (searchFocused) {
            if (keyCode == 259) { // Backspace
                if (!furnitureSearch.isEmpty()) {
                    furnitureSearch = furnitureSearch.substring(0, furnitureSearch.length() - 1);
                    scrollOffset = 0;
                }
                return true;
            }
            if (keyCode == 256) { // Escape
                searchFocused = false;
                return true;
            }
            if (keyCode == 257) { // Enter
                searchFocused = false;
                return true;
            }
            return super.keyPressed(event);
        }
        return super.keyPressed(event);
    }

    public boolean charTyped(net.minecraft.client.input.CharacterEvent event) {
        char ch = (char) event.codepoint();
        if (searchFocused && furnitureSearch.length() < 32) {
            if (ch >= 32 && ch < 127) {
                furnitureSearch += ch;
                scrollOffset = 0;
                return true;
            }
        }
        return super.charTyped(event);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        // Scroll category sidebar if mouse is over it
        if (activeTab == 1 && mouseX < this.listLeft - 4 + 90) {
            categoryScroll = Math.max(0, categoryScroll - (int)(scrollY * 14));
            return true;
        }
        this.scrollOffset -= (int) (scrollY * 36.0);
        this.scrollOffset = Math.max(0, this.scrollOffset);
        return true;
    }

    public boolean mouseDragged(MouseButtonEvent e, double dx, double dy) {
        if (draggingScrollbar && e.button() == 0) {
            double deltaY = e.y() - dragStartY;
            int thumbTravel = dragTrackH - dragThumbH;
            if (thumbTravel > 0) {
                double ratio = deltaY / (double) thumbTravel;
                this.scrollOffset = (int) Math.round(dragStartScroll + ratio * dragMaxScroll);
                this.scrollOffset = Math.max(0, Math.min(this.scrollOffset, dragMaxScroll));
            }
            return true;
        }
        return super.mouseDragged(e, dx, dy);
    }

    public boolean mouseReleased(MouseButtonEvent e) {
        if (draggingScrollbar && e.button() == 0) {
            draggingScrollbar = false;
            return true;
        }
        return super.mouseReleased(e);
    }

    private void sendBuyAction(int index, int quantity) {
        for (int i = 0; i < quantity; i++) {
            ClientPacketDistributor.sendToServer(new ComputerActionPayload("buy_shop_item", String.valueOf(index)));
        }
    }

    private void sendSellAction(int index, int quantity) {
        ClientPacketDistributor.sendToServer(new ComputerActionPayload("sell_shop_item", index + ":" + quantity));
    }

    private void sendFurnitureBuyAction(int index, int quantity) {
        for (int i = 0; i < quantity; i++) {
            ClientPacketDistributor.sendToServer(new ComputerActionPayload("buy_furniture_item", String.valueOf(index)));
        }
    }

    private ItemStack resolveItemStack(String itemId) {
        try {
            Identifier id = Identifier.parse(itemId);
            Optional<?> itemOptional = BuiltInRegistries.ITEM.getOptional(id);
            if (itemOptional.isPresent()) {
                return new ItemStack((ItemLike) itemOptional.get());
            }
        } catch (Exception ignored) {}
        return ItemStack.EMPTY;
    }

    public boolean isPauseScreen() {
        return false;
    }

    private record ClickRect(int x, int y, int w, int h, String action, int index) {}
}

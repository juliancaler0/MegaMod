package com.ultra.megamod.feature.computer.screen.panels;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ultra.megamod.feature.computer.network.ComputerActionPayload;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Admin panel for marketplace oversight: view all listings, moderate, detect fraud.
 */
public class MarketplaceAdminPanel {

    private static final int BG = 0xFF12121A;
    private static final int CARD_BG = 0xFF1C1C26;
    private static final int BORDER = 0xFF3A3A48;
    private static final int TEXT = 0xFFE6EDF3;
    private static final int LABEL = 0xFF8B949E;
    private static final int ACCENT = 0xFF58A6FF;
    private static final int SUCCESS = 0xFF4CAF50;
    private static final int WARNING = 0xFFD29922;
    private static final int ERROR = 0xFFF85149;
    private static final int PURPLE = 0xFFA371F7;
    private static final int ROW_EVEN = 0xFF1E1E2A;
    private static final int ROW_ODD = 0xFF222234;
    private static final int HEADER_BG = 0xFF161B22;
    private static final int ROW_HEIGHT = 16;

    private final Font font;
    private int scroll = 0;
    private int maxScroll = 0;
    private int refreshTicks = 0;
    private boolean dataLoaded = false;
    private String subView = "listings"; // "listings", "stats", "history"

    // Listings data
    private final List<ListingEntry> listings = new ArrayList<>();
    private int totalListings, totalValue;
    private String mostTradedItem = "";

    // Stats
    private int wtsCount, wtbCount;
    private int avgPrice;
    private final List<String[]> priceWatch = new ArrayList<>(); // [item, avgPrice, currentPrice, flag]
    private final List<String[]> tradeHistory = new ArrayList<>(); // [time, buyer, seller, item, qty, price]
    private final List<String[]> fraudFlags = new ArrayList<>(); // [player, reason, details]

    // Button bounds
    private final List<int[]> subTabBounds = new ArrayList<>();
    private final List<int[]> actionBtns = new ArrayList<>();

    public record ListingEntry(String id, String type, String player, String playerUuid, String item, int quantity, int pricePerUnit, long posted, long expiry) {}

    public MarketplaceAdminPanel(Font font) {
        this.font = font;
    }

    public void requestData() {
        scroll = 0;
        sendAction("marketplace_admin_request", "");
    }

    private void sendAction(String action, String data) {
        ClientPacketDistributor.sendToServer(
                (CustomPacketPayload) new ComputerActionPayload(action, data),
                (CustomPacketPayload[]) new CustomPacketPayload[0]);
    }

    public void tick() {
        refreshTicks++;
        if (refreshTicks % 200 == 0) requestData();

        ComputerDataPayload response = ComputerDataPayload.lastResponse;
        if (response != null && "marketplace_admin_data".equals(response.dataType())) {
            handleResponse(response.dataType(), response.jsonData());
            ComputerDataPayload.lastResponse = null;
        }
    }

    public void handleResponse(String type, String jsonData) {
        if (!"marketplace_admin_data".equals(type)) return;
        try {
            JsonObject root = JsonParser.parseString(jsonData).getAsJsonObject();

            totalListings = getInt(root, "totalListings");
            totalValue = getInt(root, "totalValue");
            mostTradedItem = getString(root, "mostTradedItem");
            wtsCount = getInt(root, "wtsCount");
            wtbCount = getInt(root, "wtbCount");
            avgPrice = getInt(root, "avgPrice");

            listings.clear();
            if (root.has("listings")) {
                for (JsonElement el : root.getAsJsonArray("listings")) {
                    JsonObject l = el.getAsJsonObject();
                    listings.add(new ListingEntry(
                            getString(l, "id"), getString(l, "type"), getString(l, "player"),
                            getString(l, "playerUuid"), getString(l, "item"),
                            getInt(l, "quantity"), getInt(l, "pricePerUnit"),
                            l.has("posted") ? l.get("posted").getAsLong() : 0,
                            l.has("expiry") ? l.get("expiry").getAsLong() : 0
                    ));
                }
            }

            priceWatch.clear();
            if (root.has("priceWatch")) {
                for (JsonElement el : root.getAsJsonArray("priceWatch")) {
                    JsonObject pw = el.getAsJsonObject();
                    priceWatch.add(new String[]{
                            getString(pw, "item"), String.valueOf(getInt(pw, "avgPrice")),
                            String.valueOf(getInt(pw, "currentPrice")), getString(pw, "flag")
                    });
                }
            }

            tradeHistory.clear();
            if (root.has("tradeHistory")) {
                for (JsonElement el : root.getAsJsonArray("tradeHistory")) {
                    JsonObject th = el.getAsJsonObject();
                    tradeHistory.add(new String[]{
                            getString(th, "time"), getString(th, "buyer"), getString(th, "seller"),
                            getString(th, "item"), String.valueOf(getInt(th, "quantity")),
                            String.valueOf(getInt(th, "price"))
                    });
                }
            }

            fraudFlags.clear();
            if (root.has("fraudFlags")) {
                for (JsonElement el : root.getAsJsonArray("fraudFlags")) {
                    JsonObject ff = el.getAsJsonObject();
                    fraudFlags.add(new String[]{getString(ff, "player"), getString(ff, "reason"), getString(ff, "details")});
                }
            }

            dataLoaded = true;
        } catch (Exception ignored) {}
    }

    public void render(GuiGraphics g, int mouseX, int mouseY, int left, int top, int right, int bottom) {
        int w = right - left;
        int h = bottom - top;
        g.fill(left, top, right, bottom, BG);

        // Sub-tab bar
        subTabBounds.clear();
        actionBtns.clear();
        String[] tabs = {"Listings", "Stats", "History"};
        String[] tabKeys = {"listings", "stats", "history"};
        int tabX = left + 4;
        int tabY = top + 2;
        for (int i = 0; i < tabs.length; i++) {
            int tabW = font.width(tabs[i]) + 12;
            boolean selected = subView.equals(tabKeys[i]);
            boolean hov = mouseX >= tabX && mouseX < tabX + tabW && mouseY >= tabY && mouseY < tabY + 18;
            g.fill(tabX, tabY, tabX + tabW, tabY + 18, selected ? 0xFF1A3A4A : (hov ? 0xFF2A2A36 : CARD_BG));
            drawBorder(g, tabX, tabY, tabX + tabW, tabY + 18, selected ? ACCENT : BORDER);
            g.drawString(font, tabs[i], tabX + 6, tabY + 5, selected ? TEXT : LABEL, false);
            subTabBounds.add(new int[]{tabX, tabY, tabW, 18, i});
            tabX += tabW + 2;
        }

        int contentTop = top + 24;
        int contentLeft = left + 4;
        int contentRight = right - 4;
        int contentW = contentRight - contentLeft;

        g.enableScissor(contentLeft, contentTop, contentRight, bottom - 2);
        int y = contentTop - scroll;

        // Header stats
        renderSectionHeader(g, contentLeft, y, contentW, "Marketplace Overview");
        y += 18;

        int cardW = (contentW - 10) / 3;
        renderCard(g, contentLeft, y, cardW, 36, "Total Listings", String.valueOf(totalListings), ACCENT, mouseX, mouseY);
        renderCard(g, contentLeft + cardW + 5, y, cardW, 36, "Total Value", formatCoins(totalValue), SUCCESS, mouseX, mouseY);
        renderCard(g, contentLeft + (cardW + 5) * 2, y, cardW, 36, "WTS/WTB", wtsCount + "/" + wtbCount, PURPLE, mouseX, mouseY);
        y += 40;

        if (!mostTradedItem.isEmpty()) {
            g.fill(contentLeft, y, contentLeft + contentW, y + 14, CARD_BG);
            g.drawString(font, "Most Traded: " + mostTradedItem + "  |  Avg Price: " + formatCoins(avgPrice), contentLeft + 4, y + 3, LABEL, false);
            y += 18;
        }

        if (!dataLoaded) {
            g.drawString(font, "Loading marketplace data...", contentLeft + 4, y + 10, LABEL, false);
            g.disableScissor();
            return;
        }

        switch (subView) {
            case "listings": y = renderListings(g, mouseX, mouseY, contentLeft, y, contentW); break;
            case "stats": y = renderStats(g, mouseX, mouseY, contentLeft, y, contentW); break;
            case "history": y = renderHistory(g, mouseX, mouseY, contentLeft, y, contentW); break;
        }

        y += 20;
        g.disableScissor();

        int totalContentH = y + scroll - contentTop;
        maxScroll = Math.max(0, totalContentH - (bottom - 2 - contentTop));

        if (maxScroll > 0) {
            int barX = right - 4;
            g.fill(barX, contentTop, barX + 3, bottom - 2, 0xFF21262D);
            int thumbH = Math.max(15, (int)((float)(bottom - 2 - contentTop) / totalContentH * (bottom - 2 - contentTop)));
            int thumbY = contentTop + (int)((float) scroll / maxScroll * ((bottom - 2 - contentTop) - thumbH));
            g.fill(barX, thumbY, barX + 3, thumbY + thumbH, LABEL);
        }
    }

    private int renderListings(GuiGraphics g, int mx, int my, int left, int y, int w) {
        renderSectionHeader(g, left, y, w, "Active Listings (" + listings.size() + ")");
        y += 18;

        // Table header
        g.fill(left, y, left + w, y + 14, HEADER_BG);
        g.drawString(font, "Type", left + 4, y + 3, LABEL, false);
        g.drawString(font, "Player", left + 35, y + 3, LABEL, false);
        g.drawString(font, "Item", left + 110, y + 3, LABEL, false);
        g.drawString(font, "Qty", left + 210, y + 3, LABEL, false);
        g.drawString(font, "Price/ea", left + 240, y + 3, LABEL, false);
        g.drawString(font, "Action", left + w - 50, y + 3, LABEL, false);
        y += 15;

        for (int i = 0; i < listings.size(); i++) {
            ListingEntry l = listings.get(i);
            boolean alt = i % 2 == 0;
            g.fill(left, y, left + w, y + ROW_HEIGHT, alt ? ROW_EVEN : ROW_ODD);

            int typeColor = "WTS".equals(l.type) ? SUCCESS : ACCENT;
            g.drawString(font, l.type, left + 4, y + 4, typeColor, false);
            g.drawString(font, truncate(l.player, 10), left + 35, y + 4, TEXT, false);
            g.drawString(font, truncate(l.item, 14), left + 110, y + 4, TEXT, false);
            g.drawString(font, String.valueOf(l.quantity), left + 210, y + 4, TEXT, false);
            g.drawString(font, formatCoins(l.pricePerUnit), left + 240, y + 4, SUCCESS, false);

            // Remove button
            int btnX = left + w - 52;
            int btnY = y + 1;
            boolean btnHov = mx >= btnX && mx < btnX + 48 && my >= btnY && my < btnY + 14;
            g.fill(btnX, btnY, btnX + 48, btnY + 14, btnHov ? 0xFF5A1A1A : 0xFF3A1A1A);
            drawBorder(g, btnX, btnY, btnX + 48, btnY + 14, ERROR);
            int tw = font.width("Remove");
            g.drawString(font, "Remove", btnX + (48 - tw) / 2, btnY + 3, ERROR, false);
            actionBtns.add(new int[]{btnX, btnY, 48, 14, 500 + i}); // 500+ = remove listing

            y += ROW_HEIGHT;
        }

        if (listings.isEmpty()) {
            g.fill(left, y, left + w, y + 20, CARD_BG);
            drawBorder(g, left, y, left + w, y + 20, BORDER);
            g.drawString(font, "No active listings.", left + 8, y + 6, LABEL, false);
            y += 22;
        }

        return y;
    }

    private int renderStats(GuiGraphics g, int mx, int my, int left, int y, int w) {
        // Price watch
        if (!priceWatch.isEmpty()) {
            renderSectionHeader(g, left, y, w, "Price Watch - Anomalies");
            y += 18;

            g.fill(left, y, left + w, y + 14, HEADER_BG);
            g.drawString(font, "Item", left + 4, y + 3, LABEL, false);
            g.drawString(font, "Avg Price", left + 120, y + 3, LABEL, false);
            g.drawString(font, "Current", left + 190, y + 3, LABEL, false);
            g.drawString(font, "Flag", left + 260, y + 3, LABEL, false);
            y += 15;

            for (int i = 0; i < priceWatch.size(); i++) {
                String[] pw = priceWatch.get(i);
                boolean alt = i % 2 == 0;
                g.fill(left, y, left + w, y + 13, alt ? ROW_EVEN : ROW_ODD);
                g.drawString(font, truncate(pw[0], 16), left + 4, y + 2, TEXT, false);
                g.drawString(font, pw[1], left + 120, y + 2, TEXT, false);
                g.drawString(font, pw[2], left + 190, y + 2, TEXT, false);
                int flagColor = "HIGH".equals(pw[3]) ? ERROR : ("LOW".equals(pw[3]) ? WARNING : LABEL);
                g.drawString(font, pw[3], left + 260, y + 2, flagColor, false);
                y += 14;
            }
        } else {
            renderSectionHeader(g, left, y, w, "Price Watch");
            y += 18;
            g.fill(left, y, left + w, y + 20, CARD_BG);
            g.drawString(font, "No price anomalies detected.", left + 8, y + 6, LABEL, false);
            y += 22;
        }

        // Fraud detection
        y += 8;
        renderSectionHeader(g, left, y, w, "Fraud Detection");
        y += 18;

        if (!fraudFlags.isEmpty()) {
            for (int i = 0; i < fraudFlags.size(); i++) {
                String[] ff = fraudFlags.get(i);
                g.fill(left, y, left + w, y + 24, 0xFF2A1A1A);
                drawBorder(g, left, y, left + w, y + 24, ERROR);
                g.drawString(font, "Player: " + ff[0], left + 6, y + 2, TEXT, false);
                g.drawString(font, ff[1] + " - " + ff[2], left + 6, y + 13, WARNING, false);
                y += 26;
            }
        } else {
            g.fill(left, y, left + w, y + 20, CARD_BG);
            drawBorder(g, left, y, left + w, y + 20, BORDER);
            g.drawString(font, "No fraud flags.", left + 8, y + 6, SUCCESS, false);
            y += 22;
        }

        return y;
    }

    private int renderHistory(GuiGraphics g, int mx, int my, int left, int y, int w) {
        renderSectionHeader(g, left, y, w, "Recent Completed Trades");
        y += 18;

        g.fill(left, y, left + w, y + 14, HEADER_BG);
        g.drawString(font, "Time", left + 4, y + 3, LABEL, false);
        g.drawString(font, "Buyer", left + 55, y + 3, LABEL, false);
        g.drawString(font, "Seller", left + 120, y + 3, LABEL, false);
        g.drawString(font, "Item", left + 185, y + 3, LABEL, false);
        g.drawString(font, "Qty", left + 265, y + 3, LABEL, false);
        g.drawString(font, "Price", left + w - 50, y + 3, LABEL, false);
        y += 15;

        for (int i = 0; i < tradeHistory.size(); i++) {
            String[] th = tradeHistory.get(i);
            boolean alt = i % 2 == 0;
            g.fill(left, y, left + w, y + 13, alt ? ROW_EVEN : ROW_ODD);
            g.drawString(font, th[0], left + 4, y + 2, LABEL, false);
            g.drawString(font, truncate(th[1], 8), left + 55, y + 2, TEXT, false);
            g.drawString(font, truncate(th[2], 8), left + 120, y + 2, TEXT, false);
            g.drawString(font, truncate(th[3], 10), left + 185, y + 2, TEXT, false);
            g.drawString(font, th[4], left + 265, y + 2, TEXT, false);
            g.drawString(font, formatCoins(Integer.parseInt(th[5])), left + w - 50, y + 2, SUCCESS, false);
            y += 14;
        }

        if (tradeHistory.isEmpty()) {
            g.fill(left, y, left + w, y + 20, CARD_BG);
            g.drawString(font, "No trade history available.", left + 8, y + 6, LABEL, false);
            y += 22;
        }

        return y;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button, int left, int top, int right, int bottom) {
        int mx = (int) mouseX;
        int my = (int) mouseY;

        for (int[] tb : subTabBounds) {
            if (mx >= tb[0] && mx < tb[0] + tb[2] && my >= tb[1] && my < tb[1] + tb[3]) {
                String[] tabKeys = {"listings", "stats", "history"};
                subView = tabKeys[tb[4]];
                scroll = 0;
                return true;
            }
        }

        for (int[] ab : actionBtns) {
            if (mx >= ab[0] && mx < ab[0] + ab[2] && my >= ab[1] && my < ab[1] + ab[3]) {
                int actionId = ab[4];
                if (actionId >= 500 && actionId < 500 + listings.size()) {
                    ListingEntry listing = listings.get(actionId - 500);
                    sendAction("marketplace_admin_remove", listing.id);
                }
                return true;
            }
        }

        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scroll = Math.max(0, Math.min(maxScroll, scroll - (int)(scrollY * 12)));
        return true;
    }

    // Helpers
    private void renderSectionHeader(GuiGraphics g, int left, int y, int w, String title) {
        g.fill(left, y, left + w, y + 16, HEADER_BG);
        g.fill(left, y + 16, left + w, y + 17, BORDER);
        g.drawString(font, title, left + 4, y + 4, ACCENT, false);
    }

    private void renderCard(GuiGraphics g, int x, int y, int w, int h, String label, String value, int color, int mx, int my) {
        boolean hov = mx >= x && mx < x + w && my >= y && my < y + h;
        g.fill(x, y, x + w, y + h, hov ? 0xFF222230 : CARD_BG);
        drawBorder(g, x, y, x + w, y + h, hov ? color : BORDER);
        g.drawString(font, label, x + 4, y + 4, LABEL, false);
        g.drawString(font, value, x + 4, y + 16, color, false);
    }

    private void drawBorder(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        g.fill(x1, y1, x2, y1 + 1, color);
        g.fill(x1, y2 - 1, x2, y2, color);
        g.fill(x1, y1, x1 + 1, y2, color);
        g.fill(x2 - 1, y1, x2, y2, color);
    }

    private String truncate(String s, int max) { return s.length() > max ? s.substring(0, max) + ".." : s; }
    private String formatCoins(int a) {
        if (Math.abs(a) >= 1000000) return String.format("%.1fM", a / 1000000.0);
        if (Math.abs(a) >= 1000) return String.format("%.1fK", a / 1000.0);
        return String.valueOf(a);
    }
    private int getInt(JsonObject o, String k) { return o.has(k) ? o.get(k).getAsInt() : 0; }
    private String getString(JsonObject o, String k) { return o.has(k) ? o.get(k).getAsString() : ""; }
}

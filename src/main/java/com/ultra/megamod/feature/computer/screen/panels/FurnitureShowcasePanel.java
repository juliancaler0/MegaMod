package com.ultra.megamod.feature.computer.screen.panels;

import com.ultra.megamod.feature.computer.network.ComputerActionPayload;
import com.ultra.megamod.feature.economy.shop.FurnitureShop;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.ArrayList;
import java.util.List;

/**
 * Admin panel tab for placing all furniture blocks in a grid on flat terrain.
 * Uses the same pattern as MobShowcasePanel but places blocks instead of entities.
 * Each block is spaced 3 apart in rows of 20, with category signs.
 */
public class FurnitureShowcasePanel {
    private final Font font;

    private static final int BG = 0xFF0D1117;
    private static final int HEADER_BG = 0xFF161B22;
    private static final int BORDER = 0xFF30363D;
    private static final int TEXT = 0xFFE6EDF3;
    private static final int LABEL = 0xFF8B949E;
    private static final int ACCENT = 0xFF58A6FF;
    private static final int SUCCESS = 0xFF3FB950;
    private static final int WARNING = 0xFFD29922;
    private static final int ERROR = 0xFFF85149;
    private static final int BTN_BG = 0xFF21262D;
    private static final int BTN_HOVER = 0xFF30363D;

    private int scroll = 0;
    private String statusMessage = "";
    private int statusColor = TEXT;
    private long statusExpiry = 0;

    // Categories and their block IDs (built from FurnitureShop catalog)
    private final List<String> categories;
    private final List<List<String>> categoryBlockIds;
    private final List<List<String>> categoryDisplayNames;
    private int totalBlocks;

    public FurnitureShowcasePanel(Font font) {
        this.font = font;

        // Build category lists from the furniture catalog
        categories = new ArrayList<>();
        categoryBlockIds = new ArrayList<>();
        categoryDisplayNames = new ArrayList<>();
        totalBlocks = 0;

        var catalog = FurnitureShop.getCatalog();
        var catOrder = FurnitureShop.getCategories();

        for (String cat : catOrder) {
            List<String> ids = new ArrayList<>();
            List<String> names = new ArrayList<>();
            for (int i = 0; i < catalog.size(); i++) {
                if (cat.equals(FurnitureShop.getCategoryForIndex(i))) {
                    ids.add(catalog.get(i).itemId());
                    names.add(catalog.get(i).displayName());
                }
            }
            if (!ids.isEmpty()) {
                categories.add(cat);
                categoryBlockIds.add(ids);
                categoryDisplayNames.add(names);
                totalBlocks += ids.size();
            }
        }
    }

    public void render(GuiGraphics g, int mouseX, int mouseY, int left, int top, int right, int bottom) {
        int y = top + 4 - scroll;

        // Header
        g.fill(left, top, right, top + 24, HEADER_BG);
        g.drawString(font, "Furniture Showcase — Block Viewer", left + 6, top + 4, ACCENT, false);
        g.drawString(font, "Place furniture blocks in a grid for inspection (" + totalBlocks + " items)", left + 6, top + 14, LABEL, false);

        y = top + 28;

        // Status message
        if (!statusMessage.isEmpty() && System.currentTimeMillis() < statusExpiry) {
            g.drawString(font, statusMessage, left + 6, y, statusColor, false);
            y += 14;
        }

        // ---- Global buttons ----
        int btnX = left + 6;

        int placeAllW = font.width("Place All Furniture") + 12;
        boolean hoverPlaceAll = mouseX >= btnX && mouseX < btnX + placeAllW && mouseY >= y && mouseY < y + 16;
        g.fill(btnX, y, btnX + placeAllW, y + 16, hoverPlaceAll ? BTN_HOVER : BTN_BG);
        g.drawString(font, "Place All Furniture", btnX + 6, y + 4, ACCENT, false);
        btnX += placeAllW + 6;

        int clearW = font.width("Clear All Nearby") + 12;
        boolean hoverClear = mouseX >= btnX && mouseX < btnX + clearW && mouseY >= y && mouseY < y + 16;
        g.fill(btnX, y, btnX + clearW, y + 16, hoverClear ? BTN_HOVER : BTN_BG);
        g.drawString(font, "Clear All Nearby", btnX + 6, y + 4, ERROR, false);

        y += 24;

        // ---- Category sections ----
        int[] catColors = {0xFFE040FB, 0xFF58A6FF, 0xFF3FB950, 0xFFD29922, 0xFFF85149,
            0xFF79C0FF, 0xFFBC8CFF, 0xFF56D364, 0xFFE3B341, 0xFFFF7B72,
            0xFF8B949E, 0xFFA5D6FF, 0xFF7EE787, 0xFFD2A8FF, 0xFFFFA657,
            0xFFFF9492, 0xFF96D0FF};

        for (int ci = 0; ci < categories.size(); ci++) {
            if (y > bottom + 100) break; // stop early if way off screen
            String cat = categories.get(ci);
            int catColor = catColors[ci % catColors.length];
            List<String> ids = categoryBlockIds.get(ci);
            List<String> names = categoryDisplayNames.get(ci);

            // Category header
            g.drawString(font, cat.toUpperCase() + " (" + ids.size() + ")", left + 6, y, catColor, false);

            // Place category button
            int placeCatW = font.width("Place") + 10;
            int placeCatX = right - placeCatW - 8;
            boolean hoverPlaceCat = mouseX >= placeCatX && mouseX < placeCatX + placeCatW && mouseY >= y - 1 && mouseY < y + 11;
            g.fill(placeCatX, y - 1, placeCatX + placeCatW, y + 11, hoverPlaceCat ? BTN_HOVER : BTN_BG);
            g.drawString(font, "Place", placeCatX + 5, y + 1, catColor, false);

            y += 14;

            // Item rows
            for (int i = 0; i < ids.size(); i++) {
                if (y > bottom + 50) break;
                int rowH = 16;
                boolean hoverRow = mouseX >= left + 4 && mouseX < right - 4 && mouseY >= y && mouseY < y + rowH;
                g.fill(left + 4, y, right - 4, y + rowH, hoverRow ? 0xFF151A23 : HEADER_BG);
                g.fill(left + 4, y, left + 6, y + rowH, catColor);

                // Item name
                g.drawString(font, names.get(i), left + 10, y + 4, TEXT, false);

                // Block ID (smaller, dimmer)
                String shortId = ids.get(i).replace("megamod:", "");
                int idW = font.width(shortId);
                g.drawString(font, shortId, right - idW - 66, y + 4, LABEL, false);

                // Place single button
                int singleBtnX = right - 60;
                int singleBtnW = 50;
                boolean hoverSingle = mouseX >= singleBtnX && mouseX < singleBtnX + singleBtnW && mouseY >= y + 1 && mouseY < y + rowH - 1;
                g.fill(singleBtnX, y + 1, singleBtnX + singleBtnW, y + rowH - 1, hoverSingle ? BTN_HOVER : BTN_BG);
                g.drawString(font, "Place", singleBtnX + 12, y + 4, catColor, false);

                y += rowH + 1;
            }

            y += 8;
        }

        // Info
        y += 4;
        g.drawString(font, "Blocks placed 3 apart in rows of 20, on signs with names.", left + 6, y, LABEL, false);
        y += 12;
        g.drawString(font, "Use 'Clear All Nearby' to remove placed showcase blocks.", left + 6, y, LABEL, false);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button, int left, int top, int right, int bottom) {
        int y = top + 28;

        // Status offset
        if (!statusMessage.isEmpty() && System.currentTimeMillis() < statusExpiry) {
            y += 14;
        }

        // ---- Global buttons ----
        int btnX = left + 6;
        int placeAllW = font.width("Place All Furniture") + 12;
        if (mouseX >= btnX && mouseX < btnX + placeAllW && mouseY >= y && mouseY < y + 16) {
            sendAction("furniture_showcase_place", "{\"type\":\"all\"}");
            setStatus("Placing all " + totalBlocks + " furniture blocks...", ACCENT);
            return true;
        }
        btnX += placeAllW + 6;

        int clearW = font.width("Clear All Nearby") + 12;
        if (mouseX >= btnX && mouseX < btnX + clearW && mouseY >= y && mouseY < y + 16) {
            sendAction("furniture_showcase_clear", "{}");
            setStatus("Cleared nearby showcase blocks", ERROR);
            return true;
        }

        y += 24;

        // ---- Category sections ----
        for (int ci = 0; ci < categories.size(); ci++) {
            String cat = categories.get(ci);
            List<String> ids = categoryBlockIds.get(ci);

            // Place category button
            int placeCatW = font.width("Place") + 10;
            int placeCatX = right - placeCatW - 8;
            if (mouseX >= placeCatX && mouseX < placeCatX + placeCatW && mouseY >= y - 1 && mouseY < y + 11) {
                sendAction("furniture_showcase_place", "{\"type\":\"category\",\"category\":\"" + cat + "\"}");
                setStatus("Placing " + ids.size() + " " + cat + " blocks...", SUCCESS);
                return true;
            }

            y += 14;

            // Item rows
            for (int i = 0; i < ids.size(); i++) {
                int rowH = 16;
                int singleBtnX = right - 60;
                int singleBtnW = 50;
                if (mouseX >= singleBtnX && mouseX < singleBtnX + singleBtnW && mouseY >= y + 1 && mouseY < y + rowH - 1) {
                    sendAction("furniture_showcase_place", "{\"type\":\"single\",\"blockId\":\"" + ids.get(i) + "\"}");
                    setStatus("Placed " + categoryDisplayNames.get(ci).get(i), SUCCESS);
                    return true;
                }
                y += rowH + 1;
            }
            y += 8;
        }

        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scroll = Math.max(0, scroll - (int)(scrollY * 15));
        return true;
    }

    public void handleResponse(String type, String jsonData) {
        if ("furniture_showcase_result".equals(type)) {
            try {
                com.google.gson.JsonObject obj = com.google.gson.JsonParser.parseString(jsonData).getAsJsonObject();
                if (obj.has("msg")) {
                    setStatus(obj.get("msg").getAsString(), SUCCESS);
                }
            } catch (Exception ignored) {}
        }
    }

    private void sendAction(String action, String data) {
        ClientPacketDistributor.sendToServer(
            (CustomPacketPayload) new ComputerActionPayload(action, data),
            (CustomPacketPayload[]) new CustomPacketPayload[0]);
    }

    private void setStatus(String msg, int color) {
        this.statusMessage = msg;
        this.statusColor = color;
        this.statusExpiry = System.currentTimeMillis() + 5000;
    }
}

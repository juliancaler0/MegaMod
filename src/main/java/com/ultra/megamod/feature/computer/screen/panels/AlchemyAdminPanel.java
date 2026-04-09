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

import java.util.*;

/**
 * Admin panel for alchemy system oversight: recipe stats, ingredient usage, toggle recipes.
 */
public class AlchemyAdminPanel {

    private static final int BG = 0xFF12121A;
    private static final int CARD_BG = 0xFF1C1C26;
    private static final int BORDER = 0xFF3A3A48;
    private static final int TEXT = 0xFFE6EDF3;
    private static final int LABEL = 0xFF8B949E;
    private static final int ACCENT = 0xFFA371F7; // Purple for alchemy
    private static final int SUCCESS = 0xFF4CAF50;
    private static final int WARNING = 0xFFD29922;
    private static final int ERROR = 0xFFF85149;
    private static final int BLUE = 0xFF58A6FF;
    private static final int ROW_EVEN = 0xFF1E1E2A;
    private static final int ROW_ODD = 0xFF222234;
    private static final int HEADER_BG = 0xFF161B22;
    private static final int ROW_HEIGHT = 16;
    private static final int SILVER = 0xFFC0C0C0;

    private final Font font;
    private int scroll = 0;
    private int maxScroll = 0;
    private int refreshTicks = 0;
    private boolean dataLoaded = false;
    private String subView = "recipes"; // "recipes", "players", "ingredients", "economy"

    // Recipe stats
    private final List<RecipeEntry> recipes = new ArrayList<>();
    private int totalBrewed, totalDiscoveries, totalRecipes;

    // Player alchemy levels
    private final List<PlayerAlchEntry> playerAlch = new ArrayList<>();

    // Ingredient usage
    private final List<IngredientEntry> ingredients = new ArrayList<>();

    // Economy impact
    private int totalPotionValue, potionsInCirculation;

    // Buttons
    private final List<int[]> subTabBounds = new ArrayList<>();
    private final List<int[]> actionBtns = new ArrayList<>();

    public record RecipeEntry(String name, String id, int timesBrewed, int discoveryCount, boolean enabled, String difficulty) {}
    public record PlayerAlchEntry(String name, String uuid, int level, int xp, int discovered, int brewed) {}
    public record IngredientEntry(String name, int timesUsed, int available, String source) {}

    public AlchemyAdminPanel(Font font) {
        this.font = font;
    }

    public void requestData() {
        scroll = 0;
        sendAction("alchemy_admin_request", "");
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
        if (response != null && "alchemy_admin_data".equals(response.dataType())) {
            handleResponse(response.dataType(), response.jsonData());
            ComputerDataPayload.lastResponse = null;
        }
    }

    public void handleResponse(String type, String jsonData) {
        if (!"alchemy_admin_data".equals(type)) return;
        try {
            JsonObject root = JsonParser.parseString(jsonData).getAsJsonObject();

            totalBrewed = getInt(root, "totalBrewed");
            totalDiscoveries = getInt(root, "totalDiscoveries");
            totalRecipes = getInt(root, "totalRecipes");
            totalPotionValue = getInt(root, "totalPotionValue");
            potionsInCirculation = getInt(root, "potionsInCirculation");

            recipes.clear();
            if (root.has("recipes")) {
                for (JsonElement el : root.getAsJsonArray("recipes")) {
                    JsonObject r = el.getAsJsonObject();
                    recipes.add(new RecipeEntry(
                            getString(r, "name"), getString(r, "id"),
                            getInt(r, "timesBrewed"), getInt(r, "discoveryCount"),
                            r.has("enabled") && r.get("enabled").getAsBoolean(),
                            getString(r, "difficulty")
                    ));
                }
            }

            playerAlch.clear();
            if (root.has("players")) {
                for (JsonElement el : root.getAsJsonArray("players")) {
                    JsonObject p = el.getAsJsonObject();
                    playerAlch.add(new PlayerAlchEntry(
                            getString(p, "name"), getString(p, "uuid"),
                            getInt(p, "level"), getInt(p, "xp"),
                            getInt(p, "discovered"), getInt(p, "brewed")
                    ));
                }
            }

            ingredients.clear();
            if (root.has("ingredients")) {
                for (JsonElement el : root.getAsJsonArray("ingredients")) {
                    JsonObject ig = el.getAsJsonObject();
                    ingredients.add(new IngredientEntry(
                            getString(ig, "name"), getInt(ig, "timesUsed"),
                            getInt(ig, "available"), getString(ig, "source")
                    ));
                }
            }

            dataLoaded = true;
        } catch (Exception ignored) {}
    }

    public void render(GuiGraphics g, int mouseX, int mouseY, int left, int top, int right, int bottom) {
        int w = right - left;
        g.fill(left, top, right, bottom, BG);

        // Sub-tab bar
        subTabBounds.clear();
        actionBtns.clear();
        String[] tabs = {"Recipes", "Players", "Ingredients", "Economy"};
        String[] tabKeys = {"recipes", "players", "ingredients", "economy"};
        int tabX = left + 4;
        int tabY = top + 2;
        for (int i = 0; i < tabs.length; i++) {
            int tabW = font.width(tabs[i]) + 12;
            boolean selected = subView.equals(tabKeys[i]);
            boolean hov = mouseX >= tabX && mouseX < tabX + tabW && mouseY >= tabY && mouseY < tabY + 18;
            g.fill(tabX, tabY, tabX + tabW, tabY + 18, selected ? 0xFF2A1A3A : (hov ? 0xFF2A2A36 : CARD_BG));
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

        // Summary cards
        renderSectionHeader(g, contentLeft, y, contentW, "Alchemy Overview");
        y += 18;

        int cardW = (contentW - 10) / 3;
        renderCard(g, contentLeft, y, cardW, 36, "Total Brewed", String.valueOf(totalBrewed), ACCENT, mouseX, mouseY);
        renderCard(g, contentLeft + cardW + 5, y, cardW, 36, "Discoveries", String.valueOf(totalDiscoveries), SUCCESS, mouseX, mouseY);
        renderCard(g, contentLeft + (cardW + 5) * 2, y, cardW, 36, "Recipes", String.valueOf(totalRecipes), BLUE, mouseX, mouseY);
        y += 40;

        if (!dataLoaded) {
            g.drawString(font, "Loading alchemy data...", contentLeft + 4, y + 10, LABEL, false);
            g.disableScissor();
            return;
        }

        switch (subView) {
            case "recipes": y = renderRecipes(g, mouseX, mouseY, contentLeft, y, contentW); break;
            case "players": y = renderPlayers(g, mouseX, mouseY, contentLeft, y, contentW); break;
            case "ingredients": y = renderIngredients(g, mouseX, mouseY, contentLeft, y, contentW); break;
            case "economy": y = renderEconomy(g, mouseX, mouseY, contentLeft, y, contentW); break;
        }

        y += 20;
        g.disableScissor();

        int totalContentH = y + scroll - contentTop;
        maxScroll = Math.max(0, totalContentH - (bottom - 2 - contentTop));

        if (maxScroll > 0) {
            int barX = right - 4;
            g.fill(barX, contentTop, barX + 3, bottom - 2, 0xFF21262D);
            int barH = bottom - 2 - contentTop;
            int thumbH = Math.max(15, (int)((float) barH / totalContentH * barH));
            int thumbY = contentTop + (int)((float) scroll / maxScroll * (barH - thumbH));
            g.fill(barX, thumbY, barX + 3, thumbY + thumbH, LABEL);
        }
    }

    private int renderRecipes(GuiGraphics g, int mx, int my, int left, int y, int w) {
        renderSectionHeader(g, left, y, w, "Recipe Statistics");
        y += 18;

        // Table header
        g.fill(left, y, left + w, y + 14, HEADER_BG);
        g.drawString(font, "Recipe", left + 4, y + 3, LABEL, false);
        g.drawString(font, "Brewed", left + 140, y + 3, LABEL, false);
        g.drawString(font, "Discovered", left + 200, y + 3, LABEL, false);
        g.drawString(font, "Difficulty", left + 275, y + 3, LABEL, false);
        g.drawString(font, "Status", left + w - 55, y + 3, LABEL, false);
        y += 15;

        for (int i = 0; i < recipes.size(); i++) {
            RecipeEntry r = recipes.get(i);
            boolean alt = i % 2 == 0;
            g.fill(left, y, left + w, y + ROW_HEIGHT, alt ? ROW_EVEN : ROW_ODD);

            g.drawString(font, truncate(r.name, 18), left + 4, y + 4, TEXT, false);
            g.drawString(font, String.valueOf(r.timesBrewed), left + 140, y + 4, TEXT, false);
            g.drawString(font, String.valueOf(r.discoveryCount), left + 200, y + 4, TEXT, false);

            int diffColor = "Hard".equals(r.difficulty) ? ERROR : ("Medium".equals(r.difficulty) ? WARNING : SUCCESS);
            g.drawString(font, r.difficulty, left + 275, y + 4, diffColor, false);

            // Toggle button
            int btnX = left + w - 58;
            int btnY = y + 1;
            boolean btnHov = mx >= btnX && mx < btnX + 52 && my >= btnY && my < btnY + 14;
            String btnLabel = r.enabled ? "ON" : "OFF";
            int btnBg = r.enabled ? (btnHov ? 0xFF1A4A1A : 0xFF1A3A1A) : (btnHov ? 0xFF5A1A1A : 0xFF3A1A1A);
            g.fill(btnX, btnY, btnX + 52, btnY + 14, btnBg);
            drawBorder(g, btnX, btnY, btnX + 52, btnY + 14, r.enabled ? SUCCESS : ERROR);
            int tw = font.width(btnLabel);
            g.drawString(font, btnLabel, btnX + (52 - tw) / 2, btnY + 3, r.enabled ? SUCCESS : ERROR, false);
            actionBtns.add(new int[]{btnX, btnY, 52, 14, 600 + i}); // 600+ = toggle recipe

            y += ROW_HEIGHT;
        }

        if (recipes.isEmpty()) {
            g.fill(left, y, left + w, y + 20, CARD_BG);
            g.drawString(font, "No alchemy recipes registered.", left + 8, y + 6, LABEL, false);
            y += 22;
        }

        return y;
    }

    private int renderPlayers(GuiGraphics g, int mx, int my, int left, int y, int w) {
        renderSectionHeader(g, left, y, w, "Player Alchemy Levels");
        y += 18;

        g.fill(left, y, left + w, y + 14, HEADER_BG);
        g.drawString(font, "Player", left + 4, y + 3, LABEL, false);
        g.drawString(font, "Level", left + 110, y + 3, LABEL, false);
        g.drawString(font, "XP", left + 155, y + 3, LABEL, false);
        g.drawString(font, "Discovered", left + 210, y + 3, LABEL, false);
        g.drawString(font, "Brewed", left + 285, y + 3, LABEL, false);
        g.drawString(font, "Action", left + w - 65, y + 3, LABEL, false);
        y += 15;

        for (int i = 0; i < playerAlch.size(); i++) {
            PlayerAlchEntry p = playerAlch.get(i);
            boolean alt = i % 2 == 0;
            g.fill(left, y, left + w, y + ROW_HEIGHT, alt ? ROW_EVEN : ROW_ODD);

            g.drawString(font, truncate(p.name, 14), left + 4, y + 4, TEXT, false);
            g.drawString(font, String.valueOf(p.level), left + 110, y + 4, ACCENT, false);
            g.drawString(font, String.valueOf(p.xp), left + 155, y + 4, TEXT, false);
            g.drawString(font, String.valueOf(p.discovered), left + 210, y + 4, SUCCESS, false);
            g.drawString(font, String.valueOf(p.brewed), left + 285, y + 4, TEXT, false);

            // Grant All button
            int btnX = left + w - 68;
            int btnY = y + 1;
            boolean btnHov = mx >= btnX && mx < btnX + 62 && my >= btnY && my < btnY + 14;
            g.fill(btnX, btnY, btnX + 62, btnY + 14, btnHov ? 0xFF2A3A2A : CARD_BG);
            drawBorder(g, btnX, btnY, btnX + 62, btnY + 14, btnHov ? SUCCESS : BORDER);
            g.drawString(font, "Grant All", btnX + 6, btnY + 3, SUCCESS, false);
            actionBtns.add(new int[]{btnX, btnY, 62, 14, 700 + i}); // 700+ = grant all recipes to player

            y += ROW_HEIGHT;
        }

        if (playerAlch.isEmpty()) {
            g.fill(left, y, left + w, y + 20, CARD_BG);
            g.drawString(font, "No player alchemy data.", left + 8, y + 6, LABEL, false);
            y += 22;
        }

        return y;
    }

    private int renderIngredients(GuiGraphics g, int mx, int my, int left, int y, int w) {
        renderSectionHeader(g, left, y, w, "Ingredient Usage Stats");
        y += 18;

        g.fill(left, y, left + w, y + 14, HEADER_BG);
        g.drawString(font, "Ingredient", left + 4, y + 3, LABEL, false);
        g.drawString(font, "Times Used", left + 150, y + 3, LABEL, false);
        g.drawString(font, "Available", left + 235, y + 3, LABEL, false);
        g.drawString(font, "Source", left + 310, y + 3, LABEL, false);
        y += 15;

        for (int i = 0; i < ingredients.size(); i++) {
            IngredientEntry ig = ingredients.get(i);
            boolean alt = i % 2 == 0;
            g.fill(left, y, left + w, y + 13, alt ? ROW_EVEN : ROW_ODD);

            g.drawString(font, truncate(ig.name, 20), left + 4, y + 2, TEXT, false);
            g.drawString(font, String.valueOf(ig.timesUsed), left + 150, y + 2, TEXT, false);
            g.drawString(font, String.valueOf(ig.available), left + 235, y + 2, ig.available > 0 ? SUCCESS : ERROR, false);
            g.drawString(font, truncate(ig.source, 12), left + 310, y + 2, LABEL, false);
            y += 14;
        }

        if (ingredients.isEmpty()) {
            g.fill(left, y, left + w, y + 20, CARD_BG);
            g.drawString(font, "No ingredient data available.", left + 8, y + 6, LABEL, false);
            y += 22;
        }

        return y;
    }

    private int renderEconomy(GuiGraphics g, int mx, int my, int left, int y, int w) {
        renderSectionHeader(g, left, y, w, "Alchemy Economy Impact");
        y += 18;

        int cardW = (w - 5) / 2;
        renderCard(g, left, y, cardW, 36, "Potions in Circulation", String.valueOf(potionsInCirculation), ACCENT, mx, my);
        renderCard(g, left + cardW + 5, y, cardW, 36, "Total Potion Value", formatCoins(totalPotionValue), SUCCESS, mx, my);
        y += 40;

        // Top recipes by value
        renderSectionHeader(g, left, y, w, "Most Valuable Recipes");
        y += 18;

        List<RecipeEntry> sorted = new ArrayList<>(recipes);
        sorted.sort((a, b) -> Integer.compare(b.timesBrewed, a.timesBrewed));

        for (int i = 0; i < Math.min(sorted.size(), 10); i++) {
            RecipeEntry r = sorted.get(i);
            boolean alt = i % 2 == 0;
            g.fill(left, y, left + w, y + 13, alt ? ROW_EVEN : ROW_ODD);

            int rankColor = i == 0 ? 0xFFFFD700 : (i == 1 ? 0xFFC0C0C0 : (i == 2 ? 0xFFCD7F32 : LABEL));
            g.drawString(font, "#" + (i + 1), left + 4, y + 2, rankColor, false);
            g.drawString(font, r.name, left + 24, y + 2, TEXT, false);
            g.drawString(font, "Brewed: " + r.timesBrewed, left + 180, y + 2, ACCENT, false);
            y += 14;
        }

        if (recipes.isEmpty()) {
            g.fill(left, y, left + w, y + 20, CARD_BG);
            g.drawString(font, "No recipe data yet.", left + 8, y + 6, LABEL, false);
            y += 22;
        }

        return y;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button, int left, int top, int right, int bottom) {
        int mx = (int) mouseX;
        int my = (int) mouseY;

        for (int[] tb : subTabBounds) {
            if (mx >= tb[0] && mx < tb[0] + tb[2] && my >= tb[1] && my < tb[1] + tb[3]) {
                String[] tabKeys = {"recipes", "players", "ingredients", "economy"};
                subView = tabKeys[tb[4]];
                scroll = 0;
                return true;
            }
        }

        for (int[] ab : actionBtns) {
            if (mx >= ab[0] && mx < ab[0] + ab[2] && my >= ab[1] && my < ab[1] + ab[3]) {
                int actionId = ab[4];
                if (actionId >= 600 && actionId < 600 + recipes.size()) {
                    RecipeEntry r = recipes.get(actionId - 600);
                    sendAction("alchemy_admin_toggle", r.id);
                } else if (actionId >= 700 && actionId < 700 + playerAlch.size()) {
                    PlayerAlchEntry p = playerAlch.get(actionId - 700);
                    sendAction("alchemy_admin_grant_all", p.uuid);
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

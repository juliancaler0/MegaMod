package com.ultra.megamod.feature.computer.screen.panels;

import com.ultra.megamod.feature.computer.network.ComputerActionPayload;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.ArrayList;
import java.util.List;

/**
 * Admin search panel — global search across players, items, entities,
 * economy, toggles, citizens, warps, modules, and more.
 * Results are categorized with clickable action buttons.
 */
public class AdminSearchPanel {

    private static final int BG = 0xFF0D1117;
    private static final int CARD_BG = 0xFF161B22;
    private static final int BORDER = 0xFF30363D;
    private static final int TEXT = 0xFFE6EDF3;
    private static final int LABEL = 0xFF8B949E;
    private static final int ACCENT = 0xFF58A6FF;
    private static final int GOLD = 0xFFD4AF37;
    private static final int GREEN = 0xFF3FB950;

    private final Minecraft mc = Minecraft.getInstance();
    private final Font font = mc.font;

    private EditBox searchBox;
    private String lastQuery = "";
    private int scroll = 0;
    private boolean initialized = false;

    // Search results grouped by category
    private final List<SearchResult> results = new ArrayList<>();
    private boolean searching = false;
    private long lastSearchTick = 0;

    public record SearchResult(String category, String name, String detail, String action) {}

    public void init(int left, int top, int right, int bottom) {
        if (searchBox == null) {
            searchBox = new EditBox(font, left + 8, top + 8, right - left - 16, 16,
                    Component.literal("Search..."));
            searchBox.setMaxLength(100);
            searchBox.setHint(Component.literal("Search players, items, toggles, citizens, warps..."));
            searchBox.setResponder(this::onSearchChanged);
        } else {
            searchBox.setX(left + 8);
            searchBox.setY(top + 8);
            searchBox.setWidth(right - left - 16);
        }
        initialized = true;
    }

    private void onSearchChanged(String query) {
        if (query.length() < 2) {
            results.clear();
            searching = false;
            return;
        }
        if (query.equals(lastQuery)) return;
        lastQuery = query;
        searching = true;

        // Send search query to server
        ClientPacketDistributor.sendToServer(
                new ComputerActionPayload("admin_search", query),
                new CustomPacketPayload[0]);
    }

    public void handleResponse(String json) {
        results.clear();
        searching = false;
        // Parse JSON array of results: [{"cat":"Players","name":"NeverNotch","detail":"Online, 500 MC","action":"tp_player NeverNotch"}, ...]
        try {
            // Simple JSON array parser
            int idx = json.indexOf('[');
            if (idx < 0) return;
            String arr = json.substring(idx);
            int pos = 1;
            while (pos < arr.length()) {
                int objStart = arr.indexOf('{', pos);
                if (objStart < 0) break;
                int objEnd = arr.indexOf('}', objStart);
                if (objEnd < 0) break;
                String obj = arr.substring(objStart, objEnd + 1);

                String cat = extractJsonStr(obj, "cat");
                String name = extractJsonStr(obj, "name");
                String detail = extractJsonStr(obj, "detail");
                String action = extractJsonStr(obj, "action");
                results.add(new SearchResult(cat, name, detail, action));

                pos = objEnd + 1;
            }
        } catch (Exception ignored) {}
    }

    private static String extractJsonStr(String json, String key) {
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search);
        if (start < 0) return "";
        start += search.length();
        int end = json.indexOf("\"", start);
        if (end < 0) return "";
        return json.substring(start, end);
    }

    public void render(GuiGraphics g, int mx, int my, int left, int top, int right, int bottom) {
        if (!initialized) init(left, top, right, bottom);

        int w = right - left;
        int y = top + 4;

        // Search box
        if (searchBox != null) {
            searchBox.setX(left + 8);
            searchBox.setY(y);
            searchBox.setWidth(w - 16);
            searchBox.render(g, mx, my, 0);
        }
        y += 24;

        // Divider
        g.fill(left + 4, y, right - 4, y + 1, BORDER);
        y += 6;

        if (searching) {
            g.drawString(font, "Searching...", left + 8, y, LABEL, false);
            return;
        }

        if (results.isEmpty() && lastQuery.length() >= 2) {
            g.drawString(font, "No results found for \"" + lastQuery + "\"", left + 8, y, LABEL, false);
            return;
        }

        if (results.isEmpty()) {
            g.drawString(font, "Type at least 2 characters to search", left + 8, y, LABEL, false);
            y += 14;
            g.drawString(font, "Searches: players, items, toggles,", left + 8, y, LABEL, false);
            y += 12;
            g.drawString(font, "citizens, warps, modules, economy,", left + 8, y, LABEL, false);
            y += 12;
            g.drawString(font, "skills, mobs, blocks, commands", left + 8, y, LABEL, false);
            return;
        }

        // Render results with scroll
        g.enableScissor(left, y, right, bottom - 4);
        int drawY = y - scroll;
        String lastCat = "";
        for (int i = 0; i < results.size(); i++) {
            SearchResult r = results.get(i);

            // Category header
            if (!r.category().equals(lastCat)) {
                lastCat = r.category();
                if (drawY + 16 > y - 20) {
                    g.drawString(font, r.category().toUpperCase(), left + 8, drawY + 2, GOLD, false);
                    g.fill(left + 8 + font.width(r.category().toUpperCase()) + 4, drawY + 6,
                            right - 8, drawY + 7, BORDER);
                }
                drawY += 16;
            }

            // Result row
            if (drawY >= y - 20 && drawY < bottom + 20) {
                boolean hov = mx >= left + 6 && mx < right - 6 && my >= drawY && my < drawY + 20;
                g.fill(left + 6, drawY, right - 6, drawY + 20, hov ? 0xFF1C2128 : CARD_BG);

                g.drawString(font, r.name(), left + 12, drawY + 2, TEXT, false);
                if (!r.detail().isEmpty()) {
                    g.drawString(font, r.detail(), left + 12, drawY + 11, LABEL, false);
                }

                // Action hint on right
                if (!r.action().isEmpty() && hov) {
                    String hint = "[Click]";
                    int hw = font.width(hint);
                    g.drawString(font, hint, right - hw - 12, drawY + 6, ACCENT, false);
                }
            }
            drawY += 22;
        }
        g.disableScissor();
    }

    public boolean mouseClicked(int mx, int my, int button, int left, int top, int right, int bottom) {
        if (searchBox != null && mx >= searchBox.getX() && mx < searchBox.getX() + searchBox.getWidth()
                && my >= searchBox.getY() && my < searchBox.getY() + 16) {
            searchBox.setFocused(true);
            return true;
        }
        if (searchBox != null) searchBox.setFocused(false);

        // Check result clicks
        int y = top + 34;
        int drawY = y - scroll;
        String lastCat = "";
        for (SearchResult r : results) {
            if (!r.category().equals(lastCat)) {
                lastCat = r.category();
                drawY += 16;
            }
            if (mx >= left + 6 && mx < right - 6 && my >= drawY && my < drawY + 20) {
                if (!r.action().isEmpty()) {
                    ClientPacketDistributor.sendToServer(
                            new ComputerActionPayload("admin_search_action", r.action()),
                            new CustomPacketPayload[0]);
                }
                return true;
            }
            drawY += 22;
        }
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // EditBox handles key input via the Screen's keyPressed forwarding
        return searchBox != null && searchBox.isFocused();
    }

    public boolean charTyped(char ch, int modifiers) {
        if (searchBox != null && searchBox.isFocused()) {
            searchBox.insertText(String.valueOf(ch));
            return true;
        }
        return false;
    }

    public boolean mouseScrolled(double mx, double my, double scrollX, double scrollY) {
        scroll = Math.max(0, scroll - (int)(scrollY * 12));
        return true;
    }

    public void tick() {}
}

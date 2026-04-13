package com.ultra.megamod.feature.computer.screen.panels;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ultra.megamod.feature.computer.network.ComputerActionPayload;
import com.ultra.megamod.feature.worldedit.wiki.WorldEditWikiEntries;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.ArrayList;
import java.util.List;

/**
 * Admin Terminal tab content for WorldEdit.
 *
 * Layout (top to bottom):
 *   - WE Mode toggle (requests server-side hand-out / reclaim of wand)
 *   - Status block: selection volume, clipboard size, undo depth, masks
 *   - Action buttons
 *   - Searchable command wiki with category filter
 */
public class WorldEditPanel {

    private static final int BG = 0xFF0D1117;
    private static final int HEADER = 0xFF161B22;
    private static final int BORDER = 0xFF30363D;
    private static final int TEXT = 0xFFE6EDF3;
    private static final int LABEL = 0xFF8B949E;
    private static final int ACCENT = 0xFFA371F7;
    private static final int GREEN = 0xFF3FB950;
    private static final int RED = 0xFFF85149;
    private static final int BUTTON_BG = 0xFF21262D;
    private static final int BUTTON_HOVER = 0xFF2E3540;

    private static final String[] CATEGORIES = {"All", "Selection", "Region", "Clipboard", "Schematic",
        "Generation", "History", "Brush", "Tool", "Utility", "Navigation", "Chunk", "Biome"};

    private final Font font;
    private boolean weMode = false;
    private int selectionVolume = 0;
    private int clipboardSize = 0;
    private int undoDepth = 0;
    private int redoDepth = 0;
    private String maskText = "none";
    private String patternText = "none";
    private String selPos1 = "-";
    private String selPos2 = "-";
    private String brushInfo = "none";

    private int wikiScroll = 0;
    private String search = "";
    private String currentCategory = "All";
    private boolean searchFocused = false;
    private long searchBlinkTick = 0;

    private int lastLeft, lastTop, lastRight, lastBottom;
    private long lastStatusRequestTick = 0;

    public WorldEditPanel(Font font) {
        this.font = font;
    }

    public void tick() {
        searchBlinkTick++;
        // Request status every second (20 ticks)
        if (searchBlinkTick - lastStatusRequestTick > 40) {
            requestStatus();
            lastStatusRequestTick = searchBlinkTick;
        }
    }

    public void requestStatus() {
        ClientPacketDistributor.sendToServer(
            (CustomPacketPayload) new ComputerActionPayload("we_get_status", ""));
    }

    public void handleResponse(String type, String json) {
        if (!"we_status_data".equals(type)) return;
        try {
            JsonObject o = JsonParser.parseString(json).getAsJsonObject();
            if (o.has("mode")) weMode = o.get("mode").getAsBoolean();
            if (o.has("selectionVolume")) selectionVolume = o.get("selectionVolume").getAsInt();
            if (o.has("clipboardSize")) clipboardSize = o.get("clipboardSize").getAsInt();
            if (o.has("undoDepth")) undoDepth = o.get("undoDepth").getAsInt();
            if (o.has("redoDepth")) redoDepth = o.get("redoDepth").getAsInt();
            if (o.has("mask")) maskText = o.get("mask").getAsString();
            if (o.has("pattern")) patternText = o.get("pattern").getAsString();
            if (o.has("pos1")) selPos1 = o.get("pos1").getAsString();
            if (o.has("pos2")) selPos2 = o.get("pos2").getAsString();
            if (o.has("brush")) brushInfo = o.get("brush").getAsString();
        } catch (Exception ignored) {}
    }

    public void render(GuiGraphics g, int mx, int my, int left, int top, int right, int bottom) {
        this.lastLeft = left; this.lastTop = top; this.lastRight = right; this.lastBottom = bottom;
        g.fill(left, top, right, bottom, BG);

        int y = top;
        // Header
        g.fill(left, y, right, y + 22, HEADER);
        g.fill(left, y + 21, right, y + 22, BORDER);
        g.drawString(font, "WorldEdit", left + 6, y + 7, ACCENT, false);
        String subtitle = "Admin-only world editor";
        g.drawString(font, subtitle, right - font.width(subtitle) - 6, y + 7, LABEL, false);
        y += 24;

        // WE Mode toggle
        g.drawString(font, "WE Mode:", left + 6, y + 5, TEXT, false);
        int toggleX = left + 70, toggleY = y, toggleW = 70, toggleH = 18;
        boolean toggleHover = mx >= toggleX && mx < toggleX + toggleW && my >= toggleY && my < toggleY + toggleH;
        g.fill(toggleX, toggleY, toggleX + toggleW, toggleY + toggleH, weMode ? GREEN : RED);
        if (toggleHover) drawRectOutline(g, toggleX, toggleY, toggleX + toggleW, toggleY + toggleH, TEXT);
        String modeLabel = weMode ? "ENABLED" : "DISABLED";
        g.drawString(font, modeLabel, toggleX + (toggleW - font.width(modeLabel)) / 2, toggleY + 5, 0xFF000000, false);
        y += 22;

        // Status block
        g.fill(left + 2, y, right - 2, y + 76, HEADER);
        drawRectOutline(g, left + 2, y, right - 2, y + 76, BORDER);
        int sx = left + 8;
        int sy = y + 6;
        drawStatus(g, sx, sy, "pos1",      selPos1);
        drawStatus(g, sx, sy + 10, "pos2", selPos2);
        drawStatus(g, sx, sy + 20, "volume", String.valueOf(selectionVolume));
        drawStatus(g, sx, sy + 30, "clipboard", clipboardSize + " block(s)");
        drawStatus(g, sx + (right - left) / 2, sy, "undo", String.valueOf(undoDepth));
        drawStatus(g, sx + (right - left) / 2, sy + 10, "redo", String.valueOf(redoDepth));
        drawStatus(g, sx + (right - left) / 2, sy + 20, "mask", maskText);
        drawStatus(g, sx + (right - left) / 2, sy + 30, "pattern", patternText);
        drawStatus(g, sx, sy + 50, "brush", brushInfo);
        y += 80;

        // Action buttons row
        int btnY = y;
        int btnH = 20;
        int[] btnW = {130, 110, 110, 140};
        String[] actions = {"Save Selection As Schematic", "Clear Selection", "Clear Clipboard", "Show Brush Bindings"};
        String[] actionIds = {"we_save_selection", "we_clear_selection", "we_clear_clipboard", "we_brush_list"};
        int bx = left + 4;
        for (int i = 0; i < actions.length; i++) {
            boolean bHover = mx >= bx && mx < bx + btnW[i] && my >= btnY && my < btnY + btnH;
            g.fill(bx, btnY, bx + btnW[i], btnY + btnH, bHover ? BUTTON_HOVER : BUTTON_BG);
            drawRectOutline(g, bx, btnY, bx + btnW[i], btnY + btnH, BORDER);
            g.drawString(font, actions[i], bx + 4, btnY + 6, TEXT, false);
            bx += btnW[i] + 4;
        }
        y += btnH + 6;

        // Category bar
        int catY = y;
        int catX = left + 4;
        for (String c : CATEGORIES) {
            int w = font.width(c) + 8;
            boolean selected = c.equals(currentCategory);
            boolean hover = mx >= catX && mx < catX + w && my >= catY && my < catY + 16;
            g.fill(catX, catY, catX + w, catY + 16, selected ? ACCENT : (hover ? BUTTON_HOVER : HEADER));
            drawRectOutline(g, catX, catY, catX + w, catY + 16, BORDER);
            g.drawString(font, c, catX + 4, catY + 4, selected ? 0xFF000000 : TEXT, false);
            catX += w + 3;
            if (catX > right - 60) { catX = left + 4; catY += 18; }
        }
        y = catY + 20;

        // Search box
        int searchX = left + 4;
        int searchY = y;
        int searchW = right - left - 8;
        int searchH = 16;
        g.fill(searchX, searchY, searchX + searchW, searchY + searchH, BUTTON_BG);
        drawRectOutline(g, searchX, searchY, searchX + searchW, searchY + searchH, searchFocused ? ACCENT : BORDER);
        String disp = search.isEmpty() && !searchFocused ? "Search commands..." : search;
        int tc = search.isEmpty() && !searchFocused ? LABEL : TEXT;
        g.drawString(font, disp, searchX + 4, searchY + 4, tc, false);
        if (searchFocused && (searchBlinkTick / 10) % 2 == 0) {
            int cx = searchX + 4 + font.width(search);
            g.fill(cx, searchY + 3, cx + 1, searchY + searchH - 3, TEXT);
        }
        y += searchH + 4;

        // Wiki list
        List<WorldEditWikiEntries.Entry> entries = WorldEditWikiEntries.filter(currentCategory, search);
        int listLeft = left + 2;
        int listRight = right - 2;
        int listTop = y;
        int listBottom = bottom - 4;
        int rowH = 30;
        int visible = Math.max(1, (listBottom - listTop) / rowH);
        int maxScroll = Math.max(0, entries.size() - visible);
        if (wikiScroll > maxScroll) wikiScroll = maxScroll;
        if (wikiScroll < 0) wikiScroll = 0;

        g.enableScissor(listLeft, listTop, listRight, listBottom);
        int ry = listTop;
        for (int i = wikiScroll; i < entries.size() && ry < listBottom; i++) {
            WorldEditWikiEntries.Entry e = entries.get(i);
            g.fill(listLeft, ry, listRight, ry + rowH - 2, i % 2 == 0 ? BG : HEADER);
            g.drawString(font, e.name(), listLeft + 4, ry + 3, ACCENT, false);
            g.drawString(font, e.syntax(), listLeft + 120, ry + 3, TEXT, false);
            g.drawString(font, e.category(), listRight - font.width(e.category()) - 6, ry + 3, LABEL, false);
            String desc = e.description();
            if (desc.length() > 80) desc = desc.substring(0, 77) + "...";
            g.drawString(font, desc, listLeft + 4, ry + 14, LABEL, false);
            if (e.examples().length > 0) {
                String ex = e.examples()[0];
                if (ex.length() > 90) ex = ex.substring(0, 87) + "...";
                g.drawString(font, "\u203A " + ex, listLeft + 4, ry + 22, GREEN, false);
            }
            ry += rowH;
        }
        g.disableScissor();

        // Scroll indicator
        if (entries.size() > visible) {
            int barX = listRight - 4;
            int totalH = listBottom - listTop;
            int thumbH = Math.max(20, totalH * visible / Math.max(1, entries.size()));
            int thumbY = listTop + (totalH - thumbH) * wikiScroll / Math.max(1, maxScroll);
            g.fill(barX, listTop, barX + 3, listBottom, 0x66000000);
            g.fill(barX, thumbY, barX + 3, thumbY + thumbH, ACCENT);
        }
    }

    private void drawStatus(GuiGraphics g, int x, int y, String label, String value) {
        g.drawString(font, label + ":", x, y, LABEL, false);
        g.drawString(font, value, x + 60, y, TEXT, false);
    }

    private static void drawRectOutline(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        g.fill(x1, y1, x2, y1 + 1, color);
        g.fill(x1, y2 - 1, x2, y2, color);
        g.fill(x1, y1, x1 + 1, y2, color);
        g.fill(x2 - 1, y1, x2, y2, color);
    }

    public boolean mouseClicked(double mx, double my, int button, int left, int top, int right, int bottom) {
        // WE Mode toggle
        int toggleX = left + 70, toggleY = top + 24, toggleW = 70, toggleH = 18;
        if (mx >= toggleX && mx < toggleX + toggleW && my >= toggleY && my < toggleY + toggleH) {
            boolean newVal = !weMode;
            ClientPacketDistributor.sendToServer(
                (CustomPacketPayload) new ComputerActionPayload("we_toggle_mode", Boolean.toString(newVal)));
            return true;
        }
        // Action buttons - at y = top + 24 + 22 + 80
        int btnY = top + 24 + 22 + 80;
        int btnH = 20;
        int[] btnW = {130, 110, 110, 140};
        String[] ids = {"we_save_selection", "we_clear_selection", "we_clear_clipboard", "we_brush_list"};
        int bx = left + 4;
        for (int i = 0; i < ids.length; i++) {
            if (mx >= bx && mx < bx + btnW[i] && my >= btnY && my < btnY + btnH) {
                ClientPacketDistributor.sendToServer(
                    (CustomPacketPayload) new ComputerActionPayload(ids[i], ""));
                return true;
            }
            bx += btnW[i] + 4;
        }
        // Category bar click
        int catY = btnY + btnH + 6;
        int catX = left + 4;
        for (String c : CATEGORIES) {
            int w = font.width(c) + 8;
            if (mx >= catX && mx < catX + w && my >= catY && my < catY + 16) {
                currentCategory = c;
                wikiScroll = 0;
                return true;
            }
            catX += w + 3;
            if (catX > right - 60) { catX = left + 4; catY += 18; }
        }
        // Search box
        int searchX = left + 4, searchW = right - left - 8, searchH = 16;
        int searchY = catY + 20;
        searchFocused = mx >= searchX && mx < searchX + searchW && my >= searchY && my < searchY + searchH;
        return searchFocused;
    }

    public boolean keyPressed(int key, int sc, int mods) {
        if (!searchFocused) return false;
        if (key == 259) { // backspace
            if (!search.isEmpty()) { search = search.substring(0, search.length() - 1); wikiScroll = 0; return true; }
            return true;
        }
        if (key == 256) { searchFocused = false; return true; } // escape
        return false;
    }

    public boolean charTyped(char ch, int mods) {
        if (!searchFocused) return false;
        if (ch >= 32 && ch != 127) {
            search = search + ch;
            wikiScroll = 0;
            return true;
        }
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        wikiScroll -= (int) Math.signum(scrollY);
        if (wikiScroll < 0) wikiScroll = 0;
        return true;
    }
}

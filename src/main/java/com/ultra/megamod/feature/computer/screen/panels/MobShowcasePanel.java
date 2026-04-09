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
 * Unified Showcase Panel — Mobs & Furniture
 * Tab 0 (Mobs): Spawn NoAI showcase versions of dungeon mobs and bosses.
 * Tab 1 (Furniture): Place furniture blocks in a grid for inspection.
 */
public class MobShowcasePanel {
    private final Font font;

    // Style constants
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
    private static final int BOSS_COLOR = 0xFFE040FB;

    private static final int TAB_ACTIVE_BG = 0xFF58A6FF;
    private static final int TAB_INACTIVE_BG = 0xFF21262D;
    private static final int TAB_ACTIVE_TEXT = 0xFFFFFFFF;
    private static final int TAB_INACTIVE_TEXT = 0xFF8B949E;

    // View state: 0 = Mobs, 1 = Furniture
    private int activeView = 0;

    // Shared state
    private int scroll = 0;
    private String statusMessage = "";
    private int statusColor = TEXT;
    private long statusExpiry = 0;

    // ---- Mob data ----
    private static final String[][] BOSSES = {
        {"Wraith", "megamod:wraith_boss"},
        {"Ossukage", "megamod:ossukage_boss"},
        {"Dungeon Keeper", "megamod:dungeon_keeper"},
        {"Frostmaw", "megamod:frostmaw_boss"},
        {"Wroughtnaut", "megamod:wroughtnaut_boss"},
        {"Umvuthi", "megamod:umvuthi_boss"},
        {"Chaos Spawner", "megamod:chaos_spawner_boss"},
        {"Sculptor", "megamod:sculptor_boss"},
    };

    private static final String[][] MOBS = {
        {"Dungeon Mob", "megamod:dungeon_mob"},
        {"Minion", "megamod:minion"},
        {"Dungeon Rat", "megamod:dungeon_rat"},
        {"Undead Knight", "megamod:undead_knight"},
        {"Dungeon Slime", "megamod:dungeon_slime"},
        {"Hollow", "megamod:hollow"},
        {"Naga", "megamod:naga"},
        {"Grottol", "megamod:grottol"},
        {"Lantern", "megamod:lantern"},
        {"Foliaath", "megamod:foliaath"},
        {"Umvuthana", "megamod:umvuthana"},
        {"Spawner Carrier", "megamod:spawner_carrier"},
        {"Bluff", "megamod:bluff"},
        {"Baby Foliaath", "megamod:baby_foliaath"},
    };

    // ---- Furniture data (built from FurnitureShop catalog) ----
    private final List<String> furnitureCategories;
    private final List<List<String>> furnitureCategoryBlockIds;
    private final List<List<String>> furnitureCategoryDisplayNames;
    private int totalFurnitureBlocks;

    public MobShowcasePanel(Font font) {
        this.font = font;

        // Build furniture category lists from the furniture catalog
        furnitureCategories = new ArrayList<>();
        furnitureCategoryBlockIds = new ArrayList<>();
        furnitureCategoryDisplayNames = new ArrayList<>();
        totalFurnitureBlocks = 0;

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
                furnitureCategories.add(cat);
                furnitureCategoryBlockIds.add(ids);
                furnitureCategoryDisplayNames.add(names);
                totalFurnitureBlocks += ids.size();
            }
        }
    }

    /** Allow external callers (e.g. AdminTerminalScreen tab 33) to force Furniture view. */
    public void setActiveView(int view) {
        if (view != activeView) {
            activeView = view;
            scroll = 0;
        }
    }

    public void render(GuiGraphics g, int mouseX, int mouseY, int left, int top, int right, int bottom) {
        int w = right - left;

        // ---- Tab bar ----
        int tabY = top;
        int tabH = 18;
        g.fill(left, tabY, right, tabY + tabH, HEADER_BG);

        String[] tabLabels = {"Mobs", "Furniture"};
        int tabX = left + 6;
        for (int t = 0; t < tabLabels.length; t++) {
            int tabW = font.width(tabLabels[t]) + 16;
            boolean active = (activeView == t);
            boolean hover = mouseX >= tabX && mouseX < tabX + tabW && mouseY >= tabY + 1 && mouseY < tabY + tabH - 1;
            g.fill(tabX, tabY + 1, tabX + tabW, tabY + tabH - 1, active ? TAB_ACTIVE_BG : (hover ? BTN_HOVER : TAB_INACTIVE_BG));
            g.drawString(font, tabLabels[t], tabX + 8, tabY + 5, active ? TAB_ACTIVE_TEXT : TAB_INACTIVE_TEXT, false);
            tabX += tabW + 4;
        }

        // Separator line under tabs
        g.fill(left, tabY + tabH, right, tabY + tabH + 1, BORDER);

        int contentTop = top + tabH + 1;

        if (activeView == 0) {
            renderMobs(g, mouseX, mouseY, left, contentTop, right, bottom);
        } else {
            renderFurniture(g, mouseX, mouseY, left, contentTop, right, bottom);
        }
    }

    // ========================== MOB VIEW ==========================

    private void renderMobs(GuiGraphics g, int mouseX, int mouseY, int left, int top, int right, int bottom) {
        int y = top + 4 - scroll;

        // Header
        g.fill(left, top, right, top + 24, HEADER_BG);
        g.drawString(font, "Mob Showcase — Model Viewer", left + 6, top + 4, ACCENT, false);
        g.drawString(font, "Spawn NoAI entities for inspection", left + 6, top + 14, LABEL, false);

        y = top + 28;

        // Status message
        if (!statusMessage.isEmpty() && System.currentTimeMillis() < statusExpiry) {
            g.drawString(font, statusMessage, left + 6, y, statusColor, false);
            y += 14;
        }

        // ---- Spawn All Section ----
        int allBtnX = left + 6;
        int allBtnW = font.width("Spawn All Bosses") + 12;
        boolean hoverAllBosses = mouseX >= allBtnX && mouseX < allBtnX + allBtnW && mouseY >= y && mouseY < y + 16;
        g.fill(allBtnX, y, allBtnX + allBtnW, y + 16, hoverAllBosses ? BTN_HOVER : BTN_BG);
        g.drawString(font, "Spawn All Bosses", allBtnX + 6, y + 4, BOSS_COLOR, false);
        allBtnX += allBtnW + 6;

        int allMobBtnW = font.width("Spawn All Mobs") + 12;
        boolean hoverAllMobs = mouseX >= allBtnX && mouseX < allBtnX + allMobBtnW && mouseY >= y && mouseY < y + 16;
        g.fill(allBtnX, y, allBtnX + allMobBtnW, y + 16, hoverAllMobs ? BTN_HOVER : BTN_BG);
        g.drawString(font, "Spawn All Mobs", allBtnX + 6, y + 4, SUCCESS, false);
        allBtnX += allMobBtnW + 6;

        int allBtnW2 = font.width("Spawn Everything") + 12;
        boolean hoverAll = mouseX >= allBtnX && mouseX < allBtnX + allBtnW2 && mouseY >= y && mouseY < y + 16;
        g.fill(allBtnX, y, allBtnX + allBtnW2, y + 16, hoverAll ? BTN_HOVER : BTN_BG);
        g.drawString(font, "Spawn Everything", allBtnX + 6, y + 4, ACCENT, false);
        allBtnX += allBtnW2 + 6;

        int clearBtnW = font.width("Kill All Nearby") + 12;
        boolean hoverClear = mouseX >= allBtnX && mouseX < allBtnX + clearBtnW && mouseY >= y && mouseY < y + 16;
        g.fill(allBtnX, y, allBtnX + clearBtnW, y + 16, hoverClear ? BTN_HOVER : BTN_BG);
        g.drawString(font, "Kill All Nearby", allBtnX + 6, y + 4, ERROR, false);

        y += 24;

        // ---- Bosses Section ----
        g.drawString(font, "BOSSES (" + BOSSES.length + ")", left + 6, y, BOSS_COLOR, false);
        y += 14;

        for (int i = 0; i < BOSSES.length; i++) {
            if (y > bottom) break;
            String name = BOSSES[i][0];
            String entityId = BOSSES[i][1];

            int rowH = 20;
            boolean hoverRow = mouseX >= left + 4 && mouseX < right - 4 && mouseY >= y && mouseY < y + rowH;
            g.fill(left + 4, y, right - 4, y + rowH, hoverRow ? 0xFF151A23 : HEADER_BG);
            g.fill(left + 4, y, left + 6, y + rowH, BOSS_COLOR);

            g.drawString(font, name, left + 10, y + 2, TEXT, false);
            g.drawString(font, entityId, left + 10, y + 11, LABEL, false);

            int spawnBtnX = right - 64;
            int spawnBtnW = 56;
            boolean hoverSpawn = mouseX >= spawnBtnX && mouseX < spawnBtnX + spawnBtnW && mouseY >= y + 2 && mouseY < y + rowH - 2;
            g.fill(spawnBtnX, y + 2, spawnBtnX + spawnBtnW, y + rowH - 2, hoverSpawn ? BTN_HOVER : BTN_BG);
            g.drawString(font, "Spawn", spawnBtnX + 10, y + 6, BOSS_COLOR, false);

            y += rowH + 2;
        }

        y += 8;

        // ---- Mobs Section ----
        g.drawString(font, "MOBS (" + MOBS.length + ")", left + 6, y, SUCCESS, false);
        y += 14;

        for (int i = 0; i < MOBS.length; i++) {
            if (y > bottom) break;
            String name = MOBS[i][0];
            String entityId = MOBS[i][1];

            int rowH = 20;
            boolean hoverRow = mouseX >= left + 4 && mouseX < right - 4 && mouseY >= y && mouseY < y + rowH;
            g.fill(left + 4, y, right - 4, y + rowH, hoverRow ? 0xFF151A23 : HEADER_BG);
            g.fill(left + 4, y, left + 6, y + rowH, SUCCESS);

            g.drawString(font, name, left + 10, y + 2, TEXT, false);
            g.drawString(font, entityId, left + 10, y + 11, LABEL, false);

            int spawnBtnX = right - 64;
            int spawnBtnW = 56;
            boolean hoverSpawn = mouseX >= spawnBtnX && mouseX < spawnBtnX + spawnBtnW && mouseY >= y + 2 && mouseY < y + rowH - 2;
            g.fill(spawnBtnX, y + 2, spawnBtnX + spawnBtnW, y + rowH - 2, hoverSpawn ? BTN_HOVER : BTN_BG);
            g.drawString(font, "Spawn", spawnBtnX + 10, y + 6, SUCCESS, false);

            y += rowH + 2;
        }

        y += 12;

        // Info text
        g.drawString(font, "Entities spawn with NoAI, spaced 4 blocks apart", left + 6, y, LABEL, false);
        y += 12;
        g.drawString(font, "facing you. Use 'Kill All Nearby' to clean up.", left + 6, y, LABEL, false);
    }

    // ========================== FURNITURE VIEW ==========================

    private void renderFurniture(GuiGraphics g, int mouseX, int mouseY, int left, int top, int right, int bottom) {
        int y = top + 4 - scroll;

        // Header
        g.fill(left, top, right, top + 24, HEADER_BG);
        g.drawString(font, "Furniture Showcase — Block Viewer", left + 6, top + 4, ACCENT, false);
        g.drawString(font, "Place furniture blocks in a grid for inspection (" + totalFurnitureBlocks + " items)", left + 6, top + 14, LABEL, false);

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

        for (int ci = 0; ci < furnitureCategories.size(); ci++) {
            if (y > bottom + 100) break;
            String cat = furnitureCategories.get(ci);
            int catColor = catColors[ci % catColors.length];
            List<String> ids = furnitureCategoryBlockIds.get(ci);
            List<String> names = furnitureCategoryDisplayNames.get(ci);

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

    // ========================== CLICK HANDLING ==========================

    public boolean mouseClicked(double mouseX, double mouseY, int button, int left, int top, int right, int bottom) {
        // ---- Tab bar clicks ----
        int tabY = top;
        int tabH = 18;
        String[] tabLabels = {"Mobs", "Furniture"};
        int tabX = left + 6;
        for (int t = 0; t < tabLabels.length; t++) {
            int tabW = font.width(tabLabels[t]) + 16;
            if (mouseX >= tabX && mouseX < tabX + tabW && mouseY >= tabY + 1 && mouseY < tabY + tabH - 1) {
                if (activeView != t) {
                    activeView = t;
                    scroll = 0;
                    statusMessage = "";
                }
                return true;
            }
            tabX += tabW + 4;
        }

        int contentTop = top + tabH + 1;

        if (activeView == 0) {
            return mouseClickedMobs(mouseX, mouseY, button, left, contentTop, right, bottom);
        } else {
            return mouseClickedFurniture(mouseX, mouseY, button, left, contentTop, right, bottom);
        }
    }

    private boolean mouseClickedMobs(double mouseX, double mouseY, int button, int left, int top, int right, int bottom) {
        int y = top + 28;

        // Status offset
        if (!statusMessage.isEmpty() && System.currentTimeMillis() < statusExpiry) {
            y += 14;
        }

        // ---- Spawn All buttons ----
        int allBtnX = left + 6;
        int allBtnW = font.width("Spawn All Bosses") + 12;
        if (mouseX >= allBtnX && mouseX < allBtnX + allBtnW && mouseY >= y && mouseY < y + 16) {
            sendAction("mob_showcase_spawn", "{\"type\":\"all_bosses\"}");
            setStatus("Spawning all 8 bosses...", BOSS_COLOR);
            return true;
        }
        allBtnX += allBtnW + 6;

        int allMobBtnW = font.width("Spawn All Mobs") + 12;
        if (mouseX >= allBtnX && mouseX < allBtnX + allMobBtnW && mouseY >= y && mouseY < y + 16) {
            sendAction("mob_showcase_spawn", "{\"type\":\"all_mobs\"}");
            setStatus("Spawning all 14 mobs...", SUCCESS);
            return true;
        }
        allBtnX += allMobBtnW + 6;

        int allBtnW2 = font.width("Spawn Everything") + 12;
        if (mouseX >= allBtnX && mouseX < allBtnX + allBtnW2 && mouseY >= y && mouseY < y + 16) {
            sendAction("mob_showcase_spawn", "{\"type\":\"all\"}");
            setStatus("Spawning all 22 entities...", ACCENT);
            return true;
        }
        allBtnX += allBtnW2 + 6;

        int clearBtnW = font.width("Kill All Nearby") + 12;
        if (mouseX >= allBtnX && mouseX < allBtnX + clearBtnW && mouseY >= y && mouseY < y + 16) {
            sendAction("mob_showcase_kill", "{}");
            setStatus("Killed nearby showcase entities", ERROR);
            return true;
        }

        y += 24;

        // ---- Boss rows ----
        y += 14; // "BOSSES" label
        for (int i = 0; i < BOSSES.length; i++) {
            int rowH = 20;
            int spawnBtnX = right - 64;
            int spawnBtnW = 56;
            if (mouseX >= spawnBtnX && mouseX < spawnBtnX + spawnBtnW && mouseY >= y + 2 && mouseY < y + rowH - 2) {
                sendAction("mob_showcase_spawn", "{\"type\":\"single\",\"entity\":\"" + BOSSES[i][1] + "\"}");
                setStatus("Spawned " + BOSSES[i][0], BOSS_COLOR);
                return true;
            }
            y += rowH + 2;
        }

        y += 8;

        // ---- Mob rows ----
        y += 14; // "MOBS" label
        for (int i = 0; i < MOBS.length; i++) {
            int rowH = 20;
            int spawnBtnX = right - 64;
            int spawnBtnW = 56;
            if (mouseX >= spawnBtnX && mouseX < spawnBtnX + spawnBtnW && mouseY >= y + 2 && mouseY < y + rowH - 2) {
                sendAction("mob_showcase_spawn", "{\"type\":\"single\",\"entity\":\"" + MOBS[i][1] + "\"}");
                setStatus("Spawned " + MOBS[i][0], SUCCESS);
                return true;
            }
            y += rowH + 2;
        }

        return false;
    }

    private boolean mouseClickedFurniture(double mouseX, double mouseY, int button, int left, int top, int right, int bottom) {
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
            setStatus("Placing all " + totalFurnitureBlocks + " furniture blocks...", ACCENT);
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
        for (int ci = 0; ci < furnitureCategories.size(); ci++) {
            String cat = furnitureCategories.get(ci);
            List<String> ids = furnitureCategoryBlockIds.get(ci);

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
                    setStatus("Placed " + furnitureCategoryDisplayNames.get(ci).get(i), SUCCESS);
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
        if ("mob_showcase_result".equals(type) || "furniture_showcase_result".equals(type)) {
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

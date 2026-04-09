package com.ultra.megamod.feature.computer.screen;

import com.ultra.megamod.feature.computer.network.ComputerActionPayload;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.ui.UIHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public class EncyclopediaScreen extends Screen {
    private final Screen parent;
    private String category = "Guide";
    private int listScroll = 0;
    private int detailScroll = 0;
    private int selectedEntry = -1;
    private List<WikiEntry> allEntries = new ArrayList<>();
    private List<WikiEntry> filteredEntries = new ArrayList<>();
    private EditBox searchBox;
    private Set<String> discoveredIds = new HashSet<>();
    private boolean allUnlocked = false;
    private boolean dataLoaded = false;

    // Layout
    private int titleBarH;
    private int tabW;
    private int tabH;
    private int tabX;
    private int tabStartY;
    private int listLeft;
    private int listRight;
    private int listTop;
    private int listBottom;
    private int detailLeft;
    private int detailRight;
    private int detailTop;
    private int detailBottom;
    private int backX, backY, backW, backH;

    // Category tabs
    private static final String[] CATEGORIES = {"Guide", "Relics", "Mobs", "Dungeons", "Skills", "Items", "Museum", "Controls", "Citizens", "Casino", "Backpacks", "Marketplace", "Corruption", "Alchemy"};
    private static final int[] CATEGORY_COLORS = {
        0xFFFFFFFF, // Guide = white
        0xFFA371F7, // Relics = purple
        0xFF3FB950, // Mobs = green
        0xFFF85149, // Dungeons = red
        0xFF58A6FF, // Skills = blue
        0xFFD29922, // Items = gold
        0xFF4FC3F7, // Museum = light blue
        0xFFBDBDBD, // Controls = silver
        0xFF2E7D32, // Citizens = forest green
        0xFFD4AF37, // Casino = gold
        0xFF8B6914, // Backpacks = leather brown
        0xFF26C6DA, // Marketplace = teal
        0xFFCC3333, // Corruption = dark red
        0xFF9C27B0  // Alchemy = purple/magenta
    };
    private static final String[] CATEGORY_ICONS = {"\u2736", "\u2666", "\u2623", "\u2694", "\u2605", "\u2726", "\u2302", "\u2328", "\u2691", "\u2680", "\u25A3", "\u2696", "\u2620", "\u2697"};

    // Colors
    private static final int TEXT_NORMAL = 0xFFE6EDF3;
    private static final int TEXT_LOCKED = 0xFF4A4A50;
    private static final int TEXT_DIM = 0xFF8B949E;
    private static final int TEXT_HEADER = 0xFFFFD700;

    private static final int ENTRY_ROW_H = 16;

    public record WikiEntry(String id, String name, String category, List<String> detailLines) {}

    public EncyclopediaScreen(Screen parent) {
        super(Component.literal("Encyclopedia"));
        this.parent = parent;
        buildAllEntries();
    }

    @Override
    protected void init() {
        super.init();
        Objects.requireNonNull(this.font);
        this.titleBarH = 9 + 16;

        // Back button
        this.backW = 50;
        this.backH = 16;
        this.backX = 8;
        this.backY = (this.titleBarH - this.backH) / 2;

        // Category tabs on the left
        this.tabW = 70;
        this.tabH = 22;
        this.tabX = 4;
        this.tabStartY = this.titleBarH + 6;

        // List panel (middle)
        this.listLeft = this.tabX + this.tabW + 4;
        this.listRight = this.listLeft + Math.min(170, (this.width - this.tabW - 12) / 3);
        this.listTop = this.titleBarH + 6;
        this.listBottom = this.height - 8;

        // Detail panel (right)
        this.detailLeft = this.listRight + 4;
        this.detailRight = this.width - 4;
        this.detailTop = this.titleBarH + 6;
        this.detailBottom = this.height - 8;

        // Search box at top of list panel
        int searchW = this.listRight - this.listLeft - 4;
        int searchX = this.listLeft + 2;
        int searchY = this.listTop + 2;
        this.searchBox = new EditBox(this.font, searchX, searchY, Math.max(40, searchW), 14, Component.literal("Search..."));
        this.searchBox.setMaxLength(64);
        this.searchBox.setTextColor(TEXT_NORMAL);
        this.searchBox.setResponder(text -> {
            this.listScroll = 0;
            this.selectedEntry = -1;
            filterEntries();
        });
        this.addRenderableWidget(this.searchBox);

        // Request discovery data from server
        ClientPacketDistributor.sendToServer(new ComputerActionPayload("encyclopedia_request", ""), new CustomPacketPayload[0]);

        filterEntries();
    }

    @Override
    public void tick() {
        super.tick();
        ComputerDataPayload response = ComputerDataPayload.lastResponse;
        if (response != null && "encyclopedia_data".equals(response.dataType())) {
            ComputerDataPayload.lastResponse = null;
            parseDiscoveries(response.jsonData());
            dataLoaded = true;
            filterEntries();
        }
        // Consume error responses so the screen doesn't stay stuck
        if (response != null && "error".equals(response.dataType())) {
            ComputerDataPayload.lastResponse = null;
            this.dataLoaded = true;
        }
    }

    /** Guide entries are always unlocked for everyone */
    private boolean isDiscovered(String id) {
        return allUnlocked || id.startsWith("guide_") || discoveredIds.contains(id);
    }

    private void parseDiscoveries(String json) {
        discoveredIds.clear();
        if (json == null || json.length() < 5) return;
        // Check for allUnlocked flag
        if (json.contains("\"allUnlocked\":true")) {
            allUnlocked = true;
            return;
        }
        // Parse {"discoveries":["id1","id2",...]}
        int arrStart = json.indexOf('[');
        int arrEnd = json.lastIndexOf(']');
        if (arrStart < 0 || arrEnd < 0 || arrEnd <= arrStart) return;
        String arrContent = json.substring(arrStart + 1, arrEnd).trim();
        if (arrContent.isEmpty()) return;
        // Split by comma, strip quotes
        int i = 0;
        while (i < arrContent.length()) {
            int qStart = arrContent.indexOf('"', i);
            if (qStart < 0) break;
            int qEnd = arrContent.indexOf('"', qStart + 1);
            if (qEnd < 0) break;
            discoveredIds.add(arrContent.substring(qStart + 1, qEnd));
            i = qEnd + 1;
        }
    }

    private void filterEntries() {
        filteredEntries.clear();
        String search = (searchBox != null) ? searchBox.getValue().toLowerCase(Locale.ROOT).trim() : "";
        for (WikiEntry entry : allEntries) {
            if (!entry.category().equals(this.category)) continue;
            if (!search.isEmpty()) {
                boolean discovered = isDiscovered(entry.id());
                // Only search discovered entries by name; undiscovered are always hidden from search
                if (discovered && !entry.name().toLowerCase(Locale.ROOT).contains(search)) continue;
                if (!discovered) continue;
            }
            filteredEntries.add(entry);
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Background
        g.fill(0, 0, this.width, this.height, 0xFF0D1117);
        UIHelper.drawPanel(g, 0, 0, this.width, this.height);

        // Title bar
        UIHelper.drawTitleBar(g, 0, 0, this.width, this.titleBarH);
        Objects.requireNonNull(this.font);
        int titleY = (this.titleBarH - 9) / 2;
        UIHelper.drawCenteredTitle(g, this.font, "Encyclopedia", this.width / 2, titleY);

        // Back button
        boolean backHover = mouseX >= backX && mouseX < backX + backW && mouseY >= backY && mouseY < backY + backH;
        UIHelper.drawButton(g, backX, backY, backW, backH, backHover);
        int backTextX = backX + (backW - this.font.width("< Back")) / 2;
        g.drawString(this.font, "< Back", backTextX, backY + (backH - 9) / 2, UIHelper.CREAM_TEXT, false);

        // Category tabs
        renderCategoryTabs(g, mouseX, mouseY);

        // List panel background
        UIHelper.drawInsetPanel(g, listLeft, listTop, listRight - listLeft, listBottom - listTop);

        // List content (below search box)
        int listContentTop = listTop + 20;
        renderEntryList(g, mouseX, mouseY, listContentTop);

        // Detail panel
        UIHelper.drawInsetPanel(g, detailLeft, detailTop, detailRight - detailLeft, detailBottom - detailTop);
        renderDetailPanel(g, mouseX, mouseY);

        // Discovery progress in title bar
        int catIndex = getCategoryIndex(this.category);
        int totalInCat = countEntriesInCategory(this.category);
        int discoveredInCat = countDiscoveredInCategory(this.category);
        String progress = discoveredInCat + "/" + totalInCat + " Discovered";
        int progressW = this.font.width(progress);
        int progressColor = catIndex >= 0 ? CATEGORY_COLORS[catIndex] : TEXT_DIM;
        g.drawString(this.font, progress, this.width - progressW - 10, titleY, progressColor, false);

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void renderCategoryTabs(GuiGraphics g, int mouseX, int mouseY) {
        for (int i = 0; i < CATEGORIES.length; i++) {
            int ty = tabStartY + i * (tabH + 2);
            boolean selected = CATEGORIES[i].equals(this.category);
            boolean hovered = mouseX >= tabX && mouseX < tabX + tabW && mouseY >= ty && mouseY < ty + tabH;
            UIHelper.drawTab(g, tabX, ty, tabW, tabH, selected, CATEGORY_COLORS[i]);
            int textColor = selected ? TEXT_NORMAL : TEXT_DIM;
            String label = CATEGORY_ICONS[i] + " " + CATEGORIES[i];
            int textW = this.font.width(label);
            int textX = tabX + (tabW - textW) / 2;
            int textY = ty + (tabH - 9) / 2;
            g.drawString(this.font, label, textX, textY, textColor, false);
        }
    }

    private void renderEntryList(GuiGraphics g, int mouseX, int mouseY, int contentTop) {
        int visibleH = listBottom - contentTop - 2;
        int maxVisible = visibleH / ENTRY_ROW_H;
        int maxScroll = Math.max(0, filteredEntries.size() - maxVisible);
        listScroll = Math.max(0, Math.min(listScroll, maxScroll));

        // Enable scissor for list area
        g.enableScissor(listLeft + 1, contentTop, listRight - 1, listBottom - 1);

        for (int i = 0; i < maxVisible && (i + listScroll) < filteredEntries.size(); i++) {
            int entryIndex = i + listScroll;
            WikiEntry entry = filteredEntries.get(entryIndex);
            boolean discovered = isDiscovered(entry.id());
            int ey = contentTop + i * ENTRY_ROW_H;
            int rowW = listRight - listLeft - 4;

            boolean rowHovered = mouseX >= listLeft + 2 && mouseX < listRight - 2
                    && mouseY >= ey && mouseY < ey + ENTRY_ROW_H;
            boolean isSelected = (selectedEntry == entryIndex);

            // Row background
            if (isSelected) {
                int catIdx = getCategoryIndex(this.category);
                int accentColor = catIdx >= 0 ? CATEGORY_COLORS[catIdx] : 0xFF58A6FF;
                g.fill(listLeft + 2, ey, listRight - 2, ey + ENTRY_ROW_H, (accentColor & 0x00FFFFFF) | 0x40000000);
            } else if (rowHovered && discovered) {
                g.fill(listLeft + 2, ey, listRight - 2, ey + ENTRY_ROW_H, 0x20FFFFFF);
            }

            int textY = ey + (ENTRY_ROW_H - 9) / 2;
            if (discovered) {
                g.drawString(this.font, entry.name(), listLeft + 6, textY, TEXT_NORMAL, false);
            } else {
                // Locked entry
                g.drawString(this.font, "\u2716 ???", listLeft + 6, textY, TEXT_LOCKED, false);
            }
        }

        g.disableScissor();

        // Scrollbar
        if (filteredEntries.size() > maxVisible && maxScroll > 0) {
            float scrollProgress = (float) listScroll / maxScroll;
            UIHelper.drawScrollbar(g, listRight - 8, contentTop, visibleH, scrollProgress);
        }
    }

    private void renderDetailPanel(GuiGraphics g, int mouseX, int mouseY) {
        int panelW = detailRight - detailLeft;
        int panelH = detailBottom - detailTop;
        int pad = 6;
        int contentLeft = detailLeft + pad;
        int contentRight = detailRight - pad;
        int contentTop = detailTop + pad;
        int contentW = contentRight - contentLeft;

        if (selectedEntry < 0 || selectedEntry >= filteredEntries.size()) {
            // No entry selected
            String hint = "Select an entry to view details";
            int hintW = this.font.width(hint);
            g.drawString(this.font, hint, detailLeft + (panelW - hintW) / 2, detailTop + panelH / 2 - 4, TEXT_DIM, false);
            return;
        }

        WikiEntry entry = filteredEntries.get(selectedEntry);
        boolean discovered = isDiscovered(entry.id());

        if (!discovered) {
            String locked = "\u2716 This entry has not been discovered yet.";
            int lockedW = this.font.width(locked);
            g.drawString(this.font, locked, detailLeft + (panelW - lockedW) / 2, detailTop + panelH / 2 - 4, TEXT_LOCKED, false);

            // Hint on how to unlock
            String hint = getUnlockHint(entry);
            if (hint != null) {
                int hintW = this.font.width(hint);
                g.drawString(this.font, hint, detailLeft + (panelW - hintW) / 2, detailTop + panelH / 2 + 10, TEXT_DIM, false);
            }
            return;
        }

        // Render detail content with scrolling
        g.enableScissor(detailLeft + 1, detailTop + 1, detailRight - 1, detailBottom - 1);

        int y = contentTop - detailScroll;

        // Title
        int catIdx = getCategoryIndex(entry.category());
        int accentColor = catIdx >= 0 ? CATEGORY_COLORS[catIdx] : TEXT_HEADER;
        UIHelper.drawShadowedText(g, this.font, entry.name(), contentLeft, y, accentColor);
        y += 14;

        // Divider
        UIHelper.drawHorizontalDivider(g, contentLeft, y, contentW);
        y += 6;

        // Detail lines
        for (String line : entry.detailLines()) {
            if (line.isEmpty()) {
                y += 6;
                continue;
            }
            if (line.startsWith("##")) {
                // Section header
                String header = line.substring(2).trim();
                y += 2;
                UIHelper.drawShadowedText(g, this.font, header, contentLeft, y, TEXT_HEADER);
                y += 12;
                continue;
            }
            if (line.startsWith("--")) {
                // Sub-divider
                g.fill(contentLeft, y + 2, contentLeft + contentW, y + 3, 0xFF3A3A40);
                y += 6;
                continue;
            }
            // Word wrap long lines
            List<String> wrapped = wrapText(line, contentW);
            for (String wl : wrapped) {
                int color = TEXT_NORMAL;
                if (wl.startsWith("  -") || wl.startsWith("  *")) {
                    color = TEXT_DIM;
                }
                g.drawString(this.font, wl, contentLeft, y, color, false);
                y += 10;
            }
        }

        int totalDetailH = y + detailScroll - contentTop;
        g.disableScissor();

        // Detail scrollbar
        int visibleH = detailBottom - detailTop - pad * 2;
        int maxDetailScroll = Math.max(0, totalDetailH - visibleH);
        detailScroll = Math.max(0, Math.min(detailScroll, maxDetailScroll));
        if (maxDetailScroll > 0) {
            float scrollProgress = (float) detailScroll / maxDetailScroll;
            UIHelper.drawScrollbar(g, detailRight - 8, detailTop + 2, panelH - 4, scrollProgress);
        }
    }

    private List<String> wrapText(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        if (this.font.width(text) <= maxWidth) {
            lines.add(text);
            return lines;
        }
        String[] words = text.split(" ");
        StringBuilder current = new StringBuilder();
        for (String word : words) {
            String test = current.isEmpty() ? word : current + " " + word;
            if (this.font.width(test) > maxWidth && !current.isEmpty()) {
                lines.add(current.toString());
                current = new StringBuilder(word);
            } else {
                current = new StringBuilder(test);
            }
        }
        if (!current.isEmpty()) {
            lines.add(current.toString());
        }
        return lines;
    }

    private String getUnlockHint(WikiEntry entry) {
        if (entry.id().startsWith("relic_") || entry.id().startsWith("weapon_")) {
            return "Equip or use this relic to unlock its entry.";
        } else if (entry.id().startsWith("mob_")) {
            return "Donate this mob to the Museum to unlock its entry.";
        } else if (entry.id().startsWith("dungeon_")) {
            return "Complete a dungeon of this tier to unlock its entry.";
        } else if (entry.id().startsWith("skill_")) {
            return "Invest points in this skill tree to unlock its entry.";
        } else if (entry.id().startsWith("item_")) {
            return "Obtain this item to unlock its entry.";
        }
        return null;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean consumed) {
        if (consumed) {
            return super.mouseClicked(event, consumed);
        }
        int mx = (int) event.x();
        int my = (int) event.y();

        // Back button
        if (mx >= backX && mx < backX + backW && my >= backY && my < backY + backH) {
            Minecraft.getInstance().setScreen(this.parent);
            return true;
        }

        // Category tabs
        for (int i = 0; i < CATEGORIES.length; i++) {
            int ty = tabStartY + i * (tabH + 2);
            if (mx >= tabX && mx < tabX + tabW && my >= ty && my < ty + tabH) {
                if (!CATEGORIES[i].equals(this.category)) {
                    this.category = CATEGORIES[i];
                    this.listScroll = 0;
                    this.selectedEntry = -1;
                    this.detailScroll = 0;
                    filterEntries();
                }
                return true;
            }
        }

        // Entry list clicks
        int listContentTop = listTop + 20;
        if (mx >= listLeft + 2 && mx < listRight - 2 && my >= listContentTop && my < listBottom - 1) {
            int clickedRow = (my - listContentTop) / ENTRY_ROW_H + listScroll;
            if (clickedRow >= 0 && clickedRow < filteredEntries.size()) {
                this.selectedEntry = clickedRow;
                this.detailScroll = 0;
            }
            return true;
        }

        return super.mouseClicked(event, consumed);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        // Scroll entry list
        if (mouseX >= listLeft && mouseX < listRight && mouseY >= listTop && mouseY < listBottom) {
            listScroll -= (int) scrollY;
            int listContentTop = listTop + 20;
            int visibleH = listBottom - listContentTop - 2;
            int maxVisible = visibleH / ENTRY_ROW_H;
            int maxScroll = Math.max(0, filteredEntries.size() - maxVisible);
            listScroll = Math.max(0, Math.min(listScroll, maxScroll));
            return true;
        }
        // Scroll detail panel
        if (mouseX >= detailLeft && mouseX < detailRight && mouseY >= detailTop && mouseY < detailBottom) {
            detailScroll += (int) (-scrollY * 12);
            detailScroll = Math.max(0, detailScroll);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();
        if (keyCode == 256) { // Escape
            Minecraft.getInstance().setScreen(this.parent);
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private int getCategoryIndex(String cat) {
        for (int i = 0; i < CATEGORIES.length; i++) {
            if (CATEGORIES[i].equals(cat)) return i;
        }
        return -1;
    }

    private int countEntriesInCategory(String cat) {
        int count = 0;
        for (WikiEntry e : allEntries) {
            if (e.category().equals(cat)) count++;
        }
        return count;
    }

    private int countDiscoveredInCategory(String cat) {
        int count = 0;
        for (WikiEntry e : allEntries) {
            if (e.category().equals(cat) && isDiscovered(e.id())) count++;
        }
        return count;
    }

    // ========================================================================
    // ENTRY DATA — All hardcoded entries
    // ========================================================================

    private void buildAllEntries() {
        allEntries.clear();
        buildGuideEntries();
        buildRelicEntries();
        buildWeaponEntries();
        buildMobEntries();
        buildDungeonEntries();
        buildSkillEntries();
        buildItemEntries();
        buildMuseumEntries();
        buildControlEntries();
        buildCitizenEntries();
        buildCasinoEntries();
        buildBackpackEntries();
        buildMarketplaceEntries();
        buildCorruptionEntries();
        buildAlchemyEntries();
        // QoL feature wiki entries
        allEntries.add(new WikiEntry("guide_sorting", "Sorting System", "Guide", List.of("##Inventory Sorting", "", "Sort any container. Press O or click sort button.", "Cycles: Name, ID, Category, Count, Rarity.", "Merges partial stacks. Works on all containers.")));
        allEntries.add(new WikiEntry("guide_death_recovery", "Death Recovery", "Guide", List.of("##Gravestone System", "", "Gravestone spawns on death holding ALL items.", "Right-click to retrieve (owner only).", "60-min despawn. Coords sent via Mail.", "Items persist across server restarts.")));
        allEntries.add(new WikiEntry("relic_lodestone_magnet", "Lodestone Magnet", "Relics", List.of("##Lodestone Magnet (Belt Relic)", "", "Attracts nearby items. 4 abilities:", "Lv1 Attraction, Lv3 Selective Pull,", "Lv5 Expanded Range, Lv7 Vacuum.")));
        allEntries.add(new WikiEntry("guide_map", "Map App Guide", "Guide", List.of("##World Map", "", "Block-level terrain, waypoints, drawings,", "chunk highlights, structures. Tools: Pan,", "Draw, Text, Waypoint, Measure. Cave view.", "3D beacons.")));
        allEntries.add(new WikiEntry("guide_spell_enchantments", "Spell Enchantments", "Guide", List.of(
            "##Spell Enchantments",
            "",
            "8 new enchantments that enhance spell-casting.",
            "Apply them to weapons at an enchanting table.",
            "",
            "--- Enchantments ---",
            "  Spell Power: Increases spell damage by 5-15%",
            "  Sunfire: +Fire damage to spells, chance to ignite",
            "  Soulfrost: +Ice damage to spells, chance to slow",
            "  Energize: +Lightning damage, chance to chain",
            "  Spell Crit Chance: +5-15% spell critical chance",
            "  Spell Crit Damage: +20-60% spell critical damage",
            "  Spell Haste: Reduces spell cooldowns by 5-15%",
            "  Magic Protection: Reduces incoming spell damage",
            "",
            "Enchantments stack with class bonuses and gear.",
            "Pair with Arcane tree nodes for maximum effect."
        )));
        allEntries.add(new WikiEntry("guide_stealth_system", "Stealth System", "Guide", List.of(
            "##Stealth System",
            "",
            "Enter stealth using the Vanish spell (Rogue class).",
            "",
            "--- How It Works ---",
            "  Cast Vanish to become invisible",
            "  Grants STEALTH + STEALTH_SPEED effects",
            "  Mobs cannot target you beyond 2 blocks",
            "  Very close mobs (< 2 blocks) can still detect you",
            "",
            "--- Breaking Stealth ---",
            "  Attacking any entity breaks stealth immediately",
            "  Both STEALTH and STEALTH_SPEED are removed",
            "  Use this to set up ambush attacks!",
            "",
            "Combine with Rogue Armor (+Crit, +Dodge) for",
            "devastating stealth-to-strike combos."
        )));
        allEntries.add(new WikiEntry("guide_exhaust_mechanic", "Exhaust Mechanic", "Guide", List.of(
            "##Exhaust Mechanic",
            "",
            "Powerful spells cost food/saturation to cast.",
            "",
            "--- How It Works ---",
            "  Each spell has an exhaust value (shown in tooltip)",
            "  Casting drains food points proportional to exhaust",
            "  If your food bar is too low, you cannot cast",
            "",
            "--- Tips ---",
            "  Keep food stocked for extended spell combat",
            "  Hunger Efficiency attribute reduces exhaust drain",
            "  Feeding backpack upgrade auto-eats for you",
            "  Paladin heal spells have low exhaust costs"
        )));
        allEntries.add(new WikiEntry("guide_dodge_system", "Dodge System", "Guide", List.of(
            "##Dodge System",
            "",
            "The DODGE_CHANCE attribute gives a % chance to",
            "completely avoid incoming damage.",
            "",
            "--- How It Works ---",
            "  When hit, the game rolls against your dodge %",
            "  A successful dodge negates ALL damage from the hit",
            "  A 'DODGE' combat text appears when triggered",
            "  A sweep sound plays as audio feedback",
            "",
            "--- Sources of Dodge Chance ---",
            "  Rogue Armor sets (+Dodge stat)",
            "  Archer Armor sets (+Dodge stat)",
            "  Skill tree nodes (Survival branch)",
            "  Accessory bonuses",
            "",
            "Dodge works against melee, ranged, and spell damage."
        )));
        allEntries.add(new WikiEntry("guide_raw_gems", "Raw Gems", "Guide", List.of(
            "##Raw Gems",
            "",
            "6 rare gem types found in dungeon loot.",
            "Used to craft jewelry (rings & necklaces).",
            "",
            "--- Gem Types ---",
            "  Ruby: Red gem, boosts fire-related stats",
            "  Topaz: Orange gem, boosts lightning stats",
            "  Citrine: Yellow gem, boosts holy stats",
            "  Jade: Green gem, boosts poison/nature stats",
            "  Sapphire: Blue gem, boosts ice/frost stats",
            "  Tanzanite: Purple gem, boosts shadow/arcane stats",
            "",
            "--- Where to Find ---",
            "  Dungeon treasure chests (all tiers)",
            "  Boss drops (higher tiers = better chance)",
            "  Resource Dimension mining (rare)",
            "",
            "Combine gems with gold at a crafting table",
            "to create stat-boosting jewelry pieces."
        )));
        allEntries.add(new WikiEntry("guide_spell_books", "Spell Books & Scrolls", "Guide", List.of(
            "##Spell Books & Scrolls",
            "---",
            "Spell Books (offhand) grant access to all spells of a school:",
            "  - Arcane Spell Book — all Arcane spells",
            "  - Fire Spell Book — all Fire spells",
            "  - Frost Spell Book — all Frost spells",
            "  - Healing Spell Book — all Healing spells",
            "",
            "Spell Scrolls are consumables that permanently teach one spell:",
            "  Scroll of Fireball, Frostbolt, Arcane Bolt, Heal,",
            "  Flash Heal, Shadow Step, Power Shot, Charge",
            "",
            "Books drop from Hard+ dungeons. Scrolls from Normal+ dungeons."
        )));
        allEntries.add(new WikiEntry("guide_quivers", "Quivers", "Guide", List.of(
            "##Quivers",
            "---",
            "Quivers are offhand items that boost ranged damage:",
            "  - Small Quiver: +5% Ranged Damage",
            "  - Medium Quiver: +10% Ranged Damage",
            "  - Large Quiver: +15% Ranged Damage",
            "",
            "Equip in your offhand alongside a bow or crossbow.",
            "Found in dungeon loot at all difficulty tiers."
        )));
        allEntries.add(new WikiEntry("guide_auto_fire", "Auto-Fire Hook", "Guide", List.of(
            "##Auto-Fire Hook",
            "---",
            "An offhand attachment for crossbows.",
            "When held in your offhand with a loaded crossbow in",
            "your main hand, the crossbow fires automatically.",
            "Found in Hard+ dungeon loot."
        )));
        allEntries.add(new WikiEntry("guide_spell_resistance", "Spell Resistance", "Guide", List.of(
            "##Spell Resistance",
            "---",
            "Elemental resistance attributes reduce incoming spell damage:",
            "  - Fire Resistance — reduces Fire spell damage",
            "  - Ice Resistance — reduces Frost spell damage",
            "  - Lightning Resistance — reduces Lightning spell damage",
            "  - Shadow Resistance — reduces Soul spell damage",
            "",
            "Resistance is a percentage reduction, capped at 75%.",
            "Sources: armor set bonuses, enchantments, potions."
        )));
        allEntries.add(new WikiEntry("guide_vulnerability", "Spell Vulnerability & Mob Weaknesses", "Guide", List.of(
            "##Spell Vulnerability & Mob Weaknesses",
            "---",
            "Certain effects increase spell damage taken:",
            "  - Frozen: +30% Frost damage taken",
            "  - Frostbite: +20% Frost damage taken",
            "  - Hunter's Mark: +15% ALL spell damage taken",
            "",
            "Mob-type weaknesses:",
            "  - Undead: +50% Healing, +20% Fire damage",
            "  - Blaze/Magma Cube: -50% Fire, +30% Frost",
            "  - Snow/Ice mobs: -50% Frost, +30% Fire",
            "  - Ender mobs: +20% Arcane damage",
            "  - Warden: -30% ALL spell damage"
        )));
    }

    // ---- GUIDE ----

    private void buildGuideEntries() {
        allEntries.add(new WikiEntry("guide_welcome", "Welcome to MegaMod", "Guide", List.of(
            "##Welcome to MegaMod!",
            "",
            "MegaMod is an all-in-one mod that adds economy,",
            "relics, dungeons, colonies, skills, a museum,",
            "arenas, alchemy, a player marketplace, corruption",
            "events, and 34 quality-of-life improvements.",
            "",
            "This wiki covers everything. Start with the",
            "entries in this Guide tab, then explore the",
            "other categories as you discover new content.",
            "",
            "##Your First Steps",
            "  1. Craft a Computer (your main hub)",
            "  2. Earn MegaCoins by killing mobs",
            "  3. Open the Computer and explore the apps",
            "  4. Press V to open the Accessory panel",
            "  5. Press K to open the Skill Tree",
            "",
            "##Key Items to Craft Early",
            "  - Computer: Access all MegaMod systems",
            "  - ATM: Quick bank access anywhere",
            "  - Dungeon Keys: Enter roguelike dungeons",
            "  - Mob Net: Catch mobs for your museum"
        )));

        allEntries.add(new WikiEntry("guide_keybinds", "Keybinds", "Guide", List.of(
            "##Essential Keybinds",
            "",
            "  V - Open Accessories panel",
            "  K - Open Skill Tree",
            "  R - Cast primary ability / cycle abilities",
            "  G - Cast secondary ability / cycle slot",
            "",
            "##Accessories (V key)",
            "  Equip relics in 10 slots: Head, Face,",
            "  Necklace, Back, Hands L/R, Belt,",
            "  Ring L/R, and Feet.",
            "",
            "##Abilities",
            "  Hold R + Scroll: cycle weapon abilities",
            "  Tap R: cast selected ability",
            "  Hold G + Scroll: cycle accessory slots",
            "  Tap G: cast accessory ability",
            "",
            "##Computer",
            "  Right-click a placed Computer block",
            "  to open the desktop with 20 apps."
        )));

        allEntries.add(new WikiEntry("guide_economy", "Economy & Coins", "Guide", List.of(
            "##MegaCoins (MC)",
            "",
            "The universal currency for everything in MegaMod.",
            "",
            "##Earning Coins",
            "  - Kill mobs (scales with difficulty)",
            "  - Complete dungeons (boss loot + rewards)",
            "  - Marketplace (sell items to other players via WTS)",
            "  - Bounty Hunting (kill named mobs for 30-500 MC)",
            "  - Corruption Purges (tier x 50 MC per purge)",
            "  - Arena PvE (wave rewards) & PvP (win rewards)",
            "  - Weekly Challenges (+20 MC per completed)",
            "  - Merchant citizens (passive 10% sales income)",
            "",
            "##Wallet vs Bank",
            "  Wallet: coins on you. Lost on death!",
            "  Bank: safe storage. Never lost.",
            "  Use the Bank app or craft an ATM block.",
            "",
            "##Spending Coins",
            "  - MegaShop: 60-item daily rotating catalog",
            "  - Hire citizens (workers & recruits)",
            "  - Citizen upkeep (daily salary costs)",
            "  - Marketplace (buy items via WTB with escrow)",
            "  - Player Marketplace (Trading Terminal trades)",
            "  - Casino games (Blackjack, Wheel, etc.)",
            "  - Skill Prestige (100-500 MC per prestige)"
        )));

        allEntries.add(new WikiEntry("guide_computer", "The Computer", "Guide", List.of(
            "##Your Hub for Everything",
            "",
            "Craft and place a Computer block, then right-",
            "click it to open the MegaMod desktop.",
            "",
            "##22 Apps Available",
            "  Shop - Daily rotating item shop + furniture",
            "  Bank - Deposit/withdraw MegaCoins",
            "  Stats - Your gameplay statistics",
            "  Skills - Quick link to the Skill Tree",
            "  Recipes - Browse all crafting recipes",
            "  Wiki - This encyclopedia",
            "  Music - In-game music player",
            "  Games - Minigames (Minesweeper, etc.)",
            "  Notes - Personal notepad",
            "  Map - World map with waypoints",
            "  Friends - Manage friends, visit museums",
            "  Ranks - Server leaderboards",
            "  Mail - Send mail, items & messages",
            "  Party - Form 2-4 player dungeon parties",
            "  Settings - Toggle HUD/FX/gameplay prefs",
            "  Market - Buy & sell with players",
            "  Bounties - Hunt mobs for rewards",
            "  Town - Colony management dashboard",
            "  Casino - Gambling games",
            "  Challenges - Weekly skill challenges",
            "  Arena - PvE waves & PvP combat",
            "  Customize - Badge, name color & mastery"
        )));

        allEntries.add(new WikiEntry("guide_relics", "Relics & Accessories", "Guide", List.of(
            "##What Are Relics?",
            "",
            "Powerful equippable items with unique abilities.",
            "Press V to open the Accessory panel.",
            "",
            "##10 Equip Slots",
            "  Head, Face, Necklace, Back, Hands L/R,",
            "  Belt, Ring L/R, Feet",
            "",
            "##30 Relics + 10 RPG Weapons",
            "  Each relic grants passive bonuses and",
            "  1-2 castable abilities (R or G key).",
            "",
            "##How to Get Relics",
            "  - Dungeon boss drops and chest loot",
            "  - Crafting (some relics)",
            "  - MegaShop purchases",
            "",
            "##Ability Types",
            "  PASSIVE: Always active when equipped",
            "  INSTANTANEOUS: One-shot cast",
            "  TOGGLE: Activate/deactivate",
            "",
            "##Combat Combos",
            "  Chain two abilities with matching elemental tags",
            "  within 8 seconds for powerful combo effects!",
            "  See 'Combat Combos' entry for all 10 combos.",
            "",
            "See the Relics tab for full details on each."
        )));

        allEntries.add(new WikiEntry("guide_skills", "Skill Tree", "Guide", List.of(
            "##Press K to Open",
            "",
            "5 skill trees with 25 branches and 100 nodes.",
            "",
            "##The 5 Trees",
            "  Combat - Blade, Ranged, Shield, Berserker",
            "  Mining - Ore Finding, Vein Seeking, Fortune",
            "  Farming - Crops, Animals, Harvesting",
            "  Arcane - Spells, Summoning, Colony bonuses",
            "  Survival - Navigation, Foraging, Stamina",
            "",
            "##How It Works",
            "  - Earn XP by doing related activities",
            "  - Spend Skill Points on tree nodes",
            "  - Each node grants stat bonuses",
            "  - Tier 5 capstones unlock special powers",
            "  - Drag to pan, scroll to zoom the tree",
            "",
            "##Colony Bonuses (Arcane Tree)",
            "  - Summoner branch: +citizen capacity",
            "  - Mana Weaver branch: -upkeep costs",
            "  - Reduced hire costs"
        )));

        allEntries.add(new WikiEntry("guide_dungeons", "Dungeons", "Guide", List.of(
            "##Roguelike Dungeon System",
            "",
            "Procedurally generated dungeons in a pocket",
            "dimension with 8 bosses and 19 mob types.",
            "",
            "##6 Difficulty Tiers",
            "  Normal - 5-7 rooms, 1x difficulty",
            "  Hard - 6-8 rooms, 1.5x difficulty",
            "  Nightmare - 7-10 rooms, 2.5x difficulty",
            "  Infernal - 9-13 rooms, 4x difficulty",
            "  Mythic - 11-15 rooms, 6x (New Game+)",
            "  Eternal - 13-18 rooms, 10x (New Game+)",
            "",
            "##How to Enter",
            "  1. Accept a quest from the Royal Herald",
            "  2. Right-click to use the key",
            "  3. Clear rooms and defeat the boss",
            "  4. Step into the return portal to exit",
            "",
            "##Party Dungeons",
            "  Form a party (Party app, max 4 players)",
            "  and all members enter together!",
            "",
            "##Loot",
            "  Relics, void shards, essences, trophies,",
            "  and MegaCoins await inside."
        )));

        allEntries.add(new WikiEntry("guide_citizens", "Citizens & Colonies", "Guide", List.of(
            "##Colony Management",
            "",
            "Build and manage your own colony!",
            "",
            "##Getting Started",
            "  1. Craft a Colony Hall and place it",
            "  2. Claims a 64-block radius territory",
            "  3. Build 35+ building types (levels 1-5)",
            "  4. Hire citizens with MegaCoins",
            "",
            "##Two Ways to Hire Citizens",
            "  1. Right-click a Villager (full price)",
            "  2. Find wandering citizens (half price!)",
            "",
            "##57 Job Types",
            "  Workers: Farmer, Miner, Lumberjack, etc.",
            "  Recruits: Bowman, Shieldman, Horseman, etc.",
            "",
            "##Every Citizen Needs",
            "  - A bed for sleeping",
            "  - A chest for tools and output",
            "  - An upkeep chest with food",
            "  - A work position",
            "",
            "##Colony Features",
            "  - Research tree unlocks upgrades",
            "  - Quest system for colony objectives",
            "  - War system for colony vs colony",
            "  - Logistics network between buildings",
            "  - Daily upkeep costs MC from your bank",
            "",
            "See the Citizens tab for full details."
        )));

        allEntries.add(new WikiEntry("guide_museum", "Museum", "Guide", List.of(
            "##Collect and Display",
            "",
            "An Animal Crossing-inspired collection system.",
            "",
            "##How It Works",
            "  1. Craft a Mob Net",
            "  2. Throw it at mobs to capture them",
            "  3. Visit your Museum via Friends app",
            "  4. Display captured mobs on pedestals",
            "",
            "##5 Catalog Wings",
            "  Each wing has items to discover and donate.",
            "  Complete catalogs for bragging rights!",
            "",
            "##32 Masterpiece Paintings",
            "  Discover and collect unique paintings.",
            "",
            "##Social",
            "  Friends can visit each other's museums",
            "  using the Friends app button."
        )));

        allEntries.add(new WikiEntry("guide_vanilla_refresh", "Vanilla Refresh (QoL)", "Guide", List.of(
            "##34 Quality-of-Life Features",
            "",
            "MegaMod enhances vanilla Minecraft with:",
            "",
            "  - Player Sitting (crouch on stairs/slabs)",
            "  - Path Sprinting (faster on paths)",
            "  - Equipable Banners (wear as backpack)",
            "  - Totem in Void (saves you from void)",
            "  - Head Drops (mobs drop heads on kill)",
            "  - Death Sounds (custom sound effects)",
            "  - Mob Health HUD (see health bars)",
            "  - Homing XP (orbs come to you)",
            "  - Readable Clocks (show in-game time)",
            "  - Better Armor Stands (more poses)",
            "  - Drop Ladders (place ladders downward)",
            "  - Invisible Item Frames",
            "  - Better Lodestones",
            "  - Villager Trade Refresh",
            "  - Craft Sounds, Block Animations, more",
            "",
            "All features can be toggled on/off",
            "in the Settings app on your Computer."
        )));

        allEntries.add(new WikiEntry("guide_progression", "Progression Path", "Guide", List.of(
            "##Suggested Progression",
            "",
            "##Early Game",
            "  - Craft a Computer and ATM",
            "  - Earn MegaCoins (kill mobs, mine ores)",
            "  - Deposit coins in your bank",
            "  - Buy gear from the MegaShop",
            "  - Press V to check for relic drops",
            "",
            "##Mid Game",
            "  - Open the Skill Tree (K) and invest points",
            "  - Get Dungeon Keys from the Royal Herald",
            "  - Collect relics and build your loadout",
            "  - Start a Museum (craft Mob Net)",
            "  - Hire your first citizen workers",
            "",
            "##Late Game",
            "  - Party up for Hard/Nightmare dungeons",
            "  - Max out a Skill Tree branch",
            "  - Build a colony with 50 citizens",
            "  - Complete all Museum catalogs",
            "  - Form factions and claim territory",
            "",
            "##Endgame",
            "  - Infernal dungeons (4x difficulty)",
            "  - All 30 relics collected",
            "  - All 5 Skill Tree capstones",
            "  - Colony wars and sieges",
            "  - Prestige skill trees for permanent bonuses",
            "  - Hunt Elite & Champion mobs in the wild",
            "  - Master combat combos (chain abilities)",
            "  - Complete weekly challenges for extra rewards",
            "",
            "##Post-Endgame (New Game+)",
            "  - Prestige all trees to unlock Mythic tier",
            "  - Clear Mythic dungeons (6x difficulty)",
            "  - Reach Prestige 15+ for Eternal tier (10x)",
            "  - Boss Rush: fight all 8 bosses back-to-back",
            "  - PvE Arena: survive 50+ waves",
            "  - PvP Arena: climb the ELO rankings",
            "  - Accept bounty hunts for named mob targets"
        )));

        allEntries.add(new WikiEntry("guide_furniture", "Furniture Shop", "Guide", List.of(
            "##Furniture System",
            "",
            "MegaMod includes 275+ decorative furniture blocks.",
            "",
            "##Categories",
            "  - Office, Vintage, Classic, Coffee Shop",
            "  - Market (2 sets), Casino (2 sets)",
            "  - Dungeon, Farmer, Caribbean Vacation",
            "  - Master Bedroom, Modern",
            "",
            "##How to Get Furniture",
            "  - Computer > Shop > Furniture tab",
            "  - Buy with MegaCoins (10-70 MC each)",
            "  - All furniture rotates with placement direction",
            "",
            "##Features",
            "  - Decorative only (no function, just looks)",
            "  - Lamps emit light",
            "  - Trash Bin has chest storage",
            "  - All appear in MegaMod Furniture creative tab"
        )));

        allEntries.add(new WikiEntry("guide_insurance", "Dungeon Insurance", "Guide", List.of(
            "##Dungeon Insurance System",
            "",
            "Protect your gear when entering dungeons.",
            "",
            "##How It Works",
            "  1. Use a Dungeon Key to start a dungeon",
            "  2. Insurance GUI appears before entry",
            "  3. Select slots to insure (armor, weapons, relics)",
            "  4. Each slot costs MegaCoins (varies by tier)",
            "  5. Insured items are saved if you die",
            "",
            "##Buttons",
            "  - Insure & Ready: Pay and enter",
            "  - Skip: Enter with no insurance",
            "  - Cancel: Abort dungeon entry",
            "",
            "##Party Insurance",
            "  Each player insures independently.",
            "  All must ready up before dungeon starts.",
            "",
            "##Soul Anchor",
            "  Alternative to insurance. A consumable item",
            "  that saves ALL items on death. Found in chests."
        )));

        allEntries.add(new WikiEntry("guide_party", "Party System", "Guide", List.of(
            "##Party System",
            "",
            "Team up with up to 3 other players.",
            "",
            "##Creating a Party",
            "  Computer > Party app > Create Party",
            "  Invite players by clicking their name.",
            "",
            "##Party Features",
            "  - Max 4 players per party",
            "  - Party enters dungeons together",
            "  - All members share dungeon instance",
            "  - Party disbands when leader logs out",
            "",
            "##Dungeon Parties",
            "  When the party leader uses a Dungeon Key,",
            "  all members get the insurance screen and",
            "  teleport into the same dungeon together.",
            "",
            "##Party Buffs (Skill Tree)",
            "  The Tactician branch (Combat tree) grants",
            "  party-wide buffs when in a group."
        )));

        allEntries.add(new WikiEntry("guide_bounty", "Bounty Board", "Guide", List.of(
            "##Bounty Board",
            "",
            "Post requests for items — other players fill them.",
            "",
            "##Posting a Bounty",
            "  Computer > Bounty app > Post Bounty",
            "  Set item, quantity, and price per item.",
            "  Coins are held in escrow from your bank.",
            "",
            "##Filling a Bounty",
            "  Browse open bounties in the Bounty app.",
            "  Click Fill — items are taken, coins awarded.",
            "",
            "##Rules",
            "  - Max 5 active bounties per player",
            "  - Bounties expire after 24 hours",
            "  - Min price enforced per item type",
            "  - Unfilled bounties refund escrow on expiry",
            "",
            "##Tracker HUD",
            "  Active bounties show on the right side of",
            "  your screen during gameplay."
        )));

        allEntries.add(new WikiEntry("guide_arena", "Arena", "Guide", List.of(
            "##Arena System",
            "",
            "Three combat arenas accessible via the",
            "Computer Party app.",
            "",
            "##PvE Wave Arena",
            "  Survive endless waves of mobs in an arena.",
            "  Difficulty scales each wave. Earn MegaCoins",
            "  and loot as rewards for surviving.",
            "  Checkpoint every 5 waves to bank progress.",
            "",
            "##PvP Arena",
            "  1v1 duels against other players.",
            "  ELO-based matchmaking and rankings.",
            "  Win rewards scale with your rank.",
            "",
            "##Boss Rush",
            "  Fight all 8 dungeon bosses back-to-back.",
            "  Timed leaderboard — compete for fastest clear.",
            "  No healing between fights. Ultimate test of skill."
        )));

        allEntries.add(new WikiEntry("guide_alchemy", "Alchemy", "Guide", List.of(
            "##Alchemy System",
            "",
            "Brew powerful potions and grind reagents.",
            "",
            "##Stations",
            "  Alchemy Cauldron: Brew potions from reagents",
            "  Alchemy Grindstone: Grind items into reagents",
            "  Alchemy Shelf: Store reagents (decorative)",
            "",
            "##Reagents",
            "  Ember Dust, Frost Crystal, Shadow Essence,",
            "  and more. Obtained from grinding items or",
            "  found in dungeon chests.",
            "",
            "##Recipes",
            "  Combine reagents in the Cauldron to brew",
            "  potions. Higher tier recipes need rarer",
            "  reagents. See the Alchemy tab for full list."
        )));

        allEntries.add(new WikiEntry("guide_corruption", "Corruption", "Guide", List.of(
            "##Corruption System",
            "",
            "Dark zones that spread and corrupt the world.",
            "",
            "##How It Works",
            "  Corruption zones spawn randomly and spread",
            "  over time, converting blocks and spawning",
            "  corrupted mobs.",
            "",
            "##Effects",
            "  - Debuffs while inside corruption zones",
            "  - Corrupted mobs are tougher and hostile",
            "  - Blocks change to corrupted variants",
            "",
            "##Defense",
            "  - Build purification blocks to slow spread",
            "  - Complete corruption purges for MC rewards",
            "  - Colony defenses can protect your territory",
            "",
            "See the Corruption tab for full details."
        )));

        allEntries.add(new WikiEntry("guide_marketplace", "Marketplace", "Guide", List.of(
            "##Player Marketplace",
            "",
            "Trade items with other players using the",
            "Trading Terminal block or Computer apps.",
            "",
            "##WTS (Want To Sell)",
            "  List items for sale at your price.",
            "  Other players browse and buy from you.",
            "",
            "##WTB (Want To Buy)",
            "  Post buy orders for items you need.",
            "  Coins held in escrow until filled.",
            "",
            "##Trading Terminal",
            "  Craft and place for direct access to",
            "  the marketplace without a Computer.",
            "",
            "##Trade App",
            "  Player-to-player direct trading.",
            "  Both players confirm before items swap.",
            "  120-second timeout per trade session."
        )));

        allEntries.add(new WikiEntry("guide_backpacks", "Backpacks", "Guide", List.of(
            "##Backpack System",
            "",
            "Extra portable storage with special features.",
            "",
            "##Resource Dimension",
            "  Special backpacks can access a personal",
            "  resource dimension — a flat mining world",
            "  just for gathering materials.",
            "",
            "##Insurance",
            "  Some backpacks come with item insurance.",
            "  Insured items are returned on death",
            "  (separate from dungeon insurance).",
            "",
            "##Getting Backpacks",
            "  Found in dungeon chests, the MegaShop,",
            "  or crafted with rare materials.",
            "",
            "See the Backpacks tab for full details."
        )));

        allEntries.add(new WikiEntry("guide_casino", "Casino", "Guide", List.of(
            "##Casino Games",
            "",
            "Gamble your MegaCoins in the Casino app!",
            "",
            "##Games Available",
            "  Slots: Match symbols for multiplied payouts",
            "  Blackjack: Classic 21 against the dealer",
            "  Wheel: Spin for random prize tiers",
            "",
            "##How to Play",
            "  Open the Casino app on your Computer.",
            "  Select a game and place your bet.",
            "  Winnings go straight to your wallet.",
            "",
            "##Stats",
            "  The Casino tracks your total wagered,",
            "  won, lost, and biggest single win.",
            "  Compete on the Casino leaderboard!"
        )));

        allEntries.add(new WikiEntry("guide_customize", "Customize App", "Guide", List.of(
            "##Customize Your Identity",
            "",
            "Open the Customize app on your Computer",
            "to personalize your badge, name color,",
            "and spend Mastery Marks.",
            "",
            "##Badge Tab",
            "  Choose a custom badge title from any",
            "  skill branch you've reached Tier 3+.",
            "  Pick from 14 badge colors.",
            "  Or leave on Auto to use your highest branch.",
            "",
            "##Name Color Tab",
            "  Requires prestige 5+ to unlock.",
            "  Choose from 12 name colors for chat",
            "  and the tab list.",
            "  Default: Yellow (5+), Gold (15+), Red (25+).",
            "",
            "##Mastery Marks Tab",
            "  View your mark balance and spend on:",
            "  - Permanent +5% Coin Drops (100 marks)",
            "  - Permanent +5% XP Gain (100 marks)",
            "  - Exclusive Furniture Crate (30 marks)",
            "  Requires at least 1 prestige to unlock."
        )));

        allEntries.add(new WikiEntry("guide_mastery", "Mastery Marks", "Guide", List.of(
            "##Marks of Mastery",
            "",
            "A prestige currency earned from major",
            "milestones across all systems.",
            "",
            "##How to Earn",
            "  Clear Normal Dungeon: +5 marks",
            "  Clear Hard Dungeon: +10 marks",
            "  Clear Nightmare Dungeon: +20 marks",
            "  Clear Infernal Dungeon: +40 marks",
            "  Prestige a Skill Tree: +25 marks",
            "  Complete Museum Wing: +15 marks",
            "  100% Museum Completion: +50 marks",
            "  Fill 50 Bounties: +10 marks",
            "  Fill 100 Bounties: +20 marks",
            "  50 Colony Citizens: +15 marks",
            "  Win a Siege: +10 marks",
            "",
            "##Unlocks at Prestige 1",
            "  You must prestige at least one skill",
            "  tree before you can view or spend marks.",
            "  Prestige at least one tree to begin!"
        )));

        allEntries.add(new WikiEntry("guide_compass", "Compass HUD", "Guide", List.of(
            "##Compass Bar",
            "",
            "A minimal direction compass is shown at",
            "the top center of your screen.",
            "",
            "  N (blue) = North",
            "  S (red) = South",
            "  E, W (white) = East, West",
            "  NE, NW, SE, SW = intercardinals",
            "",
            "The compass slides as you turn, with",
            "tick marks every 15 degrees.",
            "A thin white center line shows your",
            "exact heading.",
            "",
            "Hidden when GUI is toggled off (F1)."
        )));

        allEntries.add(new WikiEntry("guide_prestige", "Skill Prestige", "Guide", List.of(
            "##Prestige System",
            "",
            "After maxing a skill tree, you can prestige",
            "it to reset and earn permanent bonuses.",
            "",
            "##Benefits",
            "  +5% skill bonus per prestige level",
            "  Prestige 3+: unlocks third branch slot",
            "  Prestige stars in chat (up to 25)",
            "  Name color changes at 5/15/25 prestige",
            "  Particle auras at prestige milestones",
            "",
            "##Cosmetics Unlocked",
            "  Prestige 1+: Golden totem aura particles",
            "  Prestige 5+: Custom name color options",
            "  Prestige 25+: Orbiting END_ROD aura",
            "  Prestige 25+: Bold red name in chat",
            "",
            "##Mastery Marks",
            "  Each prestige awards 25 Mastery Marks.",
            "  Spend marks in the Customize app."
        )));
    }

    // ---- RELICS (30) ----

    private void buildRelicEntries() {
        // Back slot (3)
        allEntries.add(new WikiEntry("relic_arrow_quiver", "Arrow Quiver", "Relics", List.of(
            "##Slot: Back",
            "",
            "A magical quiver that automatically collects",
            "nearby arrows and enhances ranged combat.",
            "",
            "##Passive",
            "  - Auto-collect nearby arrows within 5 blocks",
            "  - +10% Arrow Damage",
            "  - +2 Agility",
            "",
            "##Ability 1: Leap",
            "  - Cast Type: Instantaneous",
            "  - Launch yourself high into the air",
            "  - Cooldown: 8 seconds",
            "",
            "##Ability 2: Arrow Rain",
            "  - Cast Type: Instantaneous",
            "  - Rain a volley of arrows in the target area,",
            "    dealing damage to all enemies within range",
            "  - Cooldown: 20 seconds"
        )));
        allEntries.add(new WikiEntry("relic_elytra_booster", "Elytra Booster", "Relics", List.of(
            "##Slot: Back",
            "",
            "A rocket-powered harness that supercharges",
            "elytra flight with explosive speed.",
            "",
            "##Passive",
            "  - +15% Glide efficiency",
            "  - Reduced firework fuel consumption",
            "",
            "##Ability 1: Boost",
            "  - Cast Type: Instantaneous",
            "  - Surge forward at extreme speed while gliding",
            "  - Cooldown: 6 seconds",
            "",
            "##Ability 2: Fuel Save",
            "  - Cast Type: Toggle",
            "  - While active, firework rockets last 50% longer",
            "  - Drains hunger slowly while active"
        )));
        allEntries.add(new WikiEntry("relic_midnight_robe", "Midnight Robe", "Relics", List.of(
            "##Slot: Back",
            "",
            "A cloak woven from shadows that grants stealth",
            "abilities and empowers sneak attacks.",
            "",
            "##Passive",
            "  - +20% Sneak attack damage",
            "  - Mobs have reduced detection range at night",
            "",
            "##Ability 1: Vanish",
            "  - Cast Type: Instantaneous",
            "  - Become invisible for 5 seconds.",
            "    Breaking stealth with an attack deals 2x damage.",
            "  - Cooldown: 15 seconds",
            "",
            "##Ability 2: Shadow Step",
            "  - Cast Type: Instantaneous",
            "  - Teleport behind the nearest hostile mob",
            "    within 10 blocks",
            "  - Cooldown: 12 seconds"
        )));

        // Belt slot (3)
        allEntries.add(new WikiEntry("relic_leather_belt", "Leather Belt", "Relics", List.of(
            "##Slot: Belt",
            "",
            "A sturdy leather belt that increases toughness",
            "and endurance for long adventures.",
            "",
            "##Passive",
            "  - +2 Armor Toughness",
            "  - +10% Knockback Resistance",
            "  - +1 extra inventory row (carry weight)",
            "",
            "##Ability 1: Endurance",
            "  - Cast Type: Toggle",
            "  - Reduce incoming damage by 15% but move 10% slower",
            "",
            "##Ability 2: Brace",
            "  - Cast Type: Instantaneous",
            "  - Gain full knockback immunity for 4 seconds",
            "  - Cooldown: 20 seconds"
        )));
        allEntries.add(new WikiEntry("relic_drowned_belt", "Drowned Belt", "Relics", List.of(
            "##Slot: Belt",
            "",
            "A waterlogged belt infused with oceanic power.",
            "Grants aquatic abilities and water breathing.",
            "",
            "##Passive",
            "  - Permanent Water Breathing",
            "  - +30% Swim Speed",
            "",
            "##Ability 1: Pressure AOE",
            "  - Cast Type: Instantaneous",
            "  - Release a shockwave of water pressure,",
            "    pushing all nearby entities away",
            "  - Cooldown: 14 seconds",
            "",
            "##Ability 2: Depth Charge",
            "  - Cast Type: Instantaneous",
            "  - Propel downward rapidly and create an",
            "    explosion of bubbles on impact",
            "  - Cooldown: 18 seconds"
        )));
        allEntries.add(new WikiEntry("relic_hunter_belt", "Hunter Belt", "Relics", List.of(
            "##Slot: Belt",
            "",
            "A utility belt for expert trackers. Enhances",
            "resource gathering and mob awareness.",
            "",
            "##Passive",
            "  - +20% mob loot drops",
            "  - Nearby hostile mobs glow through walls (8 blocks)",
            "",
            "##Ability 1: Tracker",
            "  - Cast Type: Instantaneous",
            "  - Highlight all mobs within 32 blocks with",
            "    Glowing effect for 10 seconds",
            "  - Cooldown: 25 seconds",
            "",
            "##Ability 2: Predator Mark",
            "  - Cast Type: Instantaneous",
            "  - Mark a target mob. Attacks against the marked",
            "    mob deal +30% damage for 8 seconds",
            "  - Cooldown: 20 seconds"
        )));

        // Necklace slot (3)
        allEntries.add(new WikiEntry("relic_reflection_necklace", "Reflection Necklace", "Relics", List.of(
            "##Slot: Necklace",
            "",
            "A crystalline pendant that absorbs incoming damage",
            "and releases it as a devastating explosion.",
            "",
            "##Passive",
            "  - Absorb 10% of all incoming damage as stored energy",
            "",
            "##Ability 1: Absorb",
            "  - Cast Type: Toggle",
            "  - While active, absorb 30% of damage taken.",
            "    Stored energy caps at 50 HP worth of damage.",
            "",
            "##Ability 2: Explode Release",
            "  - Cast Type: Instantaneous",
            "  - Release all stored energy as an explosion,",
            "    dealing stored damage to all nearby enemies",
            "  - Cooldown: 10 seconds"
        )));
        allEntries.add(new WikiEntry("relic_jellyfish_necklace", "Jellyfish Necklace", "Relics", List.of(
            "##Slot: Necklace",
            "",
            "A bioluminescent necklace pulsing with electric",
            "energy from deep-sea jellyfish.",
            "",
            "##Passive",
            "  - Slow falling when sneaking in the air",
            "  - Emit light particles while underwater",
            "",
            "##Ability 1: Shock AOE",
            "  - Cast Type: Instantaneous",
            "  - Release an electric pulse dealing damage and",
            "    Slowness II to all entities within 6 blocks",
            "  - Cooldown: 12 seconds",
            "",
            "##Ability 2: Paralysis",
            "  - Cast Type: Instantaneous",
            "  - Stun the nearest enemy for 3 seconds,",
            "    preventing all movement and attacks",
            "  - Cooldown: 22 seconds"
        )));
        allEntries.add(new WikiEntry("relic_holy_locket", "Holy Locket", "Relics", List.of(
            "##Slot: Necklace",
            "",
            "A sacred golden locket that channels healing",
            "power and purification magic.",
            "",
            "##Passive",
            "  - Heal steal: 5% of damage dealt restores HP",
            "  - +10% Healing received",
            "",
            "##Ability 1: Purify",
            "  - Cast Type: Instantaneous",
            "  - Remove all negative status effects from yourself",
            "  - Cooldown: 30 seconds",
            "",
            "##Ability 2: Sanctify",
            "  - Cast Type: Instantaneous",
            "  - Create a healing zone for 8 seconds that",
            "    restores 1 HP/sec to all players within 5 blocks",
            "  - Cooldown: 45 seconds"
        )));

        // Feet slot (6)
        allEntries.add(new WikiEntry("relic_magma_walker", "Magma Walker", "Relics", List.of(
            "##Slot: Feet",
            "",
            "Boots forged in the Nether that grant mastery",
            "over fire and lava.",
            "",
            "##Passive",
            "  - Permanent Fire Resistance",
            "  - Walk on lava (converts to magma blocks)",
            "",
            "##Ability 1: Lava Walk",
            "  - Cast Type: Toggle",
            "  - Convert lava to obsidian in a wider radius",
            "    while walking (5 block radius)",
            "",
            "##Ability 2: Eruption",
            "  - Cast Type: Instantaneous",
            "  - Erupt a pillar of lava beneath the targeted",
            "    area, dealing massive fire damage",
            "  - Cooldown: 25 seconds"
        )));
        allEntries.add(new WikiEntry("relic_aqua_walker", "Aqua Walker", "Relics", List.of(
            "##Slot: Feet",
            "",
            "Enchanted sandals that let you walk on water",
            "and manipulate ocean currents.",
            "",
            "##Passive",
            "  - Walk on water surfaces",
            "  - No movement penalty in water",
            "",
            "##Ability 1: Ripple Push",
            "  - Cast Type: Instantaneous",
            "  - Send a wave outward that pushes all entities",
            "    away from you within 8 blocks",
            "  - Cooldown: 10 seconds",
            "",
            "##Ability 2: Tidal Dash",
            "  - Cast Type: Instantaneous",
            "  - Dash forward on a wave of water, damaging",
            "    enemies in your path",
            "  - Cooldown: 14 seconds"
        )));
        allEntries.add(new WikiEntry("relic_ice_skates", "Ice Skates", "Relics", List.of(
            "##Slot: Feet",
            "",
            "Crystal-bladed skates that grant incredible",
            "speed on frozen surfaces.",
            "",
            "##Passive",
            "  - +50% movement speed on ice and packed ice",
            "  - No slip on ice blocks",
            "",
            "##Ability 1: Ice Trail",
            "  - Cast Type: Toggle",
            "  - Leave a trail of ice behind you as you walk,",
            "    freezing water and creating paths",
            "",
            "##Ability 2: Flash Freeze",
            "  - Cast Type: Instantaneous",
            "  - Freeze all water within 10 blocks into ice",
            "  - Cooldown: 20 seconds"
        )));
        allEntries.add(new WikiEntry("relic_ice_breaker", "Ice Breaker", "Relics", List.of(
            "##Slot: Feet",
            "",
            "Heavy frost-forged boots designed to shatter",
            "ice and freeze enemies with each step.",
            "",
            "##Passive",
            "  - +15% melee damage on frozen/snowy terrain",
            "  - Frost Thorns: attackers take frost damage",
            "",
            "##Ability 1: Stomp Freeze",
            "  - Cast Type: Instantaneous",
            "  - Stomp the ground to freeze all nearby mobs",
            "    in place for 3 seconds (8 block radius)",
            "  - Cooldown: 18 seconds",
            "",
            "##Ability 2: Frost Nova",
            "  - Cast Type: Instantaneous",
            "  - Release a burst of frost dealing damage",
            "    and applying Slowness III to all nearby enemies",
            "  - Cooldown: 22 seconds"
        )));
        allEntries.add(new WikiEntry("relic_roller_skates", "Roller Skates", "Relics", List.of(
            "##Slot: Feet",
            "",
            "Enchanted wheels that build momentum the",
            "longer you run, reaching incredible speeds.",
            "",
            "##Passive",
            "  - Speed increases over time while running",
            "    (up to +40% after 5 seconds of sprinting)",
            "  - Speed resets when you stop",
            "",
            "##Ability 1: Momentum Burst",
            "  - Cast Type: Instantaneous",
            "  - Instantly gain maximum speed momentum",
            "  - Cooldown: 15 seconds",
            "",
            "##Ability 2: Double Jump",
            "  - Cast Type: Instantaneous",
            "  - Jump again while in mid-air",
            "  - Cooldown: 3 seconds"
        )));
        allEntries.add(new WikiEntry("relic_amphibian_boot", "Amphibian Boot", "Relics", List.of(
            "##Slot: Feet",
            "",
            "Webbed boots designed for both land and water,",
            "inspired by frog biology.",
            "",
            "##Passive",
            "  - +40% Swim Speed",
            "  - No movement penalty in water",
            "",
            "##Ability 1: Frog Jump",
            "  - Cast Type: Instantaneous",
            "  - Perform a powerful frog-like leap, launching",
            "    yourself forward and upward",
            "  - Cooldown: 5 seconds",
            "",
            "##Ability 2: Tongue Lash",
            "  - Cast Type: Instantaneous",
            "  - Pull the targeted mob toward you from up to",
            "    12 blocks away",
            "  - Cooldown: 10 seconds"
        )));

        // Hands slot (3)
        allEntries.add(new WikiEntry("relic_ender_hand", "Ender Hand", "Relics", List.of(
            "##Slot: Hands",
            "",
            "A gauntlet infused with Ender energy. Endermen",
            "will not attack you and you gain teleportation.",
            "",
            "##Passive",
            "  - Endermen are permanently neutral toward you",
            "  - Hold items in rain without angering endermen",
            "",
            "##Ability 1: Teleport Swap",
            "  - Cast Type: Instantaneous",
            "  - Swap positions with the mob you are looking at",
            "    (up to 20 blocks)",
            "  - Cooldown: 10 seconds",
            "",
            "##Ability 2: Ender Warp",
            "  - Cast Type: Instantaneous",
            "  - Teleport to the block you are looking at",
            "    (up to 30 blocks line of sight)",
            "  - Cooldown: 8 seconds"
        )));
        allEntries.add(new WikiEntry("relic_rage_glove", "Rage Glove", "Relics", List.of(
            "##Slot: Hands",
            "",
            "Blood-red gauntlets that empower bare-fisted",
            "combat with escalating fury.",
            "",
            "##Passive",
            "  - Fury Stacks: each hit builds a fury stack",
            "    (+2% damage per stack, max 20 stacks)",
            "  - Stacks decay after 5 seconds of no combat",
            "",
            "##Ability 1: Bare Knuckle",
            "  - Cast Type: Toggle",
            "  - When toggled on, fists deal +5 base damage",
            "    and attacks have slight knockback",
            "",
            "##Ability 2: Berserk",
            "  - Cast Type: Toggle",
            "  - +50% attack damage but take +30% more damage.",
            "    Attack speed increased by 20%."
        )));
        allEntries.add(new WikiEntry("relic_wool_mitten", "Wool Mitten", "Relics", List.of(
            "##Slot: Hands",
            "",
            "Cozy enchanted mittens that keep you warm and",
            "steadily restore health.",
            "",
            "##Passive",
            "  - +1 HP/5 sec natural regeneration",
            "  - Immune to Freezing damage",
            "",
            "##Ability 1: Warmth",
            "  - Cast Type: Toggle",
            "  - Emit a warm aura. Nearby players also gain",
            "    the regeneration bonus (4 block radius).",
            "",
            "##Ability 2: Cozy Aura",
            "  - Cast Type: Instantaneous",
            "  - Instantly restore 6 HP and remove Freezing,",
            "    Hunger, and Slowness effects",
            "  - Cooldown: 30 seconds"
        )));

        // Ring slot (2)
        allEntries.add(new WikiEntry("relic_bastion_ring", "Bastion Ring", "Relics", List.of(
            "##Slot: Ring",
            "",
            "A golden ring looted from a Piglin bastion.",
            "Piglins recognize you as one of their own.",
            "",
            "##Passive",
            "  - Piglins are permanently neutral toward you",
            "    (even without gold armor)",
            "  - +15% Barter bonus (better loot from bartering)",
            "",
            "##Ability 1: Gilded Strike",
            "  - Cast Type: Instantaneous",
            "  - Your next melee attack deals +8 bonus gold damage",
            "    and has a 25% chance to drop a gold nugget",
            "  - Cooldown: 12 seconds",
            "",
            "##Ability 2: Rally Piglins",
            "  - Cast Type: Instantaneous",
            "  - Nearby Piglins fight for you for 15 seconds",
            "  - Cooldown: 60 seconds"
        )));
        allEntries.add(new WikiEntry("relic_chorus_inhibitor", "Chorus Inhibitor", "Relics", List.of(
            "##Slot: Ring",
            "",
            "A ring crafted from refined chorus fruit. Anchors",
            "your position in space and manipulates void energy.",
            "",
            "##Passive",
            "  - Immune to forced teleportation (Enderman, Shulker)",
            "  - Void damage reduced by 50%",
            "",
            "##Ability 1: Anchor / Displace",
            "  - Cast Type: Instantaneous",
            "  - First use: set an anchor point at your location.",
            "    Second use: teleport back to the anchor point.",
            "  - Cooldown: 15 seconds (after teleport)",
            "",
            "##Ability 2: Void Grip",
            "  - Cast Type: Instantaneous",
            "  - Pull all entities within 10 blocks toward you",
            "  - Cooldown: 18 seconds"
        )));

        // Usable slot (7)
        allEntries.add(new WikiEntry("relic_shadow_glaive", "Shadow Glaive", "Relics", List.of(
            "##Slot: Usable",
            "",
            "A spectral throwing weapon that bounces between",
            "enemies and can unleash a devastating saw attack.",
            "",
            "##Ability 1: Bouncing Throw",
            "  - Cast Type: Instantaneous",
            "  - Throw the glaive. It bounces between up to 4",
            "    enemies within 8 blocks of each other, dealing",
            "    decreasing damage with each bounce.",
            "  - Cooldown: 6 seconds",
            "",
            "##Ability 2: Saw AOE",
            "  - Cast Type: Instantaneous",
            "  - Spin the glaive around you, dealing damage to",
            "    all enemies within 4 blocks",
            "  - Cooldown: 14 seconds"
        )));
        allEntries.add(new WikiEntry("relic_infinity_ham", "Infinity Ham", "Relics", List.of(
            "##Slot: Usable",
            "",
            "A magical ham leg that never runs out. Eating",
            "it fully restores hunger and may grant a random",
            "potion effect.",
            "",
            "##Ability 1: Eat",
            "  - Cast Type: Instantaneous",
            "  - Consume the ham to restore 8 hunger and 12",
            "    saturation. Grants a random positive potion",
            "    effect for 30 seconds.",
            "  - Cooldown: 30 seconds",
            "",
            "##Ability 2: Food Fight",
            "  - Cast Type: Instantaneous",
            "  - Throw the ham at a target, dealing 4 damage",
            "    and applying Nausea for 5 seconds.",
            "    The ham returns to you.",
            "  - Cooldown: 8 seconds"
        )));
        allEntries.add(new WikiEntry("relic_space_dissector", "Space Dissector", "Relics", List.of(
            "##Slot: Usable",
            "",
            "A strange device that tears holes in space-time,",
            "allowing instant teleportation between marks.",
            "",
            "##Ability 1: Mark",
            "  - Cast Type: Instantaneous",
            "  - Place a spatial mark at your current position.",
            "    You can have up to 2 marks at a time.",
            "  - Cooldown: 3 seconds",
            "",
            "##Ability 2: Warp",
            "  - Cast Type: Instantaneous",
            "  - Teleport to your most recent mark. The mark is",
            "    consumed upon arrival. Works across dimensions.",
            "  - Cooldown: 20 seconds"
        )));
        allEntries.add(new WikiEntry("relic_magic_mirror", "Magic Mirror", "Relics", List.of(
            "##Slot: Usable",
            "",
            "An enchanted mirror that reflects your home point.",
            "Use it to instantly recall to your spawn or bed.",
            "",
            "##Ability 1: Recall",
            "  - Cast Type: Instantaneous",
            "  - Teleport to your bed spawn point or world spawn.",
            "    2 second cast time (must stand still).",
            "  - Cooldown: 120 seconds",
            "",
            "##Ability 2: Mirror Image",
            "  - Cast Type: Instantaneous",
            "  - Leave a decoy at your current position that",
            "    attracts mob aggro for 10 seconds",
            "  - Cooldown: 45 seconds"
        )));
        allEntries.add(new WikiEntry("relic_horse_flute", "Horse Flute", "Relics", List.of(
            "##Slot: Usable",
            "",
            "A magical pan flute that can store and summon",
            "your tamed horse anywhere.",
            "",
            "##Ability 1: Store Horse",
            "  - Cast Type: Instantaneous",
            "  - While looking at your tamed horse, store it",
            "    inside the flute. The horse disappears.",
            "  - Cooldown: 5 seconds",
            "",
            "##Ability 2: Release Horse",
            "  - Cast Type: Instantaneous",
            "  - Summon your stored horse at your location.",
            "    The horse retains all equipment and stats.",
            "  - Cooldown: 5 seconds"
        )));
        allEntries.add(new WikiEntry("relic_spore_sack", "Spore Sack", "Relics", List.of(
            "##Slot: Usable",
            "",
            "A pulsating fungal sack that fires poisonous",
            "spore projectiles and clouds.",
            "",
            "##Ability 1: Poison Shot",
            "  - Cast Type: Instantaneous",
            "  - Fire a spore projectile that deals 3 damage and",
            "    applies Poison II for 5 seconds on hit",
            "  - Cooldown: 4 seconds",
            "",
            "##Ability 2: Poison Cloud",
            "  - Cast Type: Instantaneous",
            "  - Drop a lingering poison cloud at your feet that",
            "    lasts 8 seconds, dealing Poison damage to all",
            "    mobs that enter it",
            "  - Cooldown: 20 seconds"
        )));
        allEntries.add(new WikiEntry("relic_blazing_flask", "Blazing Flask", "Relics", List.of(
            "##Slot: Usable",
            "",
            "A flask of concentrated blaze powder that can",
            "fire explosive fireballs or power a jetpack.",
            "",
            "##Ability 1: Fireball",
            "  - Cast Type: Instantaneous",
            "  - Launch an explosive fireball that deals",
            "    8 fire damage on impact with a 3 block explosion",
            "  - Cooldown: 6 seconds",
            "",
            "##Ability 2: Jetpack",
            "  - Cast Type: Toggle",
            "  - While active, propel yourself upward with fire.",
            "    Drains fuel over time. Toggle off to glide down.",
            "    Burns entities directly below you."
        )));
        allEntries.add(new WikiEntry("relic_wardens_visor", "Warden's Visor", "Relics", List.of(
            "##Slot: Face",
            "",
            "A sculk-infused visor that grants tremorsense",
            "and sonic powers.",
            "",
            "##Ability 1: Tremor Sense",
            "  - Cast Type: Passive",
            "  - Highlight mobs through walls in dark areas",
            "",
            "##Ability 2: Sonic Pulse",
            "  - Cast Type: Instantaneous",
            "  - AOE damage + slow to all nearby mobs",
            "  - Cooldown: 8 seconds",
            "",
            "##Ability 3: Sculk Shroud",
            "  - Cast Type: Toggle",
            "  - Enemies can't detect you while active"
        )));
        allEntries.add(new WikiEntry("relic_verdant_mask", "Verdant Mask", "Relics", List.of(
            "##Slot: Face",
            "",
            "A living mask woven from enchanted vines",
            "and blossoms.",
            "",
            "##Ability 1: Pollen Shield",
            "  - Cast Type: Passive",
            "  - Poison nearby hostile mobs",
            "",
            "##Ability 2: Thorned Veil",
            "  - Cast Type: Passive",
            "  - Chance to poison attackers on hit",
            "",
            "##Ability 3: Blossom Burst",
            "  - Cast Type: Instantaneous",
            "  - Poison AOE + heal self",
            "  - Cooldown: 10 seconds"
        )));
        allEntries.add(new WikiEntry("relic_frostweave_veil", "Frostweave Veil", "Relics", List.of(
            "##Slot: Face",
            "",
            "A veil of woven frost that chills all",
            "who approach.",
            "",
            "##Ability 1: Cold Breath",
            "  - Cast Type: Passive",
            "  - Slow mobs in front of you",
            "",
            "##Ability 2: Frozen Gaze",
            "  - Cast Type: Instantaneous",
            "  - Freeze the entity you are looking at",
            "  - Cooldown: 6 seconds",
            "",
            "##Ability 3: Blizzard Aura",
            "  - Cast Type: Toggle",
            "  - Slow all mobs in radius while active"
        )));
        allEntries.add(new WikiEntry("relic_stormcaller_circlet", "Stormcaller Circlet", "Relics", List.of(
            "##Slot: Head",
            "",
            "A crackling circlet that commands lightning",
            "itself.",
            "",
            "##Ability 1: Static Field",
            "  - Cast Type: Passive",
            "  - Shock the nearest mob periodically",
            "",
            "##Ability 2: Thunder Strike",
            "  - Cast Type: Instantaneous",
            "  - Call lightning on the targeted entity",
            "  - Cooldown: 12 seconds",
            "",
            "##Ability 3: Tempest Aura",
            "  - Cast Type: Toggle",
            "  - Electric damage aura while active"
        )));
        allEntries.add(new WikiEntry("relic_ashen_diadem", "Ashen Diadem", "Relics", List.of(
            "##Slot: Head",
            "",
            "A crown forged in volcanic ash and shadow",
            "flame.",
            "",
            "##Ability 1: Ember Sight",
            "  - Cast Type: Passive",
            "  - Night vision in dark areas",
            "",
            "##Ability 2: Infernal Command",
            "  - Cast Type: Instantaneous",
            "  - Fire mobs stop targeting you",
            "  - Cooldown: 15 seconds",
            "",
            "##Ability 3: Pyroclasm",
            "  - Cast Type: Instantaneous",
            "  - Massive fire AOE around you",
            "  - Cooldown: 20 seconds"
        )));
        allEntries.add(new WikiEntry("relic_wraith_crown", "Wraith Crown", "Relics", List.of(
            "##Slot: Head",
            "",
            "A spectral crown that bridges the living",
            "and the dead.",
            "",
            "##Ability 1: Ethereal Sight",
            "  - Cast Type: Passive",
            "  - Hostile mobs glow",
            "",
            "##Ability 2: Soul Siphon",
            "  - Cast Type: Instantaneous",
            "  - Drain HP from target",
            "  - Cooldown: 6 seconds",
            "",
            "##Ability 3: Phantom Form",
            "  - Cast Type: Toggle",
            "  - 30% damage reduction but -20% damage dealt"
        )));
        allEntries.add(new WikiEntry("relic_arcane_gauntlet", "Arcane Gauntlet", "Relics", List.of(
            "##Slot: Right Hand",
            "",
            "A gauntlet pulsing with raw arcane energy.",
            "",
            "##Ability 1: Mana Shield",
            "  - Cast Type: Passive",
            "  - Absorb damage using XP",
            "",
            "##Ability 2: Arcane Bolt",
            "  - Cast Type: Instantaneous",
            "  - Fire a magic projectile at your target",
            "  - Cooldown: 3 seconds",
            "",
            "##Ability 3: Spellweave",
            "  - Cast Type: Instantaneous",
            "  - Boost ability damage by 25-40%",
            "  - Cooldown: 30 seconds"
        )));
        allEntries.add(new WikiEntry("relic_iron_fist", "Iron Fist", "Relics", List.of(
            "##Slot: Right Hand",
            "",
            "Heavy iron knuckles that pack a devastating",
            "punch.",
            "",
            "##Ability 1: Heavy Blows",
            "  - Cast Type: Passive",
            "  - Bonus unarmed damage",
            "",
            "##Ability 2: Uppercut",
            "  - Cast Type: Instantaneous",
            "  - Launch mob upward into the air",
            "  - Cooldown: 5 seconds",
            "",
            "##Ability 3: Ground Pound",
            "  - Cast Type: Instantaneous",
            "  - AOE slam + slow all nearby mobs",
            "  - Cooldown: 10 seconds"
        )));
        allEntries.add(new WikiEntry("relic_plague_grasp", "Plague Grasp", "Relics", List.of(
            "##Slot: Right Hand",
            "",
            "A gauntlet dripping with venomous corruption.",
            "",
            "##Ability 1: Venomous Touch",
            "  - Cast Type: Passive",
            "  - Melee attacks poison targets",
            "",
            "##Ability 2: Wither Grip",
            "  - Cast Type: Instantaneous",
            "  - Apply wither to target",
            "  - Cooldown: 8 seconds",
            "",
            "##Ability 3: Plague Wave",
            "  - Cast Type: Instantaneous",
            "  - Poison cone attack in front of you",
            "  - Cooldown: 15 seconds"
        )));
        allEntries.add(new WikiEntry("relic_sunforged_bracer", "Sunforged Bracer", "Relics", List.of(
            "##Slot: Right Hand",
            "",
            "A bracer blessed by solar radiance, bane of",
            "the undead.",
            "",
            "##Ability 1: Blessed Strikes",
            "  - Cast Type: Passive",
            "  - Bonus damage vs undead mobs",
            "",
            "##Ability 2: Purifying Touch",
            "  - Cast Type: Instantaneous",
            "  - Remove negative effects from yourself",
            "  - Cooldown: 20 seconds",
            "",
            "##Ability 3: Divine Smite",
            "  - Cast Type: Instantaneous",
            "  - Holy beam on target dealing radiant damage",
            "  - Cooldown: 12 seconds"
        )));
        allEntries.add(new WikiEntry("relic_stormband", "Stormband", "Relics", List.of(
            "##Slot: Right Ring",
            "",
            "A ring that crackles with contained lightning.",
            "",
            "##Ability 1: Charged Steps",
            "  - Cast Type: Passive",
            "  - Shock mobs while sprinting",
            "",
            "##Ability 2: Arc Discharge",
            "  - Cast Type: Instantaneous",
            "  - Chain lightning hitting 3-5 targets",
            "  - Cooldown: 8 seconds",
            "",
            "##Ability 3: Galvanic Surge",
            "  - Cast Type: Instantaneous",
            "  - Gain Speed III + Strength I",
            "  - Cooldown: 25 seconds"
        )));
        allEntries.add(new WikiEntry("relic_gravestone_ring", "Gravestone Ring", "Relics", List.of(
            "##Slot: Right Ring",
            "",
            "A ring carved from a gravestone, defying",
            "death itself.",
            "",
            "##Ability 1: Death Ward",
            "  - Cast Type: Passive",
            "  - Survive lethal hit once per 5 minutes",
            "",
            "##Ability 2: Life Tap",
            "  - Cast Type: Instantaneous",
            "  - Sacrifice HP for Strength II",
            "  - Cooldown: 15 seconds",
            "",
            "##Ability 3: Undying Resolve",
            "  - Cast Type: Instantaneous",
            "  - Invulnerability for 5 seconds",
            "  - Cooldown: 60 seconds"
        )));
        allEntries.add(new WikiEntry("relic_verdant_signet", "Verdant Signet", "Relics", List.of(
            "##Slot: Right Ring",
            "",
            "A ring of living wood that channels nature's",
            "healing.",
            "",
            "##Ability 1: Nature's Blessing",
            "  - Cast Type: Passive",
            "  - Regenerate while standing on grass",
            "",
            "##Ability 2: Growth Surge",
            "  - Cast Type: Instantaneous",
            "  - Instant heal + regeneration effect",
            "  - Cooldown: 20 seconds",
            "",
            "##Ability 3: Bloom Shield",
            "  - Cast Type: Instantaneous",
            "  - AOE healing zone for nearby allies",
            "  - Cooldown: 30 seconds"
        )));
        allEntries.add(new WikiEntry("relic_phoenix_mantle", "Phoenix Mantle", "Relics", List.of(
            "##Slot: Back",
            "",
            "A mantle of phoenix feathers that burns with",
            "eternal flame.",
            "",
            "##Ability 1: Fireborn",
            "  - Cast Type: Passive",
            "  - Fire immunity",
            "",
            "##Ability 2: Rebirth",
            "  - Cast Type: Passive",
            "  - Revive on death once per 5 minutes",
            "",
            "##Ability 3: Inferno Wings",
            "  - Cast Type: Instantaneous",
            "  - Launch upward + glide + fire rain below",
            "  - Cooldown: 20 seconds"
        )));
        allEntries.add(new WikiEntry("relic_windrunner_cloak", "Windrunner Cloak", "Relics", List.of(
            "##Slot: Back",
            "",
            "A billowing cloak that catches every breeze.",
            "",
            "##Ability 1: Tailwind",
            "  - Cast Type: Passive",
            "  - +10-20% movement speed",
            "",
            "##Ability 2: Gust",
            "  - Cast Type: Instantaneous",
            "  - Push all nearby mobs away",
            "  - Cooldown: 5 seconds",
            "",
            "##Ability 3: Dash",
            "  - Cast Type: Instantaneous",
            "  - Teleport 8-14 blocks forward",
            "  - Cooldown: 8 seconds"
        )));
        allEntries.add(new WikiEntry("relic_abyssal_cape", "Abyssal Cape", "Relics", List.of(
            "##Slot: Back",
            "",
            "A cape woven from void energy, folding space",
            "around you.",
            "",
            "##Ability 1: Void Walker",
            "  - Cast Type: Passive",
            "  - 40-60% fall damage reduction",
            "",
            "##Ability 2: Blink",
            "  - Cast Type: Instantaneous",
            "  - Teleport 6-10 blocks in facing direction",
            "  - Cooldown: 4 seconds",
            "",
            "##Ability 3: Void Rift",
            "  - Cast Type: Instantaneous",
            "  - Line damage + pull enemies toward you",
            "  - Cooldown: 12 seconds"
        )));
        allEntries.add(new WikiEntry("relic_alchemists_sash", "Alchemist's Sash", "Relics", List.of(
            "##Slot: Belt",
            "",
            "A sash adorned with vials of volatile",
            "concoctions.",
            "",
            "##Ability 1: Potion Mastery",
            "  - Cast Type: Passive",
            "  - +20-40% potion duration",
            "",
            "##Ability 2: Transmute",
            "  - Cast Type: Instantaneous",
            "  - Flip negative effects to positive effects",
            "  - Cooldown: 15 seconds",
            "",
            "##Ability 3: Volatile Mix",
            "  - Cast Type: Instantaneous",
            "  - Splash bomb AOE damage",
            "  - Cooldown: 10 seconds"
        )));
        allEntries.add(new WikiEntry("relic_guardians_girdle", "Guardian's Girdle", "Relics", List.of(
            "##Slot: Belt",
            "",
            "A heavy girdle worn by temple guardians",
            "of old.",
            "",
            "##Ability 1: Fortitude",
            "  - Cast Type: Passive",
            "  - +1-2 armor toughness",
            "",
            "##Ability 2: Brace",
            "  - Cast Type: Instantaneous",
            "  - Resistance II for 4-6 seconds",
            "  - Cooldown: 15 seconds",
            "",
            "##Ability 3: Stalwart Stand",
            "  - Cast Type: Instantaneous",
            "  - No knockback + 30% DR for 5 seconds",
            "  - Cooldown: 25 seconds"
        )));
        allEntries.add(new WikiEntry("relic_serpent_belt", "Serpent Belt", "Relics", List.of(
            "##Slot: Belt",
            "",
            "A belt fashioned from a serpent's enchanted",
            "scales.",
            "",
            "##Ability 1: Constrictor",
            "  - Cast Type: Passive",
            "  - Melee attacks slow enemies",
            "",
            "##Ability 2: Venom Spit",
            "  - Cast Type: Instantaneous",
            "  - Poison projectile at target",
            "  - Cooldown: 5 seconds",
            "",
            "##Ability 3: Coil Strike",
            "  - Cast Type: Instantaneous",
            "  - Cone pull + damage in front of you",
            "  - Cooldown: 10 seconds"
        )));
        allEntries.add(new WikiEntry("relic_frostfire_pendant", "Frostfire Pendant", "Relics", List.of(
            "##Slot: Necklace",
            "",
            "A pendant that holds the balance of ice",
            "and flame.",
            "",
            "##Ability 1: Equilibrium",
            "  - Cast Type: Passive",
            "  - Biome-adaptive resistance",
            "",
            "##Ability 2: Frostfire Bolt",
            "  - Cast Type: Instantaneous",
            "  - Dual-element projectile (fire + ice)",
            "  - Cooldown: 5 seconds",
            "",
            "##Ability 3: Elemental Storm",
            "  - Cast Type: Instantaneous",
            "  - Combined fire + ice AOE attack",
            "  - Cooldown: 18 seconds"
        )));
        allEntries.add(new WikiEntry("relic_tidekeeper_amulet", "Tidekeeper Amulet", "Relics", List.of(
            "##Slot: Necklace",
            "",
            "An amulet blessed by the ocean's ancient",
            "guardians.",
            "",
            "##Ability 1: Aquatic Grace",
            "  - Cast Type: Passive",
            "  - Water breathing + dolphin's grace",
            "",
            "##Ability 2: Tidal Wave",
            "  - Cast Type: Instantaneous",
            "  - Push + damage to nearby mobs",
            "  - Cooldown: 8 seconds",
            "",
            "##Ability 3: Whirlpool",
            "  - Cast Type: Instantaneous",
            "  - Pull vortex AOE centered on you",
            "  - Cooldown: 15 seconds"
        )));
        allEntries.add(new WikiEntry("relic_bloodstone_choker", "Bloodstone Choker", "Relics", List.of(
            "##Slot: Necklace",
            "",
            "A choker set with a crimson gem that hungers",
            "for blood.",
            "",
            "##Ability 1: Bloodlust",
            "  - Cast Type: Passive",
            "  - Heal when below half HP on kill",
            "",
            "##Ability 2: Blood Price",
            "  - Cast Type: Instantaneous",
            "  - Sacrifice HP for true damage on target",
            "  - Cooldown: 6 seconds",
            "",
            "##Ability 3: Sanguine Feast",
            "  - Cast Type: Instantaneous",
            "  - Drain HP from all nearby mobs",
            "  - Cooldown: 20 seconds"
        )));
        allEntries.add(new WikiEntry("relic_thornweave_glove", "Thornweave Glove", "Relics", List.of(
            "##Slot: Left Hand",
            "",
            "A glove of living thorns that punishes",
            "attackers.",
            "",
            "##Ability 1: Thorned Grasp",
            "  - Cast Type: Passive",
            "  - Reflect thorn damage to attackers",
            "",
            "##Ability 2: Vine Lash",
            "  - Cast Type: Instantaneous",
            "  - Pull mob toward you",
            "  - Cooldown: 6 seconds",
            "",
            "##Ability 3: Entangle",
            "  - Cast Type: Instantaneous",
            "  - Root all mobs in radius",
            "  - Cooldown: 15 seconds"
        )));
        allEntries.add(new WikiEntry("relic_chrono_glove", "Chrono Glove", "Relics", List.of(
            "##Slot: Left Hand",
            "",
            "A glove warped by temporal magic, bending",
            "time itself.",
            "",
            "##Ability 1: Temporal Flux",
            "  - Cast Type: Passive",
            "  - +mining speed",
            "",
            "##Ability 2: Time Snap",
            "  - Cast Type: Instantaneous",
            "  - Teleport backward + restore HP",
            "  - Cooldown: 20 seconds",
            "",
            "##Ability 3: Stasis Field",
            "  - Cast Type: Instantaneous",
            "  - Freeze all mobs in area",
            "  - Cooldown: 30 seconds"
        )));
        allEntries.add(new WikiEntry("relic_stormstrider_boots", "Stormstrider Boots", "Relics", List.of(
            "##Slot: Feet",
            "",
            "Boots that leave lightning in every footstep.",
            "",
            "##Ability 1: Lightning Step",
            "  - Cast Type: Passive",
            "  - Electric trail damages nearby mobs while",
            "    moving",
            "",
            "##Ability 2: Thunder Leap",
            "  - Cast Type: Instantaneous",
            "  - High jump + landing AOE damage",
            "  - Cooldown: 8 seconds"
        )));
        allEntries.add(new WikiEntry("relic_sandwalker_treads", "Sandwalker Treads", "Relics", List.of(
            "##Slot: Feet",
            "",
            "Ancient treads that glide effortlessly across",
            "desert sands.",
            "",
            "##Ability 1: Desert Born",
            "  - Cast Type: Passive",
            "  - Speed boost on sand blocks",
            "",
            "##Ability 2: Quicksand",
            "  - Cast Type: Instantaneous",
            "  - Create a root zone on the ground",
            "  - Cooldown: 12 seconds",
            "",
            "##Ability 3: Sandstorm",
            "  - Cast Type: Instantaneous",
            "  - AOE damage + blindness",
            "  - Cooldown: 15 seconds"
        )));
        allEntries.add(new WikiEntry("relic_emberstone_band", "Emberstone Band", "Relics", List.of(
            "##Slot: Left Ring",
            "",
            "A ring of smoldering stone that radiates",
            "warmth.",
            "",
            "##Ability 1: Warm Aura",
            "  - Cast Type: Passive",
            "  - Prevent freeze effects",
            "",
            "##Ability 2: Fire Snap",
            "  - Cast Type: Instantaneous",
            "  - Ignite the targeted entity",
            "  - Cooldown: 4 seconds",
            "",
            "##Ability 3: Combustion",
            "  - Cast Type: Instantaneous",
            "  - Explode all burning mobs nearby",
            "  - Cooldown: 15 seconds"
        )));
        allEntries.add(new WikiEntry("relic_void_lantern", "Void Lantern", "Relics", List.of(
            "##Slot: Usable",
            "",
            "A lantern that burns with void-light, pulling",
            "the world inward.",
            "",
            "##Ability 1: Dark Beacon",
            "  - Cast Type: Instantaneous",
            "  - Place a mob-pulling gravity well",
            "  - Cooldown: 15 seconds",
            "",
            "##Ability 2: Dimensional Tear",
            "  - Cast Type: Instantaneous",
            "  - Open a portal dealing AOE damage",
            "  - Cooldown: 12 seconds"
        )));
        allEntries.add(new WikiEntry("relic_thunderhorn", "Thunderhorn", "Relics", List.of(
            "##Slot: Usable",
            "",
            "A horn carved from a thunderbeast's tusk.",
            "",
            "##Ability 1: War Cry",
            "  - Cast Type: Instantaneous",
            "  - Buff self + nearby allies with Strength",
            "    and Speed",
            "  - Cooldown: 30 seconds",
            "",
            "##Ability 2: Sonic Boom",
            "  - Cast Type: Instantaneous",
            "  - Cone knockback + damage",
            "  - Cooldown: 10 seconds"
        )));
        allEntries.add(new WikiEntry("relic_mending_chalice", "Mending Chalice", "Relics", List.of(
            "##Slot: Usable",
            "",
            "A sacred chalice that overflows with healing",
            "light.",
            "",
            "##Ability 1: Healing Draught",
            "  - Cast Type: Instantaneous",
            "  - Heal 6-12 HP instantly",
            "  - Cooldown: 15 seconds",
            "",
            "##Ability 2: Sanctified Ground",
            "  - Cast Type: Instantaneous",
            "  - AOE healing zone for allies",
            "  - Cooldown: 30 seconds"
        )));
    }

    // ---- RPG WEAPONS (10) ----

    private void buildWeaponEntries() {
        allEntries.add(new WikiEntry("weapon_lunar_crown", "Lunar Crown", "Relics", List.of(
            "##RPG Weapon",
            "",
            "A crescent-shaped crown that channels moonlight",
            "into devastating magic attacks.",
            "",
            "##Skill 1: Moonbeam",
            "  - Fire a beam of concentrated moonlight that",
            "    pierces through enemies in a line",
            "  - Deals +6 Arcane damage",
            "  - Cooldown: 8 seconds",
            "",
            "##Skill 2: Lunar Eclipse",
            "  - Shroud the area in darkness for 5 seconds.",
            "    All enemies gain Blindness and take 3 damage/sec",
            "  - Cooldown: 30 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_solar_crown", "Solar Crown", "Relics", List.of(
            "##RPG Weapon",
            "",
            "A radiant crown that blazes with the power of",
            "the sun. Counterpart to the Lunar Crown.",
            "",
            "##Skill 1: Solar Flare",
            "  - Release a burst of solar energy in a cone,",
            "    dealing 8 fire damage and setting enemies ablaze",
            "  - Cooldown: 8 seconds",
            "",
            "##Skill 2: Supernova",
            "  - Charge for 2 seconds, then release a massive",
            "    explosion of solar fire (10 block radius)",
            "  - Cooldown: 35 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_vampiric_tome", "Vampiric Tome", "Relics", List.of(
            "##RPG Weapon",
            "",
            "A blood-soaked spellbook that drains life from",
            "enemies to restore your own.",
            "",
            "##Skill 1: Life Drain",
            "  - Channel a beam that drains 2 HP/sec from a",
            "    target and restores it to you. Range: 8 blocks",
            "  - Cooldown: 6 seconds",
            "",
            "##Skill 2: Blood Ritual",
            "  - Sacrifice 4 HP to deal 12 damage to all",
            "    enemies within 6 blocks. Heal 3 HP per enemy hit.",
            "  - Cooldown: 20 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_static_seeker", "Static Seeker", "Relics", List.of(
            "##RPG Weapon",
            "",
            "A lightning-charged lance that zaps enemies",
            "with chain lightning attacks.",
            "",
            "##Skill 1: Chain Lightning",
            "  - Strike the target with lightning that chains",
            "    to up to 3 nearby enemies, dealing decreasing",
            "    damage per chain (8, 5, 3)",
            "  - Cooldown: 7 seconds",
            "",
            "##Skill 2: Thunderstorm",
            "  - Call down lightning bolts on all enemies within",
            "    12 blocks for 4 seconds. Each bolt deals 6 damage.",
            "  - Cooldown: 40 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_battledancer", "Battledancer", "Relics", List.of(
            "##RPG Weapon",
            "",
            "Twin blades that reward fluid, evasive combat",
            "with increasing damage and speed.",
            "",
            "##Skill 1: Blade Dance",
            "  - Perform a rapid 4-hit combo that deals",
            "    3 damage per hit with increasing speed",
            "  - Cooldown: 5 seconds",
            "",
            "##Skill 2: Evasion Stance",
            "  - Enter a defensive stance for 6 seconds. Dodge",
            "    50% of incoming attacks and counter with a",
            "    slash dealing 4 damage per dodge.",
            "  - Cooldown: 25 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_ebonchill", "Ebonchill", "Relics", List.of(
            "##RPG Weapon",
            "",
            "An ancient staff of pure frost that slows and",
            "shatters frozen enemies.",
            "",
            "##Skill 1: Frostbolt",
            "  - Fire an icicle projectile dealing 6 damage and",
            "    applying Slowness III for 4 seconds",
            "  - Cooldown: 4 seconds",
            "",
            "##Skill 2: Glacial Shatter",
            "  - Freeze the target solid for 3 seconds, then",
            "    shatter the ice dealing 15 damage. Enemies",
            "    already slowed take 50% more damage.",
            "  - Cooldown: 22 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_lightbinder", "Lightbinder", "Relics", List.of(
            "##RPG Weapon",
            "",
            "A holy scepter that channels divine light to",
            "heal allies and smite undead.",
            "",
            "##Skill 1: Holy Bolt",
            "  - Fire a bolt of light. Deals 8 damage to undead,",
            "    4 damage to others, and heals allies for 4 HP.",
            "  - Cooldown: 5 seconds",
            "",
            "##Skill 2: Divine Judgment",
            "  - Smite all undead within 10 blocks, dealing 12",
            "    damage and applying Weakness II for 8 seconds",
            "  - Cooldown: 30 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_crescent_blade", "Crescent Blade", "Relics", List.of(
            "##RPG Weapon",
            "",
            "A curved sword that fires crescent-shaped energy",
            "slashes at range.",
            "",
            "##Skill 1: Crescent Slash",
            "  - Fire a crescent-shaped projectile that deals 6",
            "    damage and passes through multiple enemies",
            "  - Cooldown: 3 seconds",
            "",
            "##Skill 2: Moonlit Barrage",
            "  - Unleash 5 rapid crescent slashes in a wide arc.",
            "    Each slash deals 4 damage. Total potential: 20",
            "  - Cooldown: 18 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_ghost_fang", "Ghost Fang", "Relics", List.of(
            "##RPG Weapon",
            "",
            "A spectral dagger that phases through armor and",
            "lets you become incorporeal.",
            "",
            "##Skill 1: Phantom Strike",
            "  - Dash forward through entities, dealing 5 damage",
            "    to each enemy you pass through. Ignores armor.",
            "  - Cooldown: 6 seconds",
            "",
            "##Skill 2: Incorporeal",
            "  - Become a ghost for 4 seconds. You cannot take",
            "    or deal damage, but can pass through blocks.",
            "    Exiting this state deals 6 damage to all",
            "    nearby enemies.",
            "  - Cooldown: 28 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_terra_warhammer", "Terra Warhammer", "Relics", List.of(
            "##RPG Weapon",
            "",
            "A massive earthen warhammer that shakes the ground",
            "and summons stone walls.",
            "",
            "##Skill 1: Seismic Slam",
            "  - Slam the ground, sending a shockwave forward",
            "    that deals 10 damage and launches enemies up",
            "  - Cooldown: 8 seconds",
            "",
            "##Skill 2: Stone Wall",
            "  - Raise a wall of stone blocks (5 wide, 3 tall)",
            "    in front of you. The wall lasts 10 seconds",
            "    and blocks projectiles and mob movement.",
            "  - Cooldown: 25 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_voidreaver", "Voidreaver", "Relics", List.of(
            "##RPG Weapon",
            "",
            "A shadow scythe that rends the fabric between",
            "dimensions, bypassing even the strongest armor.",
            "",
            "##Skill 1: Nether Rend",
            "  - Swing a shadow arc that bypasses 50% of",
            "    the target's armor",
            "  - Cooldown: 5 seconds",
            "",
            "##Skill 2: Dimensional Collapse",
            "  - Create a void singularity that pulls enemies",
            "    in and deals massive AOE damage",
            "  - Cooldown: 12 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_solaris", "Solaris", "Relics", List.of(
            "##RPG Weapon",
            "",
            "A holy greatsword radiating divine light,",
            "capable of both healing allies and judging foes.",
            "",
            "##Skill 1: Consecrate",
            "  - Create a circle of holy fire that damages",
            "    enemies and heals nearby allies",
            "  - Cooldown: 6 seconds",
            "",
            "##Skill 2: Judgment",
            "  - Mark a target with holy light, then trigger",
            "    a delayed burst of radiant damage",
            "  - Cooldown: 10 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_stormfury", "Stormfury", "Relics", List.of(
            "##RPG Weapon",
            "",
            "A lightning-infused katana that crackles with",
            "the fury of a thousand storms.",
            "",
            "##Skill 1: Lightning Dash",
            "  - Dash through enemies in a line, dealing",
            "    lightning damage to all in your path",
            "  - Cooldown: 4 seconds",
            "",
            "##Skill 2: Thunder God's Descent",
            "  - Leap into the air and slam down with an",
            "    electrifying AOE shockwave",
            "  - Cooldown: 10 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_briarthorn", "Briarthorn", "Relics", List.of(
            "##RPG Weapon",
            "",
            "A nature staff woven from living thorns,",
            "channeling the wrath of the wild.",
            "",
            "##Skill 1: Entangling Roots",
            "  - Summon roots that trap enemies in place",
            "    and deal poison AOE damage",
            "  - Cooldown: 6 seconds",
            "",
            "##Skill 2: Nature's Wrath",
            "  - Rain thorns from above, applying Poison II",
            "    to all enemies in the area",
            "  - Cooldown: 9 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_abyssal_trident", "Abyssal Trident", "Relics", List.of(
            "##RPG Weapon",
            "",
            "A water and ice polearm forged in the deepest",
            "ocean trenches. Commands the tides themselves.",
            "",
            "##Skill 1: Tidal Surge",
            "  - Send a wave of water crashing forward,",
            "    knocking enemies back with tremendous force",
            "  - Cooldown: 4 seconds",
            "",
            "##Skill 2: Maelstrom",
            "  - Create a whirlpool that pulls all nearby",
            "    enemies inward and deals AOE damage",
            "  - Cooldown: 12 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_pyroclast", "Pyroclast", "Relics", List.of(
            "##RPG Weapon",
            "",
            "A fire warhammer forged in volcanic magma.",
            "Each strike erupts with molten fury.",
            "",
            "##Skill 1: Molten Strike",
            "  - Slam the ground to open a lava fissure",
            "    in a line, burning all enemies in the path",
            "  - Cooldown: 5 seconds",
            "",
            "##Skill 2: Eruption",
            "  - Trigger a volcanic eruption that launches",
            "    you upward and rains fire AOE below",
            "  - Cooldown: 13 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_whisperwind", "Whisperwind", "Relics", List.of(
            "##RPG Weapon",
            "",
            "An arcane bow that fires arrows woven from",
            "pure wind magic. Silent and deadly.",
            "",
            "##Skill 1: Piercing Gale",
            "  - Fire a wind arrow that pierces through",
            "    all enemies in a line",
            "  - Cooldown: 3 seconds",
            "",
            "##Skill 2: Cyclone Volley",
            "  - Release 8 homing wind arrows that seek",
            "    out nearby enemies automatically",
            "  - Cooldown: 10 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_soulchain", "Soulchain", "Relics", List.of(
            "##RPG Weapon",
            "",
            "A shadow flail bound with necromantic chains.",
            "Reaps the souls of the fallen to sustain you.",
            "",
            "##Skill 1: Soul Lash",
            "  - Lash out with the chain, pulling the first",
            "    enemy hit toward you",
            "  - Cooldown: 4 seconds",
            "",
            "##Skill 2: Reaping Harvest",
            "  - Spin the flail in a wide AOE, dealing damage",
            "    and healing you for each enemy hit",
            "  - Cooldown: 8 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_unique_whip_1", "Serpent's Lash", "Relics", List.of(
            "##RPG Weapon",
            "",
            "A venomous whip that strikes with the speed",
            "and lethality of a serpent's bite.",
            "",
            "##Skill 1: Venom Crack",
            "  - Crack the whip, applying poison to the",
            "    target on hit",
            "  - Cooldown: 4 seconds",
            "",
            "##Skill 2: Constrict",
            "  - Pull and trap nearby enemies with the whip,",
            "    holding them in place",
            "  - Cooldown: 7 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_unique_whip_2", "Flamelash", "Relics", List.of(
            "##RPG Weapon",
            "",
            "A whip wreathed in searing flames. Each crack",
            "leaves a trail of fire in its wake.",
            "",
            "##Skill 1: Fire Crack",
            "  - Crack the whip to set the target ablaze",
            "    with lingering fire damage",
            "  - Cooldown: 4 seconds",
            "",
            "##Skill 2: Infernal Coil",
            "  - Coil the whip into a fire trap that ignites",
            "    enemies who step on it",
            "  - Cooldown: 8 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_unique_whip_sw", "Thornwhip of the Verdant Warden", "Relics", List.of(
            "##RPG Weapon",
            "",
            "A living whip of thorny vines, wielded by the",
            "ancient wardens of the forest.",
            "",
            "##Skill 1: Thorn Snap",
            "  - Snap the whip to deal damage and slow",
            "    the target",
            "  - Cooldown: 3 seconds",
            "",
            "##Skill 2: Binding Vines",
            "  - Root all nearby enemies in place with",
            "    entangling vines",
            "  - Cooldown: 9 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_unique_wand_1", "Arcane Focus", "Relics", List.of(
            "##RPG Weapon",
            "",
            "A crystalline wand that channels raw arcane",
            "energy into rapid magical projectiles.",
            "",
            "##Skill 1: Arcane Missile",
            "  - Fire 3 arcane bolts that home in on the",
            "    nearest target",
            "  - Cooldown: 3 seconds",
            "",
            "##Skill 2: Mana Burst",
            "  - Release an arcane shockwave that damages",
            "    all nearby enemies",
            "  - Cooldown: 6 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_unique_wand_2", "Frostfinger", "Relics", List.of(
            "##RPG Weapon",
            "",
            "An ice wand carved from a glacial shard.",
            "Its touch freezes the very air around it.",
            "",
            "##Skill 1: Ice Shard",
            "  - Fire an ice shard that damages and slows",
            "    the target",
            "  - Cooldown: 3 seconds",
            "",
            "##Skill 2: Flash Freeze",
            "  - Unleash a frost shockwave that freezes all",
            "    enemies in a radius around you",
            "  - Cooldown: 8 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_unique_wand_sw", "Star Conduit", "Relics", List.of(
            "##RPG Weapon",
            "",
            "A holy wand that draws power from the stars",
            "themselves, raining celestial fury on foes.",
            "",
            "##Skill 1: Starfall",
            "  - Call down 5 star strikes on the targeted",
            "    area, each dealing holy damage",
            "  - Cooldown: 5 seconds",
            "",
            "##Skill 2: Celestial Beam",
            "  - Channel a continuous beam of starlight that",
            "    deals sustained holy damage",
            "  - Cooldown: 10 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_unique_katana_1", "Windcutter", "Relics", List.of(
            "##RPG Weapon",
            "",
            "A physical katana so sharp it cuts the wind",
            "itself. Favored by master swordsmen.",
            "",
            "##Skill 1: Quick Draw",
            "  - Perform a lightning-fast critical slash",
            "    from the sheath",
            "  - Cooldown: 3 seconds",
            "",
            "##Skill 2: Blade Flurry",
            "  - Execute 3 rapid slashes in quick succession",
            "    against the target",
            "  - Cooldown: 6 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_unique_katana_2", "Shadowmoon Blade", "Relics", List.of(
            "##RPG Weapon",
            "",
            "A shadow katana infused with dark moonlight.",
            "Its wielder strikes from the shadows unseen.",
            "",
            "##Skill 1: Shadow Step",
            "  - Teleport behind the targeted enemy",
            "    for a surprise attack",
            "  - Cooldown: 4 seconds",
            "",
            "##Skill 2: Moonlit Slash",
            "  - Fire a shadow arc projectile that deals",
            "    heavy damage at range",
            "  - Cooldown: 7 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_unique_katana_sw", "Ashenblade of the Eternal Dusk", "Relics", List.of(
            "##RPG Weapon",
            "",
            "A fire and shadow katana forged at the boundary",
            "between day and night. Burns with twilight flame.",
            "",
            "##Skill 1: Twilight Slash",
            "  - Phase through the target dealing damage as",
            "    you pass through their form",
            "  - Cooldown: 4 seconds",
            "",
            "##Skill 2: Dusk Storm",
            "  - Unleash 5 expanding shadow arcs that fan",
            "    outward in a wide cone",
            "  - Cooldown: 10 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_unique_greatshield_1", "Ironwall", "Relics", List.of(
            "##RPG Weapon",
            "",
            "A massive physical greatshield forged from solid",
            "iron. An impenetrable mobile fortress.",
            "",
            "##Skill 1: Fortress",
            "  - Plant the shield and gain massive damage",
            "    resistance for a short duration",
            "  - Cooldown: 4 seconds",
            "",
            "##Skill 2: Battering Ram",
            "  - Charge forward shield-first, bashing all",
            "    enemies in your path",
            "  - Cooldown: 8 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_unique_greatshield_2", "Aegis of Dawn", "Relics", List.of(
            "##RPG Weapon",
            "",
            "A holy greatshield blessed by the dawn. Its",
            "radiance shields both the wielder and allies.",
            "",
            "##Skill 1: Holy Barrier",
            "  - Project a shield that protects yourself",
            "    and nearby allies from damage",
            "  - Cooldown: 5 seconds",
            "",
            "##Skill 2: Radiant Repulse",
            "  - Release a burst of holy energy that knocks",
            "    back all nearby enemies",
            "  - Cooldown: 9 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_unique_greatshield_sw", "Titan's Bulwark", "Relics", List.of(
            "##RPG Weapon",
            "",
            "A colossal greatshield said to have been wielded",
            "by the titans of old. Virtually indestructible.",
            "",
            "##Skill 1: Unbreakable",
            "  - Raise a golden dome shield that blocks all",
            "    incoming damage for a short duration",
            "  - Cooldown: 6 seconds",
            "",
            "##Skill 2: Titan Slam",
            "  - Slam the shield into the ground creating a",
            "    massive shockwave in all directions",
            "  - Cooldown: 12 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_unique_throwing_axe_1", "Stormhatchet", "Relics", List.of(
            "##RPG Weapon",
            "",
            "A lightning-charged throwing axe that arcs",
            "between enemies like a bolt of thunder.",
            "",
            "##Skill 1: Thunder Throw",
            "  - Throw the axe, chaining lightning to up",
            "    to 2 additional enemies on impact",
            "  - Cooldown: 3 seconds",
            "",
            "##Skill 2: Boomerang Arc",
            "  - Hurl the axe in a wide arc that returns",
            "    to you, hitting enemies both ways",
            "  - Cooldown: 7 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_unique_throwing_axe_2", "Frostbite Hatchet", "Relics", List.of(
            "##RPG Weapon",
            "",
            "An ice-encrusted throwing axe that freezes",
            "targets solid on impact.",
            "",
            "##Skill 1: Frozen Hurl",
            "  - Throw the axe to freeze the target on",
            "    impact for a short duration",
            "  - Cooldown: 4 seconds",
            "",
            "##Skill 2: Shatter Throw",
            "  - Hurl the axe with such force it creates a",
            "    frost shockwave on impact",
            "  - Cooldown: 8 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_unique_throwing_axe_sw", "Galeforce Tomahawk", "Relics", List.of(
            "##RPG Weapon",
            "",
            "A wind-enchanted throwing axe that slices through",
            "the air with unstoppable force.",
            "",
            "##Skill 1: Windborne Hatchet",
            "  - Throw the axe with wind power, piercing",
            "    through all enemies in its path",
            "  - Cooldown: 3 seconds",
            "",
            "##Skill 2: Whirlwind Toss",
            "  - Throw 4 axes simultaneously in all cardinal",
            "    directions at once",
            "  - Cooldown: 9 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_unique_rapier_1", "Duelist's Sting", "Relics", List.of(
            "##RPG Weapon",
            "",
            "A finely crafted physical rapier favored by",
            "master duelists. Precision over power.",
            "",
            "##Skill 1: Riposte",
            "  - Parry the next incoming attack and counter",
            "    with a precise strike",
            "  - Cooldown: 4 seconds",
            "",
            "##Skill 2: Flurry of Blows",
            "  - Execute 4 rapid thrusting strikes in quick",
            "    succession against the target",
            "  - Cooldown: 6 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_unique_rapier_2", "Venomfang Foil", "Relics", List.of(
            "##RPG Weapon",
            "",
            "A poison-coated rapier with a serpent fang tip.",
            "Each thrust delivers a lethal dose of venom.",
            "",
            "##Skill 1: Envenom",
            "  - Coat the blade in concentrated poison for",
            "    enhanced damage on the next few strikes",
            "  - Cooldown: 4 seconds",
            "",
            "##Skill 2: Lethal Lunge",
            "  - Dash forward with a powerful thrust that",
            "    deals heavy poison damage",
            "  - Cooldown: 7 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_unique_rapier_sw", "Silvered Estoc of the Court", "Relics", List.of(
            "##RPG Weapon",
            "",
            "A holy rapier blessed with silver. Favored by",
            "the noble court's finest swordmasters.",
            "",
            "##Skill 1: Perfect Parry",
            "  - Parry an attack and reflect the damage",
            "    back at the attacker",
            "  - Cooldown: 5 seconds",
            "",
            "##Skill 2: En Passant",
            "  - Dash through the target, phasing past their",
            "    defenses and dealing holy damage",
            "  - Cooldown: 8 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_unique_longsword_1", "Hearthguard Blade", "Relics", List.of(
            "##RPG Weapon",
            "",
            "A fire longsword forged in the eternal hearth.",
            "Its blade burns with protective warmth.",
            "",
            "##Skill 1: Sweeping Cut",
            "  - Perform a wide fire sweep that hits and",
            "    ignites all enemies in front of you",
            "  - Cooldown: 3 seconds",
            "",
            "##Skill 2: Valiant Charge",
            "  - Charge forward with the blade extended,",
            "    dealing fire damage and igniting all hit",
            "  - Cooldown: 6 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_unique_longsword_2", "Nightfall Edge", "Relics", List.of(
            "##RPG Weapon",
            "",
            "A shadow longsword that drinks the light around",
            "it. Each strike saps the victim's strength.",
            "",
            "##Skill 1: Shadow Slash",
            "  - Slash with shadow energy in a cone,",
            "    weakening all enemies hit",
            "  - Cooldown: 4 seconds",
            "",
            "##Skill 2: Dark Rend",
            "  - Rend all nearby enemies with shadow magic",
            "    that bypasses armor and weakens them",
            "  - Cooldown: 8 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_unique_claymore_3", "Northwind Greatsword", "Relics", List.of(
            "##RPG Weapon",
            "",
            "An ice claymore forged in the frozen north.",
            "Its strikes carry the chill of an endless winter.",
            "",
            "##Skill 1: Frostwind Slash",
            "  - Swing with icy force, slowing all enemies",
            "    caught in the arc",
            "  - Cooldown: 5 seconds",
            "",
            "##Skill 2: Avalanche",
            "  - Slam the blade down to create an icy",
            "    shockwave that freezes the ground",
            "  - Cooldown: 10 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_unique_dagger_3", "Whispersting", "Relics", List.of(
            "##RPG Weapon",
            "",
            "A physical dagger so thin it is nearly invisible.",
            "Strikes with surgical precision.",
            "",
            "##Skill 1: Nerve Strike",
            "  - Hit a pressure point to stun the target",
            "    briefly",
            "  - Cooldown: 3 seconds",
            "",
            "##Skill 2: Fan of Knives",
            "  - Throw poisoned knives in a cone, hitting",
            "    enemies in front and leaving a poison cloud",
            "  - Cooldown: 6 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_unique_double_axe_3", "Cindercleaver", "Relics", List.of(
            "##RPG Weapon",
            "",
            "A fire double axe with twin blades of molten",
            "metal. Cleaves through foes with searing heat.",
            "",
            "##Skill 1: Flame Rend",
            "  - Slash with both fiery blades in a dual",
            "    strike combo",
            "  - Cooldown: 4 seconds",
            "",
            "##Skill 2: Pyroclastic Frenzy",
            "  - Enter a frenzy of rapid fire-infused swings",
            "    that hit all nearby enemies",
            "  - Cooldown: 8 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_unique_glaive_3", "Moonreaver Glaive", "Relics", List.of(
            "##RPG Weapon",
            "",
            "An arcane glaive etched with lunar runes.",
            "Its sweeping strikes trail moonlit energy.",
            "",
            "##Skill 1: Crescent Arc",
            "  - Sweep a wide moonlit arc that damages all",
            "    enemies in front of you",
            "  - Cooldown: 4 seconds",
            "",
            "##Skill 2: Lunar Pierce",
            "  - Thrust forward with a piercing moonlit",
            "    lance through all enemies in a line",
            "  - Cooldown: 7 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_unique_hammer_3", "Frostfall Maul", "Relics", List.of(
            "##RPG Weapon",
            "",
            "An ice hammer encased in permafrost. Each slam",
            "sends waves of freezing cold through the ground.",
            "",
            "##Skill 1: Permafrost Slam",
            "  - Slam the hammer down to create a frost",
            "    shockwave around you",
            "  - Cooldown: 5 seconds",
            "",
            "##Skill 2: Glacier Crush",
            "  - Bring the hammer down with full force,",
            "    freezing all nearby enemies solid",
            "  - Cooldown: 10 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_unique_mace_3", "Radiant Morningstar", "Relics", List.of(
            "##RPG Weapon",
            "",
            "A holy mace radiating divine light. Especially",
            "devastating against the undead.",
            "",
            "##Skill 1: Holy Impact",
            "  - Strike with holy power, dealing bonus damage",
            "    against undead enemies",
            "  - Cooldown: 4 seconds",
            "",
            "##Skill 2: Cleansing Smite",
            "  - Smash down with purging light, cleansing",
            "    debuffs and dealing holy damage",
            "  - Cooldown: 8 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_unique_sickle_3", "Frostbite Reaper", "Relics", List.of(
            "##RPG Weapon",
            "",
            "An ice sickle that reaps with the bitter cold",
            "of winter's embrace. Slows all it touches.",
            "",
            "##Skill 1: Frozen Reap",
            "  - Slash with the frozen blade, slowing all",
            "    enemies hit by the swing",
            "  - Cooldown: 4 seconds",
            "",
            "##Skill 2: Winter's Harvest",
            "  - Spin with the sickle, creating a freezing",
            "    vortex that damages and slows all nearby",
            "  - Cooldown: 6 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_unique_spear_3", "Thunderlance", "Relics", List.of(
            "##RPG Weapon",
            "",
            "A lightning spear crackling with electric charge.",
            "Thrusts carry the force of a thunderbolt.",
            "",
            "##Skill 1: Charged Thrust",
            "  - Thrust with lightning-charged force, dealing",
            "    electric damage that arcs to the target",
            "  - Cooldown: 4 seconds",
            "",
            "##Skill 2: Chain Impale",
            "  - Pierce through the target with a lightning",
            "    lance that chains to enemies behind them",
            "  - Cooldown: 7 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_unique_longbow_3", "Galewind Bow", "Relics", List.of(
            "##RPG Weapon",
            "",
            "A wind longbow that fires arrows with the speed",
            "and force of a gale. Blasts enemies away.",
            "",
            "##Skill 1: Wind Arrow",
            "  - Fire a wind-powered arrow that knocks",
            "    back the target with gale force",
            "  - Cooldown: 3 seconds",
            "",
            "##Skill 2: Tornado Shot",
            "  - Fire a volley of wind arrows that blow",
            "    enemies away with powerful gusts",
            "  - Cooldown: 8 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_unique_heavy_crossbow_3", "Arcane Arbalest", "Relics", List.of(
            "##RPG Weapon",
            "",
            "An arcane crossbow infused with magical energy.",
            "Fires bolts of pure mana instead of arrows.",
            "",
            "##Skill 1: Mana Bolt",
            "  - Fire a bolt of concentrated mana that deals",
            "    magic damage, ignoring physical armor",
            "  - Cooldown: 4 seconds",
            "",
            "##Skill 2: Arcane Barrage",
            "  - Rapid-fire a volley of arcane bolts at the",
            "    target in quick succession",
            "  - Cooldown: 10 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_unique_staff_damage_8", "Stormcaller's Rod", "Relics", List.of(
            "##RPG Weapon",
            "",
            "A lightning staff that commands the storms. Its",
            "wielder becomes a conduit for raw electricity.",
            "",
            "##Skill 1: Ball Lightning",
            "  - Launch a concentrated ball of lightning at",
            "    your target, dealing heavy electric damage",
            "  - Cooldown: 4 seconds",
            "",
            "##Skill 2: Tempest",
            "  - Summon a lightning storm that strikes all",
            "    enemies in the area repeatedly",
            "  - Cooldown: 10 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_unique_staff_heal_3", "Living Wood Staff", "Relics", List.of(
            "##RPG Weapon",
            "",
            "A nature staff grown from a living tree. Channels",
            "the life force of nature to mend wounds.",
            "",
            "##Skill 1: Nature's Embrace",
            "  - Channel healing energy to restore health",
            "    to yourself and nearby allies",
            "  - Cooldown: 3 seconds",
            "",
            "##Skill 2: Verdant Bloom",
            "  - Create a healing garden that continuously",
            "    regenerates allies standing within it",
            "  - Cooldown: 9 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_unique_shield_3", "Dreadnought Ward", "Relics", List.of(
            "##RPG Weapon",
            "",
            "A dread shield wreathed in dark fire. Reflects",
            "flames and incinerates those who dare attack.",
            "",
            "##Skill 1: Fear Aura",
            "  - Gain fire resistance and reflect flame",
            "    damage back to any attacker who strikes you",
            "  - Cooldown: 4 seconds",
            "",
            "##Skill 2: Dread Slam",
            "  - Slam the shield to ignite and knock back",
            "    all nearby enemies with a fiery shockwave",
            "  - Cooldown: 8 seconds"
        )));

        // ---- Arsenal Batch 1 Weapons ----

        // Claymores (32x32, 11 dmg)
        allEntries.add(new WikiEntry("weapon_unique_claymore_1", "Cataclysm's Edge", "Relics", List.of(
            "##Arsenal Weapon (Claymore)",
            "",
            "A massive greatsword wreathed in explosive flames.",
            "Its devastating swings leave fire in their wake.",
            "",
            "##Skill 1: Explosive Strike",
            "  - Trigger a fiery explosion at target,",
            "    dealing AOE damage to all nearby enemies",
            "  - Cooldown: 5 seconds",
            "",
            "##Skill 2: Flame Cleave",
            "  - Wide fire-infused slash hitting all",
            "    enemies in front of you",
            "  - Cooldown: 10 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_unique_claymore_2", "Champion's Greatsword", "Relics", List.of(
            "##Arsenal Weapon (Claymore)",
            "",
            "A radiant greatsword blessed with holy light.",
            "Its strikes mend allies while smiting foes.",
            "",
            "##Skill 1: Radiant Strike",
            "  - Strike with holy power, dealing bonus",
            "    holy damage and healing nearby allies",
            "  - Cooldown: 5 seconds",
            "",
            "##Skill 2: Holy Cleave",
            "  - Swing a wide holy arc that damages foes",
            "    and heals allies caught in its path",
            "  - Cooldown: 10 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_unique_claymore_sw", "Apolyon, the Soul-Render", "Relics", List.of(
            "##Arsenal Weapon (Claymore)",
            "",
            "A soul-forged greatsword that hungers for carnage.",
            "Grants berserk fury to those who wield it.",
            "",
            "##Skill 1: Soul Rampage",
            "  - Enter a rage mode, gaining increased",
            "    damage and movement speed temporarily",
            "  - Cooldown: 5 seconds",
            "",
            "##Skill 2: Annihilating Slash",
            "  - Unleash a devastating soul fire strike",
            "    that annihilates enemies before you",
            "  - Cooldown: 10 seconds"
        )));

        // Daggers (16x16, 5 dmg)
        allEntries.add(new WikiEntry("weapon_unique_dagger_1", "Frost Fang", "Relics", List.of(
            "##Arsenal Weapon (Dagger)",
            "",
            "A frost-coated dagger that bites with icy venom.",
            "Each stab leaves the target frozen and sluggish.",
            "",
            "##Skill 1: Frostbite",
            "  - Strike to freeze the target in place",
            "    and apply a heavy slow effect",
            "  - Cooldown: 5 seconds",
            "",
            "##Skill 2: Frozen Barrage",
            "  - Unleash rapid ice strikes that freeze",
            "    all nearby enemies around the target",
            "  - Cooldown: 10 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_unique_dagger_2", "Demonic Shiv", "Relics", List.of(
            "##Arsenal Weapon (Dagger)",
            "",
            "A cursed dagger pulsing with demonic energy.",
            "Drains the life force of those it cuts.",
            "",
            "##Skill 1: Life Leech",
            "  - Stab the target and drain their life,",
            "    healing yourself for the damage dealt",
            "  - Cooldown: 5 seconds",
            "",
            "##Skill 2: Shadow Strike",
            "  - Teleport behind the target and deliver",
            "    a devastating backstab for bonus damage",
            "  - Cooldown: 10 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_unique_dagger_sw", "Crux of the Apocalypse", "Relics", List.of(
            "##Arsenal Weapon (Dagger)",
            "",
            "A dagger forged from the end of worlds.",
            "Sunders armor and marks targets for doom.",
            "",
            "##Skill 1: Armor Sunder",
            "  - Shatter the target's defenses, reducing",
            "    their armor and resistance temporarily",
            "  - Cooldown: 5 seconds",
            "",
            "##Skill 2: Apocalypse Mark",
            "  - Mark the target for doom, weakening them",
            "    and increasing all damage they receive",
            "  - Cooldown: 10 seconds"
        )));

        // Double Axes (32x32, 10 dmg)
        allEntries.add(new WikiEntry("weapon_unique_double_axe_1", "Dual-blade Butcher", "Relics", List.of(
            "##Arsenal Weapon (Double Axe)",
            "",
            "A vicious twin-bladed axe dripping with malice.",
            "Its strikes drain the lifeblood of enemies.",
            "",
            "##Skill 1: Butcher's Leech",
            "  - Hack into the target and drain their life,",
            "    restoring your own health with each hit",
            "  - Cooldown: 5 seconds",
            "",
            "##Skill 2: Cleaving Frenzy",
            "  - Enter a dual spin attack, slashing all",
            "    enemies around you with both blades",
            "  - Cooldown: 10 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_unique_double_axe_2", "Arcanite Reaper", "Relics", List.of(
            "##Arsenal Weapon (Double Axe)",
            "",
            "A legendary double axe infused with dark power.",
            "Its blows spread withering decay to all they touch.",
            "",
            "##Skill 1: Wither Strike",
            "  - Strike with withering force, applying a",
            "    wither effect that decays the target",
            "  - Cooldown: 5 seconds",
            "",
            "##Skill 2: Reaping Swing",
            "  - Swing a wide wither-infused arc that",
            "    hits all enemies in front of you",
            "  - Cooldown: 10 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_unique_double_axe_sw", "Sunreaver War Axe", "Relics", List.of(
            "##Arsenal Weapon (Double Axe)",
            "",
            "A golden war axe radiating martial fury.",
            "Empowers allies and drives the wielder to battle.",
            "",
            "##Skill 1: War Rampage",
            "  - Enter a rage, stacking damage and speed",
            "    bonuses with each successive hit",
            "  - Cooldown: 5 seconds",
            "",
            "##Skill 2: Battle Cry",
            "  - Rally nearby allies, granting strength",
            "    and resistance buffs to the whole party",
            "  - Cooldown: 10 seconds"
        )));

        // Glaives (64x64, 9 dmg)
        allEntries.add(new WikiEntry("weapon_unique_glaive_1", "Hellreaver", "Relics", List.of(
            "##Arsenal Weapon (Glaive)",
            "",
            "A wicked glaive wreathed in hellish flames.",
            "Leaves clouds of fire wherever it strikes.",
            "",
            "##Skill 1: Flame Strike",
            "  - Strike to create a lingering fire cloud",
            "    that burns enemies standing within it",
            "  - Cooldown: 5 seconds",
            "",
            "##Skill 2: Infernal Thrust",
            "  - Thrust with piercing fire, impaling and",
            "    igniting enemies in a line before you",
            "  - Cooldown: 10 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_unique_glaive_2", "Crystalforge Glaive", "Relics", List.of(
            "##Arsenal Weapon (Glaive)",
            "",
            "A crystalline glaive humming with kinetic force.",
            "Its sweeps send shockwaves through enemy ranks.",
            "",
            "##Skill 1: Shockwave",
            "  - Send a piercing shockwave forward that",
            "    damages all enemies in its path",
            "  - Cooldown: 5 seconds",
            "",
            "##Skill 2: Crystal Sweep",
            "  - Perform a wide sweeping strike that",
            "    knocks back all enemies in front of you",
            "  - Cooldown: 10 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_unique_glaive_sw", "Shivering Felspine", "Relics", List.of(
            "##Arsenal Weapon (Glaive)",
            "",
            "A fel-touched glaive pulsing with dark energy.",
            "Spins with demonic force and pierces defenses.",
            "",
            "##Skill 1: Fel Swirl",
            "  - Spin with the glaive, creating a vortex",
            "    of fel-fire that damages nearby enemies",
            "  - Cooldown: 5 seconds",
            "",
            "##Skill 2: Spine Thrust",
            "  - Execute an extended piercing thrust that",
            "    impales enemies at extreme range",
            "  - Cooldown: 10 seconds"
        )));

        // Hammers (64x64, 12 dmg)
        allEntries.add(new WikiEntry("weapon_unique_hammer_1", "Hammer of Destiny", "Relics", List.of(
            "##Arsenal Weapon (Hammer)",
            "",
            "A legendary hammer that shakes the earth itself.",
            "Each blow sends devastating shockwaves outward.",
            "",
            "##Skill 1: Shockwave Slam",
            "  - Slam the ground to create a shockwave",
            "    that damages all enemies in a wide area",
            "  - Cooldown: 5 seconds",
            "",
            "##Skill 2: Destined Impact",
            "  - Deliver a massive overhead blow that",
            "    stuns the target on impact",
            "  - Cooldown: 10 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_unique_hammer_2", "Blackhand", "Relics", List.of(
            "##Arsenal Weapon (Hammer)",
            "",
            "A scorching hammer forged in volcanic fury.",
            "Its impacts erupt with fiery explosions.",
            "",
            "##Skill 1: Explosive Impact",
            "  - Strike to trigger a fiery explosion that",
            "    damages all enemies in a radius",
            "  - Cooldown: 5 seconds",
            "",
            "##Skill 2: Scorched Earth",
            "  - Slam the ground to create a massive fire",
            "    explosion, scorching everything nearby",
            "  - Cooldown: 10 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_unique_hammer_sw", "Hammer of Sanctification", "Relics", List.of(
            "##Arsenal Weapon (Hammer)",
            "",
            "A holy hammer that purifies with divine light.",
            "Heals the faithful and smites the unholy.",
            "",
            "##Skill 1: Holy Radiance",
            "  - Emit a burst of holy light that heals",
            "    allies and deals bonus damage to undead",
            "  - Cooldown: 5 seconds",
            "",
            "##Skill 2: Sanctify",
            "  - Purify the area, healing all allies and",
            "    smiting enemies with divine power",
            "  - Cooldown: 10 seconds"
        )));

        // Maces (32x32, 8 dmg)
        allEntries.add(new WikiEntry("weapon_unique_mace_1", "Bonecracker", "Relics", List.of(
            "##Arsenal Weapon (Mace)",
            "",
            "A brutal mace designed to shatter bone and armor.",
            "Each blow weakens and cripples the target.",
            "",
            "##Skill 1: Armor Sunder",
            "  - Crush the target's defenses, reducing",
            "    their armor rating temporarily",
            "  - Cooldown: 5 seconds",
            "",
            "##Skill 2: Bone Crush",
            "  - Deliver a crippling blow that weakens",
            "    and slows the target significantly",
            "  - Cooldown: 10 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_unique_mace_2", "Stormherald", "Relics", List.of(
            "##Arsenal Weapon (Mace)",
            "",
            "A thunder-charged mace crackling with storm power.",
            "Stuns foes and calls down lightning on impact.",
            "",
            "##Skill 1: Stun Strike",
            "  - Smash the target with stunning force,",
            "    briefly incapacitating them",
            "  - Cooldown: 5 seconds",
            "",
            "##Skill 2: Storm Smash",
            "  - Strike the ground to summon lightning,",
            "    dealing AOE damage to all nearby enemies",
            "  - Cooldown: 10 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_unique_mace_sw", "Archon's Scepter", "Relics", List.of(
            "##Arsenal Weapon (Mace)",
            "",
            "A regal scepter infused with divine authority.",
            "Bolsters the wielder's defenses while judging foes.",
            "",
            "##Skill 1: Guarding Strike",
            "  - Strike the target while gaining a",
            "    defensive buff that reduces incoming damage",
            "  - Cooldown: 5 seconds",
            "",
            "##Skill 2: Archon's Judgment",
            "  - Pass judgment on the target, dealing magic",
            "    damage and granting absorption shields",
            "  - Cooldown: 10 seconds"
        )));

        // Sickles (16x16, 6 dmg)
        allEntries.add(new WikiEntry("weapon_unique_sickle_1", "Toxic Sickle", "Relics", List.of(
            "##Arsenal Weapon (Sickle)",
            "",
            "A venomous sickle dripping with deadly toxins.",
            "Leaves clouds of poison wherever it reaps.",
            "",
            "##Skill 1: Poison Cloud",
            "  - Create a toxic cloud at the target that",
            "    poisons all enemies standing within it",
            "  - Cooldown: 5 seconds",
            "",
            "##Skill 2: Toxic Slash",
            "  - Slash the target with a poisoned blade,",
            "    dealing damage and applying poison",
            "  - Cooldown: 10 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_unique_sickle_2", "Infernal Harvester", "Relics", List.of(
            "##Arsenal Weapon (Sickle)",
            "",
            "A hellfire sickle that reaps souls and flame.",
            "Harvests life force from the dying.",
            "",
            "##Skill 1: Infernal Explosion",
            "  - Trigger a fire explosion at the target,",
            "    dealing AOE damage to nearby enemies",
            "  - Cooldown: 5 seconds",
            "",
            "##Skill 2: Soul Harvest",
            "  - Reap the life force from nearby enemies,",
            "    restoring your health for each target hit",
            "  - Cooldown: 10 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_unique_sickle_sw", "Thalassian Sickle", "Relics", List.of(
            "##Arsenal Weapon (Sickle)",
            "",
            "An elegant elven sickle blessed with nature's grace.",
            "Its multi-hit combo heals the wielder with each reap.",
            "",
            "##Skill 1: Swirling Reap",
            "  - Spin with the sickle, dealing AOE damage",
            "    to all enemies around you",
            "  - Cooldown: 5 seconds",
            "",
            "##Skill 2: Elven Harvest",
            "  - Perform a multi-hit combo that heals you",
            "    for each successful strike landed",
            "  - Cooldown: 10 seconds"
        )));

        // Spears (32x32, 8 dmg)
        allEntries.add(new WikiEntry("weapon_unique_spear_1", "Sonic Spear", "Relics", List.of(
            "##Arsenal Weapon (Spear)",
            "",
            "A frost-tipped spear that strikes at sonic speed.",
            "Its thrusts freeze and pierce through targets.",
            "",
            "##Skill 1: Frostbite Thrust",
            "  - Thrust with freezing force, freezing the",
            "    target in place on impact",
            "  - Cooldown: 5 seconds",
            "",
            "##Skill 2: Sonic Impale",
            "  - Launch a long-range piercing thrust that",
            "    impales all enemies in a line",
            "  - Cooldown: 10 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_unique_spear_2", "Spear of the Damned", "Relics", List.of(
            "##Arsenal Weapon (Spear)",
            "",
            "A cursed spear radiating divine and dark power.",
            "Stuns the wicked and pierces with holy wrath.",
            "",
            "##Skill 1: Stun Thrust",
            "  - Thrust to stun the target, briefly",
            "    incapacitating them on impact",
            "  - Cooldown: 5 seconds",
            "",
            "##Skill 2: Divine Impale",
            "  - Pierce the target with holy power,",
            "    dealing massive bonus damage to evil foes",
            "  - Cooldown: 10 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_unique_spear_sw", "Mounting Vengeance", "Relics", List.of(
            "##Arsenal Weapon (Spear)",
            "",
            "A vengeful spear that grows stronger with each blow.",
            "Drains life and escalates damage relentlessly.",
            "",
            "##Skill 1: Vengeance Leech",
            "  - Thrust to drain the target's life force,",
            "    healing yourself for the damage dealt",
            "  - Cooldown: 5 seconds",
            "",
            "##Skill 2: Mounting Strike",
            "  - Deliver a triple-hit combo with escalating",
            "    damage on each successive strike",
            "  - Cooldown: 10 seconds"
        )));

        // Longsword SW variant (32x32, 9 dmg)
        allEntries.add(new WikiEntry("weapon_unique_longsword_sw", "Dragonscale-Encrusted Longblade", "Relics", List.of(
            "##Arsenal Weapon (Longsword)",
            "",
            "A longsword encrusted with dragon scales.",
            "Its slashes leave trails of dragonfire behind.",
            "",
            "##Skill 1: Armor Sunder",
            "  - Shatter the target's defenses with a",
            "    powerful strike that reduces their armor",
            "  - Cooldown: 5 seconds",
            "",
            "##Skill 2: Dragon Strike",
            "  - Slash with dragonfire, leaving a trail of",
            "    flame that burns enemies in its path",
            "  - Cooldown: 10 seconds"
        )));

        // Longbows (32x32, 7 dmg)
        allEntries.add(new WikiEntry("weapon_unique_longbow_1", "Sunfury Hawk-Bow", "Relics", List.of(
            "##Arsenal Weapon (Longbow)",
            "",
            "A radiant bow blessed by the sun's fury.",
            "Its arrows carry healing light to allies.",
            "",
            "##Skill 1: Radiant Arrow",
            "  - Fire a radiant arrow that heals you",
            "    when it strikes the target",
            "  - Cooldown: 5 seconds",
            "",
            "##Skill 2: Solar Volley",
            "  - Launch a volley of radiant arrows that",
            "    home in on nearby enemies",
            "  - Cooldown: 10 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_unique_longbow_2", "Black Bow of the Betrayer", "Relics", List.of(
            "##Arsenal Weapon (Longbow)",
            "",
            "A dark bow corrupted by betrayal's curse.",
            "Its arrows spread withering decay on impact.",
            "",
            "##Skill 1: Withering Shot",
            "  - Fire a wither-infused arrow that applies",
            "    a wither effect to the target",
            "  - Cooldown: 5 seconds",
            "",
            "##Skill 2: Dark Barrage",
            "  - Rapidly fire multiple wither arrows at",
            "    the target in quick succession",
            "  - Cooldown: 10 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_unique_longbow_sw", "Golden Bow of Quel'Thalas", "Relics", List.of(
            "##Arsenal Weapon (Longbow)",
            "",
            "A legendary elven bow of unmatched precision.",
            "Focuses the archer's power for devastating volleys.",
            "",
            "##Skill 1: Focusing Shot",
            "  - Enter a focused state that increases your",
            "    damage output for a short duration",
            "  - Cooldown: 5 seconds",
            "",
            "##Skill 2: Arrow Rain",
            "  - Fire a massive volley of arrows into the",
            "    sky that rain down on all nearby enemies",
            "  - Cooldown: 10 seconds"
        )));

        // Heavy Crossbows (32x32, 9 dmg)
        allEntries.add(new WikiEntry("weapon_unique_heavy_crossbow_1", "Heavy Crossbow of the Phoenix", "Relics", List.of(
            "##Arsenal Weapon (Heavy Crossbow)",
            "",
            "A phoenix-forged crossbow wreathed in flame.",
            "Its bolts erupt into devastating fire on impact.",
            "",
            "##Skill 1: Flame Bolt",
            "  - Fire a bolt that creates a fire cloud",
            "    at the point of impact",
            "  - Cooldown: 5 seconds",
            "",
            "##Skill 2: Phoenix Shot",
            "  - Launch an explosive bolt that detonates",
            "    in a massive fire explosion on impact",
            "  - Cooldown: 10 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_unique_heavy_crossbow_2", "Necropolis Ballista", "Relics", List.of(
            "##Arsenal Weapon (Heavy Crossbow)",
            "",
            "A necromantic siege crossbow of the undead legions.",
            "Its bolts carry toxic death to all they strike.",
            "",
            "##Skill 1: Toxic Bolt",
            "  - Fire a bolt that creates a poison cloud",
            "    at the point of impact",
            "  - Cooldown: 5 seconds",
            "",
            "##Skill 2: Death Bolt",
            "  - Fire a bolt of pure death that spreads",
            "    wither to all enemies near the impact",
            "  - Cooldown: 10 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_unique_heavy_crossbow_sw", "Crossbow of Relentless Strikes", "Relics", List.of(
            "##Arsenal Weapon (Heavy Crossbow)",
            "",
            "A rapid-fire crossbow built for sustained assault.",
            "Fires bolts in relentless, unending barrages.",
            "",
            "##Skill 1: Multi-Shot",
            "  - Fire bolts at multiple targets at once,",
            "    hitting several enemies simultaneously",
            "  - Cooldown: 5 seconds",
            "",
            "##Skill 2: Relentless Barrage",
            "  - Unleash a rapid-fire stream of bolts at",
            "    the target in quick succession",
            "  - Cooldown: 10 seconds"
        )));

        // Damage Staves (32x32, 4 dmg)
        allEntries.add(new WikiEntry("weapon_unique_staff_damage_1", "Nexus Key", "Relics", List.of(
            "##Arsenal Weapon (Damage Staff)",
            "",
            "A staff attuned to the arcane nexus of power.",
            "Manipulates frost and time to control the battlefield.",
            "",
            "##Skill 1: Frost Bolt",
            "  - Launch a frost bolt that damages and",
            "    slows the target on impact",
            "  - Cooldown: 5 seconds",
            "",
            "##Skill 2: Cooldown Reset",
            "  - Channel nexus energy to instantly reset",
            "    all of your ability cooldowns",
            "  - Cooldown: 10 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_unique_staff_damage_2", "Antonidas's Staff", "Relics", List.of(
            "##Arsenal Weapon (Damage Staff)",
            "",
            "The legendary staff of Archmage Antonidas.",
            "Channels arcane missiles that chain between targets.",
            "",
            "##Skill 1: Chain Reaction",
            "  - Fire a chain missile that bounces between",
            "    up to 3 targets, dealing damage to each",
            "  - Cooldown: 5 seconds",
            "",
            "##Skill 2: Arcane Blast",
            "  - Concentrate arcane energy into a powerful",
            "    blast of pure arcane damage at the target",
            "  - Cooldown: 10 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_unique_staff_damage_3", "Draconic Battle Staff", "Relics", List.of(
            "##Arsenal Weapon (Damage Staff)",
            "",
            "A staff forged in dragonfire and draconic fury.",
            "Commands the breath of dragons to incinerate foes.",
            "",
            "##Skill 1: Flame Cloud",
            "  - Create a lingering fire zone that burns",
            "    all enemies standing within it",
            "  - Cooldown: 5 seconds",
            "",
            "##Skill 2: Dragon Breath",
            "  - Breathe a cone of dragonfire that",
            "    incinerates all enemies in front of you",
            "  - Cooldown: 10 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_unique_staff_damage_4", "Gargoyle's Bite", "Relics", List.of(
            "##Arsenal Weapon (Damage Staff)",
            "",
            "A dark staff that channels gargoyle curses.",
            "Drains life and afflicts targets with decay.",
            "",
            "##Skill 1: Soul Leech",
            "  - Drain life from the target with dark magic,",
            "    healing yourself for the damage dealt",
            "  - Cooldown: 5 seconds",
            "",
            "##Skill 2: Gargoyle's Curse",
            "  - Curse the target with wither and slow,",
            "    crippling them for a duration",
            "  - Cooldown: 10 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_unique_staff_damage_5", "Mage Lord Cane", "Relics", List.of(
            "##Arsenal Weapon (Damage Staff)",
            "",
            "The cane of a legendary mage lord of old.",
            "Unleashes shockwaves and arcane barrages.",
            "",
            "##Skill 1: Shockwave Burst",
            "  - Release shockwaves in 4 directions that",
            "    damage all enemies in their paths",
            "  - Cooldown: 5 seconds",
            "",
            "##Skill 2: Arcane Barrage",
            "  - Rapidly fire bolts of arcane energy at",
            "    the target in quick succession",
            "  - Cooldown: 10 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_unique_staff_damage_6", "Endless Winter", "Relics", List.of(
            "##Arsenal Weapon (Damage Staff)",
            "",
            "A staff radiating the chill of an eternal winter.",
            "Freezes the battlefield in sheets of ice.",
            "",
            "##Skill 1: Frosty Puddle",
            "  - Create a freeze zone that slows and",
            "    damages enemies who step within it",
            "  - Cooldown: 5 seconds",
            "",
            "##Skill 2: Blizzard",
            "  - Summon a massive frost AOE that freezes",
            "    and damages all enemies in a wide area",
            "  - Cooldown: 10 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_unique_staff_damage_sw", "Grand Magister's Staff", "Relics", List.of(
            "##Arsenal Weapon (Damage Staff)",
            "",
            "The supreme staff of the Grand Magister.",
            "Channels surging arcane power for obliteration.",
            "",
            "##Skill 1: Surging Power",
            "  - Stack spell power with each cast, increasing",
            "    your magic damage temporarily",
            "  - Cooldown: 5 seconds",
            "",
            "##Skill 2: Grand Arcanum",
            "  - Unleash a massive arcane explosion that",
            "    devastates all enemies in a wide area",
            "  - Cooldown: 10 seconds"
        )));

        // Healing Staves (32x32, 3 dmg)
        allEntries.add(new WikiEntry("weapon_unique_staff_heal_1", "Crystalline Life-Staff", "Relics", List.of(
            "##Arsenal Weapon (Healing Staff)",
            "",
            "A crystal staff pulsing with life-giving energy.",
            "Mends wounds and shields allies from harm.",
            "",
            "##Skill 1: Radiance",
            "  - Emit a burst of healing light that",
            "    restores health to you and nearby allies",
            "  - Cooldown: 5 seconds",
            "",
            "##Skill 2: Crystal Shield",
            "  - Grant absorption shields to yourself and",
            "    nearby allies for a short duration",
            "  - Cooldown: 10 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_unique_staff_heal_2", "Staff of Immaculate Recovery", "Relics", List.of(
            "##Arsenal Weapon (Healing Staff)",
            "",
            "A holy staff blessed with powers of restoration.",
            "Purifies ailments and mends even grievous wounds.",
            "",
            "##Skill 1: Guardian Remedy",
            "  - Heal the target and grant them absorption",
            "    shields for additional protection",
            "  - Cooldown: 5 seconds",
            "",
            "##Skill 2: Purify",
            "  - Remove all negative effects from yourself",
            "    and nearby allies, restoring purity",
            "  - Cooldown: 10 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_unique_staff_heal_sw", "Golden Staff of the Sin'dorei", "Relics", List.of(
            "##Arsenal Weapon (Healing Staff)",
            "",
            "The sacred staff of the Sin'dorei high priests.",
            "Bestows the most powerful blessings known to exist.",
            "",
            "##Skill 1: Cooldown Touch",
            "  - Touch an ally below 50% HP to instantly",
            "    reset all of their ability cooldowns",
            "  - Cooldown: 5 seconds",
            "",
            "##Skill 2: Sin'dorei Blessing",
            "  - Bestow a massive blessing granting heal,",
            "    regen, absorption, and resistance to allies",
            "  - Cooldown: 10 seconds"
        )));

        // Shields (64x64, 2 dmg)
        allEntries.add(new WikiEntry("weapon_unique_shield_1", "Bulwark of Azzinoth", "Relics", List.of(
            "##Arsenal Weapon (Shield)",
            "",
            "A demonic bulwark forged in the fires of Azzinoth.",
            "Reflects damage back at those who dare strike it.",
            "",
            "##Skill 1: Spiked Block",
            "  - Raise the shield and reflect a portion of",
            "    incoming damage back at the attacker",
            "  - Cooldown: 5 seconds",
            "",
            "##Skill 2: Shield Bash",
            "  - Bash the target with the shield, stunning",
            "    and knocking them back with great force",
            "  - Cooldown: 10 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_unique_shield_2", "Bastion of Light", "Relics", List.of(
            "##Arsenal Weapon (Shield)",
            "",
            "A radiant shield that protects with holy light.",
            "Creates auras of protection for nearby allies.",
            "",
            "##Skill 1: Guarding Aura",
            "  - Emit an aura that reduces damage taken",
            "    for all nearby allies temporarily",
            "  - Cooldown: 5 seconds",
            "",
            "##Skill 2: Light Barrier",
            "  - Create a powerful barrier granting absorption",
            "    shields and resistance to nearby allies",
            "  - Cooldown: 10 seconds"
        )));
        allEntries.add(new WikiEntry("weapon_unique_shield_sw", "Sword Breaker's Bulwark", "Relics", List.of(
            "##Arsenal Weapon (Shield)",
            "",
            "An indestructible bulwark that shatters weapons.",
            "Grants near-invulnerability when raised in defense.",
            "",
            "##Skill 1: Unyielding",
            "  - Brace with the shield, gaining powerful",
            "    damage resistance for a short duration",
            "  - Cooldown: 5 seconds",
            "",
            "##Skill 2: Bulwark Slam",
            "  - Slam the ground with the bulwark, sending",
            "    a massive knockback wave in all directions",
            "  - Cooldown: 10 seconds"
        )));

        // ---- COMBAT OVERHAUL: SPELL SCHOOLS ----

        allEntries.add(new WikiEntry("combat_spell_schools", "Spell Schools", "Relics", List.of(
            "##Spell Schools",
            "",
            "Every spell belongs to a school that determines",
            "its damage type and which attribute scales it.",
            "",
            "##Arcane (purple)",
            "  Scaled by: Arcane Power attribute",
            "  Spells: Arcane Bolt, Arcane Blast, Arcane Missile,",
            "  Arcane Beam, Arcane Blink",
            "  Class: Wizard",
            "",
            "##Fire (orange)",
            "  Scaled by: Fire Damage Bonus attribute",
            "  Spells: Fireball, Fire Blast, Fire Breath,",
            "  Fire Meteor, Fire Wall, Fire Scorch",
            "  Class: Wizard",
            "",
            "##Frost (blue)",
            "  Scaled by: Ice Damage Bonus attribute",
            "  Spells: Frost Shard, Frostbolt, Frost Nova,",
            "  Frost Shield, Frost Blizzard",
            "  Class: Wizard",
            "",
            "##Healing (yellow-green)",
            "  Scaled by: Healing Power attribute",
            "  Spells: Heal, Flash Heal, Holy Shock, Holy Beam,",
            "  Divine Protection, Circle of Healing, Battle Banner,",
            "  Barrier",
            "  Class: Paladin",
            "",
            "##Lightning (yellow)",
            "  Scaled by: Lightning Damage Bonus attribute",
            "  Spells: Chain Lightning, Thunderbolt, Static Field",
            "  Class: Wizard",
            "",
            "##Soul (purple)",
            "  Scaled by: Soul Power attribute",
            "  Spells: Soul Drain, Shadow Bolt, Wither Touch",
            "  Class: Wizard / Rogue",
            "",
            "##Physical Melee (brown)",
            "  Scaled by: Attack Damage",
            "  Spells: Slice and Dice, Shock Powder, Shadow Step,",
            "  Vanish (Rogue), Shattering Throw, Shout, Charge (Warrior)",
            "",
            "##Physical Ranged (green)",
            "  Scaled by: Ranged Damage attribute",
            "  Spells: Power Shot, Entangling Roots, Barrage,",
            "  Magic Arrow",
            "  Class: Ranger"
        )));

        // ---- COMBAT OVERHAUL: KEY SPELLS BY CLASS ----

        allEntries.add(new WikiEntry("combat_spells_wizard", "Wizard Spells", "Relics", List.of(
            "##Wizard Spells (16 total)",
            "",
            "##Arcane School",
            "  Arcane Bolt: Charged projectile, fast and accurate",
            "  Arcane Blast: Charged projectile + Arcane Charge buff",
            "  Arcane Missile: Channeled homing projectiles",
            "  Arcane Beam: Channeled continuous raycast beam",
            "  Arcane Blink: Instant teleport to aim point (15 range)",
            "",
            "##Fire School",
            "  Fireball: Charged projectile, sets enemies ablaze",
            "  Fire Blast: Charged AOE projectile with knockback",
            "  Fire Breath: Channeled cone of flames (40-deg arc)",
            "  Fire Meteor: Charged massive projectile, 6-block AOE",
            "  Fire Wall: Instant cloud of fire at target (8-sec)",
            "  Fire Scorch: Instant 8-block AOE fire burst",
            "",
            "##Frost School",
            "  Frost Shard: Charged projectile with slow",
            "  Frostbolt: Charged projectile, bounces, slows",
            "  Frost Nova: Charged 6-block AOE freeze",
            "  Frost Shield: Self-buff, protective frost barrier",
            "  Frost Blizzard: Channeled 5-block radius blizzard"
        )));
        allEntries.add(new WikiEntry("combat_spells_paladin", "Paladin Spells", "Relics", List.of(
            "##Paladin Spells (9 total)",
            "",
            "##Healing Spells",
            "  Heal: Charged single-target heal (16 range)",
            "  Flash Heal: Fast charged heal, higher coefficient",
            "  Holy Shock: Charged, damages enemies OR heals allies",
            "  Holy Beam: Channeled heal/damage beam (32 range)",
            "  Circle of Healing: AOE heal + Absorption for allies",
            "",
            "##Holy / Support Spells",
            "  Divine Protection: Self-buff, damage reduction aura",
            "  Judgement: Charged holy AOE slam with stun",
            "  Battle Banner: Places healing/buff cloud (10-sec)",
            "  Barrier: Spawns a physical barrier at target"
        )));
        allEntries.add(new WikiEntry("combat_spells_rogue", "Rogue Spells", "Relics", List.of(
            "##Rogue Spells (4 total)",
            "",
            "  Slice and Dice: Self-buff, +attack speed for 10s",
            "  Shock Powder: Instant 5-block AOE stun",
            "  Shadow Step: Instant teleport to target (15 range),",
            "    grants brief damage buff on arrival",
            "  Vanish: Self-buff, become invisible for 8 seconds,",
            "    breaking stealth with an attack = bonus damage"
        )));
        allEntries.add(new WikiEntry("combat_spells_warrior", "Warrior Spells", "Relics", List.of(
            "##Warrior Spells (3 total)",
            "",
            "  Shattering Throw: Charged projectile that applies",
            "    Shatter debuff (armor reduction) for 8 seconds,",
            "    bounces once on terrain",
            "  Shout: Instant 12-block AOE Demoralize debuff,",
            "    weakens all enemies in range",
            "  Charge: Self-buff, massive speed boost for 2 seconds,",
            "    use to close distance on ranged enemies"
        )));
        allEntries.add(new WikiEntry("combat_spells_ranger", "Ranger Spells", "Relics", List.of(
            "##Ranger Spells (4 total)",
            "",
            "  Power Shot: Self-buff, next arrow shot deals",
            "    massively increased damage (Hunter's Mark)",
            "  Entangling Roots: Places root cloud at your feet,",
            "    trapping enemies who walk through for 8 seconds",
            "  Barrage: Charged rapid-fire arrow volley,",
            "    fires multiple arrows in quick succession",
            "  Magic Arrow: Charged piercing projectile that passes",
            "    through ALL enemies in a line (infinite pierce)"
        )));

        // ---- COMBAT OVERHAUL: COMBO SYSTEM ----

        allEntries.add(new WikiEntry("combat_combo_system", "Combat Combo System", "Relics", List.of(
            "##Combat Combo System",
            "",
            "##Weapon Swing Patterns",
            "Each weapon type has a unique combo sequence of",
            "attacks with different hitbox shapes and swing",
            "directions. Attacks cycle through the pattern:",
            "",
            "  Swords: slash right, slash left, stab (3-hit)",
            "  Claymores: wide slash, stab, overhead slam (3-hit)",
            "  Daggers: stab, slash, stab (3-hit, +bonus if dual)",
            "  Maces: vertical slam, horizontal swing (2-hit)",
            "  Great Hammers: overhead slam, side slam (2-hit)",
            "  Double Axes: vertical chop, vertical chop (2-hit)",
            "  Glaives: wide slash, stab (2-hit polearm)",
            "  Spears: single long-range stab (1-hit, max reach)",
            "  Sickles: quick slash, slash (2-hit fast)",
            "  Staves: swing, slam (2-hit two-handed)",
            "  Wands: single direct hit (1-hit, narrow angle)",
            "",
            "##Ability Combos",
            "Cast two abilities with matching elemental tags within",
            "8 seconds to trigger a combo effect:",
            "",
            "  SHATTER: Fire + Ice = frost-fire AOE burst",
            "  COUNTER STRIKE: Shield + Attack = 2x next hit",
            "  LIFE DRAIN: Heal + Attack = 50% lifesteal 8s",
            "  DEEP FREEZE: Ice + Ice = freeze nearby mobs 3s",
            "  INFERNO: Fire + Fire = AOE fire damage over time",
            "  ASSASSINATE: Shadow + Attack = next hit ignores armor",
            "  OVERCHARGE: Lightning + Arcane = +50% ability dmg 5s",
            "  SANCTIFY: Heal + Heal = AOE heal to nearby players",
            "  FORTRESS: Shield + Shield = damage immunity 2s",
            "  ARCANE SURGE: Arcane + Attack = +25% XP gain 10s"
        )));
    }

    // ---- MOBS (Wildlife + Aquarium) ----

    private void buildMobEntries() {
        // Passive
        addMob("minecraft:cow", "Cow", "Passive", "Plains, Forest", "Leather, Raw Beef", "Gentle bovine. Can be milked with a bucket.");
        addMob("minecraft:pig", "Pig", "Passive", "Plains, Forest", "Raw Porkchop", "Oinks around aimlessly. Rideable with a saddle.");
        addMob("minecraft:sheep", "Sheep", "Passive", "Plains, Forest", "Wool, Raw Mutton", "Shearable for wool. Comes in many colors.");
        addMob("minecraft:chicken", "Chicken", "Passive", "Plains, Forest", "Feathers, Raw Chicken, Eggs", "Lays eggs periodically. Slow falling from height.");
        addMob("minecraft:horse", "Horse", "Passive", "Plains, Savanna", "Leather", "Fast rideable mount. Variable speed and jump height.");
        addMob("minecraft:donkey", "Donkey", "Passive", "Plains, Savanna", "Leather", "Slower than horses but can carry a chest.");
        addMob("minecraft:mule", "Mule", "Passive", "Bred from Horse+Donkey", "Leather", "Hybrid with chest storage. Cannot breed further.");
        addMob("minecraft:skeleton_horse", "Skeleton Horse", "Passive", "Lightning strike trap", "Bone", "Undead horse immune to drowning. Fast underwater mount.");
        addMob("minecraft:zombie_horse", "Zombie Horse", "Passive", "Unused (spawn egg only)", "Rotten Flesh", "Undead horse. Does not spawn naturally.");
        addMob("minecraft:rabbit", "Rabbit", "Passive", "Various biomes", "Rabbit Hide, Raw Rabbit, Rabbit Foot (rare)", "Small, fast creature. The Killer Bunny variant is hostile.");
        addMob("minecraft:mooshroom", "Mooshroom", "Passive", "Mushroom Island", "Leather, Raw Beef, Mushrooms", "Cow covered in mushrooms. Can be milked for stew.");
        addMob("minecraft:cat", "Cat", "Passive", "Villages, Witch Huts", "String", "Scares creepers and phantoms. Brings gifts while you sleep.");
        addMob("minecraft:ocelot", "Ocelot", "Passive", "Jungle", "None", "Shy jungle cat. Gaining its trust scares creepers.");
        addMob("minecraft:parrot", "Parrot", "Passive", "Jungle", "Feathers", "Mimics nearby mob sounds. Dances to jukebox music.");
        addMob("minecraft:villager", "Villager", "Passive", "Villages", "None (emerald trades)", "Trades goods for emeralds. Has professions and gossip AI.");
        addMob("minecraft:wandering_trader", "Wandering Trader", "Passive", "Random spawn near player", "None (special trades)", "Offers rare or biome-specific items. Disappears after a while.");
        addMob("minecraft:snow_golem", "Snow Golem", "Passive", "Player-created", "Snowballs", "Built from 2 snow + pumpkin. Throws snowballs at hostiles.");
        addMob("minecraft:iron_golem", "Iron Golem", "Passive", "Villages, Player-created", "Iron Ingot, Poppy", "Powerful village guardian. Built from 4 iron + pumpkin.");
        addMob("minecraft:allay", "Allay", "Passive", "Pillager Outpost, Woodland Mansion", "None", "Collects items matching what you give it. Loves note blocks.");
        addMob("minecraft:strider", "Strider", "Passive", "Nether (lava oceans)", "String", "Walks on lava. Rideable with warped fungus on a stick.");
        addMob("minecraft:camel", "Camel", "Passive", "Desert Villages", "None", "Tall mount. Two riders. Has a dash ability.");
        addMob("minecraft:sniffer", "Sniffer", "Passive", "Hatched from Sniffer Egg", "Moss Block", "Ancient mob. Digs up unique seeds from the ground.");
        addMob("minecraft:armadillo", "Armadillo", "Passive", "Savanna, Badlands", "Armadillo Scute", "Curls into a ball when threatened. Scutes craft wolf armor.");

        // Neutral
        addMob("minecraft:wolf", "Wolf", "Neutral", "Forest, Taiga", "None", "Tameable with bones. Loyal combat companion. Attacks hostiles.");
        addMob("minecraft:fox", "Fox", "Neutral", "Taiga, Snowy Taiga", "None", "Nocturnal hunter. Picks up and carries items in its mouth.");
        addMob("minecraft:panda", "Panda", "Neutral", "Bamboo Jungle", "Bamboo", "Has personality genes (lazy, playful, worried, etc.).");
        addMob("minecraft:bee", "Bee", "Neutral", "Flower Forest, Plains", "None", "Pollinates flowers and crops. Stings once then dies.");
        addMob("minecraft:llama", "Llama", "Neutral", "Savanna, Mountains", "Leather", "Caravan-forming pack animal. Spits at enemies.");
        addMob("minecraft:trader_llama", "Trader Llama", "Neutral", "With Wandering Trader", "Leather", "Guards the Wandering Trader. More aggressive than normal.");
        addMob("minecraft:polar_bear", "Polar Bear", "Neutral", "Snowy biomes", "Raw Cod, Raw Salmon", "Aggressive near cubs. Strong melee attack.");
        addMob("minecraft:goat", "Goat", "Neutral", "Mountains", "None", "Rams entities. Screaming variant is rare.");
        addMob("minecraft:enderman", "Enderman", "Neutral", "All dimensions", "Ender Pearl", "Teleporting mob. Attacks when looked at. Carries blocks.");
        addMob("minecraft:zombified_piglin", "Zombified Piglin", "Neutral", "Nether", "Rotten Flesh, Gold Nugget, Gold Ingot (rare)", "Attacks in groups when one is provoked. Former piglin.");
        addMob("minecraft:spider", "Spider", "Neutral", "Anywhere (dark)", "String, Spider Eye", "Hostile at night, neutral by day. Climbs walls.");
        addMob("minecraft:cave_spider", "Cave Spider", "Neutral", "Mineshafts", "String, Spider Eye", "Smaller and venomous. Inflicts Poison on hit.");
        addMob("minecraft:piglin", "Piglin", "Neutral", "Nether (Crimson Forest, Bastion)", "Gold equipment (rare)", "Barters gold ingots for random items. Hostile without gold armor.");

        // Hostile
        addMob("minecraft:zombie", "Zombie", "Hostile", "Anywhere (dark)", "Rotten Flesh, Iron Ingot (rare), Carrot (rare), Potato (rare)", "Classic undead. Burns in sunlight. Can break doors on Hard.");
        addMob("minecraft:zombie_villager", "Zombie Villager", "Hostile", "Anywhere (dark)", "Rotten Flesh", "Curable with Weakness potion + golden apple.");
        addMob("minecraft:husk", "Husk", "Hostile", "Desert", "Rotten Flesh, Iron Ingot (rare)", "Desert zombie variant. Applies Hunger effect. Does not burn.");
        addMob("minecraft:skeleton", "Skeleton", "Hostile", "Anywhere (dark)", "Bone, Arrow, Bow (rare)", "Ranged attacker with bow. Burns in sunlight.");
        addMob("minecraft:stray", "Stray", "Hostile", "Snowy biomes", "Bone, Arrow, Arrow of Slowness", "Icy skeleton variant. Arrows apply Slowness.");
        addMob("minecraft:wither_skeleton", "Wither Skeleton", "Hostile", "Nether Fortress", "Bone, Coal, Wither Skeleton Skull (rare)", "Tall skeleton with stone sword. Applies Wither effect.");
        addMob("minecraft:bogged", "Bogged", "Hostile", "Swamp, Mangrove Swamp", "Bone, Arrow of Poison", "Swamp skeleton variant. Shoots poison arrows.");
        addMob("minecraft:creeper", "Creeper", "Hostile", "Anywhere (dark)", "Gunpowder, Music Disc (killed by skeleton)", "Silent explosive. Charged by lightning for bigger boom.");
        addMob("minecraft:witch", "Witch", "Hostile", "Swamp Hut, Raids", "Various potions, Glowstone, Redstone, etc.", "Throws harmful potions. Drinks healing and resistance potions.");
        addMob("minecraft:slime", "Slime", "Hostile", "Swamp, Slime chunks", "Slimeball", "Splits into smaller slimes on death. Bouncy.");
        addMob("minecraft:magma_cube", "Magma Cube", "Hostile", "Nether", "Magma Cream", "Nether slime. Splits on death. Immune to fire.");
        addMob("minecraft:phantom", "Phantom", "Hostile", "Sky (insomnia)", "Phantom Membrane", "Attacks players who skip sleeping. Swoops from above.");
        addMob("minecraft:blaze", "Blaze", "Hostile", "Nether Fortress", "Blaze Rod", "Flies and shoots fireballs in volleys of 3.");
        addMob("minecraft:ghast", "Ghast", "Hostile", "Nether (open areas)", "Ghast Tear, Gunpowder", "Large flying mob. Shoots explosive fireballs. Deflectable.");
        addMob("minecraft:piglin_brute", "Piglin Brute", "Hostile", "Bastions", "Golden Axe (rare)", "Elite piglin guard. Always hostile. Does not barter.");
        addMob("minecraft:hoglin", "Hoglin", "Hostile", "Nether (Crimson Forest)", "Raw Porkchop, Leather", "Aggressive boar-like beast. Afraid of warped fungus.");
        addMob("minecraft:zoglin", "Zoglin", "Hostile", "Nether (zombified hoglin)", "Rotten Flesh", "Zombified hoglin. Attacks everything. Cannot be scared.");
        addMob("minecraft:ravager", "Ravager", "Hostile", "Raids", "Saddle", "Massive beast ridden by pillagers. Destroys crops.");
        addMob("minecraft:vindicator", "Vindicator", "Hostile", "Woodland Mansion, Raids", "Emerald, Iron Axe (rare)", "Illager with an axe. High melee damage.");
        addMob("minecraft:evoker", "Evoker", "Hostile", "Woodland Mansion, Raids", "Totem of Undying, Emerald", "Summons Vexes and Fangs. Drops Totem of Undying.");
        addMob("minecraft:pillager", "Pillager", "Hostile", "Outposts, Patrols, Raids", "Crossbow (rare), Arrow", "Crossbow-wielding illager. Triggers raids via Bad Omen.");
        addMob("minecraft:illusioner", "Illusioner", "Hostile", "Unused (spawn egg only)", "Bow (rare)", "Creates duplicates of itself. Casts Blindness.");
        addMob("minecraft:vex", "Vex", "Hostile", "Summoned by Evoker", "None", "Tiny flying mob. Phases through blocks. Short lifespan.");
        addMob("minecraft:shulker", "Shulker", "Hostile", "End Cities", "Shulker Shell", "Hides in shell. Shoots homing levitation projectiles.");
        addMob("minecraft:endermite", "Endermite", "Hostile", "Ender Pearl use (5%)", "None", "Tiny parasitic mob. Endermen attack endermites.");
        addMob("minecraft:silverfish", "Silverfish", "Hostile", "Stronghold, Mountains", "None", "Hides in infested blocks. Calls reinforcements.");
        addMob("minecraft:warden", "Warden", "Hostile", "Deep Dark", "Sculk Catalyst", "Blind apex predator. Detects sound/smell. Extreme damage.");
        addMob("minecraft:breeze", "Breeze", "Hostile", "Trial Chambers", "Breeze Rod", "Wind-powered mob. Shoots wind charges that knock back.");

        // Boss
        addMob("minecraft:ender_dragon", "Ender Dragon", "Boss", "The End", "XP, Dragon Egg (first kill)", "Final boss. Destroys blocks. Healed by End Crystals.");
        addMob("minecraft:wither", "Wither", "Boss", "Player-summoned", "Nether Star", "3-headed boss. Shoots explosive skulls. Destroys terrain.");

        // Ambient
        addMob("minecraft:bat", "Bat", "Ambient", "Caves", "None", "Harmless cave-dweller. Hangs from ceilings.");

        // Aquarium
        addAquaMob("minecraft:cod", "Cod", "Ocean", "Raw Cod, Bone Meal", "Common ocean fish. Swims in schools.");
        addAquaMob("minecraft:salmon", "Salmon", "River, Ocean", "Raw Salmon, Bone Meal", "River and ocean fish. Swims upstream.");
        addAquaMob("minecraft:tropical_fish", "Tropical Fish", "Warm Ocean", "Tropical Fish, Bone Meal", "Colorful fish with 2,700+ pattern variants.");
        addAquaMob("minecraft:pufferfish", "Pufferfish", "Warm Ocean", "Pufferfish, Bone Meal", "Inflates when threatened. Deals Poison on contact.");
        addAquaMob("minecraft:squid", "Squid", "Ocean, River", "Ink Sac", "Common cephalopod. Squirts ink when hit.");
        addAquaMob("minecraft:glow_squid", "Glow Squid", "Deep Ocean, Underground", "Glow Ink Sac", "Bioluminescent squid. Ink makes signs glow.");
        addAquaMob("minecraft:dolphin", "Dolphin", "Ocean", "Raw Cod", "Grants Dolphin's Grace speed boost. Leads to treasure.");
        addAquaMob("minecraft:turtle", "Turtle", "Beach", "Seagrass, Scute (baby growth)", "Returns to home beach to lay eggs. Baby drops Scute.");
        addAquaMob("minecraft:axolotl", "Axolotl", "Lush Cave", "Tropical Fish Bucket", "Cute amphibian. Plays dead to heal. Attacks aquatic hostiles.");
        addAquaMob("minecraft:frog", "Frog", "Swamp", "Slimeball, Froglight (from eating Magma Cube)", "Three variants: Temperate, Cold, Warm. Eats slimes.");
        addAquaMob("minecraft:tadpole", "Tadpole", "Swamp", "None", "Baby frog. Grows into frog variant based on biome temperature.");
        addAquaMob("minecraft:guardian", "Guardian", "Ocean Monument", "Prismarine Shard, Prismarine Crystals, Raw Cod", "Shoots laser beam. Thorns damage on melee attack.");
        addAquaMob("minecraft:elder_guardian", "Elder Guardian", "Ocean Monument", "Prismarine Shard, Prismarine Crystals, Wet Sponge, Raw Cod", "Boss-like guardian. Inflicts Mining Fatigue in large radius.");
        addAquaMob("minecraft:drowned", "Drowned", "Ocean, River (deep)", "Rotten Flesh, Copper Ingot, Trident (rare)", "Underwater zombie. Rare trident-throwing variant.");
    }

    private void addMob(String entityId, String name, String type, String biome, String drops, String notes) {
        allEntries.add(new WikiEntry("mob_" + entityId, name, "Mobs", List.of(
            "##Type: " + type,
            "",
            "##Spawn Biomes",
            "  " + biome,
            "",
            "##Drops",
            "  " + drops,
            "",
            "##Behavior",
            "  " + notes
        )));
    }

    private void addAquaMob(String entityId, String name, String biome, String drops, String notes) {
        allEntries.add(new WikiEntry("mob_" + entityId, name, "Mobs", List.of(
            "##Type: Aquatic",
            "",
            "##Spawn Biomes",
            "  " + biome,
            "",
            "##Drops",
            "  " + drops,
            "",
            "##Behavior",
            "  " + notes
        )));
    }

    // ---- DUNGEONS (4 tiers) ----

    private void buildDungeonEntries() {
        allEntries.add(new WikiEntry("dungeon_normal", "Normal Dungeon", "Dungeons", List.of(
            "##Tier: Normal (Tier 1)",
            "",
            "##Difficulty",
            "  The introductory dungeon tier. Monsters have base",
            "  stats and drop basic loot. A good starting point",
            "  for learning dungeon mechanics.",
            "",
            "##Structure",
            "  - 5-7 rooms total",
            "  - Room types: Entrance, Corridors, Combat rooms,",
            "    Treasure room, Boss room",
            "  - No Trap Corridors or Mini-Boss rooms",
            "",
            "##Monsters",
            "  - DungeonMob (base skeleton-type)",
            "  - Theme-specific mobs at 1.0x HP multiplier",
            "  - Mob damage: standard",
            "",
            "##Bosses (one of three, theme-biased)",
            "  - Wraith (3 phases): Spectral floating boss",
            "    with ranged attacks and teleportation",
            "  - Ossukage (4 phases): Melee bruiser with kunai",
            "    throws and ground-pound attacks",
            "  - Dungeon Keeper (3 phases): Humanoid boss with",
            "    crystalline beam attacks and hollow summons",
            "",
            "##Loot",
            "  - Common to Uncommon quality items",
            "  - 1-2 attribute modifiers per item",
            "  - MegaCoin rewards: 50-150 MC",
            "",
            "##Tips",
            "  - Bring iron or diamond gear",
            "  - No ender pearls allowed inside",
            "  - Soul Anchor saves your gear on death"
        )));
        allEntries.add(new WikiEntry("dungeon_hard", "Hard Dungeon", "Dungeons", List.of(
            "##Tier: Hard (Tier 2)",
            "",
            "##Difficulty",
            "  A significant step up. Monsters hit harder, have",
            "  more health, and new room types appear. Mini-boss",
            "  encounters guard valuable treasure.",
            "",
            "##Structure",
            "  - 6-9 rooms total",
            "  - Room types: Entrance, Corridors, Combat rooms,",
            "    Treasure room, Boss room, Mini-Boss room, Trap",
            "    Corridors (25% chance per corridor)",
            "  - Mini-boss rooms contain elite mobs with 3x HP",
            "",
            "##Monsters",
            "  - All Normal tier mobs plus:",
            "  - Undead Knight (tanky melee, mining fatigue, 25%",
            "    chance to block attacks)",
            "  - DungeonSlime (splits on death, theme-tinted)",
            "  - Hollow (fast, inflicts Blindness, teleports",
            "    when hurt)",
            "  - 1.5x HP multiplier on all mobs",
            "",
            "##Trap Corridors",
            "  - Arrow dispensers with tripwire triggers",
            "  - Sneak to avoid triggering tripwires",
            "",
            "##Bosses",
            "  - Same boss pool as Normal, but with 1.5x HP",
            "    and enhanced attack patterns",
            "",
            "##Loot",
            "  - Uncommon to Rare quality items",
            "  - 2-3 attribute modifiers per item",
            "  - MegaCoin rewards: 150-400 MC"
        )));
        allEntries.add(new WikiEntry("dungeon_nightmare", "Nightmare Dungeon", "Dungeons", List.of(
            "##Tier: Nightmare (Tier 3)",
            "",
            "##Difficulty",
            "  Only for experienced dungeon delvers. Mobs are",
            "  dangerously strong. Trap corridors are common.",
            "  Death here means losing everything unless you",
            "  have a Soul Anchor.",
            "",
            "##Structure",
            "  - 7-11 rooms total",
            "  - All room types available at higher frequency",
            "  - Multiple mini-boss encounters possible",
            "  - Trap corridors at 25% chance, more complex",
            "",
            "##Monsters",
            "  - All Hard tier mobs",
            "  - Rat (fast melee, swarm attacks)",
            "  - 2.5x HP multiplier on all mobs",
            "  - Mobs deal significantly more damage",
            "",
            "##Bosses",
            "  - 2.5x HP bosses with new attack combos",
            "  - Boss rooms contain additional mob spawns",
            "",
            "##Loot",
            "  - Rare to Epic quality items",
            "  - 3-4 attribute modifiers per item",
            "  - MegaCoin rewards: 400-800 MC",
            "  - Chance for dungeon-exclusive drops:",
            "    Void Shard, Infernal Essence",
            "",
            "##Tips",
            "  - Diamond gear minimum, enchanted recommended",
            "  - Bring multiple Soul Anchors",
            "  - Watch for Hollow teleportation ambushes"
        )));
        allEntries.add(new WikiEntry("dungeon_infernal", "Infernal Dungeon", "Dungeons", List.of(
            "##Tier: Infernal (Tier 4)",
            "",
            "##Difficulty",
            "  The ultimate challenge. Reserved for the most",
            "  powerful and prepared players. Extreme mob density",
            "  and the hardest boss fights in the game.",
            "",
            "##Structure",
            "  - 9-14 rooms total",
            "  - Maximum room variety and size",
            "  - Multiple mini-bosses guaranteed",
            "  - Puzzle rooms with unique mechanics",
            "  - Trap corridors are frequent and deadly",
            "",
            "##Monsters",
            "  - All Nightmare tier mobs",
            "  - 4.0x HP multiplier on all mobs",
            "  - Elite variants with special abilities",
            "  - Mob spawns include reinforcements",
            "",
            "##Bosses",
            "  - 4.0x HP bosses with ultimate attack patterns",
            "  - Multi-phase fights with stage transitions",
            "  - Additional minion summons during fight",
            "",
            "##Loot",
            "  - Epic to Legendary quality items",
            "  - 4-5 attribute modifiers per item",
            "  - MegaCoin rewards: 800-2000 MC",
            "  - Guaranteed dungeon-exclusive drops",
            "  - Boss Trophy for display",
            "  - Fang on a Stick (boss chest exclusive)",
            "",
            "##Tips",
            "  - Netherite gear with high enchants required",
            "  - Stock up on golden apples and potions",
            "  - Coordinate with relic abilities for best results",
            "  - Warp Stone can save a doomed run",
            "",
            "##Beyond Infernal",
            "  Clear all 8 bosses here + Prestige 5+ to",
            "  unlock Mythic tier. See 'New Game+' entry."
        )));

        allEntries.add(new WikiEntry("dungeon_mythic", "Mythic Dungeon", "Dungeons", List.of(
            "##Tier: Mythic (Tier 5) - New Game+",
            "",
            "##Unlock Requirements",
            "  - Defeat ALL 8 bosses on Infernal tier",
            "  - Total Prestige level 5+ across all trees",
            "",
            "##Difficulty",
            "  6x HP/damage multiplier on all mobs.",
            "  11-15 rooms with maximum mob density.",
            "  Every encounter is a serious threat.",
            "",
            "##Exclusive Loot",
            "  - Guaranteed mythic-quality relics",
            "  - Diamond + enchanted golden apple drops",
            "  - Totem of Undying chance from bosses",
            "  - Exclusive cosmetic rewards",
            "",
            "##Tips",
            "  - Max prestige bonuses are essential",
            "  - Skill synergies provide crucial edge",
            "  - Party runs strongly recommended"
        )));
        allEntries.add(new WikiEntry("dungeon_eternal", "Eternal Dungeon", "Dungeons", List.of(
            "##Tier: Eternal (Tier 6) - New Game+",
            "",
            "##Unlock Requirements",
            "  - Defeat ALL 8 bosses on Mythic tier",
            "  - Total Prestige level 15+ across all trees",
            "",
            "##Difficulty",
            "  10x HP/damage multiplier on all mobs.",
            "  13-18 rooms. The true endgame challenge.",
            "  Mobs have Resistance, Speed, Fire Resistance.",
            "",
            "##Exclusive Loot",
            "  - Guaranteed legendary-quality relics",
            "  - Netherite gear with max modifiers",
            "  - Nether Stars from boss chests",
            "  - Items unobtainable anywhere else",
            "",
            "##Tips",
            "  - Full prestige (5 per tree) recommended",
            "  - Combat combos are key for DPS",
            "  - Prepare for 30+ minute runs",
            "  - Only the most dedicated will clear this"
        )));

        // Bosses
        allEntries.add(new WikiEntry("dungeon_boss_wraith", "Boss: The Wraith", "Dungeons", List.of(
            "##The Wraith", "  Theme: Void Castle",
            "  HP: 200 | Damage: 8 | Armor: 2", "",
            "##Phases (3)",
            "  Phase 1: Magic Bolt + Minion Summons",
            "  Phase 2: Shadow Pulse (AOE dark damage)",
            "    Gains Speed II, Resistance I",
            "  Phase 3: Minion Rush (5 minions at once)",
            "    Gains Speed III, Strength II", "",
            "Flies with no gravity. Weak to burst damage.")));
        allEntries.add(new WikiEntry("dungeon_boss_ossukage", "Boss: Ossukage", "Dungeons", List.of(
            "##Ossukage", "  Theme: Overgrown Ruin",
            "  HP: 300 | Damage: 12 | Armor: 8", "",
            "##Phases (4)",
            "  Phase 1: Heavy Swing melee",
            "  Phase 2: Dash Charge + Speed II",
            "  Phase 3: Kunai Throw (3 projectiles)",
            "  Phase 4: Berserk Combo + Fire Resistance", "",
            "Circle-strafe to avoid charges.")));
        allEntries.add(new WikiEntry("dungeon_boss_keeper", "Boss: Dungeon Keeper", "Dungeons", List.of(
            "##The Dungeon Keeper", "  Theme: Any",
            "  HP: 250 | Damage: 10 | Armor: 4", "",
            "##Phases (3)",
            "  Phase 1: Sweep melee with knockback",
            "  Phase 2: Crystalline Beam Barrage (3 beams)",
            "  Phase 3: Summons 2 Hollows + Barrage combo", "",
            "Kill summons quickly, dodge beam spread.")));
        allEntries.add(new WikiEntry("dungeon_boss_frostmaw", "Boss: Frostmaw", "Dungeons", List.of(
            "##Frostmaw", "  Theme: Ice Cavern",
            "  HP: 250 | Damage: 10 | Armor: 6", "",
            "##Phases (3)",
            "  Phase 1: Ice Breath (cone, Slowness II)",
            "  Phase 2: Ice Ball (leaves ice trails)",
            "  Phase 3: Blizzard (multi-target freeze)", "",
            "Get close to avoid breath cone.")));
        allEntries.add(new WikiEntry("dungeon_boss_umvuthi", "Boss: Umvuthi", "Dungeons", List.of(
            "##Umvuthi, the Mask Lord", "  Theme: Ancient Temple",
            "  HP: 200 | Damage: 8 | Armor: 4", "",
            "##Phases (3)",
            "  Phase 1: Staff Blast + random debuffs",
            "  Phase 2: Summons 3 minions + mask abilities",
            "  Phase 3: Mass Summon (4 minions) + staff", "",
            "Focus on managing minion spawns.")));
        allEntries.add(new WikiEntry("dungeon_boss_wroughtnaut", "Boss: Wroughtnaut", "Dungeons", List.of(
            "##The Wroughtnaut", "  Theme: Nether Fortress",
            "  HP: 350 | Damage: 14 | Armor: 12", "",
            "##Phases (3)",
            "  Phase 1: Axe Slam (5-block AOE, Slowness)",
            "  Phase 2: Charge (rushes with flame particles)",
            "  Phase 3: Berserk Strike + Fire Resistance", "",
            "Heaviest boss. Cannot be knocked back.",
            "Stay mobile, avoid ground effects.")));
        allEntries.add(new WikiEntry("dungeon_boss_sculptor", "Boss: The Sculptor", "Dungeons", List.of(
            "##The Sculptor", "  Theme: Any",
            "  HP: 250 | Damage: 12 | Armor: 10", "",
            "##Phases (3)",
            "  Phase 1: Stone Pillar (raises pillars under you)",
            "  Phase 2: Boulder Throw (projectile boulders)",
            "  Phase 3: Ground Fissure (terrain hazards)", "",
            "Modifies dungeon terrain. Move away from pillars.")));
        allEntries.add(new WikiEntry("dungeon_boss_chaos", "Boss: Chaos Spawner", "Dungeons", List.of(
            "##The Chaos Spawner", "  Theme: Void Castle",
            "  HP: 300 | Damage: 10 | Armor: 4", "",
            "##Phases (4 - only 4-phase boss)",
            "  Phase 1: Ghost Bullet (passes through walls)",
            "  Phase 2: Summon Wave + ghost bullets",
            "  Phase 3: Void Pulse (8-block AOE darkness)",
            "  Phase 4: Chaos Barrage (rapid shots + summons)", "",
            "Flies. Projectiles ignore walls. Use ranged.")));

        // Themes
        allEntries.add(new WikiEntry("dungeon_themes", "Dungeon Themes", "Dungeons", List.of(
            "##5 Visual Themes", "",
            "##Nether Fortress",
            "  Walls: Nether Bricks | Light: Soul Lantern",
            "  Favored Boss: Wroughtnaut (75%)", "",
            "##Ice Cavern",
            "  Walls: Packed/Blue Ice | Light: Sea Lantern",
            "  Favored Boss: Frostmaw (75%)", "",
            "##Ancient Temple",
            "  Walls: Sandstone | Light: Glowstone",
            "  Favored Boss: Umvuthi (75%)", "",
            "##Void Castle",
            "  Walls: Purpur Block | Light: End Rod",
            "  Favored Boss: Chaos Spawner (75%)", "",
            "##Overgrown Ruin",
            "  Walls: Mossy Stone Bricks | Light: Shroomlight",
            "  Favored Boss: Ossukage (75%)")));

        // Materials
        allEntries.add(new WikiEntry("dungeon_materials", "Dungeon Materials", "Dungeons", List.of(
            "##Exclusive Crafting Materials", "",
            "##Cerulean Ingot (Hard+ tier)",
            "  Cool blue metal from dungeon depths.",
            "  Used for weapon rerolling.", "",
            "##Crystalline Shard (Hard+ tier)",
            "  Fragment of enchanted crystal.", "",
            "##Spectral Silk (Nightmare+ tier)",
            "  Ethereal thread from spectral entities.", "",
            "##Umbra Ingot (Nightmare+ tier)",
            "  Dark metal forged in shadow.", "",
            "##Void Shard",
            "  Fragment of void energy.", "",
            "All 5 materials are used at the Research Table",
            "to reroll weapon bonus stats.")));

        // Soul Anchor
        allEntries.add(new WikiEntry("dungeon_soul_anchor", "Soul Anchor", "Dungeons", List.of(
            "##Soul Anchor (Dungeon Insurance)", "",
            "Carry in your inventory when entering dungeons.",
            "If you die, instead of losing all items,",
            "you survive with 50% health.", "",
            "##Important",
            "  - Consumed on use (1 charge)",
            "  - Without one, death = lose ALL items",
            "  - Stack them for multiple attempts", "",
            "Obtained from dungeon treasure chests.")));

        // Party
        allEntries.add(new WikiEntry("dungeon_party", "Party Dungeons", "Dungeons", List.of(
            "##Party Support (Max 4 Players)", "",
            "##How It Works",
            "  1. Form a party (max 4 players)",
            "  2. Party leader uses the dungeon key",
            "  3. All party members enter together", "",
            "##Rules",
            "  - Cannot join if already in a dungeon",
            "  - Leader logout ejects all members",
            "  - Member disconnect: 5 min reconnect window",
            "  - Each member needs their own Soul Anchors")));

        // Progression
        allEntries.add(new WikiEntry("dungeon_progression", "Tier Progression", "Dungeons", List.of(
            "##How to Unlock Tiers", "",
            "  Normal: Available immediately",
            "  Hard: Clear 1+ Normal dungeon",
            "  Nightmare: Clear 1+ Hard dungeon",
            "  Infernal: Clear 1+ Nightmare dungeon",
            "",
            "##New Game+ Tiers",
            "  Mythic: All 8 Infernal bosses + Prestige 5+",
            "  Eternal: All 8 Mythic bosses + Prestige 15+",
            "  See 'New Game+' wiki entry for details.",
            "",
            "##Getting Keys",
            "  Find and interact with a Royal Herald NPC.",
            "  Accept quests to receive dungeon keys", "",
            "  Herald quests also reward MegaCoins:",
            "  Normal: 50-99 MC | Hard: 100-199 MC",
            "  Nightmare: 200-399 MC | Infernal: 400-799 MC", "",
            "Once unlocked, a tier stays unlocked.")));

        // Reroll
        allEntries.add(new WikiEntry("dungeon_reroll", "Weapon Rerolling", "Dungeons", List.of(
            "##Research Table - Reroll Weapon Stats", "",
            "Right-click the Research Table with empty hand.",
            "Hold a weapon with stat modifiers in main hand.", "",
            "##Costs (MegaCoins + 1 Dungeon Material)",
            "  Common:    50 MC",
            "  Uncommon:  150 MC",
            "  Rare:      400 MC",
            "  Mythic:    1,000 MC",
            "  Legendary: 2,500 MC", "",
            "Keeps rarity unchanged. Re-randomizes all",
            "bonus stats. Major late-game money sink.")));

        allEntries.add(new WikiEntry("dungeon_insurance", "Dungeon Insurance", "Dungeons", List.of(
            "##Dungeon Insurance System",
            "",
            "##Overview",
            "  When you use a Dungeon Key, an insurance screen",
            "  opens before you enter. You can insure individual",
            "  gear pieces so they survive if you die.",
            "",
            "##How It Works",
            "  1. Right-click a Dungeon Key",
            "  2. Insurance screen shows your gear slots",
            "  3. Toggle items you want to insure",
            "  4. See per-item costs based on rarity",
            "  5. Click Ready or Skip Insurance",
            "  6. In a party, ALL members must ready up",
            "",
            "##Costs (per item, from wallet)",
            "  Cost scales by item rarity:",
            "  - Common:   100 MC x tier multiplier",
            "  - Uncommon:  250 MC x tier multiplier",
            "  - Rare:      500 MC x tier multiplier",
            "  - Epic:    1,000 MC x tier multiplier",
            "",
            "  Tier multipliers:",
            "  - Normal: 1x | Hard: 1.5x",
            "  - Nightmare: 2.5x | Infernal: 4x",
            "",
            "##On Death",
            "  - Soul Anchor: saves ALL gear (separate system)",
            "  - Insurance: saves only selected items",
            "  - No insurance: ALL items lost",
            "",
            "##Tips",
            "  - Insurance is paid from your wallet only",
            "  - Session times out after 60 seconds",
            "  - Disconnecting cancels the session"
        )));
    }

    // ---- SKILLS (5 trees) ----

    private void buildSkillEntries() {
        allEntries.add(new WikiEntry("skill_combat", "Combat", "Skills", List.of(
            "##Skill Tree: Combat",
            "Mastery of weapons and warfare",
            "",
            "##XP Sources",
            "  - Killing hostile mobs",
            "  - Dealing melee and ranged damage",
            "  - PvP combat",
            "",
            "##Branches (5)",
            "",
            "##Blade Mastery",
            "  Sword and melee weapon specialization.",
            "  - Keen Edge: +5% melee damage",
            "  - Swift Strikes: +5% attack speed",
            "  - Deep Cuts: +10% melee damage",
            "  - Executioner: +15% damage to enemies below 30% HP",
            "",
            "##Ranged Precision",
            "  Bow and crossbow specialization.",
            "  - Steady Aim: +5% projectile damage",
            "  - Quick Draw: +10% draw speed",
            "  - Eagle Eye: +10% projectile damage",
            "  - Sniper: +20% damage at long range (>15 blocks)",
            "",
            "##Armor Training",
            "  Defensive combat proficiency.",
            "  - Tough Skin: +1 Armor",
            "  - Iron Will: +5% damage reduction",
            "  - Fortified: +2 Armor Toughness",
            "  - Juggernaut: +10% knockback resistance",
            "",
            "##Battle Tactics",
            "  Combat utility and tactics.",
            "  - First Strike: +10% damage on first hit",
            "  - Momentum: kills grant 2s Speed boost",
            "  - War Cry: +5% damage for 10s after killing",
            "  - Berserker: +20% damage below 30% HP",
            "",
            "##Critical Force",
            "  Critical hit specialization.",
            "  - Sharp Impact: +10% crit damage",
            "  - Lucky Strike: +5% crit chance",
            "  - Devastating Blow: +20% crit damage",
            "  - Lethal Precision: crits have 10% lifesteal"
        )));
        allEntries.add(new WikiEntry("skill_mining", "Mining", "Skills", List.of(
            "##Skill Tree: Mining",
            "Expertise in excavation and ore extraction",
            "",
            "##XP Sources",
            "  - Breaking ore blocks",
            "  - Mining stone and deepslate",
            "  - Smelting ores",
            "",
            "##Branches (5)",
            "",
            "##Prospector",
            "  Ore finding and yield specialization.",
            "  - Ore Sense: nearby ores glow briefly",
            "  - Rich Veins: +10% ore drops",
            "  - Motherlode: +20% ore drops",
            "  - Midas Touch: rare gold nugget on any ore",
            "",
            "##Excavation",
            "  Mining speed and efficiency.",
            "  - Quick Pick: +10% mining speed",
            "  - Efficient Strokes: +15% mining speed",
            "  - Tunnel Bore: mine 3x1 with pickaxe",
            "  - Speed Miner: +25% mining speed total",
            "",
            "##Smelting",
            "  Furnace and processing bonuses.",
            "  - Hot Forge: +10% smelting speed",
            "  - Fuel Efficiency: fuel lasts 25% longer",
            "  - Double Smelt: 10% chance for double output",
            "  - Master Smelter: 20% double output chance",
            "",
            "##Durability",
            "  Tool preservation.",
            "  - Careful Hands: +10% tool durability",
            "  - Reinforced: +20% tool durability",
            "  - Unbreaking: 5% chance to not consume durability",
            "  - Eternal Pick: 10% free durability chance",
            "",
            "##Deep Delver",
            "  Underground specialization.",
            "  - Night Vision: see in caves (subtle brightness)",
            "  - Cave Explorer: +10% XP from deep ores",
            "  - Lava Walker: brief fire resistance on lava contact",
            "  - Depth Master: +25% all mining bonuses below Y=0"
        )));
        allEntries.add(new WikiEntry("skill_farming", "Farming", "Skills", List.of(
            "##Skill Tree: Farming",
            "Knowledge of agriculture and animal husbandry",
            "",
            "##XP Sources",
            "  - Harvesting crops",
            "  - Breeding animals",
            "  - Fishing",
            "  - Crafting food items",
            "",
            "##Branches (5)",
            "",
            "##Green Thumb",
            "  Crop growth and yield.",
            "  - Fertile Soil: +10% crop growth speed (nearby)",
            "  - Bountiful Harvest: +15% crop drops",
            "  - Double Yield: 10% chance for double harvest",
            "  - Master Farmer: +25% crop drops total",
            "",
            "##Animal Husbandry",
            "  Animal breeding and care.",
            "  - Gentle Touch: animals breed faster",
            "  - Healthy Stock: +1 HP to bred animals",
            "  - Twin Birth: 10% chance of twin offspring",
            "  - Shepherd: animals follow you from 10 blocks",
            "",
            "##Fishing",
            "  Fishing efficiency.",
            "  - Patient Angler: -10% bite time",
            "  - Lucky Catch: +10% treasure chance",
            "  - Big Fish: +20% fish size/value",
            "  - Master Angler: -25% bite time total",
            "",
            "##Cooking",
            "  Food quality and effects.",
            "  - Home Cook: +1 hunger from cooked food",
            "  - Seasoned Chef: +2 saturation from food",
            "  - Gourmet: cooked food has 10% potion effect chance",
            "  - Master Chef: +3 hunger, +4 saturation total",
            "",
            "##Herbalism",
            "  Plant knowledge and utility.",
            "  - Forager: +10% bonemeal efficiency",
            "  - Plant Lore: identify crop growth stages",
            "  - Natural Remedy: eating crops heals +1 HP",
            "  - Botanist: flowers give brief positive effects"
        )));
        allEntries.add(new WikiEntry("skill_arcane", "Arcane", "Skills", List.of(
            "##Skill Tree: Arcane",
            "Understanding of magical forces and relics",
            "",
            "##XP Sources",
            "  - Using relic abilities",
            "  - Enchanting items",
            "  - Brewing potions",
            "  - Gaining XP orbs",
            "",
            "##Branches (5)",
            "",
            "##Enchanting",
            "  Enchantment table mastery.",
            "  - Arcane Knowledge: better enchantment options",
            "  - Lapis Saver: 20% chance to not consume lapis",
            "  - Enchant Boost: +1 enchantment level bonus",
            "  - Master Enchanter: rare double enchant chance",
            "",
            "##Potion Mastery",
            "  Brewing specialization.",
            "  - Quick Brew: +15% brewing speed",
            "  - Potent Mixtures: +20% potion duration",
            "  - Splash Expert: +15% splash radius",
            "  - Alchemist: new recipes unlocked at max level",
            "",
            "##Relic Attunement",
            "  Relic power enhancement.",
            "  - Ability Power: +5% relic ability damage",
            "  - Cooldown Reduction: -5% relic cooldowns",
            "  - Greater Power: +10% relic ability damage",
            "  - Attunement Mastery: -10% cooldowns total",
            "",
            "##XP Mastery",
            "  Experience point optimization.",
            "  - XP Magnet: +20% XP pickup range",
            "  - XP Boost: +10% XP gained",
            "  - XP Shield: keep 20% of XP on death",
            "  - XP Master: +25% XP gained total",
            "",
            "##Mystic Ward",
            "  Magical defense.",
            "  - Magic Resistance: +5% magic damage reduction",
            "  - Spell Shield: absorb first magic hit every 30s",
            "  - Ward: +10% magic damage reduction",
            "  - Arcane Barrier: 15% chance to negate magic damage"
        )));
        allEntries.add(new WikiEntry("skill_survival", "Survival", "Skills", List.of(
            "##Skill Tree: Survival",
            "Endurance, exploration, and environmental mastery",
            "",
            "##XP Sources",
            "  - Exploring new chunks",
            "  - Taking damage and surviving",
            "  - Eating food",
            "  - Traveling long distances",
            "",
            "##Branches (5)",
            "",
            "##Endurance",
            "  Health and stamina.",
            "  - Tough: +1 Max HP",
            "  - Vitality: +2 Max HP",
            "  - Iron Stomach: hunger depletes 15% slower",
            "  - Undying: +4 Max HP total",
            "",
            "##Explorer",
            "  Movement and exploration.",
            "  - Trailblazer: +5% movement speed",
            "  - Pathfinder: no speed penalty in rough terrain",
            "  - Sprinter: +10% movement speed total",
            "  - Explorer: new chunks grant bonus XP",
            "",
            "##Scavenger",
            "  Resource recovery.",
            "  - Salvager: +10% mob drops",
            "  - Lucky Looter: +5% rare drop chance",
            "  - Resource Recovery: broken blocks have 5% chance",
            "    to drop extra resources",
            "  - Master Scavenger: +20% all drop bonuses",
            "",
            "##Environmental",
            "  Biome and weather adaptation.",
            "  - Cold Resistance: -25% freeze damage",
            "  - Heat Resistance: -25% fire damage",
            "  - Water Breathing: hold breath 50% longer",
            "  - Adaptation: immune to environmental debuffs",
            "",
            "##Economy",
            "  MegaCoin earning bonuses.",
            "  - Coin Finder: +5% MegaCoin drops",
            "  - Bargain Hunter: -5% shop prices",
            "  - Sell Bonus: +10% sell prices",
            "  - Tycoon: +15% MegaCoin from all sources"
        )));

        // ---- SKILL BRANCH ENTRIES (25) ----

        allEntries.add(new WikiEntry("skill_branch_blade_mastery", "Blade Mastery", "Skills", List.of(
            "Combat Tree — Blade Mastery",
            "",
            "Mastery of swords and bladed weapons.",
            "Total: +15 Attack Damage across 5 tiers.",
            "",
            "T1: Sharpened Edge (+1 attack damage)",
            "T2: Honed Blade (+2 dmg, sword durability savings)",
            "T3: Master Swordsman (+3 dmg)",
            "T4: Legendary Blade (+4 dmg)",
            "  Capstone: Executioner",
            "  Kill non-boss mobs below 15% HP instantly.",
            "T5: Godslayer (+5 dmg)",
            "  Enhanced: Executioner threshold raised to 20%",
            "",
            "--- Unlocks ---",
            "Diamond+ Swords, Daggers, Claymores, Glaives",
            "Sharpness V (exclusive generation)",
            "",
            "--- Synergies ---",
            "Bloodblade (+ Berserker): Lifesteal +50%",
            "Arcane Swordsman (+ Spell Blade): Melee kill -1s cooldown"
        )));
        allEntries.add(new WikiEntry("skill_branch_ranged_precision", "Ranged Precision", "Skills", List.of(
            "Combat Tree — Ranged Precision",
            "",
            "Mastery of bows, crossbows, and projectiles.",
            "Total: +40% Crit Chance across 5 tiers.",
            "",
            "T1: Steady Hand (+4% crit chance)",
            "T2: Quick Shot (+8% crit, speed boost after shot)",
            "T3: Marksman (+10% crit chance)",
            "T4: Eagle Eye (+10% crit chance)",
            "  Capstone: Deadeye",
            "  +50% projectile damage at full draw.",
            "T5: True Shot (+8% crit chance)",
            "  Enhanced: Deadeye also applies to crossbows",
            "",
            "--- Unlocks ---",
            "Bows, Crossbows, Spears, Thrown Weapons",
            "Power V, Infinity (exclusive generation)",
            "",
            "--- Synergies ---",
            "Sharpshooter (+ Tactician): Crit proj = 3x dmg",
            "Hawk Eye (+ Explorer): Mark targets through walls"
        )));
        allEntries.add(new WikiEntry("skill_branch_shield_wall", "Shield Wall", "Skills", List.of(
            "Combat Tree — Shield Wall",
            "",
            "Defensive mastery of armor and shields.",
            "Total: +25 Armor, +25% Dodge across 5 tiers.",
            "",
            "T1: Iron Skin (+3 armor, +3% dodge)",
            "T2: Shield Bearer (+5 armor, +5% dodge,",
            "  blocking grants Strength, shield durability save)",
            "T3: Bulwark (+6 armor, +6% dodge)",
            "T4: Sentinel (+6 armor, +6% dodge)",
            "  Capstone: Fortress",
            "  Reflect 30% melee damage back to attacker.",
            "T5: Unbreakable (+5 armor, +5% dodge)",
            "  Enhanced: Fortress reflects ranged damage too",
            "",
            "--- Unlocks ---",
            "Hammers, Maces, Shields, Netherite Armor",
            "",
            "--- Synergies ---",
            "Iron Fortress (+ Endurance): +10% max HP when shielded"
        )));
        allEntries.add(new WikiEntry("skill_branch_berserker", "Berserker", "Skills", List.of(
            "Combat Tree — Berserker",
            "",
            "Reckless fury and lifesteal combat.",
            "Total: +23% Lifesteal, +25% Atk Speed across 5 tiers.",
            "",
            "T1: Blood Scent (+3% lifesteal, +3% atk speed)",
            "T2: Frenzy (+5% lifesteal, +5% atk speed,",
            "  low-HP Haste effect)",
            "T3: Bloodthirst (+5% lifesteal, +6% atk speed)",
            "T4: Warlord (+5% lifesteal, +6% atk speed)",
            "  Capstone: Undying Rage",
            "  Cheat death once per 5 min, gain 3s invulnerability.",
            "T5: Eternal Fury (+5% lifesteal, +5% atk speed)",
            "  Enhanced: Undying Rage cooldown reduced to 3 min",
            "",
            "--- Unlocks ---",
            "Axes, Claymores, Hammers, Sickles, Totem",
            "",
            "--- Synergies ---",
            "Bloodblade (+ Blade Mastery): Lifesteal +50%",
            "Undying (+ Endurance): Rage heals to 50% HP"
        )));
        allEntries.add(new WikiEntry("skill_branch_tactician", "Tactician", "Skills", List.of(
            "Combat Tree — Tactician",
            "",
            "Critical hit specialization and exploit tactics.",
            "Total: +75% Crit Damage across 5 tiers.",
            "",
            "T1: Calculated Strike (+10% crit damage)",
            "T2: Precise Blows (+15% crit dmg,",
            "  crit hits save tool durability)",
            "T3: Lethal Focus (+15% crit damage)",
            "T4: Exploit Opening (+17% crit damage)",
            "  Capstone: Exploit Weakness",
            "  Every 3rd hit on same target deals 2x damage.",
            "T5: Grand Strategist (+18% crit damage)",
            "  Enhanced: Exploit triggers on 2nd hit instead",
            "",
            "--- Unlocks ---",
            "Daggers, Glaives, Sickles",
            "Looting I-III (exclusive generation)",
            "",
            "--- Synergies ---",
            "Sharpshooter (+ Ranged Precision): Crit proj = 3x dmg",
            "Calculated Hunter (+ Hunter Instinct): Crits grant +2s Speed"
        )));
        allEntries.add(new WikiEntry("skill_branch_ore_finder", "Ore Finder", "Skills", List.of(
            "Mining Tree — Ore Finder",
            "",
            "Locating and extracting valuable ores.",
            "Total: +75% Mining XP across 5 tiers.",
            "",
            "T1: Keen Eye (+10% mining XP)",
            "T2: Ore Shimmer (+15% mining XP,",
            "  nearby ores glow through walls)",
            "T3: Magnetic Pull (+15% mining XP,",
            "  item magnet draws drops to you)",
            "T4: Ore Master (+17% mining XP)",
            "  Capstone: Vein Pulse",
            "  Right-click pickaxe on ore to highlight entire vein.",
            "T5: Mother Lode (+18% mining XP)",
            "  Enhanced: Vein Pulse also reveals hidden ores nearby",
            "",
            "--- Unlocks ---",
            "Fortune, Silk Touch, Efficiency III+",
            "(exclusive generation)",
            "",
            "--- Synergies ---",
            "Prospector's Rush (+ Efficient Mining):",
            "  Mining ore grants +20% speed for 5s"
        )));
        allEntries.add(new WikiEntry("skill_branch_efficient_mining", "Efficient Mining", "Skills", List.of(
            "Mining Tree — Efficient Mining",
            "",
            "Raw mining speed and tool preservation.",
            "Total: +75% Mining Speed across 5 tiers.",
            "",
            "T1: Quick Strike (+10% mining speed)",
            "T2: Precision Swing (+15% speed,",
            "  tool durability savings on stone/ore)",
            "T3: Power Mining (+15% mining speed)",
            "T4: Relentless Miner (+17% mining speed)",
            "  Capstone: Shatter Strike",
            "  Breaking a block also breaks adjacent identical blocks.",
            "T5: Unstoppable Force (+18% mining speed)",
            "  Enhanced: Shatter Strike radius increased to 3x3",
            "",
            "--- Unlocks ---",
            "Netherite Tools, Efficiency III+, Silk Touch",
            "",
            "--- Synergies ---",
            "Prospector's Rush (+ Ore Finder):",
            "  Mining ore grants +20% speed for 5s"
        )));
        allEntries.add(new WikiEntry("skill_branch_gem_cutter", "Gem Cutter", "Skills", List.of(
            "Mining Tree — Gem Cutter",
            "",
            "Maximizing loot from every block broken.",
            "Total: +40% Loot Fortune across 5 tiers.",
            "",
            "T1: Keen Cuts (+5% loot fortune)",
            "T2: Jeweler's Eye (+8% loot fortune)",
            "T3: Faceting (+9% loot fortune)",
            "T4: Master Cutter (+9% loot fortune)",
            "  Capstone: Perfect Cut",
            "  Ores always drop max possible items.",
            "T5: Flawless Extraction (+9% loot fortune)",
            "  Enhanced: Perfect Cut also applies to crop drops",
            "",
            "--- Unlocks ---",
            "Fortune I-III (exclusive generation)",
            "",
            "--- Synergies ---",
            "Fortune's Favor (+ Fisherman):",
            "  +10% bonus loot from all sources"
        )));
        allEntries.add(new WikiEntry("skill_branch_tunnel_rat", "Tunnel Rat", "Skills", List.of(
            "Mining Tree — Tunnel Rat",
            "",
            "Underground movement and survival.",
            "Total: +85% Fall Dmg Reduction across 5 tiers.",
            "",
            "T1: Soft Landing (+10% fall dmg reduction)",
            "T2: Sure Footed (+17% fall reduction,",
            "  no knockback while sneaking, faster ladder climbing)",
            "T3: Cave Runner (+18% fall reduction,",
            "  Haste effect in darkness)",
            "T4: Underground Expert (+20% fall reduction)",
            "  Capstone: Earthen Shield",
            "  Take 50% less damage while below Y=32.",
            "T5: Subterranean Master (+20% fall reduction)",
            "  Enhanced: Earthen Shield also grants Resistance I",
            "",
            "--- Unlocks ---",
            "(No exclusive item locks)",
            "",
            "--- Synergies ---",
            "Underground Express (+ Navigator):",
            "  +15% speed while below Y=32"
        )));
        allEntries.add(new WikiEntry("skill_branch_smelter", "Smelter", "Skills", List.of(
            "Mining Tree — Smelter",
            "",
            "Furnace mastery and coin earnings.",
            "Total: +75% MegaCoin Bonus across 5 tiers.",
            "",
            "T1: Hot Forge (+10% megacoin bonus)",
            "T2: Efficient Fuel (+10% megacoin,",
            "  Nearby furnaces smelt 2x faster)",
            "T3: Molten Mastery (+15% megacoin,",
            "  Nearby furnaces smelt 3x faster)",
            "T4: Forge Lord (+17% megacoin bonus)",
            "  Capstone: Auto-Smelt",
            "  Ores drop smelted ingots directly.",
            "T5: Grand Smelter (+18% megacoin bonus)",
            "  Enhanced: Auto-Smelt grants bonus XP per smelt",
            "",
            "--- Unlocks ---",
            "Netherite Tools",
            "",
            "--- Synergies ---",
            "Treasure Alchemist (+ Dungeoneer):",
            "  Smelted items worth +25% sell value"
        )));
        allEntries.add(new WikiEntry("skill_branch_crop_master", "Crop Master", "Skills", List.of(
            "Farming Tree — Crop Master",
            "",
            "Crop growth, yield, and harvesting mastery.",
            "Total: +75% Farming XP across 5 tiers.",
            "",
            "T1: Green Thumb (+10% farming XP)",
            "T2: Tilled Earth (+15% farming XP,",
            "  auto-replant on harvest, bone meal efficiency)",
            "T3: Growth Aura (+15% farming XP,",
            "  nearby crops grow faster passively)",
            "T4: Harvest King (+17% farming XP)",
            "  Capstone: Golden Harvest",
            "  Harvested crops have 10% chance to be golden quality",
            "  (2x output).",
            "T5: Eternal Spring (+18% farming XP)",
            "  Enhanced: Golden Harvest chance raised to 20%",
            "",
            "--- Unlocks ---",
            "(No exclusive item locks)",
            "",
            "--- Synergies ---",
            "Nature's Harmony (+ Animal Handler):",
            "  Animals near farms grow faster"
        )));
        allEntries.add(new WikiEntry("skill_branch_animal_handler", "Animal Handler", "Skills", List.of(
            "Farming Tree — Animal Handler",
            "",
            "Animal breeding, taming, and combat pets.",
            "Total: +40% XP from animal interactions across 5 tiers.",
            "",
            "T1: Gentle Touch (+5% XP)",
            "T2: Loyal Companion (+8% XP,",
            "  tamed pets take 25% less damage, more animal drops)",
            "T3: Pack Leader (+9% XP, pack hunter aura —",
            "  nearby tamed mobs deal +10% damage)",
            "T4: Beast Whisperer (+9% XP)",
            "  Capstone: Beast Bond",
            "  Tamed animals share 20% of your combat buffs.",
            "T5: Alpha (+9% XP)",
            "  Enhanced: Beast Bond shares 35% of buffs",
            "",
            "--- Unlocks ---",
            "Wolf Armor, Saddle, Name Tags",
            "",
            "--- Synergies ---",
            "Nature's Harmony (+ Crop Master):",
            "  Animals near farms grow faster"
        )));
        allEntries.add(new WikiEntry("skill_branch_botanist", "Botanist", "Skills", List.of(
            "Farming Tree — Botanist",
            "",
            "Plant knowledge, food efficiency, and nature magic.",
            "Total: +45% Hunger Efficiency across 5 tiers.",
            "",
            "T1: Plant Lore (+5% hunger efficiency)",
            "T2: Cross-Pollination (+10% hunger efficiency,",
            "  small chance of hybrid crops)",
            "T3: Nature's Gift (+10% hunger efficiency,",
            "  eating food triggers small Regeneration)",
            "T4: Flora Sage (+10% hunger efficiency)",
            "  Capstone: Nature's Bounty",
            "  Harvesting any plant has 5% chance to spawn",
            "  a bonus rare herb with potion effects.",
            "T5: Verdant Master (+10% hunger efficiency)",
            "  Enhanced: Nature's Bounty chance raised to 10%",
            "",
            "--- Unlocks ---",
            "Splash Potions, Lingering Potions",
            "",
            "--- Synergies ---",
            "Gourmet (+ Cook): Food heals +2 HP on eat"
        )));
        allEntries.add(new WikiEntry("skill_branch_cook", "Cook", "Skills", List.of(
            "Farming Tree — Cook",
            "",
            "Food preparation and nourishment mastery.",
            "Total: +5.0 Health Regen across 5 tiers.",
            "",
            "T1: Home Cook (+0.5 health regen)",
            "T2: Seasoned Chef (+0.5 regen,",
            "  +1 extra hunger on eat,",
            "  nearby smokers cook 2x faster)",
            "T3: Sous Chef (+0.75 regen,",
            "  eating grants Absorption I 3s,",
            "  nearby smokers cook 3x faster)",
            "T4: Head Chef (+1.25 regen)",
            "  Capstone: Master Chef",
            "  All food you craft is 'Gourmet Quality' —",
            "  +50% hunger and saturation values.",
            "T5: Legendary Cook (+1.25 regen)",
            "  Enhanced: Gourmet Quality food also grants",
            "  a random 30s buff (Speed, Strength, or Regen)",
            "",
            "--- Unlocks ---",
            "Golden Apples, Suspicious Stew",
            "",
            "--- Synergies ---",
            "Gourmet (+ Botanist): Food heals +2 HP on eat"
        )));
        allEntries.add(new WikiEntry("skill_branch_fisherman", "Fisherman", "Skills", List.of(
            "Farming Tree — Fisherman",
            "",
            "Fishing efficiency and ocean treasure hunting.",
            "Total: +30% Loot Fortune across 5 tiers.",
            "",
            "T1: Patient Angler (+4% loot fortune)",
            "T2: Rainy Day Bonus (+6% loot fortune,",
            "  rain doubles fishing speed)",
            "T3: Ocean Conduit (+7% loot fortune,",
            "  Conduit Power while fishing in ocean)",
            "T4: Treasure Hunter (+7% loot fortune)",
            "  Capstone: Treasure Sense",
            "  Fishing has 15% chance to pull dungeon-tier loot.",
            "T5: Sea King (+6% loot fortune)",
            "  Enhanced: Treasure Sense chance raised to 25%",
            "",
            "--- Unlocks ---",
            "Luck of the Sea, Lure (exclusive generation)",
            "",
            "--- Synergies ---",
            "Fortune's Favor (+ Gem Cutter):",
            "  +10% bonus loot from all sources"
        )));
        allEntries.add(new WikiEntry("skill_branch_relic_lore", "Relic Lore", "Skills", List.of(
            "Arcane Tree — Relic Lore",
            "",
            "Deep understanding of relic power and attunement.",
            "Total: +100% Ability Power across 5 tiers.",
            "",
            "T1: Attunement (+12% ability power)",
            "T2: Resonance (+18% ability power)",
            "T3: Arcane Bond (+20% ability power)",
            "T4: Relic Master (+25% ability power)",
            "  Capstone: Arcane Resonance",
            "  +25% magic damage, relic abilities chain to",
            "  a second nearby target.",
            "T5: Transcendence (+25% ability power)",
            "  Enhanced: Chain range doubled, chains to 2 targets",
            "",
            "--- Unlocks ---",
            "Crowns, Masks, Tomes, equip 3+ Relics at once",
            "",
            "--- Synergies ---",
            "Arcane Flow (+ Mana Weaver):",
            "  Ability kills restore 15% cooldown"
        )));
        allEntries.add(new WikiEntry("skill_branch_enchanter", "Enchanter", "Skills", List.of(
            "Arcane Tree — Enchanter",
            "",
            "Enchantment table mastery and arcane crafting.",
            "Total: +75% Arcane XP across 5 tiers.",
            "",
            "T1: Lapis Sage (+10% arcane XP)",
            "T2: Cost Cutter (+15% arcane XP,",
            "  enchanting costs 1 fewer level)",
            "T3: Efficient Enchant (+15% arcane XP,",
            "  further enchanting cost reduction)",
            "T4: Arcane Scholar (+17% arcane XP)",
            "  Capstone: +1 Enchant Level",
            "  All enchantments applied are +1 level above normal.",
            "T5: Grand Enchanter (+18% arcane XP)",
            "  Enhanced: +1 becomes +2 for weapon enchantments",
            "",
            "--- Unlocks ---",
            "Mending (exclusive), ALL max-level enchantments",
            "",
            "--- Synergies ---",
            "(No active synergies)"
        )));
        allEntries.add(new WikiEntry("skill_branch_mana_weaver", "Mana Weaver", "Skills", List.of(
            "Arcane Tree — Mana Weaver",
            "",
            "Cooldown reduction and mana flow mastery.",
            "Total: +53% CDR across 5 tiers.",
            "",
            "T1: Quick Cast (+7% CDR)",
            "T2: Mana Flow (+10% CDR)",
            "T3: Arcane Haste (+11% CDR)",
            "T4: Temporal Mastery (+12% CDR)",
            "  Capstone: Mana Surge",
            "  Every 5th ability cast is instant (no cooldown).",
            "T5: Time Lord (+13% CDR)",
            "  Enhanced: Mana Surge triggers every 4th cast",
            "",
            "--- Unlocks ---",
            "Staves, Ender Pearls, Mending",
            "",
            "--- Synergies ---",
            "Arcane Flow (+ Relic Lore):",
            "  Ability kills restore 15% cooldown",
            "Spirit Conductor (+ Summoner):",
            "  Summons inherit 30% of your CDR"
        )));
        allEntries.add(new WikiEntry("skill_branch_spell_blade", "Spell Blade", "Skills", List.of(
            "Arcane Tree — Spell Blade",
            "",
            "Elemental damage infusion on melee attacks.",
            "Total: +22 Fire, +21 Lightning, +22 Shadow across tiers.",
            "",
            "T1: Ember Touch (+4 fire, +3 lightning, +4 shadow)",
            "T2: Charged Blade (+4 fire, +4 lightning, +4 shadow)",
            "T3: Shadow Strike (+5 fire, +5 lightning, +5 shadow)",
            "T4: Tri-Element (+5 fire, +5 lightning, +5 shadow)",
            "  Capstone: Elemental Mastery",
            "  Melee hits cycle Fire/Lightning/Shadow, each",
            "  applying a unique debuff (burn/stun/weaken).",
            "T5: Elemental Lord (+4 fire, +4 lightning, +4 shadow)",
            "  Enhanced: Debuffs last twice as long",
            "",
            "--- Unlocks ---",
            "Damage Staves, Tomes",
            "",
            "--- Synergies ---",
            "Arcane Swordsman (+ Blade Mastery):",
            "  Melee kill reduces ability cooldown by 1s"
        )));
        allEntries.add(new WikiEntry("skill_branch_summoner", "Summoner", "Skills", List.of(
            "Arcane Tree — Summoner",
            "",
            "Conjuration, summoned allies, and spell range.",
            "Total: +55% Ability Power, +40% Spell Range across tiers.",
            "",
            "T1: Minor Conjuration (+7% ability power, +5% range)",
            "T2: Spirit Call (+10% ability power, +8% range)",
            "T3: Greater Summon (+12% ability power, +9% range)",
            "T4: Conjuration Master (+13% ability power, +9% range)",
            "  Capstone: Spectral Familiar",
            "  Summon a permanent spectral wolf/cat that fights",
            "  alongside you and scales with Ability Power.",
            "T5: Grand Summoner (+13% ability power, +9% range)",
            "  Enhanced: Spectral Familiar can revive once per 5 min",
            "",
            "--- Unlocks ---",
            "Healing Staves, Crowns",
            "",
            "--- Synergies ---",
            "Spirit Conductor (+ Mana Weaver):",
            "  Summons inherit 30% of your CDR"
        )));
        allEntries.add(new WikiEntry("skill_branch_explorer", "Explorer (Branch)", "Skills", List.of(
            "Survival Tree — Explorer",
            "",
            "Movement speed and exploration rewards.",
            "Total: +75% Survival XP, +13% Move Speed across tiers.",
            "",
            "T1: Wanderer (+10% survival XP, +1% speed)",
            "T2: Pathfinder (+15% survival XP, +3% speed,",
            "  reduced hunger drain while sprinting)",
            "T3: Trailblazer (+15% survival XP, +3% speed,",
            "  night vision in caves)",
            "T4: Cartographer (+17% survival XP, +3% speed)",
            "  Capstone: Cartographer",
            "  Auto-map explored areas, reveal structures",
            "  within 200 blocks on map.",
            "T5: World Walker (+18% survival XP, +3% speed)",
            "  Enhanced: Structure reveal range increased to 400",
            "",
            "--- Unlocks ---",
            "Elytra, Ender Chest, Shulker Boxes",
            "",
            "--- Synergies ---",
            "Adventurer (+ Dungeoneer): +20% dungeon XP",
            "Hawk Eye (+ Ranged Precision): Mark targets through walls"
        )));
        allEntries.add(new WikiEntry("skill_branch_endurance", "Endurance (Branch)", "Skills", List.of(
            "Survival Tree — Endurance",
            "",
            "Maximum health and damage resistance.",
            "Total: +18 Max Health across 5 tiers.",
            "",
            "T1: Tough (+2 max health)",
            "T2: Resilient (+4 max health,",
            "  slower hunger depletion)",
            "T3: Stalwart (+4 max health)",
            "T4: Ironclad (+4 max health)",
            "  Capstone: Second Wind",
            "  When below 20% HP, gain Regeneration III for 5s.",
            "  60s cooldown.",
            "T5: Immortal Constitution (+4 max health)",
            "  Enhanced: Second Wind cooldown reduced to 40s",
            "",
            "--- Unlocks ---",
            "Totem of Undying, Netherite Armor, Turtle Shell",
            "",
            "--- Synergies ---",
            "Undying (+ Berserker): Rage heals to 50% HP",
            "Iron Fortress (+ Shield Wall): +10% max HP when shielded"
        )));
        allEntries.add(new WikiEntry("skill_branch_hunter_instinct", "Hunter Instinct", "Skills", List.of(
            "Survival Tree — Hunter Instinct",
            "",
            "Tracking prey and gaining combat XP.",
            "Total: +53% Combat XP across 5 tiers.",
            "",
            "T1: Predator's Eye (+7% combat XP)",
            "T2: Threat Detection (+10% combat XP,",
            "  hostile mobs glow within 16 blocks)",
            "T3: Kill Momentum (+11% combat XP,",
            "  kills grant Speed I for 5s)",
            "T4: Apex Predator (+12% combat XP)",
            "  Capstone: Predator's Mark",
            "  Mark a target — take 25% more damage from you,",
            "  visible through walls. 30s cooldown.",
            "T5: Alpha Hunter (+13% combat XP)",
            "  Enhanced: Predator's Mark can mark 2 targets",
            "",
            "--- Unlocks ---",
            "Bows, Crossbows, Spears",
            "Looting (exclusive generation)",
            "",
            "--- Synergies ---",
            "Calculated Hunter (+ Tactician):",
            "  Crits grant +2s Speed"
        )));
        allEntries.add(new WikiEntry("skill_branch_navigator", "Navigator", "Skills", List.of(
            "Survival Tree — Navigator",
            "",
            "Movement speed, terrain mastery, and travel.",
            "Total: +23% Move Speed across 5 tiers.",
            "",
            "T1: Light Feet (+3% move speed)",
            "T2: Terrain Master (+5% speed,",
            "  no slow on soul sand/mud, faster boats)",
            "T3: Agile (+5% speed,",
            "  Jump Boost I passively)",
            "T4: Fleet Footed (+5% speed)",
            "  Capstone: Windwalker",
            "  Double-tap jump to dash forward 5 blocks.",
            "  3s cooldown.",
            "T5: Gale Runner (+5% speed)",
            "  Enhanced: Windwalker dash distance = 8 blocks",
            "",
            "--- Unlocks ---",
            "Elytra, Movement Relics, Saddle",
            "",
            "--- Synergies ---",
            "Underground Express (+ Tunnel Rat):",
            "  +15% speed while below Y=32"
        )));
        allEntries.add(new WikiEntry("skill_branch_dungeoneer", "Dungeoneer", "Skills", List.of(
            "Survival Tree — Dungeoneer",
            "",
            "Dungeon loot, survival, and economy bonuses.",
            "Total: +30% Loot Fortune, +35% Sell Bonus across tiers.",
            "",
            "T1: Delver (+4% loot fortune, +5% sell bonus)",
            "T2: Dungeon Rat (+6% loot, +7% sell)",
            "T3: Treasure Seeker (+7% loot, +8% sell)",
            "T4: Vault Breaker (+7% loot, +8% sell)",
            "  Capstone: Delver's Fortune",
            "  Dungeon chests always contain 1 extra rare item.",
            "T5: Dungeon Lord (+6% loot, +7% sell)",
            "  Enhanced: Extra item can be epic-tier",
            "",
            "--- Unlocks ---",
            "ALL dungeon unique gear, Umvuthana Masks,",
            "Shulker Boxes",
            "",
            "--- Synergies ---",
            "Adventurer (+ Explorer): +20% dungeon XP",
            "Treasure Alchemist (+ Smelter):",
            "  Smelted items worth +25% sell value"
        )));

        // ---- SKILL SYSTEM ENTRIES (5) ----

        allEntries.add(new WikiEntry("skill_system_item_locks", "Item Lock System", "Skills", List.of(
            "##Item Lock System",
            "",
            "Many powerful items are LOCKED behind skill branches.",
            "There are two types of locks:",
            "",
            "--- Use Locks ---",
            "You cannot USE the item until you reach the required",
            "tier in the relevant branch. Attempting to use a locked",
            "item shows a warning message. Examples:",
            "  - Netherite Armor: Shield Wall T3 or Endurance T3",
            "  - Elytra: Explorer T4 or Navigator T4",
            "  - Totem of Undying: Berserker T3 or Endurance T3",
            "",
            "--- Generation Locks ---",
            "Certain enchantments only GENERATE (appear in loot,",
            "villager trades, enchanting table) if you have the",
            "required branch tier. Examples:",
            "  - Sharpness V: Blade Mastery T3+",
            "  - Mending: Enchanter T4+",
            "  - Infinity: Ranged Precision T3+",
            "  - Fortune I-III: Gem Cutter T2+",
            "",
            "--- Trading ---",
            "Locked items CAN still be traded between players.",
            "The recipient must meet the use-lock requirement",
            "to equip/use the item."
        )));
        allEntries.add(new WikiEntry("skill_system_synergies", "Synergies", "Skills", List.of(
            "##Synergy System",
            "",
            "Investing in TWO specific branches from different",
            "trees unlocks a powerful Synergy bonus. Synergies",
            "activate when both branches reach T3+.",
            "",
            "--- All 16 Synergies ---",
            "",
            "1. Bloodblade (Blade Mastery + Berserker)",
            "   Lifesteal healing increased by 50%.",
            "2. Sharpshooter (Ranged Precision + Tactician)",
            "   First hit on a new target always crits.",
            "3. Prospector's Rush (Efficient Mining + Ore Finder)",
            "   Mining ores grants brief Haste I.",
            "4. Arcane Flow (Relic Lore + Mana Weaver)",
            "   Ability kills reduce other cooldowns by 2s.",
            "5. Adventurer (Explorer + Dungeoneer)",
            "   10% faster movement speed in dungeons.",
            "6. Undying (Berserker + Endurance)",
            "   Heal 1 HP/s while below 30% health.",
            "7. Iron Fortress (Shield Wall + Endurance)",
            "   Resistance I when below 50% HP.",
            "8. Gourmet (Cook + Botanist)",
            "   Food-related buffs last 50% longer.",
            "9. Arcane Swordsman (Spell Blade + Blade Mastery)",
            "   Melee kills: 20% chance to reduce cooldowns 1s.",
            "10. Treasure Alchemist (Smelter + Dungeoneer)",
            "    Dungeon loot has higher quality tier.",
            "11. Spirit Conductor (Mana Weaver + Summoner)",
            "    Spectral Familiar deals double damage.",
            "12. Hawk Eye (Ranged Precision + Explorer)",
            "    +15% ranged damage when outdoors.",
            "13. Nature's Harmony (Crop Master + Animal Handler)",
            "    Crops and animals grow 25% faster nearby.",
            "14. Fortune's Favor (Gem Cutter + Fisherman)",
            "    10% chance to double rare drops.",
            "15. Calculated Hunter (Tactician + Hunter Instinct)",
            "    Consecutive kills: stacking +5% damage (max 25%).",
            "16. Underground Express (Navigator + Tunnel Rat)",
            "    +15% movement speed below Y=50."
        )));
        allEntries.add(new WikiEntry("skill_system_prestige", "Prestige System", "Skills", List.of(
            "##Prestige System",
            "",
            "Once you reach level 50 in a skill tree,",
            "you can PRESTIGE that tree.",
            "",
            "--- How It Works ---",
            "Prestiging resets all nodes in that tree but grants",
            "a permanent +5% bonus to the tree's primary stat",
            "per prestige level.",
            "",
            "--- Prestige Levels ---",
            "  Prestige 1: +5% primary stat",
            "  Prestige 2: +10% primary stat",
            "  Prestige 3: +15% primary stat, unlock 3rd branch",
            "  Prestige 4: +20% primary stat",
            "  Prestige 5: +25% primary stat (MAX)",
            "",
            "--- Third Branch ---",
            "At Prestige 3+, you can invest points into a THIRD",
            "branch in that tree (normally limited to 2 active",
            "branches). This allows powerful triple-branch builds.",
            "",
            "--- Prestige Cost ---",
            "  Prestige 1: 100 MegaCoins",
            "  Prestige 2: 200 MegaCoins",
            "  Prestige 3: 300 MegaCoins",
            "  Prestige 4: 400 MegaCoins",
            "  Prestige 5: 500 MegaCoins",
            "",
            "--- Notes ---",
            "  - Prestige is per-tree, not global",
            "  - Level resets to 0, all nodes cleared",
            "  - Earns 25 Marks of Mastery per prestige",
            "  - Synergies re-activate once branches reach T3 again",
            "  - Max prestige is 5 (cannot exceed)",
            "  - Total prestige across all trees unlocks NG+ tiers",
            "",
            "--- Cosmetics ---",
            "Prestige also unlocks visual rewards:",
            "  - Gold star badges in chat (1 per prestige)",
            "  - Particle auras (gold totem particles)",
            "  - Max prestige: full golden END_ROD aura",
            "  - Tab list name coloring (gold/orange/red)",
            "See the Prestige Cosmetics entry for details."
        )));
        allEntries.add(new WikiEntry("skill_system_mastery_marks", "Marks of Mastery", "Skills", List.of(
            "##Marks of Mastery",
            "",
            "A cross-system prestige currency awarded for",
            "major milestones across all MegaMod systems.",
            "",
            "--- How to Earn ---",
            "  Skill Prestige: 25 marks per prestige",
            "  Dungeon Clears: 5-40 marks (by tier)",
            "  Museum Completion: 15-50 marks",
            "  Bounty Milestones: 10-20 marks",
            "  Colony/Siege: 10-15 marks",
            "",
            "--- What to Spend On ---",
            "  Cosmetic titles (chat display names)",
            "  Permanent +5% coin bonus",
            "  Permanent +5% XP bonus",
            "  Exclusive furniture blueprints",
            "",
            "Milestones are one-time awards. You cannot earn",
            "the same milestone twice."
        )));
        allEntries.add(new WikiEntry("skill_system_passive_mastery", "Passive Mastery", "Skills", List.of(
            "##Passive Mastery",
            "",
            "Unspent skill points are not wasted — they provide",
            "a passive bonus to your primary stat.",
            "",
            "--- How It Works ---",
            "For every unspent skill point you hold, you gain",
            "+0.5% to that tree's primary stat.",
            "",
            "--- Example ---",
            "If you have 10 unspent Mining points, you gain +5%",
            "mining speed passively even without spending them.",
            "",
            "--- Strategy ---",
            "This system rewards players who bank points while",
            "waiting for the right branch investment. It also",
            "helps new players who haven't decided on a build.",
            "",
            "--- Limits ---",
            "  - Only applies to the tree the points belong to",
            "  - Bonus is recalculated when points are spent",
            "  - Does not stack with spent-node bonuses",
            "  - Maximum meaningful reserve: ~50 points (+25%)"
        )));
        allEntries.add(new WikiEntry("skill_system_weekly_challenges", "Weekly Challenges", "Skills", List.of(
            "##Weekly Challenges",
            "",
            "Every week, 3 rotating skill challenges appear.",
            "Complete them for bonus skill XP and MegaCoins.",
            "",
            "--- How to View ---",
            "Open the Challenges app on the Computer, or",
            "use /megamod challenges in chat.",
            "",
            "--- Challenge Types ---",
            "  - Kill X mobs / kill with crits",
            "  - Mine X ores / specific blocks",
            "  - Harvest crops / sweet berries",
            "  - Catch X fish / breed animals",
            "  - Explore X chunks / discover biomes",
            "  - Craft items / tools / smelt / brew / enchant",
            "  - Complete X dungeons",
            "  - Earn X MegaCoins",
            "  - Use X relic abilities",
            "  - Walk X blocks / breed unique species",
            "",
            "--- Rewards ---",
            "Each challenge grants:",
            "  - 500-2000 bonus XP in the relevant tree",
            "  - 50-200 MegaCoins",
            "  - Completing all 3 grants a bonus chest with",
            "    a random relic shard or dungeon key.",
            "",
            "--- Reset ---",
            "Challenges reset every Monday at midnight server time.",
            "Uncompleted challenges do not carry over."
        )));

        // Chat Badges
        allEntries.add(new WikiEntry("skill_system_chat_badges", "Chat Badges", "Skills", List.of(
            "##Chat Badges",
            "",
            "Earn a visible title badge in chat and the tab",
            "list by specializing in a skill branch.",
            "",
            "--- How Badges Work ---",
            "Reach Tier 3+ in any branch to earn a badge.",
            "The badge shows your highest specialization:",
            "  - T3: Branch name (e.g. [Blade Master])",
            "  - T4: Capstone name (e.g. [Legendary Blade])",
            "  - T5: Ultimate name (e.g. [Godslayer])",
            "",
            "If you have multiple T3+ branches, the badge",
            "uses the highest tier. Ties go to the branch",
            "with the most nodes unlocked.",
            "",
            "--- Prestige Stars ---",
            "Players with prestige levels gain gold stars",
            "before their badge. One star per prestige level.",
            "",
            "--- Toggling ---",
            "Toggle your badge on/off with /megamod badge",
            "or via the Settings app on your Computer."
        )));

        // Party Buffs
        allEntries.add(new WikiEntry("skill_system_party_buffs", "Party Buffs", "Skills", List.of(
            "##Party Combo Buffs",
            "",
            "When party members have complementary skill",
            "specializations (T3+), the whole party gets",
            "a bonus buff! Checked every 10 seconds.",
            "",
            "--- Available Combos ---",
            "",
            "##Vanguard",
            "  - Requires: Shield Wall + Blade Mastery",
            "  - Buff: Resistance I + Strength I",
            "",
            "##Sniper Duo",
            "  - Requires: Ranged Precision + Tactician",
            "  - Buff: Strength I",
            "",
            "##Healer's Guard",
            "  - Requires: Mana Weaver + Endurance",
            "  - Buff: Regeneration I",
            "",
            "##Arcane Artillery",
            "  - Requires: Spell Blade + Ranged Precision",
            "  - Buff: Strength I",
            "",
            "##Nature's Alliance",
            "  - Requires: Crop Master + Animal Handler",
            "  - Buff: Saturation I",
            "",
            "##Dungeon Delvers",
            "  - Requires: Dungeoneer + Explorer",
            "  - Buff: Speed I",
            "",
            "##Mining Expedition",
            "  - Requires: Efficient Mining + Ore Finder",
            "  - Buff: Haste I",
            "",
            "##War Party",
            "  - Requires: Berserker + Shield Wall",
            "  - Buff: Resistance I + Strength I",
            "",
            "Only one combo can be active at a time.",
            "The highest priority match is chosen."
        )));

        // Prestige Cosmetics
        allEntries.add(new WikiEntry("skill_system_prestige_cosmetics", "Prestige Cosmetics", "Skills", List.of(
            "##Prestige Cosmetics",
            "",
            "Prestiging your skill trees unlocks visual",
            "cosmetic effects visible to other players.",
            "",
            "--- Particle Auras ---",
            "Reach T4 in any branch for subtle particles:",
            "  - Combat: Red crit particles",
            "  - Mining: Brown/orange dust particles",
            "  - Farming: Green happy villager particles",
            "  - Arcane: Purple enchant particles",
            "  - Survival: White cloud particles",
            "",
            "T5 upgrades particles to be more frequent",
            "with an upward drift effect.",
            "",
            "--- Prestige Effects ---",
            "  - Prestige 1+: Gold totem particles mixed in",
            "  - Prestige 5 (max): Full golden END_ROD aura",
            "    circling your character",
            "",
            "--- Tab List Colors ---",
            "  - Prestige 1-2: Light gold name",
            "  - Prestige 3-4: Orange name",
            "  - Prestige 5: Red bold name",
            "",
            "--- Chat Stars ---",
            "Gold stars appear before your badge,",
            "one per total prestige level.",
            "",
            "Toggle particles via Settings app",
            "(Skill Particle Aura toggle)."
        )));

        // Respec Costs
        allEntries.add(new WikiEntry("skill_system_respec_costs", "Respec Costs", "Skills", List.of(
            "##Respec Cost System",
            "",
            "Respeccing skill points costs MegaCoins.",
            "The cost scales with your investment.",
            "",
            "--- First Respec Free ---",
            "Your first respec per tree is always free!",
            "After that, costs scale up.",
            "",
            "--- Full Tree Respec ---",
            "  - Base cost: 100 MC",
            "  - Plus 25 MC per skill level invested",
            "  - Example: 30 levels invested =",
            "    100 + (25 x 30) = 850 MC",
            "",
            "--- Branch Respec ---",
            "  - Base cost: 50 MC",
            "  - Plus 50 MC per tier of the branch",
            "  - T3 branch: 50 + 150 = 200 MC",
            "  - T5 branch: 50 + 250 = 300 MC",
            "",
            "--- Tips ---",
            "  - Plan your build carefully to avoid",
            "    frequent expensive respecs",
            "  - Branch respecs are cheaper than full",
            "    tree respecs for small adjustments",
            "  - The first-free respec lets you",
            "    experiment early without penalty"
        )));

        // ---- COMBAT OVERHAUL: CLASS BRANCHES ----

        allEntries.add(new WikiEntry("skill_class_paladin", "Paladin Class", "Skills", List.of(
            "##Class Branch: Paladin",
            "",
            "The holy warrior class. Found in the Combat tree",
            "(Shield Wall branch) and Arcane tree (Relic Lore",
            "and Summoner branches).",
            "",
            "##What It Unlocks",
            "  - Healing spells (Heal, Flash Heal, Holy Shock)",
            "  - Holy damage spells (Judgement, Holy Beam)",
            "  - Support spells (Divine Protection, Barrier,",
            "    Battle Banner, Circle of Healing)",
            "  - Kite Shields (Iron, Diamond, Netherite)",
            "  - Great Hammers, Maces, Claymores",
            "  - Paladin/Crusader armor sets",
            "  - Healing Wands and Healing Staves",
            "",
            "##Playstyle",
            "  Tanky healer/support that wears heavy armor,",
            "  wields a mace or hammer with kite shield,",
            "  and casts healing/holy spells with R/G keys.",
            "  Best class for party play and dungeon support."
        )));
        allEntries.add(new WikiEntry("skill_class_warrior", "Warrior Class", "Skills", List.of(
            "##Class Branch: Warrior",
            "",
            "The frontline bruiser class. Found in the Combat",
            "tree (Berserker and Shield Wall branches).",
            "",
            "##What It Unlocks",
            "  - Warrior spells (Shattering Throw, Shout, Charge)",
            "  - Double Axes, Glaives, Claymores, Hammers",
            "  - Warrior/Berserker armor sets",
            "  - Lifesteal and attack speed bonuses",
            "",
            "##Playstyle",
            "  Aggressive melee fighter who thrives on lifesteal",
            "  and raw damage. Uses Charge to close gaps, Shout",
            "  to demoralize enemies, and Shattering Throw for",
            "  ranged engagement. Berserker builds gain power",
            "  as health drops. Best for solo dungeon clearing."
        )));
        allEntries.add(new WikiEntry("skill_class_wizard", "Wizard Class", "Skills", List.of(
            "##Class Branch: Wizard",
            "",
            "The arcane spellcaster class. Found in the Arcane",
            "tree (Spell Blade, Mana Weaver, Relic Lore branches).",
            "",
            "##What It Unlocks",
            "  - Arcane spells (Arcane Bolt, Blast, Missile, Beam, Blink)",
            "  - Fire spells (Fireball, Fire Blast, Fire Breath,",
            "    Fire Meteor, Fire Wall, Fire Scorch)",
            "  - Frost spells (Frost Shard, Frostbolt, Frost Nova,",
            "    Frost Shield, Frost Blizzard)",
            "  - Wands (Novice, Arcane, Fire, Frost, Netherite)",
            "  - Staves (Wizard, Arcane, Fire, Frost, Netherite)",
            "  - Wizard Robe armor sets (Arcane, Fire, Frost)",
            "",
            "##Playstyle",
            "  Ranged magic damage dealer who stays at distance.",
            "  Uses Arcane Blink for mobility, channels powerful",
            "  beams and blizzards, or bursts with Fireball/Meteor.",
            "  Gear stacks Spell Haste and CDR for rapid casting.",
            "  Wears robes (light armor) for spell power bonuses."
        )));
        allEntries.add(new WikiEntry("skill_class_rogue", "Rogue Class", "Skills", List.of(
            "##Class Branch: Rogue",
            "",
            "The stealthy assassin class. Found in the Combat",
            "tree (Tactician and Blade Mastery branches).",
            "",
            "##What It Unlocks",
            "  - Rogue spells (Slice and Dice, Shock Powder,",
            "    Shadow Step, Vanish)",
            "  - Daggers, Sickles (fast melee weapons)",
            "  - Rogue/Assassin armor sets",
            "  - Crit chance, crit damage, dodge bonuses",
            "",
            "##Playstyle",
            "  Fast melee assassin who dual-wields daggers for",
            "  rapid combo attacks. Uses Vanish to go invisible,",
            "  Shadow Step to teleport behind targets, Shock Powder",
            "  to stun groups, and Slice and Dice for attack speed.",
            "  Relies on crits and dodge rather than raw HP.",
            "  Dual-wielding daggers unlocks a special 4th combo hit."
        )));
        allEntries.add(new WikiEntry("skill_class_ranger", "Ranger Class", "Skills", List.of(
            "##Class Branch: Ranger",
            "",
            "The ranged marksman class. Found in the Combat tree",
            "(Ranged Precision branch) and Survival tree",
            "(Hunter Instinct branch).",
            "",
            "##What It Unlocks",
            "  - Ranger spells (Power Shot, Entangling Roots,",
            "    Barrage, Magic Arrow)",
            "  - Bows (Composite Longbow, Mechanic Shortbow,",
            "    Royal Longbow, Netherite Shortbow/Longbow)",
            "  - Crossbows (Rapid, Heavy, Netherite variants)",
            "  - Spears (Flint to Netherite tiers)",
            "  - Archer/Ranger armor sets",
            "",
            "##Playstyle",
            "  Ranged physical damage dealer who kites enemies",
            "  from a distance. Uses Entangling Roots to trap",
            "  foes, Barrage for rapid arrow volleys, Power Shot",
            "  for single-target burst, and Magic Arrow for a",
            "  piercing shot that passes through all enemies.",
            "  Best class for open-world combat and exploration."
        )));

        // ---- COMBAT OVERHAUL: SPELL SYSTEM ----

        allEntries.add(new WikiEntry("skill_system_spells", "Spell System", "Skills", List.of(
            "##Spell System",
            "",
            "Spells are special abilities unlocked via skill nodes",
            "in the Combat and Arcane trees. Each spell belongs to",
            "a class (Wizard, Paladin, Rogue, Warrior, or Ranger)",
            "and a spell school (Arcane, Fire, Frost, Healing,",
            "Lightning, Soul, Physical, Ranged).",
            "",
            "##How to Cast",
            "  - R key: Cast primary spell (from equipped spell slot)",
            "  - G key: Cast secondary spell",
            "  - Hold R/G for charged spells (release to fire)",
            "  - Hold R/G for channeled spells (continuous effect)",
            "  - Tap R/G for instant spells (immediate cast)",
            "",
            "##Cast Modes",
            "  - Instant: Fires immediately, no wind-up",
            "  - Charged: Hold to charge, release to fire",
            "    (longer charge = stronger effect)",
            "  - Channeled: Continuous effect while holding",
            "    (beams, blizzards, fire breath)",
            "",
            "##Unlocking Spells",
            "  Spells unlock at specific tier thresholds in skill",
            "  branches. Higher tier = more powerful spells.",
            "  Each class has 4-16 spells across multiple schools.",
            "",
            "##36 Total Spells",
            "  Wizard: 16 (Arcane, Fire, Frost)",
            "  Paladin: 9 (Healing, Holy, Physical)",
            "  Rogue: 4 (Physical Melee)",
            "  Warrior: 3 (Physical Melee)",
            "  Ranger: 4 (Physical Ranged)"
        )));
    }

    // ---- ITEMS ----

    private void buildItemEntries() {
        addItem("soul_anchor", "Soul Anchor", "Dungeon-exclusive safety item",
            "Preserves your inventory on death inside a dungeon. Consumed on use.",
            "Dungeon treasure chests / Crafting");
        addItem("void_shard", "Void Shard", "Rare dungeon material",
            "A shard of solidified void energy dropped by DungeonSlimes. Used in advanced crafting recipes.",
            "DungeonSlime drops (15% chance, Void theme)");
        addItem("boss_trophy", "Boss Trophy", "Decorative display item",
            "A trophy commemorating a boss defeat. Place it in your base as a display of your dungeon conquests. Unique model per boss.",
            "Boss rooms in Infernal dungeons");
        addItem("dungeon_map", "Dungeon Map", "Dungeon navigation aid",
            "Reveals the layout of the current dungeon floor, showing room types and your position.",
            "Treasure chests in Hard+ dungeons");
        addItem("infernal_essence", "Infernal Essence", "Rare crafting material",
            "Concentrated demonic energy from the deepest dungeons. Used to craft high-tier equipment.",
            "Nightmare/Infernal dungeon loot");
        addItem("warp_stone", "Warp Stone", "Emergency escape item",
            "A one-use item that teleports you out of a dungeon instantly, saving your current inventory. Consumed on use.",
            "Rare treasure chest drop in Hard+ dungeons");
        addItem("cerulean_arrow", "Cerulean Arrow", "Custom arrow type",
            "An arrow tipped with rat fang venom. Applies Slowness II for 4 seconds on hit. Crafted from Rat Fang + Arrow.",
            "Crafting: Rat Fang + Arrow");
        addItem("crystal_arrow", "Crystal Arrow", "Custom arrow type",
            "An arrow reinforced with old skeleton bone. Deals +4 bonus damage and applies Glowing for 8 seconds.",
            "Crafting: Old Skeleton Bone + Arrow");
        addItem("rat_fang", "Rat Fang", "Dungeon monster drop",
            "A sharp fang from a dungeon Rat. Used to craft Cerulean Arrows.",
            "Rat mob drops (40% chance)");
        addItem("fang_on_a_stick", "Fang on a Stick", "Unique melee weapon",
            "A crude but effective weapon made from a giant rat fang. Decent damage with a fast swing speed.",
            "Boss chests in dungeons");
        addItem("old_skeleton_bone", "Old Skeleton Bone", "Dungeon monster drop",
            "An ancient bone from a DungeonMob skeleton. Used to craft Crystal Arrows.",
            "DungeonMob drops (60% chance)");
        addItem("old_skeleton_head", "Old Skeleton Head", "Dungeon monster drop",
            "The skull of an ancient dungeon skeleton. Wearable as a helmet. Rare collector's item.",
            "DungeonMob drops (5% chance) / Undead Knight (10%)");
        addItem("mob_net", "Mob Net", "Museum capture tool",
            "A special net for capturing passive mobs to donate to the Museum. Right-click a mob to capture it.",
            "Crafting recipe (string + sticks)");
        addItem("dungeon_key_normal", "Normal Dungeon Key", "Dungeon entry item",
            "A brass key that grants access to a Normal difficulty dungeon. Consumed on entry.",
            "Accept a quest from the Royal Herald");
        addItem("dungeon_key_hard", "Hard Dungeon Key", "Dungeon entry item",
            "An iron key that grants access to a Hard difficulty dungeon. Consumed on entry.",
            "Accept a quest from the Royal Herald");
        addItem("dungeon_key_nightmare", "Nightmare Dungeon Key", "Dungeon entry item",
            "A dark steel key for Nightmare dungeons. The metal is cold to the touch. Consumed on entry.",
            "Accept a quest from the Royal Herald");
        addItem("dungeon_key_infernal", "Infernal Dungeon Key", "Dungeon entry item",
            "A key forged in hellfire for the deadliest dungeons. Glows with an ominous red light. Consumed on entry.",
            "Accept a quest from the Royal Herald");

        // ---- Dungeon Weapons & Items (Phase 2-3) ----
        addItem("naga_fang_dagger", "Naga Fang Dagger", "Dungeon melee weapon",
            "A curved dagger crafted from a Naga's fang. Applies Poison on hit. Fast attack speed.",
            "Naga mob drops / Dungeon treasure chests");
        addItem("wrought_axe", "Wrought Axe", "Heavy dungeon weapon",
            "A massive iron axe wielded by Wroughtnaut bosses. Extremely heavy with high base damage.",
            "Wroughtnaut boss drop");
        addItem("wrought_helm", "Wrought Helm", "Boss armor piece",
            "An iron helm forged in the dungeon depths. High armor value with knockback resistance.",
            "Wroughtnaut boss drop");
        addItem("ice_crystal", "Ice Crystal", "Dungeon magic item",
            "A crystallized shard of pure frost energy. Use to create an AOE freeze effect around you.",
            "Frostmaw boss drop / Ice theme treasure");
        addItem("spear", "Spear", "Dungeon melee weapon",
            "A long-reach melee weapon effective against charging mobs. Slightly longer range than swords.",
            "Dungeon treasure chests");
        addItem("life_stealer", "Life Stealer", "Dungeon legendary weapon",
            "A cursed blade that drains 20% of damage dealt as health. Glows with a dark red aura.",
            "Rare dungeon treasure (Hard+ difficulty)");
        addItem("scepter_of_chaos", "Scepter of Chaos", "Dungeon magic weapon",
            "A staff that fires homing magic bolts. The bolts seek nearby enemies with slight tracking.",
            "ChaosSpawner boss drop");
        addItem("sol_visage", "Sol Visage", "Dungeon magic item",
            "A golden mask that channels a 20-block sun beam, dealing heavy fire damage in a line.",
            "Umvuthi boss drop");
        addItem("earthrend_gauntlet", "Earthrend Gauntlet", "Dungeon melee weapon",
            "Massive stone fists that summon 3x3 stone pillars on impact, dealing 12 damage.",
            "Sculptor boss drop");
        addItem("blowgun", "Blowgun", "Dungeon ranged weapon",
            "A tribal ranged weapon that fires darts with random potion effects.",
            "Dungeon treasure chests (Temple/Overgrown themes)");
        addItem("glowing_jelly", "Glowing Jelly", "Dungeon consumable",
            "Bioluminescent jelly that grants Night Vision, Speed, and Absorption when consumed.",
            "Dungeon treasure chests");
        addItem("strange_meat", "Strange Meat", "Dungeon food item",
            "Mysterious meat found in dungeon kitchens. Restores hunger but may have... side effects.",
            "Dungeon treasure chests");
        addItem("great_experience_bottle", "Great Experience Bottle", "Enhanced XP bottle",
            "An oversized experience bottle that grants 50 XP when thrown. Much more potent than vanilla.",
            "SpawnerCarrier mob drops (2-3 per kill)");
        addItem("absorption_orb", "Absorption Orb", "Dungeon utility item",
            "A glowing orb that grants Absorption hearts when used. Provides temporary extra HP.",
            "Dungeon treasure chests (Nightmare+ difficulty)");
        addItem("living_divining_rod", "Living Divining Rod", "Dungeon utility item",
            "A mysterious rod that pulses when near valuable ore. Points toward the nearest ore vein.",
            "Rare dungeon treasure drop");
        addItem("captured_grottol", "Captured Grottol", "Dungeon summon item",
            "A tamed Grottol in a jar. Release it as a mining helper that digs ore for you.",
            "Grottol mob capture (rare)");
        addItem("foliaath_seed", "Foliaath Seed", "Dungeon plant item",
            "Plant it to grow a Foliaath ally that attacks nearby hostile mobs with poison.",
            "Foliaath mob drops (Overgrown theme)");

        // Masks
        addItem("mask_of_fear", "Mask of Fear", "Umvuthana mask",
            "A tribal mask that radiates terror. Passive: nearby hostile mobs flee from you within 6 blocks.",
            "Umvuthana mob drops / Umvuthi boss");
        addItem("mask_of_fury", "Mask of Fury", "Umvuthana mask",
            "A ferocious tribal mask. Passive: +15% melee damage but -10% defense.",
            "Umvuthana mob drops / Umvuthi boss");
        addItem("mask_of_faith", "Mask of Faith", "Umvuthana mask",
            "A serene tribal mask. Passive: +1 HP/5s regeneration and +10% healing received.",
            "Umvuthana mob drops / Umvuthi boss");
        addItem("mask_of_rage", "Mask of Rage", "Umvuthana mask",
            "A wrathful tribal mask. Passive: attack speed increases as health decreases.",
            "Umvuthana mob drops / Umvuthi boss");
        addItem("mask_of_misery", "Mask of Misery", "Umvuthana mask",
            "A sorrowful tribal mask. Passive: enemies that hit you receive Weakness II for 3 seconds.",
            "Umvuthana mob drops / Umvuthi boss");
        addItem("mask_of_bliss", "Mask of Bliss", "Umvuthana mask",
            "A joyful tribal mask. Passive: grants Luck effect and +10% XP gain.",
            "Umvuthana mob drops / Umvuthi boss");

        // Geomancer Armor
        addItem("geomancer_helm", "Geomancer Helm", "Dungeon armor set piece",
            "Stone-forged helmet from the Sculptor boss. Part of the 4-piece Geomancer set.",
            "Sculptor boss drop");
        addItem("geomancer_chest", "Geomancer Chestplate", "Dungeon armor set piece",
            "Stone-forged chestplate. Part of the 4-piece Geomancer set with earth magic bonuses.",
            "Sculptor boss drop");
        addItem("geomancer_legs", "Geomancer Leggings", "Dungeon armor set piece",
            "Stone-forged leggings. Part of the 4-piece Geomancer set.",
            "Sculptor boss drop");
        addItem("geomancer_boots", "Geomancer Boots", "Dungeon armor set piece",
            "Stone-forged boots. Complete the 4-piece set for bonus earth abilities.",
            "Sculptor boss drop");

        // Boss Trophies
        addItem("wraith_trophy", "Wraith Trophy", "Boss trophy display",
            "A spectral trophy commemorating victory over the Wraith boss. Placeable as decoration.",
            "Wraith boss (15% drop chance)");
        addItem("ossukage_trophy", "Ossukage Trophy", "Boss trophy display",
            "A trophy commemorating victory over Ossukage. Placeable as decoration.",
            "Ossukage boss (15% drop chance)");
        addItem("dungeon_keeper_trophy", "Dungeon Keeper Trophy", "Boss trophy display",
            "A crystalline trophy from the Dungeon Keeper. Placeable as decoration.",
            "Dungeon Keeper boss (15% drop chance)");
        addItem("frostmaw_trophy", "Frostmaw Trophy", "Boss trophy display",
            "An icy trophy from the Frostmaw titan. Placeable as decoration.",
            "Frostmaw boss (15% drop chance)");
        addItem("wroughtnaut_trophy", "Wroughtnaut Trophy", "Boss trophy display",
            "An iron trophy from the Wroughtnaut warden. Placeable as decoration.",
            "Wroughtnaut boss (15% drop chance)");
        addItem("umvuthi_trophy", "Umvuthi Trophy", "Boss trophy display",
            "A golden mask trophy from Lord Umvuthi. Placeable as decoration.",
            "Umvuthi boss (15% drop chance)");
        addItem("chaos_spawner_trophy", "Chaos Spawner Trophy", "Boss trophy display",
            "A void-touched trophy from the Chaos Spawner. Placeable as decoration.",
            "Chaos Spawner boss (15% drop chance)");
        addItem("sculptor_trophy", "Sculptor Trophy", "Boss trophy display",
            "A stone trophy from the Sculptor golem. Placeable as decoration.",
            "Sculptor boss (15% drop chance)");

        // ---- Economy System ----
        addItem("economy_wallet", "Wallet (MegaCoins)", "Economy system",
            "Your wallet holds MegaCoins earned from killing mobs and mining ores. WARNING: Wallet coins are LOST on death! Deposit coins into the Bank for safety.",
            "Automatic - earned from combat and mining");
        addItem("economy_bank", "Bank Account", "Economy system",
            "Your bank stores MegaCoins safely - they are NOT lost on death. Access via ATM blocks or the Computer Bank app. Deposit from wallet, withdraw to wallet.",
            "Access via ATM block or Computer > Bank app");
        addItem("economy_megashop", "MegaShop", "Economy system",
            "A 60-item tiered catalog with daily rotation. Buy items with MegaCoins. Higher-tier items cost more but offer better gear. New selection every day.",
            "Computer > Shop app");
        addItem("economy_atm", "ATM Block", "Economy system",
            "A craftable block that gives you access to your Bank account. Deposit wallet coins for safekeeping, or withdraw when you need them. Place anywhere.",
            "Crafting recipe");
        addItem("economy_mob_rewards", "Mob Kill Rewards", "Economy system",
            "Mobs drop MegaCoins on kill. Rewards: Zombie/Skeleton/Spider 2mc, Creeper 5mc, Witch 8mc, Enderman 18mc, Blaze 18mc, Wither Skeleton 30mc, Warden 500mc, Wither 750mc, Ender Dragon 1500mc. Passive mobs give 0.",
            "Kill hostile mobs");
        addItem("economy_ore_rewards", "Ore Mining Rewards", "Economy system",
            "Mining ores grants MegaCoins. Rewards: Coal/Copper 1mc, Iron/Lapis/Redstone/Nether Quartz/Nether Gold 2mc, Gold 3mc, Emerald 8mc, Diamond 10mc, Ancient Debris 25mc.",
            "Mine ore blocks");
        addItem("resource_dimension_key", "Resource Dimension Key", "Dimension key",
            "A craftable key that teleports you to the Resource Dimension — a fresh overworld clone for gathering resources. The dimension resets every 24 hours (real time), giving players a new area to mine. Single use, consumed on right-click. Walk into the portal at spawn to return.",
            "Crafting: Iron Nuggets (cross) + Diamond (center)");

        // Core blocks & tools
        addItem("computer", "Computer Block", "Core MegaMod block",
            "The hub for all MegaMod apps: Shop, Bank, Stats, Skills, Recipes, Wiki, Music, Games, Notes, Map, Friends, Ranks, Mail, Party, Settings, Trade, Bounty, Town, Casino.",
            "Crafting recipe (iron + redstone + glass)");
        addItem("atm_block", "ATM Block", "Economy block",
            "Deposit and withdraw MegaCoins between wallet and bank. Right-click to open. Wallet coins are lost on death — bank is safe.",
            "Crafting recipe (iron + gold + glass)");
        addItem("furniture_overview", "Furniture Blocks", "Decorative blocks",
            "275+ decorative furniture blocks across 13 themes: Office, Vintage, Classic, Coffee Shop, Market (x2), Casino (x2), Dungeon, Farmer, Caribbean, Bedroom, Modern. Buy from the Furniture Shop (Computer > Shop > Furniture tab).",
            "Purchase from Furniture Shop (10-70 MC each)");

        // Dungeon mobs
        addItem("mob_dungeon_rat", "Dungeon Rat", "Dungeon mob",
            "Small aggressive rat found in dungeon corridors. Multiple color variants (grey, blue, yellow). Fast movement, low HP. Drops Rat Fang (40% chance).",
            "Spawns in dungeon rooms");
        addItem("mob_undead_knight", "Undead Knight", "Dungeon mob",
            "Armored skeleton warrior with sword. High damage, medium HP. Patrols dungeon halls. Drops Old Skeleton Head (10% chance).",
            "Spawns in dungeon combat rooms");
        addItem("mob_hollow", "Hollow", "Dungeon mob",
            "Spectral humanoid entity summoned by the Dungeon Keeper boss. Medium HP, melee attacker.",
            "Summoned by Dungeon Keeper boss in Phase 3");
        addItem("mob_naga", "Naga", "Dungeon mob",
            "Flying serpent that hovers above players. Bites with Poison II at close range, spits PoisonBall projectiles from distance. 35 HP.",
            "Spawns in dungeon rooms");
        addItem("mob_foliaath", "Foliaath", "Dungeon mob",
            "Dormant plant ambush mob. Invisible until a player comes within 6 blocks, then emerges and attacks with Poison I bite. 30 HP, 9 damage.",
            "Spawns camouflaged in dungeon rooms");
        addItem("mob_grottol", "Grottol", "Dungeon mob",
            "Small armored beetle that flees from players. When hit, burrows underground (invulnerable for 16 ticks) then teleports away. Drops bones, diamonds, emeralds on escape.",
            "Spawns in dungeon rooms (non-aggressive loot mob)");
        addItem("mob_lantern", "Lantern", "Dungeon mob",
            "Floating jellyfish-like mob. Attacks with puff knockback (4-block AOE) and fireball projectiles. 20 HP, hovers 3 blocks above ground.",
            "Spawns in dungeon rooms");
        addItem("mob_bluff", "Bluff", "Dungeon mob",
            "Small pufferfish-like mob. Puffs up when damaged, creating a 3-block knockback AOE. Non-aggressive — defensive only. Drops BluffRod (50%).",
            "Spawns in dungeon rooms (passive)");

        // ---- COMBAT OVERHAUL: CLASS WEAPONS ----

        allEntries.add(new WikiEntry("item_class_weapons_overview", "Class Weapons", "Items", List.of(
            "##Class Weapons Overview",
            "",
            "Class weapons are tiered combat weapons tied to RPG classes.",
            "Each type comes in material tiers: Stone/Flint, Golden,",
            "Iron, Diamond, and Netherite — with increasing base damage.",
            "",
            "##Paladin / Warrior Weapons",
            "  - Claymores: Slow 2H, high damage (4-10 base)",
            "    Combo: wide slash, stab, overhead slam",
            "  - Great Hammers: Slowest 2H, highest damage (4-11)",
            "    Combo: overhead slam, side slam",
            "  - Maces: 1H blunt (4-9 base)",
            "    Combo: vertical slam, horizontal swing",
            "",
            "##Rogue Weapons",
            "  - Daggers: Fast 1H (3-6 base), dual-wieldable",
            "    Combo: stab, slash, stab (+bonus stab if dual)",
            "  - Sickles: Fast 1H curved blades (3-6 base)",
            "    Combo: quick slash, slash",
            "",
            "##Warrior Weapons",
            "  - Double Axes: Heavy 2H axes (5-11 base)",
            "    Combo: vertical chop, vertical chop",
            "  - Glaives: 2H polearm slashers (5-10 base)",
            "    Combo: wide slash, stab",
            "",
            "##Ranger Weapons",
            "  - Spears: 2H thrusting polearms (3-9 base)",
            "    Single long-range stab, extra reach",
            "  - Bows: Composite, Mechanic, Royal, Netherite",
            "  - Crossbows: Rapid, Heavy, Netherite variants",
            "",
            "##Wizard Weapons",
            "  - Wands: 1H spell catalysts (Novice, Arcane/Fire/Frost,",
            "    Netherite Arcane/Fire/Frost)",
            "  - Staves: 2H spell catalysts, higher power (Wizard,",
            "    Arcane/Fire/Frost, Netherite Arcane/Fire/Frost)",
            "",
            "##Paladin Healing Weapons",
            "  - Healing Wands: 1H (Acolyte, Holy, Diamond, Netherite)",
            "  - Healing Staves: 2H (Holy, Diamond, Netherite)"
        )));

        // ---- KITE SHIELDS ----

        addItem("kite_shield", "Kite Shields", "Paladin defensive shields",
            "Paladin-exclusive one-handed shields. Larger than vanilla shields with higher durability. Come in Iron (336 dur), Golden (112), Diamond (528), and Netherite (672) tiers. Pair with a mace or healing wand for sword-and-board Paladin builds.",
            "Crafting / Dungeon loot / MegaShop");

        // ---- JEWELRY ----

        allEntries.add(new WikiEntry("item_jewelry_overview", "Jewelry", "Items", List.of(
            "##Jewelry System",
            "",
            "Rings and Necklaces that equip in accessory slots",
            "and provide passive stat bonuses.",
            "",
            "##Tiers",
            "  Tier 0 (Basic): Copper, Iron, Gold rings — minor armor",
            "  Tier 2 (Gem): Gemstone rings/necklaces — 4% bonuses",
            "  Tier 3 (Netherite Gem): Netherite-framed — 8% bonuses",
            "",
            "##Gemstone Bonuses",
            "  Ruby: +Attack Damage (melee)",
            "  Topaz: +Arcane Power, +Fire Damage",
            "  Citrine: +Healing Power, +Lightning Damage",
            "  Jade: +Ranged Damage",
            "  Sapphire: +Max Health (+2 Gem / +4 Netherite)",
            "  Tanzanite: +Frost Damage, +Soul Power",
            "",
            "##How to Use",
            "  Equip rings in Ring accessory slots (press V).",
            "  Equip necklaces in the Neck accessory slot.",
            "  Stack bonuses by wearing matching gem types",
            "  across ring + necklace for maximum effect.",
            "  Example: Ruby Ring + Ruby Necklace = +8% attack damage."
        )));

        // ---- EQUIPMENT SET BONUSES ----

        allEntries.add(new WikiEntry("item_set_bonuses", "Equipment Set Bonuses", "Items", List.of(
            "##Equipment Set Bonuses",
            "",
            "Wearing multiple armor pieces from the same set",
            "grants stacking bonuses. More pieces = bigger bonus.",
            "",
            "##How It Works",
            "  Each set has 4 pieces (head, chest, legs, boots).",
            "  Bonuses activate at 2-piece, 3-piece, and 4-piece.",
            "  Equip matching armor to see active bonuses in tooltip.",
            "",
            "##Wizard Sets (7 total)",
            "  Wizard Robe: +Arcane Power, +Mana Efficiency",
            "  Arcane/Fire/Frost Robes: Elemental spell power,",
            "    Spell Haste, Crit Chance/Damage, CDR",
            "  Netherite variants: Highest bonuses (up to +55 power)",
            "",
            "##Paladin Sets (3 total)",
            "  Paladin Armor: +Healing Power, +Holy Damage, +Regen",
            "  Crusader Armor: +CDR, +Thorns Damage",
            "  Netherite Crusader: Up to +60 Healing, +35 Holy",
            "",
            "##Priest Sets (3 total)",
            "  Priest/Prior Robes: +Healing Power, +Mana Efficiency",
            "  Netherite Prior: Up to +70 Healing, +8 CDR, +3 Regen",
            "",
            "##Rogue Sets (3 total)",
            "  Rogue Armor: +Crit Chance, +Dodge, +Combo Speed",
            "  Assassin Armor: +Crit Damage, +Armor Shred",
            "  Netherite Assassin: Up to +22% Crit, +60% Crit Dmg",
            "",
            "##Warrior Sets (3 total)",
            "  Warrior Armor: +Crit Damage, +Stun, +Lifesteal",
            "  Berserker Armor: +Armor Shred, +Thorns",
            "  Netherite Berserker: Up to +70% Crit Dmg, +10% Steal",
            "",
            "##Archer Sets (3 total)",
            "  Archer Armor: +Ranged Damage, +Dodge, +Crit",
            "  Ranger Armor: +Combo Speed",
            "  Netherite Ranger: Up to +60 Ranged, +16% Crit"
        )));

        // ---- Raw Gems (6 types) ----
        addItem("raw_ruby", "Raw Ruby", "Rare dungeon gem",
            "A brilliant red gemstone. Used in jewelry crafting. Boosts fire-related attributes when set in a ring or necklace.",
            "Dungeon treasure chests / Boss drops");
        addItem("raw_topaz", "Raw Topaz", "Rare dungeon gem",
            "A vibrant orange gemstone. Used in jewelry crafting. Boosts lightning-related attributes when set in a ring or necklace.",
            "Dungeon treasure chests / Boss drops");
        addItem("raw_citrine", "Raw Citrine", "Rare dungeon gem",
            "A warm yellow gemstone. Used in jewelry crafting. Boosts holy-related attributes when set in a ring or necklace.",
            "Dungeon treasure chests / Boss drops");
        addItem("raw_jade", "Raw Jade", "Rare dungeon gem",
            "A rich green gemstone. Used in jewelry crafting. Boosts poison and nature attributes when set in a ring or necklace.",
            "Dungeon treasure chests / Boss drops");
        addItem("raw_sapphire", "Raw Sapphire", "Rare dungeon gem",
            "A deep blue gemstone. Used in jewelry crafting. Boosts ice and frost attributes when set in a ring or necklace.",
            "Dungeon treasure chests / Boss drops");
        addItem("raw_tanzanite", "Raw Tanzanite", "Rare dungeon gem",
            "A mystical purple gemstone. Used in jewelry crafting. Boosts shadow and arcane attributes when set in a ring or necklace.",
            "Dungeon treasure chests / Boss drops");
    }

    private void addItem(String itemId, String name, String subtitle, String description, String obtainMethod) {
        allEntries.add(new WikiEntry("item_" + itemId, name, "Items", List.of(
            "##" + subtitle,
            "",
            description,
            "",
            "##How to Obtain",
            "  " + obtainMethod
        )));
    }

    // ---- MUSEUM (6 entries) ----

    private void buildMuseumEntries() {
        allEntries.add(new WikiEntry("museum_overview", "Museum Overview", "Museum", List.of(
            "##Your Personal Museum",
            "",
            "An Animal Crossing-inspired collection building where you",
            "donate items, mobs, and art to fill 5 exhibit wings.",
            "",
            "##How to Access",
            "  - Craft a Museum Block and place it in the world",
            "  - Right-click: Enter your museum pocket dimension",
            "  - Sneak + empty hand: Open the catalog GUI",
            "  - Sneak + holding item: Donate the held item",
            "",
            "##Wings",
            "  - Items Collection (South wing)",
            "  - Art Gallery (North wing)",
            "  - Aquarium (East wing)",
            "  - Wildlife (West wing)",
            "  - Achievements (Basement)",
            "",
            "##Completion",
            "  Fill each wing to earn MegaCoin milestone rewards",
            "  at 25%, 50%, 75%, and 100% completion per wing.",
            "  Overall milestones grant bonus items too!"
        )));
        allEntries.add(new WikiEntry("museum_donations", "Donation Guide", "Museum", List.of(
            "##How to Donate",
            "",
            "Sneak + right-click the Museum Block while holding an item.",
            "",
            "##Accepted Donations",
            "  - Any item: Goes to Items Collection wing",
            "  - Spawn Eggs: Registers the mob in Wildlife/Aquarium",
            "  - Captured Mobs (from Mob Net): Wildlife/Aquarium",
            "  - Fish Buckets: Registers aquatic mob in Aquarium",
            "  - Masterpiece Paintings: Art Gallery wing",
            "  - Vanilla Painting: Art Gallery wing",
            "",
            "##Skill XP",
            "  Each donation grants skill XP:",
            "  - Mobs: 5 Combat XP",
            "  - Art: 5 Arcane XP",
            "  - Mining items: 3 Mining XP",
            "  - Farming items: 3 Farming XP",
            "  - Other items: 2 Survival XP",
            "",
            "##Dungeon Loot & Relics",
            "  All dungeon items, boss trophies, masks, and relics",
            "  can be donated to the Items Collection wing!"
        )));
        allEntries.add(new WikiEntry("museum_mob_net", "Mob Net", "Museum", List.of(
            "##Museum Capture Tool",
            "",
            "A special net for capturing mobs to donate to the Museum.",
            "",
            "##Usage",
            "  Right-click any living entity to capture it. The mob",
            "  becomes a 'Captured Mob' item in your inventory.",
            "",
            "##Details",
            "  - Stack size: 16",
            "  - Prioritizes spawn eggs if available",
            "  - Custom mobs stored with NBT data",
            "  - Captured item displays [Captured MobName] in aqua",
            "",
            "##How to Obtain",
            "  Crafting recipe (string + sticks)"
        )));
        allEntries.add(new WikiEntry("museum_curator", "Museum Curator", "Museum", List.of(
            "##Museum NPC Guide",
            "",
            "The Curator is a librarian-type villager NPC that lives",
            "inside your museum pocket dimension.",
            "",
            "##Behavior",
            "  - Invulnerable and stationary (won't wander off)",
            "  - Looks at nearby players",
            "  - Right-click to open the Museum Catalog GUI",
            "",
            "##Dialogue",
            "  The Curator gives dynamic hints based on your",
            "  collection progress:",
            "  - Welcome messages for new collectors",
            "  - Hints for your least-complete wing",
            "  - Dungeon loot collection tips",
            "  - Milestone celebrations at 25/50/75/100%",
            "  - Special messages when wings are completed"
        )));
        allEntries.add(new WikiEntry("museum_dimension", "Museum Dimension", "Museum", List.of(
            "##Personal Pocket Dimension",
            "",
            "Each player gets their own museum dimension with a",
            "pre-built structure that grows with your collection.",
            "",
            "##Structure",
            "  - Main Hall: 21x21 with central trophy display",
            "  - 5 Wing Rooms connected by corridors",
            "  - Achievement Basement with staircase",
            "  - Return Portal in the main hall",
            "",
            "##Display Types",
            "  - Items: Pedestals with floating items + labels",
            "  - Art: Wall-mounted paintings with spotlights",
            "  - Aquarium: Glass tanks with water and live mobs",
            "  - Wildlife: Fenced enclosures (size-sorted)",
            "  - Boss exhibits: Large dedicated rooms",
            "",
            "##Protection",
            "  Museum dimension blocks mob griefing, explosions,",
            "  and block breaking."
        )));
        allEntries.add(new WikiEntry("museum_rewards", "Completion Rewards", "Museum", List.of(
            "##Milestone Rewards",
            "",
            "Earn MegaCoins and items by filling your museum!",
            "",
            "##Per-Wing Milestones",
            "  - 25% complete: +50 MegaCoins",
            "  - 50% complete: +200 MegaCoins",
            "  - 75% complete: +500 MegaCoins",
            "  - 100% complete: +1000 MegaCoins",
            "",
            "##Overall Milestones",
            "  - 25% overall: +100 MegaCoins",
            "  - 50% overall: +300 MegaCoins",
            "  - 75% overall: +750 MC + Enchanted Golden Apple",
            "  - 100% overall: +2000 MC + Nether Star",
            "",
            "##Museum Achievements",
            "  - Master Collector: Complete Items wing",
            "  - Art Connoisseur: Complete Art Gallery wing",
            "  - Ocean Explorer: Complete Aquarium wing",
            "  - Beast Master: Complete Wildlife wing",
            "  - Completionist: Complete all 4 wings"
        )));
        allEntries.add(new WikiEntry("museum_history", "Donation History", "Museum", List.of(
            "##History Tab",
            "",
            "The Museum Catalog GUI has a History tab showing",
            "all your donations sorted by game day.",
            "",
            "##Features",
            "  - Most recent donations shown first",
            "  - Day separator headers group donations",
            "  - Type tags: [Item], [Mob], [Art]",
            "  - Color-coded by donation type",
            "",
            "##Accessing",
            "  Open the catalog (Sneak + right-click Museum Block",
            "  with empty hand, or right-click the Curator NPC)",
            "  and click the History tab."
        )));
    }

    // ---- CONTROLS (keybinds) ----

    private void buildControlEntries() {
        allEntries.add(new WikiEntry("controls_overview", "Controls Overview", "Controls", List.of(
            "##MegaMod Keybinds",
            "",
            "MegaMod adds custom keybinds. All are rebindable",
            "in Options > Controls > Key Binds > MegaMod.",
            "",
            "##Default Keybinds",
            "  V - Open Accessories/Relics GUI",
            "  R - Cast Relic Ability",
            "  R+Scroll - Cycle weapon abilities",
            "  G+Scroll - Cycle equipped relics",
            "  G tap - Flip ability on selected relic",
            "  K - Open Skill Tree",
            "  O - Sort container inventory",
            "  Right-Click - Cast weapon ability (on ability weapons)",
            "",
            "##How to Rebind",
            "  1. Open Minecraft Settings",
            "  2. Go to Controls > Key Binds",
            "  3. Scroll to the 'MegaMod' section",
            "  4. Click the key you want to change",
            "  5. Press your desired new key"
        )));
        allEntries.add(new WikiEntry("controls_accessories", "Accessories Key (V)", "Controls", List.of(
            "##Default: V",
            "",
            "Opens the Accessories/Relics equipment GUI.",
            "",
            "##Details",
            "  Shows your 8 relic slot types:",
            "  Back, Belt, Necklace, Feet, Hands, Ring, Usable, Head",
            "",
            "  Equip relics by placing them in slots. Each relic",
            "  grants passive bonuses and up to 2 active abilities.",
            "",
            "##Rebindable",
            "  Options > Controls > Key Binds > MegaMod"
        )));
        allEntries.add(new WikiEntry("controls_ability_primary", "Relic Ability (R)", "Controls", List.of(
            "##Default: R",
            "",
            "Casts the selected RELIC ability. Weapons use right-click.",
            "",
            "##Tap R",
            "  Casts the ability from your currently selected relic.",
            "  Works even while holding a weapon in your hand.",
            "",
            "##Hold R + Scroll",
            "  Cycles through weapon abilities (visual selection",
            "  for right-click). Does NOT cast.",
            "",
            "##Notes",
            "  - Abilities have cooldowns shown on the HUD",
            "  - Some abilities are toggle-based (press again to deactivate)",
            "  - Ability power scales with Arcane skill tree bonuses",
            "",
            "##Rebindable",
            "  Options > Controls > Key Binds > MegaMod"
        )));
        allEntries.add(new WikiEntry("controls_ability_secondary", "Relic Select (G)", "Controls", List.of(
            "##Default: G",
            "",
            "Selects which relic and ability to use with R.",
            "",
            "##Hold G + Scroll",
            "  Cycles through equipped relics that have",
            "  castable (non-passive) abilities.",
            "",
            "##Tap G",
            "  Flips to the next ability on the currently",
            "  selected relic. E.g. if a relic has 3 abilities,",
            "  tapping G cycles through them.",
            "",
            "##Notes",
            "  - Selected relic shows gold border on HUD",
            "  - Page dots below show which ability is active",
            "  - Passive abilities run automatically (can't select)",
            "",
            "##Rebindable",
            "  Options > Controls > Key Binds > MegaMod"
        )));
        allEntries.add(new WikiEntry("controls_skill_tree", "Skill Tree Key (K)", "Controls", List.of(
            "##Default: K",
            "",
            "Opens the Skill Tree screen with 5 progression trees.",
            "",
            "##Skill Trees",
            "  - Combat: Weapon mastery, armor, criticals",
            "  - Mining: Ore yield, mining speed, smelting",
            "  - Farming: Crops, animals, fishing, cooking",
            "  - Arcane: Enchanting, potions, relic power",
            "  - Survival: Health, movement, economy, defense",
            "",
            "##Navigation",
            "  Click tabs to switch trees. Click nodes to unlock.",
            "  Earn XP from gameplay actions matching each tree.",
            "",
            "##Rebindable",
            "  Options > Controls > Key Binds > MegaMod"
        )));
        allEntries.add(new WikiEntry("controls_museum", "Museum Controls", "Controls", List.of(
            "##Museum Interactions",
            "",
            "The Museum uses block interactions, not keybinds:",
            "",
            "##Museum Block",
            "  - Right-click: Enter museum dimension",
            "  - Sneak + right-click (empty hand): Open catalog",
            "  - Sneak + right-click (holding item): Donate item",
            "",
            "##Curator NPC",
            "  - Right-click: Open catalog GUI + hear dialogue",
            "",
            "##Museum Catalog GUI",
            "  - 6 tabs: Aquarium, Wildlife, Achievements, Art, Items, History",
            "  - Mouse scroll: Navigate entries",
            "  - Page Up/Down: Scroll pages",
            "  - Home/End: Jump to top/bottom"
        )));

        allEntries.add(new WikiEntry("controls_qol", "Quality of Life Features", "Controls", List.of(
            "##MegaMod QoL Features",
            "",
            "MegaMod includes many quality-of-life improvements:",
            "",
            "##Movement & Interaction",
            "  - Player Sitting: Shift+right-click blocks to sit",
            "  - Path Sprinting: Sprint faster on paths",
            "  - Homing XP: XP orbs seek you out",
            "  - Better Lodestones: Enhanced compass tracking",
            "",
            "##Combat & Survival",
            "  - Totem in Void: Totem saves you from void",
            "  - Head Drops: Mobs drop decorative heads",
            "  - Low Health Warning: Alert when health is low",
            "  - Recovery System: Recover items on death",
            "",
            "##Crafting & Items",
            "  - Craft Sounds: Audio feedback when crafting",
            "  - Readable Clocks: See actual time on clocks",
            "  - Equipable Banners: Wear banners cosmetically",
            "  - Invisible Frames: Item frames can be invisible",
            "",
            "##World & Mobs",
            "  - Mob Health HUD: Health bars above mobs",
            "  - Day Counter: Track world day count",
            "  - Villager Trade Refresh: Better trading UI",
            "  - Better Armor Stands: Easier customization"
        )));

        allEntries.add(new WikiEntry("controls_economy", "Economy Controls", "Controls", List.of(
            "##Economy System",
            "",
            "MegaMod has a full economy using MegaCoins.",
            "",
            "##Earning Coins",
            "  - Kill hostile mobs for coin rewards",
            "  - Mine ores for coin rewards",
            "  - Complete bounties on the Bounty Board",
            "",
            "##Wallet vs Bank",
            "  - Wallet: coins on hand (LOST on death!)",
            "  - Bank: safe storage (kept on death)",
            "  - Use ATM blocks to deposit/withdraw",
            "",
            "##Spending Coins",
            "  - MegaShop: Computer > Shop app",
            "  - Player Trading: Computer > Trade app",
            "  - Bounty Board: Computer > Bounty app",
            "",
            "##Quick Access",
            "  - ATM Block: right-click to open bank",
            "  - Computer Bank App: full bank management",
            "  - HUD: wallet/bank shown in top-left corner"
        )));

        allEntries.add(new WikiEntry("controls_sorting", "Sort Key (O)", "Controls", List.of(
            "##Default: O",
            "",
            "Sorts the contents of any open container.",
            "",
            "##How It Works",
            "  Press O or click the sort button (top-right of",
            "  any container) to sort & cycle algorithm.",
            "",
            "##Sort Algorithms",
            "  - Name: Alphabetical by item name",
            "  - ID: By registry path",
            "  - Category: Weapons > Armor > Tools > Blocks > etc.",
            "  - Count: By stack size (largest first)",
            "  - Rarity: By item rarity (rarest first)",
            "",
            "  Each click sorts AND cycles to the next algorithm."
        )));

        allEntries.add(new WikiEntry("controls_weapon_cast", "Weapon Abilities (Right-Click)", "Controls", List.of(
            "##Right-Click (on ability weapons)",
            "",
            "Casts the selected ability on your held weapon.",
            "",
            "##Which Weapons",
            "  - RPG Weapons (Vampiric Tome, Static Seeker, etc.)",
            "  - Arsenal weapons (claymores, staves, etc.)",
            "  - Any weapon with castable abilities",
            "",
            "##Switching Abilities",
            "  Hold R + Scroll to cycle through weapon abilities.",
            "  The selected ability is shown with a gold border",
            "  on the weapon zone (left side of ability HUD).",
            "",
            "##Notes",
            "  - Cooldowns shown on HUD as red overlay",
            "  - Right-click is separate from R (relic casting)",
            "  - Both can be used independently in combat"
        )));
    }

    private void buildCitizenEntries() {
        // Getting Started Guide (expanded)
        allEntries.add(new WikiEntry("citizen_getting_started", "Getting Started", "Citizens", List.of(
            "##Colony Setup Guide",
            "",
            "Citizens are NPCs you hire and manage.",
            "Workers gather resources automatically.",
            "Recruits fight and defend your territory.",
            "All citizens need food, shelter, and pay.",
            "",
            "##Step 1: Earn MegaCoins",
            "  You need coins to hire and maintain citizens.",
            "  - Kill mobs to earn MegaCoins",
            "  - Mine ores (iron, gold, diamond, etc.)",
            "  - Sell items at the MegaShop",
            "  - Deposit coins in the Bank to keep them",
            "    safe! Wallet coins are LOST on death.",
            "  Your first farmer costs just 20 MC (or 10 MC",
            "  if you find a wandering citizen to recruit).",
            "",
            "##Step 2: Hire Your First Citizen",
            "",
            "  Option A - Hire a Villager (full price):",
            "    Right-click any vanilla Villager to open the",
            "    Hire Screen. Pick a job and pay from wallet.",
            "    The villager is replaced with your citizen.",
            "    Villager professions suggest a matching job",
            "    (e.g. a Farmer villager suggests Farmer).",
            "",
            "  Option B - Recruit a Wanderer (half price!):",
            "    Unowned citizens spawn near you in patrols",
            "    every ~5 minutes. Right-click one to hire",
            "    at 50% of the normal cost. You can change",
            "    their job during hiring too!",
            "",
            "  Tip: Look for citizens without colored names",
            "  walking nearby. They're unowned wanderers.",
            "",
            "##Step 3: Set Up Your Citizen",
            "",
            "  After hiring, right-click your citizen to",
            "  open the Interaction Menu. You need to assign:",
            "",
            "  1. A Bed (click 'Set Bed', then click a bed)",
            "     Citizens sleep at night to heal slowly.",
            "",
            "  2. A Chest (click 'Set Chest', then click one)",
            "     Workers use ONE chest for tools, food, and",
            "     storing gathered items. Keep it stocked!",
            "",
            "  3. A Work Position (workers only)",
            "     Click 'Set Work Pos', then click the block",
            "     where the citizen should work.",
            "     - Farmer: click farmland or nearby dirt",
            "     - Miner: click where to start mining",
            "     - Lumberjack: click near trees",
            "     - Fisherman: click near water",
            "",
            "  4. Stock the Chest with correct tools + food",
            "     (See 'Worker Setup Checklist' for details)",
            "",
            "  Your citizen will tell you in chat if they",
            "  are missing anything they need!",
            "",
            "##Step 4: Interaction Menu",
            "",
            "  Right-click an owned citizen to see options:",
            "",
            "  Workers:",
            "    Follow Me - citizen follows you",
            "    Stay Here - citizen stays in place",
            "    Go Work - citizen returns to work",
            "    Set Bed / Set Chest / Set Work Pos",
            "    Settings - job-specific config",
            "    Inventory - view citizen's 18-slot inv",
            "    Dismiss - permanently remove citizen",
            "",
            "  Recruits:",
            "    Follow Me / Hold Position",
            "    Set Aggro - set combat behavior",
            "    Set Group - assign to a squad",
            "    Commands - open commands menu",
            "    Set Chest - assign food/weapon chest",
            "    Inventory / Dismiss",
            "",
            "##Step 5: Expand Your Colony",
            "",
            "  Suggested early progression:",
            "  1. Farmer (20 MC) - automated food supply",
            "  2. Lumberjack (20 MC) - wood for building",
            "  3. Miner (30 MC) - ores and stone",
            "  4. A few Recruits (40 MC) - protection",
            "  5. Warehouse Worker (45 MC) + Town Chest",
            "     for automated logistics between workers",
            "",
            "  As income grows, add animal farmers for",
            "  food variety and a Merchant (60 MC) for",
            "  passive income from player purchases.",
            "",
            "##Step 6: Build an Army",
            "",
            "  Hire Recruits and equip them with weapons.",
            "  Set their chest with weapons and food.",
            "  Create squads via the Town App (Computer),",
            "  assign formations, and coordinate defense.",
            "",
            "  Use recruit Commands to set behavior:",
            "  Follow, Hold, Patrol, Protect Owner,",
            "  Aggressive, Defensive, or Passive.",
            "",
            "##Step 7: Factions & Territory",
            "",
            "  Create a faction (150 MC) to unlock:",
            "  - Territory claiming (25 MC per chunk)",
            "  - Diplomacy with other factions",
            "  - Siege warfare for territory control",
            "  - Manage it all via the Town App (Computer)",
            "",
            "##Important Tips",
            "  - Max 50 citizens per player",
            "  - Citizens have 18 inventory slots",
            "  - Upkeep is auto-deducted from your Bank",
            "    once per Minecraft day (24,000 ticks)",
            "  - If bank runs dry, citizens go idle",
            "  - Feed your citizens or they starve!",
            "  - Better tools = faster work",
            "  - Skill tree bonuses can reduce hire costs",
            "    and improve citizen stats"
        )));

        allEntries.add(new WikiEntry("citizen_setup_checklist", "Worker Setup Checklist", "Citizens", List.of(
            "##Every Worker Needs:",
            "  1. A Bed (sleeping at night, heals HP)",
            "  2. A Chest (tools, food, output storage)",
            "  3. A Work Position (where to work)",
            "  4. The right Tools in their chest",
            "  5. Food in their chest (any food works)",
            "",
            "Each citizen uses ONE chest for everything:",
            "tools, food, seeds, and storing gathered items.",
            "Citizens have 18 inventory slots.",
            "",
            "##Tool Requirements by Job:",
            "  Farmer: Hoe + Seeds (+ opt. Bone Meal)",
            "  Miner: Pickaxe + Shovel + Torches",
            "  Lumberjack: Axe",
            "  Fisherman: Fishing Rod",
            "  Shepherd: No tools (breeds sheep)",
            "  Cattle Farmer: No tools (breeds cows)",
            "  Chicken Farmer: No tools (breeds chickens)",
            "  Swineherd: No tools (breeds pigs)",
            "  Rabbit Farmer: No tools (breeds rabbits)",
            "  Beekeeper: No tools (harvests honey)",
            "  Goat Farmer: Bucket + Wheat",
            "  Merchant: No tools (runs shop)",
            "  Warehouse Worker: Needs a Town Chest block",
            "",
            "##Recruit Equipment:",
            "  - Give recruits a chest with weapons + food",
            "  - Recruit (basic): Sword or Axe",
            "  - Shieldman: Sword + Shield",
            "  - Bowman: Bow + Arrows",
            "  - Crossbowman: Crossbow + Arrows",
            "  - Nomad: Sword (fast melee)",
            "  - Horseman: Sword (auto-spawns horse)",
            "  - Assassin: Sword (high burst damage)",
            "  - All recruits eat food and drink potions",
            "",
            "##Worker Settings (right-click > Settings):",
            "  Farmer: Farm radius (4-16 blocks)",
            "  Miner: Pattern, direction, depth",
            "  Lumberjack: Toggle sapling replant",
            "  Fisherman: Toggle boat usage",
            "  Animal Farmers: Max animal count (2-20)",
            "",
            "##Troubleshooting:",
            "  If a citizen is idle, check:",
            "  - Are bed, chest, and work pos assigned?",
            "  - Does the chest have the right tools?",
            "  - Is there food in the chest?",
            "  - Is the tool broken? (auto-replaces from",
            "    chest when current tool breaks)",
            "  - Is their inventory full? They'll deposit",
            "    items before returning to work.",
            "  - Citizens tell you what's missing in chat!"
        )));

        // Workers (13)
        allEntries.add(new WikiEntry("citizen_farmer", "Farmer", "Citizens", List.of(
            "##Slot: Worker", "",
            "Automated crop farmer. Uses a hoe to plow",
            "dirt, plant seeds, fertilize with bone meal,",
            "and harvest fully grown crops.", "",
            "##How It Works",
            "  1. Walks to work position",
            "  2. Plows dirt into farmland in radius",
            "  3. Plants seeds from inventory",
            "  4. Applies bone meal if available",
            "  5. Harvests mature crops",
            "  6. Deposits produce to assigned chest", "",
            "##Settings (right-click > Settings)",
            "  Farm Radius: 4 to 16 blocks (default 8)", "",
            "##Required: Hoe + Seeds (+ opt. Bone Meal)",
            "##Cost: 20 MC | Upkeep: 5 MC/day")));
        allEntries.add(new WikiEntry("citizen_miner", "Miner", "Citizens", List.of(
            "##Slot: Worker", "",
            "Digs ores and stone using configurable mine",
            "patterns. Places torches in dark areas and",
            "plank supports under sand/gravel.", "",
            "##Settings (right-click > Settings)",
            "  Pattern: 9 options (see Mine Patterns page)",
            "  Direction: N / S / E / W",
            "  Depth: 8 to 64 blocks", "",
            "##Required: Pickaxe + Shovel + Torches",
            "  Better pickaxe = faster mining speed.",
            "##Cost: 30 MC | Upkeep: 8 MC/day")));
        allEntries.add(new WikiEntry("citizen_lumberjack", "Lumberjack", "Citizens", List.of(
            "##Slot: Worker", "",
            "Detects and chops down nearby trees.",
            "Can replant saplings automatically.", "",
            "##How It Works",
            "  1. Searches for trees near work position",
            "  2. Pathfinds to trunk and chops it down",
            "  3. Replants sapling (if toggle is on)",
            "  4. Deposits logs to assigned chest", "",
            "##Settings (right-click > Settings)",
            "  Replant Saplings: On / Off", "",
            "##Required: Axe",
            "##Cost: 20 MC | Upkeep: 5 MC/day")));
        allEntries.add(new WikiEntry("citizen_fisherman", "Fisherman", "Citizens", List.of(
            "##Slot: Worker", "",
            "Seeks water near work position and fishes",
            "using a fishing rod. Can optionally use boats",
            "for deep water fishing.", "",
            "##How It Works",
            "  1. Walks to water near work position",
            "  2. Casts fishing line and waits",
            "  3. Reels in catch automatically",
            "  4. Deposits fish to assigned chest", "",
            "##Settings (right-click > Settings)",
            "  Use Boat: On / Off", "",
            "##Required: Fishing Rod",
            "##Cost: 20 MC | Upkeep: 5 MC/day")));
        allEntries.add(new WikiEntry("citizen_shepherd", "Shepherd", "Citizens", List.of(
            "##Slot: Worker (Animal Farmer)", "",
            "Breeds and shears sheep for wool.", "",
            "##Behavior",
            "  - Breeds sheep when under max count",
            "  - Shears sheep when wool is grown",
            "  - Slaughters excess sheep above max", "",
            "##Settings (right-click > Settings)",
            "  Max Animals: 2 to 20 (default varies)", "",
            "##Produces: Wool + Mutton",
            "##Cost: 25 MC | Upkeep: 6 MC/day")));
        allEntries.add(new WikiEntry("citizen_cattle_farmer", "Cattle Farmer", "Citizens", List.of(
            "##Slot: Worker (Animal Farmer)", "",
            "Breeds and manages cows for leather and beef.", "",
            "##Behavior",
            "  - Breeds cows when under max count",
            "  - Slaughters excess cows above max", "",
            "##Settings (right-click > Settings)",
            "  Max Animals: 2 to 20", "",
            "##Produces: Beef + Leather",
            "##Cost: 25 MC | Upkeep: 6 MC/day")));
        allEntries.add(new WikiEntry("citizen_chicken_farmer", "Chicken Farmer", "Citizens", List.of(
            "##Slot: Worker (Animal Farmer)", "",
            "Breeds chickens and collects eggs.", "",
            "##Behavior",
            "  - Breeds chickens when under max count",
            "  - Picks up eggs laid by chickens",
            "  - Slaughters excess chickens above max", "",
            "##Settings (right-click > Settings)",
            "  Max Animals: 2 to 20", "",
            "##Produces: Chicken + Feathers + Eggs",
            "##Cost: 25 MC | Upkeep: 6 MC/day")));
        allEntries.add(new WikiEntry("citizen_swineherd", "Swineherd", "Citizens", List.of(
            "##Slot: Worker (Animal Farmer)", "",
            "Breeds and manages pigs for porkchops.", "",
            "##Behavior",
            "  - Breeds pigs when under max count",
            "  - Slaughters excess pigs above max", "",
            "##Settings (right-click > Settings)",
            "  Max Animals: 2 to 20", "",
            "##Produces: Porkchops",
            "##Cost: 25 MC | Upkeep: 6 MC/day")));
        allEntries.add(new WikiEntry("citizen_rabbit_farmer", "Rabbit Farmer", "Citizens", List.of(
            "##Slot: Worker (Animal Farmer)", "",
            "Breeds and manages rabbits.", "",
            "##Behavior",
            "  - Breeds rabbits when under max count",
            "  - Slaughters excess rabbits above max", "",
            "##Settings (right-click > Settings)",
            "  Max Animals: 2 to 20", "",
            "##Produces: Rabbit + Rabbit Hide + Rabbit Foot",
            "##Cost: 25 MC | Upkeep: 6 MC/day")));
        allEntries.add(new WikiEntry("citizen_beekeeper", "Beekeeper", "Citizens", List.of(
            "##Slot: Worker", "",
            "Manages bee hives for honey production.", "",
            "##How It Works",
            "  1. Plants flowers near hives to keep bees",
            "     happy and productive",
            "  2. Waits for hives to fill with honey",
            "  3. Harvests honey bottles and honeycomb",
            "  4. Deposits to assigned chest", "",
            "##Produces: Honey Bottles + Honeycomb",
            "##Cost: 25 MC | Upkeep: 6 MC/day")));
        allEntries.add(new WikiEntry("citizen_merchant", "Merchant", "Citizens", List.of(
            "##Slot: Worker (Shop NPC)", "",
            "A player-owned shop that other players can",
            "buy from. Stands at work position during the",
            "day, sleeps at bed at night.", "",
            "##Shop System",
            "  - 6 items for sale at a time:",
            "    2 common, 2 uncommon, 1 rare, 1 epic",
            "  - Stock rotates daily (unique per merchant)",
            "  - Prices marked up 20% from MegaShop", "",
            "##Revenue",
            "  - 10% of each sale goes to your bank",
            "  - 90% is removed from the economy",
            "  - Passive income while you're offline!", "",
            "##Limits: Max 3 merchants per faction",
            "##Cost: 60 MC | Upkeep: 15 MC/day")));
        allEntries.add(new WikiEntry("citizen_warehouse_worker", "Warehouse Worker", "Citizens", List.of(
            "##Slot: Worker (Logistics)", "",
            "Manages item flow between the Town Chest",
            "and all citizen chests in your colony.", "",
            "##Behavior Cycle",
            "  1. Collects non-food items from all citizen",
            "     work chests and deposits into Town Chest",
            "  2. Picks up food from the Town Chest",
            "  3. Delivers food to citizen upkeep chests",
            "     that are running low (under 16 items)", "",
            "##Setup",
            "  1. Place a Town Chest block",
            "  2. Hire a Warehouse Worker",
            "  3. Set work position to the Town Chest",
            "  4. Assign upkeep chests to your workers", "",
            "Has faster movement speed (0.32) and long",
            "range for efficient logistics.", "",
            "##Cost: 45 MC | Upkeep: 10 MC/day")));
        allEntries.add(new WikiEntry("citizen_goat_farmer", "Goat Farmer", "Citizens", List.of(
            "##Slot: Worker (Animal Farmer)", "",
            "Specialized farmer that manages goats.", "",
            "##Three Work Phases",
            "  MILKING: Uses buckets to milk goats",
            "  BREEDING: Uses wheat to breed goats",
            "    when below max animal count",
            "  SLAUGHTERING: Kills excess goats when",
            "    above max animal count", "",
            "##Settings (right-click > Settings)",
            "  Max Animals: 2 to 20", "",
            "##Required: Bucket + Wheat",
            "##Produces: Milk Buckets + Goat Meat",
            "##Cost: 25 MC | Upkeep: 6 MC/day")));
        allEntries.add(new WikiEntry("citizen_town_chest", "Town Chest", "Citizens", List.of(
            "##Colony Storage Block", "",
            "Massive 324-slot container (equivalent to",
            "6 double chests) for centralized colony storage.", "",
            "##Features",
            "  - Scrollable inventory with scrollbar",
            "  - Row indicator and slot usage counter",
            "  - Works with the Warehouse Worker citizen",
            "  - Used as the dungeon chest open model", "",
            "##How to Use",
            "  1. Craft/place a Town Chest block",
            "  2. Right-click to open scrollable inventory",
            "  3. Scroll with mouse wheel or drag scrollbar",
            "  4. Assign a Warehouse Worker's work position",
            "     to the Town Chest for automated logistics", "",
            "##Logistics Flow",
            "  Citizen chests -> Warehouse Worker -> Town Chest",
            "  Town Chest -> Warehouse Worker -> Upkeep chests", "",
            "The Warehouse Worker automatically collects",
            "goods from citizen work chests and delivers",
            "food to upkeep chests that are running low.")));
        allEntries.add(new WikiEntry("citizen_upkeep_chest", "Upkeep Chest", "Citizens", List.of(
            "A linked chest that citizens will visit when hungry.",
            "",
            "Set an upkeep chest via the citizen interaction menu.",
            "When a citizen's hunger drops below 60, they will",
            "pathfind to their upkeep chest and extract food items.",
            "",
            "Workers and recruits can each have their own upkeep chest.",
            "The Town App Upkeep panel shows food stockpile status."
        )));
        allEntries.add(new WikiEntry("citizen_patrols", "Patrols", "Citizens", List.of(
            "##Unowned Citizen Patrols",
            "",
            "Unowned citizens spawn naturally near players.",
            "Right-click any unowned citizen to hire them",
            "at half the normal cost!",
            "",
            "##Combat Patrols (spawn 60-120 blocks away)",
            "  Tiny (2): 2 Recruits",
            "  Small (4): 2 Recruits, Bowman, Shieldman",
            "  Medium (6): +Crossbowman, Scout",
            "  Large (8): +extra Bowman & Shieldman, Commander",
            "  Huge (10): Full army with Captain",
            "  Road (3): Merchant + 2 Recruits",
            "  Caravan (5): Merchant + armed escort",
            "  Herald (3): Royal Herald + 2 escort guards",
            "",
            "##Worker Patrols (spawn 30-60 blocks away)",
            "  Wanderer (1): Single random worker",
            "  Settlers (3): Farmer, Miner, Lumberjack",
            "  Farmhands (3): Farmer, Shepherd, Chicken Farmer",
            "  Miners (2): Two Miners",
            "  Ranchers (3): Cattle, Swineherd, Shepherd",
            "  Laborers (4): Farmer, Lumberjack, Fisherman, Miner",
            "  Villagers (5): Mixed group of 5 workers",
            "",
            "Spawns every ~5 minutes with 50% chance.",
            "Spawns are configurable per server."
        )));
        allEntries.add(new WikiEntry("citizen_recruiting", "Recruiting Citizens", "Citizens", List.of(
            "##Two Ways to Get Citizens",
            "",
            "##1. Hire from Villagers",
            "  Right-click any vanilla Villager to open the",
            "  hire screen. Choose a job and pay full price",
            "  from your wallet. The villager is replaced",
            "  with a citizen you own.",
            "",
            "##2. Recruit Wandering Citizens",
            "  Unowned citizens roam the world in patrols.",
            "  Right-click one to open the hire screen.",
            "  Costs only 50% of the normal hire price!",
            "  You can also change their job when hiring.",
            "",
            "##How to Find Wanderers",
            "  Worker patrols spawn 30-60 blocks from you.",
            "  Combat patrols spawn 60-120 blocks away.",
            "  Look for citizens with names like Wanderer,",
            "  Settler, Traveler, or Nomad.",
            "",
            "##Tips",
            "  - Wanderers spawn every ~5 minutes",
            "  - They are unowned (no nametag color)",
            "  - Half-price hiring is a great early game deal",
            "  - Skill tree bonuses reduce hire costs further"
        )));
        // Recruits (11)
        allEntries.add(new WikiEntry("citizen_recruit", "Recruit", "Citizens", List.of(
            "##Slot: Recruit (Melee)", "",
            "Basic melee fighter. The most affordable",
            "combat unit. Great for early defense.", "",
            "##Stats",
            "  30 HP | 0 Armor | 5 Atk | Normal Speed", "",
            "##Behavior",
            "  - Attacks hostile mobs and enemy citizens",
            "  - Gains XP from kills, levels up (max 50)",
            "  - Higher level = better combat performance",
            "  - Eats food and drinks potions when low HP",
            "  - Flees TNT and fire", "",
            "##Equipment: Sword or Axe + Food",
            "##Cost: 40 MC | Upkeep: 8 MC/day")));
        allEntries.add(new WikiEntry("citizen_shieldman", "Shieldman", "Citizens", List.of(
            "##Slot: Recruit (Tank)", "",
            "Heavily armored tank with a shield. Slower",
            "than other recruits but very durable.", "",
            "##Stats",
            "  45 HP | 6 Armor | 4 Atk | Slow (0.25)", "",
            "##Special Abilities",
            "  - Auto-blocks with shield when targeted",
            "  - Highest HP of any recruit type",
            "  - Best used as a frontline protector", "",
            "##Equipment: Sword + Shield + Food",
            "##Cost: 50 MC | Upkeep: 12 MC/day")));
        allEntries.add(new WikiEntry("citizen_bowman", "Bowman", "Citizens", List.of(
            "##Slot: Recruit (Ranged)", "",
            "Ranged bow attacker. Effective at medium",
            "range. Picks up arrows after combat.", "",
            "##Stats",
            "  25 HP | 0 Armor | 3 Atk | Normal Speed", "",
            "##Special Abilities",
            "  - Ranged bow attacks at distance",
            "  - Picks up arrows from the ground",
            "  - Higher level = better accuracy",
            "  - Keep behind Shieldmen for best results", "",
            "##Equipment: Bow + Arrows + Food",
            "##Cost: 40 MC | Upkeep: 8 MC/day")));
        allEntries.add(new WikiEntry("citizen_crossbowman", "Crossbowman", "Citizens", List.of(
            "##Slot: Recruit (Heavy Ranged)", "",
            "Slower-firing crossbow specialist with",
            "higher damage per shot than the Bowman.", "",
            "##Stats",
            "  28 HP | 0 Armor | 4 Atk | Slow (0.28)", "",
            "##Special Abilities",
            "  - Heavy ranged crossbow attacks",
            "  - Higher damage than Bowman per shot",
            "  - Picks up arrows after combat",
            "  - Best at long range behind frontline", "",
            "##Equipment: Crossbow + Arrows + Food",
            "##Cost: 50 MC | Upkeep: 12 MC/day")));
        allEntries.add(new WikiEntry("citizen_nomad", "Nomad", "Citizens", List.of(
            "##Slot: Recruit (Fast Melee)", "",
            "The fastest melee recruit. High mobility",
            "and strong damage, but lighter armor.", "",
            "##Stats",
            "  25 HP | 1 Armor | 7 Atk | Fast (0.38)", "",
            "##Special Abilities",
            "  - Very fast movement speed",
            "  - High attack damage for a melee unit",
            "  - Excellent for flanking maneuvers",
            "  - Glass cannon: fast and deadly but fragile", "",
            "##Equipment: Sword or Axe + Food",
            "##Cost: 60 MC | Upkeep: 15 MC/day")));
        allEntries.add(new WikiEntry("citizen_horseman", "Horseman", "Citizens", List.of(
            "##Slot: Recruit (Cavalry)", "",
            "Mounted cavalry unit. A horse is auto-spawned",
            "when the Horseman is hired.", "",
            "##Stats",
            "  35 HP | 4 Armor | 6 Atk | Mounted", "",
            "##Special Abilities",
            "  - Rides a horse for high speed",
            "  - Horse is auto-summoned on spawn",
            "  - Charge damage from mounted attacks",
            "  - Great for hit-and-run tactics", "",
            "##Equipment: Sword + Food",
            "##Cost: 80 MC | Upkeep: 20 MC/day")));
        allEntries.add(new WikiEntry("citizen_commander", "Commander", "Citizens", List.of(
            "##Slot: Leader", "",
            "Squad leader that commands a group of",
            "recruits. Assign as group leader for",
            "formation commands and coordination.", "",
            "##Stats",
            "  35 HP | 4 Armor | 5 Atk | Normal Speed", "",
            "##Leadership",
            "  - Can be assigned as a group leader",
            "  - Leads squads with formation commands",
            "  - Coordinates patrol waypoints", "",
            "##Equipment: Sword + Food",
            "##Cost: 100 MC | Upkeep: 25 MC/day")));
        allEntries.add(new WikiEntry("citizen_captain", "Captain", "Citizens", List.of(
            "##Slot: Leader (Highest Rank)", "",
            "Army commander. The most powerful recruit",
            "type with the best stats and armor toughness.", "",
            "##Stats",
            "  40 HP | 5 Armor | 6 Atk | Normal Speed",
            "  + 2 Armor Toughness (unique!)", "",
            "##Leadership",
            "  - Highest rank recruit available",
            "  - Multi-group coordination",
            "  - Best used to lead large armies", "",
            "##Equipment: Sword + Food",
            "##Cost: 150 MC | Upkeep: 30 MC/day")));
        allEntries.add(new WikiEntry("citizen_messenger", "Messenger", "Citizens", List.of(
            "##Slot: Recruit (Diplomat)", "",
            "Delivers treaties between factions. The",
            "fastest recruit type but weak in combat.", "",
            "##Stats",
            "  20 HP | 0 Armor | 2 Atk | Fastest (0.40)", "",
            "##Special Abilities",
            "  - Delivers faction treaties and messages",
            "  - Fastest movement speed of all recruits",
            "  - Minimal combat ability (diplomat role)",
            "  - Required for diplomacy between factions", "",
            "##Cost: 60 MC | Upkeep: 15 MC/day")));
        allEntries.add(new WikiEntry("citizen_scout", "Scout", "Citizens", List.of(
            "##Slot: Recruit (Recon)", "",
            "Reconnaissance specialist. Fast movement",
            "and reveals enemy positions in the area.", "",
            "##Stats",
            "  22 HP | 1 Armor | 3 Atk | Fast (0.36)", "",
            "##Special Abilities",
            "  - Reveals enemy positions in area",
            "  - Fast movement for scouting terrain",
            "  - Useful before committing your army",
            "  - Low combat power, keep out of fights", "",
            "##Cost: 40 MC | Upkeep: 8 MC/day")));
        // System entries (4)
        allEntries.add(new WikiEntry("citizen_hiring", "Hiring Citizens", "Citizens", List.of(
            "##How to Hire Citizens", "",
            "##Method 1: From Villagers (full price)",
            "  Right-click any vanilla Villager to open",
            "  the Hire Screen. Select a citizen type and",
            "  pay with MegaCoins from your wallet.",
            "  The villager is permanently replaced.", "",
            "  Villager professions suggest matching jobs:",
            "    Mason -> Miner",
            "    Farmer -> Farmer",
            "    Fisherman -> Fisherman",
            "    Butcher -> Swineherd",
            "    Leatherworker -> Cattle Farmer",
            "    Fletcher -> Lumberjack",
            "    Cleric -> Beekeeper",
            "    Cartographer -> Merchant",
            "    Armorer/Weaponsmith -> Recruit",
            "    Toolsmith -> Miner",
            "    Shepherd -> Shepherd",
            "    Nitwit -> Farmer",
            "  You can override the suggestion and pick",
            "  any job you want.", "",
            "##Method 2: From Wanderers (half price!)",
            "  Unowned citizens roam the world in patrols.",
            "  Right-click one to open the Hire Screen at",
            "  50% of the normal cost! You can also change",
            "  their job during hiring.", "",
            "##Hire Screen Features",
            "  - Browse Workers and Recruits tabs",
            "  - See cost, upkeep, and job description",
            "  - Set a custom name for your citizen",
            "  - Pay from wallet (not bank)", "",
            "##Requirements",
            "  - Sufficient MegaCoins in wallet",
            "  - Under max citizen cap (50 per player)",
            "  - Must be within 4 blocks of target")));
        allEntries.add(new WikiEntry("citizen_upkeep", "Upkeep System", "Citizens", List.of(
            "##Daily Upkeep", "",
            "Citizens require daily MegaCoin upkeep,",
            "auto-deducted from your Bank account once",
            "per Minecraft day (24,000 ticks).", "",
            "##Upkeep Costs by Job",
            "  Farmer/Lumberjack/Fisherman: 5 MC/day",
            "  Animal Farmers/Beekeeper: 6 MC/day",
            "  Miner: 8 MC/day",
            "  Recruit/Bowman/Scout: 8 MC/day",
            "  Warehouse Worker: 10 MC/day",
            "  Shieldman/Crossbowman: 12 MC/day",
            "  Merchant/Nomad/Messenger: 15 MC/day",
            "  Horseman/Assassin: 20 MC/day",
            "  Commander: 25 MC/day",
            "  Captain: 30 MC/day", "",
            "##If You Can't Pay",
            "  When bank funds are insufficient, citizens",
            "  will go idle until you deposit more coins.",
            "  (Configurable: idle / dismiss / reduced)", "",
            "##Tips",
            "  - Check Town App > Upkeep tab for breakdown",
            "  - Keep Bank balance ahead of daily costs",
            "  - Merchants earn passive income to offset",
            "  - Dismiss unused citizens to save coins")));
        allEntries.add(new WikiEntry("citizen_factions_wiki", "Factions & Diplomacy", "Citizens", List.of(
            "##Factions", "",
            "Create a faction to organize players and",
            "unlock territory claims, diplomacy, and wars.", "",
            "##Creating a Faction",
            "  /megamod faction create <name>",
            "  Costs 150 MC from your wallet.", "",
            "##Diplomacy",
            "  Set relations with other factions:",
            "  - Ally: Friendly, no combat",
            "  - Neutral: Default, no interaction",
            "  - Enemy: Hostile, enables siege warfare",
            "  Send treaties via Messenger citizens!", "",
            "##Faction Commands",
            "  /megamod faction create <name>",
            "  /megamod faction leave",
            "  /megamod faction list",
            "  /megamod faction info", "",
            "##Town App: Diplomacy Tab",
            "  View all factions and their relations.",
            "  Manage treaties and alliances.")));
        allEntries.add(new WikiEntry("citizen_territory_wiki", "Territory & Siege", "Citizens", List.of(
            "##Territory Claiming", "",
            "Claim chunks for your faction to protect",
            "builds and enable defensive bonuses.", "",
            "##Claiming Chunks",
            "  - Base cost: 25 MC per chunk",
            "  - Cost scales with total claims",
            "  - Claimed chunks have build protection",
            "  - Explosion protection is enabled", "",
            "##Siege Warfare",
            "  Enemy factions can siege your territory.", "",
            "  Siege Requirements:",
            "  - Must have Enemy faction relation",
            "  - Attacker needs at least 3 recruits",
            "  - Owner does not need to be online", "",
            "  During Siege:",
            "  - Claim health drains over 3 MC days",
            "  - Default claim health: 100",
            "  - Territory transfers when health hits 0", "",
            "##Town App: Territory & War Tabs",
            "  View claims, borders, active sieges,",
            "  and army composition.")));

        // Assassin
        allEntries.add(new WikiEntry("citizen_assassin", "Assassin", "Citizens", List.of(
            "##Slot: Recruit (Stealth)", "",
            "High burst damage stealth unit. After killing",
            "a target, enters stealth and repositions", "before re-engaging.", "",
            "##Stats",
            "  20 HP | 0 Armor | 10 Atk | Fast (0.38)", "",
            "##Special Abilities",
            "  - Highest single-hit damage (10 Atk!)",
            "  - Enters stealth for 100 ticks after a kill",
            "  - Flees briefly before re-engaging",
            "  - Glass cannon: deadly but very fragile",
            "  - Best used to assassinate key targets", "",
            "##Equipment: Sword + Food",
            "##Cost: 80 MC | Upkeep: 20 MC/day")));

        // System guides (10)
        allEntries.add(new WikiEntry("citizen_town_app", "Town App Guide", "Citizens", List.of(
            "##Computer App: Town", "",
            "The Town app on your Computer is the central",
            "hub for managing your entire colony.", "",
            "##8 Tabs", "",
            "  Overview:",
            "    Colony stats at a glance. Total citizens,",
            "    income vs costs, population breakdown.", "",
            "  Workers:",
            "    Worker roster with name, job, and status.",
            "    Quick commands: follow, work, dismiss.", "",
            "  Recruits:",
            "    Recruit roster with combat stats, level,",
            "    kills, morale. Quick combat commands.", "",
            "  Groups:",
            "    Create and manage squads. Set formation,",
            "    assign leader, issue group commands.", "",
            "  Diplomacy:",
            "    View all factions and their relations.",
            "    Manage treaties, alliances, enemies.", "",
            "  Territory:",
            "    View and manage chunk claims. See costs",
            "    and permissions for claimed territory.", "",
            "  Upkeep:",
            "    Full cost breakdown by citizen. Shows",
            "    total daily cost vs current bank balance.", "",
            "  War:",
            "    Active sieges, army composition, and",
            "    siege progress tracking.")));

        allEntries.add(new WikiEntry("citizen_groups", "Groups & Squads", "Citizens", List.of(
            "##Organizing Recruits into Squads", "",
            "Create squads via the Town App > Groups tab.",
            "Assign recruits to groups for coordinated", "control during combat.", "",
            "##Creating a Group",
            "  1. Open Town App > Groups tab",
            "  2. Click 'Create Group'",
            "  3. Name it and pick an icon",
            "  4. Assign recruits via their interaction menu",
            "     (right-click > Set Group)", "",
            "##Group Features",
            "  - 14 group icons (sword, shield, bow, etc.)",
            "  - Assign a leader (Commander or Captain)",
            "  - Set a protect target",
            "  - Toggle ranged attacks on/off per group",
            "  - Toggle rest mode per group",
            "  - Split groups in half or merge two together", "",
            "##Group Commands",
            "  Follow - group follows you",
            "  Hold - group holds current position",
            "  Patrol - group patrols an area",
            "  Attack - group engages enemies")));

        allEntries.add(new WikiEntry("citizen_formations", "Formations", "Citizens", List.of(
            "##10 Formation Types", "",
            "  Line:",
            "    Single row, shoulder to shoulder.",
            "    Good for blocking chokepoints.", "",
            "  Column:",
            "    Single file, marching order.",
            "    Best for narrow paths.", "",
            "  Wedge:",
            "    V-shape, leader at front.",
            "    Aggressive push formation.", "",
            "  Square:",
            "    Filled square grid. Balanced defense.", "",
            "  Circle:",
            "    Filled concentric rings.",
            "    Good for all-around defense.", "",
            "  Scatter:",
            "    Random dispersal. Avoids AoE damage.", "",
            "  Movement:",
            "    3-wide marching column. Travel mode.", "",
            "  Hollow Square:",
            "    Perimeter only. Protects center units.", "",
            "  Hollow Circle:",
            "    Ring perimeter. Same as above, round.", "",
            "  V-Formation:",
            "    Two angled wings. Flanking attack.", "",
            "Set formations via Groups tab or commands.",
            "Formations reposition recruits automatically.")));

        allEntries.add(new WikiEntry("citizen_combat", "Combat System", "Citizens", List.of(
            "##Recruit Combat Mechanics", "",
            "##Morale (0-100)",
            "  Morale represents willingness to fight.",
            "  - Starts at 100 when freshly hired",
            "  - Drops when allies die or taking damage",
            "  - Below 20: recruits flee from combat!",
            "  - Recovers above 30 to re-engage",
            "  - Keep recruits fed and healthy", "",
            "##XP & Leveling",
            "  - Recruits earn XP from kills",
            "  - Level = XP / 100 (max level 50)",
            "  - Higher level = better accuracy (ranged)",
            "  - Higher level = better combat overall", "",
            "##Universal Recruit Abilities",
            "  - Dodge ranged attacks (attempt to sidestep)",
            "  - Flee from TNT and fire",
            "  - Drink potions when HP is low",
            "  - Eat food automatically when hungry",
            "  - Pick up nearby items (5.5 block radius)", "",
            "##Type-Specific Abilities",
            "  - Shieldmen auto-block when targeted",
            "  - Bowmen/Crossbowmen pick up arrows",
            "  - Assassins enter stealth after kills",
            "  - Scouts reveal enemy positions",
            "  - Horsemen ride horses for speed", "",
            "##Recruit Commands (right-click > Commands)",
            "  Follow, Hold Position, Patrol,",
            "  Protect Owner, Aggressive, Defensive, Passive")));

        allEntries.add(new WikiEntry("citizen_tools", "Worker Tools & Chests", "Citizens", List.of(
            "##Tool Management", "",
            "Workers automatically equip the best tool",
            "from their inventory. When a tool breaks,",
            "they pathfind to their chest and grab a",
            "replacement.", "",
            "##Required Tools by Job",
            "  Farmer: Hoe (+ seeds in chest)",
            "  Miner: Pickaxe + Shovel + Torches",
            "  Lumberjack: Axe",
            "  Fisherman: Fishing Rod",
            "  Goat Farmer: Bucket + Wheat",
            "  Others: No tools required", "",
            "##Better Tools = Faster Work",
            "  Iron tools are faster than stone.",
            "  Diamond tools are faster than iron.",
            "  Stock your chests with the best tools",
            "  you can afford for maximum efficiency.", "",
            "##Chest Interaction Cycle",
            "  1. Worker gathers items (ores, logs, etc.)",
            "  2. Inventory full -> walks to chest",
            "  3. Deposits gathered items",
            "  4. Picks up tools, food, seeds, torches",
            "  5. Returns to work position",
            "  Also triggers when current tool breaks.")));

        allEntries.add(new WikiEntry("citizen_mine_patterns", "Mine Patterns", "Citizens", List.of(
            "##9 Mine Patterns (Miner Settings)", "",
            "  0. 1x2 Tunnel",
            "     1 wide, 2 high. Basic tunnel.", "",
            "  1. 3x3 Tunnel",
            "     3 wide, 3 high. Spacious tunnel.", "",
            "  2. 8x8x8 Pit",
            "     Digs down 8 blocks deep.", "",
            "  3. 8x8x1 Flat",
            "     Clears a single layer. Strip mine.", "",
            "  4. 8x8x3 Room",
            "     3-high room. Underground base.", "",
            "  5. 16x16x16 Large Pit",
            "     Massive quarry. Takes a while.", "",
            "  6. 16x16x1 Large Flat",
            "     Large area strip mine.", "",
            "  7. 16x16x3 Large Room",
            "     Large underground room.", "",
            "  8. (Pattern 8)",
            "     Additional pattern variant.", "",
            "##Settings (right-click Miner > Settings)",
            "  Pattern: Cycle through 0-8",
            "  Direction: N / S / E / W",
            "  Depth: 8 to 64 blocks", "",
            "##Auto Safety Features",
            "  - Places torches in dark areas",
            "  - Places plank supports under sand/gravel",
            "  - Avoids digging into lava (pathfinds around)")));

        allEntries.add(new WikiEntry("citizen_hunger", "Hunger & Eating", "Citizens", List.of(
            "##Hunger System", "",
            "All citizens have a hunger bar from 0 to 100.",
            "Hunger depletes over time, faster while working.", "",
            "##Hunger Thresholds",
            "  100: Maximum (fully fed)",
            "  90+: Saturated (no hunger loss)",
            "  60: Citizens start seeking food",
            "  5: Starvation damage begins!", "",
            "##Eating Behavior",
            "  Citizens eat food from their inventory",
            "  automatically when hungry (below 60).",
            "  If no food in inventory, they go to their",
            "  chest or upkeep chest to find food.", "",
            "  Nutrition value x 4 = hunger restored",
            "  Nutrition value x 0.5 = HP healed", "",
            "##Blacklisted Foods (will not eat)",
            "  - Poisonous Potato",
            "  - Spider Eye",
            "  - Pufferfish", "",
            "##Sleeping",
            "  Workers sleep at night in their assigned bed.",
            "  While sleeping: heals 0.025 HP per tick.",
            "  Citizens auto-heal when out of combat for",
            "  5 seconds (heals slowly every 3 seconds).", "",
            "##Keeping Citizens Fed",
            "  - Stock food in their chest",
            "  - Use a Warehouse Worker to auto-deliver",
            "    food from the Town Chest to upkeep chests",
            "  - Cooked foods restore more hunger")));

        allEntries.add(new WikiEntry("citizen_commands_wiki", "Commands Reference", "Citizens", List.of(
            "##Chat Commands", "",
            "##Citizen Commands",
            "  /megamod citizens list",
            "    List all your owned citizens.", "",
            "  /megamod citizens count",
            "    Show citizen count vs max (50).", "",
            "  /megamod citizens recall",
            "    Teleport ALL your citizens to you.",
            "    Useful if they get lost or stuck.", "",
            "  /megamod citizens dismiss all",
            "    Dismiss ALL citizens permanently.", "",
            "##Faction Commands",
            "  /megamod faction create <name>",
            "    Create a new faction (150 MC).", "",
            "  /megamod faction leave",
            "    Leave your current faction.", "",
            "  /megamod faction list",
            "    List all factions on the server.", "",
            "  /megamod faction info",
            "    Show your faction's details.", "",
            "##In-World Interactions",
            "  Right-click Villager:",
            "    Opens the Hire Screen",
            "  Right-click Owned Citizen:",
            "    Opens the Interaction Menu",
            "  Right-click Unowned Citizen:",
            "    Opens Hire Screen (half price!)",
            "  Right-click Royal Herald:",
            "    Opens quest interface")));

        allEntries.add(new WikiEntry("citizen_spawn_bonuses", "Recruit Spawn Bonuses", "Citizens", List.of(
            "##Random Stat Bonuses", "",
            "Each recruit spawns with random bonus stats,",
            "making every recruit unique!", "",
            "##Bonus Ranges",
            "  Max Health: +0% to +50%",
            "  Attack Damage: +0% to +50%",
            "  Movement Speed: +0% to +10%",
            "  Knockback Resistance: +0% to +10%", "",
            "##What This Means",
            "  A lucky Recruit could spawn with +50% HP",
            "  and +50% Attack, making them significantly",
            "  stronger than average.", "",
            "  An unlucky one gets +0% across the board.", "",
            "##Checking Bonuses",
            "  Check individual recruit stats in the",
            "  Town App > Recruits tab. It shows each",
            "  recruit's actual HP, damage, and speed.", "",
            "##Tips",
            "  - Keep high-bonus recruits, dismiss low ones",
            "  - Wanderer recruits also get random bonuses",
            "  - Bonuses are permanent and cannot change")));
    }

    // ---- CASINO (5) ----

    private void buildCasinoEntries() {
        allEntries.add(new WikiEntry("casino_overview", "Casino Overview", "Casino", List.of(
            "##The MegaMod Casino",
            "",
            "A pocket dimension filled with gambling games",
            "where you can wager MegaCoins for big payouts.",
            "",
            "##How to Visit",
            "  - Open the Casino app on your Computer",
            "  - Click 'Go to Casino' to teleport",
            "  - Walk through the portal to return",
            "",
            "##Casino Chips",
            "  Exchange MegaCoins for chips at the Cashier.",
            "  Available: $1, $3, $5, $10, $20, $50, $100, $500, $1K",
            "  Drag chips onto tables to place bets.",
            "  Chips auto-cash out when you leave the casino.",
            "",
            "##Currency",
            "  All bets use casino chips (exchange at Cashier).",
            "  Bank funds are safe and cannot be wagered.",
            "  Winnings are returned as chips.",
            "",
            "##Games Available",
            "  - Slot Machines (multiple machines)",
            "  - Blackjack (up to 4 players per table)",
            "  - Big Wheel (unlimited players)",
            "  - Roulette (bet on numbers or colors)",
            "  - Craps (dice rolling game)",
            "  - Baccarat (Player vs Banker card game)",
            "",
            "##Tips",
            "  - Set a budget before gambling",
            "  - Higher risk bets offer higher rewards",
            "  - Track your stats in the Casino app")));

        allEntries.add(new WikiEntry("casino_slots", "Slot Machines", "Casino", List.of(
            "##Slot Machines",
            "",
            "Classic 3-reel slot machines with 9 symbols",
            "and multiple pay line configurations.",
            "",
            "##How to Play",
            "  1. Right-click a Slot Machine to sit down",
            "  2. Use +/- to adjust your bet (1 to 1,000 MC)",
            "  3. Choose line mode (1, 3, or 5 lines)",
            "  4. Click SPIN!",
            "  5. Match symbols across pay lines to win",
            "",
            "##Symbols (worst to best)",
            "  Skull - filler, no win",
            "  Coal - 6x bet",
            "  Cherry - special: 1/2/3 match = 2x/3x/5x",
            "  Lapis - 8x bet",
            "  Redstone - 10x bet",
            "  Iron - 20x bet",
            "  Gold - 50x bet",
            "  Emerald - 100x bet",
            "  Diamond - 500x bet (JACKPOT!)",
            "",
            "##Line Modes",
            "  1 Line: Center row only (cheapest)",
            "  3 Lines: Top, center, bottom rows (3x bet)",
            "  5 Lines: All rows + both diagonals (5x bet)",
            "",
            "##Tips",
            "  - 5 lines gives you more chances to win per spin",
            "  - Cherry is the most common winning symbol",
            "  - Diamond triple is the ultimate jackpot")));

        allEntries.add(new WikiEntry("casino_blackjack", "Blackjack", "Casino", List.of(
            "##Blackjack",
            "",
            "The classic card game. Beat the dealer by getting",
            "closer to 21 without going over.",
            "",
            "##How to Play",
            "  1. Right-click a Blackjack Table to join",
            "  2. Place your bet (1 to 10,000 MC)",
            "  3. Receive 2 cards, dealer gets 2 (1 face-down)",
            "  4. Choose your action each turn",
            "  5. Dealer reveals and plays last",
            "",
            "##Card Values",
            "  Number cards: face value (2-10)",
            "  Face cards (J/Q/K): 10",
            "  Ace: 1 or 11 (whichever is better)",
            "",
            "##Actions",
            "  HIT - Take another card",
            "  STAND - Keep your hand, end turn",
            "  DOUBLE - Double bet, take exactly 1 card",
            "  SPLIT - Split a pair into 2 hands (up to 4)",
            "  SURRENDER - Forfeit half your bet",
            "  INSURANCE - Side bet when dealer shows Ace",
            "",
            "##Payouts",
            "  Blackjack (21 with 2 cards): 3:2 (1.5x bet)",
            "  Regular Win: 1:1 (1x bet)",
            "  Push (tie): bet returned",
            "",
            "##Dealer Rules",
            "  - Dealer must hit until 17 or higher",
            "  - Dealer stands on soft 17",
            "",
            "##Multiplayer",
            "  Up to 4 players can sit at the same table.",
            "  Each player plays against the dealer independently.",
            "  You can see other players' hands.")));

        allEntries.add(new WikiEntry("casino_wheel", "Big Wheel", "Casino", List.of(
            "##Big Wheel",
            "",
            "A massive spinning wheel where all players bet",
            "together on which segment will win.",
            "",
            "##How to Play",
            "  1. Right-click the Big Wheel to open betting",
            "  2. Place bets on one or more segments",
            "  3. Wait for the betting phase to end (30 sec)",
            "  4. Watch the wheel spin!",
            "  5. If your segment wins, collect your payout",
            "",
            "##Segments & Odds",
            "  1x  - 43% chance (safest)",
            "  2x  - 13% chance",
            "  3x  - 20% chance",
            "  5x  - 13% chance",
            "  10x - 7% chance",
            "  JACKPOT (20x) - 3.3% chance (rarest!)",
            "",
            "##Payout",
            "  If your segment hits: bet x multiplier",
            "  Example: 100 MC on 10x = 1,000 MC!",
            "",
            "##Tips",
            "  - You can bet on multiple segments at once",
            "  - The wheel spins every ~45 seconds",
            "  - High multiplier segments are rare but rewarding",
            "  - All players see the same wheel result")));

        allEntries.add(new WikiEntry("casino_stats", "Casino Stats", "Casino", List.of(
            "##Casino Statistics",
            "",
            "Track your gambling history in the Casino app",
            "on your Computer.",
            "",
            "##Tracked Stats",
            "  - Total Wagered: all bets placed",
            "  - Total Won: all winnings received",
            "  - Total Lost: all bets lost",
            "  - Profit/Loss: net gains or losses",
            "  - Games Played: total game count",
            "  - Biggest Win: largest single payout",
            "",
            "##Per-Game Breakdown",
            "  - Slots: spins played, spins won",
            "  - Blackjack: hands played, hands won",
            "  - Wheel: rounds played, rounds won",
            "  - Roulette: rounds played, rounds won",
            "  - Craps: rounds played, rounds won",
            "  - Baccarat: rounds played, rounds won",
            "",
            "##How to Access",
            "  Open Computer > Casino app to view your stats.",
            "  Stats persist across sessions.")));

        allEntries.add(new WikiEntry("casino_roulette", "Roulette", "Casino", List.of(
            "##Roulette",
            "",
            "Bet on where the ball lands on a spinning wheel",
            "with numbers 0-36.",
            "",
            "##How to Play",
            "  1. Sit at a Roulette table chair",
            "  2. Choose your bet type and amount",
            "  3. Wait for the betting timer to end",
            "  4. Watch the wheel spin!",
            "",
            "##Bet Types & Payouts",
            "  Straight (single number): 35:1",
            "  Red or Black: 1:1",
            "  Odd or Even: 1:1",
            "  Low (1-18) or High (19-36): 1:1",
            "  Dozens (1-12, 13-24, 25-36): 2:1",
            "",
            "##Tips",
            "  - 0 is green (not red or black)",
            "  - Outside bets (red/black) are safer",
            "  - Straight number bets are risky but pay 35x")));

        allEntries.add(new WikiEntry("casino_craps", "Craps", "Casino", List.of(
            "##Craps",
            "",
            "A dice game where you bet on the outcome",
            "of two dice rolls.",
            "",
            "##How to Play",
            "  1. Sit at the Craps table chair",
            "  2. Place your Pass Line bet",
            "  3. Roll the dice!",
            "",
            "##Come Out Roll",
            "  7 or 11 = Instant Win (1:1)",
            "  2, 3, or 12 = Instant Loss",
            "  Any other number = Sets the Point",
            "",
            "##Point Phase",
            "  Keep rolling until:",
            "  - You hit the Point = Win (1:1)",
            "  - You roll a 7 = Lose (Seven Out)",
            "",
            "##Tips",
            "  - The come-out roll has the best odds",
            "  - Once a point is set, 7 is your enemy")));

        allEntries.add(new WikiEntry("casino_baccarat", "Baccarat", "Casino", List.of(
            "##Baccarat",
            "",
            "A card game where you bet on which side",
            "will get closest to 9: Player or Banker.",
            "",
            "##How to Play",
            "  1. Sit at the Baccarat table chair",
            "  2. Bet on Player, Banker, or Tie",
            "  3. Cards are dealt automatically",
            "",
            "##Card Values",
            "  Ace = 1, 2-9 = face value",
            "  10/J/Q/K = 0",
            "  Hand value = total mod 10 (ones digit)",
            "",
            "##Payouts",
            "  Player wins: 1:1",
            "  Banker wins: 0.95:1 (5% commission)",
            "  Tie: 8:1",
            "",
            "##Drawing Rules",
            "  Player draws on 0-5, stands on 6-7",
            "  Banker follows standard tableau rules",
            "",
            "##Tips",
            "  - Banker bet has the best odds (lowest house edge)",
            "  - Tie bet pays big but is very rare")));
    }

    // ---- BACKPACKS ----

    private void buildBackpackEntries() {
        allEntries.add(new WikiEntry("backpack_overview", "Backpacks Overview", "Backpacks", List.of(
            "##Traveler's Backpacks",
            "",
            "Wearable storage items with extra inventory space.",
            "42 unique variants, each with special abilities.",
            "",
            "##How to Use",
            "  - Right-click or press B to open backpack",
            "  - Sneak + Right-click to place as block",
            "  - Click Equip button in GUI to wear on back",
            "  - Worn backpack is visible on your character",
            "  - Break placed backpack to pick up with contents",
            "",
            "##Tiers",
            "  - Leather: 27 slots, 2 upgrades, 2 tools",
            "  - Iron: 45 slots, 3 upgrades, 3 tools",
            "  - Gold: 63 slots, 4 upgrades, 4 tools",
            "  - Diamond: 81 slots, 5 upgrades, 5 tools",
            "  - Netherite: 99 slots, 6 upgrades, 6 tools",
            "",
            "##Variant Abilities",
            "  Each non-standard backpack grants a passive effect",
            "  when worn: speed boosts, fire resistance, luck,",
            "  water breathing, and more. See individual entries.",
            "",
            "##Sleeping Bags",
            "  16 colored sleeping bags let you skip the night.",
            "  Right-click at night, no monsters within 8 blocks.",
            "  60-second cooldown between uses.",
            "",
            "##Skill Lock",
            "  Standard backpack: free for all players.",
            "  All other 41 variants + upgrades require",
            "  Explorer or Navigator specialization (Tier 3+).",
            "",
            "##GUI Buttons",
            "  - Sort: sorts backpack inventory",
            "  - Quick Stack: merges partial stacks",
            "  - Transfer In/Out: bulk move items",
            "  - Equip/Unequip: wear on back or remove")));

        allEntries.add(new WikiEntry("backpack_tiers", "Tier Upgrades", "Backpacks", List.of(
            "##Tier Upgrade System",
            "",
            "Upgrade your backpack's tier to increase storage,",
            "upgrade slots, and tool slots.",
            "",
            "##Progression Path",
            "  Leather \u2192 Iron \u2192 Gold \u2192 Diamond \u2192 Netherite",
            "",
            "##Tier Upgrade Recipes",
            "  - Iron Tier: 8 Iron Ingots + Blank Upgrade",
            "  - Gold Tier: 8 Gold Ingots + Blank Upgrade",
            "  - Diamond Tier: 8 Diamonds + Blank Upgrade",
            "  - Netherite Tier: 8 Netherite Ingots + Blank Upgrade",
            "",
            "##Blank Upgrade",
            "  The base crafting material for all upgrades.",
            "  Crafted with 4 Leather + 4 Paper + 1 Redstone.")));

        allEntries.add(new WikiEntry("backpack_upgrades", "Upgrade Items", "Backpacks", List.of(
            "##Backpack Upgrades",
            "",
            "Place upgrade items in the upgrade slots on the right",
            "side of the backpack inventory.",
            "",
            "##Functionality Upgrades",
            "  - Crafting: 3x3 crafting grid in your backpack",
            "  - Furnace: Smelt items while traveling",
            "  - Smoker: Cook food on the go",
            "  - Blast Furnace: Smelt ores faster",
            "  - Tanks: Two fluid tanks for water/lava/potions",
            "",
            "##Utility Upgrades",
            "  - Pickup: Auto-collect nearby items into backpack",
            "  - Magnet: Wider range magnetic item attraction",
            "  - Feeding: Auto-feed when hunger is low",
            "  - Refill: Auto-refill hotbar from backpack stock",
            "  - Void: Auto-trash filtered items",
            "  - Jukebox: Play music discs from your backpack")));

        addBackpackVariant("standard", "Standard Backpack", "Leather", "The basic traveler's backpack. Available to all players without skill requirements.", "None");
        addBackpackVariant("iron", "Iron Backpack", "Iron", "A sturdy iron-plated backpack.", "+2 Armor");
        addBackpackVariant("gold", "Gold Backpack", "Gold", "An ornate gold-trimmed backpack.", "Luck I");
        addBackpackVariant("diamond", "Diamond Backpack", "Diamond", "A pristine diamond-encrusted backpack.", "+4 Armor");
        addBackpackVariant("netherite", "Netherite Backpack", "Netherite", "The ultimate backpack. Fireproof.", "+4 Armor + Fire Resistance");
        addBackpackVariant("emerald", "Emerald Backpack", "Iron", "A merchant's backpack adorned with emeralds.", "Luck II");
        addBackpackVariant("creeper", "Creeper Backpack", "Gold", "A creeper-themed backpack.", "No passive (revive on death via event)");
        addBackpackVariant("dragon", "Dragon Backpack", "Netherite", "An Ender Dragon scale backpack.", "Fire Resistance");
        addBackpackVariant("enderman", "Enderman Backpack", "Diamond", "A void-touched backpack.", "Night Vision");
        addBackpackVariant("warden", "Warden Backpack", "Netherite", "A sculk-infused backpack.", "Strength + Night Vision");
        addBackpackVariant("blaze", "Blaze Backpack", "Gold", "A Nether-forged backpack.", "Fire Resistance");
        addBackpackVariant("spider", "Spider Backpack", "Iron", "A web-woven backpack.", "Slow Falling");
        addBackpackVariant("wither", "Wither Backpack", "Netherite", "A backpack imbued with wither energy.", "Resistance I");
        addBackpackVariant("fox", "Fox Backpack", "Leather", "A fluffy fox-fur backpack.", "Speed (night-time only)");
        addBackpackVariant("wolf", "Wolf Backpack", "Iron", "A loyal wolf-hide backpack.", "Strength");
        addBackpackVariant("bee", "Bee Backpack", "Leather", "A honeycomb-patterned backpack.", "Saturation (every 200 ticks)");
        addBackpackVariant("bat", "Bat Backpack", "Leather", "A dark leather backpack with wings.", "Slow Falling + Night Vision");
        addBackpackVariant("cow", "Cow Backpack", "Leather", "A spotted cowhide backpack.", "Saturation (every 400 ticks)");
        addBackpackVariant("pig", "Pig Backpack", "Leather", "A pink pig-themed backpack.", "Saturation (every 400 ticks)");
        addBackpackVariant("sheep", "Sheep Backpack", "Leather", "A fluffy wool backpack.", "Regeneration");
        addBackpackVariant("chicken", "Chicken Backpack", "Leather", "A feathered backpack.", "Slow Falling");
        addBackpackVariant("squid", "Squid Backpack", "Leather", "An ocean-themed backpack.", "Water Breathing + Dolphin's Grace");
        addBackpackVariant("villager", "Villager Backpack", "Iron", "A merchant-style backpack.", "Luck II + Hero of the Village");
        addBackpackVariant("iron_golem", "Iron Golem Backpack", "Diamond", "A heavy iron backpack.", "Resistance + Strength");
        addBackpackVariant("horse", "Horse Backpack", "Iron", "A saddlebag-style backpack.", "Speed II");
        addBackpackVariant("ocelot", "Ocelot Backpack", "Leather", "A spotted jungle cat backpack.", "Speed");
        addBackpackVariant("skeleton", "Skeleton Backpack", "Iron", "A bone-white backpack.", "No passive");
        addBackpackVariant("ghast", "Ghast Backpack", "Gold", "A ghostly white backpack.", "Slow Falling");
        addBackpackVariant("magma_cube", "Magma Cube Backpack", "Gold", "A fiery magma-themed backpack.", "Fire Resistance (Nether only)");
        addBackpackVariant("cake", "Cake Backpack", "Leather", "A sweet cake-themed backpack.", "Saturation (every 200 ticks)");
        addBackpackVariant("cactus", "Cactus Backpack", "Leather", "A prickly cactus backpack.", "No passive");
        addBackpackVariant("pumpkin", "Pumpkin Backpack", "Leather", "A spooky pumpkin backpack.", "No passive");
        addBackpackVariant("melon", "Melon Backpack", "Leather", "A juicy melon-themed backpack.", "No passive");
        addBackpackVariant("hay", "Hay Backpack", "Leather", "A farm-fresh hay bale backpack.", "No passive");
        addBackpackVariant("sponge", "Sponge Backpack", "Iron", "An absorbent sponge backpack.", "Water Breathing");
        addBackpackVariant("bookshelf", "Bookshelf Backpack", "Iron", "A scholarly backpack.", "No passive");
        addBackpackVariant("snow", "Snow Backpack", "Leather", "A frosty snowball backpack.", "Slow Falling (cold biomes only)");
        addBackpackVariant("sandstone", "Sandstone Backpack", "Leather", "A desert-carved sandstone backpack.", "Haste (desert biomes only)");
        addBackpackVariant("quartz", "Quartz Backpack", "Iron", "A smooth white quartz backpack.", "Strength I");
        addBackpackVariant("coal", "Coal Backpack", "Leather", "A charcoal-dark backpack.", "Night Vision (underground only)");
        addBackpackVariant("lapis", "Lapis Backpack", "Iron", "A deep blue lapis lazuli backpack.", "Hero of the Village");
        addBackpackVariant("redstone", "Redstone Backpack", "Iron", "A powered redstone-infused backpack.", "Haste");

        // === Progression & Combat Features ===
        allEntries.add(new WikiEntry("newgameplus", "New Game+", "Dungeons", List.of(
            "##New Game+",
            "",
            "After clearing all Infernal dungeon bosses,",
            "two new tiers unlock:",
            "",
            "--- Mythic Tier ---",
            "  Requires: All 8 Infernal bosses + Prestige 5+",
            "  Difficulty: 6x multiplier",
            "  Exclusive mythic-tier loot",
            "",
            "--- Eternal Tier ---",
            "  Requires: All 8 Mythic bosses + Prestige 15+",
            "  Difficulty: 10x multiplier",
            "  The ultimate challenge with unique rewards"
        )));
        allEntries.add(new WikiEntry("guide_mob_variants", "Mob Variants", "Guide", List.of(
            "##Elite & Champion Mobs",
            "",
            "Hostile mobs in the overworld have a chance to",
            "spawn as enhanced variants:",
            "",
            "--- Elite (5% chance) ---",
            "  2x HP, +2 armor, 1 modifier",
            "  Yellow [Elite] name tag",
            "  3x coin reward + guaranteed item drop",
            "",
            "--- Champion (1% chance) ---",
            "  3x HP, +5 armor, 2 modifiers, glowing",
            "  Gold [Champion] name tag",
            "  5x coins + rare drops + 10% special item",
            "",
            "--- Modifiers ---",
            "  Speed, Regen, Explosive, Wither Touch,",
            "  Thorns, Teleporting"
        )));
        allEntries.add(new WikiEntry("guide_bounty_hunting", "Bounty Hunting", "Guide", List.of(
            "##Bounty Hunting",
            "",
            "Hunt named mobs for MegaCoin rewards!",
            "",
            "--- How It Works ---",
            "  1. Open Bounty Board > Hunt tab",
            "  2. Accept up to 3 bounties",
            "  3. Find and kill the named bounty mob",
            "  4. Collect your reward!",
            "",
            "--- Details ---",
            "  Bounties rotate daily (5-8 available)",
            "  Bounty mobs spawn as glowing named mobs",
            "  Rewards scale by mob danger (30-500 MC)",
            "  24-hour expiry on accepted bounties"
        )));
        allEntries.add(new WikiEntry("guide_combat_combos", "Combat Combos", "Guide", List.of(
            "##Combat Combos",
            "",
            "Chain relic abilities to trigger powerful combos!",
            "Cast two abilities with matching tags within 8s.",
            "",
            "--- Combos ---",
            "  Fire + Ice = SHATTER! (AoE damage burst)",
            "  Shield + Attack = COUNTER STRIKE! (2x next hit)",
            "  Heal + Attack = LIFE DRAIN! (regen 8s)",
            "  Ice + Ice = DEEP FREEZE! (freeze nearby mobs)",
            "  Fire + Fire = INFERNO! (AoE fire damage)",
            "  Shadow + Attack = ASSASSINATE! (armor pierce)",
            "  Lightning + Arcane = OVERCHARGE! (+50% dmg 5s)",
            "  Heal + Heal = SANCTIFY! (AoE heal)",
            "  Shield + Shield = FORTRESS! (immunity 2s)",
            "  Arcane + Attack = ARCANE SURGE! (+25% XP 10s)"
        )));
        allEntries.add(new WikiEntry("guide_arena_system", "Arena System", "Guide", List.of(
            "##PvE & PvP Arena",
            "",
            "Access via the Arena app on the Computer.",
            "",
            "--- PvE Arena ---",
            "  Survive escalating waves of mobs!",
            "  Every 5th wave = mini-boss",
            "  Rewards scale with wave reached",
            "",
            "--- PvP Arena ---",
            "  1v1 structured combat with ELO ranking",
            "  Queue via Arena app, matched by skill",
            "  Best of 3 rounds, 60s per round",
            "",
            "--- Boss Rush ---",
            "  Fight all 8 dungeon bosses in sequence!",
            "  Unlocks after clearing all Infernal bosses",
            "  Timer-based with global leaderboard",
            "  Death = run over (no respawns)"
        )));
    }

    private void addBackpackVariant(String id, String name, String defaultTier, String description, String ability) {
        allEntries.add(new WikiEntry("backpack_" + id, name, "Backpacks", List.of(
            "##" + name,
            "",
            description,
            "",
            "##Default Tier: " + defaultTier,
            "  Can be upgraded with Tier Upgrade items.",
            "",
            "##Passive Ability",
            "  " + ability,
            "",
            "##Skill Requirement",
            id.equals("standard") ? "  None - available to all players." :
                "  Explorer or Navigator specialization (Tier 3+).")));
    }

    // ---- MARKETPLACE (10) ----

    private void buildMarketplaceEntries() {
        allEntries.add(new WikiEntry("market_overview", "Marketplace Overview", "Marketplace", List.of(
            "##Player Marketplace",
            "",
            "The Marketplace is MegaMod's player-driven economy",
            "hub. Buy and sell items with other players safely",
            "through the Market app on your Computer.",
            "",
            "##Two Listing Types",
            "  WTS (Want To Sell):",
            "    Post items you have for a set price.",
            "    Items are held in escrow until sold.",
            "",
            "  WTB (Want To Buy):",
            "    Post requests for items you need.",
            "    Coins are held in escrow from your bank.",
            "",
            "##How It Works",
            "  1. Open Market app on your Computer",
            "  2. Browse Buy tab (WTS listings) or Sell tab (WTB)",
            "  3. Post your own listings or contact sellers/buyers",
            "  4. The other player gets a chat notification",
            "  5. They check their Computer to respond",
            "  6. If accepted, meet at the Trading Dimension",
            "  7. Use a Trading Terminal for a secure swap",
            "",
            "##Key Features",
            "  - Online status shown for all listing owners",
            "  - Search/filter listings by item name",
            "  - Up to 10 active listings per player",
            "  - 48-hour expiry with automatic refunds",
            "  - Min price enforcement to prevent abuse",
            "  - Escrow protects both buyer and seller"
        )));

        allEntries.add(new WikiEntry("market_wts", "Selling Items (WTS)", "Marketplace", List.of(
            "##Want To Sell (WTS)",
            "",
            "Post items you own for other players to buy.",
            "",
            "##How to Post a WTS Listing",
            "  1. Open Market app > click 'Post Listing'",
            "  2. Select type: WTS",
            "  3. Enter the item name (e.g. 'diamond')",
            "  4. Set quantity and price per unit",
            "  5. Click Post",
            "",
            "##What Happens",
            "  - Items are REMOVED from your inventory (escrow)",
            "  - They're held safely until someone buys them",
            "  - If the listing expires or you cancel, items return",
            "",
            "##When Someone Wants to Buy",
            "  - You receive a chat notification",
            "  - Check Market app > Trade Requests tab",
            "  - Accept or Decline the request",
            "  - If accepted, meet at Trading Terminal",
            "",
            "##Tips",
            "  - Check existing WTB listings first - someone may",
            "    already be looking for what you're selling",
            "  - Competitive pricing sells faster",
            "  - Rare items (void shards, relics) fetch high prices"
        )));

        allEntries.add(new WikiEntry("market_wtb", "Buying Items (WTB)", "Marketplace", List.of(
            "##Want To Buy (WTB)",
            "",
            "Post requests for items you need, and other players",
            "can fulfill them.",
            "",
            "##How to Post a WTB Listing",
            "  1. Open Market app > click 'Post Listing'",
            "  2. Select type: WTB",
            "  3. Enter item name, quantity, and price per unit",
            "  4. Click Post",
            "",
            "##What Happens",
            "  - Coins are DEDUCTED from your bank (escrow)",
            "  - They're held safely until someone sells to you",
            "  - If the listing expires or you cancel, coins return",
            "",
            "##When Someone Wants to Sell",
            "  - You receive a chat notification",
            "  - Check Market app > Trade Requests tab",
            "  - Accept or Decline their offer",
            "  - If accepted, meet at Trading Terminal",
            "",
            "##Minimum Prices",
            "  Each item has a minimum price per unit:",
            "  Common (dirt, cobble): 1 MC",
            "  Logs, crops: 2-3 MC",
            "  Ores (iron, gold): 5-8 MC",
            "  Rare (diamond, emerald): 15-25 MC",
            "  Very rare (netherite, elytra): 50-200 MC"
        )));

        allEntries.add(new WikiEntry("market_trading", "Trading Process", "Marketplace", List.of(
            "##How a Trade Works End-to-End",
            "",
            "##Step 1: Browse & Contact",
            "  - Browse listings in Buy or Sell tab",
            "  - Click [Contact] on a listing you want",
            "  - A message is sent to the listing owner:",
            "    '[Market] PlayerName wants to buy/sell your items!'",
            "",
            "##Step 2: Response",
            "  - The listing owner checks their Computer",
            "  - They see the trade request in their inbox",
            "  - They can Accept or Decline",
            "",
            "##Step 3: Meet Up",
            "  - If accepted, both players are notified:",
            "    'Meet at the Trading Terminal!'",
            "  - Both players go to a Trading Terminal block",
            "  - (Optional: use the Trading Dimension for safety)",
            "",
            "##Step 4: Secure Swap",
            "  - Both players interact with the same Terminal",
            "  - Each places their offered items/coins",
            "  - Both must click Confirm",
            "  - Atomic swap: items/coins exchange simultaneously",
            "  - No possibility of scamming!",
            "",
            "##Trade Requests expire after 5 minutes"
        )));

        allEntries.add(new WikiEntry("market_terminal", "Trading Terminal", "Marketplace", List.of(
            "##Trading Terminal Block",
            "",
            "A secure block for completing player-to-player trades.",
            "Ensures both parties fulfill their end of the deal.",
            "",
            "##Crafting",
            "  Place at any location, or find one pre-built",
            "  in the Trading Dimension.",
            "",
            "##How to Use",
            "  1. Both traders right-click the same Terminal",
            "  2. Your side: add items from inventory + set coins",
            "  3. See what the other player is offering",
            "  4. When satisfied, click [Confirm]",
            "  5. When BOTH confirm, trade executes instantly",
            "",
            "##Safety Features",
            "  - Cannot break Terminal during active trade",
            "  - 120-second timeout prevents stalling",
            "  - Items only transfer when BOTH confirm",
            "  - Either player can cancel at any time",
            "  - Distance check: must stay near Terminal",
            "",
            "##The Terminal prevents ALL forms of trade scamming."
        )));

        allEntries.add(new WikiEntry("market_dimension", "Trading Dimension", "Marketplace", List.of(
            "##Trading Dimension",
            "",
            "A secure pocket dimension for trade meetups.",
            "",
            "##How to Access",
            "  When a trade is accepted, both players can",
            "  teleport to a private trading room via the",
            "  Market app's accepted trade notification.",
            "",
            "##The Trading Room",
            "  - Small 15x15 glass-walled room",
            "  - Pre-built Trading Terminal in the center",
            "  - Two entry points (one per player)",
            "  - Portal to return to the overworld",
            "  - Decorated with carpets and lighting",
            "",
            "##Why Use It",
            "  - Neutral ground: no PvP advantage",
            "  - No mob interference",
            "  - Pre-placed Terminal ready to use",
            "  - Private: only the two traders can enter"
        )));

        allEntries.add(new WikiEntry("market_escrow", "Escrow System", "Marketplace", List.of(
            "##How Escrow Works",
            "",
            "Escrow protects both parties in every trade.",
            "",
            "##WTS Escrow (Items)",
            "  When you post a WTS listing:",
            "  - Items are removed from your inventory",
            "  - Held safely in the marketplace system",
            "  - Returned if listing expires or is cancelled",
            "  - Transferred to buyer on successful trade",
            "",
            "##WTB Escrow (Coins)",
            "  When you post a WTB listing:",
            "  - Coins are deducted from your BANK",
            "  - Held safely in the marketplace system",
            "  - Returned if listing expires or is cancelled",
            "  - Transferred to seller on successful trade",
            "",
            "##This means:",
            "  - Sellers always have the items they claim",
            "  - Buyers always have the coins they offer",
            "  - No bait-and-switch possible",
            "  - Server restart safe (NbtIo persistent)"
        )));

        allEntries.add(new WikiEntry("market_notifications", "Notifications", "Marketplace", List.of(
            "##Marketplace Notifications",
            "",
            "Stay informed about your trading activity.",
            "",
            "##Chat Notifications",
            "  You receive gold chat messages when:",
            "  - Someone wants to buy/sell your listing",
            "  - A trade request is accepted or declined",
            "  - A listing expires or is fulfilled",
            "",
            "##Computer Inbox",
            "  The Market app has an Inbox tab showing:",
            "  - Incoming trade requests",
            "  - Outgoing trade requests and their status",
            "  - Notification history",
            "",
            "##Offline Players",
            "  - Listings show online/offline status (green/grey dot)",
            "  - You CAN contact offline players",
            "  - They'll see the notification when they log in",
            "  - Trade requests from offline contacts expire in 5 min"
        )));

        allEntries.add(new WikiEntry("market_my_listings", "Managing Listings", "Marketplace", List.of(
            "##My Listings",
            "",
            "Manage all your active marketplace listings.",
            "",
            "##Viewing Your Listings",
            "  Open Market app > My Listings tab",
            "  Shows all your active WTS and WTB listings",
            "  with status, time remaining, and controls.",
            "",
            "##Cancelling a Listing",
            "  Click [Cancel] on any active listing",
            "  - WTS: items returned to your inventory",
            "  - WTB: coins returned to your bank",
            "",
            "##Limits",
            "  - Maximum 10 active listings per player",
            "  - Listings expire after 48 hours",
            "  - Expired WTS: items returned to inventory",
            "  - Expired WTB: coins returned to bank",
            "",
            "##Sorting & Search",
            "  - Sort by: Newest, Price Low-High, Price High-Low",
            "  - Search bar filters by item name",
            "  - Auto-refresh every 3 seconds"
        )));

        allEntries.add(new WikiEntry("market_tips", "Market Tips", "Marketplace", List.of(
            "##Tips for Marketplace Success",
            "",
            "##For Sellers",
            "  - Check WTB listings first: instant demand!",
            "  - Price competitively vs other WTS listings",
            "  - Bulk sales (64 stacks) attract more buyers",
            "  - Rare dungeon drops command premium prices",
            "  - Relist expired items at adjusted prices",
            "",
            "##For Buyers",
            "  - Post WTB listings for hard-to-find items",
            "  - Offer fair prices: too low gets ignored",
            "  - Check back often: new WTS listings appear",
            "  - Message sellers directly for custom deals",
            "",
            "##General",
            "  - Always use the Trading Terminal for safety",
            "  - The Trading Dimension is the safest meetup spot",
            "  - Keep some coins in your bank for WTB escrow",
            "  - Min prices prevent market manipulation",
            "  - Build a reputation: fair traders get repeat business"
        )));
    }

    // ---- CORRUPTION (10) ----

    private void buildCorruptionEntries() {
        allEntries.add(new WikiEntry("corruption_overview", "Corruption Overview", "Corruption", List.of(
            "##World Corruption",
            "",
            "A dark force spreads across the world, corrupting",
            "the land and spawning dangerous mobs. If left",
            "unchecked, it will consume everything.",
            "",
            "##What Is Corruption?",
            "  Corruption zones are hostile areas that:",
            "  - Expand over time (radius grows)",
            "  - Spawn powerful corrupted mobs",
            "  - Apply debuffs to players inside",
            "  - Have 4 tiers of increasing danger",
            "",
            "##How It Appears",
            "  - Natural spawning: 10% chance per MC day",
            "  - Spawns 200-500 blocks from online players",
            "  - Never spawns near claimed faction territory",
            "  - Maximum 8 active zones at once",
            "  - Announced to all: '[Corruption] A dark",
            "    corruption has appeared near [biome]!'",
            "",
            "##How to Fight It",
            "  - Claim territory with your faction (pushes it back)",
            "  - Station military recruits in the area",
            "  - Launch a Purge to destroy the zone",
            "  - See the Purge Events entry for details"
        )));

        allEntries.add(new WikiEntry("corruption_zones", "Corruption Zones", "Corruption", List.of(
            "##Zone Structure",
            "",
            "Each corruption zone has a center point and an",
            "expanding radius. The zone is circular.",
            "",
            "##Zone Tiers (1-4)",
            "  Tier 1: Max radius 64 blocks",
            "    - Spreads every 10 minutes",
            "    - Spawns zombies & skeletons",
            "    - Mild debuffs",
            "",
            "  Tier 2: Max radius 80 blocks",
            "    - Spreads every 7.5 minutes",
            "    - Adds spiders & creepers",
            "    - Moderate debuffs",
            "",
            "  Tier 3: Max radius 96 blocks",
            "    - Spreads every 5 minutes",
            "    - Adds witches, phantoms, strays",
            "    - Severe debuffs (mining fatigue)",
            "",
            "  Tier 4: Max radius 128 blocks",
            "    - Spreads every 2.5 minutes",
            "    - Adds wither skeletons, evokers, ravagers",
            "    - Extreme debuffs (darkness, wither)",
            "",
            "##Zone Sources",
            "  Natural, dungeon failure, world events, or",
            "  child zones spawned from tier 3+ parents"
        )));

        allEntries.add(new WikiEntry("corruption_effects", "Corruption Effects", "Corruption", List.of(
            "##Effects on Players",
            "",
            "While inside a corruption zone, players suffer",
            "tier-based debuffs applied every 5 seconds:",
            "",
            "##All Tiers",
            "  - Slowness I (reduced movement speed)",
            "  - Ambient damage: 0.5 hearts every 5 seconds",
            "",
            "##Tier 3+",
            "  - Mining Fatigue I (slower block breaking)",
            "",
            "##Tier 4",
            "  - Darkness (reduced visibility)",
            "  - Wither I (1 heart damage every 5 seconds)",
            "",
            "##Visual Indicators",
            "  - Screen vignette effect (red tint)",
            "  - Intensity scales with zone tier",
            "  - HUD shows corruption tier when inside",
            "",
            "##Mob Buffs Inside Corruption",
            "  - +25% HP per tier",
            "  - +15% damage per tier",
            "  - Corrupted mobs don't despawn naturally"
        )));

        allEntries.add(new WikiEntry("corruption_spread", "How Corruption Spreads", "Corruption", List.of(
            "##Spread Mechanics",
            "",
            "Corruption zones grow their radius over time.",
            "",
            "##Spread Rate (by tier)",
            "  Tier 1: +1 block radius every 10 minutes",
            "  Tier 2: +1 block radius every 7.5 minutes",
            "  Tier 3: +1 block radius every 5 minutes",
            "  Tier 4: +1 block radius every 2.5 minutes",
            "",
            "##Child Zone Spawning",
            "  Tier 3+ zones have a 5% chance per tick cycle",
            "  to spawn a new Tier 1 zone within 256 blocks.",
            "  This is how corruption can cascade across the world.",
            "",
            "##Spread Blockers",
            "  - Faction claimed chunks block spread completely",
            "  - Each claimed chunk reduces effective radius by 8",
            "  - Each military recruit reduces radius by 2 more",
            "  - 3+ recruits in a claimed chunk = total immunity",
            "",
            "##Natural Spawning",
            "  - 10% chance per Minecraft day",
            "  - 200-500 blocks from a random online player",
            "  - Never within 128 blocks of faction claims",
            "  - Never within 64 blocks of existing corruption",
            "  - Maximum 8 active zones in the world"
        )));

        allEntries.add(new WikiEntry("corruption_defense", "Defending Against Corruption", "Corruption", List.of(
            "##Colony Defense",
            "",
            "Your faction is your best defense against corruption.",
            "",
            "##Claim Territory",
            "  Claimed chunks cannot be corrupted.",
            "  Each claimed chunk within a zone's radius",
            "  reduces the effective radius by 8 blocks.",
            "  Claim more chunks = smaller corruption zone!",
            "",
            "##Station Military Recruits",
            "  Each military recruit in the corruption zone",
            "  reduces effective radius by an additional 2 blocks.",
            "  Military jobs that count:",
            "  - Recruit, Shieldman, Bowman",
            "  - Crossbowman, Nomad, Horseman",
            "  - Commander, Captain",
            "",
            "##Total Immunity",
            "  A claimed chunk with 3+ military recruits",
            "  is completely immune to corruption spread.",
            "",
            "##Strategic Tips",
            "  - Claim chunks facing the corruption first",
            "  - Station 3+ recruits per border chunk",
            "  - Use Shieldmen for frontline defense",
            "  - Launch a Purge to eliminate the zone entirely"
        )));

        allEntries.add(new WikiEntry("corruption_purge", "Purge Events", "Corruption", List.of(
            "##Launching a Purge",
            "",
            "A purge is a combat event to destroy a corruption",
            "zone permanently.",
            "",
            "##How to Start",
            "  - Be within 64 blocks of a corruption zone",
            "  - Use /megamod corruption purge <zoneId>",
            "",
            "##How It Works",
            "  1. Purge spawns waves of corrupted mobs",
            "  2. Kill count required: 10 x tier x tier",
            "     Tier 1: 10 kills, Tier 2: 40 kills",
            "     Tier 3: 90 kills, Tier 4: 160 kills",
            "  3. Mobs spawn in 3 waves (every ~100 seconds)",
            "  4. All kills by participants within the zone count",
            "  5. When kill target reached: zone is destroyed!",
            "",
            "##Duration",
            "  5 minutes (6000 ticks) to complete the purge.",
            "  If time runs out before enough kills:",
            "  - Zone tier INCREASES by 1 (max tier 4)",
            "  - Zone radius grows by 8 blocks",
            "  - Failure is broadcast to all players",
            "",
            "##Faction recruits in the zone contribute kills too!"
        )));

        allEntries.add(new WikiEntry("corruption_rewards", "Purge Rewards", "Corruption", List.of(
            "##Rewards for Completing a Purge",
            "",
            "Everyone who participated gets rewarded!",
            "",
            "##MegaCoins",
            "  Each participant receives: tier x 50 MC",
            "  Tier 1: 50 MC, Tier 2: 100 MC",
            "  Tier 3: 150 MC, Tier 4: 200 MC",
            "",
            "##Combat XP",
            "  Bonus experience points for the Combat skill tree.",
            "  Higher tier = more XP.",
            "",
            "##Corruption Shards",
            "  Corrupted mobs drop Corruption Shards (25% chance).",
            "  Breaking blocks in corruption also drops them (5%).",
            "  These may be used in future crafting recipes.",
            "",
            "##Participation",
            "  Anyone who scores at least 1 kill in the zone",
            "  during the purge is counted as a participant.",
            "  Faction recruits contribute kills but rewards",
            "  go to their owning player."
        )));

        allEntries.add(new WikiEntry("corruption_mobs", "Corrupted Mobs", "Corruption", List.of(
            "##Mob Types by Tier",
            "",
            "##Tier 1 (2-4 mobs per spawn cycle)",
            "  - Zombies",
            "  - Skeletons",
            "",
            "##Tier 2 (3-5 mobs)",
            "  - All Tier 1 mobs",
            "  - Spiders",
            "  - Creepers",
            "",
            "##Tier 3 (4-7 mobs)",
            "  - All Tier 2 mobs",
            "  - Witches",
            "  - Phantoms",
            "  - Strays",
            "",
            "##Tier 4 (5-10 mobs)",
            "  - All Tier 3 mobs",
            "  - Wither Skeletons",
            "  - Evokers",
            "  - Ravagers",
            "",
            "##Corrupted Mob Buffs",
            "  Per tier: +25% HP, +15% damage",
            "  Tier 4 mobs have +100% HP and +60% damage!",
            "  Corrupted mobs are persistent (no despawn).",
            "  Mob spawns occur every 400 ticks (20 seconds)."
        )));

        allEntries.add(new WikiEntry("corruption_colony", "Colony vs Corruption", "Corruption", List.of(
            "##Faction Territory Defense",
            "",
            "Corruption and colonies are natural enemies.",
            "Your faction's claims actively push back corruption.",
            "",
            "##Defense Mechanics",
            "  1. Claimed chunk in zone: -8 effective radius",
            "  2. Military recruit in zone: -2 effective radius",
            "  3. 3+ recruits in claimed chunk: total immunity",
            "",
            "##Example",
            "  A Tier 2 zone with radius 40:",
            "  - 3 claimed chunks in range: 40 - 24 = 16 radius",
            "  - Plus 5 recruits: 16 - 10 = 6 radius",
            "  - The corruption is nearly contained!",
            "",
            "##Colony Pushback (Every ~10 minutes)",
            "  The system recalculates colony influence:",
            "  - Claimed chunks reduce effective radius",
            "  - Zones can be shrunk to nothing by claiming",
            "",
            "##Strategy",
            "  - Claim aggressively toward corruption",
            "  - Use cheap recruits (40 MC each) for defense",
            "  - Coordinate with allies for shared defense",
            "  - Purge before corruption reaches tier 3+"
        )));

        allEntries.add(new WikiEntry("corruption_commands", "Corruption Commands", "Corruption", List.of(
            "##Available Commands",
            "",
            "  /megamod corruption status",
            "    Shows nearby corruption zone info",
            "",
            "  /megamod corruption purge <zoneId>",
            "    Start a purge event to destroy a zone",
            "    (must be within 64 blocks of the zone)",
            "",
            "##Tips",
            "  - Use 'status' to check zone tier & radius",
            "  - Coordinate purge parties for higher tiers",
            "  - Claim territory to passively shrink zones"
        )));
    }

    // ---- ALCHEMY (15) ----

    private void buildAlchemyEntries() {
        allEntries.add(new WikiEntry("alchemy_overview", "Alchemy Overview", "Alchemy", List.of(
            "##The Alchemy System",
            "",
            "A custom potion brewing system with unique effects",
            "far beyond vanilla Minecraft potions.",
            "",
            "##Key Features",
            "  - 10 unique reagents ground from vanilla items",
            "  - 15 custom potions with powerful effects",
            "  - 12 unique MobEffects (tempest, berserker, etc.)",
            "  - Alchemy Cauldron for brewing",
            "  - Alchemy Grindstone for reagent preparation",
            "  - 5 tiers of recipes locked behind Arcane skill",
            "  - Recipe discovery system",
            "",
            "##Getting Started",
            "  1. Level Arcane skill tree to at least 5",
            "  2. Craft an Alchemy Grindstone",
            "  3. Craft an Alchemy Cauldron",
            "  4. Grind vanilla items into reagents",
            "  5. Combine 3 reagents in the cauldron to brew!",
            "",
            "##Skill Requirements",
            "  Tier 1: Arcane level 5+",
            "  Tier 2: Arcane level 10+",
            "  Tier 3: Mana Weaver 1 node unlocked",
            "  Tier 4: Mana Weaver 3 node unlocked",
            "  Tier 5: Mana Weaver 5 capstone unlocked"
        )));

        allEntries.add(new WikiEntry("alchemy_cauldron", "Alchemy Cauldron", "Alchemy", List.of(
            "##Alchemy Cauldron Block",
            "",
            "The cauldron is where potions are brewed from reagents.",
            "",
            "##Setup",
            "  Place the cauldron with a heat source below:",
            "  - Fire (flint & steel the block below)",
            "  - Lava (place lava below)",
            "  - Campfire (place campfire below)",
            "  Without heat, brewing will not progress!",
            "",
            "##Brewing Process",
            "  1. Right-click with Water Bucket to fill",
            "  2. Right-click with reagents to add (max 3)",
            "  3. Wait for brewing progress (200 ticks = 10s)",
            "  4. Right-click with Glass Bottle to collect result",
            "",
            "##Tips",
            "  - The Stir button speeds up brewing slightly",
            "  - Higher Alchemy Level = faster brewing",
            "  - Watch for particles: they indicate brewing state",
            "  - Purple bubbles = brewing in progress",
            "  - Invalid recipes produce failed brew (wasted reagents)",
            "",
            "##The cauldron GUI shows ingredients, progress,",
            "##and possible recipes based on current inputs."
        )));

        allEntries.add(new WikiEntry("alchemy_grindstone", "Alchemy Grindstone", "Alchemy", List.of(
            "##Alchemy Grindstone Block",
            "",
            "Converts vanilla Minecraft items into reagents",
            "used in alchemy brewing.",
            "",
            "##How to Use",
            "  1. Right-click the Grindstone",
            "  2. Insert a valid vanilla item",
            "  3. Grinding takes 100 ticks (~5 seconds)",
            "  4. Collect the reagent output",
            "",
            "##Grinding Yields",
            "  Some items produce more reagent than others:",
            "  - Blaze Powder -> 2x Ember Dust",
            "  - Blue Ice -> 1x Frost Crystal",
            "  - Glistering Melon -> 2x Life Bloom",
            "  - Experience Bottle -> 3x Arcane Flux",
            "  - Most items -> 1-2 reagent",
            "",
            "##The Grindstone GUI shows a recipe list on the",
            "##side for quick reference of what you can grind."
        )));

        allEntries.add(new WikiEntry("alchemy_reagents", "Reagents Guide", "Alchemy", List.of(
            "##All 10 Reagents",
            "",
            "##Ember Dust (fire element)",
            "  Source: Blaze Powder (x2)",
            "  Used in: Inferno, Tempest, Berserker, Midas Touch",
            "",
            "##Frost Crystal (ice element)",
            "  Source: Blue Ice (x1), Packed Ice (x1)",
            "  Used in: Glacier, Stone Skin, Eagle Eye",
            "",
            "##Shadow Essence (dark element)",
            "  Source: Ink Sac + Coal (x2)",
            "  Used in: Shadow Step",
            "",
            "##Life Bloom (life element)",
            "  Source: Golden Apple (x1), Glistering Melon (x2)",
            "  Used in: Inferno, Vitality, Swiftbrew, Iron Gut, Undying",
            "",
            "##Void Salt (void element)",
            "  Source: Ender Pearl (x1), Chorus Fruit (x1)",
            "  Used in: Shadow Step, Void Walk, Eagle Eye, Undying",
            "",
            "##Storm Charge (lightning element)",
            "  Source: Gunpowder (x2)",
            "  Used in: Glacier, Tempest, Swiftbrew, Eagle Eye",
            "",
            "##Blood Moss (blood element)",
            "  Source: Spider Eye (x2), Red Mushroom (x1)",
            "  Used in: Berserker, Iron Gut",
            "",
            "##Starlight Dew (light element)",
            "  Source: Glowstone Dust (x2), Amethyst Shard (x1)",
            "  Used in: Void Walk, Starlight, Arcane Surge, Midas",
            "",
            "##Earth Root (earth element)",
            "  Source: Clay Ball (x2)",
            "  Used in: Vitality, Stone Skin, Swiftbrew, Iron Gut",
            "",
            "##Arcane Flux (magic element)",
            "  Source: Lapis (x1), Redstone (x1), XP Bottle (x3)",
            "  Used in: Starlight, Arcane Surge, Midas, Undying"
        )));

        allEntries.add(new WikiEntry("alchemy_recipes", "All Brewing Recipes", "Alchemy", List.of(
            "##Complete Recipe List (3 reagents each)",
            "",
            "##Tier 1 (Arcane 5+)",
            "  Storm Charge + Earth Root + Life Bloom = Swiftbrew",
            "  Earth Root + Life Bloom + Blood Moss = Iron Gut",
            "  Earth Root + Earth Root + Frost Crystal = Stone Skin",
            "",
            "##Tier 2 (Arcane 10+)",
            "  Ember Dust + Ember Dust + Life Bloom = Inferno",
            "  Frost Crystal + Frost Crystal + Storm Charge = Glacier",
            "  Life Bloom + Life Bloom + Earth Root = Vitality",
            "",
            "##Tier 3 (Mana Weaver 1)",
            "  Shadow Ess. + Shadow Ess. + Void Salt = Shadow Step",
            "  Storm Charge + Storm Charge + Ember Dust = Tempest",
            "  Blood Moss + Blood Moss + Ember Dust = Berserker",
            "  Starlight Dew + Starlight Dew + Arcane Flux = Starlight",
            "",
            "##Tier 4 (Mana Weaver 3)",
            "  Void Salt + Void Salt + Starlight Dew = Void Walk",
            "  Arcane Flux + Arcane Flux + Starlight Dew = Arcane Surge",
            "  Arcane Flux + Ember Dust + Starlight Dew = Midas Touch",
            "  Frost Crystal + Storm Charge + Void Salt = Eagle Eye",
            "",
            "##Tier 5 (Mana Weaver 5 Capstone)",
            "  Life Bloom + Void Salt + Arcane Flux = Undying"
        )));

        allEntries.add(new WikiEntry("alchemy_tier1", "Tier 1 Potions", "Alchemy", List.of(
            "##Tier 1 Potions (Arcane Level 5+)",
            "",
            "##Potion of Swiftbrew",
            "  Speed III + Jump Boost II for 30 seconds",
            "  Recipe: Storm Charge + Earth Root + Life Bloom",
            "  Great for travel and parkour. Basic but very useful.",
            "",
            "##Potion of Iron Gut",
            "  Hunger immunity + Saturation + Poison immunity",
            "  Duration: 120 seconds",
            "  Recipe: Earth Root + Life Bloom + Blood Moss",
            "  Perfect for long mining or dungeon sessions.",
            "  Ignore all food needs for 2 full minutes.",
            "",
            "##Potion of Stone Skin",
            "  Resistance III + Slowness I + Knockback immunity",
            "  Duration: 30 seconds",
            "  Recipe: Earth Root + Earth Root + Frost Crystal",
            "  Become a tank: massive defense but slower movement.",
            "  Knockback immunity makes you immovable."
        )));

        allEntries.add(new WikiEntry("alchemy_tier2", "Tier 2 Potions", "Alchemy", List.of(
            "##Tier 2 Potions (Arcane Level 10+)",
            "",
            "##Potion of Inferno",
            "  Fire damage boost + Fire resistance for 30 seconds",
            "  Recipe: Ember Dust + Ember Dust + Life Bloom",
            "  Your attacks deal bonus fire damage.",
            "  Full fire immunity while active.",
            "",
            "##Potion of Glacier",
            "  Slowness aura to nearby mobs + Frost resistance",
            "  Duration: 45 seconds",
            "  Recipe: Frost Crystal + Frost Crystal + Storm Charge",
            "  All mobs within range are slowed.",
            "  You're immune to slowness effects.",
            "",
            "##Potion of Vitality",
            "  Regeneration III + Absorption II for 30 seconds",
            "  Recipe: Life Bloom + Life Bloom + Earth Root",
            "  Rapid health regeneration plus extra hearts.",
            "  Excellent for boss fights and purges."
        )));

        allEntries.add(new WikiEntry("alchemy_tier3", "Tier 3 Potions", "Alchemy", List.of(
            "##Tier 3 Potions (Mana Weaver 1 Required)",
            "",
            "##Potion of Shadow Step",
            "  Invisibility + Speed II + Silent footsteps (20s)",
            "  Recipe: Shadow Essence x2 + Void Salt",
            "  True stealth: no particles, no sounds.",
            "",
            "##Potion of Tempest",
            "  Lightning strikes a random nearby mob every 3s (30s)",
            "  Recipe: Storm Charge x2 + Ember Dust",
            "  Devastating AoE damage in a crowd.",
            "",
            "##Potion of Berserker",
            "  Strength III + 10% lifesteal - 50% defense (20s)",
            "  Recipe: Blood Moss x2 + Ember Dust",
            "  High risk, high reward: massive damage but fragile.",
            "  Every hit heals you for 10% of damage dealt.",
            "",
            "##Potion of Starlight",
            "  Night Vision + 32-block mob detection + Luck (60s)",
            "  Recipe: Starlight Dew x2 + Arcane Flux",
            "  See in the dark and detect all nearby mobs.",
            "  Luck effect boosts loot table quality."
        )));

        allEntries.add(new WikiEntry("alchemy_tier4", "Tier 4 Potions", "Alchemy", List.of(
            "##Tier 4 Potions (Mana Weaver 3 Required)",
            "",
            "##Potion of Void Walk",
            "  No fall damage + Float control + 2x teleport range (25s)",
            "  Recipe: Void Salt x2 + Starlight Dew",
            "  Control your vertical movement freely.",
            "  Ender pearl range doubled.",
            "",
            "##Potion of Arcane Surge",
            "  +50% ability power + 30% cooldown reduction (45s)",
            "  Recipe: Arcane Flux x2 + Starlight Dew",
            "  Supercharges all relic and weapon abilities.",
            "",
            "##Potion of Midas Touch",
            "  +100% MegaCoin drops from mobs (60s)",
            "  Recipe: Arcane Flux + Ember Dust + Starlight Dew",
            "  Double all coin drops for 1 full minute!",
            "  Stack with Merchant citizen income.",
            "",
            "##Potion of Eagle Eye",
            "  +50% ranged damage + Glowing projectiles (30s)",
            "  Recipe: Frost Crystal + Storm Charge + Void Salt",
            "  Arrows and projectiles glow and hit harder.",
            "  No arrow gravity: they fly straight."
        )));

        allEntries.add(new WikiEntry("alchemy_tier5", "Tier 5: Undying", "Alchemy", List.of(
            "##Tier 5 Potion (Mana Weaver 5 Capstone Required)",
            "",
            "##Potion of Undying",
            "  Totem of Undying effect ONCE within 120 seconds",
            "  Recipe: Life Bloom + Void Salt + Arcane Flux",
            "",
            "##How It Works",
            "  - Drink the potion to gain the Undying Grace buff",
            "  - Duration: 120 seconds (2 minutes)",
            "  - If you take lethal damage during this time:",
            "    - Death is prevented (like a Totem of Undying)",
            "    - You're restored to half health",
            "    - Absorption hearts are granted",
            "    - The effect is consumed (one-time use)",
            "",
            "##Strategy",
            "  - Drink before entering dangerous content",
            "  - Essential for Tier 4 corruption purges",
            "  - Great insurance for Infernal dungeon bosses",
            "  - Does NOT stack with actual Totem of Undying",
            "  - 120s duration means you must time it well",
            "",
            "##This is the most powerful potion in the game.",
            "##It requires the capstone node of the Mana Weaver",
            "##branch in the Arcane skill tree."
        )));

        allEntries.add(new WikiEntry("alchemy_effects", "Custom Effects", "Alchemy", List.of(
            "##12 Custom MobEffects",
            "",
            "##Inferno Boost - +50% fire damage on attacks",
            "##Frost Aura - AoE Slowness to mobs every 1 second",
            "##Shadow Step - True invisibility + silence",
            "##Void Walk - No fall damage + controlled levitation",
            "##Tempest - Lightning on a random mob every 3s",
            "##Berserker Rage - +75% damage, -50% defense, 10% heal",
            "##Starlight - Mobs in 32 blocks glow (detection)",
            "##Stone Skin - Complete knockback immunity",
            "##Arcane Surge - +50% ability power, -30% cooldowns",
            "##Eagle Eye - +50% ranged damage, no arrow gravity",
            "##Undying Grace - Prevent 1 lethal hit (like Totem)",
            "##Midas Touch - +100% MegaCoin mob drops",
            "",
            "##These are unique to MegaMod Alchemy.",
            "They cannot be obtained through vanilla brewing.",
            "Each effect has custom gameplay logic beyond",
            "simple stat modifiers."
        )));

        allEntries.add(new WikiEntry("alchemy_skill_req", "Skill Requirements", "Alchemy", List.of(
            "##Alchemy Skill Requirements",
            "",
            "Alchemy is locked behind the Arcane skill tree.",
            "You must invest in the Mana Weaver branch to",
            "unlock higher-tier recipes.",
            "",
            "##Tier Breakdown",
            "",
            "  Tier 1 - Arcane Level 5+",
            "    Swiftbrew, Iron Gut, Stone Skin",
            "    Basic utility potions. Easy to reach.",
            "",
            "  Tier 2 - Arcane Level 10+",
            "    Inferno, Glacier, Vitality",
            "    Combat-focused potions.",
            "",
            "  Tier 3 - Mana Weaver 1 node",
            "    Shadow Step, Tempest, Berserker, Starlight",
            "    Powerful effects. Requires specialization.",
            "",
            "  Tier 4 - Mana Weaver 3 node",
            "    Void Walk, Arcane Surge, Midas Touch, Eagle Eye",
            "    Top-tier potions. Deep Arcane investment.",
            "",
            "  Tier 5 - Mana Weaver 5 capstone",
            "    Undying",
            "    The ultimate potion. Capstone required.",
            "",
            "##Open the Skill Tree (K key) and navigate to",
            "##Arcane > Mana Weaver to plan your path."
        )));

        allEntries.add(new WikiEntry("alchemy_discovery", "Recipe Discovery", "Alchemy", List.of(
            "##Recipe Discovery System",
            "",
            "Successfully brewing a potion permanently discovers",
            "that recipe for your character.",
            "",
            "##How Discovery Works",
            "  - First successful brew: recipe is 'discovered'",
            "  - Discovered recipes show full details in wiki",
            "  - Undiscovered recipes show '???' in the wiki",
            "  - The Cauldron GUI shows hints for partial inputs",
            "",
            "##Alchemy Level",
            "  Your alchemy level increases with total brews:",
            "  - Tracks how many potions you've brewed overall",
            "  - Higher level = faster brewing speed",
            "  - Bonus chance for extra potions at high levels",
            "",
            "##Tips for Discovery",
            "  - Experiment with different reagent combinations",
            "  - Each recipe uses exactly 3 reagents",
            "  - Order doesn't matter (reagents are unordered)",
            "  - Adding 2 reagents shows possible third reagents",
            "  - Failed brews waste reagents but give hints"
        )));

        allEntries.add(new WikiEntry("alchemy_tips", "Alchemy Tips", "Alchemy", List.of(
            "##Tips & Strategies",
            "",
            "##Efficient Reagent Gathering",
            "  - Blaze farm = unlimited Ember Dust",
            "  - Witch farm = Spider Eyes (Blood Moss)",
            "  - Enderman farm = Ender Pearls (Void Salt)",
            "  - XP farm + bottles = Arcane Flux (x3 each!)",
            "  - Clay is abundant: easy Earth Root supply",
            "",
            "##Best Potions for Each Situation",
            "  Dungeons: Vitality + Berserker (or Stone Skin)",
            "  Corruption Purge: Inferno + Tempest (AoE focus)",
            "  PvP Arena: Shadow Step + Eagle Eye",
            "  Farming coins: Midas Touch (60s of 2x drops)",
            "  Exploration: Starlight + Swiftbrew",
            "  Boss fights: Undying + Arcane Surge",
            "",
            "##Stacking Effects",
            "  Most alchemy effects stack with vanilla potions!",
            "  Combine Berserker Rage with vanilla Strength",
            "  for devastating damage output.",
            "",
            "##Brewing Efficiency",
            "  Higher Alchemy Level = faster brew times.",
            "  Brew cheap Tier 1 potions to level up quickly.",
            "  Stir the cauldron to speed up brewing."
        )));
    }
}

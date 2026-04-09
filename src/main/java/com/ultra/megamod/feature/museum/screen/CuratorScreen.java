/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.neoforged.neoforge.client.network.ClientPacketDistributor
 */
package com.ultra.megamod.feature.museum.screen;

import com.ultra.megamod.feature.museum.catalog.AchievementCatalog;
import com.ultra.megamod.feature.museum.catalog.AquariumCatalog;
import com.ultra.megamod.feature.museum.catalog.ArtCatalog;
import com.ultra.megamod.feature.museum.catalog.ItemCatalog;
import com.ultra.megamod.feature.museum.catalog.WildlifeCatalog;
import com.ultra.megamod.feature.museum.network.MuseumActionPayload;
import com.ultra.megamod.feature.ui.UIHelper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class CuratorScreen
extends Screen {
    private static final int BG_COLOR = -15069688;
    private static final int PANEL_COLOR = -12967400;
    private static final int BORDER_COLOR = -3626932;
    private static final int TITLE_BG = -14018544;
    private static final int ACCENT = -2448096;
    private static final int TEXT_PRIMARY = -991040;
    private static final int TEXT_SECONDARY = -5728136;
    private static final int SUCCESS = -11751600;
    private static final int WARNING = -2448096;
    private static final int ERROR = -3394765;
    private static final int PROGRESS_BG = -12967400;
    private static final int WING_AQUARIUM = -10830166;
    private static final int WING_WILDLIFE = -9728434;
    private static final int WING_ACHIEVE = -2448096;
    private static final int WING_ART = -6591602;
    private static final int WING_ITEMS = -3638726;
    private static final int MARGIN = 10;
    private static final int TAB_HEIGHT = 22;
    private static final int PROGRESS_BAR_HEIGHT = 12;
    private static final int ROW_HEIGHT = 14;
    private static final int CATEGORY_HEADER_HEIGHT = 18;
    private static final int ITEM_ROW_HEIGHT = 20;
    private static final int TAB_AQUARIUM = 0;
    private static final int TAB_WILDLIFE = 1;
    private static final int TAB_ACHIEVEMENTS = 2;
    private static final int TAB_ART = 3;
    private static final int TAB_ITEMS = 4;
    private static final int TAB_HISTORY = 5;
    private static final String[] TAB_NAMES = new String[]{"Aquarium", "Wildlife", "Achievements", "Art Gallery", "Items", "History"};
    private static final int[] TAB_WING_COLORS = new int[]{-10830166, -9728434, -2448096, -6591602, -3638726, -4144960};
    private int activeTab = 0;
    private final double[] scrollOffsets = new double[6];
    private int selectedItemCategory = 0;
    private final Set<String> donatedItems = new HashSet<String>();
    private final Set<String> donatedMobs = new HashSet<String>();
    private final Set<String> donatedArt = new HashSet<String>();
    private final Set<String> completedAchievements = new HashSet<String>();
    private final Map<String, Long> donationTimestamps = new LinkedHashMap<String, Long>();
    private final List<Map.Entry<String, Long>> sortedHistory = new ArrayList<>();
    private int titleBarHeight;
    private int tabBarY;
    private int progressBarY;
    private int contentTop;
    private int contentBottom;
    private int contentLeft;
    private int contentRight;
    private static final String[] WILDLIFE_CATEGORIES = new String[]{"Passive", "Neutral", "Hostile", "Boss", "Ambient"};
    private static final String[] ACHIEVEMENT_GROUPS = new String[]{"Story", "Nether", "End", "Adventure", "Husbandry"};
    private static final String[] ACHIEVEMENT_PREFIXES = new String[]{"minecraft:story/", "minecraft:nether/", "minecraft:end/", "minecraft:adventure/", "minecraft:husbandry/"};

    public CuratorScreen(String museumDataJson) {
        super((Component)Component.literal((String)"Museum Curator"));
        this.parseMuseumData(museumDataJson);
    }

    private void drawPanel(GuiGraphics g, int x, int y, int w, int h) {
        UIHelper.drawPanel(g, x, y, w, h);
    }

    private void drawTitleBar(GuiGraphics g, int w, int h) {
        UIHelper.drawPanel(g, 0, 0, w, h);
    }

    private void parseMuseumData(String json) {
        if (json == null || json.isEmpty()) {
            return;
        }
        this.parseArrayInto(json, "items", this.donatedItems);
        this.parseArrayInto(json, "mobs", this.donatedMobs);
        this.parseArrayInto(json, "art", this.donatedArt);
        this.parseArrayInto(json, "achievements", this.completedAchievements);
        this.parseTimestamps(json);
    }

    private void parseTimestamps(String json) {
        String search = "\"timestamps\":{";
        int start = json.indexOf(search);
        if (start == -1) return;
        start += search.length();
        int end = json.indexOf("}", start);
        if (end == -1) return;
        String content = json.substring(start, end);
        if (content.isEmpty()) return;
        for (String entry : content.split(",")) {
            int colon = entry.indexOf(':');
            if (colon == -1) continue;
            String key = entry.substring(0, colon).trim();
            if (key.length() >= 2 && key.startsWith("\"") && key.endsWith("\"")) {
                key = key.substring(1, key.length() - 1);
            }
            try {
                long day = Long.parseLong(entry.substring(colon + 1).trim());
                this.donationTimestamps.put(key, day);
            } catch (NumberFormatException ignored) {}
        }
        // Sort by day descending (most recent first)
        this.sortedHistory.addAll(this.donationTimestamps.entrySet());
        this.sortedHistory.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));
    }

    private void parseArrayInto(String json, String key, Set<String> target) {
        String search = "\"" + key + "\":[";
        int start = json.indexOf(search);
        if (start == -1) {
            return;
        }
        int end = json.indexOf("]", start += search.length());
        if (end == -1) {
            return;
        }
        String arrayContent = json.substring(start, end);
        if (arrayContent.isEmpty()) {
            return;
        }
        for (String entry : arrayContent.split(",")) {
            String trimmed = entry.trim();
            if (trimmed.length() < 2 || !trimmed.startsWith("\"") || !trimmed.endsWith("\"")) continue;
            target.add(trimmed.substring(1, trimmed.length() - 1));
        }
    }

    protected void init() {
        super.init();
        Objects.requireNonNull(this.font);
        this.tabBarY = this.titleBarHeight = 9 + 14;
        this.progressBarY = this.tabBarY + 22 + 4;
        this.contentTop = this.progressBarY + 12 + 8;
        this.contentBottom = this.height - 30;
        this.contentLeft = 10;
        this.contentRight = this.width - 10;
        this.addRenderableWidget(Button.builder((Component)Component.literal((String)"\u2190 Back"), btn -> {
            if (this.minecraft != null) {
                this.minecraft.setScreen(null);
            }
        }).bounds(10, (this.titleBarHeight - 20) / 2, 50, 20).build());
        this.rebuildTabButtons();
        int qbW = 90;
        int qbH = 20;
        this.addRenderableWidget(Button.builder((Component)Component.literal((String)"Quick Donate"), btn -> ClientPacketDistributor.sendToServer((CustomPacketPayload)new MuseumActionPayload("quick_donate", ""), (CustomPacketPayload[])new CustomPacketPayload[0])).bounds(this.width - 10 - qbW, this.height - 10 - qbH, qbW, qbH).build());
    }

    private void rebuildTabButtons() {
        int tabAreaStart = 10;
        int tabAreaWidth = this.width - 20;
        int tabCount = TAB_NAMES.length;
        int tabWidth = tabAreaWidth / tabCount;
        for (int i = 0; i < tabCount; ++i) {
            int tabIndex = i;
            int x = tabAreaStart + i * tabWidth;
            this.addRenderableWidget(Button.builder((Component)Component.literal((String)TAB_NAMES[i]), btn -> {
                this.activeTab = tabIndex;
                this.selectedItemCategory = 0;
                this.rebuildAll();
            }).bounds(x, this.tabBarY, tabWidth, 22).build());
        }
    }

    private void rebuildAll() {
        this.clearWidgets();
        this.addRenderableWidget(Button.builder((Component)Component.literal((String)"\u2190 Back"), btn -> {
            if (this.minecraft != null) {
                this.minecraft.setScreen(null);
            }
        }).bounds(10, (this.titleBarHeight - 20) / 2, 50, 20).build());
        this.rebuildTabButtons();
        int qbW = 90;
        int qbH = 20;
        this.addRenderableWidget(Button.builder((Component)Component.literal((String)"Quick Donate"), btn -> ClientPacketDistributor.sendToServer((CustomPacketPayload)new MuseumActionPayload("quick_donate", ""), (CustomPacketPayload[])new CustomPacketPayload[0])).bounds(this.width - 10 - qbW, this.height - 10 - qbH, qbW, qbH).build());
        if (this.activeTab == 4) {
            this.rebuildItemCategoryButtons();
        }
    }

    private void rebuildItemCategoryButtons() {
        String[] categories = ItemCatalog.CATEGORIES;
        if (categories == null || categories.length == 0) {
            return;
        }
        int btnW = 70;
        int btnH = 16;
        int spacing = 4;
        int totalW = categories.length * (btnW + spacing) - spacing;
        int startX = (this.width - totalW) / 2;
        int btnY = this.contentTop;
        for (int i = 0; i < categories.length; ++i) {
            int catIndex = i;
            this.addRenderableWidget(Button.builder((Component)Component.literal((String)categories[i]), btn -> {
                this.selectedItemCategory = catIndex;
                this.scrollOffsets[4] = 0.0;
            }).bounds(startX + i * (btnW + spacing), btnY, btnW, btnH).build());
        }
    }

    public boolean isPauseScreen() {
        return false;
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, -16120316);
        UIHelper.drawPanel(graphics, 0, 0, this.width, this.height);
        this.drawTitleBar(graphics, this.width, this.titleBarHeight);
        String playerName = this.minecraft != null && this.minecraft.player != null ? this.minecraft.player.getName().getString() : "Player";
        Objects.requireNonNull(this.font);
        int titleY = (this.titleBarHeight - 9) / 2;
        int overallPercent = this.computeOverallCompletion();
        String titleText = "Museum of " + playerName;
        String completionText = "Collection: " + overallPercent + "%";
        graphics.drawCenteredString(this.font, titleText, this.width / 2, titleY, -991040);
        int compWidth = this.font.width(completionText);
        graphics.drawString(this.font, completionText, this.width - compWidth - 10 - 100, titleY, this.getProgressColor(overallPercent));
        UIHelper.drawPanel(graphics, 0, this.tabBarY, this.width, 22);
        int tabAreaStart = 10;
        int tabAreaWidth = this.width - 20;
        int tabWidth = tabAreaWidth / TAB_NAMES.length;
        for (int i = 0; i < TAB_NAMES.length; ++i) {
            int tx = tabAreaStart + i * tabWidth;
            int squareX = tx + 3;
            int squareY = this.tabBarY + 8;
            graphics.fill(squareX, squareY, squareX + 6, squareY + 6, TAB_WING_COLORS[i]);
            if (i == this.activeTab) {
                graphics.fill(tx, this.tabBarY + 22 - 2, tx + tabWidth, this.tabBarY + 22, -2448096);
                continue;
            }
            graphics.fill(tx, this.tabBarY + 22 - 1, tx + tabWidth, this.tabBarY + 22, -3626932);
        }
        this.renderProgressBar(graphics);
        graphics.enableScissor(this.contentLeft, this.contentTop, this.contentRight, this.contentBottom);
        switch (this.activeTab) {
            case 0: {
                this.renderAquariumTab(graphics);
                break;
            }
            case 1: {
                this.renderWildlifeTab(graphics);
                break;
            }
            case 2: {
                this.renderAchievementsTab(graphics);
                break;
            }
            case 3: {
                this.renderArtTab(graphics);
                break;
            }
            case 4: {
                this.renderItemsTab(graphics);
                break;
            }
            case 5: {
                this.renderHistoryTab(graphics);
            }
        }
        graphics.disableScissor();
        this.renderScrollIndicators(graphics);
        UIHelper.drawPanel(graphics, 0, this.height - 28, this.width, 28);
        String bottomInfo = this.wingShortStats();
        graphics.drawCenteredString(this.font, bottomInfo, this.width / 2, this.height - 20, -5728136);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderProgressBar(GuiGraphics graphics) {
        int collected;
        int total;
        String label = switch (this.activeTab) {
            case 0 -> {
                total = AquariumCatalog.ENTRIES.size();
                collected = this.countCollectedAquarium();
                yield "Aquarium";
            }
            case 1 -> {
                total = WildlifeCatalog.ENTRIES.size();
                collected = this.countCollectedWildlife();
                yield "Wildlife";
            }
            case 2 -> {
                total = AchievementCatalog.ENTRIES.size();
                collected = this.countCollectedAchievements();
                yield "Achievements";
            }
            case 3 -> {
                total = ArtCatalog.ENTRIES.size();
                collected = this.countCollectedArt();
                yield "Art Gallery";
            }
            case 4 -> {
                total = this.countTotalItems();
                collected = this.countCollectedItems();
                yield "Items";
            }
            case 5 -> {
                total = this.sortedHistory.size();
                collected = this.sortedHistory.size();
                yield "Donation History";
            }
            default -> {
                total = 1;
                collected = 0;
                yield "";
            }
        };
        int percent = total > 0 ? collected * 100 / total : 0;
        String progressText = label + ": " + collected + "/" + total + " (" + percent + "%)";
        int barX = 10;
        int barW = this.width - 20;
        int barY = this.progressBarY;
        graphics.fill(barX, barY, barX + barW, barY + 12, -12967400);
        int fillW = total > 0 ? barW * collected / total : 0;
        int fillColor = this.getProgressColor(percent);
        graphics.fill(barX, barY, barX + fillW, barY + 12, fillColor);
        graphics.fill(barX, barY, barX + barW, barY + 1, -3626932);
        graphics.fill(barX, barY + 12 - 1, barX + barW, barY + 12, -3626932);
        graphics.fill(barX, barY, barX + 1, barY + 12, -3626932);
        graphics.fill(barX + barW - 1, barY, barX + barW, barY + 12, -3626932);
        Objects.requireNonNull(this.font);
        int textY = barY + (12 - 9) / 2;
        graphics.drawCenteredString(this.font, progressText, this.width / 2, textY, -991040);
    }

    private int getProgressColor(int percent) {
        if (percent >= 100) {
            return -10496;
        }
        if (percent >= 75) {
            return -2448096;
        }
        if (percent >= 50) {
            return -3626932;
        }
        if (percent >= 25) {
            return -9728434;
        }
        return -11751600;
    }

    private void renderAquariumTab(GuiGraphics graphics) {
        List<AquariumCatalog.MobEntry> entries = AquariumCatalog.ENTRIES;
        int y = this.contentTop - (int)this.scrollOffsets[0];
        int collected = this.countCollectedAquarium();
        graphics.drawString(this.font, "Collected: " + collected + " / " + entries.size(), this.contentLeft + 4, y, -5728136);
        graphics.fill(this.contentLeft, y += 18, this.contentRight, y + 1, -3626932);
        y += 4;
        for (int idx = 0; idx < entries.size(); ++idx) {
            AquariumCatalog.MobEntry entry = entries.get(idx);
            boolean donated = this.donatedMobs.contains(entry.entityId());
            int rowBg = idx % 2 == 0 ? -12967400 : -14018032;
            graphics.fill(this.contentLeft, y, this.contentRight, y + 14, rowBg);
            if (donated) {
                graphics.fill(this.contentLeft + 4, y + 3, this.contentLeft + 8, y + 7, -10830166);
                graphics.drawString(this.font, entry.displayName(), this.contentLeft + 14, y + 2, -991040);
                String desc = entry.description();
                int descW = this.font.width(desc);
                graphics.drawString(this.font, desc, this.contentRight - descW - 4, y + 2, -5728136);
            } else {
                graphics.fill(this.contentLeft + 4, y + 3, this.contentLeft + 8, y + 7, -3626932);
                graphics.drawString(this.font, "???", this.contentLeft + 14, y + 2, -5728136);
            }
            y += 14;
        }
    }

    private void renderWildlifeTab(GuiGraphics graphics) {
        int y = this.contentTop - (int)this.scrollOffsets[1];
        int collected = this.countCollectedWildlife();
        graphics.drawString(this.font, "Collected: " + collected + " / " + WildlifeCatalog.ENTRIES.size(), this.contentLeft + 4, y, -5728136);
        y += 18;
        for (String category : WILDLIFE_CATEGORIES) {
            List<WildlifeCatalog.MobEntry> catEntries = WildlifeCatalog.getByCategory(category);
            if (catEntries.isEmpty()) continue;
            this.drawPanel(graphics, this.contentLeft, y, this.contentRight - this.contentLeft, 18);
            int catCollected = 0;
            for (WildlifeCatalog.MobEntry e : catEntries) {
                if (!this.donatedMobs.contains(e.entityId())) continue;
                ++catCollected;
            }
            String headerText = "-- " + category + " (" + catCollected + "/" + catEntries.size() + ") --";
            int n = this.width / 2;
            Objects.requireNonNull(this.font);
            graphics.drawCenteredString(this.font, headerText, n, y + (18 - 9) / 2, this.getCategoryColor(category));
            y += 18;
            int rowIndex = 0;
            for (WildlifeCatalog.MobEntry entry : catEntries) {
                boolean donated = this.donatedMobs.contains(entry.entityId());
                int rowBg = rowIndex % 2 == 0 ? -12967400 : -14018032;
                graphics.fill(this.contentLeft, y, this.contentRight, y + 14, rowBg);
                if (donated) {
                    graphics.fill(this.contentLeft + 4, y + 3, this.contentLeft + 8, y + 7, -9728434);
                    graphics.drawString(this.font, entry.displayName(), this.contentLeft + 14, y + 2, -991040);
                    int catW = this.font.width(entry.category());
                    graphics.drawString(this.font, entry.category(), this.contentRight - catW - 4, y + 2, -5728136);
                } else {
                    graphics.fill(this.contentLeft + 4, y + 3, this.contentLeft + 8, y + 7, -3626932);
                    graphics.drawString(this.font, "???", this.contentLeft + 14, y + 2, -5728136);
                }
                y += 14;
                ++rowIndex;
            }
            y += 4;
        }
    }

    private int getCategoryColor(String category) {
        return switch (category) {
            case "Passive" -> -9728434;
            case "Neutral" -> -2448096;
            case "Hostile" -> -3394765;
            case "Boss" -> -6591602;
            case "Ambient" -> -10830166;
            default -> -5728136;
        };
    }

    private void renderAchievementsTab(GuiGraphics graphics) {
        int y = this.contentTop - (int)this.scrollOffsets[2];
        int collected = this.countCollectedAchievements();
        graphics.drawString(this.font, "Completed: " + collected + " / " + AchievementCatalog.ENTRIES.size(), this.contentLeft + 4, y, -5728136);
        y += 18;
        Map<String, List<AchievementCatalog.AchievementEntry>> grouped = this.groupAchievements();
        for (int g = 0; g < ACHIEVEMENT_GROUPS.length; ++g) {
            String groupName = ACHIEVEMENT_GROUPS[g];
            List<AchievementCatalog.AchievementEntry> groupEntries = grouped.get(groupName);
            if (groupEntries == null || groupEntries.isEmpty()) continue;
            this.drawPanel(graphics, this.contentLeft, y, this.contentRight - this.contentLeft, 18);
            int grpCollected = 0;
            for (AchievementCatalog.AchievementEntry e : groupEntries) {
                if (!this.completedAchievements.contains(e.advancementId())) continue;
                ++grpCollected;
            }
            String headerText = "-- " + groupName + " (" + grpCollected + "/" + groupEntries.size() + ") --";
            int n = this.width / 2;
            Objects.requireNonNull(this.font);
            graphics.drawCenteredString(this.font, headerText, n, y + (18 - 9) / 2, -2448096);
            y += 18;
            int rowIndex = 0;
            for (AchievementCatalog.AchievementEntry entry : groupEntries) {
                boolean done = this.completedAchievements.contains(entry.advancementId());
                int rowBg = rowIndex % 2 == 0 ? -12967400 : -14018032;
                graphics.fill(this.contentLeft, y, this.contentRight, y + 14, rowBg);
                if (done) {
                    graphics.drawString(this.font, "\u2713", this.contentLeft + 4, y + 2, -2448096);
                    graphics.drawString(this.font, entry.displayName(), this.contentLeft + 16, y + 2, -991040);
                    String desc = entry.description();
                    int descW = this.font.width(desc);
                    int descX = this.contentRight - descW - 4;
                    if (descX > this.contentLeft + 16 + this.font.width(entry.displayName()) + 8) {
                        graphics.drawString(this.font, desc, descX, y + 2, -5728136);
                    }
                } else {
                    graphics.drawString(this.font, "\u2717", this.contentLeft + 4, y + 2, -3626932);
                    graphics.drawString(this.font, entry.displayName(), this.contentLeft + 16, y + 2, -5728136);
                }
                y += 14;
                ++rowIndex;
            }
            y += 4;
        }
        List<AchievementCatalog.AchievementEntry> other = grouped.get("Other");
        if (other != null && !other.isEmpty()) {
            this.drawPanel(graphics, this.contentLeft, y, this.contentRight - this.contentLeft, 18);
            int otherCollected = 0;
            for (AchievementCatalog.AchievementEntry e : other) {
                if (!this.completedAchievements.contains(e.advancementId())) continue;
                ++otherCollected;
            }
            String headerText = "-- Other (" + otherCollected + "/" + other.size() + ") --";
            int n = this.width / 2;
            Objects.requireNonNull(this.font);
            graphics.drawCenteredString(this.font, headerText, n, y + (18 - 9) / 2, -5728136);
            y += 18;
            int rowIndex = 0;
            for (AchievementCatalog.AchievementEntry entry : other) {
                boolean done = this.completedAchievements.contains(entry.advancementId());
                int rowBg = rowIndex % 2 == 0 ? -12967400 : -14018032;
                graphics.fill(this.contentLeft, y, this.contentRight, y + 14, rowBg);
                if (done) {
                    graphics.drawString(this.font, "\u2713", this.contentLeft + 4, y + 2, -2448096);
                    graphics.drawString(this.font, entry.displayName(), this.contentLeft + 16, y + 2, -991040);
                } else {
                    graphics.drawString(this.font, "\u2717", this.contentLeft + 4, y + 2, -3626932);
                    graphics.drawString(this.font, entry.displayName(), this.contentLeft + 16, y + 2, -5728136);
                }
                y += 14;
                ++rowIndex;
            }
        }
    }

    private Map<String, List<AchievementCatalog.AchievementEntry>> groupAchievements() {
        LinkedHashMap<String, List<AchievementCatalog.AchievementEntry>> grouped = new LinkedHashMap<String, List<AchievementCatalog.AchievementEntry>>();
        for (String group : ACHIEVEMENT_GROUPS) {
            grouped.put(group, new ArrayList());
        }
        grouped.put("Other", new ArrayList());
        for (AchievementCatalog.AchievementEntry entry : AchievementCatalog.ENTRIES) {
            boolean matched = false;
            for (int i = 0; i < ACHIEVEMENT_PREFIXES.length; ++i) {
                if (!entry.advancementId().startsWith(ACHIEVEMENT_PREFIXES[i])) continue;
                ((List)grouped.get(ACHIEVEMENT_GROUPS[i])).add(entry);
                matched = true;
                break;
            }
            if (matched) continue;
            ((List)grouped.get("Other")).add(entry);
        }
        return grouped;
    }

    private void renderArtTab(GuiGraphics graphics) {
        List<ArtCatalog.ArtEntry> entries = ArtCatalog.ENTRIES;
        int y = this.contentTop - (int)this.scrollOffsets[3];
        int collected = this.countCollectedArt();
        graphics.drawString(this.font, "Collected: " + collected + " / " + entries.size(), this.contentLeft + 4, y, -5728136);
        graphics.fill(this.contentLeft, y += 18, this.contentRight, y + 1, -3626932);
        y += 4;
        int artRowHeight = 32;
        for (int i = 0; i < entries.size(); ++i) {
            ArtCatalog.ArtEntry entry = entries.get(i);
            boolean donated = this.donatedArt.contains(entry.id());
            int cardBg = i % 2 == 0 ? -12967400 : -14018032;
            graphics.fill(this.contentLeft, y, this.contentRight, y + artRowHeight, cardBg);
            graphics.fill(this.contentLeft, y + artRowHeight - 1, this.contentRight, y + artRowHeight, -3626932);
            if (donated) {
                graphics.fill(this.contentLeft + 4, y + 3, this.contentLeft + 8, y + 7, -6591602);
                graphics.drawString(this.font, entry.title(), this.contentLeft + 14, y + 2, -991040);
                String meta = "by " + entry.artist() + " (" + entry.year() + ")";
                int metaW = this.font.width(meta);
                graphics.drawString(this.font, meta, this.contentRight - metaW - 4, y + 2, -6591602);
                graphics.drawString(this.font, entry.description(), this.contentLeft + 14, y + 14 + 2, -5728136);
            } else {
                graphics.fill(this.contentLeft + 4, y + 3, this.contentLeft + 8, y + 7, -3626932);
                graphics.drawString(this.font, "???", this.contentLeft + 14, y + 2, -5728136);
                graphics.drawString(this.font, "Undiscovered artwork", this.contentLeft + 14, y + 14 + 2, -3626932);
            }
            y += artRowHeight;
        }
    }

    private void renderItemsTab(GuiGraphics graphics) {
        String[] categories = ItemCatalog.CATEGORIES;
        if (categories == null || categories.length == 0) {
            graphics.drawCenteredString(this.font, "No item categories defined.", this.width / 2, this.contentTop + 30, -5728136);
            return;
        }
        int itemContentTop = this.contentTop + 22;
        int y = itemContentTop - (int)this.scrollOffsets[4];
        String category = categories[Math.min(this.selectedItemCategory, categories.length - 1)];
        List<String> itemIds = ItemCatalog.ITEMS_BY_CATEGORY.get(category);
        if (itemIds == null || itemIds.isEmpty()) {
            graphics.drawCenteredString(this.font, "No items in category: " + category, this.width / 2, y + 10, -5728136);
            return;
        }
        int catCollected = 0;
        for (String id : itemIds) {
            if (!this.donatedItems.contains(id)) continue;
            ++catCollected;
        }
        String catProgress = category + ": " + catCollected + "/" + itemIds.size();
        graphics.drawString(this.font, catProgress, this.contentLeft + 4, y, -5728136);
        int miniBarW = this.contentRight - this.contentLeft;
        int miniBarH = 4;
        graphics.fill(this.contentLeft, y += 18, this.contentRight, y + miniBarH, -12967400);
        int miniFillW = itemIds.size() > 0 ? miniBarW * catCollected / itemIds.size() : 0;
        int miniPercent = itemIds.size() > 0 ? catCollected * 100 / itemIds.size() : 0;
        graphics.fill(this.contentLeft, y, this.contentLeft + miniFillW, y + miniBarH, this.getProgressColor(miniPercent));
        int colWidth = 140;
        int cols = Math.max(1, (this.contentRight - this.contentLeft) / colWidth);
        int actualColWidth = (this.contentRight - this.contentLeft) / cols;
        int col = 0;
        int rowStartY = y += miniBarH + 4;
        for (String itemId : itemIds) {
            boolean donated = this.donatedItems.contains(itemId);
            int ix = this.contentLeft + col * actualColWidth;
            int iy = rowStartY;
            if (donated) {
                ItemStack stack = this.resolveItemStack(itemId);
                if (!stack.isEmpty()) {
                    graphics.fill(ix + 1, iy - 1, ix + 19, iy + 17, -15069688);
                    graphics.renderItem(stack, ix + 2, iy);
                } else {
                    graphics.fill(ix + 2, iy, ix + 18, iy + 16, -3638726);
                }
                String displayName = this.getItemDisplayName(itemId, stack);
                graphics.drawString(this.font, displayName, ix + 22, iy + 4, -991040);
            } else {
                graphics.fill(ix + 2, iy, ix + 18, iy + 16, -12967400);
                graphics.fill(ix + 6, iy + 4, ix + 14, iy + 12, -3626932);
                graphics.drawString(this.font, "???", ix + 22, iy + 4, -5728136);
            }
            if (++col < cols) continue;
            col = 0;
            rowStartY += 20;
        }
    }

    private void renderHistoryTab(GuiGraphics graphics) {
        int y = this.contentTop - (int) this.scrollOffsets[5];
        if (this.sortedHistory.isEmpty()) {
            graphics.drawCenteredString(this.font, "No donations yet! Bring items to the Museum Block.", this.width / 2, y + 20, -5728136);
            return;
        }
        graphics.drawString(this.font, "Total donations: " + this.sortedHistory.size(), this.contentLeft + 4, y, -5728136);
        graphics.fill(this.contentLeft, y + 14, this.contentRight, y + 15, -3626932);
        y += 18;
        long lastDay = -1;
        for (int i = 0; i < this.sortedHistory.size(); i++) {
            Map.Entry<String, Long> entry = this.sortedHistory.get(i);
            String id = entry.getKey();
            long day = entry.getValue();
            // Day separator header
            if (day != lastDay) {
                lastDay = day;
                this.drawPanel(graphics, this.contentLeft, y, this.contentRight - this.contentLeft, 14);
                graphics.drawString(this.font, "-- Day " + day + " --", this.contentLeft + 8, y + 3, -2448096);
                y += 18;
            }
            int rowBg = i % 2 == 0 ? -12967400 : -14018032;
            graphics.fill(this.contentLeft, y, this.contentRight, y + 18, rowBg);
            // Determine donation type and color
            String typeLabel;
            int typeColor;
            if (this.donatedMobs.contains(id)) {
                typeLabel = "[Mob]";
                typeColor = -9728434; // green
            } else if (this.donatedArt.contains(id)) {
                typeLabel = "[Art]";
                typeColor = -6591602; // purple
            } else {
                typeLabel = "[Item]";
                typeColor = -3638726; // brown/orange
            }
            graphics.drawString(this.font, typeLabel, this.contentLeft + 4, y + 4, typeColor);
            // Display name
            String displayName = this.getHistoryDisplayName(id);
            graphics.drawString(this.font, displayName, this.contentLeft + 40, y + 4, -991040);
            y += 18;
        }
    }

    private String getHistoryDisplayName(String id) {
        // For items, try to resolve to display name
        if (id.contains(":")) {
            ItemStack stack = this.resolveItemStack(id);
            if (!stack.isEmpty()) {
                return stack.getHoverName().getString();
            }
        }
        // Fallback: format the ID nicely
        String path = id.contains(":") ? id.substring(id.indexOf(':') + 1) : id;
        StringBuilder sb = new StringBuilder();
        for (String word : path.split("_")) {
            if (word.isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(word.charAt(0)));
            if (word.length() > 1) sb.append(word.substring(1));
        }
        return sb.toString();
    }

    private ItemStack resolveItemStack(String itemId) {
        try {
            Identifier id = Identifier.parse((String)itemId);
            Optional itemOptional = BuiltInRegistries.ITEM.getOptional(id);
            if (itemOptional.isPresent()) {
                return new ItemStack((ItemLike)itemOptional.get());
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return ItemStack.EMPTY;
    }

    private String getItemDisplayName(String itemId, ItemStack stack) {
        if (!stack.isEmpty()) {
            return stack.getHoverName().getString();
        }
        String path = itemId.contains(":") ? itemId.substring(itemId.indexOf(58) + 1) : itemId;
        StringBuilder sb = new StringBuilder();
        for (String word : path.split("_")) {
            if (word.isEmpty()) continue;
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(Character.toUpperCase(word.charAt(0)));
            if (word.length() <= 1) continue;
            sb.append(word.substring(1));
        }
        return sb.toString();
    }

    private void renderScrollIndicators(GuiGraphics graphics) {
        int visibleHeight;
        int totalHeight = this.getContentHeight();
        if (totalHeight <= (visibleHeight = this.contentBottom - this.contentTop)) {
            return;
        }
        int trackX = this.contentRight - 3;
        int trackH = visibleHeight;
        graphics.fill(trackX, this.contentTop, trackX + 3, this.contentBottom, -12967400);
        double scrollFraction = this.scrollOffsets[this.activeTab] / (double)Math.max(1, totalHeight - visibleHeight);
        int thumbH = Math.max(10, visibleHeight * visibleHeight / totalHeight);
        int thumbY = this.contentTop + (int)((double)(trackH - thumbH) * scrollFraction);
        graphics.fill(trackX, thumbY, trackX + 3, thumbY + thumbH, -2448096);
        if (this.scrollOffsets[this.activeTab] > 0.0) {
            graphics.drawCenteredString(this.font, "\u25b2", this.contentRight - 8, this.contentTop + 2, -2448096);
        }
        if (this.scrollOffsets[this.activeTab] < (double)(totalHeight - visibleHeight)) {
            Objects.requireNonNull(this.font);
            graphics.drawCenteredString(this.font, "\u25bc", this.contentRight - 8, this.contentBottom - 9 - 2, -2448096);
        }
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int totalHeight = this.getContentHeight();
        int visibleHeight = this.contentBottom - this.contentTop;
        int maxScroll = Math.max(0, totalHeight - visibleHeight);
        int n = this.activeTab;
        this.scrollOffsets[n] = this.scrollOffsets[n] - scrollY * 12.0;
        if (this.scrollOffsets[this.activeTab] < 0.0) {
            this.scrollOffsets[this.activeTab] = 0.0;
        }
        if (this.scrollOffsets[this.activeTab] > (double)maxScroll) {
            this.scrollOffsets[this.activeTab] = maxScroll;
        }
        return true;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        int visibleHeight = this.contentBottom - this.contentTop;
        int totalHeight = this.getContentHeight();
        int maxScroll = Math.max(0, totalHeight - visibleHeight);
        if (keyCode == 266) {
            this.scrollOffsets[this.activeTab] = Math.max(0.0, this.scrollOffsets[this.activeTab] - (double)visibleHeight);
            return true;
        }
        if (keyCode == 267) {
            this.scrollOffsets[this.activeTab] = Math.min((double)maxScroll, this.scrollOffsets[this.activeTab] + (double)visibleHeight);
            return true;
        }
        if (keyCode == 268) {
            this.scrollOffsets[this.activeTab] = 0.0;
            return true;
        }
        if (keyCode == 269) {
            this.scrollOffsets[this.activeTab] = maxScroll;
            return true;
        }
        return false;
    }

    private int getContentHeight() {
        switch (this.activeTab) {
            case 0: {
                return 22 + AquariumCatalog.ENTRIES.size() * 14;
            }
            case 1: {
                int h = 18;
                for (String cat : WILDLIFE_CATEGORIES) {
                    List<WildlifeCatalog.MobEntry> entries = WildlifeCatalog.getByCategory(cat);
                    if (entries.isEmpty()) continue;
                    h += 18 + entries.size() * 14 + 4;
                }
                return h;
            }
            case 2: {
                int h = 18;
                Map<String, List<AchievementCatalog.AchievementEntry>> grouped = this.groupAchievements();
                for (String group : ACHIEVEMENT_GROUPS) {
                    List<AchievementCatalog.AchievementEntry> entries = grouped.get(group);
                    if (entries == null || entries.isEmpty()) continue;
                    h += 18 + entries.size() * 14 + 4;
                }
                List<AchievementCatalog.AchievementEntry> other = grouped.get("Other");
                if (other != null && !other.isEmpty()) {
                    h += 18 + other.size() * 14;
                }
                return h;
            }
            case 3: {
                int artRowH = 32;
                return 22 + ArtCatalog.ENTRIES.size() * artRowH;
            }
            case 4: {
                String[] categories = ItemCatalog.CATEGORIES;
                if (categories == null || categories.length == 0) {
                    return 60;
                }
                String category = categories[Math.min(this.selectedItemCategory, categories.length - 1)];
                List<String> itemIds = ItemCatalog.ITEMS_BY_CATEGORY.get(category);
                if (itemIds == null) {
                    return 60;
                }
                int colWidth = 140;
                int cols = Math.max(1, (this.contentRight - this.contentLeft) / colWidth);
                int rows = (itemIds.size() + cols - 1) / cols;
                return 26 + rows * 20 + 22;
            }
            case 5: {
                return 22 + this.sortedHistory.size() * 18 + 10;
            }
        }
        return 0;
    }

    private int countCollectedAquarium() {
        int count = 0;
        for (AquariumCatalog.MobEntry entry : AquariumCatalog.ENTRIES) {
            if (!this.donatedMobs.contains(entry.entityId())) continue;
            ++count;
        }
        return count;
    }

    private int countCollectedWildlife() {
        int count = 0;
        for (WildlifeCatalog.MobEntry entry : WildlifeCatalog.ENTRIES) {
            if (!this.donatedMobs.contains(entry.entityId())) continue;
            ++count;
        }
        return count;
    }

    private int countCollectedAchievements() {
        int count = 0;
        for (AchievementCatalog.AchievementEntry entry : AchievementCatalog.ENTRIES) {
            if (!this.completedAchievements.contains(entry.advancementId())) continue;
            ++count;
        }
        return count;
    }

    private int countCollectedArt() {
        int count = 0;
        for (ArtCatalog.ArtEntry entry : ArtCatalog.ENTRIES) {
            if (!this.donatedArt.contains(entry.id())) continue;
            ++count;
        }
        return count;
    }

    private int countCollectedItems() {
        int count = 0;
        for (List<String> itemIds : ItemCatalog.ITEMS_BY_CATEGORY.values()) {
            for (String id : itemIds) {
                if (!this.donatedItems.contains(id)) continue;
                ++count;
            }
        }
        return count;
    }

    private int countTotalItems() {
        int total = 0;
        for (List<String> itemIds : ItemCatalog.ITEMS_BY_CATEGORY.values()) {
            total += itemIds.size();
        }
        return total;
    }

    private int computeOverallCompletion() {
        int collected = this.countCollectedAquarium() + this.countCollectedWildlife() + this.countCollectedAchievements() + this.countCollectedArt() + this.countCollectedItems();
        int total = AquariumCatalog.ENTRIES.size() + WildlifeCatalog.ENTRIES.size() + AchievementCatalog.ENTRIES.size() + ArtCatalog.ENTRIES.size() + this.countTotalItems();
        return total > 0 ? collected * 100 / total : 0;
    }

    private String wingShortStats() {
        return "Aq:" + this.countCollectedAquarium() + "/" + AquariumCatalog.ENTRIES.size() + "  Wi:" + this.countCollectedWildlife() + "/" + WildlifeCatalog.ENTRIES.size() + "  Ac:" + this.countCollectedAchievements() + "/" + AchievementCatalog.ENTRIES.size() + "  Ar:" + this.countCollectedArt() + "/" + ArtCatalog.ENTRIES.size() + "  It:" + this.countCollectedItems() + "/" + this.countTotalItems();
    }
}


/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.input.MouseButtonEvent
 *  net.minecraft.network.chat.Component
 */
package com.ultra.megamod.feature.computer.screen;

import com.ultra.megamod.feature.combat.PlayerClassManager.PlayerClass;
import com.ultra.megamod.feature.combat.client.ClientClassCache;
import com.ultra.megamod.feature.combat.spell.SpellDefinition;
import com.ultra.megamod.feature.combat.spell.client.SpellUnlockClientHelper;
import com.ultra.megamod.feature.computer.screen.ComputerScreen;
import com.ultra.megamod.feature.skills.SkillNode;
import com.ultra.megamod.feature.skills.SkillTreeDefinitions;
import com.ultra.megamod.feature.skills.SkillTreeType;
import com.ultra.megamod.feature.skills.client.SkillTreeScreen;
import com.ultra.megamod.feature.skills.network.SkillSyncPayload;
import com.ultra.megamod.feature.ui.UIHelper;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class SkillsAppScreen
extends Screen {
    private final ComputerScreen parentScreen;
    private static final int ROW_HEIGHT = 34;
    private static final int ROW_GAP = 6;
    private static final int MARGIN = 10;
    private int titleBarH;
    private int backX;
    private int backY;
    private int backW;
    private int backH;
    private int openBtnX;
    private int openBtnY;
    private int openBtnW;
    private int openBtnH;

    // Tab system
    private static final int TAB_HEIGHT = 20;
    private static final String[] TAB_NAMES = {"Overview", "Spells", "Leaderboard"};
    private int selectedTab = 0;
    private int tabY;

    public SkillsAppScreen(ComputerScreen parent) {
        super((Component)Component.literal((String)"Skills Overview"));
        this.parentScreen = parent;
    }

    protected void init() {
        super.init();
        Objects.requireNonNull(this.font);
        this.titleBarH = 9 + 16;
        this.backW = 50;
        this.backH = 16;
        this.backX = 8;
        this.backY = (this.titleBarH - this.backH) / 2;
        this.openBtnW = 170;
        this.openBtnH = 22;
        this.openBtnX = (this.width - this.openBtnW) / 2;
        this.openBtnY = this.height - 34;
        this.tabY = this.titleBarH + 4;
    }

    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, this.width, this.height, 0xFF0E0E18);
        UIHelper.drawPanel(g, 0, 0, this.width, this.height);
        UIHelper.drawTitleBar(g, 0, 0, this.width, this.titleBarH);
        Objects.requireNonNull(this.font);
        int titleY = (this.titleBarH - 9) / 2;
        UIHelper.drawCenteredTitle(g, this.font, "Skills", this.width / 2, titleY);
        boolean backHover = mouseX >= this.backX && mouseX < this.backX + this.backW && mouseY >= this.backY && mouseY < this.backY + this.backH;
        UIHelper.drawButton(g, this.backX, this.backY, this.backW, this.backH, backHover);
        int backTextX = this.backX + (this.backW - this.font.width("< Back")) / 2;
        Objects.requireNonNull(this.font);
        g.drawString(this.font, "< Back", backTextX, this.backY + (this.backH - 9) / 2, 0xFFCCCCDD, false);

        // Draw tabs
        drawTabs(g, mouseX, mouseY);

        int contentTop = this.tabY + TAB_HEIGHT + 6;

        if (this.selectedTab == 0) {
            renderOverviewTab(g, mouseX, mouseY, contentTop);
        } else if (this.selectedTab == 1) {
            renderSpellProgressionTab(g, mouseX, mouseY, contentTop);
        } else {
            renderLeaderboardTab(g, mouseX, mouseY, contentTop);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void drawTabs(GuiGraphics g, int mouseX, int mouseY) {
        int tabW = 90;
        int totalTabW = TAB_NAMES.length * (tabW + 4) - 4;
        int tabStartX = (this.width - totalTabW) / 2;
        for (int i = 0; i < TAB_NAMES.length; i++) {
            int tx = tabStartX + i * (tabW + 4);
            boolean hover = mouseX >= tx && mouseX < tx + tabW && mouseY >= this.tabY && mouseY < this.tabY + TAB_HEIGHT;
            boolean selected = i == this.selectedTab;
            int bg = selected ? 0xFF2A2A3A : (hover ? 0xFF1E1E2E : 0xFF151520);
            int border = selected ? 0xFF5555AA : 0xFF333344;
            g.fill(tx, this.tabY, tx + tabW, this.tabY + TAB_HEIGHT, border);
            g.fill(tx + 1, this.tabY + 1, tx + tabW - 1, this.tabY + TAB_HEIGHT - 1, bg);
            int textColor = selected ? 0xFFDDDDFF : 0xFF888899;
            int textW = this.font.width(TAB_NAMES[i]);
            Objects.requireNonNull(this.font);
            g.drawString(this.font, TAB_NAMES[i], tx + (tabW - textW) / 2, this.tabY + (TAB_HEIGHT - 9) / 2, textColor, false);
        }
    }

    private void renderOverviewTab(GuiGraphics g, int mouseX, int mouseY, int contentTop) {
        int points = SkillSyncPayload.clientPoints;
        String pointsStr = "Available Points: " + points;
        int pointsW = this.font.width(pointsStr);
        g.drawString(this.font, pointsStr, this.width - pointsW - 10, contentTop, 0xFF888899, false);

        Map<SkillTreeType, Integer> levels = SkillSyncPayload.clientLevels;
        Map<SkillTreeType, Integer> xpMap = SkillSyncPayload.clientXp;
        Set<String> unlockedNodes = SkillSyncPayload.clientUnlockedNodes;
        int rowW = this.width - 20;
        SkillTreeType[] types = SkillTreeType.values();
        int rowAreaTop = contentTop + 16;
        int totalH = types.length * 40 - 6;
        int startY = rowAreaTop + Math.max(0, (this.openBtnY - 10 - rowAreaTop - totalH) / 2);
        for (int i = 0; i < types.length; ++i) {
            SkillTreeType type = types[i];
            int rowY = startY + i * 40;
            UIHelper.drawCard(g, 10, rowY, rowW, 34, false);
            Objects.requireNonNull(this.font);
            int textY = rowY + (34 - 9) / 2;
            int colX = 20;
            UIHelper.drawShadowedText(g, this.font, type.getDisplayName(), colX, textY, 0xFFDDDDEE);
            int level = levels.getOrDefault((Object)type, 0);
            g.drawString(this.font, "Lv." + level, colX += 74, textY, 0xFFCCCCDD, false);
            colX += 44;
            int xp = xpMap.getOrDefault((Object)type, 0);
            int xpNeeded = SkillTreeType.xpForLevel(level);
            float xpFraction = xpNeeded > 0 ? Math.min(1.0f, (float)xp / (float)xpNeeded) : 0.0f;
            int barW = 100;
            int barH = 10;
            int barY = rowY + (34 - barH) / 2;
            UIHelper.drawProgressBar(g, colX, barY, barW, barH, xpFraction, 0xFF44BF44);
            String xpStr = xp + "/" + xpNeeded;
            g.drawString(this.font, xpStr, colX += barW + 8, textY, 0xFF666677, false);
            colX += this.font.width(xpStr) + 12;
            int totalNodes = SkillTreeDefinitions.getNodesForTree(type).size();
            int unlockedCount = 0;
            for (SkillNode node : SkillTreeDefinitions.getNodesForTree(type)) {
                if (!unlockedNodes.contains(node.id())) continue;
                ++unlockedCount;
            }
            String nodeStr = unlockedCount + "/" + totalNodes + " nodes";
            g.drawString(this.font, nodeStr, colX, textY, -11751600, false);
        }
        String hint = "Press K to open the full skill tree anytime";
        int n = this.width / 2;
        Objects.requireNonNull(this.font);
        UIHelper.drawCenteredLabel(g, this.font, hint, n, this.openBtnY - 9 - 6);
        boolean openHover = mouseX >= this.openBtnX && mouseX < this.openBtnX + this.openBtnW && mouseY >= this.openBtnY && mouseY < this.openBtnY + this.openBtnH;
        UIHelper.drawButton(g, this.openBtnX, this.openBtnY, this.openBtnW, this.openBtnH, openHover);
        String openLabel = "Open Full Skill Tree [K]";
        int openTextX = this.openBtnX + (this.openBtnW - this.font.width(openLabel)) / 2;
        Objects.requireNonNull(this.font);
        g.drawString(this.font, openLabel, openTextX, this.openBtnY + (this.openBtnH - 9) / 2, 0xFFCCCCDD, false);
    }

    private void renderSpellProgressionTab(GuiGraphics g, int mouseX, int mouseY, int contentTop) {
        PlayerClass cls = ClientClassCache.getPlayerClass();

        if (cls == PlayerClass.NONE) {
            Objects.requireNonNull(this.font);
            UIHelper.drawCenteredLabel(g, this.font, "No class selected yet.", this.width / 2, this.height / 2 - 10);
            UIHelper.drawCenteredLabel(g, this.font, "Choose a class to see spell progression.", this.width / 2, this.height / 2 + 6);
            return;
        }

        // Class info header
        SkillTreeType tree = SpellUnlockClientHelper.getTreeForClass(cls);
        int playerLevel = tree != null ? SkillSyncPayload.clientLevels.getOrDefault(tree, 0) : 0;
        String treeName = tree != null ? tree.getDisplayName() : "?";

        int headerY = contentTop;
        int classColor = cls.getColor() | 0xFF000000;

        // Class name + tree level
        String classInfo = cls.getDisplayName() + " - " + treeName + " Lv. " + playerLevel;
        Objects.requireNonNull(this.font);
        UIHelper.drawCenteredTitle(g, this.font, classInfo, this.width / 2, headerY);

        int cardW = Math.min(360, this.width - 40);
        int startX = (this.width - cardW) / 2;
        int currentY = headerY + 18;

        // Get spells grouped by level
        TreeMap<Integer, List<SpellDefinition>> spellsByLevel = SpellUnlockClientHelper.getSpellsByLevel(cls);

        if (spellsByLevel.isEmpty()) {
            Objects.requireNonNull(this.font);
            UIHelper.drawCenteredLabel(g, this.font, "No spells found for this class.", this.width / 2, this.height / 2);
            return;
        }

        for (Map.Entry<Integer, List<SpellDefinition>> entry : spellsByLevel.entrySet()) {
            int reqLevel = entry.getKey();
            List<SpellDefinition> spells = entry.getValue();
            boolean tierUnlocked = playerLevel >= reqLevel;

            // Tier header card
            int tierCardH = 18;
            if (currentY + tierCardH > this.height - 10) break;

            // Tier header background
            int tierBg = tierUnlocked ? 0xFF1A2A1A : 0xFF2A1A1A;
            int tierBorder = tierUnlocked ? 0xFF338833 : 0xFF883333;
            g.fill(startX, currentY, startX + cardW, currentY + tierCardH, tierBorder);
            g.fill(startX + 1, currentY + 1, startX + cardW - 1, currentY + tierCardH - 1, tierBg);

            // Tier label
            String tierLabel = tierUnlocked ? "Lv. " + reqLevel + " - UNLOCKED" : "Lv. " + reqLevel + " - LOCKED";
            int tierLabelColor = tierUnlocked ? 0xFF44DD44 : 0xFFDD4444;
            Objects.requireNonNull(this.font);
            g.drawString(this.font, tierLabel, startX + 8, currentY + (tierCardH - 9) / 2, tierLabelColor, false);

            // Unlock progress indicator on the right
            if (!tierUnlocked) {
                int levelsNeeded = reqLevel - playerLevel;
                String progressStr = levelsNeeded + " level" + (levelsNeeded != 1 ? "s" : "") + " to go";
                int progW = this.font.width(progressStr);
                g.drawString(this.font, progressStr, startX + cardW - progW - 8, currentY + (tierCardH - 9) / 2, 0xFF888899, false);
            }

            currentY += tierCardH + 2;

            // Spell entries
            for (SpellDefinition spell : spells) {
                int spellCardH = 22;
                if (currentY + spellCardH > this.height - 10) break;

                boolean hover = mouseX >= startX && mouseX < startX + cardW
                        && mouseY >= currentY && mouseY < currentY + spellCardH;
                UIHelper.drawCard(g, startX, currentY, cardW, spellCardH, hover);

                Objects.requireNonNull(this.font);
                int textY = currentY + (spellCardH - 9) / 2;

                // Status icon
                String statusIcon = tierUnlocked ? "\u2713" : "\u2717";
                int statusColor = tierUnlocked ? 0xFF44DD44 : 0xFFDD4444;
                g.drawString(this.font, statusIcon, startX + 8, textY, statusColor, false);

                // Spell name (colored by school if unlocked, gray if locked)
                int nameColor = tierUnlocked ? (spell.school().color | 0xFF000000) : 0xFF666677;
                g.drawString(this.font, spell.name(), startX + 22, textY, nameColor, false);

                // School tag
                String schoolTag = "[" + spell.school().displayName + "]";
                int schoolTagColor = tierUnlocked ? 0xFF888899 : 0xFF555566;
                int schoolTagX = startX + 22 + this.font.width(spell.name()) + 6;
                g.drawString(this.font, schoolTag, schoolTagX, textY, schoolTagColor, false);

                // Cooldown on the right
                if (spell.cooldownSeconds() > 0) {
                    String cdStr = String.format("%.0fs CD", spell.cooldownSeconds());
                    int cdW = this.font.width(cdStr);
                    g.drawString(this.font, cdStr, startX + cardW - cdW - 8, textY, 0xFF667788, false);
                }

                currentY += spellCardH + 2;
            }

            currentY += 4; // gap between tiers
        }
    }

    private void renderLeaderboardTab(GuiGraphics g, int mouseX, int mouseY, int contentTop) {
        List<SkillSyncPayload.LeaderboardEntry> leaderboard = SkillSyncPayload.clientLeaderboard;

        // Header
        Objects.requireNonNull(this.font);
        UIHelper.drawCenteredTitle(g, this.font, "Top Players (Combined Level)", this.width / 2, contentTop);

        int cardW = Math.min(320, this.width - 40);
        int cardH = 28;
        int cardGap = 4;
        int startX = (this.width - cardW) / 2;
        int startY = contentTop + 18;

        if (leaderboard.isEmpty()) {
            Objects.requireNonNull(this.font);
            UIHelper.drawCenteredLabel(g, this.font, "No ranking data yet.", this.width / 2, this.height / 2);
            return;
        }

        for (int i = 0; i < leaderboard.size(); i++) {
            SkillSyncPayload.LeaderboardEntry entry = leaderboard.get(i);
            int cy = startY + i * (cardH + cardGap);
            if (cy + cardH > this.openBtnY - 10) break;

            boolean hover = mouseX >= startX && mouseX < startX + cardW && mouseY >= cy && mouseY < cy + cardH;
            UIHelper.drawCard(g, startX, cy, cardW, cardH, hover);

            Objects.requireNonNull(this.font);
            int textY = cy + (cardH - 9) / 2;

            // Rank number with medal colors for top 3
            int rankColor;
            String rankStr;
            if (i == 0) {
                rankColor = 0xFFFFD700; // Gold
                rankStr = "1st";
            } else if (i == 1) {
                rankColor = 0xFFC0C0C0; // Silver
                rankStr = "2nd";
            } else if (i == 2) {
                rankColor = 0xFFCD7F32; // Bronze
                rankStr = "3rd";
            } else {
                rankColor = 0xFF888899;
                rankStr = (i + 1) + "th";
            }
            g.drawString(this.font, rankStr, startX + 10, textY, rankColor, false);

            // Player name
            g.drawString(this.font, entry.name(), startX + 44, textY, 0xFFDDDDEE, false);

            // Level
            String lvlStr = "Lv. " + entry.level();
            int lvlW = this.font.width(lvlStr);
            g.drawString(this.font, lvlStr, startX + cardW - lvlW - 10, textY, 0xFF44BF44, false);

            // XP bar showing fraction of max (250 combined levels)
            float progress = Math.min(1.0f, (float)entry.level() / 250.0f);
            int barX = startX + 44 + this.font.width(entry.name()) + 10;
            int barW = startX + cardW - lvlW - 20 - barX;
            if (barW > 20) {
                int barY = cy + (cardH - 6) / 2;
                UIHelper.drawProgressBar(g, barX, barY, barW, 6, progress, rankColor);
            }
        }
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean consumed) {
        if (consumed) {
            return super.mouseClicked(event, consumed);
        }
        int mx = (int)event.x();
        int my = (int)event.y();
        if (mx >= this.backX && mx < this.backX + this.backW && my >= this.backY && my < this.backY + this.backH) {
            Minecraft.getInstance().setScreen((Screen)this.parentScreen);
            return true;
        }
        // Tab clicks
        int tabW = 90;
        int totalTabW = TAB_NAMES.length * (tabW + 4) - 4;
        int tabStartX = (this.width - totalTabW) / 2;
        for (int i = 0; i < TAB_NAMES.length; i++) {
            int tx = tabStartX + i * (tabW + 4);
            if (mx >= tx && mx < tx + tabW && my >= this.tabY && my < this.tabY + TAB_HEIGHT) {
                this.selectedTab = i;
                return true;
            }
        }
        // Only handle open button on overview tab
        if (this.selectedTab == 0) {
            if (mx >= this.openBtnX && mx < this.openBtnX + this.openBtnW && my >= this.openBtnY && my < this.openBtnY + this.openBtnH) {
                Minecraft.getInstance().setScreen((Screen)new SkillTreeScreen());
                return true;
            }
        }
        return super.mouseClicked(event, consumed);
    }

    public boolean isPauseScreen() {
        return false;
    }
}

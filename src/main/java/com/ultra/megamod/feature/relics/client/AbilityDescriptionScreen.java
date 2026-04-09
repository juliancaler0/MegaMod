/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.input.MouseButtonEvent
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.world.item.ItemStack
 *  net.neoforged.neoforge.client.network.ClientPacketDistributor
 */
package com.ultra.megamod.feature.relics.client;

import com.ultra.megamod.feature.relics.RelicItem;
import com.ultra.megamod.feature.relics.data.RelicAbility;
import com.ultra.megamod.feature.relics.data.RelicData;
import com.ultra.megamod.feature.relics.data.RelicStat;
import com.ultra.megamod.feature.relics.network.RelicTweakPayload;
import com.ultra.megamod.feature.ui.UIHelper;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class AbilityDescriptionScreen
extends Screen {
    private static final int GOLD = -2448096;
    private static final int GOLD_BRIGHT = -10496;
    private static final int CREAM = -661816;
    private static final int DIM = -10066330;
    private static final int GREEN = -11141291;
    private static final int RED = -43691;
    private static final int AQUA = -11141121;
    private static final int YELLOW = -171;
    private static final int GREEN_FILL = -12272828;
    private static final int BLUE_FILL = -12285731;
    private static final int LOCKED_GREY = -11184811;
    private static final int STAR_FILLED = -10496;
    private static final int STAR_EMPTY = -12303292;
    private static final int PANEL_W = 280;
    private static final int PANEL_H = 200;
    private static final int TITLE_H = 20;
    private static final int BTN_SIZE = 18;
    private static final int BTN_GAP = 4;
    private final ItemStack relicStack;
    private final RelicItem relicItem;
    private final RelicAbility ability;
    private final Screen parentScreen;
    private int hoveredButton = 0;

    public AbilityDescriptionScreen(ItemStack relicStack, RelicItem relicItem, RelicAbility ability, Screen parentScreen) {
        super((Component)Component.literal((String)ability.name()));
        this.relicStack = relicStack;
        this.relicItem = relicItem;
        this.ability = ability;
        this.parentScreen = parentScreen;
    }

    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        boolean rsHover;
        boolean reHover;
        boolean upHover;
        super.render(g, mouseX, mouseY, partialTick);
        ItemStack liveStack = this.findLiveStack();
        if (liveStack == null || liveStack.isEmpty()) {
            liveStack = this.relicStack;
        }
        int px = (this.width - 280) / 2;
        int py = (this.height - 200) / 2;
        UIHelper.drawScreenBg(g, px, py, 280, 200);
        int titleBarX = px + 4;
        int titleBarY = py + 4;
        int titleBarW = 272;
        UIHelper.drawTitleBar(g, titleBarX, titleBarY, titleBarW, 20);
        int backX = titleBarX + 4;
        int backY = titleBarY + 2;
        int backW = 36;
        int backH = 16;
        boolean backHovered = mouseX >= backX && mouseX < backX + backW && mouseY >= backY && mouseY < backY + backH;
        this.hoveredButton = backHovered ? 4 : 0;
        UIHelper.drawButton(g, backX, backY, backW, backH, backHovered);
        int backTextW = this.font.width("\u25c4 Back");
        g.drawString(this.font, "\u25c4 Back", backX + (backW - backTextW) / 2, backY + 4, -661816, false);
        UIHelper.drawCenteredTitle(g, this.font, this.ability.name(), px + 140, titleBarY + 6);
        int btnStartX = px + 280 - 12 - 62;
        int btnY = titleBarY + 1;
        int abilityPoints = RelicData.getAbilityPoints(liveStack, this.ability.name());
        int statCount = Math.max(1, this.ability.stats().size());
        int upgradeCost = (abilityPoints + 1) * statCount * 15;
        int rerollCost = 100 / statCount;
        int resetCost = abilityPoints * 50;
        int unspentPoints = RelicData.getUnspentPoints(liveStack, this.relicItem.getAbilities());
        Minecraft mc = Minecraft.getInstance();
        int playerXp = mc.player != null ? mc.player.totalExperience : 0;
        boolean canUpgrade = unspentPoints > 0 && playerXp >= upgradeCost;
        boolean canReroll = playerXp >= rerollCost;
        boolean canReset = abilityPoints > 0 && playerXp >= resetCost;
        int upX = btnStartX;
        boolean bl = upHover = mouseX >= upX && mouseX < upX + 18 && mouseY >= btnY && mouseY < btnY + 18;
        if (upHover) {
            this.hoveredButton = 1;
        }
        UIHelper.drawButton(g, upX, btnY, 18, 18, upHover && canUpgrade);
        g.drawString(this.font, "\u2b06", upX + 5, btnY + 5, canUpgrade ? -11141291 : -11184811, false);
        int reX = upX + 18 + 4;
        boolean bl2 = reHover = mouseX >= reX && mouseX < reX + 18 && mouseY >= btnY && mouseY < btnY + 18;
        if (reHover) {
            this.hoveredButton = 2;
        }
        UIHelper.drawButton(g, reX, btnY, 18, 18, reHover && canReroll);
        g.drawString(this.font, "\u267b", reX + 5, btnY + 5, canReroll ? -171 : -11184811, false);
        int rsX = reX + 18 + 4;
        boolean bl3 = rsHover = mouseX >= rsX && mouseX < rsX + 18 && mouseY >= btnY && mouseY < btnY + 18;
        if (rsHover) {
            this.hoveredButton = 3;
        }
        UIHelper.drawButton(g, rsX, btnY, 18, 18, rsHover && canReset);
        g.drawString(this.font, "\u21ba", rsX + 5, btnY + 5, canReset ? -43691 : -11184811, false);
        int cx = px + 10;
        int cy = py + 20 + 10;
        int contentW = 260;
        String castLabel = switch (this.ability.castType()) {
            default -> throw new MatchException(null, null);
            case RelicAbility.CastType.PASSIVE -> "PASSIVE";
            case RelicAbility.CastType.INSTANTANEOUS -> "INSTANTANEOUS";
            case RelicAbility.CastType.TOGGLE -> "TOGGLE";
        };
        g.drawString(this.font, "Cast Type: " + castLabel, cx + 4, cy, -11141121, false);
        String reqText = "Req Level: " + this.ability.requiredLevel();
        int reqW = this.font.width(reqText);
        g.drawString(this.font, reqText, cx + contentW - reqW - 4, cy, -10066330, false);
        cy += 14;
        if (this.ability.description() != null && !this.ability.description().isEmpty()) {
            g.drawString(this.font, this.ability.description(), cx + 4, cy, -661816, false);
            cy += 12;
        }
        UIHelper.drawDivider(g, cx, cy, contentW);
        g.drawString(this.font, "Stats", cx + 4, cy += 6, -2448096, false);
        g.drawString(this.font, "Points invested: " + abilityPoints, cx + contentW - this.font.width("Points invested: " + abilityPoints) - 4, cy, -11141121, false);
        cy += 12;
        List<RelicStat> stats = this.ability.stats();
        for (RelicStat stat : stats) {
            this.renderStatRow(g, cx + 4, cy, contentW - 8, liveStack, stat);
            if ((cy += 26) <= py + 200 - 30) continue;
            break;
        }
        int barX = cx + 4;
        int barW = contentW - 8;
        int barH = 10;
        int bottomY = py + 200 - 24;
        g.drawString(this.font, "Player XP: " + playerXp, barX, bottomY, -661816, false);
        float playerXpFrac = Math.min(1.0f, (float)playerXp / 1000.0f);
        UIHelper.drawProgressBar(g, barX, bottomY += 10, barW, barH, playerXpFrac, -12272828);
        String pxpText = String.valueOf(playerXp);
        int pxpTextW = this.font.width(pxpText);
        g.drawString(this.font, pxpText, barX + (barW - pxpTextW) / 2, bottomY + 1, -661816, false);
        if (this.hoveredButton == 1) {
            String[] lines = new String[]{"Upgrade", "Cost: " + upgradeCost + " XP + 1 point", canUpgrade ? "Click to upgrade" : (unspentPoints <= 0 ? "No unspent points!" : "Not enough XP!")};
            this.renderMultilineTooltip(g, mouseX, mouseY, lines);
        } else if (this.hoveredButton == 2) {
            String[] lines = new String[]{"Reroll Stats", "Cost: " + rerollCost + " XP", "Re-randomizes all stat base values", canReroll ? "Click to reroll" : "Not enough XP!"};
            this.renderMultilineTooltip(g, mouseX, mouseY, lines);
        } else if (this.hoveredButton == 3) {
            String[] lines = new String[]{"Reset Points", "Cost: " + resetCost + " XP", "Reclaims " + abilityPoints + " invested points", canReset ? "Click to reset" : (abilityPoints <= 0 ? "No points to reset!" : "Not enough XP!")};
            this.renderMultilineTooltip(g, mouseX, mouseY, lines);
        }
    }

    private void renderStatRow(GuiGraphics g, int x, int y, int w, ItemStack stack, RelicStat stat) {
        g.drawString(this.font, stat.name() + ":", x, y, -661816, false);
        int barX = x + 60;
        int barW = w - 120;
        int barH = 8;
        double baseValue = RelicData.getStatBaseValue(stack, this.ability.name(), stat.name());
        double computedValue = RelicData.getComputedStatValue(stack, this.ability.name(), stat);
        double range = stat.maxValue() - stat.minValue();
        float fillFrac = range > 0.0 ? (float)((computedValue - stat.minValue()) / range) : 1.0f;
        fillFrac = Math.max(0.0f, Math.min(1.0f, fillFrac));
        UIHelper.drawProgressBar(g, barX, y, barW, barH, fillFrac, -12285731);
        String valText = String.format("%.1f", computedValue);
        g.drawString(this.font, valText, barX + barW + 4, y, -10496, false);
        String rangeText = "(min:" + String.format("%.0f", stat.minValue()) + " max:" + String.format("%.0f", stat.maxValue()) + ")";
        int rangeW = this.font.width(rangeText);
        g.drawString(this.font, rangeText, x + w - rangeW, y, -10066330, false);
        int y2 = y + 10;
        float qualityFrac = range > 0.0 ? (float)((baseValue - stat.minValue()) / range) : 1.0f;
        int stars = Math.max(0, Math.min(5, Math.round(qualityFrac * 5.0f)));
        g.drawString(this.font, "Quality:", x + 60, y2, -10066330, false);
        int qstarX = x + 60 + this.font.width("Quality:") + 2;
        for (int i = 1; i <= 5; ++i) {
            g.drawString(this.font, "\u2605", qstarX, y2, i <= stars ? -10496 : -12303292, false);
            qstarX += 7;
        }
        int pts = RelicData.getAbilityPoints(stack, this.ability.name());
        String ptsText = "Pts: " + pts;
        int ptsW = this.font.width(ptsText);
        g.drawString(this.font, ptsText, x + w - ptsW, y2, -11141121, false);
        String scaleText = switch (stat.scaleType()) {
            default -> throw new MatchException(null, null);
            case RelicStat.ScaleType.ADD -> "+";
            case RelicStat.ScaleType.MULTIPLY_BASE -> "*base";
            case RelicStat.ScaleType.MULTIPLY_TOTAL -> "*total";
        };
        g.drawString(this.font, scaleText + " " + String.format("%.2f", stat.scaleAmount()) + "/pt", x, y2, -10066330, false);
    }

    private void renderMultilineTooltip(GuiGraphics g, int mx, int my, String[] lines) {
        int maxW = 0;
        for (String line : lines) {
            int w = this.font.width(line);
            if (w <= maxW) continue;
            maxW = w;
        }
        int tipW = maxW + 10;
        int tipH = lines.length * 11 + 6;
        int tipX = mx + 12;
        int tipY = my - 4;
        if (tipX + tipW > this.width) {
            tipX = mx - tipW - 4;
        }
        if (tipY + tipH > this.height) {
            tipY = this.height - tipH;
        }
        UIHelper.drawTooltipBackground(g, tipX, tipY, tipW, tipH);
        for (int i = 0; i < lines.length; ++i) {
            int color = i == 0 ? -2448096 : -661816;
            g.drawString(this.font, lines[i], tipX + 5, tipY + 4 + i * 11, color, false);
        }
    }

    private ItemStack findLiveStack() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return null;
        }
        ItemStack mainHand = mc.player.getMainHandItem();
        if (!mainHand.isEmpty() && mainHand.getItem() == this.relicItem) {
            return mainHand;
        }
        ItemStack offHand = mc.player.getOffhandItem();
        if (!offHand.isEmpty() && offHand.getItem() == this.relicItem) {
            return offHand;
        }
        for (int i = 0; i < mc.player.getInventory().getContainerSize(); ++i) {
            ItemStack s = mc.player.getInventory().getItem(i);
            if (s.isEmpty() || s.getItem() != this.relicItem) continue;
            return s;
        }
        return null;
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean consumed) {
        if (consumed) {
            return super.mouseClicked(event, consumed);
        }
        if (event.button() != 0) {
            return super.mouseClicked(event, consumed);
        }
        ItemStack liveStack = this.findLiveStack();
        if (liveStack == null) {
            liveStack = this.relicStack;
        }
        int abilityPoints = RelicData.getAbilityPoints(liveStack, this.ability.name());
        int statCount = Math.max(1, this.ability.stats().size());
        int upgradeCost = (abilityPoints + 1) * statCount * 15;
        int rerollCost = 100 / statCount;
        int resetCost = abilityPoints * 50;
        int unspentPoints = RelicData.getUnspentPoints(liveStack, this.relicItem.getAbilities());
        Minecraft mc = Minecraft.getInstance();
        int playerXp = mc.player != null ? mc.player.totalExperience : 0;
        switch (this.hoveredButton) {
            case 4: {
                Minecraft.getInstance().setScreen(this.parentScreen);
                return true;
            }
            case 1: {
                if (unspentPoints > 0 && playerXp >= upgradeCost) {
                    ClientPacketDistributor.sendToServer((CustomPacketPayload)new RelicTweakPayload("MAINHAND", this.ability.name(), "upgrade"), (CustomPacketPayload[])new CustomPacketPayload[0]);
                }
                return true;
            }
            case 2: {
                if (playerXp >= rerollCost) {
                    ClientPacketDistributor.sendToServer((CustomPacketPayload)new RelicTweakPayload("MAINHAND", this.ability.name(), "reroll"), (CustomPacketPayload[])new CustomPacketPayload[0]);
                }
                return true;
            }
            case 3: {
                if (abilityPoints > 0 && playerXp >= resetCost) {
                    ClientPacketDistributor.sendToServer((CustomPacketPayload)new RelicTweakPayload("MAINHAND", this.ability.name(), "reset"), (CustomPacketPayload[])new CustomPacketPayload[0]);
                }
                return true;
            }
        }
        return super.mouseClicked(event, consumed);
    }

    public boolean isPauseScreen() {
        return false;
    }
}


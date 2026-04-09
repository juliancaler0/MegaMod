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
import com.ultra.megamod.feature.relics.client.AbilityDescriptionScreen;
import com.ultra.megamod.feature.relics.data.RelicAbility;
import com.ultra.megamod.feature.relics.data.RelicData;
import com.ultra.megamod.feature.relics.network.RelicExchangePayload;
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

public class RelicDescriptionScreen
extends Screen {
    private static final int GOLD = -2448096;
    private static final int GOLD_BRIGHT = -10496;
    private static final int CREAM = -661816;
    private static final int DIM = -10066330;
    private static final int RED = -43691;
    private static final int AQUA = -11141121;
    private static final int YELLOW = -171;
    private static final int ORANGE_FILL = -2258944;
    private static final int GREEN_FILL = -12272828;
    private static final int LOCKED = -11184811;
    private static final int STAR_FILLED = -10496;
    private static final int STAR_EMPTY = -12303292;
    private static final int PANEL_W = 280;
    private static final int PANEL_H = 200;
    private static final int TITLE_H = 20;
    private static final int CARD_SIZE = 40;
    private static final int CARD_GAP = 6;
    private static final int EXCHANGE_BTN_W = 70;
    private static final int EXCHANGE_BTN_H = 14;
    private final ItemStack relicStack;
    private final RelicItem relicItem;
    private final List<RelicAbility> abilities;
    private int hoveredAbilityIndex = -1;
    private boolean exchangeHovered = false;
    private boolean exchangeHeld = false;
    private long lastExchangeTick = 0L;
    private int exBtnX;
    private int exBtnY;

    public RelicDescriptionScreen(ItemStack relicStack, RelicItem relicItem) {
        super((Component)Component.literal((String)relicItem.getRelicName()));
        this.relicStack = relicStack;
        this.relicItem = relicItem;
        this.abilities = relicItem.getAbilities();
    }

    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        long now;
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
        String relicName = this.relicItem.getRelicName();
        UIHelper.drawCenteredTitle(g, this.font, relicName, px + 140 - 20, titleBarY + 6);
        int unspent = RelicData.getUnspentPoints(liveStack, this.abilities);
        String pointsText = "Points: " + unspent;
        int ptW = this.font.width(pointsText);
        g.drawString(this.font, pointsText, px + 280 - ptW - 12, titleBarY + 6, unspent > 0 ? -171 : -10066330, false);
        int cx = px + 10;
        int cy = py + 20 + 10;
        int contentW = 260;
        int iconX = cx + 4;
        int iconY = cy + 2;
        UIHelper.drawSlot(g, iconX - 2, iconY - 2, 20, false, true);
        g.renderItem(liveStack, iconX, iconY);
        int infoX = cx + 30;
        int infoY = cy;
        int level = RelicData.getLevel(liveStack);
        g.drawString(this.font, "Level: ", infoX, infoY, -661816, false);
        g.drawString(this.font, level + "/10", infoX + this.font.width("Level: "), infoY, -10496, false);
        int quality = RelicData.getQuality(liveStack);
        g.drawString(this.font, "Quality: ", infoX, infoY += 12, -661816, false);
        int starX = infoX + this.font.width("Quality: ");
        for (int i = 1; i <= 10; ++i) {
            g.drawString(this.font, "\u2605", starX, infoY, i <= quality ? -10496 : -12303292, false);
            starX += 7;
        }
        infoY += 14;
        int barX = cx + 4;
        int barW = contentW - 8;
        int barH = 10;
        int xp = RelicData.getXp(liveStack);
        int xpNeeded = RelicData.getXpForNextLevel(level);
        float xpProgress = level >= 10 ? 1.0f : (float)xp / (float)Math.max(1, xpNeeded);
        g.drawString(this.font, "Relic XP:", barX, infoY, -661816, false);
        UIHelper.drawProgressBar(g, barX, infoY += 10, barW, barH, xpProgress, -2258944);
        String xpText = level >= 10 ? "MAX" : xp + "/" + xpNeeded;
        int xpTextW = this.font.width(xpText);
        g.drawString(this.font, xpText, barX + (barW - xpTextW) / 2, infoY + 1, -661816, false);
        Minecraft mc = Minecraft.getInstance();
        int playerXp = mc.player != null ? mc.player.totalExperience : 0;
        g.drawString(this.font, "Player XP:", barX, infoY += barH + 4, -661816, false);
        this.exBtnX = barX + barW - 70;
        this.exBtnY = infoY - 1;
        this.exchangeHovered = mouseX >= this.exBtnX && mouseX < this.exBtnX + 70 && mouseY >= this.exBtnY && mouseY < this.exBtnY + 14;
        boolean canExchange = playerXp > 0 && level < 10;
        UIHelper.drawButton(g, this.exBtnX, this.exBtnY, 70, 14, this.exchangeHovered && canExchange, this.exchangeHeld);
        String exText = "\u21c4 Exchange";
        int exTextW = this.font.width(exText);
        g.drawString(this.font, exText, this.exBtnX + (70 - exTextW) / 2, this.exBtnY + 3, canExchange ? -661816 : -11184811, false);
        int playerBarW = barW - 70 - 6;
        float playerXpFrac = Math.min(1.0f, (float)playerXp / 1000.0f);
        UIHelper.drawProgressBar(g, barX, infoY += 11, playerBarW, barH, playerXpFrac, -12272828);
        String pxpText = String.valueOf(playerXp);
        int pxpTextW = this.font.width(pxpText);
        g.drawString(this.font, pxpText, barX + (playerBarW - pxpTextW) / 2, infoY + 1, -661816, false);
        UIHelper.drawDivider(g, cx, infoY += barH + 6, contentW);
        g.drawString(this.font, "Abilities", cx + 4, infoY += 6, -2448096, false);
        infoY += 12;
        int totalCardsWidth = this.abilities.size() * 46 - (this.abilities.isEmpty() ? 0 : 6);
        int cardsStartX = cx + (contentW - totalCardsWidth) / 2;
        this.hoveredAbilityIndex = -1;
        for (int i = 0; i < this.abilities.size(); ++i) {
            boolean cardHovered;
            int cardX = cardsStartX + i * 46;
            int cardY = infoY;
            RelicAbility ability = this.abilities.get(i);
            boolean unlocked = RelicData.isAbilityUnlocked(level, ability, this.abilities);
            boolean bl = cardHovered = mouseX >= cardX && mouseX < cardX + 40 && mouseY >= cardY && mouseY < cardY + 40;
            if (cardHovered) {
                this.hoveredAbilityIndex = i;
            }
            this.drawAbilityCard(g, cardX, cardY, ability, liveStack, unlocked, cardHovered);
        }
        if (this.hoveredAbilityIndex >= 0) {
            RelicAbility hovered = this.abilities.get(this.hoveredAbilityIndex);
            boolean unlocked = RelicData.isAbilityUnlocked(level, hovered, this.abilities);
            this.renderAbilityTooltip(g, mouseX, mouseY, hovered, liveStack, unlocked);
        }
        if (this.exchangeHovered) {
            int cost = RelicData.getExchangeCost(liveStack);
            int gain = RelicData.getXpGainPerExchange(liveStack);
            String tipLine1 = "Hold to convert Player XP -> Relic XP";
            String tipLine2 = "Cost: " + cost + " XP per exchange";
            String tipLine3 = "Gain: " + gain + " relic XP per exchange";
            this.renderMultilineTooltip(g, mouseX, mouseY, new String[]{tipLine1, tipLine2, tipLine3});
        }
        if (this.exchangeHeld && canExchange && (now = System.currentTimeMillis()) - this.lastExchangeTick > 150L) {
            this.lastExchangeTick = now;
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new RelicExchangePayload("MAINHAND", 1), (CustomPacketPayload[])new CustomPacketPayload[0]);
        }
    }

    private void drawAbilityCard(GuiGraphics g, int x, int y, RelicAbility ability, ItemStack stack, boolean unlocked, boolean hovered) {
        if (unlocked) {
            UIHelper.drawSlot(g, x, y, 40, hovered, true);
        } else {
            UIHelper.drawSlot(g, x, y, 40, hovered, false);
            g.fill(x + 2, y + 2, x + 40 - 2, y + 40 - 2, -2013265920);
        }
        String abbrev = ability.name().length() >= 2 ? ability.name().substring(0, 2).toUpperCase() : ability.name().toUpperCase();
        int textW = this.font.width(abbrev);
        int textColor = unlocked ? -10496 : -11184811;
        g.drawString(this.font, abbrev, x + (40 - textW) / 2, y + 6, textColor, false);
        if (!unlocked) {
            String reqText = "Lv" + ability.requiredLevel();
            int reqW = this.font.width(reqText);
            g.drawString(this.font, reqText, x + (40 - reqW) / 2, y + 40 - 14, -43691, false);
        } else {
            int pts = RelicData.getAbilityPoints(stack, ability.name());
            if (pts > 0) {
                String ptStr = String.valueOf(pts);
                int ptStrW = this.font.width(ptStr);
                g.drawString(this.font, ptStr, x + 40 - ptStrW - 4, y + 40 - 12, -11141121, false);
            }
            String typeChar = switch (ability.castType()) {
                default -> throw new MatchException(null, null);
                case RelicAbility.CastType.PASSIVE -> "P";
                case RelicAbility.CastType.INSTANTANEOUS -> "!";
                case RelicAbility.CastType.TOGGLE -> "T";
            };
            g.drawString(this.font, typeChar, x + 4, y + 40 - 12, -10066330, false);
        }
    }

    private void renderAbilityTooltip(GuiGraphics g, int mx, int my, RelicAbility ability, ItemStack stack, boolean unlocked) {
        String[] lines;
        if (unlocked) {
            String desc;
            String castLabel = switch (ability.castType()) {
                default -> throw new MatchException(null, null);
                case RelicAbility.CastType.PASSIVE -> "Passive";
                case RelicAbility.CastType.INSTANTANEOUS -> "Instantaneous";
                case RelicAbility.CastType.TOGGLE -> "Toggle";
            };
            int pts = RelicData.getAbilityPoints(stack, ability.name());
            String string = desc = ability.description() != null && !ability.description().isEmpty() ? ability.description() : "";
            lines = !desc.isEmpty() ? new String[]{ability.name(), desc, "Cast: " + castLabel + "  Points: " + pts, "Click to view details"} : new String[]{ability.name(), "Cast: " + castLabel + "  Points: " + pts, "Click to view details"};
        } else {
            String desc = ability.description() != null && !ability.description().isEmpty() ? ability.description() : "";
            lines = !desc.isEmpty() ? new String[]{ability.name() + " (Locked)", desc, "Requires Level " + ability.requiredLevel()} : new String[]{ability.name() + " (Locked)", "Requires Level " + ability.requiredLevel()};
        }
        this.renderMultilineTooltip(g, mx, my, lines);
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
        if (this.exchangeHovered) {
            this.exchangeHeld = true;
            this.lastExchangeTick = 0L;
            return true;
        }
        if (this.hoveredAbilityIndex >= 0) {
            boolean unlocked;
            ItemStack liveStack = this.findLiveStack();
            if (liveStack == null) {
                liveStack = this.relicStack;
            }
            RelicAbility ability = this.abilities.get(this.hoveredAbilityIndex);
            int level = RelicData.getLevel(liveStack);
            boolean bl = unlocked = RelicData.isAbilityUnlocked(level, ability, this.abilities);
            if (unlocked) {
                Minecraft.getInstance().setScreen((Screen)new AbilityDescriptionScreen(liveStack, this.relicItem, ability, this));
            }
            return true;
        }
        return super.mouseClicked(event, consumed);
    }

    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 0) {
            this.exchangeHeld = false;
        }
        return super.mouseReleased(event);
    }

    public boolean isPauseScreen() {
        return false;
    }
}


package com.ultra.megamod.feature.relics.client;

import com.ultra.megamod.feature.relics.RelicItem;
import com.ultra.megamod.feature.relics.data.AccessorySlotType;
import com.ultra.megamod.feature.relics.data.RelicAbility;
import com.ultra.megamod.feature.relics.data.RelicData;
import com.ultra.megamod.feature.relics.data.RelicStat;
import com.ultra.megamod.feature.relics.network.AccessoryPayload;
import com.ultra.megamod.feature.ui.UIHelper;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class AccessoryScreen
extends Screen {
    private static final int GOLD = -2448096;
    private static final int CREAM = -661816;
    private static final int LEATHER = -11916774;
    private static final int DARK = -15069688;
    private static final int SLOT_EMPTY_BG = -15923451;
    private static final int SLOT_HOVER_BG = -14018032;
    private static final int SLOT_EQUIPPED_GLOW = 819635488;
    private static final int GREEN_TEXT = -11141291;
    private static final int DIM_TEXT = -10066330;
    private static final int RED_TEXT = -43691;
    private static final int AQUA_TEXT = -11141121;
    private static final int YELLOW_TEXT = -171;
    private static final int PURPLE_TEXT = -5614081;
    private static final int LOCKED_TEXT = -11184811;
    private static final int STAR_FILLED = -10496;
    private static final int STAR_EMPTY = -12303292;
    private static final int TOTAL_WIDTH = 340;
    private static final int TOTAL_HEIGHT = 296;
    private static final int SLOT_PANEL_WIDTH = 50;
    private static final int INFO_PANEL_WIDTH = 278;
    private static final int SLOT_SIZE = 22;
    private static final int SLOT_GAP = 3;
    private static final int TITLE_HEIGHT = 20;
    private static final int CLOSE_BTN_W = 60;
    private static final int CLOSE_BTN_H = 16;
    private static final int SCROLL_STEP = 12;
    private static final AccessorySlotType[] SLOT_ORDER = new AccessorySlotType[]{AccessorySlotType.HEAD, AccessorySlotType.FACE, AccessorySlotType.BACK, AccessorySlotType.BELT, AccessorySlotType.HANDS_LEFT, AccessorySlotType.HANDS_RIGHT, AccessorySlotType.FEET, AccessorySlotType.NECKLACE, AccessorySlotType.RING_LEFT, AccessorySlotType.RING_RIGHT};
    private static final String[] SLOT_LABELS = new String[]{"HED", "FCE", "BAK", "BLT", "L.H", "R.H", "FET", "NCK", "L.R", "R.R"};
    private int selectedSlotIndex = -1;
    private String errorMessage = null;
    private long errorTime = 0L;
    private int infoScrollOffset = 0;
    private int infoContentHeight = 0;

    public AccessoryScreen() {
        super((Component)Component.literal((String)"Accessories"));
    }

    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        int panelX = (this.width - 340) / 2;
        int panelY = (this.height - 296) / 2;
        UIHelper.drawScreenBg(g, panelX, panelY, 340, 296);
        int titleBarX = panelX + 4;
        int titleBarY = panelY + 4;
        int titleBarW = 332;
        UIHelper.drawTitleBar(g, titleBarX, titleBarY, titleBarW, 20);
        this.drawCenteredGoldText(g, "Accessories", panelX + 170, titleBarY + 6);
        int slotPanelX = panelX + 6;
        int slotPanelY = panelY + 20 + 8;
        int slotPanelH = 242;
        UIHelper.drawCard(g, slotPanelX, slotPanelY, 50, slotPanelH);
        Map<String, String> equipped = AccessoryPayload.AccessorySyncPayload.clientEquipped;
        int slotStartX = slotPanelX + 14;
        int slotStartY = slotPanelY + 6;
        for (int i = 0; i < SLOT_ORDER.length; ++i) {
            int sx = slotStartX;
            int sy = slotStartY + i * 23;
            this.drawAccessorySlot(g, sx, sy, i, equipped, mouseX, mouseY);
        }
        int infoPanelX = slotPanelX + 50 + 6;
        int infoPanelY = slotPanelY;
        int infoPanelW = 278;
        int infoPanelH = slotPanelH;
        UIHelper.drawCard(g, infoPanelX, infoPanelY, infoPanelW, infoPanelH);

        // Scissor clip the info panel content so scrolled text doesn't bleed
        g.enableScissor(infoPanelX + 1, infoPanelY + 1, infoPanelX + infoPanelW - 1, infoPanelY + infoPanelH - 1);
        this.renderInfoPanel(g, infoPanelX, infoPanelY, infoPanelW, infoPanelH, equipped);
        g.disableScissor();

        // Scroll indicator when content overflows
        if (this.infoContentHeight > infoPanelH - 12) {
            int maxScroll = Math.max(0, this.infoContentHeight - (infoPanelH - 12));
            if (this.infoScrollOffset < maxScroll) {
                g.drawString(this.font, "\u25BC", infoPanelX + infoPanelW - 12, infoPanelY + infoPanelH - 10, DIM_TEXT, false);
            }
            if (this.infoScrollOffset > 0) {
                g.drawString(this.font, "\u25B2", infoPanelX + infoPanelW - 12, infoPanelY + 4, DIM_TEXT, false);
            }
        }

        int closeBtnX = panelX + 140;
        int closeBtnY = panelY + 296 - 22;
        boolean closeHovered = mouseX >= closeBtnX && mouseX < closeBtnX + 60 && mouseY >= closeBtnY && mouseY < closeBtnY + 16;
        UIHelper.drawButton(g, closeBtnX, closeBtnY, 60, 16, closeHovered);
        int closeTxtW = this.font.width("Close");
        g.drawString(this.font, "Close", closeBtnX + (60 - closeTxtW) / 2, closeBtnY + 4, -661816, false);
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            ItemStack held = mc.player.getMainHandItem();
            int hintY = panelY + 296 + 4;
            if (!held.isEmpty() && held.getItem() instanceof RelicItem) {
                String heldName = held.getHoverName().getString();
                g.drawCenteredString(this.font, "Holding: " + heldName, this.width / 2, hintY, -11141291);
            } else if (!held.isEmpty()) {
                g.drawCenteredString(this.font, "Holding: " + held.getHoverName().getString() + " (not a relic)", this.width / 2, hintY, -10066330);
            } else {
                g.drawCenteredString(this.font, "Hold a relic in main hand to equip", this.width / 2, hintY, -10066330);
            }
        }
        if (this.errorMessage != null && System.currentTimeMillis() - this.errorTime < 2000L) {
            g.drawCenteredString(this.font, this.errorMessage, this.width / 2, panelY + 296 + 14, -43691);
        } else {
            this.errorMessage = null;
        }
        this.renderSlotTooltip(g, mouseX, mouseY, equipped);
    }

    private void drawAccessorySlot(GuiGraphics g, int x, int y, int slotIndex, Map<String, String> equipped, int mouseX, int mouseY) {
        AccessorySlotType slotType = SLOT_ORDER[slotIndex];
        String slotName = slotType.name();
        boolean isEquipped = equipped != null && equipped.containsKey(slotName);
        boolean isSelected = slotIndex == this.selectedSlotIndex;
        boolean hovered = mouseX >= x && mouseX < x + 22 && mouseY >= y && mouseY < y + 22;
        UIHelper.drawSlot(g, x, y, 22);
        if (isEquipped) {
            g.fill(x + 1, y + 1, x + 22 - 1, y + 22 - 1, 819635488);
        }
        if (hovered) {
            g.fill(x + 1, y + 1, x + 22 - 1, y + 22 - 1, 0x20FFFFFF);
        }
        if (isSelected) {
            AccessoryScreen.drawBorder(g, x, y, 22, 22, -2448096);
        }
        if (isEquipped) {
            String itemId = equipped.get(slotName);
            try {
                Item item = (Item)BuiltInRegistries.ITEM.getValue(Identifier.parse((String)itemId));
                ItemStack displayStack = new ItemStack((ItemLike)item);
                int itemX = x + 3;
                int itemY = y + 3;
                g.renderItem(displayStack, itemX, itemY);
            }
            catch (Exception e) {
                g.fill(x + 7, y + 7, x + 15, y + 15, -11141291);
            }
        } else {
            String label = SLOT_LABELS[slotIndex];
            int labelW = this.font.width(label);
            int labelX = x + (22 - labelW) / 2;
            int labelY = y + 7;
            g.drawString(this.font, label, labelX, labelY, -10066330, false);
        }
    }

    private void renderInfoPanel(GuiGraphics g, int px, int py, int pw, int ph, Map<String, String> equipped) {
        Item item;
        int textX = px + 6;
        int textY = py + 6 - this.infoScrollOffset;
        int startY = textY;
        if (this.selectedSlotIndex < 0 || this.selectedSlotIndex >= SLOT_ORDER.length) {
            g.drawString(this.font, "Select a slot", textX, textY, -10066330, false);
            g.drawString(this.font, "to view details.", textX, textY + 12, -10066330, false);
            this.infoContentHeight = 24;
            return;
        }
        AccessorySlotType slotType = SLOT_ORDER[this.selectedSlotIndex];
        String slotName = slotType.name();
        g.drawString(this.font, slotType.getDisplayName() + " Slot", textX, textY, -2448096, false);
        textY += 14;
        if (equipped == null || !equipped.containsKey(slotName)) {
            g.drawString(this.font, "Empty", textX, textY, -10066330, false);
            this.infoContentHeight = textY + 10 - startY;
            return;
        }
        String itemId = equipped.get(slotName);
        try {
            item = (Item)BuiltInRegistries.ITEM.getValue(Identifier.parse((String)itemId));
        }
        catch (Exception e) {
            g.drawString(this.font, "Unknown item", textX, textY, -43691, false);
            this.infoContentHeight = textY + 10 - startY;
            return;
        }
        if (!(item instanceof RelicItem) && !(item instanceof com.ultra.megamod.feature.dungeons.item.UmvuthanaMaskItem)) {
            g.drawString(this.font, "Not a relic", textX, textY, -43691, false);
            this.infoContentHeight = textY + 10 - startY;
            return;
        }
        if (item instanceof com.ultra.megamod.feature.dungeons.item.UmvuthanaMaskItem maskItem) {
            g.drawString(this.font, "Mask of " + maskItem.getMaskType().displayName, textX, textY, -661816, false);
            UIHelper.drawDivider(g, textX, textY += 12, pw - 12);
            textY += 7;
            g.drawString(this.font, maskItem.getMaskType().description, textX, textY, -11141291, false);
            textY += 11;
            g.drawString(this.font, "Passive when worn", textX, textY, -10066330, false);
            this.infoContentHeight = textY + 10 - startY;
            return;
        }
        RelicItem relicItem = (RelicItem)item;
        String relicName = relicItem.getRelicName();
        g.drawString(this.font, relicName, textX, textY, -661816, false);
        UIHelper.drawDivider(g, textX, textY += 12, pw - 12);
        textY += 7;
        ItemStack infoStack = this.findInfoStack(relicItem);
        if (infoStack != null && RelicData.isInitialized(infoStack)) {
            int unspent;
            int level = RelicData.getLevel(infoStack);
            int quality = RelicData.getQuality(infoStack);
            int xp = RelicData.getXp(infoStack);
            int xpNeeded = 100 + level * 50;
            g.drawString(this.font, "Level: " + level + "/10", textX, textY, -11141291, false);
            g.drawString(this.font, "XP:", textX, textY += 11, -661816, false);
            int barX = textX + 22;
            int barW = pw - 34;
            int barH = 8;
            UIHelper.drawProgressBarBg(g, barX, textY, barW, barH);
            float progress = xpNeeded > 0 ? (float)xp / (float)xpNeeded : 0.0f;
            int fillW = (int)((float)barW * Math.min(1.0f, progress));
            UIHelper.drawProgressBarFill(g, barX, textY, fillW, barH);
            String xpText = xp + "/" + xpNeeded;
            int xpTextW = this.font.width(xpText);
            g.drawString(this.font, xpText, barX + (barW - xpTextW) / 2, textY, -661816, false);
            this.renderQualityStars(g, textX, textY += 12, quality);
            UIHelper.drawDivider(g, textX, textY += 12, pw - 12);
            g.drawString(this.font, "Abilities:", textX, textY += 7, -2448096, false);
            textY += 11;
            List<RelicAbility> abilities = relicItem.getAbilities();
            for (RelicAbility ability : abilities) {
                boolean unlocked = RelicData.isAbilityUnlocked(level, ability, abilities);
                String castLabel = switch (ability.castType()) {
                    default -> throw new MatchException(null, null);
                    case RelicAbility.CastType.PASSIVE -> "Passive";
                    case RelicAbility.CastType.INSTANTANEOUS -> "Instant";
                    case RelicAbility.CastType.TOGGLE -> "Toggle";
                };
                String lockSuffix = unlocked ? "" : " [Lv" + ability.requiredLevel() + "]";
                int nameColor = unlocked ? -661816 : -11184811;
                String line = " \u2022 " + ability.name() + " (" + castLabel + ")" + lockSuffix;
                g.drawString(this.font, line, textX, textY, nameColor, false);
                textY += 10;
                if (unlocked && !ability.stats().isEmpty()) {
                    for (RelicStat stat : ability.stats()) {
                        double value = RelicData.getComputedStatValue(infoStack, ability.name(), stat);
                        String statLine = "     " + stat.name() + ": " + String.format("%.1f", value);
                        g.drawString(this.font, statLine, textX, textY, -10066330, false);
                        textY += 9;
                    }
                }
            }
            if ((unspent = RelicData.getUnspentPoints(infoStack, abilities)) > 0) {
                g.drawString(this.font, unspent + " unspent point" + (unspent > 1 ? "s" : "") + "!", textX, textY += 2, -171, false);
                textY += 10;
            }
        } else {
            g.drawString(this.font, "Equipped: " + AccessoryScreen.formatItemId(itemId), textX, textY, -661816, false);
            textY += 12;
            List<RelicAbility> abilities = relicItem.getAbilities();
            if (!abilities.isEmpty()) {
                UIHelper.drawDivider(g, textX, textY, pw - 12);
                g.drawString(this.font, "Abilities:", textX, textY += 7, -2448096, false);
                textY += 11;
                for (RelicAbility ability : abilities) {
                    String castLabel = switch (ability.castType()) {
                        default -> throw new MatchException(null, null);
                        case RelicAbility.CastType.PASSIVE -> "Passive";
                        case RelicAbility.CastType.INSTANTANEOUS -> "Instant";
                        case RelicAbility.CastType.TOGGLE -> "Toggle";
                    };
                    String line = " \u2022 " + ability.name() + " (" + castLabel + ") [Lv" + ability.requiredLevel() + "]";
                    g.drawString(this.font, line, textX, textY, -661816, false);
                    textY += 10;
                }
            }
            g.drawString(this.font, "Hold this relic for", textX, textY += 4, -10066330, false);
            g.drawString(this.font, "detailed stats.", textX, textY += 10, -10066330, false);
            textY += 10;
        }
        this.infoContentHeight = textY - startY;
    }

    private void renderQualityStars(GuiGraphics g, int x, int y, int quality) {
        g.drawString(this.font, "Quality: ", x, y, -661816, false);
        int starX = x + this.font.width("Quality: ");
        for (int i = 1; i <= 10; ++i) {
            int color = i <= quality ? -10496 : -12303292;
            g.drawString(this.font, "\u2605", starX, y, color, false);
            starX += 7;
        }
    }

    private ItemStack findInfoStack(RelicItem relicItem) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return null;
        }
        ItemStack mainHand = mc.player.getMainHandItem();
        if (!mainHand.isEmpty() && mainHand.getItem() == relicItem) {
            return mainHand;
        }
        ItemStack offHand = mc.player.getOffhandItem();
        if (!offHand.isEmpty() && offHand.getItem() == relicItem) {
            return offHand;
        }
        for (int i = 0; i < mc.player.getInventory().getContainerSize(); ++i) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack.isEmpty() || stack.getItem() != relicItem) continue;
            return stack;
        }
        return null;
    }

    private void renderSlotTooltip(GuiGraphics g, int mouseX, int mouseY, Map<String, String> equipped) {
        int panelX = (this.width - 340) / 2;
        int panelY = (this.height - 296) / 2;
        int slotPanelX = panelX + 6;
        int slotStartX = slotPanelX + 14;
        int slotStartY = panelY + 20 + 8 + 6;
        for (int i = 0; i < SLOT_ORDER.length; ++i) {
            String tipText;
            boolean isEquipped;
            int sx = slotStartX;
            int sy = slotStartY + i * 23;
            if (mouseX < sx || mouseX >= sx + 22 || mouseY < sy || mouseY >= sy + 22) continue;
            AccessorySlotType slotType = SLOT_ORDER[i];
            String slotName = slotType.name();
            boolean bl = isEquipped = equipped != null && equipped.containsKey(slotName);
            if (isEquipped) {
                String itemId = equipped.get(slotName);
                tipText = slotType.getDisplayName() + ": " + AccessoryScreen.formatItemId(itemId) + " (click to unequip)";
            } else {
                tipText = slotType.getDisplayName() + ": Empty (hold relic & click)";
            }
            int tipW = this.font.width(tipText) + 8;
            int tipH = 14;
            int tipX = mouseX + 10;
            int tipY = mouseY - 4;
            if (tipX + tipW > this.width) {
                tipX = mouseX - tipW - 4;
            }
            if (tipY + tipH > this.height) {
                tipY = mouseY - tipH;
            }
            g.fill(tipX - 2, tipY - 2, tipX + tipW + 2, tipY + tipH + 2, -267386864);
            g.fill(tipX - 1, tipY - 1, tipX + tipW + 1, tipY + tipH + 1, -265813976);
            AccessoryScreen.drawBorder(g, tipX - 2, tipY - 2, tipW + 4, tipH + 4, -11534176);
            g.drawString(this.font, tipText, tipX + 2, tipY + 3, -661816, false);
            break;
        }
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean consumed) {
        if (consumed) {
            return super.mouseClicked(event, consumed);
        }
        if (event.button() != 0) {
            return super.mouseClicked(event, consumed);
        }
        double mx = event.x();
        double my = event.y();
        int panelX = (this.width - 340) / 2;
        int panelY = (this.height - 296) / 2;
        Map<String, String> equipped = AccessoryPayload.AccessorySyncPayload.clientEquipped;
        int slotPanelX = panelX + 6;
        int slotStartX = slotPanelX + 14;
        int slotStartY = panelY + 20 + 8 + 6;
        for (int i = 0; i < SLOT_ORDER.length; ++i) {
            int sx = slotStartX;
            int sy = slotStartY + i * 23;
            if (!(mx >= (double)sx) || !(mx < (double)(sx + 22)) || !(my >= (double)sy) || !(my < (double)(sy + 22))) continue;
            if (i != this.selectedSlotIndex) {
                this.infoScrollOffset = 0;
            }
            this.selectedSlotIndex = i;
            this.handleSlotClick(SLOT_ORDER[i], equipped);
            return true;
        }
        int closeBtnX = panelX + 140;
        int closeBtnY = panelY + 296 - 22;
        if (mx >= (double)closeBtnX && mx < (double)(closeBtnX + 60) && my >= (double)closeBtnY && my < (double)(closeBtnY + 16)) {
            this.onClose();
            return true;
        }
        return super.mouseClicked(event, consumed);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int panelX = (this.width - 340) / 2;
        int panelY = (this.height - 296) / 2;
        int slotPanelX = panelX + 6;
        int infoPanelX = slotPanelX + 50 + 6;
        int infoPanelY = panelY + 20 + 8;
        int infoPanelW = 278;
        int infoPanelH = 242;

        // Only scroll when mouse is over the info panel
        if (mouseX >= infoPanelX && mouseX < infoPanelX + infoPanelW
                && mouseY >= infoPanelY && mouseY < infoPanelY + infoPanelH) {
            this.infoScrollOffset -= (int)(scrollY * SCROLL_STEP);
            int maxScroll = Math.max(0, this.infoContentHeight - (infoPanelH - 12));
            this.infoScrollOffset = Math.max(0, Math.min(this.infoScrollOffset, maxScroll));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private void handleSlotClick(AccessorySlotType slotType, Map<String, String> equipped) {
        boolean isEquipped;
        String slotName = slotType.name();
        boolean bl = isEquipped = equipped != null && equipped.containsKey(slotName);
        if (isEquipped) {
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new AccessoryPayload.AccessoryEquipPayload(slotName, true), (CustomPacketPayload[])new CustomPacketPayload[0]);
        } else {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) {
                return;
            }
            ItemStack held = mc.player.getMainHandItem();
            if (held.isEmpty()) {
                this.showError("Nothing in main hand!");
                return;
            }
            Item item = held.getItem();
            AccessorySlotType relicSlot;
            if (item instanceof RelicItem relicItem) {
                relicSlot = relicItem.getSlotType();
            } else if (item instanceof com.ultra.megamod.feature.dungeons.item.UmvuthanaMaskItem) {
                relicSlot = AccessorySlotType.FACE;
            } else {
                this.showError("Not a relic item!");
                return;
            }
            boolean compatible = false;
            if (relicSlot == slotType) {
                compatible = true;
            } else if (!(relicSlot != AccessorySlotType.HANDS_LEFT && relicSlot != AccessorySlotType.HANDS_RIGHT || slotType != AccessorySlotType.HANDS_LEFT && slotType != AccessorySlotType.HANDS_RIGHT)) {
                compatible = true;
            } else if (!(relicSlot != AccessorySlotType.RING_LEFT && relicSlot != AccessorySlotType.RING_RIGHT || slotType != AccessorySlotType.RING_LEFT && slotType != AccessorySlotType.RING_RIGHT)) {
                compatible = true;
            }
            if (!compatible) {
                this.showError("Wrong slot! This relic goes in " + relicSlot.getDisplayName());
                return;
            }
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new AccessoryPayload.AccessoryEquipPayload(slotName, false), (CustomPacketPayload[])new CustomPacketPayload[0]);
        }
    }

    private void showError(String message) {
        this.errorMessage = message;
        this.errorTime = System.currentTimeMillis();
    }

    private void drawCenteredGoldText(GuiGraphics g, String text, int centerX, int y) {
        int w = this.font.width(text);
        g.drawString(this.font, text, centerX - w / 2, y, -2448096, false);
    }

    private static void drawBorder(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + 1, color);
        g.fill(x, y + h - 1, x + w, y + h, color);
        g.fill(x, y, x + 1, y + h, color);
        g.fill(x + w - 1, y, x + w, y + h, color);
    }

    private static String formatItemId(String itemId) {
        String name = itemId.contains(":") ? itemId.substring(itemId.indexOf(58) + 1) : itemId;
        StringBuilder sb = new StringBuilder();
        String[] parts = name.split("_");
        for (int i = 0; i < parts.length; ++i) {
            if (i > 0) {
                sb.append(" ");
            }
            if (parts[i].isEmpty()) continue;
            sb.append(Character.toUpperCase(parts[i].charAt(0)));
            if (parts[i].length() <= 1) continue;
            sb.append(parts[i].substring(1));
        }
        return sb.toString();
    }

    public boolean isPauseScreen() {
        return false;
    }
}

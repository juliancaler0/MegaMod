/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.inventory.InventoryScreen
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.inventory.InventoryMenu
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.client.event.ScreenEvent$Closing
 *  net.neoforged.neoforge.client.event.ScreenEvent$Init$Post
 *  net.neoforged.neoforge.client.event.ScreenEvent$MouseButtonPressed$Pre
 *  net.neoforged.neoforge.client.event.ScreenEvent$Render$Post
 *  net.neoforged.neoforge.client.network.ClientPacketDistributor
 */
package com.ultra.megamod.feature.relics.client;

import com.ultra.megamod.feature.relics.RelicItem;
import com.ultra.megamod.feature.relics.data.AccessorySlotType;
import com.ultra.megamod.feature.relics.data.RelicData;
import com.ultra.megamod.feature.relics.network.AccessoryPayload;
import com.ultra.megamod.feature.relics.network.AccessoryQuickEquipPayload;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

@EventBusSubscriber(modid="megamod", value={Dist.CLIENT})
public class InventoryButtonHandler {
    private static boolean panelOpen = false;
    private static int hoveredSlot = -1;
    private static String errorMessage = null;
    private static long errorTime = 0L;
    private static Button toggleButton = null;
    private static final int SLOT_SIZE = 18;
    private static final int SLOT_GAP = 2;
    private static final int PANEL_PADDING = 4;
    private static final int PANEL_WIDTH = 26;
    private static final int PANEL_GAP = 4;
    private static final int TOGGLE_SIZE = 14;
    private static final int PANEL_BG = -871625716;
    private static final int PANEL_BORDER = -12961222;
    private static final int PANEL_BORDER_LIGHT = -10855846;
    private static final int SLOT_BG = -7631989;
    private static final int SLOT_BORDER_DARK = -13158601;
    private static final int SLOT_BORDER_LIGHT = -1;
    private static final int SLOT_INNER = -11184811;
    private static final int HOVER_HIGHLIGHT = 0x40FFFFFF;
    private static final int EQUIPPED_GLOW = 822073088;
    private static final int DIM_TEXT = -11184811;
    private static final int CREAM = -661816;
    private static final int GOLD = -2448096;
    private static final int RED_TEXT = -43691;
    private static final int GREEN_TEXT = -11141291;
    private static final int TOOLTIP_BG = -267386864;
    private static final int TOOLTIP_BORDER_OUTER = -11534176;
    private static final int TOOLTIP_BORDER_INNER = -14155736;
    private static final AccessorySlotType[] SLOT_ORDER = new AccessorySlotType[]{AccessorySlotType.HEAD, AccessorySlotType.FACE, AccessorySlotType.BACK, AccessorySlotType.BELT, AccessorySlotType.HANDS_LEFT, AccessorySlotType.HANDS_RIGHT, AccessorySlotType.FEET, AccessorySlotType.NECKLACE, AccessorySlotType.RING_LEFT, AccessorySlotType.RING_RIGHT};
    private static final String[] SLOT_ICONS = new String[]{"\u2655", "\u263a", "\u25c8", "\u25ac", "\u270b", "\u270b", "\u25bc", "\u25c6", "\u25cb", "\u25cb"};

    public static void togglePanel() {
        panelOpen = !panelOpen;
    }

    public static void setPanelOpen(boolean open) {
        panelOpen = open;
    }

    public static boolean isPanelOpen() {
        return panelOpen;
    }

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();
        if (!(screen instanceof InventoryScreen)) {
            toggleButton = null;
            return;
        }
        InventoryScreen inv = (InventoryScreen)screen;
        int btnX = inv.getGuiLeft() - 14 - 2;
        int btnY = inv.getGuiTop() + 2;
        toggleButton = Button.builder((Component)Component.literal((String)"\u2b22"), btn -> {
            panelOpen = !panelOpen;
        }).bounds(btnX, btnY, 14, 14).build();
        event.addListener((GuiEventListener)toggleButton);
    }

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        ItemStack carried;
        Screen screen = event.getScreen();
        if (!(screen instanceof InventoryScreen)) {
            return;
        }
        InventoryScreen inv = (InventoryScreen)screen;
        Minecraft mc = Minecraft.getInstance();
        if (toggleButton != null) {
            int btnX = inv.getGuiLeft() - 14 - 2;
            int btnY = inv.getGuiTop() + 2;
            toggleButton.setX(btnX);
            toggleButton.setY(btnY);
            toggleButton.render(event.getGuiGraphics(), event.getMouseX(), event.getMouseY(), event.getPartialTick());
        }
        if (!panelOpen) {
            return;
        }
        GuiGraphics g = event.getGuiGraphics();
        int mouseX = event.getMouseX();
        int mouseY = event.getMouseY();
        int panelHeight = SLOT_ORDER.length * 20 - 2 + 8;
        int panelX = inv.getGuiLeft() - 26 - 4;
        int panelY = inv.getGuiTop();
        InventoryButtonHandler.drawPanelBackground(g, panelX, panelY, 26, panelHeight);
        Map<String, String> equipped = AccessoryPayload.AccessorySyncPayload.clientEquipped;
        hoveredSlot = -1;
        for (int i = 0; i < SLOT_ORDER.length; ++i) {
            boolean hovered;
            int sx = panelX + 4;
            int sy = panelY + 4 + i * 20;
            boolean bl = hovered = mouseX >= sx && mouseX < sx + 18 && mouseY >= sy && mouseY < sy + 18;
            if (hovered) {
                hoveredSlot = i;
            }
            InventoryButtonHandler.drawAccessorySlot(g, mc, sx, sy, i, equipped, hovered);
        }
        if (errorMessage != null && System.currentTimeMillis() - errorTime < 2000L) {
            int errX = panelX;
            int errY = panelY + panelHeight + 4;
            int errW = mc.font.width(errorMessage);
            g.drawString(mc.font, errorMessage, panelX - errW + 26, errY, -43691, true);
        } else {
            errorMessage = null;
        }
        if (hoveredSlot >= 0) {
            InventoryButtonHandler.renderSlotTooltip(g, mc, mouseX, mouseY, equipped);
        }
        if (!(carried = ((InventoryMenu)inv.getMenu()).getCarried()).isEmpty()) {
            g.nextStratum();
            g.renderItem(carried, mouseX - 8, mouseY - 8);
            g.renderItemDecorations(mc.font, carried, mouseX - 8, mouseY - 8);
        }
    }

    @SubscribeEvent
    public static void onMouseClick(ScreenEvent.MouseButtonPressed.Pre event) {
        Slot hoveredInvSlot;
        Screen screen = event.getScreen();
        if (!(screen instanceof InventoryScreen)) {
            return;
        }
        InventoryScreen inv = (InventoryScreen)screen;
        if (event.getButton() != 0) {
            return;
        }
        if (panelOpen && event.getMouseButtonEvent().hasShiftDown() && (hoveredInvSlot = inv.getSlotUnderMouse()) != null && hoveredInvSlot.hasItem()) {
            RelicItem relic;
            AccessorySlotType targetSlot;
            ItemStack stack;
            Item item;
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && hoveredInvSlot.container == mc.player.getInventory()) {
                item = (stack = hoveredInvSlot.getItem()).getItem();
                if (item instanceof RelicItem ri && ri.getSlotType() != AccessorySlotType.NONE) {
                    ClientPacketDistributor.sendToServer((CustomPacketPayload)new AccessoryQuickEquipPayload(hoveredInvSlot.getContainerSlot()), (CustomPacketPayload[])new CustomPacketPayload[0]);
                    event.setCanceled(true);
                    return;
                } else if (item instanceof com.ultra.megamod.feature.dungeons.item.UmvuthanaMaskItem) {
                    ClientPacketDistributor.sendToServer((CustomPacketPayload)new AccessoryQuickEquipPayload(hoveredInvSlot.getContainerSlot()), (CustomPacketPayload[])new CustomPacketPayload[0]);
                    event.setCanceled(true);
                    return;
                }
            }
        }
        if (!panelOpen) {
            return;
        }
        double mx = event.getMouseX();
        double my = event.getMouseY();
        int panelHeight = SLOT_ORDER.length * 20 - 2 + 8;
        int panelX = inv.getGuiLeft() - 26 - 4;
        int panelY = inv.getGuiTop();
        Map<String, String> equipped = AccessoryPayload.AccessorySyncPayload.clientEquipped;
        for (int i = 0; i < SLOT_ORDER.length; ++i) {
            int sx = panelX + 4;
            int sy = panelY + 4 + i * 20;
            if (!(mx >= (double)sx) || !(mx < (double)(sx + 18)) || !(my >= (double)sy) || !(my < (double)(sy + 18))) continue;
            InventoryButtonHandler.handleSlotClick(SLOT_ORDER[i], equipped);
            event.setCanceled(true);
            return;
        }
    }

    @SubscribeEvent
    public static void onScreenClose(ScreenEvent.Closing event) {
        if (event.getScreen() instanceof InventoryScreen) {
            toggleButton = null;
            hoveredSlot = -1;
        }
    }

    private static void drawPanelBackground(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x - 1, y - 1, x + w + 1, y + h + 1, -12961222);
        g.fill(x, y, x + w, y + h, -3750202);
        g.fill(x, y, x + w, y + 1, -1);
        g.fill(x, y, x + 1, y + h, -1);
        g.fill(x, y + h - 1, x + w, y + h, -13158601);
        g.fill(x + w - 1, y, x + w, y + h, -13158601);
    }

    private static void drawAccessorySlot(GuiGraphics g, Minecraft mc, int x, int y, int slotIndex, Map<String, String> equipped, boolean hovered) {
        AccessorySlotType slotType = SLOT_ORDER[slotIndex];
        String slotName = slotType.name();
        boolean isEquipped = equipped != null && equipped.containsKey(slotName);
        g.fill(x, y, x + 18, y + 18, -7631989);
        g.fill(x, y, x + 18, y + 1, -13158601);
        g.fill(x, y, x + 1, y + 18, -13158601);
        g.fill(x, y + 18 - 1, x + 18, y + 18, -1);
        g.fill(x + 18 - 1, y, x + 18, y + 18, -1);
        g.fill(x + 1, y + 1, x + 18 - 1, y + 18 - 1, -11184811);
        if (isEquipped) {
            g.fill(x + 1, y + 1, x + 18 - 1, y + 18 - 1, 822073088);
            String itemId = equipped.get(slotName);
            try {
                Item item = (Item)BuiltInRegistries.ITEM.getValue(Identifier.parse((String)itemId));
                ItemStack displayStack = new ItemStack((ItemLike)item);
                int itemX = x + 1;
                int itemY = y + 1;
                g.renderItem(displayStack, itemX, itemY);
            }
            catch (Exception e) {
                g.fill(x + 5, y + 5, x + 18 - 5, y + 18 - 5, -11141291);
            }
        } else {
            String icon = SLOT_ICONS[slotIndex];
            int iconW = mc.font.width(icon);
            int iconX = x + (18 - iconW) / 2;
            int iconY = y + 5;
            g.drawString(mc.font, icon, iconX, iconY, -12961222, false);
        }
        if (hovered) {
            g.fill(x + 1, y + 1, x + 18 - 1, y + 18 - 1, 0x40FFFFFF);
        }
    }

    private static void renderSlotTooltip(GuiGraphics g, Minecraft mc, int mouseX, int mouseY, Map<String, String> equipped) {
        String line1;
        String line3;
        Object line2;
        boolean isEquipped;
        block14: {
            if (hoveredSlot < 0 || hoveredSlot >= SLOT_ORDER.length) {
                return;
            }
            AccessorySlotType slotType = SLOT_ORDER[hoveredSlot];
            String slotName = slotType.name();
            isEquipped = equipped != null && equipped.containsKey(slotName);
            line2 = null;
            line3 = null;
            if (isEquipped) {
                String itemId = equipped.get(slotName);
                String itemName = InventoryButtonHandler.formatItemId(itemId);
                try {
                    Item item = (Item)BuiltInRegistries.ITEM.getValue(Identifier.parse((String)itemId));
                    if (item instanceof RelicItem) {
                        RelicItem relicItem = (RelicItem)item;
                        line1 = slotType.getDisplayName() + ": " + relicItem.getRelicName();
                        ItemStack infoStack = InventoryButtonHandler.findInfoStack(relicItem);
                        if (infoStack != null && RelicData.isInitialized(infoStack)) {
                            int level = RelicData.getLevel(infoStack);
                            int quality = RelicData.getQuality(infoStack);
                            line2 = "Lv." + level + " / Quality " + quality + "/10";
                        }
                        line3 = "Click to unequip";
                        break block14;
                    }
                    if (item instanceof com.ultra.megamod.feature.dungeons.item.UmvuthanaMaskItem maskItem) {
                        line1 = slotType.getDisplayName() + ": Mask of " + maskItem.getMaskType().displayName;
                        line2 = maskItem.getMaskType().description;
                        line3 = "Click to unequip";
                        break block14;
                    }
                    line1 = slotType.getDisplayName() + ": " + itemName;
                    line3 = "Click to unequip";
                }
                catch (Exception e) {
                    line1 = slotType.getDisplayName() + ": " + itemName;
                    line3 = "Click to unequip";
                }
            } else {
                line1 = slotType.getDisplayName() + " - Empty";
                line2 = "Hold matching relic";
                line3 = "and click to equip";
            }
        }
        int maxWidth = mc.font.width(line1);
        if (line2 != null) {
            maxWidth = Math.max(maxWidth, mc.font.width((String)line2));
        }
        if (line3 != null) {
            maxWidth = Math.max(maxWidth, mc.font.width(line3));
        }
        int tipW = maxWidth + 8;
        int lineCount = 1 + (line2 != null ? 1 : 0) + (line3 != null ? 1 : 0);
        int tipH = lineCount * 10 + 6;
        int tipX = mouseX + 12;
        int tipY = mouseY - 4;
        if (tipX + tipW > mc.getWindow().getGuiScaledWidth()) {
            tipX = mouseX - tipW - 4;
        }
        if (tipY + tipH > mc.getWindow().getGuiScaledHeight()) {
            tipY = mouseY - tipH;
        }
        if (tipY < 0) {
            tipY = 0;
        }
        g.fill(tipX - 3, tipY - 3, tipX + tipW + 3, tipY + tipH + 3, -267386864);
        g.fill(tipX - 2, tipY - 2, tipX + tipW + 2, tipY + tipH + 2, -14155736);
        g.fill(tipX - 1, tipY - 1, tipX + tipW + 1, tipY + tipH + 1, -267386864);
        InventoryButtonHandler.drawBorder(g, tipX - 3, tipY - 3, tipW + 6, tipH + 6, -11534176);
        int textY = tipY + 2;
        g.drawString(mc.font, line1, tipX + 2, textY, isEquipped ? -2448096 : -661816, false);
        textY += 10;
        if (line2 != null) {
            g.drawString(mc.font, (String)line2, tipX + 2, textY, isEquipped ? -11141291 : -11184811, false);
            textY += 10;
        }
        if (line3 != null) {
            g.drawString(mc.font, line3, tipX + 2, textY, -11184811, false);
        }
    }

    private static void handleSlotClick(AccessorySlotType slotType, Map<String, String> equipped) {
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
                InventoryButtonHandler.showError("Nothing in main hand!");
                return;
            }
            Item item = held.getItem();
            AccessorySlotType relicSlot;
            if (item instanceof RelicItem relicItem) {
                relicSlot = relicItem.getSlotType();
            } else if (item instanceof com.ultra.megamod.feature.dungeons.item.UmvuthanaMaskItem) {
                relicSlot = AccessorySlotType.FACE;
            } else {
                InventoryButtonHandler.showError("Not a relic!");
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
                InventoryButtonHandler.showError(relicSlot.getDisplayName() + " slot!");
                return;
            }
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new AccessoryPayload.AccessoryEquipPayload(slotName, false), (CustomPacketPayload[])new CustomPacketPayload[0]);
        }
    }

    private static void showError(String message) {
        errorMessage = message;
        errorTime = System.currentTimeMillis();
    }

    private static ItemStack findInfoStack(RelicItem relicItem) {
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

    private static void drawBorder(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + 1, color);
        g.fill(x, y + h - 1, x + w, y + h, color);
        g.fill(x, y, x + 1, y + h, color);
        g.fill(x + w - 1, y, x + w, y + h, color);
    }
}


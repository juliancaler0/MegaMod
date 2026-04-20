package com.ultra.megamod.feature.dungeons.insurance.client;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ultra.megamod.feature.dungeons.insurance.network.InsuranceOpenPayload;
import com.ultra.megamod.feature.dungeons.insurance.network.InsuranceReadyPayload;
import com.ultra.megamod.feature.dungeons.insurance.network.InsuranceStatusPayload;
import com.ultra.megamod.feature.ui.UIHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.*;

public class InsuranceScreen extends Screen {
    private static final Gson GSON = new Gson();
    private static final int PANEL_WIDTH = 370;
    private static final int PANEL_HEIGHT = 440;
    private static final int SLOT_SIZE = 20;
    private static final int SLOT_GAP = 8;
    private static final int MAX_SLOTS_PER_ROW = 9; // wrap after 9 items

    private final String tierName;
    private final Map<String, Integer> slotCosts; // slot name → cost
    private final List<String> partyNames;
    private final Set<String> selectedSlots = new HashSet<>();

    // UI state
    private final List<ClickRect> clickRects = new ArrayList<>();
    private int panelX, panelY;
    private boolean submitted = false;

    // Party status (updated via tick polling)
    private Map<String, Boolean> partyReady = new LinkedHashMap<>();

    public InsuranceScreen(String tierName, String jsonSlotCosts, String jsonPartyNames) {
        super(Component.literal("Dungeon Insurance"));
        this.tierName = tierName;

        // Parse slot costs
        this.slotCosts = new LinkedHashMap<>();
        try {
            JsonObject obj = GSON.fromJson(jsonSlotCosts, JsonObject.class);
            for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                slotCosts.put(entry.getKey(), entry.getValue().getAsInt());
            }
        } catch (Exception ignored) {}

        // Parse party names
        this.partyNames = new ArrayList<>();
        try {
            JsonArray arr = GSON.fromJson(jsonPartyNames, JsonArray.class);
            for (int i = 0; i < arr.size(); i++) {
                partyNames.add(arr.get(i).getAsString());
            }
        } catch (Exception ignored) {}
    }

    @Override
    protected void init() {
        super.init();
        panelX = (this.width - PANEL_WIDTH) / 2;
        panelY = (this.height - PANEL_HEIGHT) / 2;
    }

    @Override
    public void tick() {
        super.tick();

        // Check for status updates
        if (InsuranceStatusPayload.clientAllReady || InsuranceStatusPayload.clientCancelled) {
            InsuranceOpenPayload.clearClientState();
            InsuranceStatusPayload.clearClientState();
            this.onClose();
            return;
        }

        // Update party ready status
        String readyJson = InsuranceStatusPayload.clientReadyStatus;
        if (readyJson != null && !readyJson.equals("{}")) {
            try {
                JsonObject obj = GSON.fromJson(readyJson, JsonObject.class);
                partyReady.clear();
                for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                    partyReady.put(entry.getKey(), entry.getValue().getAsBoolean());
                }
            } catch (Exception ignored) {}
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        clickRects.clear();
        hoveredTooltipText = null;

        // Background overlay
        g.fill(0, 0, this.width, this.height, 0xAA000000);

        // Main panel
        UIHelper.drawPanel(g, panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT);

        int x = panelX + 8;
        int y = panelY + 8;

        // Title
        String title = "Dungeon Insurance \u2014 " + tierName;
        g.drawString(this.font, title, x, y, UIHelper.GOLD_BRIGHT, false);
        y += 14;

        // Divider
        g.fill(x, y, panelX + PANEL_WIDTH - 8, y + 1, UIHelper.GOLD_DARK);
        y += 6;

        // Left side: insurable slots
        int leftWidth = 200;
        int slotX = x;
        int slotY = y;

        // Section: Armor
        g.drawString(this.font, "Armor", slotX, slotY, UIHelper.GOLD_MID, false);
        slotY += 12;
        slotY = drawSlotRow(g, slotX, slotY, mouseX, mouseY,
                new String[]{"armor_head", "armor_chest", "armor_legs", "armor_feet"},
                new String[]{"Head", "Chest", "Legs", "Feet"});

        slotY += 6;

        // Section: Weapons
        g.drawString(this.font, "Weapons", slotX, slotY, UIHelper.GOLD_MID, false);
        slotY += 12;
        slotY = drawSlotRow(g, slotX, slotY, mouseX, mouseY,
                new String[]{"mainhand", "offhand"},
                new String[]{"Main", "Off"});

        slotY += 6;

        // Section: Accessories
        g.drawString(this.font, "Accessories", slotX, slotY, UIHelper.GOLD_MID, false);
        slotY += 12;
        String[] accSlots = {"accessory_BACK", "accessory_BELT", "accessory_HANDS_LEFT", "accessory_HANDS_RIGHT",
                "accessory_FEET", "accessory_NECKLACE", "accessory_RING_LEFT", "accessory_RING_RIGHT",
                "accessory_HEAD", "accessory_FACE"};
        String[] accLabels = {"Back", "Belt", "L.Hand", "R.Hand", "Feet", "Neck", "L.Ring", "R.Ring", "Head", "Face"};
        // Draw in two rows of 5
        slotY = drawSlotRow(g, slotX, slotY, mouseX, mouseY,
                Arrays.copyOfRange(accSlots, 0, 5), Arrays.copyOfRange(accLabels, 0, 5));
        slotY += 2;
        slotY = drawSlotRow(g, slotX, slotY, mouseX, mouseY,
                Arrays.copyOfRange(accSlots, 5, 10), Arrays.copyOfRange(accLabels, 5, 10));

        slotY += 6;

        // Section: Inventory (hotbar + main inventory)
        g.drawString(this.font, "Inventory", slotX, slotY, UIHelper.GOLD_MID, false);
        slotY += 12;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            // Hotbar (slots 0-8)
            List<String> hotbarSlots = new ArrayList<>();
            List<String> hotbarLabels = new ArrayList<>();
            for (int i = 0; i < 9; i++) {
                String key = "inv_" + i;
                if (slotCosts.containsKey(key)) {
                    hotbarSlots.add(key);
                    ItemStack item = mc.player.getInventory().getItem(i);
                    hotbarLabels.add(item.isEmpty() ? "?" : item.getHoverName().getString().substring(0, Math.min(4, item.getHoverName().getString().length())));
                }
            }
            if (!hotbarSlots.isEmpty()) {
                g.drawString(this.font, "Hotbar:", slotX + 2, slotY, 0xFF888888, false);
                slotY += 10;
                // Draw in rows of 9
                slotY = drawSlotRow(g, slotX, slotY, mouseX, mouseY,
                    hotbarSlots.toArray(new String[0]), hotbarLabels.toArray(new String[0]));
            }

            // Main inventory (slots 9-35)
            List<String> invSlots = new ArrayList<>();
            List<String> invLabels = new ArrayList<>();
            for (int i = 9; i < 36; i++) {
                String key = "inv_" + i;
                if (slotCosts.containsKey(key)) {
                    invSlots.add(key);
                    ItemStack item = mc.player.getInventory().getItem(i);
                    invLabels.add(item.isEmpty() ? "?" : item.getHoverName().getString().substring(0, Math.min(4, item.getHoverName().getString().length())));
                }
            }
            if (!invSlots.isEmpty()) {
                slotY += 2;
                g.drawString(this.font, "Main:", slotX + 2, slotY, 0xFF888888, false);
                slotY += 10;
                // Draw in rows of 9
                for (int row = 0; row < invSlots.size(); row += 9) {
                    int end = Math.min(row + 9, invSlots.size());
                    slotY = drawSlotRow(g, slotX, slotY, mouseX, mouseY,
                        invSlots.subList(row, end).toArray(new String[0]),
                        invLabels.subList(row, end).toArray(new String[0]));
                    slotY += 2;
                }
            }
        }

        // Right side: Party status
        int rightX = panelX + PANEL_WIDTH - 110;
        int rightY = panelY + 28;
        g.drawString(this.font, "Party", rightX, rightY, UIHelper.GOLD_MID, false);
        rightY += 14;

        for (String name : partyNames) {
            boolean ready = partyReady.getOrDefault(name, false);
            int dotColor = ready ? 0xFF44BF44 : UIHelper.GOLD_DARK;
            g.fill(rightX, rightY + 2, rightX + 6, rightY + 8, dotColor);
            g.drawString(this.font, name, rightX + 10, rightY, UIHelper.CREAM_TEXT, false);
            rightY += 13;
        }

        // Bottom: total cost + wallet on separate lines
        int bottomY = panelY + PANEL_HEIGHT - 62;
        g.fill(x, bottomY, panelX + PANEL_WIDTH - 8, bottomY + 1, UIHelper.GOLD_DARK);
        bottomY += 4;
        int totalCost = calculateTotalCost();
        int walletBalance = getWalletBalance();
        boolean canAfford = walletBalance >= totalCost;

        g.drawString(this.font, "Total Cost: " + totalCost + " MC",
                x, bottomY, canAfford ? UIHelper.XP_GREEN : 0xFFFF4444, false);
        String walletStr = "Wallet: " + walletBalance + " MC";
        int walletW = this.font.width(walletStr);
        g.drawString(this.font, walletStr,
                panelX + PANEL_WIDTH - 8 - walletW, bottomY, UIHelper.GOLD_MID, false);
        bottomY += 14;

        // Buttons
        int btnW = 90;
        int btnH = 18;
        int btnY = bottomY + 2;

        // [Insure & Ready]
        int readyBtnX = x;
        boolean readyHover = mouseX >= readyBtnX && mouseX <= readyBtnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH;
        UIHelper.drawButton(g, readyBtnX, btnY, btnW, btnH, readyHover, false);
        String readyLabel = selectedSlots.isEmpty() ? "Ready" : "Insure & Ready";
        int readyLabelX = readyBtnX + (btnW - this.font.width(readyLabel)) / 2;
        g.drawString(this.font, readyLabel, readyLabelX, btnY + 5, UIHelper.GOLD_BRIGHT, false);
        clickRects.add(new ClickRect(readyBtnX, btnY, btnW, btnH, "ready"));

        // [Skip Insurance]
        int skipBtnX = x + btnW + 8;
        boolean skipHover = mouseX >= skipBtnX && mouseX <= skipBtnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH;
        UIHelper.drawButton(g, skipBtnX, btnY, btnW, btnH, skipHover, false);
        String skipLabel = "Skip";
        int skipLabelX = skipBtnX + (btnW - this.font.width(skipLabel)) / 2;
        g.drawString(this.font, skipLabel, skipLabelX, btnY + 5, UIHelper.GOLD_MID, false);
        clickRects.add(new ClickRect(skipBtnX, btnY, btnW, btnH, "skip"));

        // [Cancel]
        int cancelBtnX = x + (btnW + 8) * 2;
        boolean cancelHover = mouseX >= cancelBtnX && mouseX <= cancelBtnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH;
        UIHelper.drawButton(g, cancelBtnX, btnY, btnW, btnH, cancelHover, false);
        String cancelLabel = "Cancel";
        int cancelLabelX = cancelBtnX + (btnW - this.font.width(cancelLabel)) / 2;
        g.drawString(this.font, cancelLabel, cancelLabelX, btnY + 5, 0xFFFF6666, false);
        clickRects.add(new ClickRect(cancelBtnX, btnY, btnW, btnH, "cancel"));

        // Draw hover tooltip last (on top of everything)
        if (hoveredTooltipText != null) {
            int tw = this.font.width(hoveredTooltipText);
            int tx = hoveredTooltipX + 12;
            int ty = hoveredTooltipY - 12;
            if (tx + tw + 6 > this.width) tx = hoveredTooltipX - tw - 12;
            g.fill(tx - 3, ty - 2, tx + tw + 3, ty + 11, 0xEE1A1A2E);
            g.fill(tx - 4, ty - 3, tx + tw + 4, ty - 2, 0xCC6644AA);
            g.fill(tx - 4, ty + 11, tx + tw + 4, ty + 12, 0xCC6644AA);
            g.fill(tx - 4, ty - 2, tx - 3, ty + 11, 0xCC6644AA);
            g.fill(tx + tw + 3, ty - 2, tx + tw + 4, ty + 11, 0xCC6644AA);
            g.drawString(this.font, hoveredTooltipText, tx, ty, 0xFFFFDD44, false);
        }
    }

    /** Hover tooltip state — set during slot rendering, drawn at end */
    private String hoveredTooltipText = null;
    private int hoveredTooltipX = 0, hoveredTooltipY = 0;

    private int drawSlotRow(GuiGraphics g, int startX, int startY, int mouseX, int mouseY,
                            String[] slotNames, String[] labels) {
        int maxX = panelX + PANEL_WIDTH - 12; // right edge with padding
        int x = startX;
        int y = startY;
        int slotsInRow = 0;

        for (int i = 0; i < slotNames.length; i++) {
            // Wrap to next row if we'd exceed panel width
            if (slotsInRow >= MAX_SLOTS_PER_ROW || (x + SLOT_SIZE > maxX && slotsInRow > 0)) {
                x = startX;
                y += SLOT_SIZE + SLOT_GAP + 10; // room for cost label
                slotsInRow = 0;
            }

            String slotName = slotNames[i];
            Integer cost = slotCosts.get(slotName);
            if (cost == null) {
                // No item in this slot — draw empty slot
                UIHelper.drawSlot(g, x, y, SLOT_SIZE, false, false);
                // Tiny label below
                String label = labels[i].length() > 3 ? labels[i].substring(0, 3) : labels[i];
                int labelW = this.font.width(label);
                g.drawString(this.font, label, x + (SLOT_SIZE - labelW) / 2, y + SLOT_SIZE + 1, UIHelper.GOLD_DARK, false);
                x += SLOT_SIZE + SLOT_GAP;
                slotsInRow++;
                continue;
            }

            boolean selected = selectedSlots.contains(slotName);
            boolean hovered = mouseX >= x && mouseX <= x + SLOT_SIZE && mouseY >= y && mouseY <= y + SLOT_SIZE;

            // Draw slot background
            UIHelper.drawSlot(g, x, y, SLOT_SIZE, hovered, selected);

            // Draw item icon
            ItemStack stack = getClientItemForSlot(slotName);
            if (!stack.isEmpty()) {
                g.renderItem(stack, x + 2, y + 2);
            }

            // Checkmark if selected
            if (selected) {
                g.fill(x + SLOT_SIZE - 6, y + 1, x + SLOT_SIZE - 1, y + 6, 0xFF44BF44);
            }

            // Cost label below slot (compact, k-suffix for 1000+)
            String costStr = formatCost(cost);
            int costColor = selected ? UIHelper.XP_GREEN : UIHelper.GOLD_MID;
            int costW = this.font.width(costStr);
            g.drawString(this.font, costStr, x + (SLOT_SIZE - costW) / 2, y + SLOT_SIZE + 1, costColor, false);

            // Hover tooltip
            if (hovered && !stack.isEmpty()) {
                hoveredTooltipText = stack.getHoverName().getString() + " — " + cost + " MC";
                hoveredTooltipX = mouseX;
                hoveredTooltipY = mouseY;
            }

            clickRects.add(new ClickRect(x, y, SLOT_SIZE, SLOT_SIZE, "toggle:" + slotName));

            x += SLOT_SIZE + SLOT_GAP;
            slotsInRow++;
        }
        return y + SLOT_SIZE + 12;
    }

    /** Compact cost label: "800", "1.2k", "12k", "120k". Keeps label within slot pitch. */
    private static String formatCost(int cost) {
        if (cost < 1000) return Integer.toString(cost);
        if (cost < 10000) {
            int tenths = (cost + 50) / 100; // round to nearest 0.1k
            int whole = tenths / 10;
            int frac = tenths % 10;
            return frac == 0 ? whole + "k" : whole + "." + frac + "k";
        }
        return ((cost + 500) / 1000) + "k";
    }

    private ItemStack getClientItemForSlot(String slotName) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return ItemStack.EMPTY;

        if (slotName.startsWith("armor_")) {
            String name = slotName.substring(6);
            EquipmentSlot slot = EquipmentSlot.byName(name);
            return mc.player.getItemBySlot(slot);
        } else if (slotName.equals("mainhand")) {
            return mc.player.getMainHandItem();
        } else if (slotName.equals("offhand")) {
            return mc.player.getOffhandItem();
        } else if (slotName.startsWith("accessory_")) {
            // Accessory items aren't directly accessible client-side from player inventory
            // The server sent costs for equipped accessories — we can show a placeholder or the synced item
            // For now, use the AccessoryPayload client sync data
            String accName = slotName.substring(10);
            Map<String, String> equipped = com.ultra.megamod.feature.relics.network.AccessoryPayload.AccessorySyncPayload.clientEquipped;
            String itemId = equipped.get(accName);
            if (itemId != null && !itemId.isEmpty()) {
                try {
                    net.minecraft.resources.Identifier id = net.minecraft.resources.Identifier.parse(itemId);
                    net.minecraft.world.item.Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(id);
                    return new ItemStack(item);
                } catch (Exception e) {
                    return ItemStack.EMPTY;
                }
            }
        } else if (slotName.startsWith("inv_")) {
            try {
                int slot = Integer.parseInt(slotName.substring(4));
                if (slot >= 0 && slot < mc.player.getInventory().getContainerSize()) {
                    return mc.player.getInventory().getItem(slot);
                }
            } catch (NumberFormatException ignored) {}
        }
        return ItemStack.EMPTY;
    }

    private int calculateTotalCost() {
        int total = 0;
        for (String slot : selectedSlots) {
            Integer cost = slotCosts.get(slot);
            if (cost != null) total += cost;
        }
        return total;
    }

    private int getWalletBalance() {
        return com.ultra.megamod.feature.economy.network.PlayerInfoSyncPayload.clientWallet;
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean consumed) {
        if (consumed) return super.mouseClicked(event, true);
        int mx = (int) event.x();
        int my = (int) event.y();
        if (!submitted) {
            for (ClickRect rect : clickRects) {
                if (mx >= rect.x && mx <= rect.x + rect.w &&
                        my >= rect.y && my <= rect.y + rect.h) {
                    handleClick(rect.action);
                    return true;
                }
            }
        }
        return super.mouseClicked(event, consumed);
    }

    private void handleClick(String action) {
        if (action.startsWith("toggle:")) {
            String slotName = action.substring(7);
            if (selectedSlots.contains(slotName)) {
                selectedSlots.remove(slotName);
            } else {
                selectedSlots.add(slotName);
            }
        } else if (action.equals("ready")) {
            sendReady(false);
        } else if (action.equals("skip")) {
            selectedSlots.clear();
            sendReady(false);
        } else if (action.equals("cancel")) {
            sendReady(true);
        }
    }

    private void sendReady(boolean cancelled) {
        if (submitted) return;
        submitted = true;

        JsonArray arr = new JsonArray();
        for (String slot : selectedSlots) {
            arr.add(slot);
        }

        ClientPacketDistributor.sendToServer(
                new InsuranceReadyPayload(arr.toString(), cancelled),
                new CustomPacketPayload[0]);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        InsuranceOpenPayload.clearClientState();
        InsuranceStatusPayload.clearClientState();
        super.onClose();
    }

    private record ClickRect(int x, int y, int w, int h, String action) {}
}

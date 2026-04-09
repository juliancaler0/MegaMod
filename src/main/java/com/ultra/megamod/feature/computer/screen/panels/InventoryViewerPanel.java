package com.ultra.megamod.feature.computer.screen.panels;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ultra.megamod.feature.computer.network.ComputerActionPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class InventoryViewerPanel {

    // Visual style constants
    private static final int BG_COLOR = 0xFF0D1117;
    private static final int HEADER_COLOR = 0xFF161B22;
    private static final int BORDER_COLOR = 0xFF30363D;
    private static final int TEXT_COLOR = 0xFFE6EDF3;
    private static final int LABEL_COLOR = 0xFF8B949E;
    private static final int ACCENT_BLUE = 0xFF58A6FF;
    private static final int SUCCESS_GREEN = 0xFF3FB950;
    private static final int ERROR_RED = 0xFFF85149;
    private static final int SLOT_BG = 0xFF21262D;
    private static final int SELECTED_SLOT = 0xFF388BFD;

    private static final int SLOT_SIZE = 20;
    private static final int SLOT_GAP = 2;
    private static final int GRID_COLS = 9;
    private static final int ARMOR_SLOT_COUNT = 4;

    private final Font font;
    private List<String> playerNames = new ArrayList<>();
    private List<String> playerUUIDs = new ArrayList<>();
    private int selectedPlayerIndex = 0;
    private int selectedSlot = -1;
    private List<InvSlotEntry> slots = new ArrayList<>();
    private String viewedPlayerName = "";
    private int playerListScroll = 0;
    private int detailScroll = 0;
    private String statusMessage = "";
    private long statusMessageTime = 0;
    private boolean awaitingCountInput = false;
    private String countInputBuffer = "";
    private boolean awaitingGiveInput = false;
    private String giveItemBuffer = "";
    private String giveCountBuffer = "1";
    private boolean giveItemFocused = true;

    public record InvSlotEntry(int slot, String itemId, String displayName, int count,
                               float durability, float maxDurability, String enchants,
                               String customData) {}

    public InventoryViewerPanel(Font font) {
        this.font = font;
    }

    public void render(GuiGraphics g, int mouseX, int mouseY, int left, int top, int right, int bottom) {
        int panelW = right - left;
        int panelH = bottom - top;

        // --- Player selector bar at top ---
        int selectorH = 28;
        g.fill(left, top, right, top + selectorH, HEADER_COLOR);
        g.fill(left, top + selectorH - 1, right, top + selectorH, BORDER_COLOR);

        refreshPlayerList();

        g.drawString(font, "Player:", left + 4, top + 10, LABEL_COLOR, false);

        // Left/right arrows for player selection
        int arrowY = top + 6;
        int arrowSize = 16;
        int arrowLeftX = left + 44;
        boolean leftHover = mouseX >= arrowLeftX && mouseX < arrowLeftX + arrowSize && mouseY >= arrowY && mouseY < arrowY + arrowSize;
        drawSmallButton(g, arrowLeftX, arrowY, arrowSize, arrowSize, "<", leftHover, ACCENT_BLUE);

        int nameBoxX = arrowLeftX + arrowSize + 2;
        int nameBoxW = 120;
        g.fill(nameBoxX, arrowY, nameBoxX + nameBoxW, arrowY + arrowSize, SLOT_BG);
        g.fill(nameBoxX, arrowY, nameBoxX + nameBoxW, arrowY + 1, BORDER_COLOR);
        g.fill(nameBoxX, arrowY + arrowSize - 1, nameBoxX + nameBoxW, arrowY + arrowSize, BORDER_COLOR);
        g.fill(nameBoxX, arrowY, nameBoxX + 1, arrowY + arrowSize, BORDER_COLOR);
        g.fill(nameBoxX + nameBoxW - 1, arrowY, nameBoxX + nameBoxW, arrowY + arrowSize, BORDER_COLOR);

        String displayedPlayer = playerNames.isEmpty() ? "No players" : playerNames.get(selectedPlayerIndex);
        int nameW = font.width(displayedPlayer);
        g.drawString(font, displayedPlayer, nameBoxX + (nameBoxW - nameW) / 2, arrowY + 4, TEXT_COLOR, false);

        int arrowRightX = nameBoxX + nameBoxW + 2;
        boolean rightHover = mouseX >= arrowRightX && mouseX < arrowRightX + arrowSize && mouseY >= arrowY && mouseY < arrowY + arrowSize;
        drawSmallButton(g, arrowRightX, arrowY, arrowSize, arrowSize, ">", rightHover, ACCENT_BLUE);

        // Refresh button
        int refreshX = arrowRightX + arrowSize + 10;
        int refreshW = 56;
        boolean refreshHover = mouseX >= refreshX && mouseX < refreshX + refreshW && mouseY >= arrowY && mouseY < arrowY + arrowSize;
        drawSmallButton(g, refreshX, arrowY, refreshW, arrowSize, "Refresh", refreshHover, ACCENT_BLUE);

        // Global action buttons
        int globalBtnY = arrowY;
        int clearAllX = right - 176;
        int clearAllW = 84;
        boolean clearAllHover = mouseX >= clearAllX && mouseX < clearAllX + clearAllW && mouseY >= globalBtnY && mouseY < globalBtnY + arrowSize;
        drawSmallButton(g, clearAllX, globalBtnY, clearAllW, arrowSize, "Clear Inv", clearAllHover, ERROR_RED);

        int copyAllX = right - 86;
        int copyAllW = 82;
        boolean copyAllHover = mouseX >= copyAllX && mouseX < copyAllX + copyAllW && mouseY >= globalBtnY && mouseY < globalBtnY + arrowSize;
        drawSmallButton(g, copyAllX, globalBtnY, copyAllW, arrowSize, "Copy All", copyAllHover, SUCCESS_GREEN);

        // Sort Inv button
        int sortX = clearAllX - 58;
        int sortW = 52;
        boolean sortHover = mouseX >= sortX && mouseX < sortX + sortW && mouseY >= globalBtnY && mouseY < globalBtnY + arrowSize;
        drawSmallButton(g, sortX, globalBtnY, sortW, arrowSize, "Sort", sortHover, ACCENT_BLUE);

        // Give Item button
        int giveX = sortX - 62;
        int giveW = 56;
        boolean giveHover = mouseX >= giveX && mouseX < giveX + giveW && mouseY >= globalBtnY && mouseY < globalBtnY + arrowSize;
        drawSmallButton(g, giveX, globalBtnY, giveW, arrowSize, "Give...", giveHover, 0xFFA371F7);

        // --- Main content area ---
        int contentTop = top + selectorH + 2;
        int contentH = bottom - contentTop;

        // Left panel: inventory grid
        int gridPanelW = (SLOT_SIZE + SLOT_GAP) * GRID_COLS + SLOT_GAP + 8;
        int gridLeft = left + 4;
        int gridTop = contentTop + 4;

        // Section: Main Inventory (slots 0-35, displayed as 4 rows of 9)
        g.drawString(font, "Main Inventory", gridLeft, gridTop, ACCENT_BLUE, false);
        gridTop += 12;

        // Hotbar row (slots 0-8)
        for (int col = 0; col < GRID_COLS; col++) {
            int slotIdx = col;
            int sx = gridLeft + col * (SLOT_SIZE + SLOT_GAP);
            int sy = gridTop + 3 * (SLOT_SIZE + SLOT_GAP) + 6; // Below main inv, with gap
            renderSlot(g, sx, sy, slotIdx, mouseX, mouseY);
        }

        // Main inventory rows (slots 9-35)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                int slotIdx = 9 + row * GRID_COLS + col;
                int sx = gridLeft + col * (SLOT_SIZE + SLOT_GAP);
                int sy = gridTop + row * (SLOT_SIZE + SLOT_GAP);
                renderSlot(g, sx, sy, slotIdx, mouseX, mouseY);
            }
        }

        // Armor + Offhand section
        int armorTop = gridTop + 4 * (SLOT_SIZE + SLOT_GAP) + 14;
        g.drawString(font, "Armor", gridLeft, armorTop, ACCENT_BLUE, false);
        armorTop += 12;

        // Armor slots 36-39 (feet, legs, chest, head) - render head first visually
        String[] armorLabels = {"Feet", "Legs", "Chest", "Head"};
        for (int i = 0; i < ARMOR_SLOT_COUNT; i++) {
            int slotIdx = 36 + (3 - i); // Reverse: head(39) at top, feet(36) at bottom
            int sx = gridLeft + i * (SLOT_SIZE + SLOT_GAP + 16);
            renderSlot(g, sx, armorTop, slotIdx, mouseX, mouseY);
            g.drawString(font, armorLabels[3 - i], sx + SLOT_SIZE + 2, armorTop + 6, LABEL_COLOR, false);
        }

        // Offhand
        int offhandX = gridLeft + 4 * (SLOT_SIZE + SLOT_GAP + 16) + 10;
        g.drawString(font, "Off", offhandX, armorTop - 12, ACCENT_BLUE, false);
        renderSlot(g, offhandX, armorTop, 40, mouseX, mouseY);

        // --- Right panel: Detail view ---
        int detailLeft = gridLeft + gridPanelW + 12;
        int detailTop = contentTop + 4;
        int detailW = right - detailLeft - 4;
        int detailH = bottom - detailTop - 4;

        g.fill(detailLeft, detailTop, detailLeft + detailW, detailTop + detailH, HEADER_COLOR);
        g.fill(detailLeft, detailTop, detailLeft + detailW, detailTop + 1, BORDER_COLOR);
        g.fill(detailLeft, detailTop + detailH - 1, detailLeft + detailW, detailTop + detailH, BORDER_COLOR);
        g.fill(detailLeft, detailTop, detailLeft + 1, detailTop + detailH, BORDER_COLOR);
        g.fill(detailLeft + detailW - 1, detailTop, detailLeft + detailW, detailTop + detailH, BORDER_COLOR);

        if (selectedSlot < 0 || selectedSlot > 40) {
            g.drawString(font, "Select a slot to", detailLeft + 8, detailTop + detailH / 2 - 10, LABEL_COLOR, false);
            g.drawString(font, "view details", detailLeft + 8, detailTop + detailH / 2 + 2, LABEL_COLOR, false);
        } else {
            renderDetailPanel(g, mouseX, mouseY, detailLeft, detailTop, detailW, detailH);
        }

        // Status message
        if (!statusMessage.isEmpty() && System.currentTimeMillis() - statusMessageTime < 3000) {
            int msgW = font.width(statusMessage);
            int msgX = left + (panelW - msgW) / 2;
            int msgY = bottom - 14;
            g.fill(msgX - 4, msgY - 2, msgX + msgW + 4, msgY + 11, 0xCC000000);
            int msgColor = statusMessage.startsWith("Error") ? ERROR_RED : SUCCESS_GREEN;
            g.drawString(font, statusMessage, msgX, msgY, msgColor, false);
        }

        // Count input overlay
        if (awaitingCountInput) {
            renderCountInputOverlay(g, mouseX, mouseY, left, top, right, bottom);
        }

        // Give item input overlay
        if (awaitingGiveInput) {
            renderGiveItemOverlay(g, mouseX, mouseY, left, top, right, bottom);
        }
    }

    private void renderSlot(GuiGraphics g, int sx, int sy, int slotIdx, int mouseX, int mouseY) {
        boolean isSelected = selectedSlot == slotIdx;
        boolean isHovered = mouseX >= sx && mouseX < sx + SLOT_SIZE && mouseY >= sy && mouseY < sy + SLOT_SIZE;

        int bgColor = isSelected ? SELECTED_SLOT : (isHovered ? 0xFF2D333B : SLOT_BG);
        g.fill(sx, sy, sx + SLOT_SIZE, sy + SLOT_SIZE, bgColor);
        g.fill(sx, sy, sx + SLOT_SIZE, sy + 1, BORDER_COLOR);
        g.fill(sx, sy + SLOT_SIZE - 1, sx + SLOT_SIZE, sy + SLOT_SIZE, BORDER_COLOR);
        g.fill(sx, sy, sx + 1, sy + SLOT_SIZE, BORDER_COLOR);
        g.fill(sx + SLOT_SIZE - 1, sy, sx + SLOT_SIZE, sy + SLOT_SIZE, BORDER_COLOR);

        InvSlotEntry entry = getSlotEntry(slotIdx);
        if (entry != null && !"minecraft:air".equals(entry.itemId) && entry.count > 0) {
            // Render item icon
            try {
                Item item = BuiltInRegistries.ITEM.getValue(net.minecraft.resources.Identifier.parse(entry.itemId));
                g.renderItem(new ItemStack((net.minecraft.world.level.ItemLike) item), sx + 2, sy + 2);
            } catch (Exception e) {
                // Fallback: draw a colored square
                g.fill(sx + 3, sy + 3, sx + SLOT_SIZE - 3, sy + SLOT_SIZE - 3, ACCENT_BLUE);
            }

            // Draw count badge if > 1
            if (entry.count > 1) {
                String countStr = String.valueOf(entry.count);
                int cw = font.width(countStr);
                g.drawString(font, countStr, sx + SLOT_SIZE - cw - 1, sy + SLOT_SIZE - 9, TEXT_COLOR, true);
            }

            // Durability bar
            if (entry.maxDurability > 0) {
                float pct = 1.0f - (entry.durability / entry.maxDurability);
                if (pct > 0.0f && pct < 1.0f) {
                    int barW = (int) (pct * (SLOT_SIZE - 4));
                    int barColor;
                    if (pct > 0.6f) barColor = SUCCESS_GREEN;
                    else if (pct > 0.3f) barColor = 0xFFD29922;
                    else barColor = ERROR_RED;
                    g.fill(sx + 2, sy + SLOT_SIZE - 3, sx + 2 + barW, sy + SLOT_SIZE - 1, barColor);
                }
            }
        }

        // Slot number tooltip on hover
        if (isHovered) {
            String slotLabel = getSlotLabel(slotIdx);
            int labelW = font.width(slotLabel);
            int tipX = mouseX + 8;
            int tipY = mouseY - 12;
            g.fill(tipX - 2, tipY - 1, tipX + labelW + 2, tipY + 10, 0xEE000000);
            g.drawString(font, slotLabel, tipX, tipY, TEXT_COLOR, false);
        }
    }

    private void renderDetailPanel(GuiGraphics g, int mouseX, int mouseY, int dx, int dy, int dw, int dh) {
        InvSlotEntry entry = getSlotEntry(selectedSlot);

        int px = dx + 6;
        int py = dy + 6;

        g.drawString(font, "Slot " + selectedSlot + " - " + getSlotLabel(selectedSlot), px, py, ACCENT_BLUE, false);
        py += 14;

        if (entry == null || "minecraft:air".equals(entry.itemId) || entry.count <= 0) {
            g.drawString(font, "Empty slot", px, py, LABEL_COLOR, false);
            return;
        }

        // Item icon large area
        try {
            Item item = BuiltInRegistries.ITEM.getValue(net.minecraft.resources.Identifier.parse(entry.itemId));
            g.renderItem(new ItemStack((net.minecraft.world.level.ItemLike) item), px, py);
        } catch (Exception e) {
            g.fill(px, py, px + 16, py + 16, ACCENT_BLUE);
        }

        // Item name
        String name = entry.displayName;
        if (name.length() > 28) name = name.substring(0, 28) + "...";
        g.drawString(font, name, px + 20, py, TEXT_COLOR, false);

        // Item ID
        String id = entry.itemId;
        if (id.length() > 30) id = id.substring(0, 30) + "...";
        g.drawString(font, id, px + 20, py + 10, LABEL_COLOR, false);
        py += 24;

        // Count
        g.drawString(font, "Count: " + entry.count, px, py, TEXT_COLOR, false);
        py += 12;

        // Durability
        if (entry.maxDurability > 0) {
            int remaining = (int) (entry.maxDurability - entry.durability);
            g.drawString(font, "Durability: " + remaining + " / " + (int) entry.maxDurability, px, py, TEXT_COLOR, false);
            py += 4;
            // Durability bar
            float pct = 1.0f - (entry.durability / entry.maxDurability);
            int barW = Math.min(dw - 16, 120);
            g.fill(px, py, px + barW, py + 4, SLOT_BG);
            int fillW = (int) (pct * barW);
            int barColor = pct > 0.6f ? SUCCESS_GREEN : (pct > 0.3f ? 0xFFD29922 : ERROR_RED);
            g.fill(px, py, px + fillW, py + 4, barColor);
            py += 8;
        }

        // Enchantments
        if (entry.enchants != null && !entry.enchants.isEmpty()) {
            g.drawString(font, "Enchantments:", px, py, 0xFFA371F7, false);
            py += 11;
            String[] enchParts = entry.enchants.split(", ");
            for (String ench : enchParts) {
                if (py + 10 > dy + dh - 50) {
                    g.drawString(font, "...", px + 4, py, LABEL_COLOR, false);
                    py += 10;
                    break;
                }
                g.drawString(font, "  " + ench, px, py, 0xFFC9D1D9, false);
                py += 10;
            }
        }

        // Custom data
        if (entry.customData != null && !entry.customData.isEmpty()) {
            g.drawString(font, "Custom Data:", px, py, 0xFFD29922, false);
            py += 11;
            // Split long custom data into lines
            String cd = entry.customData;
            while (!cd.isEmpty() && py + 10 < dy + dh - 50) {
                int maxChars = Math.max(10, (dw - 20) / 6);
                if (cd.length() <= maxChars) {
                    g.drawString(font, "  " + cd, px, py, LABEL_COLOR, false);
                    py += 10;
                    cd = "";
                } else {
                    int breakAt = cd.lastIndexOf(',', maxChars);
                    if (breakAt <= 0) breakAt = maxChars;
                    g.drawString(font, "  " + cd.substring(0, breakAt + 1), px, py, LABEL_COLOR, false);
                    cd = cd.substring(breakAt + 1).trim();
                    py += 10;
                }
            }
        }

        // --- Action buttons at bottom of detail panel ---
        int btnY = dy + dh - 40;
        int btnH = 16;
        int btnGap = 4;

        // Delete button
        int deleteBtnX = px;
        int deleteBtnW = 50;
        boolean deleteHover = mouseX >= deleteBtnX && mouseX < deleteBtnX + deleteBtnW && mouseY >= btnY && mouseY < btnY + btnH;
        drawSmallButton(g, deleteBtnX, btnY, deleteBtnW, btnH, "Delete", deleteHover, ERROR_RED);

        // Copy to Me button
        int copyBtnX = deleteBtnX + deleteBtnW + btnGap;
        int copyBtnW = 68;
        boolean copyHover = mouseX >= copyBtnX && mouseX < copyBtnX + copyBtnW && mouseY >= btnY && mouseY < btnY + btnH;
        drawSmallButton(g, copyBtnX, btnY, copyBtnW, btnH, "Copy to Me", copyHover, SUCCESS_GREEN);

        // Set Count button
        int countBtnX = copyBtnX + copyBtnW + btnGap;
        int countBtnW = 64;
        boolean countHover = mouseX >= countBtnX && mouseX < countBtnX + countBtnW && mouseY >= btnY && mouseY < btnY + btnH;
        drawSmallButton(g, countBtnX, btnY, countBtnW, btnH, "Set Count", countHover, ACCENT_BLUE);

        // Repair button (only if item is damageable)
        if (entry.maxDurability > 0) {
            int repairBtnX = countBtnX + countBtnW + btnGap;
            int repairBtnW = 52;
            boolean repairHover = mouseX >= repairBtnX && mouseX < repairBtnX + repairBtnW && mouseY >= btnY && mouseY < btnY + btnH;
            drawSmallButton(g, repairBtnX, btnY, repairBtnW, btnH, "Repair", repairHover, 0xFFA371F7);
        }
    }

    private void renderCountInputOverlay(GuiGraphics g, int mouseX, int mouseY, int left, int top, int right, int bottom) {
        int centerX = (left + right) / 2;
        int centerY = (top + bottom) / 2;
        int boxW = 160;
        int boxH = 70;
        int bx = centerX - boxW / 2;
        int by = centerY - boxH / 2;

        // Dim background
        g.fill(left, top, right, bottom, 0x88000000);

        // Dialog box
        g.fill(bx, by, bx + boxW, by + boxH, HEADER_COLOR);
        g.fill(bx, by, bx + boxW, by + 1, ACCENT_BLUE);
        g.fill(bx, by + boxH - 1, bx + boxW, by + boxH, BORDER_COLOR);
        g.fill(bx, by, bx + 1, by + boxH, BORDER_COLOR);
        g.fill(bx + boxW - 1, by, bx + boxW, by + boxH, BORDER_COLOR);

        g.drawString(font, "Enter new count:", bx + 8, by + 8, TEXT_COLOR, false);

        // Input field
        int inputX = bx + 8;
        int inputY = by + 24;
        int inputW = boxW - 16;
        int inputH = 16;
        g.fill(inputX, inputY, inputX + inputW, inputY + inputH, SLOT_BG);
        g.fill(inputX, inputY, inputX + inputW, inputY + 1, BORDER_COLOR);
        g.fill(inputX, inputY + inputH - 1, inputX + inputW, inputY + inputH, BORDER_COLOR);
        g.fill(inputX, inputY, inputX + 1, inputY + inputH, BORDER_COLOR);
        g.fill(inputX + inputW - 1, inputY, inputX + inputW, inputY + inputH, BORDER_COLOR);

        String displayText = countInputBuffer + "_";
        g.drawString(font, displayText, inputX + 4, inputY + 4, TEXT_COLOR, false);

        // OK and Cancel buttons
        int okX = bx + 8;
        int okW = 60;
        int cancelX = bx + boxW - 68;
        int cancelW = 60;
        int btnBY = by + boxH - 22;
        int btnBH = 16;

        boolean okHover = mouseX >= okX && mouseX < okX + okW && mouseY >= btnBY && mouseY < btnBY + btnBH;
        drawSmallButton(g, okX, btnBY, okW, btnBH, "OK", okHover, SUCCESS_GREEN);

        boolean cancelHover = mouseX >= cancelX && mouseX < cancelX + cancelW && mouseY >= btnBY && mouseY < btnBY + btnBH;
        drawSmallButton(g, cancelX, btnBY, cancelW, btnBH, "Cancel", cancelHover, ERROR_RED);
    }

    private void renderGiveItemOverlay(GuiGraphics g, int mouseX, int mouseY, int left, int top, int right, int bottom) {
        int centerX = (left + right) / 2;
        int centerY = (top + bottom) / 2;
        int boxW = 200;
        int boxH = 110;
        int bx = centerX - boxW / 2;
        int by = centerY - boxH / 2;

        // Dim background
        g.fill(left, top, right, bottom, 0x88000000);

        // Dialog box
        g.fill(bx, by, bx + boxW, by + boxH, HEADER_COLOR);
        g.fill(bx, by, bx + boxW, by + 1, 0xFFA371F7);
        g.fill(bx, by + boxH - 1, bx + boxW, by + boxH, BORDER_COLOR);
        g.fill(bx, by, bx + 1, by + boxH, BORDER_COLOR);
        g.fill(bx + boxW - 1, by, bx + boxW, by + boxH, BORDER_COLOR);

        g.drawString(font, "Give Item to Player", bx + 8, by + 8, TEXT_COLOR, false);

        // Item ID field
        g.drawString(font, "Item ID:", bx + 8, by + 24, LABEL_COLOR, false);
        int itemInputX = bx + 8;
        int itemInputY = by + 34;
        int inputW = boxW - 16;
        int inputH = 16;
        int itemBorder = giveItemFocused ? 0xFFA371F7 : BORDER_COLOR;
        g.fill(itemInputX, itemInputY, itemInputX + inputW, itemInputY + inputH, SLOT_BG);
        g.fill(itemInputX, itemInputY, itemInputX + inputW, itemInputY + 1, itemBorder);
        g.fill(itemInputX, itemInputY + inputH - 1, itemInputX + inputW, itemInputY + inputH, itemBorder);
        g.fill(itemInputX, itemInputY, itemInputX + 1, itemInputY + inputH, itemBorder);
        g.fill(itemInputX + inputW - 1, itemInputY, itemInputX + inputW, itemInputY + inputH, itemBorder);

        String itemDisplay = giveItemBuffer + (giveItemFocused ? "_" : "");
        g.drawString(font, itemDisplay, itemInputX + 4, itemInputY + 4, TEXT_COLOR, false);

        // Count field
        g.drawString(font, "Count:", bx + 8, by + 56, LABEL_COLOR, false);
        int countInputX = bx + 8;
        int countInputY = by + 66;
        int countInputW = 60;
        int countBorder = !giveItemFocused ? 0xFFA371F7 : BORDER_COLOR;
        g.fill(countInputX, countInputY, countInputX + countInputW, countInputY + inputH, SLOT_BG);
        g.fill(countInputX, countInputY, countInputX + countInputW, countInputY + 1, countBorder);
        g.fill(countInputX, countInputY + inputH - 1, countInputX + countInputW, countInputY + inputH, countBorder);
        g.fill(countInputX, countInputY, countInputX + 1, countInputY + inputH, countBorder);
        g.fill(countInputX + countInputW - 1, countInputY, countInputX + countInputW, countInputY + inputH, countBorder);

        String countDisplay = giveCountBuffer + (!giveItemFocused ? "_" : "");
        g.drawString(font, countDisplay, countInputX + 4, countInputY + 4, TEXT_COLOR, false);

        // Hint
        g.drawString(font, "(Tab to switch fields)", bx + 74, by + 70, LABEL_COLOR, false);

        // OK and Cancel buttons
        int okX = bx + 8;
        int okW = 60;
        int cancelX = bx + boxW - 68;
        int cancelW = 60;
        int btnBY = by + boxH - 22;
        int btnBH = 16;

        boolean okHover = mouseX >= okX && mouseX < okX + okW && mouseY >= btnBY && mouseY < btnBY + btnBH;
        drawSmallButton(g, okX, btnBY, okW, btnBH, "Give", okHover, SUCCESS_GREEN);

        boolean cancelHover = mouseX >= cancelX && mouseX < cancelX + cancelW && mouseY >= btnBY && mouseY < btnBY + btnBH;
        drawSmallButton(g, cancelX, btnBY, cancelW, btnBH, "Cancel", cancelHover, ERROR_RED);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button, int left, int top, int right, int bottom) {
        int mx = (int) mouseX;
        int my = (int) mouseY;

        // Handle count input overlay first
        if (awaitingCountInput) {
            int centerX = (left + right) / 2;
            int centerY = (top + bottom) / 2;
            int boxW = 160;
            int boxH = 70;
            int bx = centerX - boxW / 2;
            int by = centerY - boxH / 2;

            int okX = bx + 8;
            int cancelX = bx + boxW - 68;
            int btnBY = by + boxH - 22;
            int btnBH = 16;

            if (mx >= okX && mx < okX + 60 && my >= btnBY && my < btnBY + btnBH) {
                // OK pressed
                submitCountInput();
                return true;
            }
            if (mx >= cancelX && mx < cancelX + 60 && my >= btnBY && my < btnBY + btnBH) {
                // Cancel
                awaitingCountInput = false;
                countInputBuffer = "";
                return true;
            }
            return true; // Consume all clicks while overlay is open
        }

        // Handle give item input overlay
        if (awaitingGiveInput) {
            int centerX = (left + right) / 2;
            int centerY = (top + bottom) / 2;
            int boxW = 200;
            int boxH = 110;
            int bx = centerX - boxW / 2;
            int by = centerY - boxH / 2;

            // Click on item ID field to focus it
            int itemInputX = bx + 8;
            int itemInputY = by + 34;
            int inputW = boxW - 16;
            int inputH = 16;
            if (mx >= itemInputX && mx < itemInputX + inputW && my >= itemInputY && my < itemInputY + inputH) {
                giveItemFocused = true;
                return true;
            }

            // Click on count field to focus it
            int countInputX = bx + 8;
            int countInputY = by + 66;
            int countInputW = 60;
            if (mx >= countInputX && mx < countInputX + countInputW && my >= countInputY && my < countInputY + inputH) {
                giveItemFocused = false;
                return true;
            }

            int okX = bx + 8;
            int cancelX = bx + boxW - 68;
            int btnBY = by + boxH - 22;
            int btnBH = 16;

            if (mx >= okX && mx < okX + 60 && my >= btnBY && my < btnBY + btnBH) {
                // Give pressed
                submitGiveInput();
                return true;
            }
            if (mx >= cancelX && mx < cancelX + 60 && my >= btnBY && my < btnBY + btnBH) {
                // Cancel
                awaitingGiveInput = false;
                giveItemBuffer = "";
                giveCountBuffer = "1";
                return true;
            }
            return true; // Consume all clicks while overlay is open
        }

        int selectorH = 28;
        int arrowY = top + 6;
        int arrowSize = 16;
        int arrowLeftX = left + 44;

        // Left arrow
        if (mx >= arrowLeftX && mx < arrowLeftX + arrowSize && my >= arrowY && my < arrowY + arrowSize) {
            if (!playerNames.isEmpty()) {
                selectedPlayerIndex = (selectedPlayerIndex - 1 + playerNames.size()) % playerNames.size();
                selectedSlot = -1;
                slots.clear();
                requestData();
            }
            return true;
        }

        // Right arrow
        int nameBoxX = arrowLeftX + arrowSize + 2;
        int nameBoxW = 120;
        int arrowRightX = nameBoxX + nameBoxW + 2;
        if (mx >= arrowRightX && mx < arrowRightX + arrowSize && my >= arrowY && my < arrowY + arrowSize) {
            if (!playerNames.isEmpty()) {
                selectedPlayerIndex = (selectedPlayerIndex + 1) % playerNames.size();
                selectedSlot = -1;
                slots.clear();
                requestData();
            }
            return true;
        }

        // Refresh button
        int refreshX = arrowRightX + arrowSize + 10;
        int refreshW = 56;
        if (mx >= refreshX && mx < refreshX + refreshW && my >= arrowY && my < arrowY + arrowSize) {
            requestData();
            return true;
        }

        // Clear Inventory global button
        int clearAllX = right - 176;
        int clearAllW = 84;
        if (mx >= clearAllX && mx < clearAllX + clearAllW && my >= arrowY && my < arrowY + arrowSize) {
            if (!playerUUIDs.isEmpty() && selectedPlayerIndex < playerUUIDs.size()) {
                String uuid = playerUUIDs.get(selectedPlayerIndex);
                ClientPacketDistributor.sendToServer(
                    (CustomPacketPayload) new ComputerActionPayload("inv_view_clear", uuid),
                    (CustomPacketPayload[]) new CustomPacketPayload[0]);
                setStatus("Clearing inventory...");
            }
            return true;
        }

        // Copy All global button
        int copyAllX = right - 86;
        int copyAllW = 82;
        if (mx >= copyAllX && mx < copyAllX + copyAllW && my >= arrowY && my < arrowY + arrowSize) {
            if (!playerUUIDs.isEmpty() && selectedPlayerIndex < playerUUIDs.size()) {
                String uuid = playerUUIDs.get(selectedPlayerIndex);
                ClientPacketDistributor.sendToServer(
                    (CustomPacketPayload) new ComputerActionPayload("inv_view_copy_all", uuid),
                    (CustomPacketPayload[]) new CustomPacketPayload[0]);
                setStatus("Copying all items...");
            }
            return true;
        }

        // Sort Inv global button
        int sortX = clearAllX - 58;
        int sortW = 52;
        if (mx >= sortX && mx < sortX + sortW && my >= arrowY && my < arrowY + arrowSize) {
            if (!playerUUIDs.isEmpty() && selectedPlayerIndex < playerUUIDs.size()) {
                String uuid = playerUUIDs.get(selectedPlayerIndex);
                ClientPacketDistributor.sendToServer(
                    (CustomPacketPayload) new ComputerActionPayload("inv_view_sort", uuid),
                    (CustomPacketPayload[]) new CustomPacketPayload[0]);
                setStatus("Sorting inventory...");
            }
            return true;
        }

        // Give Item global button
        int giveX = sortX - 62;
        int giveW = 56;
        if (mx >= giveX && mx < giveX + giveW && my >= arrowY && my < arrowY + arrowSize) {
            if (!playerUUIDs.isEmpty() && selectedPlayerIndex < playerUUIDs.size()) {
                awaitingGiveInput = true;
                giveItemBuffer = "";
                giveCountBuffer = "1";
                giveItemFocused = true;
            }
            return true;
        }

        // --- Inventory grid clicks ---
        int contentTop = top + selectorH + 2;
        int gridLeft = left + 4;
        int gridTop = contentTop + 4 + 12; // account for "Main Inventory" label

        // Main inventory rows (slots 9-35)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                int slotIdx = 9 + row * GRID_COLS + col;
                int sx = gridLeft + col * (SLOT_SIZE + SLOT_GAP);
                int sy = gridTop + row * (SLOT_SIZE + SLOT_GAP);
                if (mx >= sx && mx < sx + SLOT_SIZE && my >= sy && my < sy + SLOT_SIZE) {
                    selectedSlot = slotIdx;
                    return true;
                }
            }
        }

        // Hotbar row (slots 0-8)
        for (int col = 0; col < GRID_COLS; col++) {
            int slotIdx = col;
            int sx = gridLeft + col * (SLOT_SIZE + SLOT_GAP);
            int sy = gridTop + 3 * (SLOT_SIZE + SLOT_GAP) + 6;
            if (mx >= sx && mx < sx + SLOT_SIZE && my >= sy && my < sy + SLOT_SIZE) {
                selectedSlot = slotIdx;
                return true;
            }
        }

        // Armor slots (36-39, rendered reversed)
        int armorTop2 = gridTop + 4 * (SLOT_SIZE + SLOT_GAP) + 14 + 12;
        for (int i = 0; i < ARMOR_SLOT_COUNT; i++) {
            int slotIdx = 36 + (3 - i);
            int sx = gridLeft + i * (SLOT_SIZE + SLOT_GAP + 16);
            if (mx >= sx && mx < sx + SLOT_SIZE && my >= armorTop2 && my < armorTop2 + SLOT_SIZE) {
                selectedSlot = slotIdx;
                return true;
            }
        }

        // Offhand slot (40)
        int offhandX = gridLeft + 4 * (SLOT_SIZE + SLOT_GAP + 16) + 10;
        if (mx >= offhandX && mx < offhandX + SLOT_SIZE && my >= armorTop2 && my < armorTop2 + SLOT_SIZE) {
            selectedSlot = 40;
            return true;
        }

        // --- Detail panel action buttons ---
        if (selectedSlot >= 0 && selectedSlot <= 40) {
            int gridPanelW = (SLOT_SIZE + SLOT_GAP) * GRID_COLS + SLOT_GAP + 8;
            int detailLeft = gridLeft + gridPanelW + 12;
            int detailW = right - detailLeft - 4;
            int detailH = bottom - (contentTop + 4) - 4;
            int detailTop2 = contentTop + 4;

            int px = detailLeft + 6;
            int btnY = detailTop2 + detailH - 40;
            int btnH = 16;
            int btnGap = 4;

            InvSlotEntry entry = getSlotEntry(selectedSlot);
            if (entry != null && !"minecraft:air".equals(entry.itemId) && entry.count > 0) {
                // Delete
                int deleteBtnX = px;
                int deleteBtnW = 50;
                if (mx >= deleteBtnX && mx < deleteBtnX + deleteBtnW && my >= btnY && my < btnY + btnH) {
                    String uuid = getSelectedUUID();
                    if (uuid != null) {
                        ClientPacketDistributor.sendToServer(
                            (CustomPacketPayload) new ComputerActionPayload("inv_view_delete", uuid + ":" + selectedSlot),
                            (CustomPacketPayload[]) new CustomPacketPayload[0]);
                        setStatus("Deleted slot " + selectedSlot);
                    }
                    return true;
                }

                // Copy to Me
                int copyBtnX = deleteBtnX + deleteBtnW + btnGap;
                int copyBtnW = 68;
                if (mx >= copyBtnX && mx < copyBtnX + copyBtnW && my >= btnY && my < btnY + btnH) {
                    String uuid = getSelectedUUID();
                    if (uuid != null) {
                        ClientPacketDistributor.sendToServer(
                            (CustomPacketPayload) new ComputerActionPayload("inv_view_copy", uuid + ":" + selectedSlot),
                            (CustomPacketPayload[]) new CustomPacketPayload[0]);
                        setStatus("Copied item to your inventory");
                    }
                    return true;
                }

                // Set Count
                int countBtnX = copyBtnX + copyBtnW + btnGap;
                int countBtnW = 64;
                if (mx >= countBtnX && mx < countBtnX + countBtnW && my >= btnY && my < btnY + btnH) {
                    awaitingCountInput = true;
                    countInputBuffer = String.valueOf(entry.count);
                    return true;
                }

                // Repair (only if item is damageable)
                if (entry.maxDurability > 0) {
                    int repairBtnX = countBtnX + countBtnW + btnGap;
                    int repairBtnW = 52;
                    if (mx >= repairBtnX && mx < repairBtnX + repairBtnW && my >= btnY && my < btnY + btnH) {
                        String uuid = getSelectedUUID();
                        if (uuid != null) {
                            ClientPacketDistributor.sendToServer(
                                (CustomPacketPayload) new ComputerActionPayload("inv_view_repair", uuid + ":" + selectedSlot),
                                (CustomPacketPayload[]) new CustomPacketPayload[0]);
                            setStatus("Repairing item...");
                        }
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        // Currently no scrolling needed since the grid is fixed size
        // Could be extended for players with many items
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (awaitingCountInput) {
            // Backspace
            if (keyCode == 259 && !countInputBuffer.isEmpty()) {
                countInputBuffer = countInputBuffer.substring(0, countInputBuffer.length() - 1);
                return true;
            }
            // Enter
            if (keyCode == 257 || keyCode == 335) {
                submitCountInput();
                return true;
            }
            // Escape
            if (keyCode == 256) {
                awaitingCountInput = false;
                countInputBuffer = "";
                return true;
            }
            return true;
        }
        if (awaitingGiveInput) {
            // Tab to switch fields
            if (keyCode == 258) {
                giveItemFocused = !giveItemFocused;
                return true;
            }
            // Backspace
            if (keyCode == 259) {
                if (giveItemFocused && !giveItemBuffer.isEmpty()) {
                    giveItemBuffer = giveItemBuffer.substring(0, giveItemBuffer.length() - 1);
                } else if (!giveItemFocused && !giveCountBuffer.isEmpty()) {
                    giveCountBuffer = giveCountBuffer.substring(0, giveCountBuffer.length() - 1);
                }
                return true;
            }
            // Enter
            if (keyCode == 257 || keyCode == 335) {
                submitGiveInput();
                return true;
            }
            // Escape
            if (keyCode == 256) {
                awaitingGiveInput = false;
                giveItemBuffer = "";
                giveCountBuffer = "1";
                return true;
            }
            return true;
        }
        return false;
    }

    public boolean charTyped(char ch, int modifiers) {
        if (awaitingCountInput) {
            if (ch >= '0' && ch <= '9' && countInputBuffer.length() < 4) {
                countInputBuffer += ch;
                return true;
            }
            return true;
        }
        if (awaitingGiveInput) {
            if (giveItemFocused) {
                // Item ID field: allow alphanumeric, colon, underscore, dot, slash
                if ((ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9') || ch == ':' || ch == '_' || ch == '.' || ch == '/') {
                    if (giveItemBuffer.length() < 50) {
                        giveItemBuffer += ch;
                    }
                }
            } else {
                // Count field: digits only
                if (ch >= '0' && ch <= '9' && giveCountBuffer.length() < 4) {
                    giveCountBuffer += ch;
                }
            }
            return true;
        }
        return false;
    }

    private void submitCountInput() {
        awaitingCountInput = false;
        try {
            int newCount = Integer.parseInt(countInputBuffer);
            if (newCount < 0) newCount = 0;
            if (newCount > 64) newCount = 64;
            String uuid = getSelectedUUID();
            if (uuid != null && selectedSlot >= 0) {
                ClientPacketDistributor.sendToServer(
                    (CustomPacketPayload) new ComputerActionPayload("inv_view_set_count", uuid + ":" + selectedSlot + ":" + newCount),
                    (CustomPacketPayload[]) new CustomPacketPayload[0]);
                setStatus("Set count to " + newCount);
            }
        } catch (NumberFormatException e) {
            setStatus("Error: Invalid number");
        }
        countInputBuffer = "";
    }

    private void submitGiveInput() {
        awaitingGiveInput = false;
        String itemId = giveItemBuffer.trim();
        if (itemId.isEmpty()) {
            setStatus("Error: Item ID is empty");
            giveItemBuffer = "";
            giveCountBuffer = "1";
            return;
        }
        int count = 1;
        try {
            count = Integer.parseInt(giveCountBuffer.trim());
            if (count < 1) count = 1;
            if (count > 64) count = 64;
        } catch (NumberFormatException e) {
            count = 1;
        }
        String uuid = getSelectedUUID();
        if (uuid != null) {
            ClientPacketDistributor.sendToServer(
                (CustomPacketPayload) new ComputerActionPayload("inv_view_give", uuid + ":" + itemId + ":" + count),
                (CustomPacketPayload[]) new CustomPacketPayload[0]);
            setStatus("Giving " + count + "x " + itemId + "...");
        }
        giveItemBuffer = "";
        giveCountBuffer = "1";
    }

    public void handleResponse(String type, String jsonData) {
        if ("inv_view_data".equals(type)) {
            parseInventoryData(jsonData);
        } else if ("inv_view_result".equals(type)) {
            try {
                JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
                if (obj.has("msg")) {
                    setStatus(obj.get("msg").getAsString());
                }
                // Auto-refresh after action
                requestData();
            } catch (Exception e) {
                setStatus("Action completed");
                requestData();
            }
        }
    }

    public void requestData() {
        refreshPlayerList();
        if (!playerUUIDs.isEmpty() && selectedPlayerIndex < playerUUIDs.size()) {
            String uuid = playerUUIDs.get(selectedPlayerIndex);
            ClientPacketDistributor.sendToServer(
                (CustomPacketPayload) new ComputerActionPayload("inv_view_request", uuid),
                (CustomPacketPayload[]) new CustomPacketPayload[0]);
        }
    }

    private void refreshPlayerList() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() == null) return;

        Collection<PlayerInfo> players = mc.getConnection().getOnlinePlayers();
        List<String> newNames = new ArrayList<>();
        List<String> newUUIDs = new ArrayList<>();
        for (PlayerInfo info : players) {
            newNames.add(info.getProfile().name());
            newUUIDs.add(info.getProfile().id().toString());
        }

        // Preserve selection if possible
        String previousUUID = null;
        if (!playerUUIDs.isEmpty() && selectedPlayerIndex < playerUUIDs.size()) {
            previousUUID = playerUUIDs.get(selectedPlayerIndex);
        }

        playerNames = newNames;
        playerUUIDs = newUUIDs;

        if (previousUUID != null) {
            int idx = playerUUIDs.indexOf(previousUUID);
            if (idx >= 0) {
                selectedPlayerIndex = idx;
            } else {
                selectedPlayerIndex = 0;
            }
        }
        if (selectedPlayerIndex >= playerNames.size()) {
            selectedPlayerIndex = Math.max(0, playerNames.size() - 1);
        }
    }

    private void parseInventoryData(String jsonData) {
        slots.clear();
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            if (obj.has("playerName")) {
                viewedPlayerName = obj.get("playerName").getAsString();
            }
            if (obj.has("slots")) {
                JsonArray arr = obj.getAsJsonArray("slots");
                for (JsonElement el : arr) {
                    JsonObject slotObj = el.getAsJsonObject();
                    int slot = slotObj.get("slot").getAsInt();
                    String itemId = slotObj.get("itemId").getAsString();
                    String name = slotObj.get("name").getAsString();
                    int count = slotObj.get("count").getAsInt();
                    float durability = slotObj.has("durability") ? slotObj.get("durability").getAsFloat() : 0;
                    float maxDurability = slotObj.has("maxDurability") ? slotObj.get("maxDurability").getAsFloat() : 0;
                    String enchants = slotObj.has("enchants") ? slotObj.get("enchants").getAsString() : "";
                    String customData = slotObj.has("customData") ? slotObj.get("customData").getAsString() : "";
                    slots.add(new InvSlotEntry(slot, itemId, name, count, durability, maxDurability, enchants, customData));
                }
            }
        } catch (Exception e) {
            setStatus("Error: Failed to parse inventory data");
        }
    }

    private InvSlotEntry getSlotEntry(int slotIdx) {
        for (InvSlotEntry entry : slots) {
            if (entry.slot == slotIdx) return entry;
        }
        return null;
    }

    private String getSelectedUUID() {
        if (playerUUIDs.isEmpty() || selectedPlayerIndex >= playerUUIDs.size()) return null;
        return playerUUIDs.get(selectedPlayerIndex);
    }

    private String getSlotLabel(int slotIdx) {
        if (slotIdx >= 0 && slotIdx <= 8) return "Hotbar " + (slotIdx + 1);
        if (slotIdx >= 9 && slotIdx <= 35) return "Inv " + (slotIdx - 8);
        if (slotIdx == 36) return "Feet";
        if (slotIdx == 37) return "Legs";
        if (slotIdx == 38) return "Chest";
        if (slotIdx == 39) return "Head";
        if (slotIdx == 40) return "Offhand";
        return "Slot " + slotIdx;
    }

    private void setStatus(String msg) {
        statusMessage = msg;
        statusMessageTime = System.currentTimeMillis();
    }

    private void drawSmallButton(GuiGraphics g, int x, int y, int w, int h, String text, boolean hovered, int accentColor) {
        int bg = hovered ? 0xFF30363D : SLOT_BG;
        g.fill(x, y, x + w, y + h, bg);
        g.fill(x, y, x + w, y + 1, hovered ? accentColor : BORDER_COLOR);
        g.fill(x, y + h - 1, x + w, y + h, BORDER_COLOR);
        g.fill(x, y, x + 1, y + h, BORDER_COLOR);
        g.fill(x + w - 1, y, x + w, y + h, BORDER_COLOR);
        int tw = font.width(text);
        int tx = x + (w - tw) / 2;
        int ty = y + (h - 8) / 2;
        g.drawString(font, text, tx, ty, hovered ? TEXT_COLOR : accentColor, false);
    }
}

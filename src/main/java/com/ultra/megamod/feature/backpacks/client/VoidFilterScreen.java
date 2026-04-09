package com.ultra.megamod.feature.backpacks.client;

import com.ultra.megamod.feature.backpacks.network.BackpackActionPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration screen for the Void Upgrade filter list.
 * Allows players to add/remove item registry IDs that the void upgrade will destroy.
 * Extends Screen (not AbstractContainerScreen) — this is a configuration overlay.
 */
public class VoidFilterScreen extends Screen {

    private static final int PANEL_WIDTH = 220;
    private static final int PANEL_HEIGHT = 200;
    private static final int ENTRY_HEIGHT = 16;
    private static final int LIST_START_Y = 38;
    private static final int MAX_VISIBLE_ENTRIES = 8;

    private final Screen parentScreen;
    private final List<String> filterItems;
    private int panelX;
    private int panelY;
    private int scrollOffset = 0;

    public VoidFilterScreen(Screen parentScreen, List<String> filterItems) {
        super(Component.literal("Void Filter"));
        this.parentScreen = parentScreen;
        this.filterItems = new ArrayList<>(filterItems);
    }

    @Override
    protected void init() {
        super.init();
        this.panelX = (this.width - PANEL_WIDTH) / 2;
        this.panelY = (this.height - PANEL_HEIGHT) / 2;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Dim background
        g.fill(0, 0, this.width, this.height, 0x88000000);

        // Panel background with border
        g.fill(panelX - 1, panelY - 1, panelX + PANEL_WIDTH + 1, panelY + PANEL_HEIGHT + 1, 0xFF444444);
        g.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, 0xFF1A1A2E);

        // Title
        g.drawCenteredString(this.font, "Void Filter", panelX + PANEL_WIDTH / 2, panelY + 8, 0xFFFFFFFF);

        // Separator line below title
        g.fill(panelX + 10, panelY + 22, panelX + PANEL_WIDTH - 10, panelY + 23, 0xFF555555);

        // "Add from hand" button
        int addBtnW = 100;
        int addBtnX = panelX + (PANEL_WIDTH - addBtnW) / 2;
        int addBtnY = panelY + 25;
        boolean addHovered = mouseX >= addBtnX && mouseX <= addBtnX + addBtnW
                && mouseY >= addBtnY && mouseY <= addBtnY + 12;
        g.fill(addBtnX, addBtnY, addBtnX + addBtnW, addBtnY + 12, addHovered ? 0xFF446644 : 0xFF335533);
        g.drawCenteredString(this.font, "\u00A7aAdd from Hand", addBtnX + addBtnW / 2, addBtnY + 2, 0xFFFFFFFF);

        // Filter list entries
        if (filterItems.isEmpty()) {
            g.drawCenteredString(this.font, "\u00A78No items in void filter", panelX + PANEL_WIDTH / 2, panelY + LIST_START_Y + 20, 0xFF888888);
        } else {
            int listAreaHeight = MAX_VISIBLE_ENTRIES * ENTRY_HEIGHT;
            // Clip area background
            g.fill(panelX + 8, panelY + LIST_START_Y, panelX + PANEL_WIDTH - 8, panelY + LIST_START_Y + listAreaHeight, 0xFF111122);

            int maxOffset = Math.max(0, filterItems.size() - MAX_VISIBLE_ENTRIES);
            scrollOffset = Math.min(scrollOffset, maxOffset);

            for (int i = 0; i < MAX_VISIBLE_ENTRIES && (i + scrollOffset) < filterItems.size(); i++) {
                int idx = i + scrollOffset;
                String itemId = filterItems.get(idx);
                int entryY = panelY + LIST_START_Y + i * ENTRY_HEIGHT;

                // Entry hover highlight
                boolean entryHovered = mouseX >= panelX + 8 && mouseX <= panelX + PANEL_WIDTH - 8
                        && mouseY >= entryY && mouseY < entryY + ENTRY_HEIGHT;
                if (entryHovered) {
                    g.fill(panelX + 8, entryY, panelX + PANEL_WIDTH - 8, entryY + ENTRY_HEIGHT, 0x33FFFFFF);
                }

                // Item ID text (truncate if too long)
                String displayText = itemId;
                int maxTextWidth = PANEL_WIDTH - 60;
                if (this.font.width(displayText) > maxTextWidth) {
                    while (this.font.width(displayText + "...") > maxTextWidth && displayText.length() > 3) {
                        displayText = displayText.substring(0, displayText.length() - 1);
                    }
                    displayText = displayText + "...";
                }
                g.drawString(this.font, displayText, panelX + 14, entryY + 4, 0xFFCCCCCC, false);

                // Remove button [X]
                int removeBtnX = panelX + PANEL_WIDTH - 28;
                int removeBtnY = entryY + 2;
                boolean removeHovered = mouseX >= removeBtnX && mouseX <= removeBtnX + 18
                        && mouseY >= removeBtnY && mouseY <= removeBtnY + 12;
                g.fill(removeBtnX, removeBtnY, removeBtnX + 18, removeBtnY + 12, removeHovered ? 0xFF664444 : 0xFF553333);
                g.drawCenteredString(this.font, "\u00A7cX", removeBtnX + 9, removeBtnY + 2, 0xFFFFFFFF);
            }

            // Scroll indicators
            if (scrollOffset > 0) {
                g.drawCenteredString(this.font, "\u00A77\u25B2", panelX + PANEL_WIDTH - 16, panelY + LIST_START_Y - 8, 0xFFAAAAAA);
            }
            if (scrollOffset + MAX_VISIBLE_ENTRIES < filterItems.size()) {
                g.drawCenteredString(this.font, "\u00A77\u25BC", panelX + PANEL_WIDTH - 16, panelY + LIST_START_Y + listAreaHeight + 1, 0xFFAAAAAA);
            }
        }

        // Item count
        g.drawString(this.font, "\u00A77Items: \u00A7f" + filterItems.size(), panelX + 10, panelY + PANEL_HEIGHT - 38, 0xFFCCCCCC, false);

        // Back button
        int backW = 60;
        int backX = panelX + (PANEL_WIDTH - backW) / 2;
        int backY = panelY + PANEL_HEIGHT - 24;
        boolean backHovered = mouseX >= backX && mouseX <= backX + backW
                && mouseY >= backY && mouseY <= backY + 16;
        g.fill(backX, backY, backX + backW, backY + 16, backHovered ? 0xFF555577 : 0xFF333355);
        g.drawCenteredString(this.font, "Back", backX + backW / 2, backY + 4, 0xFFFFFFFF);
    }

    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean consumed) {
        if (consumed) return true;
        if (event.button() != 0) return false;
        int mx = (int) event.x();
        int my = (int) event.y();

        // Check "Add from hand" button
        int addBtnW = 100;
        int addBtnX = panelX + (PANEL_WIDTH - addBtnW) / 2;
        int addBtnY = panelY + 25;
        if (mx >= addBtnX && mx <= addBtnX + addBtnW && my >= addBtnY && my <= addBtnY + 12) {
            sendAction("void_add_filter");
            // Optimistically add the held item to the local list
            if (this.minecraft != null && this.minecraft.player != null) {
                net.minecraft.world.item.ItemStack heldItem = this.minecraft.player.getMainHandItem();
                if (!heldItem.isEmpty()) {
                    String itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(heldItem.getItem()).toString();
                    if (!filterItems.contains(itemId)) {
                        filterItems.add(itemId);
                    }
                }
            }
            return true;
        }

        // Check remove buttons
        if (!filterItems.isEmpty()) {
            for (int i = 0; i < MAX_VISIBLE_ENTRIES && (i + scrollOffset) < filterItems.size(); i++) {
                int idx = i + scrollOffset;
                int entryY = panelY + LIST_START_Y + i * ENTRY_HEIGHT;
                int removeBtnX = panelX + PANEL_WIDTH - 28;
                int removeBtnY = entryY + 2;
                if (mx >= removeBtnX && mx <= removeBtnX + 18 && my >= removeBtnY && my <= removeBtnY + 12) {
                    String itemId = filterItems.get(idx);
                    sendAction("void_remove_filter:" + itemId);
                    filterItems.remove(idx);
                    // Adjust scroll if we removed the last visible item
                    int maxOffset = Math.max(0, filterItems.size() - MAX_VISIBLE_ENTRIES);
                    if (scrollOffset > maxOffset) scrollOffset = maxOffset;
                    return true;
                }
            }
        }

        // Check back button
        int backW = 60;
        int backX = panelX + (PANEL_WIDTH - backW) / 2;
        int backY = panelY + PANEL_HEIGHT - 24;
        if (mx >= backX && mx <= backX + backW && my >= backY && my <= backY + 16) {
            onClose();
            return true;
        }

        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (filterItems.size() > MAX_VISIBLE_ENTRIES) {
            if (scrollY > 0) {
                scrollOffset = Math.max(0, scrollOffset - 1);
            } else if (scrollY < 0) {
                scrollOffset = Math.min(filterItems.size() - MAX_VISIBLE_ENTRIES, scrollOffset + 1);
            }
            return true;
        }
        return false;
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(parentScreen);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void sendAction(String action) {
        ClientPacketDistributor.sendToServer(
            (CustomPacketPayload) new BackpackActionPayload(action),
            (CustomPacketPayload[]) new CustomPacketPayload[0]
        );
    }
}

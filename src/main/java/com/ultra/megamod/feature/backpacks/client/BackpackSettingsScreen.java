package com.ultra.megamod.feature.backpacks.client;

import com.ultra.megamod.feature.backpacks.BackpackItem;
import com.ultra.megamod.feature.backpacks.network.BackpackActionPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple settings overlay screen for backpack toggles.
 * Accessible from a gear button in BackpackScreen.
 */
public class BackpackSettingsScreen extends Screen {

    private static final int PANEL_WIDTH = 200;
    private static final int PANEL_HEIGHT = 185;
    private static final int TOGGLE_HEIGHT = 22;
    private static final int TOGGLE_START_Y = 30;

    private final Screen parentScreen;
    private int panelX;
    private int panelY;

    // Toggle states (client-side tracking; actual state is server-authoritative)
    private boolean toolSlotsVisible = true;
    private boolean magnetActive = true;
    private boolean autoPickupActive = true;
    private boolean feedingActive = true;

    private final List<ToggleEntry> toggles = new ArrayList<>();

    public BackpackSettingsScreen(Screen parentScreen) {
        super(Component.literal("Backpack Settings"));
        this.parentScreen = parentScreen;
    }

    @Override
    protected void init() {
        super.init();
        this.panelX = (this.width - PANEL_WIDTH) / 2;
        this.panelY = (this.height - PANEL_HEIGHT) / 2;

        toggles.clear();
        toggles.add(new ToggleEntry("Tool Slots Visible", "toggle_tools", () -> toolSlotsVisible, v -> toolSlotsVisible = v));
        toggles.add(new ToggleEntry("Magnet Active", "toggle_magnet", () -> magnetActive, v -> magnetActive = v));
        toggles.add(new ToggleEntry("Auto-Pickup Active", "toggle_pickup", () -> autoPickupActive, v -> autoPickupActive = v));
        toggles.add(new ToggleEntry("Feeding Active", "toggle_feeding", () -> feedingActive, v -> feedingActive = v));
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Dim background
        g.fill(0, 0, this.width, this.height, 0x88000000);

        // Panel background
        g.fill(panelX - 1, panelY - 1, panelX + PANEL_WIDTH + 1, panelY + PANEL_HEIGHT + 1, 0xFF444444);
        g.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, 0xFF1A1A2E);

        // Title
        g.drawCenteredString(this.font, "Backpack Settings", panelX + PANEL_WIDTH / 2, panelY + 8, 0xFFFFFFFF);

        // Separator line
        g.fill(panelX + 10, panelY + 22, panelX + PANEL_WIDTH - 10, panelY + 23, 0xFF555555);

        // Toggle entries
        for (int i = 0; i < toggles.size(); i++) {
            ToggleEntry toggle = toggles.get(i);
            int ty = panelY + TOGGLE_START_Y + i * TOGGLE_HEIGHT;
            boolean hovered = mouseX >= panelX + 10 && mouseX <= panelX + PANEL_WIDTH - 10
                    && mouseY >= ty && mouseY < ty + TOGGLE_HEIGHT - 2;

            // Hover highlight
            if (hovered) {
                g.fill(panelX + 10, ty, panelX + PANEL_WIDTH - 10, ty + TOGGLE_HEIGHT - 2, 0x33FFFFFF);
            }

            // Label
            g.drawString(this.font, toggle.label, panelX + 16, ty + 6, 0xFFCCCCCC, false);

            // On/Off indicator
            boolean on = toggle.getter.get();
            String stateText = on ? "\u00A7aON" : "\u00A7cOFF";
            int stateWidth = this.font.width(on ? "ON" : "OFF");
            g.drawString(this.font, stateText, panelX + PANEL_WIDTH - 20 - stateWidth, ty + 6, 0xFFFFFFFF, false);
        }

        // Separator before Void Filter button
        int voidSepY = panelY + TOGGLE_START_Y + toggles.size() * TOGGLE_HEIGHT + 4;
        g.fill(panelX + 10, voidSepY, panelX + PANEL_WIDTH - 10, voidSepY + 1, 0xFF555555);

        // Void Filter button
        int voidBtnW = 100;
        int voidBtnX = panelX + (PANEL_WIDTH - voidBtnW) / 2;
        int voidBtnY = voidSepY + 6;
        boolean voidHovered = mouseX >= voidBtnX && mouseX <= voidBtnX + voidBtnW
                && mouseY >= voidBtnY && mouseY <= voidBtnY + 16;
        g.fill(voidBtnX, voidBtnY, voidBtnX + voidBtnW, voidBtnY + 16, voidHovered ? 0xFF555577 : 0xFF333355);
        g.drawCenteredString(this.font, "Void Filter", voidBtnX + voidBtnW / 2, voidBtnY + 4, 0xFFFFFFFF);

        // Back button
        int backY = panelY + PANEL_HEIGHT - 24;
        int backW = 60;
        int backX = panelX + (PANEL_WIDTH - backW) / 2;
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

        // Check toggle clicks
        for (int i = 0; i < toggles.size(); i++) {
            ToggleEntry toggle = toggles.get(i);
            int ty = panelY + TOGGLE_START_Y + i * TOGGLE_HEIGHT;
            if (mx >= panelX + 10 && mx <= panelX + PANEL_WIDTH - 10
                    && my >= ty && my < ty + TOGGLE_HEIGHT - 2) {
                // Toggle the state locally
                toggle.setter.accept(!toggle.getter.get());
                // Send action to server
                sendAction(toggle.action);
                return true;
            }
        }

        // Check Void Filter button
        int voidSepY = panelY + TOGGLE_START_Y + toggles.size() * TOGGLE_HEIGHT + 4;
        int voidBtnW = 100;
        int voidBtnX = panelX + (PANEL_WIDTH - voidBtnW) / 2;
        int voidBtnY = voidSepY + 6;
        if (mx >= voidBtnX && mx <= voidBtnX + voidBtnW && my >= voidBtnY && my <= voidBtnY + 16) {
            openVoidFilter();
            return true;
        }

        // Check back button
        int backY = panelY + PANEL_HEIGHT - 24;
        int backW = 60;
        int backX = panelX + (PANEL_WIDTH - backW) / 2;
        if (mx >= backX && mx <= backX + backW && my >= backY && my <= backY + 16) {
            onClose();
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

    /**
     * Open the Void Filter screen. Reads the current filter list from the
     * backpack's CustomData on the client side.
     */
    private void openVoidFilter() {
        if (this.minecraft == null || this.minecraft.player == null) return;

        List<String> filterItems = new ArrayList<>();

        // Read void filter from the backpack item's CustomData (client-side read)
        for (int i = 0; i < this.minecraft.player.getInventory().getContainerSize(); i++) {
            ItemStack stack = this.minecraft.player.getInventory().getItem(i);
            if (stack.getItem() instanceof BackpackItem) {
                CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
                if (customData != null) {
                    CompoundTag tag = customData.copyTag();
                    if (tag.contains("UpgradeData")) {
                        CompoundTag upgradeData = tag.getCompoundOrEmpty("UpgradeData");
                        if (upgradeData.contains("void")) {
                            CompoundTag voidData = upgradeData.getCompoundOrEmpty("void");
                            if (voidData.contains("VoidFilter")) {
                                ListTag filterTag = voidData.getListOrEmpty("VoidFilter");
                                for (int j = 0; j < filterTag.size(); j++) {
                                    String id = filterTag.getString(j).orElse("");
                                    if (!id.isEmpty()) {
                                        filterItems.add(id);
                                    }
                                }
                            }
                        }
                    }
                }
                break;
            }
        }

        this.minecraft.setScreen(new VoidFilterScreen(this, filterItems));
    }

    private void sendAction(String action) {
        ClientPacketDistributor.sendToServer(
            (CustomPacketPayload) new BackpackActionPayload(action),
            (CustomPacketPayload[]) new CustomPacketPayload[0]
        );
    }

    // --- Toggle data helper ---

    private static class ToggleEntry {
        final String label;
        final String action;
        final java.util.function.Supplier<Boolean> getter;
        final java.util.function.Consumer<Boolean> setter;

        ToggleEntry(String label, String action,
                    java.util.function.Supplier<Boolean> getter,
                    java.util.function.Consumer<Boolean> setter) {
            this.label = label;
            this.action = action;
            this.getter = getter;
            this.setter = setter;
        }
    }
}

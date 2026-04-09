package com.ultra.megamod.feature.backpacks.client;

import com.ultra.megamod.feature.backpacks.menu.BackpackMenu;
import com.ultra.megamod.feature.backpacks.network.BackpackActionPayload;
import com.ultra.megamod.feature.backpacks.upgrade.BackpackUpgrade;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class BackpackScreen extends AbstractContainerScreen<BackpackMenu> {

    private static final Identifier BACKGROUND = Identifier.fromNamespaceAndPath("megamod",
            "textures/gui/backpack/background_9.png");
    private static final Identifier SLOTS_TEX = Identifier.fromNamespaceAndPath("megamod",
            "textures/gui/backpack/slots.png");
    private static final Identifier ICONS = Identifier.fromNamespaceAndPath("megamod",
            "textures/gui/backpack/icons.png");
    private static final Identifier TOOLS_TEX = Identifier.fromNamespaceAndPath("megamod",
            "textures/gui/backpack/tools_overlay.png");

    private static final int TEX_W = 256;
    private static final int TEX_H = 256;
    private static final int TOP_BAR_OFFSET = 18;
    private static final int PLAYER_INV_HEIGHT = 98;
    private static final int TOOL_SLOT_WIDTH = 22; // width of tool column on left side

    private final int rows;
    private final int slotsHeight;
    private final int toolSlotCount;

    // Button definitions
    private final List<IconButton> actionButtons = new ArrayList<>();
    private boolean showMoreButtons = false;

    // Sort row buttons (always visible, top-right)
    private int sortRowX;
    private int sortRowY;
    private static final int SORT_CONTAINER_W = 50;
    private static final int SORT_CONTAINER_H = 13;

    // Upgrade tabs (right side of GUI)
    private static final int TAB_COLLAPSED_W = 24;
    private static final int TAB_COLLAPSED_H = 24;
    private static final int TAB_EXPANDED_W = 110;
    private static final int TAB_EXPANDED_H = 60;
    private static final int TAB_SPACING = 26;
    private static final int TAB_COLOR_BG = 0xFF3A3A4A;
    private static final int TAB_COLOR_BG_HOVER = 0xFF4A4A5A;
    private static final int TAB_COLOR_EXPANDED = 0xFF2A2A3A;
    private static final int TAB_COLOR_BORDER = 0xFF606070;
    private static final int TAB_COLOR_HEADER = 0xFF50506A;
    private int expandedUpgradeTab = -1; // index of currently expanded tab, -1 = none

    public BackpackScreen(BackpackMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.rows = menu.getTier().getStorageRows();
        this.slotsHeight = rows * 18;
        this.toolSlotCount = menu.getToolSlotCount();
        this.imageWidth = 176;
        this.imageHeight = TOP_BAR_OFFSET + slotsHeight + 14 + 96;
        this.inventoryLabelY = TOP_BAR_OFFSET + slotsHeight + 14 + 1;
    }

    @Override
    protected void init() {
        super.init();
        buildButtons();
    }

    private void buildButtons() {
        actionButtons.clear();
        int middleBar = TOP_BAR_OFFSET + slotsHeight + 1;

        // Sort row position (top-right of backpack inventory area)
        sortRowX = imageWidth - 56;
        sortRowY = TOP_BAR_OFFSET - 12;

        // --- Action buttons (right side, at middle bar between backpack and player inv) ---
        // More button (always visible)
        actionButtons.add(new IconButton(157, middleBar, 12, 12,
                4, 44, 78, 82, "More Options", () -> showMoreButtons = !showMoreButtons));

        // Equip button (visible when More toggled)
        actionButtons.add(new IconButton(145, middleBar, 12, 12,
                63, 56, 78, 82, "Equip Backpack", () -> sendAction("equip")) {
            @Override boolean isVisible() { return showMoreButtons; }
        });

        // Unequip button (visible when More toggled)
        actionButtons.add(new IconButton(133, middleBar, 12, 12,
                63, 67, 78, 82, "Unequip Backpack", () -> sendAction("unequip")) {
            @Override boolean isVisible() { return showMoreButtons; }
        });

        // Settings gear button (visible when More toggled)
        actionButtons.add(new IconButton(121, middleBar, 12, 12,
                4, 44, 78, 82, "Settings", this::openSettings) {
            @Override boolean isVisible() { return showMoreButtons; }
        });
    }

    private void openSettings() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(new BackpackSettingsScreen(this));
        }
    }

    private void sendAction(String action) {
        ClientPacketDistributor.sendToServer(
            (CustomPacketPayload) new BackpackActionPayload(action),
            (CustomPacketPayload[]) new CustomPacketPayload[0]
        );
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;

        // 3-part rendering: top frame + middle fill + bottom player inventory
        // The background texture has: top 17px (title bar) ... bottom ~96px (player inventory)
        // The player inventory section starts at V=160 in the texture

        // Part 1: Top frame (title bar area, 17px)
        g.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND,
                x, y, 0f, 0f, imageWidth, TOP_BAR_OFFSET, TEX_W, TEX_H);

        // Part 2: Middle area fill (backpack slot area) — tile the gray background
        // Use a 1-pixel-high strip from the texture at V=18 (solid gray area)
        for (int row = 0; row < slotsHeight; row++) {
            g.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND,
                    x, y + TOP_BAR_OFFSET + row, 0f, 18f, imageWidth, 1, TEX_W, TEX_H);
        }

        // Part 3: Separator + player inventory section from bottom of texture
        // The player inv section in the texture is the last 110 pixels (14px gap + 96px inv)
        int playerSectionHeight = 14 + PLAYER_INV_HEIGHT - 2;
        float playerSectionV = 256f - playerSectionHeight;
        g.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND,
                x, y + TOP_BAR_OFFSET + slotsHeight,
                0f, playerSectionV, imageWidth, playerSectionHeight, TEX_W, TEX_H);

        // Slot grid overlay for backpack storage
        g.blit(RenderPipelines.GUI_TEXTURED, SLOTS_TEX,
                x + 7, y + TOP_BAR_OFFSET, 0f, 0f, 9 * 18, slotsHeight, TEX_W, TEX_H);

        // Tool slots on the left side
        if (toolSlotCount > 0) {
            for (int i = 0; i < toolSlotCount; i++) {
                int toolX = x - TOOL_SLOT_WIDTH + 4;
                int toolY = y + TOP_BAR_OFFSET + i * 18;
                // Draw tool slot background (dark border + inner slot)
                g.fill(toolX, toolY, toolX + 18, toolY + 18, 0xFF8B8B8B);
                g.fill(toolX + 1, toolY + 1, toolX + 17, toolY + 17, 0xFF373737);
            }
        }

        // --- Sort row (4 buttons with background container) ---
        renderSortRow(g, mouseX, mouseY);

        // --- Action buttons ---
        for (IconButton btn : actionButtons) {
            if (btn.isVisible()) {
                btn.render(g, leftPos, topPos, mouseX, mouseY);
            }
        }

        // --- Upgrade tabs (right side) ---
        renderUpgradeTabs(g, mouseX, mouseY);
    }

    /**
     * Renders the sort row: a container background with 4 icon buttons inside.
     * Container background from ICONS at (77, 54), 50x13.
     * Individual hover overlays for each of the 4 buttons.
     */
    private void renderSortRow(GuiGraphics g, int mouseX, int mouseY) {
        int cx = leftPos + sortRowX;
        int cy = topPos + sortRowY;

        // Container background
        g.blit(RenderPipelines.GUI_TEXTURED, ICONS,
                cx, cy, 77f, 54f, SORT_CONTAINER_W, SORT_CONTAINER_H, TEX_W, TEX_H);

        // Hover overlays for each sort button (12x12 each, offset by 2px from container left, 2px from top)
        // Sort
        if (isSortButtonHovered(mouseX, mouseY, 0)) {
            g.blit(RenderPipelines.GUI_TEXTURED, ICONS,
                    cx + 1, cy + 2, 78f, 69f, 12, 12, TEX_W, TEX_H);
        }
        // Quick Stack
        if (isSortButtonHovered(mouseX, mouseY, 1)) {
            g.blit(RenderPipelines.GUI_TEXTURED, ICONS,
                    cx + 13, cy + 2, 90f, 69f, 12, 12, TEX_W, TEX_H);
        }
        // Transfer to Backpack
        if (isSortButtonHovered(mouseX, mouseY, 2)) {
            g.blit(RenderPipelines.GUI_TEXTURED, ICONS,
                    cx + 25, cy + 2, 102f, 69f, 12, 12, TEX_W, TEX_H);
        }
        // Transfer to Player
        if (isSortButtonHovered(mouseX, mouseY, 3)) {
            g.blit(RenderPipelines.GUI_TEXTURED, ICONS,
                    cx + 37, cy + 2, 114f, 69f, 12, 12, TEX_W, TEX_H);
        }
    }

    /**
     * Check if a sort row button is hovered.
     * Each button is 10x10 clickable area, spaced 12px apart, starting at container +2, +3.
     */
    private boolean isSortButtonHovered(int mouseX, int mouseY, int index) {
        int bx = leftPos + sortRowX + 2 + index * 12;
        int by = topPos + sortRowY + 3;
        return mouseX >= bx && mouseX < bx + 10 && mouseY >= by && mouseY < by + 10;
    }

    /**
     * Renders upgrade tabs on the right side of the backpack GUI.
     * Each installed upgrade gets a small colored tab. Clicking expands it to show details.
     */
    private void renderUpgradeTabs(GuiGraphics g, int mouseX, int mouseY) {
        List<BackpackUpgrade> upgrades = menu.getUpgradeManager().getActiveUpgrades();
        if (upgrades.isEmpty()) return;

        int tabX = leftPos + imageWidth + 2; // 2px gap to the right of the main GUI
        int tabStartY = topPos + TOP_BAR_OFFSET;

        // Assign a distinct color per upgrade based on index
        int[] tabColors = {
                0xFF4488CC, // blue
                0xFFCC6644, // orange
                0xFF44CC66, // green
                0xFFCC44AA, // pink
                0xFFAAAA44, // yellow
                0xFF8844CC, // purple
                0xFF44CCCC, // cyan
                0xFFCC4444  // red
        };

        for (int i = 0; i < upgrades.size(); i++) {
            BackpackUpgrade upgrade = upgrades.get(i);
            int ty = tabStartY + i * TAB_SPACING;
            int color = tabColors[i % tabColors.length];

            if (expandedUpgradeTab == i) {
                // --- Expanded tab ---
                // Border
                g.fill(tabX - 1, ty - 1, tabX + TAB_EXPANDED_W + 1, ty + TAB_EXPANDED_H + 1, TAB_COLOR_BORDER);
                // Background
                g.fill(tabX, ty, tabX + TAB_EXPANDED_W, ty + TAB_EXPANDED_H, TAB_COLOR_EXPANDED);
                // Header bar with upgrade color
                g.fill(tabX, ty, tabX + TAB_EXPANDED_W, ty + 14, TAB_COLOR_HEADER);
                // Color accent on left edge
                g.fill(tabX, ty, tabX + 3, ty + TAB_EXPANDED_H, color);

                // Header text: upgrade display name
                String name = upgrade.getDisplayName();
                g.drawString(this.font, name, tabX + 6, ty + 3, 0xFFFFFFFF, false);

                // Close X in top-right of expanded panel
                int closeX = tabX + TAB_EXPANDED_W - 10;
                int closeY = ty + 3;
                boolean closeHovered = mouseX >= closeX - 1 && mouseX < closeX + 7
                        && mouseY >= closeY - 1 && mouseY < closeY + 9;
                g.drawString(this.font, "x", closeX, closeY, closeHovered ? 0xFFFF6666 : 0xFFAAAAAA, false);

                // Content area: show upgrade info
                int contentY = ty + 16;
                String status = upgrade.isActive() ? "Active" : "Inactive";
                int statusColor = upgrade.isActive() ? 0xFF66FF66 : 0xFFFF6666;
                g.drawString(this.font, "Status: ", tabX + 6, contentY, 0xFFAAAAAA, false);
                g.drawString(this.font, status, tabX + 6 + this.font.width("Status: "), contentY, statusColor, false);

                String slots = "Slots: " + upgrade.getSlotCount();
                g.drawString(this.font, slots, tabX + 6, contentY + 12, 0xFFAAAAAA, false);

                String id = "ID: " + upgrade.getId();
                g.drawString(this.font, id, tabX + 6, contentY + 24, 0xFF888888, false);
            } else {
                // --- Collapsed tab ---
                boolean hovered = mouseX >= tabX && mouseX < tabX + TAB_COLLAPSED_W
                        && mouseY >= ty && mouseY < ty + TAB_COLLAPSED_H;
                int bgColor = hovered ? TAB_COLOR_BG_HOVER : TAB_COLOR_BG;

                // Border
                g.fill(tabX - 1, ty - 1, tabX + TAB_COLLAPSED_W + 1, ty + TAB_COLLAPSED_H + 1, TAB_COLOR_BORDER);
                // Background
                g.fill(tabX, ty, tabX + TAB_COLLAPSED_W, ty + TAB_COLLAPSED_H, bgColor);
                // Color accent on left edge
                g.fill(tabX, ty, tabX + 3, ty + TAB_COLLAPSED_H, color);

                // Draw first letter of upgrade name centered in the tab
                String letter = upgrade.getDisplayName().substring(0, 1).toUpperCase();
                int letterW = this.font.width(letter);
                int letterX = tabX + 3 + (TAB_COLLAPSED_W - 3 - letterW) / 2;
                int letterY = ty + (TAB_COLLAPSED_H - 8) / 2;
                g.drawString(this.font, letter, letterX, letterY, 0xFFFFFFFF, false);
            }
        }
    }

    /**
     * Handle click events on upgrade tabs.
     * Returns true if a tab was clicked and the event was consumed.
     */
    private boolean handleUpgradeTabClick(int mx, int my) {
        List<BackpackUpgrade> upgrades = menu.getUpgradeManager().getActiveUpgrades();
        if (upgrades.isEmpty()) return false;

        int tabX = leftPos + imageWidth + 2;
        int tabStartY = topPos + TOP_BAR_OFFSET;

        for (int i = 0; i < upgrades.size(); i++) {
            int ty = tabStartY + i * TAB_SPACING;

            if (expandedUpgradeTab == i) {
                // Check for click on the close X button
                int closeX = tabX + TAB_EXPANDED_W - 10;
                int closeY = ty + 3;
                if (mx >= closeX - 1 && mx < closeX + 7 && my >= closeY - 1 && my < closeY + 9) {
                    expandedUpgradeTab = -1;
                    return true;
                }
                // Click anywhere on the expanded header collapses it
                if (mx >= tabX && mx < tabX + TAB_EXPANDED_W && my >= ty && my < ty + 14) {
                    expandedUpgradeTab = -1;
                    return true;
                }
            } else {
                // Click on collapsed tab expands it
                if (mx >= tabX && mx < tabX + TAB_COLLAPSED_W && my >= ty && my < ty + TAB_COLLAPSED_H) {
                    expandedUpgradeTab = i;
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        this.renderTooltip(g, mouseX, mouseY);

        // Render sort row tooltips
        String[] sortTooltips = {"Sort Items", "Quick Stack", "Transfer to Backpack", "Transfer to Player"};
        for (int i = 0; i < 4; i++) {
            if (isSortButtonHovered(mouseX, mouseY, i)) {
                renderButtonTooltip(g, sortTooltips[i], mouseX, mouseY);
            }
        }

        // Render action button tooltips
        for (IconButton btn : actionButtons) {
            if (btn.isVisible() && btn.isHovered(leftPos, topPos, mouseX, mouseY)) {
                renderButtonTooltip(g, btn.tooltip, mouseX, mouseY);
            }
        }

        // Render upgrade tab tooltips (collapsed tabs only)
        List<BackpackUpgrade> upgrades = menu.getUpgradeManager().getActiveUpgrades();
        int tabX = leftPos + imageWidth + 2;
        int tabStartY = topPos + TOP_BAR_OFFSET;
        for (int i = 0; i < upgrades.size(); i++) {
            if (expandedUpgradeTab != i) {
                int ty = tabStartY + i * TAB_SPACING;
                if (mouseX >= tabX && mouseX < tabX + TAB_COLLAPSED_W
                        && mouseY >= ty && mouseY < ty + TAB_COLLAPSED_H) {
                    renderButtonTooltip(g, upgrades.get(i).getDisplayName(), mouseX, mouseY);
                }
            }
        }
    }

    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean consumed) {
        if (!consumed && event.button() == 0) {
            int mx = (int) event.x();
            int my = (int) event.y();

            // Check upgrade tab clicks first
            if (handleUpgradeTabClick(mx, my)) {
                return true;
            }

            // Check sort row buttons
            String[] sortActions = {"sort", "quick_stack", "transfer_to_backpack", "transfer_to_player"};
            for (int i = 0; i < 4; i++) {
                if (isSortButtonHovered(mx, my, i)) {
                    sendAction(sortActions[i]);
                    return true;
                }
            }

            // Check action buttons
            for (IconButton btn : actionButtons) {
                if (btn.isVisible() && btn.isHovered(leftPos, topPos, mx, my)) {
                    btn.onClick.run();
                    return true;
                }
            }
        }
        return super.mouseClicked(event, consumed);
    }

    /**
     * Renders a simple tooltip box near the cursor.
     */
    private void renderButtonTooltip(GuiGraphics g, String text, int mx, int my) {
        int tw = this.font.width(text) + 6;
        int tx = mx + 8;
        int ty = my - 12;
        g.fill(tx - 1, ty - 1, tx + tw + 1, ty + 11, 0xFF000000);
        g.fill(tx, ty, tx + tw, ty + 10, 0xFF1A1A2A);
        g.drawString(this.font, text, tx + 3, ty + 1, 0xFFFFFFFF, false);
    }

    /**
     * Icon button rendered from ICONS texture with normal and hover UV states.
     * Normal state: draws the icon inset by 1px (width-2, height-2) at (u1, v1).
     * Hover state: draws a full-size overlay at (u2, v2) on top.
     */
    static class IconButton {
        final int x, y, w, h;
        final int u1, v1; // Normal icon UV
        final int u2, v2; // Hovered overlay UV
        final String tooltip;
        final Runnable onClick;

        IconButton(int x, int y, int w, int h, int u1, int v1, int u2, int v2,
                   String tooltip, Runnable onClick) {
            this.x = x; this.y = y; this.w = w; this.h = h;
            this.u1 = u1; this.v1 = v1; this.u2 = u2; this.v2 = v2;
            this.tooltip = tooltip;
            this.onClick = onClick;
        }

        boolean isVisible() { return true; }

        boolean isHovered(int guiLeft, int guiTop, int mx, int my) {
            int bx = guiLeft + x;
            int by = guiTop + y;
            return mx >= bx && mx < bx + w && my >= by && my < by + h;
        }

        void render(GuiGraphics g, int guiLeft, int guiTop, int mx, int my) {
            int bx = guiLeft + x;
            int by = guiTop + y;

            // Normal state: icon inset by 1px for the border
            g.blit(RenderPipelines.GUI_TEXTURED, ICONS,
                    bx + 1, by + 1, (float) u1, (float) v1, w - 2, h - 2, TEX_W, TEX_H);

            // Hovered state: full-size overlay
            if (isHovered(guiLeft, guiTop, mx, my)) {
                g.blit(RenderPipelines.GUI_TEXTURED, ICONS,
                        bx, by, (float) u2, (float) v2, w, h, TEX_W, TEX_H);
            }
        }
    }
}

package com.ultra.megamod.feature.citizen.ornament;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.List;

/**
 * Screen for the Architects Cutter. Shows:
 * - 3 input slots on the left
 * - A scrollable recipe list in the center
 * - An output slot on the right
 * - Player inventory at the bottom
 *
 * Uses MegaMod's dark theme with custom-drawn slots (no texture atlas).
 */
public class ArchitectsCutterScreen extends AbstractContainerScreen<ArchitectsCutterMenu> {

    // Theme colors matching MegaMod's dark UI
    private static final int GOLD = 0xFFFFD700;
    private static final int TEXT = 0xFFCCCCDD;
    private static final int TEXT_DIM = 0xFF888899;
    private static final int BG = 0xFF0E0E18;
    private static final int PANEL_BG = 0xFF141420;
    private static final int SLOT_BG = 0xFF1C1C2C;
    private static final int SLOT_BORDER = 0xFF2A2A3A;
    private static final int ACCENT = 0xFF6B3FA0;
    private static final int SELECTED = 0xFF3A2A6A;
    private static final int HOVER = 0xFF2A1A4A;
    private static final int LIST_BG = 0xFF101018;

    /** Visible recipe rows in the scrollable list. */
    private static final int VISIBLE_ROWS = 4;
    /** Width of each recipe entry. */
    private static final int RECIPE_ENTRY_WIDTH = 70;
    /** Height of each recipe entry. */
    private static final int RECIPE_ENTRY_HEIGHT = 18;

    /** Scroll offset (number of rows scrolled). */
    private int scrollOffset = 0;
    /** Whether the player is currently dragging the scrollbar. */
    private boolean scrolling = false;

    public ArchitectsCutterScreen(ArchitectsCutterMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageWidth = 176;
        this.imageHeight = 175;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        this.titleLabelY = 4;
    }

    // ==================== Rendering ====================

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        int w = this.imageWidth;
        int h = this.imageHeight;

        // Outer border
        g.fill(x - 4, y - 4, x + w + 4, y + h + 4, BG);
        // Accent border
        g.fill(x - 3, y - 3, x + w + 3, y + h + 3, ACCENT);
        // Inner background
        g.fill(x - 2, y - 2, x + w + 2, y + h + 2, PANEL_BG);

        // Input slots (3 slots, left column)
        for (int i = 0; i < 3; i++) {
            int sx = x + 19;
            int sy = y + 16 + i * 22;
            g.fill(sx, sy, sx + 18, sy + 18, SLOT_BORDER);
            g.fill(sx + 1, sy + 1, sx + 17, sy + 17, SLOT_BG);
        }

        // Recipe list area
        int listX = x + 50;
        int listY = y + 14;
        int listW = RECIPE_ENTRY_WIDTH + 14; // +14 for scrollbar
        int listH = VISIBLE_ROWS * RECIPE_ENTRY_HEIGHT + 4;
        g.fill(listX - 1, listY - 1, listX + listW + 1, listY + listH + 1, SLOT_BORDER);
        g.fill(listX, listY, listX + listW, listY + listH, LIST_BG);

        // Recipe entries
        List<ArchitectsCutterMenu.OrnamentRecipe> recipes = this.menu.getAvailableRecipes();
        int selectedIdx = this.menu.getSelectedRecipeIndex();
        for (int i = 0; i < VISIBLE_ROWS; i++) {
            int recipeIdx = i + scrollOffset;
            if (recipeIdx >= recipes.size()) break;

            int entryX = listX + 2;
            int entryY = listY + 2 + i * RECIPE_ENTRY_HEIGHT;
            int entryW = RECIPE_ENTRY_WIDTH;
            int entryH = RECIPE_ENTRY_HEIGHT - 2;

            // Background: selected or hovered
            boolean isSelected = recipeIdx == selectedIdx;
            boolean isHovered = mouseX >= entryX && mouseX < entryX + entryW
                    && mouseY >= entryY && mouseY < entryY + entryH;

            if (isSelected) {
                g.fill(entryX, entryY, entryX + entryW, entryY + entryH, SELECTED);
            } else if (isHovered) {
                g.fill(entryX, entryY, entryX + entryW, entryY + entryH, HOVER);
            }

            // Recipe icon (the ornament block item)
            ArchitectsCutterMenu.OrnamentRecipe recipe = recipes.get(recipeIdx);
            Block ornamentBlock = OrnamentRegistry.getOrnamentBlock(recipe.type());
            if (ornamentBlock != null) {
                ItemStack icon = new ItemStack(ornamentBlock);
                g.renderItem(icon, entryX + 1, entryY + 1);
            }

            // Recipe name
            String name = recipe.getDisplayName();
            int maxTextWidth = entryW - 20;
            String trimmed = font.plainSubstrByWidth(name, maxTextWidth);
            g.drawString(font, trimmed, entryX + 19, entryY + 5,
                    isSelected ? GOLD : TEXT_DIM, false);
        }

        // Scrollbar
        if (recipes.size() > VISIBLE_ROWS) {
            int scrollBarX = listX + listW - 10;
            int scrollBarY = listY + 2;
            int scrollBarH = listH - 4;
            g.fill(scrollBarX, scrollBarY, scrollBarX + 8, scrollBarY + scrollBarH, SLOT_BORDER);

            int maxScroll = Math.max(1, recipes.size() - VISIBLE_ROWS);
            int thumbH = Math.max(8, scrollBarH * VISIBLE_ROWS / recipes.size());
            int thumbY = scrollBarY + (int) ((scrollBarH - thumbH) * ((float) scrollOffset / maxScroll));
            g.fill(scrollBarX + 1, thumbY, scrollBarX + 7, thumbY + thumbH, ACCENT);
        }

        // Output slot
        int ox = x + 142;
        int oy = y + 37;
        g.fill(ox, oy, ox + 18, oy + 18, SLOT_BORDER);
        g.fill(ox + 1, oy + 1, ox + 17, oy + 17, SLOT_BG);

        // Arrow from list to output
        g.fill(x + 126, y + 43, x + 140, y + 45, TEXT_DIM);

        // Player inventory slots
        int playerInvY = y + this.inventoryLabelY + 11;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int sx = x + 7 + col * 18;
                int sy = playerInvY - 1 + row * 18;
                g.fill(sx, sy, sx + 18, sy + 18, SLOT_BORDER);
                g.fill(sx + 1, sy + 1, sx + 17, sy + 17, SLOT_BG);
            }
        }

        // Player hotbar
        int hotbarY = playerInvY + 58;
        for (int col = 0; col < 9; col++) {
            int sx = x + 7 + col * 18;
            int sy = hotbarY - 1;
            g.fill(sx, sy, sx + 18, sy + 18, SLOT_BORDER);
            g.fill(sx + 1, sy + 1, sx + 17, sy + 17, SLOT_BG);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, GOLD, false);
        g.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, TEXT, false);

        // "Materials" label above input slots
        g.drawString(this.font, "Materials", 10, 7, TEXT_DIM, false);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        this.renderTooltip(g, mouseX, mouseY);
    }

    // ==================== Input Handling ====================

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            // Check recipe list clicks
            int listX = this.leftPos + 50;
            int listY = this.topPos + 14;
            int listW = RECIPE_ENTRY_WIDTH;
            int listH = VISIBLE_ROWS * RECIPE_ENTRY_HEIGHT + 4;

            if (mouseX >= listX + 2 && mouseX < listX + 2 + listW
                    && mouseY >= listY + 2 && mouseY < listY + listH - 2) {
                int clickedRow = (int) ((mouseY - listY - 2) / RECIPE_ENTRY_HEIGHT);
                int recipeIdx = clickedRow + scrollOffset;
                List<ArchitectsCutterMenu.OrnamentRecipe> recipes = this.menu.getAvailableRecipes();
                if (recipeIdx >= 0 && recipeIdx < recipes.size()) {
                    // Send selection to server
                    if (this.minecraft != null && this.minecraft.gameMode != null) {
                        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, recipeIdx);
                    }
                    return true;
                }
            }

            // Check scrollbar drag
            List<ArchitectsCutterMenu.OrnamentRecipe> recipes = this.menu.getAvailableRecipes();
            if (recipes.size() > VISIBLE_ROWS) {
                int scrollBarX = listX + RECIPE_ENTRY_WIDTH + 4;
                if (mouseX >= scrollBarX && mouseX < scrollBarX + 10
                        && mouseY >= listY && mouseY < listY + listH) {
                    scrolling = true;
                    return true;
                }
            }
        }
        return false; // let AbstractContainerScreen handle slot clicks — avoid calling super with old signature
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        scrolling = false;
        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (scrolling) {
            List<ArchitectsCutterMenu.OrnamentRecipe> recipes = this.menu.getAvailableRecipes();
            int maxScroll = Math.max(0, recipes.size() - VISIBLE_ROWS);
            int listY = this.topPos + 14;
            int listH = VISIBLE_ROWS * RECIPE_ENTRY_HEIGHT + 4;
            float ratio = Mth.clamp((float) (mouseY - listY) / listH, 0.0f, 1.0f);
            scrollOffset = Mth.clamp(Math.round(ratio * maxScroll), 0, maxScroll);
            return true;
        }
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        List<ArchitectsCutterMenu.OrnamentRecipe> recipes = this.menu.getAvailableRecipes();
        int maxScroll = Math.max(0, recipes.size() - VISIBLE_ROWS);
        scrollOffset = Mth.clamp(scrollOffset - (int) scrollY, 0, maxScroll);
        return true;
    }
}

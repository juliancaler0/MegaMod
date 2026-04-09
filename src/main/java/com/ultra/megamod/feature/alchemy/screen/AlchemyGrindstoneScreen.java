package com.ultra.megamod.feature.alchemy.screen;

import com.ultra.megamod.feature.alchemy.AlchemyRecipeRegistry;
import com.ultra.megamod.feature.alchemy.network.AlchemyGrindstonePayload;
import com.ultra.megamod.feature.alchemy.network.AlchemyGrindstoneSyncPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.List;

/**
 * Alchemy Grindstone screen - shows input/output areas, progress bar, and recipe list.
 */
public class AlchemyGrindstoneScreen extends Screen {

    private final BlockPos grindstonePos;
    private int syncTimer = 0;
    private int scrollOffset = 0;

    // Layout
    private static final int PANEL_W = 260;
    private static final int PANEL_H = 200;
    private static final int BG_COLOR = 0xEE1A1A2E;
    private static final int BORDER_COLOR = 0xFF6B5F3A;
    private static final int ACCENT_COLOR = 0xFFC8A84E;
    private static final int TITLE_COLOR = 0xFFFFD700;
    private static final int TEXT_COLOR = 0xFFCCCCDD;
    private static final int DIM_COLOR = 0xFF666677;
    private static final int SUCCESS_COLOR = 0xFF44FF44;
    private static final int SLOT_BG = 0xFF2A2A3E;
    private static final int SLOT_BORDER = 0xFF444466;
    private static final int PROGRESS_COLOR = 0xFFC8A84E;

    public AlchemyGrindstoneScreen(BlockPos pos) {
        super(Component.literal("Alchemy Grindstone"));
        this.grindstonePos = pos;
    }

    @Override
    protected void init() {
        super.init();
        requestSync();
    }

    @Override
    public void tick() {
        super.tick();
        syncTimer++;
        if (syncTimer % 10 == 0) {
            requestSync();
        }
    }

    private void requestSync() {
        ClientPacketDistributor.sendToServer(new AlchemyGrindstonePayload("request_sync", grindstonePos, ""));
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        super.render(gfx, mouseX, mouseY, partialTick);

        int left = (width - PANEL_W) / 2;
        int top = (height - PANEL_H) / 2;

        // Background
        gfx.fill(left - 1, top - 1, left + PANEL_W + 1, top + PANEL_H + 1, BORDER_COLOR);
        gfx.fill(left, top, left + PANEL_W, top + PANEL_H, BG_COLOR);

        // Title
        gfx.drawCenteredString(font, "\u00a7l\u00a76Alchemy Grindstone", left + PANEL_W / 2, top + 6, TITLE_COLOR);
        gfx.fill(left + 10, top + 18, left + PANEL_W - 10, top + 19, ACCENT_COLOR);

        // Status section
        int statusY = top + 24;

        if (AlchemyGrindstoneSyncPayload.clientGrinding) {
            gfx.drawString(font, "Status:", left + 10, statusY, TEXT_COLOR);
            gfx.drawString(font, "Grinding...", left + 60, statusY, ACCENT_COLOR);

            // Progress bar
            int progBarX = left + 10;
            int progBarY = statusY + 14;
            int progBarW = PANEL_W - 20;
            gfx.fill(progBarX, progBarY, progBarX + progBarW, progBarY + 14, SLOT_BG);
            gfx.fill(progBarX, progBarY, progBarX + progBarW, progBarY + 1, SLOT_BORDER);
            gfx.fill(progBarX, progBarY + 13, progBarX + progBarW, progBarY + 14, SLOT_BORDER);

            int total = AlchemyGrindstoneSyncPayload.clientTotal > 0 ? AlchemyGrindstoneSyncPayload.clientTotal : 100;
            float progress = (float) AlchemyGrindstoneSyncPayload.clientProgress / total;
            int fillW = (int) (progress * (progBarW - 2));
            gfx.fill(progBarX + 1, progBarY + 1, progBarX + 1 + fillW, progBarY + 13, PROGRESS_COLOR);

            int pct = (int) (progress * 100);
            gfx.drawCenteredString(font, pct + "%", progBarX + progBarW / 2, progBarY + 3, TEXT_COLOR);
        } else if (AlchemyGrindstoneSyncPayload.clientOutputReady) {
            gfx.drawString(font, "Status:", left + 10, statusY, TEXT_COLOR);
            gfx.drawString(font, "Output ready!", left + 60, statusY, SUCCESS_COLOR);

            // Collect button
            int btnX = left + 10;
            int btnY = statusY + 14;
            int btnW = 80;
            int btnH = 16;
            boolean btnHover = mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH;
            gfx.fill(btnX, btnY, btnX + btnW, btnY + btnH, btnHover ? 0xFF3A8E3A : 0xFF2A6E2A);
            gfx.fill(btnX, btnY, btnX + btnW, btnY + 1, SUCCESS_COLOR);
            gfx.fill(btnX, btnY + btnH - 1, btnX + btnW, btnY + btnH, SUCCESS_COLOR);
            gfx.drawCenteredString(font, "Collect", btnX + btnW / 2, btnY + 4, TEXT_COLOR);
        } else {
            gfx.drawString(font, "Status:", left + 10, statusY, TEXT_COLOR);
            gfx.drawString(font, "Idle - right-click with ingredient", left + 60, statusY, DIM_COLOR);
        }

        // Recipe list divider
        int recipeY = top + 62;
        gfx.fill(left + 10, recipeY, left + PANEL_W - 10, recipeY + 1, ACCENT_COLOR);
        gfx.drawString(font, "\u00a7lGrinding Recipes:", left + 10, recipeY + 4, TITLE_COLOR);

        // Recipe list
        List<AlchemyRecipeRegistry.GrindingRecipe> recipes = AlchemyRecipeRegistry.getAllGrindingRecipes();
        int listY = recipeY + 16;
        int visibleRows = 10;

        for (int i = scrollOffset; i < Math.min(recipes.size(), scrollOffset + visibleRows); i++) {
            AlchemyRecipeRegistry.GrindingRecipe recipe = recipes.get(i);
            int rowY = listY + (i - scrollOffset) * 12;

            // Alternate row background
            if ((i - scrollOffset) % 2 == 0) {
                gfx.fill(left + 8, rowY - 1, left + PANEL_W - 8, rowY + 11, 0x20FFFFFF);
            }

            // Input names
            StringBuilder inputs = new StringBuilder();
            for (String input : recipe.inputs()) {
                if (!inputs.isEmpty()) inputs.append(" + ");
                inputs.append(formatItemName(input));
            }

            // Output name
            String output = AlchemyRecipeRegistry.getReagentDisplayName(recipe.output()) + " x" + recipe.outputCount();

            // Draw: input -> output
            String line = inputs + " \u00a78\u2192 \u00a7e" + output;
            if (font.width(line) > PANEL_W - 20) {
                // Truncate
                line = inputs.toString().substring(0, Math.min(inputs.length(), 15)) + ".. \u00a78\u2192 \u00a7e" + output;
            }
            gfx.drawString(font, line, left + 12, rowY, TEXT_COLOR);
        }

        // Scroll indicators
        if (scrollOffset > 0) {
            gfx.drawCenteredString(font, "\u25B2", left + PANEL_W - 15, recipeY + 4, ACCENT_COLOR);
        }
        if (scrollOffset + visibleRows < recipes.size()) {
            gfx.drawCenteredString(font, "\u25BC", left + PANEL_W - 15, top + PANEL_H - 14, ACCENT_COLOR);
        }

        // Footer
        gfx.drawCenteredString(font, "\u00a77Right-click with items | Scroll for recipes", left + PANEL_W / 2, top + PANEL_H - 10, DIM_COLOR);
    }

    // Note: mouseClicked signature changed in 1.21.11 - avoid calling super with old signature.
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int left = (width - PANEL_W) / 2;
        int top = (height - PANEL_H) / 2;

        // Collect button
        if (AlchemyGrindstoneSyncPayload.clientOutputReady) {
            int statusY = top + 24;
            int btnX = left + 10;
            int btnY = statusY + 14;
            int btnW = 80;
            int btnH = 16;
            if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
                ClientPacketDistributor.sendToServer(new AlchemyGrindstonePayload("collect_output", grindstonePos, ""));
                return true;
            }
        }

        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        List<AlchemyRecipeRegistry.GrindingRecipe> recipes = AlchemyRecipeRegistry.getAllGrindingRecipes();
        int visibleRows = 10;

        if (scrollY < 0) {
            if (scrollOffset + visibleRows < recipes.size()) {
                scrollOffset++;
            }
        } else if (scrollY > 0) {
            if (scrollOffset > 0) {
                scrollOffset--;
            }
        }
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static String formatItemName(String itemId) {
        // "minecraft:blaze_powder" -> "Blaze Powder"
        String name = itemId;
        if (name.contains(":")) {
            name = name.substring(name.indexOf(":") + 1);
        }
        StringBuilder sb = new StringBuilder();
        for (String word : name.split("_")) {
            if (!sb.isEmpty()) sb.append(" ");
            sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return sb.toString();
    }
}

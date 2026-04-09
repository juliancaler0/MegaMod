package com.ultra.megamod.feature.alchemy.screen;

import com.ultra.megamod.feature.alchemy.AlchemyRecipeRegistry;
import com.ultra.megamod.feature.alchemy.network.AlchemyCauldronPayload;
import com.ultra.megamod.feature.alchemy.network.AlchemyCauldronSyncPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.ArrayList;
import java.util.List;

/**
 * Alchemy Cauldron screen - dark themed UI showing water level, ingredients, brewing progress, and output.
 */
public class AlchemyCauldronScreen extends Screen {

    private final BlockPos cauldronPos;
    private int syncTimer = 0;

    // Layout constants
    private static final int PANEL_W = 240;
    private static final int PANEL_H = 180;
    private static final int BG_COLOR = 0xEE1A1A2E;
    private static final int BORDER_COLOR = 0xFF6B3FA0;
    private static final int ACCENT_COLOR = 0xFF9B59B6;
    private static final int TITLE_COLOR = 0xFFD4A0FF;
    private static final int TEXT_COLOR = 0xFFCCCCDD;
    private static final int DIM_COLOR = 0xFF666677;
    private static final int SUCCESS_COLOR = 0xFF44FF44;
    private static final int ERROR_COLOR = 0xFFFF4444;
    private static final int WATER_COLOR = 0xFF3498DB;
    private static final int BREW_COLOR = 0xFF9B59B6;
    private static final int SLOT_BG = 0xFF2A2A3E;
    private static final int SLOT_BORDER = 0xFF444466;

    public AlchemyCauldronScreen(BlockPos pos) {
        super(Component.literal("Alchemy Cauldron"));
        this.cauldronPos = pos;
    }

    @Override
    protected void init() {
        super.init();
        // Request initial sync
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
        ClientPacketDistributor.sendToServer(new AlchemyCauldronPayload("request_sync", cauldronPos, ""));
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        super.render(gfx, mouseX, mouseY, partialTick);

        int left = (width - PANEL_W) / 2;
        int top = (height - PANEL_H) / 2;

        // Background panel
        gfx.fill(left - 1, top - 1, left + PANEL_W + 1, top + PANEL_H + 1, BORDER_COLOR);
        gfx.fill(left, top, left + PANEL_W, top + PANEL_H, BG_COLOR);

        // Title
        gfx.drawCenteredString(font, "\u00a7l\u00a7dAlchemy Cauldron", left + PANEL_W / 2, top + 6, TITLE_COLOR);

        // Horizontal divider
        gfx.fill(left + 10, top + 18, left + PANEL_W - 10, top + 19, ACCENT_COLOR);

        // Water level indicator
        int waterY = top + 24;
        gfx.drawString(font, "Water:", left + 10, waterY, TEXT_COLOR);
        int waterBarX = left + 55;
        int waterBarW = 60;
        gfx.fill(waterBarX, waterY, waterBarX + waterBarW, waterY + 10, SLOT_BG);
        gfx.fill(waterBarX, waterY, waterBarX + waterBarW, waterY + 1, SLOT_BORDER);
        gfx.fill(waterBarX, waterY + 9, waterBarX + waterBarW, waterY + 10, SLOT_BORDER);
        if (AlchemyCauldronSyncPayload.clientWater > 0) {
            gfx.fill(waterBarX + 1, waterY + 1, waterBarX + waterBarW - 1, waterY + 9, WATER_COLOR);
            gfx.drawString(font, "Full", waterBarX + waterBarW + 4, waterY + 1, SUCCESS_COLOR);
        } else {
            gfx.drawString(font, "Empty", waterBarX + waterBarW + 4, waterY + 1, ERROR_COLOR);
        }

        // Ingredient slots
        int slotsY = top + 42;
        gfx.drawString(font, "Ingredients:", left + 10, slotsY, TEXT_COLOR);

        String[] ingredients = AlchemyCauldronSyncPayload.clientIngredients.isEmpty() ?
                new String[0] : AlchemyCauldronSyncPayload.clientIngredients.split(",");

        for (int i = 0; i < 3; i++) {
            int slotX = left + 10 + i * 75;
            int slotY = slotsY + 14;

            // Slot background
            gfx.fill(slotX, slotY, slotX + 70, slotY + 24, SLOT_BG);
            gfx.fill(slotX, slotY, slotX + 70, slotY + 1, SLOT_BORDER);
            gfx.fill(slotX, slotY + 23, slotX + 70, slotY + 24, SLOT_BORDER);
            gfx.fill(slotX, slotY, slotX + 1, slotY + 24, SLOT_BORDER);
            gfx.fill(slotX + 69, slotY, slotX + 70, slotY + 24, SLOT_BORDER);

            if (i < ingredients.length && !ingredients[i].isEmpty()) {
                String displayName = AlchemyRecipeRegistry.getReagentDisplayName(ingredients[i].trim());
                // Truncate if too long
                if (font.width(displayName) > 66) {
                    displayName = displayName.substring(0, Math.min(displayName.length(), 8)) + "..";
                }
                gfx.drawString(font, displayName, slotX + 3, slotY + 8, ACCENT_COLOR);
            } else {
                gfx.drawString(font, "Empty", slotX + 20, slotY + 8, DIM_COLOR);
            }

            // Slot number
            gfx.drawString(font, String.valueOf(i + 1), slotX + 32, slotY + 2, DIM_COLOR);
        }

        // Brewing progress bar
        int progressY = slotsY + 48;
        gfx.drawString(font, "Brewing:", left + 10, progressY, TEXT_COLOR);
        int progBarX = left + 65;
        int progBarW = 155;
        gfx.fill(progBarX, progressY, progBarX + progBarW, progressY + 12, SLOT_BG);
        gfx.fill(progBarX, progressY, progBarX + progBarW, progressY + 1, SLOT_BORDER);
        gfx.fill(progBarX, progressY + 11, progBarX + progBarW, progressY + 12, SLOT_BORDER);

        int brewProgress = AlchemyCauldronSyncPayload.clientBrewProgress;
        if (brewProgress > 0) {
            int fillW = (int) ((brewProgress / 200.0f) * (progBarW - 2));
            gfx.fill(progBarX + 1, progressY + 1, progBarX + 1 + fillW, progressY + 11, BREW_COLOR);
        }

        int pct = (int) ((brewProgress / 200.0f) * 100);
        String progressText = brewProgress > 0 ? pct + "%" : "Idle";
        gfx.drawCenteredString(font, progressText, progBarX + progBarW / 2, progressY + 2, TEXT_COLOR);

        // Output section
        int outputY = progressY + 20;
        gfx.drawString(font, "Output:", left + 10, outputY, TEXT_COLOR);

        if (AlchemyCauldronSyncPayload.clientResultReady) {
            String potionName = AlchemyRecipeRegistry.getPotionDisplayName(AlchemyCauldronSyncPayload.clientOutputPotion);
            gfx.drawString(font, potionName, left + 60, outputY, SUCCESS_COLOR);
            gfx.drawString(font, "\u00a7aUse glass bottle to collect!", left + 10, outputY + 14, SUCCESS_COLOR);
        } else if (brewProgress > 0) {
            gfx.drawString(font, "Brewing in progress...", left + 60, outputY, ACCENT_COLOR);
        } else {
            gfx.drawString(font, "None", left + 60, outputY, DIM_COLOR);
        }

        // Recipe hints: if 2+ ingredients, show possible recipes
        if (ingredients.length >= 2) {
            int hintY = outputY + 28;
            gfx.fill(left + 10, hintY - 2, left + PANEL_W - 10, hintY - 1, ACCENT_COLOR);
            gfx.drawString(font, "Possible recipes:", left + 10, hintY, TITLE_COLOR);

            List<String> currentReagents = new ArrayList<>();
            for (String ing : ingredients) {
                if (!ing.trim().isEmpty()) currentReagents.add(ing.trim());
            }

            List<AlchemyRecipeRegistry.BrewingRecipe> possible = AlchemyRecipeRegistry.getPossibleRecipes(currentReagents);
            int hintRow = 0;
            for (AlchemyRecipeRegistry.BrewingRecipe recipe : possible) {
                if (hintRow >= 3) break;
                String name = AlchemyRecipeRegistry.getPotionDisplayName(recipe.output());
                String tier = " (T" + recipe.tier() + ")";
                gfx.drawString(font, "  " + name + tier, left + 10, hintY + 12 + hintRow * 10, DIM_COLOR);
                hintRow++;
            }
            if (possible.isEmpty()) {
                gfx.drawString(font, "  No matching recipes.", left + 10, hintY + 12, ERROR_COLOR);
            }
        }

        // Stir button
        int btnX = left + PANEL_W - 60;
        int btnY = progressY - 2;
        int btnW = 50;
        int btnH = 14;
        boolean btnHover = mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH;
        gfx.fill(btnX, btnY, btnX + btnW, btnY + btnH, btnHover ? 0xFF7B4FB0 : ACCENT_COLOR);
        gfx.drawCenteredString(font, "Stir", btnX + btnW / 2, btnY + 3, TEXT_COLOR);

        // Instructions at bottom
        gfx.drawCenteredString(font, "\u00a77Right-click with items | ESC to close", left + PANEL_W / 2, top + PANEL_H - 12, DIM_COLOR);
    }

    // Note: mouseClicked signature changed in 1.21.11 - we use the old (double,double,int) form
    // and avoid calling super which has a new signature.
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int left = (width - PANEL_W) / 2;
        int top = (height - PANEL_H) / 2;

        // Stir button
        int progressY = top + 42 + 48;
        int btnX = left + PANEL_W - 60;
        int btnY = progressY - 2;
        int btnW = 50;
        int btnH = 14;

        if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
            ClientPacketDistributor.sendToServer(new AlchemyCauldronPayload("stir", cauldronPos, ""));
            return true;
        }

        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

package com.ultra.megamod.feature.combat.runes.client;

import com.ultra.megamod.feature.combat.runes.RuneCraftingMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.ItemCombinerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * Client-side screen for the Rune Crafting Altar.
 * Uses the smithing table UI layout.
 * Ported 1:1 from the Runes mod's RuneCraftingScreen.
 */
public class RuneCraftingScreen extends ItemCombinerScreen<RuneCraftingMenu> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("megamod", "textures/gui/crafting_altar.png");

    public RuneCraftingScreen(RuneCraftingMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, TEXTURE);
        this.titleLabelX = 60;
        this.titleLabelY = 18;
    }

    @Override
    protected void renderErrorIcon(GuiGraphics graphics, int x, int y) {
        // No error icon — rune crafting always shows valid state via createResult()
    }
}

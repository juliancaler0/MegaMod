package com.ultra.megamod.feature.combat.runes.pouch;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * Client-side screen for a rune pouch. Uses vanilla chest texture (generic_54).
 * Dynamic height based on pouch capacity — small (9) / medium (18) / large (27).
 */
public class RunePouchScreen extends AbstractContainerScreen<RunePouchMenu> {
    private static final Identifier BG = Identifier.withDefaultNamespace("textures/gui/container/generic_54.png");

    private final int pouchSlotCount;
    private final int pouchRows;

    public RunePouchScreen(RunePouchMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.pouchSlotCount = countPouchSlots(menu);
        this.pouchRows = (pouchSlotCount + 8) / 9;
        this.imageHeight = 114 + this.pouchRows * 18;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    private static int countPouchSlots(RunePouchMenu menu) {
        int count = 0;
        for (var slot : menu.slots) {
            if (!(slot.container instanceof Inventory)) count++;
        }
        return count;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        int topStripHeight = pouchRows * 18 + 17;
        // Top part (title strip + pouch slots) pulled from the top of the generic_54 atlas
        g.blit(RenderPipelines.GUI_TEXTURED, BG,
                x, y, 0f, 0f, imageWidth, topStripHeight, 256, 256);
        // Bottom part (player inventory) pulled from the bottom of the atlas
        g.blit(RenderPipelines.GUI_TEXTURED, BG,
                x, y + topStripHeight, 0f, 126f, imageWidth, 96, 256, 256);
    }
}

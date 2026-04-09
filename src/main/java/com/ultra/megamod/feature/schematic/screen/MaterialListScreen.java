package com.ultra.megamod.feature.schematic.screen;

import com.ultra.megamod.feature.schematic.client.SchematicPlacementMode;
import com.ultra.megamod.feature.schematic.data.MaterialListCalculator;
import com.ultra.megamod.feature.schematic.data.MaterialListEntry;
import com.ultra.megamod.feature.schematic.placement.SchematicPlacement;
import com.ultra.megamod.feature.ui.UIHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays the material list for a schematic placement.
 */
public class MaterialListScreen extends Screen {

    private List<MaterialListEntry> materials = new ArrayList<>();
    private int scrollOffset = 0;
    private static final int ENTRY_HEIGHT = 18;
    private static final int VISIBLE_ENTRIES = 14;

    public MaterialListScreen() {
        super(Component.literal("Material List"));
    }

    @Override
    protected void init() {
        super.init();
        calculateMaterials();
    }

    private void calculateMaterials() {
        SchematicPlacement placement = SchematicPlacementMode.getActivePlacement();
        if (placement != null) {
            materials = MaterialListCalculator.calculate(placement, null);
        } else {
            materials = new ArrayList<>();
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        super.render(g, mouseX, mouseY, delta);

        int panelW = 300;
        int panelH = 310;
        int px = (width - panelW) / 2;
        int py = (height - panelH) / 2;

        UIHelper.drawPanel(g, px, py, panelW, panelH);

        // Title
        g.drawCenteredString(font, "Material List", width / 2, py + 6, UIHelper.BLUE_ACCENT);

        // Summary
        int totalNeeded = MaterialListCalculator.getTotalBlocksNeeded(materials);
        int totalMissing = MaterialListCalculator.getTotalBlocksMissing(materials);
        g.drawCenteredString(font, materials.size() + " types, " + totalNeeded + " blocks total",
                width / 2, py + 18, UIHelper.GOLD_MID);

        // Column headers
        int listX = px + 8;
        int listY = py + 34;
        int listW = panelW - 16;

        g.drawString(font, "Item", listX + 20, listY, UIHelper.GOLD_DARK, false);
        g.drawString(font, "Need", listX + listW - 80, listY, UIHelper.GOLD_DARK, false);
        g.drawString(font, "Have", listX + listW - 45, listY, UIHelper.GOLD_DARK, false);
        listY += 12;

        // Material entries
        for (int i = 0; i < VISIBLE_ENTRIES && i + scrollOffset < materials.size(); i++) {
            int idx = i + scrollOffset;
            MaterialListEntry entry = materials.get(idx);
            int ey = listY + i * ENTRY_HEIGHT;

            boolean hovered = mouseX >= listX && mouseX <= listX + listW
                    && mouseY >= ey && mouseY <= ey + ENTRY_HEIGHT - 1;
            g.fill(listX, ey, listX + listW, ey + ENTRY_HEIGHT - 1,
                    hovered ? 0xFF1E1E30 : (idx % 2 == 0 ? 0xFF141420 : 0xFF161626));

            // Item icon
            ItemStack displayStack = entry.getDisplayStack();
            g.renderItem(displayStack, listX + 1, ey);

            // Item name (truncated)
            String name = displayStack.getHoverName().getString();
            if (font.width(name) > listW - 100) {
                name = font.plainSubstrByWidth(name, listW - 106) + "...";
            }
            g.drawString(font, name, listX + 20, ey + 4, UIHelper.CREAM_TEXT, false);

            // Count needed
            g.drawString(font, String.valueOf(entry.getCountTotal()),
                    listX + listW - 80, ey + 4, UIHelper.CREAM_TEXT, false);

            // Count available
            int availColor = entry.isComplete() ? UIHelper.XP_GREEN : 0xFFFF5555;
            g.drawString(font, String.valueOf(entry.getCountAvailable()),
                    listX + listW - 45, ey + 4, availColor, false);
        }

        // Scrollbar
        if (materials.size() > VISIBLE_ENTRIES) {
            int scrollBarX = px + panelW - 10;
            int scrollBarH = VISIBLE_ENTRIES * ENTRY_HEIGHT;
            g.fill(scrollBarX, listY, scrollBarX + 4, listY + scrollBarH, 0xFF0E0E18);

            float ratio = (float) scrollOffset / (materials.size() - VISIBLE_ENTRIES);
            int thumbH = Math.max(10, scrollBarH * VISIBLE_ENTRIES / materials.size());
            int thumbY = listY + (int) ((scrollBarH - thumbH) * ratio);
            g.fill(scrollBarX, thumbY, scrollBarX + 4, thumbY + thumbH, 0xFF555577);
        }

        // Close button
        int btnY = py + panelH - 24;
        drawButton(g, px + panelW / 2 - 40, btnY, 80, 18, "Close", mouseX, mouseY);
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean consumed) {
        if (consumed) return super.mouseClicked(event, consumed);
        double mouseX = event.x();
        double mouseY = event.y();
        int panelW = 300;
        int panelH = 310;
        int px = (width - panelW) / 2;
        int py = (height - panelH) / 2;

        if (isOver(px + panelW / 2 - 40, py + panelH - 24, 80, 18, mouseX, mouseY)) {
            onClose();
            return true;
        }

        return super.mouseClicked(event, consumed);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY > 0 && scrollOffset > 0) {
            scrollOffset--;
        } else if (scrollY < 0 && scrollOffset < materials.size() - VISIBLE_ENTRIES) {
            scrollOffset++;
        }
        return true;
    }

    private void drawButton(GuiGraphics g, int x, int y, int w, int h,
                            String text, int mouseX, int mouseY) {
        boolean hovered = isOver(x, y, w, h, mouseX, mouseY);
        g.fill(x, y, x + w, y + h, hovered ? 0xFF2A2A4A : 0xFF1C1C28);
        g.drawCenteredString(font, text, x + w / 2, y + (h - 8) / 2,
                hovered ? 0xFF55FFFF : UIHelper.CREAM_TEXT);
    }

    private boolean isOver(int x, int y, int w, int h, double mx, double my) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    @Override
    public boolean isPauseScreen() { return false; }
}

package com.ultra.megamod.feature.citizen.blueprint.screen;

import com.ultra.megamod.feature.citizen.blueprint.packs.StructurePackMeta;
import com.ultra.megamod.feature.citizen.blueprint.packs.StructurePacks;
import com.ultra.megamod.feature.ui.UIHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Browse available style packs.
 * Shows a list of loaded packs with name, author, description, and blueprint count.
 * Allows the player to select the active pack for building.
 */
public class PackBrowserScreen extends Screen {

    private static final int PANEL_W = 360;
    private static final int PANEL_H = 280;
    private static final int ENTRY_HEIGHT = 48;
    private static final int VISIBLE_ENTRIES = 4;

    private final List<StructurePackMeta> packs = new ArrayList<>();
    private int scrollOffset = 0;
    private int selectedIndex = -1;

    public PackBrowserScreen() {
        super(Component.literal("Style Pack Browser"));
    }

    @Override
    protected void init() {
        super.init();
        refreshPacks();
    }

    private void refreshPacks() {
        packs.clear();
        packs.addAll(StructurePacks.getAllPacks());

        // Find currently selected
        for (int i = 0; i < packs.size(); i++) {
            if (packs.get(i).getId().equals(StructurePacks.selectedPack)) {
                selectedIndex = i;
                break;
            }
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        super.render(g, mouseX, mouseY, delta);

        int px = (width - PANEL_W) / 2;
        int py = (height - PANEL_H) / 2;

        UIHelper.drawPanel(g, px, py, PANEL_W, PANEL_H);

        // Title
        g.drawCenteredString(font, "Style Pack Browser", width / 2, py + 8, UIHelper.BLUE_ACCENT);
        g.drawCenteredString(font, packs.size() + " pack(s) available", width / 2, py + 20, UIHelper.GOLD_MID);

        // Pack list
        int listX = px + 8;
        int listY = py + 34;
        int listW = PANEL_W - 16;

        if (packs.isEmpty()) {
            g.drawCenteredString(font, "No packs found.", width / 2, py + PANEL_H / 2, UIHelper.GOLD_MID);
            g.drawCenteredString(font, "Place packs in blueprints/megamod/", width / 2, py + PANEL_H / 2 + 14, UIHelper.GOLD_MID);
        } else {
            for (int i = 0; i < VISIBLE_ENTRIES && i + scrollOffset < packs.size(); i++) {
                int idx = i + scrollOffset;
                StructurePackMeta pack = packs.get(idx);
                int ey = listY + i * ENTRY_HEIGHT;

                boolean hovered = mouseX >= listX && mouseX <= listX + listW
                        && mouseY >= ey && mouseY <= ey + ENTRY_HEIGHT - 2;
                boolean selected = idx == selectedIndex;
                boolean isActive = pack.getId().equals(StructurePacks.selectedPack);

                int bgColor = selected ? 0xFF2A2A4A : (hovered ? 0xFF1E1E30 : 0xFF141420);
                g.fill(listX, ey, listX + listW, ey + ENTRY_HEIGHT - 2, bgColor);

                // Active indicator
                if (isActive) {
                    g.fill(listX, ey, listX + 3, ey + ENTRY_HEIGHT - 2, 0xFF55FF55);
                }

                // Pack name
                int nameColor = isActive ? 0xFF55FFFF : (selected ? 0xFFDDDDEE : 0xFFCCCCDD);
                g.drawString(font, pack.getName(), listX + 8, ey + 4, nameColor, false);

                // Version + author
                String metaLine = "v" + pack.getVersion();
                if (!pack.getAuthorsString().isEmpty()) {
                    metaLine += " by " + pack.getAuthorsString();
                }
                g.drawString(font, font.plainSubstrByWidth(metaLine, listW - 16),
                        listX + 8, ey + 16, UIHelper.GOLD_MID, false);

                // Description
                if (!pack.getDescription().isEmpty()) {
                    g.drawString(font, font.plainSubstrByWidth(pack.getDescription(), listW - 16),
                            listX + 8, ey + 28, 0xFF888899, false);
                }

                // Blueprint count
                int blueprintCount = StructurePacks.getBlueprintList(pack.getId()).size();
                String countStr = blueprintCount + " blueprints";
                g.drawString(font, countStr,
                        listX + listW - font.width(countStr) - 8, ey + 4, UIHelper.GOLD_MID, false);
            }
        }

        // Scrollbar
        if (packs.size() > VISIBLE_ENTRIES) {
            int scrollBarX = px + PANEL_W - 12;
            int scrollBarY = listY;
            int scrollBarH = VISIBLE_ENTRIES * ENTRY_HEIGHT;
            g.fill(scrollBarX, scrollBarY, scrollBarX + 4, scrollBarY + scrollBarH, 0xFF0A0A14);

            float ratio = (float) scrollOffset / (packs.size() - VISIBLE_ENTRIES);
            int thumbH = Math.max(10, scrollBarH * VISIBLE_ENTRIES / packs.size());
            int thumbY = scrollBarY + (int) ((scrollBarH - thumbH) * ratio);
            g.fill(scrollBarX, thumbY, scrollBarX + 4, thumbY + thumbH, 0xFF555577);
        }

        // Bottom buttons
        int btnY = py + PANEL_H - 30;
        boolean canSelect = selectedIndex >= 0 && selectedIndex < packs.size();
        drawButton(g, px + PANEL_W / 2 - 100, btnY, 90, 20, "Select", mouseX, mouseY, canSelect);
        drawButton(g, px + PANEL_W / 2 + 10, btnY, 90, 20, "Close", mouseX, mouseY, true);
    }

    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean consumed) {
        if (consumed) return super.mouseClicked(event, consumed);

        double mx = event.x();
        double my = event.y();

        int px = (width - PANEL_W) / 2;
        int py = (height - PANEL_H) / 2;
        int listX = px + 8;
        int listY = py + 34;
        int listW = PANEL_W - 16;

        // Pack list clicks
        for (int i = 0; i < VISIBLE_ENTRIES && i + scrollOffset < packs.size(); i++) {
            int idx = i + scrollOffset;
            int ey = listY + i * ENTRY_HEIGHT;
            if (mx >= listX && mx <= listX + listW && my >= ey && my <= ey + ENTRY_HEIGHT - 2) {
                if (idx == selectedIndex) {
                    // Double-click to select
                    selectPack();
                    return true;
                }
                selectedIndex = idx;
                return true;
            }
        }

        // Select button
        int btnY = py + PANEL_H - 30;
        if (isHovered(px + PANEL_W / 2 - 100, btnY, 90, 20, mx, my)) {
            selectPack();
            return true;
        }

        // Close button
        if (isHovered(px + PANEL_W / 2 + 10, btnY, 90, 20, mx, my)) {
            onClose();
            return true;
        }

        return super.mouseClicked(event, consumed);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int maxScroll = Math.max(0, packs.size() - VISIBLE_ENTRIES);
        if (scrollY > 0 && scrollOffset > 0) {
            scrollOffset--;
        } else if (scrollY < 0 && scrollOffset < maxScroll) {
            scrollOffset++;
        }
        return true;
    }

    private void selectPack() {
        if (selectedIndex >= 0 && selectedIndex < packs.size()) {
            StructurePacks.setSelectedPack(packs.get(selectedIndex).getId());
        }
    }

    private void drawButton(GuiGraphics g, int x, int y, int w, int h,
                            String text, int mouseX, int mouseY, boolean enabled) {
        boolean hovered = enabled && mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        int bg = enabled ? (hovered ? 0xFF2A2A4A : 0xFF1C1C28) : 0xFF0E0E18;
        int textColor = enabled ? (hovered ? 0xFF55FFFF : UIHelper.CREAM_TEXT) : 0xFF555566;
        g.fill(x, y, x + w, y + h, bg);
        g.fill(x, y, x + w, y + 1, 0xFF2A2A3A);
        g.drawCenteredString(font, text, x + w / 2, y + (h - 8) / 2, textColor);
    }

    private boolean isHovered(int x, int y, int w, int h, double mx, double my) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

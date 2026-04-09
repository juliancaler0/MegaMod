package com.ultra.megamod.feature.schematic.screen;

import com.ultra.megamod.feature.schematic.client.SchematicPlacementMode;
import com.ultra.megamod.feature.schematic.data.SchematicData;
import com.ultra.megamod.feature.schematic.data.SchematicLoader;
import com.ultra.megamod.feature.ui.UIHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * File browser for selecting .litematic schematic files.
 * Lists files from the <minecraft>/schematics/ directory.
 */
public class SchematicBrowserScreen extends Screen {

    private final List<SchematicFileEntry> files = new ArrayList<>();
    private int scrollOffset = 0;
    private int selectedIndex = -1;
    private static final int ENTRY_HEIGHT = 20;
    private static final int VISIBLE_ENTRIES = 12;

    /** If opened from a Builder citizen, this is their entity ID. -1 otherwise. */
    private final int builderEntityId;

    public SchematicBrowserScreen() {
        this(-1);
    }

    public SchematicBrowserScreen(int builderEntityId) {
        super(Component.literal("Schematic Browser"));
        this.builderEntityId = builderEntityId;
    }

    @Override
    protected void init() {
        super.init();
        loadFileList();
    }

    private void loadFileList() {
        files.clear();
        Path schematicsDir = Minecraft.getInstance().gameDirectory.toPath().resolve("schematics");

        if (!Files.exists(schematicsDir)) {
            try {
                Files.createDirectories(schematicsDir);
            } catch (IOException ignored) {}
            return;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(schematicsDir, "*.litematic")) {
            for (Path path : stream) {
                long size = Files.size(path);
                String name = path.getFileName().toString();
                files.add(new SchematicFileEntry(name, path, size));
            }
        } catch (IOException ignored) {}

        // Sort by name
        files.sort((a, b) -> a.name.compareToIgnoreCase(b.name));
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        super.render(g, mouseX, mouseY, delta);

        int panelW = 320;
        int panelH = 300;
        int px = (width - panelW) / 2;
        int py = (height - panelH) / 2;

        UIHelper.drawPanel(g, px, py, panelW, panelH);

        // Title
        g.drawCenteredString(font, "Schematic Browser", width / 2, py + 6, UIHelper.BLUE_ACCENT);
        g.drawCenteredString(font, files.size() + " files found", width / 2, py + 18, UIHelper.GOLD_MID);

        // File list
        int listX = px + 8;
        int listY = py + 32;
        int listW = panelW - 16;

        for (int i = 0; i < VISIBLE_ENTRIES && i + scrollOffset < files.size(); i++) {
            int idx = i + scrollOffset;
            SchematicFileEntry entry = files.get(idx);
            int ey = listY + i * ENTRY_HEIGHT;

            boolean hovered = mouseX >= listX && mouseX <= listX + listW
                    && mouseY >= ey && mouseY <= ey + ENTRY_HEIGHT - 1;
            boolean selected = idx == selectedIndex;

            int bgColor = selected ? 0xFF2A2A4A : (hovered ? 0xFF1E1E30 : 0xFF141420);
            g.fill(listX, ey, listX + listW, ey + ENTRY_HEIGHT - 1, bgColor);

            // File name
            String displayName = entry.name;
            if (displayName.endsWith(".litematic")) {
                displayName = displayName.substring(0, displayName.length() - 10);
            }
            g.drawString(font, displayName, listX + 4, ey + 2, selected ? 0xFF55FFFF : 0xFFCCCCDD, false);

            // File size
            String sizeStr = formatSize(entry.size);
            g.drawString(font, sizeStr, listX + listW - font.width(sizeStr) - 4, ey + 2, 0xFF888899, false);
        }

        // Scrollbar
        if (files.size() > VISIBLE_ENTRIES) {
            int scrollBarX = px + panelW - 10;
            int scrollBarY = listY;
            int scrollBarH = VISIBLE_ENTRIES * ENTRY_HEIGHT;
            g.fill(scrollBarX, scrollBarY, scrollBarX + 4, scrollBarY + scrollBarH, 0xFF0E0E18);

            float ratio = (float) scrollOffset / (files.size() - VISIBLE_ENTRIES);
            int thumbH = Math.max(10, scrollBarH * VISIBLE_ENTRIES / files.size());
            int thumbY = scrollBarY + (int) ((scrollBarH - thumbH) * ratio);
            g.fill(scrollBarX, thumbY, scrollBarX + 4, thumbY + thumbH, 0xFF555577);
        }

        // Bottom buttons
        int btnY = py + panelH - 26;
        int btnW = 80;

        // Load button
        boolean canLoad = selectedIndex >= 0 && selectedIndex < files.size();
        drawButton(g, px + panelW / 2 - btnW - 8, btnY, btnW, 20,
                "Load", mouseX, mouseY, canLoad);

        // Cancel button
        drawButton(g, px + panelW / 2 + 8, btnY, btnW, 20,
                "Cancel", mouseX, mouseY, true);

        // Help text
        if (files.isEmpty()) {
            g.drawCenteredString(font, "Place .litematic files in the schematics/ folder",
                    width / 2, py + panelH / 2, 0xFF888899);
        }
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean consumed) {
        if (consumed) return super.mouseClicked(event, consumed);
        double mouseX = event.x();
        double mouseY = event.y();
        int panelW = 320;
        int panelH = 300;
        int px = (width - panelW) / 2;
        int py = (height - panelH) / 2;
        int listX = px + 8;
        int listY = py + 32;
        int listW = panelW - 16;

        // Check file list clicks
        for (int i = 0; i < VISIBLE_ENTRIES && i + scrollOffset < files.size(); i++) {
            int idx = i + scrollOffset;
            int ey = listY + i * ENTRY_HEIGHT;
            if (mouseX >= listX && mouseX <= listX + listW && mouseY >= ey && mouseY <= ey + ENTRY_HEIGHT - 1) {
                if (idx == selectedIndex) {
                    loadSelected();
                    return true;
                }
                selectedIndex = idx;
                return true;
            }
        }

        // Check buttons
        int btnY = py + panelH - 26;
        int btnW = 80;

        if (isButtonHovered(px + panelW / 2 - btnW - 8, btnY, btnW, 20, mouseX, mouseY)) {
            loadSelected();
            return true;
        }
        if (isButtonHovered(px + panelW / 2 + 8, btnY, btnW, 20, mouseX, mouseY)) {
            onClose();
            return true;
        }

        return super.mouseClicked(event, consumed);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY > 0 && scrollOffset > 0) {
            scrollOffset--;
        } else if (scrollY < 0 && scrollOffset < files.size() - VISIBLE_ENTRIES) {
            scrollOffset++;
        }
        return true;
    }

    private void loadSelected() {
        if (selectedIndex < 0 || selectedIndex >= files.size()) return;
        SchematicFileEntry entry = files.get(selectedIndex);

        SchematicData schematic = SchematicLoader.load(entry.path);
        if (schematic != null) {
            SchematicPlacementMode.startPlacement(schematic);
            Minecraft.getInstance().setScreen(new SchematicPlacementScreen(builderEntityId));
        }
    }

    private void drawButton(GuiGraphics g, int x, int y, int w, int h,
                            String text, int mouseX, int mouseY, boolean enabled) {
        boolean hovered = enabled && mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        int bg = enabled ? (hovered ? 0xFF2A2A4A : 0xFF1C1C28) : 0xFF0E0E18;
        int textColor = enabled ? (hovered ? 0xFF55FFFF : 0xFFCCCCDD) : 0xFF555566;
        g.fill(x, y, x + w, y + h, bg);
        g.fill(x, y, x + w, y + 1, 0xFF2A2A3A);
        g.drawCenteredString(font, text, x + w / 2, y + (h - 8) / 2, textColor);
    }

    private boolean isButtonHovered(int x, int y, int w, int h, double mx, double my) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private static String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }

    @Override
    public boolean isPauseScreen() { return false; }

    private record SchematicFileEntry(String name, Path path, long size) {}
}

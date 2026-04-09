package com.ultra.megamod.feature.citizen.blueprint.screen;

import com.ultra.megamod.feature.citizen.blueprint.BlockInfo;
import com.ultra.megamod.feature.citizen.blueprint.RotationMirror;
import com.ultra.megamod.feature.citizen.blueprint.client.BlueprintRenderer;
import com.ultra.megamod.feature.citizen.blueprint.network.BuildToolPlacePayload;
import com.ultra.megamod.feature.citizen.blueprint.packs.StructurePackMeta;
import com.ultra.megamod.feature.citizen.blueprint.packs.StructurePacks;
import com.ultra.megamod.feature.ui.UIHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.ArrayList;
import java.util.List;

/**
 * Blueprint browser and placement screen.
 * Left panel shows a pack selector and scrollable blueprint file list.
 * Right panel shows preview info, rotation buttons, mirror toggle, and place/cancel buttons.
 */
public class BuildToolScreen extends Screen {

    private static final int PANEL_W = 420;
    private static final int PANEL_H = 300;
    private static final int ENTRY_HEIGHT = 16;
    private static final int VISIBLE_ENTRIES = 14;
    private static final int LEFT_PANEL_W = 200;

    private final BlockPos anchorPos;

    private List<String> blueprintFiles = new ArrayList<>();
    private List<String> packIds = new ArrayList<>();
    private int selectedPackIndex = 0;
    private int selectedFileIndex = -1;
    private int scrollOffset = 0;
    private RotationMirror rotationMirror = RotationMirror.NONE;

    public BuildToolScreen(BlockPos anchorPos) {
        super(Component.literal("Build Tool"));
        this.anchorPos = anchorPos != null ? anchorPos : BlockPos.ZERO;
    }

    @Override
    protected void init() {
        super.init();
        refreshPackList();
        refreshFileList();
    }

    private void refreshPackList() {
        packIds.clear();
        packIds.addAll(StructurePacks.loadedPacks.keySet());

        // Find index of currently selected pack
        selectedPackIndex = Math.max(0, packIds.indexOf(StructurePacks.selectedPack));
    }

    private void refreshFileList() {
        blueprintFiles.clear();
        selectedFileIndex = -1;
        scrollOffset = 0;

        if (!packIds.isEmpty() && selectedPackIndex >= 0 && selectedPackIndex < packIds.size()) {
            String packId = packIds.get(selectedPackIndex);
            blueprintFiles = StructurePacks.getBlueprintList(packId);
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        super.render(g, mouseX, mouseY, delta);

        int px = (width - PANEL_W) / 2;
        int py = (height - PANEL_H) / 2;

        UIHelper.drawPanel(g, px, py, PANEL_W, PANEL_H);

        // Title
        g.drawCenteredString(font, "Build Tool", width / 2, py + 6, UIHelper.BLUE_ACCENT);

        // === Left panel: Pack selector + file list ===
        int leftX = px + 6;
        int leftY = py + 22;

        // Pack selector
        g.drawString(font, "Pack:", leftX, leftY, UIHelper.GOLD_MID, false);
        String currentPackName = "None";
        if (!packIds.isEmpty() && selectedPackIndex < packIds.size()) {
            StructurePackMeta meta = StructurePacks.getPack(packIds.get(selectedPackIndex));
            currentPackName = meta != null ? meta.getName() : packIds.get(selectedPackIndex);
        }

        // Pack name with prev/next arrows
        int packBtnY = leftY - 1;
        drawSmallButton(g, leftX + 30, packBtnY, 12, 14, "<", mouseX, mouseY, packIds.size() > 1);
        g.drawString(font, currentPackName, leftX + 46, leftY, UIHelper.CREAM_TEXT, false);
        int nameW = font.width(currentPackName);
        drawSmallButton(g, leftX + 48 + nameW, packBtnY, 12, 14, ">", mouseX, mouseY, packIds.size() > 1);

        // File list
        int listX = leftX;
        int listY = leftY + 18;
        int listW = LEFT_PANEL_W - 12;

        g.fill(listX, listY, listX + listW, listY + VISIBLE_ENTRIES * ENTRY_HEIGHT, 0xFF0E0E18);

        for (int i = 0; i < VISIBLE_ENTRIES && i + scrollOffset < blueprintFiles.size(); i++) {
            int idx = i + scrollOffset;
            String file = blueprintFiles.get(idx);
            int ey = listY + i * ENTRY_HEIGHT;

            boolean hovered = mouseX >= listX && mouseX <= listX + listW
                    && mouseY >= ey && mouseY <= ey + ENTRY_HEIGHT - 1;
            boolean selected = idx == selectedFileIndex;

            int bgColor = selected ? 0xFF2A2A4A : (hovered ? 0xFF1E1E30 : 0xFF141420);
            g.fill(listX, ey, listX + listW, ey + ENTRY_HEIGHT - 1, bgColor);

            // Display name without .blueprint extension and path
            String displayName = file;
            if (displayName.endsWith(".blueprint")) {
                displayName = displayName.substring(0, displayName.length() - 10);
            }
            int lastSlash = displayName.lastIndexOf('/');
            if (lastSlash >= 0) {
                displayName = displayName.substring(lastSlash + 1);
            }

            int textColor = selected ? 0xFF55FFFF : (hovered ? UIHelper.CREAM_TEXT : 0xFFAAAAAA);
            g.drawString(font, font.plainSubstrByWidth(displayName, listW - 4), listX + 3, ey + 4, textColor, false);
        }

        // Scrollbar
        if (blueprintFiles.size() > VISIBLE_ENTRIES) {
            int scrollBarX = listX + listW - 4;
            int scrollBarH = VISIBLE_ENTRIES * ENTRY_HEIGHT;
            float ratio = (float) scrollOffset / (blueprintFiles.size() - VISIBLE_ENTRIES);
            int thumbH = Math.max(8, scrollBarH * VISIBLE_ENTRIES / blueprintFiles.size());
            int thumbY = listY + (int) ((scrollBarH - thumbH) * ratio);
            g.fill(scrollBarX, listY, scrollBarX + 4, listY + scrollBarH, 0xFF0A0A14);
            g.fill(scrollBarX, thumbY, scrollBarX + 4, thumbY + thumbH, 0xFF555577);
        }

        // File count
        g.drawString(font, blueprintFiles.size() + " blueprints", leftX, listY + VISIBLE_ENTRIES * ENTRY_HEIGHT + 2, UIHelper.GOLD_MID, false);

        // === Right panel: Preview info + controls ===
        int rightX = px + LEFT_PANEL_W + 14;
        int rightY = py + 22;

        g.drawString(font, "Preview", rightX, rightY, UIHelper.BLUE_ACCENT, false);

        if (selectedFileIndex >= 0 && selectedFileIndex < blueprintFiles.size()) {
            String selectedFile = blueprintFiles.get(selectedFileIndex);
            String displayPath = selectedFile;
            if (displayPath.endsWith(".blueprint")) {
                displayPath = displayPath.substring(0, displayPath.length() - 10);
            }

            g.drawString(font, "File:", rightX, rightY + 16, UIHelper.GOLD_MID, false);
            g.drawString(font, font.plainSubstrByWidth(displayPath, PANEL_W - LEFT_PANEL_W - 28),
                    rightX + 30, rightY + 16, UIHelper.CREAM_TEXT, false);

            // Pack info
            if (!packIds.isEmpty() && selectedPackIndex < packIds.size()) {
                StructurePackMeta meta = StructurePacks.getPack(packIds.get(selectedPackIndex));
                if (meta != null) {
                    g.drawString(font, "Authors:", rightX, rightY + 30, UIHelper.GOLD_MID, false);
                    g.drawString(font, meta.getAuthorsString().isEmpty() ? "Unknown" : meta.getAuthorsString(),
                            rightX + 48, rightY + 30, UIHelper.CREAM_TEXT, false);
                }
            }

            // Position info
            g.drawString(font, "Position:", rightX, rightY + 48, UIHelper.GOLD_MID, false);
            g.drawString(font, anchorPos.getX() + ", " + anchorPos.getY() + ", " + anchorPos.getZ(),
                    rightX + 52, rightY + 48, UIHelper.CREAM_TEXT, false);
        } else {
            g.drawString(font, "Select a blueprint", rightX, rightY + 20, UIHelper.GOLD_MID, false);
            g.drawString(font, "from the list", rightX, rightY + 32, UIHelper.GOLD_MID, false);
        }

        // Rotation buttons
        int ctrlY = rightY + 72;
        g.drawString(font, "Rotation:", rightX, ctrlY, UIHelper.GOLD_MID, false);

        String rotLabel = switch (rotationMirror) {
            case NONE, MIR_NONE -> "0";
            case R90, MIR_R90 -> "90";
            case R180, MIR_R180 -> "180";
            case R270, MIR_R270 -> "270";
        };
        rotLabel += rotationMirror.isMirrored() ? " (M)" : "";

        drawSmallButton(g, rightX + 52, ctrlY - 2, 20, 14, "<<", mouseX, mouseY, true);
        g.drawCenteredString(font, rotLabel, rightX + 92, ctrlY, UIHelper.CREAM_TEXT);
        drawSmallButton(g, rightX + 112, ctrlY - 2, 20, 14, ">>", mouseX, mouseY, true);

        // Mirror toggle
        int mirY = ctrlY + 20;
        g.drawString(font, "Mirror:", rightX, mirY, UIHelper.GOLD_MID, false);
        String mirState = rotationMirror.isMirrored() ? "ON" : "OFF";
        int mirColor = rotationMirror.isMirrored() ? 0xFF55FF55 : UIHelper.CREAM_TEXT;
        drawSmallButton(g, rightX + 52, mirY - 2, 40, 14, mirState, mouseX, mouseY, true);

        // Place and Cancel buttons
        int btnY = py + PANEL_H - 30;
        boolean canPlace = selectedFileIndex >= 0 && selectedFileIndex < blueprintFiles.size();
        drawButton(g, rightX, btnY, 80, 20, "Place", mouseX, mouseY, canPlace);
        drawButton(g, rightX + 90, btnY, 80, 20, "Cancel", mouseX, mouseY, true);
    }

    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean consumed) {
        if (consumed) return super.mouseClicked(event, consumed);

        double mx = event.x();
        double my = event.y();

        int px = (width - PANEL_W) / 2;
        int py = (height - PANEL_H) / 2;

        // Left panel bounds
        int leftX = px + 6;
        int leftY = py + 22;
        int listX = leftX;
        int listY = leftY + 18;
        int listW = LEFT_PANEL_W - 12;

        // Pack prev/next
        int packBtnY = leftY - 1;
        if (isHovered(leftX + 30, packBtnY, 12, 14, mx, my) && packIds.size() > 1) {
            selectedPackIndex = (selectedPackIndex - 1 + packIds.size()) % packIds.size();
            StructurePacks.selectedPack = packIds.get(selectedPackIndex);
            refreshFileList();
            return true;
        }
        String currentPackName = "None";
        if (!packIds.isEmpty() && selectedPackIndex < packIds.size()) {
            StructurePackMeta meta = StructurePacks.getPack(packIds.get(selectedPackIndex));
            currentPackName = meta != null ? meta.getName() : packIds.get(selectedPackIndex);
        }
        int nameW = font.width(currentPackName);
        if (isHovered(leftX + 48 + nameW, packBtnY, 12, 14, mx, my) && packIds.size() > 1) {
            selectedPackIndex = (selectedPackIndex + 1) % packIds.size();
            StructurePacks.selectedPack = packIds.get(selectedPackIndex);
            refreshFileList();
            return true;
        }

        // File list clicks
        for (int i = 0; i < VISIBLE_ENTRIES && i + scrollOffset < blueprintFiles.size(); i++) {
            int idx = i + scrollOffset;
            int ey = listY + i * ENTRY_HEIGHT;
            if (mx >= listX && mx <= listX + listW && my >= ey && my <= ey + ENTRY_HEIGHT - 1) {
                if (idx == selectedFileIndex) {
                    // Double-click to load preview
                    loadPreview();
                    return true;
                }
                selectedFileIndex = idx;
                loadPreview();
                return true;
            }
        }

        // Right panel controls
        int rightX = px + LEFT_PANEL_W + 14;
        int rightY = py + 22;
        int ctrlY = rightY + 72;

        // Rotate left
        if (isHovered(rightX + 52, ctrlY - 2, 20, 14, mx, my)) {
            rotationMirror = rotationMirror.rotate(net.minecraft.world.level.block.Rotation.COUNTERCLOCKWISE_90);
            updatePreviewRotation();
            return true;
        }
        // Rotate right
        if (isHovered(rightX + 112, ctrlY - 2, 20, 14, mx, my)) {
            rotationMirror = rotationMirror.rotate(net.minecraft.world.level.block.Rotation.CLOCKWISE_90);
            updatePreviewRotation();
            return true;
        }

        // Mirror toggle
        int mirY = ctrlY + 20;
        if (isHovered(rightX + 52, mirY - 2, 40, 14, mx, my)) {
            rotationMirror = rotationMirror.mirror();
            updatePreviewRotation();
            return true;
        }

        // Place button
        int btnY = py + PANEL_H - 30;
        if (isHovered(rightX, btnY, 80, 20, mx, my) && selectedFileIndex >= 0 && selectedFileIndex < blueprintFiles.size()) {
            String packId = packIds.get(selectedPackIndex);
            String blueprintPath = blueprintFiles.get(selectedFileIndex);
            ClientPacketDistributor.sendToServer(new BuildToolPlacePayload(
                    packId, blueprintPath, anchorPos, rotationMirror.ordinal()));
            BlueprintRenderer.clearPreview();
            onClose();
            return true;
        }

        // Cancel button
        if (isHovered(rightX + 90, btnY, 80, 20, mx, my)) {
            BlueprintRenderer.clearPreview();
            onClose();
            return true;
        }

        return super.mouseClicked(event, consumed);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int maxScroll = Math.max(0, blueprintFiles.size() - VISIBLE_ENTRIES);
        if (scrollY > 0 && scrollOffset > 0) {
            scrollOffset--;
        } else if (scrollY < 0 && scrollOffset < maxScroll) {
            scrollOffset++;
        }
        return true;
    }

    /**
     * Loads the selected blueprint file and sets it as the renderer preview.
     */
    private void loadPreview() {
        if (selectedFileIndex < 0 || selectedFileIndex >= blueprintFiles.size()) return;
        if (packIds.isEmpty() || selectedPackIndex >= packIds.size()) return;

        String packId = packIds.get(selectedPackIndex);
        String blueprintPath = blueprintFiles.get(selectedFileIndex);
        List<BlockInfo> blocks = StructurePacks.loadBlueprint(packId, blueprintPath);
        if (blocks != null && !blocks.isEmpty()) {
            BlueprintRenderer.setPreview(blocks, anchorPos, rotationMirror);
        }
    }

    /**
     * Updates the preview rotation without reloading the file.
     */
    private void updatePreviewRotation() {
        if (BlueprintRenderer.hasPreview()) {
            // Re-load to apply new rotation (preview blocks are local-space, rotation applied in renderer)
            loadPreview();
        }
    }

    @Override
    public void onClose() {
        super.onClose();
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

    private void drawSmallButton(GuiGraphics g, int x, int y, int w, int h,
                                  String text, int mouseX, int mouseY, boolean enabled) {
        boolean hovered = enabled && mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        int bg = enabled ? (hovered ? 0xFF2A2A4A : 0xFF1C1C28) : 0xFF0E0E18;
        int textColor = enabled ? (hovered ? 0xFF55FFFF : UIHelper.CREAM_TEXT) : 0xFF555566;
        g.fill(x, y, x + w, y + h, bg);
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

package com.ultra.megamod.feature.schematic.screen;

import com.ultra.megamod.feature.schematic.client.SchematicPlacementMode;
import net.minecraft.client.input.MouseButtonEvent;
import com.ultra.megamod.feature.schematic.data.SchematicLoader;
import com.ultra.megamod.feature.schematic.network.SchematicPlacementPayload;
import com.ultra.megamod.feature.schematic.placement.SchematicPlacement;
import com.ultra.megamod.feature.ui.UIHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.nio.file.Path;

/**
 * Screen for adjusting schematic placement (position, rotation, mirror).
 */
public class SchematicPlacementScreen extends Screen {

    /** If opened from a Builder citizen, this is their entity ID. -1 otherwise. */
    private final int builderEntityId;

    public SchematicPlacementScreen() {
        this(-1);
    }

    public SchematicPlacementScreen(int builderEntityId) {
        super(Component.literal("Schematic Placement"));
        this.builderEntityId = builderEntityId;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        super.render(g, mouseX, mouseY, delta);

        SchematicPlacement placement = SchematicPlacementMode.getActivePlacement();
        if (placement == null) {
            onClose();
            return;
        }

        int panelW = 280;
        int panelH = 240;
        int px = (width - panelW) / 2;
        int py = (height - panelH) / 2;

        UIHelper.drawPanel(g, px, py, panelW, panelH);

        // Title
        g.drawCenteredString(font, placement.getName(), width / 2, py + 6, UIHelper.BLUE_ACCENT);

        int y = py + 24;
        int labelX = px + 12;
        int valueX = px + 80;

        // Size info
        var size = placement.getSchematic().getSize();
        g.drawString(font, "Size:", labelX, y, UIHelper.GOLD_MID, false);
        g.drawString(font, size.getX() + " x " + size.getY() + " x " + size.getZ(), valueX, y, UIHelper.CREAM_TEXT, false);
        y += 14;

        // Blocks
        g.drawString(font, "Blocks:", labelX, y, UIHelper.GOLD_MID, false);
        g.drawString(font, String.valueOf(placement.getSchematic().getTotalBlockCount()), valueX, y, UIHelper.CREAM_TEXT, false);
        y += 18;

        // Position controls
        BlockPos origin = placement.getOrigin();
        g.drawString(font, "Position:", labelX, y, UIHelper.GOLD_BRIGHT, false);
        y += 12;

        drawPosRow(g, labelX, y, "X:", origin.getX(), mouseX, mouseY, 0);
        y += 16;
        drawPosRow(g, labelX, y, "Y:", origin.getY(), mouseX, mouseY, 1);
        y += 16;
        drawPosRow(g, labelX, y, "Z:", origin.getZ(), mouseX, mouseY, 2);
        y += 22;

        // Rotation
        String rotStr = rotationName(placement.getRotation());
        drawCycleRow(g, labelX, y, "Rotation:", rotStr, mouseX, mouseY, 3);
        y += 20;

        // Mirror
        String mirStr = mirrorName(placement.getMirror());
        drawCycleRow(g, labelX, y, "Mirror:", mirStr, mouseX, mouseY, 4);
        y += 28;

        // Action buttons
        int btnW = 80;
        int btnX1 = px + panelW / 2 - btnW - 20;
        int btnX2 = px + panelW / 2 + 20;

        // Move to Player button
        drawButton(g, px + panelW / 2 - 60, y - 6, 120, 16, "Move to Player", mouseX, mouseY, true);
        y += 22;

        // Accept / Cancel
        drawButton(g, btnX1, y, btnW, 20, "Accept", mouseX, mouseY, true);
        drawButton(g, btnX2, y, btnW, 20, "Cancel", mouseX, mouseY, true);
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean consumed) {
        if (consumed) return super.mouseClicked(event, consumed);
        double mouseX = event.x();
        double mouseY = event.y();
        SchematicPlacement placement = SchematicPlacementMode.getActivePlacement();
        if (placement == null) return super.mouseClicked(event, consumed);

        int panelW = 280;
        int panelH = 240;
        int px = (width - panelW) / 2;
        int py = (height - panelH) / 2;
        int labelX = px + 12;
        int y = py + 24 + 14 + 18 + 12; // Skip to position rows

        // Position +/- buttons
        BlockPos origin = placement.getOrigin();
        for (int axis = 0; axis < 3; axis++) {
            int rowY = y + axis * 16;
            // Minus button
            if (isOver(labelX + 20, rowY, 16, 12, mouseX, mouseY)) {
                nudgeAxis(placement, axis, -1);
                return true;
            }
            // Plus button
            if (isOver(labelX + 110, rowY, 16, 12, mouseX, mouseY)) {
                nudgeAxis(placement, axis, 1);
                return true;
            }
            // Minus 10
            if (isOver(labelX + 4, rowY, 16, 12, mouseX, mouseY)) {
                nudgeAxis(placement, axis, -10);
                return true;
            }
            // Plus 10
            if (isOver(labelX + 126, rowY, 16, 12, mouseX, mouseY)) {
                nudgeAxis(placement, axis, 10);
                return true;
            }
        }

        y += 3 * 16 + 6; // Skip past position rows

        // Rotation cycle
        if (isOver(labelX + 80, y, 80, 14, mouseX, mouseY)) {
            placement.cycleRotation();
            SchematicPlacementMode.markDirty();
            return true;
        }
        y += 20;

        // Mirror cycle
        if (isOver(labelX + 80, y, 80, 14, mouseX, mouseY)) {
            placement.cycleMirror();
            SchematicPlacementMode.markDirty();
            return true;
        }
        y += 28;

        // Move to Player
        if (isOver(px + panelW / 2 - 60, y - 6, 120, 16, mouseX, mouseY)) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                placement.setOrigin(mc.player.blockPosition());
                SchematicPlacementMode.markDirty();
            }
            return true;
        }
        y += 22;

        int btnW = 80;
        int btnX1 = px + panelW / 2 - btnW - 20;
        int btnX2 = px + panelW / 2 + 20;

        // Accept
        if (isOver(btnX1, y, btnW, 20, mouseX, mouseY)) {
            acceptPlacement();
            return true;
        }

        // Cancel
        if (isOver(btnX2, y, btnW, 20, mouseX, mouseY)) {
            SchematicPlacementMode.cancel();
            onClose();
            return true;
        }

        return super.mouseClicked(event, consumed);
    }

    private void acceptPlacement() {
        SchematicPlacement placement = SchematicPlacementMode.getActivePlacement();
        if (placement == null) return;

        // Read the schematic file bytes and send to server
        // Use the stored source file path (set during load) to avoid name mismatch
        String sourcePath = placement.getSchematic().getSourceFilePath();
        byte[] fileBytes = null;
        if (sourcePath != null) {
            fileBytes = SchematicLoader.fileToBytes(Path.of(sourcePath));
        }
        if (fileBytes == null) {
            // Fallback: try by name in schematics dir
            Minecraft mc2 = Minecraft.getInstance();
            Path schematicsDir = mc2.gameDirectory.toPath().resolve("schematics");
            Path filePath = schematicsDir.resolve(placement.getSchematic().getName() + ".litematic");
            fileBytes = SchematicLoader.fileToBytes(filePath);
            if (fileBytes == null) {
                filePath = schematicsDir.resolve(placement.getSchematic().getName());
                fileBytes = SchematicLoader.fileToBytes(filePath);
            }
        }

        if (fileBytes != null) {
            ClientPacketDistributor.sendToServer(new SchematicPlacementPayload(
                    fileBytes,
                    placement.getSchematic().getName(),
                    placement.getOrigin(),
                    placement.getRotationIndex(),
                    placement.getMirrorIndex(),
                    builderEntityId
            ));
        }

        SchematicPlacementMode.clear();
        onClose();
    }

    private void nudgeAxis(SchematicPlacement placement, int axis, int amount) {
        switch (axis) {
            case 0 -> placement.nudge(amount, 0, 0);
            case 1 -> placement.nudge(0, amount, 0);
            case 2 -> placement.nudge(0, 0, amount);
        }
        SchematicPlacementMode.markDirty();
    }

    private void drawPosRow(GuiGraphics g, int x, int y, String label, int value,
                            int mouseX, int mouseY, int axis) {
        g.drawString(font, label, x, y + 2, UIHelper.GOLD_MID, false);

        // -10 button
        drawMiniButton(g, x + 4, y, "<<", mouseX, mouseY);
        // -1 button
        drawMiniButton(g, x + 20, y, " -", mouseX, mouseY);
        // Value
        String valStr = String.valueOf(value);
        g.drawCenteredString(font, valStr, x + 75, y + 2, UIHelper.CREAM_TEXT);
        // +1 button
        drawMiniButton(g, x + 110, y, " +", mouseX, mouseY);
        // +10 button
        drawMiniButton(g, x + 126, y, ">>", mouseX, mouseY);
    }

    private void drawCycleRow(GuiGraphics g, int x, int y, String label, String value,
                              int mouseX, int mouseY, int id) {
        g.drawString(font, label, x, y + 2, UIHelper.GOLD_MID, false);
        boolean hovered = isOver(x + 80, y, 80, 14, mouseX, mouseY);
        int bg = hovered ? 0xFF2A2A4A : 0xFF1C1C28;
        g.fill(x + 80, y, x + 160, y + 14, bg);
        g.drawCenteredString(font, value, x + 120, y + 3, hovered ? 0xFF55FFFF : UIHelper.CREAM_TEXT);
    }

    private void drawMiniButton(GuiGraphics g, int x, int y, String text, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX <= x + 16 && mouseY >= y && mouseY <= y + 12;
        g.fill(x, y, x + 16, y + 12, hovered ? 0xFF2A2A4A : 0xFF1C1C28);
        g.drawString(font, text, x + 2, y + 2, hovered ? 0xFF55FFFF : UIHelper.CREAM_TEXT, false);
    }

    private void drawButton(GuiGraphics g, int x, int y, int w, int h,
                            String text, int mouseX, int mouseY, boolean enabled) {
        boolean hovered = enabled && isOver(x, y, w, h, mouseX, mouseY);
        int bg = enabled ? (hovered ? 0xFF2A2A4A : 0xFF1C1C28) : 0xFF0E0E18;
        g.fill(x, y, x + w, y + h, bg);
        g.fill(x, y, x + w, y + 1, 0xFF2A2A3A);
        g.drawCenteredString(font, text, x + w / 2, y + (h - 8) / 2,
                enabled ? (hovered ? 0xFF55FFFF : UIHelper.CREAM_TEXT) : 0xFF555566);
    }

    private boolean isOver(int x, int y, int w, int h, double mx, double my) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private static String rotationName(Rotation rot) {
        return switch (rot) {
            case NONE -> "None";
            case CLOCKWISE_90 -> "CW 90";
            case CLOCKWISE_180 -> "180";
            case COUNTERCLOCKWISE_90 -> "CCW 90";
        };
    }

    private static String mirrorName(Mirror mirror) {
        return switch (mirror) {
            case NONE -> "None";
            case FRONT_BACK -> "Front/Back";
            case LEFT_RIGHT -> "Left/Right";
        };
    }

    @Override
    public boolean isPauseScreen() { return false; }
}

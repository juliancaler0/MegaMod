package com.ultra.megamod.feature.citizen.blueprint.screen;

import com.ultra.megamod.feature.citizen.blueprint.network.ScanSavePayload;
import com.ultra.megamod.feature.citizen.blueprint.packs.StructurePacks;
import com.ultra.megamod.feature.ui.UIHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

/**
 * Screen for the Scan Tool: allows the player to define two corners of a region,
 * name the scan, and save it as a .blueprint file on the server.
 */
public class ScanToolScreen extends Screen {

    private static final int PANEL_W = 280;
    private static final int PANEL_H = 180;

    private final BlockPos pos1;
    private final BlockPos pos2;

    private EditBox nameField;

    public ScanToolScreen(BlockPos pos1, BlockPos pos2) {
        super(Component.literal("Scan Tool"));
        this.pos1 = pos1 != null ? pos1 : BlockPos.ZERO;
        this.pos2 = pos2 != null ? pos2 : BlockPos.ZERO;
    }

    @Override
    protected void init() {
        super.init();

        int px = (width - PANEL_W) / 2;
        int py = (height - PANEL_H) / 2;

        // Name input field
        nameField = new EditBox(font, px + 60, py + 90, PANEL_W - 76, 18, Component.literal("Name"));
        nameField.setMaxLength(64);
        nameField.setValue("scan");
        nameField.setHint(Component.literal("Blueprint name..."));
        addRenderableWidget(nameField);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        super.render(g, mouseX, mouseY, delta);

        int px = (width - PANEL_W) / 2;
        int py = (height - PANEL_H) / 2;

        UIHelper.drawPanel(g, px, py, PANEL_W, PANEL_H);

        // Title
        g.drawCenteredString(font, "Scan Tool", width / 2, py + 8, UIHelper.BLUE_ACCENT);

        // Pos 1 display
        String pos1Str = "Pos 1: " + pos1.getX() + ", " + pos1.getY() + ", " + pos1.getZ();
        g.drawString(font, pos1Str, px + 12, py + 30, UIHelper.CREAM_TEXT, false);

        // Pos 2 display
        String pos2Str = "Pos 2: " + pos2.getX() + ", " + pos2.getY() + ", " + pos2.getZ();
        g.drawString(font, pos2Str, px + 12, py + 44, UIHelper.CREAM_TEXT, false);

        // Size info
        int sizeX = Math.abs(pos2.getX() - pos1.getX()) + 1;
        int sizeY = Math.abs(pos2.getY() - pos1.getY()) + 1;
        int sizeZ = Math.abs(pos2.getZ() - pos1.getZ()) + 1;
        String sizeStr = "Size: " + sizeX + " x " + sizeY + " x " + sizeZ + " (" + ((long) sizeX * sizeY * sizeZ) + " blocks)";
        g.drawString(font, sizeStr, px + 12, py + 60, UIHelper.GOLD_MID, false);

        // Name label
        g.drawString(font, "Name:", px + 12, py + 93, UIHelper.CREAM_TEXT, false);

        // Pack label
        String packLabel = "Pack: " + (StructurePacks.selectedPack.isEmpty() ? "default" : StructurePacks.selectedPack);
        g.drawString(font, packLabel, px + 12, py + 116, UIHelper.GOLD_MID, false);

        // Save button
        int btnX = px + PANEL_W / 2 - 50;
        int btnY = py + PANEL_H - 32;
        boolean canSave = !nameField.getValue().trim().isEmpty();
        boolean btnHover = canSave && mouseX >= btnX && mouseX <= btnX + 100
                && mouseY >= btnY && mouseY <= btnY + 20;

        int btnBg = canSave ? (btnHover ? 0xFF2A2A4A : 0xFF1C1C28) : 0xFF0E0E18;
        int btnText = canSave ? (btnHover ? 0xFF55FFFF : UIHelper.CREAM_TEXT) : 0xFF555566;
        g.fill(btnX, btnY, btnX + 100, btnY + 20, btnBg);
        g.fill(btnX, btnY, btnX + 100, btnY + 1, 0xFF2A2A3A);
        g.drawCenteredString(font, "Save Blueprint", btnX + 50, btnY + 6, btnText);
    }

    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean consumed) {
        if (consumed) return super.mouseClicked(event, consumed);

        int px = (width - PANEL_W) / 2;
        int py = (height - PANEL_H) / 2;

        int btnX = px + PANEL_W / 2 - 50;
        int btnY = py + PANEL_H - 32;

        double mx = event.x();
        double my = event.y();

        if (mx >= btnX && mx <= btnX + 100 && my >= btnY && my <= btnY + 20) {
            String name = nameField.getValue().trim();
            if (!name.isEmpty()) {
                String packId = StructurePacks.selectedPack.isEmpty() ? "default" : StructurePacks.selectedPack;
                ClientPacketDistributor.sendToServer(new ScanSavePayload(pos1, pos2, name, packId));
                onClose();
                return true;
            }
        }

        return super.mouseClicked(event, consumed);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

package com.ultra.megamod.feature.citizen.multipiston;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

/**
 * Configuration GUI for the Multi-Piston block.
 * Input/output direction selector, range (1-10), speed (1-3), confirm button.
 */
public class MultiPistonScreen extends Screen {

    private static final int PANEL_W = 240;
    private static final int PANEL_H = 200;

    private static final String[] DIR_NAMES = {"Down", "Up", "North", "South", "West", "East"};

    private final BlockPos blockPos;
    private int inputDirIndex = 3;  // South
    private int outputDirIndex = 2; // North
    private EditBox rangeField;
    private EditBox speedField;

    public MultiPistonScreen(BlockPos pos) {
        super(Component.literal("Multi-Piston Config"));
        this.blockPos = pos;
    }

    @Override
    protected void init() {
        super.init();
        int px = (width - PANEL_W) / 2;
        int py = (height - PANEL_H) / 2;

        // Input direction cycle button
        addRenderableWidget(Button.builder(Component.literal("Input: " + DIR_NAMES[inputDirIndex]),
            btn -> {
                inputDirIndex = (inputDirIndex + 1) % 6;
                btn.setMessage(Component.literal("Input: " + DIR_NAMES[inputDirIndex]));
            }).bounds(px + 10, py + 35, PANEL_W - 20, 20).build());

        // Output direction cycle button
        addRenderableWidget(Button.builder(Component.literal("Output: " + DIR_NAMES[outputDirIndex]),
            btn -> {
                outputDirIndex = (outputDirIndex + 1) % 6;
                btn.setMessage(Component.literal("Output: " + DIR_NAMES[outputDirIndex]));
            }).bounds(px + 10, py + 60, PANEL_W - 20, 20).build());

        // Range input
        rangeField = new EditBox(font, px + 70, py + 90, 60, 18, Component.literal("Range"));
        rangeField.setMaxLength(2);
        rangeField.setValue("1");
        rangeField.setHint(Component.literal("1-10"));
        addRenderableWidget(rangeField);

        // Speed input
        speedField = new EditBox(font, px + 70, py + 115, 60, 18, Component.literal("Speed"));
        speedField.setMaxLength(1);
        speedField.setValue("1");
        speedField.setHint(Component.literal("1-3"));
        addRenderableWidget(speedField);

        // Confirm button
        addRenderableWidget(Button.builder(Component.literal("Confirm"),
            btn -> {
                int range = parseIntSafe(rangeField.getValue(), 1, 10);
                int speed = parseIntSafe(speedField.getValue(), 1, 3);
                ClientPacketDistributor.sendToServer(
                    new MultiPistonPayload(blockPos, inputDirIndex, outputDirIndex, range, speed));
                onClose();
            }).bounds(px + 10, py + 150, PANEL_W - 20, 20).build());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        super.render(g, mouseX, mouseY, delta);

        int px = (width - PANEL_W) / 2;
        int py = (height - PANEL_H) / 2;

        // Background panel
        g.fill(px, py, px + PANEL_W, py + PANEL_H, 0xCC1A1A2E);
        g.renderOutline(px, py, PANEL_W, PANEL_H, 0xFF4A90D9);

        // Title
        g.drawCenteredString(font, "Multi-Piston Config", width / 2, py + 8, 0xFF4A90D9);

        // Labels
        g.drawString(font, "Range:", px + 14, py + 93, 0xFFE8D5B5, false);
        g.drawString(font, "Speed:", px + 14, py + 118, 0xFFE8D5B5, false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static int parseIntSafe(String s, int min, int max) {
        try {
            int val = Integer.parseInt(s.trim());
            return Math.max(min, Math.min(max, val));
        } catch (NumberFormatException e) {
            return min;
        }
    }
}

package com.ultra.megamod.feature.schematic.client;

import com.ultra.megamod.feature.schematic.placement.SchematicPlacement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

/**
 * HUD overlay that shows schematic placement info when in placement mode.
 */
public class SchematicHudOverlay {

    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(
                Identifier.fromNamespaceAndPath("megamod", "schematic_hud"),
                SchematicHudOverlay::render
        );
    }

    private static void render(GuiGraphics g, DeltaTracker dt) {
        if (!SchematicPlacementMode.isActive()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;

        SchematicPlacement placement = SchematicPlacementMode.getActivePlacement();
        if (placement == null) return;

        int screenW = mc.getWindow().getGuiScaledWidth();
        int panelW = 220;
        int panelH = 68;
        int panelX = (screenW - panelW) / 2;
        int baseY = 18; // just below compass (compass is at y=4, ~12px tall)
        int x = panelX + 4;
        int y = baseY + 4;
        int color = 0xFFFFFFFF;
        int dimColor = 0xFFAAAAAA;
        int accentColor = 0xFF55FFFF;

        // Background panel (top-center, under compass)
        g.fill(panelX, baseY, panelX + panelW, baseY + panelH, 0xAA000000);

        // Schematic name
        g.drawString(mc.font, placement.getName(), x, y, accentColor, false);
        y += 11;

        // Size
        Vec3i size = placement.getSchematic().getSize();
        g.drawString(mc.font, "Size: " + size.getX() + " x " + size.getY() + " x " + size.getZ(), x, y, dimColor, false);
        y += 10;

        // Position
        BlockPos origin = placement.getOrigin();
        g.drawString(mc.font, "Pos: " + origin.getX() + ", " + origin.getY() + ", " + origin.getZ(), x, y, color, false);
        y += 10;

        // Rotation and Mirror
        String rotStr = rotationName(placement.getRotation());
        String mirStr = mirrorName(placement.getMirror());
        g.drawString(mc.font, "Rot: " + rotStr + "  Mirror: " + mirStr, x, y, color, false);
        y += 10;

        // Blocks
        g.drawString(mc.font, "Blocks: " + placement.getSchematic().getTotalBlockCount(), x, y, dimColor, false);
        y += 12;

        // Controls hint
        if (!placement.isLocked()) {
            g.drawString(mc.font, "[L] Settings  [Scroll] Move  [R/M] Rotate/Mirror", x, y, 0xFF888888, false);
        }
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
}

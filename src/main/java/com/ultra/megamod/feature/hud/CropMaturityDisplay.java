package com.ultra.megamod.feature.hud;

import com.ultra.megamod.MegaMod;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

/**
 * Shows crop growth stage as a small HUD element below the crosshair
 * when the player is looking at a crop, nether wart, sweet berry bush, etc.
 */
public class CropMaturityDisplay {

    private static final int BAR_W = 60;
    private static final int BAR_H = 3;
    private static final int BG = 0xAA000000;
    private static final int BORDER = 0x44FFFFFF;
    private static final int MATURE_COLOR = 0xFF55FF55;
    private static final int GROWING_COLOR = 0xFFFFAA00;
    private static final int LABEL_COLOR = 0xFFDDDDDD;

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(
            Identifier.fromNamespaceAndPath("megamod", "crop_maturity"),
            CropMaturityDisplay::render);
    }

    private static void render(GuiGraphics g, DeltaTracker dt) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (mc.options.hideGui) return;

        HitResult hit = mc.hitResult;
        if (hit == null || hit.getType() != HitResult.Type.BLOCK) return;

        BlockPos pos = ((BlockHitResult) hit).getBlockPos();
        BlockState state = mc.level.getBlockState(pos);
        Block block = state.getBlock();

        // Determine age and max age for the crop
        int age = -1;
        int maxAge = -1;
        String name = null;

        if (block instanceof CropBlock crop) {
            age = crop.getAge(state);
            maxAge = crop.getMaxAge();
            name = getBlockName(block);
        } else if (block instanceof NetherWartBlock) {
            age = state.getValue(NetherWartBlock.AGE);
            maxAge = 3;
            name = "Nether Wart";
        } else if (block instanceof SweetBerryBushBlock) {
            age = state.getValue(SweetBerryBushBlock.AGE);
            maxAge = 3;
            name = "Sweet Berry Bush";
        } else if (block instanceof CocoaBlock) {
            age = state.getValue(CocoaBlock.AGE);
            maxAge = 2;
            name = "Cocoa";
        } else if (block instanceof StemBlock) {
            age = state.getValue(StemBlock.AGE);
            maxAge = 7;
            name = block == Blocks.PUMPKIN_STEM ? "Pumpkin Stem" : "Melon Stem";
        }

        if (age < 0 || maxAge <= 0) return;

        boolean mature = age >= maxAge;
        float progress = (float) age / maxAge;

        int screenW = g.guiWidth();
        int screenH = g.guiHeight();
        int cx = screenW / 2;
        int y = screenH / 2 + 16;

        // Panel
        int panelW = BAR_W + 12;
        int panelH = 22;
        int px = cx - panelW / 2;
        int py = y;

        g.fill(px, py, px + panelW, py + panelH, BG);
        g.fill(px, py, px + panelW, py + 1, BORDER);
        g.fill(px, py + panelH - 1, px + panelW, py + panelH, BORDER);
        g.fill(px, py, px + 1, py + panelH, BORDER);
        g.fill(px + panelW - 1, py, px + panelW, py + panelH, BORDER);

        // Label
        String label = mature ? name + " \u2714" : name + " " + age + "/" + maxAge;
        int labelW = mc.font.width(label);
        g.drawString(mc.font, label, cx - labelW / 2, py + 3, mature ? MATURE_COLOR : LABEL_COLOR, true);

        // Progress bar
        int barX = cx - BAR_W / 2;
        int barY = py + 14;
        g.fill(barX - 1, barY - 1, barX + BAR_W + 1, barY + BAR_H + 1, 0xFF111111);
        int fillW = (int) (BAR_W * progress);
        if (fillW > 0) {
            g.fill(barX, barY, barX + fillW, barY + BAR_H, mature ? MATURE_COLOR : GROWING_COLOR);
        }
    }

    private static String getBlockName(Block block) {
        String key = block.getDescriptionId();
        // "block.minecraft.wheat" -> "Wheat"
        int lastDot = key.lastIndexOf('.');
        String raw = lastDot >= 0 ? key.substring(lastDot + 1) : key;
        raw = raw.replace("_", " ");
        StringBuilder sb = new StringBuilder();
        for (String word : raw.split(" ")) {
            if (!sb.isEmpty()) sb.append(" ");
            if (!word.isEmpty()) sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return sb.toString();
    }
}

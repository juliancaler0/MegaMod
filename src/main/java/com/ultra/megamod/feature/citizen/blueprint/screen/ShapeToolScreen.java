package com.ultra.megamod.feature.citizen.blueprint.screen;

import com.ultra.megamod.feature.citizen.blueprint.BlockInfo;
import com.ultra.megamod.feature.citizen.blueprint.RotationMirror;
import com.ultra.megamod.feature.citizen.blueprint.client.BlueprintRenderer;
import com.ultra.megamod.feature.citizen.blueprint.tools.Shape;
import com.ultra.megamod.feature.ui.UIHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * Shape configuration screen for the Shape Tool.
 * Allows the player to generate geometric shapes (cube, sphere, cylinder, etc.)
 * as blueprint previews using a selected block material.
 *
 * <p>Uses the {@link Shape} enum from the tools package for shape types.
 */
public class ShapeToolScreen extends Screen {

    private static final int PANEL_W = 300;
    private static final int PANEL_H = 240;

    private final BlockPos anchorPos;

    private Shape currentShape = Shape.CUBE;
    private boolean hollow = false;

    private EditBox widthField;
    private EditBox heightField;
    private EditBox depthField;

    public ShapeToolScreen(BlockPos anchorPos) {
        super(Component.literal("Shape Tool"));
        this.anchorPos = anchorPos != null ? anchorPos : BlockPos.ZERO;
    }

    @Override
    protected void init() {
        super.init();

        int px = (width - PANEL_W) / 2;
        int py = (height - PANEL_H) / 2;

        int fieldW = 50;
        int fieldX = px + 80;

        // Width input
        widthField = new EditBox(font, fieldX, py + 74, fieldW, 16, Component.literal("Width"));
        widthField.setMaxLength(4);
        widthField.setValue("5");
        widthField.setFilter(ShapeToolScreen::isNumericInput);
        addRenderableWidget(widthField);

        // Height input
        heightField = new EditBox(font, fieldX, py + 96, fieldW, 16, Component.literal("Height"));
        heightField.setMaxLength(4);
        heightField.setValue("5");
        heightField.setFilter(ShapeToolScreen::isNumericInput);
        addRenderableWidget(heightField);

        // Depth input
        depthField = new EditBox(font, fieldX, py + 118, fieldW, 16, Component.literal("Depth"));
        depthField.setMaxLength(4);
        depthField.setValue("5");
        depthField.setFilter(ShapeToolScreen::isNumericInput);
        addRenderableWidget(depthField);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        super.render(g, mouseX, mouseY, delta);

        int px = (width - PANEL_W) / 2;
        int py = (height - PANEL_H) / 2;

        UIHelper.drawPanel(g, px, py, PANEL_W, PANEL_H);

        // Title
        g.drawCenteredString(font, "Shape Tool", width / 2, py + 8, UIHelper.BLUE_ACCENT);

        // Shape selector
        g.drawString(font, "Shape:", px + 12, py + 32, UIHelper.GOLD_MID, false);
        drawSmallButton(g, px + 54, py + 30, 14, 14, "<", mouseX, mouseY, true);
        g.drawString(font, currentShape.getDisplayName(), px + 74, py + 32, UIHelper.CREAM_TEXT, false);
        int nameW = font.width(currentShape.getDisplayName());
        drawSmallButton(g, px + 78 + nameW, py + 30, 14, 14, ">", mouseX, mouseY, true);

        // Material display
        g.drawString(font, "Material:", px + 12, py + 52, UIHelper.GOLD_MID, false);
        BlockState material = getMaterialBlock();
        String materialName = material.getBlock().getName().getString();
        g.drawString(font, materialName, px + 62, py + 52, UIHelper.CREAM_TEXT, false);

        // Dimension inputs (labels)
        g.drawString(font, "Width:", px + 12, py + 77, UIHelper.CREAM_TEXT, false);
        g.drawString(font, "Height:", px + 12, py + 99, UIHelper.CREAM_TEXT, false);
        g.drawString(font, "Depth:", px + 12, py + 121, UIHelper.CREAM_TEXT, false);

        // Hollow toggle
        int hollowY = py + 146;
        g.drawString(font, "Hollow:", px + 12, hollowY, UIHelper.GOLD_MID, false);
        String hollowState = hollow ? "Yes" : "No";
        drawSmallButton(g, px + 54, hollowY - 2, 40, 14, hollowState, mouseX, mouseY, true);

        // Position info
        g.drawString(font, "Pos: " + anchorPos.getX() + ", " + anchorPos.getY() + ", " + anchorPos.getZ(),
                px + 12, py + 168, UIHelper.GOLD_MID, false);

        // Preview volume estimate
        int w = parseIntSafe(widthField.getValue(), 1);
        int h = parseIntSafe(heightField.getValue(), 1);
        int d = parseIntSafe(depthField.getValue(), 1);
        g.drawString(font, "Est. blocks: ~" + estimateBlockCount(currentShape, w, h, d, hollow),
                px + 12, py + 182, UIHelper.GOLD_MID, false);

        // Generate and Cancel buttons
        int btnY = py + PANEL_H - 30;
        drawButton(g, px + PANEL_W / 2 - 90, btnY, 80, 20, "Generate", mouseX, mouseY, true);
        drawButton(g, px + PANEL_W / 2 + 10, btnY, 80, 20, "Cancel", mouseX, mouseY, true);
    }

    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean consumed) {
        if (consumed) return super.mouseClicked(event, consumed);

        double mx = event.x();
        double my = event.y();

        int px = (width - PANEL_W) / 2;
        int py = (height - PANEL_H) / 2;

        // Shape prev
        if (isHovered(px + 54, py + 30, 14, 14, mx, my)) {
            Shape[] shapes = Shape.values();
            currentShape = shapes[(currentShape.ordinal() - 1 + shapes.length) % shapes.length];
            return true;
        }
        // Shape next
        String shapeName = currentShape.getDisplayName();
        int nameW = font.width(shapeName);
        if (isHovered(px + 78 + nameW, py + 30, 14, 14, mx, my)) {
            Shape[] shapes = Shape.values();
            currentShape = shapes[(currentShape.ordinal() + 1) % shapes.length];
            return true;
        }

        // Hollow toggle
        int hollowY = py + 146;
        if (isHovered(px + 54, hollowY - 2, 40, 14, mx, my)) {
            hollow = !hollow;
            return true;
        }

        // Generate button
        int btnY = py + PANEL_H - 30;
        if (isHovered(px + PANEL_W / 2 - 90, btnY, 80, 20, mx, my)) {
            generateShape();
            return true;
        }

        // Cancel button
        if (isHovered(px + PANEL_W / 2 + 10, btnY, 80, 20, mx, my)) {
            BlueprintRenderer.clearPreview();
            onClose();
            return true;
        }

        return super.mouseClicked(event, consumed);
    }

    /**
     * Generates a shape as a list of BlockInfo and sets it as the renderer preview.
     */
    private void generateShape() {
        int w = parseIntSafe(widthField.getValue(), 1);
        int h = parseIntSafe(heightField.getValue(), 1);
        int d = parseIntSafe(depthField.getValue(), 1);

        // Clamp dimensions
        w = Math.max(1, Math.min(w, 128));
        h = Math.max(1, Math.min(h, 128));
        d = Math.max(1, Math.min(d, 128));

        BlockState material = getMaterialBlock();
        List<BlockInfo> blocks = new ArrayList<>();

        switch (currentShape) {
            case CUBE -> generateCube(blocks, w, h, d, material);
            case SPHERE -> generateSphere(blocks, w, h, d, material);
            case HALF_SPHERE -> generateHalfSphere(blocks, w, h, d, material);
            case BOWL -> generateBowl(blocks, w, h, d, material);
            case DIAMOND -> generateDiamond(blocks, w, h, d, material);
            case PYRAMID -> generatePyramid(blocks, w, h, d, material);
            case UPSIDE_DOWN_PYRAMID -> generateUpsideDownPyramid(blocks, w, h, d, material);
            case CYLINDER -> generateCylinder(blocks, w, h, d, material);
            case CONE -> generateCone(blocks, w, h, d, material);
            case WAVE -> generateWave(blocks, w, h, d, material);
            case WAVE_3D -> generateWave3D(blocks, w, h, d, material);
        }

        if (!blocks.isEmpty()) {
            BlueprintRenderer.setPreview(blocks, anchorPos, RotationMirror.NONE);
        }
    }

    // === Shape generators ===

    private void generateCube(List<BlockInfo> blocks, int w, int h, int d, BlockState state) {
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                for (int z = 0; z < d; z++) {
                    if (hollow && x > 0 && x < w - 1 && y > 0 && y < h - 1 && z > 0 && z < d - 1) continue;
                    blocks.add(new BlockInfo(new BlockPos(x, y, z), state, null));
                }
            }
        }
    }

    private void generateSphere(List<BlockInfo> blocks, int w, int h, int d, BlockState state) {
        double rx = w / 2.0;
        double ry = h / 2.0;
        double rz = d / 2.0;
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                for (int z = 0; z < d; z++) {
                    double dx = (x + 0.5 - rx) / rx;
                    double dy = (y + 0.5 - ry) / ry;
                    double dz = (z + 0.5 - rz) / rz;
                    double dist = dx * dx + dy * dy + dz * dz;
                    if (dist <= 1.0) {
                        if (hollow && isInsideInnerEllipsoid(x, y, z, rx, ry, rz)) continue;
                        blocks.add(new BlockInfo(new BlockPos(x, y, z), state, null));
                    }
                }
            }
        }
    }

    private void generateHalfSphere(List<BlockInfo> blocks, int w, int h, int d, BlockState state) {
        double rx = w / 2.0;
        double ry = h;
        double rz = d / 2.0;
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                for (int z = 0; z < d; z++) {
                    double dx = (x + 0.5 - rx) / rx;
                    double dy = (y + 0.5) / ry;
                    double dz = (z + 0.5 - rz) / rz;
                    double dist = dx * dx + dy * dy + dz * dz;
                    if (dist <= 1.0) {
                        if (hollow && rx > 1 && ry > 1 && rz > 1) {
                            double idx = (x + 0.5 - rx) / (rx - 1);
                            double idy = (y + 0.5) / (ry - 1);
                            double idz = (z + 0.5 - rz) / (rz - 1);
                            if (idx * idx + idy * idy + idz * idz <= 1.0 && y > 0) continue;
                        }
                        blocks.add(new BlockInfo(new BlockPos(x, y, z), state, null));
                    }
                }
            }
        }
    }

    private void generateBowl(List<BlockInfo> blocks, int w, int h, int d, BlockState state) {
        // Inverted half sphere (bottom half)
        double rx = w / 2.0;
        double ry = h;
        double rz = d / 2.0;
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                for (int z = 0; z < d; z++) {
                    double dx = (x + 0.5 - rx) / rx;
                    double dy = ((h - y - 1) + 0.5) / ry;
                    double dz = (z + 0.5 - rz) / rz;
                    double dist = dx * dx + dy * dy + dz * dz;
                    if (dist <= 1.0) {
                        if (hollow && rx > 1 && ry > 1 && rz > 1) {
                            double idx = (x + 0.5 - rx) / (rx - 1);
                            double idy = ((h - y - 1) + 0.5) / (ry - 1);
                            double idz = (z + 0.5 - rz) / (rz - 1);
                            if (idx * idx + idy * idy + idz * idz <= 1.0 && y < h - 1) continue;
                        }
                        blocks.add(new BlockInfo(new BlockPos(x, y, z), state, null));
                    }
                }
            }
        }
    }

    private void generateCylinder(List<BlockInfo> blocks, int w, int h, int d, BlockState state) {
        double rx = w / 2.0;
        double rz = d / 2.0;
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                for (int z = 0; z < d; z++) {
                    double dx = (x + 0.5 - rx) / rx;
                    double dz = (z + 0.5 - rz) / rz;
                    double dist = dx * dx + dz * dz;
                    if (dist <= 1.0) {
                        if (hollow && y > 0 && y < h - 1) {
                            double innerRx = rx - 1;
                            double innerRz = rz - 1;
                            if (innerRx > 0 && innerRz > 0) {
                                double idx = (x + 0.5 - rx) / innerRx;
                                double idz = (z + 0.5 - rz) / innerRz;
                                if (idx * idx + idz * idz <= 1.0) continue;
                            }
                        }
                        blocks.add(new BlockInfo(new BlockPos(x, y, z), state, null));
                    }
                }
            }
        }
    }

    private void generatePyramid(List<BlockInfo> blocks, int w, int h, int d, BlockState state) {
        for (int y = 0; y < h; y++) {
            float progress = h > 1 ? (float) y / (h - 1) : 0;
            int layerW = Math.max(1, (int) (w * (1.0f - progress)));
            int layerD = Math.max(1, (int) (d * (1.0f - progress)));
            int offsetX = (w - layerW) / 2;
            int offsetZ = (d - layerD) / 2;

            for (int x = 0; x < layerW; x++) {
                for (int z = 0; z < layerD; z++) {
                    if (hollow && y < h - 1 && x > 0 && x < layerW - 1 && z > 0 && z < layerD - 1) continue;
                    blocks.add(new BlockInfo(new BlockPos(offsetX + x, y, offsetZ + z), state, null));
                }
            }
        }
    }

    private void generateUpsideDownPyramid(List<BlockInfo> blocks, int w, int h, int d, BlockState state) {
        for (int y = 0; y < h; y++) {
            float progress = h > 1 ? (float) y / (h - 1) : 0;
            int layerW = Math.max(1, (int) (w * progress));
            int layerD = Math.max(1, (int) (d * progress));
            int offsetX = (w - layerW) / 2;
            int offsetZ = (d - layerD) / 2;

            for (int x = 0; x < layerW; x++) {
                for (int z = 0; z < layerD; z++) {
                    if (hollow && y > 0 && x > 0 && x < layerW - 1 && z > 0 && z < layerD - 1) continue;
                    blocks.add(new BlockInfo(new BlockPos(offsetX + x, y, offsetZ + z), state, null));
                }
            }
        }
    }

    private void generateDiamond(List<BlockInfo> blocks, int w, int h, int d, BlockState state) {
        double cx = w / 2.0;
        double cy = h / 2.0;
        double cz = d / 2.0;
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                for (int z = 0; z < d; z++) {
                    double dx = Math.abs(x + 0.5 - cx) / cx;
                    double dy = Math.abs(y + 0.5 - cy) / cy;
                    double dz = Math.abs(z + 0.5 - cz) / cz;
                    if (dx + dy + dz <= 1.0) {
                        if (hollow) {
                            double innerCx = cx - 1;
                            double innerCy = cy - 1;
                            double innerCz = cz - 1;
                            if (innerCx > 0 && innerCy > 0 && innerCz > 0) {
                                double idx = Math.abs(x + 0.5 - cx) / innerCx;
                                double idy = Math.abs(y + 0.5 - cy) / innerCy;
                                double idz = Math.abs(z + 0.5 - cz) / innerCz;
                                if (idx + idy + idz <= 1.0) continue;
                            }
                        }
                        blocks.add(new BlockInfo(new BlockPos(x, y, z), state, null));
                    }
                }
            }
        }
    }

    private void generateCone(List<BlockInfo> blocks, int w, int h, int d, BlockState state) {
        double rx = w / 2.0;
        double rz = d / 2.0;
        for (int y = 0; y < h; y++) {
            float progress = h > 1 ? (float) y / (h - 1) : 0;
            double layerRx = rx * (1.0 - progress);
            double layerRz = rz * (1.0 - progress);
            if (layerRx < 0.5 && layerRz < 0.5 && y < h - 1) continue;

            for (int x = 0; x < w; x++) {
                for (int z = 0; z < d; z++) {
                    if (layerRx <= 0 || layerRz <= 0) {
                        // Tip of the cone: single block at center
                        if (x == w / 2 && z == d / 2) {
                            blocks.add(new BlockInfo(new BlockPos(x, y, z), state, null));
                        }
                        continue;
                    }
                    double dx = (x + 0.5 - rx) / layerRx;
                    double dz = (z + 0.5 - rz) / layerRz;
                    if (dx * dx + dz * dz <= 1.0) {
                        if (hollow && y < h - 1) {
                            double innerRx = layerRx - 1;
                            double innerRz = layerRz - 1;
                            if (innerRx > 0 && innerRz > 0) {
                                double idx = (x + 0.5 - rx) / innerRx;
                                double idz = (z + 0.5 - rz) / innerRz;
                                if (idx * idx + idz * idz <= 1.0) continue;
                            }
                        }
                        blocks.add(new BlockInfo(new BlockPos(x, y, z), state, null));
                    }
                }
            }
        }
    }

    private void generateWave(List<BlockInfo> blocks, int w, int h, int d, BlockState state) {
        // 2D wave pattern along X axis
        for (int x = 0; x < w; x++) {
            int waveH = (int) (h / 2.0 + h / 2.0 * Math.sin(2.0 * Math.PI * x / w));
            waveH = Math.max(1, Math.min(waveH, h));
            for (int y = 0; y < waveH; y++) {
                for (int z = 0; z < d; z++) {
                    if (hollow && y > 0 && y < waveH - 1 && z > 0 && z < d - 1) continue;
                    blocks.add(new BlockInfo(new BlockPos(x, y, z), state, null));
                }
            }
        }
    }

    private void generateWave3D(List<BlockInfo> blocks, int w, int h, int d, BlockState state) {
        // 3D wave pattern along both X and Z axes
        for (int x = 0; x < w; x++) {
            for (int z = 0; z < d; z++) {
                double waveVal = Math.sin(2.0 * Math.PI * x / w) + Math.sin(2.0 * Math.PI * z / d);
                int waveH = (int) (h / 2.0 + h / 4.0 * waveVal);
                waveH = Math.max(1, Math.min(waveH, h));
                for (int y = 0; y < waveH; y++) {
                    if (hollow && y > 0 && y < waveH - 1) continue;
                    blocks.add(new BlockInfo(new BlockPos(x, y, z), state, null));
                }
            }
        }
    }

    // === Helpers ===

    /**
     * Returns true if the point is inside a 1-block-smaller ellipsoid (for hollow check).
     */
    private boolean isInsideInnerEllipsoid(int x, int y, int z, double rx, double ry, double rz) {
        double innerRx = rx - 1;
        double innerRy = ry - 1;
        double innerRz = rz - 1;
        if (innerRx <= 0 || innerRy <= 0 || innerRz <= 0) return false;
        double dx = (x + 0.5 - rx) / innerRx;
        double dy = (y + 0.5 - ry) / innerRy;
        double dz = (z + 0.5 - rz) / innerRz;
        return dx * dx + dy * dy + dz * dz <= 1.0;
    }

    /**
     * Returns the block state from the player's currently held block item,
     * or stone as a fallback.
     */
    private BlockState getMaterialBlock() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            ItemStack held = mc.player.getMainHandItem();
            if (held.getItem() instanceof BlockItem blockItem) {
                return blockItem.getBlock().defaultBlockState();
            }
            // Check offhand
            held = mc.player.getOffhandItem();
            if (held.getItem() instanceof BlockItem blockItem) {
                return blockItem.getBlock().defaultBlockState();
            }
        }
        return Blocks.STONE.defaultBlockState();
    }

    /**
     * Estimates the block count for a shape (used for display only).
     */
    private static int estimateBlockCount(Shape shape, int w, int h, int d, boolean hollow) {
        return switch (shape) {
            case CUBE -> hollow ? (w * h * d - Math.max(0, (w - 2) * (h - 2) * (d - 2))) : w * h * d;
            case SPHERE -> (int) (4.0 / 3.0 * Math.PI * (w / 2.0) * (h / 2.0) * (d / 2.0));
            case HALF_SPHERE -> (int) (2.0 / 3.0 * Math.PI * (w / 2.0) * h * (d / 2.0));
            case BOWL -> (int) (2.0 / 3.0 * Math.PI * (w / 2.0) * h * (d / 2.0));
            case CYLINDER -> (int) (Math.PI * (w / 2.0) * (d / 2.0) * h);
            case PYRAMID, UPSIDE_DOWN_PYRAMID -> w * d * h / 3;
            case DIAMOND -> w * h * d / 3;
            case CONE -> (int) (Math.PI * (w / 2.0) * (d / 2.0) * h / 3.0);
            case WAVE, WAVE_3D -> w * h * d / 2;
        };
    }

    private static boolean isNumericInput(String text) {
        return text.isEmpty() || text.matches("\\d+");
    }

    private static int parseIntSafe(String text, int fallback) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return fallback;
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

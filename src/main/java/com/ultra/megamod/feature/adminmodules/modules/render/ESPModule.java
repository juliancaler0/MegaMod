package com.ultra.megamod.feature.adminmodules.modules.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ultra.megamod.feature.adminmodules.AdminModule;
import com.ultra.megamod.feature.adminmodules.ModuleCategory;
import com.ultra.megamod.feature.adminmodules.ModuleSetting;
import com.ultra.megamod.feature.furniture.DungeonChestBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import java.util.ArrayList;
import java.util.List;

public class ESPModule extends AdminModule {
    private ModuleSetting.DoubleSetting range;
    private ModuleSetting.BoolSetting players;
    private ModuleSetting.BoolSetting hostiles;
    private ModuleSetting.BoolSetting passive;
    private ModuleSetting.EnumSetting colorMode;
    private ModuleSetting.EnumSetting playerColor;
    private ModuleSetting.EnumSetting hostileColor;
    private ModuleSetting.EnumSetting passiveColor;
    private ModuleSetting.EnumSetting fillMode;
    private ModuleSetting.DoubleSetting fadeDistance;
    private ModuleSetting.BoolSetting highlightTarget;
    private ModuleSetting.BoolSetting dungeonChests;
    private ModuleSetting.EnumSetting chestColor;

    private static final List<String> COLOR_OPTIONS = List.of("Red", "Blue", "Green", "Yellow", "Orange", "Purple", "White", "Cyan");

    // Cached dungeon chest positions (rescanned periodically to avoid per-frame block scans)
    private static final int CHEST_SCAN_INTERVAL = 20;
    private int chestScanTick = 0;
    private final List<BlockPos> cachedChestPositions = new ArrayList<>();

    // Cached entity list (rescanned every few frames to avoid per-frame entity scans)
    private int entityScanTick = 0;
    private static final int ENTITY_SCAN_INTERVAL = 4; // rescan every 4 frames
    private List<Entity> cachedEntities = new ArrayList<>();

    public ESPModule() {
        super("esp", "ESP", "Draws wireframe boxes around entities", ModuleCategory.RENDER);
    }

    @Override
    protected void initSettings() {
        range = decimal("Range", 64.0, 8.0, 256.0, "Render range");
        players = bool("Players", true, "Show player boxes");
        hostiles = bool("Hostiles", true, "Show hostile mob boxes");
        passive = bool("Passive", false, "Show passive mob boxes");
        colorMode = enumVal("Color", "Category", List.of("Category", "Health", "Distance"), "Coloring mode: Category=type-based, Health=HP-based, Distance=range-based");
        playerColor = enumVal("Player Color", "Red", COLOR_OPTIONS, "Box color for players (Category mode)");
        hostileColor = enumVal("Hostile Color", "Orange", COLOR_OPTIONS, "Box color for hostile mobs (Category mode)");
        passiveColor = enumVal("Passive Color", "Green", COLOR_OPTIONS, "Box color for passive mobs (Category mode)");
        fillMode = enumVal("Fill", "Lines", List.of("Lines", "Thick"), "Lines=wireframe, Thick=triple-drawn lines for visibility");
        fadeDistance = decimal("Fade Dist", 3.0, 0.0, 10.0, "Entities within this distance have reduced alpha");
        highlightTarget = bool("Highlight Target", true, "Brighten box of entity under crosshair");
        dungeonChests = bool("Dungeon Chests", true, "Show dungeon chest outlines through walls");
        chestColor = enumVal("Chest Color", "Yellow", COLOR_OPTIONS, "Box color for dungeon chests");
    }

    @Override public boolean isServerSide() { return false; }
    @Override public boolean isClientSide() { return true; }

    @Override
    public void onRenderWorld(Object eventObj) {
        if (!(eventObj instanceof RenderLevelStageEvent.AfterTranslucentBlocks event)) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        PoseStack poseStack = event.getPoseStack();
        try {
            float partialTick = mc.getDeltaTracker().getRealtimeDeltaTicks();
            Vec3 camPos = mc.player.getEyePosition(partialTick);
            double r = range.getValue();
            AABB scanBox = mc.player.getBoundingBox().inflate(r);

            entityScanTick++;
            if (entityScanTick >= ENTITY_SCAN_INTERVAL || cachedEntities.isEmpty()) {
                entityScanTick = 0;
                cachedEntities = mc.level.getEntities(mc.player, scanBox, e -> {
                    if (e == mc.player) return false;
                    if (!e.isAlive()) return false;
                    if (e instanceof Player && !players.getValue()) return false;
                    if (e instanceof Monster && !hostiles.getValue()) return false;
                    if (e instanceof LivingEntity && !(e instanceof Player) && !(e instanceof Monster) && !passive.getValue()) return false;
                    return e.distanceTo(mc.player) <= r;
                });
            }
            List<Entity> entities = cachedEntities;

            // Scan for dungeon chests periodically
            boolean showChests = dungeonChests.getValue();
            if (showChests) {
                chestScanTick++;
                if (chestScanTick >= CHEST_SCAN_INTERVAL || cachedChestPositions.isEmpty()) {
                    chestScanTick = 0;
                    scanDungeonChests(mc, (int) r);
                }
            }

            if (entities.isEmpty() && (!showChests || cachedChestPositions.isEmpty())) return;

            MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

            poseStack.pushPose();
            poseStack.translate(-camPos.x, -camPos.y, -camPos.z);

            VertexConsumer consumer = bufferSource.getBuffer(ESPRenderHelper.LINES_SEE_THROUGH);
            Matrix4f matrix = poseStack.last().pose();

            Entity crosshairTarget = mc.crosshairPickEntity;
            double fadeDist = fadeDistance.getValue();
            boolean thick = "Thick".equals(fillMode.getValue());

            for (Entity e : entities) {
                float cr, cg, cb;
                String mode = colorMode.getValue();
                if ("Health".equals(mode) && e instanceof LivingEntity le) {
                    float pct = le.getHealth() / le.getMaxHealth();
                    cr = 1.0f - pct;
                    cg = pct;
                    cb = 0.0f;
                } else if ("Distance".equals(mode)) {
                    float dist = e.distanceTo(mc.player);
                    float pct = Math.min(1.0f, dist / (float) r);
                    cr = pct;
                    cg = 1.0f - pct;
                    cb = 0.2f;
                } else {
                    // Category mode: use configurable colors
                    float[] rgb;
                    if (e instanceof Player) {
                        rgb = getColorRGB(playerColor.getValue());
                    } else if (e instanceof Monster) {
                        rgb = getColorRGB(hostileColor.getValue());
                    } else {
                        rgb = getColorRGB(passiveColor.getValue());
                    }
                    cr = rgb[0];
                    cg = rgb[1];
                    cb = rgb[2];
                }

                // Highlight crosshair target
                boolean isTarget = highlightTarget.getValue() && crosshairTarget != null && e == crosshairTarget;
                if (isTarget) {
                    cr = Math.min(1.0f, cr + 0.3f);
                    cg = Math.min(1.0f, cg + 0.3f);
                    cb = Math.min(1.0f, cb + 0.3f);
                }

                // Fade alpha for nearby entities
                float alpha = 0.8f;
                if (fadeDist > 0) {
                    float dist = e.distanceTo(mc.player);
                    if (dist < fadeDist) {
                        alpha = 0.1f + 0.7f * (dist / (float) fadeDist);
                    }
                }
                // Crosshair target always full alpha
                if (isTarget) {
                    alpha = 1.0f;
                }

                // Interpolate position for smooth rendering
                double ix = e.xOld + (e.getX() - e.xOld) * partialTick;
                double iy = e.yOld + (e.getY() - e.yOld) * partialTick;
                double iz = e.zOld + (e.getZ() - e.zOld) * partialTick;
                AABB box = e.getBoundingBox().move(ix - e.getX(), iy - e.getY(), iz - e.getZ());

                drawWireBox(consumer, matrix,
                    (float) box.minX, (float) box.minY, (float) box.minZ,
                    (float) box.maxX, (float) box.maxY, (float) box.maxZ,
                    cr, cg, cb, alpha);

                if (thick) {
                    // Draw offset lines for thickness effect
                    float off = 0.005f;
                    drawWireBox(consumer, matrix,
                        (float) box.minX - off, (float) box.minY - off, (float) box.minZ - off,
                        (float) box.maxX + off, (float) box.maxY + off, (float) box.maxZ + off,
                        cr, cg, cb, alpha * 0.6f);
                    drawWireBox(consumer, matrix,
                        (float) box.minX + off, (float) box.minY + off, (float) box.minZ + off,
                        (float) box.maxX - off, (float) box.maxY - off, (float) box.maxZ - off,
                        cr, cg, cb, alpha * 0.6f);
                }
            }

            // Render dungeon chest outlines
            if (showChests && !cachedChestPositions.isEmpty()) {
                float[] chestRgb = getColorRGB(chestColor.getValue());
                for (BlockPos pos : cachedChestPositions) {
                    drawWireBox(consumer, matrix,
                        pos.getX(), pos.getY(), pos.getZ(),
                        pos.getX() + 1.0f, pos.getY() + 1.0f, pos.getZ() + 1.0f,
                        chestRgb[0], chestRgb[1], chestRgb[2], 0.85f);
                    if (thick) {
                        float off = 0.005f;
                        drawWireBox(consumer, matrix,
                            pos.getX() - off, pos.getY() - off, pos.getZ() - off,
                            pos.getX() + 1.0f + off, pos.getY() + 1.0f + off, pos.getZ() + 1.0f + off,
                            chestRgb[0], chestRgb[1], chestRgb[2], 0.5f);
                    }
                }
            }

            poseStack.popPose();
            bufferSource.endBatch();
        } catch (Exception e) {
            try { poseStack.popPose(); } catch (Exception ignored) {}
        }
    }

    private void scanDungeonChests(Minecraft mc, int range) {
        cachedChestPositions.clear();
        if (mc.level == null || mc.player == null) return;
        BlockPos center = mc.player.blockPosition();
        for (int dx = -range; dx <= range; dx++) {
            for (int dz = -range; dz <= range; dz++) {
                for (int dy = -range; dy <= range; dy++) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    if (!mc.level.isLoaded(pos)) continue;
                    BlockEntity be = mc.level.getBlockEntity(pos);
                    if (be instanceof DungeonChestBlockEntity chest && (!chest.isEmpty() || chest.hasPendingLoot())) {
                        cachedChestPositions.add(pos.immutable());
                    }
                }
            }
        }
    }

    private float[] getColorRGB(String colorName) {
        return switch (colorName) {
            case "Red" -> new float[]{1.0f, 0.2f, 0.2f};
            case "Blue" -> new float[]{0.2f, 0.4f, 1.0f};
            case "Green" -> new float[]{0.2f, 1.0f, 0.2f};
            case "Yellow" -> new float[]{1.0f, 1.0f, 0.2f};
            case "Orange" -> new float[]{1.0f, 0.5f, 0.0f};
            case "Purple" -> new float[]{0.7f, 0.3f, 1.0f};
            case "White" -> new float[]{1.0f, 1.0f, 1.0f};
            case "Cyan" -> new float[]{0.0f, 0.9f, 0.9f};
            default -> new float[]{1.0f, 1.0f, 1.0f};
        };
    }

    private void drawWireBox(VertexConsumer consumer, Matrix4f matrix,
                             float x1, float y1, float z1, float x2, float y2, float z2,
                             float r, float g, float b, float a) {
        // Bottom edges
        consumer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a).setNormal(1, 0, 0).setLineWidth(1.0f);
        consumer.addVertex(matrix, x2, y1, z1).setColor(r, g, b, a).setNormal(1, 0, 0).setLineWidth(1.0f);
        consumer.addVertex(matrix, x2, y1, z1).setColor(r, g, b, a).setNormal(0, 0, 1).setLineWidth(1.0f);
        consumer.addVertex(matrix, x2, y1, z2).setColor(r, g, b, a).setNormal(0, 0, 1).setLineWidth(1.0f);
        consumer.addVertex(matrix, x2, y1, z2).setColor(r, g, b, a).setNormal(-1, 0, 0).setLineWidth(1.0f);
        consumer.addVertex(matrix, x1, y1, z2).setColor(r, g, b, a).setNormal(-1, 0, 0).setLineWidth(1.0f);
        consumer.addVertex(matrix, x1, y1, z2).setColor(r, g, b, a).setNormal(0, 0, -1).setLineWidth(1.0f);
        consumer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a).setNormal(0, 0, -1).setLineWidth(1.0f);
        // Top edges
        consumer.addVertex(matrix, x1, y2, z1).setColor(r, g, b, a).setNormal(1, 0, 0).setLineWidth(1.0f);
        consumer.addVertex(matrix, x2, y2, z1).setColor(r, g, b, a).setNormal(1, 0, 0).setLineWidth(1.0f);
        consumer.addVertex(matrix, x2, y2, z1).setColor(r, g, b, a).setNormal(0, 0, 1).setLineWidth(1.0f);
        consumer.addVertex(matrix, x2, y2, z2).setColor(r, g, b, a).setNormal(0, 0, 1).setLineWidth(1.0f);
        consumer.addVertex(matrix, x2, y2, z2).setColor(r, g, b, a).setNormal(-1, 0, 0).setLineWidth(1.0f);
        consumer.addVertex(matrix, x1, y2, z2).setColor(r, g, b, a).setNormal(-1, 0, 0).setLineWidth(1.0f);
        consumer.addVertex(matrix, x1, y2, z2).setColor(r, g, b, a).setNormal(0, 0, -1).setLineWidth(1.0f);
        consumer.addVertex(matrix, x1, y2, z1).setColor(r, g, b, a).setNormal(0, 0, -1).setLineWidth(1.0f);
        // Vertical edges
        consumer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a).setNormal(0, 1, 0).setLineWidth(1.0f);
        consumer.addVertex(matrix, x1, y2, z1).setColor(r, g, b, a).setNormal(0, 1, 0).setLineWidth(1.0f);
        consumer.addVertex(matrix, x2, y1, z1).setColor(r, g, b, a).setNormal(0, 1, 0).setLineWidth(1.0f);
        consumer.addVertex(matrix, x2, y2, z1).setColor(r, g, b, a).setNormal(0, 1, 0).setLineWidth(1.0f);
        consumer.addVertex(matrix, x2, y1, z2).setColor(r, g, b, a).setNormal(0, 1, 0).setLineWidth(1.0f);
        consumer.addVertex(matrix, x2, y2, z2).setColor(r, g, b, a).setNormal(0, 1, 0).setLineWidth(1.0f);
        consumer.addVertex(matrix, x1, y1, z2).setColor(r, g, b, a).setNormal(0, 1, 0).setLineWidth(1.0f);
        consumer.addVertex(matrix, x1, y2, z2).setColor(r, g, b, a).setNormal(0, 1, 0).setLineWidth(1.0f);
    }
}

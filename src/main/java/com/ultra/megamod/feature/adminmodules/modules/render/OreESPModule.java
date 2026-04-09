package com.ultra.megamod.feature.adminmodules.modules.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ultra.megamod.feature.adminmodules.AdminModule;
import com.ultra.megamod.feature.adminmodules.ModuleCategory;
import com.ultra.megamod.feature.adminmodules.ModuleSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import java.util.*;

public class OreESPModule extends AdminModule {
    private ModuleSetting.IntSetting range;
    private ModuleSetting.IntSetting scanRate;
    private ModuleSetting.BoolSetting diamonds;
    private ModuleSetting.BoolSetting gold;
    private ModuleSetting.BoolSetting iron;
    private ModuleSetting.BoolSetting emeralds;
    private ModuleSetting.BoolSetting lapis;
    private ModuleSetting.BoolSetting redstone;
    private ModuleSetting.BoolSetting ancientDebris;
    private ModuleSetting.BoolSetting copper;

    private final List<OrePos> foundOres = new ArrayList<>();
    private int scanTick = 0;
    // Incremental scanning: split the full volume into slices by Y layer
    private int currentScanSlice = 0;
    private final List<OrePos> scanBuffer = new ArrayList<>();
    private boolean scanInProgress = false;

    private record OrePos(BlockPos pos, float r, float g, float b) {}

    public OreESPModule() {
        super("ore_esp", "OreESP", "Highlights ores through walls. WARNING: Range >16 may cause lag spikes during scanning", ModuleCategory.RENDER);
    }

    @Override
    protected void initSettings() {
        range = integer("Range", 16, 4, 24, "Scan range in blocks (max 24 to avoid lag)");
        scanRate = integer("Scan Rate", 40, 10, 100, "Ticks between scan slices");
        diamonds = bool("Diamonds", true, "Show diamond ore");
        gold = bool("Gold", true, "Show gold ore");
        iron = bool("Iron", false, "Show iron ore");
        emeralds = bool("Emeralds", true, "Show emerald ore");
        lapis = bool("Lapis", false, "Show lapis ore");
        redstone = bool("Redstone", false, "Show redstone ore");
        ancientDebris = bool("Ancient Debris", true, "Show ancient debris");
        copper = bool("Copper", false, "Show copper ore");
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
            scanTick++;
            if (scanTick >= scanRate.getValue()) {
                scanTick = 0;
                scanOresIncremental(mc);
            }

            if (foundOres.isEmpty()) return;

            Vec3 camPos = mc.player.getEyePosition(mc.getDeltaTracker().getRealtimeDeltaTicks());
            MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

            poseStack.pushPose();
            poseStack.translate(-camPos.x, -camPos.y, -camPos.z);

            VertexConsumer consumer = bufferSource.getBuffer(ESPRenderHelper.LINES_SEE_THROUGH);
            Matrix4f matrix = poseStack.last().pose();

            for (OrePos ore : foundOres) {
                float x = ore.pos.getX();
                float y = ore.pos.getY();
                float z = ore.pos.getZ();
                drawWireBox(consumer, matrix, x, y, z, x + 1, y + 1, z + 1, ore.r, ore.g, ore.b, 0.8f);
            }

            poseStack.popPose();
            bufferSource.endBatch();
        } catch (Exception e) {
            try { poseStack.popPose(); } catch (Exception ignored) {}
        }
    }

    /**
     * Incremental scanning: scan a few Y-layer slices per tick instead of the entire volume at once.
     * For range 16, full volume is 33^3 = 35,937 blocks. We scan ~4 Y layers per tick,
     * so each tick processes ~33*33*4 = 4,356 blocks -- much more manageable.
     */
    private void scanOresIncremental(Minecraft mc) {
        int r = range.getValue();
        BlockPos center = mc.player.blockPosition();
        int slicesPerTick = 4; // Scan 4 Y-layers per tick

        if (!scanInProgress) {
            // Start a new scan cycle
            scanBuffer.clear();
            currentScanSlice = -r;
            scanInProgress = true;
        }

        int slicesDone = 0;
        while (currentScanSlice <= r && slicesDone < slicesPerTick) {
            int y = currentScanSlice;
            for (int x = -r; x <= r; x++) {
                for (int z = -r; z <= r; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    Block block = mc.level.getBlockState(pos).getBlock();
                    float[] color = getOreColor(block);
                    if (color != null) {
                        scanBuffer.add(new OrePos(pos, color[0], color[1], color[2]));
                    }
                }
            }
            currentScanSlice++;
            slicesDone++;
        }

        if (currentScanSlice > r) {
            // Scan complete -- swap buffer to display list
            foundOres.clear();
            foundOres.addAll(scanBuffer);
            scanBuffer.clear();
            scanInProgress = false;
        }
    }

    private float[] getOreColor(Block block) {
        if (diamonds.getValue() && (block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE)) return new float[]{0.2f, 0.9f, 0.9f};
        if (gold.getValue() && (block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE || block == Blocks.NETHER_GOLD_ORE)) return new float[]{1.0f, 0.85f, 0.0f};
        if (iron.getValue() && (block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE)) return new float[]{0.85f, 0.7f, 0.6f};
        if (emeralds.getValue() && (block == Blocks.EMERALD_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE)) return new float[]{0.2f, 1.0f, 0.2f};
        if (lapis.getValue() && (block == Blocks.LAPIS_ORE || block == Blocks.DEEPSLATE_LAPIS_ORE)) return new float[]{0.2f, 0.3f, 1.0f};
        if (redstone.getValue() && (block == Blocks.REDSTONE_ORE || block == Blocks.DEEPSLATE_REDSTONE_ORE)) return new float[]{1.0f, 0.0f, 0.0f};
        if (ancientDebris.getValue() && block == Blocks.ANCIENT_DEBRIS) return new float[]{0.6f, 0.3f, 0.2f};
        if (copper.getValue() && (block == Blocks.COPPER_ORE || block == Blocks.DEEPSLATE_COPPER_ORE)) return new float[]{0.9f, 0.55f, 0.3f};
        return null;
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

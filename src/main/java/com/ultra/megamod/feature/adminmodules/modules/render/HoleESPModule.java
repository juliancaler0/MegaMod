package com.ultra.megamod.feature.adminmodules.modules.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ultra.megamod.feature.adminmodules.AdminModule;
import com.ultra.megamod.feature.adminmodules.ModuleCategory;
import com.ultra.megamod.feature.adminmodules.ModuleSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import java.util.ArrayList;
import java.util.List;

public class HoleESPModule extends AdminModule {
    private ModuleSetting.IntSetting range;
    private ModuleSetting.BoolSetting obsidianOnly;
    private final List<HoleEntry> holes = new ArrayList<>();
    private int scanTick = 0;
    // Incremental scanning state
    private int currentScanSlice = 0;
    private final List<HoleEntry> scanBuffer = new ArrayList<>();
    private boolean scanInProgress = false;

    private record HoleEntry(BlockPos pos, boolean allObsidian) {}

    public HoleESPModule() {
        super("hole_esp", "HoleESP", "Highlights safe 1x1 blast-resistant holes (obsidian/bedrock/crying obsidian)", ModuleCategory.RENDER);
    }

    @Override
    protected void initSettings() {
        range = integer("Range", 16, 4, 32, "Scan range");
        obsidianOnly = bool("Obsidian Only", false, "Only show pure obsidian/bedrock holes (hide mixed material holes)");
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
            if (++scanTick >= 10) {
                scanTick = 0;
                scanHolesIncremental(mc);
            }

            if (holes.isEmpty()) return;

            Vec3 camPos = mc.player.getEyePosition(mc.getDeltaTracker().getRealtimeDeltaTicks());
            MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

            poseStack.pushPose();
            poseStack.translate(-camPos.x, -camPos.y, -camPos.z);

            VertexConsumer consumer = bufferSource.getBuffer(ESPRenderHelper.LINES_SEE_THROUGH);
            Matrix4f matrix = poseStack.last().pose();

            for (HoleEntry hole : holes) {
                BlockPos pos = hole.pos;
                float x1 = pos.getX();
                float y1 = pos.getY();
                float z1 = pos.getZ();
                float x2 = x1 + 1;
                float z2 = z1 + 1;
                // Green = pure obsidian/bedrock, Yellow = mixed materials
                float cr = hole.allObsidian ? 0.0f : 1.0f;
                float cg = 1.0f;
                float cb = 0.0f;
                // Draw bottom face outline (4 edges of the bottom square)
                consumer.addVertex(matrix, x1, y1, z1).setColor(cr, cg, cb, 0.8f).setNormal(1, 0, 0).setLineWidth(1.0f);
                consumer.addVertex(matrix, x2, y1, z1).setColor(cr, cg, cb, 0.8f).setNormal(1, 0, 0).setLineWidth(1.0f);
                consumer.addVertex(matrix, x2, y1, z1).setColor(cr, cg, cb, 0.8f).setNormal(0, 0, 1).setLineWidth(1.0f);
                consumer.addVertex(matrix, x2, y1, z2).setColor(cr, cg, cb, 0.8f).setNormal(0, 0, 1).setLineWidth(1.0f);
                consumer.addVertex(matrix, x2, y1, z2).setColor(cr, cg, cb, 0.8f).setNormal(-1, 0, 0).setLineWidth(1.0f);
                consumer.addVertex(matrix, x1, y1, z2).setColor(cr, cg, cb, 0.8f).setNormal(-1, 0, 0).setLineWidth(1.0f);
                consumer.addVertex(matrix, x1, y1, z2).setColor(cr, cg, cb, 0.8f).setNormal(0, 0, -1).setLineWidth(1.0f);
                consumer.addVertex(matrix, x1, y1, z1).setColor(cr, cg, cb, 0.8f).setNormal(0, 0, -1).setLineWidth(1.0f);
            }

            poseStack.popPose();
            bufferSource.endBatch();
        } catch (Exception e) {
            try { poseStack.popPose(); } catch (Exception ignored) {}
        }
    }

    /**
     * Incremental scanning: scan a few Y-layer slices per call instead of the entire volume at once.
     * At range 32, the full volume is 65^3 = 274,625 blocks with 6+ blockstate reads each.
     * We scan 4 Y layers per call to keep each frame lightweight.
     */
    private void scanHolesIncremental(Minecraft mc) {
        int r = range.getValue();
        BlockPos center = mc.player.blockPosition();
        int slicesPerCall = 4;

        if (!scanInProgress) {
            scanBuffer.clear();
            currentScanSlice = -r;
            scanInProgress = true;
        }

        int slicesDone = 0;
        while (currentScanSlice <= r && slicesDone < slicesPerCall) {
            int y = currentScanSlice;
            for (int x = -r; x <= r; x++) {
                for (int z = -r; z <= r; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    if (!mc.level.getBlockState(pos).isAir()) continue;
                    if (!mc.level.getBlockState(pos.above()).isAir()) continue;
                    BlockState[] surrounding = {
                        mc.level.getBlockState(pos.below()),
                        mc.level.getBlockState(pos.north()),
                        mc.level.getBlockState(pos.south()),
                        mc.level.getBlockState(pos.east()),
                        mc.level.getBlockState(pos.west())
                    };
                    boolean allResistant = true;
                    boolean allObs = true;
                    for (BlockState s : surrounding) {
                        if (!isBlastResistant(s)) {
                            allResistant = false;
                            break;
                        }
                        if (!isObsidianOrBedrock(s)) {
                            allObs = false;
                        }
                    }
                    if (!allResistant) continue;
                    if (obsidianOnly.getValue() && !allObs) continue;
                    scanBuffer.add(new HoleEntry(pos, allObs));
                }
            }
            currentScanSlice++;
            slicesDone++;
        }

        if (currentScanSlice > r) {
            holes.clear();
            holes.addAll(scanBuffer);
            scanBuffer.clear();
            scanInProgress = false;
        }
    }

    private boolean isBlastResistant(BlockState state) {
        return state.getBlock() == Blocks.OBSIDIAN || state.getBlock() == Blocks.BEDROCK
            || state.getBlock() == Blocks.CRYING_OBSIDIAN || state.getBlock() == Blocks.ENDER_CHEST
            || state.getBlock() == Blocks.ANVIL || state.getBlock() == Blocks.ENCHANTING_TABLE
            || state.getBlock() == Blocks.NETHERITE_BLOCK;
    }

    private boolean isObsidianOrBedrock(BlockState state) {
        return state.getBlock() == Blocks.OBSIDIAN || state.getBlock() == Blocks.BEDROCK || state.getBlock() == Blocks.CRYING_OBSIDIAN;
    }
}

package com.ultra.megamod.feature.dungeons;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ultra.megamod.feature.adminmodules.modules.render.ESPRenderHelper;
import com.ultra.megamod.feature.dimensions.MegaModDimensions;
import com.ultra.megamod.feature.dungeons.boss.DungeonAltarBlock;
import com.ultra.megamod.feature.dungeons.entity.DungeonEntityRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import java.util.ArrayList;
import java.util.List;

/**
 * Client-side renderer that draws glowing outlines through walls around
 * dungeon altars and loot chests when in the dungeon dimension.
 * Always active in dungeons when toggle is enabled.
 */
@EventBusSubscriber(modid = "megamod", value = Dist.CLIENT)
public class DungeonLootGlow {

    /** Controlled by admin toggle. Default OFF. */
    public static volatile boolean enabled = false;

    private static final int SCAN_RADIUS = 64;
    private static final int SCAN_TICKS = 20; // rescan every 20 render ticks
    private static int scanTick = 0;
    private static final List<GlowPos> glowPositions = new ArrayList<>();

    private record GlowPos(BlockPos pos, float r, float g, float b) {}

    @SubscribeEvent
    public static void onRenderWorld(RenderLevelStageEvent.AfterTranslucentBlocks event) {
        if (!enabled) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        if (!mc.level.dimension().equals(MegaModDimensions.DUNGEON)) return;

        PoseStack poseStack = event.getPoseStack();
        try {
            // Rescan periodically
            scanTick++;
            if (scanTick >= SCAN_TICKS || glowPositions.isEmpty()) {
                scanTick = 0;
                fullScan(mc);
            }

            if (glowPositions.isEmpty()) return;

            Vec3 camPos = mc.player.getEyePosition(mc.getDeltaTracker().getRealtimeDeltaTicks());
            MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

            poseStack.pushPose();
            poseStack.translate(-camPos.x, -camPos.y, -camPos.z);

            VertexConsumer consumer = bufferSource.getBuffer(ESPRenderHelper.LINES_SEE_THROUGH);
            Matrix4f matrix = poseStack.last().pose();

            for (GlowPos gp : glowPositions) {
                float x = gp.pos.getX();
                float y = gp.pos.getY();
                float z = gp.pos.getZ();
                ESPRenderHelper.drawWireBox(consumer, matrix, x, y, z, x + 1, y + 1, z + 1,
                    gp.r, gp.g, gp.b, 0.85f);
            }

            poseStack.popPose();
            bufferSource.endBatch();
        } catch (Exception e) {
            try { poseStack.popPose(); } catch (Exception ignored) {}
        }
    }

    private static void fullScan(Minecraft mc) {
        ClientLevel level = mc.level;
        if (level == null) return;
        BlockPos center = mc.player.blockPosition();
        List<GlowPos> found = new ArrayList<>();

        // Get the altar block for comparison
        Block altarBlock = DungeonEntityRegistry.DUNGEON_ALTAR_BLOCK.get();

        // Scan every block in range - step 1 so we don't miss anything
        for (int dx = -SCAN_RADIUS; dx <= SCAN_RADIUS; dx++) {
            for (int dz = -SCAN_RADIUS; dz <= SCAN_RADIUS; dz++) {
                for (int dy = -30; dy <= 30; dy++) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    if (!level.isLoaded(pos)) continue;
                    BlockState state = level.getBlockState(pos);
                    Block block = state.getBlock();

                    // Dungeon Altar - purple outline
                    if (block == altarBlock) {
                        found.add(new GlowPos(pos, 0.6f, 0.2f, 1.0f));
                        continue;
                    }

                    // Vanilla chests with loot
                    if (block == Blocks.CHEST || block == Blocks.TRAPPED_CHEST) {
                        BlockEntity be = level.getBlockEntity(pos);
                        if (be instanceof ChestBlockEntity chest && hasLoot(chest)) {
                            found.add(new GlowPos(pos, 1.0f, 0.85f, 0.0f));
                        }
                    }

                    // Custom dungeon chests (with pending or actual loot)
                    BlockEntity be = level.getBlockEntity(pos);
                    if (be instanceof com.ultra.megamod.feature.furniture.DungeonChestBlockEntity dChest) {
                        if (dChest.hasPendingLoot() || !dChest.isEmpty()) {
                            found.add(new GlowPos(pos, 1.0f, 0.85f, 0.0f));
                        }
                    }
                }
            }
        }

        glowPositions.clear();
        glowPositions.addAll(found);
    }

    private static boolean hasLoot(ChestBlockEntity chest) {
        for (int i = 0; i < chest.getContainerSize(); i++) {
            ItemStack stack = chest.getItem(i);
            if (!stack.isEmpty()) {
                // Any non-empty chest in the dungeon dimension is a loot chest
                return true;
            }
        }
        return false;
    }
}

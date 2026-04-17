package com.ultra.megamod.feature.treefelling;

import com.ultra.megamod.feature.computer.admin.AdminSystem;
import com.ultra.megamod.feature.toggles.FeatureToggleManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.*;

/**
 * Tree felling: break a log block with an axe and the entire tree comes down.
 * Requires Crop Master (Tier 3+) or Herbalist (Tier 3+) skill branch.
 * Sneak to disable (chop single block normally).
 */
@EventBusSubscriber(modid = "megamod")
public class TreeFellingHandler {

    private static final int MAX_LOGS = 128;        // Max logs per tree
    private static final int MAX_SCAN = 512;        // Max blocks to scan
    private static final int LEAF_BREAK_RADIUS = 6; // Radius to force-break leaves
    private static final int MIN_LEAVES = 3;        // Min leaves around top to confirm it's a tree

    // Prevent recursive calls when we break logs ourselves
    private static boolean isFelling = false;

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (isFelling) return;
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        if (event.getLevel().isClientSide()) return;

        // Sneak = normal chop
        if (player.isShiftKeyDown()) return;

        ServerLevel level = (ServerLevel) event.getLevel();

        // Feature toggle check
        if (!FeatureToggleManager.get(level.getServer().overworld()).isEnabled("tree_felling")) return;

        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);

        // Must be a log block
        if (!state.is(BlockTags.LOGS)) return;

        // Must be holding an axe
        ItemStack tool = player.getMainHandItem();
        if (!(tool.getItem() instanceof AxeItem)) return;

        // Skill check: need Crop Master or Herbalist tier 3+
        if (!hasTreeFellingSkill(player, level)) return;

        // Find all connected logs (BFS upward)
        List<BlockPos> treeLogs = findTreeLogs(level, pos);
        if (treeLogs.size() <= 1) return; // Single log, not a tree

        // Validate it's actually a tree (check for leaves near top)
        if (!hasLeavesNearTop(level, treeLogs)) return;

        // Cancel the original break — we handle it ourselves
        event.setCanceled(true);

        // Break the tree
        isFelling = true;
        try {
            fellTree(player, level, treeLogs, tool);
        } finally {
            isFelling = false;
        }
    }

    /** Feature toggle governs this now; no per-player unlock requirement. */
    private static boolean hasTreeFellingSkill(ServerPlayer player, ServerLevel level) {
        return true;
    }

    /**
     * BFS to find all connected log blocks above the break point.
     * Searches in a 3x3 column upward (allows diagonal branching).
     */
    private static List<BlockPos> findTreeLogs(ServerLevel level, BlockPos origin) {
        List<BlockPos> logs = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();

        queue.add(origin);
        visited.add(origin);

        while (!queue.isEmpty() && visited.size() < MAX_SCAN && logs.size() < MAX_LOGS) {
            BlockPos current = queue.poll();
            BlockState state = level.getBlockState(current);

            if (!state.is(BlockTags.LOGS)) continue;

            logs.add(current);

            // Check all 26 neighbors (3x3x3 cube) but only go up or sideways, not below origin
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;

                        BlockPos neighbor = current.offset(dx, dy, dz);

                        // Don't go below the break point
                        if (neighbor.getY() < origin.getY()) continue;

                        if (visited.contains(neighbor)) continue;
                        visited.add(neighbor);

                        BlockState nState = level.getBlockState(neighbor);
                        if (nState.is(BlockTags.LOGS)) {
                            queue.add(neighbor);
                        }
                    }
                }
            }
        }

        return logs;
    }

    /**
     * Check that there are leaves near the topmost log (confirms it's a tree, not a log cabin).
     */
    private static boolean hasLeavesNearTop(ServerLevel level, List<BlockPos> logs) {
        // Find the topmost log
        BlockPos top = logs.get(0);
        for (BlockPos log : logs) {
            if (log.getY() > top.getY()) top = log;
        }

        // Count leaves in a 3-block radius around the top
        int leafCount = 0;
        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = -2; dy <= 3; dy++) {
                for (int dz = -3; dz <= 3; dz++) {
                    BlockState state = level.getBlockState(top.offset(dx, dy, dz));
                    if (state.is(BlockTags.LEAVES)) {
                        leafCount++;
                        if (leafCount >= MIN_LEAVES) return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Break all logs and force-break nearby leaves.
     */
    private static void fellTree(ServerPlayer player, ServerLevel level, List<BlockPos> logs, ItemStack tool) {
        // Sort furthest first (top to bottom) for natural-looking break order
        logs.sort((a, b) -> Integer.compare(b.getY(), a.getY()));

        int broken = 0;
        for (BlockPos logPos : logs) {
            BlockState logState = level.getBlockState(logPos);
            if (!logState.is(BlockTags.LOGS)) continue;

            // Check tool durability — stop if tool would break (preserve with 1 durability)
            if (tool.isDamageableItem() && tool.getDamageValue() >= tool.getMaxDamage() - 1) break;

            // Drop loot and break block
            Block.dropResources(logState, level, logPos, null, player, tool);
            level.removeBlock(logPos, false);
            broken++;
        }

        // Damage tool: 1 durability per log broken
        if (broken > 0 && tool.isDamageableItem()) {
            tool.hurtAndBreak(broken, player, EquipmentSlot.MAINHAND);
        }

        // Force-break leaves around the topmost log
        if (!logs.isEmpty()) {
            BlockPos topLog = logs.get(0); // Already sorted highest first
            forceBreakLeaves(level, topLog, player, tool);
        }
    }

    /**
     * Force-break leaf blocks in a radius around the top log.
     * Leaves drop their items naturally (saplings, sticks, apples).
     */
    private static void forceBreakLeaves(ServerLevel level, BlockPos center, ServerPlayer player, ItemStack tool) {
        for (int dx = -LEAF_BREAK_RADIUS; dx <= LEAF_BREAK_RADIUS; dx++) {
            for (int dy = -LEAF_BREAK_RADIUS; dy <= LEAF_BREAK_RADIUS; dy++) {
                for (int dz = -LEAF_BREAK_RADIUS; dz <= LEAF_BREAK_RADIUS; dz++) {
                    // Spherical radius check
                    if (dx * dx + dy * dy + dz * dz > LEAF_BREAK_RADIUS * LEAF_BREAK_RADIUS) continue;

                    BlockPos leafPos = center.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(leafPos);
                    if (state.is(BlockTags.LEAVES)) {
                        Block.dropResources(state, level, leafPos, null, player, tool);
                        level.removeBlock(leafPos, false);
                    }
                }
            }
        }
    }
}

package com.ultra.megamod.feature.baritone.process;

import com.ultra.megamod.feature.baritone.goals.Goal;
import com.ultra.megamod.feature.baritone.goals.GoalBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

/**
 * Process: tracks blocks broken during tunneling/mining and fills them back in
 * when the bot backtracks. Prevents the bot from creating dangerous pits.
 *
 * - Maintains a queue of broken block positions (max 200)
 * - Only fills positions below the bot's current Y (prevents filling ceilings)
 * - Places cobblestone/dirt from inventory to fill holes
 * - Rate limited to 2 blocks per tick
 * - Toggle via "backfill" command
 */
public class BackfillProcess implements BotProcess {
    private boolean active = false;
    private boolean enabled = false; // toggled by command
    private String status = "Idle";
    private ServerPlayer player;
    private ServerLevel level;

    /** Queue of positions that were broken and need backfilling, ordered oldest first */
    private final Deque<BlockPos> brokenPositions = new ArrayDeque<>();

    /** Maximum tracked positions */
    private static final int MAX_TRACKED = 200;

    /** Maximum blocks to place per tick */
    private static final int MAX_PER_TICK = 2;

    /** Maximum distance from player to consider filling a position */
    private static final double MAX_FILL_DISTANCE_SQ = 5.0 * 5.0;

    /** Maximum distance to path toward a backfill target */
    private static final double MAX_PATH_DISTANCE_SQ = 32.0 * 32.0;

    /**
     * Called by other processes (Mine, Tunnel, Quarry) when they break a block.
     * Records the position for potential backfilling.
     */
    public void recordBrokenBlock(BlockPos pos) {
        if (!enabled) return;
        brokenPositions.addLast(pos.immutable());
        if (brokenPositions.size() > MAX_TRACKED) {
            brokenPositions.removeFirst();
        }
    }

    /**
     * Toggle backfilling on/off.
     */
    public void toggle(ServerPlayer player, ServerLevel level) {
        this.player = player;
        this.level = level;
        this.enabled = !this.enabled;
        this.active = this.enabled;
        if (!enabled) {
            status = "Backfill disabled";
        } else {
            status = "Backfill enabled (" + brokenPositions.size() + " positions tracked)";
        }
    }

    /**
     * Enable backfilling.
     */
    public void enable(ServerPlayer player, ServerLevel level) {
        this.player = player;
        this.level = level;
        this.enabled = true;
        this.active = true;
        status = "Backfill enabled (" + brokenPositions.size() + " positions tracked)";
    }

    /**
     * Disable backfilling.
     */
    public void disable() {
        this.enabled = false;
        this.active = false;
        status = "Backfill disabled";
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void updateState(ServerPlayer player, ServerLevel level) {
        this.player = player;
        this.level = level;
    }

    @Override
    public String name() { return "Backfill"; }

    @Override
    public boolean isActive() {
        return active && enabled && !brokenPositions.isEmpty();
    }

    @Override
    public double priority() { return 25; }

    @Override
    public PathingCommand onTick(boolean calcFailed, boolean safeToCancel) {
        if (!active || !enabled || player == null || level == null) return null;

        // Clean up positions that are no longer air (already filled by something else)
        // or that are above the player's Y level (don't fill ceilings)
        cleanupPositions();

        if (brokenPositions.isEmpty()) {
            status = "Backfill idle (no positions to fill)";
            return null;
        }

        BlockPos playerPos = player.blockPosition();

        // Find positions near the player that can be filled right now
        int filled = 0;
        Iterator<BlockPos> it = brokenPositions.iterator();
        while (it.hasNext() && filled < MAX_PER_TICK) {
            BlockPos pos = it.next();
            double distSq = playerPos.distSqr(pos);

            // Only fill blocks that are close enough and below us
            if (distSq <= MAX_FILL_DISTANCE_SQ && pos.getY() < playerPos.getY()) {
                BlockState currentState = level.getBlockState(pos);
                if (currentState.isAir() || currentState.liquid()) {
                    // Try to place a fill block from inventory
                    if (tryPlaceBlock(pos)) {
                        it.remove();
                        filled++;
                    }
                } else {
                    // Already filled by something else
                    it.remove();
                }
            }
        }

        if (filled > 0) {
            status = "Backfilling (" + filled + " placed, " + brokenPositions.size() + " remaining)";
        }

        // If there are still positions to fill, path toward the nearest one below us
        BlockPos nearest = findNearestFillable(playerPos);
        if (nearest != null) {
            // Stand adjacent to the fill target (one block above or beside it)
            BlockPos standPos = nearest.above();
            Goal goal = new GoalBlock(standPos.getX(), standPos.getY(), standPos.getZ());
            return new PathingCommand(goal, PathingCommand.CommandType.SET_GOAL_AND_PATH);
        }

        status = "Backfill idle (" + brokenPositions.size() + " tracked)";
        return null;
    }

    /**
     * Remove positions that are no longer valid for backfilling.
     */
    private void cleanupPositions() {
        if (player == null || level == null) return;
        int playerY = player.blockPosition().getY();

        brokenPositions.removeIf(pos -> {
            // Remove if above player Y (don't fill ceilings)
            if (pos.getY() >= playerY) return true;
            // Remove if already filled
            BlockState state = level.getBlockState(pos);
            return !state.isAir() && !state.liquid();
        });
    }

    /**
     * Find the nearest backfillable position below the player.
     */
    private BlockPos findNearestFillable(BlockPos playerPos) {
        BlockPos nearest = null;
        double nearestDist = MAX_PATH_DISTANCE_SQ;

        for (BlockPos pos : brokenPositions) {
            if (pos.getY() >= playerPos.getY()) continue;
            BlockState state = level.getBlockState(pos);
            if (!state.isAir() && !state.liquid()) continue;

            double dist = playerPos.distSqr(pos);
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = pos;
            }
        }
        return nearest;
    }

    /**
     * Try to place cobblestone or dirt from the player's inventory at the given position.
     * Returns true if a block was successfully placed.
     */
    private boolean tryPlaceBlock(BlockPos pos) {
        if (player == null || level == null) return false;

        // Search inventory for cobblestone first, then dirt
        int slot = findItemSlot(Items.COBBLESTONE.getDefaultInstance());
        if (slot == -1) {
            slot = findItemSlot(Items.DIRT.getDefaultInstance());
        }
        if (slot == -1) {
            slot = findItemSlot(Items.COBBLED_DEEPSLATE.getDefaultInstance());
        }
        if (slot == -1) {
            slot = findItemSlot(Items.STONE.getDefaultInstance());
        }
        if (slot == -1) {
            slot = findItemSlot(Items.NETHERRACK.getDefaultInstance());
        }

        if (slot == -1) {
            status = "Backfill: no fill blocks in inventory";
            return false;
        }

        // Place the block
        ItemStack stack = player.getInventory().getItem(slot);
        level.setBlockAndUpdate(pos, getFillBlock(stack));
        stack.shrink(1);
        if (stack.isEmpty()) {
            player.getInventory().setItem(slot, ItemStack.EMPTY);
        }
        return true;
    }

    /**
     * Find a slot in the player's inventory containing the given item type.
     */
    private int findItemSlot(ItemStack match) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() == match.getItem()) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Get the block state to place based on the item in hand.
     */
    private BlockState getFillBlock(ItemStack stack) {
        if (stack.getItem() == Items.COBBLESTONE) return Blocks.COBBLESTONE.defaultBlockState();
        if (stack.getItem() == Items.DIRT) return Blocks.DIRT.defaultBlockState();
        if (stack.getItem() == Items.COBBLED_DEEPSLATE) return Blocks.COBBLED_DEEPSLATE.defaultBlockState();
        if (stack.getItem() == Items.STONE) return Blocks.STONE.defaultBlockState();
        if (stack.getItem() == Items.NETHERRACK) return Blocks.NETHERRACK.defaultBlockState();
        return Blocks.COBBLESTONE.defaultBlockState();
    }

    public int getTrackedCount() {
        return brokenPositions.size();
    }

    public void clearTracked() {
        brokenPositions.clear();
    }

    @Override
    public void onLostControl() {}

    @Override
    public void cancel() {
        active = false;
        enabled = false;
        brokenPositions.clear();
        status = "Backfill cancelled";
    }

    @Override
    public String getStatus() { return status; }
}

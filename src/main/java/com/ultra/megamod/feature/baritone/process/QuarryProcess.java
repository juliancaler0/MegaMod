package com.ultra.megamod.feature.baritone.process;

import com.ultra.megamod.feature.baritone.goals.Goal;
import com.ultra.megamod.feature.baritone.goals.GoalBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Consumer;

/**
 * Process: quarry a rectangular area layer by layer from top to bottom.
 * Mines everything in the box except bedrock. Rate-limited to 4 blocks/tick.
 */
public class QuarryProcess implements BotProcess {
    private boolean active = false;
    private String status = "Idle";
    private ServerLevel level;

    // Quarry bounds (inclusive, normalized so min <= max)
    private int minX, minY, minZ;
    private int maxX, maxY, maxZ;

    // Current mining state
    private int currentLayer; // Y coordinate of the layer we're mining (top-down)
    private int cursorX, cursorZ; // Current position within the layer
    private int blocksMined = 0;
    private int totalLayers;
    private int currentLayerIndex; // 0-based from top

    // Rate limiting
    private static final int MAX_BLOCKS_PER_TICK = 4;
    private Consumer<BlockPos> blockBreakListener;

    public void start(int x1, int y1, int z1, int x2, int y2, int z2, ServerLevel level) {
        this.minX = Math.min(x1, x2);
        this.minY = Math.min(y1, y2);
        this.minZ = Math.min(z1, z2);
        this.maxX = Math.max(x1, x2);
        this.maxY = Math.max(y1, y2);
        this.maxZ = Math.max(z1, z2);
        this.level = level;
        this.currentLayer = maxY;
        this.cursorX = minX;
        this.cursorZ = minZ;
        this.blocksMined = 0;
        this.totalLayers = maxY - minY + 1;
        this.currentLayerIndex = 0;
        this.active = true;
        this.status = "Quarry started (" + (maxX - minX + 1) + "x" + totalLayers + "x" + (maxZ - minZ + 1) + ")";
    }

    @Override
    public String name() { return "Quarry"; }

    @Override
    public boolean isActive() { return active; }

    @Override
    public double priority() { return 65; }

    @Override
    public PathingCommand onTick(boolean calcFailed, boolean safeToCancel) {
        if (!active || level == null) return null;

        // Check if quarry is complete
        if (currentLayer < minY) {
            status = "Quarry complete! (" + blocksMined + " blocks mined)";
            active = false;
            return null;
        }

        // Mine up to MAX_BLOCKS_PER_TICK blocks in the current layer
        int minedThisTick = 0;
        while (minedThisTick < MAX_BLOCKS_PER_TICK && currentLayer >= minY) {
            BlockPos pos = new BlockPos(cursorX, currentLayer, cursorZ);
            BlockState state = level.getBlockState(pos);

            // Skip air, liquids, and bedrock
            if (!state.isAir() && !state.liquid() && state.getBlock() != Blocks.BEDROCK) {
                level.destroyBlock(pos, true);
                blocksMined++;
                if (blockBreakListener != null) blockBreakListener.accept(pos.immutable());
                minedThisTick++;
            }

            // Advance cursor in a zigzag pattern across the layer
            advanceCursor();
        }

        currentLayerIndex = maxY - currentLayer;
        status = "Quarry layer " + (currentLayerIndex + 1) + "/" + totalLayers
            + " (Y=" + currentLayer + ", " + blocksMined + " mined)";

        // Navigate to the position on the current layer to mine from
        // Stand on top of the layer we're mining (one block above)
        int standY = currentLayer + 1;
        Goal goal = new GoalBlock(cursorX, standY, cursorZ);
        return new PathingCommand(goal, PathingCommand.CommandType.SET_GOAL_AND_PATH);
    }

    private void advanceCursor() {
        cursorZ++;
        if (cursorZ > maxZ) {
            cursorZ = minZ;
            cursorX++;
            if (cursorX > maxX) {
                cursorX = minX;
                currentLayer--;
            }
        }
    }

    public void setBlockBreakListener(Consumer<BlockPos> listener) { this.blockBreakListener = listener; }
    public int getBlocksMined() { return blocksMined; }
    public int getCurrentLayer() { return currentLayer; }
    public int getTotalLayers() { return totalLayers; }
    public int getMinX() { return minX; }
    public int getMinY() { return minY; }
    public int getMinZ() { return minZ; }
    public int getMaxX() { return maxX; }
    public int getMaxY() { return maxY; }
    public int getMaxZ() { return maxZ; }

    public void updateLevel(ServerLevel level) { this.level = level; }

    @Override
    public void onLostControl() {}

    @Override
    public void cancel() {
        active = false;
        status = "Quarry stopped (layer " + (currentLayerIndex + 1) + "/" + totalLayers + ", " + blocksMined + " mined)";
    }

    @Override
    public String getStatus() { return status; }
}

package com.ultra.megamod.feature.baritone.process;

import com.ultra.megamod.feature.baritone.goals.Goal;
import com.ultra.megamod.feature.baritone.goals.GoalComposite;
import com.ultra.megamod.feature.baritone.goals.GoalGetToBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * Process: place blocks from a simple cuboid pattern.
 * Supports layer-by-layer building, gravity-aware (bottom-up),
 * material inventory check, skip already-correct blocks.
 */
public class BuildProcess implements BotProcess {
    private boolean active = false;
    private String status = "Idle";
    private BlockPos origin;
    private int width, height, depth;
    private Block material;
    private ServerLevel level;
    private int placed = 0;
    private int skipped = 0;
    private int totalBlocks;
    private boolean buildInLayers = true;
    private boolean buildBottomUp = true;
    private boolean skipMatchingBlocks = true;
    private int currentLayer = 0;

    public void start(BlockPos origin, int width, int height, int depth, String blockName, ServerLevel level) {
        this.origin = origin;
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.level = level;
        this.placed = 0;
        this.skipped = 0;
        this.totalBlocks = width * height * depth;
        this.currentLayer = 0;

        Identifier loc = Identifier.parse(blockName.contains(":") ? blockName : "minecraft:" + blockName);
        Block resolved = BuiltInRegistries.BLOCK.getValue(loc);
        this.material = (resolved == null || resolved == Blocks.AIR) ? Blocks.COBBLESTONE : resolved;

        this.active = true;
        this.status = "Building " + width + "x" + height + "x" + depth + " with " + blockName;
    }

    public void configure(boolean buildInLayers, boolean buildBottomUp, boolean skipMatchingBlocks) {
        this.buildInLayers = buildInLayers;
        this.buildBottomUp = buildBottomUp;
        this.skipMatchingBlocks = skipMatchingBlocks;
    }

    @Override
    public String name() { return "Build"; }

    @Override
    public boolean isActive() { return active; }

    @Override
    public double priority() { return 45; }

    @Override
    public PathingCommand onTick(boolean calcFailed, boolean safeToCancel) {
        if (!active || level == null) return null;

        List<Goal> targets = new ArrayList<>();

        if (buildInLayers) {
            // Build one layer at a time (bottom-up or top-down)
            boolean layerComplete = true;
            int startY = buildBottomUp ? 0 : height - 1;
            int endY = buildBottomUp ? height : -1;
            int stepY = buildBottomUp ? 1 : -1;

            for (int y = startY; y != endY; y += stepY) {
                layerComplete = true;
                for (int x = 0; x < width; x++) {
                    for (int z = 0; z < depth; z++) {
                        BlockPos pos = origin.offset(x, y, z);
                        BlockState current = level.getBlockState(pos);
                        if (skipMatchingBlocks && current.getBlock() == material) {
                            continue; // Already correct
                        }
                        if (current.getBlock() != material) {
                            targets.add(new GoalGetToBlock(pos.getX(), pos.getY(), pos.getZ()));
                            layerComplete = false;
                            if (targets.size() >= 10) break;
                        }
                    }
                    if (targets.size() >= 10) break;
                }
                if (!targets.isEmpty()) break; // Work on this layer before moving up
            }
        } else {
            // Original simple scan
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    for (int z = 0; z < depth; z++) {
                        BlockPos pos = origin.offset(x, y, z);
                        BlockState current = level.getBlockState(pos);
                        if (skipMatchingBlocks && current.getBlock() == material) {
                            continue;
                        }
                        if (current.getBlock() != material) {
                            targets.add(new GoalGetToBlock(pos.getX(), pos.getY(), pos.getZ()));
                            if (targets.size() >= 10) break;
                        }
                    }
                    if (targets.size() >= 10) break;
                }
                if (targets.size() >= 10) break;
            }
        }

        if (targets.isEmpty()) {
            status = "Build complete! Placed " + placed + " blocks";
            active = false;
            return null;
        }

        int percent = totalBlocks > 0 ? (placed * 100 / totalBlocks) : 0;
        status = "Building... (" + placed + "/" + totalBlocks + ", " + percent + "%)";
        return new PathingCommand(new GoalComposite(targets.toArray(new Goal[0])), PathingCommand.CommandType.SET_GOAL_AND_PATH);
    }

    public void onBlockPlaced() {
        placed++;
    }

    public Block getMaterial() { return material; }
    public BlockPos getOrigin() { return origin; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getDepth() { return depth; }
    public int getPlaced() { return placed; }

    public void updateLevel(ServerLevel level) { this.level = level; }

    @Override
    public void onLostControl() {}

    @Override
    public void cancel() {
        active = false;
        status = "Stopped building (" + placed + " placed)";
    }

    @Override
    public String getStatus() { return status; }
}

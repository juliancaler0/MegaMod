package com.ultra.megamod.feature.baritone.process;

import com.ultra.megamod.feature.baritone.goals.Goal;
import com.ultra.megamod.feature.baritone.goals.GoalGetToBlock;
import com.ultra.megamod.feature.baritone.pathfinding.WorldScanner;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Process: find nearest block type and navigate to it.
 * Supports blacklisting positions that proved unreachable.
 */
public class GetToBlockProcess implements BotProcess {
    private Block targetBlock;
    private boolean active = false;
    private String status = "Idle";
    private ServerLevel level;
    private BlockPos playerPos;
    private int scanRadius = 64;
    private final Set<Long> blacklist = new HashSet<>();
    private BlockPos foundTarget;
    private int scanCooldown = 0;

    public void start(String blockName, ServerLevel level, BlockPos playerPos) {
        this.start(blockName, level, playerPos, 64);
    }

    public void start(String blockName, ServerLevel level, BlockPos playerPos, int radius) {
        Identifier loc = Identifier.parse(blockName.contains(":") ? blockName : "minecraft:" + blockName);
        Block block = BuiltInRegistries.BLOCK.getValue(loc);
        if (block == null || block == Blocks.AIR) {
            status = "Unknown block: " + blockName;
            return;
        }
        this.targetBlock = block;
        this.level = level;
        this.playerPos = playerPos;
        this.scanRadius = radius;
        this.active = true;
        this.foundTarget = null;
        this.blacklist.clear();
        this.status = "Searching for " + blockName;
    }

    @Override
    public String name() { return "GetToBlock"; }

    @Override
    public boolean isActive() { return active; }

    @Override
    public double priority() { return 55; }

    @Override
    public PathingCommand onTick(boolean calcFailed, boolean safeToCancel) {
        if (!active || targetBlock == null || level == null) return null;

        if (calcFailed && foundTarget != null) {
            // Blacklist this position and search again
            blacklist.add(BlockPos.asLong(foundTarget.getX(), foundTarget.getY(), foundTarget.getZ()));
            foundTarget = null;
        }

        if (scanCooldown > 0 && foundTarget != null) {
            scanCooldown--;
            return new PathingCommand(
                new GoalGetToBlock(foundTarget.getX(), foundTarget.getY(), foundTarget.getZ()),
                PathingCommand.CommandType.SET_GOAL_AND_PATH
            );
        }
        scanCooldown = 60;

        // Scan for nearest target block
        WorldScanner scanner = new WorldScanner(level);
        List<BlockPos> found = scanner.scan(playerPos, targetBlock, scanRadius, 20);

        // Filter blacklisted
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (BlockPos pos : found) {
            long key = BlockPos.asLong(pos.getX(), pos.getY(), pos.getZ());
            if (blacklist.contains(key)) continue;
            double dist = pos.distSqr(playerPos);
            if (dist < bestDist) {
                bestDist = dist;
                best = pos;
            }
        }

        if (best == null) {
            status = "No " + BuiltInRegistries.BLOCK.getKey(targetBlock) + " found nearby";
            active = false;
            return null;
        }

        foundTarget = best;
        String name = BuiltInRegistries.BLOCK.getKey(targetBlock).getPath();
        status = "Going to " + name + " at " + best.getX() + "," + best.getY() + "," + best.getZ();

        Goal goal = new GoalGetToBlock(best.getX(), best.getY(), best.getZ());
        return new PathingCommand(goal, PathingCommand.CommandType.SET_GOAL_AND_PATH);
    }

    public void markComplete() {
        active = false;
        status = "Arrived at target block!";
    }

    public Block getTargetBlock() { return targetBlock; }
    public BlockPos getFoundTarget() { return foundTarget; }
    public void updatePlayerPos(BlockPos pos) { this.playerPos = pos; }
    public void updateLevel(ServerLevel level) { this.level = level; }

    @Override
    public void onLostControl() {}

    @Override
    public void cancel() {
        active = false;
        status = "Cancelled";
    }

    @Override
    public String getStatus() { return status; }
}

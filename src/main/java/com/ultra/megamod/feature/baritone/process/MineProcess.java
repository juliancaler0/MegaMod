package com.ultra.megamod.feature.baritone.process;

import com.ultra.megamod.feature.baritone.goals.Goal;
import com.ultra.megamod.feature.baritone.goals.GoalComposite;
import com.ultra.megamod.feature.baritone.goals.GoalGetToBlock;
import com.ultra.megamod.feature.baritone.pathfinding.WorldScanner;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.*;

/**
 * Process: find and mine specific block types.
 * Uses WorldScanner for efficient scanning, blacklists failed positions,
 * supports ore prioritization and quantity tracking.
 */
public class MineProcess implements BotProcess {
    private Block targetBlock;
    private int remaining;
    private int mined;
    private boolean active = false;
    private String status = "Idle";
    private ServerLevel level;
    private BlockPos playerPos;
    private int scanRadius = 32;
    private List<Goal> cachedTargets;
    private int scanCooldown = 0;
    private int scanInterval = 40;
    private final Set<Long> blacklist = new HashSet<>();

    /** Ore priority for multi-ore mining (higher = mine first) */
    private static final Map<Block, Integer> ORE_PRIORITY = new HashMap<>();
    static {
        ORE_PRIORITY.put(Blocks.DIAMOND_ORE, 100);
        ORE_PRIORITY.put(Blocks.DEEPSLATE_DIAMOND_ORE, 100);
        ORE_PRIORITY.put(Blocks.EMERALD_ORE, 90);
        ORE_PRIORITY.put(Blocks.DEEPSLATE_EMERALD_ORE, 90);
        ORE_PRIORITY.put(Blocks.GOLD_ORE, 70);
        ORE_PRIORITY.put(Blocks.DEEPSLATE_GOLD_ORE, 70);
        ORE_PRIORITY.put(Blocks.LAPIS_ORE, 65);
        ORE_PRIORITY.put(Blocks.DEEPSLATE_LAPIS_ORE, 65);
        ORE_PRIORITY.put(Blocks.REDSTONE_ORE, 60);
        ORE_PRIORITY.put(Blocks.DEEPSLATE_REDSTONE_ORE, 60);
        ORE_PRIORITY.put(Blocks.IRON_ORE, 50);
        ORE_PRIORITY.put(Blocks.DEEPSLATE_IRON_ORE, 50);
        ORE_PRIORITY.put(Blocks.COPPER_ORE, 40);
        ORE_PRIORITY.put(Blocks.DEEPSLATE_COPPER_ORE, 40);
        ORE_PRIORITY.put(Blocks.COAL_ORE, 30);
        ORE_PRIORITY.put(Blocks.DEEPSLATE_COAL_ORE, 30);
    }

    public void start(String blockName, int count, ServerLevel level, BlockPos playerPos) {
        this.start(blockName, count, level, playerPos, 32, 40);
    }

    public void start(String blockName, int count, ServerLevel level, BlockPos playerPos, int radius, int interval) {
        Identifier loc = Identifier.parse(blockName.contains(":") ? blockName : "minecraft:" + blockName);
        Block block = BuiltInRegistries.BLOCK.getValue(loc);
        if (block == null || block == Blocks.AIR) {
            status = "Unknown block: " + blockName;
            return;
        }
        this.targetBlock = block;
        this.remaining = count;
        this.mined = 0;
        this.active = true;
        this.level = level;
        this.playerPos = playerPos;
        this.scanRadius = radius;
        this.scanInterval = interval;
        this.blacklist.clear();
        this.cachedTargets = null;
        this.scanCooldown = 0;
        this.status = "Mining " + blockName + " (0/" + count + ")";
    }

    @Override
    public String name() { return "Mine"; }

    @Override
    public boolean isActive() { return active; }

    @Override
    public double priority() { return 60; }

    @Override
    public PathingCommand onTick(boolean calcFailed, boolean safeToCancel) {
        if (!active || targetBlock == null || level == null) return null;

        // If pathfinding failed, blacklist the target position
        if (calcFailed && cachedTargets != null && !cachedTargets.isEmpty()) {
            // Blacklist the first target (the one we were pathing to)
            for (Goal g : cachedTargets) {
                if (g instanceof GoalGetToBlock gtb) {
                    blacklist.add(BlockPos.asLong(gtb.x, gtb.y, gtb.z));
                    break;
                }
            }
            cachedTargets = null; // Force rescan
        }

        // Only rescan at the configured interval
        if (scanCooldown > 0 && cachedTargets != null && !cachedTargets.isEmpty()) {
            scanCooldown--;
            return new PathingCommand(new GoalComposite(cachedTargets.toArray(new Goal[0])), PathingCommand.CommandType.SET_GOAL_AND_PATH);
        }
        scanCooldown = scanInterval;

        // Use WorldScanner for efficient block scanning
        WorldScanner scanner = new WorldScanner(level);
        List<BlockPos> found = scanner.scan(playerPos, targetBlock, scanRadius, 40);

        // Filter blacklisted positions and sort by priority+distance
        List<BlockPos> filtered = new ArrayList<>();
        for (BlockPos pos : found) {
            long key = BlockPos.asLong(pos.getX(), pos.getY(), pos.getZ());
            if (!blacklist.contains(key)) {
                filtered.add(pos);
            }
        }

        // Sort by distance (already sorted by scanner, but re-sort with priority)
        filtered.sort((a, b) -> {
            double distA = a.distSqr(playerPos);
            double distB = b.distSqr(playerPos);
            return Double.compare(distA, distB);
        });

        cachedTargets = new ArrayList<>();
        for (int i = 0; i < Math.min(filtered.size(), 20); i++) {
            BlockPos pos = filtered.get(i);
            cachedTargets.add(new GoalGetToBlock(pos.getX(), pos.getY(), pos.getZ()));
        }

        if (cachedTargets.isEmpty()) {
            status = "No " + BuiltInRegistries.BLOCK.getKey(targetBlock) + " found nearby";
            active = false;
            return null;
        }

        Goal composite = new GoalComposite(cachedTargets.toArray(new Goal[0]));
        return new PathingCommand(composite, PathingCommand.CommandType.SET_GOAL_AND_PATH);
    }

    public void onBlockMined() {
        mined++;
        remaining--;
        String name = BuiltInRegistries.BLOCK.getKey(targetBlock).toString();
        status = "Mining " + name + " (" + mined + "/" + (mined + remaining) + ")";
        if (remaining <= 0) {
            active = false;
            status = "Done! Mined " + mined + " blocks";
        }
        // Force rescan after mining
        cachedTargets = null;
        scanCooldown = 0;
    }

    public Block getTargetBlock() { return targetBlock; }
    public int getMined() { return mined; }
    public int getRemaining() { return remaining; }

    public void updatePlayerPos(BlockPos pos) { this.playerPos = pos; }
    public void updateLevel(ServerLevel level) { this.level = level; }

    @Override
    public void onLostControl() {}

    @Override
    public void cancel() {
        active = false;
        cachedTargets = null;
        scanCooldown = 0;
        status = "Cancelled (" + mined + " mined)";
    }

    @Override
    public String getStatus() { return status; }
}

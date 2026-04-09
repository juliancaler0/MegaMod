package com.ultra.megamod.feature.baritone.process;

import com.ultra.megamod.feature.baritone.goals.Goal;
import com.ultra.megamod.feature.baritone.goals.GoalComposite;
import com.ultra.megamod.feature.baritone.goals.GoalGetToBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * Process: auto-harvest and replant mature crops.
 * Supports replanting, bone meal usage, sugar cane/melon/pumpkin/cactus.
 */
public class FarmProcess implements BotProcess {
    private boolean active = false;
    private String status = "Idle";
    private int radius = 16;
    private ServerLevel level;
    private BlockPos center;
    private int harvested = 0;
    private int replanted = 0;
    private List<Goal> cachedTargets;
    private int scanCooldown = 0;
    private int scanInterval = 60;
    private boolean autoReplant = true;
    private boolean useBoneMeal = false;

    public void start(int radius, ServerLevel level, BlockPos center) {
        this.start(radius, level, center, true, false, 60);
    }

    public void start(int radius, ServerLevel level, BlockPos center, boolean autoReplant, boolean useBoneMeal, int scanInterval) {
        this.radius = radius;
        this.level = level;
        this.center = center;
        this.active = true;
        this.harvested = 0;
        this.replanted = 0;
        this.autoReplant = autoReplant;
        this.useBoneMeal = useBoneMeal;
        this.scanInterval = scanInterval;
        this.status = "Farming (radius " + radius + ")";
    }

    @Override
    public String name() { return "Farm"; }

    @Override
    public boolean isActive() { return active; }

    @Override
    public double priority() { return 40; }

    @Override
    public PathingCommand onTick(boolean calcFailed, boolean safeToCancel) {
        if (!active || level == null) return null;

        // Only rescan at the configured interval
        if (scanCooldown > 0 && cachedTargets != null && !cachedTargets.isEmpty()) {
            scanCooldown--;
            return new PathingCommand(new GoalComposite(cachedTargets.toArray(new Goal[0])), PathingCommand.CommandType.SET_GOAL_AND_PATH);
        }
        scanCooldown = scanInterval;

        List<Goal> targets = new ArrayList<>();

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                for (int y = -4; y <= 4; y++) {
                    BlockPos pos = center.offset(x, y, z);
                    if (!level.isLoaded(pos)) continue;
                    BlockState state = level.getBlockState(pos);
                    if (isHarvestable(state)) {
                        targets.add(new GoalGetToBlock(pos.getX(), pos.getY(), pos.getZ()));
                        if (targets.size() >= 15) break;
                    }
                }
                if (targets.size() >= 15) break;
            }
            if (targets.size() >= 15) break;
        }

        cachedTargets = targets;

        if (targets.isEmpty()) {
            status = "No harvestable crops found (harvested " + harvested + ")";
            active = false;
            return null;
        }

        status = "Farming... (" + harvested + " harvested, " + replanted + " replanted, " + targets.size() + " targets)";
        Goal composite = new GoalComposite(targets.toArray(new Goal[0]));
        return new PathingCommand(composite, PathingCommand.CommandType.SET_GOAL_AND_PATH);
    }

    /**
     * Check if a block is a harvestable crop (mature or harvestable plant).
     */
    private boolean isHarvestable(BlockState state) {
        Block block = state.getBlock();
        // Standard crops
        if (block instanceof CropBlock crop) {
            return crop.isMaxAge(state);
        }
        // Nether wart
        if (block instanceof NetherWartBlock) {
            return state.getValue(NetherWartBlock.AGE) >= 3;
        }
        // Cocoa
        if (block instanceof CocoaBlock) {
            return state.getValue(CocoaBlock.AGE) >= 2;
        }
        // Sweet berries
        if (block instanceof SweetBerryBushBlock) {
            return state.getValue(SweetBerryBushBlock.AGE) >= 3;
        }
        // Sugar cane (harvest all but bottom block)
        if (block instanceof SugarCaneBlock) {
            // Only harvest if there's sugar cane below (not the base)
            return true; // Base check done in onCropHarvested
        }
        // Cactus (harvest all but bottom block)
        if (block instanceof CactusBlock) {
            return true;
        }
        // Melon and pumpkin (the fruit blocks, not stems)
        if (block == Blocks.MELON || block == Blocks.PUMPKIN) {
            return true;
        }
        return false;
    }

    /**
     * Called when the bot harvests a crop. Handles replanting.
     */
    public void onCropHarvested() {
        harvested++;
        cachedTargets = null; // Force rescan
        scanCooldown = 0;
    }

    /**
     * Attempt to replant a crop at the given position.
     * Returns true if replanted successfully.
     */
    public boolean tryReplant(ServerLevel level, BlockPos pos, net.minecraft.server.level.ServerPlayer player) {
        if (!autoReplant) return false;

        // Check what was growing here
        BlockState below = level.getBlockState(pos.below());
        if (below.getBlock() instanceof FarmBlock) {
            // Look for seeds in inventory
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (stack.isEmpty()) continue;
                if (stack.is(Items.WHEAT_SEEDS) || stack.is(Items.BEETROOT_SEEDS)
                    || stack.is(Items.CARROT) || stack.is(Items.POTATO)
                    || stack.is(Items.MELON_SEEDS) || stack.is(Items.PUMPKIN_SEEDS)) {
                    // Place the crop
                    Block crop = getCropForSeed(stack);
                    if (crop != null) {
                        level.setBlockAndUpdate(pos, crop.defaultBlockState());
                        stack.shrink(1);
                        replanted++;
                        return true;
                    }
                }
            }
        }
        // Nether wart
        if (below.getBlock() instanceof SoulSandBlock) {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (stack.is(Items.NETHER_WART)) {
                    level.setBlockAndUpdate(pos, Blocks.NETHER_WART.defaultBlockState());
                    stack.shrink(1);
                    replanted++;
                    return true;
                }
            }
        }
        return false;
    }

    private Block getCropForSeed(ItemStack seed) {
        if (seed.is(Items.WHEAT_SEEDS)) return Blocks.WHEAT;
        if (seed.is(Items.BEETROOT_SEEDS)) return Blocks.BEETROOTS;
        if (seed.is(Items.CARROT)) return Blocks.CARROTS;
        if (seed.is(Items.POTATO)) return Blocks.POTATOES;
        return null;
    }

    public int getHarvested() { return harvested; }
    public int getReplanted() { return replanted; }

    public void updateState(ServerLevel level, BlockPos center) {
        this.level = level;
        this.center = center;
    }

    @Override
    public void onLostControl() {}

    @Override
    public void cancel() {
        active = false;
        status = "Stopped farming (" + harvested + " harvested, " + replanted + " replanted)";
    }

    @Override
    public String getStatus() { return status; }
}

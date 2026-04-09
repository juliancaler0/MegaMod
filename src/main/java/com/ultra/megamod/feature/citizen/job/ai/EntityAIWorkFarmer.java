package com.ultra.megamod.feature.citizen.job.ai;

import com.ultra.megamod.feature.citizen.job.AIWorkerState;
import com.ultra.megamod.feature.citizen.job.JobFarmer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Farmer AI state machine. Implements the full farming workflow:
 * <ol>
 *   <li>Go to farm building</li>
 *   <li>Walk to assigned field</li>
 *   <li>Scan field for work: hoe, plant, or harvest</li>
 *   <li>Hoe dirt blocks into farmland</li>
 *   <li>Plant seeds on farmland</li>
 *   <li>Harvest mature crops</li>
 *   <li>Deposit harvested crops at building</li>
 * </ol>
 * <p>
 * Ported from MineColonies' EntityAIWorkFarmer, adapted for MegaMod.
 * Uses the building position as the default field area if no explicit
 * fields are assigned.
 */
public class EntityAIWorkFarmer extends AbstractEntityAIBasic {

    // ==================== Constants ====================

    /** How far to scan for farmland around the field center. */
    private static final int FIELD_SCAN_RADIUS = 7;

    /** Expanded radius per building level. */
    private static final int RADIUS_PER_LEVEL = 2;

    /** Maximum blocks processed before returning to dump. */
    private static final int MAX_BLOCKS_PROCESSED = 64;

    /** XP gained per harvest. */
    private static final double XP_PER_HARVEST = 0.5;

    /** Delay between individual block operations. */
    private static final int WORK_DELAY = 5;

    // ==================== Fields ====================

    private final JobFarmer farmerJob;

    /** The current field center we're working at. */
    @Nullable
    private BlockPos currentFieldCenter = null;

    /** Current scanning position within the field. */
    private int scanX = 0;
    private int scanZ = 0;
    private int scanRadius = FIELD_SCAN_RADIUS;

    /** Track what state of field work we need to do. */
    private boolean needsHoeing = false;
    private boolean needsPlanting = false;
    private boolean needsHarvesting = false;

    /** Flag: should dump inventory after completing the current field pass. */
    private boolean shouldDumpInventory = false;

    // ==================== Constructor ====================

    public EntityAIWorkFarmer(@NotNull JobFarmer job) {
        super(job);
        this.farmerJob = job;

        // Register farmer-specific state handlers
        registerTarget(AIWorkerState.FARMER_CHECK_FIELD, this::checkField);
        registerTarget(AIWorkerState.FARMER_WALKING_TO_FIELD, this::walkToField);
        registerTarget(AIWorkerState.FARMER_HOE, this::hoeField);
        registerTarget(AIWorkerState.FARMER_PLANT, this::plantField);
        registerTarget(AIWorkerState.FARMER_HARVEST, this::harvestField);
        registerTarget(AIWorkerState.PREPARING, this::prepareForFarming);
    }

    // ==================== Override Base Handlers ====================

    @Override
    protected AIWorkerState handleStartWorking() {
        if (!walkToBuilding()) {
            return AIWorkerState.START_WORKING;
        }
        return AIWorkerState.PREPARING;
    }

    @Override
    protected AIWorkerState handleDecide() {
        if (shouldDumpInventory || isInventoryFull() || job.getActionsDone() >= MAX_BLOCKS_PROCESSED) {
            shouldDumpInventory = false;
            return AIWorkerState.INVENTORY_FULL;
        }
        return AIWorkerState.FARMER_CHECK_FIELD;
    }

    // ==================== Farmer State Handlers ====================

    /**
     * PREPARING: Ensure the farmer has a hoe and seeds.
     */
    private AIWorkerState prepareForFarming() {
        // Check for a hoe in inventory
        boolean hasHoe = false;
        for (int i = 0; i < worker.getCitizenInventory().getContainerSize(); i++) {
            ItemStack stack = worker.getCitizenInventory().getItem(i);
            if (stack.getItem() instanceof HoeItem) {
                hasHoe = true;
                break;
            }
        }

        // If no hoe, equip a basic one (citizens start with tools in MegaMod)
        if (!hasHoe) {
            ItemStack hoe = new ItemStack(Items.WOODEN_HOE);
            addToInventory(hoe);
        }

        return AIWorkerState.FARMER_CHECK_FIELD;
    }

    /**
     * FARMER_CHECK_FIELD: Determine which field to work and what needs doing.
     */
    private AIWorkerState checkField() {
        // Select field center
        if (farmerJob.getAssignedFields().isEmpty()) {
            // No explicit fields - use area around the building
            currentFieldCenter = job.getBuildingPos();
        } else {
            farmerJob.nextField();
            currentFieldCenter = farmerJob.getCurrentFieldPos();
        }

        if (currentFieldCenter == null) {
            setDelay(TICKS_SECOND * 10);
            return AIWorkerState.IDLE;
        }

        // Calculate scan radius from building level
        scanRadius = FIELD_SCAN_RADIUS + (RADIUS_PER_LEVEL * getBuildingLevel());

        // Scan the field area to determine what work is needed
        needsHoeing = false;
        needsPlanting = false;
        needsHarvesting = false;

        for (int x = -scanRadius; x <= scanRadius && !(needsHoeing && needsPlanting && needsHarvesting); x++) {
            for (int z = -scanRadius; z <= scanRadius && !(needsHoeing && needsPlanting && needsHarvesting); z++) {
                BlockPos checkPos = currentFieldCenter.offset(x, 0, z);
                // Check at building Y and slightly above/below
                for (int dy = -2; dy <= 2; dy++) {
                    BlockPos pos = checkPos.above(dy);
                    BlockState state = world.getBlockState(pos);

                    if (canHoe(pos)) {
                        needsHoeing = true;
                    }
                    if (canPlant(pos)) {
                        needsPlanting = true;
                    }
                    if (canHarvest(pos, state)) {
                        needsHarvesting = true;
                    }
                }
            }
        }

        // Prioritize: harvest > plant > hoe
        if (needsHarvesting) {
            resetScan();
            return AIWorkerState.FARMER_WALKING_TO_FIELD;
        }
        if (needsPlanting) {
            resetScan();
            return AIWorkerState.FARMER_WALKING_TO_FIELD;
        }
        if (needsHoeing) {
            resetScan();
            return AIWorkerState.FARMER_WALKING_TO_FIELD;
        }

        // Nothing to do - wait and check again
        setDelay(TICKS_SECOND * 10);
        return AIWorkerState.DECIDE;
    }

    /**
     * FARMER_WALKING_TO_FIELD: Walk to the field center.
     */
    private AIWorkerState walkToField() {
        if (currentFieldCenter == null) {
            return AIWorkerState.FARMER_CHECK_FIELD;
        }

        if (!walkToBlock(currentFieldCenter)) {
            return AIWorkerState.FARMER_WALKING_TO_FIELD;
        }

        // Arrived at field - determine what to do
        if (needsHarvesting) return AIWorkerState.FARMER_HARVEST;
        if (needsPlanting) return AIWorkerState.FARMER_PLANT;
        if (needsHoeing) return AIWorkerState.FARMER_HOE;

        return AIWorkerState.DECIDE;
    }

    /**
     * FARMER_HOE: Scan the field and hoe any dirt/grass blocks near water.
     */
    private AIWorkerState hoeField() {
        if (currentFieldCenter == null) return AIWorkerState.DECIDE;

        // Scan through the field incrementally
        for (int attempts = 0; attempts < 4; attempts++) {
            if (scanX > scanRadius) {
                // Finished scanning
                resetScan();
                return AIWorkerState.FARMER_PLANT;
            }

            BlockPos checkPos = currentFieldCenter.offset(scanX, 0, scanZ);

            // Search Y range for a suitable block
            for (int dy = -2; dy <= 2; dy++) {
                BlockPos pos = checkPos.above(dy);
                if (canHoe(pos)) {
                    if (!isNear(pos, 3.0)) {
                        walkToBlock(pos);
                        return AIWorkerState.FARMER_HOE;
                    }

                    // Hoe the block
                    world.setBlock(pos, Blocks.FARMLAND.defaultBlockState(), Block.UPDATE_ALL);
                    worker.swing(InteractionHand.MAIN_HAND);
                    world.playSound(null, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0f, 1.0f);
                    job.incrementActionsDone();
                    setDelay(WORK_DELAY);
                    advanceScan();
                    return AIWorkerState.FARMER_HOE;
                }
            }

            advanceScan();
        }

        // Check if we need to dump
        if (job.getActionsDone() >= MAX_BLOCKS_PROCESSED) {
            return AIWorkerState.INVENTORY_FULL;
        }

        return AIWorkerState.FARMER_HOE;
    }

    /**
     * FARMER_PLANT: Plant seeds on empty farmland.
     */
    private AIWorkerState plantField() {
        if (currentFieldCenter == null) return AIWorkerState.DECIDE;

        for (int attempts = 0; attempts < 4; attempts++) {
            if (scanX > scanRadius) {
                // Finished scanning
                resetScan();
                return AIWorkerState.DECIDE;
            }

            BlockPos checkPos = currentFieldCenter.offset(scanX, 0, scanZ);

            for (int dy = -2; dy <= 2; dy++) {
                BlockPos pos = checkPos.above(dy);
                if (canPlant(pos)) {
                    if (!isNear(pos, 3.0)) {
                        walkToBlock(pos);
                        return AIWorkerState.FARMER_PLANT;
                    }

                    // Determine what to plant (prefer wheat seeds)
                    BlockState cropState = getCropToPlant();
                    if (cropState != null) {
                        world.setBlock(pos, cropState, Block.UPDATE_ALL);
                        worker.swing(InteractionHand.MAIN_HAND);
                        job.incrementActionsDone();
                        setDelay(WORK_DELAY);
                        advanceScan();
                        return AIWorkerState.FARMER_PLANT;
                    }
                }
            }

            advanceScan();
        }

        if (job.getActionsDone() >= MAX_BLOCKS_PROCESSED) {
            return AIWorkerState.INVENTORY_FULL;
        }

        return AIWorkerState.FARMER_PLANT;
    }

    /**
     * FARMER_HARVEST: Harvest mature crops.
     */
    private AIWorkerState harvestField() {
        if (currentFieldCenter == null) return AIWorkerState.DECIDE;

        for (int attempts = 0; attempts < 4; attempts++) {
            if (scanX > scanRadius) {
                // Finished scanning
                resetScan();
                shouldDumpInventory = true;
                return AIWorkerState.DECIDE;
            }

            BlockPos checkPos = currentFieldCenter.offset(scanX, 0, scanZ);

            for (int dy = -2; dy <= 2; dy++) {
                BlockPos pos = checkPos.above(dy);
                BlockState state = world.getBlockState(pos);

                if (canHarvest(pos, state)) {
                    if (!isNear(pos, 3.0)) {
                        walkToBlock(pos);
                        return AIWorkerState.FARMER_HARVEST;
                    }

                    // Harvest the crop
                    harvestCrop(pos, state);
                    worker.swing(InteractionHand.MAIN_HAND);
                    job.incrementActionsDone();

                    // Grant XP
                    worker.addRawXp(XP_PER_HARVEST);

                    setDelay(WORK_DELAY);
                    advanceScan();
                    return AIWorkerState.FARMER_HARVEST;
                }
            }

            advanceScan();
        }

        if (isInventoryFull() || job.getActionsDone() >= MAX_BLOCKS_PROCESSED) {
            return AIWorkerState.INVENTORY_FULL;
        }

        return AIWorkerState.FARMER_HARVEST;
    }

    // ==================== Helpers ====================

    /**
     * Check if a position can be hoed (is dirt/grass with water nearby).
     */
    private boolean canHoe(BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (!(state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK))) {
            return false;
        }

        // Check for water nearby (required for farmland to stay moist)
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                BlockPos waterCheck = pos.offset(dx, 0, dz);
                if (world.getBlockState(waterCheck).is(Blocks.WATER)) {
                    return true;
                }
                // Check one level below too
                if (world.getBlockState(waterCheck.below()).is(Blocks.WATER)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check if a position can accept a crop plant.
     */
    private boolean canPlant(BlockPos pos) {
        BlockState stateAbove = world.getBlockState(pos);
        if (!stateAbove.isAir()) {
            return false;
        }

        BlockState stateBelow = world.getBlockState(pos.below());
        return stateBelow.is(Blocks.FARMLAND);
    }

    /**
     * Check if a crop at the given position is harvestable (fully grown).
     */
    private boolean canHarvest(BlockPos pos, BlockState state) {
        Block block = state.getBlock();

        if (block instanceof CropBlock cropBlock) {
            return cropBlock.isMaxAge(state);
        }

        // Pumpkins and melons on stems
        if (block == Blocks.PUMPKIN || block == Blocks.MELON) {
            return true;
        }

        return false;
    }

    /**
     * Determine the crop block state to plant.
     */
    @Nullable
    private BlockState getCropToPlant() {
        // Check inventory for seeds
        for (int i = 0; i < worker.getCitizenInventory().getContainerSize(); i++) {
            ItemStack stack = worker.getCitizenInventory().getItem(i);
            if (stack.isEmpty()) continue;

            if (stack.is(Items.WHEAT_SEEDS)) {
                stack.shrink(1);
                return Blocks.WHEAT.defaultBlockState();
            }
            if (stack.is(Items.CARROT)) {
                stack.shrink(1);
                return Blocks.CARROTS.defaultBlockState();
            }
            if (stack.is(Items.POTATO)) {
                stack.shrink(1);
                return Blocks.POTATOES.defaultBlockState();
            }
            if (stack.is(Items.BEETROOT_SEEDS)) {
                stack.shrink(1);
                return Blocks.BEETROOTS.defaultBlockState();
            }
        }

        // No seeds available - plant wheat by default (farmer generates seeds)
        return Blocks.WHEAT.defaultBlockState();
    }

    /**
     * Harvest a crop and collect the drops.
     */
    private void harvestCrop(BlockPos pos, BlockState state) {
        if (!(world instanceof ServerLevel serverLevel)) return;

        Block block = state.getBlock();

        // Get drops
        List<ItemStack> drops = Block.getDrops(state, serverLevel, pos, world.getBlockEntity(pos));

        // Break the block
        world.destroyBlock(pos, false);

        // Collect drops
        for (ItemStack drop : drops) {
            ItemStack remainder = addToInventory(drop);
            if (!remainder.isEmpty()) {
                worker.spawnAtLocation(serverLevel, remainder);
            }
        }

        // Re-plant if this was a crop block (auto-replant)
        if (block instanceof CropBlock) {
            // Replant at age 0 if we got seeds
            boolean hasSeed = false;
            for (ItemStack drop : drops) {
                if (isSeed(drop)) {
                    hasSeed = true;
                    break;
                }
            }
            if (hasSeed) {
                world.setBlock(pos, block.defaultBlockState(), Block.UPDATE_ALL);
                // Consume one seed from inventory
                consumeSeed(block);
            }
        }
    }

    /**
     * Check if an item is a seed.
     */
    private boolean isSeed(ItemStack stack) {
        return stack.is(Items.WHEAT_SEEDS) ||
               stack.is(Items.BEETROOT_SEEDS) ||
               stack.is(Items.CARROT) ||
               stack.is(Items.POTATO) ||
               stack.is(Items.MELON_SEEDS) ||
               stack.is(Items.PUMPKIN_SEEDS);
    }

    /**
     * Consume one seed from inventory for replanting.
     */
    private void consumeSeed(Block cropBlock) {
        ItemStack seedItem;
        if (cropBlock == Blocks.WHEAT) {
            seedItem = new ItemStack(Items.WHEAT_SEEDS);
        } else if (cropBlock == Blocks.CARROTS) {
            seedItem = new ItemStack(Items.CARROT);
        } else if (cropBlock == Blocks.POTATOES) {
            seedItem = new ItemStack(Items.POTATO);
        } else if (cropBlock == Blocks.BEETROOTS) {
            seedItem = new ItemStack(Items.BEETROOT_SEEDS);
        } else {
            return;
        }

        // Remove one from inventory
        for (int i = 0; i < worker.getCitizenInventory().getContainerSize(); i++) {
            ItemStack stack = worker.getCitizenInventory().getItem(i);
            if (ItemStack.isSameItem(stack, seedItem)) {
                stack.shrink(1);
                return;
            }
        }
    }

    /**
     * Reset the field scanner.
     */
    private void resetScan() {
        scanX = -scanRadius;
        scanZ = -scanRadius;
    }

    /**
     * Advance the scan position.
     */
    private void advanceScan() {
        scanZ++;
        if (scanZ > scanRadius) {
            scanZ = -scanRadius;
            scanX++;
        }
    }
}

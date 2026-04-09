package com.ultra.megamod.feature.citizen.job.ai;

import com.ultra.megamod.feature.citizen.job.AIWorkerState;
import com.ultra.megamod.feature.citizen.job.JobFisherman;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Fisherman AI state machine. Implements the full fishing workflow:
 * <ol>
 *   <li>Go to fisherman's hut</li>
 *   <li>Search for a body of water</li>
 *   <li>Walk to the water's edge</li>
 *   <li>Cast the fishing line</li>
 *   <li>Wait for a bite (random duration based on skill)</li>
 *   <li>Reel in the catch</li>
 *   <li>Deposit fish at the hut</li>
 * </ol>
 * <p>
 * Ported from MineColonies' EntityAIWorkFisherman, adapted for MegaMod.
 * Uses simplified fishing mechanics (no actual fishing hook entity) -
 * the citizen stands at the water's edge and periodically produces fish.
 */
public class EntityAIWorkFisherman extends AbstractEntityAIBasic {

    // ==================== Constants ====================

    /** Search range for water. */
    private static final int WATER_SEARCH_RANGE = 30;

    /** Maximum ponds to remember. */
    private static final int MAX_PONDS = 20;

    /** Fish catches before dumping inventory. */
    private static final int MAX_CATCHES = 10;

    /** Base fishing time (ticks). Reduced by skill level. */
    private static final int BASE_FISHING_TIME = 200; // 10 seconds

    /** Fishing time reduction per building level. */
    private static final int FISHING_TIME_REDUCTION_PER_LEVEL = 20;

    /** XP per catch. */
    private static final double XP_PER_CATCH = 1.0;

    /** Minimum distance to water to fish. */
    private static final double MIN_WATER_DISTANCE = 2.0;

    /** Maximum distance to water to fish. */
    private static final double MAX_WATER_DISTANCE = 4.0;

    // ==================== Fields ====================

    private final JobFisherman fishJob;

    /** Timer for the current fishing action. */
    private int fishingTimer = 0;

    /** Number of catches this session. */
    private int catchCount = 0;

    /** Number of failed attempts at finding water. */
    private int searchAttempts = 0;

    /** Maximum search attempts before giving up. */
    private static final int MAX_SEARCH_ATTEMPTS = 10;

    // ==================== Constructor ====================

    public EntityAIWorkFisherman(@NotNull JobFisherman job) {
        super(job);
        this.fishJob = job;

        // Register fisherman-specific state handlers
        registerTarget(AIWorkerState.FISHERMAN_SEARCHING_WATER, this::searchForWater);
        registerTarget(AIWorkerState.FISHERMAN_WALKING_TO_WATER, this::walkToWater);
        registerTarget(AIWorkerState.FISHERMAN_CHECK_WATER, this::checkWater);
        registerTarget(AIWorkerState.FISHERMAN_START_FISHING, this::doFishing);
        registerTarget(AIWorkerState.PREPARING, this::prepareForFishing);
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
        if (isInventoryFull() || catchCount >= MAX_CATCHES || job.getActionsDone() >= MAX_CATCHES) {
            catchCount = 0;
            return AIWorkerState.INVENTORY_FULL;
        }
        return AIWorkerState.FISHERMAN_SEARCHING_WATER;
    }

    // ==================== Fisherman State Handlers ====================

    /**
     * PREPARING: Make sure we have a fishing rod.
     */
    private AIWorkerState prepareForFishing() {
        boolean hasRod = false;
        for (int i = 0; i < worker.getCitizenInventory().getContainerSize(); i++) {
            if (worker.getCitizenInventory().getItem(i).is(Items.FISHING_ROD)) {
                hasRod = true;
                break;
            }
        }

        if (!hasRod) {
            // Give the fisherman a fishing rod
            addToInventory(new ItemStack(Items.FISHING_ROD));
        }

        // Equip the rod in main hand
        for (int i = 0; i < worker.getCitizenInventory().getContainerSize(); i++) {
            ItemStack stack = worker.getCitizenInventory().getItem(i);
            if (stack.is(Items.FISHING_ROD)) {
                worker.setItemInHand(InteractionHand.MAIN_HAND, stack.copy());
                break;
            }
        }

        return AIWorkerState.FISHERMAN_SEARCHING_WATER;
    }

    /**
     * FISHERMAN_SEARCHING_WATER: Look for a body of water near the building.
     */
    private AIWorkerState searchForWater() {
        // Check if we already know a pond
        if (fishJob.getCurrentWater() != null) {
            // Verify it's still water
            if (world.getBlockState(fishJob.getCurrentWater()).is(Blocks.WATER)) {
                return AIWorkerState.FISHERMAN_WALKING_TO_WATER;
            }
            // Invalid - clear it
            fishJob.setCurrentWater(null);
            fishJob.setCurrentStand(null);
        }

        // Check known ponds
        for (JobFisherman.PondEntry pond : fishJob.getPonds()) {
            if (world.getBlockState(pond.water).is(Blocks.WATER)) {
                fishJob.setCurrentWater(pond.water);
                fishJob.setCurrentStand(pond.stand);
                return AIWorkerState.FISHERMAN_WALKING_TO_WATER;
            }
        }

        // Search for new water
        BlockPos searchCenter = job.getBuildingPos() != null ? job.getBuildingPos() : worker.blockPosition();

        for (int attempt = 0; attempt < 20; attempt++) {
            int dx = worker.getRandom().nextInt(WATER_SEARCH_RANGE * 2) - WATER_SEARCH_RANGE;
            int dz = worker.getRandom().nextInt(WATER_SEARCH_RANGE * 2) - WATER_SEARCH_RANGE;
            BlockPos candidate = searchCenter.offset(dx, 0, dz);

            // Search vertically for water
            for (int dy = -5; dy <= 5; dy++) {
                BlockPos waterPos = candidate.above(dy);
                if (world.getBlockState(waterPos).is(Blocks.WATER)) {
                    // Found water - now find a land block to stand on
                    BlockPos standPos = findStandPosition(waterPos);
                    if (standPos != null) {
                        fishJob.setCurrentWater(waterPos);
                        fishJob.setCurrentStand(standPos);

                        // Remember this pond
                        if (fishJob.getPonds().size() < MAX_PONDS) {
                            fishJob.addPond(waterPos, standPos);
                        }

                        return AIWorkerState.FISHERMAN_WALKING_TO_WATER;
                    }
                }
            }
        }

        searchAttempts++;
        if (searchAttempts >= MAX_SEARCH_ATTEMPTS) {
            searchAttempts = 0;
            setDelay(TICKS_SECOND * 30);
            return AIWorkerState.IDLE;
        }

        setDelay(TICKS_SECOND * 3);
        return AIWorkerState.FISHERMAN_SEARCHING_WATER;
    }

    /**
     * FISHERMAN_WALKING_TO_WATER: Walk to the fishing spot.
     */
    private AIWorkerState walkToWater() {
        BlockPos standPos = fishJob.getCurrentStand();
        if (standPos == null) {
            return AIWorkerState.FISHERMAN_SEARCHING_WATER;
        }

        if (!walkToBlock(standPos)) {
            return AIWorkerState.FISHERMAN_WALKING_TO_WATER;
        }

        return AIWorkerState.FISHERMAN_CHECK_WATER;
    }

    /**
     * FISHERMAN_CHECK_WATER: Verify the water is still valid before fishing.
     */
    private AIWorkerState checkWater() {
        BlockPos waterPos = fishJob.getCurrentWater();
        if (waterPos == null || !world.getBlockState(waterPos).is(Blocks.WATER)) {
            fishJob.setCurrentWater(null);
            fishJob.setCurrentStand(null);
            return AIWorkerState.FISHERMAN_SEARCHING_WATER;
        }

        // Look at the water
        worker.getLookControl().setLookAt(
                waterPos.getX() + 0.5, waterPos.getY() + 0.5, waterPos.getZ() + 0.5);

        fishingTimer = calculateFishingTime();
        return AIWorkerState.FISHERMAN_START_FISHING;
    }

    /**
     * FISHERMAN_START_FISHING: Cast the line and wait for a bite.
     */
    private AIWorkerState doFishing() {
        BlockPos waterPos = fishJob.getCurrentWater();
        if (waterPos == null) {
            return AIWorkerState.FISHERMAN_SEARCHING_WATER;
        }

        // Look at the water
        worker.getLookControl().setLookAt(
                waterPos.getX() + 0.5, waterPos.getY() + 0.5, waterPos.getZ() + 0.5);

        // Count down fishing timer
        fishingTimer -= AI_TICK_RATE;

        if (fishingTimer > 0) {
            // Still fishing - swing rod periodically for animation
            if (fishingTimer % 40 == 0) {
                worker.swing(InteractionHand.MAIN_HAND);
            }
            return AIWorkerState.FISHERMAN_START_FISHING;
        }

        // Fish is caught!
        catchFish();
        catchCount++;

        // Check if we should continue or return
        if (isInventoryFull() || catchCount >= MAX_CATCHES) {
            return AIWorkerState.INVENTORY_FULL;
        }

        // Random chance to try a different pond
        if (worker.getRandom().nextFloat() < 0.1f) {
            fishJob.setCurrentWater(null);
            fishJob.setCurrentStand(null);
            return AIWorkerState.FISHERMAN_SEARCHING_WATER;
        }

        // Fish again
        fishingTimer = calculateFishingTime();
        return AIWorkerState.FISHERMAN_START_FISHING;
    }

    // ==================== Helpers ====================

    /**
     * Calculate how long fishing takes based on building level.
     */
    private int calculateFishingTime() {
        int level = getBuildingLevel();
        int time = BASE_FISHING_TIME - (level * FISHING_TIME_REDUCTION_PER_LEVEL);
        // Add some randomness
        time += worker.getRandom().nextInt(60) - 30;
        return Math.max(40, time); // Minimum 2 seconds
    }

    /**
     * Generate a fish catch and add to inventory.
     */
    private void catchFish() {
        // Determine what was caught
        ItemStack catchItem = generateCatch();

        // Add to inventory
        ItemStack remainder = addToInventory(catchItem);
        if (!remainder.isEmpty() && world instanceof ServerLevel serverLevel) {
            worker.spawnAtLocation(serverLevel, remainder);
        }

        // Sound effect
        world.playSound(null, worker.blockPosition(), SoundEvents.FISHING_BOBBER_SPLASH,
                SoundSource.NEUTRAL, 0.5f, 1.0f);

        // Visual feedback
        worker.swing(InteractionHand.MAIN_HAND);

        // XP
        worker.addRawXp(XP_PER_CATCH);
        job.incrementActionsDone();
    }

    /**
     * Generate a random fish catch based on building level.
     */
    private ItemStack generateCatch() {
        int level = getBuildingLevel();
        float roll = worker.getRandom().nextFloat();

        // Higher building levels give better fish
        if (level >= 4 && roll < 0.1f) {
            return new ItemStack(Items.PUFFERFISH);
        }
        if (level >= 3 && roll < 0.2f) {
            return new ItemStack(Items.TROPICAL_FISH);
        }
        if (level >= 2 && roll < 0.4f) {
            return new ItemStack(Items.SALMON);
        }

        // Bonus items at high levels
        if (level >= 5 && roll < 0.05f) {
            return new ItemStack(Items.NAME_TAG);
        }
        if (level >= 3 && roll < 0.08f) {
            return new ItemStack(Items.NAUTILUS_SHELL);
        }

        // Default: raw cod
        return new ItemStack(Items.COD);
    }

    /**
     * Find a solid land block adjacent to the water to stand on.
     */
    @Nullable
    private BlockPos findStandPosition(@NotNull BlockPos waterPos) {
        // Check all 4 horizontal directions for solid ground
        BlockPos[] candidates = {
                waterPos.north(), waterPos.south(),
                waterPos.east(), waterPos.west(),
                waterPos.north().east(), waterPos.north().west(),
                waterPos.south().east(), waterPos.south().west()
        };

        for (BlockPos candidate : candidates) {
            // Check for solid ground at water level or slightly above
            for (int dy = -1; dy <= 1; dy++) {
                BlockPos checkPos = candidate.above(dy);
                BlockState groundState = world.getBlockState(checkPos);
                BlockState aboveState = world.getBlockState(checkPos.above());
                BlockState above2State = world.getBlockState(checkPos.above(2));

                if (!groundState.isAir()
                        && !groundState.getFluidState().is(Fluids.WATER)
                        && aboveState.isAir()
                        && above2State.isAir()) {
                    return checkPos.above(); // Stand on top of the solid block
                }
            }
        }

        return null;
    }
}

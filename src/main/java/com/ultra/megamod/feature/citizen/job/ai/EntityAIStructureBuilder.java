package com.ultra.megamod.feature.citizen.job.ai;

import com.ultra.megamod.feature.citizen.job.AIWorkerState;
import com.ultra.megamod.feature.citizen.job.JobBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Builder AI state machine. Implements the building workflow:
 * <ol>
 *   <li>Go to builder's hut</li>
 *   <li>Check for work orders</li>
 *   <li>Walk to the build site</li>
 *   <li>Place blocks from the blueprint</li>
 *   <li>Request missing materials</li>
 *   <li>Complete the build</li>
 * </ol>
 * <p>
 * Ported from MineColonies' EntityAIStructureBuilder, heavily simplified.
 * Since MegaMod's schematic system is separate, this AI operates on
 * simple work orders (position + type) rather than full structure iteration.
 * <p>
 * For now the builder walks to work order sites and performs basic
 * block placement. Full schematic-based building will be integrated
 * once the schematic system is connected.
 */
public class EntityAIStructureBuilder extends AbstractEntityAIBasic {

    // ==================== Constants ====================

    /** Actions before dumping. */
    private static final int ACTIONS_UNTIL_DUMP = 128;

    /** Delay between placement steps. */
    private static final int PLACEMENT_DELAY = 3;

    // ==================== Fields ====================

    private final JobBuilder builderJob;

    /** Position being built at during the current step. */
    @Nullable
    private BlockPos currentPlacePos = null;

    /** Progress counter for the current build. */
    private int buildProgress = 0;

    // ==================== Constructor ====================

    public EntityAIStructureBuilder(@NotNull JobBuilder job) {
        super(job);
        this.builderJob = job;

        // Register builder-specific state handlers
        registerTarget(AIWorkerState.BUILDER_LOAD_STRUCTURE, this::loadStructure);
        registerTarget(AIWorkerState.BUILDER_BUILDING_STEP, this::buildingStep);
        registerTarget(AIWorkerState.BUILDER_COMPLETE_BUILD, this::completeBuild);
        registerTarget(AIWorkerState.BUILDER_PICK_UP, this::pickUpMaterials);
        registerTarget(AIWorkerState.BUILDER_REQUEST_MATERIALS, this::requestMaterials);
    }

    // ==================== Override Base Handlers ====================

    @Override
    protected AIWorkerState handleStartWorking() {
        if (!walkToBuilding()) {
            return AIWorkerState.START_WORKING;
        }

        // Check for work orders
        if (builderJob.hasWorkOrder()) {
            return AIWorkerState.BUILDER_LOAD_STRUCTURE;
        }

        // No work to do - wait
        setDelay(TICKS_SECOND * 10);
        return AIWorkerState.IDLE;
    }

    @Override
    protected AIWorkerState handleDecide() {
        if (isInventoryFull() || job.getActionsDone() >= ACTIONS_UNTIL_DUMP) {
            return AIWorkerState.INVENTORY_FULL;
        }

        if (builderJob.hasWorkOrder()) {
            return AIWorkerState.BUILDER_LOAD_STRUCTURE;
        }

        setDelay(TICKS_SECOND * 10);
        return AIWorkerState.IDLE;
    }

    // ==================== Builder State Handlers ====================

    /**
     * BUILDER_LOAD_STRUCTURE: Validate the work order and prepare to build.
     */
    private AIWorkerState loadStructure() {
        BlockPos workOrderPos = builderJob.getWorkOrderPos();
        if (workOrderPos == null) {
            return AIWorkerState.DECIDE;
        }

        // Walk to the build site
        if (!walkToBlock(workOrderPos)) {
            return AIWorkerState.BUILDER_LOAD_STRUCTURE;
        }

        buildProgress = 0;
        return AIWorkerState.BUILDER_BUILDING_STEP;
    }

    /**
     * BUILDER_BUILDING_STEP: Place blocks at the build site.
     * This is a simplified implementation that clears the area
     * and places a basic platform. Full schematic integration
     * would iterate through the blueprint.
     */
    private AIWorkerState buildingStep() {
        BlockPos workOrderPos = builderJob.getWorkOrderPos();
        if (workOrderPos == null) {
            return AIWorkerState.BUILDER_COMPLETE_BUILD;
        }

        // Simple building: clear a 5x5 area and place a foundation
        int buildRadius = 2 + getBuildingLevel();
        int totalBlocks = (buildRadius * 2 + 1) * (buildRadius * 2 + 1);

        if (buildProgress >= totalBlocks) {
            return AIWorkerState.BUILDER_COMPLETE_BUILD;
        }

        // Calculate position from progress
        int row = buildProgress / (buildRadius * 2 + 1);
        int col = buildProgress % (buildRadius * 2 + 1);
        int dx = col - buildRadius;
        int dz = row - buildRadius;

        BlockPos placePos = workOrderPos.offset(dx, 0, dz);

        // Walk close enough to place
        if (!isNear(placePos, 4.0)) {
            walkToBlock(placePos);
            return AIWorkerState.BUILDER_BUILDING_STEP;
        }

        // Clear above (2 blocks for headroom)
        BlockPos above1 = placePos.above();
        BlockPos above2 = placePos.above(2);
        if (!world.getBlockState(above1).isAir()) {
            mineBlock(above1);
            setDelay(PLACEMENT_DELAY);
            return AIWorkerState.BUILDER_BUILDING_STEP;
        }
        if (!world.getBlockState(above2).isAir()) {
            mineBlock(above2);
            setDelay(PLACEMENT_DELAY);
            return AIWorkerState.BUILDER_BUILDING_STEP;
        }

        // Place foundation block if air
        if (world.getBlockState(placePos).isAir()) {
            placeBlock(placePos, Blocks.COBBLESTONE.defaultBlockState());
            setDelay(PLACEMENT_DELAY);
        }

        buildProgress++;
        worker.swing(InteractionHand.MAIN_HAND);

        if (isInventoryFull() || job.getActionsDone() >= ACTIONS_UNTIL_DUMP) {
            return AIWorkerState.INVENTORY_FULL;
        }

        return AIWorkerState.BUILDER_BUILDING_STEP;
    }

    /**
     * BUILDER_COMPLETE_BUILD: Mark the build as done.
     */
    private AIWorkerState completeBuild() {
        builderJob.clearWorkOrder();
        buildProgress = 0;
        return AIWorkerState.DECIDE;
    }

    /**
     * BUILDER_PICK_UP: Pick up materials from the building storage.
     */
    private AIWorkerState pickUpMaterials() {
        if (!walkToBuilding()) {
            return AIWorkerState.BUILDER_PICK_UP;
        }

        // In a full implementation, this would pull materials from chests
        // For now, just proceed to building
        return AIWorkerState.BUILDER_BUILDING_STEP;
    }

    /**
     * BUILDER_REQUEST_MATERIALS: Request missing materials.
     */
    private AIWorkerState requestMaterials() {
        // In a full implementation, this would create delivery requests
        // For now, just wait and retry
        setDelay(TICKS_SECOND * 10);
        return AIWorkerState.BUILDER_BUILDING_STEP;
    }
}

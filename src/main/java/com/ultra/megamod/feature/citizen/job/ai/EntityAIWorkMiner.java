package com.ultra.megamod.feature.citizen.job.ai;

import com.ultra.megamod.feature.citizen.job.AIWorkerState;
import com.ultra.megamod.feature.citizen.job.JobMiner;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Miner AI state machine. Implements the full mining workflow:
 * <ol>
 *   <li>Go to mine building</li>
 *   <li>Check/create mineshaft (ladder down)</li>
 *   <li>Walk to ladder entrance</li>
 *   <li>Mine shaft downward (dig 1x1 ladder shaft)</li>
 *   <li>Mine branch tunnels at each level</li>
 *   <li>Collect ores, return to deposit</li>
 * </ol>
 * <p>
 * Ported from MineColonies' EntityAIStructureMiner, simplified to use
 * direct block manipulation instead of structure placer.
 * <p>
 * Building level gates the maximum mining depth:
 * Level 1: Y=48, Level 2: Y=32, Level 3: Y=16, Level 4: Y=0, Level 5: Y=-48
 */
public class EntityAIWorkMiner extends AbstractEntityAIBasic {

    // ==================== Constants ====================

    /** Distance between branch tunnel levels. */
    private static final int LEVEL_SPACING = 6;

    /** Branch tunnel length per building level. */
    private static final int BRANCH_LENGTH_PER_LEVEL = 12;

    /** Maximum branch length. */
    private static final int MAX_BRANCH_LENGTH = 40;

    /** How many blocks to mine before dumping inventory. */
    private static final int MAX_BLOCKS_MINED = 64;

    /** Shaft radius (the shaft is 1x1 ladder with 3x3 landing). */
    private static final int SHAFT_RADIUS = 1;

    /** Minimum Y for each building level. */
    private static final int[] MIN_Y_PER_LEVEL = {48, 32, 16, 0, -48};

    /** The miner job. */
    private final JobMiner minerJob;

    // ---- Branch mining state ----
    private Direction branchDirection = Direction.NORTH;
    private int branchProgress = 0;
    private int branchLength = 0;
    private int currentBranchY = 0;
    @Nullable
    private BlockPos currentMiningTarget = null;

    // ==================== Constructor ====================

    public EntityAIWorkMiner(@NotNull JobMiner job) {
        super(job);
        this.minerJob = job;

        // Register miner-specific state handlers
        registerTarget(AIWorkerState.MINER_CHECK_MINESHAFT, this::checkMineshaft);
        registerTarget(AIWorkerState.MINER_WALKING_TO_LADDER, this::walkToLadder);
        registerTarget(AIWorkerState.MINER_REPAIRING_LADDER, this::repairLadder);
        registerTarget(AIWorkerState.MINER_MINING_SHAFT, this::mineShaft);
        registerTarget(AIWorkerState.MINER_BUILDING_SHAFT, this::buildShaft);
        registerTarget(AIWorkerState.MINER_MINING_NODE, this::mineNode);
    }

    // ==================== Override Base Handlers ====================

    @Override
    protected AIWorkerState handleStartWorking() {
        if (!walkToBuilding()) {
            return AIWorkerState.START_WORKING;
        }
        return AIWorkerState.MINER_CHECK_MINESHAFT;
    }

    @Override
    protected AIWorkerState handleDecide() {
        if (isInventoryFull() || job.getActionsDone() >= MAX_BLOCKS_MINED) {
            return AIWorkerState.INVENTORY_FULL;
        }
        return AIWorkerState.MINER_CHECK_MINESHAFT;
    }

    // ==================== Miner State Handlers ====================

    /**
     * MINER_CHECK_MINESHAFT: Verify the mine shaft exists, or initialize it.
     */
    private AIWorkerState checkMineshaft() {
        BlockPos buildingPos = job.getBuildingPos();
        if (buildingPos == null) {
            setDelay(TICKS_SECOND * 5);
            return AIWorkerState.IDLE;
        }

        // Initialize shaft start Y from building position
        if (minerJob.getShaftStartY() < 0) {
            minerJob.setShaftStartY(buildingPos.getY() - 1);
        }

        // Find or create ladder position (directly below the building)
        if (minerJob.getLadderPos() == null) {
            BlockPos ladderPos = buildingPos.below();
            minerJob.setLadderPos(ladderPos);
        }

        return AIWorkerState.MINER_WALKING_TO_LADDER;
    }

    /**
     * MINER_WALKING_TO_LADDER: Walk to the ladder shaft entrance.
     */
    private AIWorkerState walkToLadder() {
        BlockPos ladderPos = minerJob.getLadderPos();
        if (ladderPos == null) {
            return AIWorkerState.MINER_CHECK_MINESHAFT;
        }

        // Walk to above the ladder
        BlockPos targetPos = new BlockPos(ladderPos.getX(), minerJob.getShaftStartY() + 1, ladderPos.getZ());
        if (!walkToBlock(targetPos)) {
            return AIWorkerState.MINER_WALKING_TO_LADDER;
        }

        return AIWorkerState.MINER_MINING_SHAFT;
    }

    /**
     * MINER_REPAIRING_LADDER: Place ladders along the shaft if missing.
     */
    private AIWorkerState repairLadder() {
        // For simplicity, we just proceed to mining
        // A full implementation would check each ladder block
        return AIWorkerState.MINER_MINING_SHAFT;
    }

    /**
     * MINER_MINING_SHAFT: Dig the main shaft downward.
     * Mines a 1x1 column straight down, placing ladders on the wall.
     */
    private AIWorkerState mineShaft() {
        BlockPos ladderPos = minerJob.getLadderPos();
        if (ladderPos == null) {
            return AIWorkerState.MINER_CHECK_MINESHAFT;
        }

        // Calculate minimum Y based on building level
        int buildingLevel = Math.max(1, Math.min(5, getBuildingLevel()));
        int minY = MIN_Y_PER_LEVEL[buildingLevel - 1];

        // Find the current bottom of the shaft
        int currentLevel = minerJob.getCurrentMineLevel();
        int targetY = minerJob.getShaftStartY() - (currentLevel * LEVEL_SPACING);

        // Check if we've reached maximum depth
        if (targetY <= minY) {
            // Start mining branches at the deepest level
            currentBranchY = Math.max(targetY + LEVEL_SPACING, minY + 1);
            return AIWorkerState.MINER_MINING_NODE;
        }

        // Mine the next shaft section
        BlockPos digPos = new BlockPos(ladderPos.getX(), targetY, ladderPos.getZ());

        // Walk close to the digging position
        if (!isNear(digPos, 4.0)) {
            walkToBlock(digPos.above(2));
            return AIWorkerState.MINER_MINING_SHAFT;
        }

        // Dig a 1x1 column (the shaft)
        BlockState stateAtDig = world.getBlockState(digPos);
        if (!stateAtDig.isAir() && !isLiquid(stateAtDig)) {
            if (!mineBlock(digPos)) {
                return AIWorkerState.MINER_MINING_SHAFT;
            }
            worker.swing(InteractionHand.MAIN_HAND);
            setDelay(STANDARD_DELAY);
            return AIWorkerState.MINER_MINING_SHAFT;
        }

        // Place a ladder on the north wall of the shaft
        BlockPos ladderPlacePos = digPos;
        BlockState ladderState = Blocks.LADDER.defaultBlockState();
        if (world.getBlockState(ladderPlacePos).isAir()) {
            // Place ladder (simplified - in reality we'd set the facing direction)
            world.setBlock(ladderPlacePos, ladderState, Block.UPDATE_ALL);
        }

        // Move to the next level
        minerJob.setCurrentMineLevel(currentLevel + 1);

        // Check inventory
        if (isInventoryFull() || job.getActionsDone() >= MAX_BLOCKS_MINED) {
            return AIWorkerState.INVENTORY_FULL;
        }

        return AIWorkerState.MINER_MINING_SHAFT;
    }

    /**
     * MINER_BUILDING_SHAFT: Build shaft supports (torches, supports).
     */
    private AIWorkerState buildShaft() {
        // Simplified: proceed to node mining
        return AIWorkerState.MINER_MINING_NODE;
    }

    /**
     * MINER_MINING_NODE: Mine a branch tunnel at the current level.
     * Creates horizontal branches from the main shaft.
     */
    private AIWorkerState mineNode() {
        BlockPos ladderPos = minerJob.getLadderPos();
        if (ladderPos == null) {
            return AIWorkerState.MINER_CHECK_MINESHAFT;
        }

        // Initialize branch if needed
        if (branchLength == 0) {
            int buildingLevel = Math.max(1, Math.min(5, getBuildingLevel()));
            branchLength = Math.min(BRANCH_LENGTH_PER_LEVEL * buildingLevel, MAX_BRANCH_LENGTH);
            branchProgress = 0;

            // Calculate mining Y level
            int minY = MIN_Y_PER_LEVEL[buildingLevel - 1];
            if (currentBranchY == 0) {
                currentBranchY = minerJob.getShaftStartY() - ((minerJob.getCurrentMineLevel() - 1) * LEVEL_SPACING);
            }
            currentBranchY = Math.max(currentBranchY, minY + 1);
        }

        // Check if branch is complete
        if (branchProgress >= branchLength) {
            // Cycle to next direction
            branchDirection = nextDirection(branchDirection);
            branchProgress = 0;

            // After all 4 directions, go deeper or return
            if (branchDirection == Direction.NORTH) {
                // All branches at this level done, go deeper
                currentBranchY -= LEVEL_SPACING;
                int buildingLevel = Math.max(1, Math.min(5, getBuildingLevel()));
                int minY = MIN_Y_PER_LEVEL[buildingLevel - 1];
                if (currentBranchY <= minY) {
                    // Can't go deeper, reset and start over
                    currentBranchY = minerJob.getShaftStartY() - LEVEL_SPACING;
                    branchLength = 0;
                    setDelay(TICKS_SECOND * 30); // Wait before restarting
                    return AIWorkerState.DECIDE;
                }
            }

            return AIWorkerState.MINER_MINING_NODE;
        }

        // Calculate the target mining position
        int dx = branchDirection.getStepX() * (branchProgress + 1);
        int dz = branchDirection.getStepZ() * (branchProgress + 1);
        currentMiningTarget = new BlockPos(
                ladderPos.getX() + dx,
                currentBranchY,
                ladderPos.getZ() + dz
        );

        // Walk to mining position
        if (!isNear(currentMiningTarget, 3.0)) {
            walkToBlock(currentMiningTarget);
            return AIWorkerState.MINER_MINING_NODE;
        }

        // Mine the 1x2 tunnel (block at feet level and head level)
        BlockPos feetPos = currentMiningTarget;
        BlockPos headPos = currentMiningTarget.above();

        boolean minedFeet = false;
        boolean minedHead = false;

        BlockState feetState = world.getBlockState(feetPos);
        if (!feetState.isAir() && !isLiquid(feetState)) {
            mineBlock(feetPos);
            minedFeet = true;
        }

        BlockState headState = world.getBlockState(headPos);
        if (!headState.isAir() && !isLiquid(headState)) {
            mineBlock(headPos);
            minedHead = true;
        }

        if (minedFeet || minedHead) {
            setDelay(STANDARD_DELAY);
        }

        // Place torches every 4 blocks
        if (branchProgress > 0 && branchProgress % 4 == 0) {
            BlockPos torchPos = headPos.above();
            if (world.getBlockState(torchPos).isAir() && !world.getBlockState(torchPos.below()).isAir()) {
                // Place torch on the ceiling or wall
                world.setBlock(headPos.relative(branchDirection.getOpposite()), Blocks.TORCH.defaultBlockState(), Block.UPDATE_ALL);
            }
        }

        branchProgress++;

        // Check inventory
        if (isInventoryFull() || job.getActionsDone() >= MAX_BLOCKS_MINED) {
            return AIWorkerState.INVENTORY_FULL;
        }

        return AIWorkerState.MINER_MINING_NODE;
    }

    // ==================== Helpers ====================

    /**
     * Check if a block state is a liquid.
     */
    private boolean isLiquid(BlockState state) {
        return !state.getFluidState().isEmpty();
    }

    /**
     * Cycle through horizontal directions: N -> E -> S -> W -> N.
     */
    private Direction nextDirection(Direction current) {
        return switch (current) {
            case NORTH -> Direction.EAST;
            case EAST -> Direction.SOUTH;
            case SOUTH -> Direction.WEST;
            case WEST -> Direction.NORTH;
            default -> Direction.NORTH;
        };
    }
}

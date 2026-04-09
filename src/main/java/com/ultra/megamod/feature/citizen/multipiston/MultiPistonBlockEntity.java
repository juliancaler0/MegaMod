package com.ultra.megamod.feature.citizen.multipiston;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Block entity for the Multi-Piston.
 * Stores input/output direction, range (1-10), speed (1-3).
 * On redstone signal: pushes blocks in the output direction.
 * On signal off: pulls blocks from the input direction.
 */
public class MultiPistonBlockEntity extends BlockEntity {

    private Direction inputDirection = Direction.SOUTH;
    private Direction outputDirection = Direction.NORTH;
    private int range = 1;    // 1-10
    private int speed = 1;    // 1-3 (ticks between each block moved)

    private int progress = 0;
    private boolean pushing = false;
    private boolean pulling = false;
    private int currentStep = 0;

    public MultiPistonBlockEntity(BlockPos pos, BlockState state) {
        super(MultiPistonRegistry.MULTI_PISTON_BE.get(), pos, state);
    }

    // === Configuration ===

    public Direction getInputDirection() { return inputDirection; }
    public Direction getOutputDirection() { return outputDirection; }
    public int getRange() { return range; }
    public int getSpeed() { return speed; }

    public void setInputDirection(Direction dir) { this.inputDirection = dir; setChanged(); }
    public void setOutputDirection(Direction dir) { this.outputDirection = dir; setChanged(); }
    public void setRange(int range) { this.range = Math.max(1, Math.min(10, range)); setChanged(); }
    public void setSpeed(int speed) { this.speed = Math.max(1, Math.min(3, speed)); setChanged(); }

    /**
     * Configure all settings at once (from network payload).
     */
    public void configure(int inputDir, int outputDir, int range, int speed) {
        this.inputDirection = dirFromIndex(inputDir);
        this.outputDirection = dirFromIndex(outputDir);
        this.range = Math.max(1, Math.min(10, range));
        this.speed = Math.max(1, Math.min(3, speed));
        setChanged();
    }

    // === Push / Pull ===

    public void startPush() {
        if (pushing || pulling) return;
        pushing = true;
        pulling = false;
        progress = 0;
        currentStep = 0;
        setChanged();
    }

    public void startPull() {
        if (pushing || pulling) return;
        pulling = true;
        pushing = false;
        progress = 0;
        currentStep = 0;
        setChanged();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MultiPistonBlockEntity be) {
        if (!be.pushing && !be.pulling) return;

        int ticksPerBlock = Math.max(1, 4 - be.speed); // speed 1=3 ticks, 2=2 ticks, 3=1 tick
        be.progress++;

        if (be.progress >= ticksPerBlock) {
            be.progress = 0;

            if (be.pushing) {
                be.doPushStep(level, pos);
            } else if (be.pulling) {
                be.doPullStep(level, pos);
            }

            be.currentStep++;
            if (be.currentStep >= be.range) {
                be.pushing = false;
                be.pulling = false;
                be.currentStep = 0;
                be.setChanged();
            }
        }
    }

    private void doPushStep(Level level, BlockPos pistonPos) {
        // Push blocks in output direction: start from the farthest and move outward
        Direction out = outputDirection;
        BlockPos start = pistonPos.relative(out);

        // Find the farthest non-air block in range
        int blocksToMove = 0;
        for (int i = 0; i < range; i++) {
            BlockPos check = start.relative(out, i);
            BlockState bs = level.getBlockState(check);
            if (!bs.isAir() && bs.getDestroySpeed(level, check) >= 0 && !bs.is(Blocks.OBSIDIAN) && !bs.is(Blocks.BEDROCK)) {
                blocksToMove = i + 1;
            }
        }

        if (blocksToMove == 0) {
            pushing = false;
            return;
        }

        // Move blocks outward, starting from the farthest
        for (int i = blocksToMove - 1; i >= 0; i--) {
            BlockPos from = start.relative(out, i);
            BlockPos to = from.relative(out);
            BlockState fromState = level.getBlockState(from);
            BlockState toState = level.getBlockState(to);

            if (!fromState.isAir() && toState.isAir()) {
                // Push entities at the target position out of the way
                pushEntitiesAt(level, to, out);

                // Save block entity data from old position if present
                CompoundTag savedBeData = saveBlockEntityData(level, from);

                // Move the block
                level.setBlock(to, fromState, 3);
                level.setBlock(from, Blocks.AIR.defaultBlockState(), 3);

                // Restore block entity data at new position
                restoreBlockEntityData(level, to, savedBeData);
            }
        }

        // Play push sound
        level.playSound(null, pistonPos, SoundEvents.PISTON_EXTEND, SoundSource.BLOCKS, 0.5f, 1.0f);
    }

    private void doPullStep(Level level, BlockPos pistonPos) {
        // Pull blocks from input direction toward the piston
        Direction in = inputDirection;
        BlockPos start = pistonPos.relative(in);

        // Move blocks inward, starting from the nearest
        for (int i = 0; i < range; i++) {
            BlockPos from = start.relative(in, i + 1);
            BlockPos to = start.relative(in, i);
            BlockState fromState = level.getBlockState(from);
            BlockState toState = level.getBlockState(to);

            if (!fromState.isAir() && toState.isAir()
                && fromState.getDestroySpeed(level, from) >= 0
                && !fromState.is(Blocks.OBSIDIAN) && !fromState.is(Blocks.BEDROCK)) {
                // Push entities at the target position out of the way
                pushEntitiesAt(level, to, in.getOpposite());

                // Save block entity data from old position if present
                CompoundTag savedBeData = saveBlockEntityData(level, from);

                // Move the block
                level.setBlock(to, fromState, 3);
                level.setBlock(from, Blocks.AIR.defaultBlockState(), 3);

                // Restore block entity data at new position
                restoreBlockEntityData(level, to, savedBeData);
            }
        }

        // Play pull sound
        level.playSound(null, pistonPos, SoundEvents.PISTON_CONTRACT, SoundSource.BLOCKS, 0.5f, 1.0f);
    }

    // === Block Entity NBT Preservation ===

    /**
     * Saves the block entity NBT data at the given position, if one exists.
     * Returns null if no block entity is present.
     */
    private static CompoundTag saveBlockEntityData(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be != null && !be.isRemoved()) {
            return be.saveWithFullMetadata(level.registryAccess());
        }
        return null;
    }

    /**
     * Restores saved block entity NBT data at the new position.
     * Uses BlockEntity.loadStatic to properly recreate the block entity from NBT.
     */
    private static void restoreBlockEntityData(Level level, BlockPos pos, CompoundTag savedData) {
        if (savedData == null) return;
        BlockState state = level.getBlockState(pos);

        // Adjust position tags to the new location
        CompoundTag adjustedTag = savedData.copy();
        adjustedTag.putInt("x", pos.getX());
        adjustedTag.putInt("y", pos.getY());
        adjustedTag.putInt("z", pos.getZ());

        BlockEntity loaded = BlockEntity.loadStatic(pos, state, adjustedTag, level.registryAccess());
        if (loaded != null) {
            level.getChunkAt(pos).setBlockEntity(loaded);
            loaded.setChanged();
        }
    }

    // === Entity Pushing ===

    /**
     * Pushes all entities at the target position 1 block in the given direction.
     * This prevents entities from being trapped inside moved blocks.
     */
    private static void pushEntitiesAt(Level level, BlockPos targetPos, Direction pushDirection) {
        AABB box = new AABB(targetPos);
        List<Entity> entities = level.getEntities((Entity) null, box, e -> true);
        if (entities.isEmpty()) return;

        Vec3 pushVec = Vec3.atLowerCornerOf(pushDirection.getUnitVec3i());
        for (Entity entity : entities) {
            entity.teleportTo(
                entity.getX() + pushVec.x,
                entity.getY() + pushVec.y,
                entity.getZ() + pushVec.z
            );
        }
    }

    // === Direction helper ===

    public static Direction dirFromIndex(int index) {
        return switch (index) {
            case 0 -> Direction.DOWN;
            case 1 -> Direction.UP;
            case 2 -> Direction.NORTH;
            case 3 -> Direction.SOUTH;
            case 4 -> Direction.WEST;
            case 5 -> Direction.EAST;
            default -> Direction.NORTH;
        };
    }

    public static int indexFromDir(Direction dir) {
        return dir.get3DDataValue();
    }

    // === NBT ===

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("InputDir", indexFromDir(inputDirection));
        output.putInt("OutputDir", indexFromDir(outputDirection));
        output.putInt("Range", range);
        output.putInt("Speed", speed);
        output.putInt("Progress", progress);
        output.putBoolean("Pushing", pushing);
        output.putBoolean("Pulling", pulling);
        output.putInt("CurrentStep", currentStep);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        inputDirection = dirFromIndex(input.getIntOr("InputDir", 3));
        outputDirection = dirFromIndex(input.getIntOr("OutputDir", 2));
        range = input.getIntOr("Range", 1);
        speed = input.getIntOr("Speed", 1);
        progress = input.getIntOr("Progress", 0);
        pushing = input.getBooleanOr("Pushing", false);
        pulling = input.getBooleanOr("Pulling", false);
        currentStep = input.getIntOr("CurrentStep", 0);
    }
}

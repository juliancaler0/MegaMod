package com.ultra.megamod.feature.citizen.blueprint.placement;

import com.ultra.megamod.feature.citizen.blueprint.BlockInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Base implementation of {@link IStructureHandler} that stores the common
 * state needed for blueprint placement: world reference, position, and
 * blueprint block data access. Subclasses must implement inventory-related
 * methods since inventory sources vary (citizen inventory, creative, chest network, etc.).
 */
public abstract class AbstractStructureHandler implements IStructureHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractStructureHandler.class);

    /** Default number of blocks placed per execution step. */
    public static final int DEFAULT_STEPS_PER_CALL = 10;

    private final Level world;
    private final BlockPos worldPos;
    private final BlockPos blueprintSize;
    private final boolean creative;
    private int stepsPerCall;

    /**
     * Block states array indexed by [y][z][x] matching blueprint layout.
     * Null entries represent out-of-bounds or unset positions.
     */
    private final BlockState[][][] blockStates;

    /**
     * Tile entity data array, same indexing as blockStates.
     * Most entries will be null (only blocks with tile entities have data).
     */
    private final net.minecraft.nbt.CompoundTag[][][] tileEntityData;

    /**
     * Construct a structure handler with pre-loaded block data.
     *
     * @param world          the world to place in.
     * @param worldPos       the anchor position in the world.
     * @param blueprintSize  the blueprint dimensions (sizeX, sizeY, sizeZ).
     * @param blockStates    3D array of block states [y][z][x].
     * @param tileEntityData 3D array of tile entity NBT [y][z][x] (entries may be null).
     * @param creative       true for creative/instant placement.
     */
    protected AbstractStructureHandler(
        Level world,
        BlockPos worldPos,
        BlockPos blueprintSize,
        BlockState[][][] blockStates,
        net.minecraft.nbt.CompoundTag[][][] tileEntityData,
        boolean creative
    ) {
        this.world = world;
        this.worldPos = worldPos;
        this.blueprintSize = blueprintSize;
        this.blockStates = blockStates;
        this.tileEntityData = tileEntityData;
        this.creative = creative;
        this.stepsPerCall = DEFAULT_STEPS_PER_CALL;
    }

    /**
     * Simplified constructor for creative placement without block data arrays.
     * Subclasses using this must override {@link #getBlueprintBlockState(BlockPos)}
     * and {@link #getBlueprintBlockInfo(BlockPos)} to provide data from their own source.
     *
     * @param world         the world.
     * @param worldPos      the world anchor position.
     * @param blueprintSize the blueprint dimensions.
     * @param creative      true for creative mode.
     */
    protected AbstractStructureHandler(Level world, BlockPos worldPos, BlockPos blueprintSize, boolean creative) {
        this.world = world;
        this.worldPos = worldPos;
        this.blueprintSize = blueprintSize;
        this.creative = creative;
        this.stepsPerCall = DEFAULT_STEPS_PER_CALL;
        this.blockStates = null;
        this.tileEntityData = null;
    }

    @Override
    @Nullable
    public BlockState getBlueprintBlockState(BlockPos localPos) {
        if (blockStates == null || !isInBounds(localPos)) {
            return null;
        }
        return blockStates[localPos.getY()][localPos.getZ()][localPos.getX()];
    }

    @Override
    @Nullable
    public BlockInfo getBlueprintBlockInfo(BlockPos localPos) {
        BlockState state = getBlueprintBlockState(localPos);
        if (state == null) {
            return null;
        }
        net.minecraft.nbt.CompoundTag teData = null;
        if (tileEntityData != null && isInBounds(localPos)) {
            teData = tileEntityData[localPos.getY()][localPos.getZ()][localPos.getX()];
        }
        return new BlockInfo(localPos, state, teData);
    }

    @Override
    public Level getWorld() {
        return world;
    }

    @Override
    public BlockPos getWorldPos() {
        return worldPos;
    }

    @Override
    public BlockPos getBlueprintSize() {
        return blueprintSize;
    }

    @Override
    public boolean isCreative() {
        return creative;
    }

    @Override
    public int getStepsPerCall() {
        return stepsPerCall;
    }

    /**
     * Set the number of blocks placed per execution step.
     *
     * @param stepsPerCall the steps per call (must be positive).
     */
    public void setStepsPerCall(int stepsPerCall) {
        this.stepsPerCall = Math.max(1, stepsPerCall);
    }

    @Override
    public BlockPos localToWorld(BlockPos localPos) {
        return worldPos.offset(localPos);
    }

    @Override
    public BlockPos worldToLocal(BlockPos worldPos) {
        return worldPos.subtract(this.worldPos);
    }

    /**
     * Check if a local position is within the blueprint bounds.
     *
     * @param localPos the local position to check.
     * @return true if within bounds.
     */
    protected boolean isInBounds(BlockPos localPos) {
        return localPos.getX() >= 0 && localPos.getX() < blueprintSize.getX()
            && localPos.getY() >= 0 && localPos.getY() < blueprintSize.getY()
            && localPos.getZ() >= 0 && localPos.getZ() < blueprintSize.getZ();
    }

    @Override
    public void onComplete() {
        LOGGER.debug("Structure placement complete at {}", worldPos);
    }
}

package com.ultra.megamod.feature.citizen.blueprint.placement;

import com.ultra.megamod.feature.citizen.blueprint.BlockInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Interface providing context for blueprint structure placement operations.
 * Implementations connect the placement system to the actual world,
 * inventory management, and blueprint data.
 */
public interface IStructureHandler {

    /**
     * Get the block state at a local position in the blueprint.
     *
     * @param localPos the position relative to blueprint origin.
     * @return the block state, or null if out of bounds.
     */
    @Nullable
    BlockState getBlueprintBlockState(BlockPos localPos);

    /**
     * Get the full block info (state + tile entity data) at a local position.
     *
     * @param localPos the position relative to blueprint origin.
     * @return the block info, or null if out of bounds.
     */
    @Nullable
    BlockInfo getBlueprintBlockInfo(BlockPos localPos);

    /**
     * Get the world this structure is being placed in.
     *
     * @return the level.
     */
    Level getWorld();

    /**
     * Get the anchor position in the world where the blueprint is placed.
     * This is the world-space position of the blueprint's origin corner.
     *
     * @return the world anchor position.
     */
    BlockPos getWorldPos();

    /**
     * Get the blueprint dimensions.
     *
     * @return the size as a BlockPos (sizeX, sizeY, sizeZ).
     */
    BlockPos getBlueprintSize();

    /**
     * Whether this is a creative/instant placement (no resource cost).
     *
     * @return true if creative mode placement.
     */
    boolean isCreative();

    /**
     * How many blocks should be placed per execution step/tick.
     *
     * @return the number of steps per call.
     */
    int getStepsPerCall();

    /**
     * Check if the handler's inventory contains the required items.
     *
     * @param items the list of required item stacks.
     * @return true if all items are available.
     */
    boolean hasRequiredItems(List<ItemStack> items);

    /**
     * Consume the specified items from the handler's inventory.
     *
     * @param items the items to consume.
     */
    void consumeItems(List<ItemStack> items);

    /**
     * Called when the entire structure placement is complete.
     */
    void onComplete();

    /**
     * Convert a local blueprint position to a world position.
     *
     * @param localPos the position in blueprint-local coordinates.
     * @return the corresponding world position.
     */
    default BlockPos localToWorld(BlockPos localPos) {
        return getWorldPos().offset(localPos);
    }

    /**
     * Convert a world position to a local blueprint position.
     *
     * @param worldPos the world position.
     * @return the corresponding blueprint-local position.
     */
    default BlockPos worldToLocal(BlockPos worldPos) {
        return worldPos.subtract(getWorldPos());
    }
}

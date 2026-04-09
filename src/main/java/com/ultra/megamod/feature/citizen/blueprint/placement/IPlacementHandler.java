package com.ultra.megamod.feature.citizen.blueprint.placement;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Interface for handlers that know how to place specific types of blocks
 * during blueprint construction. Each handler specializes in certain block
 * types (doors, beds, containers, etc.) and provides the placement logic
 * and resource requirements for those blocks.
 */
public interface IPlacementHandler {

    /**
     * Check whether this handler can handle placement of the given block state.
     *
     * @param world      the world.
     * @param pos        the target position.
     * @param blockState the block state to be placed.
     * @return true if this handler can process this block type.
     */
    boolean canHandle(Level world, BlockPos pos, BlockState blockState);

    /**
     * Execute the placement of a block in the world.
     *
     * @param world          the world to place in.
     * @param pos            the target world position.
     * @param blockState     the desired block state.
     * @param tileEntityData optional tile entity NBT data to apply after placement.
     * @param complete       true if this is a creative/instant placement (no resource cost).
     * @return SUCCESS if placed, DENY if placement failed, PASS if skipped but not an error.
     */
    ActionProcessingResult handle(
        Level world,
        BlockPos pos,
        BlockState blockState,
        @Nullable CompoundTag tileEntityData,
        boolean complete);

    /**
     * Calculate the items required to place this block.
     *
     * @param world          the world.
     * @param pos            the target position.
     * @param blockState     the block state to be placed.
     * @param tileEntityData optional tile entity NBT data (may affect required items for containers).
     * @param complete       true if creative/instant mode (may return empty list).
     * @return the list of required item stacks.
     */
    List<ItemStack> getRequiredItems(
        Level world,
        BlockPos pos,
        BlockState blockState,
        @Nullable CompoundTag tileEntityData,
        boolean complete);

    /**
     * Possible results of a placement handler action.
     */
    enum ActionProcessingResult {
        /** Placement succeeded. */
        SUCCESS,
        /** Placement was denied / failed. */
        DENY,
        /** Handler chose to skip this block (not an error). */
        PASS
    }
}

package com.ultra.megamod.feature.citizen.blueprint.placement;

import net.minecraft.core.BlockPos;

/**
 * Interface for iterating through positions in a blueprint structure.
 * Implementations define the order in which blocks are visited during
 * placement or removal operations.
 */
public interface IBlueprintIterator {

    /**
     * Advance to the next position in the iteration order.
     *
     * @return NEW_BLOCK if a valid next position was found,
     *         AT_END if the iteration reached the boundary and was reset,
     *         FINISHED if the full iteration is complete with no more blocks.
     */
    Result increment();

    /**
     * Move to the previous position in the iteration order.
     *
     * @return NEW_BLOCK if a valid previous position was found,
     *         AT_END if the iteration reached the start boundary,
     *         FINISHED if the full iteration is complete.
     */
    Result decrement();

    /**
     * Get the current progress position in local blueprint coordinates.
     *
     * @return the current local position.
     */
    BlockPos getProgressPos();

    /**
     * Set the progress position, typically used to resume from a saved state.
     *
     * @param pos the local position to resume from.
     */
    void setProgressPos(BlockPos pos);

    /**
     * Whether the iterator is currently in removal mode (tearing down blocks).
     *
     * @return true if removing blocks rather than placing.
     */
    boolean isRemoving();

    /**
     * Set whether the iterator should operate in removal mode.
     *
     * @param removing true to enable removal mode.
     */
    void setRemoving(boolean removing);

    /**
     * Whether this iterator should also process entities at each position.
     *
     * @return true if entities should be included in iteration.
     */
    boolean includeEntities();

    /**
     * Set whether entities should be included during iteration.
     *
     * @param include true to include entities.
     */
    void setIncludeEntities(boolean include);

    /**
     * Reset the iterator to its initial unstarted state.
     */
    default void reset() {
        setProgressPos(new BlockPos(-1, -1, -1));
        setRemoving(false);
        setIncludeEntities(false);
    }

    /**
     * Get the size of the blueprint being iterated over.
     *
     * @return the blueprint dimensions as a BlockPos (sizeX, sizeY, sizeZ).
     */
    BlockPos getSize();

    /**
     * Results of an iteration step.
     */
    enum Result {
        /** A new valid block position was reached. */
        NEW_BLOCK,
        /** The iterator reached the end boundary of the structure. */
        AT_END,
        /** The iteration is fully complete. */
        FINISHED
    }
}

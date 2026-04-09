package com.ultra.megamod.feature.citizen.blueprint.placement;

import com.ultra.megamod.feature.citizen.blueprint.BlockInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Core placement executor that drives the block-by-block construction of a
 * blueprint structure in the world. Coordinates between the structure handler
 * (data source + inventory), the iterator (traversal order), and placement
 * handlers (block-type-specific logic).
 *
 * <p>Designed to be called once per tick (or per work cycle), placing up to
 * {@link IStructureHandler#getStepsPerCall()} blocks each invocation.</p>
 */
public class StructurePlacer {

    private static final Logger LOGGER = LoggerFactory.getLogger(StructurePlacer.class);

    private final IStructureHandler handler;
    private final IBlueprintIterator iterator;

    /**
     * Create a structure placer.
     *
     * @param handler  the structure handler providing blueprint data and inventory access.
     * @param iterator the iterator controlling block visit order.
     */
    public StructurePlacer(IStructureHandler handler, IBlueprintIterator iterator) {
        this.handler = handler;
        this.iterator = iterator;
    }

    /**
     * Execute one step of structure placement, processing up to
     * {@link IStructureHandler#getStepsPerCall()} blocks.
     *
     * @param world           the world to place blocks in.
     * @param changeStorage   optional change storage for undo support (may be null).
     * @param includeEntities whether to include entity spawning.
     * @return the result of this placement step.
     */
    public PlacementResult executeStructureStep(Level world, @Nullable ChangeStorage changeStorage, boolean includeEntities) {
        if (world.isClientSide()) {
            LOGGER.warn("StructurePlacer.executeStructureStep called on client side -- aborting");
            return new PlacementResult(iterator.getProgressPos(), IBlueprintIterator.Result.FINISHED, Collections.emptyList());
        }

        iterator.setIncludeEntities(includeEntities);

        List<ItemStack> allMissingItems = new ArrayList<>();
        int blocksProcessed = 0;

        IBlueprintIterator.Result iterResult = iterator.increment();

        while (iterResult == IBlueprintIterator.Result.NEW_BLOCK) {
            if (blocksProcessed >= handler.getStepsPerCall()) {
                // Hit the per-call limit, save progress and return
                return new PlacementResult(iterator.getProgressPos(), IBlueprintIterator.Result.NEW_BLOCK, allMissingItems);
            }

            BlockPos localPos = iterator.getProgressPos();
            BlockPos worldPos = handler.localToWorld(localPos);

            // Skip positions outside world build height
            if (world.isOutsideBuildHeight(worldPos)) {
                iterResult = iterator.increment();
                continue;
            }

            // Get the block info from the blueprint
            BlockInfo blockInfo = handler.getBlueprintBlockInfo(localPos);
            if (blockInfo == null || blockInfo.state() == null) {
                iterResult = iterator.increment();
                continue;
            }

            BlockState targetState = blockInfo.state();
            CompoundTag tileEntityData = blockInfo.tileEntityData();

            // Record pre-change state for undo
            if (changeStorage != null) {
                changeStorage.addChange(worldPos, world.getBlockState(worldPos), getExistingTileEntityData(world, worldPos));
            }

            // Find the appropriate placement handler
            IPlacementHandler placementHandler = PlacementHandlers.getHandler(world, worldPos, targetState);

            if (!placementHandler.canHandle(world, worldPos, targetState)) {
                LOGGER.warn("Handler {} reported canHandle=false for {} at {}", placementHandler.getClass().getSimpleName(), targetState, worldPos);
                iterResult = iterator.increment();
                continue;
            }

            // Check resource requirements in non-creative mode
            if (!handler.isCreative()) {
                List<ItemStack> required = placementHandler.getRequiredItems(world, worldPos, targetState, tileEntityData, false);
                if (!required.isEmpty() && !handler.hasRequiredItems(required)) {
                    allMissingItems.addAll(required);
                    // Cannot place this block -- return and let the citizen gather resources
                    return new PlacementResult(iterator.getProgressPos(), IBlueprintIterator.Result.NEW_BLOCK, allMissingItems);
                }
            }

            // Execute the placement
            IPlacementHandler.ActionProcessingResult result = placementHandler.handle(
                world, worldPos, targetState, tileEntityData, handler.isCreative()
            );

            if (result == IPlacementHandler.ActionProcessingResult.DENY) {
                LOGGER.debug("Placement denied at {} for state {}", worldPos, targetState);
            } else if (result == IPlacementHandler.ActionProcessingResult.SUCCESS && !handler.isCreative()) {
                // Consume resources on successful placement
                List<ItemStack> required = placementHandler.getRequiredItems(world, worldPos, targetState, tileEntityData, false);
                handler.consumeItems(required);
            }

            blocksProcessed++;
            iterResult = iterator.increment();
        }

        // Iteration complete
        if (iterResult == IBlueprintIterator.Result.AT_END || iterResult == IBlueprintIterator.Result.FINISHED) {
            handler.onComplete();
            return new PlacementResult(iterator.getProgressPos(), IBlueprintIterator.Result.FINISHED, allMissingItems);
        }

        return new PlacementResult(iterator.getProgressPos(), iterResult, allMissingItems);
    }

    /**
     * Get existing tile entity data at a position for undo storage.
     */
    @Nullable
    private CompoundTag getExistingTileEntityData(Level world, BlockPos pos) {
        var blockEntity = world.getBlockEntity(pos);
        if (blockEntity != null) {
            return blockEntity.saveWithoutMetadata(world.registryAccess());
        }
        return null;
    }

    /**
     * Get the iterator instance.
     *
     * @return the blueprint iterator.
     */
    public IBlueprintIterator getIterator() {
        return iterator;
    }

    /**
     * Get the structure handler instance.
     *
     * @return the structure handler.
     */
    public IStructureHandler getHandler() {
        return handler;
    }

    /**
     * Result of a single placement execution step.
     *
     * @param iteratorPos  the iterator's current position after this step.
     * @param result       the iteration result (NEW_BLOCK = more to do, FINISHED = done).
     * @param missingItems items that were needed but not available (empty if all placed successfully).
     */
    public record PlacementResult(
        BlockPos iteratorPos,
        IBlueprintIterator.Result result,
        List<ItemStack> missingItems
    ) {
        /**
         * Whether the structure placement is fully complete.
         */
        public boolean isComplete() {
            return result == IBlueprintIterator.Result.FINISHED;
        }

        /**
         * Whether there are missing items preventing progress.
         */
        public boolean hasMissingItems() {
            return !missingItems.isEmpty();
        }
    }
}

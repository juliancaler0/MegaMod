package com.ultra.megamod.feature.citizen.blueprint.placement;

import com.ultra.megamod.feature.citizen.colonyblocks.BlockPlaceholder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Collection of placement handler implementations for different block types.
 * Handlers are ordered by priority -- more specific handlers appear first,
 * with the general fallback handler at the end.
 *
 * <p>Block update flag used throughout: {@code 3} (notify neighbors + clients).</p>
 */
public final class PlacementHandlers {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlacementHandlers.class);

    /** Block update flag: notify neighbors (1) + send to clients (2). */
    private static final int UPDATE_FLAG = Block.UPDATE_NEIGHBORS | Block.UPDATE_CLIENTS;

    /** Ordered list of placement handlers, checked from first to last. */
    private static final List<IPlacementHandler> handlers = new ArrayList<>();

    /** Cache mapping block -> handler to avoid repeated linear scans. */
    private static final Map<Block, IPlacementHandler> handlerCache = new IdentityHashMap<>(128);

    static {
        handlers.add(new AirPlacementHandler());
        handlers.add(new SubstitutionPlacementHandler());
        handlers.add(new DoorPlacementHandler());
        handlers.add(new BedPlacementHandler());
        handlers.add(new DoublePlantPlacementHandler());
        handlers.add(new ContainerPlacementHandler());
        handlers.add(new GeneralBlockPlacementHandler());
    }

    private PlacementHandlers() {
        // Utility class
    }

    /**
     * Find the appropriate placement handler for a given block state.
     *
     * @param world    the world.
     * @param worldPos the world position.
     * @param state    the block state being placed.
     * @return the matching handler (never null -- GeneralBlockPlacementHandler is the fallback).
     */
    public static IPlacementHandler getHandler(Level world, BlockPos worldPos, BlockState state) {
        Block block = state.getBlock();

        IPlacementHandler cached = handlerCache.get(block);
        if (cached != null) {
            return cached;
        }

        for (IPlacementHandler handler : handlers) {
            if (handler.canHandle(world, worldPos, state)) {
                handlerCache.put(block, handler);
                return handler;
            }
        }

        LOGGER.error("No PlacementHandler found for {}; this should never happen.", state);
        GeneralBlockPlacementHandler fallback = new GeneralBlockPlacementHandler();
        handlerCache.put(block, fallback);
        return fallback;
    }

    /**
     * Register an additional handler. It is inserted before the GeneralBlockPlacementHandler
     * (second-to-last priority) so it can override more specific types.
     *
     * @param handler the handler to add.
     */
    public static void addHandler(IPlacementHandler handler) {
        synchronized (handlers) {
            // Insert before the last handler (GeneralBlockPlacementHandler)
            int insertIndex = Math.max(0, handlers.size() - 1);
            handlers.add(insertIndex, handler);
            handlerCache.clear();
        }
    }

    /**
     * Apply tile entity (block entity) NBT data to a position after block placement.
     *
     * @param tileEntityData the saved NBT data.
     * @param world          the world.
     * @param pos            the block position.
     */
    public static void handleTileEntityPlacement(@Nullable CompoundTag tileEntityData, Level world, BlockPos pos) {
        if (tileEntityData == null) {
            return;
        }

        BlockState state = world.getBlockState(pos);

        // Overwrite position tags to match the actual world position
        CompoundTag adjustedTag = tileEntityData.copy();
        adjustedTag.putInt("x", pos.getX());
        adjustedTag.putInt("y", pos.getY());
        adjustedTag.putInt("z", pos.getZ());

        // Recreate the block entity from scratch using loadStatic, which properly
        // handles the ValueInput conversion internally
        BlockEntity loaded = BlockEntity.loadStatic(pos, state, adjustedTag, world.registryAccess());
        if (loaded != null) {
            world.getChunkAt(pos).setBlockEntity(loaded);
            loaded.setChanged();
        }
    }

    // ==================== Handler Implementations ====================

    /**
     * Handles air blocks by clearing the target position.
     */
    public static class AirPlacementHandler implements IPlacementHandler {

        @Override
        public boolean canHandle(Level world, BlockPos pos, BlockState blockState) {
            return blockState.isAir();
        }

        @Override
        public ActionProcessingResult handle(Level world, BlockPos pos, BlockState blockState,
                                              @Nullable CompoundTag tileEntityData, boolean complete) {
            if (!world.isEmptyBlock(pos)) {
                world.removeBlock(pos, false);
            }
            return ActionProcessingResult.PASS;
        }

        @Override
        public List<ItemStack> getRequiredItems(Level world, BlockPos pos, BlockState blockState,
                                                 @Nullable CompoundTag tileEntityData, boolean complete) {
            return Collections.emptyList();
        }
    }

    /**
     * Handles placeholder/substitution blocks in blueprints.
     * <ul>
     *   <li><b>SUBSTITUTION</b> -- keep whatever block is already at this position.</li>
     *   <li><b>SOLID_SUBSTITUTION</b> -- if existing block is non-solid, replace with cobblestone.</li>
     *   <li><b>FLUID_SUBSTITUTION</b> -- place water (overworld) or lava (nether).</li>
     *   <li>Structure voids are also handled as a basic "skip" substitution.</li>
     * </ul>
     */
    public static class SubstitutionPlacementHandler implements IPlacementHandler {

        @Override
        public boolean canHandle(Level world, BlockPos pos, BlockState blockState) {
            return blockState.is(Blocks.STRUCTURE_VOID)
                || blockState.getBlock() instanceof BlockPlaceholder;
        }

        @Override
        public ActionProcessingResult handle(Level world, BlockPos pos, BlockState blockState,
                                              @Nullable CompoundTag tileEntityData, boolean complete) {
            if (blockState.is(Blocks.STRUCTURE_VOID)) {
                // Legacy substitution: just skip
                return ActionProcessingResult.PASS;
            }

            if (blockState.getBlock() instanceof BlockPlaceholder placeholder) {
                BlockPlaceholder.PlaceholderType type = placeholder.getPlaceholderType();

                switch (type) {
                    case SUBSTITUTION:
                        // Keep whatever is already here
                        return ActionProcessingResult.PASS;

                    case SOLID_SUBSTITUTION:
                        // If existing block is non-solid, replace with cobblestone
                        BlockState existing = world.getBlockState(pos);
                        if (!existing.isSolid()) {
                            world.setBlock(pos, Blocks.COBBLESTONE.defaultBlockState(), UPDATE_FLAG);
                            return ActionProcessingResult.SUCCESS;
                        }
                        return ActionProcessingResult.PASS;

                    case FLUID_SUBSTITUTION:
                        // Place dimension-appropriate fluid
                        boolean isNether = world.dimension() == Level.NETHER;
                        BlockState fluid = isNether
                            ? Blocks.LAVA.defaultBlockState()
                            : Blocks.WATER.defaultBlockState();
                        world.setBlock(pos, fluid, UPDATE_FLAG);
                        return ActionProcessingResult.SUCCESS;
                }
            }

            return ActionProcessingResult.PASS;
        }

        @Override
        public List<ItemStack> getRequiredItems(Level world, BlockPos pos, BlockState blockState,
                                                 @Nullable CompoundTag tileEntityData, boolean complete) {
            return Collections.emptyList();
        }
    }

    /**
     * Handles door blocks, placing both halves (upper + lower) together.
     */
    public static class DoorPlacementHandler implements IPlacementHandler {

        @Override
        public boolean canHandle(Level world, BlockPos pos, BlockState blockState) {
            return blockState.getBlock() instanceof DoorBlock;
        }

        @Override
        public ActionProcessingResult handle(Level world, BlockPos pos, BlockState blockState,
                                              @Nullable CompoundTag tileEntityData, boolean complete) {
            if (blockState.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER) {
                world.setBlock(pos, blockState.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER), UPDATE_FLAG);
                world.setBlock(pos.above(), blockState.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), UPDATE_FLAG);
                return ActionProcessingResult.SUCCESS;
            }
            // Upper half is placed by the lower half handler -- skip
            return ActionProcessingResult.PASS;
        }

        @Override
        public List<ItemStack> getRequiredItems(Level world, BlockPos pos, BlockState blockState,
                                                 @Nullable CompoundTag tileEntityData, boolean complete) {
            if (complete) {
                return Collections.emptyList();
            }
            // Only require items for the lower half to avoid double-counting
            if (blockState.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER) {
                return Collections.singletonList(new ItemStack(blockState.getBlock()));
            }
            return Collections.emptyList();
        }
    }

    /**
     * Handles bed blocks, placing both halves (head + foot) together.
     */
    public static class BedPlacementHandler implements IPlacementHandler {

        @Override
        public boolean canHandle(Level world, BlockPos pos, BlockState blockState) {
            return blockState.getBlock() instanceof BedBlock;
        }

        @Override
        public ActionProcessingResult handle(Level world, BlockPos pos, BlockState blockState,
                                              @Nullable CompoundTag tileEntityData, boolean complete) {
            if (blockState.getValue(BedBlock.PART) == BedPart.HEAD) {
                Direction facing = blockState.getValue(BedBlock.FACING);
                BlockPos footPos = pos.relative(facing.getOpposite());

                world.setBlock(footPos, blockState.setValue(BedBlock.PART, BedPart.FOOT), UPDATE_FLAG);
                world.setBlock(pos, blockState.setValue(BedBlock.PART, BedPart.HEAD), UPDATE_FLAG);

                if (tileEntityData != null) {
                    handleTileEntityPlacement(tileEntityData, world, pos);
                    handleTileEntityPlacement(tileEntityData, world, footPos);
                }
                return ActionProcessingResult.SUCCESS;
            }
            // Foot part is placed by the head handler -- skip
            return ActionProcessingResult.PASS;
        }

        @Override
        public List<ItemStack> getRequiredItems(Level world, BlockPos pos, BlockState blockState,
                                                 @Nullable CompoundTag tileEntityData, boolean complete) {
            if (complete) {
                return Collections.emptyList();
            }
            // Only require items for the head part to avoid double-counting
            if (blockState.getValue(BedBlock.PART) == BedPart.HEAD) {
                return Collections.singletonList(new ItemStack(blockState.getBlock()));
            }
            return Collections.emptyList();
        }
    }

    /**
     * Handles double-height plants (tall grass, sunflowers, etc.), placing both halves.
     */
    public static class DoublePlantPlacementHandler implements IPlacementHandler {

        @Override
        public boolean canHandle(Level world, BlockPos pos, BlockState blockState) {
            return blockState.getBlock() instanceof DoublePlantBlock;
        }

        @Override
        public ActionProcessingResult handle(Level world, BlockPos pos, BlockState blockState,
                                              @Nullable CompoundTag tileEntityData, boolean complete) {
            if (blockState.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.LOWER) {
                world.setBlock(pos, blockState.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER), UPDATE_FLAG);
                world.setBlock(pos.above(), blockState.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER), UPDATE_FLAG);
                return ActionProcessingResult.SUCCESS;
            }
            // Upper half is placed by the lower half handler
            return ActionProcessingResult.PASS;
        }

        @Override
        public List<ItemStack> getRequiredItems(Level world, BlockPos pos, BlockState blockState,
                                                 @Nullable CompoundTag tileEntityData, boolean complete) {
            if (complete) {
                return Collections.emptyList();
            }
            if (blockState.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.LOWER) {
                return Collections.singletonList(new ItemStack(blockState.getBlock()));
            }
            return Collections.emptyList();
        }
    }

    /**
     * Handles blocks with tile entities (chests, furnaces, brewing stands, etc.).
     * Places the block and restores the tile entity data including inventory contents.
     */
    public static class ContainerPlacementHandler implements IPlacementHandler {

        @Override
        public boolean canHandle(Level world, BlockPos pos, BlockState blockState) {
            return blockState.getBlock() instanceof BaseEntityBlock;
        }

        @Override
        public ActionProcessingResult handle(Level world, BlockPos pos, BlockState blockState,
                                              @Nullable CompoundTag tileEntityData, boolean complete) {
            if (!world.setBlock(pos, blockState, UPDATE_FLAG)) {
                return ActionProcessingResult.DENY;
            }

            if (tileEntityData != null) {
                handleTileEntityPlacement(tileEntityData, world, pos);
            }

            return ActionProcessingResult.SUCCESS;
        }

        @Override
        public List<ItemStack> getRequiredItems(Level world, BlockPos pos, BlockState blockState,
                                                 @Nullable CompoundTag tileEntityData, boolean complete) {
            if (complete) {
                return Collections.emptyList();
            }
            List<ItemStack> items = new ArrayList<>();
            items.add(new ItemStack(blockState.getBlock()));
            // Note: container contents are not counted as required building materials.
            // Citizens will fill containers separately after construction.
            return items;
        }
    }

    /**
     * Default/fallback handler that can place any block type.
     * This is always the last handler checked.
     */
    public static class GeneralBlockPlacementHandler implements IPlacementHandler {

        @Override
        public boolean canHandle(Level world, BlockPos pos, BlockState blockState) {
            return true;
        }

        @Override
        public ActionProcessingResult handle(Level world, BlockPos pos, BlockState blockState,
                                              @Nullable CompoundTag tileEntityData, boolean complete) {
            if (world.getBlockState(pos).equals(blockState)) {
                // Block already matches -- still apply tile entity data if present
                if (tileEntityData != null) {
                    handleTileEntityPlacement(tileEntityData, world, pos);
                }
                return ActionProcessingResult.PASS;
            }

            if (!world.setBlock(pos, blockState, UPDATE_FLAG)) {
                return ActionProcessingResult.DENY;
            }

            if (tileEntityData != null) {
                handleTileEntityPlacement(tileEntityData, world, pos);
            }

            return ActionProcessingResult.SUCCESS;
        }

        @Override
        public List<ItemStack> getRequiredItems(Level world, BlockPos pos, BlockState blockState,
                                                 @Nullable CompoundTag tileEntityData, boolean complete) {
            if (complete) {
                return Collections.emptyList();
            }
            List<ItemStack> items = new ArrayList<>();
            items.add(new ItemStack(blockState.getBlock()));
            return items;
        }
    }
}

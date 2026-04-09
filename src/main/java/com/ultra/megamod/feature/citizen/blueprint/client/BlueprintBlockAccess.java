package com.ultra.megamod.feature.citizen.blueprint.client;

import com.ultra.megamod.feature.citizen.blueprint.BlockInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple block access wrapper for blueprint rendering.
 * Returns block states from a blueprint at given positions.
 * Used by BlueprintRenderer to query block states during ghost rendering.
 */
public class BlueprintBlockAccess implements BlockGetter {

    private final Map<BlockPos, BlockState> blockStates = new HashMap<>();

    /**
     * Populates this block access from a list of BlockInfo entries.
     *
     * @param blocks the blueprint block data
     * @param offset world offset to apply to each position
     */
    public void loadFromBlueprint(List<BlockInfo> blocks, BlockPos offset) {
        blockStates.clear();
        for (BlockInfo info : blocks) {
            if (info.state() != null && !info.state().isAir()) {
                blockStates.put(info.pos().offset(offset), info.state());
            }
        }
    }

    /**
     * Sets a block state at the given position.
     */
    public void setBlockState(BlockPos pos, BlockState state) {
        if (state != null && !state.isAir()) {
            blockStates.put(pos.immutable(), state);
        } else {
            blockStates.remove(pos);
        }
    }

    /**
     * Clears all stored block states.
     */
    public void clear() {
        blockStates.clear();
    }

    /**
     * Returns the number of stored block positions.
     */
    public int size() {
        return blockStates.size();
    }

    /**
     * Returns all stored block positions and their states.
     */
    public Map<BlockPos, BlockState> getAllStates() {
        return blockStates;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        BlockState state = blockStates.get(pos);
        return state != null ? state : Blocks.AIR.defaultBlockState();
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        BlockState state = blockStates.get(pos);
        if (state != null) {
            return state.getFluidState();
        }
        return Fluids.EMPTY.defaultFluidState();
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        // Blueprint rendering does not support block entities
        return null;
    }

    @Override
    public int getHeight() {
        return 384; // Standard overworld height
    }

    @Override
    public int getMinY() {
        return -64; // Standard overworld min Y
    }

    /**
     * Returns a dummy max light value since blueprints don't have real lighting.
     */
    public int getLightLevel(BlockPos pos) {
        return 15;
    }
}

package com.ultra.megamod.feature.citizen.blueprint;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Holds a block's position, state, and optional tile entity data within a blueprint.
 * Positions are local to the blueprint (0,0,0 is the blueprint origin corner).
 */
public record BlockInfo(BlockPos pos, @Nullable BlockState state, @Nullable CompoundTag tileEntityData) {

    /**
     * Returns true if this block info has associated tile entity (block entity) NBT data.
     */
    public boolean hasTileEntityData() {
        return tileEntityData != null;
    }

    /**
     * Creates a copy of this BlockInfo with the position replaced.
     */
    public BlockInfo withPos(BlockPos newPos) {
        return new BlockInfo(newPos, state, tileEntityData);
    }

    /**
     * Creates a copy of this BlockInfo with the state replaced.
     */
    public BlockInfo withState(BlockState newState) {
        return new BlockInfo(pos, newState, tileEntityData);
    }

    /**
     * Creates a copy of this BlockInfo with the tile entity data replaced.
     */
    public BlockInfo withTileEntityData(@Nullable CompoundTag newData) {
        return new BlockInfo(pos, state, newData);
    }
}

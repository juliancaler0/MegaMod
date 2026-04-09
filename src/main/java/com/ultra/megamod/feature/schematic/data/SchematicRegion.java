package com.ultra.megamod.feature.schematic.data;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

/**
 * A single region within a schematic, storing blocks at relative positions.
 */
public class SchematicRegion {

    private final BlockPos relativePosition;
    private final Vec3i size;
    private final Map<BlockPos, BlockState> blocks;
    private final Map<BlockPos, CompoundTag> blockEntities;

    public SchematicRegion(BlockPos relativePosition, Vec3i size,
                           Map<BlockPos, BlockState> blocks,
                           Map<BlockPos, CompoundTag> blockEntities) {
        this.relativePosition = relativePosition;
        this.size = size;
        this.blocks = blocks;
        this.blockEntities = blockEntities;
    }

    public BlockPos getRelativePosition() { return relativePosition; }
    public Vec3i getSize() { return size; }
    public Map<BlockPos, BlockState> getBlocks() { return blocks; }
    public Map<BlockPos, CompoundTag> getBlockEntities() { return blockEntities; }

    public int getBlockCount() {
        return blocks.size();
    }
}

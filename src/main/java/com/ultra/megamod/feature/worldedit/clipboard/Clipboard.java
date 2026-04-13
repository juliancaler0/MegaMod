package com.ultra.megamod.feature.worldedit.clipboard;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

/**
 * WorldEdit clipboard: stores blocks as a relative map (anchor at 0,0,0)
 * plus the copy-offset recording where the player was relative to the
 * minimum corner at copy time — used by paste to preserve position.
 */
public class Clipboard {

    private final Vec3i size;
    private final Map<BlockPos, BlockState> blocks;
    private final Map<BlockPos, CompoundTag> blockEntities;
    private BlockPos origin;    // offset from player at copy time
    private String name;

    public Clipboard(Vec3i size, Map<BlockPos, BlockState> blocks, Map<BlockPos, CompoundTag> blockEntities, BlockPos origin, String name) {
        this.size = size;
        this.blocks = blocks;
        this.blockEntities = blockEntities;
        this.origin = origin;
        this.name = name;
    }

    public Vec3i getSize() { return size; }
    public Map<BlockPos, BlockState> getBlocks() { return blocks; }
    public Map<BlockPos, CompoundTag> getBlockEntities() { return blockEntities; }
    public BlockPos getOrigin() { return origin; }
    public void setOrigin(BlockPos o) { this.origin = o; }
    public String getName() { return name; }
    public void setName(String s) { this.name = s; }

    public int getVolume() { return blocks.size(); }

    public static Clipboard empty() {
        return new Clipboard(Vec3i.ZERO, new HashMap<>(), new HashMap<>(), BlockPos.ZERO, "empty");
    }
}

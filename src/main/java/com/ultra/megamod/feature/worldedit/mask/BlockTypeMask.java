package com.ultra.megamod.feature.worldedit.mask;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.Collection;
import java.util.List;

/** Matches if the block at pos is of one of the given Block types (ignoring state). */
public record BlockTypeMask(Collection<Block> blocks) implements Mask {
    public BlockTypeMask(Block... b) { this(List.of(b)); }

    @Override
    public boolean test(Level level, BlockPos pos) {
        Block here = level.getBlockState(pos).getBlock();
        for (Block b : blocks) {
            if (here == b) return true;
        }
        return false;
    }
}

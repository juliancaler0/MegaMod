package com.ultra.megamod.feature.worldedit.mask;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collection;
import java.util.List;

/** Matches if the block state at pos equals any of the given states exactly. */
public record BlockMask(Collection<BlockState> states) implements Mask {
    public BlockMask(BlockState... s) { this(List.of(s)); }

    @Override
    public boolean test(Level level, BlockPos pos) {
        BlockState here = level.getBlockState(pos);
        for (BlockState s : states) {
            if (here == s) return true;
        }
        return false;
    }
}

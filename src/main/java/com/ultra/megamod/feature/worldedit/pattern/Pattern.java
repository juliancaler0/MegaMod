package com.ultra.megamod.feature.worldedit.pattern;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** A block-state factory that returns a state at a world position. */
public interface Pattern {
    BlockState apply(BlockPos pos);
}

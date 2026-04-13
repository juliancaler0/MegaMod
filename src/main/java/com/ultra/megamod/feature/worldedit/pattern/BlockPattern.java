package com.ultra.megamod.feature.worldedit.pattern;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** Pattern that always returns a single fixed BlockState. */
public record BlockPattern(BlockState state) implements Pattern {
    @Override
    public BlockState apply(BlockPos pos) { return state; }
}

package com.ultra.megamod.feature.worldedit.mask;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/** A predicate over a position in a world. */
public interface Mask {
    boolean test(Level level, BlockPos pos);
}

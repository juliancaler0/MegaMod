package com.ultra.megamod.feature.worldedit.mask;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/** Matches blocks that have a solid bounding. */
public class SolidBlockMask implements Mask {
    @Override
    public boolean test(Level level, BlockPos pos) {
        var state = level.getBlockState(pos);
        return state.isSolid();
    }
}

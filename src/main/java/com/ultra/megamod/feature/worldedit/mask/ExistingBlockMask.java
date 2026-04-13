package com.ultra.megamod.feature.worldedit.mask;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/** Matches any non-air block. */
public class ExistingBlockMask implements Mask {
    @Override
    public boolean test(Level level, BlockPos pos) {
        return !level.getBlockState(pos).isAir();
    }
}

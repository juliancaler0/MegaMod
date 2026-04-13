package com.ultra.megamod.feature.worldedit.mask;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.List;

public record UnionMask(List<Mask> masks) implements Mask {
    @Override
    public boolean test(Level level, BlockPos pos) {
        for (Mask m : masks) if (m.test(level, pos)) return true;
        return false;
    }
}

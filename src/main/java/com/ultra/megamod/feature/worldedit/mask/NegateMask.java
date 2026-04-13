package com.ultra.megamod.feature.worldedit.mask;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public record NegateMask(Mask inner) implements Mask {
    @Override
    public boolean test(Level level, BlockPos pos) {
        return !inner.test(level, pos);
    }
}

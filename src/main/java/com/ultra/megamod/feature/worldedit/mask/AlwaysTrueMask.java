package com.ultra.megamod.feature.worldedit.mask;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class AlwaysTrueMask implements Mask {
    public static final AlwaysTrueMask INSTANCE = new AlwaysTrueMask();
    @Override public boolean test(Level level, BlockPos pos) { return true; }
}

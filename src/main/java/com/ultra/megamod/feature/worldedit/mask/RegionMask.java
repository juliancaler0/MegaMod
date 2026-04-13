package com.ultra.megamod.feature.worldedit.mask;

import com.ultra.megamod.feature.worldedit.region.Region;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public record RegionMask(Region region) implements Mask {
    @Override
    public boolean test(Level level, BlockPos pos) {
        return region.contains(pos);
    }
}

package com.ultra.megamod.feature.worldedit.region;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;

/**
 * Base region implementation. Subclasses override the primitive ops.
 */
public abstract class AbstractRegion implements Region {

    @Override
    public Vec3i getDimensions() {
        BlockPos min = getMinimumPoint();
        BlockPos max = getMaximumPoint();
        return new Vec3i(max.getX() - min.getX() + 1,
                         max.getY() - min.getY() + 1,
                         max.getZ() - min.getZ() + 1);
    }

    @Override
    public long getArea() {
        Vec3i d = getDimensions();
        return (long) d.getX() * (long) d.getZ();
    }

    @Override
    public long getVolume() {
        Vec3i d = getDimensions();
        return (long) d.getX() * (long) d.getY() * (long) d.getZ();
    }

    @Override
    public Region clone() {
        try {
            return (Region) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    /** Simple shift helper that moves min/max by a delta. */
    protected BlockPos addDelta(BlockPos pos, BlockPos[] changes, boolean negate) {
        int x = pos.getX(), y = pos.getY(), z = pos.getZ();
        for (BlockPos c : changes) {
            int mx = negate ? -c.getX() : c.getX();
            int my = negate ? -c.getY() : c.getY();
            int mz = negate ? -c.getZ() : c.getZ();
            x += mx; y += my; z += mz;
        }
        return new BlockPos(x, y, z);
    }
}

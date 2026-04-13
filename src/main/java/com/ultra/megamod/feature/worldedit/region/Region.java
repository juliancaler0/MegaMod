package com.ultra.megamod.feature.worldedit.region;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;

import java.util.Iterator;

/**
 * A selection region for WorldEdit operations.
 * Ported from WorldEdit 7.4.x com.sk89q.worldedit.regions.Region.
 */
public interface Region extends Iterable<BlockPos> {

    /** Returns the volume (number of blocks) contained in this region. */
    long getVolume();

    /** Returns the inclusive minimum corner of this region's bounding box. */
    BlockPos getMinimumPoint();

    /** Returns the inclusive maximum corner of this region's bounding box. */
    BlockPos getMaximumPoint();

    /** Returns the size along each axis (max - min + 1). */
    Vec3i getDimensions();

    /** Tests whether a position is inside this region. */
    boolean contains(BlockPos pos);

    /** Returns the width of this region (X axis). */
    default int getWidth() { return getDimensions().getX(); }

    /** Returns the height (Y axis). */
    default int getHeight() { return getDimensions().getY(); }

    /** Returns the length (Z axis). */
    default int getLength() { return getDimensions().getZ(); }

    /** Returns the floor area on the X/Z plane (width * length). */
    default long getArea() { return (long) getWidth() * getLength(); }

    /**
     * Expand this region by the given direction vectors. Each vector
     * extends the bounding box on the face aligned with its dominant axis.
     */
    void expand(BlockPos... changes);

    /** Contract this region by the given direction vectors. */
    void contract(BlockPos... changes);

    /** Shift (translate) this region by the given delta. */
    void shift(BlockPos delta);

    /** Creates a copy of this region. */
    Region clone();

    /** Iterates over every block position contained in this region. */
    @Override
    Iterator<BlockPos> iterator();
}

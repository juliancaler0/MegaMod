package com.ultra.megamod.feature.worldedit.region;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Axis-aligned cuboid region between two corners (inclusive).
 */
public class CuboidRegion extends AbstractRegion {

    private BlockPos pos1;
    private BlockPos pos2;

    public CuboidRegion(BlockPos pos1, BlockPos pos2) {
        this.pos1 = pos1;
        this.pos2 = pos2;
    }

    public BlockPos getPos1() { return pos1; }
    public BlockPos getPos2() { return pos2; }

    public void setPos1(BlockPos p) { this.pos1 = p; }
    public void setPos2(BlockPos p) { this.pos2 = p; }

    @Override
    public BlockPos getMinimumPoint() {
        return new BlockPos(
            Math.min(pos1.getX(), pos2.getX()),
            Math.min(pos1.getY(), pos2.getY()),
            Math.min(pos1.getZ(), pos2.getZ())
        );
    }

    @Override
    public BlockPos getMaximumPoint() {
        return new BlockPos(
            Math.max(pos1.getX(), pos2.getX()),
            Math.max(pos1.getY(), pos2.getY()),
            Math.max(pos1.getZ(), pos2.getZ())
        );
    }

    @Override
    public boolean contains(BlockPos p) {
        BlockPos mn = getMinimumPoint();
        BlockPos mx = getMaximumPoint();
        return p.getX() >= mn.getX() && p.getX() <= mx.getX()
            && p.getY() >= mn.getY() && p.getY() <= mx.getY()
            && p.getZ() >= mn.getZ() && p.getZ() <= mx.getZ();
    }

    @Override
    public void expand(BlockPos... changes) {
        BlockPos mn = getMinimumPoint();
        BlockPos mx = getMaximumPoint();
        int minX = mn.getX(), minY = mn.getY(), minZ = mn.getZ();
        int maxX = mx.getX(), maxY = mx.getY(), maxZ = mx.getZ();
        for (BlockPos c : changes) {
            if (c.getX() > 0) maxX += c.getX(); else if (c.getX() < 0) minX += c.getX();
            if (c.getY() > 0) maxY += c.getY(); else if (c.getY() < 0) minY += c.getY();
            if (c.getZ() > 0) maxZ += c.getZ(); else if (c.getZ() < 0) minZ += c.getZ();
        }
        pos1 = new BlockPos(minX, minY, minZ);
        pos2 = new BlockPos(maxX, maxY, maxZ);
    }

    @Override
    public void contract(BlockPos... changes) {
        BlockPos mn = getMinimumPoint();
        BlockPos mx = getMaximumPoint();
        int minX = mn.getX(), minY = mn.getY(), minZ = mn.getZ();
        int maxX = mx.getX(), maxY = mx.getY(), maxZ = mx.getZ();
        for (BlockPos c : changes) {
            if (c.getX() < 0) maxX += c.getX(); else if (c.getX() > 0) minX += c.getX();
            if (c.getY() < 0) maxY += c.getY(); else if (c.getY() > 0) minY += c.getY();
            if (c.getZ() < 0) maxZ += c.getZ(); else if (c.getZ() > 0) minZ += c.getZ();
        }
        if (minX > maxX) { int t = minX; minX = maxX; maxX = t; }
        if (minY > maxY) { int t = minY; minY = maxY; maxY = t; }
        if (minZ > maxZ) { int t = minZ; minZ = maxZ; maxZ = t; }
        pos1 = new BlockPos(minX, minY, minZ);
        pos2 = new BlockPos(maxX, maxY, maxZ);
    }

    @Override
    public void shift(BlockPos d) {
        pos1 = pos1.offset(d);
        pos2 = pos2.offset(d);
    }

    @Override
    public CuboidRegion clone() {
        return new CuboidRegion(pos1, pos2);
    }

    @Override
    public Iterator<BlockPos> iterator() {
        final BlockPos mn = getMinimumPoint();
        final BlockPos mx = getMaximumPoint();
        return new Iterator<>() {
            int x = mn.getX();
            int y = mn.getY();
            int z = mn.getZ();
            boolean done = false;

            @Override public boolean hasNext() { return !done; }
            @Override public BlockPos next() {
                if (done) throw new NoSuchElementException();
                BlockPos cur = new BlockPos(x, y, z);
                if (++x > mx.getX()) {
                    x = mn.getX();
                    if (++z > mx.getZ()) {
                        z = mn.getZ();
                        if (++y > mx.getY()) {
                            done = true;
                        }
                    }
                }
                return cur;
            }
        };
    }

    /** Build a CuboidRegion from two arbitrary corners. */
    public static CuboidRegion fromCorners(BlockPos a, BlockPos b) {
        return new CuboidRegion(a, b);
    }

    public static Vec3i sizeBetween(BlockPos a, BlockPos b) {
        return new Vec3i(
            Math.abs(a.getX() - b.getX()) + 1,
            Math.abs(a.getY() - b.getY()) + 1,
            Math.abs(a.getZ() - b.getZ()) + 1
        );
    }
}

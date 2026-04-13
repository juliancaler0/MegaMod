package com.ultra.megamod.feature.worldedit.region;

import net.minecraft.core.BlockPos;

import java.util.Iterator;
import java.util.NoSuchElementException;

/** A vertical cylinder — circular base on the XZ plane, height along Y. */
public class CylinderRegion extends AbstractRegion {

    private BlockPos center;
    private int rx;
    private int rz;
    private int minY;
    private int maxY;

    public CylinderRegion(BlockPos center, int rx, int rz, int minY, int maxY) {
        this.center = center;
        this.rx = Math.max(0, rx);
        this.rz = Math.max(0, rz);
        this.minY = Math.min(minY, maxY);
        this.maxY = Math.max(minY, maxY);
    }

    public BlockPos getCenter() { return center; }
    public int getRadiusX() { return rx; }
    public int getRadiusZ() { return rz; }
    public int getMinY() { return minY; }
    public int getMaxY() { return maxY; }

    @Override
    public BlockPos getMinimumPoint() { return new BlockPos(center.getX() - rx, minY, center.getZ() - rz); }
    @Override
    public BlockPos getMaximumPoint() { return new BlockPos(center.getX() + rx, maxY, center.getZ() + rz); }

    @Override
    public boolean contains(BlockPos p) {
        if (p.getY() < minY || p.getY() > maxY) return false;
        double dx = (double) (p.getX() - center.getX()) / Math.max(1, rx);
        double dz = (double) (p.getZ() - center.getZ()) / Math.max(1, rz);
        return dx * dx + dz * dz <= 1.0;
    }

    @Override
    public void expand(BlockPos... changes) {
        for (BlockPos c : changes) {
            rx += Math.abs(c.getX());
            rz += Math.abs(c.getZ());
            if (c.getY() > 0) maxY += c.getY();
            else if (c.getY() < 0) minY += c.getY();
        }
    }

    @Override
    public void contract(BlockPos... changes) {
        for (BlockPos c : changes) {
            rx = Math.max(0, rx - Math.abs(c.getX()));
            rz = Math.max(0, rz - Math.abs(c.getZ()));
            if (c.getY() < 0) maxY += c.getY();
            else if (c.getY() > 0) minY += c.getY();
        }
        if (minY > maxY) { int t = minY; minY = maxY; maxY = t; }
    }

    @Override
    public void shift(BlockPos d) {
        center = center.offset(d);
        minY += d.getY();
        maxY += d.getY();
    }

    @Override
    public CylinderRegion clone() { return new CylinderRegion(center, rx, rz, minY, maxY); }

    @Override
    public Iterator<BlockPos> iterator() {
        return new Iterator<>() {
            int x = -rx, z = -rz, y = minY;
            BlockPos nxt = advance();

            private BlockPos advance() {
                while (y <= maxY) {
                    while (z <= rz) {
                        int cx = x, cz = z, cy = y;
                        if (++x > rx) {
                            x = -rx;
                            if (++z > rz) {
                                z = -rz;
                                ++y;
                            }
                        }
                        double ex = (double) cx / Math.max(1, rx);
                        double ez = (double) cz / Math.max(1, rz);
                        if (ex * ex + ez * ez <= 1.0) {
                            return new BlockPos(center.getX() + cx, cy, center.getZ() + cz);
                        }
                    }
                    z = -rz;
                    ++y;
                }
                return null;
            }

            @Override public boolean hasNext() { return nxt != null; }
            @Override public BlockPos next() {
                if (nxt == null) throw new NoSuchElementException();
                BlockPos r = nxt;
                nxt = advance();
                return r;
            }
        };
    }
}

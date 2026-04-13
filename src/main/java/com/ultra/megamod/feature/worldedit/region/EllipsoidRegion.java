package com.ultra.megamod.feature.worldedit.region;

import net.minecraft.core.BlockPos;

import java.util.Iterator;
import java.util.NoSuchElementException;

/** Axis-aligned ellipsoid region. */
public class EllipsoidRegion extends AbstractRegion {

    private BlockPos center;
    private int rx;
    private int ry;
    private int rz;

    public EllipsoidRegion(BlockPos center, int rx, int ry, int rz) {
        this.center = center;
        this.rx = Math.max(0, rx);
        this.ry = Math.max(0, ry);
        this.rz = Math.max(0, rz);
    }

    public BlockPos getCenter() { return center; }
    public int getRadiusX() { return rx; }
    public int getRadiusY() { return ry; }
    public int getRadiusZ() { return rz; }

    @Override
    public BlockPos getMinimumPoint() { return center.offset(-rx, -ry, -rz); }
    @Override
    public BlockPos getMaximumPoint() { return center.offset(rx, ry, rz); }

    @Override
    public boolean contains(BlockPos p) {
        double dx = (double) (p.getX() - center.getX()) / Math.max(1, rx);
        double dy = (double) (p.getY() - center.getY()) / Math.max(1, ry);
        double dz = (double) (p.getZ() - center.getZ()) / Math.max(1, rz);
        return dx * dx + dy * dy + dz * dz <= 1.0;
    }

    @Override
    public void expand(BlockPos... changes) {
        for (BlockPos c : changes) {
            rx += Math.abs(c.getX());
            ry += Math.abs(c.getY());
            rz += Math.abs(c.getZ());
        }
    }

    @Override
    public void contract(BlockPos... changes) {
        for (BlockPos c : changes) {
            rx = Math.max(0, rx - Math.abs(c.getX()));
            ry = Math.max(0, ry - Math.abs(c.getY()));
            rz = Math.max(0, rz - Math.abs(c.getZ()));
        }
    }

    @Override
    public void shift(BlockPos d) { center = center.offset(d); }

    @Override
    public EllipsoidRegion clone() { return new EllipsoidRegion(center, rx, ry, rz); }

    @Override
    public Iterator<BlockPos> iterator() {
        return new Iterator<>() {
            int x = -rx, y = -ry, z = -rz;
            BlockPos nxt = advance();

            private BlockPos advance() {
                while (y <= ry) {
                    while (z <= rz) {
                        int cx = x, cy = y, cz = z;
                        if (++x > rx) {
                            x = -rx;
                            if (++z > rz) {
                                z = -rz;
                                ++y;
                            }
                        }
                        double ex = (double) cx / Math.max(1, rx);
                        double ey = (double) cy / Math.max(1, ry);
                        double ez = (double) cz / Math.max(1, rz);
                        if (ex * ex + ey * ey + ez * ez <= 1.0) {
                            return center.offset(cx, cy, cz);
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

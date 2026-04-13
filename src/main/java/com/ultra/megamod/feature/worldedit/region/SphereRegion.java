package com.ultra.megamod.feature.worldedit.region;

import net.minecraft.core.BlockPos;

import java.util.Iterator;
import java.util.NoSuchElementException;

/** A sphere region centered on an origin with an integer radius. */
public class SphereRegion extends AbstractRegion {

    private BlockPos center;
    private int radius;

    public SphereRegion(BlockPos center, int radius) {
        this.center = center;
        this.radius = Math.max(0, radius);
    }

    public BlockPos getCenter() { return center; }
    public int getRadius() { return radius; }
    public void setCenter(BlockPos c) { this.center = c; }
    public void setRadius(int r) { this.radius = Math.max(0, r); }

    @Override
    public BlockPos getMinimumPoint() {
        return center.offset(-radius, -radius, -radius);
    }
    @Override
    public BlockPos getMaximumPoint() {
        return center.offset(radius, radius, radius);
    }

    @Override
    public boolean contains(BlockPos p) {
        int dx = p.getX() - center.getX();
        int dy = p.getY() - center.getY();
        int dz = p.getZ() - center.getZ();
        return (long) dx * dx + (long) dy * dy + (long) dz * dz <= (long) radius * radius;
    }

    @Override
    public void expand(BlockPos... changes) {
        for (BlockPos c : changes) {
            radius += Math.max(Math.abs(c.getX()), Math.max(Math.abs(c.getY()), Math.abs(c.getZ())));
        }
    }

    @Override
    public void contract(BlockPos... changes) {
        for (BlockPos c : changes) {
            radius -= Math.max(Math.abs(c.getX()), Math.max(Math.abs(c.getY()), Math.abs(c.getZ())));
        }
        if (radius < 0) radius = 0;
    }

    @Override
    public void shift(BlockPos d) { center = center.offset(d); }

    @Override
    public SphereRegion clone() { return new SphereRegion(center, radius); }

    @Override
    public long getVolume() {
        // Exact count of lattice points in the sphere — iterate once
        long count = 0;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if ((long) x * x + (long) y * y + (long) z * z <= (long) radius * radius) count++;
                }
            }
        }
        return count;
    }

    @Override
    public Iterator<BlockPos> iterator() {
        return new Iterator<>() {
            int x = -radius, y = -radius, z = -radius;
            BlockPos nxt = advance();

            private BlockPos advance() {
                while (y <= radius) {
                    while (z <= radius) {
                        int cx = x, cy = y, cz = z;
                        if (++x > radius) {
                            x = -radius;
                            if (++z > radius) {
                                z = -radius;
                                ++y;
                            }
                        }
                        if ((long) cx * cx + (long) cy * cy + (long) cz * cz <= (long) radius * radius) {
                            return center.offset(cx, cy, cz);
                        }
                    }
                    z = -radius;
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

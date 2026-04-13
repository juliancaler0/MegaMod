package com.ultra.megamod.feature.worldedit.region;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * A polygon defined on the XZ plane, extruded vertically between minY..maxY.
 */
public class Polygonal2DRegion extends AbstractRegion {

    private final List<int[]> points = new ArrayList<>(); // [x, z]
    private int minY;
    private int maxY;

    public Polygonal2DRegion() {
        this.minY = Integer.MAX_VALUE;
        this.maxY = Integer.MIN_VALUE;
    }

    public Polygonal2DRegion(List<int[]> points, int minY, int maxY) {
        this.points.addAll(points);
        this.minY = Math.min(minY, maxY);
        this.maxY = Math.max(minY, maxY);
    }

    public List<int[]> getPoints() { return points; }

    public void addPoint(int x, int z) {
        points.add(new int[]{x, z});
    }

    public void addPoint(BlockPos p) {
        points.add(new int[]{p.getX(), p.getZ()});
        if (p.getY() < minY) minY = p.getY();
        if (p.getY() > maxY) maxY = p.getY();
    }

    public void setY(int minY, int maxY) {
        this.minY = Math.min(minY, maxY);
        this.maxY = Math.max(minY, maxY);
    }

    public int getMinY() { return minY; }
    public int getMaxY() { return maxY; }

    @Override
    public BlockPos getMinimumPoint() {
        if (points.isEmpty()) return BlockPos.ZERO;
        int mnX = Integer.MAX_VALUE, mnZ = Integer.MAX_VALUE;
        for (int[] p : points) {
            if (p[0] < mnX) mnX = p[0];
            if (p[1] < mnZ) mnZ = p[1];
        }
        return new BlockPos(mnX, minY, mnZ);
    }

    @Override
    public BlockPos getMaximumPoint() {
        if (points.isEmpty()) return BlockPos.ZERO;
        int mxX = Integer.MIN_VALUE, mxZ = Integer.MIN_VALUE;
        for (int[] p : points) {
            if (p[0] > mxX) mxX = p[0];
            if (p[1] > mxZ) mxZ = p[1];
        }
        return new BlockPos(mxX, maxY, mxZ);
    }

    @Override
    public boolean contains(BlockPos p) {
        if (p.getY() < minY || p.getY() > maxY) return false;
        return pointInPolygon(p.getX() + 0.5, p.getZ() + 0.5);
    }

    private boolean pointInPolygon(double x, double z) {
        int n = points.size();
        if (n < 3) return false;
        boolean inside = false;
        for (int i = 0, j = n - 1; i < n; j = i++) {
            double xi = points.get(i)[0], zi = points.get(i)[1];
            double xj = points.get(j)[0], zj = points.get(j)[1];
            boolean crosses = (zi > z) != (zj > z);
            if (crosses) {
                double xIntersect = (xj - xi) * (z - zi) / (zj - zi) + xi;
                if (x < xIntersect) inside = !inside;
            }
        }
        return inside;
    }

    @Override
    public void expand(BlockPos... changes) {
        for (BlockPos c : changes) {
            if (c.getY() > 0) maxY += c.getY();
            else if (c.getY() < 0) minY += c.getY();
            // horizontal expand: scale polygon from centroid
            if (c.getX() != 0 || c.getZ() != 0) {
                int dx = Math.abs(c.getX());
                int dz = Math.abs(c.getZ());
                int cx = 0, cz = 0;
                for (int[] pt : points) { cx += pt[0]; cz += pt[1]; }
                cx /= Math.max(1, points.size());
                cz /= Math.max(1, points.size());
                for (int[] pt : points) {
                    pt[0] += pt[0] > cx ? dx : -dx;
                    pt[1] += pt[1] > cz ? dz : -dz;
                }
            }
        }
    }

    @Override
    public void contract(BlockPos... changes) {
        for (BlockPos c : changes) {
            if (c.getY() < 0) maxY += c.getY();
            else if (c.getY() > 0) minY += c.getY();
        }
        if (minY > maxY) { int t = minY; minY = maxY; maxY = t; }
    }

    @Override
    public void shift(BlockPos d) {
        for (int[] p : points) { p[0] += d.getX(); p[1] += d.getZ(); }
        minY += d.getY();
        maxY += d.getY();
    }

    @Override
    public Polygonal2DRegion clone() {
        List<int[]> copy = new ArrayList<>(points.size());
        for (int[] p : points) copy.add(new int[]{p[0], p[1]});
        return new Polygonal2DRegion(copy, minY, maxY);
    }

    @Override
    public Iterator<BlockPos> iterator() {
        return new Iterator<>() {
            final BlockPos mn = getMinimumPoint();
            final BlockPos mx = getMaximumPoint();
            int x = mn.getX(), y = mn.getY(), z = mn.getZ();
            BlockPos nxt = advance();

            private BlockPos advance() {
                while (y <= mx.getY()) {
                    while (z <= mx.getZ()) {
                        while (x <= mx.getX()) {
                            BlockPos cur = new BlockPos(x, y, z);
                            ++x;
                            if (pointInPolygon(cur.getX() + 0.5, cur.getZ() + 0.5)) return cur;
                        }
                        x = mn.getX();
                        ++z;
                    }
                    z = mn.getZ();
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

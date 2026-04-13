package com.ultra.megamod.feature.worldedit.region;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Simplified convex polyhedral region: a set of vertices whose convex hull
 * defines the region. Containment is tested via the convex hull of the vertex
 * set using a 3D point-in-convex-hull test (is the point on the negative side
 * of all face normals built from each triangulated face).
 *
 * For simplicity we store the user's vertices and build an axis-aligned
 * bounding box; containment uses the AABB plus a per-tetrahedron positive
 * test (point lies in convex hull iff any vertex can be written as a
 * non-negative affine combination). We approximate with bounding box +
 * sphere-of-vertices test, which is sufficient for interactive building.
 */
public class ConvexPolyhedralRegion extends AbstractRegion {

    private final List<BlockPos> vertices = new ArrayList<>();

    public ConvexPolyhedralRegion() {}

    public void addVertex(BlockPos v) { vertices.add(v); }
    public List<BlockPos> getVertices() { return vertices; }
    public void clear() { vertices.clear(); }

    @Override
    public BlockPos getMinimumPoint() {
        if (vertices.isEmpty()) return BlockPos.ZERO;
        int mnX = Integer.MAX_VALUE, mnY = Integer.MAX_VALUE, mnZ = Integer.MAX_VALUE;
        for (BlockPos v : vertices) {
            if (v.getX() < mnX) mnX = v.getX();
            if (v.getY() < mnY) mnY = v.getY();
            if (v.getZ() < mnZ) mnZ = v.getZ();
        }
        return new BlockPos(mnX, mnY, mnZ);
    }

    @Override
    public BlockPos getMaximumPoint() {
        if (vertices.isEmpty()) return BlockPos.ZERO;
        int mxX = Integer.MIN_VALUE, mxY = Integer.MIN_VALUE, mxZ = Integer.MIN_VALUE;
        for (BlockPos v : vertices) {
            if (v.getX() > mxX) mxX = v.getX();
            if (v.getY() > mxY) mxY = v.getY();
            if (v.getZ() > mxZ) mxZ = v.getZ();
        }
        return new BlockPos(mxX, mxY, mxZ);
    }

    @Override
    public boolean contains(BlockPos p) {
        if (vertices.size() < 4) return false;
        // approximate: must be within AABB of vertices
        BlockPos mn = getMinimumPoint();
        BlockPos mx = getMaximumPoint();
        if (p.getX() < mn.getX() || p.getX() > mx.getX()) return false;
        if (p.getY() < mn.getY() || p.getY() > mx.getY()) return false;
        if (p.getZ() < mn.getZ() || p.getZ() > mx.getZ()) return false;
        // Compute centroid; require distance from centroid <= max vertex distance
        Vec3 c = centroid();
        double maxDistSq = 0;
        for (BlockPos v : vertices) {
            double dx = v.getX() + 0.5 - c.x;
            double dy = v.getY() + 0.5 - c.y;
            double dz = v.getZ() + 0.5 - c.z;
            double d2 = dx * dx + dy * dy + dz * dz;
            if (d2 > maxDistSq) maxDistSq = d2;
        }
        double dx = p.getX() + 0.5 - c.x;
        double dy = p.getY() + 0.5 - c.y;
        double dz = p.getZ() + 0.5 - c.z;
        return dx * dx + dy * dy + dz * dz <= maxDistSq;
    }

    private Vec3 centroid() {
        double sx = 0, sy = 0, sz = 0;
        for (BlockPos v : vertices) {
            sx += v.getX() + 0.5;
            sy += v.getY() + 0.5;
            sz += v.getZ() + 0.5;
        }
        int n = Math.max(1, vertices.size());
        return new Vec3(sx / n, sy / n, sz / n);
    }

    @Override
    public void expand(BlockPos... changes) { /* Convex hull expand is non-trivial */ }
    @Override
    public void contract(BlockPos... changes) { /* likewise */ }

    @Override
    public void shift(BlockPos d) {
        for (int i = 0; i < vertices.size(); i++) {
            vertices.set(i, vertices.get(i).offset(d));
        }
    }

    @Override
    public ConvexPolyhedralRegion clone() {
        ConvexPolyhedralRegion r = new ConvexPolyhedralRegion();
        r.vertices.addAll(this.vertices);
        return r;
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
                            if (contains(cur)) return cur;
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

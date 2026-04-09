package com.ultra.megamod.feature.baritone.pathfinding;

/**
 * Path favoring system — reduces cost of nodes near the start of an existing path
 * to produce smoother, more consistent paths when recalculating.
 * Adapted from Baritone's favoring system.
 */
public class Favoring {
    private final long[] favoredPositions;
    private final int count;
    private static final double FAVOR_COEFFICIENT = 0.8; // 20% cost reduction

    public Favoring(ServerPath previousPath, int maxNodes) {
        if (previousPath == null || previousPath.getPositions().isEmpty()) {
            this.favoredPositions = new long[0];
            this.count = 0;
            return;
        }
        int limit = Math.min(previousPath.getPositions().size(), maxNodes);
        this.favoredPositions = new long[limit];
        for (int i = 0; i < limit; i++) {
            BetterBlockPos p = previousPath.getPositions().get(i);
            favoredPositions[i] = posKey(p.x, p.y, p.z);
        }
        this.count = limit;
    }

    public Favoring() {
        this.favoredPositions = new long[0];
        this.count = 0;
    }

    /**
     * Apply favoring to a cost. If the position was on the previous path,
     * reduce the cost to encourage path consistency.
     */
    public double applyFavoring(int x, int y, int z, double cost) {
        if (count == 0) return cost;
        long key = posKey(x, y, z);
        for (int i = 0; i < count; i++) {
            if (favoredPositions[i] == key) {
                return cost * FAVOR_COEFFICIENT;
            }
        }
        return cost;
    }

    public boolean isEmpty() {
        return count == 0;
    }

    private static long posKey(int x, int y, int z) {
        return ((long) x & 0x3FFFFFFL) | (((long) y & 0xFFFL) << 26) | (((long) z & 0x3FFFFFFL) << 38);
    }
}

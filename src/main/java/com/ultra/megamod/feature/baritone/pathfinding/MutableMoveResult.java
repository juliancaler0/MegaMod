package com.ultra.megamod.feature.baritone.pathfinding;

/**
 * Reusable movement result object to reduce GC pressure during A* calculation.
 * Instead of creating new objects for each movement evaluation, this is reset and reused.
 */
public class MutableMoveResult {
    public int x;
    public int y;
    public int z;
    public double cost;

    public MutableMoveResult() {
        reset();
    }

    public void reset() {
        x = 0;
        y = 0;
        z = 0;
        cost = Double.MAX_VALUE;
    }

    public void set(int x, int y, int z, double cost) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.cost = cost;
    }

    public boolean isValid() {
        return cost < 1_000_000.0;
    }
}

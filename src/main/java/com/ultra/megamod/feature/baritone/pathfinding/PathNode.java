package com.ultra.megamod.feature.baritone.pathfinding;

/**
 * A* graph node for pathfinding.
 */
public class PathNode {
    public final int x, y, z;
    public double cost;       // g: actual cost from start
    public double heuristic;  // h: estimated cost to goal
    public double combinedCost; // f = g + h
    public PathNode parent;
    public int heapIndex = -1;
    public boolean isOpen;

    public PathNode(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.cost = Double.MAX_VALUE;
        this.heuristic = 0;
        this.combinedCost = Double.MAX_VALUE;
    }

    public BetterBlockPos toBlockPos() {
        return new BetterBlockPos(x, y, z);
    }

    @Override
    public int hashCode() {
        int h = x * 0x45D9F3B;
        h ^= y * 0x3C6EF35F;
        h ^= z * 0x27D4EB2D;
        return h;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof PathNode other) {
            return this.x == other.x && this.y == other.y && this.z == other.z;
        }
        return false;
    }
}

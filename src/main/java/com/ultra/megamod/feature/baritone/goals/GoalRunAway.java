package com.ultra.megamod.feature.baritone.goals;

import com.ultra.megamod.feature.baritone.movement.ActionCosts;

/**
 * Goal that tries to maximize distance from a position.
 * Heuristic returns NEGATIVE of distance (farther = better = lower cost).
 * isInGoal returns true when distance exceeds minDistance.
 */
public class GoalRunAway implements Goal {
    private final int x, y, z;
    private final int minDistance;
    private final int minDistanceSq;
    private static final double MAX_COST = 300.0 * ActionCosts.WALK_ONE_BLOCK_COST;

    public GoalRunAway(int x, int y, int z, int minDistance) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.minDistance = minDistance;
        this.minDistanceSq = minDistance * minDistance;
    }

    public GoalRunAway(int x, int y, int z) {
        this(x, y, z, 64);
    }

    @Override
    public boolean isInGoal(int x, int y, int z) {
        int dx = x - this.x;
        int dy = y - this.y;
        int dz = z - this.z;
        return dx * dx + dy * dy + dz * dz > minDistanceSq;
    }

    @Override
    public double heuristic(int x, int y, int z) {
        int dx = x - this.x;
        int dy = y - this.y;
        int dz = z - this.z;
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        // Farther = lower heuristic cost (better for A*)
        // Close positions have high heuristic, pushing pathfinding outward
        double distCost = dist * ActionCosts.WALK_ONE_BLOCK_COST;
        return Math.max(0, MAX_COST - distCost);
    }

    public int getMinDistance() {
        return minDistance;
    }

    @Override
    public String toString() {
        return "GoalRunAway{from=" + x + "," + y + "," + z + " minDist=" + minDistance + "}";
    }
}

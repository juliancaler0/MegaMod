package com.ultra.megamod.feature.baritone.goals;

import com.ultra.megamod.feature.baritone.pathfinding.BetterBlockPos;

/**
 * Goal interface for A* pathfinding.
 */
public interface Goal {
    /** Whether the given position satisfies this goal */
    boolean isInGoal(int x, int y, int z);

    /** Heuristic estimate of cost from pos to goal (must be admissible) */
    double heuristic(int x, int y, int z);

    default boolean isInGoal(BetterBlockPos pos) {
        return isInGoal(pos.x, pos.y, pos.z);
    }

    default double heuristic(BetterBlockPos pos) {
        return heuristic(pos.x, pos.y, pos.z);
    }
}

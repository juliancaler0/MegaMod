package com.ultra.megamod.feature.baritone.goals;

import com.ultra.megamod.feature.baritone.movement.ActionCosts;

/**
 * Goal: get far from a position.
 * Uses a bounded inversion: heuristic is high when close, low when far.
 * The A* will naturally explore outward because close nodes have high cost.
 * isInGoal triggers once the player is beyond a minimum distance.
 */
public class GoalInverted implements Goal {
    private final Goal original;
    private final double minDistance;
    private static final double MAX_COST = 200.0 * ActionCosts.WALK_ONE_BLOCK_COST;

    public GoalInverted(Goal original) {
        this(original, 32);
    }

    public GoalInverted(Goal original, int minDistance) {
        this.original = original;
        this.minDistance = minDistance;
    }

    @Override
    public boolean isInGoal(int x, int y, int z) {
        // Satisfied once the original heuristic is large enough (we're far away)
        return original.heuristic(x, y, z) >= minDistance * ActionCosts.WALK_ONE_BLOCK_COST;
    }

    @Override
    public double heuristic(int x, int y, int z) {
        // Invert: close to original = high heuristic, far from original = low heuristic
        // Clamp to [0, MAX_COST] to keep A* admissible
        double origH = original.heuristic(x, y, z);
        return Math.max(0, MAX_COST - origH);
    }
}

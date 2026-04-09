package com.ultra.megamod.feature.baritone.goals;

import com.ultra.megamod.feature.baritone.movement.ActionCosts;

/**
 * Goal: reach a specific Y level.
 */
public class GoalYLevel implements Goal {
    public final int y;

    public GoalYLevel(int y) {
        this.y = y;
    }

    @Override
    public boolean isInGoal(int x, int y, int z) {
        return y == this.y;
    }

    @Override
    public double heuristic(int x, int y, int z) {
        int dy = y - this.y;
        if (dy > 0) {
            return dy * ActionCosts.FALL_ONE_BLOCK_COST;
        } else {
            return -dy * (ActionCosts.WALK_ONE_BLOCK_COST + ActionCosts.JUMP_PENALTY);
        }
    }
}

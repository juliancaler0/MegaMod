package com.ultra.megamod.feature.baritone.goals;

import com.ultra.megamod.feature.baritone.movement.ActionCosts;

/**
 * Goal to reach the nearest axis line (X=0 or Z=0).
 * Heuristic is the minimum of abs(x) and abs(z).
 * isInGoal when x==0 or z==0.
 */
public class GoalAxis implements Goal {

    @Override
    public boolean isInGoal(int x, int y, int z) {
        return x == 0 || z == 0;
    }

    @Override
    public double heuristic(int x, int y, int z) {
        // Distance to the nearest axis — whichever is closer
        int distToXAxis = Math.abs(z); // Z=0 means on X axis
        int distToZAxis = Math.abs(x); // X=0 means on Z axis
        return Math.min(distToXAxis, distToZAxis) * ActionCosts.WALK_ONE_BLOCK_COST;
    }

    @Override
    public String toString() {
        return "GoalAxis{X=0 or Z=0}";
    }
}

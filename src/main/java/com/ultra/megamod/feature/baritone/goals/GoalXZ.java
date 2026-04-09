package com.ultra.megamod.feature.baritone.goals;

import com.ultra.megamod.feature.baritone.movement.ActionCosts;

/**
 * Goal: reach X/Z coordinates at any Y level.
 */
public class GoalXZ implements Goal {
    public final int x, z;

    public GoalXZ(int x, int z) {
        this.x = x;
        this.z = z;
    }

    @Override
    public boolean isInGoal(int x, int y, int z) {
        return x == this.x && z == this.z;
    }

    @Override
    public double heuristic(int x, int y, int z) {
        int dx = Math.abs(x - this.x);
        int dz = Math.abs(z - this.z);
        int diag = Math.min(dx, dz);
        int straight = dx + dz - 2 * diag;
        return diag * ActionCosts.WALK_ONE_BLOCK_COST * ActionCosts.SQRT_2
             + straight * ActionCosts.WALK_ONE_BLOCK_COST;
    }

    @Override
    public String toString() {
        return "GoalXZ{" + x + ", " + z + "}";
    }
}

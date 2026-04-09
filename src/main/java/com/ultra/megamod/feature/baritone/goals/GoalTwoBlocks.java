package com.ultra.megamod.feature.baritone.goals;

import com.ultra.megamod.feature.baritone.movement.ActionCosts;

/**
 * Goal: feet or head position is in the block (2 block tall entity).
 */
public class GoalTwoBlocks implements Goal {
    public final int x, y, z;

    public GoalTwoBlocks(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public boolean isInGoal(int gx, int gy, int gz) {
        return gx == x && gz == z && (gy == y || gy == y - 1);
    }

    @Override
    public double heuristic(int gx, int gy, int gz) {
        int dx = Math.abs(gx - x);
        int dz = Math.abs(gz - z);
        int diag = Math.min(dx, dz);
        int straight = dx + dz - 2 * diag;
        double h = diag * ActionCosts.WALK_ONE_BLOCK_COST * ActionCosts.SQRT_2
                 + straight * ActionCosts.WALK_ONE_BLOCK_COST;
        int dy = gy - y;
        if (dy > 1) {
            h += (dy - 1) * ActionCosts.FALL_ONE_BLOCK_COST;
        } else if (dy < 0) {
            h += -dy * (ActionCosts.WALK_ONE_BLOCK_COST + ActionCosts.JUMP_PENALTY);
        }
        return h;
    }
}

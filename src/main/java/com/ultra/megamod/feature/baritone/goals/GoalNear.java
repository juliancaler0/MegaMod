package com.ultra.megamod.feature.baritone.goals;

import com.ultra.megamod.feature.baritone.movement.ActionCosts;

/**
 * Goal: within a radius of a position.
 */
public class GoalNear implements Goal {
    public final int x, y, z;
    public final int rangeSq;

    public GoalNear(int x, int y, int z, int range) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.rangeSq = range * range;
    }

    @Override
    public boolean isInGoal(int x, int y, int z) {
        int dx = x - this.x;
        int dy = y - this.y;
        int dz = z - this.z;
        return dx * dx + dy * dy + dz * dz <= rangeSq;
    }

    @Override
    public double heuristic(int x, int y, int z) {
        int dx = Math.abs(x - this.x);
        int dy = y - this.y;
        int dz = Math.abs(z - this.z);
        int range = (int) Math.sqrt(rangeSq);
        // Reduce distances by range
        dx = Math.max(0, dx - range);
        dz = Math.max(0, dz - range);
        int diag = Math.min(dx, dz);
        int straight = dx + dz - 2 * diag;
        double h = diag * ActionCosts.WALK_ONE_BLOCK_COST * ActionCosts.SQRT_2
                 + straight * ActionCosts.WALK_ONE_BLOCK_COST;
        if (dy > 0) {
            h += Math.max(0, dy - range) * (ActionCosts.WALK_ONE_BLOCK_COST + ActionCosts.JUMP_PENALTY);
        } else if (dy < 0) {
            h += Math.max(0, -dy - range) * ActionCosts.FALL_ONE_BLOCK_COST;
        }
        return h;
    }
}

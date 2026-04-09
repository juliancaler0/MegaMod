package com.ultra.megamod.feature.baritone.goals;

import com.ultra.megamod.feature.baritone.movement.ActionCosts;

/**
 * Goal: stand at an exact block position.
 */
public class GoalBlock implements Goal {
    public final int x, y, z;

    public GoalBlock(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public boolean isInGoal(int x, int y, int z) {
        return x == this.x && y == this.y && z == this.z;
    }

    @Override
    public double heuristic(int x, int y, int z) {
        int dx = Math.abs(x - this.x);
        int dy = y - this.y;
        int dz = Math.abs(z - this.z);
        // Diagonal distance heuristic
        int diag = Math.min(dx, dz);
        int straight = dx + dz - 2 * diag;
        double h = diag * ActionCosts.WALK_ONE_BLOCK_COST * ActionCosts.SQRT_2
                 + straight * ActionCosts.WALK_ONE_BLOCK_COST;
        // Y penalty
        if (dy > 0) {
            h += dy * (ActionCosts.WALK_ONE_BLOCK_COST + ActionCosts.JUMP_PENALTY);
        } else if (dy < 0) {
            h += -dy * ActionCosts.FALL_ONE_BLOCK_COST;
        }
        return h;
    }

    @Override
    public String toString() {
        return "GoalBlock{" + x + ", " + y + ", " + z + "}";
    }
}

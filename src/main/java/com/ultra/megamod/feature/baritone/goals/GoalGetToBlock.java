package com.ultra.megamod.feature.baritone.goals;

import com.ultra.megamod.feature.baritone.movement.ActionCosts;

/**
 * Goal: be adjacent to a specific block (within 1 block).
 */
public class GoalGetToBlock implements Goal {
    public final int x, y, z;

    public GoalGetToBlock(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public boolean isInGoal(int gx, int gy, int gz) {
        int dx = Math.abs(gx - x);
        int dy = Math.abs(gy - y);
        int dz = Math.abs(gz - z);
        // Adjacent means within 1 block in any direction, but at most 1 block away total
        return dx + dz <= 1 && dy <= 1 && !(dx == 0 && dy == 0 && dz == 0);
    }

    @Override
    public double heuristic(int gx, int gy, int gz) {
        int dx = Math.abs(gx - x);
        int dz = Math.abs(gz - z);
        // Subtract 1 for adjacency
        double dist = Math.max(0, Math.sqrt(dx * dx + dz * dz) - 1.0);
        int dy = gy - y;
        double h = dist * ActionCosts.WALK_ONE_BLOCK_COST;
        if (dy > 0) {
            h += dy * ActionCosts.FALL_ONE_BLOCK_COST;
        } else if (dy < 0) {
            h += -dy * (ActionCosts.WALK_ONE_BLOCK_COST + ActionCosts.JUMP_PENALTY);
        }
        return h;
    }
}

package com.ultra.megamod.feature.baritone.goals;

/**
 * Goal: satisfied when ANY of the sub-goals is satisfied.
 */
public class GoalComposite implements Goal {
    private final Goal[] goals;

    public GoalComposite(Goal... goals) {
        this.goals = goals;
    }

    @Override
    public boolean isInGoal(int x, int y, int z) {
        for (Goal g : goals) {
            if (g.isInGoal(x, y, z)) return true;
        }
        return false;
    }

    @Override
    public double heuristic(int x, int y, int z) {
        double min = Double.MAX_VALUE;
        for (Goal g : goals) {
            min = Math.min(min, g.heuristic(x, y, z));
        }
        return min;
    }
}

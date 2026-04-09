package com.ultra.megamod.feature.baritone.pathfinding;

import com.ultra.megamod.feature.baritone.goals.Goal;
import com.ultra.megamod.feature.baritone.movement.Movement;

import java.util.List;

/**
 * A computed path from A* — ordered list of movements with a goal.
 */
public class ServerPath {
    private final List<BetterBlockPos> positions;
    private final List<Movement> movements;
    private final Goal goal;
    private final boolean reachedGoal;
    private final int nodesExplored;

    public ServerPath(List<BetterBlockPos> positions, List<Movement> movements, Goal goal, boolean reachedGoal, int nodesExplored) {
        this.positions = positions;
        this.movements = movements;
        this.goal = goal;
        this.reachedGoal = reachedGoal;
        this.nodesExplored = nodesExplored;
    }

    public List<BetterBlockPos> getPositions() { return positions; }
    public List<Movement> getMovements() { return movements; }
    public Goal getGoal() { return goal; }
    public boolean didReachGoal() { return reachedGoal; }
    public int getNodesExplored() { return nodesExplored; }
    public int length() { return positions.size(); }

    public BetterBlockPos getStart() {
        return positions.isEmpty() ? null : positions.get(0);
    }

    public BetterBlockPos getEnd() {
        return positions.isEmpty() ? null : positions.get(positions.size() - 1);
    }
}

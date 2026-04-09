package com.ultra.megamod.feature.baritone.pathfinding;

import com.ultra.megamod.feature.baritone.goals.Goal;
import com.ultra.megamod.feature.baritone.movement.ActionCosts;
import com.ultra.megamod.feature.baritone.movement.Movement;
import com.ultra.megamod.feature.baritone.movement.Moves;

import java.util.*;

/**
 * A* pathfinder with segmented pathfinding, multi-metric backoff,
 * favoring system, and dual timeouts. Adapted from Baritone for server-side use.
 */
public class AStarPathFinder {
    /** Coefficient array for multi-metric backoff — try different heuristic weights */
    private static final double[] COEFFICIENTS = {1.5, 2.0, 3.0, 5.0};

    private final BetterBlockPos start;
    private final Goal goal;
    private final CalculationContext ctx;
    private final Favoring favoring;

    public AStarPathFinder(BetterBlockPos start, Goal goal, CalculationContext ctx) {
        this(start, goal, ctx, new Favoring());
    }

    public AStarPathFinder(BetterBlockPos start, Goal goal, CalculationContext ctx, Favoring favoring) {
        this.start = start;
        this.goal = goal;
        this.ctx = ctx;
        this.favoring = favoring;
    }

    /**
     * Run A* pathfinding. Call from background thread.
     * Uses dual timeouts: primary timeout returns partial path,
     * failure timeout extends search if no decent path found.
     */
    public ServerPath calculate() {
        long primaryTimeout = ctx.settings.primaryTimeoutMS;
        long failureTimeout = ctx.settings.failureTimeoutMS;
        int maxNodes = ctx.settings.maxNodes;

        BinaryHeapOpenSet openSet = new BinaryHeapOpenSet();
        Map<Long, PathNode> nodeMap = new HashMap<>();

        PathNode startNode = getOrCreate(nodeMap, start.x, start.y, start.z);
        startNode.cost = 0;
        startNode.heuristic = goal.heuristic(start.x, start.y, start.z);
        startNode.combinedCost = startNode.heuristic;
        openSet.insert(startNode);

        PathNode bestSoFar = startNode;
        double bestHeuristic = startNode.heuristic;
        // Also track best by (cost + heuristic) and by distance from start
        PathNode bestByScore = startNode;
        double bestScore = Double.MAX_VALUE;
        PathNode furthestFromStart = startNode;
        double furthestDist = 0;

        int nodesExplored = 0;
        long startTime = System.currentTimeMillis();
        boolean usedFailureTimeout = false;

        while (!openSet.isEmpty() && nodesExplored < maxNodes) {
            long elapsed = System.currentTimeMillis() - startTime;

            // Dual timeout: primary timeout first, then failure timeout if no good path
            if (elapsed > primaryTimeout) {
                if (bestHeuristic < startNode.heuristic * 0.5 || elapsed > failureTimeout) {
                    break; // We have a decent partial path, or we've exceeded failure timeout
                }
                usedFailureTimeout = true;
            }

            PathNode current = openSet.removeLowest();
            nodesExplored++;

            if (goal.isInGoal(current.x, current.y, current.z)) {
                return buildPath(current, nodesExplored, true);
            }

            BetterBlockPos currentPos = current.toBlockPos();

            // Try all movement types
            for (Moves moveType : Moves.values()) {
                try {
                    Movement movement = moveType.apply(ctx, currentPos);
                    double cost = movement.calculateCost(ctx);
                    if (cost >= ActionCosts.COST_INF) continue;

                    BetterBlockPos dest = movement.getDest();

                    // Apply avoidance cost if enabled
                    if (ctx.avoidance != null && !ctx.avoidance.isEmpty()) {
                        cost += ctx.avoidance.costAt(dest.x, dest.y, dest.z);
                    }

                    // Apply favoring
                    if (!favoring.isEmpty()) {
                        cost = favoring.applyFavoring(dest.x, dest.y, dest.z, cost);
                    }

                    double newCost = current.cost + cost;
                    PathNode neighbor = getOrCreate(nodeMap, dest.x, dest.y, dest.z);

                    if (newCost < neighbor.cost) {
                        neighbor.cost = newCost;
                        neighbor.heuristic = goal.heuristic(dest.x, dest.y, dest.z);
                        neighbor.combinedCost = newCost + neighbor.heuristic;
                        neighbor.parent = current;

                        if (neighbor.isOpen) {
                            openSet.update(neighbor);
                        } else {
                            openSet.insert(neighbor);
                        }

                        // Track best partial path using multiple metrics
                        if (neighbor.heuristic < bestHeuristic) {
                            bestHeuristic = neighbor.heuristic;
                            bestSoFar = neighbor;
                        }
                        if (neighbor.combinedCost < bestScore) {
                            bestScore = neighbor.combinedCost;
                            bestByScore = neighbor;
                        }
                        double distFromStart = neighbor.cost;
                        if (distFromStart > furthestDist && neighbor.heuristic < startNode.heuristic) {
                            furthestDist = distFromStart;
                            furthestFromStart = neighbor;
                        }
                    }
                } catch (Exception e) {
                    // Skip this movement if it errors
                }
            }
        }

        // Select best partial path from multiple candidates
        PathNode bestPartial = selectBestPartial(startNode, bestSoFar, bestByScore, furthestFromStart);
        if (bestPartial != null && bestPartial != startNode) {
            return buildPath(bestPartial, nodesExplored, false);
        }
        return null; // No path found at all
    }

    /**
     * Select the best partial path from multiple tracking metrics.
     */
    private PathNode selectBestPartial(PathNode startNode, PathNode bestHeuristic,
                                         PathNode bestScore, PathNode furthest) {
        // Prefer: closest to goal, then best score, then furthest progress
        PathNode best = startNode;
        double bestH = startNode.heuristic;

        if (bestHeuristic != startNode && bestHeuristic.heuristic < bestH) {
            best = bestHeuristic;
            bestH = bestHeuristic.heuristic;
        }
        if (bestScore != startNode && bestScore.heuristic < bestH * 1.1 && countNodes(bestScore) > countNodes(best)) {
            best = bestScore;
        }
        // Only use furthest if other options are bad
        if (best == startNode && furthest != startNode) {
            best = furthest;
        }

        return best;
    }

    private int countNodes(PathNode node) {
        int count = 0;
        PathNode n = node;
        while (n != null) {
            count++;
            n = n.parent;
        }
        return count;
    }

    private ServerPath buildPath(PathNode endNode, int nodesExplored, boolean reachedGoal) {
        List<BetterBlockPos> positions = new ArrayList<>();
        PathNode current = endNode;
        while (current != null) {
            positions.add(current.toBlockPos());
            current = current.parent;
        }
        Collections.reverse(positions);

        // Build movement list between consecutive positions
        List<Movement> movements = new ArrayList<>();
        for (int i = 0; i < positions.size() - 1; i++) {
            BetterBlockPos from = positions.get(i);
            BetterBlockPos to = positions.get(i + 1);
            Movement best = findMovement(from, to);
            if (best != null) {
                movements.add(best);
            }
        }

        return new ServerPath(positions, movements, goal, reachedGoal, nodesExplored);
    }

    private Movement findMovement(BetterBlockPos from, BetterBlockPos to) {
        int dx = to.x - from.x;
        int dy = to.y - from.y;
        int dz = to.z - from.z;

        for (Moves moveType : Moves.values()) {
            // Exact match on nominal offsets
            if (moveType.dx == dx && moveType.dy == dy && moveType.dz == dz) {
                return moveType.apply(ctx, from);
            }
            // Parkour and Fall moves have variable destinations — check actual dest
            if (Math.abs(moveType.dx) == 2 || Math.abs(moveType.dz) == 2
                || moveType.name().startsWith("FALL_")) {
                Movement m = moveType.apply(ctx, from);
                if (m.getDest().equals(to)) return m;
            }
        }
        // Fallback: create a traverse in the correct direction
        if (dx > 0) return Moves.TRAVERSE_EAST.apply(ctx, from);
        if (dx < 0) return Moves.TRAVERSE_WEST.apply(ctx, from);
        if (dz > 0) return Moves.TRAVERSE_SOUTH.apply(ctx, from);
        if (dz < 0) return Moves.TRAVERSE_NORTH.apply(ctx, from);
        return Moves.TRAVERSE_NORTH.apply(ctx, from);
    }

    private PathNode getOrCreate(Map<Long, PathNode> map, int x, int y, int z) {
        long key = ((long) x & 0x3FFFFFFL) | (((long) y & 0xFFFL) << 26) | (((long) z & 0x3FFFFFFL) << 38);
        return map.computeIfAbsent(key, k -> new PathNode(x, y, z));
    }
}

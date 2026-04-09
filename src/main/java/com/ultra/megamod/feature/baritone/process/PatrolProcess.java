package com.ultra.megamod.feature.baritone.process;

import com.ultra.megamod.feature.baritone.goals.Goal;
import com.ultra.megamod.feature.baritone.goals.GoalBlock;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Process: patrol between a set of waypoints in a loop.
 * Configurable pause duration at each waypoint and random patrol mode.
 */
public class PatrolProcess implements BotProcess {
    private boolean active = false;
    private String status = "Idle";
    private final List<BlockPos> waypoints = new ArrayList<>();
    private int currentWaypoint = 0;
    private int loops = 0;
    private int pauseTicks = 0;
    private int waypointPauseDuration = 40;
    private boolean loopPatrol = true;
    private boolean randomMode = false;

    public void start(List<BlockPos> points) {
        this.start(points, 40, true, false);
    }

    public void start(List<BlockPos> points, int pauseDuration, boolean loop, boolean random) {
        this.waypoints.clear();
        this.waypoints.addAll(points);
        this.currentWaypoint = 0;
        this.loops = 0;
        this.pauseTicks = 0;
        this.waypointPauseDuration = pauseDuration;
        this.loopPatrol = loop;
        this.randomMode = random;
        this.active = !points.isEmpty();
        if (active) {
            BlockPos wp = waypoints.get(0);
            this.status = "Patrolling to waypoint 1/" + waypoints.size() + " (" + wp.getX() + "," + wp.getY() + "," + wp.getZ() + ")";
        }
    }

    @Override
    public String name() { return "Patrol"; }

    @Override
    public boolean isActive() { return active; }

    @Override
    public double priority() { return 35; }

    @Override
    public PathingCommand onTick(boolean calcFailed, boolean safeToCancel) {
        if (!active || waypoints.isEmpty()) return null;

        // Pause at waypoint
        if (pauseTicks > 0) {
            pauseTicks--;
            status = "Pausing at waypoint " + (currentWaypoint + 1) + "/" + waypoints.size()
                   + " (" + pauseTicks + " ticks)";
            return new PathingCommand(null, PathingCommand.CommandType.REQUEST_PAUSE);
        }

        BlockPos target = waypoints.get(currentWaypoint);
        status = "Patrol wp " + (currentWaypoint + 1) + "/" + waypoints.size() + " (loop " + (loops + 1) + ")";

        Goal goal = new GoalBlock(target.getX(), target.getY(), target.getZ());
        return new PathingCommand(goal, PathingCommand.CommandType.SET_GOAL_AND_PATH);
    }

    /** Called when the bot reaches the current waypoint */
    public void onWaypointReached() {
        pauseTicks = waypointPauseDuration;

        if (randomMode) {
            // Pick a random next waypoint (different from current)
            if (waypoints.size() > 1) {
                int next;
                do {
                    next = (int) (Math.random() * waypoints.size());
                } while (next == currentWaypoint);
                currentWaypoint = next;
            }
        } else {
            currentWaypoint++;
            if (currentWaypoint >= waypoints.size()) {
                if (loopPatrol) {
                    currentWaypoint = 0;
                    loops++;
                } else {
                    active = false;
                    status = "Patrol complete (" + loops + " loops, " + waypoints.size() + " waypoints)";
                }
            }
        }
    }

    public BlockPos getCurrentTarget() {
        if (waypoints.isEmpty()) return null;
        return waypoints.get(currentWaypoint);
    }

    public List<BlockPos> getWaypoints() { return waypoints; }
    public int getLoops() { return loops; }

    @Override
    public void onLostControl() {}

    @Override
    public void cancel() {
        active = false;
        status = "Patrol stopped (loop " + loops + ")";
    }

    @Override
    public String getStatus() { return status; }
}

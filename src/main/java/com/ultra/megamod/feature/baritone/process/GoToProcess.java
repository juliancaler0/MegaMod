package com.ultra.megamod.feature.baritone.process;

import com.ultra.megamod.feature.baritone.goals.Goal;
import com.ultra.megamod.feature.baritone.goals.GoalBlock;
import com.ultra.megamod.feature.baritone.goals.GoalXZ;

/**
 * Process: navigate to specific coordinates.
 */
public class GoToProcess implements BotProcess {
    private Goal goal;
    private boolean active = false;
    private String status = "Idle";

    public void setGoal(int x, int y, int z) {
        this.goal = new GoalBlock(x, y, z);
        this.active = true;
        this.status = "Going to " + x + ", " + y + ", " + z;
    }

    public void setGoalXZ(int x, int z) {
        this.goal = new GoalXZ(x, z);
        this.active = true;
        this.status = "Going to " + x + ", " + z;
    }

    @Override
    public String name() { return "GoTo"; }

    @Override
    public boolean isActive() { return active; }

    @Override
    public double priority() { return 50; }

    @Override
    public PathingCommand onTick(boolean calcFailed, boolean safeToCancel) {
        if (!active || goal == null) return null;

        if (calcFailed) {
            status = "Path failed, retrying...";
            return new PathingCommand(goal, PathingCommand.CommandType.FORCE_REVALIDATE);
        }
        return new PathingCommand(goal, PathingCommand.CommandType.SET_GOAL_AND_PATH);
    }

    @Override
    public void onLostControl() {
        // Keep goal but don't cancel — might resume
    }

    @Override
    public void cancel() {
        active = false;
        goal = null;
        status = "Cancelled";
    }

    @Override
    public String getStatus() { return status; }

    public void markComplete() {
        active = false;
        status = "Arrived!";
    }
}

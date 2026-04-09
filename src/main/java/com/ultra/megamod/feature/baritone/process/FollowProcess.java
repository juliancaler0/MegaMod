package com.ultra.megamod.feature.baritone.process;

import com.ultra.megamod.feature.baritone.goals.Goal;
import com.ultra.megamod.feature.baritone.goals.GoalNear;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

/**
 * Process: follow a target player/entity.
 * Configurable follow distance, sprint option, dimension-change detection.
 */
public class FollowProcess implements BotProcess {
    private ServerPlayer target;
    private String targetName;
    private boolean active = false;
    private String status = "Idle";
    private int followDistance = 3;
    private boolean followSprint = true;
    private ResourceKey<Level> lastDimension;

    public void start(ServerPlayer target) {
        this.start(target, 3, true);
    }

    public void start(ServerPlayer target, int followDistance, boolean followSprint) {
        this.target = target;
        this.targetName = target.getGameProfile().name();
        this.followDistance = followDistance;
        this.followSprint = followSprint;
        this.active = true;
        this.lastDimension = target.level().dimension();
        this.status = "Following " + targetName;
    }

    @Override
    public String name() { return "Follow"; }

    @Override
    public boolean isActive() { return active; }

    @Override
    public double priority() { return 70; } // Higher than GoTo/Mine

    @Override
    public PathingCommand onTick(boolean calcFailed, boolean safeToCancel) {
        if (!active || target == null) return null;

        // Check if target is still online
        if (target.isRemoved()) {
            status = targetName + " went offline";
            active = false;
            return null;
        }

        // Dimension change detection
        ResourceKey<Level> currentDim = target.level().dimension();
        if (!currentDim.equals(lastDimension)) {
            status = targetName + " changed dimensions!";
            lastDimension = currentDim;
            // Can't follow across dimensions — pause
            return new PathingCommand(null, PathingCommand.CommandType.REQUEST_PAUSE);
        }

        int tx = target.blockPosition().getX();
        int ty = target.blockPosition().getY();
        int tz = target.blockPosition().getZ();

        status = "Following " + targetName + " (" + tx + ", " + ty + ", " + tz + ") d=" + followDistance;
        Goal goal = new GoalNear(tx, ty, tz, followDistance);
        return new PathingCommand(goal, PathingCommand.CommandType.SET_GOAL_AND_PATH);
    }

    public ServerPlayer getTarget() { return target; }
    public String getTargetName() { return targetName; }
    public int getFollowDistance() { return followDistance; }

    @Override
    public void onLostControl() {}

    @Override
    public void cancel() {
        active = false;
        target = null;
        status = "Stopped following";
    }

    @Override
    public String getStatus() { return status; }
}

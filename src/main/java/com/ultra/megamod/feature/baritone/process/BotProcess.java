package com.ultra.megamod.feature.baritone.process;

import com.ultra.megamod.feature.baritone.goals.Goal;

/**
 * Interface for bot process modules (GoTo, Mine, Follow, etc.).
 * Each process provides goals and manages its own state.
 */
public interface BotProcess {
    /** Display name of this process */
    String name();

    /** Whether this process is currently active */
    boolean isActive();

    /** Priority (higher = more important). Range 0-100. */
    double priority();

    /**
     * Called each tick while this process is active.
     * Returns a PathingCommand describing what path goal the bot should pursue,
     * or null if the process has nothing to do this tick.
     */
    PathingCommand onTick(boolean calcFailed, boolean safeToCancel);

    /** Called when this process loses control (higher priority process took over) */
    void onLostControl();

    /** Cancel this process */
    void cancel();

    /** Get status text for display */
    String getStatus();

    /**
     * A command from a process to the pathfinding system.
     */
    record PathingCommand(Goal goal, CommandType type) {
        public enum CommandType {
            /** Set goal and start pathing */
            SET_GOAL_AND_PATH,
            /** Force recalculate path */
            FORCE_REVALIDATE,
            /** Request a revalidation only if current path seems bad */
            REQUEST_PAUSE,
            /** Cancel current pathing */
            CANCEL
        }
    }
}

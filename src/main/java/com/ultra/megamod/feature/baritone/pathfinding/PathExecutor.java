package com.ultra.megamod.feature.baritone.pathfinding;

import com.ultra.megamod.feature.baritone.movement.Movement;
import com.ultra.megamod.feature.baritone.movement.MovementState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

/**
 * Tick-by-tick path following state machine.
 * Supports path splicing (execute current while calculating next),
 * per-movement timeout, and recovery from terrain changes.
 */
public class PathExecutor {
    public enum State {
        RUNNING,
        FINISHED,
        FAILED
    }

    private final ServerPath path;
    private int currentIndex = 0;
    private State state = State.RUNNING;
    private int ticksSinceLastProgress = 0;
    private int movementTickCount = 0;
    private static final int STUCK_THRESHOLD = 100; // Ticks before declaring stuck
    private static final int MOVEMENT_TIMEOUT = 80; // Max ticks per single movement
    private int retryCount = 0;
    private static final int MAX_RETRIES = 3;

    public PathExecutor(ServerPath path) {
        this.path = path;
    }

    public State getState() { return state; }
    public ServerPath getPath() { return path; }
    public int getCurrentIndex() { return currentIndex; }

    public int getRemainingMovements() {
        return path.getMovements().size() - currentIndex;
    }

    public BetterBlockPos getCurrentTarget() {
        List<BetterBlockPos> positions = path.getPositions();
        int targetIdx = Math.min(currentIndex + 1, positions.size() - 1);
        return positions.get(targetIdx);
    }

    /**
     * Get ETA in ticks based on remaining movements.
     */
    public int getEstimatedTicksRemaining() {
        // Rough estimate: ~10 ticks per movement on average
        return getRemainingMovements() * 10;
    }

    /**
     * Check if the current path should be spliced with a new one.
     * Returns true if we're past the halfway point.
     */
    public boolean shouldSplice() {
        return currentIndex > path.getMovements().size() / 2;
    }

    /**
     * Splice a new path onto this one — continue current execution
     * but note that a new path is ready to take over.
     */
    public int getSpliceIndex() {
        return currentIndex;
    }

    /**
     * Tick the path executor. Returns the current state.
     */
    public State tick(ServerPlayer player, ServerLevel level) {
        if (state != State.RUNNING) return state;

        List<Movement> movements = path.getMovements();
        if (currentIndex >= movements.size()) {
            state = State.FINISHED;
            return state;
        }

        Movement current = movements.get(currentIndex);
        movementTickCount++;

        // Per-movement timeout
        if (movementTickCount > MOVEMENT_TIMEOUT) {
            retryCount++;
            if (retryCount > MAX_RETRIES) {
                state = State.FAILED;
                return state;
            }
            // Skip this movement and try the next one
            currentIndex++;
            movementTickCount = 0;
            ticksSinceLastProgress = 0;
            if (currentIndex >= movements.size()) {
                state = State.FINISHED;
            }
            return state;
        }

        MovementState.Status result = current.tick(player, level);

        switch (result) {
            case SUCCESS:
                currentIndex++;
                ticksSinceLastProgress = 0;
                movementTickCount = 0;
                retryCount = 0;
                if (currentIndex >= movements.size()) {
                    state = State.FINISHED;
                }
                break;
            case FAILED:
                retryCount++;
                if (retryCount > MAX_RETRIES) {
                    state = State.FAILED;
                } else {
                    // Try skipping this movement
                    currentIndex++;
                    movementTickCount = 0;
                    ticksSinceLastProgress = 0;
                    if (currentIndex >= movements.size()) {
                        state = State.FINISHED;
                    }
                }
                break;
            case RUNNING:
            case WAITING:
                ticksSinceLastProgress++;
                if (ticksSinceLastProgress > STUCK_THRESHOLD) {
                    state = State.FAILED; // Stuck
                }
                break;
            default:
                break;
        }

        return state;
    }
}

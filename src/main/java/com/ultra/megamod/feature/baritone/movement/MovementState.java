package com.ultra.megamod.feature.baritone.movement;

/**
 * Tracks the execution state of a single movement.
 */
public class MovementState {
    public enum Status {
        /** Not started yet */
        WAITING,
        /** Movement is being executed */
        RUNNING,
        /** Movement completed successfully */
        SUCCESS,
        /** Movement failed / unreachable */
        FAILED,
        /** Movement was cancelled */
        CANCELLED
    }

    private Status status = Status.WAITING;
    private int ticksInCurrent = 0;

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public int getTicksInCurrent() { return ticksInCurrent; }
    public void incrementTick() { ticksInCurrent++; }
    public void reset() {
        status = Status.WAITING;
        ticksInCurrent = 0;
    }
}

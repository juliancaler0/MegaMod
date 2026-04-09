package com.ultra.megamod.feature.baritone.process;

import com.ultra.megamod.feature.baritone.goals.Goal;
import com.ultra.megamod.feature.baritone.goals.GoalXZ;

import java.util.HashSet;
import java.util.Set;

/**
 * Process: auto-explore unvisited chunks spiraling outward.
 * Chunk-loading awareness, configurable chunk radius, spiral expansion.
 */
public class ExploreProcess implements BotProcess {
    private boolean active = false;
    private String status = "Idle";
    private int originX, originZ;
    private int currentRing = 1;
    private int ringIndex = 0;
    private final Set<Long> visitedChunks = new HashSet<>();
    private int chunksExplored = 0;
    private int maxRings = 32; // Max exploration radius in chunks
    private boolean spiralExpansion = true;
    private int failedPathCount = 0;
    private static final int MAX_FAILED_PATHS = 5;

    public void start(int originX, int originZ) {
        this.start(originX, originZ, 32, true);
    }

    public void start(int originX, int originZ, int maxChunkRadius, boolean spiral) {
        this.originX = originX;
        this.originZ = originZ;
        this.currentRing = 1;
        this.ringIndex = 0;
        this.active = true;
        this.chunksExplored = 0;
        this.maxRings = maxChunkRadius;
        this.spiralExpansion = spiral;
        this.failedPathCount = 0;
        this.status = "Exploring from " + originX + ", " + originZ;
    }

    @Override
    public String name() { return "Explore"; }

    @Override
    public boolean isActive() { return active; }

    @Override
    public double priority() { return 30; } // Low priority

    @Override
    public PathingCommand onTick(boolean calcFailed, boolean safeToCancel) {
        if (!active) return null;

        // Track failed paths — skip ahead if stuck
        if (calcFailed) {
            failedPathCount++;
            if (failedPathCount > MAX_FAILED_PATHS) {
                // Skip to next ring
                currentRing++;
                ringIndex = 0;
                failedPathCount = 0;
            } else {
                // Skip this chunk
                ringIndex++;
            }
        } else {
            failedPathCount = 0;
        }

        // Find next unvisited chunk in a spiral pattern
        int[] next = getNextChunk();
        if (next == null) {
            if (currentRing < maxRings) {
                currentRing++;
                ringIndex = 0;
                next = getNextChunk();
            }
        }

        if (next == null || currentRing > maxRings) {
            status = "Explored " + chunksExplored + " chunks (max radius reached)";
            active = false;
            return null;
        }

        int targetX = next[0] * 16 + 8;
        int targetZ = next[1] * 16 + 8;
        status = "Exploring chunk " + next[0] + "," + next[1] + " (ring " + currentRing + ", " + chunksExplored + " done)";

        Goal goal = new GoalXZ(targetX, targetZ);
        return new PathingCommand(goal, PathingCommand.CommandType.SET_GOAL_AND_PATH);
    }

    public void markChunkVisited(int chunkX, int chunkZ) {
        visitedChunks.add(chunkKey(chunkX, chunkZ));
        chunksExplored++;
    }

    public int getChunksExplored() { return chunksExplored; }

    private int[] getNextChunk() {
        // Spiral: walk around the current ring
        int perimeter = currentRing * 8;
        while (ringIndex < perimeter) {
            int[] chunk = ringPos(currentRing, ringIndex);
            ringIndex++;
            long key = chunkKey(chunk[0], chunk[1]);
            if (!visitedChunks.contains(key)) {
                return chunk;
            }
        }
        return null;
    }

    private int[] ringPos(int ring, int index) {
        int side = ring * 2;
        int cx = originX >> 4;
        int cz = originZ >> 4;
        if (index < side) {
            return new int[]{cx - ring + index, cz - ring};
        } else if (index < side * 2) {
            return new int[]{cx + ring, cz - ring + (index - side)};
        } else if (index < side * 3) {
            return new int[]{cx + ring - (index - side * 2), cz + ring};
        } else {
            return new int[]{cx - ring, cz + ring - (index - side * 3)};
        }
    }

    private long chunkKey(int cx, int cz) {
        return ((long) cx << 32) | (cz & 0xFFFFFFFFL);
    }

    @Override
    public void onLostControl() {}

    @Override
    public void cancel() {
        active = false;
        status = "Stopped exploring (" + chunksExplored + " chunks)";
    }

    @Override
    public String getStatus() { return status; }
}

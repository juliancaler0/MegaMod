package com.ultra.megamod.feature.baritone.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Client-side path data storage for bot pathfinding visualization.
 * Stores path points, goal position, ETA, stats, waypoint markers,
 * target block highlights, calculating path, and selection region.
 */
public class BotPathRenderer {
    private static final List<PathPoint> currentPath = new CopyOnWriteArrayList<>();
    private static boolean enabled = false;
    private static long pathExpiry = 0;
    private static String targetDescription = "";
    private static PathPoint goalPosition;
    private static int eta = -1;
    private static int blocksMined = 0;
    private static int cropsHarvested = 0;
    private static int blocksPlaced = 0;
    private static final List<PathPoint> waypointMarkers = new CopyOnWriteArrayList<>();

    // Target blocks (ores being mined, crops being farmed, etc.)
    private static final List<TargetBlock> targetBlocks = new CopyOnWriteArrayList<>();

    // Calculating path (partial A* exploration)
    private static final List<PathPoint> calculatingPath = new CopyOnWriteArrayList<>();
    private static boolean isCalculating = false;
    private static int nodesExplored = 0;

    // Selection region (quarry bounds, build bounds)
    private static double[] selectionMin = null; // {x, y, z}
    private static double[] selectionMax = null; // {x, y, z}

    // Bot active state — tracks whether a bot process is running for this player
    private static boolean botActive = false;
    private static String processName = "none";
    private static long botActiveExpiry = 0;

    public record PathPoint(double x, double y, double z) {}
    public record TargetBlock(double x, double y, double z, float r, float g, float b) {}

    public static void setPath(List<PathPoint> path) {
        currentPath.clear();
        currentPath.addAll(path);
        pathExpiry = System.currentTimeMillis() + 30000;
        enabled = true;
    }

    public static void setTargetDescription(String desc) {
        targetDescription = desc;
    }

    public static void setGoalPosition(PathPoint goal) {
        goalPosition = goal;
    }

    public static void setEta(int etaSeconds) {
        eta = etaSeconds;
    }

    public static void setStats(int mined, int harvested, int placed) {
        blocksMined = mined;
        cropsHarvested = harvested;
        blocksPlaced = placed;
    }

    public static void setWaypoints(List<PathPoint> wps) {
        waypointMarkers.clear();
        waypointMarkers.addAll(wps);
    }

    public static void setTargetBlocks(List<TargetBlock> blocks) {
        targetBlocks.clear();
        if (blocks != null) targetBlocks.addAll(blocks);
    }

    public static void setCalculatingPath(List<PathPoint> partial, int nodes) {
        calculatingPath.clear();
        if (partial != null) calculatingPath.addAll(partial);
        nodesExplored = nodes;
    }

    public static void setCalculating(boolean calc) {
        isCalculating = calc;
    }

    public static void setSelection(double[] min, double[] max) {
        selectionMin = min;
        selectionMax = max;
    }

    public static void clearSelection() {
        selectionMin = null;
        selectionMax = null;
    }

    public static void setBotActive(boolean active, String process) {
        botActive = active;
        processName = process != null ? process : "none";
        botActiveExpiry = System.currentTimeMillis() + 10000; // 10s timeout
    }

    public static void clear() {
        currentPath.clear();
        waypointMarkers.clear();
        targetBlocks.clear();
        calculatingPath.clear();
        enabled = false;
        targetDescription = "";
        goalPosition = null;
        eta = -1;
        blocksMined = 0;
        cropsHarvested = 0;
        blocksPlaced = 0;
        isCalculating = false;
        nodesExplored = 0;
        selectionMin = null;
        selectionMax = null;
        botActive = false;
        processName = "none";
    }

    public static boolean isEnabled() { return enabled && System.currentTimeMillis() < pathExpiry; }
    public static boolean isBotActive() { return botActive && System.currentTimeMillis() < botActiveExpiry; }
    public static String getProcessName() { return processName; }
    public static List<PathPoint> getPath() { return currentPath; }
    public static String getTargetDescription() { return targetDescription; }
    public static int getPathSize() { return currentPath.size(); }
    public static PathPoint getGoalPosition() { return goalPosition; }
    public static int getEta() { return eta; }
    public static int getBlocksMined() { return blocksMined; }
    public static int getCropsHarvested() { return cropsHarvested; }
    public static int getBlocksPlaced() { return blocksPlaced; }
    public static List<PathPoint> getWaypointMarkers() { return waypointMarkers; }
    public static List<TargetBlock> getTargetBlocks() { return targetBlocks; }
    public static List<PathPoint> getCalculatingPath() { return calculatingPath; }
    public static boolean isCalculating() { return isCalculating; }
    public static int getNodesExplored() { return nodesExplored; }
    public static double[] getSelectionMin() { return selectionMin; }
    public static double[] getSelectionMax() { return selectionMax; }

    /** Get the next waypoint on the path (first point) */
    public static PathPoint getNextWaypoint() {
        return currentPath.isEmpty() ? null : currentPath.get(0);
    }

    /** Get the final destination */
    public static PathPoint getDestination() {
        if (goalPosition != null) return goalPosition;
        return currentPath.isEmpty() ? null : currentPath.get(currentPath.size() - 1);
    }

    /** Parse path data from server JSON */
    public static void parsePathData(String json) {
        try {
            com.google.gson.JsonObject obj = com.google.gson.JsonParser.parseString(json).getAsJsonObject();
            if (obj.has("path")) {
                com.google.gson.JsonArray arr = obj.getAsJsonArray("path");
                List<PathPoint> path = new ArrayList<>();
                for (com.google.gson.JsonElement el : arr) {
                    com.google.gson.JsonObject p = el.getAsJsonObject();
                    path.add(new PathPoint(p.get("x").getAsDouble(), p.get("y").getAsDouble(), p.get("z").getAsDouble()));
                }
                setPath(path);
            }
            if (obj.has("status")) {
                String status = obj.get("status").getAsString();
                setTargetDescription(status);
                // Mark bot as active if status is not idle/none
                boolean active = !status.isEmpty() && !status.equalsIgnoreCase("idle") && !status.equalsIgnoreCase("No active process");
                setBotActive(active || !currentPath.isEmpty(), status);
            }
            if (obj.has("goal")) {
                com.google.gson.JsonObject g = obj.getAsJsonObject("goal");
                double gx = g.get("x").getAsDouble();
                double gy = g.has("y") ? g.get("y").getAsDouble() : 64;
                double gz = g.get("z").getAsDouble();
                setGoalPosition(new PathPoint(gx, gy, gz));
            }
            if (obj.has("eta")) {
                setEta(obj.get("eta").getAsInt());
            }
            if (obj.has("blocksMined")) {
                setStats(
                    obj.get("blocksMined").getAsInt(),
                    obj.has("cropsHarvested") ? obj.get("cropsHarvested").getAsInt() : 0,
                    obj.has("blocksPlaced") ? obj.get("blocksPlaced").getAsInt() : 0
                );
            }

            // Parse target blocks (ores, crops, etc.)
            if (obj.has("targetBlocks")) {
                com.google.gson.JsonArray tbArr = obj.getAsJsonArray("targetBlocks");
                List<TargetBlock> targets = new ArrayList<>();
                for (com.google.gson.JsonElement el : tbArr) {
                    com.google.gson.JsonObject tb = el.getAsJsonObject();
                    targets.add(new TargetBlock(
                        tb.get("x").getAsDouble(),
                        tb.get("y").getAsDouble(),
                        tb.get("z").getAsDouble(),
                        tb.has("r") ? tb.get("r").getAsFloat() : 0.2f,
                        tb.has("g") ? tb.get("g").getAsFloat() : 0.9f,
                        tb.has("b") ? tb.get("b").getAsFloat() : 0.9f
                    ));
                }
                setTargetBlocks(targets);
            } else {
                setTargetBlocks(null);
            }

            // Parse calculating state
            if (obj.has("calculating")) {
                setCalculating(obj.get("calculating").getAsBoolean());
            } else {
                setCalculating(false);
            }
            if (obj.has("calculatingPath")) {
                com.google.gson.JsonArray cpArr = obj.getAsJsonArray("calculatingPath");
                List<PathPoint> partial = new ArrayList<>();
                for (com.google.gson.JsonElement el : cpArr) {
                    com.google.gson.JsonObject p = el.getAsJsonObject();
                    partial.add(new PathPoint(p.get("x").getAsDouble(), p.get("y").getAsDouble(), p.get("z").getAsDouble()));
                }
                int nodes = obj.has("nodesExplored") ? obj.get("nodesExplored").getAsInt() : 0;
                setCalculatingPath(partial, nodes);
            } else {
                setCalculatingPath(null, obj.has("nodesExplored") ? obj.get("nodesExplored").getAsInt() : 0);
            }

            // Parse selection region (quarry/build bounds)
            if (obj.has("selection")) {
                com.google.gson.JsonObject sel = obj.getAsJsonObject("selection");
                double[] min = new double[] {
                    sel.get("minX").getAsDouble(),
                    sel.get("minY").getAsDouble(),
                    sel.get("minZ").getAsDouble()
                };
                double[] max = new double[] {
                    sel.get("maxX").getAsDouble(),
                    sel.get("maxY").getAsDouble(),
                    sel.get("maxZ").getAsDouble()
                };
                setSelection(min, max);
            } else {
                clearSelection();
            }

            // Parse waypoint markers
            if (obj.has("waypoints")) {
                com.google.gson.JsonArray wpArr = obj.getAsJsonArray("waypoints");
                List<PathPoint> wps = new ArrayList<>();
                for (com.google.gson.JsonElement el : wpArr) {
                    com.google.gson.JsonObject wp = el.getAsJsonObject();
                    wps.add(new PathPoint(wp.get("x").getAsDouble(), wp.get("y").getAsDouble(), wp.get("z").getAsDouble()));
                }
                setWaypoints(wps);
            }
        } catch (Exception ignored) {}
    }
}

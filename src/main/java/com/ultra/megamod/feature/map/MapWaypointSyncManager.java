package com.ultra.megamod.feature.map;

import java.util.ArrayList;
import java.util.List;

/**
 * Client-side cache of waypoints for use by WaypointBeaconRenderer.
 * Updated whenever MapScreen parses waypoint data from the server.
 */
public class MapWaypointSyncManager {

    public record BeaconWaypoint(int x, int y, int z, int colorIndex, boolean beaconEnabled, String dimension) {}

    private static final List<BeaconWaypoint> waypoints = new ArrayList<>();
    private static boolean beaconsGloballyEnabled = true;

    /**
     * Replace the cached waypoint list with fresh data from the server.
     */
    public static void updateWaypoints(List<BeaconWaypoint> newWaypoints) {
        synchronized (waypoints) {
            waypoints.clear();
            waypoints.addAll(newWaypoints);
        }
    }

    /**
     * Returns all waypoints that should render a beacon beam in the given dimension.
     */
    public static List<BeaconWaypoint> getActiveBeaconWaypoints(String currentDimension) {
        if (!beaconsGloballyEnabled) return List.of();
        synchronized (waypoints) {
            return waypoints.stream()
                    .filter(w -> w.beaconEnabled() && w.dimension().equals(currentDimension))
                    .toList();
        }
    }

    /**
     * Returns all waypoints in the given dimension (for minimap display).
     */
    public static List<BeaconWaypoint> getWaypointsForDimension(String currentDimension) {
        synchronized (waypoints) {
            return waypoints.stream()
                    .filter(w -> w.dimension().equals(currentDimension))
                    .toList();
        }
    }

    /**
     * Master toggle for all 3D beacon beams.
     */
    public static void setBeaconsEnabled(boolean enabled) {
        beaconsGloballyEnabled = enabled;
    }

    public static boolean areBeaconsEnabled() {
        return beaconsGloballyEnabled;
    }

    /**
     * Clear all cached waypoints (e.g. on disconnect).
     */
    public static void clear() {
        synchronized (waypoints) {
            waypoints.clear();
        }
    }
}

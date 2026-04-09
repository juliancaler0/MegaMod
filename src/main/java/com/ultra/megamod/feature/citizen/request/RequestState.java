package com.ultra.megamod.feature.citizen.request;

/**
 * Lifecycle states for a request in the colony request system.
 */
public enum RequestState {
    CREATED,
    ASSIGNED,
    IN_PROGRESS,
    OVERRULED,
    COMPLETED,
    CANCELLED,
    FAILED;

    public static RequestState fromOrdinal(int ordinal) {
        RequestState[] values = values();
        if (ordinal >= 0 && ordinal < values.length) {
            return values[ordinal];
        }
        return CREATED;
    }
}

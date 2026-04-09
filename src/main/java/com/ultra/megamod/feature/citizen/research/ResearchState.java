package com.ultra.megamod.feature.citizen.research;

/**
 * Tracks the lifecycle state of a research within a colony.
 */
public enum ResearchState {
    NOT_STARTED,
    IN_PROGRESS,
    FINISHED;

    public static ResearchState fromString(String s) {
        try {
            return valueOf(s);
        } catch (Exception e) {
            return NOT_STARTED;
        }
    }
}

package com.ultra.megamod.feature.citizen.data;

public enum CitizenStatus {
    IDLE("Idle"),
    WORK("Working"),
    FOLLOW("Following"),
    SLEEP("Sleeping"),
    DEPOSIT("Depositing"),
    HOLD_POSITION("Holding"),
    MOVE_TO("Moving"),
    PATROL("Patrolling"),
    COMBAT("In Combat"),
    WANDER("Wandering"),
    FLEE("Fleeing");

    private final String displayName;

    CitizenStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }

    public static CitizenStatus fromString(String name) {
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return IDLE;
        }
    }
}

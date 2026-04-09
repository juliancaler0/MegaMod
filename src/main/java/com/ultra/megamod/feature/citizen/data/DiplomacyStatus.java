package com.ultra.megamod.feature.citizen.data;

import org.jetbrains.annotations.NotNull;

/**
 * Diplomacy relation status between two colonies.
 */
public enum DiplomacyStatus {
    NEUTRAL("Neutral"),
    FRIENDLY("Friendly"),
    ALLIED("Allied"),
    HOSTILE("Hostile"),
    WAR("War");

    private final String displayName;

    DiplomacyStatus(String displayName) {
        this.displayName = displayName;
    }

    @NotNull
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Parses a status from a string, defaulting to NEUTRAL if unknown.
     */
    @NotNull
    public static DiplomacyStatus fromString(String str) {
        if (str == null || str.isEmpty()) return NEUTRAL;
        try {
            return valueOf(str.toUpperCase());
        } catch (IllegalArgumentException e) {
            return NEUTRAL;
        }
    }
}

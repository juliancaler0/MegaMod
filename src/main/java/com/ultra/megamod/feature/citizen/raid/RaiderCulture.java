package com.ultra.megamod.feature.citizen.raid;

/**
 * Enum of raider cultures. Each culture has a display name and a difficulty multiplier
 * that scales raider stats (health, damage, count).
 */
public enum RaiderCulture {
    BARBARIAN("Barbarian", 1.0),
    PIRATE("Pirate", 1.1),
    EGYPTIAN("Egyptian", 1.2),
    NORSEMEN("Norsemen", 1.15),
    AMAZON("Amazon", 1.1),
    DROWNED_PIRATE("Drowned Pirate", 1.3);

    private final String displayName;
    private final double difficultyMultiplier;

    RaiderCulture(String displayName, double difficultyMultiplier) {
        this.displayName = displayName;
        this.difficultyMultiplier = difficultyMultiplier;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getDifficultyMultiplier() {
        return difficultyMultiplier;
    }

    /**
     * Get a culture by ordinal, with BARBARIAN as fallback.
     */
    public static RaiderCulture fromOrdinal(int ordinal) {
        RaiderCulture[] values = values();
        if (ordinal >= 0 && ordinal < values.length) {
            return values[ordinal];
        }
        return BARBARIAN;
    }

    /**
     * Get a culture by name (case-insensitive), with BARBARIAN as fallback.
     */
    public static RaiderCulture fromName(String name) {
        for (RaiderCulture culture : values()) {
            if (culture.name().equalsIgnoreCase(name)) {
                return culture;
            }
        }
        return BARBARIAN;
    }
}

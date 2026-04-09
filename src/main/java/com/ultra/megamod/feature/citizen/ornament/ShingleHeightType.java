package com.ultra.megamod.feature.citizen.ornament;

/**
 * Height variants for shingle roof blocks.
 * Controls the slope angle and vertical offset of the shingle.
 */
public enum ShingleHeightType {
    DEFAULT("default", "Default"),
    FLAT("flat", "Flat"),
    FLAT_LOWER("flat_lower", "Flat Lower"),
    STEEP("steep", "Steep"),
    STEEP_LOWER("steep_lower", "Steep Lower");

    private final String id;
    private final String displayName;

    ShingleHeightType(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }
}

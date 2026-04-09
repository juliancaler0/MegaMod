package com.ultra.megamod.feature.citizen.ornament;

/**
 * Shape variants for pillar ornamental blocks.
 * Controls the cross-section geometry of the pillar.
 */
public enum PillarShapeType {
    ROUND("round", "Round"),
    VOXEL("voxel", "Voxel"),
    SQUARE("square", "Square");

    private final String id;
    private final String displayName;

    PillarShapeType(String id, String displayName) {
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

package com.ultra.megamod.feature.citizen.blueprint.tools;

/**
 * Procedural shapes supported by the shape tool.
 */
public enum Shape {
    CUBE("Cube"),
    SPHERE("Sphere"),
    HALF_SPHERE("Half Sphere"),
    BOWL("Bowl"),
    WAVE("Wave"),
    WAVE_3D("Wave 3D"),
    DIAMOND("Diamond"),
    PYRAMID("Pyramid"),
    UPSIDE_DOWN_PYRAMID("Upside-Down Pyramid"),
    CYLINDER("Cylinder"),
    CONE("Cone");

    private final String displayName;

    Shape(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the human-readable display name.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Looks up a Shape by name (case-insensitive). Returns CUBE if not found.
     */
    public static Shape fromString(String name) {
        if (name == null || name.isEmpty()) {
            return CUBE;
        }
        for (Shape shape : values()) {
            if (shape.name().equalsIgnoreCase(name) || shape.displayName.equalsIgnoreCase(name)) {
                return shape;
            }
        }
        return CUBE;
    }
}

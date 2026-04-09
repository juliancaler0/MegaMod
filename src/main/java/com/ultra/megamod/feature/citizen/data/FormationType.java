package com.ultra.megamod.feature.citizen.data;

public enum FormationType {
    LINE("Line"),
    COLUMN("Column"),
    WEDGE("Wedge"),
    SQUARE("Square"),
    CIRCLE("Circle"),
    SCATTER("Scatter"),
    MOVEMENT("Movement"),
    HOLLOW_SQUARE("Hollow Square"),
    HOLLOW_CIRCLE("Hollow Circle"),
    V_FORMATION("V Formation");

    private final String displayName;

    FormationType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }

    public static FormationType fromString(String name) {
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return LINE;
        }
    }
}

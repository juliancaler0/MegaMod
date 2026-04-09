package com.ultra.megamod.feature.citizen.colonyblocks;

/**
 * Enum for gate types with associated hardness values.
 */
public enum GateType {
    IRON("iron", 10.0f),
    WOODEN("wooden", 7.0f);

    private final String name;
    private final float hardness;

    GateType(String name, float hardness) {
        this.name = name;
        this.hardness = hardness;
    }

    public String getName() {
        return name;
    }

    public float getHardness() {
        return hardness;
    }
}

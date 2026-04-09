package com.ultra.megamod.feature.citizen.data;

public enum PenState {
    NONE, PREVIEW, BUILDING, COMPLETE;

    public static PenState fromString(String s) {
        try {
            return valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            return NONE;
        }
    }
}

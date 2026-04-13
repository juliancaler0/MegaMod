package com.ultra.megamod.lib.accessories.fabric;

/**
 * Adapter for net.fabricmc.fabric.api.util.TriState.
 * Three-valued boolean: TRUE, FALSE, DEFAULT.
 */
public enum TriState {
    TRUE,
    FALSE,
    DEFAULT;

    public boolean isTrue() {
        return this == TRUE;
    }

    public boolean isFalse() {
        return this == FALSE;
    }

    public boolean isDefault() {
        return this == DEFAULT;
    }

    public boolean orElse(boolean fallback) {
        return switch (this) {
            case TRUE -> true;
            case FALSE -> false;
            case DEFAULT -> fallback;
        };
    }

    public static TriState of(boolean value) {
        return value ? TRUE : FALSE;
    }
}

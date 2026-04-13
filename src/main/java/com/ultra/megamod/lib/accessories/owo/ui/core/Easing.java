package com.ultra.megamod.lib.accessories.owo.ui.core;

/**
 * Adapter stub for io.wispforest.owo.ui.core.Easing.
 */
public enum Easing {
    LINEAR,
    QUADRATIC,
    CUBIC,
    SINE,
    EXPO;

    public float apply(float t) {
        return switch (this) {
            case LINEAR -> t;
            case QUADRATIC -> t * t;
            case CUBIC -> t * t * t;
            case SINE -> (float) Math.sin(t * Math.PI / 2);
            case EXPO -> t == 0 ? 0 : (float) Math.pow(2, 10 * (t - 1));
        };
    }
}

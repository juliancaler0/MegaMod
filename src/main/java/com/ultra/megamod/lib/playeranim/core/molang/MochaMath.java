package com.ultra.megamod.lib.playeranim.core.molang;

/**
 * Stub replacement for team.unnamed.mocha MochaMath.
 * Provides basic math utilities without Molang dependency.
 */
public final class MochaMath {
    public static final float PI = (float) Math.PI;

    public static float sqrt(float value) {
        return (float) Math.sqrt(value);
    }

    public static float pow(float base, float exponent) {
        return (float) Math.pow(base, exponent);
    }

    public static float lerp(float start, float end, float delta) {
        return start + delta * (end - start);
    }

    /**
     * Degrees to radians.
     */
    public static float d2r(float degrees) {
        return (float) Math.toRadians(degrees);
    }

    private MochaMath() {}
}

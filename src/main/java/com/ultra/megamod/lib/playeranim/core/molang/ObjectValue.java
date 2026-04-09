package com.ultra.megamod.lib.playeranim.core.molang;

/**
 * Stub replacement for Mocha ObjectValue.
 */
public final class ObjectValue {
    /**
     * Functional interface for easing functions that take three float parameters.
     */
    @FunctionalInterface
    public interface FloatFunction3 {
        float apply(float a, float b, float c);
    }

    private ObjectValue() {}
}

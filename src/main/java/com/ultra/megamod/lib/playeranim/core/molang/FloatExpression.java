package com.ultra.megamod.lib.playeranim.core.molang;

/**
 * Stub replacement for Mocha FloatExpression / NumberValue.
 * A constant float expression.
 */
public record FloatExpression(float value) implements Expression {
    public static final FloatExpression ZERO = new FloatExpression(0f);
    public static final FloatExpression ONE = new FloatExpression(1f);

    public static FloatExpression of(float value) {
        if (value == 0f) return ZERO;
        if (value == 1f) return ONE;
        return new FloatExpression(value);
    }

    @Override
    public float eval() {
        return value;
    }
}

package com.ultra.megamod.lib.playeranim.core.molang;

/**
 * Stub: checks if an expression is a constant (non-dynamic) value.
 * Since we have no Molang, all expressions are constant.
 */
public final class IsConstantExpression {
    public static boolean test(Expression expr) {
        return expr instanceof FloatExpression;
    }

    private IsConstantExpression() {}
}

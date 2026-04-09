package com.ultra.megamod.lib.playeranim.core.molang;

import java.util.List;

/**
 * Stub replacement for team.unnamed.mocha MochaEngine.
 * Evaluates Expression lists by returning their float values.
 * The type parameter is kept for API compatibility but unused.
 */
public class MochaEngine<T> {

    /**
     * Evaluate a list of expressions, returning the value of the first one
     * (standard behavior for keyframe values which are single-element lists).
     */
    public float eval(List<Expression> expressions) {
        if (expressions == null || expressions.isEmpty()) return 0f;
        return expressions.getFirst().eval();
    }

    /**
     * Evaluate a single expression.
     */
    public float eval(Expression expression) {
        if (expression == null) return 0f;
        return expression.eval();
    }
}

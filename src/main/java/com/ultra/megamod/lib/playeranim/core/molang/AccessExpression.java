package com.ultra.megamod.lib.playeranim.core.molang;

/**
 * Stub replacement for Mocha AccessExpression (property access like "pal.disabled").
 */
public record AccessExpression(Expression object, String property) implements Expression {
    @Override
    public float eval() {
        return 0f;
    }
}

package com.ultra.megamod.lib.playeranim.core.molang;

/**
 * Stub replacement for Mocha IdentifierExpression.
 */
public record IdentifierExpression(String name) implements Expression {
    @Override
    public float eval() {
        return 0f;
    }
}

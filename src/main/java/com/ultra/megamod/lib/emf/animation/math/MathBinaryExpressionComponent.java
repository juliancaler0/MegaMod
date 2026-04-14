package com.ultra.megamod.lib.emf.animation.math;

/**
 * Binary operator AST node: {@code left OP right}.
 * <p>
 * {@link #getOptimizedExpression(MathComponent, MathOperator, MathComponent)} folds
 * all-constant subtrees to a {@link MathConstant}. Ported 1:1 from upstream.
 */
public class MathBinaryExpressionComponent extends MathValue {

    private final MathComponent first;
    private final MathOperator action;
    private final MathComponent second;

    private MathBinaryExpressionComponent(MathComponent first, MathOperator action, MathComponent second) {
        this.first = first;
        this.action = action;
        this.second = second;
    }

    public static MathComponent getOptimizedExpression(MathComponent first, MathOperator action, MathComponent second) {
        MathBinaryExpressionComponent component = new MathBinaryExpressionComponent(first, action, second);
        if (component.first.isConstant() && component.second.isConstant()) {
            // all-constant subtree: precompute and emit a constant
            return new MathConstant(component.getResult(), false);
        }
        return component;
    }

    @Override
    protected ResultSupplier getResultSupplier() {
        return null;
    }

    @Override
    public float getResult() {
        float value = action.execute(first, second);
        return isNegative ? -value : value;
    }

    @Override
    public String toString() {
        return "[oExp:{" + first + ", " + action + ", " + second + "}=" + getResult() + "]";
    }
}

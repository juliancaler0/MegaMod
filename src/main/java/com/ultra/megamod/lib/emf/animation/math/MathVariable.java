package com.ultra.megamod.lib.emf.animation.math;

import com.ultra.megamod.lib.emf.animation.EmfParseContext;
import com.ultra.megamod.lib.emf.animation.math.variables.VariableRegistry;

/**
 * AST node that resolves a named variable at evaluation time.
 * <p>
 * Upstream's {@code MathVariable} delegates the actual lookup into
 * {@link VariableRegistry}; we preserve that split.
 */
public class MathVariable extends MathValue {

    private final ResultSupplier resultSupplier;
    private final String name;

    public MathVariable(String variableName, boolean isNegative, ResultSupplier supplier) {
        super(isNegative);
        resultSupplier = supplier;
        name = variableName;
    }

    public MathVariable(String variableName, ResultSupplier supplier) {
        resultSupplier = supplier;
        name = variableName;
    }

    public static MathComponent getOptimizedVariable(String variableName, boolean isNegative, EmfParseContext ctx) {
        if (variableName.startsWith("-")) {
            // double-negative: unwrap and flip
            return VariableRegistry.getInstance().getVariable(variableName.substring(1), true, ctx);
        }
        return VariableRegistry.getInstance().getVariable(variableName, isNegative, ctx);
    }

    @Override
    protected ResultSupplier getResultSupplier() {
        return resultSupplier;
    }

    @Override
    public String toString() {
        return "variable[" + name + "]=" + getResult();
    }
}

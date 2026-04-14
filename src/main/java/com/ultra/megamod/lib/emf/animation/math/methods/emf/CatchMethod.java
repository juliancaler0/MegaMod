package com.ultra.megamod.lib.emf.animation.math.methods.emf;

import com.ultra.megamod.lib.emf.animation.EmfParseContext;
import com.ultra.megamod.lib.emf.animation.math.EMFMathException;
import com.ultra.megamod.lib.emf.animation.math.MathComponent;
import com.ultra.megamod.lib.emf.animation.math.MathExpressionParser;
import com.ultra.megamod.lib.emf.animation.math.MathMethod;
import com.ultra.megamod.lib.emf.utils.EMFUtils;

import java.util.List;

/**
 * {@code catch(expr, fallback[, debug])} — evaluates {@code expr}, substituting
 * {@code fallback} on NaN or exception. Ported 1:1.
 */
public class CatchMethod extends MathMethod {

    public CatchMethod(final List<String> args, final boolean isNegative, final EmfParseContext parseCtx) throws EMFMathException {
        super(isNegative, parseCtx, args.size());

        final MathComponent x = MathExpressionParser.getOptimizedExpression(args.get(0), false, parseCtx);
        final MathComponent c = MathExpressionParser.getOptimizedExpression(args.get(1), false, parseCtx);

        final String print = (args.size() == 3 && !args.get(2).isBlank()) ? args.get(2) : null;

        setSupplierAndOptimize(() -> {
            try {
                float result = x.getResult();
                if (Float.isNaN(result)) {
                    if (print != null) EMFUtils.log("print: catch(" + print + ") found NaN in x.");
                    return c.getResult();
                }
                return result;
            } catch (Exception e) {
                if (print != null) EMFUtils.log("print: catch(" + print + ") found Exception in x: " + e.getMessage());
                return c.getResult();
            }
        }, List.of(x, c));
    }

    @Override
    protected boolean canOptimizeForConstantArgs() {
        return false;
    }

    @Override
    protected boolean hasCorrectArgCount(final int argCount) {
        return argCount == 2 || argCount == 3;
    }
}

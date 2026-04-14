package com.ultra.megamod.lib.emf.animation.math.methods.optifine;

import com.ultra.megamod.lib.emf.animation.EmfParseContext;
import com.ultra.megamod.lib.emf.animation.EmfRuntime;
import com.ultra.megamod.lib.emf.animation.math.EMFMathException;
import com.ultra.megamod.lib.emf.animation.math.MathComponent;
import com.ultra.megamod.lib.emf.animation.math.MathExpressionParser;
import com.ultra.megamod.lib.emf.animation.math.MathMethod;
import com.ultra.megamod.lib.emf.animation.math.MathValue;
import com.ultra.megamod.lib.emf.utils.EMFUtils;

import java.util.List;

/**
 * {@code printb(...)} — boolean variant of {@link PrintMethod}. Ported 1:1.
 */
public class PrintBMethod extends MathMethod {

    private int printCount = 0;

    public PrintBMethod(final List<String> args, final boolean isNegative, final EmfParseContext parseCtx) throws EMFMathException {
        super(isNegative, parseCtx, args.size());

        if (args.size() == 1) {
            final String expressionStr = args.get(0);
            final MathComponent x = MathExpressionParser.getOptimizedExpression(expressionStr, false, parseCtx);
            setSupplierAndOptimize(() -> {
                float xVal = x.getResult();
                if (!EmfRuntime.current().isValidationPhase()) {
                    EMFUtils.log("printb: [" + expressionStr + "] = " + MathValue.toBoolean(xVal));
                }
                return xVal;
            });
            return;
        }

        final String id = args.get(0);
        final MathComponent n = MathExpressionParser.getOptimizedExpression(args.get(1), false, parseCtx);
        final MathComponent x = MathExpressionParser.getOptimizedExpression(args.get(2), false, parseCtx);

        setSupplierAndOptimize(() -> {
            float xVal = x.getResult();
            if (!EmfRuntime.current().isValidationPhase()
                    && getPrintCount() % Math.max(1, (int) n.getResult()) == 0) {
                EMFUtils.log("printb: [" + id + "] = " + MathValue.toBoolean(xVal));
            }
            return xVal;
        });
    }

    private int getPrintCount() {
        printCount++;
        return printCount;
    }

    @Override
    protected boolean canOptimizeForConstantArgs() {
        return false;
    }

    @Override
    protected boolean hasCorrectArgCount(final int argCount) {
        return argCount == 3 || argCount == 1;
    }
}

package com.ultra.megamod.lib.emf.animation.math.methods.optifine;

import com.ultra.megamod.lib.emf.animation.EmfParseContext;
import com.ultra.megamod.lib.emf.animation.EmfRuntime;
import com.ultra.megamod.lib.emf.animation.math.EMFMathException;
import com.ultra.megamod.lib.emf.animation.math.MathComponent;
import com.ultra.megamod.lib.emf.animation.math.MathExpressionParser;
import com.ultra.megamod.lib.emf.animation.math.MathMethod;
import com.ultra.megamod.lib.emf.utils.EMFUtils;

import java.util.List;

/**
 * {@code print(expr)} or {@code print(id, frequency, expr)} — debug helper that
 * returns the inner value while logging it periodically. Ported 1:1 from upstream,
 * with the isPaused() check delegated to the runtime (Phase E will wire it to
 * {@code Minecraft.getInstance().isPaused()}).
 */
public class PrintMethod extends MathMethod {

    private int printCount = 0;

    public PrintMethod(final List<String> args, final boolean isNegative, final EmfParseContext parseCtx) throws EMFMathException {
        super(isNegative, parseCtx, args.size());

        if (args.size() == 1) {
            final String expressionStr = args.get(0);
            final MathComponent x = MathExpressionParser.getOptimizedExpression(expressionStr, false, parseCtx);
            setSupplierAndOptimize(() -> {
                float xVal = x.getResult();
                if (!EmfRuntime.current().isValidationPhase()) {
                    EMFUtils.log("print: [" + expressionStr + "] = " + xVal);
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
                EMFUtils.log("print: [" + id + "] = " + xVal);
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

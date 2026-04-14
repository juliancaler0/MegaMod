package com.ultra.megamod.lib.emf.animation.math.methods.simple;

import com.ultra.megamod.lib.emf.animation.EmfParseContext;
import com.ultra.megamod.lib.emf.animation.math.EMFMathException;
import com.ultra.megamod.lib.emf.animation.math.MathComponent;
import com.ultra.megamod.lib.emf.animation.math.MathMethod;
import com.ultra.megamod.lib.emf.animation.math.methods.MethodRegistry;

import java.util.List;
import java.util.function.Function;

/**
 * Adapter for single-argument float to float method implementations.
 * Ported 1:1 from upstream.
 */
public class FunctionMethods extends MathMethod {

    protected FunctionMethods(final List<String> args,
                              final boolean isNegative,
                              final EmfParseContext parseCtx,
                              final Function<Float, Float> function) throws EMFMathException {
        super(isNegative, parseCtx, args.size());
        MathComponent arg = parseArg(args.get(0), parseCtx);
        setSupplierAndOptimize(() -> function.apply(arg.getResult()), arg);
    }

    public static MethodRegistry.MethodFactory makeFactory(final String methodName, final Function<Float, Float> function) {
        return (args, isNegative, parseCtx) -> {
            try {
                return new FunctionMethods(args, isNegative, parseCtx, function);
            } catch (Exception e) {
                throw new EMFMathException("Failed to create " + methodName + "() method, because: " + e);
            }
        };
    }

    @Override
    protected boolean hasCorrectArgCount(final int argCount) {
        return argCount == 1;
    }
}

package com.ultra.megamod.lib.emf.animation.math.methods.simple;

import com.ultra.megamod.lib.emf.animation.EmfParseContext;
import com.ultra.megamod.lib.emf.animation.math.EMFMathException;
import com.ultra.megamod.lib.emf.animation.math.MathComponent;
import com.ultra.megamod.lib.emf.animation.math.MathMethod;
import com.ultra.megamod.lib.emf.animation.math.methods.MethodRegistry;

import java.util.List;
import java.util.function.BiFunction;

/**
 * Adapter for two-argument float→float method implementations.
 * Ported 1:1 from upstream.
 */
public class BiFunctionMethods extends MathMethod {

    protected BiFunctionMethods(final List<String> args,
                                final boolean isNegative,
                                final EmfParseContext parseCtx,
                                final BiFunction<Float, Float, Float> function) throws EMFMathException {
        super(isNegative, parseCtx, args.size());
        MathComponent arg = parseArg(args.get(0), parseCtx);
        MathComponent arg2 = parseArg(args.get(1), parseCtx);
        setSupplierAndOptimize(() -> function.apply(arg.getResult(), arg2.getResult()), List.of(arg, arg2));
    }

    public static MethodRegistry.MethodFactory makeFactory(final String methodName, final BiFunction<Float, Float, Float> function) {
        return (args, isNegative, parseCtx) -> {
            try {
                return new BiFunctionMethods(args, isNegative, parseCtx, function);
            } catch (Exception e) {
                throw new EMFMathException("Failed to create " + methodName + "() method, because: " + e);
            }
        };
    }

    @Override
    protected boolean hasCorrectArgCount(final int argCount) {
        return argCount == 2;
    }
}

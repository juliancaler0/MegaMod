package com.ultra.megamod.lib.emf.models.animation.math.methods.simple;

import com.ultra.megamod.lib.emf.models.animation.EMFAnimation;
import com.ultra.megamod.lib.emf.models.animation.math.EMFMathException;
import com.ultra.megamod.lib.emf.models.animation.math.MathComponent;
import com.ultra.megamod.lib.emf.models.animation.math.MathMethod;
import com.ultra.megamod.lib.emf.models.animation.math.methods.MethodRegistry;

import java.util.List;
import java.util.function.Function;

public class FunctionMethods extends MathMethod {


    protected FunctionMethods(final List<String> args,
                              final boolean isNegative,
                              final EMFAnimation calculationInstance,
                              final Function<Float, Float> function) throws EMFMathException {
        super(isNegative, calculationInstance, args.size());

        var arg = parseArg(args.get(0), calculationInstance);
        setSupplierAndOptimize(() -> function.apply(arg.getResult()), arg);
    }

    public static MethodRegistry.MethodFactory makeFactory(final String methodName, final Function<Float, Float> function) {
        return (args, isNegative, calculationInstance) -> {
            try {
                return new FunctionMethods(args, isNegative, calculationInstance, function);
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

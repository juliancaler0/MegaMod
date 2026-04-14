package com.ultra.megamod.lib.emf.animation.math.methods.simple;

import com.ultra.megamod.lib.emf.animation.EmfParseContext;
import com.ultra.megamod.lib.emf.animation.math.EMFMathException;
import com.ultra.megamod.lib.emf.animation.math.MathComponent;
import com.ultra.megamod.lib.emf.animation.math.MathMethod;
import com.ultra.megamod.lib.emf.animation.math.methods.MethodRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Adapter for variable-arity method implementations. Also hosts spline helpers
 * ({@code catmullrom}, {@code quadbezier}, {@code cubicbezier}, {@code hermite}).
 * Ported 1:1 from upstream.
 */
public class MultiFunctionMethods extends MathMethod {

    private final int argCount;

    public MultiFunctionMethods(final List<String> args,
                                final boolean isNegative,
                                final EmfParseContext parseCtx,
                                final Function<List<Float>, Float> function) throws EMFMathException {
        super(isNegative, parseCtx, args.size());
        argCount = args.size();
        List<MathComponent> parsedArgs = parseAllArgs(args, parseCtx);
        setSupplierAndOptimize(() -> {
            List<Float> results = new ArrayList<>();
            for (MathComponent parsedArg : parsedArgs) {
                results.add(parsedArg.getResult());
            }
            return function.apply(results);
        }, parsedArgs);
    }

    public static MethodRegistry.MethodFactory makeFactory(final String methodName, final Function<List<Float>, Float> function) {
        return (args, isNegative, parseCtx) -> {
            try {
                return new MultiFunctionMethods(args, isNegative, parseCtx, function);
            } catch (Exception e) {
                throw new EMFMathException("Failed to create " + methodName + "() method, because: " + e);
            }
        };
    }

    public static float quadraticBezier(float t, float p0, float p1, float p2) {
        float oneMinusT = 1f - t;
        return oneMinusT * oneMinusT * p0 + 2f * oneMinusT * t * p1 + t * t * p2;
    }

    public static float cubicBezier(float t, float p0, float p1, float p2, float p3) {
        float oneMinusT = 1f - t;
        float oneMinusTSquared = oneMinusT * oneMinusT;
        float tSquared = t * t;
        return oneMinusTSquared * oneMinusT * p0 + 3f * oneMinusTSquared * t * p1 + 3f * oneMinusT * tSquared * p2 + tSquared * t * p3;
    }

    public static float hermiteInterpolation(float t, float p0, float p1, float m0, float m1) {
        float tSquared = t * t;
        float tCubed = tSquared * t;
        float h00 = 2 * tCubed - 3 * tSquared + 1;
        float h10 = tCubed - 2 * tSquared + t;
        float h01 = -2 * tCubed + 3 * tSquared;
        float h11 = tCubed - tSquared;
        return h00 * p0 + h10 * m0 + h01 * p1 + h11 * m1;
    }

    public static float catmullRom(float t, float p0, float p1, float p2, float p3) {
        // matches Mth.catmullrom
        return 0.5f * (
                (2f * p1)
                + (-p0 + p2) * t
                + (2f * p0 - 5f * p1 + 4f * p2 - p3) * t * t
                + (-p0 + 3f * p1 - 3f * p2 + p3) * t * t * t
        );
    }

    @Override
    protected boolean hasCorrectArgCount(final int argCount) {
        return argCount == this.argCount;
    }
}

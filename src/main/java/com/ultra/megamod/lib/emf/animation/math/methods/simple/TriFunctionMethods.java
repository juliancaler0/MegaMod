package com.ultra.megamod.lib.emf.animation.math.methods.simple;

import com.ultra.megamod.lib.emf.animation.EmfParseContext;
import com.ultra.megamod.lib.emf.animation.math.EMFMathException;
import com.ultra.megamod.lib.emf.animation.math.MathComponent;
import com.ultra.megamod.lib.emf.animation.math.MathMethod;
import com.ultra.megamod.lib.emf.animation.math.methods.MethodRegistry;

import java.util.List;

/**
 * Adapter for three-argument method implementations. Also hosts the 24+ easing
 * helpers that share this arity ({@code easeInQuad}, {@code easeInOutCubic}, ...).
 * Ported 1:1 from upstream.
 */
public class TriFunctionMethods extends MathMethod {

    protected TriFunctionMethods(final List<String> args,
                                 final boolean isNegative,
                                 final EmfParseContext parseCtx,
                                 final TriFunction<Float, Float, Float, Float> function) throws EMFMathException {
        super(isNegative, parseCtx, args.size());
        MathComponent arg = parseArg(args.get(0), parseCtx);
        MathComponent arg2 = parseArg(args.get(1), parseCtx);
        MathComponent arg3 = parseArg(args.get(2), parseCtx);
        setSupplierAndOptimize(() -> function.apply(arg.getResult(), arg2.getResult(), arg3.getResult()),
                List.of(arg, arg2, arg3));
    }

    public static MethodRegistry.MethodFactory makeFactory(final String methodName, final TriFunction<Float, Float, Float, Float> function) {
        return (args, isNegative, parseCtx) -> {
            try {
                return new TriFunctionMethods(args, isNegative, parseCtx, function);
            } catch (Exception e) {
                throw new EMFMathException("Failed to create " + methodName + "() method, because: " + e);
            }
        };
    }

    // ---------- Easing helpers (ported 1:1 from upstream) ----------

    public static float easeInQuad(float t, float start, float end) {
        float delta = end - start;
        return start + delta * t * t;
    }

    public static float easeOutQuad(float t, float start, float end) {
        float delta = end - start;
        return start + delta * -t * (t - 2);
    }

    public static float easeInOutQuad(float t, float start, float end) {
        float delta = end - start;
        if (t < 0.5f) return start + delta * (2 * t * t);
        return start + delta * (-2 * t * (t - 2) - 1);
    }

    public static float easeInCubic(float t, float start, float end) {
        float delta = end - start;
        return start + delta * t * t * t;
    }

    public static float easeOutCubic(float t, float start, float end) {
        float delta = end - start;
        float tm1 = t - 1;
        return start + delta * tm1 * tm1 * tm1 + 1;
    }

    public static float easeInOutCubic(float t, float start, float end) {
        float delta = end - start;
        if (t < 0.5f) return start + delta * 4 * t * t * t;
        float tm1 = t - 1;
        return start + delta * tm1 * (2 * tm1 * tm1 + 2) + 1;
    }

    public static float easeInQuart(float t, float start, float end) {
        float delta = end - start;
        return start + delta * t * t * t * t;
    }

    public static float easeOutQuart(float t, float start, float end) {
        float delta = end - start;
        float tm1 = t - 1;
        return start + delta * tm1 * tm1 * tm1 * tm1 + 1;
    }

    public static float easeInOutQuart(float t, float start, float end) {
        float delta = end - start;
        if (t < 0.5f) return start + delta * 8 * t * t * t * t;
        float tm1 = t - 1;
        return start + delta * tm1 * (8 * tm1 * tm1 * tm1 + 1) + 1;
    }

    public static float easeInQuint(float t, float start, float end) {
        float delta = end - start;
        return start + delta * t * t * t * t * t;
    }

    public static float easeOutQuint(float t, float start, float end) {
        float delta = end - start;
        float tm1 = t - 1;
        return start + delta * tm1 * tm1 * tm1 * tm1 * tm1 + 1;
    }

    public static float easeInOutQuint(float t, float start, float end) {
        float delta = end - start;
        if (t < 0.5f) return start + delta * 16 * t * t * t * t * t;
        float tm1 = t - 1;
        return start + delta * tm1 * (16 * tm1 * tm1 * tm1 * tm1 + 1) + 1;
    }

    public static float easeInSine(float t, float start, float end) {
        float delta = end - start;
        return start + delta * (1 - (float) Math.cos(t * Math.PI / 2));
    }

    public static float easeOutSine(float t, float start, float end) {
        float delta = end - start;
        return start + delta * (float) Math.sin(t * Math.PI / 2);
    }

    public static float easeInOutSine(float t, float start, float end) {
        float delta = end - start;
        return start + delta * (float) (-0.5 * (Math.cos(Math.PI * t) - 1));
    }

    public static float easeInExpo(float t, float start, float end) {
        float delta = end - start;
        return start + delta * (float) Math.pow(2, 10 * (t - 1));
    }

    public static float easeOutExpo(float t, float start, float end) {
        float delta = end - start;
        return start + delta * (float) (-Math.pow(2, -10 * t) + 1);
    }

    public static float easeInOutExpo(float t, float start, float end) {
        float delta = end - start;
        if (t < 1) return start + delta * (float) (0.5 * Math.pow(2, 10 * (t - 1)));
        return start + delta * (float) (0.5 * (-Math.pow(2, -10 * (t - 1)) + 2));
    }

    public static float easeInCirc(float t, float start, float end) {
        float delta = end - start;
        return start + delta * (float) -(Math.sqrt(1 - t * t) - 1);
    }

    public static float easeOutCirc(float t, float start, float end) {
        float delta = end - start;
        float tm1 = t - 1;
        return start + delta * (float) Math.sqrt(1 - tm1 * tm1);
    }

    public static float easeInOutCirc(float t, float start, float end) {
        float delta = end - start;
        float t2 = t * 2;
        if (t2 < 1) return start + delta * (float) (-0.5 * (Math.sqrt(1 - t2 * t2) - 1));
        float t2m2 = t2 - 2;
        return start + delta * (float) (0.5 * (Math.sqrt(1 - t2m2 * t2m2) + 1));
    }

    public static float easeInElastic(float t, float start, float end) {
        float delta = end - start;
        float tm1 = t - 1;
        return start + delta * (float) (-Math.pow(2, 10 * tm1) * Math.sin((tm1 - 0.3 / 4) * (2 * Math.PI) / 0.3));
    }

    public static float easeOutElastic(float t, float start, float end) {
        float delta = end - start;
        return start + delta * (float) (Math.pow(2, -10 * t) * Math.sin((t - 0.3 / 4) * (2 * Math.PI) / 0.3) + 1);
    }

    public static float easeInOutElastic(float t, float start, float end) {
        float delta = end - start;
        if (t < 0.5f) {
            float tm1 = t - 1;
            return start + delta * (float) (-0.5 * Math.pow(2, 10 * tm1) * Math.sin((tm1 - 0.225 / 4) * (2 * Math.PI) / 0.45));
        }
        float tm1 = t - 1;
        return start + delta * (float) (0.5 * Math.pow(2, -10 * tm1) * Math.sin((tm1 - 0.225 / 4) * (2 * Math.PI) / 0.45) + 1);
    }

    public static float easeInBounce(float t, float start, float end) {
        float delta = end - start;
        return start + delta * (1 - easeOutBounce(1 - t, 0, 1));
    }

    public static float easeOutBounce(float t, float start, float end) {
        float delta = end - start;
        if (t < (1 / 2.75f)) {
            return start + delta * (7.5625f * t * t);
        } else if (t < (2 / 2.75f)) {
            float tt = t - (1.5f / 2.75f);
            return start + delta * (7.5625f * tt * tt + 0.75f);
        } else if (t < (2.5f / 2.75f)) {
            float tt = t - (2.25f / 2.75f);
            return start + delta * (7.5625f * tt * tt + 0.9375f);
        } else {
            float tt = t - (2.625f / 2.75f);
            return start + delta * (7.5625f * tt * tt + 0.984375f);
        }
    }

    public static float easeInOutBounce(float t, float start, float end) {
        float delta = end - start;
        if (t < 0.5f) return start + delta * (0.5f * easeInBounce(t * 2, 0, 1));
        return start + delta * (0.5f * easeOutBounce(t * 2 - 1, 0, 1) + 0.5f);
    }

    public static float easeInBack(float t, float start, float end) {
        float delta = end - start;
        return start + delta * (t * t * (2.70158f * t - 1.70158f));
    }

    public static float easeOutBack(float t, float start, float end) {
        float delta = end - start;
        float tm1 = t - 1;
        return start + delta * (tm1 * tm1 * (2.70158f * tm1 + 1.70158f) + 1);
    }

    public static float easeInOutBack(float t, float start, float end) {
        float delta = end - start;
        if (t < 0.5f) return start + delta * (t * t * (7 * t - 2.5f) * 2);
        float tm1 = t - 1;
        return start + delta * ((tm1 * tm1 * (7 * tm1 + 2.5f) + 2) * 2);
    }

    @Override
    protected boolean hasCorrectArgCount(final int argCount) {
        return argCount == 3;
    }
}

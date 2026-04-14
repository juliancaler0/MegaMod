package com.ultra.megamod.lib.emf.animation.math.methods.optifine;

import com.ultra.megamod.lib.emf.animation.EmfParseContext;
import com.ultra.megamod.lib.emf.animation.math.EMFMathException;
import com.ultra.megamod.lib.emf.animation.math.MathComponent;
import com.ultra.megamod.lib.emf.animation.math.MathMethod;

import java.util.List;

/**
 * {@code random([seed])} — seeded pseudo-random in [0, 1).
 * Uses OptiFine's deterministic int hash when a seed is supplied. Ported 1:1.
 */
public class RandomMethod extends MathMethod {

    private final boolean hasSeed;

    public RandomMethod(final List<String> args, final boolean isNegative, final EmfParseContext parseCtx) throws EMFMathException {
        super(isNegative, parseCtx, args.size());

        hasSeed = args.size() == 1 && !args.get(0).isBlank();
        if (hasSeed) {
            MathComponent arg = parseArg(args.get(0), parseCtx);
            setSupplierAndOptimize(() -> nextValue(arg.getResult()), arg);
        } else {
            setSupplierAndOptimize(this::nextValue);
        }
    }

    protected float nextValue(float seed) {
        int hash = optifineIntHash(Float.floatToIntBits(seed));
        return (float) Math.abs(hash) / 2.14748365E9F;
    }

    protected float nextValue() {
        return (float) Math.random();
    }

    public static int optifineIntHash(int x) {
        x = x ^ 61 ^ x >> 16;
        x += x << 3;
        x ^= x >> 4;
        x *= 668265261;
        return x ^ x >> 15;
    }

    @Override
    protected boolean canOptimizeForConstantArgs() {
        return hasSeed;
    }

    @Override
    protected boolean hasCorrectArgCount(final int argCount) {
        return argCount == 1 || argCount == 0;
    }
}

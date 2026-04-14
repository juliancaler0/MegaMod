package com.ultra.megamod.lib.emf.animation.math.methods.emf;

import com.ultra.megamod.lib.emf.animation.EmfParseContext;
import com.ultra.megamod.lib.emf.animation.math.EMFMathException;
import com.ultra.megamod.lib.emf.animation.math.MathValue;
import com.ultra.megamod.lib.emf.animation.math.methods.optifine.RandomMethod;

import java.util.List;

/**
 * {@code randomb([seed])} — boolean variant of {@code random}. Ported 1:1.
 */
public class RandomBMethod extends RandomMethod {

    public RandomBMethod(final List<String> args, final boolean isNegative, final EmfParseContext parseCtx) throws EMFMathException {
        super(args, isNegative, parseCtx);
    }

    @Override
    protected float nextValue(float seed) {
        return MathValue.fromBoolean(super.nextValue(seed) >= 0.5f);
    }

    @Override
    protected float nextValue() {
        return MathValue.fromBoolean(super.nextValue() >= 0.5f);
    }
}

package com.ultra.megamod.lib.emf.animation.math.methods.emf;

import com.ultra.megamod.lib.emf.animation.EmfParseContext;
import com.ultra.megamod.lib.emf.animation.math.EMFMathException;
import com.ultra.megamod.lib.emf.animation.math.MathValue;
import com.ultra.megamod.lib.emf.animation.math.methods.optifine.IfMethod;

import java.util.List;

/**
 * {@code ifb(...)} — boolean variant of {@code if}: validates that the chosen branch
 * yielded a boolean sentinel. Ported 1:1.
 */
public class IfBMethod extends IfMethod {

    public IfBMethod(final List<String> args, final boolean isNegative, final EmfParseContext parseCtx) throws EMFMathException {
        super(args, isNegative, parseCtx);

        final ResultSupplier current = supplier;
        supplier = () -> MathValue.validateBoolean(current.get());
        if (optimizedAlternativeToThis != null) {
            final var current2 = optimizedAlternativeToThis;
            optimizedAlternativeToThis = () -> MathValue.validateBoolean(current2.getResult());
        }
    }
}

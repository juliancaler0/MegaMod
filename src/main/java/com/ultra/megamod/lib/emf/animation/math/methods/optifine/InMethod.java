package com.ultra.megamod.lib.emf.animation.math.methods.optifine;

import com.ultra.megamod.lib.emf.animation.EmfParseContext;
import com.ultra.megamod.lib.emf.animation.math.EMFMathException;
import com.ultra.megamod.lib.emf.animation.math.MathComponent;
import com.ultra.megamod.lib.emf.animation.math.MathMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code in(x, a, b, c...)} — returns TRUE if x equals any of the following args.
 * Ported 1:1.
 */
public class InMethod extends MathMethod {

    public InMethod(final List<String> args, final boolean isNegative, final EmfParseContext parseCtx) throws EMFMathException {
        super(isNegative, parseCtx, args.size());
        List<MathComponent> parsedArgs = parseAllArgs(args, parseCtx);

        MathComponent x = parsedArgs.get(0);
        List<MathComponent> vals = new ArrayList<>(parsedArgs);
        vals.remove(0);

        setSupplierAndOptimize(() -> {
            float X = x.getResult();
            for (MathComponent expression : vals) {
                if (expression.getResult() == X) return TRUE;
            }
            return FALSE;
        }, parsedArgs);
    }

    @Override
    protected boolean hasCorrectArgCount(final int argCount) {
        return argCount >= 2;
    }
}

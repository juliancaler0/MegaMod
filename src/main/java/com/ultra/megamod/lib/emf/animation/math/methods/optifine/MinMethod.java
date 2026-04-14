package com.ultra.megamod.lib.emf.animation.math.methods.optifine;

import com.ultra.megamod.lib.emf.animation.EmfParseContext;
import com.ultra.megamod.lib.emf.animation.math.EMFMathException;
import com.ultra.megamod.lib.emf.animation.math.MathComponent;
import com.ultra.megamod.lib.emf.animation.math.MathMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code min(a, b, ...)} — variadic minimum. Ported 1:1.
 */
public class MinMethod extends MathMethod {

    public MinMethod(final List<String> args, final boolean isNegative, final EmfParseContext parseCtx) throws EMFMathException {
        super(isNegative, parseCtx, args.size());
        List<MathComponent> parsedArgs = parseAllArgs(args, parseCtx);
        MathComponent initial = parsedArgs.get(0);
        List<MathComponent> theRest = new ArrayList<>(parsedArgs);
        theRest.remove(0);

        setSupplierAndOptimize(() -> {
            float min = initial.getResult();
            for (MathComponent parsedArg : theRest) {
                float val = parsedArg.getResult();
                if (val < min) min = val;
            }
            return min;
        }, parsedArgs);
    }

    @Override
    protected boolean hasCorrectArgCount(final int argCount) {
        return argCount >= 2;
    }
}

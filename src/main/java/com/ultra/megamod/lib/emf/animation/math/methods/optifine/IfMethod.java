package com.ultra.megamod.lib.emf.animation.math.methods.optifine;

import com.ultra.megamod.lib.emf.animation.EmfParseContext;
import com.ultra.megamod.lib.emf.animation.math.EMFMathException;
import com.ultra.megamod.lib.emf.animation.math.MathComponent;
import com.ultra.megamod.lib.emf.animation.math.MathMethod;
import com.ultra.megamod.lib.emf.animation.math.MathValue;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code if(cond1, then1, [cond2, then2, ...,] else)} — ladder of conditionals.
 * Arg count must be odd and &gt;= 3. Ported 1:1 from upstream (with the vanilla
 * {@code Tuple} replaced by a local record to keep Phase D self-contained).
 */
public class IfMethod extends MathMethod {

    public IfMethod(final List<String> args, final boolean isNegative, final EmfParseContext parseCtx) throws EMFMathException {
        super(isNegative, parseCtx, args.size());

        List<MathComponent> parsedArgs = parseAllArgs(args, parseCtx);

        if (parsedArgs.size() == 3) {
            MathComponent bool = parsedArgs.get(0);
            MathComponent tru = parsedArgs.get(1);
            MathComponent fals = parsedArgs.get(2);

            if (bool.isConstant()) {
                setOptimizedAlternativeToThis(MathValue.toBoolean(bool.getResult()) ? tru : fals);
            }

            setSupplierAndOptimize(
                    () -> MathValue.toBoolean(bool.getResult()) ? tru.getResult() : fals.getResult(),
                    parsedArgs);
        } else {
            List<Pair> ifSets = new ArrayList<>();
            MathComponent lastElse = parsedArgs.get(parsedArgs.size() - 1);

            for (int i = 0; i < parsedArgs.size() - 1; i += 2) {
                MathComponent condition = parsedArgs.get(i);
                MathComponent result = parsedArgs.get(i + 1);

                if (!condition.isConstant()) {
                    MathValue.toBoolean(condition.getResult()); // validate shape
                    ifSets.add(new Pair(condition, result));
                } else if (MathValue.toBoolean(condition.getResult())) {
                    lastElse = result;
                    break;
                }
            }
            if (ifSets.isEmpty()) {
                setOptimizedAlternativeToThis(lastElse);
            }

            final MathComponent finalElse = lastElse;
            setSupplierAndOptimize(() -> {
                for (Pair ifSet : ifSets) {
                    if (MathValue.toBoolean(ifSet.cond.getResult())) {
                        return ifSet.value.getResult();
                    }
                }
                return finalElse.getResult();
            }, parsedArgs);
        }
    }

    @Override
    protected boolean hasCorrectArgCount(final int argCount) {
        return argCount >= 3 && argCount % 2 == 1;
    }

    private record Pair(MathComponent cond, MathComponent value) {
    }
}

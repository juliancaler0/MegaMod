package com.ultra.megamod.lib.emf.models.animation.math.methods.optifine;

import com.ultra.megamod.lib.emf.models.animation.EMFAnimation;
import com.ultra.megamod.lib.emf.models.animation.math.EMFMathException;
import com.ultra.megamod.lib.emf.models.animation.math.MathComponent;
import com.ultra.megamod.lib.emf.models.animation.math.MathMethod;

import java.util.ArrayList;
import java.util.List;

public class InMethod extends MathMethod {


    public InMethod(final List<String> args, final boolean isNegative, final EMFAnimation calculationInstance) throws EMFMathException {
        super(isNegative, calculationInstance, args.size());

        var parsedArgs = parseAllArgs(args, calculationInstance);

        MathComponent x = parsedArgs.get(0);
        List<MathComponent> vals = new ArrayList<>(parsedArgs);
        vals.remove(0);

        setSupplierAndOptimize(() -> {
            float X = x.getResult();
            for (MathComponent expression :
                    vals) {
                if (expression.getResult() == X) {
                    return TRUE;
                }
            }
            return FALSE;
        }, parsedArgs);
    }


    @Override
    protected boolean hasCorrectArgCount(final int argCount) {
        return argCount >= 2;
    }

}

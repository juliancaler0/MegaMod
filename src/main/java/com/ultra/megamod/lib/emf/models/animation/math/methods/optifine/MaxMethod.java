package com.ultra.megamod.lib.emf.models.animation.math.methods.optifine;

import com.ultra.megamod.lib.emf.models.animation.EMFAnimation;
import com.ultra.megamod.lib.emf.models.animation.math.EMFMathException;
import com.ultra.megamod.lib.emf.models.animation.math.MathComponent;
import com.ultra.megamod.lib.emf.models.animation.math.MathMethod;

import java.util.ArrayList;
import java.util.List;

public class MaxMethod extends MathMethod {


    public MaxMethod(final List<String> args, final boolean isNegative, final EMFAnimation calculationInstance) throws EMFMathException {
        super(isNegative, calculationInstance, args.size());

        var parsedArgs = parseAllArgs(args, calculationInstance);
        var initial = parsedArgs.get(0);
        var theRest = new ArrayList<>(parsedArgs);
        theRest.remove(0);

        setSupplierAndOptimize(() -> {
            float max = initial.getResult();
            for (MathComponent parsedArg : theRest) {
                float val = parsedArg.getResult();
                if (val > max) {
                    max = val;
                }
            }
            return max;
        }, parsedArgs);
    }


    @Override
    protected boolean hasCorrectArgCount(final int argCount) {
        return argCount >= 2;
    }

}

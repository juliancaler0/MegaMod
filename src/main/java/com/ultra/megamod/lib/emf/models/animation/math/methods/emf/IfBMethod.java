package com.ultra.megamod.lib.emf.models.animation.math.methods.emf;

import com.ultra.megamod.lib.emf.models.animation.EMFAnimation;
import com.ultra.megamod.lib.emf.models.animation.math.EMFMathException;
import com.ultra.megamod.lib.emf.models.animation.math.MathValue;
import com.ultra.megamod.lib.emf.models.animation.math.methods.optifine.IfMethod;

import java.util.List;

public class IfBMethod extends IfMethod {


    public IfBMethod(final List<String> args, final boolean isNegative, final EMFAnimation calculationInstance) throws EMFMathException {
        super(args, isNegative, calculationInstance);

        var current = supplier;
        //validate output is boolean
        supplier = () -> MathValue.validateBoolean(current.get());
        if (optimizedAlternativeToThis != null) {
            var current2 = optimizedAlternativeToThis;
            optimizedAlternativeToThis = () -> MathValue.validateBoolean(current2.getResult());
        }
    }
}

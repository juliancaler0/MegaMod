package com.ultra.megamod.lib.azurelib.core.math.functions.easing.circ;

import com.ultra.megamod.lib.azurelib.core.math.IValue;
import com.ultra.megamod.lib.azurelib.core.math.functions.easing.EasingFunction;

public class EaseInCirc extends EasingFunction {

    public EaseInCirc(IValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    protected double ease(double t) {
        return 1 - Math.sqrt(1 - Math.pow(t, 2));
    }
}

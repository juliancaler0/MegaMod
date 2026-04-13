package com.ultra.megamod.lib.azurelib.core.math.functions.easing.quint;

import com.ultra.megamod.lib.azurelib.core.math.IValue;
import com.ultra.megamod.lib.azurelib.core.math.functions.easing.EasingFunction;

public class EaseInQuint extends EasingFunction {

    public EaseInQuint(IValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    protected double ease(double t) {
        return t * t * t * t * t;
    }
}

package com.ultra.megamod.lib.azurelib.core.math.functions.easing.sine;

import com.ultra.megamod.lib.azurelib.core.math.IValue;
import com.ultra.megamod.lib.azurelib.core.math.functions.easing.EasingFunction;

public class EaseOutSine extends EasingFunction {

    public EaseOutSine(IValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    protected double ease(double t) {
        return Math.sin((t * Math.PI) / 2);
    }
}

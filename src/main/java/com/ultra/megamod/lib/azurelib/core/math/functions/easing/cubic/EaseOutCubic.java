package com.ultra.megamod.lib.azurelib.core.math.functions.easing.cubic;

import com.ultra.megamod.lib.azurelib.core.math.IValue;
import com.ultra.megamod.lib.azurelib.core.math.functions.easing.EasingFunction;

public class EaseOutCubic extends EasingFunction {

    public EaseOutCubic(IValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    protected double ease(double t) {
        return 1 - Math.pow(1 - t, 3);
    }
}

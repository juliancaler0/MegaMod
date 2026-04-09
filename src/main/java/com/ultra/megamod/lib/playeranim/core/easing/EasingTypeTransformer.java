package com.ultra.megamod.lib.playeranim.core.easing;

import com.ultra.megamod.lib.playeranim.core.molang.Expression;
import com.ultra.megamod.lib.playeranim.core.molang.MochaEngine;
import com.ultra.megamod.lib.playeranim.core.molang.MochaMath;
import com.ultra.megamod.lib.playeranim.core.molang.ObjectValue;
import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@FunctionalInterface
public interface EasingTypeTransformer extends ObjectValue.FloatFunction3 {
    Float2FloatFunction buildTransformer(@Nullable Float value);

    default float apply(MochaEngine<?> env, float startValue, float endValue, float transitionLength, float lerpValue, @Nullable List<List<Expression>> easingArgs) {
        if (lerpValue >= 1) return endValue;
        if (Float.isNaN(lerpValue)) return startValue;

        Float easingVariable = null;
        if (easingArgs != null && !easingArgs.isEmpty()) {
            easingVariable = env.eval(easingArgs.getFirst());
        }
        return apply(startValue, endValue, easingVariable, lerpValue);
    }

    @Override
    default float apply(float startValue, float endValue, float lerpValue) {
        return apply(startValue, endValue, null, lerpValue);
    }

    default float apply(float startValue, float endValue, @Nullable Float easingValue, float lerpValue) {
        return MochaMath.lerp(startValue, endValue, buildTransformer(easingValue).apply(lerpValue));
    }
}

package com.ultra.megamod.lib.emf.models.animation.math;

public interface MathComponent {

    float getResult();

    default boolean isConstant() {
        return false;
    }
}

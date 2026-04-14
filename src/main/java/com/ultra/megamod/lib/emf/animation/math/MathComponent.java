package com.ultra.megamod.lib.emf.animation.math;

/**
 * Base node of the animation-expression AST.
 * <p>
 * All concrete components implement {@link #getResult()}. Constants can advertise via
 * {@link #isConstant()} so the parser can fold them at compile time.
 * <p>
 * Ported 1:1 from {@code traben.entity_model_features.models.animation.math.MathComponent}.
 */
public interface MathComponent {

    float getResult();

    default boolean isConstant() {
        return false;
    }
}

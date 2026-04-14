package com.ultra.megamod.lib.emf.animation.math;

import com.ultra.megamod.lib.emf.EMFException;

/**
 * Thrown by the math parser when an expression cannot be compiled or evaluated.
 * Ported 1:1 from {@code traben.entity_model_features.models.animation.math.EMFMathException}.
 */
public class EMFMathException extends EMFException {
    public EMFMathException(String message) {
        super(message);
    }
}

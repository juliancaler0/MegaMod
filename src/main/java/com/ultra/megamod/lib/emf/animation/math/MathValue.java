package com.ultra.megamod.lib.emf.animation.math;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * Superclass for all non-constant math components.
 * <p>
 * EMF represents booleans as float sentinels ({@link #TRUE} = positive infinity,
 * {@link #FALSE} = negative infinity) so the entire evaluation pipeline can stay in
 * primitive {@code float} territory. The static helpers let method implementations
 * convert between the two worlds without duplicating logic.
 * <p>
 * Ported 1:1 from upstream.
 */
public abstract class MathValue implements MathComponent {

    public static final float TRUE = Float.POSITIVE_INFINITY;
    public static final float FALSE = Float.NEGATIVE_INFINITY;

    public boolean isNegative;

    protected MathValue(boolean isNegative) {
        this.isNegative = isNegative;
    }

    protected MathValue() {
        this.isNegative = false;
    }

    public static float fromBoolean(boolean value) {
        return value ? TRUE : FALSE;
    }

    public static boolean toBoolean(float value) {
        if (value == FALSE) return false;
        if (value == TRUE) return true;
        // Lenient coercion — matches upstream behaviour for user-space variables
        // (varb.*) which may have been assigned a plain 0 / 1 before being read back
        // as boolean. Fresh Animations relies on this for e.g. var.in_air bootstrap.
        if (Float.isNaN(value)) return false;
        return value != 0.0f;
    }

    public static float validateBoolean(float value) {
        toBoolean(value);
        return value;
    }

    public static float invertBoolean(boolean value) {
        return fromBoolean(!value);
    }

    public static float invertBoolean(float value) {
        return fromBoolean(!toBoolean(value));
    }

    public static float invertBoolean(ResultSupplier value) {
        return fromBoolean(!toBoolean(value.get()));
    }

    public static float fromBoolean(BooleanSupplier value) {
        return fromBoolean(value.getAsBoolean());
    }

    public static float invertBoolean(BooleanSupplier value) {
        return invertBoolean(value.getAsBoolean());
    }

    public static boolean isBoolean(float value) {
        return value == TRUE || value == FALSE;
    }

    protected abstract ResultSupplier getResultSupplier();

    @Override
    public float getResult() {
        return isNegative ? -getResultSupplier().get() : getResultSupplier().get();
    }

    public MathValue makeNegative() {
        isNegative = !isNegative;
        return this;
    }

    /**
     * A {@link Supplier} of float, declared separately to avoid autoboxing and to make
     * call sites legible.
     */
    @FunctionalInterface
    public interface ResultSupplier {
        float get();
    }
}

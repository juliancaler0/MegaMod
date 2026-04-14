package com.ultra.megamod.lib.emf.animation.math;

import com.ultra.megamod.lib.emf.utils.EMFUtils;

/**
 * A literal constant node. Ported 1:1 from upstream.
 */
public class MathConstant extends MathValue {

    public static final MathConstant ZERO_CONST = new MathConstant(0);
    public static final MathConstant FALSE_CONST = new MathConstant(FALSE);

    private final float hardCodedValue;

    public MathConstant(float number, boolean isNegative) {
        hardCodedValue = isNegative ? -number : number;
    }

    public MathConstant(float number) {
        hardCodedValue = number;
    }

    @Override
    protected ResultSupplier getResultSupplier() {
        EMFUtils.logError("EMF math constant called supplier: this shouldn't happen!");
        return this::getResult;
    }

    @Override
    public boolean isConstant() {
        return true;
    }

    @Override
    public MathValue makeNegative() {
        return new MathConstant(-hardCodedValue);
    }

    @Override
    public String toString() {
        return String.valueOf(getResult());
    }

    @Override
    public float getResult() {
        return hardCodedValue;
    }
}

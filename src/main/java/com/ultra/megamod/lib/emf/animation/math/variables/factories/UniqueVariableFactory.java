package com.ultra.megamod.lib.emf.animation.math.variables.factories;

import com.ultra.megamod.lib.emf.animation.EmfParseContext;
import com.ultra.megamod.lib.emf.animation.math.MathValue;
import org.jetbrains.annotations.Nullable;

/**
 * A factory for creating variables that aren't simple singletons (e.g. bone
 * references, global variables, render variables). Ported 1:1 from upstream.
 */
public abstract class UniqueVariableFactory {

    @Override
    public boolean equals(final Object obj) {
        return obj != null && obj.getClass().equals(this.getClass());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    /**
     * Return a supplier that produces the variable's current float value, or {@code null}
     * if this factory can't resolve {@code variableKey} against {@code parseCtx}.
     */
    @Nullable
    public abstract MathValue.ResultSupplier getSupplierOrNull(String variableKey, EmfParseContext parseCtx);

    /**
     * Whether this factory can produce a variable with the given key. Called with the
     * unparsed variable name so regex-matching factories can key off a prefix.
     */
    public abstract boolean createsThisVariable(String variableKey);

    @Nullable
    public String getExplanationTranslationKey() {
        return null;
    }

    @Nullable
    public String getTitleTranslationKey() {
        return null;
    }
}

package com.ultra.megamod.lib.emf.animation.math.variables.factories;

import com.ultra.megamod.lib.emf.animation.EmfParseContext;
import com.ultra.megamod.lib.emf.animation.EmfRuntime;
import com.ultra.megamod.lib.emf.animation.math.MathValue;
import org.jetbrains.annotations.Nullable;

/**
 * Factory for variables of the form {@code global_var.xxx} / {@code global_varb.xxx}.
 * Reads/writes through the active {@link com.ultra.megamod.lib.emf.animation.EmfVariableContext}
 * rather than upstream's process-wide static map, so Phase E can scope globals per world.
 */
public class GlobalVariableFactory extends UniqueVariableFactory {

    @Override
    public MathValue.ResultSupplier getSupplierOrNull(final String variableKey, final EmfParseContext parseCtx) {
        return () -> EmfRuntime.current().context().getGlobalVariable(variableKey);
    }

    @Override
    public boolean createsThisVariable(final String variableKey) {
        return variableKey != null && variableKey.matches("global_(var|varb)\\.\\w+");
    }

    @Override
    public @Nullable String getExplanationTranslationKey() {
        return "entity_model_features.config.variable_explanation.global_variable";
    }

    @Override
    public @Nullable String getTitleTranslationKey() {
        return "entity_model_features.config.variable_explanation.global_variable.title";
    }
}

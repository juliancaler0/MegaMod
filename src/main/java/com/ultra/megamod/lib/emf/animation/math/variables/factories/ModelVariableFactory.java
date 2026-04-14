package com.ultra.megamod.lib.emf.animation.math.variables.factories;

import com.ultra.megamod.lib.emf.animation.EmfExpression;
import com.ultra.megamod.lib.emf.animation.EmfParseContext;
import com.ultra.megamod.lib.emf.animation.EmfRuntime;
import com.ultra.megamod.lib.emf.animation.math.MathValue;
import org.jetbrains.annotations.Nullable;

/**
 * Factory for user-defined per-entity variables {@code var.xxx} / {@code varb.xxx}.
 * <p>
 * Resolution order: sibling expressions declared earlier in the same {@code animations}
 * block take precedence, otherwise we fall back to the context's entity-variable store.
 */
public class ModelVariableFactory extends UniqueVariableFactory {

    @Override
    public MathValue.ResultSupplier getSupplierOrNull(final String variableKey, final EmfParseContext parseCtx) {
        EmfExpression sibling = parseCtx == null ? null : parseCtx.siblingVariables.get(variableKey);
        if (sibling != null) {
            return sibling::peekLastResult;
        }
        return () -> EmfRuntime.current().context().getEntityVariable(variableKey);
    }

    @Override
    public boolean createsThisVariable(final String variableKey) {
        return variableKey != null && variableKey.matches("(var|varb)\\.\\w+");
    }

    @Override
    public @Nullable String getExplanationTranslationKey() {
        return "entity_model_features.config.variable_explanation.model_variable";
    }

    @Override
    public @Nullable String getTitleTranslationKey() {
        return "entity_model_features.config.variable_explanation.model_variable.title";
    }
}

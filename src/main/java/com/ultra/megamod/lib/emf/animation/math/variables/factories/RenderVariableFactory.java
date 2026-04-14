package com.ultra.megamod.lib.emf.animation.math.variables.factories;

import com.ultra.megamod.lib.emf.animation.EmfExpression;
import com.ultra.megamod.lib.emf.animation.EmfParseContext;
import com.ultra.megamod.lib.emf.animation.EmfRuntime;
import com.ultra.megamod.lib.emf.animation.math.MathValue;
import org.jetbrains.annotations.Nullable;

/**
 * Factory for {@code render.xxx} variables — values driven by the current render pass
 * (age deltas, light, etc.). Ported logic from upstream, with the concrete variable
 * set delegated to {@link com.ultra.megamod.lib.emf.animation.EmfVariableContext}.
 */
public class RenderVariableFactory extends UniqueVariableFactory {

    @Override
    public MathValue.ResultSupplier getSupplierOrNull(final String variableKey, final EmfParseContext parseCtx) {
        // sibling var with this literal name wins first
        EmfExpression sibling = parseCtx == null ? null : parseCtx.siblingVariables.get(variableKey);
        if (sibling != null) return sibling::peekLastResult;
        return () -> EmfRuntime.current().context().getFloat(variableKey);
    }

    @Override
    public boolean createsThisVariable(final String variableKey) {
        return variableKey != null && variableKey.matches("render\\.\\w+");
    }

    @Override
    public @Nullable String getExplanationTranslationKey() {
        return "entity_model_features.config.variable_explanation.render_variable";
    }

    @Override
    public @Nullable String getTitleTranslationKey() {
        return "entity_model_features.config.variable_explanation.render_variable.title";
    }
}

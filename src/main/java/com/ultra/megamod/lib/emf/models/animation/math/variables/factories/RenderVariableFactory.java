package com.ultra.megamod.lib.emf.models.animation.math.variables.factories;

import org.jetbrains.annotations.Nullable;
import com.ultra.megamod.lib.emf.models.animation.EMFAnimation;
import com.ultra.megamod.lib.emf.models.animation.math.MathValue;
import com.ultra.megamod.lib.emf.models.animation.math.variables.EMFModelOrRenderVariable;
import com.ultra.megamod.lib.emf.utils.EMFUtils;

public class RenderVariableFactory extends UniqueVariableFactory {
    @Override
    public MathValue.ResultSupplier getSupplierOrNull(final String variableKey, final EMFAnimation calculationInstance) {
        //requires calculation instance check before global check so must be a factory
        EMFAnimation renderVariableCalculator = calculationInstance.temp_emfAnimationVariables.get(variableKey);
        if (renderVariableCalculator != null) {
            return renderVariableCalculator::getLastResultOnly;
        }
        //try get default
        EMFModelOrRenderVariable variable = EMFModelOrRenderVariable.getRenderVariable(variableKey);
        if (variable != null && variable.isRenderVariable())
            return variable::getValue;
        if (printing()) EMFUtils.logWarn("no render variable found for: [" + variableKey + "]");
        return null;
    }

    @Override
    public boolean createsThisVariable(final String variableKey) {
        if (variableKey == null) return false;
        return variableKey.matches("(render)\\.\\w+");
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

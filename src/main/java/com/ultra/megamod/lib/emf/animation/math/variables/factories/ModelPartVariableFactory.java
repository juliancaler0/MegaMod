package com.ultra.megamod.lib.emf.animation.math.variables.factories;

import com.ultra.megamod.lib.emf.animation.EmfParseContext;
import com.ultra.megamod.lib.emf.animation.EmfRuntime;
import com.ultra.megamod.lib.emf.animation.EmfVariableContext;
import com.ultra.megamod.lib.emf.animation.math.MathConstant;
import com.ultra.megamod.lib.emf.animation.math.MathValue;
import org.jetbrains.annotations.Nullable;

/**
 * Factory for bone references like {@code head.rx}, {@code body.sy}, {@code left_leg.visible}.
 * Ported from upstream with the resolution delegated to the active variable context.
 */
public class ModelPartVariableFactory extends UniqueVariableFactory {

    @Override
    public MathValue.ResultSupplier getSupplierOrNull(final String variableKey, final EmfParseContext parseCtx) {
        String[] split = variableKey.split("\\.", 2);
        if (split.length != 2) return null;

        final String partName = split[0];
        final EmfVariableContext.BoneProperty property = EmfVariableContext.BoneProperty.fromKey(split[1]);
        if (property == null) return null;

        // If the package name contains "render", the render-variable factory handles it.
        if ("render".equals(partName)) return null;

        return () -> EmfRuntime.current().context().getBoneProperty(partName, property);
    }

    @Override
    public boolean createsThisVariable(final String variableKey) {
        return variableKey != null
                && variableKey.matches("[a-zA-Z0-9_]+\\.([trs][xyz]$|visible$|visible_boxes$)");
    }

    @Override
    public @Nullable String getExplanationTranslationKey() {
        return "entity_model_features.config.variable_explanation.model_part";
    }

    @Override
    public @Nullable String getTitleTranslationKey() {
        return "entity_model_features.config.variable_explanation.model_part.title";
    }

    /** Convenience fallback for bone lookups that match the predicate but produce no part. */
    public static MathValue.ResultSupplier missingPartFallback(String partKey) {
        return partKey.startsWith("is_") || partKey.endsWith(".visible") || partKey.endsWith(".visible_boxes")
                ? MathConstant.FALSE_CONST::getResult
                : MathConstant.ZERO_CONST::getResult;
    }
}

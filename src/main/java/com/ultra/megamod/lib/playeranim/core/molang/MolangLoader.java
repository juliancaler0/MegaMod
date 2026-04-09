package com.ultra.megamod.lib.playeranim.core.molang;

import com.google.gson.JsonElement;
import com.ultra.megamod.lib.playeranim.core.animation.AnimationController;

import java.util.Collections;
import java.util.List;

/**
 * Stub replacement for the Molang expression loader.
 * Parses JSON values as simple float constants instead of Molang expressions.
 */
public final class MolangLoader {
    /**
     * A shared static MochaEngine instance for evaluating expressions.
     */
    public static final MochaEngine<Object> MOCHA_ENGINE = new MochaEngine<>();

    /**
     * Parse a JSON element into a list of Expressions.
     * Without Molang, we only support numeric constants.
     *
     * @param isForRotation whether this is a rotation value (unused without Molang)
     * @param element       the JSON element to parse
     * @param defaultValue  fallback expression if parsing fails
     * @return a singleton list containing the parsed expression
     */
    public static List<Expression> parseJson(boolean isForRotation, JsonElement element, Expression defaultValue) {
        try {
            if (element != null && element.isJsonPrimitive()) {
                float value = element.getAsFloat();
                if (isForRotation) {
                    value = (float) Math.toRadians(value);
                }
                return Collections.singletonList(FloatExpression.of(value));
            }
        } catch (Exception ignored) {}
        return Collections.singletonList(defaultValue);
    }

    /**
     * Check if all expressions in the list are constant (non-Molang).
     * Without Molang support, all expressions are constant.
     */
    public static boolean isConstant(List<Expression> expressions) {
        if (expressions == null || expressions.isEmpty()) return true;
        for (Expression expr : expressions) {
            if (!(expr instanceof FloatExpression)) return false;
        }
        return true;
    }

    /**
     * Creates a new MochaEngine for an AnimationController.
     * Stub: returns a plain engine with no Molang queries.
     */
    public static MochaEngine<AnimationController> createNewEngine(AnimationController controller) {
        return new MochaEngine<>();
    }

    private MolangLoader() {}
}

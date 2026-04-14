package com.ultra.megamod.lib.emf.animation;

import com.ultra.megamod.lib.emf.animation.math.MathComponent;
import com.ultra.megamod.lib.emf.animation.math.MathExpressionParser;

/**
 * Public handle around a parsed animation expression.
 * <p>
 * Built from {@link #compile(String, EmfParseContext)}; evaluated against an
 * {@link EmfVariableContext} via {@link #evaluate(EmfVariableContext)}. Phase E mixins
 * will call {@code evaluate(...)} once per bone animation per frame.
 * <p>
 * The expression also caches its last evaluated result so that sibling {@code var.X}
 * references can read the value without re-evaluating (matches upstream's
 * {@code EMFAnimation.getLastResultOnly}).
 */
public final class EmfExpression {

    public final String key;
    public final String source;
    public final String modelName;
    private final MathComponent compiled;
    private float lastResult = 0f;

    private EmfExpression(String key, String source, String modelName, MathComponent compiled) {
        this.key = key;
        this.source = source;
        this.modelName = modelName;
        this.compiled = compiled;
    }

    /**
     * Parse {@code source} into an expression against the given parse context. Parse
     * errors are captured into a NULL_EXPRESSION that logs and returns NaN on evaluate.
     */
    public static EmfExpression compile(String source, EmfParseContext ctx) {
        MathComponent compiled = MathExpressionParser.getOptimizedExpression(source, false, ctx);
        return new EmfExpression(ctx.animKey, source, ctx.modelName, compiled);
    }

    /**
     * Parse with a convenience context. Preferred for one-off expressions from Phase E tests.
     */
    public static EmfExpression compile(String source, String modelName, String animKey) {
        EmfParseContext ctx = new EmfParseContext(modelName, animKey);
        return compile(source, ctx);
    }

    /** True if this expression compiled successfully (constant-foldable or otherwise). */
    public boolean isValid() {
        return compiled != MathExpressionParser.NULL_EXPRESSION;
    }

    /**
     * Evaluate under {@code context} and return the float result. Installs {@code context}
     * on the thread-local {@link EmfRuntime} for the duration of the call so any node in
     * the AST can reach it through the static {@link EmfRuntime#current()} handle.
     */
    public float evaluate(EmfVariableContext context) {
        EmfRuntime runtime = EmfRuntime.current();
        try (var ignored = runtime.push(context)) {
            float v = compiled.getResult();
            lastResult = v;
            return v;
        } catch (Exception e) {
            return 0f;
        }
    }

    /** Returns the last value computed by {@link #evaluate}. Used by sibling variable refs. */
    public float peekLastResult() {
        return lastResult;
    }

    /** The parsed AST, exposed for tests / debug tooling. */
    public MathComponent compiled() {
        return compiled;
    }

    @Override
    public String toString() {
        return "EmfExpression{" + modelName + "::" + key + "='" + source + "'}";
    }
}

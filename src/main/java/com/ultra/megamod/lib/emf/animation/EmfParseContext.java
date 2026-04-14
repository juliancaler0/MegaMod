package com.ultra.megamod.lib.emf.animation;

import com.ultra.megamod.lib.emf.animation.math.MathComponent;

import java.util.LinkedHashMap;

/**
 * Per-parse context passed through the expression parser.
 * <p>
 * Upstream calls this {@code EMFAnimation} — a heavyweight object that also holds the
 * parsed expression and per-frame results. In Phase D we just need the identifying
 * strings (so error messages match upstream) plus the per-parse lookup of previously
 * declared user variables, so that {@code varb.foo = ...} can be referenced by later
 * expressions in the same {@code .jem}.
 */
public class EmfParseContext {

    public final String modelName;
    public final String animKey;

    /**
     * Sibling variable expressions declared in the same {@code animations} block.
     * Upstream names this {@code temp_emfAnimationVariables} on {@link }.
     */
    public final LinkedHashMap<String, EmfExpression> siblingVariables = new LinkedHashMap<>();

    /**
     * Per-parse store of sibling bone part names → placeholder supplier. We only need
     * the keyset for the variable factory predicate check in Phase D; Phase E replaces
     * the value type with real {@code ModelPart} references.
     */
    public final LinkedHashMap<String, MathComponent> knownBoneIds = new LinkedHashMap<>();

    public EmfParseContext(String modelName, String animKey) {
        this.modelName = modelName;
        this.animKey = animKey;
    }

    public EmfParseContext(String modelName) {
        this(modelName, "");
    }

    public EmfParseContext forKey(String animKey) {
        EmfParseContext child = new EmfParseContext(this.modelName, animKey);
        child.siblingVariables.putAll(this.siblingVariables);
        child.knownBoneIds.putAll(this.knownBoneIds);
        return child;
    }
}

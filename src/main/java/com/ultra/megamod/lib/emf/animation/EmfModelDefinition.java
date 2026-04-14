package com.ultra.megamod.lib.emf.animation;

import com.ultra.megamod.lib.emf.jem.EmfJemData;
import com.ultra.megamod.lib.emf.jem.EmfPartData;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Compiled, render-ready projection of an {@link EmfJemData}.
 * <p>
 * This is the handshake object Phase D produces and Phase E consumes:
 * <ol>
 *     <li>Phase D: {@link #compile(EmfJemData)} walks the prepared jem, turns every
 *         animation string into a parsed {@link EmfExpression}, and buckets them by
 *         the vanilla bone the assignment targets.</li>
 *     <li>Phase E: looks up the {@link CompiledAnimation}s for a bone at frame time,
 *         calls {@link EmfExpression#evaluate(EmfVariableContext)} on each one, and
 *         applies the result to the corresponding vanilla {@code ModelPart} field.</li>
 * </ol>
 * No render wiring happens in Phase D — {@link #applyTo(Object, EmfVariableContext)}
 * is provided purely as a documentation handle so the call site is present from the
 * beginning. It currently forwards evaluation only.
 */
public final class EmfModelDefinition {

    public final EmfJemData source;
    public final Map<String, List<CompiledAnimation>> animationsByBone;

    private EmfModelDefinition(EmfJemData source, Map<String, List<CompiledAnimation>> animationsByBone) {
        this.source = source;
        this.animationsByBone = animationsByBone;
    }

    /**
     * Compile every animation expression in {@code jem} against the expression parser.
     * The resulting object is safe to cache at the pack-load tier.
     */
    public static EmfModelDefinition compile(EmfJemData jem) {
        LinkedHashMap<String, List<CompiledAnimation>> out = new LinkedHashMap<>();

        jem.getAllTopLevelAnimationsByVanillaPartName().forEach((vanillaBone, animationBlocks) -> {
            List<CompiledAnimation> compiled = new ArrayList<>();
            EmfParseContext sharedCtx = new EmfParseContext(jem.displayFileName);
            // Register every declared part id so ModelPartVariableFactory predicate matches
            collectBoneIds(jem.models, sharedCtx);

            int blockIndex = 0;
            for (LinkedHashMap<String, String> block : animationBlocks) {
                blockIndex++;
                for (Map.Entry<String, String> entry : block.entrySet()) {
                    String targetKey = entry.getKey();
                    String source = entry.getValue();
                    EmfParseContext ctx = sharedCtx.forKey(targetKey);

                    // sibling variables: expressions declared earlier with `var.foo = ...`
                    // are visible to later expressions in the same block.
                    EmfExpression expr = EmfExpression.compile(source, ctx);
                    // if this defines a variable, register it for later siblings
                    if (targetKey.startsWith("var.") || targetKey.startsWith("varb.")
                            || targetKey.startsWith("global_var.") || targetKey.startsWith("global_varb.")) {
                        sharedCtx.siblingVariables.put(targetKey, expr);
                    }
                    compiled.add(new CompiledAnimation(vanillaBone, targetKey, expr, blockIndex));
                }
            }
            out.put(vanillaBone, compiled);
        });

        return new EmfModelDefinition(jem, out);
    }

    private static void collectBoneIds(Iterable<EmfPartData> parts, EmfParseContext ctx) {
        if (parts == null) return;
        for (EmfPartData p : parts) {
            if (p == null) continue;
            if (p.id != null && !p.id.isBlank()) {
                ctx.knownBoneIds.put(p.id, null);
            }
            if (p.submodel != null) {
                LinkedList<EmfPartData> single = new LinkedList<>();
                single.add(p.submodel);
                collectBoneIds(single, ctx);
            }
            if (p.submodels != null) collectBoneIds(p.submodels, ctx);
        }
    }

    /**
     * Phase D stub: evaluates every animation expression against {@code context} so the
     * cache warms up and any parse errors fire. Returns the number of expressions
     * successfully evaluated.
     * <p>
     * Phase E replaces this body: it will resolve {@code vanillaModelRoot} into a
     * per-bone name -> ModelPart map, push {@code context} onto the runtime, and for
     * each evaluated result write the value back to the target ModelPart axis /
     * visibility field.
     *
     * @param vanillaModelRoot the {@code ModelPart} root (typed as {@code Object} in
     *                         Phase D to keep this package render-free).
     * @param context          the frame-scoped variable context.
     * @return number of expressions evaluated.
     */
    public int applyTo(Object vanillaModelRoot, EmfVariableContext context) {
        int count = 0;
        for (List<CompiledAnimation> list : animationsByBone.values()) {
            for (CompiledAnimation anim : list) {
                anim.expression.evaluate(context);
                count++;
            }
        }
        return count;
    }

    /** One parsed animation assignment {@code targetKey = source}. */
    public static final class CompiledAnimation {
        public final String vanillaBone;
        public final String targetKey;
        public final EmfExpression expression;
        public final int blockIndex;

        public CompiledAnimation(String vanillaBone, String targetKey, EmfExpression expression, int blockIndex) {
            this.vanillaBone = vanillaBone;
            this.targetKey = targetKey;
            this.expression = expression;
            this.blockIndex = blockIndex;
        }

        @Override
        public String toString() {
            return vanillaBone + "." + targetKey + " = " + expression.source;
        }
    }
}

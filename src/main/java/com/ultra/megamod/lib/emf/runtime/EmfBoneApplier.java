package com.ultra.megamod.lib.emf.runtime;

import com.ultra.megamod.lib.emf.animation.EmfExpression;
import com.ultra.megamod.lib.emf.animation.EmfModelDefinition;
import com.ultra.megamod.lib.emf.animation.EmfRuntime;
import com.ultra.megamod.lib.emf.animation.EmfVariableContext;
import com.ultra.megamod.lib.emf.utils.EMFUtils;
import net.minecraft.client.model.geom.ModelPart;

import java.util.List;
import java.util.Map;

/**
 * The per-frame bridge from Phase D expressions to vanilla {@link ModelPart}
 * transform fields.
 * <p>
 * Called from the {@code HumanoidModel.setupAnim} / {@code EntityModel.setupAnim}
 * mixin TAIL after vanilla writes its default pose. For each compiled animation
 * in the {@link EmfModelDefinition}, we:
 * <ol>
 *   <li>Resolve the target {@code vanillaBone -> ModelPart}.</li>
 *   <li>Push {@code context} onto the {@link EmfRuntime} thread-local so expression
 *       leaves see the frame's variable values.</li>
 *   <li>Evaluate the expression tree.</li>
 *   <li>Translate {@code targetKey} (e.g. {@code head.rx}, {@code body.tz}) into a
 *       write on the corresponding {@code ModelPart} field.</li>
 * </ol>
 * This is the last mile of the pipeline — once this runs, the vanilla renderer
 * draws the bone transforms Fresh Animations packed into the {@code .jem}.
 */
public final class EmfBoneApplier {

    private EmfBoneApplier() {
    }

    /**
     * Applies every compiled animation in {@code active} to {@code active.bone(...)}.
     * Returns the number of assignments successfully written.
     */
    public static int apply(EmfActiveModel active, ModelPart root, EmfVariableContext ctx) {
        if (active == null || active.definition == null) return 0;
        active.bindRoot(root);

        int written = 0;
        try (AutoCloseable scope = EmfRuntime.current().push(ctx)) {
            for (Map.Entry<String, List<EmfModelDefinition.CompiledAnimation>> entry
                    : active.definition.animationsByBone.entrySet()) {
                String boneName = entry.getKey();
                ModelPart bone = active.bone(boneName);
                if (bone == null) continue;

                for (EmfModelDefinition.CompiledAnimation anim : entry.getValue()) {
                    if (applyOne(bone, anim, active, ctx)) written++;
                }
            }
        } catch (Exception e) {
            EMFUtils.logError("EMF apply failed for " + active.sourceJemId + ": " + e.getMessage());
        }
        return written;
    }

    private static boolean applyOne(ModelPart bone, EmfModelDefinition.CompiledAnimation anim,
                                    EmfActiveModel active, EmfVariableContext ctx) {
        EmfExpression expr = anim.expression;
        if (expr == null) return false;
        String key = anim.targetKey;
        if (key == null || key.isEmpty()) return false;

        // User variable assignment: value is recorded on the frame context, no model write
        if (key.startsWith("var.") || key.startsWith("varb.")
                || key.startsWith("global_var.") || key.startsWith("global_varb.")) {
            float v = expr.evaluate(ctx);
            String plain = key.substring(key.indexOf('.') + 1);
            if (key.startsWith("global_")) {
                ctx.setGlobalVariable(plain, v);
            } else {
                ctx.setEntityVariable(plain, v);
            }
            return true;
        }

        // "bone.axis" target: find the last dot and split
        int dot = key.lastIndexOf('.');
        if (dot < 0) return false;
        String targetBoneId = key.substring(0, dot);
        String axis = key.substring(dot + 1);

        // If the animation targets a sibling bone by id rather than the owning bone,
        // resolve via the active model's map. Otherwise use the passed-in bone.
        ModelPart part = bone;
        if (!targetBoneId.isEmpty()
                && !targetBoneId.equals(anim.vanillaBone)
                && !targetBoneId.startsWith("EMF_")) {
            ModelPart alt = active.bone(targetBoneId);
            if (alt != null) part = alt;
        }

        float value = expr.evaluate(ctx);
        return writeField(part, axis, value);
    }

    private static boolean writeField(ModelPart part, String axis, float value) {
        if (part == null || axis == null) return false;
        switch (axis) {
            case "tx": part.x = value; return true;
            case "ty": part.y = value; return true;
            case "tz": part.z = value; return true;
            case "rx": part.xRot = value; return true;
            case "ry": part.yRot = value; return true;
            case "rz": part.zRot = value; return true;
            case "sx": part.xScale = value; return true;
            case "sy": part.yScale = value; return true;
            case "sz": part.zScale = value; return true;
            case "visible": part.visible = value != 0f; return true;
            case "visible_boxes":
                // vanilla doesn't distinguish; fall back to visible
                part.visible = value != 0f; return true;
            default:
                return false;
        }
    }
}

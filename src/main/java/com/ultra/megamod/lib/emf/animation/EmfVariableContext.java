package com.ultra.megamod.lib.emf.animation;

/**
 * Frame-scoped variable lookup used by the expression evaluator.
 * <p>
 * Upstream fulfils this role with a blob of static state on
 * {@code EMFAnimationEntityContext} plus {@code EMFManager} — which both directly
 * depend on the live render state / vanilla Entity hierarchy. To keep Phase D a
 * pure-logic layer, we compress it into this interface: Phase E will provide a
 * concrete implementation driven by the vanilla render state at frame time.
 * <p>
 * The method set mirrors the full upstream surface (see {@code VariableRegistry}),
 * so every built-in variable any {@code .jem} pack references resolves through a
 * single call here.
 */
public interface EmfVariableContext {

    /** Returns {@link #NO_OP} by default — used during parse-time constant folding. */
    EmfVariableContext NO_OP = new EmfVariableContext() { };

    /** Read a named entity / render variable; unknown names should return {@code 0}. */
    default float getFloat(String name) {
        return 0f;
    }

    /** Read a named boolean variable; unknown names should return {@code false}. */
    default boolean getBoolean(String name) {
        return false;
    }

    /** Read a bone transform component (e.g. {@code head.rx}, {@code body.sy}). */
    default float getBoneProperty(String boneName, BoneProperty property) {
        return 0f;
    }

    /** Whether we are inside the animation-validation phase (see upstream EMFManager). */
    default boolean isValidationPhase() {
        return false;
    }

    /** Returns the value of a user-defined entity variable previously set via {@code var.X = ...}. */
    default float getEntityVariable(String name) {
        return 0f;
    }

    /** Sets the value of a user-defined entity variable (from {@code var.X = ...} animation lines). */
    default void setEntityVariable(String name, float value) {
    }

    /** Returns the value of a global variable (shared across models). */
    default float getGlobalVariable(String name) {
        return 0f;
    }

    /** Sets a global variable (from {@code global_var.X = ...} animation lines). */
    default void setGlobalVariable(String name, float value) {
    }

    /**
     * Evaluates an NBT lookup expression, e.g. {@code nbt("path")}.
     * Phase E will wire this through to the real entity NBT; Phase D returns 0.
     */
    default float evaluateNbt(String expression) {
        return 0f;
    }

    /**
     * Transform properties that can be read off a bone reference (e.g. {@code head.rx}).
     * Upstream encodes these on {@code EMFModelOrRenderVariable}; we keep the key list
     * inline here because Phase D has no concrete model parts yet.
     */
    enum BoneProperty {
        TX, TY, TZ,
        RX, RY, RZ,
        SX, SY, SZ,
        VISIBLE, VISIBLE_BOXES;

        public static BoneProperty fromKey(String key) {
            return switch (key) {
                case "tx" -> TX;
                case "ty" -> TY;
                case "tz" -> TZ;
                case "rx" -> RX;
                case "ry" -> RY;
                case "rz" -> RZ;
                case "sx" -> SX;
                case "sy" -> SY;
                case "sz" -> SZ;
                case "visible" -> VISIBLE;
                case "visible_boxes" -> VISIBLE_BOXES;
                default -> null;
            };
        }

        public boolean isBoolean() {
            return this == VISIBLE || this == VISIBLE_BOXES;
        }
    }
}

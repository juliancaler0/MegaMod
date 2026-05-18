package com.ultra.megamod.feature.combat.animation.client;

/**
 * Per-render-frame carrier for the whirlwind-style body-rotation angle.
 * <p>
 * The pipeline (1.21.11):
 * <ol>
 *   <li>{@code AvatarRendererMixin.extractRenderState} HEAD inspects the player's
 *       active SpellEngine cast process. If the spell has a non-zero
 *       {@code animation_spin} field, it computes
 *       {@code angle = castTicks * animation_spin} (in degrees) and stores it
 *       here via {@link #set(float)}.</li>
 *   <li>{@code LivingEntityRendererSpinMixin.render} HEAD calls {@link #consume()}
 *       and, if non-zero, applies {@code poseStack.mulPose(Axis.YP.rotationDegrees(angle))}
 *       which rotates the entire model around its vertical axis for that frame.</li>
 * </ol>
 * {@code consume()} resets the value to 0, so a non-casting entity rendered
 * right after a casting one doesn't inherit a stale angle.
 * <p>
 * In 1.21.11 the original {@code LivingEntityRendererWhirlwind} mixin from Rogues
 * couldn't be ported directly because the render method now takes a
 * {@code LivingEntityRenderState} instead of the entity object — this thread-local
 * bridges the gap between extract (which has the {@code Avatar} entity) and render
 * (which has only the state).
 */
public final class SpellSpinContext {
    private static final ThreadLocal<Float> ANGLE_DEGREES = ThreadLocal.withInitial(() -> 0f);

    private SpellSpinContext() {}

    public static void set(float degrees) {
        ANGLE_DEGREES.set(degrees);
    }

    /**
     * Returns the current angle (degrees) and clears it. Returns 0 when no
     * spin is active for the entity about to render.
     */
    public static float consume() {
        float v = ANGLE_DEGREES.get();
        ANGLE_DEGREES.set(0f);
        return v;
    }
}

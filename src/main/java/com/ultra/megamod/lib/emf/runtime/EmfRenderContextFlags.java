package com.ultra.megamod.lib.emf.runtime;

/**
 * Lightweight static flags that track the render-context the current
 * {@code setupAnim} is happening in.
 * <p>
 * Upstream EMF keeps these on {@code EMFAnimationEntityContext} as static
 * fields; the render-layer mixins toggle them at the start of each layer pass
 * and the animation variable resolvers read them back. We match the pattern
 * 1:1 so the variable names {@code is_in_hand}, {@code is_in_item_frame},
 * {@code is_on_head}, {@code is_in_gui}, {@code is_first_person_hand}, and
 * {@code is_in_ground} resolve correctly without needing to thread the values
 * through every {@link MinecraftRenderStateContext} creation site.
 * <p>
 * These flags are inherently per-thread only in single-client gameplay.
 * If we ever grow true multi-threaded renderer dispatch the implementation
 * here will need to go ThreadLocal; upstream has the same assumption.
 */
public final class EmfRenderContextFlags {

    private EmfRenderContextFlags() {
    }

    public static volatile boolean setInHand = false;
    public static volatile boolean setInItemFrame = false;
    public static volatile boolean setIsOnHead = false;
    public static volatile boolean setIsInGui = false;
    public static volatile boolean isFirstPersonHand = false;
    public static volatile boolean is_in_ground_override = false;

    /** Resets every flag. Typically called by a render-end hook. */
    public static void reset() {
        setInHand = false;
        setInItemFrame = false;
        setIsOnHead = false;
        setIsInGui = false;
        isFirstPersonHand = false;
        is_in_ground_override = false;
    }
}

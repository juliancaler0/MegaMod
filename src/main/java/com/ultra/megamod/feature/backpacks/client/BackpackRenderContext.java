package com.ultra.megamod.feature.backpacks.client;

/**
 * Thread-local context used to pass the entity ID from the player renderer
 * (via mixin on AvatarRenderer.extractRenderState) to the BackpackLayerRenderer.
 *
 * In 1.21.11, LivingEntityRenderState does not carry entity identity, so
 * render layers cannot determine which entity they are rendering for.
 * This thread-local bridges that gap: the mixin sets the entity ID before
 * rendering, and the layer reads it during submit().
 */
public class BackpackRenderContext {

    private static final ThreadLocal<Integer> CURRENT_ENTITY_ID =
        ThreadLocal.withInitial(() -> -1);

    /**
     * Set the entity ID of the player currently being rendered.
     * Called from AvatarRendererMixin.extractRenderState.
     */
    public static void setEntityId(int id) {
        CURRENT_ENTITY_ID.set(id);
    }

    /**
     * Get the entity ID of the player currently being rendered.
     * Returns -1 if no entity is being rendered (should not happen in practice).
     */
    public static int getEntityId() {
        return CURRENT_ENTITY_ID.get();
    }

    /**
     * Clear the stored entity ID after rendering is complete.
     */
    public static void clear() {
        CURRENT_ENTITY_ID.remove();
    }
}

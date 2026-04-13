package com.ultra.megamod.lib.etf.features;

import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import org.jetbrains.annotations.Nullable;

/**
 * Global per-render-pass context.
 * <p>
 * Upstream uses this to communicate between render mixins and the rest of the ETF
 * pipeline: which entity is rendering, whether texture patching is allowed, whether
 * a feature renderer or the base model is currently running, etc. Phase B enables the
 * subset of flags actually read by render mixins:
 * <ul>
 *   <li>{@link #getCurrentEntityState()} — set by the render dispatcher / entity renderer
 *       so {@link com.ultra.megamod.lib.etf.utils.ETFUtils2#getETFVariantNotNullForInjector}
 *       knows which entity's variant to return.</li>
 *   <li>{@link #allowRenderLayerTextureModify()}/{@link #preventRenderLayerTextureModify()}
 *       — gate the {@code RenderType} factory substitution; nametag / post-renderers flip
 *       this off so the player name doesn't get a swapped texture.</li>
 *   <li>{@link #setRenderingFeatures(boolean)} — flag read by future feature-renderer
 *       swapping.</li>
 *   <li>{@link #isRandomLimitedToProperties()} — used by
 *       {@link com.ultra.megamod.lib.etf.ETFApi} to skip true-random variants when only
 *       properties-driven variants should apply.</li>
 * </ul>
 * <p>
 * Special-render-overlay phase, model-part depth tracking, and NBT caching stay on the
 * class so future phases (emissive, enchant) can layer on without another rewrite.
 */
public final class ETFRenderContext {

    private static boolean renderingFeatures = false;
    private static boolean allowRenderLayerTextureModify = true;
    private static boolean limitModifyToProperties = false;
    private static ETFEntityRenderState currentEntity = null;
    private static int currentModelPartDepth = 0;
    private static boolean isInSpecialRenderOverlayPhase = false;
    private static boolean allowedToPatch = false;

    private ETFRenderContext() {}

    @Nullable
    public static ETFEntityRenderState getCurrentEntityState() {
        return currentEntity;
    }

    /**
     * Set the entity the renderer is currently processing. Also resets texture-modify
     * gating so the new entity starts from the default ("allowed") state.
     */
    public static void setCurrentEntity(@Nullable ETFEntityRenderState state) {
        allowRenderLayerTextureModify = true;
        currentEntity = state;
    }

    public static boolean isRenderingFeatures() {
        return renderingFeatures;
    }

    public static void setRenderingFeatures(boolean renderingFeatures) {
        ETFRenderContext.renderingFeatures = renderingFeatures;
    }

    public static boolean isAllowedToRenderLayerTextureModify() {
        return allowRenderLayerTextureModify;
    }

    public static void preventRenderLayerTextureModify() {
        allowRenderLayerTextureModify = false;
    }

    public static void allowRenderLayerTextureModify() {
        allowRenderLayerTextureModify = true;
    }

    public static void allowOnlyPropertiesRandom() {
        limitModifyToProperties = true;
    }

    public static void allowAllRandom() {
        limitModifyToProperties = false;
    }

    public static boolean isRandomLimitedToProperties() {
        return limitModifyToProperties;
    }

    public static int getCurrentModelPartDepth() {
        return currentModelPartDepth;
    }

    public static void incrementCurrentModelPartDepth() {
        currentModelPartDepth++;
    }

    public static void decrementCurrentModelPartDepth() {
        currentModelPartDepth--;
    }

    public static void resetCurrentModelPartDepth() {
        currentModelPartDepth = 0;
    }

    public static boolean isIsInSpecialRenderOverlayPhase() {
        return isInSpecialRenderOverlayPhase;
    }

    public static void startSpecialRenderOverlayPhase() {
        isInSpecialRenderOverlayPhase = true;
    }

    public static void endSpecialRenderOverlayPhase() {
        isInSpecialRenderOverlayPhase = false;
    }

    public static boolean isAllowedToPatch() {
        return allowedToPatch;
    }

    public static void allowTexturePatching() {
        allowedToPatch = true;
    }

    public static void preventTexturePatching() {
        allowedToPatch = false;
    }

    public static void reset() {
        currentModelPartDepth = 0;
        currentEntity = null;
        allowedToPatch = false;
        allowRenderLayerTextureModify = true;
        limitModifyToProperties = false;
    }
}

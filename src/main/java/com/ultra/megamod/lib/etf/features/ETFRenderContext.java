package com.ultra.megamod.lib.etf.features;

import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import org.jetbrains.annotations.Nullable;

/**
 * Phase A slim render context.
 * <p>
 * Upstream ETF uses this class to communicate between render mixins and the rest of
 * the ETF pipeline — tracking the current entity being rendered, which render pass is
 * active, whether texture patching / render-layer substitution is allowed, and so on.
 * <p>
 * Phase A exposes only the bits the parser reads directly:
 * <ul>
 *   <li>{@link #getCurrentEntityState()} — used by {@link com.ultra.megamod.lib.etf.ETFException}
 *       to include the currently-rendering entity in failure messages.</li>
 *   <li>{@link #isRandomLimitedToProperties()} — checked by the variant-supplier factory
 *       to decide whether to fall back to a "true random" provider.</li>
 * </ul>
 * The remaining methods (render-layer substitution, depth tracking, special overlay
 * phase) land in Phase B when mixins are introduced.
 */
public final class ETFRenderContext {

    private static ETFEntityRenderState currentEntity = null;
    private static boolean limitModifyToProperties = false;

    private ETFRenderContext() {}

    @Nullable
    public static ETFEntityRenderState getCurrentEntityState() {
        return currentEntity;
    }

    public static void setCurrentEntity(ETFEntityRenderState state) {
        currentEntity = state;
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

    public static void reset() {
        currentEntity = null;
        limitModifyToProperties = false;
    }
}

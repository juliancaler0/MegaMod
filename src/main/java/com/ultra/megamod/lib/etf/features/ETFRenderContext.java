package com.ultra.megamod.lib.etf.features;

import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Supplier;

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

    /** Current ETFTexture — Phase C addition, set by the variator each frame so the
     *  emissive/enchant model-part overlay mixins know which texture's emissive variant
     *  to render. */
    private static com.ultra.megamod.lib.etf.features.texture_handlers.ETFTexture currentTexture = null;
    private static net.minecraft.client.renderer.MultiBufferSource currentBufferSource = null;
    private static net.minecraft.client.renderer.rendertype.RenderType currentRenderType = null;

    private static CompoundTag currentEntityNBT = null;
    private static UUID entityNBT_UUID = null;

    private ETFRenderContext() {}

    /**
     * Cache an entity's NBT snapshot for the current frame. Ported 1:1 from upstream —
     * used by {@code MixinEntity} / {@code MixinBlockEntity} to avoid re-serialising
     * the same entity's NBT multiple times per render tick.
     */
    public static CompoundTag cacheEntityNBTForFrame(UUID entityUUID, Supplier<CompoundTag> computeNBT) {
        if (currentEntityNBT == null || !entityUUID.equals(entityNBT_UUID)) {
            currentEntityNBT = computeNBT.get();
            entityNBT_UUID = entityUUID;
        }
        return currentEntityNBT;
    }

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
        currentEntityNBT = null;
        entityNBT_UUID = null;
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
        currentTexture = null;
        currentBufferSource = null;
        currentRenderType = null;
        allowedToPatch = false;
        allowRenderLayerTextureModify = true;
        limitModifyToProperties = false;
        currentEntityNBT = null;
        entityNBT_UUID = null;
    }

    // === Phase C: current ETFTexture bookkeeping for emissive/enchant overlays ===

    @Nullable
    public static com.ultra.megamod.lib.etf.features.texture_handlers.ETFTexture getCurrentTexture() {
        return currentTexture;
    }

    public static void setCurrentTexture(@Nullable com.ultra.megamod.lib.etf.features.texture_handlers.ETFTexture texture) {
        currentTexture = texture;
    }

    @Nullable
    public static net.minecraft.client.renderer.MultiBufferSource getCurrentBufferSource() {
        return currentBufferSource;
    }

    public static void setCurrentBufferSource(@Nullable net.minecraft.client.renderer.MultiBufferSource bufferSource) {
        currentBufferSource = bufferSource;
    }

    @Nullable
    public static net.minecraft.client.renderer.rendertype.RenderType getCurrentRenderType() {
        return currentRenderType;
    }

    public static void setCurrentRenderType(@Nullable net.minecraft.client.renderer.rendertype.RenderType renderType) {
        currentRenderType = renderType;
    }

    /**
     * True when we're currently rendering the main pass for an entity (not feature-renderer
     * overlays, not recursed from inside an emissive/enchant pass).
     */
    public static boolean isCurrentlyRenderingEntity() {
        return currentEntity != null && !isInSpecialRenderOverlayPhase;
    }
}

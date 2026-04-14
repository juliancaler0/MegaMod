package com.ultra.megamod.lib.etf.features.player;

import org.jetbrains.annotations.Nullable;

/**
 * Duck-typed onto {@link net.minecraft.client.player.AbstractClientPlayer} by
 * {@link com.ultra.megamod.lib.etf.mixin.MixinAbstractClientPlayerSkinHolder}.
 * <p>
 * Exposes the per-player {@link ETFPlayerTexture} so the player renderer's skin-feature
 * layer can retrieve it without doing its own cache lookups.
 */
public interface ETFPlayerSkinHolder {
    @Nullable
    ETFPlayerTexture etf$getETFPlayerTexture();

    default void etf$setETFPlayerTexture(@Nullable ETFPlayerTexture texture) {}
}

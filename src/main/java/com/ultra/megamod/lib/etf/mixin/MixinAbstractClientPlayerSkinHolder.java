package com.ultra.megamod.lib.etf.mixin;

import com.ultra.megamod.lib.etf.features.player.ETFPlayerSkinHolder;
import com.ultra.megamod.lib.etf.features.player.ETFPlayerTexture;
import net.minecraft.client.player.AbstractClientPlayer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds {@link ETFPlayerSkinHolder} to every {@link AbstractClientPlayer}, backed by a
 * single mutable field so the skin-feature renderer can cache per-player state.
 */
@Mixin(AbstractClientPlayer.class)
public abstract class MixinAbstractClientPlayerSkinHolder implements ETFPlayerSkinHolder {

    @Unique
    @Nullable
    private ETFPlayerTexture etf$playerTexture;

    @Override
    public @Nullable ETFPlayerTexture etf$getETFPlayerTexture() {
        return etf$playerTexture;
    }

    @Override
    public void etf$setETFPlayerTexture(@Nullable ETFPlayerTexture texture) {
        this.etf$playerTexture = texture;
    }
}

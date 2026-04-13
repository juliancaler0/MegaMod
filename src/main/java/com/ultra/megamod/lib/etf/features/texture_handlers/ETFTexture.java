package com.ultra.megamod.lib.etf.features.texture_handlers;

import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Phase B slim {@code ETFTexture}.
 * <p>
 * Upstream's {@code ETFTexture} owns emissive / enchanted / patched variants, native-image
 * caches, and per-entity overlay state. Those are deferred to Phase C.
 * <p>
 * Phase B keeps only what's needed to drive the basic texture-swap pipeline: a resolved
 * {@link Identifier} pointing to the variant PNG. The renderer mixin calls
 * {@link #getTextureIdentifier(ETFEntityRenderState)} and substitutes the result into the
 * {@code RenderType} before it gets built.
 */
public class ETFTexture {

    /** The vanilla (or variant-numbered) identifier this texture represents. */
    public final @NotNull Identifier thisIdentifier;

    public ETFTexture(@NotNull Identifier thisIdentifier) {
        this.thisIdentifier = thisIdentifier;
    }

    /**
     * Phase B: the texture identifier is unconditional — whatever the variator handed us.
     * Phase C will branch on emissive pass / patched state / entity-specific overlays.
     */
    @NotNull
    public Identifier getTextureIdentifier(@Nullable ETFEntityRenderState entity) {
        return thisIdentifier;
    }

    @Override
    public String toString() {
        return "ETFTexture{" + thisIdentifier + "}";
    }
}

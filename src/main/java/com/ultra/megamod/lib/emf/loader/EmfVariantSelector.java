package com.ultra.megamod.lib.emf.loader;

import com.ultra.megamod.lib.emf.utils.EMFUtils;
import com.ultra.megamod.lib.etf.ETFApi;
import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * Thin adapter that reuses ETF's existing predicate / random-selection engine for
 * picking a {@code .jem} model variant at runtime.
 * <p>
 * Upstream EMF ships its own properties parser for model swaps. In this port we avoid
 * duplication: the OptiFine {@code properties} format is already fully parsed by
 * {@link ETFApi#getVariantSupplierOrNull}. This class just wraps the result to return
 * a variant-suffixed model {@link Identifier} instead of a texture one.
 * <p>
 * Phase E wires this to the actual entity render state; in Phase D it stays purely
 * callable for future integration.
 */
public final class EmfVariantSelector {

    private final ETFApi.ETFVariantSuffixProvider provider;
    private final Identifier baseJem;

    private EmfVariantSelector(ETFApi.ETFVariantSuffixProvider provider, Identifier baseJem) {
        this.provider = provider;
        this.baseJem = baseJem;
    }

    /**
     * Builds a selector by reading a {@code .properties} file sitting next to
     * {@code baseJem}.
     * <p>
     * The properties file uses OptiFine {@code random entities} syntax; any predicate
     * that ETF understands (biome, health, NBT, mod-loaded, etc.) will route through
     * here via {@link com.ultra.megamod.lib.etf.features.property_reading}.
     * <p>
     * Returns {@code null} if no properties file exists or nothing matched — callers
     * should fall back to {@code baseJem} directly in that case.
     */
    @Nullable
    public static EmfVariantSelector of(Identifier baseJem) {
        Identifier propertiesPath = withExtension(baseJem, ".properties");
        if (propertiesPath == null) return null;

        // ETF takes the "vanilla texture" identifier to scan numbered neighbours
        // ({name}2.png, {name}3.png...). For .jem model swaps we re-use the same
        // numbered-neighbour scheme against the .jem path itself.
        ETFApi.ETFVariantSuffixProvider provider =
                ETFApi.getVariantSupplierOrNull(propertiesPath, baseJem, "models", "jems");

        if (provider == null) return null;
        return new EmfVariantSelector(provider, baseJem);
    }

    /**
     * Returns the variant-suffixed {@link Identifier} for {@code state}. If no variant
     * applies, returns the base model. If the selector couldn't be built, callers
     * should pass through to the base model themselves.
     * <p>
     * Phase F: consults {@link com.ultra.megamod.lib.emf.runtime.EmfEntityVariantCache}
     * so an entity with a given UUID gets the same variant until the configured
     * update-frequency window elapses.
     */
    public Identifier selectJem(ETFEntityRenderState state) {
        if (state == null) return baseJem;

        com.ultra.megamod.lib.emf.runtime.EmfEntityVariantCache cache =
                com.ultra.megamod.lib.emf.runtime.EmfEntityVariantCache.getInstance();
        java.util.UUID uuid = state.uuid();

        Integer cached = cache.getCachedSuffix(uuid, baseJem);
        if (cached != null && !cache.shouldReEvaluate(uuid, baseJem)) {
            return cached <= 1 ? baseJem : withSuffix(baseJem, cached);
        }

        int suffix = provider.getSuffixForETFEntity(state);
        cache.putSuffix(uuid, baseJem, suffix);
        if (suffix <= 1) return baseJem;
        return withSuffix(baseJem, suffix);
    }

    /** Replaces the extension of {@code id}'s path. Returns {@code null} if no dot found. */
    @Nullable
    private static Identifier withExtension(Identifier id, String newExtension) {
        String p = id.getPath();
        int dot = p.lastIndexOf('.');
        if (dot < 0) return null;
        return Identifier.fromNamespaceAndPath(id.getNamespace(), p.substring(0, dot) + newExtension);
    }

    /** Inserts a numeric suffix before the extension: {@code creeper.jem} -> {@code creeper2.jem}. */
    private static Identifier withSuffix(Identifier id, int suffix) {
        String p = id.getPath();
        int dot = p.lastIndexOf('.');
        if (dot < 0) {
            EMFUtils.logWarn("cannot attach variant suffix to " + id + " (no extension)");
            return id;
        }
        return Identifier.fromNamespaceAndPath(id.getNamespace(),
                p.substring(0, dot) + suffix + p.substring(dot));
    }
}

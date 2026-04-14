package com.ultra.megamod.lib.etf.features.texture_handlers;

import com.ultra.megamod.lib.etf.ETFApi;
import com.ultra.megamod.lib.etf.features.ETFManager;
import com.ultra.megamod.lib.etf.features.property_reading.PropertiesRandomProvider;
import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import com.ultra.megamod.lib.etf.utils.ETFUtils2;
import com.ultra.megamod.lib.etf.utils.EntityIntLRU;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Phase B slim {@code ETFTextureVariator}.
 * <p>
 * Owns a per-vanilla-texture variant map + per-entity suffix LRU. On
 * {@link #getVariantOf(ETFEntityRenderState)}:
 * <ol>
 *   <li>If the entity already has a cached suffix, return that variant's {@link ETFTexture}.</li>
 *   <li>Otherwise ask the {@link ETFApi.ETFVariantSuffixProvider} for a new suffix,
 *       cache it, and return the corresponding variant.</li>
 * </ol>
 * <p>
 * Ported 1:1 from upstream's {@code ETFTextureVariator.ETFTextureMultiple}, minus the
 * emissive / patched / debug logging paths that aren't in Phase B yet.
 */
public abstract class ETFTextureVariator {

    public static @NotNull ETFTextureVariator of(@NotNull Identifier vanillaIdentifier) {
        Identifier propertiesId = ETFUtils2.replaceIdentifier(vanillaIdentifier, "\\.png$", ".properties");
        ETFApi.ETFVariantSuffixProvider variantProvider = ETFApi.ETFVariantSuffixProvider.getVariantProviderOrNull(
                propertiesId, vanillaIdentifier, "skins", "textures");
        if (variantProvider != null) {
            return new Multiple(vanillaIdentifier, variantProvider);
        }
        return new Singleton(vanillaIdentifier);
    }

    public abstract @NotNull ETFTexture getVariantOf(@NotNull ETFEntityRenderState entity);

    protected abstract @NotNull Identifier getVanillaIdentifier();

    /** No variants — the base texture is all there is. */
    public static final class Singleton extends ETFTextureVariator {
        private final ETFTexture self;
        private final Identifier vanillaIdentifier;

        public Singleton(@NotNull Identifier vanillaIdentifier) {
            this.vanillaIdentifier = vanillaIdentifier;
            this.self = ETFManager.getInstance().getETFTextureNoVariation(vanillaIdentifier);
        }

        @Override
        public @NotNull ETFTexture getVariantOf(@NotNull ETFEntityRenderState entity) {
            return self;
        }

        @Override
        protected @NotNull Identifier getVanillaIdentifier() {
            return vanillaIdentifier;
        }
    }

    /** Has variants — driven by a {@link ETFApi.ETFVariantSuffixProvider}. */
    public static final class Multiple extends ETFTextureVariator {

        public final @NotNull EntityIntLRU entitySuffixMap = new EntityIntLRU(500);
        final @NotNull ETFApi.ETFVariantSuffixProvider suffixProvider;
        private final @NotNull Int2ObjectArrayMap<ETFTexture> variantMap = new Int2ObjectArrayMap<>();
        private final @NotNull Identifier vanillaIdentifier;

        Multiple(@NotNull Identifier vanillaIdentifier, @NotNull ETFApi.ETFVariantSuffixProvider suffixProvider) {
            this.vanillaIdentifier = vanillaIdentifier;
            this.suffixProvider = suffixProvider;
            entitySuffixMap.defaultReturnValue(-1);

            // upstream: hook the properties provider so LAST_RULE_INDEX_OF_ENTITY stays current
            if (suffixProvider instanceof PropertiesRandomProvider propertiesProvider) {
                propertiesProvider.setOnMeetsRuleHook((entity, rule) -> {
                    if (rule == null) {
                        ETFManager.getInstance().LAST_RULE_INDEX_OF_ENTITY.removeInt(entity.uuid());
                    } else {
                        ETFManager.getInstance().LAST_RULE_INDEX_OF_ENTITY.put(entity.uuid(), rule.ruleNumber);
                    }
                });
            }

            // base variant (suffix 1) — uses the directorized version if it exists
            Identifier directorized = ETFDirectory.getDirectoryVersionOf(vanillaIdentifier);
            ETFTexture vanilla = ETFManager.getInstance().getETFTextureNoVariation(
                    directorized == null ? vanillaIdentifier : directorized);
            variantMap.put(1, vanilla);
            variantMap.defaultReturnValue(vanilla);

            // resolve each additional variant's actual identifier
            IntOpenHashSet suffixes = suffixProvider.getAllSuffixes();
            suffixes.remove(0);
            suffixes.remove(1);
            for (int suffix : suffixes) {
                Identifier withSuffix = ETFUtils2.addVariantNumberSuffix(vanillaIdentifier, suffix);
                Identifier variant = withSuffix == null ? null : ETFDirectory.getDirectoryVersionOf(withSuffix);
                if (variant != null) {
                    variantMap.put(suffix, ETFManager.getInstance().getETFTextureNoVariation(variant));
                } else {
                    variantMap.put(suffix, vanilla);
                }
            }
        }

        @Override
        public @NotNull ETFTexture getVariantOf(@NotNull ETFEntityRenderState entity) {
            UUID id = entity.uuid();
            int knownSuffix = entitySuffixMap.getInt(id);
            if (knownSuffix != -1) {
                return variantMap.get(knownSuffix);
            }
            int newSuffix = suffixProvider.getSuffixForETFEntity(entity);
            entitySuffixMap.put(id, newSuffix);
            ETFManager.getInstance().LAST_SUFFIX_OF_ENTITY.put(id, newSuffix);
            return variantMap.get(newSuffix);
        }

        @Override
        protected @NotNull Identifier getVanillaIdentifier() {
            return vanillaIdentifier;
        }
    }
}

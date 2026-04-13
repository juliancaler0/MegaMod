package com.ultra.megamod.lib.etf.features;

import com.ultra.megamod.lib.etf.ETFApi;
import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import com.ultra.megamod.lib.etf.features.texture_handlers.ETFDirectory;
import com.ultra.megamod.lib.etf.features.texture_handlers.ETFTexture;
import com.ultra.megamod.lib.etf.features.texture_handlers.ETFTextureVariator;
import com.ultra.megamod.lib.etf.utils.EntityIntLRU;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackResources;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

/**
 * Phase B {@code ETFManager}.
 * <p>
 * Phase A only held the state the parser / selector read. Phase B adds the
 * texture-swap machinery:
 * <ul>
 *   <li>{@link #ETF_TEXTURE_CACHE} — de-duped {@link ETFTexture} instances keyed by
 *       resolved identifier.</li>
 *   <li>{@link #VARIATOR_MAP} — one variator per vanilla base texture.</li>
 *   <li>{@link #getETFTextureVariant(Identifier, ETFEntityRenderState)} — the render
 *       mixin's entry point, returns the variant texture for an entity.</li>
 *   <li>{@link #getETFTextureNoVariation(Identifier)} — interned {@link ETFTexture}
 *       lookup for a bare identifier (the "just the texture file itself" path).</li>
 * </ul>
 * <p>
 * Emissive suffix list, patched texture state, player texture map, and the extra render
 * modes are all still Phase C — this stays a slim facade over the variator map.
 */
public class ETFManager {

    private static ETFManager instance;

    public final ArrayList<String> KNOWN_RESOURCEPACK_ORDER = new ArrayList<>();
    public final HashMap<@NotNull Identifier, @NotNull ETFDirectory> ETF_DIRECTORY_CACHE = new HashMap<>();
    public final EntityIntLRU LAST_SUFFIX_OF_ENTITY = new EntityIntLRU();
    public final EntityIntLRU LAST_RULE_INDEX_OF_ENTITY = new EntityIntLRU();

    /** Interned {@link ETFTexture} objects per resolved identifier. */
    public final HashMap<@NotNull Identifier, @NotNull ETFTexture> ETF_TEXTURE_CACHE = new HashMap<>();
    /** One variator per vanilla base texture. */
    private final HashMap<@NotNull Identifier, @NotNull ETFTextureVariator> VARIATOR_MAP = new HashMap<>();

    private ETFManager() {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.getResourceManager() != null) {
            for (PackResources pack : mc.getResourceManager().listPacks().toList()) {
                KNOWN_RESOURCEPACK_ORDER.add(pack.packId());
            }
        }
    }

    public static ETFManager getInstance() {
        if (instance == null) {
            instance = new ETFManager();
        }
        return instance;
    }

    /**
     * Replace the singleton. Called by the resource-pack reload listener and by
     * client world disconnect so reload-fresh state picks up new properties/textures.
     * <p>
     * Upstream deliberately builds a new instance rather than clearing in place so
     * in-flight reads on the old instance keep working.
     */
    public static void resetInstance() {
        instance = new ETFManager();
    }

    /**
     * Phase A no-op. Upstream reads {@code vanillaBrightnessOverride}, {@code suppressParticles},
     * and {@code entityRenderLayerOverride} into the config and applies them at render time.
     * Those features land in Phase C when the full config object comes online.
     */
    public void grabSpecialProperties(Properties props, ETFEntityRenderState entity) {
        // intentional no-op for Phase A/B
    }

    /**
     * Intern an {@link ETFTexture} for the given identifier. Never returns null —
     * if the identifier doesn't exist in the resource pack, the caller is still given a
     * fresh object wrapping the missing identifier (rendering falls back to the vanilla
     * texture loader's missing-texture behaviour).
     */
    @NotNull
    public ETFTexture getETFTextureNoVariation(@NotNull Identifier ofIdentifier) {
        ETFTexture cached = ETF_TEXTURE_CACHE.get(ofIdentifier);
        if (cached != null) return cached;
        ETFTexture created = new ETFTexture(ofIdentifier);
        ETF_TEXTURE_CACHE.put(ofIdentifier, created);
        return created;
    }

    /**
     * Resolve the variant texture for an entity rendering {@code vanillaIdentifier}.
     * <p>
     * Mirrors upstream behaviour: bails out to the no-variation path for generic /
     * position-less entities, otherwise caches a variator per vanilla texture and
     * delegates to it.
     */
    @NotNull
    public ETFTexture getETFTextureVariant(@NotNull Identifier vanillaIdentifier, @Nullable ETFEntityRenderState entity) {
        if (entity == null
                || ETFApi.ETF_GENERIC_UUID.equals(entity.uuid())
                || (entity.blockPos() != null
                    && entity.blockPos().equals(Vec3i.ZERO)
                    && entity.uuid().getLeastSignificantBits() != ETFApi.ETF_SPAWNER_MARKER)) {
            return getETFTextureNoVariation(vanillaIdentifier);
        }

        ETFTextureVariator variator = VARIATOR_MAP.get(vanillaIdentifier);
        if (variator == null) {
            variator = ETFTextureVariator.of(vanillaIdentifier);
            VARIATOR_MAP.put(vanillaIdentifier, variator);
        }
        return variator.getVariantOf(entity);
    }
}

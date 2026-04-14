package com.ultra.megamod.lib.emf.runtime;

import com.ultra.megamod.lib.emf.EMF;
import com.ultra.megamod.lib.emf.animation.EmfModelDefinition;
import com.ultra.megamod.lib.emf.jem.EmfJemData;
import com.ultra.megamod.lib.emf.loader.EmfModelLoader;
import com.ultra.megamod.lib.emf.loader.EmfVariantSelector;
import com.ultra.megamod.lib.emf.utils.EMFUtils;
import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central singleton that tracks discovered {@code .jem} files, their compiled
 * {@link EmfModelDefinition}s, and their optional {@link EmfVariantSelector}s.
 * <p>
 * Lifecycle:
 * <ol>
 *   <li>Client init calls {@link #getInstance()} to create the manager eagerly so
 *       it can sit under the resource reload listener.</li>
 *   <li>Entity renderer registration (via {@code EmfEntityRenderersMixin}) records
 *       a short-lived {@link #currentSpecifiedModelLoading} marker that maps
 *       transient entity types (boats, spectral arrows, husks) to a stable key.</li>
 *   <li>The first {@code EntityModel.setupAnim} per frame calls
 *       {@link #bindFor(String, boolean)} — the method returns a cached
 *       {@link EmfActiveModel} or attempts to load one from the pack.</li>
 *   <li>{@link #resetAll()} is invoked by
 *       {@link com.ultra.megamod.lib.emf.EmfReloadListener} on every resource pack
 *       reload.</li>
 * </ol>
 * Upstream's equivalent is {@code EMFManager} (~870 lines). We keep only the public
 * surface Phase E needs; additional state (per-entity variant cache, per-model
 * shadow-size override) will live here too when Phase F adds them.
 */
public final class EmfModelManager {

    private static volatile EmfModelManager instance;

    /**
     * Short-lived string set by the renderer-init mixin, cleared at the end of the
     * same registration call. Encodes transient entity types that would otherwise
     * share a model class name — matches upstream {@code EMFManager.currentSpecifiedModelLoading}.
     */
    public volatile String currentSpecifiedModelLoading = "";

    private final Map<String, CacheEntry> byEntityTypeKey = new ConcurrentHashMap<>();
    private final Map<String, Boolean> missing = new ConcurrentHashMap<>();

    private EmfModelManager() {
    }

    public static EmfModelManager getInstance() {
        EmfModelManager local = instance;
        if (local == null) {
            synchronized (EmfModelManager.class) {
                local = instance;
                if (local == null) {
                    local = new EmfModelManager();
                    instance = local;
                }
            }
        }
        return local;
    }

    /** Clears every cached model/definition/variant. Called from the reload listener. */
    public static void resetAll() {
        EmfModelManager local = instance;
        if (local != null) {
            local.byEntityTypeKey.clear();
            local.missing.clear();
        }
        // Phase F: invalidate the per-entity UUID variant cache too — new pack state means
        // every entity re-picks its variant on the next render frame.
        EmfEntityVariantCache.getInstance().clear();
    }

    /**
     * Returns an active EMF binding for the given {@code entityTypeKey} (vanilla
     * entity registry path, e.g. {@code "creeper"}). Returns {@code null} if no
     * {@code .jem} ships in the pack or if parsing failed.
     *
     * @param entityTypeKey e.g. {@code minecraft:creeper -> "creeper"}.
     * @param perFrame      if {@code true}, returns the cached result and does not
     *                      attempt to re-scan. Phase E callers pass {@code true}.
     */
    @Nullable
    public EmfActiveModel bindFor(String entityTypeKey, boolean perFrame) {
        if (entityTypeKey == null || entityTypeKey.isEmpty()) return null;
        CacheEntry hit = byEntityTypeKey.get(entityTypeKey);
        if (hit != null) return hit.active;
        if (perFrame && missing.containsKey(entityTypeKey)) return null;
        return loadAndCache(entityTypeKey);
    }

    /**
     * Same as {@link #bindFor(String, boolean)} but additionally resolves a per-entity
     * variant via {@link EmfVariantSelector} when the pack ships a sibling
     * {@code .properties} file.
     */
    @Nullable
    public EmfActiveModel bindForEntity(String entityTypeKey, @Nullable ETFEntityRenderState state) {
        CacheEntry entry = findOrLoad(entityTypeKey);
        if (entry == null || entry.active == null) return null;
        if (entry.variantSelector == null || state == null) return entry.active;

        Identifier variantId = entry.variantSelector.selectJem(state);
        if (variantId == null || variantId.equals(entry.active.sourceJemId)) return entry.active;

        // Separate key per variant so each variant builds its own cached definition
        String variantKey = entityTypeKey + "#" + variantId;
        CacheEntry variantEntry = byEntityTypeKey.computeIfAbsent(variantKey, k -> compileOne(variantId));
        return variantEntry == null ? entry.active : variantEntry.active;
    }

    @Nullable
    private CacheEntry findOrLoad(String entityTypeKey) {
        CacheEntry hit = byEntityTypeKey.get(entityTypeKey);
        if (hit != null) return hit;
        if (missing.containsKey(entityTypeKey)) return null;
        loadAndCache(entityTypeKey);
        return byEntityTypeKey.get(entityTypeKey);
    }

    @Nullable
    private synchronized EmfActiveModel loadAndCache(String entityTypeKey) {
        // Another thread may have raced in
        CacheEntry existing = byEntityTypeKey.get(entityTypeKey);
        if (existing != null) return existing.active;

        Identifier jemId = Identifier.fromNamespaceAndPath(
                "minecraft", "optifine/cem/" + entityTypeKey + ".jem");

        CacheEntry entry = compileOne(jemId);
        if (entry == null) {
            missing.put(entityTypeKey, Boolean.TRUE);
            return null;
        }
        byEntityTypeKey.put(entityTypeKey, entry);
        return entry.active;
    }

    @Nullable
    private CacheEntry compileOne(Identifier jemId) {
        ResourceManager rm = resourceManager();
        if (rm == null) return null;

        EmfJemData jem;
        try {
            jem = EmfModelLoader.loadJem(jemId, rm);
        } catch (Throwable t) {
            EMFUtils.logError("jem load threw for " + jemId + ": " + t);
            return null;
        }
        if (jem == null) return null;

        EmfModelDefinition def;
        try {
            def = EmfModelDefinition.compile(jem);
        } catch (Throwable t) {
            EMFUtils.logError("jem compile failed for " + jemId + ": " + t);
            return null;
        }

        Identifier textureOverride = null;
        if (jem.texture != null && !jem.texture.isBlank()) {
            Identifier resolved = Identifier.tryParse(jem.texture);
            if (resolved != null) textureOverride = resolved;
        }

        EmfActiveModel active = new EmfActiveModel(jem, def, jemId, textureOverride);
        EmfVariantSelector selector = EmfVariantSelector.of(jemId);
        if (EMF.logModelCreationData) {
            EMFUtils.log("EMF active model built for " + jemId
                    + " (variants=" + (selector != null) + ")");
        }
        return new CacheEntry(active, selector);
    }

    @Nullable
    private static ResourceManager resourceManager() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null) return null;
            return mc.getResourceManager();
        } catch (Throwable t) {
            return null;
        }
    }

    /** Read-only snapshot (for debug overlays / tests). */
    public Map<String, CacheEntry> snapshot() {
        return Collections.unmodifiableMap(byEntityTypeKey);
    }

    /** Bundled cache record — one per entity-type key. */
    public record CacheEntry(EmfActiveModel active, @Nullable EmfVariantSelector variantSelector) {
    }
}

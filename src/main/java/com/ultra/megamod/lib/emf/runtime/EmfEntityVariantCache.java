package com.ultra.megamod.lib.emf.runtime;

import com.ultra.megamod.lib.emf.config.EMFConfig;
import com.ultra.megamod.lib.emf.EMF;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-entity UUID-hash variant cache for EMF.
 * <p>
 * Upstream EMF's {@code EMFManager.lastModelSuffixOfEntity} /
 * {@code lastModelRuleOfEntity} fulfil this role: once an entity's UUID resolves
 * to a variant (via the {@code .properties} predicate stack), the same UUID gets
 * the same variant on every subsequent frame until a re-evaluation interval
 * elapses or a predicate input (health, age, etc.) changes enough to trip a
 * different rule.
 * <p>
 * We keep two maps:
 * <ul>
 *   <li>{@link #suffixByEntity} — the winning variant suffix per (UUID,jemKey).
 *       {@code 1} = base, {@code 2..N} = numbered variants matching the pack
 *       file layout ({@code creeper2.jem}, {@code creeper3.jem}, ...).</li>
 *   <li>{@link #lastEvaluatedAt} — tick stamp of the last re-evaluation. Bulk
 *       lookups within the same tick skip recomputation.</li>
 * </ul>
 * <p>
 * Both maps are cleared on resource reload (through
 * {@link EmfModelManager#resetAll()}) and drop their oldest entries when the
 * soft cap is exceeded so long-running worlds don't leak memory for despawned
 * entities.
 */
public final class EmfEntityVariantCache {

    /** Soft cap beyond which the oldest quarter of entries is evicted. */
    private static final int SOFT_CAP = 4096;

    private static final EmfEntityVariantCache INSTANCE = new EmfEntityVariantCache();

    private final ConcurrentHashMap<Key, Integer> suffixByEntity = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Key, Long> lastEvaluatedAt = new ConcurrentHashMap<>();
    private volatile long currentTick = 0L;

    private EmfEntityVariantCache() {
    }

    public static EmfEntityVariantCache getInstance() {
        return INSTANCE;
    }

    /** Clears all cached bindings. Called from {@link EmfModelManager#resetAll()}. */
    public void clear() {
        suffixByEntity.clear();
        lastEvaluatedAt.clear();
    }

    /** Bumped externally once per client tick so {@link #shouldReEvaluate(UUID, Identifier)} has a time base. */
    public void setTick(long tick) {
        this.currentTick = tick;
    }

    /** Returns the cached suffix for this UUID+jem, or {@code null} if nothing bound yet. */
    @Nullable
    public Integer getCachedSuffix(UUID uuid, Identifier jemKey) {
        if (uuid == null || jemKey == null) return null;
        return suffixByEntity.get(new Key(uuid, jemKey));
    }

    /** Records a selected suffix for this entity. */
    public void putSuffix(UUID uuid, Identifier jemKey, int suffix) {
        if (uuid == null || jemKey == null) return;
        Key k = new Key(uuid, jemKey);
        suffixByEntity.put(k, suffix);
        lastEvaluatedAt.put(k, currentTick);
        maybeEvict();
    }

    /**
     * Returns true if the configured update frequency window has elapsed for this
     * entity+jem pair. Callers should pick a fresh variant in that case.
     */
    public boolean shouldReEvaluate(UUID uuid, Identifier jemKey) {
        if (uuid == null || jemKey == null) return true;
        Long last = lastEvaluatedAt.get(new Key(uuid, jemKey));
        if (last == null) return true;
        EMFConfig cfg;
        try {
            cfg = EMF.config().getConfig();
        } catch (Throwable t) {
            return false;
        }
        int ticksBetween = cfg.modelUpdateFrequency == null
                ? EMFConfig.UpdateFrequency.AVERAGE.ticksBetweenUpdates
                : cfg.modelUpdateFrequency.ticksBetweenUpdates;
        return (currentTick - last) >= ticksBetween;
    }

    /** Drops cache entries for a single entity (e.g. on despawn). */
    public void invalidate(UUID uuid) {
        if (uuid == null) return;
        suffixByEntity.keySet().removeIf(k -> k.uuid().equals(uuid));
        lastEvaluatedAt.keySet().removeIf(k -> k.uuid().equals(uuid));
    }

    private void maybeEvict() {
        if (suffixByEntity.size() <= SOFT_CAP) return;
        // Drop the oldest quarter by tick.
        int toDrop = suffixByEntity.size() / 4;
        if (toDrop <= 0) return;
        // Copy entries into a simple array and sort by tick ascending.
        var entries = lastEvaluatedAt.entrySet().stream()
                .sorted(java.util.Map.Entry.comparingByValue())
                .limit(toDrop)
                .toList();
        for (var e : entries) {
            suffixByEntity.remove(e.getKey());
            lastEvaluatedAt.remove(e.getKey());
        }
    }

    /** Composite key: {@code (uuid, jemKey)}. */
    private record Key(UUID uuid, Identifier jemKey) {
    }
}

package com.ultra.megamod.lib.emf.runtime;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cross-frame storage for user-defined animation variables.
 * <p>
 * Upstream EMF persists {@code var.X} and {@code global_var.X} so that expressions
 * can reference values computed on the previous frame. Our per-context map is
 * per-frame only; this helper adds the persistent tier.
 * <ul>
 *   <li>{@link #perEntity} — keyed by {@code (uuid, name)}. Cleared on resource
 *       reload alongside the variant cache.</li>
 *   <li>{@link #globals} — keyed by {@code name}. Shared across every EMF model
 *       on the client. Also cleared on reload.</li>
 * </ul>
 */
public final class EmfPerEntityVariables {

    private static final ConcurrentHashMap<Key, Float> perEntity = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Float> globals = new ConcurrentHashMap<>();

    private EmfPerEntityVariables() {
    }

    public static float getEntity(UUID uuid, String name) {
        if (uuid == null || name == null) return 0f;
        Float f = perEntity.get(new Key(uuid, name));
        return f == null ? 0f : f;
    }

    public static void setEntity(UUID uuid, String name, float value) {
        if (uuid == null || name == null) return;
        perEntity.put(new Key(uuid, name), value);
    }

    public static float getGlobal(String name) {
        if (name == null) return 0f;
        Float f = globals.get(name);
        return f == null ? 0f : f;
    }

    public static void setGlobal(String name, float value) {
        if (name == null) return;
        globals.put(name, value);
    }

    public static void clearAll() {
        perEntity.clear();
        globals.clear();
    }

    public static void invalidate(UUID uuid) {
        if (uuid == null) return;
        perEntity.keySet().removeIf(k -> k.uuid().equals(uuid));
    }

    private record Key(UUID uuid, String name) {
    }
}

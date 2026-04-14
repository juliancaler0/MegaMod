package com.ultra.megamod.lib.emf.utils;

import com.ultra.megamod.lib.emf.EMF;

import java.util.Set;

/**
 * Small collection of helpers that EMF-internal code uses everywhere.
 * Ported (trimmed) from {@code traben.entity_model_features.utils.EMFUtils}.
 */
public final class EMFUtils {

    private EMFUtils() {
    }

    public static void log(String message) {
        if (EMF.logModelCreationData) EMF.LOGGER.info(message);
    }

    public static void logWarn(String message) {
        EMF.LOGGER.warn(message);
    }

    public static void logError(String message) {
        EMF.LOGGER.error(message);
    }

    /**
     * Returns {@code id} if it doesn't collide with {@code existing}; otherwise appends
     * {@code _2}, {@code _3}, ... until a fresh id is found.
     * <p>
     * Mirrors the upstream {@code getIdUnique} helper used while preparing part data.
     */
    public static String getIdUnique(Set<String> existing, String id) {
        if (id == null || id.isEmpty()) return id;
        if (!existing.contains(id)) return id;
        int suffix = 2;
        while (existing.contains(id + "_" + suffix)) suffix++;
        return id + "_" + suffix;
    }
}

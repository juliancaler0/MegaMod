package com.ultra.megamod.feature.citizen.building;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Central registry of all building types.
 * Building entries are registered during mod initialization and can be
 * looked up by ID at runtime. Individual buildings (concrete subclasses)
 * are registered in Phase 1.3.
 */
public class BuildingRegistry {

    private static final Map<String, BuildingEntry> ENTRIES = new LinkedHashMap<>();

    /**
     * Registers a building entry. Called during mod init to register all building types.
     *
     * @param entry the building entry to register
     * @throws IllegalArgumentException if an entry with the same ID is already registered
     */
    public static void register(BuildingEntry entry) {
        if (ENTRIES.containsKey(entry.id())) {
            throw new IllegalArgumentException("Duplicate building entry: " + entry.id());
        }
        ENTRIES.put(entry.id(), entry);
    }

    /**
     * Gets a building entry by its unique ID.
     *
     * @param id the building type ID (e.g., "residence", "baker")
     * @return the building entry, or null if not found
     */
    public static BuildingEntry get(String id) {
        return ENTRIES.get(id);
    }

    /**
     * Returns an unmodifiable collection of all registered building entries.
     *
     * @return all building entries
     */
    public static Collection<BuildingEntry> getAll() {
        return Collections.unmodifiableCollection(ENTRIES.values());
    }

    /**
     * Returns the number of registered building types.
     *
     * @return the count
     */
    public static int size() {
        return ENTRIES.size();
    }

    /**
     * Checks whether a building type with the given ID is registered.
     *
     * @param id the building type ID
     * @return true if registered
     */
    public static boolean contains(String id) {
        return ENTRIES.containsKey(id);
    }
}

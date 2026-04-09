package com.ultra.megamod.feature.citizen.building;

import java.util.function.Supplier;

/**
 * Registry entry describing a building type.
 * Each building type (residence, bakery, mine, etc.) gets one BuildingEntry
 * that defines its ID, display name, max level, and factories for creating
 * building logic and hut block instances.
 *
 * @param id               unique string identifier (e.g., "residence", "baker")
 * @param displayName      human-readable name (e.g., "Residence", "Bakery")
 * @param maxLevel         maximum upgrade level, typically 5
 * @param buildingFactory  supplier that creates a new AbstractBuilding instance
 * @param hutBlockFactory  supplier that creates a new AbstractBlockHut instance
 */
public record BuildingEntry(
        String id,
        String displayName,
        int maxLevel,
        Supplier<AbstractBuilding> buildingFactory,
        Supplier<AbstractBlockHut<?>> hutBlockFactory
) {
}

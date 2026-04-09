package com.ultra.megamod.feature.citizen.building.module;

import java.util.List;
import java.util.UUID;

/**
 * Module interface for buildings that track a list of entities.
 * Used for buildings like guard towers (tracking hostile mobs),
 * animal farms (tracking livestock), etc.
 */
public interface IEntityListModule extends IBuildingModule {

    /**
     * Returns the list of tracked entity UUIDs.
     *
     * @return unmodifiable list of entity UUIDs
     */
    List<UUID> getEntityList();

    /**
     * Adds an entity to the tracking list.
     *
     * @param entityId the UUID of the entity to track
     * @return true if the entity was added, false if already tracked
     */
    boolean addEntity(UUID entityId);

    /**
     * Removes an entity from the tracking list.
     *
     * @param entityId the UUID of the entity to remove
     */
    void removeEntity(UUID entityId);
}

package com.ultra.megamod.feature.citizen.building.module;

/**
 * Module interface for buildings that react to lifecycle events.
 * Provides hooks for construction, upgrades, repairs, and demolition.
 */
public interface IBuildingEventsModule extends IBuildingModule {

    /**
     * Called when the building is first constructed.
     *
     * @param level the building level it was built at
     */
    void onBuildingBuilt(int level);

    /**
     * Called when the building is upgraded to a new level.
     *
     * @param oldLevel the previous building level
     * @param newLevel the new building level
     */
    void onBuildingUpgraded(int oldLevel, int newLevel);

    /**
     * Called when the building is repaired after damage.
     */
    void onBuildingRepaired();

    /**
     * Called when the building is removed or demolished.
     */
    void onBuildingRemoved();
}

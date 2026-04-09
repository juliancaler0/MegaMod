package com.ultra.megamod.feature.citizen.building.buildings;

import com.ultra.megamod.feature.citizen.building.AbstractBuilding;

/**
 * Town Hall — the administrative center of the colony. No worker.
 * Required to found a colony.
 */
public class BuildingTownHall extends AbstractBuilding {

    @Override
    public String getBuildingId() {
        return "town_hall";
    }

    @Override
    public String getDisplayName() {
        return "Town Hall";
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    protected void registerModules() {
        // No worker — management building
    }
}

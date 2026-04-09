package com.ultra.megamod.feature.citizen.building.buildings;

import com.ultra.megamod.feature.citizen.building.AbstractBuilding;

/**
 * Tavern — attracts visitors to the colony. No worker.
 */
public class BuildingTavern extends AbstractBuilding {

    @Override
    public String getBuildingId() {
        return "tavern";
    }

    @Override
    public String getDisplayName() {
        return "Tavern";
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    protected void registerModules() {
        // No worker — visitors come on their own
    }
}

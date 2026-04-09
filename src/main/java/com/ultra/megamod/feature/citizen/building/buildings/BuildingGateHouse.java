package com.ultra.megamod.feature.citizen.building.buildings;

import com.ultra.megamod.feature.citizen.building.AbstractBuilding;

/**
 * Gate House — defensive gate structure, no worker.
 */
public class BuildingGateHouse extends AbstractBuilding {

    @Override
    public String getBuildingId() {
        return "gate_house";
    }

    @Override
    public String getDisplayName() {
        return "Gate House";
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    protected void registerModules() {
        // No worker — purely defensive structure
    }
}

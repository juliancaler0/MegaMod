package com.ultra.megamod.feature.citizen.building.buildings;

import com.ultra.megamod.feature.citizen.building.AbstractBuilding;
import com.ultra.megamod.feature.citizen.building.module.CraftingBuildingModule;
import com.ultra.megamod.feature.citizen.building.module.WorkerBuildingModule;

import com.ultra.megamod.feature.citizen.data.CitizenJob;

/**
 * Mechanic's Shop building implementation.
 */
public class BuildingMechanic extends AbstractBuilding {

    @Override
    public String getBuildingId() {
        return "mechanic";
    }

    @Override
    public String getDisplayName() {
        return "Mechanic's Shop";
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    protected void registerModules() {
        addModule(new WorkerBuildingModule(CitizenJob.MECHANIC, 1));
        addModule(new CraftingBuildingModule("mechanic", 10));
    }
}

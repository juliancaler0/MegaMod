package com.ultra.megamod.feature.citizen.building.buildings;

import com.ultra.megamod.feature.citizen.building.AbstractBuilding;
import com.ultra.megamod.feature.citizen.building.module.CraftingBuildingModule;
import com.ultra.megamod.feature.citizen.building.module.WorkerBuildingModule;

import com.ultra.megamod.feature.citizen.data.CitizenJob;

/**
 * Sawmill building implementation.
 */
public class BuildingSawmill extends AbstractBuilding {

    @Override
    public String getBuildingId() {
        return "sawmill";
    }

    @Override
    public String getDisplayName() {
        return "Sawmill";
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    protected void registerModules() {
        addModule(new WorkerBuildingModule(CitizenJob.SAWMILL, 1));
        addModule(new CraftingBuildingModule("sawmill", 10));
    }
}

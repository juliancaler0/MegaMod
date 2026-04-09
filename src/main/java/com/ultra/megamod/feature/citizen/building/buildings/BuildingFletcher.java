package com.ultra.megamod.feature.citizen.building.buildings;

import com.ultra.megamod.feature.citizen.building.AbstractBuilding;
import com.ultra.megamod.feature.citizen.building.module.CraftingBuildingModule;
import com.ultra.megamod.feature.citizen.building.module.WorkerBuildingModule;

import com.ultra.megamod.feature.citizen.data.CitizenJob;

/**
 * Fletcher's Hut building implementation.
 */
public class BuildingFletcher extends AbstractBuilding {

    @Override
    public String getBuildingId() {
        return "fletcher";
    }

    @Override
    public String getDisplayName() {
        return "Fletcher's Hut";
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    protected void registerModules() {
        addModule(new WorkerBuildingModule(CitizenJob.FLETCHER, 1));
        addModule(new CraftingBuildingModule("fletcher", 10));
    }
}

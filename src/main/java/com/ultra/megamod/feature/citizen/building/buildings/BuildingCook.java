package com.ultra.megamod.feature.citizen.building.buildings;

import com.ultra.megamod.feature.citizen.building.AbstractBuilding;
import com.ultra.megamod.feature.citizen.building.module.CraftingBuildingModule;
import com.ultra.megamod.feature.citizen.building.module.WorkerBuildingModule;

import com.ultra.megamod.feature.citizen.data.CitizenJob;

/**
 * Restaurant building implementation.
 */
public class BuildingCook extends AbstractBuilding {

    @Override
    public String getBuildingId() {
        return "cook";
    }

    @Override
    public String getDisplayName() {
        return "Restaurant";
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    protected void registerModules() {
        addModule(new WorkerBuildingModule(CitizenJob.COOK, 1));
        addModule(new CraftingBuildingModule("cook", 10));
    }
}

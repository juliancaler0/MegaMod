package com.ultra.megamod.feature.citizen.building.buildings;

import com.ultra.megamod.feature.citizen.building.AbstractBuilding;
import com.ultra.megamod.feature.citizen.building.module.CraftingBuildingModule;
import com.ultra.megamod.feature.citizen.building.module.WorkerBuildingModule;

import com.ultra.megamod.feature.citizen.data.CitizenJob;

/**
 * Dye Shop building implementation.
 */
public class BuildingDyer extends AbstractBuilding {

    @Override
    public String getBuildingId() {
        return "dyer";
    }

    @Override
    public String getDisplayName() {
        return "Dye Shop";
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    protected void registerModules() {
        addModule(new WorkerBuildingModule(CitizenJob.DYE_WORKER, 1));
        addModule(new CraftingBuildingModule("dyer", 10));
    }
}

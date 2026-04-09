package com.ultra.megamod.feature.citizen.building.buildings;

import com.ultra.megamod.feature.citizen.building.AbstractBuilding;
import com.ultra.megamod.feature.citizen.building.module.CraftingBuildingModule;
import com.ultra.megamod.feature.citizen.building.module.WorkerBuildingModule;

import com.ultra.megamod.feature.citizen.data.CitizenJob;

/**
 * Smeltery building implementation.
 */
public class BuildingSmeltery extends AbstractBuilding {

    @Override
    public String getBuildingId() {
        return "smeltery";
    }

    @Override
    public String getDisplayName() {
        return "Smeltery";
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    protected void registerModules() {
        addModule(new WorkerBuildingModule(CitizenJob.SMELTER, 1));
        addModule(new CraftingBuildingModule("smeltery", 10));
    }
}

package com.ultra.megamod.feature.citizen.building.buildings;

import com.ultra.megamod.feature.citizen.building.AbstractBuilding;
import com.ultra.megamod.feature.citizen.building.module.CraftingBuildingModule;
import com.ultra.megamod.feature.citizen.building.module.WorkerBuildingModule;

import com.ultra.megamod.feature.citizen.data.CitizenJob;

/**
 * Bakery building implementation.
 */
public class BuildingBaker extends AbstractBuilding {

    @Override
    public String getBuildingId() {
        return "baker";
    }

    @Override
    public String getDisplayName() {
        return "Bakery";
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    protected void registerModules() {
        addModule(new WorkerBuildingModule(CitizenJob.BAKER, 1));
        addModule(new CraftingBuildingModule("baker", 10));
    }
}

package com.ultra.megamod.feature.citizen.building.buildings;

import com.ultra.megamod.feature.citizen.building.AbstractBuilding;
import com.ultra.megamod.feature.citizen.building.module.CraftingBuildingModule;
import com.ultra.megamod.feature.citizen.building.module.WorkerBuildingModule;

import com.ultra.megamod.feature.citizen.data.CitizenJob;

/**
 * Stone Smeltery building implementation.
 */
public class BuildingStoneSmeltery extends AbstractBuilding {

    @Override
    public String getBuildingId() {
        return "stone_smeltery";
    }

    @Override
    public String getDisplayName() {
        return "Stone Smeltery";
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    protected void registerModules() {
        addModule(new WorkerBuildingModule(CitizenJob.STONE_SMELTER, 1));
        addModule(new CraftingBuildingModule("stone_smeltery", 10));
    }
}

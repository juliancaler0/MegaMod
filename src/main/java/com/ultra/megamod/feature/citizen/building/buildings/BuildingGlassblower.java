package com.ultra.megamod.feature.citizen.building.buildings;

import com.ultra.megamod.feature.citizen.building.AbstractBuilding;
import com.ultra.megamod.feature.citizen.building.module.CraftingBuildingModule;
import com.ultra.megamod.feature.citizen.building.module.WorkerBuildingModule;

import com.ultra.megamod.feature.citizen.data.CitizenJob;

/**
 * Glassblower's Hut building implementation.
 */
public class BuildingGlassblower extends AbstractBuilding {

    @Override
    public String getBuildingId() {
        return "glassblower";
    }

    @Override
    public String getDisplayName() {
        return "Glassblower's Hut";
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    protected void registerModules() {
        addModule(new WorkerBuildingModule(CitizenJob.GLASSBLOWER, 1));
        addModule(new CraftingBuildingModule("glassblower", 10));
    }
}

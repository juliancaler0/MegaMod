package com.ultra.megamod.feature.citizen.building.buildings;

import com.ultra.megamod.feature.citizen.building.AbstractBuilding;
import com.ultra.megamod.feature.citizen.building.module.CraftingBuildingModule;
import com.ultra.megamod.feature.citizen.building.module.WorkerBuildingModule;

import com.ultra.megamod.feature.citizen.data.CitizenJob;

/**
 * Alchemist Tower building implementation.
 */
public class BuildingAlchemist extends AbstractBuilding {

    @Override
    public String getBuildingId() {
        return "alchemist";
    }

    @Override
    public String getDisplayName() {
        return "Alchemist Tower";
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    protected void registerModules() {
        addModule(new WorkerBuildingModule(CitizenJob.ALCHEMIST_CITIZEN, 1));
        addModule(new CraftingBuildingModule("alchemist", 10));
    }
}

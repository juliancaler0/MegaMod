package com.ultra.megamod.feature.citizen.building.buildings;

import com.ultra.megamod.feature.citizen.building.AbstractBuilding;
import com.ultra.megamod.feature.citizen.building.module.WorkerBuildingModule;

import com.ultra.megamod.feature.citizen.data.CitizenJob;

/**
 * Chicken Coop building implementation.
 */
public class BuildingChickenHerder extends AbstractBuilding {

    @Override
    public String getBuildingId() {
        return "chicken_herder";
    }

    @Override
    public String getDisplayName() {
        return "Chicken Coop";
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    protected void registerModules() {
        addModule(new WorkerBuildingModule(CitizenJob.CHICKEN_FARMER, 1));
    }
}

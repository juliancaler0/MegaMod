package com.ultra.megamod.feature.citizen.building.buildings;

import com.ultra.megamod.feature.citizen.building.AbstractBuilding;
import com.ultra.megamod.feature.citizen.building.module.WorkerBuildingModule;

import com.ultra.megamod.feature.citizen.data.CitizenJob;

/**
 * Composter's Hut building implementation.
 */
public class BuildingComposter extends AbstractBuilding {

    @Override
    public String getBuildingId() {
        return "composter";
    }

    @Override
    public String getDisplayName() {
        return "Composter's Hut";
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    protected void registerModules() {
        addModule(new WorkerBuildingModule(CitizenJob.COMPOSTER, 1));
    }
}

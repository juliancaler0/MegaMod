package com.ultra.megamod.feature.citizen.building.buildings;

import com.ultra.megamod.feature.citizen.building.AbstractBuilding;
import com.ultra.megamod.feature.citizen.building.module.WorkerBuildingModule;
import com.ultra.megamod.feature.citizen.data.CitizenJob;

/**
 * University — researchers unlock colony upgrades.
 */
public class BuildingUniversity extends AbstractBuilding {

    @Override
    public String getBuildingId() {
        return "university";
    }

    @Override
    public String getDisplayName() {
        return "University";
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    protected void registerModules() {
        addModule(new WorkerBuildingModule(CitizenJob.RESEARCHER, 1));
    }
}

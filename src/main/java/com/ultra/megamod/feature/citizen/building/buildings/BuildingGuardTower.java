package com.ultra.megamod.feature.citizen.building.buildings;

import com.ultra.megamod.feature.citizen.building.AbstractBuilding;
import com.ultra.megamod.feature.citizen.building.module.WorkerBuildingModule;
import com.ultra.megamod.feature.citizen.data.CitizenJob;

/**
 * Guard Tower — standalone defensive structure with a knight guard.
 */
public class BuildingGuardTower extends AbstractBuilding {

    @Override
    public String getBuildingId() {
        return "guard_tower";
    }

    @Override
    public String getDisplayName() {
        return "Guard Tower";
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    protected void registerModules() {
        addModule(new WorkerBuildingModule(CitizenJob.KNIGHT, 1));
    }
}

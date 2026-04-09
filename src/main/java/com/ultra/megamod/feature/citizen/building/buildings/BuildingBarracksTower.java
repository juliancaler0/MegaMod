package com.ultra.megamod.feature.citizen.building.buildings;

import com.ultra.megamod.feature.citizen.building.AbstractBuilding;
import com.ultra.megamod.feature.citizen.building.module.WorkerBuildingModule;
import com.ultra.megamod.feature.citizen.data.CitizenJob;

/**
 * Barracks Tower — a guard tower within the barracks compound.
 * Houses a knight guard.
 */
public class BuildingBarracksTower extends AbstractBuilding {

    @Override
    public String getBuildingId() {
        return "barracks_tower";
    }

    @Override
    public String getDisplayName() {
        return "Barracks Tower";
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

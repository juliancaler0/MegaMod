package com.ultra.megamod.feature.citizen.building.buildings;

import com.ultra.megamod.feature.citizen.building.AbstractBuilding;
import com.ultra.megamod.feature.citizen.building.module.WorkerBuildingModule;
import com.ultra.megamod.feature.citizen.data.CitizenJob;

/**
 * Warehouse — central storage hub for the colony.
 */
public class BuildingWarehouse extends AbstractBuilding {

    @Override
    public String getBuildingId() {
        return "warehouse";
    }

    @Override
    public String getDisplayName() {
        return "Warehouse";
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    protected void registerModules() {
        addModule(new WorkerBuildingModule(CitizenJob.WAREHOUSE_WORKER, 1));
    }
}

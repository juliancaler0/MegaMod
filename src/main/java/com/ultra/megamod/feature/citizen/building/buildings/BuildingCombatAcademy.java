package com.ultra.megamod.feature.citizen.building.buildings;

import com.ultra.megamod.feature.citizen.building.AbstractBuilding;
import com.ultra.megamod.feature.citizen.building.module.WorkerBuildingModule;
import com.ultra.megamod.feature.citizen.data.CitizenJob;

/**
 * Combat Academy — trains melee fighters.
 */
public class BuildingCombatAcademy extends AbstractBuilding {

    @Override
    public String getBuildingId() {
        return "combat_academy";
    }

    @Override
    public String getDisplayName() {
        return "Combat Academy";
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    protected void registerModules() {
        addModule(new WorkerBuildingModule(CitizenJob.COMBAT_TRAINING, 1));
    }
}

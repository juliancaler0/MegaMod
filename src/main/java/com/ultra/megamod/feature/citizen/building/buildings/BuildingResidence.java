package com.ultra.megamod.feature.citizen.building.buildings;

import com.ultra.megamod.feature.citizen.building.AbstractBuilding;
import com.ultra.megamod.feature.citizen.building.module.ResidenceBuildingModule;

/**
 * Residence — housing for citizens. Uses ResidenceBuildingModule
 * to manage citizen housing capacity based on building level.
 */
public class BuildingResidence extends AbstractBuilding {

    @Override
    public String getBuildingId() {
        return "residence";
    }

    @Override
    public String getDisplayName() {
        return "Residence";
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    protected void registerModules() {
        addModule(new ResidenceBuildingModule(1));
    }
}

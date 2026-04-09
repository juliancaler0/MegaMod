package com.ultra.megamod.feature.citizen.building.module;

/**
 * Module interface for defining core building statistics such as
 * the maximum upgrade level. Buildings typically max at level 5.
 */
public interface IDefinesCoreBuildingStatsModule extends IBuildingModule {

    /**
     * Returns the maximum level this building can be upgraded to.
     *
     * @return the max building level
     */
    int getMaxLevel();
}

package com.ultra.megamod.feature.citizen.building.module;

/**
 * Marker interface for building modules that persist their state to NBT.
 * Modules implementing this interface will have their onBuildingLoad/onBuildingSave
 * methods called during world save/load cycles.
 */
public interface IPersistentModule extends IBuildingModule {
}

package com.ultra.megamod.feature.citizen.building.module;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

/**
 * Base interface for all building modules.
 * Buildings compose multiple modules to define their behavior.
 */
public interface IBuildingModule {

    /**
     * Called when the building is loaded from NBT.
     *
     * @param tag the compound tag containing this module's persisted data
     */
    void onBuildingLoad(CompoundTag tag);

    /**
     * Called when the building is saved to NBT.
     *
     * @param tag the compound tag to write this module's data into
     */
    void onBuildingSave(CompoundTag tag);

    /**
     * Called each game tick while the building is active.
     *
     * @param level the server level the building exists in
     */
    void onBuildingTick(Level level);

    /**
     * Returns the unique string identifier for this module type.
     * Used as the NBT key prefix when persisting module data.
     *
     * @return the module id string
     */
    String getModuleId();
}

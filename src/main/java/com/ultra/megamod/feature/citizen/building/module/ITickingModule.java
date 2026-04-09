package com.ultra.megamod.feature.citizen.building.module;

import net.minecraft.world.level.Level;

/**
 * Marker interface for building modules that require per-tick updates.
 * The building will call {@link #tick(Level)} each game tick for modules
 * implementing this interface.
 */
public interface ITickingModule extends IBuildingModule {

    /**
     * Called each game tick while the building is loaded and active.
     *
     * @param level the server level
     */
    void tick(Level level);
}

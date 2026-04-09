package com.ultra.megamod.feature.citizen.entity.mc.handlers;

import net.minecraft.nbt.CompoundTag;

import java.util.List;

/**
 * Handler interface for citizen happiness tracking.
 * Ported from MineColonies' ICitizenHappinessHandler.
 */
public interface ICitizenHappinessHandler {

    /**
     * Add a happiness modifier by name and value.
     *
     * @param name  the modifier name.
     * @param value the value (-1.0 to 1.0, 0 is neutral).
     * @param duration ticks to last (0 = permanent until removed).
     */
    void addModifier(String name, double value, int duration);

    /**
     * Reset a modifier.
     */
    void resetModifier(String name);

    /**
     * Get the computed happiness of the citizen (0.0 - 10.0).
     */
    double getHappiness();

    /**
     * Process daily happiness decay.
     */
    void processDailyHappiness();

    /**
     * Read the handler from NBT.
     */
    void read(CompoundTag compound);

    /**
     * Write the handler to NBT.
     */
    void write(CompoundTag compound);

    /**
     * Get a list of all modifier names.
     */
    List<String> getModifiers();
}

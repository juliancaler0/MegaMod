package com.ultra.megamod.feature.citizen.building.module;

import net.minecraft.nbt.CompoundTag;

/**
 * Base interface for typed building settings.
 * Settings are named key-value pairs that can be persisted to NBT
 * and configured by players through the building GUI.
 *
 * @param <T> the value type of this setting
 */
public interface ISetting<T> {

    /**
     * Returns the unique key identifying this setting.
     *
     * @return the setting key
     */
    String getKey();

    /**
     * Returns the current value of this setting.
     *
     * @return the current value
     */
    T getValue();

    /**
     * Sets the value of this setting.
     *
     * @param value the new value
     */
    void setValue(T value);

    /**
     * Returns the default value of this setting.
     *
     * @return the default value
     */
    T getDefaultValue();

    /**
     * Loads this setting's value from the given NBT tag.
     *
     * @param tag the compound tag to read from
     */
    void loadFromNbt(CompoundTag tag);

    /**
     * Saves this setting's value to the given NBT tag.
     *
     * @param tag the compound tag to write to
     */
    void saveToNbt(CompoundTag tag);
}

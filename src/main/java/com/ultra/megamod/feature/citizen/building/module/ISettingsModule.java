package com.ultra.megamod.feature.citizen.building.module;

import java.util.Map;

/**
 * Module interface for buildings with configurable settings.
 * Provides generic key-value storage for building configuration options.
 */
public interface ISettingsModule extends IBuildingModule {

    /**
     * Retrieves a setting value by key, returning the default if not set.
     *
     * @param key          the setting key
     * @param defaultValue the value to return if the key is not found
     * @param <T>          the setting value type
     * @return the setting value or the default
     */
    <T> T getSetting(String key, T defaultValue);

    /**
     * Sets a setting value.
     *
     * @param key   the setting key
     * @param value the value to store
     * @param <T>   the setting value type
     */
    <T> void setSetting(String key, T value);

    /**
     * Returns all current settings as an unmodifiable map.
     *
     * @return map of all setting keys to their values
     */
    Map<String, Object> getAllSettings();
}

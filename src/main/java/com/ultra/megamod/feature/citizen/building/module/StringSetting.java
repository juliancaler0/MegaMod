package com.ultra.megamod.feature.citizen.building.module;

import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * A string setting for building modules with an optional list of allowed values.
 */
public class StringSetting implements ISetting<String> {

    private final String key;
    private String value;
    private final String defaultValue;
    private final List<String> allowedValues;

    /**
     * Creates a string setting with no value restrictions.
     *
     * @param key          the setting key
     * @param defaultValue the default value
     */
    public StringSetting(String key, String defaultValue) {
        this(key, defaultValue, null);
    }

    /**
     * Creates a string setting with an optional list of allowed values.
     *
     * @param key           the setting key
     * @param defaultValue  the default value
     * @param allowedValues the allowed values, or null for unrestricted
     */
    public StringSetting(String key, String defaultValue, @Nullable List<String> allowedValues) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.allowedValues = allowedValues != null ? List.copyOf(allowedValues) : Collections.emptyList();
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        if (allowedValues.isEmpty() || allowedValues.contains(value)) {
            this.value = value;
        }
    }

    @Override
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * Returns the list of allowed values. Empty list means unrestricted.
     *
     * @return the allowed values list
     */
    public List<String> getAllowedValues() {
        return allowedValues;
    }

    @Override
    public void loadFromNbt(CompoundTag tag) {
        String raw = tag.getStringOr(key, defaultValue);
        if (allowedValues.isEmpty() || allowedValues.contains(raw)) {
            this.value = raw;
        } else {
            this.value = defaultValue;
        }
    }

    @Override
    public void saveToNbt(CompoundTag tag) {
        tag.putString(key, value);
    }

    @Override
    public String toString() {
        return "StringSetting{key='" + key + "', value='" + value + "'}";
    }
}

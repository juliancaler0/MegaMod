package com.ultra.megamod.feature.citizen.building.module;

import net.minecraft.nbt.CompoundTag;

/**
 * A boolean toggle setting for building modules.
 */
public class BoolSetting implements ISetting<Boolean> {

    private final String key;
    private Boolean value;
    private final Boolean defaultValue;

    public BoolSetting(String key, boolean defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public Boolean getValue() {
        return value;
    }

    @Override
    public void setValue(Boolean value) {
        this.value = value;
    }

    @Override
    public Boolean getDefaultValue() {
        return defaultValue;
    }

    @Override
    public void loadFromNbt(CompoundTag tag) {
        this.value = tag.getBooleanOr(key, defaultValue);
    }

    @Override
    public void saveToNbt(CompoundTag tag) {
        tag.putBoolean(key, value);
    }

    @Override
    public String toString() {
        return "BoolSetting{key='" + key + "', value=" + value + "}";
    }
}

package com.ultra.megamod.feature.citizen.building.module;

import net.minecraft.nbt.CompoundTag;

/**
 * An integer value setting for building modules with min/max bounds.
 */
public class IntSetting implements ISetting<Integer> {

    private final String key;
    private Integer value;
    private final Integer defaultValue;
    private final int min;
    private final int max;

    public IntSetting(String key, int defaultValue, int min, int max) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.min = min;
        this.max = max;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public void setValue(Integer value) {
        this.value = Math.max(min, Math.min(max, value));
    }

    @Override
    public Integer getDefaultValue() {
        return defaultValue;
    }

    /**
     * Returns the minimum allowed value.
     *
     * @return the minimum bound
     */
    public int getMin() {
        return min;
    }

    /**
     * Returns the maximum allowed value.
     *
     * @return the maximum bound
     */
    public int getMax() {
        return max;
    }

    @Override
    public void loadFromNbt(CompoundTag tag) {
        int raw = tag.getIntOr(key, defaultValue);
        this.value = Math.max(min, Math.min(max, raw));
    }

    @Override
    public void saveToNbt(CompoundTag tag) {
        tag.putInt(key, value);
    }

    @Override
    public String toString() {
        return "IntSetting{key='" + key + "', value=" + value + ", range=[" + min + "," + max + "]}";
    }
}

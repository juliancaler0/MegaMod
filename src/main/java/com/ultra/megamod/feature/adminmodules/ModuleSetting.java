package com.ultra.megamod.feature.adminmodules;

import java.util.List;

public abstract class ModuleSetting<T> {
    private final String name;
    private final String description;
    private T value;

    protected ModuleSetting(String name, T defaultValue, String description) {
        this.name = name;
        this.value = defaultValue;
        this.description = description;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public T getValue() { return value; }
    public void setValue(T value) { this.value = value; }
    public abstract String getType();
    public abstract String serializeValue();
    public abstract void deserializeValue(String raw);

    public static class BoolSetting extends ModuleSetting<Boolean> {
        public BoolSetting(String name, boolean defaultValue, String description) {
            super(name, defaultValue, description);
        }
        public String getType() { return "bool"; }
        public String serializeValue() { return getValue().toString(); }
        public void deserializeValue(String raw) { setValue(Boolean.parseBoolean(raw)); }
    }

    public static class IntSetting extends ModuleSetting<Integer> {
        private final int min, max;
        public IntSetting(String name, int defaultValue, int min, int max, String description) {
            super(name, defaultValue, description);
            this.min = min;
            this.max = max;
        }
        public int getMin() { return min; }
        public int getMax() { return max; }
        public String getType() { return "int"; }
        public String serializeValue() { return getValue().toString(); }
        public void deserializeValue(String raw) { setValue(Math.max(min, Math.min(max, Integer.parseInt(raw)))); }
    }

    public static class DoubleSetting extends ModuleSetting<Double> {
        private final double min, max;
        public DoubleSetting(String name, double defaultValue, double min, double max, String description) {
            super(name, defaultValue, description);
            this.min = min;
            this.max = max;
        }
        public double getMin() { return min; }
        public double getMax() { return max; }
        public String getType() { return "double"; }
        public String serializeValue() { return String.format("%.2f", getValue()); }
        public void deserializeValue(String raw) { setValue(Math.max(min, Math.min(max, Double.parseDouble(raw)))); }
    }

    public static class EnumSetting extends ModuleSetting<String> {
        private final List<String> options;
        public EnumSetting(String name, String defaultValue, List<String> options, String description) {
            super(name, defaultValue, description);
            this.options = options;
        }
        public List<String> getOptions() { return options; }
        public String getType() { return "enum"; }
        public String serializeValue() { return getValue(); }
        public void deserializeValue(String raw) { if (options.contains(raw)) setValue(raw); }
    }

    public static class KeybindSetting extends ModuleSetting<String> {
        public KeybindSetting(String name, String defaultKey, String description) {
            super(name, defaultKey, description);
        }
        public String getType() { return "keybind"; }
        public String serializeValue() { return getValue(); }
        public void deserializeValue(String raw) { setValue(raw); }
    }
}

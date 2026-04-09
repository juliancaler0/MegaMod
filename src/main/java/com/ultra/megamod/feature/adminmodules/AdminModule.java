package com.ultra.megamod.feature.adminmodules;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.ArrayList;
import java.util.List;

public abstract class AdminModule {
    private final String id;
    private final String name;
    private final String description;
    private final ModuleCategory category;
    private boolean enabled = false;
    private String toggleKey = "NONE";
    private final List<ModuleSetting<?>> settings = new ArrayList<>();

    protected AdminModule(String id, String name, String description, ModuleCategory category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        initSettings();
    }

    protected void initSettings() {}

    // Lifecycle methods - override as needed
    public void onEnable(ServerPlayer player) {}
    public void onDisable(ServerPlayer player) {}
    public void onServerTick(ServerPlayer player, ServerLevel level) {}
    public void onDamage(ServerPlayer player, LivingDamageEvent.Pre event) {}
    public void onBreakSpeed(ServerPlayer player, net.neoforged.neoforge.event.entity.player.PlayerEvent.BreakSpeed event) {}
    public void onRenderWorld(Object event) {} // Client-side render, passed as Object to avoid client class loading on server

    // Identification
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public ModuleCategory getCategory() { return category; }

    // State
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public void toggle(ServerPlayer player) {
        this.enabled = !this.enabled;
        if (this.enabled) {
            onEnable(player);
        } else {
            onDisable(player);
        }
    }

    // Settings
    public List<ModuleSetting<?>> getSettings() { return settings; }

    protected ModuleSetting.BoolSetting bool(String name, boolean defaultValue, String description) {
        ModuleSetting.BoolSetting s = new ModuleSetting.BoolSetting(name, defaultValue, description);
        settings.add(s);
        return s;
    }

    protected ModuleSetting.IntSetting integer(String name, int defaultValue, int min, int max, String description) {
        ModuleSetting.IntSetting s = new ModuleSetting.IntSetting(name, defaultValue, min, max, description);
        settings.add(s);
        return s;
    }

    protected ModuleSetting.DoubleSetting decimal(String name, double defaultValue, double min, double max, String description) {
        ModuleSetting.DoubleSetting s = new ModuleSetting.DoubleSetting(name, defaultValue, min, max, description);
        settings.add(s);
        return s;
    }

    protected ModuleSetting.EnumSetting enumVal(String name, String defaultValue, List<String> options, String description) {
        ModuleSetting.EnumSetting s = new ModuleSetting.EnumSetting(name, defaultValue, options, description);
        settings.add(s);
        return s;
    }

    protected ModuleSetting.KeybindSetting keybind(String name, String defaultKey, String description) {
        ModuleSetting.KeybindSetting s = new ModuleSetting.KeybindSetting(name, defaultKey, description);
        settings.add(s);
        return s;
    }

    // Toggle keybind
    public String getToggleKey() { return toggleKey; }
    public void setToggleKey(String key) { this.toggleKey = key; }

    public ModuleSetting<?> getSetting(String name) {
        for (ModuleSetting<?> s : settings) {
            if (s.getName().equals(name)) return s;
        }
        return null;
    }

    public boolean isServerSide() { return true; }
    public boolean isClientSide() { return false; }

    // JSON serialization for network
    public String toJson() {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"id\":\"").append(id).append("\"");
        sb.append(",\"name\":\"").append(name).append("\"");
        sb.append(",\"desc\":\"").append(description.replace("\"", "\\\"")).append("\"");
        sb.append(",\"category\":\"").append(category.name()).append("\"");
        sb.append(",\"enabled\":").append(enabled);
        sb.append(",\"toggleKey\":\"").append(toggleKey).append("\"");
        sb.append(",\"settings\":[");
        boolean first = true;
        for (ModuleSetting<?> s : settings) {
            if (!first) sb.append(",");
            first = false;
            sb.append("{\"name\":\"").append(s.getName()).append("\"");
            sb.append(",\"type\":\"").append(s.getType()).append("\"");
            sb.append(",\"value\":\"").append(s.serializeValue().replace("\"", "\\\"")).append("\"");
            sb.append(",\"desc\":\"").append(s.getDescription().replace("\"", "\\\"")).append("\"");
            if (s instanceof ModuleSetting.IntSetting is) {
                sb.append(",\"min\":").append(is.getMin()).append(",\"max\":").append(is.getMax());
            } else if (s instanceof ModuleSetting.DoubleSetting ds) {
                sb.append(",\"min\":").append(ds.getMin()).append(",\"max\":").append(ds.getMax());
            } else if (s instanceof ModuleSetting.EnumSetting es) {
                sb.append(",\"options\":[");
                boolean of = true;
                for (String o : es.getOptions()) {
                    if (!of) sb.append(",");
                    of = false;
                    sb.append("\"").append(o).append("\"");
                }
                sb.append("]");
            }
            sb.append("}");
        }
        sb.append("]}");
        return sb.toString();
    }
}

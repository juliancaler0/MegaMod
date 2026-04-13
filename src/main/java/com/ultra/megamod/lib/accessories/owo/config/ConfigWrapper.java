package com.ultra.megamod.lib.accessories.owo.config;

/**
 * Adapter for io.wispforest.owo.config.ConfigWrapper.
 * Stub replacement - real config uses NeoForge ModConfigSpec or static fields.
 */
public abstract class ConfigWrapper<T> {

    public abstract T instance();

    public void save() {
        // No-op in adapter
    }
}

package com.ultra.megamod.lib.spellengine.api.spell.container;

import java.util.List;

/**
 * Templates for creating spell containers.
 * Config-driven system simplified for NeoForge port.
 */
public class SpellContainerTemplates {
    public static class Config {
        public SpellContainer spell_book = new SpellContainer(SpellContainer.ContentType.NONE, "", null, "", 0, List.of());
    }

    public static Config defaults() {
        return new Config();
    }

    // Simplified config - no longer uses tiny_config ConfigManager
    public static Config config = defaults();

    public static Config getConfig() {
        return config;
    }
}

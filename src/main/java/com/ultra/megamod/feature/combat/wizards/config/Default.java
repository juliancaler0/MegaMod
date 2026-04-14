package com.ultra.megamod.feature.combat.wizards.config;

import com.ultra.megamod.lib.spellengine.api.config.ConfigFile;

/**
 * Default item configuration for Wizards content.
 * Mirrors the Paladins {@code Default} pattern.
 */
public class Default {
    public static final ConfigFile.Equipment itemConfig;
    static {
        itemConfig = new ConfigFile.Equipment();
    }
}

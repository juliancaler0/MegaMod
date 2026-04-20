package com.ultra.megamod.feature.combat.relics.util;

import com.ultra.megamod.feature.combat.relics.config.ItemConfig;

/**
 * Placeholder for the ref mod's tiny_config-based config defaults.
 * Kept as a reserved name so RelicItems can reference it; all default values are
 * currently inlined into RelicItems.Entry.config(...) calls (matching the ref mod's
 * defaults_v2 JSON shipped in the Ref Code source).
 */
public final class RelicDefaults {
    private RelicDefaults() {}
    public static ItemConfig emptyConfig() { return new ItemConfig(); }
}

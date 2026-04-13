package com.ultra.megamod.lib.spellengine.rpg_series.client;

import com.ultra.megamod.lib.spellengine.client.gui.SpellTooltip;
import com.ultra.megamod.lib.spellengine.rpg_series.datagen.WeaponSkills;

public class RPGSeriesCoreClient {
    public static void init() {
        for (var entry: WeaponSkills.entries) {
            if (entry.mutator() != null) {
                SpellTooltip.addDescriptionMutator(entry.id(), entry.mutator());
            }
        }
    }
}

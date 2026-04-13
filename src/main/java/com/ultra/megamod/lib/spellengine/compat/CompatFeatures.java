package com.ultra.megamod.lib.spellengine.compat;

import com.ultra.megamod.lib.spellengine.compat.container.ContainerCompat;

public class CompatFeatures {
    public static void initialize() {
        ContainerCompat.init();
        CombatRollCompat.init();
        MeleeCompat.init();
        CriticalStrikeCompat.init();
        FTBTeamsCompat.init();
    }
}

package net.relics_rpgs.fabric.compat;

import net.relics_rpgs.fabric.compat.trinkets.TrinketsCompat;
import net.relics_rpgs.compat.AccessoriesCompat;
import net.spell_engine.fabric.compat.FabricCompatFeatures;

public class CompatFeatures {
    public static void init() {
        var id = FabricCompatFeatures.initSlotCompat();
        if ("trinkets".equals(id)) {
            TrinketsCompat.init();
        } else if ("accessories".equals(id)) {
            AccessoriesCompat.init();
        }
    }
}

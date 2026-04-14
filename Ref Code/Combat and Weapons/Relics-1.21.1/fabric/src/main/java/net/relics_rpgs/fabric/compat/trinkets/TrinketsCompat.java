package net.relics_rpgs.fabric.compat.trinkets;

import net.fabricmc.loader.api.FabricLoader;
import net.relics_rpgs.item.RelicFactory;

public class TrinketsCompat {
    public static void init() {
        if (FabricLoader.getInstance().isModLoaded("trinkets")) {
            // Outsource to avoid class loading issues
            TrinketsHelper.registerFactory();
        }
    }
}
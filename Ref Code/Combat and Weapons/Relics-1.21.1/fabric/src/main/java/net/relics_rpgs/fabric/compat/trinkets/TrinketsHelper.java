package net.relics_rpgs.fabric.compat.trinkets;

import net.relics_rpgs.item.RelicFactory;

public class TrinketsHelper {
    public static void registerFactory() {
        RelicFactory.factory = args -> new RelicTrinketItem(args.settings(), args.attributes());
    }
}

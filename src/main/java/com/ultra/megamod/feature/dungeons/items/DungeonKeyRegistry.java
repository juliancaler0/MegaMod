package com.ultra.megamod.feature.dungeons.items;

import com.ultra.megamod.feature.dungeons.loot.DungeonExclusiveItems;
import net.neoforged.bus.api.IEventBus;

public class DungeonKeyRegistry {

    public static void init(IEventBus modBus) {
        DungeonExclusiveItems.init(modBus);
    }
}

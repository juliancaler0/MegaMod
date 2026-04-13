package com.ultra.megamod.feature.combat.paladins.item;

import com.ultra.megamod.feature.combat.paladins.item.armor.Armors;
import net.minecraft.world.item.Item;

import java.util.HashMap;

public class PaladinItems {
    public static final HashMap<String, Item> entries;
    static {
        entries = new HashMap<>();
        for (var weaponEntry : PaladinWeapons.entries) {
            entries.put(weaponEntry.id().toString(), weaponEntry.item());
        }
        for (var entry : Armors.entries) {
            var set = entry.armorSet();
            for (var piece : set.pieces()) {
                entries.put(set.idOf((Item) piece).toString(), (Item) piece);
            }
        }
    }
}
